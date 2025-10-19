package example.backend_mini_app.exception;

public class MiniAppException extends RuntimeException {

    private final ErrorCode errorCode;
    private Object[] messageArgs;

    // Constructor cơ bản
    public MiniAppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    // Constructor với cause
    public MiniAppException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }

    // Constructor với custom message
    public MiniAppException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
    }

    // Constructor với custom message và cause
    public MiniAppException(ErrorCode errorCode, String customMessage, Throwable cause) {
        super(customMessage, cause);
        this.errorCode = errorCode;
    }

    // Getters
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getCode() {
        return errorCode.getCode();
    }

    public int getHttpStatus() {
        return errorCode.getHttpStatus();
    }

    public String getServiceName() {
        return errorCode.getServiceName();
    }

    public Object[] getMessageArgs() {
        return messageArgs;
    }
}


