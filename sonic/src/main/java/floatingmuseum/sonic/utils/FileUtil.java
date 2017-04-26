package floatingmuseum.sonic.utils;

import java.io.File;
import java.math.BigDecimal;

/**
 * Created by Floatingmuseum on 2016/12/1.
 */

public class FileUtil {

    public static long mbToBytes(int mb) {
        return mb * 1024 * 1024;
    }

    public static float bytesToKb(long fileLength) {
        BigDecimal bytes = new BigDecimal(fileLength);
        BigDecimal kb = new BigDecimal(1024);
        return bytes.divide(kb).floatValue();
    }

    public static float bytesToMb(long fileLength) {
        BigDecimal bytes = new BigDecimal(fileLength);
        BigDecimal mb = new BigDecimal(1024 * 1024);
        return bytes.divide(mb).floatValue();
    }

    /**
     * Get file name from url.
     * <p>https://github.com/floatingmuseum/MoCloud/blob/master/README.md
     * <p>README.md
     */
    public static String getUrlFileName(String url) {
        int lastDivideIndex = url.lastIndexOf("/");
        String fileName = url.substring(lastDivideIndex + 1);
        return fileName;
    }

    public static void initDir(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
}
