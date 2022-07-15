package service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import http.HttpTaskServer;
import models.Epic;
import models.Status;
import models.Subtask;
import models.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import server.KVServer;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static models.Status.DONE;
import static models.Status.IN_PROGRESS;
import static models.Status.NEW;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static service.InMemoryTaskManager.getNextId;

public class HttpTaskServerTest extends FileBackedTasksManagerTest {
    private HttpTaskServer httpServer;
    private final Gson gson;
    private final HttpClient testClient;
    private KVServer kvServer;
    private final Map<String, String> kvMap = kvServer.getData();
    private static final URI URL_TASK = URI.create("http://localhost:8080/tasks/task");
    private static final URI URL_EPIC = URI.create("http://localhost:8080/tasks/epic");
    private static final URI URL_SUBTASK = URI.create("http://localhost:8080/tasks/subtask");

    HttpTaskServerTest() {
        testClient = HttpClient.newHttpClient();
        gson = new Gson();
    }

    @Override
    @AfterEach
    public void shouldDoAfterEach() {
        httpServer.stop();                                                     // закрыли HttpTaskServer и KVServer
        kvServer.stop();
        System.out.println("Остановили сервер");
    }

    @Override
    public TaskManager createManager() {
        kvServer = new KVServer();                                          // создали и запустили KVServer
        kvServer.start();
        httpServer = new HttpTaskServer();                            // создали сервер и присвоили менеджеру его поле
        return httpServer.getManager();
    }

    @Override
    @Test
    public void shouldReturnPathOfManager() {
        assertEquals("class service.HTTPTaskManager", manager.getClass().toString());
    }

    //createNewTask
    @Override
    @Test
    public void shouldReturnEmptyTaskMap() {
        HttpResponse<String> response1 = createTaskViaServer(null);
        assertNull(kvMap.get("tasks"));
        assertEquals(Collections.EMPTY_LIST, getListOfTasksByTest().listTasksOfResponse);
        assertEquals(400, response1.statusCode());
        HttpResponse<String> response2 = createTaskViaServer(wrongTask);
        assertNull(kvMap.get("tasks"));
        assertEquals(Collections.EMPTY_LIST, getListOfTasksByTest().listTasksOfResponse);
        assertEquals(400, response2.statusCode());
    }

    @Override
    @Test
    public void shouldReturnTask() {
        HttpResponse<String> response1 = null;
        HttpResponse<String> response2 = null;
        Task task11 = new Task(getNextId(), "Task11", "Description task11");
        String json1 = gson.toJson(task11);
        final HttpRequest.BodyPublisher body1 = HttpRequest.BodyPublishers.ofString(json1);
        HttpRequest request1 = HttpRequest.newBuilder().uri(URL_TASK).POST(body1).build();
        try {
            response1 = testClient.send(request1, HttpResponse.BodyHandlers.ofString());

        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        Task task12 = new Task(getNextId(), "Task12", "Description task12",
                LocalDateTime.of(2022, 4, 15, 9, 0), Duration.ofHours(12));
        String json2 = gson.toJson(task12);
        final HttpRequest.BodyPublisher body2 = HttpRequest.BodyPublishers.ofString(json2);
        HttpRequest request2 = HttpRequest.newBuilder().uri(URL_TASK).POST(body2).build();
        try {
            response2 = testClient.send(request2, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        assertEquals(List.of(task11, task12), manager.getAllTasks());
        assertEquals(200, response1.statusCode());
        assertEquals(200, response2.statusCode());
        assertEquals("[" + gson.toJson(task11) + "," + gson.toJson(task12) + "]", kvMap.get("tasks"));
    }

    @Override
    @Test
    public void shouldNotAddSameTasks() {
        HttpResponse<String> response1 = createTaskViaServer(task1);
        HttpResponse<String> response2 = createTaskViaServer(task1);
        assertEquals(Collections.singletonList(task1), getListOfTasksByTest().listTasksOfResponse);
        assertEquals(200, response1.statusCode());
        assertEquals(400, response2.statusCode());
    }

    //createNewEpic
    @Override
    @Test
    public void shouldReturnEmptyEpicMap() {
        HttpResponse<String> response1 = createEpicViaServer(null);
        assertNull(kvMap.get("epics"));
        assertEquals(Collections.EMPTY_LIST, getListOfEpicsByTest().listTasksOfResponse);
        assertEquals(400, response1.statusCode());
        HttpResponse<String> response2 = createEpicViaServer(wrongEpic);
        assertNull(kvMap.get("epics"));
        assertEquals(Collections.EMPTY_LIST, getListOfEpicsByTest().listTasksOfResponse);
        assertEquals(400, response2.statusCode());
    }

    @Override
    @Test
    public void shouldReturnEpic3() {
        HttpResponse<String> response1 = createEpicViaServer(epic3);            // создали epic3 через сервер
        assertEquals(List.of(epic3), manager.getAllEpics());
        assertEquals(200, response1.statusCode());
        assertEquals("[" + gson.toJson(epic3) + "]", kvMap.get("epics"));
    }

    @Override
    @Test
    public void shouldNotAddSameEpics() {
        HttpResponse<String> response1 = createEpicViaServer(epic3);
        HttpResponse<String> response2 = createEpicViaServer(epic3);
        assertEquals(Collections.singletonList(epic3), getListOfEpicsByTest().listTasksOfResponse);
        assertEquals(200, response1.statusCode());
        assertEquals(400, response2.statusCode());
    }

    //createNewSubtask
    @Override
    @Test
    public void shouldReturnEmptySubtaskMap() {
        HttpResponse<String> response1 = createSubtaskViaServer(null);
        createEpicViaServer(epic3);
        assertEquals("[]", kvMap.get("subtasks"));
        assertEquals(Collections.EMPTY_LIST, getListOfSubtasksByTest().listTasksOfResponse);
        assertEquals(400, response1.statusCode());
        HttpResponse<String> response2 = createSubtaskViaServer(wrongSubtask);
        assertEquals("[]", kvMap.get("subtasks"));
        assertEquals(Collections.EMPTY_LIST, getListOfSubtasksByTest().listTasksOfResponse);
        assertEquals(400, response2.statusCode());
    }

    @Override
    @Test
    public void shouldReturnEmptySubtaskMap2() {
        HttpResponse<String> response = createSubtaskViaServer(subtask7);
        assertEquals(400, response.statusCode());
        assertEquals(Collections.EMPTY_LIST, getListOfSubtasksByTest().listTasksOfResponse);
    }

    @Override
    @Test
    public void shouldReturnSubtask5() {
        HttpResponse<String> response1 = createEpicViaServer(epic3);
        HttpResponse<String> response2 = createSubtaskViaServer(subtask5);
        assertEquals(List.of(subtask5), manager.getAllSubtasks());
        assertEquals(200, response1.statusCode());
        assertEquals(200, response2.statusCode());
        assertEquals("[" + gson.toJson(subtask5) + "]", kvMap.get("subtasks"));
    }

    @Override
    @Test
    public void shouldNotAddSameSubtasks() {
        createEpicViaServer(epic3);
        HttpResponse<String> response1 = createSubtaskViaServer(subtask5);
        HttpResponse<String> response2 = createSubtaskViaServer(subtask5);
        assertEquals(List.of(subtask5), manager.getAllSubtasks());
        assertEquals(200, response1.statusCode());
        assertEquals(400, response2.statusCode());
        assertEquals("[" + gson.toJson(subtask5) + "]", kvMap.get("subtasks"));
    }

    //checkAndChangeStatus
    @Override
    @Test
    public void shouldReturnStatusNewWithEmptyListOfSubtasks() {
        createEpicViaServer(epic3);
        Epic epic = getEpicByIdForTest(3).epicOfResponse;
        assertEquals(Status.NEW, epic.getStatus());
    }

    @Override
    @Test
    public void shouldReturnStatusDONEWithAllSubtasksDONE() {
        createEpicViaServer(epic3);
        subtask5.setStatus(DONE);
        createSubtaskViaServer(subtask5);
        subtask6.setStatus(DONE);
        createSubtaskViaServer(subtask6);
        subtask7.setStatus(DONE);
        createSubtaskViaServer(subtask7);
        Epic epic = getEpicByIdForTest(3).epicOfResponse;
        assertEquals(DONE, epic.getStatus());
    }

    @Override
    @Test
    public void shouldReturnStatusIN_PROGRESSWithAllSubtasksDONEAndNEW() {
        createEpicViaServer(epic3);
        subtask5.setStatus(DONE);
        createSubtaskViaServer(subtask5);
        subtask6.setStatus(DONE);
        createSubtaskViaServer(subtask6);
        createSubtaskViaServer(subtask7);
        Epic epic = getEpicByIdForTest(3).epicOfResponse;
        assertEquals(IN_PROGRESS, epic.getStatus());
    }

    @Override
    @Test
    public void shouldReturnStatusDONEWithAllSubtasksDONEAfterRemove() {
        createEpicViaServer(epic3);
        subtask5.setStatus(DONE);
        createSubtaskViaServer(subtask5);
        subtask6.setStatus(DONE);
        createSubtaskViaServer(subtask6);
        createSubtaskViaServer(subtask7);
        URI urlForUpdate = URI.create("http://localhost:8080/tasks/subtask/?id=7");
        HttpRequest request2 = HttpRequest.newBuilder().uri(urlForUpdate).DELETE().build();
        try {
            testClient.send(request2, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        Epic epic = getEpicByIdForTest(3).epicOfResponse;
        assertEquals(DONE, epic.getStatus());
    }

    @Override
    @Test
    public void shouldReturnStatusIN_PROGRESSWithAllSubtasksIN_PROGRESS() {
        createEpicViaServer(epic3);
        subtask5.setStatus(IN_PROGRESS);
        createSubtaskViaServer(subtask5);
        subtask6.setStatus(IN_PROGRESS);
        createSubtaskViaServer(subtask6);
        subtask7.setStatus(IN_PROGRESS);
        createSubtaskViaServer(subtask7);
        Epic epic = getEpicByIdForTest(3).epicOfResponse;
        assertEquals(IN_PROGRESS, epic.getStatus());
    }

    @Override
    @Test
    public void shouldReturnStatusNEWWithAllSubtasksNEW() {
        createEpicViaServer(epic3);
        createSubtaskViaServer(subtask5);
        createSubtaskViaServer(subtask6);
        createSubtaskViaServer(subtask7);
        Epic epic = getEpicByIdForTest(3).epicOfResponse;
        assertEquals(NEW, epic.getStatus());
    }

    //getAllTasks
    @Override
    @Test
    public void shouldReturnListOfTasks() {
        createTaskViaServer(task1);
        createTaskViaServer(task2);
        List<Task> tasksOfResponse = getListOfTasksByTest().listTasksOfResponse;
        HttpResponse<String> response3 = getListOfTasksByTest().response;
        assertEquals(List.of(task1, task2), tasksOfResponse);
        assertEquals(200, response3.statusCode());
        assertEquals("[" + gson.toJson(task1) + "," + gson.toJson(task2) + "]", kvMap.get("tasks"));
    }

    //getAllEpics
    @Override
    @Test
    public void shouldReturnListOfEpics() {
        createEpicViaServer(epic3);
        createEpicViaServer(epic4);
        List<Epic> epicsOfResponse = getListOfEpicsByTest().listTasksOfResponse;
        HttpResponse<String> response3 = getListOfEpicsByTest().response;
        assertEquals(List.of(epic3, epic4), epicsOfResponse);
        assertEquals(200, response3.statusCode());
    }

    //getAllSubtasks
    @Override
    @Test
    public void shouldReturnListOfSubtasks() {
        createEpicViaServer(epic3);
        createSubtaskViaServer(subtask5);
        createSubtaskViaServer(subtask6);
        List<Subtask> subtasksOfResponse = getListOfSubtasksByTest().listTasksOfResponse;
        HttpResponse<String> response3 = getListOfSubtasksByTest().response;
        assertEquals(List.of(subtask5, subtask6), subtasksOfResponse);
        assertEquals(200, response3.statusCode());
        assertEquals("[" + gson.toJson(subtask5) + "," + gson.toJson(subtask6) + "]", kvMap.get("subtasks"));
    }

    //clearAllTasks
    @Override
    @Test
    public void shouldReturnEmptyListOfTask() {
        createTaskViaServer(task1);
        createTaskViaServer(task2);
        HttpResponse<String> response3 = null;
        HttpRequest request3 = HttpRequest.newBuilder().uri(URL_TASK).DELETE().build();
        try {
            response3 = testClient.send(request3, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        assertEquals(Collections.EMPTY_LIST, manager.getAllTasks());
        assertEquals(200, response3.statusCode());
        assertEquals("[]", kvMap.get("tasks"));
    }

    //clearAllEpics
    @Override
    @Test
    public void shouldReturnEmptyListOfEpic() {
        createEpicViaServer(epic3);
        createEpicViaServer(epic4);
        HttpResponse<String> response3 = null;
        HttpRequest request3 = HttpRequest.newBuilder().uri(URL_EPIC).DELETE().build();
        try {
            response3 = testClient.send(request3, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        assertEquals(Collections.EMPTY_LIST, manager.getAllEpics());
        assertEquals(200, response3.statusCode());
        assertEquals("[]", kvMap.get("epics"));
    }

    //clearAllSubtasks
    @Override
    @Test
    public void shouldReturnEmptyListOfSubtasks() {
        createEpicViaServer(epic3);
        createSubtaskViaServer(subtask5);
        createSubtaskViaServer(subtask6);
        getEpicByIdForTest(3);
        getSubtaskByIdForTest(5);
        getSubtaskByIdForTest(6);
        HttpResponse<String> response3 = null;
        HttpRequest request3 = HttpRequest.newBuilder().uri(URL_SUBTASK).DELETE().build();
        try {
            response3 = testClient.send(request3, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        assertEquals(Collections.EMPTY_LIST, manager.getAllSubtasks());
        assertEquals(200, response3.statusCode());
        assertEquals("[]", kvMap.get("subtasks"));
    }

    @Override
    @Test
    public void shouldReturnEmptyListOfSubtasksAndEmptyListOfEpic() {
        createEpicViaServer(epic3);
        createSubtaskViaServer(subtask5);
        createSubtaskViaServer(subtask6);
        HttpResponse<String> response3 = null;
        HttpRequest request3 = HttpRequest.newBuilder().uri(URL_EPIC).DELETE().build();
        try {
            response3 = testClient.send(request3, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        assertEquals(Collections.EMPTY_LIST, manager.getAllEpics());
        assertEquals(Collections.EMPTY_LIST, manager.getAllSubtasks());
        assertEquals(200, response3.statusCode());
        assertEquals("[]", kvMap.get("epics"));
        assertEquals("[]", kvMap.get("subtasks"));

    }

    //getTaskById
    @Override
    @Test
    public void shouldReturnNullByGetTask() {
        HttpResponse<String> response = getTaskByIdForTest(1).response;
        assertEquals(404, response.statusCode());
        assertEquals(Collections.EMPTY_LIST, getListOfEpicsByTest().listTasksOfResponse);

    }

    @Override
    @Test
    public void shouldReturnTask1ByGetTask() {
        createTaskViaServer(task1);
        createTaskViaServer(task2);
        TwoParametersByGetTaskByID<Task> tp = getTaskByIdForTest(2);
        Task taskOfResponse = tp.taskOfResponse;
        HttpResponse<String> response3 = tp.response;
        assertEquals(task2, taskOfResponse);
        assertEquals(200, response3.statusCode());
    }

    //getEpicById
    @Override
    @Test
    public void shouldReturnNullByGetEpic() {
        HttpResponse<String> response = getEpicByIdForTest(3).response;
        assertEquals(404, response.statusCode());
        assertEquals(Collections.EMPTY_LIST, getListOfEpicsByTest().listTasksOfResponse);
    }

    @Override
    @Test
    public void shouldReturnEpic3ByGetEpic() {
        createEpicViaServer(epic3);
        TwoParametersByGetEPICByID tp = getEpicByIdForTest(3);
        Task taskOfResponse = tp.epicOfResponse;
        HttpResponse<String> response3 = tp.response;
        assertEquals(epic3, taskOfResponse);
        assertEquals(200, response3.statusCode());
    }

    //getSubtaskById
    @Override
    @Test
    public void shouldReturnNullByGetSubtask() {
        HttpResponse<String> response = getSubtaskByIdForTest(5).response;
        assertEquals(404, response.statusCode());
        assertEquals(Collections.EMPTY_LIST, getListOfSubtasksByTest().listTasksOfResponse);
    }

    @Override
    @Test
    public void shouldReturnSubtask5ByGetSubtask() {
        createEpicViaServer(epic3);
        createSubtaskViaServer(subtask5);
        TwoParametersByGetTaskByID<Subtask> tp = getSubtaskByIdForTest(5);
        Task taskOfResponse = tp.taskOfResponse;
        HttpResponse<String> response3 = tp.response;
        assertEquals(subtask5, taskOfResponse);
        assertEquals(200, response3.statusCode());
    }

    //updateTask
    @Override
    @Test
    public void shouldReturnTask1_1() {
        createTaskViaServer(task1);
        HttpResponse<String> response2 = null;
        Task task1_1 = new Task(1, "Task1_1", "Update description task1_1",
                LocalDateTime.of(2022, 4, 15, 9, 0), Duration.ofHours(12));
        String json2 = gson.toJson(task1_1);
        URI urlForUpdate = URI.create("http://localhost:8080/tasks/task/?id=1");
        final HttpRequest.BodyPublisher body2 = HttpRequest.BodyPublishers.ofString(json2);
        HttpRequest request2 = HttpRequest.newBuilder().uri(urlForUpdate).POST(body2).build();
        try {
            response2 = testClient.send(request2, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        assertEquals(task1_1, getTaskByIdForTest(1).taskOfResponse);
        assertEquals(200, response2.statusCode());
    }

    @Override
    @Test
    public void shouldReturnNullByNullUpdateTask() {
        createTaskViaServer(task1);
        HttpResponse<String> response1 = null;
        String json1 = gson.toJson(null);
        URI urlForUpdate1 = URI.create("http://localhost:8080/tasks/task/?id=1");
        final HttpRequest.BodyPublisher body1 = HttpRequest.BodyPublishers.ofString(json1);
        HttpRequest request1 = HttpRequest.newBuilder().uri(urlForUpdate1).POST(body1).build();
        try {
            response1 = testClient.send(request1, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        assertEquals(task1, getTaskByIdForTest(1).taskOfResponse);
        assertEquals(400, response1.statusCode());
// проверяем неправильно созданную задачу
        HttpResponse<String> response2 = null;
        String json2 = gson.toJson(wrongTask);
        URI urlForUpdate2 = URI.create("http://localhost:8080/tasks/task/?id=1");
        final HttpRequest.BodyPublisher body2 = HttpRequest.BodyPublishers.ofString(json2);
        HttpRequest request2 = HttpRequest.newBuilder().uri(urlForUpdate2).POST(body2).build();
        try {
            response2 = testClient.send(request2, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        assertEquals(task1, getTaskByIdForTest(1).taskOfResponse);
        assertEquals(400, response2.statusCode());
    }

    @Override
    @Test
    public void shouldReturnNullByUpdateTaskWithEmptyMap() {
        HttpResponse<String> response2 = null;
        Epic epic3_1 = new Epic(3, "Epic3_1", "Update description epic3_1");
        String json2 = gson.toJson(epic3_1);
        URI urlForUpdate = URI.create("http://localhost:8080/tasks/epic/?id=3");
        final HttpRequest.BodyPublisher body2 = HttpRequest.BodyPublishers.ofString(json2);
        HttpRequest request2 = HttpRequest.newBuilder().uri(urlForUpdate).POST(body2).build();
        try {
            response2 = testClient.send(request2, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        assertEquals(Collections.EMPTY_LIST, getListOfEpicsByTest().listTasksOfResponse);
        assertNull(getEpicByIdForTest(3).epicOfResponse);
        assertEquals(400, response2.statusCode());
    }

    //updateEpic
    @Override
    @Test
    public void shouldReturnEpic3_1() {
        createEpicViaServer(epic3);
        HttpResponse<String> response2 = null;
        Epic epic3_1 = new Epic(3, "Epic3_1", "Update description epic3_1");
        String json2 = gson.toJson(epic3_1);
        URI urlForUpdate = URI.create("http://localhost:8080/tasks/epic/?id=3");
        final HttpRequest.BodyPublisher body2 = HttpRequest.BodyPublishers.ofString(json2);
        HttpRequest request2 = HttpRequest.newBuilder().uri(urlForUpdate).POST(body2).build();
        try {
            response2 = testClient.send(request2, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        assertEquals(epic3_1, getEpicByIdForTest(3).epicOfResponse);
        assertEquals(200, response2.statusCode());
    }

    @Override
    @Test
    public void shouldReturnNullByNullUpdateEpic() {
        createEpicViaServer(epic3);
        HttpResponse<String> response1 = null;
        String json1 = gson.toJson(null);
        URI urlForUpdate1 = URI.create("http://localhost:8080/tasks/epic/?id=3");
        final HttpRequest.BodyPublisher body1 = HttpRequest.BodyPublishers.ofString(json1);
        HttpRequest request1 = HttpRequest.newBuilder().uri(urlForUpdate1).POST(body1).build();
        try {
            response1 = testClient.send(request1, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        assertEquals(epic3, getEpicByIdForTest(3).epicOfResponse);
        assertEquals(400, response1.statusCode());
// проверяем неправильно созданный эпик
        HttpResponse<String> response2 = null;
        String json2 = gson.toJson(wrongEpic);
        URI urlForUpdate2 = URI.create("http://localhost:8080/tasks/epic/?id=3");
        final HttpRequest.BodyPublisher body2 = HttpRequest.BodyPublishers.ofString(json2);
        HttpRequest request2 = HttpRequest.newBuilder().uri(urlForUpdate2).POST(body2).build();
        try {
            response2 = testClient.send(request2, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        assertEquals(epic3, getEpicByIdForTest(3).epicOfResponse);
        assertEquals(400, response2.statusCode());
    }

    @Override
    @Test
    public void shouldReturnNullByUpdateEpicWithEmptyMap() {
        HttpResponse<String> response2 = null;
        Task task1_1 = new Task(1, "Task1_1", "Update description task1_1",
                LocalDateTime.of(2022, 4, 15, 9, 0), Duration.ofHours(12));
        String json2 = gson.toJson(task1_1);
        URI urlForUpdate = URI.create("http://localhost:8080/tasks/task/?id=1");
        final HttpRequest.BodyPublisher body2 = HttpRequest.BodyPublishers.ofString(json2);
        HttpRequest request2 = HttpRequest.newBuilder().uri(urlForUpdate).POST(body2).build();
        try {
            response2 = testClient.send(request2, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        assertEquals(Collections.EMPTY_LIST, getListOfTasksByTest().listTasksOfResponse);
        assertNull(getTaskByIdForTest(1).taskOfResponse);
        assertEquals(400, response2.statusCode());
    }

    @Override
    @Test
    public void shouldReturnEpicWithNumbersOfSubtasksByUpdate() {
        createEpicViaServer(epic3);
        subtask5.setStatus(DONE);
        createSubtaskViaServer(subtask5);
        createSubtaskViaServer(subtask6);
        createSubtaskViaServer(subtask7);
        Epic epic3_1 = new Epic(3, "Имя эпика 3_1", "Описание эпика 3_1");
        HttpResponse<String> response2 = null;
        String json2 = gson.toJson(epic3_1);
        URI urlForUpdate = URI.create("http://localhost:8080/tasks/epic/?id=3");
        final HttpRequest.BodyPublisher body2 = HttpRequest.BodyPublishers.ofString(json2);
        HttpRequest request2 = HttpRequest.newBuilder().uri(urlForUpdate).POST(body2).build();
        try {
            response2 = testClient.send(request2, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        assertEquals(manager.getEpicById(3), getEpicByIdForTest(3).epicOfResponse);
        assertEquals(200, response2.statusCode());
        assertEquals(IN_PROGRESS, getEpicByIdForTest(3).epicOfResponse.getStatus());
        assertEquals("[" + gson.toJson(manager.getEpicById(3)) + "]", kvMap.get("epics"));
    }

    //updateSubtask
    @Override
    @Test
    public void shouldReturnSubtask5_1() {
        createEpicViaServer(epic3);
        createSubtaskViaServer(subtask6);
        HttpResponse<String> response2 = null;
        Subtask subtask6_1 = new Subtask(6, "Subtask6_1", "Update description subtask6_1", 3,
                LocalDateTime.of(2022, 6, 7, 5, 0), Duration.ofHours(6));
        String json2 = gson.toJson(subtask6_1);
        URI urlForUpdate = URI.create("http://localhost:8080/tasks/subtask/?id=6");
        final HttpRequest.BodyPublisher body2 = HttpRequest.BodyPublishers.ofString(json2);
        HttpRequest request2 = HttpRequest.newBuilder().uri(urlForUpdate).POST(body2).build();
        try {
            response2 = testClient.send(request2, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        assertEquals(subtask6_1, getSubtaskByIdForTest(6).taskOfResponse);
        assertEquals(200, response2.statusCode());
    }

    @Override
    @Test
    public void shouldReturnNullByNullUpdateSubtask() {
        createEpicViaServer(epic3);
        createSubtaskViaServer(subtask5);
        HttpResponse<String> response1 = null;
        String json1 = gson.toJson(null);
        URI urlForUpdate1 = URI.create("http://localhost:8080/tasks/subtask/?id=5");
        final HttpRequest.BodyPublisher body1 = HttpRequest.BodyPublishers.ofString(json1);
        HttpRequest request1 = HttpRequest.newBuilder().uri(urlForUpdate1).POST(body1).build();
        try {
            response1 = testClient.send(request1, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        assertEquals(subtask5, getSubtaskByIdForTest(5).taskOfResponse);
        assertEquals(400, response1.statusCode());
        // проверяем неправильно созданную задачу
        HttpResponse<String> response2 = null;
        String json2 = gson.toJson(wrongSubtask);
        URI urlForUpdate2 = URI.create("http://localhost:8080/tasks/subtask/?id=5");
        final HttpRequest.BodyPublisher body2 = HttpRequest.BodyPublishers.ofString(json2);
        HttpRequest request2 = HttpRequest.newBuilder().uri(urlForUpdate2).POST(body2).build();
        try {
            response2 = testClient.send(request2, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        assertEquals(subtask5, getSubtaskByIdForTest(5).taskOfResponse);
        assertEquals(400, response2.statusCode());
    }

    @Override
    @Test
    public void shouldReturnNullByUpdateSubtaskWithEmptyMap() {
        HttpResponse<String> response2 = null;                          //создали Ответ
        Subtask subtask6_1 = new Subtask(6, "Subtask6_1", "Update description subtask6_1", 3,
                LocalDateTime.of(2022, 6, 7, 5, 0), Duration.ofHours(6));
        String json2 = gson.toJson(subtask6_1);
        URI urlForUpdate = URI.create("http://localhost:8080/tasks/subtask/?id=6");
        final HttpRequest.BodyPublisher body2 = HttpRequest.BodyPublishers.ofString(json2);
        HttpRequest request2 = HttpRequest.newBuilder().uri(urlForUpdate).POST(body2).build();
        try {
            response2 = testClient.send(request2, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        assertEquals(Collections.EMPTY_LIST, getListOfSubtasksByTest().listTasksOfResponse);
        assertNull(getSubtaskByIdForTest(6).taskOfResponse);
        assertEquals(400, response2.statusCode());
    }

    @Override
    @Test
    public void shouldAssignOldIDOfEpicByUpdate() {
        createEpicViaServer(epic3);
        createSubtaskViaServer(subtask6);
        Subtask subtask6_1 = new Subtask(6, "Subtask6_1", "Update description subtask6_1", 4,
                LocalDateTime.of(2022, 6, 7, 5, 0), Duration.ofHours(6));
        String json2 = gson.toJson(subtask6_1);
        URI urlForUpdate = URI.create("http://localhost:8080/tasks/subtask/?id=6");
        final HttpRequest.BodyPublisher body2 = HttpRequest.BodyPublishers.ofString(json2);
        HttpRequest request2 = HttpRequest.newBuilder().uri(urlForUpdate).POST(body2).build();
        HttpResponse<String> response2 = null;                          //создали Ответ
        try {
            response2 = testClient.send(request2, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        Subtask subtask6_2 = new Subtask(6, "Subtask6_1", "Update description subtask6_1", 3,
                LocalDateTime.of(2022, 6, 7, 5, 0), Duration.ofHours(6));

        assertEquals(Collections.singletonList(subtask6_2), getListOfSubtasksByTest().listTasksOfResponse);
        assertEquals(subtask6_2, getSubtaskByIdForTest(6).taskOfResponse);
        assertEquals(200, response2.statusCode());
    }

    //removeTaskById
    @Override
    @Test
    public void shouldRemoveTaskAndGetCorrectHistory() {
        createTaskViaServer(task1);
        createTaskViaServer(task2);
        getTaskByIdForTest(1);
        getTaskByIdForTest(2);
        URI urlForUpdate = URI.create("http://localhost:8080/tasks/task/?id=1");
        HttpRequest request2 = HttpRequest.newBuilder().uri(urlForUpdate).DELETE().build();
        HttpResponse<String> response2 = null;
        try {
            response2 = testClient.send(request2, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        assertEquals(Collections.singletonList(task2), manager.getAllTasks());
        assertEquals(Collections.singletonList(task2), manager.getHistoryInMemory().getHistory());
        assertEquals(200, response2.statusCode());
        assertEquals("[" + gson.toJson(task2) + "]", kvMap.get("tasks"));
        assertEquals("[" + gson.toJson(task2) + "]", kvMap.get("history"));
    }

    @Override
    @Test
    public void shouldNotChangeTaskMap() {
        createTaskViaServer(task1);
        createTaskViaServer(task2);
        getTaskByIdForTest(1);
        getTaskByIdForTest(2);
        URI urlForUpdate = URI.create("http://localhost:8080/tasks/task/?id=9");
        HttpRequest request2 = HttpRequest.newBuilder().uri(urlForUpdate).DELETE().build();
        HttpResponse<String> response2 = null;
        try {
            response2 = testClient.send(request2, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        assertEquals(404, response2.statusCode());
        assertEquals(List.of(task1, task2), getListOfTasksByTest().listTasksOfResponse);
        assertEquals("[" + gson.toJson(task1) + "," + gson.toJson(task2) + "]", kvMap.get("history"));
    }

    //removeEpicById
    @Override
    @Test
    public void shouldRemoveEpicAndGetCorrectHistory() {
        createEpicViaServer(epic3);
        createEpicViaServer(epic4);
        getEpicByIdForTest(3);
        getEpicByIdForTest(4);
        URI urlForUpdate = URI.create("http://localhost:8080/tasks/epic/?id=3");
        HttpRequest request2 = HttpRequest.newBuilder().uri(urlForUpdate).DELETE().build();
        HttpResponse<String> response2 = null;
        try {
            response2 = testClient.send(request2, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        assertEquals(Collections.singletonList(epic4), manager.getAllEpics());
        assertEquals(Collections.singletonList(epic4), manager.getHistoryInMemory().getHistory());
        assertEquals(200, response2.statusCode());
        assertEquals("[" + gson.toJson(epic4) + "]", kvMap.get("epics"));
        assertEquals("[" + gson.toJson(epic4) + "]", kvMap.get("history"));
    }

    @Override
    @Test
    public void shouldNotChangeEpicMap() {
        createEpicViaServer(epic3);
        createEpicViaServer(epic4);
        getEpicByIdForTest(3);
        getEpicByIdForTest(4);
        URI urlForUpdate = URI.create("http://localhost:8080/tasks/epic/?id=9");
        HttpRequest request2 = HttpRequest.newBuilder().uri(urlForUpdate).DELETE().build();
        HttpResponse<String> response2 = null;
        try {
            response2 = testClient.send(request2, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        assertEquals(404, response2.statusCode());
        assertEquals(List.of(epic3, epic4), getListOfEpicsByTest().listTasksOfResponse);
        assertEquals("[" + gson.toJson(epic3) + "," + gson.toJson(epic4) + "]", kvMap.get("history"));
    }

    //removeSubtaskById
    @Override
    @Test
    public void shouldRemoveSubtaskEverywhereAndGetCorrectHistory() {
        createEpicViaServer(epic3);
        createSubtaskViaServer(subtask5);
        createSubtaskViaServer(subtask6);
        getEpicByIdForTest(3);
        getSubtaskByIdForTest(5);
        getSubtaskByIdForTest(6);
        URI urlForUpdate = URI.create("http://localhost:8080/tasks/subtask/?id=5");
        HttpRequest request2 = HttpRequest.newBuilder().uri(urlForUpdate).DELETE().build();
        HttpResponse<String> response2 = null;
        try {
            response2 = testClient.send(request2, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        assertEquals(Collections.singletonList(subtask6), manager.getAllSubtasks());
        assertEquals(List.of(manager.getEpicById(3), manager.getSubtaskById(6)),
                manager.getHistoryInMemory().getHistory());
        assertEquals(200, response2.statusCode());
        assertEquals("[" + gson.toJson(subtask6) + "]", kvMap.get("subtasks"));
        assertEquals("[" + gson.toJson(manager.getEpicById(3)) + ","
                + gson.toJson(manager.getSubtaskById(6)) + "]", kvMap.get("history"));
    }

    @Override
    @Test
    public void shouldNotChangeSubtaskMapAndHistory() {
        createEpicViaServer(epic3);
        createSubtaskViaServer(subtask5);
        createSubtaskViaServer(subtask6);
        createSubtaskViaServer(subtask7);
        getSubtaskByIdForTest(7);
        getSubtaskByIdForTest(5);
        getSubtaskByIdForTest(6);
        assertEquals(List.of(subtask5, subtask6, subtask7), getListOfSubtasksByTest().listTasksOfResponse);
        assertEquals("[" + gson.toJson(subtask7) + "," + gson.toJson(subtask5) + ","
                + gson.toJson(subtask6) + "]", kvMap.get("history"));
    }

    @Override
    @Test
    public void shouldGetSubtasksOfEpic() {
        createEpicViaServer(epic3);
        createSubtaskViaServer(subtask5);
        createSubtaskViaServer(subtask6);
        createSubtaskViaServer(subtask7);
        assertEquals(List.of(5, 6, 7), getEpicByIdForTest(3).epicOfResponse.getSubtasks());

    }

    @Override
    @Test
    public void shouldGetEmptySubtasksOfEpic() {
        createEpicViaServer(epic4);
        createSubtaskViaServer(subtask5);
        assertEquals(Collections.emptyList(), getEpicByIdForTest(4).epicOfResponse.getSubtasks());
    }

    @Override
    @Test
    public void shouldReturnHistoryInMemory() {
        createTaskViaServer(task1);
        createTaskViaServer(task2);
        createEpicViaServer(epic3);
        createEpicViaServer(epic4);
        Task task2_2 = new Task(2, "Имя задачи 2_2", "Описание задачи 2_2",
                LocalDateTime.of(2022, 4, 15, 9, 0), Duration.ofHours(12));
        String json2 = gson.toJson(task2_2);
        URI urlForUpdate = URI.create("http://localhost:8080/tasks/task/?id=2");
        final HttpRequest.BodyPublisher body2 = HttpRequest.BodyPublishers.ofString(json2);
        HttpRequest request2 = HttpRequest.newBuilder().uri(urlForUpdate).POST(body2).build();
        HttpResponse<String> response = null;
        try {
            response = testClient.send(request2, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        getTaskByIdForTest(2);
        getEpicByIdForTest(4);
        getTaskByIdForTest(1);
        getEpicByIdForTest(3);
        assertEquals(200, response.statusCode());
        assertEquals(List.of(task1, task2_2), getListOfTasksByTest().listTasksOfResponse);
        assertEquals("[" + gson.toJson(task2_2) + "," + gson.toJson(epic4) + ","
                + gson.toJson(task1) + "," + gson.toJson(epic3) + "]", kvMap.get("history"));

    }

    @Override
    @Test
    public void shouldReturnEmptyList() {
        getTaskByIdForTest(1);
        assertNull(kvMap.get("history"));
    }

    //EpicTime
    @Override
    @Test
    public void shouldReturnCorrectTimeOfEpic() {
        Epic epicT9 = new Epic(109, "EpicT9", "Description epicT9");

        Subtask subtaskT10 = new Subtask(110, "SubtaskT10", "Description subtaskT10", 109,
                LocalDateTime.of(2022, 4, 17, 12, 0), Duration.ofHours(24));
        Subtask subtaskT11 = new Subtask(111, "SubtaskT11", "Description subtaskT11", 109,
                LocalDateTime.of(2022, 4, 18, 12, 0), Duration.ofHours(6));
        Subtask subtaskT12 = new Subtask(112, "SubtaskT12", "Description subtaskT12", 109,
                LocalDateTime.of(2022, 4, 18, 18, 0), Duration.ofHours(18));
        Subtask subtaskT13 = new Subtask(113, "SubtaskT13", "Description subtaskT13", 109,
                LocalDateTime.of(2022, 4, 19, 15, 0), Duration.ofHours(3));
        Subtask subtaskT14 = new Subtask(114, "SubtaskT14", "Description subtaskT14", 109,
                LocalDateTime.of(2022, 4, 16, 18, 0), Duration.ofHours(72));
        createEpicViaServer(epicT9);
        createSubtaskViaServer(subtaskT10);
        createSubtaskViaServer(subtaskT11);
        createSubtaskViaServer(subtaskT12);
        createSubtaskViaServer(subtaskT13);
        assertEquals("17.04.2022 12:00", getEpicByIdForTest(109).epicOfResponse.getStartTimeToString());
        assertEquals("19.04.2022 18:00", getEpicByIdForTest(109).epicOfResponse.getEndTimeToString());
        createSubtaskViaServer(subtaskT14);
        //        manager.removeSubtaskById(110);
        URI urlForRemove = URI.create("http://localhost:8080/tasks/subtask/?id=110");
        HttpRequest request7 = HttpRequest.newBuilder().uri(urlForRemove).DELETE().build();
        try {
            testClient.send(request7, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        assertEquals("18.04.2022 12:00", getEpicByIdForTest(109).epicOfResponse.getStartTimeToString());
        assertEquals("19.04.2022 18:00", getEpicByIdForTest(109).epicOfResponse.getEndTimeToString());
        Subtask subtaskT13_1 = new Subtask(113, "SubtaskT13_1", "Description subtaskT13_1", 109,
                LocalDateTime.of(2022, 4, 19, 18, 0), Duration.ofHours(4));
        //        manager.updateSubtask(subtaskT13_1);
        String json8 = gson.toJson(subtaskT13_1);
        URI urlForUpdate = URI.create("http://localhost:8080/tasks/subtask/?id=113");
        final HttpRequest.BodyPublisher body8 = HttpRequest.BodyPublishers.ofString(json8);
        HttpRequest request8 = HttpRequest.newBuilder().uri(urlForUpdate).POST(body8).build();
        try {
            testClient.send(request8, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        assertEquals("18.04.2022 12:00", getEpicByIdForTest(109).epicOfResponse.getStartTimeToString());
        assertEquals("19.04.2022 22:00", getEpicByIdForTest(109).epicOfResponse.getEndTimeToString());
//        manager.clearAllSubtasks();
        HttpRequest request3 = HttpRequest.newBuilder().uri(URL_SUBTASK).DELETE().build();
        try {
            testClient.send(request3, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        assertEquals("", getEpicByIdForTest(109).epicOfResponse.getStartTimeToString());
        assertEquals("", getEpicByIdForTest(109).epicOfResponse.getEndTimeToString());

    }

    @Override
    @Test
    public void shouldNotAddTaskInTimeAnotherTask() {
        Task taskT5 = new Task(105, "TaskT5", "Description taskT5",
                LocalDateTime.of(2022, 4, 15, 12, 0), Duration.ofHours(24));
        Task taskT6 = new Task(106, "TaskT6", "Description taskT6",
                LocalDateTime.of(2022, 4, 14, 12, 0), Duration.ofHours(36));
        Task taskT7 = new Task(107, "TaskT7", "Description taskT7",
                LocalDateTime.of(2022, 4, 15, 9, 0), Duration.ofHours(12));
        Task taskT8 = new Task(108, "TaskT8", "Description taskT8",
                LocalDateTime.of(2022, 4, 14, 12, 0), Duration.ofHours(72));
        Epic epicT9 = new Epic(109, "EpicT9", "Description epicT9");

        Subtask subtaskT10 = new Subtask(110, "SubtaskT10", "Description subtaskT10", 109,
                LocalDateTime.of(2022, 4, 17, 12, 0), Duration.ofHours(24));
        Subtask subtaskT11 = new Subtask(111, "SubtaskT11", "Description subtaskT11", 109,
                LocalDateTime.of(2022, 4, 17, 9, 0), Duration.ofHours(6));
        Subtask subtaskT12 = new Subtask(112, "SubtaskT12", "Description subtaskT12", 109,
                LocalDateTime.of(2022, 4, 18, 6, 0), Duration.ofHours(12));
        Subtask subtaskT13 = new Subtask(113, "SubtaskT13", "Description subtaskT13", 109,
                LocalDateTime.of(2022, 4, 17, 15, 0), Duration.ofHours(3));
        Subtask subtaskT14 = new Subtask(114, "SubtaskT14", "Description subtaskT14", 109,
                LocalDateTime.of(2022, 4, 16, 12, 0), Duration.ofHours(72));
        createTaskViaServer(taskT5);
        createTaskViaServer(taskT6);
        createTaskViaServer(taskT7);
        createTaskViaServer(taskT8);
        createEpicViaServer(epicT9);
        createSubtaskViaServer(subtaskT10);
        createSubtaskViaServer(subtaskT11);
        createSubtaskViaServer(subtaskT12);
        createSubtaskViaServer(subtaskT13);
        createSubtaskViaServer(subtaskT14);
        assertEquals(Collections.singletonList(taskT5), getListOfTasksByTest().listTasksOfResponse);
        assertEquals(Collections.singletonList(manager.getEpicById(109)), getListOfEpicsByTest().listTasksOfResponse);
        assertEquals(Collections.singletonList(subtaskT10), getListOfSubtasksByTest().listTasksOfResponse);
        List<Task> list = List.of(taskT5, subtaskT10);
        Iterator<Task> it = manager.getPrioritizedTasks().iterator();
        assertEquals(list.size(), manager.getPrioritizedTasks().size());
        for (Task task : list) {
            assertEquals(task, it.next());
        }

    }

    //getPrioritizedTasks
    @Override
    @Test
    public void shouldCheckGetPrioritizedTasks() {

        Task taskT8 = new Task(33, "TaskT8", "Description taskT8",
                LocalDateTime.of(2022, 4, 15, 12, 0), Duration.ofHours(24));
        Epic epicT9 = new Epic(44, "EpicT9", "Description epicT9");
        Subtask subtaskT10 = new Subtask(55, "SubtaskT10", "Description subtaskT10", 44,
                LocalDateTime.of(2022, 4, 17, 11, 30), Duration.ofHours(1));
        Subtask subtaskT11 = new Subtask(56, "SubtaskT11", "Description subtaskT11", 44,
                LocalDateTime.of(2022, 4, 18, 12, 0), Duration.ofHours(12));
        Subtask subtaskT12 = new Subtask(57, "SubtaskT12", "Description subtaskT12", 44,
                LocalDateTime.of(2022, 4, 22, 12, 0), Duration.ofHours(24));
        createTaskViaServer(task1);
        createEpicViaServer(epic3);
        createSubtaskViaServer(subtask5);
        createSubtaskViaServer(subtask7);
        createTaskViaServer(taskT8);
        createEpicViaServer(epicT9);
        createSubtaskViaServer(subtaskT10);
        createSubtaskViaServer(subtaskT12);
        createSubtaskViaServer(subtaskT11);
        List<Task> list = List.of(taskT8, subtaskT10, subtaskT11, subtaskT12, subtask7, task1, subtask5);
        Iterator<Task> iter = manager.getPrioritizedTasks().iterator();
        assertEquals(list.size(), manager.getPrioritizedTasks().size());
        for (Task task : list) {    // создали задачи по времени и без - и проверяем их порядок в prioritizedTasks
            assertEquals(task, iter.next());
        }
        // обновим задачу T8 и перенесем ее так, чтобы она по времени была после Т12
        Task taskT8_1 = new Task(33, "TaskT8_1", "Description taskT8_1",
                LocalDateTime.of(2022, 4, 24, 9, 28), Duration.ofHours(9));
        String json2 = gson.toJson(taskT8_1);
        URI urlForUpdate = URI.create("http://localhost:8080/tasks/task/?id=33");
        final HttpRequest.BodyPublisher body2 = HttpRequest.BodyPublishers.ofString(json2);
        HttpRequest request2 = HttpRequest.newBuilder().uri(urlForUpdate).POST(body2).build();
        try {
            testClient.send(request2, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
//        удалим подзадачу Т10 - manager.removeSubtaskById(55);
        URI urlForRemove = URI.create("http://localhost:8080/tasks/subtask/?id=55");
        HttpRequest request7 = HttpRequest.newBuilder().uri(urlForRemove).DELETE().build();
        try {
            testClient.send(request7, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        List<Task> list2 = List.of(subtaskT11, subtaskT12, taskT8_1, subtask7, task1, subtask5);
        Iterator<Task> it2 = manager.getPrioritizedTasks().iterator();
        assertEquals(list2.size(), manager.getPrioritizedTasks().size());
        for (Task task : list2) {
            assertEquals(task, it2.next());
        }
//        manager.clearAllSubtasks();
        HttpRequest request3 = HttpRequest.newBuilder().uri(URL_SUBTASK).DELETE().build();
        try {
            testClient.send(request3, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        List<Task> list3 = List.of(taskT8_1, task1);
        Iterator<Task> it3 = manager.getPrioritizedTasks().iterator();
        assertEquals(list3.size(), manager.getPrioritizedTasks().size());
        for (Task task : list3) {
            assertEquals(task, it3.next());
        }
        assertNull(manager.getEpicById(44).getStartTime());
        assertNull(manager.getEpicById(44).getDuration());
        assertNull(manager.getEpicById(44).getEndTime());
    }

    @Override
    @Test
    public void shouldGetEmptyByGetPrioritizedTasks() {
        assertEquals(0, manager.getPrioritizedTasks().size());
    }

    @Override
    public void shouldNotFindTheFile() {
    }

    @Override
    public void shouldGiveEmptyList() {
    }

    @Override
    public void shouldGiveMapsWithHistory() {

    }

    @Override
    public void shouldGiveEmptyMapsWithEmptyHistory() {

    }

    @Override
    public void shouldReturnChanges() {

    }

    HttpResponse<String> createTaskViaServer(Task task) {
        HttpResponse<String> response1 = null;
        String json1 = gson.toJson(task);
        final HttpRequest.BodyPublisher body1 = HttpRequest.BodyPublishers.ofString(json1);
        HttpRequest request1 = HttpRequest.newBuilder().uri(URL_TASK).POST(body1).build();
        try {
            response1 = testClient.send(request1, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        return response1;
    }

    HttpResponse<String> createEpicViaServer(Epic epic) {
        HttpResponse<String> response1 = null;
        String json1 = gson.toJson(epic);
        final HttpRequest.BodyPublisher body1 = HttpRequest.BodyPublishers.ofString(json1);
        HttpRequest request1 = HttpRequest.newBuilder().uri(URL_EPIC).POST(body1).build();
        try {
            response1 = testClient.send(request1, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        return response1;
    }

    HttpResponse<String> createSubtaskViaServer(Subtask subtask) {
        HttpResponse<String> response1 = null;
        String json1 = gson.toJson(subtask);
        final HttpRequest.BodyPublisher body1 = HttpRequest.BodyPublishers.ofString(json1);
        HttpRequest request1 = HttpRequest.newBuilder().uri(URL_SUBTASK).POST(body1).build();
        try {
            response1 = testClient.send(request1, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        return response1;
    }

    private static final class TwoParametersByGetTaskByID<T extends Task> {
        T taskOfResponse;
        HttpResponse<String> response;

        private TwoParametersByGetTaskByID(T taskOfResponse, HttpResponse<String> response) {
            this.taskOfResponse = taskOfResponse;
            this.response = response;
        }
    }

    private static final class TwoParametersByGetEPICByID {
        Epic epicOfResponse;
        HttpResponse<String> response;

        private TwoParametersByGetEPICByID(Epic taskOfResponse, HttpResponse<String> response) {
            this.epicOfResponse = taskOfResponse;
            this.response = response;
        }
    }

    TwoParametersByGetTaskByID<Task> getTaskByIdForTest(int id) {
        Task taskOfResponse = null;
        URI url2 = URI.create(URL_TASK + "/?id=" + id);
        HttpResponse<String> response3 = null;
        HttpRequest request3 = HttpRequest.newBuilder().uri(url2).GET().build();
        try {
            response3 = testClient.send(request3, HttpResponse.BodyHandlers.ofString());
            JsonElement jsonElement = JsonParser.parseString(response3.body());
            if (jsonElement.isJsonObject()) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                taskOfResponse = gson.fromJson(jsonObject, Task.class);
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        return new TwoParametersByGetTaskByID<>(taskOfResponse, response3);
    }

    TwoParametersByGetEPICByID getEpicByIdForTest(int id) {
        Epic epicOfResponse = null;
        URI url = URI.create(URL_EPIC + "/?id=" + id);
        HttpResponse<String> response = null;
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        try {
            response = testClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonElement jsonElement = JsonParser.parseString(response.body());
            if (jsonElement.isJsonObject()) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                epicOfResponse = gson.fromJson(jsonObject, Epic.class);
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        return new TwoParametersByGetEPICByID(epicOfResponse, response);
    }

    TwoParametersByGetTaskByID<Subtask> getSubtaskByIdForTest(int id) {
        Subtask subtaskOfResponse = null;
        URI url = URI.create(URL_SUBTASK + "/?id=" + id);
        HttpResponse<String> response = null;
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        try {
            response = testClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonElement jsonElement = JsonParser.parseString(response.body());
            if (jsonElement.isJsonObject()) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                subtaskOfResponse = gson.fromJson(jsonObject, Subtask.class);
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        return new TwoParametersByGetTaskByID<>(subtaskOfResponse, response);
    }


    private static final class TwoParametersByGetAllTasks<T extends Task> {
        List<T> listTasksOfResponse;
        HttpResponse<String> response;

        private TwoParametersByGetAllTasks(List<T> tasksOfResponse, HttpResponse<String> response) {
            this.listTasksOfResponse = tasksOfResponse;
            this.response = response;
        }
    }

    TwoParametersByGetAllTasks<Task> getListOfTasksByTest() {
        List<Task> tasksOfResponse = null;
        HttpResponse<String> response = null;
        HttpRequest request3 = HttpRequest.newBuilder().uri(URL_TASK).GET().build();
        try {
            response = testClient.send(request3, HttpResponse.BodyHandlers.ofString());
            JsonElement jsonElement = JsonParser.parseString(response.body());
            if (jsonElement.isJsonArray()) {
                JsonArray jsonArray = jsonElement.getAsJsonArray();
                tasksOfResponse = new ArrayList<>();
                for (var el : jsonArray) {
                    Task newTask = gson.fromJson(el, Task.class);
                    tasksOfResponse.add(newTask);
                }
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        return new TwoParametersByGetAllTasks<>(tasksOfResponse, response);
    }

    TwoParametersByGetAllTasks<Epic> getListOfEpicsByTest() {
        List<Epic> epicsOfResponse = null;
        HttpResponse<String> response = null;
        HttpRequest request3 = HttpRequest.newBuilder().uri(URL_EPIC).GET().build();
        try {
            response = testClient.send(request3, HttpResponse.BodyHandlers.ofString());
            JsonElement jsonElement = JsonParser.parseString(response.body());
            if (jsonElement.isJsonArray()) {
                JsonArray jsonArray = jsonElement.getAsJsonArray();
                epicsOfResponse = new ArrayList<>();
                for (var el : jsonArray) {
                    Epic newEpic = gson.fromJson(el, Epic.class);
                    epicsOfResponse.add(newEpic);
                }
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        return new TwoParametersByGetAllTasks<>(epicsOfResponse, response);
    }

    TwoParametersByGetAllTasks<Subtask> getListOfSubtasksByTest() {
        List<Subtask> subtasksOfResponse = null;
        HttpResponse<String> response = null;
        HttpRequest request3 = HttpRequest.newBuilder().uri(URL_SUBTASK).GET().build();
        try {
            response = testClient.send(request3, HttpResponse.BodyHandlers.ofString());
            JsonElement jsonElement = JsonParser.parseString(response.body());
            if (jsonElement.isJsonArray()) {
                JsonArray jsonArray = jsonElement.getAsJsonArray();
                subtasksOfResponse = new ArrayList<>();
                for (var el : jsonArray) {
                    Subtask newSubtask = gson.fromJson(el, Subtask.class);
                    subtasksOfResponse.add(newSubtask);
                }
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        return new TwoParametersByGetAllTasks<>(subtasksOfResponse, response);
    }
}

