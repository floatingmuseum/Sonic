package floatingmuseum.sonic;


import android.util.Log;

import java.util.ArrayList;
import java.util.List;

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
    private DownloadException downloadException;

    public DownloadTask(TaskInfo taskInfo, DBManager dbManager, TaskConfig taskConfig, TaskListener taskListener) {
        this.taskInfo = taskInfo;
        this.dbManager = dbManager;
        this.taskListener = taskListener;
        this.maxThreads = taskConfig.getMaxThreads();
        this.retryTime = taskConfig.getRetryTime();
        this.taskConfig = taskConfig;
        Log.i(TAG, "任务详情...名称:" + taskInfo.getName() + "...最大线程数:" + taskConfig.getMaxThreads() + "...进度反馈最小时间间隔:" + taskConfig.getProgressResponseInterval() + "...文件存储路径:" + taskInfo.getFilePath());
        threads = new ArrayList<>();
        FileUtil.initDir(taskInfo.getDirPath());
    }

    public TaskInfo getTaskInfo() {
        return taskInfo;
    }

    public void start() {
        taskInfo.setState(Sonic.STATE_START);
        taskListener.onStart(taskInfo);
        threadInfoList = dbManager.getAllThreadInfo(taskInfo.getDownloadUrl());
        if (threadInfoList.size() == 0) {//First time
            Log.i(TAG, "start()...第一次下载此任务." + "..." + taskInfo.getName());
            new InitThread(taskInfo.getDownloadUrl(), taskInfo.getName(), taskInfo.getDirPath(), this).start();
        } else {
            Log.i(TAG, "start()...继续下载此任务" + "..." + taskInfo.getName());
            initDownloadThread(threadInfoList);
            for (DownloadThread thread : threads) {
                thread.start();
            }
        }
    }

    public void stop() {
        /**
         * 等于0说明，是第一次下载，且处于获取任务长度的阶段,如果此时暂停，没有效果。获取长度后会继续下载
         * 所以设置一个变量来控制，当长度获取完毕后，检查变量，可以获知用户是否在获取长度阶段点击了暂停
         */
        Log.i(TAG, "stop()...停止下载线程:" + threads.size() + taskInfo.getName() + "..." + stopAfterInitThreadDone);
        if (threads.size() == 0) {
            stopAfterInitThreadDone = true;
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
        taskListener.onCancel(taskInfo);
    }

    private void initDownloadThread(List<ThreadInfo> threadInfoList) {
        if (stopAfterInitThreadDone) {
            if (isCancel) {
                cancelTask();
                return;
            }
            taskInfo.setState(Sonic.STATE_PAUSE);
            dbManager.updateTaskInfo(taskInfo);
            taskListener.onProgress(taskInfo);
            return;
        }
        Log.i(TAG, "TaskInfo...TotalSize:" + taskInfo.getTotalSize() + "...CurrentSize:" + taskInfo.getCurrentSize() + "..." + taskInfo.getName());
        for (ThreadInfo info : threadInfoList) {
            Log.i(TAG, "initDownloadThreadInfo线程" + info.getId() + "号...初始位置:" + info.getStartPosition() + "...当前位置:" + info.getCurrentPosition() + "...末尾位置:" + info.getEndPosition() + "..." + taskInfo.getName());
            if (info.getCurrentPosition() < info.getEndPosition()) {//只初始化还没完成的线程
                Log.i(TAG, info.getId() + "号开始工作" + "..." + taskInfo.getName());
                DownloadThread thread = new DownloadThread(info, taskInfo.getDirPath(), taskInfo.getName(), dbManager, taskConfig.getReadTimeout(), taskConfig.getConnectTimeout(), this);
                threads.add(thread);
            }
        }
        Log.i(TAG, "TaskInfo...TotalSize:" + taskInfo.getTotalSize() + "...CurrentSize:" + taskInfo.getCurrentSize());
        taskInfo.setProgress(getProgress());

        activeThreadsNum = threads.size();
    }

    @Override
    public void onGetContentLength(long contentLength, boolean isSupportRange) {
        Log.i(TAG, "onGetContentLength总文件大小:" + contentLength + "..." + FileUtil.bytesToMb(contentLength) + "mb" + "..." + taskInfo.getName() + "...isSupportRange:" + isSupportRange);
        taskInfo.setTotalSize(contentLength);

        if (isSupportRange) {
            initMultipleThreads(contentLength);
        } else {
            initSingleThread();
        }
    }

    private void initMultipleThreads(long contentLength) {
        dbManager.updateTaskInfo(taskInfo);
        taskListener.onProgress(taskInfo);
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
        Log.i(TAG, "updateProgress...CurrentSize:" + taskInfo.getCurrentSize() + "..." + taskInfo.getProgress() + "..." + taskInfo.getState());
        taskListener.onProgress(taskInfo);
    }

    @Override
    public void onPause(ThreadInfo threadInfo) {
        if (isAllThreadsDead()) {
            if (isCancel) {
                cancelTask();
                return;
            }
            Log.i(TAG, "onPause...暂停成功:" + taskInfo.getState());
            updateProgress();
            Log.i(TAG, "onPause...暂停成功:" + taskInfo.getState());
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
            Log.i(TAG, info.getId() + "号线程发生错误...进行重试...当前剩余重试次数:" + retryTime + "...当前位置:" + info.getCurrentPosition());
            threads.remove(errorThread);
            DownloadThread retryThread = new DownloadThread(info, taskInfo.getDirPath(), taskInfo.getName(), dbManager, taskConfig.getReadTimeout(), taskConfig.getConnectTimeout(), this);
            threads.add(retryThread);
            retryThread.start();
            return true;
        } else {
            return false;
        }
    }

    /**
     * 无论是暂停,异常,完成区块下载都表明线程运行完毕死亡.
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

            if (downloadException==null) {
                dbManager.delete(taskInfo);
                taskInfo.setState(Sonic.STATE_FINISH);
                taskListener.onFinish(taskInfo);
            }else{
                taskInfo.setState(Sonic.STATE_ERROR);
                taskListener.onError(taskInfo, downloadException);
            }
        }
    }

    @Override
    public void onInitError(DownloadException e) {
        if (retryTime != 0) {
            retryTime--;
            new InitThread(taskInfo.getDownloadUrl(), taskInfo.getName(), taskInfo.getDirPath(), this).start();
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
