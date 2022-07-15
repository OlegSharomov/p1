package service;

import com.google.gson.Gson;
import http.KVTaskClient;

import java.net.URI;

/* HTTPTaskManager - класс, в котором производится вся логика работы с задачами
 и взаимодействие с KVServer через KVTaskClient для сохранения данных*/
public class HTTPTaskManager extends FileBackedTasksManager {
    private final KVTaskClient kvTaskClient;
    private final Gson gson = new Gson();

    public HTTPTaskManager(URI uri) {
        kvTaskClient = new KVTaskClient(uri);
    }

    @Override
    void save() {
        String jsonTasks = gson.toJson(getAllTasks());
        String jsonEpics = gson.toJson(getAllEpics());
        String jsonSubtasks = gson.toJson(getAllSubtasks());
        String jsonHistory = gson.toJson(getHistoryInMemory().getHistory());
        kvTaskClient.put("tasks", jsonTasks);
        kvTaskClient.put("epics", jsonEpics);
        kvTaskClient.put("subtasks", jsonSubtasks);
        kvTaskClient.put("history", jsonHistory);
    }
}
