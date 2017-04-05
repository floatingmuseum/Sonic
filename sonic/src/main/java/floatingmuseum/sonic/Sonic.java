package floatingmuseum.sonic;

import android.content.Context;
import android.os.Environment;
import android.os.Message;
import android.util.Log;

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


    private UIHandler uiHandler;
    private static Context context;
    private static Sonic sonic;
    private int maxThreads = 3;
    private int activeTaskNumber = 3;
    private String dirPath;
    private int progressResponseTime = 500;

    private Map<String, TaskInfo> allTaskInfo;
    private Map<String, DownloadTask> activeTasks;
    private List<DownloadTask> waitingTasks;
    private DBManager dbManager;

    private Sonic() {
        dirPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        Log.i(TAG, "Default save dir path:" + dirPath);
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

    public static void init(Context applicationContext) {
        context = applicationContext;
    }

    public static Context getContext() {
        if (context == null) {
            throw new IllegalStateException("You lost Sonic.init() in your Application.");
        }
        return context;
    }

    public static Sonic getInstance() {
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
     * How many thread working for a task.
     */
    public Sonic setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
        return this;
    }

    /**
     * How many task can running at the same time.
     */
    public Sonic setActiveTaskNumber(int activeTaskNumber) {
        this.activeTaskNumber = activeTaskNumber;
        return this;
    }

    /**
     * DownloadListener onProgress() method will be call at defined interval.
     * The default for the interval is 500 milliseconds.
     */
    public Sonic setProgressResponseTime(int milliseconds) {
        progressResponseTime = milliseconds;
        return this;
    }

    /**
     * storage dir path
     */
    public Sonic setDirPath(String dirPath) {
        this.dirPath = dirPath;
        return this;
    }

    public Sonic setStopServiceAfterAllTaskFinished(boolean stopServiceAfterAllTaskFinished) {
//        this.stopServiceAfterAllTaskFinished = stopServiceAfterAllTaskFinished;
        return this;
    }


    public Sonic registerDownloadListener(DownloadListener listener) {
        uiHandler.setListener(listener);
        return this;
    }

    public void addTask(String downloadUrl) {
        addTask(downloadUrl, downloadUrl, FileUtil.getUrlFileName(downloadUrl));
    }

    public void addTask(String downloadUrl, String tag) {
        addTask(downloadUrl, tag, FileUtil.getUrlFileName(downloadUrl));
    }

    public void addTask(String downloadUrl, String tag, String fileName) {
        //check is this task inside database
        if (allTaskInfo.containsKey(tag)) {
            TaskInfo taskInfo = allTaskInfo.get(tag);
            initDownload(taskInfo, true);
        } else {
            TaskInfo taskInfo = new TaskInfo(downloadUrl, tag, fileName, dirPath, dirPath + fileName, 0, 0, 0, 0, 0);
            initDownload(taskInfo, false);
        }
    }

    public void addTask(TaskInfo taskInfo) {

    }

    public TaskInfo getTaskInfo(String tag) {
        return allTaskInfo.get(tag);
    }

    public Map<String, TaskInfo> getAllTaskInfo() {
        return allTaskInfo;
    }

    private void initDownload(TaskInfo taskInfo, boolean isExist) {
        if (!isExist) {
            dbManager.insertTaskInfo(taskInfo);
            allTaskInfo.put(taskInfo.getTag(), taskInfo);
        }

        DownloadTask downloadTask = new DownloadTask(taskInfo, dbManager, maxThreads, progressResponseTime, this);
        if (activeTasks.size() == activeTaskNumber) {
            waitingTasks.add(downloadTask);
            sendMessage(taskInfo, STATE_WAITING, null);
        } else {
            activeTasks.put(taskInfo.getTag(), downloadTask);
            downloadTask.start();
        }
        Log.i(TAG, "initDownload()...最大同时下载任务数:" + activeTaskNumber + "...当前任务数:" + activeTasks.size() + "...等待任务数:" + waitingTasks.size());
    }

    public void stopTask(String tag) {
        if (activeTasks.containsKey(tag)) {
            activeTasks.get(tag).stop();
        } else {
            Log.i(TAG, "Which task that you want removed,doesn't exist.");
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
        Log.i(TAG, "下载进度...onProgress...CurrentSize:" + taskInfo.getCurrentSize() + "...TotalSize:" + taskInfo.getTotalSize() + "...Progress:" + taskInfo.getProgress());
    }

    @Override
    public void onError(TaskInfo taskInfo, Throwable e) {
        dbManager.updateTaskInfo(taskInfo);
        sendMessage(taskInfo, STATE_ERROR, e);
        checkWaitingTasks(taskInfo);
    }

    @Override
    public void onFinish(TaskInfo taskInfo) {
        sendMessage(taskInfo, STATE_FINISH, null);
        allTaskInfo.remove(taskInfo.getTag());
        checkWaitingTasks(taskInfo);
    }

    /**
     * 查看等待列表中是否有任务
     */
    private void checkWaitingTasks(TaskInfo taskInfo) {
        activeTasks.remove(taskInfo.getTag());
        if (waitingTasks.size() > 0) {
            DownloadTask downloadTask = waitingTasks.get(0);
            waitingTasks.remove(0);
            activeTasks.put(downloadTask.getTaskInfo().getTag(), downloadTask);
            downloadTask.start();
        }
        Log.i(TAG, "onFinish()...最大同时下载任务数:" + activeTaskNumber + "...当前任务数:" + activeTasks.size() + "...等待任务数:" + waitingTasks.size());
    }

    private void sendMessage(TaskInfo taskInfo, int downloadState, Throwable throwable) {
        UIListenerMessage taskMessage;
        if (downloadState == STATE_ERROR) {
            taskMessage = new UIListenerMessage(taskInfo, downloadState, throwable);
        } else {
            taskMessage = new UIListenerMessage(taskInfo, downloadState, null);
        }
        Message message = uiHandler.obtainMessage();
        message.obj = taskMessage;
        uiHandler.sendMessage(message);
    }
}
