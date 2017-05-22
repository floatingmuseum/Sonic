package floatingmuseum.sonic;

/**
 * Created by Floatingmuseum on 2017/4/18.
 */

public class DownloadException extends Exception {
    public static int TYPE_IO = 0;
    public static int TYPE_MALFORMED_URL = 1;
    public static int TYPE_FILE_NOT_FOUND = 2;
    public static int TYPE_PROTOCOL = 3;
    public static int TYPE_INTERRUPTED_IO = 4;
    public static int TYPE_WRONG_LENGTH = 5;
    public static int TYPE_RESPONSE_CODE = 6;


    private String errorMessage;
    private int responseCode = -1;
    private int exceptionType;

    public DownloadException(int exceptionType,String errorMessage, Throwable throwable) {
        super(errorMessage, throwable);
        this.errorMessage = errorMessage;
        this.exceptionType = exceptionType;
    }

    public DownloadException(int exceptionType,String errorMessage, int responseCode) {
        super(errorMessage);
        this.errorMessage = errorMessage;
        this.responseCode = responseCode;
        this.exceptionType = exceptionType;
    }

    public DownloadException(int exceptionType,String errorMessage) {
        super(errorMessage);
        this.errorMessage = errorMessage;
        this.exceptionType = exceptionType;
    }

    /**
     * -1 means no response code.
     */
    public int getResponseCode() {
        return responseCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public int getExceptionType() {
        return exceptionType;
    }
}
