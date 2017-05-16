package floatingmuseum.sonic;


import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import floatingmuseum.sonic.db.DBManager;
import floatingmuseum.sonic.entity.TaskInfo;
import floatingmuseum.sonic.entity.ThreadInfo;
import floatingmuseum.sonic.listener.InitListener;
import floatingmuseum.sonic.listener.TaskListener;
import floatingmuseum.sonic.listener.ThreadListener;
import floatingmuseum.sonic.threads.BaseThread;
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
    private TaskConfig taskConfig;
    private int retryTime;
    private int maxThreads;
    private TaskListener taskListener;
    private List<DownloadThread> threads;
    private List<ThreadInfo> threadInfoList;
    private long lastUpdateTime = System.currentTimeMillis();
    private boolean stopAfterInitThreadDone = false;
    private boolean isCancel = false;
    private int activeThreadsNum;
    private ExecutorService threadsPool;
    private DownloadException downloadException;
    private InitThread initThread;

    public DownloadTask(TaskInfo taskInfo, DBManager dbManager, TaskConfig taskConfig, ExecutorService threadsPool, TaskListener taskListener) {
        this.taskInfo = taskInfo;
        this.dbManager = dbManager;
        this.taskListener = taskListener;
        this.maxThreads = taskConfig.getMaxThreads();
        this.retryTime = taskConfig.getRetryTime();
        this.taskConfig = taskConfig;
        Log.i(TAG, "DownloadTask...name:" + taskInfo.getName() + "...MaxThreads:" + taskConfig.getMaxThreads() + "...ProgressResponseInterval:" + taskConfig.getProgressResponseInterval() + "...FilePath:" + taskInfo.getFilePath());
        threads = new ArrayList<>();
        FileUtil.initDir(taskInfo.getDirPath());
        this.threadsPool = threadsPool;
    }

    public TaskInfo getTaskInfo() {
        return taskInfo;
    }

    public void start() {
        taskInfo.setState(Sonic.STATE_START);
        taskListener.onStart(taskInfo);
        threadInfoList = dbManager.getAllThreadInfo(taskInfo.getDownloadUrl());
        if (threadInfoList.size() == 0) {//First time
            Log.i(TAG, "start()...First download." + "..." + taskInfo.getName());
            initThread = new InitThread(taskInfo.getDownloadUrl(), taskInfo.getName(), taskInfo.getDirPath(), taskConfig.getReadTimeout(), taskConfig.getConnectTimeout(), this);
            initThread.start();
        } else {
            Log.i(TAG, "start()...Resume download" + "..." + taskInfo.getName());
            initDownloadThread(threadInfoList);
            if (threads.size() > 0) {
                for (DownloadThread thread : threads) {
                    threadsPool.execute(thread);
//                    thread.start();
                }
            } else {
                taskListener.onFinish(taskInfo);
            }
        }
    }

    public void stop() {
        /**
         * threads.size equals 0 means first download,at this time,stopAllThread will not working,and download will keep running.
         * so set stopAfterInitThreadDone true.can stop this task after it get file length.
         *
         * still has some delay.
         */
        Log.i(TAG, "stop()...Stop downloading Threads:" + threads.size() + taskInfo.getName() + "..." + stopAfterInitThreadDone);
        if (threads.size() == 0) {
            stopAfterInitThreadDone = true;
            if (initThread != null) {
                initThread.stopThread();
            }
        } else {
            stopAllThreads();
        }
    }

    private void stopAllThreads() {
        for (DownloadThread thread : threads) {
            thread.stopThread();
        }
    }

    public void cancel() {
        isCancel = true;
        stop();
    }

    public void cancelTask() {
        taskInfo.setProgress(0);
        taskInfo.setCurrentSize(0);
        taskInfo.setTotalSize(0);
        taskInfo.setState(Sonic.STATE_CANCEL);
        dbManager.delete(taskInfo);
        FileUtil.deleteFile(taskInfo.getFilePath());
        taskListener.onCancel(taskInfo);
    }

    private void initDownloadThread(List<ThreadInfo> threadInfoList) {
        Log.i(TAG, "initDownloadThread()...TaskInfo...TotalSize:" + taskInfo.getTotalSize() + "...CurrentSize:" + taskInfo.getCurrentSize() + "..." + taskInfo.getName());
        for (ThreadInfo info : threadInfoList) {
            Log.i(TAG, "initDownloadThreadInfo()...ThreadID:" + info.getId() + "...StartPosition:" + info.getStartPosition() + "...CurrentPosition:" + info.getCurrentPosition() + "...EndPosition:" + info.getEndPosition() + "..." + taskInfo.getName());
            if (info.getCurrentPosition() < info.getEndPosition()) {//Only init thread which not finished.
                Log.i(TAG, "initDownloadThreadInfo()...ThreadID:" +info.getId() + "...Not Finished" + "..." + taskInfo.getName());
                DownloadThread thread = new DownloadThread(info, taskInfo.getDirPath(), taskInfo.getName(), dbManager, taskConfig.getReadTimeout(), taskConfig.getConnectTimeout(), this);
                threads.add(thread);
            }
        }
        taskInfo.setProgress(getProgress());

        activeThreadsNum = threads.size();
    }

    @Override
    public void onGetContentLength(long contentLength, boolean isSupportRange) {
        Log.i(TAG, "onGetContentLength()...FileLength:" + contentLength + "..." + FileUtil.bytesToMb(contentLength) + "mb" + "..." + taskInfo.getName() + "...isSupportRange:" + isSupportRange);
        taskInfo.setTotalSize(contentLength);

        if (isSupportRange) {
            initMultipleThreads(contentLength);
        } else {
            initSingleThread();
        }
    }

    private void initMultipleThreads(long contentLength) {
        dbManager.updateTaskInfo(taskInfo);
        updateProgress();
        threadInfoList = new ArrayList<>();
        long blockLength = contentLength / maxThreads;

        for (int x = 1; x <= maxThreads; x++) {
            long start = x == 1 ? 0 : blockLength * (x - 1) + 1;
            long end = x == maxThreads ? contentLength : blockLength * x;
            long current = start;
            ThreadInfo threadInfo = new ThreadInfo(x, taskInfo.getDownloadUrl(), start, end, current, contentLength);
            threadInfoList.add(threadInfo);
            dbManager.insertThreadInfo(threadInfo);
        }

        if (stopAfterInitThreadDone) {
            if (isCancel) {
                cancelTask();
                return;
            }
            taskInfo.setState(Sonic.STATE_PAUSE);
            dbManager.updateTaskInfo(taskInfo);
            taskListener.onPause(taskInfo);
            return;
        }

        initDownloadThread(threadInfoList);
        for (DownloadThread thread : threads) {
            threadsPool.execute(thread);
//            thread.start();
        }
    }

    private void initSingleThread() {
        taskListener.onProgress(taskInfo);

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
        taskInfo.setCurrentSize(getCurrentSize());
        taskInfo.setProgress(getProgress());
        Log.i(TAG, "updateProgress()...CurrentSize:" + taskInfo.getCurrentSize() + "..." + taskInfo.getProgress() + "..." + taskInfo.getState());
        taskListener.onProgress(taskInfo);
    }

    @Override
    public void onPause(ThreadInfo threadInfo) {
        if (isAllThreadsDead()) {
            if (isCancel) {
                cancelTask();
                return;
            }
            Log.i(TAG, "onPause()...Pause Success:" + taskInfo.getState());
            updateProgress();
            Log.i(TAG, "onPause()...Pause Success:" + taskInfo.getState());
            updateTaskInfo(Sonic.STATE_PAUSE);
            taskListener.onPause(taskInfo);
        }
    }

    @Override
    public void onProgress(ThreadInfo threadInfo) {
        long nowTime = System.currentTimeMillis();
        if (taskConfig.getProgressResponseInterval() == 0 || (nowTime - lastUpdateTime) > taskConfig.getProgressResponseInterval()) {
            lastUpdateTime = nowTime;
            updateProgress();
        }
    }

    private synchronized boolean isHaveRetryTime(BaseThread errorThread) {
        if (retryTime != 0) {
            retryTime--;
            ThreadInfo info = errorThread.getThreadInfo();
            Log.i(TAG, "isHaveRetryTime()..."+info.getId() + "Thread exception occurred...to retry...current retryTime:" + retryTime + "...CurrentPosition:" + info.getCurrentPosition());
            threads.remove(errorThread);
            DownloadThread retryThread = new DownloadThread(info, taskInfo.getDirPath(), taskInfo.getName(), dbManager, taskConfig.getReadTimeout(), taskConfig.getConnectTimeout(), this);
            threads.add(retryThread);
            threadsPool.execute(retryThread);
//            retryThread.start();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Pause,Error,Finished,all means thread died
     */
    private synchronized boolean isAllThreadsDead() {
        if (activeThreadsNum > 0) {
            activeThreadsNum--;
            return activeThreadsNum == 0;
        } else {
            return true;
        }
    }

    @Override
    public void onError(BaseThread errorThread, Throwable e) {
        if (isHaveRetryTime(errorThread)) {
            return;
        }

        downloadException = errorThread.getException();

        if (isAllThreadsDead()) {
            if (isCancel) {
                cancelTask();
                return;
            }
            updateProgress();
            updateTaskInfo(Sonic.STATE_ERROR);
            taskListener.onError(taskInfo, errorThread.getException());
        }
    }

    @Override
    public void onFinished(int threadId) {
        if (isAllThreadsDead()) {
            if (isCancel) {
                cancelTask();
                return;
            }

            updateProgress();

            if (downloadException == null) {
                dbManager.delete(taskInfo);
                taskInfo.setState(Sonic.STATE_FINISH);
                taskListener.onFinish(taskInfo);
            } else {
                updateTaskInfo(Sonic.STATE_ERROR);
                taskListener.onError(taskInfo, downloadException);
            }
        }
    }

    @Override
    public void onInitError(DownloadException e) {
        if (retryTime != 0) {
            retryTime--;
            initThread = new InitThread(taskInfo.getDownloadUrl(), taskInfo.getName(), taskInfo.getDirPath(), taskConfig.getReadTimeout(), taskConfig.getConnectTimeout(), this);
            initThread.start();
            return;
        }
        updateTaskInfo(Sonic.STATE_ERROR);
        taskListener.onError(taskInfo, e);
    }

    private void updateTaskInfo(int state) {
        taskInfo.setState(state);
        dbManager.updateTaskInfo(taskInfo);
    }
}
