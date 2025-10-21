package example.backend_mini_app.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

    ZALO_ACCESS_TOKEN_INVALID("ZALO_001", "Access token không hợp lệ hoặc đã hết hạn", 401),
    ZALO_AUTH_CODE_EXPIRED("ZALO_002", "Mã xác thực đã hết hạn", 401),
    ZALO_USER_DENIED("ZALO_003", "Người dùng từ chối cấp quyền", 403),
    ZALO_API_CONNECTION_ERROR("ZALO_004", "Không thể kết nối đến Zalo API", 503),
    ZALO_INVALID_APP_ID("ZALO_005", "App ID không hợp lệ", 400),
    ZALO_REFRESH_TOKEN_EXPIRED("ZALO_006", "Refresh token đã hết hạn", 401),
    ZALO_INVALID_RESPONSE("ZALO_007", "Phản hồi từ Zalo API không hợp lệ", 500),


    SYSTEM_INTERNAL_ERROR("SYS_001", "Lỗi hệ thống", 500),
    SYSTEM_DATABASE_ERROR("SYS_002", "Lỗi cơ sở dữ liệu", 500),
    SYSTEM_TIMEOUT("SYS_003", "Yêu cầu quá thời gian chờ", 504);

    private final String code;
    private final String message;
    private final int httpStatus;

    ErrorCode(String code, String message, int httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public String getServiceName() {
        if (code.startsWith("ZALO")) return "Zalo Login";
        if (code.startsWith("SYS")) return "System";
        return "Unknown";
    }
}
