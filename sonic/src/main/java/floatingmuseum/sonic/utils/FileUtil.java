package floatingmuseum.sonic.utils;

import android.webkit.MimeTypeMap;

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

    /**
     * Returns the file extension or an empty string iff there is no
     * extension. This method is a convenience method for obtaining the
     * extension of a url and has undefined results for other Strings.
     */
    public static String getUrlSuffix(String url) {
        return MimeTypeMap.getFileExtensionFromUrl(url);
    }

    public static void initDir(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public static void deleteFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            file.delete();
        }
    }

    /**
     * <p>Estimate is the file complete
     * <p>1.Compare md5 between local file and server file.
     * <p>2.Compare full length between local file and server file.
     * <p>3.Named the file suffix .tmp when it start download.after download finish change suffix to original.
     */
    private static boolean isFileComplete(){
        // TODO: 2017/5/10 test file is complete
        return false;
    }
}
