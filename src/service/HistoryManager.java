package service;

import models.Task;

import java.util.List;

public interface HistoryManager {
    // Добавляет последнюю просмотренную задачу
    void add(Task task);

    // Удаляет элемент по id
    void remove(int id);

    // Возвращает List с задачами истории просмотров
    List<Task> getHistory();
}
