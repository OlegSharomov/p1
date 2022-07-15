package service;

import models.Epic;
import models.Subtask;
import models.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static service.InMemoryTaskManager.getNextId;

public class FileBackedTasksManagerTest extends InMemoryTaskManagerTest {
    private Path path;

    @Override
    @AfterEach
    public void shouldDoAfterEach() {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            System.out.println("Файл не был удален, т.к. он не существует");
        }
        System.out.println("Задача проверена");
        System.out.println();
    }

    @Override
    public TaskManager createManager() {
        path = Paths.get("HistoryOfTaskTest.csv");
        return FileBackedTasksManager.loadFromFile(new File("HistoryOfTaskTest.csv"));
    }

    @Override
    @Test
    public void shouldReturnPathOfManager() {
        assertEquals("class service.FileBackedTasksManager", manager.getClass().toString());
    }

    @Override
    @Test
    public void shouldReturnEmptyTaskMap() {
        super.shouldReturnEmptyTaskMap();
        assertEquals("", getListTasksFromFile().get(1));
    }

    @Override
    @Test
    public void shouldReturnTask() {
        super.shouldReturnTask();
        assertEquals("1,TASK,Имя задачи 1,NEW,Описание задачи 1,", getListTasksFromFile().get(1));
        assertEquals("1", getListTasksFromFile().get(3));
    }

    @Override
    @Test
    public void shouldNotAddSameTasks() {
        super.shouldNotAddSameTasks();
        assertEquals("1,TASK,Имя задачи 1,NEW,Описание задачи 1,", getListTasksFromFile().get(1));
        assertEquals("", getListTasksFromFile().get(2));
    }

    @Override
    @Test
    public void shouldReturnEmptyEpicMap() {
        super.shouldReturnEmptyEpicMap();
        assertEquals("", getListTasksFromFile().get(1));
    }

    @Override
    @Test
    public void shouldReturnEpic3() {
        super.shouldReturnEpic3();
        assertEquals("3,EPIC,Имя эпика 3,NEW,Описание эпика 3,", getListTasksFromFile().get(1));
        assertEquals("", getListTasksFromFile().get(2));
        assertEquals("3", getListTasksFromFile().get(3));
    }

    @Override
    @Test
    public void shouldNotAddSameEpics() {
        super.shouldNotAddSameEpics();
        assertEquals("3,EPIC,Имя эпика 3,NEW,Описание эпика 3,", getListTasksFromFile().get(1));
        assertEquals("", getListTasksFromFile().get(2));
        assertEquals("", getListTasksFromFile().get(3));
        assertThrows(IndexOutOfBoundsException.class, () -> getListTasksFromFile().get(4));
    }

    @Override
    @Test
    public void shouldReturnEmptySubtaskMap() {
        super.shouldReturnEmptySubtaskMap();
        assertEquals("id,type,name,status,description,epic,startTime,duration,endTime", getListTasksFromFile().get(0));
        assertEquals("", getListTasksFromFile().get(1));
        assertEquals("", getListTasksFromFile().get(2));
        assertThrows(IndexOutOfBoundsException.class, () -> getListTasksFromFile().get(3));
    }

    @Override
    @Test
    public void shouldReturnEmptySubtaskMap2() {
        super.shouldReturnEmptySubtaskMap2();
        assertEquals("", getListTasksFromFile().get(1));
        assertEquals("", getListTasksFromFile().get(2));
        assertThrows(IndexOutOfBoundsException.class, () -> getListTasksFromFile().get(3));
    }

    @Override
    @Test
    public void shouldReturnSubtask5() {
        super.shouldReturnSubtask5();
        assertEquals("3,EPIC,Имя эпика 3,NEW,Описание эпика 3,", getListTasksFromFile().get(1));
        assertEquals("5,SUBTASK,Имя подзадачи 5,NEW,Описание подзадачи 5,3", getListTasksFromFile().get(2));
        assertEquals("5", getListTasksFromFile().get(4));
    }

    @Override
    @Test
    public void shouldNotAddSameSubtasks() {
        super.shouldNotAddSameSubtasks();
        assertEquals("3,EPIC,Имя эпика 3,NEW,Описание эпика 3,", getListTasksFromFile().get(1));
        assertEquals("5,SUBTASK,Имя подзадачи 5,NEW,Описание подзадачи 5,3", getListTasksFromFile().get(2));

    }

    @Override
    @Test
    public void shouldReturnStatusNewWithEmptyListOfSubtasks() {
        super.shouldReturnStatusNewWithEmptyListOfSubtasks();
        assertEquals("3,EPIC,Имя эпика 3,NEW,Описание эпика 3,", getListTasksFromFile().get(1));
        assertEquals("3", getListTasksFromFile().get(3));
    }

    @Override
    @Test
    public void shouldReturnStatusDONEWithAllSubtasksDONE() {
        super.shouldReturnStatusDONEWithAllSubtasksDONE();
        assertEquals("3,EPIC,Имя эпика 3,DONE,Описание эпика 3,,06.06.2022 06:00,752,07.07.2022 14:00", getListTasksFromFile().get(1));
        assertEquals("5,SUBTASK,Имя подзадачи 5,DONE,Описание подзадачи 5,3", getListTasksFromFile().get(2));
        assertEquals("6,SUBTASK,Имя подзадачи 6,DONE,Описание подзадачи 6,3,06.06.2022 06:00,6,06.06.2022 12:00", getListTasksFromFile().get(3));
        assertEquals("7,SUBTASK,Имя подзадачи 7,DONE,Описание подзадачи 7,3,07.07.2022 07:00,7,07.07.2022 14:00", getListTasksFromFile().get(4));
        assertEquals("3", getListTasksFromFile().get(6));
        assertThrows(IndexOutOfBoundsException.class, () -> getListTasksFromFile().get(7));
    }

    @Override
    @Test
    public void shouldReturnStatusIN_PROGRESSWithAllSubtasksDONEAndNEW() {
        super.shouldReturnStatusIN_PROGRESSWithAllSubtasksDONEAndNEW();
        assertEquals("3,EPIC,Имя эпика 3,IN_PROGRESS,Описание эпика 3,,06.06.2022 06:00,752,07.07.2022 14:00", getListTasksFromFile().get(1));
        assertEquals("5,SUBTASK,Имя подзадачи 5,DONE,Описание подзадачи 5,3", getListTasksFromFile().get(2));
        assertEquals("6,SUBTASK,Имя подзадачи 6,DONE,Описание подзадачи 6,3,06.06.2022 06:00,6,06.06.2022 12:00", getListTasksFromFile().get(3));
        assertEquals("7,SUBTASK,Имя подзадачи 7,NEW,Описание подзадачи 7,3,07.07.2022 07:00,7,07.07.2022 14:00", getListTasksFromFile().get(4));
        assertEquals("3", getListTasksFromFile().get(6));
    }

    @Override
    @Test
    public void shouldReturnStatusDONEWithAllSubtasksDONEAfterRemove() {
        super.shouldReturnStatusDONEWithAllSubtasksDONEAfterRemove();
        assertEquals("id,type,name,status,description,epic,startTime,duration,endTime", getListTasksFromFile().get(0));
        assertEquals("3,EPIC,Имя эпика 3,DONE,Описание эпика 3,,06.06.2022 06:00,6,06.06.2022 12:00", getListTasksFromFile().get(1));
        assertEquals("5,SUBTASK,Имя подзадачи 5,DONE,Описание подзадачи 5,3", getListTasksFromFile().get(2));
        assertEquals("6,SUBTASK,Имя подзадачи 6,DONE,Описание подзадачи 6,3,06.06.2022 06:00,6,06.06.2022 12:00", getListTasksFromFile().get(3));
        assertEquals("3", getListTasksFromFile().get(5));
    }

    @Override
    @Test
    public void shouldReturnStatusIN_PROGRESSWithAllSubtasksIN_PROGRESS() {
        super.shouldReturnStatusIN_PROGRESSWithAllSubtasksIN_PROGRESS();
        assertEquals("3,EPIC,Имя эпика 3,IN_PROGRESS,Описание эпика 3,,06.06.2022 06:00,752,07.07.2022 14:00", getListTasksFromFile().get(1));
        assertEquals("5,SUBTASK,Имя подзадачи 5,IN_PROGRESS,Описание подзадачи 5,3", getListTasksFromFile().get(2));
        assertEquals("6,SUBTASK,Имя подзадачи 6,IN_PROGRESS,Описание подзадачи 6,3,06.06.2022 06:00,6,06.06.2022 12:00", getListTasksFromFile().get(3));
        assertEquals("7,SUBTASK,Имя подзадачи 7,IN_PROGRESS,Описание подзадачи 7,3,07.07.2022 07:00,7,07.07.2022 14:00", getListTasksFromFile().get(4));
        assertEquals("3", getListTasksFromFile().get(6));
    }

    @Override
    @Test
    public void shouldReturnStatusNEWWithAllSubtasksNEW() {
        super.shouldReturnStatusNEWWithAllSubtasksNEW();
        assertEquals("3,EPIC,Имя эпика 3,NEW,Описание эпика 3,,06.06.2022 06:00,752,07.07.2022 14:00", getListTasksFromFile().get(1));
        assertEquals("5,SUBTASK,Имя подзадачи 5,NEW,Описание подзадачи 5,3", getListTasksFromFile().get(2));
        assertEquals("6,SUBTASK,Имя подзадачи 6,NEW,Описание подзадачи 6,3,06.06.2022 06:00,6,06.06.2022 12:00", getListTasksFromFile().get(3));
        assertEquals("7,SUBTASK,Имя подзадачи 7,NEW,Описание подзадачи 7,3,07.07.2022 07:00,7,07.07.2022 14:00", getListTasksFromFile().get(4));
        assertEquals("3", getListTasksFromFile().get(6));
    }

    @Override
    @Test
    public void shouldReturnListOfTasks() {
        super.shouldReturnListOfTasks();
        assertEquals("1,TASK,Имя задачи 1,NEW,Описание задачи 1,", getListTasksFromFile().get(1));
        assertEquals("2,TASK,Имя задачи 2,NEW,Описание задачи 2,,02.02.2022 02:00,2,02.02.2022 04:00", getListTasksFromFile().get(2));
        assertThrows(IndexOutOfBoundsException.class, () -> getListTasksFromFile().get(5));
    }

    @Override
    @Test
    public void shouldReturnListOfEpics() {
        super.shouldReturnListOfEpics();
        assertEquals("3,EPIC,Имя эпика 3,NEW,Описание эпика 3,", getListTasksFromFile().get(1));
        assertEquals("4,EPIC,Имя эпика 4,NEW,Описание эпика 4,", getListTasksFromFile().get(2));
    }

    @Override
    @Test
    public void shouldReturnListOfSubtasks() {
        super.shouldReturnListOfSubtasks();
        assertEquals("3,EPIC,Имя эпика 3,NEW,Описание эпика 3,,06.06.2022 06:00,752,07.07.2022 14:00",
                getListTasksFromFile().get(1));
        assertEquals("5,SUBTASK,Имя подзадачи 5,NEW,Описание подзадачи 5,3", getListTasksFromFile().get(2));
        assertEquals("6,SUBTASK,Имя подзадачи 6,NEW,Описание подзадачи 6,3,06.06.2022 06:00,6,06.06.2022 12:00",
                getListTasksFromFile().get(3));
        assertEquals("7,SUBTASK,Имя подзадачи 7,NEW,Описание подзадачи 7,3,07.07.2022 07:00,7,07.07.2022 14:00",
                getListTasksFromFile().get(4));
    }

    @Override
    @Test
    public void shouldReturnEmptyListOfTask() {
        super.shouldReturnEmptyListOfTask();
        assertEquals("", getListTasksFromFile().get(1));
        assertThrows(IndexOutOfBoundsException.class, () -> getListTasksFromFile().get(3));
    }

    @Override
    @Test
    public void shouldReturnEmptyListOfEpic() {
        super.shouldReturnEmptyListOfEpic();
        assertEquals("", getListTasksFromFile().get(1));
        assertThrows(IndexOutOfBoundsException.class, () -> getListTasksFromFile().get(3));
    }

    @Override
    @Test
    public void shouldReturnEmptyListOfSubtasks() {
        super.shouldReturnEmptyListOfSubtasks();
        assertEquals("3,EPIC,Имя эпика 3,NEW,Описание эпика 3,", getListTasksFromFile().get(1));
        assertEquals("", getListTasksFromFile().get(2));
        assertEquals("3", getListTasksFromFile().get(3));
        assertThrows(IndexOutOfBoundsException.class, () -> getListTasksFromFile().get(5));
    }

    @Override
    @Test
    public void shouldReturnEmptyListOfSubtasksAndEmptyListOfEpic() {
        super.shouldReturnEmptyListOfSubtasksAndEmptyListOfEpic();
        assertEquals("", getListTasksFromFile().get(1));
        assertEquals("", getListTasksFromFile().get(2));
        assertThrows(IndexOutOfBoundsException.class, () -> getListTasksFromFile().get(3));
    }

    @Override
    @Test
    public void shouldReturnNullByGetTask() {
        super.shouldReturnNullByGetTask();
        assertEquals("", getListTasksFromFile().get(1));
        assertEquals("", getListTasksFromFile().get(2));
        assertThrows(IndexOutOfBoundsException.class, () -> getListTasksFromFile().get(3));
    }

    @Override
    @Test
    public void shouldReturnTask1ByGetTask() {
        super.shouldReturnTask1ByGetTask();
        assertEquals("1,TASK,Имя задачи 1,NEW,Описание задачи 1,", getListTasksFromFile().get(1));
        assertEquals("", getListTasksFromFile().get(2));
        assertEquals("1", getListTasksFromFile().get(3));
        assertThrows(IndexOutOfBoundsException.class, () -> getListTasksFromFile().get(4));
    }

    @Override
    @Test
    public void shouldReturnNullByGetEpic() {
        super.shouldReturnNullByGetEpic();
        assertEquals("", getListTasksFromFile().get(1));
        assertEquals("", getListTasksFromFile().get(2));
        assertThrows(IndexOutOfBoundsException.class, () -> getListTasksFromFile().get(3));
    }

    @Override
    @Test
    public void shouldReturnEpic3ByGetEpic() {
        super.shouldReturnEpic3ByGetEpic();
        assertEquals("3,EPIC,Имя эпика 3,NEW,Описание эпика 3,", getListTasksFromFile().get(1));
        assertEquals("", getListTasksFromFile().get(2));
        assertEquals("3", getListTasksFromFile().get(3));
        assertThrows(IndexOutOfBoundsException.class, () -> getListTasksFromFile().get(4));
    }

    @Override
    @Test
    public void shouldReturnNullByGetSubtask() {
        super.shouldReturnNullByGetSubtask();
        assertEquals("", getListTasksFromFile().get(1));
        assertEquals("", getListTasksFromFile().get(2));
        assertThrows(IndexOutOfBoundsException.class, () -> getListTasksFromFile().get(3));
    }

    @Override
    @Test
    public void shouldReturnSubtask5ByGetSubtask() {
        super.shouldReturnSubtask5ByGetSubtask();
        assertEquals("3,EPIC,Имя эпика 3,NEW,Описание эпика 3,", getListTasksFromFile().get(1));
        assertEquals("5,SUBTASK,Имя подзадачи 5,NEW,Описание подзадачи 5,3", getListTasksFromFile().get(2));
        assertEquals("", getListTasksFromFile().get(3));
        assertEquals("5", getListTasksFromFile().get(4));
        assertThrows(IndexOutOfBoundsException.class, () -> getListTasksFromFile().get(5));
    }

    @Override
    @Test
    public void shouldReturnTask1_1() {
        super.shouldReturnTask1_1();
        assertEquals("1,TASK,Имя задачи 1_1,NEW,Описание задачи 1_1,", getListTasksFromFile().get(1));
        assertEquals("", getListTasksFromFile().get(2));
        assertEquals("1", getListTasksFromFile().get(3));
        assertThrows(IndexOutOfBoundsException.class, () -> getListTasksFromFile().get(4));
    }

    @Override
    @Test
    public void shouldReturnNullByNullUpdateTask() {
        super.shouldReturnNullByNullUpdateTask();
        assertEquals("", getListTasksFromFile().get(1));
        assertEquals("", getListTasksFromFile().get(2));
        assertThrows(IndexOutOfBoundsException.class, () -> getListTasksFromFile().get(3));
    }

    @Override
    @Test
    public void shouldReturnNullByUpdateTaskWithEmptyMap() {
        super.shouldReturnNullByUpdateTaskWithEmptyMap();
        assertEquals("", getListTasksFromFile().get(1));
        assertEquals("", getListTasksFromFile().get(2));
        assertThrows(IndexOutOfBoundsException.class, () -> getListTasksFromFile().get(3));
    }

    @Override
    @Test
    public void shouldReturnEpic3_1() {
        super.shouldReturnEpic3_1();
        assertEquals("3,EPIC,Имя эпика 3_1,NEW,Описание эпика 3_1,", getListTasksFromFile().get(1));
        assertEquals("", getListTasksFromFile().get(2));
        assertEquals("3", getListTasksFromFile().get(3));
        assertThrows(IndexOutOfBoundsException.class, () -> getListTasksFromFile().get(4));
    }

    @Override
    @Test
    public void shouldReturnNullByNullUpdateEpic() {
        super.shouldReturnNullByNullUpdateEpic();
        assertEquals("", getListTasksFromFile().get(1));
        assertEquals("", getListTasksFromFile().get(2));
        assertThrows(IndexOutOfBoundsException.class, () -> getListTasksFromFile().get(3));
    }

    @Override
    @Test
    public void shouldReturnNullByUpdateEpicWithEmptyMap() {
        super.shouldReturnNullByUpdateEpicWithEmptyMap();
        assertEquals("", getListTasksFromFile().get(1));
        assertEquals("", getListTasksFromFile().get(2));
        assertThrows(IndexOutOfBoundsException.class, () -> getListTasksFromFile().get(3));
    }

    @Override
    @Test
    public void shouldReturnEpicWithNumbersOfSubtasksByUpdate() {
        super.shouldReturnEpicWithNumbersOfSubtasksByUpdate();
        assertEquals("3,EPIC,Имя эпика 3_1,IN_PROGRESS,Описание эпика 3_1,", getListTasksFromFile().get(1));
        assertEquals("5,SUBTASK,Имя подзадачи 5,DONE,Описание подзадачи 5,3", getListTasksFromFile().get(2));
        assertEquals("6,SUBTASK,Имя подзадачи 6,NEW,Описание подзадачи 6,3,06.06.2022 06:00,6,06.06.2022 12:00",
                getListTasksFromFile().get(3));
        assertEquals("7,SUBTASK,Имя подзадачи 7,NEW,Описание подзадачи 7,3,07.07.2022 07:00,7,07.07.2022 14:00",
                getListTasksFromFile().get(4));
        assertEquals("", getListTasksFromFile().get(5));
        assertEquals("3", getListTasksFromFile().get(6));
        assertThrows(IndexOutOfBoundsException.class, () -> getListTasksFromFile().get(7));
    }

    @Override
    @Test
    public void shouldReturnSubtask5_1() {
        super.shouldReturnSubtask5_1();
        assertEquals("3,EPIC,Имя эпика 3,NEW,Описание эпика 3,", getListTasksFromFile().get(1));
        assertEquals("5,SUBTASK,Имя подзадачи 5_1,NEW,Описание подзадачи 5_1,3", getListTasksFromFile().get(2));
        assertEquals("", getListTasksFromFile().get(3));
        assertEquals("5", getListTasksFromFile().get(4));
        assertThrows(IndexOutOfBoundsException.class, () -> getListTasksFromFile().get(5));
    }

    @Override
    @Test
    public void shouldReturnNullByNullUpdateSubtask() {
        super.shouldReturnNullByNullUpdateSubtask();
        assertEquals("3,EPIC,Имя эпика 3,NEW,Описание эпика 3,", getListTasksFromFile().get(1));
        assertEquals("", getListTasksFromFile().get(2));
        assertEquals("", getListTasksFromFile().get(3));
        assertThrows(IndexOutOfBoundsException.class, () -> getListTasksFromFile().get(4));
    }

    @Override
    @Test
    public void shouldReturnNullByUpdateSubtaskWithEmptyMap() {
        super.shouldReturnNullByUpdateSubtaskWithEmptyMap();
        assertEquals("3,EPIC,Имя эпика 3,NEW,Описание эпика 3,", getListTasksFromFile().get(1));
        assertEquals("", getListTasksFromFile().get(2));
        assertEquals("", getListTasksFromFile().get(3));

        assertThrows(IndexOutOfBoundsException.class, () -> getListTasksFromFile().get(4));
    }

    @Override
    @Test
    public void shouldAssignOldIDOfEpicByUpdate() {
        super.shouldAssignOldIDOfEpicByUpdate();
        assertEquals("3,EPIC,Имя эпика 3,NEW,Описание эпика 3,", getListTasksFromFile().get(1));
        assertEquals("5,SUBTASK,Имя подзадачи 5_1,NEW,Описание подзадачи 5_1,3", getListTasksFromFile().get(2));
        assertEquals("", getListTasksFromFile().get(3));
        assertEquals("5", getListTasksFromFile().get(4));
        assertThrows(IndexOutOfBoundsException.class, () -> getListTasksFromFile().get(5));
    }

    @Override
    @Test
    public void shouldRemoveTaskAndGetCorrectHistory() {
        super.shouldRemoveTaskAndGetCorrectHistory();
        assertEquals("1,TASK,Имя задачи 1,NEW,Описание задачи 1,", getListTasksFromFile().get(1));
        assertEquals("", getListTasksFromFile().get(2));
        assertEquals("1", getListTasksFromFile().get(3));
        assertThrows(IndexOutOfBoundsException.class, () -> getListTasksFromFile().get(4));
    }

    @Override
    @Test
    public void shouldNotChangeTaskMap() {
        super.shouldNotChangeTaskMap();
        assertEquals("id,type,name,status,description,epic,startTime,duration,endTime", getListTasksFromFile().get(0));
        assertEquals("1,TASK,Имя задачи 1,NEW,Описание задачи 1,", getListTasksFromFile().get(1));
        assertEquals("2,TASK,Имя задачи 2,NEW,Описание задачи 2,,02.02.2022 02:00,2,02.02.2022 04:00",
                getListTasksFromFile().get(2));
        assertEquals("", getListTasksFromFile().get(3));
        assertEquals("1,2", getListTasksFromFile().get(4));
        assertThrows(IndexOutOfBoundsException.class, () -> getListTasksFromFile().get(5));
    }

    @Override
    @Test
    public void shouldRemoveEpicAndGetCorrectHistory() {
        super.shouldRemoveEpicAndGetCorrectHistory();
        assertEquals("3,EPIC,Имя эпика 3,NEW,Описание эпика 3,", getListTasksFromFile().get(1));
        assertEquals("", getListTasksFromFile().get(2));
        assertEquals("3", getListTasksFromFile().get(3));
        assertThrows(IndexOutOfBoundsException.class, () -> getListTasksFromFile().get(4));
    }

    @Override
    @Test
    public void shouldNotChangeEpicMap() {
        super.shouldNotChangeEpicMap();
        assertEquals("3,EPIC,Имя эпика 3,NEW,Описание эпика 3,", getListTasksFromFile().get(1));
        assertEquals("4,EPIC,Имя эпика 4,NEW,Описание эпика 4,", getListTasksFromFile().get(2));
        assertEquals("", getListTasksFromFile().get(3));
        assertEquals("3,4", getListTasksFromFile().get(4));
        assertThrows(IndexOutOfBoundsException.class, () -> getListTasksFromFile().get(5));
    }

    @Override
    @Test
    public void shouldRemoveSubtaskEverywhereAndGetCorrectHistory() {
        super.shouldRemoveSubtaskEverywhereAndGetCorrectHistory();
        assertEquals("3,EPIC,Имя эпика 3,NEW,Описание эпика 3,,07.07.2022 07:00,7,07.07.2022 14:00",
                getListTasksFromFile().get(1));
        assertEquals("5,SUBTASK,Имя подзадачи 5,NEW,Описание подзадачи 5,3", getListTasksFromFile().get(2));
        assertEquals("7,SUBTASK,Имя подзадачи 7,NEW,Описание подзадачи 7,3,07.07.2022 07:00,7,07.07.2022 14:00",
                getListTasksFromFile().get(3));
        assertEquals("", getListTasksFromFile().get(4));
        assertEquals("5,7,3", getListTasksFromFile().get(5));
        assertThrows(IndexOutOfBoundsException.class, () -> getListTasksFromFile().get(6));
    }

    @Override
    @Test
    public void shouldNotChangeSubtaskMapAndHistory() {
        super.shouldNotChangeSubtaskMapAndHistory();
        assertEquals("3,EPIC,Имя эпика 3,NEW,Описание эпика 3,,06.06.2022 06:00,752,07.07.2022 14:00",
                getListTasksFromFile().get(1));
        assertEquals("5,SUBTASK,Имя подзадачи 5,NEW,Описание подзадачи 5,3", getListTasksFromFile().get(2));
        assertEquals("6,SUBTASK,Имя подзадачи 6,NEW,Описание подзадачи 6,3,06.06.2022 06:00,6,06.06.2022 12:00",
                getListTasksFromFile().get(3));
        assertEquals("7,SUBTASK,Имя подзадачи 7,NEW,Описание подзадачи 7,3,07.07.2022 07:00,7,07.07.2022 14:00",
                getListTasksFromFile().get(4));
        assertEquals("", getListTasksFromFile().get(5));
        assertEquals("7,5,6", getListTasksFromFile().get(6));
        assertThrows(IndexOutOfBoundsException.class, () -> getListTasksFromFile().get(7));
    }

    @Override
    @Test
    public void shouldGetSubtasksOfEpic() {
        super.shouldGetSubtasksOfEpic();
        assertEquals("3,EPIC,Имя эпика 3,NEW,Описание эпика 3,,06.06.2022 06:00,752,07.07.2022 14:00",
                getListTasksFromFile().get(1));
        assertEquals("5,SUBTASK,Имя подзадачи 5,NEW,Описание подзадачи 5,3", getListTasksFromFile().get(2));
        assertEquals("6,SUBTASK,Имя подзадачи 6,NEW,Описание подзадачи 6,3,06.06.2022 06:00,6,06.06.2022 12:00",
                getListTasksFromFile().get(3));
        assertEquals("7,SUBTASK,Имя подзадачи 7,NEW,Описание подзадачи 7,3,07.07.2022 07:00,7,07.07.2022 14:00",
                getListTasksFromFile().get(4));
        assertEquals("", getListTasksFromFile().get(5));
        assertEquals("3", getListTasksFromFile().get(6));
        assertThrows(IndexOutOfBoundsException.class, () -> getListTasksFromFile().get(7));
    }

    @Override
    @Test
    public void shouldGetEmptySubtasksOfEpic() {
        super.shouldGetEmptySubtasksOfEpic();
        assertEquals("4,EPIC,Имя эпика 4,NEW,Описание эпика 4,", getListTasksFromFile().get(1));
        assertEquals("", getListTasksFromFile().get(2));
        assertEquals("4", getListTasksFromFile().get(3));
        assertThrows(IndexOutOfBoundsException.class, () -> getListTasksFromFile().get(4));
    }

    @Override
    @Test
    public void shouldReturnHistoryInMemory() {
        super.shouldReturnHistoryInMemory();
        assertEquals("1,TASK,Имя задачи 1,NEW,Описание задачи 1,", getListTasksFromFile().get(1));
        assertEquals("2,TASK,Имя задачи 2_2,NEW,Описание задачи 2_2,", getListTasksFromFile().get(2));
        assertEquals("3,EPIC,Имя эпика 3,NEW,Описание эпика 3,,06.06.2022 06:00,752,07.07.2022 14:00",
                getListTasksFromFile().get(3));
        assertEquals("4,EPIC,Имя эпика 4,NEW,Описание эпика 4,", getListTasksFromFile().get(4));
        assertEquals("5,SUBTASK,Имя подзадачи 5,NEW,Описание подзадачи 5,3", getListTasksFromFile().get(5));
        assertEquals("6,SUBTASK,Имя подзадачи 6,NEW,Описание подзадачи 6,3,06.06.2022 06:00,6,06.06.2022 12:00",
                getListTasksFromFile().get(6));
        assertEquals("7,SUBTASK,Имя подзадачи 7,NEW,Описание подзадачи 7,3,07.07.2022 07:00,7,07.07.2022 14:00",
                getListTasksFromFile().get(7));
        assertEquals("", getListTasksFromFile().get(8));
        assertEquals("2,4,6,1,3,5,7", getListTasksFromFile().get(9));
        assertThrows(IndexOutOfBoundsException.class, () -> getListTasksFromFile().get(10));
    }

    @Override
    @Test
    public void shouldReturnEmptyList() {
        super.shouldReturnEmptyList();
        assertEquals("", getListTasksFromFile().get(1));
        assertEquals("", getListTasksFromFile().get(2));
        assertThrows(IndexOutOfBoundsException.class, () -> getListTasksFromFile().get(3));
    }

    private List<String> getListTasksFromFile() {
        List<String> list = new ArrayList<>();
        try {
            list = Files.readAllLines(path);
            for (String s : list) {
                System.out.println(s);
            }
        } catch (IOException e) {
            System.out.println("!!! Не смогли прочитать данные из файла / файл отсутствует");
        }
        return list;
    }

    @Test
    public void shouldNotFindTheFile() {
        assertFalse(Files.exists(path));
    }

    @Test
    public void shouldGiveEmptyList() {
        assertEquals(Collections.emptyList(), getListTasksFromFile());
    }

    @Test
    public void shouldGiveMapsWithHistory() {
        manager.createNewTask(task1);
        manager.createNewTask(task2);
        manager.createNewEpic(epic3);
        manager.createNewEpic(epic4);
        manager.createNewSubtask(subtask5);
        manager.createNewSubtask(subtask6);
        manager.createNewSubtask(subtask7);
        manager.getTaskById(1);
        manager.getEpicById(3);
        manager.getSubtaskById(5);
        manager.getSubtaskById(7);
        Task task1_1 = new Task(1, "Имя задачи 1_1", "Описание задачи 1_1");
        manager.updateTask(task1_1);
        manager.getSubtaskById(6);
        manager.getEpicById(3);
        FileBackedTasksManager manager2 = FileBackedTasksManager.loadFromFile(new File(path.toString()));
        assertEquals(List.of(new Task(1, "Имя задачи 1_1", "Описание задачи 1_1"), task2), manager2.getAllTasks());
        assertEquals(List.of(epic3, epic4), manager2.getAllEpics());
        assertEquals(List.of(subtask5, subtask6, subtask7), manager2.getAllSubtasks());
        assertEquals(List.of(task1_1, subtask5, subtask7, subtask6, epic3), manager2.getHistoryInMemory().getHistory());
    }

    @Test
    public void shouldGiveEmptyMapsWithEmptyHistory() {
        manager.createNewTask(task1);
        manager.createNewEpic(epic3);
        manager.createNewSubtask(subtask5);
        manager.createNewSubtask(subtask7);
        manager.getTaskById(1);
        manager.getEpicById(3);
        manager.getSubtaskById(5);
        manager.getSubtaskById(7);
        manager.removeTaskById(1);
        manager.clearAllEpics();
        FileBackedTasksManager manager2 = FileBackedTasksManager.loadFromFile(new File(path.toString()));
        assertEquals(Collections.emptyList(), manager2.getAllTasks());
        assertEquals(Collections.emptyList(), manager2.getAllEpics());
        assertEquals(Collections.emptyList(), manager2.getAllSubtasks());
        assertEquals(Collections.emptyList(), manager2.getHistoryInMemory().getHistory());
    }

    //EpicTime
    @Override
    @Test
    public void shouldReturnCorrectTimeOfEpic() {
        super.shouldReturnCorrectTimeOfEpic();
        FileBackedTasksManager manager2 = FileBackedTasksManager.loadFromFile(new File(path.toString()));
        assertEquals("[Epic{name='EpicT9', description='Description epicT9', id=109, status='NEW', subtasks=[]}]",
                manager2.getAllEpics().toString());
        assertEquals(Collections.emptyList(), manager2.getAllSubtasks());
        assertNull(manager2.getEpicById(109).getStartTime());
        assertNull(manager2.getEpicById(109).getEndTime());

        Subtask subtaskT13 = new Subtask(113, "SubtaskT13", "Description subtaskT13", 109,
                LocalDateTime.of(2022, 4, 15, 15, 0), Duration.ofHours(3));
        Subtask subtaskT14 = new Subtask(114, "SubtaskT14", "Description subtaskT14", 109,
                LocalDateTime.of(2022, 4, 16, 18, 0), Duration.ofHours(72));
        manager2.createNewSubtask(subtaskT13);
        manager2.createNewSubtask(subtaskT14);

        FileBackedTasksManager manager3 = FileBackedTasksManager.loadFromFile(new File(path.toString()));
        assertEquals("[Epic{name='EpicT9', description='Description epicT9', id=109, status='NEW', " +
                        "subtasks=[113, 114], startTime=15.04.2022 15:00, duration=99, endTime=19.04.2022 18:00}]",
                manager3.getAllEpics().toString());
        assertEquals(List.of(subtaskT13, subtaskT14), manager3.getAllSubtasks());
        assertEquals("109,EPIC,EpicT9,NEW,Description epicT9,,15.04.2022 15:00,99,19.04.2022 18:00",
                getListTasksFromFile().get(1));
    }

    @Override
    @Test
    public void shouldNotAddTaskInTimeAnotherTask() {
        super.shouldNotAddTaskInTimeAnotherTask();
        assertEquals("105,TASK,TaskT5,NEW,Description taskT5,,15.04.2022 12:00,24,16.04.2022 12:00",
                getListTasksFromFile().get(1));
        assertEquals("109,EPIC,EpicT9,NEW,Description epicT9,,17.04.2022 12:00,24,18.04.2022 12:00",
                getListTasksFromFile().get(2));
        assertEquals("110,SUBTASK,SubtaskT10,NEW,Description subtaskT10,109,17.04.2022 12:00,24,18.04.2022 12:00",
                getListTasksFromFile().get(3));
        assertEquals("", getListTasksFromFile().get(4));
        assertEquals("", getListTasksFromFile().get(5));
        assertThrows(IndexOutOfBoundsException.class, () -> getListTasksFromFile().get(6));
    }

    //getPrioritizedTasks
    @Override
    @Test
    public void shouldCheckGetPrioritizedTasks() {
        super.shouldCheckGetPrioritizedTasks();
        assertEquals("1,TASK,Имя задачи 1,NEW,Описание задачи 1,", getListTasksFromFile().get(1));
        assertEquals("33,TASK,TaskT8_1,NEW,Description taskT8_1,,24.04.2022 09:28,9,24.04.2022 18:28",
                getListTasksFromFile().get(2));
        assertEquals("3,EPIC,Имя эпика 3,NEW,Описание эпика 3,", getListTasksFromFile().get(3));
        assertEquals("44,EPIC,EpicT9,NEW,Description epicT9,", getListTasksFromFile().get(4));
        assertEquals("", getListTasksFromFile().get(5));
        assertEquals("44", getListTasksFromFile().get(6));
        assertThrows(IndexOutOfBoundsException.class, () -> getListTasksFromFile().get(7));
    }

    @Override
    @Test
    public void shouldGetEmptyByGetPrioritizedTasks() {
        super.shouldGetEmptyByGetPrioritizedTasks();
        getListTasksFromFile();
        assertThrows(IndexOutOfBoundsException.class, () -> getListTasksFromFile().get(0));
    }

    @Test
    public void shouldReturnChanges() {
        Task washTheDishes = new Task(getNextId(), "Вымыть посуду",                      //1
                "Чтобы жена не ругалась опять");
        manager.createNewTask(washTheDishes);
        Task takeOutTheTrash = new Task(getNextId(), "Вынести мусор", "В мусорный бак");    //2
        manager.createNewTask(takeOutTheTrash);
        Epic finishTheSecondSprint = new Epic(getNextId(), "Закончить третий спринт",               //3
                "Не такой уж и легкий как оказалось");
        manager.createNewEpic(finishTheSecondSprint);
        Subtask theory = new Subtask(getNextId(), "Пройти теорию",                                  //4
                "Закрепить теорию на задачках", 3);
        manager.createNewSubtask(theory);
        Subtask practice = new Subtask(getNextId(), "Сделать задание",                              //5
                "Так что бы ревью пройти с первого раза (мечты мечты...)", 3);
        manager.createNewSubtask(practice);
        Epic shopping = new Epic(getNextId(), "Сходить в магазин", "Побалуем себя");       //6
        manager.createNewEpic(shopping);
        Subtask buyFish = new Subtask(getNextId(), "Купить красную рыбу",                           //7
                "Говорят полезна для мозга", 6);
        manager.createNewSubtask(buyFish);
        manager.getTaskById(1);
        manager.getTaskById(2);
        manager.getEpicById(3);
        manager.getSubtaskById(4);
        manager.getSubtaskById(5);
        manager.getEpicById(6);
        manager.getSubtaskById(7);
        manager.getTaskById(2);
        FileBackedTasksManager manager2 = FileBackedTasksManager.loadFromFile(new File(path.toString()));
        assertEquals(List.of(washTheDishes, takeOutTheTrash), manager2.getAllTasks());
        assertEquals(List.of(finishTheSecondSprint, shopping), manager2.getAllEpics());
        assertEquals(List.of(theory, practice, buyFish), manager2.getAllSubtasks());
        assertEquals(List.of(washTheDishes, finishTheSecondSprint, theory, practice, shopping, buyFish, takeOutTheTrash),
                manager2.getHistoryInMemory().getHistory());
        manager2.removeTaskById(1);
        Subtask buyCake = new Subtask(getNextId(), "Купить тортик",                       //8
                "Сладость в радость", 6);
        manager2.createNewSubtask(buyCake);
        manager2.getSubtaskById(8);
        FileBackedTasksManager manager3 = FileBackedTasksManager.loadFromFile(new File(path.toString()));
        getListTasksFromFile();
        assertEquals(Collections.singletonList(takeOutTheTrash), manager3.getAllTasks());
        assertEquals(finishTheSecondSprint, manager3.getEpicById(3));
        assertEquals("6,EPIC,Сходить в магазин,NEW,Побалуем себя,", getListTasksFromFile().get(3));
        assertEquals("7,SUBTASK,Купить красную рыбу,NEW,Говорят полезна для мозга,6", getListTasksFromFile().get(6));
        assertEquals("8,SUBTASK,Купить тортик,NEW,Сладость в радость,6", getListTasksFromFile().get(7));
        assertEquals(List.of(theory, practice, buyFish, buyCake), manager3.getAllSubtasks());
        assertEquals("4,5,6,7,2,8,3", getListTasksFromFile().get(9));
    }
}