package models;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class Subtask extends Task {
    private int idOfEpicNumber;

    public Subtask(int id, String name, String description, int linkToTheEpicNumber) {
        super(id, name, description);
        this.idOfEpicNumber = linkToTheEpicNumber;
    }

    public Subtask(int id, String name, String description, int linkToTheEpicNumber, LocalDateTime startTime,
                   Duration duration) {
        super(id, name, description, startTime, duration);
        this.idOfEpicNumber = linkToTheEpicNumber;
    }

    @Override
    public Type getType() {
        return Type.SUBTASK;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Subtask subtask = (Subtask) o;
        return idOfEpicNumber == subtask.idOfEpicNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), idOfEpicNumber);
    }

    public int getIdOfEpicNumber() {
        return idOfEpicNumber;
    }

    public void setIdOfEpicNumber(int idOfEpicNumber) {
        this.idOfEpicNumber = idOfEpicNumber;
    }

    @Override
    public String toString() {
        String subtaskView;
        if (getStartTime() != null && getDuration() != null) {
            subtaskView = "Subtask{" +
                    "name='" + getName() + '\'' +
                    ", description='" + getDescription() + '\'' +
                    ", id=" + getId() +
                    ", status='" + getStatus() + '\'' +
                    ", idOfEpicNumber=" + idOfEpicNumber +
                    ", startTime=" + getStartTimeToString() +
                    ", duration=" + getDurationToString() +
                    ", endTime=" + getEndTimeToString() +
                    '}';
        } else {
            subtaskView = "Subtask{" +
                    "name='" + getName() + '\'' +
                    ", description='" + getDescription() + '\'' +
                    ", id=" + getId() +
                    ", status='" + getStatus() + '\'' +
                    ", idOfEpicNumber=" + idOfEpicNumber +
                    '}';
        }
        return subtaskView;
    }
}
