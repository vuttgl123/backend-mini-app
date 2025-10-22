package example.backend_mini_app.model.enumeration;

public enum Provider {
    ZALO,
    INTERNAL_API,
    FACEBOOK,
    GOOGLE,
    UNKNOWN;

    public String getDisplayName() {
        return switch (this) {
            case ZALO -> "Zalo Login";
            case INTERNAL_API -> "Internal API";
            case FACEBOOK -> "Facebook Login";
            case GOOGLE -> "Google Login";
            default -> "Unknown Provider";
        };
    }
}
