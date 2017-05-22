package floatingmuseum.sonic.threads;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import floatingmuseum.sonic.DownloadException;
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
    private int readTimeout;
    private int connectTimeout;
    private InitListener listener;

    public InitThread(String downloadUrl, String fileName, String downloadDirPath, int readTimeout, int connectTimeout, InitListener listener) {
        this.downloadUrl = downloadUrl;
        this.fileName = fileName;
        this.downloadDirPath = downloadDirPath;
        this.readTimeout = readTimeout;
        this.connectTimeout = connectTimeout;
        this.listener = listener;
    }

    @Override
    public void run() {
        URL url = null;
        HttpURLConnection connection = null;
        try {
            url = new URL(downloadUrl);
        } catch (MalformedURLException e) {
            listener.onInitError(new DownloadException(DownloadException.TYPE_MALFORMED_URL, "InitThread Request failed,Wrong url.", e));
            return;
        }

        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(connectTimeout);
            connection.setReadTimeout(readTimeout);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Range", "bytes=" + 0 + "-");
            int responseCode = connection.getResponseCode();
            LogUtil.i(TAG, "InitThread...Response code:" + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                prepare(connection, false);
            } else if (responseCode == HttpURLConnection.HTTP_PARTIAL) {
                prepare(connection, true);
            } else {
                listener.onInitError(new DownloadException(DownloadException.TYPE_RESPONSE_CODE, "InitThread Request failed", responseCode));
            }
        } catch (ProtocolException e) {
            e.printStackTrace();
            listener.onInitError(new DownloadException(DownloadException.TYPE_PROTOCOL, "InitThread Request failed", e));
        } catch (IOException e) {
            e.printStackTrace();
            listener.onInitError(new DownloadException(DownloadException.TYPE_IO, "InitThread Request failed", e));
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void prepare(HttpURLConnection connection, boolean isSupportRange) {
        long contentLength = connection.getContentLength();
        if (contentLength <= 0) {
            listener.onInitError(new DownloadException(DownloadException.TYPE_WRONG_LENGTH, "File length exception. length<=0."));
            return;
        }
        File dir = new File(downloadDirPath);
        //Create file
        File file = new File(dir, fileName);
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(file, "rwd");
            randomAccessFile.setLength(contentLength);
            listener.onGetContentLength(contentLength, isSupportRange);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            listener.onInitError(new DownloadException(DownloadException.TYPE_FILE_NOT_FOUND, "InitThread Request failed,File not found", e));
        } catch (IOException e) {
            e.printStackTrace();
            listener.onInitError(new DownloadException(DownloadException.TYPE_IO, "InitThread Request failed", e));
        } finally {
            if (randomAccessFile != null) {
                try {
                    randomAccessFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    listener.onInitError(new DownloadException(DownloadException.TYPE_IO, "InitThread Request failed", e));
                }
            }
        }
    }

    public void stopThread() {
        if (!Thread.interrupted()) {
            interrupt();
        }
    }
}


