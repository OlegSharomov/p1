package service;

import exceptions.ManagerSaveException;
import models.Epic;
import models.Status;
import models.Subtask;
import models.Task;
import models.Type;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static models.Type.SUBTASK;

public class FileBackedTasksManager extends InMemoryTaskManager {
    private Path filePathToSave;
    private static final String HEADER_CSV = "id,type,name,status,description,epic,startTime,duration,endTime";

    private FileBackedTasksManager(File file) {
        filePathToSave = Paths.get(file.getName());
    }

    FileBackedTasksManager() {
    }

    @Override
    public void createNewTask(Task task) {
        super.createNewTask(task);
        save();
    }

    @Override
    public void createNewEpic(Epic epic) {
        super.createNewEpic(epic);
        save();
    }

    @Override
    public void createNewSubtask(Subtask subtask) {
        super.createNewSubtask(subtask);
        save();
    }

    @Override
    public void clearAllTasks() {
        super.clearAllTasks();
        save();
    }

    @Override
    public void clearAllEpics() {
        super.clearAllEpics();
        save();
    }

    @Override
    public void clearAllSubtasks() {
        super.clearAllSubtasks();
        save();
    }

    @Override
    public Task getTaskById(int id) {
        Task result = super.getTaskById(id);
        save();
        return result;
    }

    @Override
    public Epic getEpicById(int id) {
        Epic result = super.getEpicById(id);
        save();
        return result;
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask result = super.getSubtaskById(id);
        save();
        return result;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void removeTaskById(int id) {
        super.removeTaskById(id);
        save();
    }

    @Override
    public void removeSubtaskById(int id) {
        super.removeSubtaskById(id);
        save();
    }

    @Override
    public void removeEpicById(int id) {
        super.removeEpicById(id);
        save();
    }

    public static FileBackedTasksManager loadFromFile(File file) {
        FileBackedTasksManager managerWithFiles = new FileBackedTasksManager(file);
        try {
            if (file.isFile()) {
                List<String> allStrings = Files.readAllLines(Path.of(file.getName()), StandardCharsets.UTF_8);
                if (!allStrings.isEmpty() && allStrings.size() > 3) {
                    for (int i = 1; i < allStrings.size() - 2; i++) {
                        String[] taskData = allStrings.get(i).split(",");
                        for (int j = 0; j < taskData.length; j++) {
                            taskData[j] = taskData[j].trim();
                        }
                        if (taskData.length >= 5) {
                            managerWithFiles.distributionOfTasksByMaps(taskData);
                        }
                    }
                    String[] historyCount = allStrings.get(allStrings.size() - 1).split(",");
                    for (String number : historyCount) {
                        managerWithFiles.parsingAndInitialization(number.trim());
                    }
                    ArrayList<Integer> allId = new ArrayList<>();
                    allId.addAll(managerWithFiles.taskMap.keySet());
                    allId.addAll(managerWithFiles.subtaskMap.keySet());
                    allId.addAll(managerWithFiles.epicMap.keySet());
                    countId = Collections.max(allId);
                }
            }
        } catch (IOException e) {
            try {
                throw new ManagerSaveException("Can't read from the file: "
                        + managerWithFiles.filePathToSave.getFileName(), e);
            } catch (ManagerSaveException exception) {
                System.out.println(exception.getMessage());
            }
        }
        return managerWithFiles;
    }

    private void distributionOfTasksByMaps(String[] taskData) {
        switch (Type.valueOf(taskData[1])) {
            case TASK:
                taskMap.put(Integer.parseInt(taskData[0]), collectTask(taskData));
                break;
            case EPIC:
                epicMap.put(Integer.parseInt(taskData[0]), collectEpic(taskData));
                break;
            case SUBTASK:
                if (epicMap.containsKey(Integer.parseInt(taskData[5]))) {
                    Subtask subtask = collectSubtask(taskData);
                    subtaskMap.put(Integer.parseInt(taskData[0]), subtask);
                    epicMap.get(subtask.getIdOfEpicNumber()).
                            getSubtasks().add(subtask.getId());
                    checkAndChangeTimeForEpic(subtask.getIdOfEpicNumber(), subtask);
                }
                break;
            default:
                System.out.println("При чтении файла не был определен тип задачи: \n"
                        + Arrays.toString(taskData));
        }
    }

    private void parsingAndInitialization(String number) {
        if (!number.isEmpty()) {
            Integer id = Integer.parseInt(number);
            if (taskMap.containsKey(id)) {
                historyInMemory.add(taskMap.get(id));
            }
            if (subtaskMap.containsKey(id)) {
                historyInMemory.add(subtaskMap.get(id));
            }
            if (epicMap.containsKey(id)) {
                historyInMemory.add(epicMap.get(id));
            }
        }
    }

    private static Task collectTask(String[] taskData) {
        Task task;
        if (taskData.length < 7) {
            task = new Task(Integer.parseInt(taskData[0]), taskData[2], taskData[4]);
        } else {
            task = new Task(Integer.parseInt(taskData[0]), taskData[2], taskData[4], collectTime(taskData[6]),
                    collectDuration(taskData[7]));
        }
        task.setStatus(Status.valueOf(taskData[3]));
        return task;
    }

    private static Epic collectEpic(String[] taskData) {
        Epic epic = new Epic(Integer.parseInt(taskData[0]), taskData[2], taskData[4]);
        epic.setStatus(Status.valueOf(taskData[3]));
        return epic;
    }

    private static Subtask collectSubtask(String[] taskData) {
        Subtask subtask;
        if (taskData.length < 7) {
            subtask = new Subtask(Integer.parseInt(taskData[0]), taskData[2], taskData[4],
                    Integer.parseInt(taskData[5]));
        } else {
            subtask = new Subtask(Integer.parseInt(taskData[0]), taskData[2], taskData[4],
                    Integer.parseInt(taskData[5]), collectTime(taskData[6]),
                    collectDuration(taskData[7]));
        }
        subtask.setStatus(Status.valueOf(taskData[3]));
        return subtask;
    }

    void save() {
        try (FileWriter fw = new FileWriter(String.valueOf(filePathToSave), StandardCharsets.UTF_8);
             BufferedWriter br = new BufferedWriter(fw)) {
            br.write(HEADER_CSV);
            br.newLine();
            for (Task task : taskMap.values()) {
                br.write(toString(task));
            }
            for (Epic epic : epicMap.values()) {
                br.write(toString(epic));
            }
            for (Subtask subtask : subtaskMap.values()) {
                br.write(toString(subtask));
            }
            br.newLine();
            if (!historyInMemory.getHistory().isEmpty()) {
                br.write(toString(historyInMemory));
            } else {
                br.newLine();
            }
        } catch (IOException e) {
            try {
                throw new ManagerSaveException("Can't save to file: " + filePathToSave.getFileName(), e);
            } catch (ManagerSaveException exception) {
                System.out.println(exception.getMessage());
            }
        }
    }

    private String toString(Task task) {
        String epicOfSubtask = "";
        if (task.getType() == SUBTASK) {
            epicOfSubtask += ((Subtask) task).getIdOfEpicNumber();
        }
        Stream<Object> fields;
        if (task.getStartTime() == null) {
            fields = Stream.builder()
                    .add(task.getId())
                    .add(task.getType())
                    .add(task.getName())
                    .add(task.getStatus())
                    .add(task.getDescription())
                    .add(epicOfSubtask)
                    .build();
        } else {
            fields = Stream.builder()
                    .add(task.getId())
                    .add(task.getType())
                    .add(task.getName())
                    .add(task.getStatus())
                    .add(task.getDescription())
                    .add(epicOfSubtask)
                    .add(task.getStartTimeToString())
                    .add(task.getDurationToString())
                    .add(task.getEndTimeToString())
                    .build();
        }
        return fields.map(String::valueOf).collect(Collectors.joining(",", "", "\n"));
    }

    private static String toString(HistoryManager historyManager) {
        final List<Task> history = historyManager.getHistory();
        if (history.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(history.get(0).getId());
        for (int i = 1; i < history.size(); i++) {
            Task task = history.get(i);
            sb.append(",");
            sb.append(task.getId());
        }
        return sb.toString();
    }

    private static LocalDateTime collectTime(String s) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        return LocalDateTime.parse(s, formatter);
    }

    private static Duration collectDuration(String s) {
        return Duration.ofHours(Integer.parseInt(s));
    }
}
