package floatingmuseum.sonic;

/**
 * Created by Floatingmuseum on 2017/4/18.
 */

public class DownloadException extends Exception {
    private String errorMessage;
    private int responseCode = -1;

    public DownloadException(String errorMessage, Throwable throwable) {
        super(errorMessage, throwable);
        this.errorMessage = errorMessage;
    }

    public DownloadException(String errorMessage, int responseCode) {
        super(errorMessage);
        this.errorMessage = errorMessage;
        this.responseCode = responseCode;
    }

    public DownloadException(String errorMessage) {
        super(errorMessage);
        this.errorMessage = errorMessage;
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
}
