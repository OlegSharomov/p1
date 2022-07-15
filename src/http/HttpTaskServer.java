package http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import models.Epic;
import models.Subtask;
import models.Task;
import exceptions.HttpInvalidRequestException;
import service.TaskManager;
import util.Managers;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static service.InMemoryTaskManager.isWrongTask;

/* HttpTaskServer - принимает запросы от клиента пользователя, обрабатывает их в менеджере
 и отправляет информацию на KVServer через KVTaskClient */
public class HttpTaskServer {
    private final TaskManager manager;
    private final HttpServer httpServer;
    private final Gson gson = new Gson();
    private final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private final String URL = "/tasks";
    private final int PORT = 8080;

    public HttpTaskServer() {
        URI uri = URI.create("http://localhost:8078");
        manager = Managers.getDefault(uri);
        httpServer = createHTTPTaskServer();
        httpServer.start();
        System.out.println("Сервер задач начал работать");
    }

    public void stop() {
        httpServer.stop(0);
    }

    public TaskManager getManager() {
        return manager;
    }

    private HttpServer createHTTPTaskServer() {
        HttpServer server = null;
        try {
            server = HttpServer.create(new InetSocketAddress(PORT), 0);
            server.createContext(URL, (httpExchange) -> {
                String method = httpExchange.getRequestMethod();
                URI uri = httpExchange.getRequestURI();
                String path = uri.getPath();
                String[] paths = path.split("/");
                if (paths[2].startsWith("task")
                        && (path.endsWith("task") || (Integer.parseInt(uri.getQuery().substring(3)) > 0))) {
                    try {
                        uploadURITask(httpExchange, method, uri, path);
                    } catch (IOException exception) {
                        httpExchange.sendResponseHeaders(404, 0);
                        OutputStream os = httpExchange.getResponseBody();
                        os.close();
                    } catch (HttpInvalidRequestException exception) {
                        httpExchange.sendResponseHeaders(400, 0);
                        OutputStream os = httpExchange.getResponseBody();
                        os.close();
                    }
                } else if (paths[2].startsWith("subtask")
                        && (path.endsWith("subtask") || (Integer.parseInt(uri.getQuery().substring(3)) > 0))) {
                    try {
                        uploadURISubtask(httpExchange, method, uri, path);
                    } catch (IOException exception) {
                        httpExchange.sendResponseHeaders(404, 0);
                        OutputStream os = httpExchange.getResponseBody();
                        os.close();
                    } catch (HttpInvalidRequestException exception) {
                        httpExchange.sendResponseHeaders(400, 0);
                        OutputStream os = httpExchange.getResponseBody();
                        os.close();
                    }
                } else if (paths[2].startsWith("epic")
                        && (path.endsWith("epic") || (Integer.parseInt(uri.getQuery().substring(3)) > 0))) {
                    try {
                        uploadURIEpic(httpExchange, method, uri, path);
                    } catch (IOException exception) {
                        httpExchange.sendResponseHeaders(404, 0);
                        OutputStream os = httpExchange.getResponseBody();
                        os.close();
                    } catch (HttpInvalidRequestException exception) {
                        httpExchange.sendResponseHeaders(400, 0);
                        OutputStream os = httpExchange.getResponseBody();
                        os.close();
                    }
                } else if (paths[2].startsWith("history")) {
                    String response = gson.toJson(manager.getHistoryInMemory().getHistory());
                    httpExchange.sendResponseHeaders(200, 0);
                    try (OutputStream os = httpExchange.getResponseBody()) {
                        os.write(response.getBytes(DEFAULT_CHARSET));
                    }
                } else if (paths[2].isEmpty()) {
                    String response = gson.toJson(manager.getPrioritizedTasks());
                    httpExchange.sendResponseHeaders(200, 0);
                    try (OutputStream os = httpExchange.getResponseBody()) {
                        os.write(response.getBytes(DEFAULT_CHARSET));
                    }
                } else {
                    System.out.println("Адрес URL указан не корректно");
                    httpExchange.sendResponseHeaders(404, 0);
                    OutputStream os = httpExchange.getResponseBody();
                    os.close();
                }
            });
        } catch (IOException e) {
            System.out.println("Произошла ошибка. Сервер не создан");
            e.printStackTrace();
        }
        return server;
    }

    private void uploadURITask(HttpExchange httpExchange, String method, URI uri, String path)
            throws IOException, HttpInvalidRequestException {
        switch (method) {
            case "GET":
                String response;
                if (path.endsWith("task")) {                      // обработка запроса получения всех задач
                    response = gson.toJson(manager.getAllTasks());
                } else {                                        // обработка запроса получения задачи по id
                    int idOfTask = Integer.parseInt(uri.getQuery().substring(3));
                    if (manager.getAllTasks().stream().noneMatch(task -> task.getId() == idOfTask)) {
                        throw new IOException();
                    }
                    response = gson.toJson(manager.getTaskById(idOfTask));
                }
                httpExchange.sendResponseHeaders(200, 0);
                try (OutputStream os = httpExchange.getResponseBody()) {
                    os.write(response.getBytes(DEFAULT_CHARSET));
                }
                break;
            case "POST":
                String jsonTask = new String(httpExchange.getRequestBody().readAllBytes(), DEFAULT_CHARSET);
                Task task = gson.fromJson(jsonTask, Task.class);
                if (path.endsWith("task")) {                         // обработка запроса создания задачи
                    if (task == null || isWrongTask(task) || !manager.findIntersection(task)
                            || manager.getAllTasks().contains(task)) {
                        throw new HttpInvalidRequestException("При добавлении задачи произошел сбой");
                    }
                    manager.createNewTask(task);
                } else {                                             // обработка запроса обновления задачи
                    if (task == null || isWrongTask(task) || !manager.findIntersection(task)
                            || manager.getAllTasks().stream().noneMatch(task1 -> task1.getId() == task.getId())) {
                        throw new HttpInvalidRequestException("При добавлении задачи произошел сбой");
                    }
                    manager.updateTask(task);
                }
                httpExchange.sendResponseHeaders(200, 0);
                OutputStream os1 = httpExchange.getResponseBody();
                os1.close();
                break;
            case "DELETE":
                if (path.endsWith("task")) {                     // обработка запроса удаления всех задач
                    manager.clearAllTasks();
                } else {                                        // обработка запроса удаления задачи по id
                    int idOfTask = Integer.parseInt(uri.getQuery().substring(3));
                    if (manager.getAllTasks().stream().noneMatch(task1 -> task1.getId() == idOfTask)) {
                        throw new IOException();
                    }
                    manager.removeTaskById(idOfTask);
                }
                httpExchange.sendResponseHeaders(200, 0);
                OutputStream os2 = httpExchange.getResponseBody();
                os2.close();
                break;
            default:
                httpExchange.sendResponseHeaders(400, 0);
                OutputStream os3 = httpExchange.getResponseBody();
                os3.close();
        }
    }

    private void uploadURISubtask(HttpExchange httpExchange, String method, URI uri, String path)
            throws IOException, HttpInvalidRequestException {
        switch (method) {
            case "GET":
                String response;
                if (path.endsWith("subtask")) {                 // обработка запроса получения всех подзадач
                    response = gson.toJson(manager.getAllSubtasks());
                } else {                                     // обработка запроса получения подзадачи по id
                    int idOfSubtask = Integer.parseInt(uri.getQuery().substring(3));
                    if (manager.getAllSubtasks().stream().noneMatch(subtask -> subtask.getId() == idOfSubtask)) {
                        throw new IOException();
                    }
                    response = gson.toJson(manager.getSubtaskById(idOfSubtask));
                }
                httpExchange.sendResponseHeaders(200, 0);
                try (OutputStream os = httpExchange.getResponseBody()) {
                    os.write(response.getBytes(DEFAULT_CHARSET));
                }
                break;
            case "POST":
                String jsonSubtask = new String(httpExchange.getRequestBody().readAllBytes(), DEFAULT_CHARSET);
                Subtask subtask = gson.fromJson(jsonSubtask, Subtask.class);
                if (path.endsWith("subtask")) {                     // обработка запроса создания подзадачи
                    if (subtask == null || isWrongTask(subtask) || manager.getAllSubtasks().contains(subtask)
                            || !manager.findIntersection(subtask)
                            || manager.getAllEpics().stream()
                            .noneMatch(epic -> epic.getId() == subtask.getIdOfEpicNumber())) {
                        throw new HttpInvalidRequestException("При добавлении подзадачи произошел сбой");
                    }
                    manager.createNewSubtask(subtask);
                } else {                                           // обработка запроса обновления подзадачи
                    if (subtask == null || isWrongTask(subtask) || !manager.findIntersection(subtask)
                            || manager.getAllSubtasks().stream()
                            .noneMatch(subtask1 -> subtask1.getId() == subtask.getId())) {
                        throw new HttpInvalidRequestException("При добавлении подзадачи произошел сбой");
                    }
                    manager.updateSubtask(subtask);
                }
                httpExchange.sendResponseHeaders(200, 0);
                OutputStream os1 = httpExchange.getResponseBody();
                os1.close();
                break;
            case "DELETE":
                if (path.endsWith("subtask")) {                // обработка запроса удаления всех подзадач
                    manager.clearAllSubtasks();
                } else {                                      // обработка запроса удаления подзадачи по id
                    int idOfSubtask = Integer.parseInt(uri.getQuery().substring(3));
                    if (manager.getAllSubtasks().stream().noneMatch(subtask1 -> subtask1.getId() == idOfSubtask)) {
                        throw new IOException();
                    }
                    manager.removeSubtaskById(idOfSubtask);
                }
                httpExchange.sendResponseHeaders(200, 0);
                OutputStream os2 = httpExchange.getResponseBody();
                os2.close();
                break;
            default:
                httpExchange.sendResponseHeaders(400, 0);
                OutputStream os3 = httpExchange.getResponseBody();
                os3.close();
        }
    }

    private void uploadURIEpic(HttpExchange httpExchange, String method, URI uri, String path)
            throws IOException, HttpInvalidRequestException {
        switch (method) {
            case "GET":
                String response;
                if (path.endsWith("epic")) {                 // обработка запроса получения всех эпиков'
                    response = gson.toJson(manager.getAllEpics());
                } else {                                     // обработка запроса получения эпиков по id
                    int idOfEpic = Integer.parseInt(uri.getQuery().substring(3));
                    if (manager.getAllEpics().stream().noneMatch(epic -> epic.getId() == idOfEpic)) {
                        throw new IOException();
                    }
                    response = gson.toJson(manager.getEpicById(idOfEpic));
                }
                httpExchange.sendResponseHeaders(200, 0);
                try (OutputStream os = httpExchange.getResponseBody()) {
                    os.write(response.getBytes(DEFAULT_CHARSET));
                }
                break;
            case "POST":
                String jsonEpic = new String(httpExchange.getRequestBody().readAllBytes(), DEFAULT_CHARSET);
                Epic epic = gson.fromJson(jsonEpic, Epic.class);
                if (path.endsWith("epic")) {                     // обработка запроса создания эпиков
                    if (epic == null || isWrongTask(epic) || manager.getAllEpics().contains(epic)) {
                        throw new HttpInvalidRequestException("При добавлении эпика произошел сбой");
                    }
                    manager.createNewEpic(epic);
                } else {                                           // обработка запроса обновления эпика
                    if (epic == null || isWrongTask(epic)
                            || manager.getAllEpics().stream().noneMatch(epic1 -> epic1.getId() == epic.getId())) {
                        throw new HttpInvalidRequestException("При добавлении эпика произошел сбой");
                    }
                    manager.updateEpic(epic);
                }
                httpExchange.sendResponseHeaders(200, 0);
                OutputStream os1 = httpExchange.getResponseBody();
                os1.close();
                break;
            case "DELETE":
                if (path.endsWith("epic")) {                // обработка запроса удаления всех эпиков
                    manager.clearAllEpics();
                } else {                                      // обработка запроса удаления эпика по id
                    int idOfEpic = Integer.parseInt(uri.getQuery().substring(3));
                    if (manager.getAllEpics().stream().noneMatch(epic1 -> epic1.getId() == idOfEpic)) {
                        throw new IOException();
                    }
                    manager.removeEpicById(idOfEpic);
                }
                httpExchange.sendResponseHeaders(200, 0);
                OutputStream os2 = httpExchange.getResponseBody();
                os2.close();
                break;
            default:
                httpExchange.sendResponseHeaders(400, 0);
                OutputStream os3 = httpExchange.getResponseBody();
                os3.close();
        }
    }
}