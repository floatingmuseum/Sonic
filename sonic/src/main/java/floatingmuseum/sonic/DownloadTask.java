package floatingmuseum.sonic;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import floatingmuseum.sonic.db.DBManager;
import floatingmuseum.sonic.entity.TaskInfo;
import floatingmuseum.sonic.entity.ThreadInfo;
import floatingmuseum.sonic.listener.TaskListener;
import floatingmuseum.sonic.listener.ThreadListener;
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
    private boolean stopDownloading = false;
    private boolean isCancel = false;
    private int activeThreadsNum;
    private ExecutorService threadsPool;
    private DownloadException downloadException;
    private InitThread initThread;
    private boolean isSupportRange = true;
    private Map<Integer, Boolean> activeMap = new HashMap<>();

    private UIHandler uiHandler;

    DownloadTask(TaskInfo taskInfo, DBManager dbManager, TaskConfig taskConfig, ExecutorService threadsPool, TaskListener taskListener) {
        this.taskInfo = taskInfo;
        this.taskInfo.setTaskHashcode(this.hashCode());
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
        LogUtil.i(TAG, "start()...call..." + this.hashCode());
        LogUtil.i(TAG, "start()...ThreadName." + "..." + Thread.currentThread().getName());
        taskInfo.setState(Sonic.STATE_START);
        taskListener.onStart(taskInfo);
        taskInfo.setState(Sonic.STATE_DOWNLOADING);
        threadInfoList = dbManager.getAllThreadInfo(taskInfo.getDownloadUrl());
        if (threadInfoList.size() == 0) {//First time
            LogUtil.i(TAG, "start()...First download." + "..." + taskInfo.getName());
            initThread = new InitThread(uiHandler, taskInfo.getDownloadUrl(), taskInfo.getName(), taskInfo.getDirPath(), taskConfig.getReadTimeout(), taskConfig.getConnectTimeout(), this.hashCode());
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
                taskListener.onFinish(taskInfo, this.hashCode());
            }
        }
    }

    public void stop() {
        LogUtil.i(TAG, "stop()...call..." + this.hashCode());
        if (singleThread != null) {
            singleThread.stopThread();
            return;
        }

        LogUtil.i(TAG, "stop()...Stop downloading Threads:" + threads.size() + taskInfo.getName() + "..." + stopDownloading);
        stopDownloading = true;
        if (threads.size() == 0) {
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
        taskListener.onCancel(taskInfo, this.hashCode());
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
                DownloadThread thread = new DownloadThread(uiHandler, info, taskInfo.getDirPath(), taskInfo.getName(), dbManager, taskConfig.getReadTimeout(), taskConfig.getConnectTimeout(), this.hashCode());
                threads.add(thread);
            }
        }
        taskInfo.setProgress(getProgress());

        activeThreadsNum = threads.size();
        for (int i = 0; i < threads.size(); i++) {
            activeMap.put(i+1, true);
        }
    }

    @Override
    public void onFetchContentLength(long contentLength, boolean isSupportRange, int hashCode) {
        LogUtil.i(TAG, "onFetchContentLength()...call..." + this.hashCode() + "...threadTask:" + hashCode);
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

        if (stopDownloading) {
            if (isCancel) {
                cancelTask();
                return;
            }
            taskInfo.setState(Sonic.STATE_PAUSE);
            dbManager.updateTaskInfo(taskInfo);
            LogUtil.i(TAG, "initSingleThread():" + this.hashCode());
            taskListener.onPause(taskInfo, this.hashCode());
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
        return (int) (((float) taskInfo.getCurrentSize() / (float) taskInfo.getTotalSize()) * 100);
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
    public void onInitThreadPause(int hashCode) {
        LogUtil.i(TAG, "onInitThreadPause()...call..." + this.hashCode() + "...threadTask:" + hashCode);
        updateTaskInfo(Sonic.STATE_PAUSE);
        taskListener.onPause(taskInfo, this.hashCode());
    }

    @Override
    public void onStart(ThreadInfo threadInfo, int hashCode) {
        LogUtil.i(TAG, "onStart()...call..." + this.hashCode() + "...threadTask:" + hashCode + "...threadID:" + threadInfo.getId());
    }

    @Override
    public void onPause(ThreadInfo threadInfo, int hashCode) {
        LogUtil.i(TAG, "onPause()...call..." + this.hashCode() + "...threadTask:" + hashCode + "...threadID:" + threadInfo.getId());
        if (!isSupportRange) {
            handleSingleThreadTask(Sonic.STATE_PAUSE);
            return;
        }
        if (isAllThreadsDead(threadInfo.getId())) {
            if (isCancel) {
                cancelTask();
                return;
            }
            LogUtil.i(TAG, "onPause()...Pause Success:" + taskInfo.getState() + "..." + this.hashCode() + "...threadTask:" + hashCode + "...threadID:" + threadInfo.getId());
            updateProgress();
            LogUtil.i(TAG, "onPause()...Pause Success:" + taskInfo.getState() + "..." + this.hashCode() + "...threadTask:" + hashCode + "...threadID:" + threadInfo.getId());
            updateTaskInfo(Sonic.STATE_PAUSE);
            taskListener.onPause(taskInfo, this.hashCode());
        }
    }

    @Override
    public void onProgress(ThreadInfo threadInfo, int hashCode) {
        LogUtil.i(TAG, "onProgress()...call..." + this.hashCode() + "...threadTask:" + hashCode + "...threadID:" + threadInfo.getId());
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

            Iterator<DownloadThread> it = threads.iterator();
            while (it.hasNext()) {
                if (it.next().getThreadInfo().getId() == info.getId()) {
                    it.remove();
                }
            }

//            threads.remove(errorThread);
            DownloadThread retryThread = new DownloadThread(uiHandler, info, taskInfo.getDirPath(), taskInfo.getName(), dbManager, taskConfig.getReadTimeout(), taskConfig.getConnectTimeout(), this.hashCode());
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

    private boolean isAllThreadsDead(int deadID) {
        LogUtil.i(TAG, "isAllThreadsDead()...call..." + this.hashCode() + "...deadID:" + deadID);
        if (isSupportRange) {
            activeMap.put(deadID, false);
            for (Integer id : activeMap.keySet()) {
                boolean inactive = activeMap.get(id);
                LogUtil.i(TAG, "isAllThreadsDead()...call..." + this.hashCode() + "...deadID:" + deadID + "...inactive:"+inactive+"...inactiveID:"+id);
                if (inactive) {
                    LogUtil.i(TAG, "isAllThreadsDead()...call..." + this.hashCode() + "...deadID:" + deadID + "...仍有线程活动");
                    return false;
                }
            }
            LogUtil.i(TAG, "isAllThreadsDead()...call..." + this.hashCode() + "...deadID:" + deadID + "...无线程活动");
            return true;
        } else {
            LogUtil.i(TAG, "isAllThreadsDead()...call..." + this.hashCode() + "...deadID:" + deadID + "...无线程活动");
            return true;
        }
    }

    @Override
    public void onError(ThreadInfo threadInfo, DownloadException e, int hashCode) {
        LogUtil.i(TAG, "onError()...call..." + this.hashCode() + "...stopDownloading:" + stopDownloading + "...threadTask:" + hashCode + "...threadID:" + threadInfo.getId() + "..." + e.getMessage() + "..." + e.getExceptionType());
        if (!stopDownloading && isHaveRetryTime(threadInfo)) {
            return;
        }

        if (!isSupportRange) {
            handleSingleThreadTask(Sonic.STATE_ERROR);
            return;
        }

        downloadException = e;

        if (isAllThreadsDead(threadInfo.getId())) {
            if (isCancel) {
                cancelTask();
                return;
            }
            updateProgress();
            updateTaskInfo(Sonic.STATE_ERROR);
            taskListener.onError(taskInfo, e, this.hashCode());
        }
    }

    @Override
    public void onFinished(ThreadInfo threadInfo, int hashCode) {
        LogUtil.i(TAG, "onFinished()...call..." + this.hashCode() + "...threadTask:" + hashCode + "...threadID:" + threadInfo.getId());
        if (!isSupportRange) {
            handleSingleThreadTask(Sonic.STATE_FINISH);
            return;
        }
        if (isAllThreadsDead(threadInfo.getId())) {
            if (isCancel) {
                cancelTask();
                return;
            }

            updateProgress();

            if (downloadException == null) {
                dbManager.delete(taskInfo);
                taskInfo.setState(Sonic.STATE_FINISH);
                taskListener.onFinish(taskInfo, this.hashCode());
            } else {
                updateTaskInfo(Sonic.STATE_ERROR);
                taskListener.onError(taskInfo, downloadException, this.hashCode());
            }
        }
    }

    @Override
    public void onInitThreadError(DownloadException e, int hashCode) {
        LogUtil.i(TAG, "onInitError()...call..." + this.hashCode() + "...threadTask:" + hashCode);
        LogUtil.d(TAG, "onInitError()..." + e.getMessage() + "..." + e.getExceptionType() + "..." + retryTime);
        if (!stopDownloading && retryTime != 0) {
            retryTime--;
            initThread = new InitThread(uiHandler, taskInfo.getDownloadUrl(), taskInfo.getName(), taskInfo.getDirPath(), taskConfig.getReadTimeout(), taskConfig.getConnectTimeout(), this.hashCode());
            threadsPool.execute(initThread);
//            initThread.start();
            return;
        }

        if (DownloadException.TYPE_MALFORMED_URL == e.getExceptionType() || DownloadException.TYPE_FILE_NOT_FOUND == e.getExceptionType()) {
            //If exception come from wrong url or file exception.remove all related info and file.
            removeRelatedInfo();
        }
        updateTaskInfo(Sonic.STATE_ERROR);
        taskListener.onError(taskInfo, e, this.hashCode());
    }

    public void handleSingleThreadTask(int state) {
        if (state == Sonic.STATE_FINISH) {
            updateSingleThreadProgress(taskInfo.getTotalSize());
            dbManager.delete(taskInfo);
            taskInfo.setState(Sonic.STATE_FINISH);
            taskListener.onFinish(taskInfo, this.hashCode());
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
