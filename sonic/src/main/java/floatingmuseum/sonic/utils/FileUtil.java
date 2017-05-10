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
     * <p>未实现的方法
     * <p>判断文件是否完整
     * <p>1.比较文件的md5值是否和服务器提供的md5值相同
     * <p>2.比较文件长度和服务器文件长度
     * <p>3.开始下载时将文件名扩展名改为.tmp,当下载完毕后改为原有扩展名.之后检查如果扩展名不是.tmp,即表示下载完毕.
     */
    private static boolean isFileComplete(){
        // TODO: 2017/5/10 检验文件是否完整
        return false;
    }
}
