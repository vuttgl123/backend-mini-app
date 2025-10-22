package example.backend_mini_app.shared.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class JsonUtils {
    private JsonUtils() {}

    public static String toJson(ObjectMapper om, Object obj) {
        try { return om.writeValueAsString(obj); }
        catch (Exception e) { return "{}"; }
    }

    public static String getText(JsonNode node, String field) {
        return node.hasNonNull(field) ? node.get(field).asText() : null;
    }

    public static boolean hasAny(JsonNode node, String... fields) {
        for (String f : fields)
            if (node.hasNonNull(f)) return true;
        return false;
    }
}