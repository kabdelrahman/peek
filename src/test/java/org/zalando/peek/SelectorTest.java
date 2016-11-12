package org.zalando.peek;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SelectorTest {

    private JsonObject _json;

    @Before
    public void loadSampleJson() throws IOException {
        _json = Json.parse(new String(Files.readAllBytes(Paths.get("test/sample.json")))).asObject();
    }

    @Test
    public void testSelectNull() {
        assertEquals(_json, new Selector(null).apply(_json));
    }

    @Test
    public void testSelectEmpty() {
        assertEquals(_json, new Selector(new ArrayList()).apply(_json));
    }

    @Test
    public void testSelectSinglePattern() {
        String pattern = "data.version";
        assertEquals(Json.object().add(pattern, Json.value(42)),
                new Selector(Arrays.asList(new String[]{pattern})).apply(_json));
    }

    @Test
    public void testSelectMultiplePatterns() {
        String pattern1 = "data.version";
        String pattern2 = "data.foo.bar";
        assertEquals(Json.object().add(pattern1, Json.value(42)).add(pattern2, Json.value(null)),
                new Selector(Arrays.asList(new String[]{pattern1, pattern2})).apply(_json));
    }

    @Test
    public void testSelectNoMatch() {
        assertEquals(Json.value(null), Selector.select(asPattern("data.foo.bar"), _json));
    }

    @Test
    public void testSelectNullJson() {
        assertEquals(Json.value(null), Selector.select(asPattern("data"), null));
    }

    @Test
    public void testSelectValue() {
        assertEquals(Json.value(42), Selector.select(asPattern("data.version"), _json));
    }

    @Test
    public void testSelectObject() {
        assertEquals(Json.object().add("id", 94371), Selector.select(asPattern("data.customer"), _json));
    }

    @Test
    public void testSelectWildcard() {
        assertEquals(Json.array(10, 15), Selector.select(asPattern("data.items.years"), _json));
    }

    @Test
    public void testSelectWildcardComplex() {
        JsonArray expected = (JsonArray) Json.array();
        expected.add(Json.array("light", "fresh"));
        expected.add(Json.array("elegant", "fruity", "smooth"));
        assertEquals(expected, Selector.select(asPattern("data.items.tags"), _json));
    }

    @Test
    public void testSelectDoubleWildcard() {
        JsonArray expected = (JsonArray) Json.array();
        expected.add(Json.array("GBP", "EUR"));
        expected.add(Json.array("GBP", "USD"));
        assertEquals(expected, Selector.select(asPattern("data.items.price.unit"), _json));
    }

    private List asPattern(String text) {
        return new ArrayList(Arrays.asList(text.split("\\.")));
    }


}
