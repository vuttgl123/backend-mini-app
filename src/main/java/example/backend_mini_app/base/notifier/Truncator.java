package example.backend_mini_app.base.notifier;

import java.util.Collection;
import java.util.Map;

public final class Truncator {
    private Truncator() {}


    public static String clamp(String s, int maxChars) {
        if (s == null) return null;
        if (maxChars <= 0) return "";
        if (s.length() <= maxChars) return s;
        int keep = Math.max(0, maxChars - 10);
        return s.substring(0, keep) + "…(truncated " + (s.length() - keep) + ")";
    }


    public static Object clampValue(Object v, int maxChars) {
        switch (v) {
            case null -> {
                return null;
            }
            case CharSequence cs -> {
                return clamp(cs.toString(), maxChars);
            }
            case byte[] b -> {
                return clamp("[bytes:" + b.length + "]", maxChars);
            }
            case Collection<?> c -> {
                return c.size() > 50 ? "[collection size=" + c.size() + "]" : c;
            }
            case Map<?, ?> m -> {
                return m.size() > 50 ? "[map size=" + m.size() + "]" : m;
            }
            default -> {
            }
        }
        String s = String.valueOf(v);
        return clamp(s, maxChars);
    }
}
