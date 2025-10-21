package example.backend_mini_app.exception;

import example.backend_mini_app.model.response.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MiniAppException.class)
    public ResponseEntity<ErrorResponse> handleMiniAppException(
            MiniAppException ex, WebRequest request) {

        logger.error("MiniAppException: {} - {}", ex.getCode(), ex.getMessage(), ex);

        ErrorResponse response = new ErrorResponse(
                ex.getCode(),
                ex.getMessage(),
                ex.getServiceName(),
                ex.getHttpStatus(),
                request.getDescription(false).replace("uri=", "")
        );

        if (ex.getCause() != null) {
            response.addDetail("cause", ex.getCause().getMessage());
        }

        return new ResponseEntity<>(response, HttpStatus.valueOf(ex.getHttpStatus()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(
            Exception ex, WebRequest request) {

        logger.error("Unhandled Exception: {}", ex.getMessage(), ex);

        ErrorResponse response = new ErrorResponse(
                "SYS_001",
                "Đã xảy ra lỗi hệ thống",
                "System",
                500,
                request.getDescription(false).replace("uri=", "")
        );

        response.addDetail("error", ex.getMessage());

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
