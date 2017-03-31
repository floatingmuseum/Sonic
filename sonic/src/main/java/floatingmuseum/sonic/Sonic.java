package floatingmuseum.sonic;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import floatingmuseum.sonic.db.DBManager;
import floatingmuseum.sonic.entity.TaskInfo;
import floatingmuseum.sonic.listener.DownloadListener;
import floatingmuseum.sonic.listener.TaskListener;
import floatingmuseum.sonic.utils.FileUtil;
import floatingmuseum.sonic.utils.ListUtil;


/**
 * Created by Floatingmuseum on 2017/3/16.
 */

public class Sonic implements TaskListener {

    private static final String TAG = Sonic.class.getName();

    private static final int STATE_NONE = 0;
    private static final int STATE_WAITING = 1;
    private static final int STATE_PAUSE = 2;
    private static final int STATE_DOWNLOADING = 3;
    private static final int STATE_ERROR = 4;
    private static final int STATE_FINISH = 5;


    private static Context context;
    private static Sonic sonic;
    private int maxThreads = 3;
    private int activeTaskNumber = 3;
    private String dirPath;
    private Map<String, TaskInfo> allTaskInfo;
    private Map<String, DownloadTask> activeTasks;
    private List<DownloadTask> waitingTasks;
    private DownloadListener listener;
    private DBManager dbManager;

    private Sonic() {
        dirPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        Log.i(TAG, "Default save dir path:" + dirPath);
        dbManager = new DBManager(context);
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

    public Sonic setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
        return this;
    }

    public Sonic setActiveTaskNumber(int activeTaskNumber) {
        this.activeTaskNumber = activeTaskNumber;
        return this;
    }

    public Sonic setDirPath(String dirPath) {
        this.dirPath = dirPath;
        return this;
    }


    public Sonic registerDownloadListener(DownloadListener listener) {
        this.listener = listener;
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

    private void initDownload(TaskInfo taskInfo, boolean isExist) {
        if (!isExist) {
            dbManager.insertTaskInfo(taskInfo);
            allTaskInfo.put(taskInfo.getTag(), taskInfo);
        }

        DownloadTask downloadTask = new DownloadTask(taskInfo, dbManager, maxThreads, this);
        if (activeTasks.size() == activeTaskNumber) {
            waitingTasks.add(downloadTask);
        } else {
            activeTasks.put(taskInfo.getTag(), downloadTask);
            downloadTask.start();
        }
    }

    public void stopTask(String tag) {
        if (activeTasks.containsKey(tag)) {
            activeTasks.get(tag).stop();
        } else {
            Log.i(TAG, "Which task that you want removed,doesn't exist.");
        }
    }

    @Override
    public void onProgress(TaskInfo taskInfo) {

    }

    @Override
    public void onError(TaskInfo taskInfo, Throwable e) {

    }

    @Override
    public void onFinish(TaskInfo taskInfo) {
        activeTasks.remove(taskInfo.getTag());
        if (waitingTasks.size() > 0) {
            DownloadTask downloadTask = waitingTasks.get(0);
            waitingTasks.remove(0);
            activeTasks.put(downloadTask.getTaskInfo().getTag(), downloadTask);
            downloadTask.start();
        }
    }
}
