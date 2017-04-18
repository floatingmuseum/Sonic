package floatingmuseum.sonic;

/**
 * Created by Floatingmuseum on 2017/4/18.
 */

public class DownloadException extends Exception {
    private String errorMessage;
    private int responseCode;
    private Throwable throwable;

    public DownloadException(String errorMessage, Throwable throwable) {
        this.errorMessage = errorMessage;
        this.throwable = throwable;
    }

    public DownloadException(String errorMessage, int responseCode) {
        this.errorMessage = errorMessage;
        this.responseCode = responseCode;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Throwable getThrowable() {
        return throwable;
    }
}
