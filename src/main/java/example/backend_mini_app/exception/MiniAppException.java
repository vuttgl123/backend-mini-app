package example.backend_mini_app.exception;

import lombok.Getter;

@Getter
public class MiniAppException extends RuntimeException {

    private final ErrorCode errorCode;
    private Object[] messageArgs;

    public MiniAppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public MiniAppException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }

    public MiniAppException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
    }

    public MiniAppException(ErrorCode errorCode, String customMessage, Throwable cause) {
        super(customMessage, cause);
        this.errorCode = errorCode;
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

}


