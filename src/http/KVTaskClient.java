package http;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import models.Task;
import server.KVServer;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static service.InMemoryTaskManager.getNextId;

/* Класс, работающий на порту 8078, в котором генерируется API key.
 Через него отправляется и возвращается информацию из KVServer*/
public class KVTaskClient {
    private final URI uri;
    private final String API_KEY;
    private final HttpClient client;

    public KVTaskClient(URI uri) {
        this.uri = uri;
        client = HttpClient.newHttpClient();
        Gson gson = new Gson();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(uri + "/register")).GET().build();
        String temporaryValueKey = null;
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            temporaryValueKey = gson.fromJson(response.body(), String.class);
        } catch (IOException | InterruptedException e) { // обрабатываем ошибки отправки запроса
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        API_KEY = temporaryValueKey;
    }

    public static void main(String[] args) {
        System.out.println("Проверка работы KVServer");
        Gson gson = new Gson();
        KVServer kvServer = new KVServer();                                          // создали и запустили KVServer
        kvServer.start();
        URI uri = URI.create("http://localhost:8078");
        KVTaskClient client = new KVTaskClient(uri);

        Task task12 = new Task(getNextId(), "Task12", "Description task12",
                LocalDateTime.of(2022, 4, 15, 9, 0), Duration.ofHours(12));
        client.put("task", gson.toJson(task12));

        String s = client.load("task");
        System.out.println("Получили ответ от data - " + s);

        Task task13 = new Task(getNextId(), "Task13", "Description task13",
                LocalDateTime.of(2023, 4, 15, 9, 0), Duration.ofHours(9));
        client.put("task", gson.toJson(task13));

        String s2 = client.load("task");
        System.out.println("Получили ответ от data - " + s2);

        Task task14 = new Task(getNextId(), "Task14", "Description task14",
                LocalDateTime.of(2022, 6, 12, 12, 12), Duration.ofHours(12));
        client.put("task2", gson.toJson(task14));
        String s3 = client.load("task2");
        System.out.println(s2 + "     " + s3);
        kvServer.stop();
    }

    public void put(String key, String json) {
        System.out.println("Зашли в метод .put()");
        URI uri2 = URI.create(uri + "/save/" + key + "?API_KEY=" + API_KEY);
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        HttpRequest request = HttpRequest.newBuilder().uri(uri2).POST(body).build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Запрос <.put> обработан, статус запроса: " + response.statusCode());
        } catch (IOException | InterruptedException e) { // обрабатываем ошибки отправки запроса
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
    }

    private String load(String key) {
        String requestBody = null;
        System.out.println("Зашли в метод .load()");
        URI uri3 = URI.create(uri + "/load/" + key + "?API_KEY=" + API_KEY);
        HttpRequest request = HttpRequest.newBuilder().uri(uri3).GET().header("Accept", "application/json").build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                System.out.println("Статус ответа = 200");
                JsonElement jsonElement = JsonParser.parseString(response.body());
                if (jsonElement.isJsonObject()) {
                    JsonObject jsonObject = jsonElement.getAsJsonObject();
                    requestBody = jsonObject.toString();
                }
            }
            System.out.println("Запрос <.load> обработан, статус запроса: " + response.statusCode());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        return requestBody;
    }
}
