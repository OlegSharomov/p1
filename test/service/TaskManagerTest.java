package service;

import models.Epic;
import models.Subtask;
import models.Task;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

public abstract class TaskManagerTest<T extends TaskManager> {
    T manager;
    Task task1;
    Task task2;
    Epic epic3;
    Epic epic4;
    Subtask subtask5;
    Subtask subtask6;
    Subtask subtask7;
    Task wrongTask;
    Epic wrongEpic;
    Subtask wrongSubtask;

    TaskManagerTest() {
        this.manager = createManager();
        System.out.println("Сработал конструктор - создался новый объект " + manager.getClass().getSimpleName());
        task1 = new Task(1, "Имя задачи 1", "Описание задачи 1");
        task2 = new Task(2, "Имя задачи 2", "Описание задачи 2",
                LocalDateTime.of(2022, 2, 2, 2, 0), Duration.ofHours(2));
        epic3 = new Epic(3, "Имя эпика 3", "Описание эпика 3");
        epic4 = new Epic(4, "Имя эпика 4", "Описание эпика 4");
        subtask5 = new Subtask(5, "Имя подзадачи 5", "Описание подзадачи 5", 3);
        subtask6 = new Subtask(6, "Имя подзадачи 6", "Описание подзадачи 6", 3,
                LocalDateTime.of(2022, 6, 6, 6, 0), Duration.ofHours(6));
        subtask7 = new Subtask(7, "Имя подзадачи 7", "Описание подзадачи 7", 3,
                LocalDateTime.of(2022, 7, 7, 7, 0), Duration.ofHours(7));
        System.out.println("Создали описания задач");
        wrongTask = new Task(1, "  ", "  ");
        wrongEpic = new Epic(3, "  ", "  ");
        wrongSubtask = new Subtask(5, "  ", "  ", 3);
    }

    public abstract T createManager();

    @Test
    public abstract void shouldReturnPathOfManager();

    //createNewTask
    @Test
    public abstract void shouldReturnEmptyTaskMap();

    @Test
    public abstract void shouldReturnTask();

    @Test
    public abstract void shouldNotAddSameTasks();

    //createNewEpic
    @Test
    public abstract void shouldReturnEmptyEpicMap();

    @Test
    public abstract void shouldReturnEpic3();

    @Test
    public abstract void shouldNotAddSameEpics();

    //createNewSubtask
    @Test
    public abstract void shouldReturnEmptySubtaskMap();

    @Test
    public abstract void shouldReturnEmptySubtaskMap2();

    @Test
    public abstract void shouldReturnSubtask5();

    @Test
    public abstract void shouldNotAddSameSubtasks();

    //checkAndChangeStatus
    @Test
    public abstract void shouldReturnStatusNewWithEmptyListOfSubtasks();

    @Test
    public abstract void shouldReturnStatusDONEWithAllSubtasksDONE();

    @Test
    public abstract void shouldReturnStatusIN_PROGRESSWithAllSubtasksDONEAndNEW();

    @Test
    public abstract void shouldReturnStatusDONEWithAllSubtasksDONEAfterRemove();

    @Test
    public abstract void shouldReturnStatusIN_PROGRESSWithAllSubtasksIN_PROGRESS();

    @Test
    public abstract void shouldReturnStatusNEWWithAllSubtasksNEW();

    //getAll
    @Test
    public abstract void shouldReturnListOfTasks();

    @Test
    public abstract void shouldReturnListOfEpics();

    @Test
    public abstract void shouldReturnListOfSubtasks();

    //clearAll
    @Test
    public abstract void shouldReturnEmptyListOfTask();

    @Test
    public abstract void shouldReturnEmptyListOfEpic();

    @Test
    public abstract void shouldReturnEmptyListOfSubtasks();

    @Test
    public abstract void shouldReturnEmptyListOfSubtasksAndEmptyListOfEpic();

    //getById
    @Test
    public abstract void shouldReturnNullByGetTask();

    @Test
    public abstract void shouldReturnTask1ByGetTask();

    @Test
    public abstract void shouldReturnNullByGetEpic();

    @Test
    public abstract void shouldReturnEpic3ByGetEpic();

    @Test
    public abstract void shouldReturnNullByGetSubtask();

    @Test
    public abstract void shouldReturnSubtask5ByGetSubtask();

    //update
    @Test
    public abstract void shouldReturnTask1_1();

    @Test
    public abstract void shouldReturnNullByNullUpdateTask();

    @Test
    public abstract void shouldReturnNullByUpdateTaskWithEmptyMap();

    @Test
    public abstract void shouldReturnEpic3_1();

    @Test
    public abstract void shouldReturnNullByNullUpdateEpic();

    @Test
    public abstract void shouldReturnNullByUpdateEpicWithEmptyMap();

    @Test
    public abstract void shouldReturnEpicWithNumbersOfSubtasksByUpdate();

    @Test
    public abstract void shouldReturnSubtask5_1();

    @Test
    public abstract void shouldReturnNullByNullUpdateSubtask();

    @Test
    public abstract void shouldReturnNullByUpdateSubtaskWithEmptyMap();

    @Test
    public abstract void shouldAssignOldIDOfEpicByUpdate();

    //removeById
    @Test
    public abstract void shouldRemoveTaskAndGetCorrectHistory();

    @Test
    public abstract void shouldNotChangeTaskMap();

    @Test
    public abstract void shouldRemoveEpicAndGetCorrectHistory();

    @Test
    public abstract void shouldNotChangeEpicMap();

    @Test
    public abstract void shouldRemoveSubtaskEverywhereAndGetCorrectHistory();

    @Test
    public abstract void shouldNotChangeSubtaskMapAndHistory();

    @Test
    public abstract void shouldGetSubtasksOfEpic();

    @Test
    public abstract void shouldGetEmptySubtasksOfEpic();

    @Test
    public abstract void shouldReturnHistoryInMemory();

    @Test
    public abstract void shouldReturnEmptyList();

    //EpicTime
    @Test
    public abstract void shouldReturnCorrectTimeOfEpic();

    @Test
    public abstract void shouldNotAddTaskInTimeAnotherTask();

    //getPrioritizedTasks
    @Test
    public abstract void shouldCheckGetPrioritizedTasks();

    @Test
    public abstract void shouldGetEmptyByGetPrioritizedTasks();
}
