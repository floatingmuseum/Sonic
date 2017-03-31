package floatingmuseum.sonic;

import android.os.Environment;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import floatingmuseum.sonic.listener.InitListener;
import floatingmuseum.sonic.utils.FileUtil;

/**
 * Created by Floatingmuseum on 2017/3/31.
 */

public class InitThread extends Thread {

    private String downloadUrl;
    private InitListener callback;
    private String downloadDirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Downloads/";

    public InitThread(String downloadUrl, InitListener callback) {
        this.downloadUrl = downloadUrl;
        this.callback = callback;
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
            if (connection.getResponseCode() == 200 || connection.getResponseCode() == 206) {
                long contentLength = connection.getContentLength();
                if (contentLength <= 0) {
                    callback.onError();
                    return;
                }
                File dir = new File(downloadDirPath);
                //创建文件
                File file = new File(dir, FileUtil.getUrlFileName(downloadUrl));
                //操作的文件，和可操作的模式，读写删
                randomAccessFile = new RandomAccessFile(file, "rwd");
                //设置长度
                randomAccessFile.setLength(contentLength);
                callback.onGetContentLength(contentLength);
                return;
            } else {
                callback.onError();
            }
        } catch (Exception e) {
            e.printStackTrace();
            callback.onError();
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
                callback.onError();
            }
        }
    }
}


