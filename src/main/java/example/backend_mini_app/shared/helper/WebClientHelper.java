package example.backend_mini_app.shared.helper;

import example.backend_mini_app.exception.ErrorCode;
import example.backend_mini_app.exception.MiniAppException;
import org.springframework.web.reactive.function.UnsupportedMediaTypeException;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import javax.net.ssl.SSLException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public final class WebClientHelper {
    private WebClientHelper() {}

    public static MiniAppException mapException(Throwable e, String context) {
        if (e instanceof WebClientResponseException ex) {
            int status = ex.getStatusCode().value();

            ErrorCode errorCode = switch (status) {
                case 400, 404 -> ErrorCode.REMOTE_BAD_REQUEST;
                case 401      -> ErrorCode.REMOTE_UNAUTHORIZED;
                case 403      -> ErrorCode.REMOTE_FORBIDDEN;
                case 504      -> ErrorCode.REMOTE_TIMEOUT;
                case 429, 502, 503, 500 -> ErrorCode.REMOTE_SERVER_ERROR;
                default -> {
                    if (status >= 400 && status < 500) yield ErrorCode.REMOTE_BAD_REQUEST;
                    if (status >= 500) yield ErrorCode.REMOTE_SERVER_ERROR;
                    yield ErrorCode.REMOTE_INVALID_RESPONSE;
                }
            };

            String errorKey = context + "_http_" + status;
            return new MiniAppException(errorCode, errorKey, ex);
        }

        if (e instanceof WebClientRequestException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof SocketTimeoutException
                    || cause instanceof io.netty.handler.timeout.ReadTimeoutException) {
                return new MiniAppException(ErrorCode.REMOTE_TIMEOUT, context + "_timeout", ex);
            }
            if (cause instanceof UnknownHostException) {
                return new MiniAppException(ErrorCode.REMOTE_CONNECTION_ERROR, context + "_dns_error", ex);
            }
            if (cause instanceof ConnectException) {
                return new MiniAppException(ErrorCode.REMOTE_CONNECTION_ERROR, context + "_connect_error", ex);
            }
            if (cause instanceof SSLException) {
                return new MiniAppException(ErrorCode.REMOTE_INVALID_RESPONSE, context + "_ssl_error", ex);
            }

            return new MiniAppException(ErrorCode.REMOTE_CONNECTION_ERROR, context + "_request_error", ex);
        }

        if (e instanceof UnsupportedMediaTypeException) {
            return new MiniAppException(ErrorCode.REMOTE_INVALID_RESPONSE, context + "_unsupported_media_type", e);
        }
        if (e instanceof org.springframework.core.codec.DecodingException) {
            return new MiniAppException(ErrorCode.REMOTE_INVALID_RESPONSE, context + "_decode_error", e);
        }

        return new MiniAppException(ErrorCode.SYSTEM_INTERNAL_ERROR, context + "_unknown_error", e);
    }
}
