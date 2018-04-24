package floatingmuseum.sonic.threads;

import android.os.Message;

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
import floatingmuseum.sonic.UIHandler;
import floatingmuseum.sonic.entity.ThreadInfo;
import floatingmuseum.sonic.entity.UIMessage;
import floatingmuseum.sonic.utils.LogUtil;

/**
 * Created by Floatingmuseum on 2017/5/2.
 */

public abstract class BaseThread implements Runnable {
    private static final String TAG = DownloadThread.class.getName();

    protected boolean stopThread = false;

    protected String dirPath;
    protected String fileName;
    protected String filePath;
    protected int readTimeout;
    protected int connectTimeout;
    protected ThreadInfo threadInfo;
    protected UIHandler uiHandler;
    protected int hashCode;

    protected boolean isDownloading = false;
    protected boolean isPaused = false;
    protected boolean isFailed = false;
    protected boolean isFinished = false;
    protected boolean isOver = false;
    protected final int DEFAULT_BUFFER_SIZE = 4 * 1024;
    protected DownloadException downloadException;

    @Override
    public void run() {
        LogUtil.i(TAG, threadInfo.getId() + "Thread start work" + "..." + fileName+"...hashcode:"+hashCode);
        HttpURLConnection connection = null;
        RandomAccessFile raf = null;
        InputStream inputStream = null;

        URL url;
        try {
            url = new URL(threadInfo.getUrl());
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(connectTimeout);
            connection.setReadTimeout(readTimeout);
            connection.setRequestMethod("GET");
            setHttpHeader(getHttpHeaders(), connection);

            long currentPosition = threadInfo.getCurrentPosition();
            long getResponseCodeStart = System.currentTimeMillis();

            if (checkStop()) {//check is pause before connect to network
                return;
            }
            int responseCode = connection.getResponseCode();
            isDownloading = true;
            if (checkStop()) {//cause connect network may take seconds,so must check here if user click pause before get response
                return;
            }

            LogUtil.d(TAG, "时间测试...download...+" + Thread.currentThread() + "...获取状态码耗时:" + (System.currentTimeMillis() - getResponseCodeStart) + "...hashCode:" + hashCode);
            if (responseCode == getResponseCode()) {

                raf = getRandomAccessFile();
                long getInputStreamStart = System.currentTimeMillis();
                inputStream = connection.getInputStream();
//                LogUtil.d(TAG, "时间测试...download...获取流:" + (System.currentTimeMillis() - getInputStreamStart));
                byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                int len;
                while ((len = inputStream.read(buffer)) != -1) {
                    if (checkStop()) {
                        return;
                    }
//                    LogUtil.d(TAG, "速度测试...:" + buffer.length + "...hashCode:" + hashCode);
                    raf.write(buffer, 0, len);
                    currentPosition += len;
                    threadInfo.setCurrentPosition(currentPosition);
                    sendMessage(UIMessage.THREAD_PROGRESS, threadInfo, null, false);
                    if (currentPosition > threadInfo.getEndPosition()) {
                        break;
                    }
                }
                //This thread has finished its work.
                LogUtil.i(TAG, "Thread "+threadInfo.getId() + " has finished its work" + "..." + fileName + "...hashCode:" + hashCode);
                updateDB();
                isFinished = true;
                isDownloading = false;
                sendMessage(UIMessage.THREAD_FINISH, threadInfo, null, false);
            } else {
                isFailed = true;
                isDownloading = false;
                LogUtil.i(TAG, threadInfo.getId() + "Thread exception occurred" + "..." + fileName + "..." + responseCode + "...hashCode:" + hashCode);
                downloadException = new DownloadException(DownloadException.TYPE_RESPONSE_CODE, "DownloadThread failed", responseCode);
                sendMessage(UIMessage.THREAD_ERROR, threadInfo, downloadException, false);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            updateDB();
            downloadException = new DownloadException(DownloadException.TYPE_MALFORMED_URL, "DownloadThread failed." + threadInfo.getUrl(), e);
            sendMessage(UIMessage.THREAD_ERROR, threadInfo, downloadException, false);
        } catch (ProtocolException e) {
            e.printStackTrace();
            updateDB();
            downloadException = new DownloadException(DownloadException.TYPE_PROTOCOL, "DownloadThread failed", e);
            sendMessage(UIMessage.THREAD_ERROR, threadInfo, downloadException, false);
        } catch (InterruptedIOException e) {
            e.printStackTrace();
            updateDB();
            LogUtil.i(TAG, threadInfo.getId() + "Thread stop by auto interrupted." + "...hashCode:" + hashCode);
            downloadException = new DownloadException(DownloadException.TYPE_INTERRUPTED_IO, "DownloadThread failed", e);
            sendMessage(UIMessage.THREAD_ERROR, threadInfo, downloadException, false);
        } catch (IOException e) {
            e.printStackTrace();
            updateDB();
            downloadException = new DownloadException(DownloadException.TYPE_IO, "DownloadThread failed", e);
            sendMessage(UIMessage.THREAD_ERROR, threadInfo, downloadException, false);
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
                updateDB();
                downloadException = new DownloadException(DownloadException.TYPE_IO, "DownloadThread failed", e);
                sendMessage(UIMessage.THREAD_ERROR, threadInfo, downloadException, false);
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

    protected void sendMessage(int state, ThreadInfo info, DownloadException e, boolean forceSend) {
        if (state != UIMessage.THREAD_PROGRESS) {
            LogUtil.d(TAG, "sendMessage()...progress...threadID:" + info.getId() + "..." + state + "...isOver:" + isOver + "...hashCode:" + hashCode);
        }
        if (forceSend || !isOver) {
            LogUtil.d(TAG, "sendMessage()...threadID:" + info.getId() + "..." + state + "...isOver:" + isOver + "...hashCode:" + hashCode);
            UIMessage uiMessage = new UIMessage(state)
                    .setThreadInfo(info)
                    .setDownloadException(e);
            uiMessage.setHashCode(hashCode);
            Message message = uiHandler.obtainMessage();
            message.obj = uiMessage;
            uiHandler.sendMessage(message);
        }
    }

    public DownloadException getException() {
        return downloadException;
    }

    public ThreadInfo getThreadInfo() {
        return threadInfo;
    }

    private boolean checkStop() {
//        LogUtil.i(TAG, "checkStop()...ThreadName:" + Thread.currentThread() + "...stopThread:" + stopThread);
        if (stopThread) {
            LogUtil.d(TAG, "checkStop()...ThreadName:" + Thread.currentThread() + "...停止线程...isOver:" + isOver + "...hashCode:" + hashCode);
            if (!isOver) {
                isOver = true;
                updateDB();
                sendMessage(UIMessage.THREAD_PAUSE, threadInfo, null, true);
                isOver = true;
            }
            return true;
        }
        return false;
    }

    public void stopThread() {
        LogUtil.i(TAG, "stopThread()...ThreadName:" + Thread.currentThread() + "..." + threadInfo.getId() + "...isDownloading:" + isDownloading + "...isOver:" + isOver + "...hashCode:" + hashCode);
        stopThread = true;
        if (!isDownloading) {//isDownloading will be true after get response code.
            isOver = true;
            updateDB();
            sendMessage(UIMessage.THREAD_PAUSE, threadInfo, null, true);
        }
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
