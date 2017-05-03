package floatingmuseum.sonic.threads;

import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Map;

import floatingmuseum.sonic.DownloadException;
import floatingmuseum.sonic.entity.ThreadInfo;
import floatingmuseum.sonic.listener.ThreadListener;

/**
 * Created by Floatingmuseum on 2017/5/2.
 */

public abstract class BaseThread extends Thread {
    private static final String TAG = DownloadThread.class.getName();

    protected boolean stopThread = false;

    //下载文件夹路径
    protected String dirPath;
    protected String fileName;
    protected int readTimeout;
    protected int connectTimeout;
    protected ThreadInfo threadInfo;
    protected ThreadListener listener;

    protected boolean isDownloading = false;
    protected boolean isPaused = false;
    protected boolean isFailed = false;
    protected boolean isFinished = false;
    protected DownloadException downloadException;

    @Override
    public void run() {
        Log.i(TAG, threadInfo.getId() + "号线程开始工作" + "..." + fileName);
        isDownloading = true;
        HttpURLConnection connection = null;
        RandomAccessFile raf = null;
        InputStream inputStream = null;

        //获取文件长度
        URL url = null;
        try {
            url = new URL(threadInfo.getUrl());
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(connectTimeout);
            connection.setReadTimeout(readTimeout);
            connection.setRequestMethod("GET");
            setHttpHeader(getHttpHeaders(), connection);

            long currentPosition = threadInfo.getCurrentPosition();
            int responseCode = connection.getResponseCode();
            if (responseCode == getResponseCode()) {

                raf = getRandomAccessFile();
                inputStream = connection.getInputStream();
                byte[] buffer = new byte[1024 * 4];
                int len;
                while ((len = inputStream.read(buffer)) != -1) {
                    raf.write(buffer, 0, len);
                    currentPosition += len;
                    threadInfo.setCurrentPosition(currentPosition);

                    listener.onProgress(threadInfo);

                    if (stopThread) {
                        //更新数据库,停止循环
                        updateDB();
                        isPaused = true;
                        isDownloading = false;
                        listener.onPause(threadInfo);
                        return;
                    }
                    if (currentPosition > threadInfo.getEndPosition()) {
                        break;
                    }
                }
                //当前区块下载完成
                Log.i(TAG, threadInfo.getId() + "号线程完成工作" + "..." + fileName);
                updateDB();
                isFinished = true;
                isDownloading = false;
                listener.onFinished(threadInfo.getId());

            } else {
                isFailed = true;
                isDownloading = false;
                Log.i(TAG, threadInfo.getId() + "号线程出现异常" + "..." + fileName + "..." + responseCode);
                downloadException = new DownloadException("DownloadThread failed", responseCode);
                listener.onError(this, downloadException);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            downloadException = new DownloadException("DownloadThread failed", e);
            listener.onError(this, downloadException);
        } catch (ProtocolException e) {
            e.printStackTrace();
            downloadException = new DownloadException("DownloadThread failed", e);
            listener.onError(this, downloadException);
        } catch (IOException e) {
            e.printStackTrace();
            downloadException = new DownloadException("DownloadThread failed", e);
            listener.onError(this, downloadException);
        }finally {
            try {
                if (connection != null) {
                    connection.disconnect();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
                if (raf != null) {
                    raf.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                downloadException = new DownloadException("DownloadThread failed", e);
                listener.onError(this, downloadException);
            }
        }
    }

    private void setHttpHeader(Map<String, String> headers, HttpURLConnection connection) {
        if (headers != null) {
            for (String key : headers.keySet()) {
                connection.setRequestProperty(key, headers.get(key));
            }
        }
    }

    public DownloadException getException() {
        return downloadException;
    }

    public ThreadInfo getThreadInfo() {
        return threadInfo;
    }

    public void stopThread() {
        stopThread = true;
    }

    public boolean isDownloading() {
        return isDownloading;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public boolean isFailed() {
        return isFailed;
    }

    public boolean isFinished() {
        return isFinished;
    }


    protected abstract RandomAccessFile getRandomAccessFile() throws IOException;

    protected abstract void updateDB();

    protected abstract Map<String, String> getHttpHeaders();

    protected abstract int getResponseCode();
}
