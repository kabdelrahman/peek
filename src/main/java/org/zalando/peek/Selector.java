package org.zalando.peek;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Selector {

    private final List<String> _patterns;

    public Selector(List<String> patterns) {
        _patterns = (patterns == null) ? new ArrayList() : new ArrayList(patterns);
    }

    public JsonObject apply(JsonObject json) {
        if (_patterns.isEmpty()) return json;
        JsonObject result = Json.object();
        for (String pattern : _patterns) {
            List<String> parts = new ArrayList(Arrays.asList(pattern.split("\\.")));
            JsonValue value = select(parts, json);
            result.add(pattern, value);
        }
        return result;
    }

    public static JsonValue select(List<String> pattern, JsonValue json) {
        if (json == null) return Json.value(null);
        if (pattern.isEmpty()) return json;
        if (json.isArray()) {
            JsonArray result = (JsonArray) Json.array();
            for (JsonValue value : json.asArray()) {
                result.add(select(pattern, value));
            }
            return result;
        } else {
            pattern = new ArrayList(pattern);
            String wanted = pattern.remove(0);
            json = json.asObject().get(wanted);
            return select(pattern, json);
        }
    }

}
