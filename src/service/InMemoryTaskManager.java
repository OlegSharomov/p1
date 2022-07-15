package service;

import models.Epic;
import models.Subtask;
import models.Task;
import util.Managers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static models.Status.DONE;
import static models.Status.IN_PROGRESS;
import static models.Status.NEW;

public class InMemoryTaskManager implements TaskManager {
    static int countId = 0;
    HashMap<Integer, Task> taskMap = new HashMap<>();
    HashMap<Integer, Epic> epicMap = new HashMap<>();
    HashMap<Integer, Subtask> subtaskMap = new HashMap<>();
    HistoryManager historyInMemory = Managers.getDefaultHistory();
    private final TreeSet<Task> prioritizedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime,
            Comparator.nullsLast(Comparator.naturalOrder())).thenComparing(Task::getId));

    public static int getNextId() {
        return ++countId;
    }

    @Override
    public void createNewTask(Task task) {
        if (task != null && !isWrongTask(task) && !taskMap.containsKey(task.getId()) && findIntersection(task)) {
            taskMap.put(task.getId(), task);
            prioritizedTasks.add(task);
        } else {
            System.out.println("Задача не добавлена. Проверьте корректность заполненных данных");
        }
    }

    @Override
    public void createNewEpic(Epic epic) {
        if (epic != null && !isWrongTask(epic) && !epicMap.containsKey(epic.getId())) {
            epicMap.put(epic.getId(), epic);
        } else {
            System.out.println("Епик не добавлен. Проверьте корректность заполненных данных");
        }
    }

    @Override
    public void createNewSubtask(Subtask subtask) {
        if (subtask != null && !isWrongTask(subtask) && epicMap.containsKey(subtask.getIdOfEpicNumber())
                && !subtaskMap.containsKey(subtask.getId()) && findIntersection(subtask)) {
            if (!epicMap.get(subtask.getIdOfEpicNumber()).getSubtasks().contains(subtask.getId())) {
                epicMap.get(subtask.getIdOfEpicNumber()).getSubtasks().add(subtask.getId());
            }
            subtaskMap.put(subtask.getId(), subtask);
            checkAndChangeStatus(subtask.getIdOfEpicNumber());
            checkAndChangeTimeForEpic(subtask.getIdOfEpicNumber(), subtask);
            prioritizedTasks.add(subtask);
        } else {
            System.out.println("Подзадача " + subtask + " не добавлена. Проверьте корректность заполненных данных");
        }
    }

    @Override
    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(taskMap.values());
    }

    @Override
    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(epicMap.values());
    }

    @Override
    public ArrayList<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtaskMap.values());
    }

    @Override
    public void clearAllTasks() {
        for (Task task : taskMap.values()) {
            if (historyInMemory.getHistory().contains(task)) {
                historyInMemory.remove(task.getId());
            }
            prioritizedTasks.remove(task);
        }
        taskMap.clear();
    }

    @Override
    public void clearAllEpics() {
        for (Subtask subtask : subtaskMap.values()) {
            if (historyInMemory.getHistory().contains(subtask)) {
                historyInMemory.remove(subtask.getId());
            }
        }
        for (Epic epic : epicMap.values()) {
            if (historyInMemory.getHistory().contains(epic)) {
                historyInMemory.remove(epic.getId());
            }
        }
        subtaskMap.clear();
        epicMap.clear();
    }

    @Override
    public void clearAllSubtasks() {
        for (Subtask subtask : subtaskMap.values()) {
            if (historyInMemory.getHistory().contains(subtask)) {
                historyInMemory.remove(subtask.getId());
            }
            prioritizedTasks.remove(subtask);
        }
        subtaskMap.clear();
        for (Epic epic : epicMap.values()) {
            epic.getSubtasks().clear();
            epic.setStartTime(null);
            epic.setDuration(null);
            epic.setEndTime(null);
        }
    }

    @Override
    public Task getTaskById(int id) {
        if (taskMap.containsKey(id)) {
            historyInMemory.add(taskMap.get(id));
        } else {
            System.out.println("Такого id нет в списке задач");
        }
        return taskMap.get(id);
    }

    @Override
    public Epic getEpicById(int id) {
        if (epicMap.containsKey(id)) {
            historyInMemory.add(epicMap.get(id));
        } else {
            System.out.println("Такого id нет в списке епиков");
        }
        return epicMap.get(id);
    }

    @Override
    public Subtask getSubtaskById(int id) {
        if (subtaskMap.containsKey(id)) {
            historyInMemory.add(subtaskMap.get(id));
        } else {
            System.out.println("Такого id нет в списке подзадач");
        }
        return subtaskMap.get(id);
    }

    @Override
    public void updateTask(Task task) {
        if (task != null && !isWrongTask(task) && taskMap.containsKey(task.getId()) && findIntersection(task)) {
            prioritizedTasks.remove(taskMap.get(task.getId()));
            prioritizedTasks.add(task);
            taskMap.put(task.getId(), task);
        } else {
            System.out.println("Такого id нет в списке задач");
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtask != null && !isWrongTask(subtask) && subtaskMap.containsKey(subtask.getId())
                && findIntersection(subtask)) {
            int idOfEpicNumber = subtaskMap.get(subtask.getId()).getIdOfEpicNumber();
            subtask.setIdOfEpicNumber(idOfEpicNumber);
            prioritizedTasks.remove(subtaskMap.get(subtask.getId()));
            prioritizedTasks.add(subtask);
            subtaskMap.put(subtask.getId(), subtask);
            checkAndChangeStatus(idOfEpicNumber);
            checkAndChangeTimeForEpic(subtask.getIdOfEpicNumber(), subtask);
        } else {
            System.out.println("Такого id нет в списке подзадач");
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epic != null && !isWrongTask(epic) && epicMap.containsKey(epic.getId())) {
            epic.setSubtasks(epicMap.get(epic.getId()).getSubtasks());
            epicMap.put(epic.getId(), epic);
            checkAndChangeStatus(epic.getId());
        } else {
            System.out.println("Такого id нет в списке задач");
        }
    }

    @Override
    public void removeTaskById(int id) {
        if (taskMap.containsKey(id)) {
            if (historyInMemory.getHistory().contains(taskMap.get(id))) {
                historyInMemory.remove(id);
            }
            prioritizedTasks.remove(taskMap.get(id));
            taskMap.remove(id);
        } else {
            System.out.println("Такой задачи не найдено");
        }
    }

    @Override
    public void removeSubtaskById(int id) {
        if (subtaskMap.containsKey(id)) {
            Subtask subtaskForRemove = subtaskMap.get(id);
            int idOfEpicNumber = subtaskForRemove.getIdOfEpicNumber();
            if (historyInMemory.getHistory().contains(subtaskForRemove)) {
                historyInMemory.remove(id);
            }
            prioritizedTasks.remove(subtaskForRemove);
            subtaskMap.remove(id);
            epicMap.get(idOfEpicNumber).getSubtasks().remove((Integer) id);
            checkAndChangeStatus(idOfEpicNumber);
            checkAndChangeTimeForEpic(idOfEpicNumber, subtaskForRemove);
        } else {
            System.out.println("Такой подзадачи не найдено");
        }
    }

    @Override
    public void removeEpicById(int id) {
        if (epicMap.containsKey(id)) {
            if (historyInMemory.getHistory().contains(epicMap.get(id))) {
                historyInMemory.remove(id);
            }
            for (Integer subInArray : epicMap.get(id).getSubtasks()) {
                subtaskMap.remove(subInArray);
            }
            epicMap.remove(id);
        } else {
            System.out.println("Такого Епика не найдено");
        }
    }

    @Override
    public ArrayList<Subtask> getSubtasksOfEpic(int idEpic) {
        ArrayList<Subtask> sub = new ArrayList<>();
        if (epicMap.containsKey(idEpic)) {
            for (Integer idSubtask : epicMap.get(idEpic).getSubtasks()) {
                sub.add(subtaskMap.get(idSubtask));
            }
        } else {
            System.out.println("Такого id не найдено в списке епиков");
        }
        return sub;
    }

    @Override
    public HistoryManager getHistoryInMemory() {
        return historyInMemory;
    }

    @Override
    public TreeSet<Task> getPrioritizedTasks() {
        return prioritizedTasks;
    }

    private void checkAndChangeStatus(int idEpic) {
        if (epicMap.containsKey(idEpic)) {
            ArrayList<Integer> thisSubtasks = epicMap.get(idEpic).getSubtasks();
            if (thisSubtasks.isEmpty()) {
                epicMap.get(idEpic).setStatus(NEW);
                return;
            }
            int countNew = 0;
            int countDone = 0;
            for (Integer subtaskNum : thisSubtasks) {
                if (subtaskMap.get(subtaskNum).getStatus().equals(IN_PROGRESS)) {
                    epicMap.get(idEpic).setStatus(IN_PROGRESS);
                    return;
                }
                if (subtaskMap.get(subtaskNum).getStatus().equals(NEW)) {
                    countNew++;
                } else {
                    countDone++;
                }
            }
            if (countDone > 0 && countNew > 0) {
                epicMap.get(idEpic).setStatus(IN_PROGRESS);
            } else if (countDone > 0) {
                epicMap.get(idEpic).setStatus(DONE);
            } else {
                epicMap.get(idEpic).setStatus(NEW);
            }
        }
    }

    void checkAndChangeTimeForEpic(int idEpic, Subtask subtask) {
        Epic findEpic = epicMap.get(idEpic);
        if (findEpic.getSubtasks().size() < 2 && (subtask.getStartTime() == null || subtask.getDuration() == null)) {
            return;
        }
        List<Subtask> subs = subtaskMap.values().stream()
                .filter(subtask1 ->
                        findEpic.getSubtasks().contains(subtask1.getId())
                                && (subtask1.getStartTime() != null || subtask1.getDuration() != null))
                .sorted(Comparator.comparing(Task::getStartTime))
                .collect(Collectors.toList());
        if (subs.size() > 0) {
            findEpic.setStartTime(subs.get(0).getStartTime());
            findEpic.setEndTime(subs.get(subs.size() - 1).getEndTime());
            findEpic.setDuration(Duration.between(findEpic.getStartTime(), findEpic.getEndTime()).abs());
        } else {
            findEpic.setStartTime(null);
            findEpic.setDuration(null);
            findEpic.setEndTime(null);
        }
    }

    public boolean findIntersection(Task task) {
        if (task.getStartTime() != null && task.getDuration() != null && task.getEndTime() != null) {
            for (Task existTask : getPrioritizedTasks()) {
                if (existTask.getStartTime() != null && existTask.getDuration()
                        != null && existTask.getEndTime() != null) {
                    if ((task.getStartTime().isAfter(existTask.getStartTime())
                            && task.getStartTime().isBefore(existTask.getEndTime()))
                            || (task.getEndTime().isAfter(existTask.getStartTime())
                            && task.getEndTime().isBefore(existTask.getEndTime()))
                            || (existTask.getStartTime().isAfter(task.getStartTime())
                            && existTask.getEndTime().isBefore(task.getEndTime()))) {
                        System.out.println("Задача не может быть создана. " +
                                "Она находится на временном отрезке уже существующей задачи");
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static <T extends Task> boolean isWrongTask(T task) {
        return task.getId() < 1 || task.getName().isBlank();
    }
}