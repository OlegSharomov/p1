package service;

import models.Epic;
import models.Subtask;
import models.Task;

import java.util.ArrayList;
import java.util.TreeSet;

public interface TaskManager {

    // Создает новую задачу
    void createNewTask(Task task);

    // Создает новый епик
    void createNewEpic(Epic epic);

    // Создает новую подзадачу
    void createNewSubtask(Subtask subtask);

    // Возвращает список задач
    ArrayList<Task> getAllTasks();

    // Возвращает список епиков
    ArrayList<Epic> getAllEpics();

    // Возвращает список подзадач
    ArrayList<Subtask> getAllSubtasks();

    // Очищает хранилище всех задач
    void clearAllTasks();

    // Очищает хранилище всех епиков и подзадач
    void clearAllEpics();

    // Очищает хранилище всех подзадач и списки подзадач связанных с ним епиков
    void clearAllSubtasks();

    // Возвращает задачу по id
    Task getTaskById(int id);

    // Возвращает епик по id
    Epic getEpicById(int id);

    // Возвращает подзадачу по id
    Subtask getSubtaskById(int id);

    // Обновляет задачу в хранилище на переданную в параметре задачу
    void updateTask(Task task);

    /* Обновляет подзадачу в хранилище на переданную в параметре подзадачу и
                         обновляет статус епика и всех свяханных с ним подзадач */
    void updateSubtask(Subtask subtask);

    /* Обновляет епик в хранилище на переданный в параметре епик и
                         обновляет статус епика и всех свяханных с ним подзадач */
    void updateEpic(Epic epic);

    // Удаляет задачу из хранилища по id
    void removeTaskById(int id);

    // Удаляет подзадачу из хранилища по id и из списка подзадач связанного с ним епика
    void removeSubtaskById(int id);

    // Удаляет епик из хранилища по id и удаляет связанные с ним подзадачи из хранилища
    void removeEpicById(int id);

    // Возвращает список связанных с епиком подзадач по id епика
    ArrayList<Subtask> getSubtasksOfEpic(int idEpic);

    // Возвращает историю просмотров (объект historyInMemory)
    HistoryManager getHistoryInMemory();

    // возвращает отсортированное множество задач
    TreeSet<Task> getPrioritizedTasks();

    boolean findIntersection(Task task);
}
