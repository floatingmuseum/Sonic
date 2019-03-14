package floatingmuseum.sonic;


import android.support.annotation.Nullable;

/**
 * Created by Floatingmuseum on 2017/4/18.
 */

public class DownloadException extends Exception{
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
    private Throwable throwable;

    public DownloadException(int exceptionType,String errorMessage, Throwable throwable) {
        super(errorMessage, throwable);
        this.errorMessage = errorMessage;
        this.exceptionType = exceptionType;
        this.throwable = throwable;
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

    @Nullable
    public Throwable getThrowable(){
        return throwable;
    }


//    @Override
//    public int describeContents() {
//        return 0;
//    }
//
//    @Override
//    public void writeToParcel(Parcel dest, int flags) {
//        dest.writeString(this.errorMessage);
//        dest.writeInt(this.responseCode);
//        dest.writeInt(this.exceptionType);
//    }
//
//    protected DownloadException(Parcel in) {
//        this.errorMessage = in.readString();
//        this.responseCode = in.readInt();
//        this.exceptionType = in.readInt();
//    }
//
//    public static final Creator<DownloadException> CREATOR = new Creator<DownloadException>() {
//        @Override
//        public DownloadException createFromParcel(Parcel source) {
//            return new DownloadException(source);
//        }
//
//        @Override
//        public DownloadException[] newArray(int size) {
//            return new DownloadException[size];
//        }
//    };
}
