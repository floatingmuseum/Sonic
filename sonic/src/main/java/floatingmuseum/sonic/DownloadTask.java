package floatingmuseum.sonic;


import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import floatingmuseum.sonic.db.DBManager;
import floatingmuseum.sonic.entity.TaskInfo;
import floatingmuseum.sonic.entity.ThreadInfo;
import floatingmuseum.sonic.listener.InitListener;
import floatingmuseum.sonic.listener.TaskListener;
import floatingmuseum.sonic.listener.ThreadListener;
import floatingmuseum.sonic.threads.DownloadThread;
import floatingmuseum.sonic.threads.InitThread;
import floatingmuseum.sonic.utils.FileUtil;

/**
 * Created by Floatingmuseum on 2017/3/31.
 */

public class DownloadTask implements InitListener, ThreadListener {

    private String TAG = DownloadTask.class.getName();
    private TaskInfo taskInfo;
    private DBManager dbManager;
    private int maxThreads;
    private TaskListener taskListener;
    private List<DownloadThread> threads;
    private List<ThreadInfo> threadInfoList;
    private int alreadyStopThreads;
    private int progressResponseTime;
    private long lastUpdateTime = System.currentTimeMillis();

    public DownloadTask(TaskInfo taskInfo, DBManager dbManager, int maxThreads, int progressResponseTime, TaskListener taskListener) {
        this.taskInfo = taskInfo;
        this.dbManager = dbManager;
        this.maxThreads = maxThreads;
        this.taskListener = taskListener;
        this.progressResponseTime = progressResponseTime;
        threads = new ArrayList<>();
        FileUtil.initDir(taskInfo.getDirPath());
    }

    public TaskInfo getTaskInfo() {
        return taskInfo;
    }

    public void start() {
        alreadyStopThreads = 0;
        threadInfoList = dbManager.getAllThreadInfo(taskInfo.getDownloadUrl());
        if (threadInfoList.size() == 0) {//First time
            Log.i(TAG, "start()...第一次下载此任务.");
            new InitThread(taskInfo.getDownloadUrl(), this).start();
        } else {
            Log.i(TAG, "start()...继续下载此任务");
            initDownloadThread(threadInfoList);
            for (DownloadThread thread : threads) {
                thread.start();
            }
        }
    }

    public void stop() {
        for (DownloadThread thread : threads) {
            thread.stopThread();
        }
    }

    private void initDownloadThread(List<ThreadInfo> threadInfoList) {
        Log.i(TAG, "TaskInfo...TotalSize:" + taskInfo.getTotalSize() + "...CurrentSize:" + taskInfo.getCurrentSize());
        for (ThreadInfo info : threadInfoList) {
            Log.i(TAG, "initDownloadThreadInfo线程" + info.getId() + "号...初始位置:" + info.getStartPosition() + "...当前位置:" + info.getCurrentPosition() + "...末尾位置:" + info.getEndPosition());
            if (info.getCurrentPosition() < info.getEndPosition()) {//只初始化还没完成的线程
                Log.i(TAG, info.getId() + "号继续工作");
                DownloadThread thread = new DownloadThread(info, taskInfo.getDirPath(), taskInfo.getName(), dbManager, this);
                threads.add(thread);
            } else {
                Log.i(TAG, info.getId() + "号已完成工作，休息");
                maxThreads--;
            }
        }
        Log.i(TAG, "TaskInfo...TotalSize:" + taskInfo.getTotalSize() + "...CurrentSize:" + taskInfo.getCurrentSize());
        taskInfo.setProgress(getProgress());
    }

    @Override
    public void onGetContentLength(long contentLength) {
        Log.i(TAG, "onGetContentLength总文件大小:" + contentLength + "..." + FileUtil.bytesToMb(contentLength) + "mb");
        taskInfo.setTotalSize(contentLength);
        dbManager.updateTaskInfo(taskInfo);
        threadInfoList = new ArrayList<>();
        long blockLength = contentLength / maxThreads;

        for (int x = 1; x <= maxThreads; x++) {
            long start = x == 1 ? 0 : blockLength * (x - 1) + 1;
            long end = x == maxThreads ? contentLength : blockLength * x;
            long current = start;
            ThreadInfo threadInfo = new ThreadInfo(x, taskInfo.getDownloadUrl(), start, end, current, contentLength);
            threadInfoList.add(threadInfo);
            dbManager.insertThreadInfo(threadInfo);//第一次初始化，存储线程信息到数据库
        }
        initDownloadThread(threadInfoList);
        for (DownloadThread thread : threads) {
            thread.start();
        }
    }

    private int getProgress() {
        int progress = (int) (((float) taskInfo.getCurrentSize() / (float) taskInfo.getTotalSize()) * 100);
        return progress;
    }

    private long getCurrentSize() {
        long currentSize = 0;
        for (ThreadInfo info : threadInfoList) {
            currentSize += (info.getCurrentPosition() - info.getStartPosition());
        }
        return currentSize;
    }

    private void updateProgress() {
        taskInfo.setState(Sonic.STATE_DOWNLOADING);
        taskInfo.setProgress(getProgress());
        taskInfo.setCurrentSize(getCurrentSize());
        taskListener.onProgress(taskInfo);
    }

    @Override
    public void onPause(ThreadInfo threadInfo) {
        alreadyStopThreads++;
        //已停止线程数达到运行线程数，回调onPause
        if (alreadyStopThreads == maxThreads) {
            updateProgress();
            taskInfo.setState(Sonic.STATE_PAUSE);
            taskListener.onPause(taskInfo);
            Log.i(TAG, "线程暂停...已完成大小：" + taskInfo.getCurrentSize());
            dbManager.updateTaskInfo(taskInfo);
        }
    }

    @Override
    public void onProgress(ThreadInfo threadInfo) {
        long nowTime = System.currentTimeMillis();
        if (nowTime - lastUpdateTime > progressResponseTime) {
            lastUpdateTime = nowTime;
            updateProgress();
        }
    }

    @Override
    public void onError(ThreadInfo threadInfo, Throwable e) {
        updateProgress();
        taskInfo.setState(Sonic.STATE_ERROR);
        taskListener.onError(taskInfo, e);
        dbManager.updateTaskInfo(taskInfo);
    }

    @Override
    public void onFinished(int threadId) {
        maxThreads--;
        if (maxThreads == 0) {
            Log.i(TAG, "下载进度...onFinished...CurrentSize:" + taskInfo.getCurrentSize() + "...TotalSize:" + taskInfo.getTotalSize());
            //删除所有记录
            dbManager.delete(DBManager.THREADS_TABLE_NAME, taskInfo.getDownloadUrl());
            dbManager.delete(DBManager.TASKS_TABLE_NAME, taskInfo.getDownloadUrl());
            Log.d(TAG, "onFinished...下载结束");
            updateProgress();
            taskListener.onFinish(taskInfo);
        }
    }

    @Override
    public void onInitError(Throwable e) {

    }
}
