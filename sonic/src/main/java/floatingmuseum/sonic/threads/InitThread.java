package floatingmuseum.sonic.threads;

import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import floatingmuseum.sonic.DownloadException;
import floatingmuseum.sonic.listener.InitListener;

/**
 * Created by Floatingmuseum on 2017/3/31.
 */

public class InitThread extends Thread {

    private static final String TAG = InitThread.class.getName();
    private String downloadUrl;
    private String fileName;
    private InitListener listener;
    private String downloadDirPath;

    public InitThread(String downloadUrl, String fileName, String downloadDirPath, InitListener listener) {
        this.downloadUrl = downloadUrl;
        this.fileName = fileName;
        this.downloadDirPath = downloadDirPath;
        this.listener = listener;
    }

    @Override
    public void run() {
        URL url = null;
        RandomAccessFile randomAccessFile = null;
        HttpURLConnection connection = null;
        try {
            url = new URL(downloadUrl);
        } catch (MalformedURLException e) {
            listener.onInitError(new DownloadException("Wrong url.", e));
            return;
        }

        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(3000);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Range", "bytes=" + 0 + "-");
            int responseCode = connection.getResponseCode();
            Log.i(TAG, "InitThread...Response code:" + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                prepare(connection, false);
            } else if (responseCode == HttpURLConnection.HTTP_PARTIAL) {
                prepare(connection, true);
            } else {
                listener.onInitError(new DownloadException("InitThread Request failed", responseCode));
            }
        } catch (Exception e) {
            e.printStackTrace();
            listener.onInitError(new DownloadException("InitThread Request failed", e));
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void prepare(HttpURLConnection connection, boolean isSupportRange) {
        long contentLength = connection.getContentLength();
        if (contentLength <= 0) {
            listener.onInitError(new DownloadException("File length exception. length<=0."));
            return;
        }
        File dir = new File(downloadDirPath);
        //创建文件
        File file = new File(dir, fileName);
        //操作的文件，和可操作的模式，读写删
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(file, "rwd");
            //设置长度
            randomAccessFile.setLength(contentLength);
            listener.onGetContentLength(contentLength, isSupportRange);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (randomAccessFile != null) {
                try {
                    randomAccessFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    listener.onInitError(new DownloadException("InitThread Request failed", e));
                }
            }
        }
    }
}


