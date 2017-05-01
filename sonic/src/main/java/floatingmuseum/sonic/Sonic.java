package floatingmuseum.sonic;

import android.content.Context;
import android.os.Environment;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import floatingmuseum.sonic.db.DBManager;
import floatingmuseum.sonic.entity.TaskInfo;
import floatingmuseum.sonic.entity.UIListenerMessage;
import floatingmuseum.sonic.listener.DownloadListener;
import floatingmuseum.sonic.listener.TaskListener;
import floatingmuseum.sonic.utils.FileUtil;
import floatingmuseum.sonic.utils.ListUtil;


/**
 * Created by Floatingmuseum on 2017/3/16.
 */

public class Sonic implements TaskListener {

    private static final String TAG = Sonic.class.getName();

    public static final int STATE_NONE = 0;
    public static final int STATE_START = 6;
    public static final int STATE_WAITING = 1;
    public static final int STATE_PAUSE = 2;
    public static final int STATE_DOWNLOADING = 3;
    public static final int STATE_ERROR = 4;
    public static final int STATE_FINISH = 5;
    public static final int STATE_CANCEL = 7;

    private UIHandler uiHandler;
    private static Context context;
    private static Sonic sonic;

    private int activeTaskNumber = 3;

    private Map<String, TaskInfo> allTaskInfo;
    private Map<String, DownloadTask> activeTasks;
    private List<DownloadTask> waitingTasks;
    private DBManager dbManager;
    private TaskConfig taskConfig = new TaskConfig();

    private Sonic() {
    }

    public void init(Context applicationContext) {

        context = applicationContext;
        Log.i(TAG, "Download dir path:" + taskConfig.getDirPath());
        dbManager = new DBManager(context);
        uiHandler = new UIHandler();
        List<TaskInfo> allTask = dbManager.getAllDownloadTask();
        allTaskInfo = new HashMap<>();
        activeTasks = new HashMap<>();
        waitingTasks = new ArrayList<>();
        if (!ListUtil.isEmpty(allTask)) {
            for (TaskInfo downloadTask : allTask) {
                allTaskInfo.put(downloadTask.getTag(), downloadTask);
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
        Log.i(TAG, "Sonic...Instance:" + sonic);
        if (sonic == null) {
            synchronized (Sonic.class) {
                if (sonic == null) {
                    sonic = new Sonic();
                }
            }
        }
        return sonic;
    }

    /**
     * How many threads working for a task.
     * default is 3.
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
     * How many tasks can running at the same time.
     * default is 3.
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
     * DownloadListener onProgress() method will be call at defined interval.
     * the milliseconds must between 0 to 1000.
     * the default for the interval is 500 milliseconds.
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
     * Storage dir path.
     * default is sdcard/downloads
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

    public Sonic setStopServiceAfterAllTaskFinished(boolean stopServiceAfterAllTaskFinished) {
//        this.stopServiceAfterAllTaskFinished = stopServiceAfterAllTaskFinished;
        return this;
    }


    public Sonic registerDownloadListener(DownloadListener listener) {
        Log.i(TAG, "DownloadListener:" + listener);
        uiHandler.setListener(listener);
        return this;
    }

    public void unRegisterDownloadListener() {
        uiHandler.removeListener();
    }

    /**
     * downloadUrl will be the tag for this task.
     */
    public void addTask(String downloadUrl) {
//        Log.i(TAG, "地址:" + context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
        addTask(downloadUrl, downloadUrl);
    }

    public void addTask(String downloadUrl, String tag) {
        addTask(downloadUrl, tag, FileUtil.getUrlFileName(downloadUrl));
    }

    public void addTask(String downloadUrl, String tag, String fileName) {
        addTask(downloadUrl, tag, fileName, null);
    }

    /**
     * downloadUrl will be the tag for this task.
     */
    public void addTask(String downloadUrl, TaskConfig singleTaskConfig) {
        addTask(downloadUrl, downloadUrl, singleTaskConfig);
    }

    public void addTask(String downloadUrl, String tag, TaskConfig singleTaskConfig) {
        addTask(downloadUrl, tag, FileUtil.getUrlFileName(downloadUrl), singleTaskConfig);
    }

    public void addTask(String downloadUrl, String tag, String fileName, TaskConfig singleTaskConfig) {
        //check is this task inside database
        if (allTaskInfo.containsKey(tag)) {
            TaskInfo taskInfo = allTaskInfo.get(tag);
            initDownload(taskInfo, true, null);
        } else {
            TaskInfo taskInfo;
            if (singleTaskConfig != null) {
                taskInfo = new TaskInfo(downloadUrl, tag, fileName, singleTaskConfig.getDirPath(), singleTaskConfig.getDirPath() + "/" + fileName, 0, 0, 0, 0, 0);
            } else {
                taskInfo = new TaskInfo(downloadUrl, tag, fileName, taskConfig.getDirPath(), taskConfig.getDirPath() + "/" + fileName, 0, 0, 0, 0, 0);
            }
            initDownload(taskInfo, false, singleTaskConfig);
        }
    }

    public TaskInfo getTaskInfo(String tag) {
        return allTaskInfo.get(tag);
    }

    public Map<String, TaskInfo> getAllTaskInfo() {
        return allTaskInfo;
    }

    public TaskConfig getFinalTaskConfig(TaskInfo taskInfo, TaskConfig singleTaskConfig) {
        TaskConfig existTaskConfig = dbManager.queryTaskConfig(taskInfo.getTag());
        if (existTaskConfig != null) {//数据库存在此任务配置,使用数据库配置
            Log.i(TAG, "任务配置...数据库已存在:" + existTaskConfig.toString());
            return existTaskConfig;
        } else if (singleTaskConfig != null) {//数据库不存在配置,且单一任务设置不为空,存储并使用此配置
            dbManager.insertTaskConfig(taskInfo.getTag(), singleTaskConfig);
            Log.i(TAG, "任务配置...数据库不存在...存储:" + singleTaskConfig.toString() + "..." + existTaskConfig);
            return singleTaskConfig;
        } else {//使用全局配置
            Log.i(TAG, "任务配置...全局配置:" + taskConfig.toString() + "..." + existTaskConfig);
            return taskConfig;
        }
    }

    private void initDownload(TaskInfo taskInfo, boolean isExist, TaskConfig singleTaskConfig) {
        TaskConfig finalTaskConfig = getFinalTaskConfig(taskInfo, singleTaskConfig);
        if (!isExist) {
            dbManager.insertTaskInfo(taskInfo);
            allTaskInfo.put(taskInfo.getTag(), taskInfo);
        }

        if (activeTasks.size() == activeTaskNumber) {
            taskInfo.setState(Sonic.STATE_WAITING);
            Log.i(TAG, "initDownload()...Name:" + taskInfo.getName() + "进入等待队列");
            DownloadTask downloadTask = new DownloadTask(taskInfo, dbManager, finalTaskConfig, this);
            waitingTasks.add(downloadTask);
            sendMessage(taskInfo, STATE_WAITING, null);
        } else {
            Log.i(TAG, "initDownload()...Name:" + taskInfo.getName() + "进入下载队列");
            DownloadTask downloadTask = new DownloadTask(taskInfo, dbManager, finalTaskConfig, this);
            activeTasks.put(taskInfo.getTag(), downloadTask);
            downloadTask.start();
        }
        Log.i(TAG, "initDownload()...最大同时下载任务数:" + activeTaskNumber + "...当前任务数:" + activeTasks.size() + "...等待任务数:" + waitingTasks.size());
    }

    public void stopTask(String tag) {
        if (activeTasks.containsKey(tag)) {
            Log.i(TAG, "stopTask()...activeTasks:" + activeTasks.size() + "..." + tag);
            activeTasks.get(tag).stop();
        } else {
            Log.i(TAG, "stopTask()...waitingTasks:" + waitingTasks.size() + "..." + tag);
            for (DownloadTask waitingTask : waitingTasks) {
                TaskInfo taskInfo = waitingTask.getTaskInfo();
                Log.i(TAG, "stopTask()...waitingTasks:" + taskInfo.getName());
                if (taskInfo.getTag().equals(tag)) {
                    Log.i(TAG, "stopTask()...waitingTasks:" + taskInfo.getName());
                    taskInfo.setState(STATE_PAUSE);
                    sendMessage(taskInfo, STATE_PAUSE, null);
                    waitingTasks.remove(waitingTask);
                    return;
                }
            }
            Log.i(TAG, "Which task that you want removed,doesn't exist.");
        }
    }

    public void stopAllTask() {
        //移除等待队列的任务
        for (DownloadTask waitingTask : waitingTasks) {
            TaskInfo taskInfo = waitingTask.getTaskInfo();
            taskInfo.setState(STATE_PAUSE);
            sendMessage(taskInfo, STATE_PAUSE, null);
//            waitingTasks.remove(waitingTask);
        }
        waitingTasks.clear();

        //停止并清空下载列表
        for (String key : activeTasks.keySet()) {
            DownloadTask downloadTask = activeTasks.get(key);
            downloadTask.stop();
//            activeTasks.remove(key);
        }
        activeTasks.clear();
        cancelTask("");
    }

    /**
     * <p>CancelTask will remove all information from database about this task.also delete file from sdcard.
     * <p>if this task downloaded by using addTask(url),so the url is the tag.
     */
    public void cancelTask(String tag) {
        //处于下载队列中时
        if (activeTasks.containsKey(tag)) {
            DownloadTask task = activeTasks.get(tag);
            task.cancel();
            FileUtil.deleteFile(task.getTaskInfo().getFilePath());
            activeTasks.remove(tag);
            allTaskInfo.remove(tag);
            return;
        }

        //处于等待队列中时
        for (DownloadTask waitingTask : waitingTasks) {
            if (tag.equals(waitingTask.getTaskInfo().getTag())) {
                waitingTask.cancelTask();
                FileUtil.deleteFile(waitingTask.getTaskInfo().getFilePath());
                waitingTasks.remove(waitingTask);
                allTaskInfo.remove(waitingTask);
                return;
            }
        }


        if (allTaskInfo.containsKey(tag)) {
            TaskInfo taskInfo = allTaskInfo.get(tag);
            taskInfo.setProgress(0);
            taskInfo.setCurrentSize(0);
            taskInfo.setTotalSize(0);
            taskInfo.setState(Sonic.STATE_CANCEL);
            onCancel(taskInfo);
            dbManager.delete(taskInfo);
            FileUtil.deleteFile(taskInfo.getFilePath());
            allTaskInfo.remove(tag);
        }
    }

    @Override
    public void onStart(TaskInfo taskInfo) {
        sendMessage(taskInfo, STATE_START, null);
    }

    @Override
    public void onPause(TaskInfo taskInfo) {
        sendMessage(taskInfo, STATE_PAUSE, null);
        checkWaitingTasks(taskInfo);
    }

    @Override
    public void onProgress(TaskInfo taskInfo) {
        sendMessage(taskInfo, STATE_DOWNLOADING, null);
//        Log.i(TAG, "下载进度...onProgress...CurrentSize:" + taskInfo.getCurrentSize() + "...TotalSize:" + taskInfo.getTotalSize() + "...Progress:" + taskInfo.getProgress());
    }

    @Override
    public void onError(TaskInfo taskInfo, DownloadException downloadException) {
        sendMessage(taskInfo, STATE_ERROR, downloadException);
        checkWaitingTasks(taskInfo);
    }

    @Override
    public void onFinish(TaskInfo taskInfo) {
        sendMessage(taskInfo, STATE_FINISH, null);
        allTaskInfo.remove(taskInfo.getTag());
        checkWaitingTasks(taskInfo);
    }

    @Override
    public void onCancel(TaskInfo taskInfo) {
        sendMessage(taskInfo, STATE_CANCEL, null);
    }

    /**
     * 查看等待列表中是否有任务
     */
    private void checkWaitingTasks(TaskInfo taskInfo) {
        // TODO: 2017/4/6 有时会有超过 activeTaskNumber 的任务在执行
        activeTasks.remove(taskInfo.getTag());
        if (waitingTasks.size() > 0) {
            DownloadTask downloadTask = waitingTasks.get(0);
            waitingTasks.remove(0);
            activeTasks.put(downloadTask.getTaskInfo().getTag(), downloadTask);
            downloadTask.start();
        }
        Log.i(TAG, "onFinish()...最大同时下载任务数:" + activeTaskNumber + "...当前任务数:" + activeTasks.size() + "...等待任务数:" + waitingTasks.size());
    }

    private void sendMessage(TaskInfo taskInfo, int downloadState, DownloadException downloadException) {
        UIListenerMessage taskMessage;
        if (downloadState == STATE_ERROR) {
            taskMessage = new UIListenerMessage(taskInfo, downloadState, downloadException);
        } else {
            taskMessage = new UIListenerMessage(taskInfo, downloadState, null);
        }
        Message message = uiHandler.obtainMessage();
        message.obj = taskMessage;
        uiHandler.sendMessage(message);
    }
}
