/**
 * Enumeration representing the possible states of a task.
 * Based on Requirements 6.3: Task status as one of "todo", "in-progress", "done"
 */
public enum TaskStatus {
    TODO("todo"),
    IN_PROGRESS("in-progress"),
    DONE("done");

    private final String value;

    TaskStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Parse a string value to TaskStatus enum
     * @param value the string representation
     * @return the corresponding TaskStatus
     * @throws IllegalArgumentException if the value is not valid
     */
    public static TaskStatus fromString(String value) {
        for (TaskStatus status : TaskStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid task status: " + value);
    }

    @Override
    public String toString() {
        return value;
    }
}