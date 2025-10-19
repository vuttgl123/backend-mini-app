package example.backend_mini_app.exception;

public enum ErrorCode {

    // ===== ZALO LOGIN ERRORS =====
    ZALO_ACCESS_TOKEN_INVALID("ZALO_001", "Access token không hợp lệ hoặc đã hết hạn", 401),
    ZALO_AUTH_CODE_EXPIRED("ZALO_002", "Mã xác thực đã hết hạn", 401),
    ZALO_USER_DENIED("ZALO_003", "Người dùng từ chối cấp quyền", 403),
    ZALO_API_CONNECTION_ERROR("ZALO_004", "Không thể kết nối đến Zalo API", 503),
    ZALO_INVALID_APP_ID("ZALO_005", "App ID không hợp lệ", 400),
    ZALO_REFRESH_TOKEN_EXPIRED("ZALO_006", "Refresh token đã hết hạn", 401),
    ZALO_INVALID_RESPONSE("ZALO_007", "Phản hồi từ Zalo API không hợp lệ", 500),

    // ===== VNEID ERRORS =====
    VNEID_TOKEN_EXPIRED("VNEID_001", "Token VneID đã hết hạn", 401),
    VNEID_INVALID_CREDENTIALS("VNEID_002", "Thông tin đăng nhập không chính xác", 401),
    VNEID_ACCOUNT_LOCKED("VNEID_003", "Tài khoản đã bị khóa", 403),
    VNEID_API_UNAVAILABLE("VNEID_004", "Dịch vụ VneID tạm thời không khả dụng", 503),
    VNEID_INVALID_OTP("VNEID_005", "Mã OTP không chính xác", 400),
    VNEID_OTP_EXPIRED("VNEID_006", "Mã OTP đã hết hạn", 400),
    VNEID_OTP_MAX_ATTEMPTS("VNEID_007", "Vượt quá số lần nhập OTP", 429),
    VNEID_PHONE_NOT_REGISTERED("VNEID_008", "Số điện thoại chưa đăng ký VneID", 404),

    // ===== SYSTEM ERRORS =====
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

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public String getServiceName() {
        if (code.startsWith("ZALO")) return "Zalo Login";
        if (code.startsWith("VNEID")) return "VneID";
        if (code.startsWith("SYS")) return "System";
        return "Unknown";
    }
}
