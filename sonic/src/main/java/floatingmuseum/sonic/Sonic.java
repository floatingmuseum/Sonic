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
        if (!ListUtil.isEmpty(allTask)) {
            for (TaskInfo taskInfo : allTask) {
                LogUtil.i(TAG, "init()...tasks exist in db:" + taskInfo.toString());
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
        if (dirPath != null && dirPath != "") {
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
            TaskInfo taskInfo = new TaskInfo(request.getUrl(), request.getTag(), request.getFileName(), request.getDirPath(), request.getDirPath() + "/" + request.getFileName(), 0, 0, 0, 0, 0);
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

    private TaskConfig getFinalTaskConfig(TaskInfo taskInfo, DownloadRequest request) {
        TaskConfig existTaskConfig = dbManager.queryTaskConfig(taskInfo.getTag());
        if (existTaskConfig != null) {
            LogUtil.i(TAG, "getFinalTaskConfig()...config exist in db:" + existTaskConfig.toString());
            return existTaskConfig;
        } else if (request.isCustomTaskConfig()) {
            TaskConfig singleTaskConfig = request.getTaskConfig();
            dbManager.insertTaskConfig(taskInfo.getTag(), singleTaskConfig);
            LogUtil.i(TAG, "getFinalTaskConfig()...save config:" + singleTaskConfig.toString() + "..." + existTaskConfig);
            return singleTaskConfig;
        } else {
            LogUtil.i(TAG, "getFinalTaskConfig()...use default config:" + taskConfig.toString() + "..." + existTaskConfig);
            return taskConfig;
        }
    }

    private void initDownload(TaskInfo taskInfo, boolean isExist, DownloadRequest request) {
        LogUtil.i(TAG, "initDownload()...TaskInfo:" + taskInfo.toString());
        TaskConfig finalTaskConfig = getFinalTaskConfig(taskInfo, request);
        if (!isExist) {
            dbManager.insertTaskInfo(taskInfo);
            allTaskInfo.put(taskInfo.getTag(), taskInfo);
        }

        if (isForceStart(taskInfo, finalTaskConfig)) {
            return;
        }

        if (activeTasks.size() == activeTaskNumber) {
            taskInfo.setState(Sonic.STATE_WAITING);
            LogUtil.i(TAG, "initDownload()...Name:" + taskInfo.getName() + "...into waiting queue");
            DownloadTask downloadTask = new DownloadTask(taskInfo, dbManager, finalTaskConfig, threadsPool, taskListener);
            waitingTasks.add(downloadTask);
            manager.sendBroadcast(STATE_WAITING, taskInfo, null);
        } else {
            LogUtil.i(TAG, "initDownload()...Name:" + taskInfo.getName() + "...into download queue");
            DownloadTask downloadTask = new DownloadTask(taskInfo, dbManager, finalTaskConfig, threadsPool, taskListener);
            activeTasks.put(taskInfo.getTag(), downloadTask);
            downloadTask.start();
        }
        LogUtil.i(TAG, "initDownload()...MaxActiveTaskNumber:" + activeTaskNumber + "...CurrentDownloadTaskNumber:" + activeTasks.size() + "...CurrentWaitingTaskNumber:" + waitingTasks.size() + "...ForceDownloadTaskNumber:" + forceStartTasks.size());
    }

    private boolean isForceStart(TaskInfo taskInfo, TaskConfig finalTaskConfig) {
        LogUtil.i(TAG, "isForceStart:" + finalTaskConfig.toString());
        if (finalTaskConfig.getForceStart() == FORCE_START_YES) {
            LogUtil.i(TAG, "initDownload()...Name:" + taskInfo.getName() + "...force start download");
            DownloadTask downloadTask = new DownloadTask(taskInfo, dbManager, finalTaskConfig, threadsPool, taskListener);
            forceStartTasks.put(taskInfo.getTag(), downloadTask);
            downloadTask.start();
            return true;
        } else {
            return false;
        }
    }

    public void pauseTask(String tag) {
        // TODO: 2017/5/10 pause has delay
        if (activeTasks.containsKey(tag)) {
            LogUtil.i(TAG, "pauseTask()...activeTasks:" + activeTasks.size() + "..." + tag);
            activeTasks.get(tag).stop();
        } else if (forceStartTasks.containsKey(tag)) {
            LogUtil.i(TAG, "pauseTask()...forceStartTasks:" + activeTasks.size() + "..." + tag);
            forceStartTasks.get(tag).stop();
        } else {
            LogUtil.i(TAG, "pauseTask()...waitingTasks:" + waitingTasks.size() + "..." + tag);
            for (DownloadTask waitingTask : waitingTasks) {
                TaskInfo taskInfo = waitingTask.getTaskInfo();
                LogUtil.i(TAG, "pauseTask()...waitingTasks:" + taskInfo.getName());
                if (taskInfo.getTag().equals(tag)) {
                    Log.i(TAG, "pauseTask()...waitingTasks:" + taskInfo.getName());
                    taskInfo.setState(STATE_PAUSE);
                    manager.sendBroadcast(STATE_PAUSE, taskInfo, null);
                    waitingTasks.remove(waitingTask);
                    return;
                }
            }
            LogUtil.i(TAG, "Which task that you want stop,doesn't exist.");
        }
    }

    public void pauseAllTask() {
        //remove all task from waitingTask
        for (DownloadTask waitingTask : waitingTasks) {
            TaskInfo taskInfo = waitingTask.getTaskInfo();
            taskInfo.setState(STATE_PAUSE);
            manager.sendBroadcast(STATE_PAUSE, taskInfo, null);
        }
        waitingTasks.clear();

        //stop all download task
        for (String key : activeTasks.keySet()) {
            DownloadTask downloadTask = activeTasks.get(key);
            downloadTask.stop();
        }
//        activeTasks.clear();

        // stop all force download task
        for (String key : forceStartTasks.keySet()) {
            DownloadTask downloadTask = forceStartTasks.get(key);
            downloadTask.stop();
        }
//        cancelTask("");
    }

    /**
     * <p>CancelTask will remove all information from database about this task.also delete file from sdcard.
     * <p>if this task downloaded by using addTask(url),so the url is the tag.
     */
    public void cancelTask(String tag) {
        // TODO: 2017/5/10 tell user cancel result immediately.then stop task,remove task on the background.

        if (activeTasks.containsKey(tag)) {
            DownloadTask task = activeTasks.get(tag);
            task.cancel();
            return;
        }

        if (forceStartTasks.containsKey(tag)) {
            DownloadTask task = forceStartTasks.get(tag);
            task.cancel();
            return;
        }

        for (DownloadTask waitingTask : waitingTasks) {
            if (tag.equals(waitingTask.getTaskInfo().getTag())) {
                waitingTask.cancelTask();
                waitingTasks.remove(waitingTask);
                allTaskInfo.remove(waitingTask.getTaskInfo());
                return;
            }
        }

        if (allTaskInfo.containsKey(tag)) {
            TaskInfo taskInfo = allTaskInfo.get(tag);
            taskInfo.setProgress(0);
            taskInfo.setCurrentSize(0);
            taskInfo.setTotalSize(0);
            taskInfo.setState(Sonic.STATE_CANCEL);
            dbManager.delete(taskInfo);
            FileUtil.deleteFile(taskInfo.getFilePath());
            taskListener.onCancel(taskInfo);
            return;
        }
        LogUtil.i(TAG, "Which task that you want cancel,doesn't exist.");
    }

    private void cancelAllTask() {
    }

    private void handleExceptionType(TaskInfo taskInfo, DownloadException downloadException) {
        if (downloadException.getExceptionType() == DownloadException.TYPE_MALFORMED_URL) {
            allTaskInfo.remove(taskInfo.getTag());
        }
    }

    private void checkWaitingTasks(TaskInfo taskInfo) {
        String tag = taskInfo.getTag();
        if (activeTasks.containsKey(tag)) {
            activeTasks.remove(taskInfo.getTag());
            if (waitingTasks.size() > 0) {
                DownloadTask downloadTask = waitingTasks.get(0);
                waitingTasks.remove(0);
                activeTasks.put(downloadTask.getTaskInfo().getTag(), downloadTask);
                downloadTask.start();
            }
        } else if (forceStartTasks.containsKey(tag)) {
            forceStartTasks.remove(tag);
        }

        LogUtil.i(TAG, "checkWaitingTasks()...MaxActiveTaskNumber:" + activeTaskNumber + "...CurrentDownloadTaskNumber:" + activeTasks.size() + "...CurrentWaitingTaskNumber:" + waitingTasks.size() + "...ForceDownloadTaskNumber:" + forceStartTasks.size());
    }

    private TaskListener taskListener = new TaskListener() {
        @Override
        public void onStart(TaskInfo taskInfo) {
            manager.sendBroadcast(STATE_START, taskInfo, null);
        }

        @Override
        public void onPause(TaskInfo taskInfo) {
            manager.sendBroadcast(STATE_PAUSE, taskInfo, null);
            checkWaitingTasks(taskInfo);
        }

        @Override
        public void onProgress(TaskInfo taskInfo) {
            manager.sendBroadcast(STATE_DOWNLOADING, taskInfo, null);
        }

        @Override
        public void onError(TaskInfo taskInfo, DownloadException downloadException) {
            handleExceptionType(taskInfo, downloadException);
            manager.sendBroadcast(STATE_ERROR, taskInfo, downloadException);
            checkWaitingTasks(taskInfo);
        }

        @Override
        public void onFinish(TaskInfo taskInfo) {
            manager.sendBroadcast(STATE_FINISH, taskInfo, null);
            allTaskInfo.remove(taskInfo.getTag());
            checkWaitingTasks(taskInfo);
        }

        @Override
        public void onCancel(TaskInfo taskInfo) {
            manager.sendBroadcast(STATE_CANCEL, taskInfo, null);
            allTaskInfo.remove(taskInfo.getTag());
            checkWaitingTasks(taskInfo);
        }
    };
}
