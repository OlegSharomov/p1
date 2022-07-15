package models;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.FileBackedTasksManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EpicTest {
    private FileBackedTasksManager manager;
    private Epic epic1;
    private Subtask subtask1;
    private Subtask subtask2;
    private Subtask subtask3;

    @BeforeEach
    void createNewEpicAndManager() {
        manager = FileBackedTasksManager.loadFromFile(new File("HistoryOfTaskTest2.csv"));
        epic1 = new Epic(1, "Epic1", "Description epic1");
        subtask1 = new Subtask(2, "Subtask1", "Descriptions subtask1", 1,
                LocalDateTime.of(2022, 4, 1, 12, 0), Duration.ofHours(6));
        subtask2 = new Subtask(3, "Subtask2", "Descriptions subtask2", 1,
                LocalDateTime.of(2022, 4, 2, 12, 0), Duration.ofHours(3));
        subtask3 = new Subtask(4, "Subtask3", "Descriptions subtask2", 1,
                LocalDateTime.of(2022, 4, 5, 12, 0), Duration.ofHours(24));
        manager.createNewEpic(epic1);
        manager.createNewSubtask(subtask1);
        manager.createNewSubtask(subtask2);
        manager.createNewSubtask(subtask3);
    }

    @AfterEach
    void deleteCSV() {
        try {
            Files.deleteIfExists(Path.of("HistoryOfTaskTest2.csv"));
        } catch (IOException e) {
            System.out.println("Файл не был удален, т.к. он не существует");
        }
    }

    @Test
    void shouldGetType() {
        assertEquals(Type.EPIC, epic1.getType());
    }

    @Test
    void shouldGetEndTime() {
        assertEquals("06.04.2022 12:00", epic1.getEndTimeToString());
    }

    @Test
    void shouldGetEndTimeToString() {
        assertEquals(LocalDateTime.of(2022, 4, 6, 12, 0), epic1.getEndTime());
    }

    @Test
    void shouldGetSubtasks() {
        assertEquals(List.of(2, 3, 4), epic1.getSubtasks());
    }

    @Test
    void shouldGetStringValue() {
        assertEquals("Epic{name='Epic1', description='Description epic1', id=1, status='NEW', subtasks=[2, 3, 4]," +
                " startTime=01.04.2022 12:00, duration=120, endTime=06.04.2022 12:00}", epic1.toString());
        Epic epic9 = new Epic(9, "Epic9", "Description epic9");
        assertEquals("Epic{name='Epic9', description='Description epic9', id=9, status='NEW', subtasks=[]}", epic9.toString());
    }
}