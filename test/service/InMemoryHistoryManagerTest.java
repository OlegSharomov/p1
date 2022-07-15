package service;

import models.Epic;
import models.Subtask;
import models.Task;
import org.junit.jupiter.api.Test;
import util.Managers;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class InMemoryHistoryManagerTest {
    private final HistoryManager hm = Managers.getDefaultHistory();
    private final Task task1 = new Task(1, "Имя задачи 1", "Описание задачи 1");
    private final Task task2 = new Task(2, "Имя задачи 2", "Описание задачи 2");
    private final Epic epic3 = new Epic(3, "Имя эпика 3", "Описание эпика 3");
    private final Epic epic4 = new Epic(4, "Имя эпика 4", "Описание эпика 4");
    private final Subtask subtask5 = new Subtask(5, "Имя подзадачи 5", "Описание подзадачи 5", 3);
    private final Subtask subtask6 = new Subtask(6, "Имя подзадачи 6", "Описание подзадачи 6", 3);
    private final Subtask subtask7 = new Subtask(7, "Имя подзадачи 7", "Описание подзадачи 7", 3);

    //add() and getHistory()
    @Test
    void shouldReturnEmptyList() {
        assertEquals(Collections.emptyList(), hm.getHistory());
    }

    @Test
    void shouldReturnTwoTasks() {
        hm.add(task1);
        hm.add(task2);
        hm.add(task1);
        assertEquals(List.of(task2, task1), hm.getHistory());
    }

    @Test
    void shouldReturn2356147() {
        hm.add(task1);
        hm.add(task1);
        hm.add(task2);
        hm.add(epic3);
        hm.add(epic4);
        hm.add(subtask5);
        hm.add(subtask6);
        hm.add(subtask7);
        hm.add(task1);
        hm.add(epic4);
        hm.add(subtask7);
        assertEquals(List.of(task2, epic3, subtask5, subtask6, task1, epic4, subtask7), hm.getHistory());
    }

    //remove
    @Test
    void shouldRemove147() {
        hm.add(task1);
        hm.add(task2);
        hm.add(epic3);
        hm.add(epic4);
        hm.add(subtask5);
        hm.add(subtask6);
        hm.add(subtask7);
        hm.remove(1);
        hm.remove(4);
        hm.remove(7);
        assertEquals(List.of(task2, epic3, subtask5, subtask6), hm.getHistory());
    }

    @Test
    void shouldRemove567() {
        hm.add(epic3);
        hm.add(subtask5);
        hm.add(subtask6);
        hm.add(subtask7);
        hm.remove(5);
        hm.remove(6);
        hm.remove(7);
        assertEquals(Collections.singletonList(epic3), hm.getHistory());
    }

    @Test
    void shouldDoesNotThrowException1() {
        assertDoesNotThrow(() -> hm.remove(5));
    }

    @Test
    void shouldDoesNotThrowException2() {
        hm.add(task1);
        assertDoesNotThrow(() -> hm.remove(2));
    }
}
