package service;

import models.Epic;
import models.Subtask;
import models.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import util.Managers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static models.Status.NEW;
import static models.Status.IN_PROGRESS;
import static models.Status.DONE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class InMemoryTaskManagerTest extends TaskManagerTest<TaskManager> {

    @AfterEach
    public void shouldDoAfterEach() {
        System.out.println();
    }

    @Override
    public TaskManager createManager() {
        return Managers.getDefault();
    }

    @Override
    @Test
    public void shouldReturnPathOfManager() {
        assertEquals("class service.InMemoryTaskManager", manager.getClass().toString());
    }

    //createNewTask
    @Override
    @Test
    public void shouldReturnEmptyTaskMap() {
        manager.createNewTask(null);
        assertEquals(0, manager.getAllTasks().size(), "После добавления null " +
                "в пустой список задач - он оказался не пустым");
    }

    @Override
    @Test
    public void shouldReturnTask() {
        manager.createNewTask(task1);
        assertEquals(task1, manager.getTaskById(1), "После добавления задачи " +
                "в пустой список задач - задача не найдена");
    }

    @Override
    @Test
    public void shouldNotAddSameTasks() {
        manager.createNewTask(task1);
        manager.createNewTask(task1);
        assertEquals(Collections.singletonList(task1), manager.getAllTasks());
    }

    //createNewEpic
    @Override
    @Test
    public void shouldReturnEmptyEpicMap() {
        manager.createNewEpic(null);
        assertEquals(0, manager.getAllEpics().size(), "После добавления null " +
                "в пустой список эпиков - он оказался не пустым");
    }

    @Override
    @Test
    public void shouldReturnEpic3() {
        manager.createNewEpic(epic3);
        assertEquals(epic3, manager.getEpicById(3), "После добавления эпика " +
                "в пустой список епиков - эпик не найден");
    }

    @Override
    @Test
    public void shouldNotAddSameEpics() {
        manager.createNewEpic(epic3);
        manager.createNewEpic(epic3);
        assertEquals(Collections.singletonList(epic3), manager.getAllEpics());
    }

    //createNewSubtask
    @Override
    @Test
    public void shouldReturnEmptySubtaskMap() {
        manager.createNewSubtask(null);
        assertEquals(0, manager.getAllSubtasks().size(), "После добавления null " +
                "в пустой список подзадач - он оказался не пустым");
    }

    @Override
    @Test
    public void shouldReturnEmptySubtaskMap2() {
        manager.createNewSubtask(subtask5);
        assertEquals(0, manager.getAllSubtasks().size(), "После добавления подзадачи в пустой список " +
                "подзадач (с idOfEpicNumber, которого нет с списке эпиков)  - список подзадач оказался не пустым");
    }

    @Override
    @Test
    public void shouldReturnSubtask5() {
        manager.createNewEpic(epic3);
        manager.createNewSubtask(subtask5);
        assertEquals(subtask5, manager.getSubtaskById(5), "После добавления подзадачи" +
                "в пустой список подзадач - подзадача не найдена");
    }

    @Override
    @Test
    public void shouldNotAddSameSubtasks() {
        manager.createNewEpic(epic3);
        manager.createNewSubtask(subtask5);
        manager.createNewSubtask(subtask5);
        assertEquals(Collections.singletonList(subtask5), manager.getAllSubtasks());
    }

    //checkAndChangeStatus
    @Override
    @Test
    public void shouldReturnStatusNewWithEmptyListOfSubtasks() {
        manager.createNewEpic(epic3);
        assertEquals(NEW, manager.getEpicById(3).getStatus(), "статус эпика и подзадач не соответствует");
    }

    @Override
    @Test
    public void shouldReturnStatusDONEWithAllSubtasksDONE() {
        manager.createNewEpic(epic3);
        subtask5.setStatus(DONE);
        manager.createNewSubtask(subtask5);
        subtask6.setStatus(DONE);
        manager.createNewSubtask(subtask6);
        subtask7.setStatus(DONE);
        manager.createNewSubtask(subtask7);
        assertEquals(DONE, manager.getEpicById(3).getStatus(), "статус эпика и подзадач не соответствует");
    }

    @Override
    @Test
    public void shouldReturnStatusIN_PROGRESSWithAllSubtasksDONEAndNEW() {
        manager.createNewEpic(epic3);
        subtask5.setStatus(DONE);
        manager.createNewSubtask(subtask5);
        subtask6.setStatus(DONE);
        manager.createNewSubtask(subtask6);
        subtask7.setStatus(NEW);
        manager.createNewSubtask(subtask7);
        assertEquals(IN_PROGRESS, manager.getEpicById(3).getStatus(), "статус эпика и подзадач не соответствует");
    }

    @Override
    @Test
    public void shouldReturnStatusDONEWithAllSubtasksDONEAfterRemove() {
        manager.createNewEpic(epic3);
        subtask5.setStatus(DONE);
        manager.createNewSubtask(subtask5);
        subtask6.setStatus(DONE);
        manager.createNewSubtask(subtask6);
        subtask7.setStatus(NEW);
        manager.createNewSubtask(subtask7);
        manager.removeSubtaskById(7);
        assertEquals(DONE, manager.getEpicById(3).getStatus(), "статус эпика и подзадач не соответствует");
    }

    @Override
    @Test
    public void shouldReturnStatusIN_PROGRESSWithAllSubtasksIN_PROGRESS() {
        manager.createNewEpic(epic3);
        subtask5.setStatus(IN_PROGRESS);
        manager.createNewSubtask(subtask5);
        subtask6.setStatus(IN_PROGRESS);
        manager.createNewSubtask(subtask6);
        subtask7.setStatus(IN_PROGRESS);
        manager.createNewSubtask(subtask7);
        assertEquals(IN_PROGRESS, manager.getEpicById(3).getStatus(), "статус эпика и подзадач не соответствует");
    }

    @Override
    @Test
    public void shouldReturnStatusNEWWithAllSubtasksNEW() {
        manager.createNewEpic(epic3);
        manager.createNewSubtask(subtask5);
        manager.createNewSubtask(subtask6);
        manager.createNewSubtask(subtask7);
        assertEquals(NEW, manager.getEpicById(3).getStatus(), "статус эпика и подзадач не соответствует");
    }

    //getAllTasks, Subtasks and Epics
    @Override
    @Test
    public void shouldReturnListOfTasks() {
        manager.createNewTask(task1);
        manager.createNewTask(task2);
        assertEquals(List.of(task1, task2), manager.getAllTasks(), "список не соответствует заданному или пуст");
    }

    @Override
    @Test
    public void shouldReturnListOfEpics() {
        manager.createNewEpic(epic3);
        manager.createNewEpic(epic4);
        assertEquals(List.of(epic3, epic4), manager.getAllEpics(), "список не соответствует заданному или пуст");
    }

    @Override
    @Test
    public void shouldReturnListOfSubtasks() {
        manager.createNewEpic(epic3);
        manager.createNewSubtask(subtask5);
        manager.createNewSubtask(subtask6);
        manager.createNewSubtask(subtask7);
        assertEquals(List.of(subtask5, subtask6, subtask7), manager.getAllSubtasks(),
                "список не соответствует заданному или пуст");
    }

    //clearAll
    @Override
    @Test
    public void shouldReturnEmptyListOfTask() {
        manager.createNewTask(task1);
        manager.createNewTask(task2);
        manager.getTaskById(1);
        manager.getTaskById(2);
        manager.clearAllTasks();
        assertEquals(Collections.emptyList(), manager.getAllTasks(), "список оказался не пустым");
        assertEquals(Collections.emptyList(), manager.getHistoryInMemory().getHistory());
    }

    @Override
    @Test
    public void shouldReturnEmptyListOfEpic() {
        manager.createNewEpic(epic3);
        manager.createNewEpic(epic4);
        manager.getEpicById(3);
        manager.getEpicById(4);
        manager.clearAllEpics();
        assertEquals(Collections.emptyList(), manager.getAllEpics(), "список оказался не пустым");
        assertEquals(Collections.emptyList(), manager.getHistoryInMemory().getHistory());
    }

    @Override
    @Test
    public void shouldReturnEmptyListOfSubtasks() {
        manager.createNewEpic(epic3);
        manager.createNewSubtask(subtask5);
        manager.createNewSubtask(subtask6);
        manager.createNewSubtask(subtask7);
        manager.getEpicById(3);
        manager.getSubtaskById(5);
        manager.getSubtaskById(6);
        manager.getSubtaskById(7);
        manager.clearAllSubtasks();
        assertEquals(Collections.emptyList(), manager.getAllSubtasks(), "список оказался не пустым");
        assertEquals(Collections.singletonList(epic3), manager.getHistoryInMemory().getHistory());
    }

    @Override
    @Test
    public void shouldReturnEmptyListOfSubtasksAndEmptyListOfEpic() {
        manager.createNewEpic(epic3);
        manager.createNewSubtask(subtask5);
        manager.createNewSubtask(subtask6);
        manager.createNewSubtask(subtask7);
        manager.clearAllEpics();
        assertEquals(0, manager.getAllSubtasks().size(), "список оказался не пустым");
        assertEquals(0, manager.getAllEpics().size(), "список оказался не пустым");
    }

    //getById
    @Override
    @Test
    public void shouldReturnNullByGetTask() {
        assertNull(manager.getTaskById(2));
        assertEquals(Collections.emptyList(), manager.getHistoryInMemory().getHistory());
    }

    @Override
    @Test
    public void shouldReturnTask1ByGetTask() {
        manager.createNewTask(task1);
        assertEquals(task1, manager.getTaskById(1));
        assertEquals(Collections.singletonList(task1), manager.getHistoryInMemory().getHistory());
    }

    @Override
    @Test
    public void shouldReturnNullByGetEpic() {
        assertNull(manager.getEpicById(4));
        assertEquals(Collections.emptyList(), manager.getHistoryInMemory().getHistory());
    }

    @Override
    @Test
    public void shouldReturnEpic3ByGetEpic() {
        manager.createNewEpic(epic3);
        assertEquals(epic3, manager.getEpicById(3));
        assertEquals(Collections.singletonList(epic3), manager.getHistoryInMemory().getHistory());
    }

    @Override
    @Test
    public void shouldReturnNullByGetSubtask() {
        assertNull(manager.getSubtaskById(7));
        assertEquals(Collections.emptyList(), manager.getHistoryInMemory().getHistory());
    }

    @Override
    @Test
    public void shouldReturnSubtask5ByGetSubtask() {
        manager.createNewEpic(epic3);
        manager.createNewSubtask(subtask5);
        assertEquals(subtask5, manager.getSubtaskById(5));
        assertEquals(Collections.singletonList(subtask5), manager.getHistoryInMemory().getHistory());

    }

    //update
    @Override
    @Test
    public void shouldReturnTask1_1() {
        manager.createNewTask(task1);
        manager.getTaskById(1);
        Task task1_1 = new Task(1, "Имя задачи 1_1", "Описание задачи 1_1");
        manager.updateTask(task1_1);
        assertEquals(task1_1, manager.getTaskById(1));
        assertEquals(Collections.singletonList(task1), manager.getHistoryInMemory().getHistory());
    }

    @Override
    @Test
    public void shouldReturnNullByNullUpdateTask() {
        manager.updateTask(null);
        assertNull(manager.getTaskById(1));
    }

    @Override
    @Test
    public void shouldReturnNullByUpdateTaskWithEmptyMap() {
        Task task1_1 = new Task(1, "Имя задачи 1_1", "Описание задачи 1_1");
        manager.updateTask(task1_1);
        assertNull(manager.getTaskById(1));
    }

    @Override
    @Test
    public void shouldReturnEpic3_1() {
        manager.createNewEpic(epic3);
        manager.getEpicById(3);
        Epic epic3_1 = new Epic(3, "Имя эпика 3_1", "Описание эпика 3_1");
        manager.updateEpic(epic3_1);
        assertEquals(epic3_1, manager.getEpicById(3));
        assertEquals(Collections.singletonList(epic3), manager.getHistoryInMemory().getHistory());
    }

    @Override
    @Test
    public void shouldReturnNullByNullUpdateEpic() {
        manager.updateEpic(null);
        assertNull(manager.getEpicById(3));
    }

    @Override
    @Test
    public void shouldReturnNullByUpdateEpicWithEmptyMap() {
        Epic epic3_1 = new Epic(3, "Имя эпика 3_1", "Описание эпика 3_1");
        manager.updateEpic(epic3_1);
        assertNull(manager.getEpicById(3));
    }

    @Override
    @Test
    public void shouldReturnEpicWithNumbersOfSubtasksByUpdate() {
        manager.createNewEpic(epic3);
        manager.createNewSubtask(subtask5);
        subtask5.setStatus(DONE);
        manager.createNewSubtask(subtask6);
        manager.createNewSubtask(subtask7);
        Epic epic3_1 = new Epic(3, "Имя эпика 3_1", "Описание эпика 3_1");
        manager.updateEpic(epic3_1);
        assertEquals(epic3_1, manager.getEpicById(3));
        assertEquals(IN_PROGRESS, manager.getEpicById(3).getStatus());
        assertEquals(List.of(5, 6, 7), manager.getEpicById(3).getSubtasks());
    }

    @Override
    @Test
    public void shouldReturnSubtask5_1() {
        manager.createNewEpic(epic3);
        manager.createNewSubtask(subtask5);
        manager.getSubtaskById(5);
        Subtask subtask5_1 = new Subtask(5, "Имя подзадачи 5_1", "Описание подзадачи 5_1", 3);
        manager.updateSubtask(subtask5_1);
        assertEquals(subtask5_1, manager.getSubtaskById(5));
        assertEquals(Collections.singletonList(subtask5), manager.getHistoryInMemory().getHistory());
    }

    @Override
    @Test
    public void shouldReturnNullByNullUpdateSubtask() {
        manager.createNewEpic(epic3);
        manager.updateSubtask(null);
        assertNull(manager.getSubtaskById(5));
    }

    @Override
    @Test
    public void shouldReturnNullByUpdateSubtaskWithEmptyMap() {
        manager.createNewEpic(epic3);
        Subtask subtask5_1 = new Subtask(5, "Имя подзадачи 5_1", "Описание подзадачи 5_1", 3);
        manager.updateSubtask(subtask5_1);
        assertNull(manager.getSubtaskById(5));
    }

    @Override
    @Test
    public void shouldAssignOldIDOfEpicByUpdate() {
        manager.createNewEpic(epic3);
        manager.createNewSubtask(subtask5);
        Subtask subtask5_1 = new Subtask(5, "Имя подзадачи 5_1", "Описание подзадачи 5_1", 4);
        manager.updateSubtask(subtask5_1);
        assertEquals(new Subtask(5, "Имя подзадачи 5_1", "Описание подзадачи 5_1", 3),
                manager.getSubtaskById(5));
    }

    //removeById
    @Override
    @Test
    public void shouldRemoveTaskAndGetCorrectHistory() {
        manager.createNewTask(task1);
        manager.createNewTask(task2);
        manager.getTaskById(1);
        manager.getTaskById(2);
        manager.removeTaskById(2);
        assertEquals(1, manager.getAllTasks().size());
        assertEquals(Collections.singletonList(task1), manager.getHistoryInMemory().getHistory());
    }

    @Override
    @Test
    public void shouldNotChangeTaskMap() {
        manager.createNewTask(task1);
        manager.createNewTask(task2);
        manager.getTaskById(1);
        manager.getTaskById(2);
        manager.removeTaskById(99);
        assertEquals(List.of(task1, task2), manager.getAllTasks());
        assertEquals(List.of(task1, task2), manager.getHistoryInMemory().getHistory());
    }

    @Override
    @Test
    public void shouldRemoveEpicAndGetCorrectHistory() {
        manager.createNewEpic(epic3);
        manager.createNewEpic(epic4);
        manager.getEpicById(3);
        manager.getEpicById(4);
        manager.removeEpicById(4);
        assertEquals(1, manager.getAllEpics().size());
        assertEquals(Collections.singletonList(epic3), manager.getHistoryInMemory().getHistory());
    }

    @Override
    @Test
    public void shouldNotChangeEpicMap() {
        manager.createNewEpic(epic3);
        manager.createNewEpic(epic4);
        manager.getEpicById(3);
        manager.getEpicById(4);
        manager.removeEpicById(99);
        assertEquals(List.of(epic3, epic4), manager.getAllEpics());
        assertEquals(List.of(epic3, epic4), manager.getHistoryInMemory().getHistory());
    }

    @Override
    @Test
    public void shouldRemoveSubtaskEverywhereAndGetCorrectHistory() {
        manager.createNewEpic(epic3);
        manager.createNewSubtask(subtask5);
        subtask6.setStatus(IN_PROGRESS);
        manager.createNewSubtask(subtask6);
        manager.createNewSubtask(subtask7);
        manager.getSubtaskById(5);
        manager.getSubtaskById(6);
        manager.getSubtaskById(7);
        manager.removeSubtaskById(6);
        assertEquals(2, manager.getAllSubtasks().size());
        assertEquals(List.of(subtask5, subtask7), manager.getHistoryInMemory().getHistory());
        assertEquals(NEW, manager.getEpicById(3).getStatus());
        assertEquals(List.of(5, 7), manager.getEpicById(3).getSubtasks());
    }

    @Override
    @Test
    public void shouldNotChangeSubtaskMapAndHistory() {
        manager.createNewEpic(epic3);
        manager.createNewSubtask(subtask5);
        manager.createNewSubtask(subtask6);
        manager.createNewSubtask(subtask7);
        manager.getSubtaskById(7);
        manager.getSubtaskById(5);
        manager.getSubtaskById(6);
        manager.removeSubtaskById(99);
        assertEquals(List.of(subtask5, subtask6, subtask7), manager.getAllSubtasks());
        assertEquals(List.of(subtask7, subtask5, subtask6), manager.getHistoryInMemory().getHistory());
    }

    @Override
    @Test
    public void shouldGetSubtasksOfEpic() {
        manager.createNewEpic(epic3);
        manager.createNewSubtask(subtask5);
        manager.createNewSubtask(subtask6);
        manager.createNewSubtask(subtask7);
        assertEquals(List.of(5, 6, 7), manager.getEpicById(3).getSubtasks());
    }

    @Override
    @Test
    public void shouldGetEmptySubtasksOfEpic() {
        manager.createNewEpic(epic4);
        manager.createNewSubtask(subtask5);
        manager.createNewSubtask(null);
        assertEquals(Collections.emptyList(), manager.getEpicById(4).getSubtasks());
    }

    @Override
    @Test
    public void shouldReturnHistoryInMemory() {
        manager.createNewTask(task1);
        manager.createNewTask(task2);
        manager.createNewEpic(epic3);
        manager.createNewEpic(epic4);
        manager.createNewSubtask(subtask5);
        manager.createNewSubtask(subtask6);
        manager.createNewSubtask(subtask7);
        manager.getSubtaskById(7);
        manager.getTaskById(2);
        manager.getEpicById(4);
        manager.getSubtaskById(6);
        manager.getTaskById(1);
        manager.getEpicById(3);
        Task task2_2 = new Task(2, "Имя задачи 2_2", "Описание задачи 2_2");
        manager.updateTask(task2_2);
        manager.getSubtaskById(5);
        manager.getSubtaskById(7);
        assertEquals(List.of(task1, task2_2), manager.getAllTasks());
        assertEquals(List.of(task2, epic4, subtask6, task1, epic3, subtask5, subtask7),
                manager.getHistoryInMemory().getHistory());
    }

    @Override
    @Test
    public void shouldReturnEmptyList() {
        manager.getTaskById(1);
        assertEquals(Collections.emptyList(), manager.getHistoryInMemory().getHistory());
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
        manager.createNewEpic(epicT9);
        manager.createNewSubtask(subtaskT10);
        manager.createNewSubtask(subtaskT11);
        manager.createNewSubtask(subtaskT12);
        manager.createNewSubtask(subtaskT13);
        assertEquals("17.04.2022 12:00", epicT9.getStartTimeToString());
        assertEquals("19.04.2022 18:00", epicT9.getEndTimeToString());
        manager.createNewSubtask(subtaskT14);
        manager.removeSubtaskById(110);
        assertEquals("18.04.2022 12:00", epicT9.getStartTimeToString());
        assertEquals("19.04.2022 18:00", epicT9.getEndTimeToString());
        Subtask subtaskT13_1 = new Subtask(113, "SubtaskT13_1", "Description subtaskT13_1", 109,
                LocalDateTime.of(2022, 4, 19, 18, 0), Duration.ofHours(4));
        manager.updateSubtask(subtaskT13_1);
        assertEquals("18.04.2022 12:00", epicT9.getStartTimeToString());
        assertEquals("19.04.2022 22:00", epicT9.getEndTimeToString());
        manager.clearAllSubtasks();
        assertEquals("", epicT9.getStartTimeToString());
        assertEquals("", epicT9.getEndTimeToString());
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
        manager.createNewTask(taskT5);
        manager.createNewTask(taskT6);
        manager.createNewTask(taskT7);
        manager.createNewTask(taskT8);
        manager.createNewEpic(epicT9);
        manager.createNewSubtask(subtaskT10);
        manager.createNewSubtask(subtaskT11);
        manager.createNewSubtask(subtaskT12);
        manager.createNewSubtask(subtaskT13);
        manager.createNewSubtask(subtaskT14);
        assertEquals(Collections.singletonList(taskT5), manager.getAllTasks());
        assertEquals(Collections.singletonList(epicT9), manager.getAllEpics());
        assertEquals(Collections.singletonList(subtaskT10), manager.getAllSubtasks());
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
        manager.createNewTask(task1);
        manager.createNewEpic(epic3);
        manager.createNewSubtask(subtask5);
        manager.createNewSubtask(subtask7);
        manager.createNewTask(taskT8);
        manager.createNewEpic(epicT9);
        manager.createNewSubtask(subtaskT10);
        manager.createNewSubtask(subtaskT12);
        manager.createNewSubtask(subtaskT11);
        List<Task> list = List.of(taskT8, subtaskT10, subtaskT11, subtaskT12, subtask7, task1, subtask5);
        Iterator<Task> iter = manager.getPrioritizedTasks().iterator();
        assertEquals(list.size(), manager.getPrioritizedTasks().size());
        for (Task task : list) {    // создали задачи по времени и без - и проверяем их порядок в prioritizedTasks
            assertEquals(task, iter.next());
        }
        // обновим задачу T8 и перенесем ее так, чтобы она по времени была после Т12
        Task taskT8_1 = new Task(33, "TaskT8_1", "Description taskT8_1",
                LocalDateTime.of(2022, 4, 24, 9, 28), Duration.ofHours(9));
        manager.updateTask(taskT8_1);
        // удалили подзадачу Т10
        manager.removeSubtaskById(55);
        List<Task> list2 = List.of(subtaskT11, subtaskT12, taskT8_1, subtask7, task1, subtask5);
        Iterator<Task> it2 = manager.getPrioritizedTasks().iterator();
        assertEquals(list2.size(), manager.getPrioritizedTasks().size());
        for (Task task : list2) {
            assertEquals(task, it2.next());
        }
        manager.clearAllSubtasks();
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
}
