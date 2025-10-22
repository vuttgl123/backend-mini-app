package example.backend_mini_app.shared.helper;

import example.backend_mini_app.exception.ErrorCode;
import example.backend_mini_app.exception.MiniAppException;

public final class ErrorHelper {
    private ErrorHelper() {}

    public static MiniAppException ex(ErrorCode code, String message) {
        return new MiniAppException(code, message);
    }

    public static MiniAppException ex(ErrorCode code, String message, Throwable cause) {
        return new MiniAppException(code, message, cause);
    }

    public static MiniAppException ex(ErrorCode code, Throwable cause) {
        return new MiniAppException(code, cause);
    }

    public static MiniAppException ex(ErrorCode code) {
        return new MiniAppException(code);
    }

}