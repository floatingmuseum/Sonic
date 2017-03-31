package floatingmuseum.sonic;


import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import floatingmuseum.sonic.db.DBManager;
import floatingmuseum.sonic.entity.TaskInfo;
import floatingmuseum.sonic.entity.ThreadInfo;
import floatingmuseum.sonic.listener.DownloadListener;
import floatingmuseum.sonic.listener.InitListener;
import floatingmuseum.sonic.listener.TaskListener;
import floatingmuseum.sonic.listener.ThreadListener;
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
    private Map<Integer, Long> blocksSize;
    private List<DownloadThread> threads;
    private List<ThreadInfo> threadInfoList;

    public DownloadTask(TaskInfo taskInfo, DBManager dbManager, int maxThreads, TaskListener taskListener) {
        this.taskInfo = taskInfo;
        this.dbManager = dbManager;
        this.maxThreads = maxThreads;
        this.taskListener = taskListener;
        threads = new ArrayList<>();
        blocksSize = new HashMap<>();
        FileUtil.initDir(taskInfo.getDirPath());
    }

    public TaskInfo getTaskInfo() {
        return taskInfo;
    }

    public void start() {
        initThreads();
    }

    public void stop() {
        for (DownloadThread thread : threads) {
            thread.stopThread();
        }
    }

    private void initThreads() {
        List<ThreadInfo> infoList = dbManager.getAllThreadInfo(taskInfo.getDownloadUrl());
        if (infoList.size() == 0) {//Means first time.
            //get file length
            new InitThread(taskInfo.getDownloadUrl(), this).start();
        } else {
            initDownloadThreadInfo(infoList);
            for (DownloadThread thread : threads) {
                thread.start();
            }
        }
    }

    private void initDownloadThreadInfo(List<ThreadInfo> threadInfoList) {
        Log.i(TAG, "initDownloadThreadInfo:" + threadInfoList.size());
        long currentSize = 0;
        for (ThreadInfo info : threadInfoList) {
            taskInfo.setTotalSize(info.getFileSize());
            // TODO: 2017/3/16 如果有线程执行完了，有的没执行完，这里就拿不到正确的size，据估计
            currentSize += (info.getCurrentPosition() - info.getStartPosition());
            blocksSize.put(info.getId(), info.getCurrentPosition() - info.getStartPosition());
            Log.i(TAG, "initDownloadThreadInfo线程" + info.getId() + "号...初始位置:" + info.getStartPosition() + "...当前位置:" + info.getCurrentPosition() + "...末尾位置:" + info.getEndPosition());
            DownloadThread thread = new DownloadThread(info, taskInfo.getDirPath(), taskInfo.getName(), dbManager, this);
            threads.add(thread);
        }
        taskInfo.setCurrentSize(currentSize);
        taskInfo.setProgress(getProgress(currentSize, taskInfo.getTotalSize()));
    }

    @Override
    public void onGetContentLength(long contentLength) {
        Log.i(TAG, "onGetContentLength总文件大小:" + contentLength + "..." + FileUtil.bytesToMb(contentLength) + "mb");
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
        initDownloadThreadInfo(threadInfoList);
        start();
    }

    private int getProgress(long currentLength, long totalLength) {
        // TODO: 2017/3/16 不准
        return (int) ((double) currentLength / (double) totalLength);
    }

    @Override
    public void onProgress(ThreadInfo threadInfo) {
        Log.i(TAG, "进度更新...Thread:" + threadInfo.getId() + "...StartPos:" + threadInfo.getStartPosition() + "...CurrentPos:" + threadInfo.getCurrentPosition() + "...EndPosition:" + threadInfo.getEndPosition());
    }

    @Override
    public void onFinished(int threadId) {
        maxThreads--;
        if (maxThreads == 0) {
            //删除所有记录
            dbManager.delete(DBManager.THREADS_TABLE_NAME, taskInfo.getDownloadUrl());
            dbManager.delete(DBManager.TASKS_TABLE_NAME, taskInfo.getDownloadUrl());
            Log.d(TAG, "onFinished...下载结束");
        }
    }

    @Override
    public void onError() {

    }
}
