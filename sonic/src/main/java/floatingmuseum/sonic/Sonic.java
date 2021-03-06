package floatingmuseum.sonic;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import floatingmuseum.sonic.db.DBManager;
import floatingmuseum.sonic.entity.DownloadRequest;
import floatingmuseum.sonic.entity.TaskInfo;
import floatingmuseum.sonic.listener.TaskListener;
import floatingmuseum.sonic.utils.FileUtil;
import floatingmuseum.sonic.utils.ListUtil;
import floatingmuseum.sonic.utils.LogUtil;


/**
 * Created by Floatingmuseum on 2017/3/16.
 */

public class Sonic {

    private static final String TAG = Sonic.class.getName();

//    public enum State{
//        NONE,
//        START,
//        WAITING,
//        PAUSE,
//        DOWNLOADING,
//        ERROR,
//        FINISH,
//        CANCEL
//    }

    public static final int STATE_NONE = 0;
    public static final int STATE_START = 6;
    public static final int STATE_WAITING = 1;
    public static final int STATE_PAUSE = 2;
    public static final int STATE_DOWNLOADING = 3;
    public static final int STATE_ERROR = 4;
    public static final int STATE_FINISH = 5;
    public static final int STATE_CANCEL = 7;

    public static final int FORCE_START_NO = 0;
    public static final int FORCE_START_YES = 1;

    public static final String EXTRA_DOWNLOAD_TASK_INFO = "actionDownloadTaskInfo";
    public static final String EXTRA_DOWNLOAD_EXCEPTION = "actionDownloadException";

    private static Context context;
//    private static Sonic sonic;

    private int activeTaskNumber = 3;

    private Map<String, TaskInfo> allTaskInfo;
    private Map<String, DownloadTask> activeTasks;
    private List<DownloadTask> waitingTasks;
    private Map<String, DownloadTask> forceStartTasks;
    private DBManager dbManager;
    private TaskConfig taskConfig = new TaskConfig();
    private ExecutorService threadsPool;
    private BroadcastManager manager;
    private String broadcastAction;

    private static class Holder {
        private static final Sonic INSTANCE = new Sonic();
    }

    private Sonic() {
    }

    public void init(Context applicationContext) {
        Log.d("SonicVersion", "ver=" + BuildConfig.VERSION_NAME);
        context = applicationContext;

        LogUtil.i(TAG, "init()...PackageName:" + context.getPackageName());
        LogUtil.i(TAG, "init()...Download dir path:" + taskConfig.getDirPath());

        dbManager = new DBManager(context);
        if (TextUtils.isEmpty(broadcastAction)) {
            manager = new BroadcastManager(context, context.getPackageName());
        } else {
            manager = new BroadcastManager(context, broadcastAction);
        }

        List<TaskInfo> allTask = dbManager.getAllDownloadTask();
        allTaskInfo = new HashMap<>();
        activeTasks = new HashMap<>();
        waitingTasks = new ArrayList<>();
        forceStartTasks = new HashMap<>();
        threadsPool = Executors.newCachedThreadPool();
        initAllTasks(allTask);
    }

    private void initAllTasks(List<TaskInfo> tasks) {
        if (!ListUtil.isEmpty(tasks)) {
            for (TaskInfo taskInfo : tasks) {
                LogUtil.i(TAG, "init()...tasks exist in db:" + taskInfo.toString());
                if (taskInfo.getState() == STATE_DOWNLOADING) {//if task state is downloading，this may cause app crashed or device rebooted when task downloading。
                    taskInfo.setState(STATE_PAUSE);
                }
                allTaskInfo.put(taskInfo.getTag(), taskInfo);
            }
        }
    }

    public static Context getContext() {
        if (context == null) {
            throw new IllegalStateException("You lost Sonic.init() in your Application.");
        }
        return context;
    }

    public static Sonic getInstance() {
//        LogUtil.i(TAG, "getInstance()...Instance:" + sonic);
//        if (sonic == null) {
//            synchronized (Sonic.class) {
//                if (sonic == null) {
//                    sonic = new Sonic();
//                }
//            }
//        }
//        return sonic;
        return Holder.INSTANCE;
    }

    public TaskConfig getTaskConfig() {
        return taskConfig;
    }

    /**
     * <p>How many tasks can running at the same time.
     * <p>default is 3.
     */
    public Sonic setActiveTaskNumber(int activeTaskNumber) {
        if (activeTaskNumber < 1) {
            this.activeTaskNumber = 1;
        } else {
            this.activeTaskNumber = activeTaskNumber;
        }
        return this;
    }

    /**
     * <p>How many threads working for a task.
     * <p>default is 3.
     */
    public Sonic setMaxThreads(int maxThreads) {
        if (maxThreads < 1) {
            taskConfig.setMaxThreads(1);
        } else {
            taskConfig.setMaxThreads(maxThreads);
        }
        return this;
    }

    /**
     * <p>DownloadListener onProgress() method will be call at defined interval.
     * <p>the milliseconds must between 0 to 1000.
     * <p>the default for the interval is 500 milliseconds.
     */
    public Sonic setProgressResponseInterval(int progressResponseInterval) {
        if (progressResponseInterval < 0) {
            taskConfig.setProgressResponseInterval(0);
        } else if (progressResponseInterval > 1000) {
            taskConfig.setProgressResponseInterval(1000);
        } else {
            taskConfig.setProgressResponseInterval(progressResponseInterval);
        }
        return this;
    }

    /**
     * Task will retry when error happen
     * default is 5;
     */
    public Sonic setRetryTime(int retryTime) {
        if (retryTime >= 0) {
            taskConfig.setRetryTime(retryTime);
        }
        return this;
    }

    /**
     * default is 5000 milliseconds.
     */
    public Sonic setReadTimeout(int readTimeout) {
        if (readTimeout > 0) {
            taskConfig.setReadTimeout(readTimeout);
        }
        return this;
    }

    /**
     * default is 5000 milliseconds.
     */
    public Sonic setConnectTimeout(int connectTimeout) {
        if (connectTimeout > 0) {
            taskConfig.setConnectTimeout(connectTimeout);
        }
        return this;
    }

    /**
     * <p>Storage dir path.
     * <p>default is sdcard/downloads
     */
    public Sonic setDirPath(String dirPath) {
        if (dirPath != null && !"".equals(dirPath)) {
            File dir = new File(dirPath);
            if (!dir.exists()) {
                dir.mkdir();
            }
            taskConfig.setDirPath(dirPath);
        }
        return this;
    }

    /**
     * Use LocalBroadcastManager register receiver where your want receive download info
     * Default action is your app's package name
     */
    public Sonic setBroadcastAction(String action) {
        broadcastAction = action;
        return this;
    }

    /**
     * If you want see All the log from this library.
     */
    public Sonic setLogEnabled() {
        LogUtil.enabled(true);
        return this;
    }

    private Sonic setStopServiceAfterAllTaskFinished(boolean stopServiceAfterAllTaskFinished) {
//        this.stopServiceAfterAllTaskFinished = stopServiceAfterAllTaskFinished;
        return this;
    }

    /**
     * downloadUrl will be the tag for this task.
     */
    public void addTask(String downloadUrl) {
        addTask(downloadUrl, downloadUrl);
    }

    public void addTask(String downloadUrl, String tag) {
        addTask(downloadUrl, tag, FileUtil.getUrlFileName(downloadUrl));
    }

    public void addTask(String downloadUrl, String tag, String fileName) {
        DownloadRequest request = new DownloadRequest().setUrl(downloadUrl)
                .setTag(tag)
                .setFileName(fileName)
                .setDirPath(taskConfig.getDirPath());
        addTask(request);
    }

    public void addTask(DownloadRequest request) {
        if (allTaskInfo.containsKey(request.getTag())) {
            TaskInfo taskInfo = allTaskInfo.get(request.getTag());
            initDownload(taskInfo, true, request);
        } else {
            TaskConfig config = getFinalTaskConfig(request.getTag(), request);
            //a downloading file has .sonic-downloading extension，it will rename to original after download finished，
            TaskInfo taskInfo = new TaskInfo(request.getUrl(), request.getTag(), request.getFileName(), request.getDirPath(), request.getDirPath() + "/" + request.getFileName() + ".sonic-downloading", 0, 0, 0, 0, 0, config);
            initDownload(taskInfo, false, request);
        }
    }

    private void addAllTask() {
    }

    public TaskInfo getTaskInfo(String tag) {
        return allTaskInfo.get(tag);
    }

    public Map<String, TaskInfo> getAllTaskInfo() {
        return allTaskInfo;
    }

    private TaskConfig getFinalTaskConfig(String tag, DownloadRequest request) {
        TaskConfig existTaskConfig = dbManager.queryTaskConfig(tag);
        if (existTaskConfig != null) {
            LogUtil.i(TAG, "getFinalTaskConfig()...config exist in db:" + existTaskConfig.toString());
            return existTaskConfig;
        } else if (request.isCustomTaskConfig()) {
            TaskConfig customTaskConfig = request.getTaskConfig();
            dbManager.insertTaskConfig(tag, customTaskConfig);
            LogUtil.i(TAG, "getFinalTaskConfig()...save config:" + customTaskConfig.toString() + "..." + existTaskConfig);
            return customTaskConfig;
        } else {
            LogUtil.i(TAG, "getFinalTaskConfig()...use default config:" + taskConfig.toString() + "..." + existTaskConfig);
            return taskConfig;
        }
    }

    private void initDownload(TaskInfo taskInfo, boolean isExist, DownloadRequest request) {
        LogUtil.i(TAG, "initDownload()...TaskInfo:" + taskInfo.toString() + "...forceStart:" + request.getForceStart() + "...isExist:" + isExist);
        TaskConfig finalTaskConfig = taskInfo.getTaskConfig();
        if (!isExist) {
            dbManager.insertTaskInfo(taskInfo);
            allTaskInfo.put(taskInfo.getTag(), taskInfo);
        }

        if (isForceStart(taskInfo, finalTaskConfig)) {
            return;
        }
        //已处于普通下载列表中的应用，忽略掉此次添加
        if (activeTasks.containsKey(taskInfo.getTag())) {
            LogUtil.e(TAG, "initDownload()...found same task in activeTasks,ignore init:" + taskInfo);
            return;
        }
        //已处于等待下载列表中的应用，忽略掉此次添加
        for (DownloadTask waitingTask : waitingTasks) {
            if (taskInfo.getTag().equals(waitingTask.getTaskInfo().getTag())) {
                LogUtil.e(TAG, "initDownload()...found same task in waitingTasks,ignore init:" + taskInfo);
                return;
            }
        }
        //已处于强制下载列表中的应用，忽略掉此次添加
        if (forceStartTasks.containsKey(taskInfo.getTag())) {
            LogUtil.e(TAG, "initDownload()...found same task in forceStartTasks,ignore init:" + taskInfo);
            return;
        }

        //可下载列表还有空位
        if (activeTasks.size() < activeTaskNumber) {
            DownloadTask downloadTask = new DownloadTask(taskInfo, dbManager, finalTaskConfig, threadsPool, taskListener);
            LogUtil.i(TAG, "initDownload()...into active queue...task:" + taskInfo);
            activeTasks.put(taskInfo.getTag(), downloadTask);
            downloadTask.start();
        }
        //可下载列表已满，进入等待页面
        else {
            taskInfo.setState(Sonic.STATE_WAITING);
            LogUtil.i(TAG, "initDownload()...into waiting queue...task:" + taskInfo);
            DownloadTask downloadTask = new DownloadTask(taskInfo, dbManager, finalTaskConfig, threadsPool, taskListener);
            waitingTasks.add(downloadTask);
            manager.sendBroadcast(STATE_WAITING, taskInfo, null);
        }

        LogUtil.i(TAG, "initDownload()...MaxActiveTaskNumber:" + activeTaskNumber + "...CurrentDownloadTaskNumber:" + activeTasks.size() + "...CurrentWaitingTaskNumber:" + waitingTasks.size() + "...ForceDownloadTaskNumber:" + forceStartTasks.size());
    }

    private boolean isForceStart(TaskInfo taskInfo, TaskConfig finalTaskConfig) {
        LogUtil.i(TAG, "isForceStart:" + finalTaskConfig.toString());
        if (finalTaskConfig.getForceStart() == FORCE_START_YES && !forceStartTasks.containsKey(taskInfo.getTag())) {
            DownloadTask existTask = forceStartTasks.get(taskInfo.getTag());
            LogUtil.i(TAG, "initDownload()...Name:" + taskInfo.getName() + "...force start download...contains:" + (existTask == null ? null : existTask.hashCode()));
            if (!forceStartTasks.containsKey(taskInfo.getTag())) {
                DownloadTask downloadTask = new DownloadTask(taskInfo, dbManager, finalTaskConfig, threadsPool, taskListener);
                LogUtil.i(TAG, "initDownload()...Name:" + taskInfo.getName() + "...into force start queue...hashcodeID:" + downloadTask.hashCode() + "...tag:" + taskInfo.getTag());
                forceStartTasks.put(taskInfo.getTag(), downloadTask);
                downloadTask.start();
            }
            return true;
        } else {
            return false;
        }
    }

    public void pauseTask(String tag) {
        // TODO: 2017/5/10 pause has delay
        LogUtil.i(TAG, "暂停DownloadTask...activeTasks:" + activeTasks.size() + "..." + forceStartTasks.size() + "..." + waitingTasks.size());
        if (activeTasks.containsKey(tag)) {
            LogUtil.i(TAG, "暂停DownloadTask...activeTasks:" + activeTasks.size() + "..." + activeTasks.get(tag).hashCode());
            LogUtil.i(TAG, "pauseTask()...activeTasks:" + activeTasks.size() + "...hashcode:" + activeTasks.get(tag) + "..." + tag);
            activeTasks.get(tag).stop();
        } else if (forceStartTasks.containsKey(tag)) {
            LogUtil.i(TAG, "暂停DownloadTask...forceTasks:" + forceStartTasks.size() + "..." + forceStartTasks.get(tag).hashCode());
            LogUtil.i(TAG, "pauseTask()...forceStartTasks:" + forceStartTasks.size() + "..." + tag);
            forceStartTasks.get(tag).stop();
        } else {
            LogUtil.i(TAG, "pauseTask()...waitingTasks:" + waitingTasks.size() + "..." + tag);
            for (DownloadTask waitingTask : waitingTasks) {
                TaskInfo taskInfo = waitingTask.getTaskInfo();
                LogUtil.i(TAG, "pauseTask()...waitingTasks:" + taskInfo.getName());
                if (taskInfo.getTag().equals(tag)) {
                    LogUtil.i(TAG, "暂停DownloadTask...waitingTasks:" + waitingTask.hashCode() + "...tag:" + tag);
                    Log.i(TAG, "pauseTask()...waitingTasks:" + taskInfo.getName());
                    taskInfo.setState(STATE_PAUSE);
                    manager.sendBroadcast(STATE_PAUSE, taskInfo, null);
                    waitingTasks.remove(waitingTask);
                    return;
                }
            }
            // TODO: 2018/4/20 monkey测试后，有的应用按钮处于暂停状态，点击后，执行暂停，却无法在下载队列中找到task，可能因程序崩溃等原因发生

            TaskInfo taskInfo = allTaskInfo.get(tag);
            if (taskInfo != null) {
                LogUtil.i(TAG, "pauseTask()...from allTaskInfo:" + waitingTasks.size() + "..." + tag);
                taskInfo.setState(STATE_PAUSE);
                manager.sendBroadcast(STATE_PAUSE, taskInfo, null);
                return;
            }

            LogUtil.i(TAG, "暂停DownloadTask...未找到:");
            LogUtil.i(TAG, "Which task that you want stop,doesn't exist.");
        }
    }

    public void pauseAllTask() {
//        //remove all task from waitingTask
//        for (DownloadTask waitingTask : waitingTasks) {
//            TaskInfo taskInfo = waitingTask.getTaskInfo();
//            taskInfo.setState(STATE_PAUSE);
//            manager.sendBroadcast(STATE_PAUSE, taskInfo, null);
//        }
//        waitingTasks.clear();
//
//        //stop all download task
//        for (String key : activeTasks.keySet()) {
//            DownloadTask downloadTask = activeTasks.get(key);
//            downloadTask.stop();
//        }
//
//        // stop all force download task
//        for (String key : forceStartTasks.keySet()) {
//            DownloadTask downloadTask = forceStartTasks.get(key);
//            downloadTask.stop();
//        }

        LogUtil.d(TAG, "pauseAllTask()");

        pauseAllNormalTask();
        pauseAllForceTask();
    }

    public void pauseAllNormalTask() {
        LogUtil.d(TAG, "pauseAllNormalTask()");
        for (DownloadTask task : waitingTasks) {
            if (task.getConfig().getForceStart() == FORCE_START_NO) {
                LogUtil.d(TAG, "pauseAllNormalTask()...pause waiting normal " + task.getTaskInfo().getName());
                task.getTaskInfo().setState(STATE_PAUSE);
                manager.sendBroadcast(STATE_PAUSE, task.getTaskInfo(), null);
            }
        }

        waitingTasks.clear();

        for (String key : activeTasks.keySet()) {
            DownloadTask downloadTask = activeTasks.get(key);
            LogUtil.d(TAG, "pauseAllNormalTask()...pause downloading normal " + downloadTask.getTaskInfo().getName());
            downloadTask.stop();
        }
    }

    public void pauseAllForceTask() {
        LogUtil.d(TAG, "pauseAllForceTask()");
        for (String key : forceStartTasks.keySet()) {
            DownloadTask downloadTask = forceStartTasks.get(key);
            LogUtil.d(TAG, "pauseAllForceTask()...pause downloading force " + downloadTask.getTaskInfo().getName());
            downloadTask.stop();
        }
    }

    /**
     * <p>CancelTask will remove all information from database about this task.also delete file from sdcard.
     * <p>if this task downloaded by using addTask(url),so the url is the tag.
     */
    public void cancelTask(String tag) {
        // TODO: 2017/5/10 tell user cancel result immediately.then stop task,remove task on the background.
        LogUtil.d(TAG, "cancelTask...tag:" + tag);
        if (activeTasks.containsKey(tag)) {
            DownloadTask task = activeTasks.get(tag);
            LogUtil.d(TAG, "cancelTask...activeTasks...hashcode:" + task.hashCode() + "...tag:" + tag);
            task.cancel();
            return;
        }

        if (forceStartTasks.containsKey(tag)) {
            DownloadTask task = forceStartTasks.get(tag);
            LogUtil.d(TAG, "cancelTask...forceTasks...hashcode:" + task.hashCode() + "...tag:" + tag);
            task.cancel();
            return;
        }

        for (DownloadTask waitingTask : waitingTasks) {
            LogUtil.d(TAG, "cancelTask...waitingTasks...tag:" + tag);
            if (tag.equals(waitingTask.getTaskInfo().getTag())) {
                LogUtil.d(TAG, "cancelTask...waitingTasks...hash:+" + waitingTask.hashCode() + "...tag:" + tag);
                waitingTask.cancelTask();
                waitingTasks.remove(waitingTask);
                allTaskInfo.remove(waitingTask.getTaskInfo());
                return;
            }
        }

        if (allTaskInfo.containsKey(tag)) {
            LogUtil.d(TAG, "cancelTask...allTaskInfo...tag:" + tag);
            TaskInfo taskInfo = allTaskInfo.get(tag);
            taskInfo.setProgress(0);
            taskInfo.setCurrentSize(0);
            taskInfo.setTotalSize(0);
            taskInfo.setState(Sonic.STATE_CANCEL);
            dbManager.delete(taskInfo);
            FileUtil.deleteFile(taskInfo.getFilePath());
            taskListener.onCancel(taskInfo, -1);
            return;
        }
        LogUtil.i(TAG, "Which task that you want cancel,doesn't exist." + tag);
    }

    private void cancelAllTask() {
    }

    private void handleExceptionType(TaskInfo taskInfo, DownloadException downloadException) {
        if (downloadException.getExceptionType() == DownloadException.TYPE_MALFORMED_URL) {
            allTaskInfo.remove(taskInfo.getTag());
        }
    }

    /**
     * remove task from activeTasks and forceTasks,execute waitingTask
     */
    private void checkWaitingTasks(TaskInfo taskInfo) {
        String tag = taskInfo.getTag();
        if (activeTasks.containsKey(tag)) {
            DownloadTask removeTask = activeTasks.get(tag);
            LogUtil.d(TAG, "移除DownloadTask...从Active:" + removeTask.hashCode() + "...taskHashCode:" + taskInfo.getTaskHashcode() + "...tag:" + taskInfo.getTag());
//            if (removeTask.hashCode()==taskInfo.getTaskHashcode()){
            activeTasks.remove(tag);
//            }
            if (waitingTasks.size() > 0) {
                DownloadTask downloadTask = waitingTasks.get(0);
                LogUtil.d(TAG, "移除DownloadTask...从Waiting:" + downloadTask.hashCode() + "...taskHashCode:" + taskInfo.getTaskHashcode() + "...tag:" + taskInfo.getTag());

                waitingTasks.remove(0);
                activeTasks.put(downloadTask.getTaskInfo().getTag(), downloadTask);
                downloadTask.start();
            }
        } else if (forceStartTasks.containsKey(tag)) {
            DownloadTask task = forceStartTasks.get(tag);
            LogUtil.d(TAG, "移除DownloadTask...从Force:" + task.hashCode() + "...taskHashCode:" + taskInfo.getTaskHashcode() + "...tag:" + taskInfo.getTag());
            forceStartTasks.remove(tag);
        }

        LogUtil.i(TAG, "checkWaitingTasks()...MaxActiveTaskNumber:" + activeTaskNumber + "...CurrentDownloadTaskNumber:" + activeTasks.size() + "...CurrentWaitingTaskNumber:" + waitingTasks.size() + "...ForceDownloadTaskNumber:" + forceStartTasks.size() + "...tag:" + taskInfo.getTag());
    }

    private TaskListener taskListener = new TaskListener() {
        @Override
        public void onStart(TaskInfo taskInfo) {
            manager.sendBroadcast(STATE_START, taskInfo, null);
        }

        @Override
        public void onPause(TaskInfo taskInfo, int hashcode) {
//            DownloadTask task = activeTasks.get(taskInfo.getTag());
//            if (task!=null) {
//            }
            LogUtil.d(TAG, "移除DownloadTask...TaskListener...onPause:" + hashcode + "...taskInfoCode" + taskInfo.getTaskHashcode() + "...tag:" + taskInfo.getTag());
            checkWaitingTasks(taskInfo);
            manager.sendBroadcast(STATE_PAUSE, taskInfo, null);
        }

        @Override
        public void onProgress(TaskInfo taskInfo) {
            manager.sendBroadcast(STATE_DOWNLOADING, taskInfo, null);
        }

        @Override
        public void onError(TaskInfo taskInfo, DownloadException downloadException, int hashcode) {
            handleExceptionType(taskInfo, downloadException);
            LogUtil.d(TAG, "移除DownloadTask...TaskListener...onError:" + hashcode + "...taskInfoCode" + taskInfo.getTaskHashcode() + "...tag:" + taskInfo.getTag());
            checkWaitingTasks(taskInfo);
            manager.sendBroadcast(STATE_ERROR, taskInfo, downloadException);
        }

        @Override
        public void onFinish(TaskInfo taskInfo, int hashcode) {
            manager.sendBroadcast(STATE_FINISH, taskInfo, null);
            allTaskInfo.remove(taskInfo.getTag());
            LogUtil.d(TAG, "移除DownloadTask...TaskListener...onFinish:" + hashcode + "...taskInfoCode" + taskInfo.getTaskHashcode() + "...tag:" + taskInfo.getTag());
            checkWaitingTasks(taskInfo);
        }

        @Override
        public void onCancel(TaskInfo taskInfo, int hashcode) {
            manager.sendBroadcast(STATE_CANCEL, taskInfo, null);
            allTaskInfo.remove(taskInfo.getTag());
            LogUtil.d(TAG, "移除DownloadTask...TaskListener...onCancel:" + hashcode + "...taskInfoCode" + taskInfo.getTaskHashcode() + "...tag:" + taskInfo.getTag());
            checkWaitingTasks(taskInfo);
        }
    };
}
