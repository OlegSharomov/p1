package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Постман: https://www.getpostman.com/collections/a83b61d9e1c81c10575c
 */
//KVServer - сервер для сохранения состояния менеджера и хранения данных.
public class KVServer {
    private static final int PORT = 8078;
    private final String API_KEY;
    private final HttpServer server;
    private final Map<String, String> data = new HashMap<>();

    public static void main(String[] args) {                                                 //MAIN METHOD
        new KVServer().start();
    }

    public KVServer() {
        API_KEY = generateApiKey();
        try {
            server = HttpServer.create(new InetSocketAddress("localhost", PORT), 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        server.createContext("/register", (httpExchange) -> {
            try {
                switch (httpExchange.getRequestMethod()) {
                    case "GET":
                        sendText(httpExchange, API_KEY); // отправили ответ с кодом 200 и тело, в котором API_KEY
                        break;
                    default:
                        System.out.println("/register ждёт GET-запрос, а получил " + httpExchange.getRequestMethod());
                        httpExchange.sendResponseHeaders(405, 0);
                }
            } finally {
                httpExchange.close();
            }
        });
        server.createContext("/save", (httpExchange) -> {
            try {
                if (!hasAuth(httpExchange)) {
                    System.out.println("Запрос неавторизован, нужен параметр в query API_KEY со значением апи-ключа");
                    httpExchange.sendResponseHeaders(403, 0);
                    return;
                }
                switch (httpExchange.getRequestMethod()) {
                    case "POST":
                        String key = httpExchange.getRequestURI().getPath().substring("/save/".length());
                        if (key.isEmpty()) {
                            System.out.println("Key для сохранения пустой. key указывается в пути: /save/{key}");
                            httpExchange.sendResponseHeaders(400, 0);
                            return;
                        }
                        String value = readText(httpExchange);
                        if (value.isEmpty()) {
                            System.out.println("Value для сохранения пустой. value указывается в теле запроса");
                            httpExchange.sendResponseHeaders(400, 0);
                            return;
                        }
                        data.put(key, value);
                        System.out.println("Значение для ключа " + key + " успешно обновлено!");
                        httpExchange.sendResponseHeaders(200, 0);
                        break;
                    default:
                        System.out.println("/save ждёт POST-запрос, а получил: " + httpExchange.getRequestMethod());
                        httpExchange.sendResponseHeaders(405, 0);
                }
            } finally {
                httpExchange.close();
            }
        });
        server.createContext("/load", (httpExchange) -> {
            try {
                if (!hasAuth(httpExchange)) {
                    System.out.println("Запрос неавторизован, нужен параметр в query API_KEY со значением апи-ключа");
                    httpExchange.sendResponseHeaders(403, 0);
                    return;
                }
                switch (httpExchange.getRequestMethod()) {
                    case "GET":
                        String key = httpExchange.getRequestURI().getPath().substring("/save/".length());
                        if (key.isEmpty()) {
                            System.out.println("Key для сохранения пустой. key указывается в пути: /save/{key}");
                            httpExchange.sendResponseHeaders(400, 0);
                            return;
                        }
                        String value = data.get(key);
                        httpExchange.sendResponseHeaders(200, 0);
                        try (OutputStream os = httpExchange.getResponseBody()) {
                            os.write(value.getBytes(StandardCharsets.UTF_8));
                        }
                        System.out.println("Значение для ключа " + key + " успешно передано!");
                        break;
                    default:
                        System.out.println("/save ждёт POST-запрос, а получил: " + httpExchange.getRequestMethod());
                        httpExchange.sendResponseHeaders(405, 0);
                }
            } finally {
                httpExchange.close();
            }
        });
    }

    public void start() {
        System.out.println("Запускаем сервер на порту " + PORT);
//        System.out.println("Открой в браузере http://localhost:" + PORT + "/");
        System.out.println("API_KEY: " + API_KEY);
        server.start();
    }

    private String generateApiKey() {
        return "" + System.currentTimeMillis();
    }

    private boolean hasAuth(HttpExchange httpExchange) {
        String rawQuery = httpExchange.getRequestURI().getRawQuery();
        return rawQuery != null && (rawQuery.contains("API_KEY=" + API_KEY) || rawQuery.contains("API_KEY=DEBUG"));
    }

    private String readText(HttpExchange httpExchange) throws IOException {
        return new String(httpExchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    private void sendText(HttpExchange httpExchange, String api_key) throws IOException {
        byte[] resp = api_key.getBytes(StandardCharsets.UTF_8);
        httpExchange.getResponseHeaders().add("Content-Type", "application/json");
        httpExchange.sendResponseHeaders(200, resp.length);
        httpExchange.getResponseBody().write(resp);
    }

    public void stop() {
        server.stop(0);
    }

    public Map<String, String> getData() {
        return data;
    }
}
