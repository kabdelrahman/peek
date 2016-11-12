package org.zalando.peek;

import com.eclipsesource.json.*;
import com.github.ryenus.rop.OptionParser;
import com.github.ryenus.rop.OptionParser.Command;
import com.github.ryenus.rop.OptionParser.Option;
import com.moandjiezana.toml.Toml;
import org.zalando.straw.Straw;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

@Command(name = "peek", descriptions = "peek [options] [event_type] [patterns]")
public class Main {

    @Option(opt = { "-a", "--array" }, description = "return array of values")
    boolean _array = false;

    @Option(opt = { "-h", "--host" }, description = "use nakadi host [name] from config")
    String _host = "default";

    @Option(opt = { "-n", "--number" }, description = "exit after processing [number] of events")
    int _number = -1;

    @Option(opt = { "-p", "--pretty" }, description = "pretty-print json output")
    boolean _pretty = false;

    public static void main(String[] args) {
        if (args.length == 0) fatal("missing parameter: event_type");
        OptionParser parser = new OptionParser(Main.class);
        parser.parse(args); // calls run()
    }

    void run(OptionParser parser, String[] params) throws IOException {

        List<String> args = new ArrayList(Arrays.asList(params));
        String eventType = args.remove(0);
        Selector selector = new Selector(args);

        URL url = getNakadiUrl(eventType);
        Map<Integer, Long> cursors = Collections.emptyMap();

        Straw straw = new Straw(url, cursors) {
            private int _count = 0;

            @Override protected void handleEvents(String raw) throws Exception {
                JsonObject json = Json.parse(raw).asObject();
                JsonValue events = json.get("events");
                if (events != null) { // skip keepalives
                    JsonObject event = events.asArray().get(0).asObject();
                    event.set("cursor", shortCursor(json));
                    JsonValue result = selector.apply(event);
                    if (_array) result = extractValues((JsonObject) result);
                    System.out.println(_pretty ? result.toString(WriterConfig.PRETTY_PRINT) : result);
                    _count++;
                    if (_count == _number) System.exit(0);
                }
            }

            @Override protected void logError(Exception e) {
                e.printStackTrace(System.err);
                System.exit(1);
            }

            @Override protected void logDebug(String message) {}
            @Override protected void logInfo(String message) {}

        };
        straw.start();
    }

    private URL getNakadiUrl(String eventType) throws MalformedURLException, FileNotFoundException {
        return new URL("https://" + getHost(_host) + "/event-types/" + eventType + "/events");
    }

    private static String getHost(String name) {
        try {
            Toml config = new Toml().read(new FileReader("./bin/peek.config"));
            String host = config.getString("host." + name);
            if (host == null) fatal("unknown host: " + name);
            return host;
        } catch (FileNotFoundException e) {
            fatal("missing file: peek.config");
            return null;
        }
    }

    private static JsonArray extractValues(JsonObject object) {
        JsonArray result = (JsonArray) Json.array();
        for (JsonObject.Member member : object) {
            result.add(member.getValue());
        }
        return result;
    }

    private static JsonValue shortCursor(JsonObject json) {
        JsonObject cursor = json.get("cursor").asObject();
        String partition = cursor.get("partition").asString();
        String offset = cursor.get("offset").asString();
        return Json.value(partition + ":" + offset);
    }

    private static void fatal(String message) {
        System.err.println(message);
        System.exit(1);
    }
}
