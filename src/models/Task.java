package models;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Task {
    private final String name;
    private final String description;
    private final int id;
    private Status status = Status.NEW;
    private LocalDateTime startTime;
    private Duration duration;

    public Task(int id, String name, String description) {
        this(id, name, description, null, null);
    }

    public Task(int id, String name, String description, LocalDateTime startTime, Duration duration) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.startTime = startTime;
        this.duration = duration;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getId() {
        return id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Type getType() {
        return Type.TASK;
    }

    public String getStartTimeToString() {
        String result = "";
        if (startTime != null) {
            result = startTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        }
        return result;
    }

    public String getDurationToString() {
        String result = "";
        if (duration != null) {
            result = "" + duration.toHours();
        }
        return result;
    }

    public String getEndTimeToString() {
        String result = "";
        if (getEndTime() != null) {
            result = startTime.plus(duration).format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        }
        return result;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public Duration getDuration() {
        return duration;
    }

    public LocalDateTime getEndTime() {
        if (startTime != null && duration != null) {
            return startTime.plus(duration);
        }
        return null;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id && Objects.equals(name, task.name) && Objects.equals(description, task.description)
                && status == task.status && Objects.equals(startTime, task.startTime)
                && Objects.equals(duration, task.duration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, id, status, startTime, duration);
    }

    @Override
    public String toString() {
        String taskView;
        if (startTime != null && duration != null) {
            taskView = "Task{" +
                    "name='" + name + '\'' +
                    ", description='" + description + '\'' +
                    ", id=" + id +
                    ", status=" + status +
                    ", startTime=" + getStartTimeToString() +
                    ", duration=" + getDurationToString() +
                    ", endTime=" + getEndTimeToString() +
                    '}';
        } else {
            taskView = "Task{" +
                    "name='" + name + '\'' +
                    ", description='" + description + '\'' +
                    ", id=" + id +
                    ", status='" + status + '\'' +
                    '}';
        }
        return taskView;
    }
}