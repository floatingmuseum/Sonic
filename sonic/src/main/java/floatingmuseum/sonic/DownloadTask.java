package floatingmuseum.sonic;


import java.util.ArrayList;
import java.util.Iterator;
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
import floatingmuseum.sonic.threads.SingleThread;
import floatingmuseum.sonic.utils.FileUtil;
import floatingmuseum.sonic.utils.LogUtil;

/**
 * Created by Floatingmuseum on 2017/3/31.
 */

public class DownloadTask implements ThreadListener {

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
    private boolean isSupportRange = true;

    private UIHandler uiHandler;

    DownloadTask(TaskInfo taskInfo, DBManager dbManager, TaskConfig taskConfig, ExecutorService threadsPool, TaskListener taskListener) {
        this.taskInfo = taskInfo;
        this.dbManager = dbManager;
        this.taskListener = taskListener;
        this.maxThreads = taskConfig.getMaxThreads();
        this.retryTime = taskConfig.getRetryTime();
        this.taskConfig = taskConfig;
        LogUtil.i(TAG, "DownloadTask...name:" + taskInfo.getName() + "...MaxThreads:" + taskConfig.getMaxThreads() + "...ProgressResponseInterval:" + taskConfig.getProgressResponseInterval() + "...FilePath:" + taskInfo.getFilePath());
        threads = new ArrayList<>();
        FileUtil.initDir(taskInfo.getDirPath());
        this.threadsPool = threadsPool;
        uiHandler = new UIHandler(this);
    }

    public TaskInfo getTaskInfo() {
        return taskInfo;
    }

    public void start() {
        LogUtil.i(TAG, "start()...ThreadName." + "..." + Thread.currentThread().getName());
        taskInfo.setState(Sonic.STATE_START);
        taskListener.onStart(taskInfo);
        taskInfo.setState(Sonic.STATE_DOWNLOADING);
        threadInfoList = dbManager.getAllThreadInfo(taskInfo.getDownloadUrl());
        if (threadInfoList.size() == 0) {//First time
            LogUtil.i(TAG, "start()...First download." + "..." + taskInfo.getName());
            initThread = new InitThread(uiHandler, taskInfo.getDownloadUrl(), taskInfo.getName(), taskInfo.getDirPath(), taskConfig.getReadTimeout(), taskConfig.getConnectTimeout());
            threadsPool.execute(initThread);
        } else {
            LogUtil.i(TAG, "start()...Resume download" + "..." + taskInfo.getName());
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
        if (singleThread != null) {
            singleThread.stopThread();
            return;
        }

        LogUtil.i(TAG, "stop()...Stop downloading Threads:" + threads.size() + taskInfo.getName() + "..." + stopAfterInitThreadDone);
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
        removeRelatedInfo();
        taskInfo.setState(Sonic.STATE_CANCEL);
        taskListener.onCancel(taskInfo);
    }

    private void removeRelatedInfo() {
        taskInfo.setProgress(0);
        taskInfo.setCurrentSize(0);
        taskInfo.setTotalSize(0);
        dbManager.delete(taskInfo);
        FileUtil.deleteFile(taskInfo.getFilePath());
    }

    private void initDownloadThread(List<ThreadInfo> threadInfoList) {
        LogUtil.i(TAG, "initDownloadThread()...TaskInfo...TotalSize:" + taskInfo.getTotalSize() + "...CurrentSize:" + taskInfo.getCurrentSize() + "..." + taskInfo.getName());
        for (ThreadInfo info : threadInfoList) {
            LogUtil.i(TAG, "initDownloadThreadInfo()...ThreadID:" + info.getId() + "...StartPosition:" + info.getStartPosition() + "...CurrentPosition:" + info.getCurrentPosition() + "...EndPosition:" + info.getEndPosition() + "..." + taskInfo.getName());
            if (info.getCurrentPosition() < info.getEndPosition()) {//Only init thread which not finished.
                LogUtil.i(TAG, "initDownloadThreadInfo()...ThreadID:" + info.getId() + "...Not Finished" + "..." + taskInfo.getName());
                DownloadThread thread = new DownloadThread(uiHandler, info, taskInfo.getDirPath(), taskInfo.getName(), dbManager, taskConfig.getReadTimeout(), taskConfig.getConnectTimeout());
                threads.add(thread);
            }
        }
        taskInfo.setProgress(getProgress());

        activeThreadsNum = threads.size();
    }

    @Override
    public void onFetchContentLength(long contentLength, boolean isSupportRange) {
        LogUtil.i(TAG, "onGetContentLength()...FileLength:" + contentLength + "..." + FileUtil.bytesToMb(contentLength) + "mb" + "..." + taskInfo.getName() + "...isSupportRange:" + isSupportRange);
        taskInfo.setTotalSize(contentLength);

        if (isSupportRange) {
            initMultipleThreads(contentLength);
        } else {
            initSingleThread();
        }
    }

    private void initMultipleThreads(long contentLength) {
        LogUtil.i(TAG, "initSingleThread()...Support multi-threads:" + taskInfo.getName());
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

    SingleThread singleThread;

    private void initSingleThread() {
        LogUtil.i(TAG, "initSingleThread()...Not support multi-threads:" + taskInfo.getName());
        isSupportRange = false;
        taskListener.onProgress(taskInfo);
        ThreadInfo info = new ThreadInfo(1, taskInfo.getDownloadUrl(), 0, taskInfo.getTotalSize(), 0, taskInfo.getTotalSize());
        singleThread = new SingleThread(uiHandler, info, taskInfo.getDirPath(), taskInfo.getName(), taskConfig.getReadTimeout(), taskConfig.getConnectTimeout());
        threadsPool.execute(singleThread);
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


        LogUtil.i(TAG, "updateProgress()...CurrentSize:" + taskInfo.getCurrentSize() + "..." + taskInfo.getProgress() + "..." + taskInfo.getState());
        taskListener.onProgress(taskInfo);
    }

    private void updateSingleThreadProgress(long currentSize) {
        taskInfo.setState(Sonic.STATE_DOWNLOADING);
        taskInfo.setCurrentSize(currentSize);
        taskInfo.setProgress(getProgress());
        taskListener.onProgress(taskInfo);
    }

    @Override
    public void onStart(ThreadInfo threadInfo) {

    }

    @Override
    public void onPause(ThreadInfo threadInfo) {
        if (!isSupportRange) {
            handleSingleThreadTask(Sonic.STATE_PAUSE);
            return;
        }
        if (isAllThreadsDead()) {
            if (isCancel) {
                cancelTask();
                return;
            }
            LogUtil.i(TAG, "onPause()...Pause Success:" + taskInfo.getState());
            updateProgress();
            LogUtil.i(TAG, "onPause()...Pause Success:" + taskInfo.getState());
            updateTaskInfo(Sonic.STATE_PAUSE);
            taskListener.onPause(taskInfo);
        }
    }

    @Override
    public void onProgress(ThreadInfo threadInfo) {
        long nowTime = System.currentTimeMillis();
        if (taskConfig.getProgressResponseInterval() == 0 || (nowTime - lastUpdateTime) > taskConfig.getProgressResponseInterval()) {
            lastUpdateTime = nowTime;
            if (!isSupportRange) {
                updateSingleThreadProgress(threadInfo.getCurrentPosition());
                return;
            }
            updateProgress();
        }
    }

    private synchronized boolean isHaveRetryTime(ThreadInfo info) {
        LogUtil.i(TAG, "isHaveRetryTime()...ThreadName." + "..." + Thread.currentThread().getName());

        if (retryTime != 0) {
            retryTime--;
            LogUtil.i(TAG, "isHaveRetryTime()..." + info.getId() + "Thread exception occurred...to retry...current retryTime:" + retryTime + "...CurrentPosition:" + info.getCurrentPosition());

            // TODO: 2018/4/12 未测试
            Iterator<DownloadThread> it = threads.iterator();
            while (it.hasNext()) {
                if (it.next().getThreadInfo().getId() == info.getId()) {
                    it.remove();
                }
            }

//            threads.remove(errorThread);
            DownloadThread retryThread = new DownloadThread(uiHandler, info, taskInfo.getDirPath(), taskInfo.getName(), dbManager, taskConfig.getReadTimeout(), taskConfig.getConnectTimeout());
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
        if (isSupportRange && activeThreadsNum > 0) {
            activeThreadsNum--;
            return activeThreadsNum == 0;
        } else {
            return true;
        }
    }

    @Override
    public void onError(ThreadInfo info, DownloadException e) {
        if (!isSupportRange) {
            handleSingleThreadTask(Sonic.STATE_ERROR);
            return;
        }

        if (isHaveRetryTime(info)) {
            return;
        }

//        downloadException = errorThread.getException();
        downloadException = e;

        if (isAllThreadsDead()) {
            if (isCancel) {
                cancelTask();
                return;
            }
            updateProgress();
            updateTaskInfo(Sonic.STATE_ERROR);
            taskListener.onError(taskInfo, e);
        }
    }

    @Override
    public void onFinished(ThreadInfo info) {
        if (!isSupportRange) {
            handleSingleThreadTask(Sonic.STATE_FINISH);
            return;
        }
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
    public void onInitThreadError(DownloadException e) {
        LogUtil.d(TAG, "onInitError()..." + e.getMessage() + "..." + e.getExceptionType() + "..." + retryTime);
        if (retryTime != 0) {
            retryTime--;
            initThread = new InitThread(uiHandler, taskInfo.getDownloadUrl(), taskInfo.getName(), taskInfo.getDirPath(), taskConfig.getReadTimeout(), taskConfig.getConnectTimeout());
            threadsPool.execute(initThread);
//            initThread.start();
            return;
        }

        if (DownloadException.TYPE_MALFORMED_URL == e.getExceptionType() || DownloadException.TYPE_FILE_NOT_FOUND == e.getExceptionType()) {
            //If exception come from wrong url or file exception.remove all related info and file.
            removeRelatedInfo();
        }
        updateTaskInfo(Sonic.STATE_ERROR);
        taskListener.onError(taskInfo, e);
    }

    public void handleSingleThreadTask(int state) {
        if (state == Sonic.STATE_FINISH) {
            updateSingleThreadProgress(taskInfo.getTotalSize());
            dbManager.delete(taskInfo);
            taskInfo.setState(Sonic.STATE_FINISH);
            taskListener.onFinish(taskInfo);
        } else {
            cancelTask();
        }
    }

    private void updateTaskInfo(int state) {
        taskInfo.setState(state);
        if (!isSupportRange) {
            dbManager.updateTaskInfo(taskInfo);
        }
    }
}
