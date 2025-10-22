package example.backend_mini_app.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    AUTH_ACCESS_TOKEN_INVALID("AUTH_001", "Access token không hợp lệ hoặc đã hết hạn", 401),
    AUTH_CODE_EXPIRED("AUTH_002", "Mã xác thực đã hết hạn", 401),
    AUTH_USER_DENIED("AUTH_003", "Người dùng từ chối cấp quyền", 403),
    AUTH_REFRESH_TOKEN_EXPIRED("AUTH_004", "Refresh token đã hết hạn", 401),
    AUTH_INVALID_APP_ID("AUTH_005", "App ID không hợp lệ", 400),

    REMOTE_CONNECTION_ERROR("REMOTE_001", "Không thể kết nối đến dịch vụ bên ngoài", 503),
    REMOTE_TIMEOUT("REMOTE_002", "Dịch vụ bên ngoài không phản hồi kịp thời", 504),
    REMOTE_INVALID_RESPONSE("REMOTE_003", "Phản hồi từ dịch vụ bên ngoài không hợp lệ", 500),
    REMOTE_BAD_REQUEST("REMOTE_004", "Yêu cầu gửi đến dịch vụ bên ngoài không hợp lệ", 400),
    REMOTE_UNAUTHORIZED("REMOTE_005", "Không có quyền truy cập dịch vụ bên ngoài", 401),
    REMOTE_FORBIDDEN("REMOTE_006", "Bị từ chối truy cập dịch vụ bên ngoài", 403),
    REMOTE_SERVER_ERROR("REMOTE_007", "Lỗi từ phía dịch vụ bên ngoài", 500),

    SYSTEM_INTERNAL_ERROR("SYS_001", "Lỗi hệ thống", 500),
    SYSTEM_DATABASE_ERROR("SYS_002", "Lỗi cơ sở dữ liệu", 500),
    SYSTEM_TIMEOUT("SYS_003", "Yêu cầu quá thời gian chờ", 504),

    STATE_ALREADY_CONSUMED("STATE_001", "State đã bị sử dụng", 400),
    STATE_EXPIRED("STATE_002", "State đã hết hạn", 400),
    STATE_REDIRECT_URI_MISMATCH("STATE_003", "Redirect URI không khớp", 400);

    private final String code;
    private final String message;
    private final int httpStatus;

    ErrorCode(String code, String message, int httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public String getCategory() {
        if (code.startsWith("AUTH")) return "Authentication";
        if (code.startsWith("REMOTE")) return "Remote API";
        if (code.startsWith("SYS")) return "System";
        if (code.startsWith("STATE")) return "State Validation";
        return "Unknown";
    }
}