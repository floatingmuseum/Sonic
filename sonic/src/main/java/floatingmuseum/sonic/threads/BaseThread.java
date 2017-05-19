package floatingmuseum.sonic.threads;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Map;

import floatingmuseum.sonic.DownloadException;
import floatingmuseum.sonic.entity.ThreadInfo;
import floatingmuseum.sonic.listener.ThreadListener;
import floatingmuseum.sonic.utils.LogUtil;

/**
 * Created by Floatingmuseum on 2017/5/2.
 */

public abstract class BaseThread implements Runnable {
    private static final String TAG = DownloadThread.class.getName();

    protected boolean stopThread = false;

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
        LogUtil.i(TAG, threadInfo.getId() + "Thread start work" + "..." + fileName);
        isDownloading = true;
        HttpURLConnection connection = null;
        RandomAccessFile raf = null;
        InputStream inputStream = null;

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
                    if (stopThread) {
                        updateDB();
                        listener.onPause(threadInfo);
                        return;
                    }
                    raf.write(buffer, 0, len);
                    currentPosition += len;
                    threadInfo.setCurrentPosition(currentPosition);
                    listener.onProgress(threadInfo);

                    if (currentPosition > threadInfo.getEndPosition()) {
                        break;
                    }
                }
                //This thread has finished its work.
                LogUtil.i(TAG, threadInfo.getId() + "Thread has finished its work" + "..." + fileName);
                updateDB();
                isFinished = true;
                isDownloading = false;
                listener.onFinished(threadInfo.getId());

            } else {
                isFailed = true;
                isDownloading = false;
                LogUtil.i(TAG, threadInfo.getId() + "Thread exception occurred" + "..." + fileName + "..." + responseCode);
                downloadException = new DownloadException(DownloadException.TYPE_RESPONSE_CODE, "DownloadThread failed", responseCode);
                listener.onError(this, downloadException);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            downloadException = new DownloadException(DownloadException.TYPE_MALFORMED_URL, "DownloadThread failed", e);
            listener.onError(this, downloadException);
        } catch (ProtocolException e) {
            e.printStackTrace();
            downloadException = new DownloadException(DownloadException.TYPE_PROTOCOL, "DownloadThread failed", e);
            listener.onError(this, downloadException);
        } catch (InterruptedIOException e) {
            e.printStackTrace();
            updateDB();
            if (stopThread) {
                LogUtil.i(TAG, threadInfo.getId() + "Thread stop by user interrupted.");
                listener.onPause(threadInfo);
            } else {
                LogUtil.i(TAG, threadInfo.getId() + "Thread stop by auto interrupted.");
                downloadException = new DownloadException(DownloadException.TYPE_INTERRUPTED_IO, "DownloadThread failed", e);
                listener.onError(this, downloadException);
            }
        } catch (IOException e) {
            e.printStackTrace();
            downloadException = new DownloadException(DownloadException.TYPE_IO, "DownloadThread failed", e);
            listener.onError(this, downloadException);
        } finally {
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
                downloadException = new DownloadException(DownloadException.TYPE_IO, "DownloadThread failed", e);
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
        LogUtil.i(TAG, "stopThread()...interrupted:" + Thread.interrupted());
//        if (!Thread.interrupted()) {
//            interrupt();
//        }
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
