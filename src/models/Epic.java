package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;

public class Epic extends Task {
    private ArrayList<Integer> subtasks = new ArrayList<>();
    private LocalDateTime endTime;

    public Epic(int id, String name, String description) {
        super(id, name, description);
    }

    @Override
    public Type getType() {
        return Type.EPIC;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    @Override
    public String getEndTimeToString() {
        String result = "";
        if (endTime != null) {
            result = endTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        }
        return result;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Epic epic = (Epic) o;
        return Objects.equals(subtasks, epic.subtasks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), subtasks);
    }

    public ArrayList<Integer> getSubtasks() {
        return subtasks;
    }

    public void setSubtasks(ArrayList<Integer> subtasks) {
        this.subtasks = subtasks;
    }

    @Override
    public String toString() {
        String epicView;
        if (getStartTime() != null && getDuration() != null && getEndTime() != null) {

            epicView = "Epic{" +
                    "name='" + getName() + '\'' +
                    ", description='" + getDescription() + '\'' +
                    ", id=" + getId() +
                    ", status='" + getStatus() + '\'' +
                    ", subtasks=" + subtasks +
                    ", startTime=" + getStartTimeToString() +
                    ", duration=" + getDurationToString() +
                    ", endTime=" + getEndTimeToString() +
                    '}';
        } else {
            epicView = "Epic{" +
                    "name='" + getName() + '\'' +
                    ", description='" + getDescription() + '\'' +
                    ", id=" + getId() +
                    ", status='" + getStatus() + '\'' +
                    ", subtasks=" + subtasks +
                    '}';
        }
        return epicView;
    }
}
