package floatingmuseum.sonic.threads;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import floatingmuseum.sonic.listener.InitListener;
import floatingmuseum.sonic.utils.FileUtil;

/**
 * Created by Floatingmuseum on 2017/3/31.
 */

public class InitThread extends Thread {

    private static final String TAG = InitThread.class.getName();
    private String downloadUrl;
    private InitListener listener;
    private String downloadDirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Downloads/";

    public InitThread(String downloadUrl, InitListener listener) {
        this.downloadUrl = downloadUrl;
        this.listener = listener;
    }

    @Override
    public void run() {
        URL url = null;
        RandomAccessFile randomAccessFile = null;
        HttpURLConnection connection = null;
        try {
            url = new URL(downloadUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(3000);
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            Log.i(TAG, "InitThread...Response code:" + responseCode);
            if (responseCode == 200 || responseCode == 206) {
                long contentLength = connection.getContentLength();
                if (contentLength <= 0) {
                    listener.onInitError(new IllegalStateException("Service file length exception. length:" + contentLength));
                    return;
                }
                File dir = new File(downloadDirPath);
                //创建文件
                File file = new File(dir, FileUtil.getUrlFileName(downloadUrl));
                //操作的文件，和可操作的模式，读写删
                randomAccessFile = new RandomAccessFile(file, "rwd");
                //设置长度
                randomAccessFile.setLength(contentLength);
                listener.onGetContentLength(contentLength);
                return;
            } else {
                listener.onInitError(new IllegalStateException("Request failed with response code:" + responseCode));
            }
        } catch (Exception e) {
            e.printStackTrace();
            listener.onInitError(e);
        } finally {
            try {
                if (connection != null) {
                    connection.disconnect();
                }
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                listener.onInitError(e);
            }
        }
    }
}


