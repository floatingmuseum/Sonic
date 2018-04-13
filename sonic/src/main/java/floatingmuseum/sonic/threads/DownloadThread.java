package floatingmuseum.sonic.threads;

import android.os.Handler;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import floatingmuseum.sonic.UIHandler;
import floatingmuseum.sonic.db.DBManager;
import floatingmuseum.sonic.entity.ThreadInfo;
import floatingmuseum.sonic.listener.ThreadListener;

/**
 * Created by Floatingmuseum on 2017/3/31.
 */

public class DownloadThread extends BaseThread {

    private static final String TAG = DownloadThread.class.getName();

    private DBManager dbManager;

    public DownloadThread(UIHandler uiHandler, ThreadInfo threadInfo, String dirPath, String fileName, DBManager dbManager, int readTimeout, int connectTimeout) {
        this.threadInfo = threadInfo;
        this.dirPath = dirPath;
        this.fileName = fileName;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.uiHandler = uiHandler;
        this.dbManager = dbManager;
    }


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
