package floatingmuseum.sonic;


import android.graphics.drawable.StateListDrawable;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.util.ArrayList;
import java.util.List;

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
    private boolean stopAfterInitThreadDone = false;

    public DownloadTask(TaskInfo taskInfo, DBManager dbManager, int maxThreads, int progressResponseTime, TaskListener taskListener) {
        this.taskInfo = taskInfo;
        this.dbManager = dbManager;
        this.maxThreads = maxThreads;
        this.taskListener = taskListener;
        this.progressResponseTime = progressResponseTime;
        Log.i(TAG, "任务详情...名称:" + taskInfo.getName() + "...最大线程数:" + maxThreads + "...进度反馈最小时间间隔:" + progressResponseTime + "...文件存储路径:" + taskInfo.getFilePath());
        threads = new ArrayList<>();
        FileUtil.initDir(taskInfo.getDirPath());
    }

    public TaskInfo getTaskInfo() {
        return taskInfo;
    }

    public void start() {
        taskInfo.setState(Sonic.STATE_START);
        taskListener.onStart(taskInfo);
        alreadyStopThreads = 0;
        threadInfoList = dbManager.getAllThreadInfo(taskInfo.getDownloadUrl());
        if (threadInfoList.size() == 0) {//First time
            Log.i(TAG, "start()...第一次下载此任务." + "..." + taskInfo.getName());
            new InitThread(taskInfo.getDownloadUrl(), this).start();
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
        Log.i(TAG, "stop()...停止下载线程:" + threads.size() + taskInfo.getName() + "..." + stopAfterInitThreadDone + "..." + alreadyStopThreads);
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

    private void initDownloadThread(List<ThreadInfo> threadInfoList) {
        if (stopAfterInitThreadDone) {
            taskInfo.setState(Sonic.STATE_PAUSE);
            dbManager.updateTaskInfo(taskInfo);
            taskListener.onProgress(taskInfo);
            return;
        }
        Log.i(TAG, "TaskInfo...TotalSize:" + taskInfo.getTotalSize() + "...CurrentSize:" + taskInfo.getCurrentSize() + "..." + taskInfo.getName());
        for (ThreadInfo info : threadInfoList) {
            Log.i(TAG, "initDownloadThreadInfo线程" + info.getId() + "号...初始位置:" + info.getStartPosition() + "...当前位置:" + info.getCurrentPosition() + "...末尾位置:" + info.getEndPosition() + "..." + taskInfo.getName());
            if (info.getCurrentPosition() < info.getEndPosition()) {//只初始化还没完成的线程
                Log.i(TAG, info.getId() + "号继续工作" + "..." + taskInfo.getName());
                DownloadThread thread = new DownloadThread(info, taskInfo.getDirPath(), taskInfo.getName(), dbManager, this);
                threads.add(thread);
            } else {
                Log.i(TAG, info.getId() + "号已完成工作，休息" + "..." + taskInfo.getName());
                maxThreads--;
            }
        }
        Log.i(TAG, "TaskInfo...TotalSize:" + taskInfo.getTotalSize() + "...CurrentSize:" + taskInfo.getCurrentSize());
        taskInfo.setProgress(getProgress());
    }

    @Override
    public void onGetContentLength(long contentLength) {
        Log.i(TAG, "onGetContentLength总文件大小:" + contentLength + "..." + FileUtil.bytesToMb(contentLength) + "mb" + "..." + taskInfo.getName());
        taskInfo.setTotalSize(contentLength);
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
        taskListener.onProgress(taskInfo);
    }

    @Override
    public void onPause(ThreadInfo threadInfo) {
        boolean isAllPaused = true;
        for (DownloadThread thread : threads) {
            //线程状态处于暂停或者失败,都表明线程停止了
            Log.i(TAG, "onPause...线程状态:...当前ID:" + threadInfo.getId() + "...ID:" + thread.getThreadInfo().getId() + "...Failed:" + thread.isFailed() + "...Paused:" + thread.isPaused() + "...Finished:" + thread.isFinished());
            if (thread.isDownloading()) {
                isAllPaused = false;
            }
        }

        if (isAllPaused) {
            updateProgress();
            updateTaskInfo(Sonic.STATE_PAUSE);
            taskListener.onPause(taskInfo);
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

    private int retryTime;

    @Override
    public void onError(ThreadInfo threadInfo, Throwable e) {

        if (retryTime != 0) {
            retryTime--;
            // TODO: 2017/4/19 还有retry可以使用时继续retry 这里可能会有多线程同步的问题?
            return;
        }

        boolean isAllFailed = true;
        DownloadException downloadException = null;
        for (DownloadThread thread : threads) {
            //线程状态处于暂停或者失败,都表明线程停止了
            if (thread.isDownloading()) {
                isAllFailed = false;
            } else {
                downloadException = thread.getException();
            }
        }

        if (isAllFailed) {
            updateProgress();
            updateTaskInfo(Sonic.STATE_ERROR);
            taskListener.onError(taskInfo, downloadException);
        }
    }

    @Override
    public void onFinished(int threadId) {
        boolean isHasError = false;
        DownloadException downloadException = null;
        for (DownloadThread thread : threads) {
            Log.i(TAG, "下载完成:" + threadId + "...其他线程状态:" + thread.getThreadInfo().getId() + "...isFailed:" + thread.isFailed() + "...isFinished:" + thread.isFinished());
            //只有当所有线程处于失败或者完成状态时(俩种状态可同时存在,因为当其中一条线程出错时,其他线程继续工作,直到工作结束,再结算失败状态)
            if (thread.isFailed() || thread.isFinished()) {
                if (thread.isFailed()) {
                    downloadException = thread.getException();
                    isHasError = true;
                }
            } else {
                return;
            }
        }

        updateProgress();

        //所有线程停止状态下含有错误,回调onError
        if (isHasError) {
            taskInfo.setState(Sonic.STATE_ERROR);
            taskListener.onError(taskInfo, downloadException);
        } else {
            dbManager.delete(DBManager.THREADS_TABLE_NAME, taskInfo.getDownloadUrl());
            dbManager.delete(DBManager.TASKS_TABLE_NAME, taskInfo.getDownloadUrl());
            taskInfo.setState(Sonic.STATE_FINISH);
            taskListener.onFinish(taskInfo);
        }
    }

    @Override
    public void onInitError(DownloadException e) {
        updateTaskInfo(Sonic.STATE_ERROR);
        // TODO: 2017/4/19 把初始线程里的异常也改成DownloadException,然后下面这里别强转
        taskListener.onError(taskInfo, e);
    }

    private void updateTaskInfo(int state) {
        taskInfo.setState(state);
        dbManager.updateTaskInfo(taskInfo);
    }
}
