package example.backend_mini_app.shared.util;

public final class StringUtils {
    private StringUtils() {}

    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static String truncate(String s, int limit) {
        if (s == null) return null;
        return s.length() > limit ? s.substring(0, limit) + "...(truncated)" : s;
    }
}
