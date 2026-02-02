import java.time.Instant;

/**
 * Task data model representing a single task item.
 * Based on Requirements 6.1-6.6: Task structure with unique ID, description, status, and timestamps
 */
public class Task {
    private int id;
    private String description;
    private TaskStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    /**
     * Constructor for creating a new task
     * @param id unique sequential integer ID
     * @param description task description (non-empty)
     */
    public Task(int id, String description) {
        this.id = id;
        this.description = validateAndTrimDescription(description);
        this.status = TaskStatus.TODO; // Default status per Requirement 2.4
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Constructor for loading existing tasks from storage
     */
    public Task(int id, String description, TaskStatus status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.description = validateAndTrimDescription(description);
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Validates and trims task description
     * @param description the description to validate
     * @return trimmed description
     * @throws IllegalArgumentException if description is empty or whitespace-only
     */
    private String validateAndTrimDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Task description cannot be empty or whitespace-only");
        }
        return description.trim();
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    // Setters with validation
    public void setDescription(String description) {
        this.description = validateAndTrimDescription(description);
        this.updatedAt = Instant.now();
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
        this.updatedAt = Instant.now();
    }

    @Override
    public String toString() {
        return String.format("Task{id=%d, description='%s', status=%s, createdAt=%s, updatedAt=%s}",
                id, description, status, createdAt, updatedAt);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Task task = (Task) obj;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}