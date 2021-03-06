package floatingmuseum.sonic.threads;

import android.os.Message;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.UnknownHostException;

import floatingmuseum.sonic.DownloadException;
import floatingmuseum.sonic.UIHandler;
import floatingmuseum.sonic.entity.UIMessage;
import floatingmuseum.sonic.listener.InitListener;
import floatingmuseum.sonic.utils.LogUtil;

/**
 * Created by Floatingmuseum on 2017/3/31.
 */

public class InitThread extends Thread {

    private static final String TAG = InitThread.class.getName();
    private String downloadUrl;
    private String fileName;
    private String downloadDirPath;
    private String filePath;
    private int readTimeout;
    private int connectTimeout;
    private UIHandler uiHandler;
    private boolean stopThread = false;
    private boolean isGetResponseCode = false;
    private boolean isOver = false;
    private int hashCode;

    public InitThread(UIHandler uiHandler, String downloadUrl, String fileName, String downloadDirPath, String filePath, int readTimeout, int connectTimeout, int hashCode) {
        this.downloadUrl = downloadUrl;
        this.fileName = fileName;
        this.downloadDirPath = downloadDirPath;
        this.filePath = filePath;
        this.readTimeout = readTimeout;
        this.connectTimeout = connectTimeout;
        this.uiHandler = uiHandler;
        this.hashCode = hashCode;
    }

    @Override
    public void run() {
        URL url;
        HttpURLConnection connection = null;
        try {
            url = new URL(downloadUrl);
        } catch (MalformedURLException e) {
            sendMessage(UIMessage.THREAD_INIT_THREAD_ERROR, 0, false, new DownloadException(DownloadException.TYPE_MALFORMED_URL, "InitThread Request failed,Wrong url." + downloadUrl, e), false);
            return;
        }

        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(connectTimeout);
            connection.setReadTimeout(readTimeout);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Range", "bytes=" + 0 + "-");


            long getResponseCodeStart = System.currentTimeMillis();
            if (checkStop()) {
                return;
            }
            int responseCode = connection.getResponseCode();
            LogUtil.d(TAG, "时间测试...init...获取状态码耗时:" + (System.currentTimeMillis() - getResponseCodeStart) + "...url:" + downloadUrl);
            isGetResponseCode = true;
            if (checkStop()) {
                return;
            }
            LogUtil.i(TAG, "InitThread...Response code:" + responseCode + "...url:" + downloadUrl);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                prepare(connection, false);
            } else if (responseCode == HttpURLConnection.HTTP_PARTIAL) {
                prepare(connection, true);
            } else {
                sendMessage(UIMessage.THREAD_INIT_THREAD_ERROR, 0, false, new DownloadException(DownloadException.TYPE_RESPONSE_CODE, "InitThread Request failed", responseCode), false);
            }
        } catch (ProtocolException e) {
            e.printStackTrace();
            LogUtil.d(TAG, "InitThread...onError...ProtocolException" + "...url:" + downloadUrl);
            sendMessage(UIMessage.THREAD_INIT_THREAD_ERROR, 0, false, new DownloadException(DownloadException.TYPE_PROTOCOL, "InitThread Request failed", e), false);
        } catch (IOException e) {
            e.printStackTrace();
            LogUtil.d(TAG, "InitThread...onError...IOException" + "...url:" + downloadUrl);
            sendMessage(UIMessage.THREAD_INIT_THREAD_ERROR, 0, false, new DownloadException(DownloadException.TYPE_IO, "InitThread Request failed", e), false);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d(TAG, "InitThread...onError...Exception...prepare" + "...url:" + downloadUrl);
            sendMessage(UIMessage.THREAD_INIT_THREAD_ERROR, 0, false, new DownloadException(DownloadException.TYPE_OTHER, "InitThread Request failed", e), false);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            isOver = true;
        }
    }

    private void prepare(HttpURLConnection connection, boolean isSupportRange) {
        long contentLength = connection.getContentLength();
        if (contentLength <= 0) {
            sendMessage(UIMessage.THREAD_INIT_THREAD_ERROR, 0, false, new DownloadException(DownloadException.TYPE_WRONG_LENGTH, "File length exception. length<=0."), false);
            return;
        }
//        File dir = new File(downloadDirPath);
        //Create file
//        File file = new File(dir, fileName);
        File file = new File(filePath);
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(file, "rwd");
            randomAccessFile.setLength(contentLength);
            sendMessage(UIMessage.THREAD_FETCH_CONTENT_LENGTH, contentLength, isSupportRange, null, false);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            LogUtil.d(TAG, "InitThread...onError...FileNotFoundException" + "...url:" + downloadUrl);
            sendMessage(UIMessage.THREAD_INIT_THREAD_ERROR, 0, false, new DownloadException(DownloadException.TYPE_FILE_NOT_FOUND, "InitThread Request failed,File not found", e), false);
        } catch (IOException e) {
            e.printStackTrace();
            LogUtil.d(TAG, "InitThread...onError...IOException...prepare" + "...url:" + downloadUrl);
            sendMessage(UIMessage.THREAD_INIT_THREAD_ERROR, 0, false, new DownloadException(DownloadException.TYPE_IO, "InitThread Request failed", e), false);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d(TAG, "InitThread...onError...Exception...prepare" + "...url:" + downloadUrl);
            sendMessage(UIMessage.THREAD_INIT_THREAD_ERROR, 0, false, new DownloadException(DownloadException.TYPE_OTHER, "InitThread Request failed", e), false);
        } finally {
            if (randomAccessFile != null) {
                try {
                    randomAccessFile.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    sendMessage(UIMessage.THREAD_INIT_THREAD_ERROR, 0, false, new DownloadException(DownloadException.TYPE_IO, "InitThread Request failed", e), false);
                }
            }
        }
    }

    private void sendMessage(int state, long contentLength, boolean isSupportRange, DownloadException e, boolean forceSend) {
        LogUtil.i(TAG, "sendMessage()...ThreadName:" + Thread.currentThread() + "...state:" + state + "...stopThread:" + stopThread + "...forceSend:" + forceSend + "...isOver:" + isOver + "...url:" + downloadUrl);
        if (forceSend || !isOver) {
            UIMessage uiMessage = new UIMessage(state);
            if (UIMessage.THREAD_INIT_PAUSE != state) {
                uiMessage.setContentLength(contentLength)
                        .setSupportRange(isSupportRange)
                        .setDownloadException(e);
            }
            uiMessage.setHashCode(hashCode);
            Message message = uiHandler.obtainMessage();
            message.obj = uiMessage;
            uiHandler.sendMessage(message);
        }
    }

    private boolean checkStop() {
        LogUtil.i(TAG, "checkStop()...ThreadName:" + Thread.currentThread() + "...stopThread:" + stopThread + "...url:" + downloadUrl);
        if (stopThread) {
            if (!isOver) {
                sendMessage(UIMessage.THREAD_INIT_PAUSE, -1, false, null, true);
            }
            return true;
        }
        return false;
    }

    public void stopThread() {
        stopThread = true;
        LogUtil.i(TAG, "stopThread()...ThreadName:" + Thread.currentThread() + "...stopThread:" + stopThread + "...isGetResponseCode:" + isGetResponseCode + "...url:" + downloadUrl);
        if (!isGetResponseCode) {
            isOver = true;
            sendMessage(UIMessage.THREAD_INIT_PAUSE, -1, false, null, true);
        }
    }
}


