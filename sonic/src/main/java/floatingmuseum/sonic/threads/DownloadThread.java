package floatingmuseum.sonic.threads;

import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.IllegalFormatCodePointException;
import java.util.Map;

import floatingmuseum.sonic.DownloadException;
import floatingmuseum.sonic.db.DBManager;
import floatingmuseum.sonic.entity.ThreadInfo;
import floatingmuseum.sonic.listener.ThreadListener;

/**
 * Created by Floatingmuseum on 2017/3/31.
 */

public class DownloadThread extends BaseThread {

//    private static final String TAG = DownloadThread.class.getName();
//
//    private boolean stopThread = false;
//
//    //下载文件夹路径
//    private String dirPath;
//    private String fileName;
//    private int readTimeout;
//    private int connectTimeout;
//    private ThreadInfo threadInfo;
    private DBManager dbManager;
//    private ThreadListener listener;

    public DownloadThread(ThreadInfo threadInfo, String dirPath, String fileName, DBManager dbManager, int readTimeout, int connectTimeout, ThreadListener listener) {
        this.threadInfo = threadInfo;
        this.dirPath = dirPath;
        this.fileName = fileName;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.listener = listener;
        this.dbManager = dbManager;
    }

//    @Override
//    public void run() {
//        Log.i(TAG, threadInfo.getId() + "号线程开始工作" + "..." + fileName);
//        isDownloading = true;
//        HttpURLConnection connection = null;
//        RandomAccessFile randomAccessFile = null;
//        InputStream inputStream = null;
//        try {
//            //获取文件长度
//            URL url = new URL(threadInfo.getUrl());
//            connection = (HttpURLConnection) url.openConnection();
//
//            connection.setConnectTimeout(connectTimeout);
//            connection.setReadTimeout(readTimeout);
//            connection.setRequestMethod("GET");
//
//            long currentPosition = threadInfo.getCurrentPosition();
//
//            //起始位置是线程所下载区块的初始位置+已完成大小
//            Log.i(TAG, "DownloadThread:" + threadInfo.getId() + " ...下载范围:" + threadInfo.getCurrentPosition() + "..." + threadInfo.getEndPosition() + "..." + fileName);
//            connection.setRequestProperty("Range", "bytes=" + threadInfo.getCurrentPosition() + "-" + threadInfo.getEndPosition());
//
//            long contentLength = -1;
//            int responseCode = connection.getResponseCode();
//            Log.i(TAG, "DownloadThread:" + threadInfo.getId() + "...Response code:" + responseCode + "..." + fileName);
//            if (responseCode == 200 || responseCode == 206) {
//                contentLength = connection.getContentLength();
//                Log.i(TAG, "获取文件长度...区块长度:" + contentLength + "..." + fileName);
//                if (contentLength <= 0) {
//                    return;
//                }
//                File file = new File(dirPath, fileName);
//                randomAccessFile = new RandomAccessFile(file, "rwd");
//                //设置写入位置
//                //seek方法 在读写的时候跳过设置的字节数,从下一个字节开始读写.例如seek(100),从101字节开始读写
//                //在这里也就是从已完成部分的末尾继续写
//                randomAccessFile.seek(currentPosition);
//                //开始写入
//                inputStream = connection.getInputStream();
//                byte[] buffer = new byte[1024 * 4];
//                int len;
//                while ((len = inputStream.read(buffer)) != -1) {
//                    randomAccessFile.write(buffer, 0, len);
//                    currentPosition += len;
//                    threadInfo.setCurrentPosition(currentPosition);
//
//                    listener.onProgress(threadInfo);
//
//                    if (stopThread) {
//                        //更新数据库,停止循环
//                        dbManager.updateThreadInfo(threadInfo);
//                        isPaused = true;
//                        isDownloading = false;
//                        listener.onPause(threadInfo);
//                        return;
//                    }
//                    if (currentPosition > threadInfo.getEndPosition()) {
//                        break;
//                    }
//                }
//                //当前区块下载完成
//                Log.i(TAG, threadInfo.getId() + "号线程完成工作" + "..." + fileName);
//                dbManager.updateThreadInfo(threadInfo);
//                isFinished = true;
//                isDownloading = false;
//                listener.onFinished(threadInfo.getId());
//            } else {
//                isFailed = true;
//                isDownloading = false;
//                Log.i(TAG, threadInfo.getId() + "号线程出现异常" + "..." + fileName + "..." + responseCode);
//                downloadException = new DownloadException("DownloadThread failed", responseCode);
//                listener.onError(this, new IllegalStateException("DownloadThread Request failed with response code:" + responseCode));
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            Log.i(TAG, threadInfo.getId() + "号线程出现异常" + "..." + fileName);
//            dbManager.updateThreadInfo(threadInfo);
//            isFailed = true;
//            isDownloading = false;
//            downloadException = new DownloadException("DownloadThread failed", e);
//            listener.onError(this, e);
//        } finally {
//            try {
//                if (connection != null) {
//                    connection.disconnect();
//                }
//                if (inputStream != null) {
//                    inputStream.close();
//                }
//                if (randomAccessFile != null) {
//                    randomAccessFile.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    private boolean isDownloading = false;
//    private boolean isPaused = false;
//    private boolean isFailed = false;
//    private boolean isFinished = false;
//    private DownloadException downloadException;
//
//    public boolean isDownloading() {
//        return isDownloading;
//    }
//
//    public boolean isPaused() {
//        return isPaused;
//    }
//
//    public boolean isFailed() {
//        return isFailed;
//    }
//
//    public boolean isFinished() {
//        return isFinished;
//    }
//
//    public DownloadException getException() {
//        return downloadException;
//    }
//
//    public ThreadInfo getThreadInfo() {
//        return threadInfo;
//    }
//
//    public void stopThread() {
//        stopThread = true;
//    }


    @Override
    protected RandomAccessFile getRandomAccessFile() throws IOException {
        File file = new File(dirPath, fileName);
        RandomAccessFile raf = new RandomAccessFile(file, "rwd");
        raf.seek(threadInfo.getCurrentPosition());
        return raf;
    }

    @Override
    protected void updateDB() {
        dbManager.updateThreadInfo(threadInfo);
    }

    @Override
    protected Map<String, String> getHttpHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Range", "bytes=" + threadInfo.getCurrentPosition() + "-" + threadInfo.getEndPosition());
        return headers;
    }

    @Override
    protected int getResponseCode() {
        return HttpURLConnection.HTTP_PARTIAL;
    }
}
