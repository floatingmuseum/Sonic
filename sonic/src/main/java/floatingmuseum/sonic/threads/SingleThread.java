package floatingmuseum.sonic.threads;


import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.util.Map;

import floatingmuseum.sonic.UIHandler;
import floatingmuseum.sonic.entity.ThreadInfo;
import floatingmuseum.sonic.listener.ThreadListener;

/**
 * Created by Floatingmuseum on 2017/5/2.
 */

public class SingleThread extends BaseThread {

    public SingleThread(UIHandler uiHandler,ThreadInfo threadInfo, String dirPath, String fileName, int readTimeout, int connectTimeout) {
        this.threadInfo = threadInfo;
        this.dirPath = dirPath;
        this.fileName = fileName;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.uiHandler = uiHandler;
    }

    @Override
    protected RandomAccessFile getRandomAccessFile() throws IOException {
        File file = new File(dirPath, fileName);
        RandomAccessFile raf = new RandomAccessFile(file, "rwd");
        raf.seek(0);
        return raf;
    }

    @Override
    protected void updateDB() {
    }

    @Override
    protected Map<String, String> getHttpHeaders() {
        return null;
    }

    @Override
    protected int getResponseCode() {
        return HttpURLConnection.HTTP_OK;
    }
}
