import java.util.List;

/**
 * Interface for task management operations.
 * Based on Requirements 1.1-1.5, 2.1-2.4, 3.1-3.4: Core business logic for task operations
 */
public interface TaskManager {
    
    /**
     * Add a new task with the given description
     * @param description task description (non-empty)
     * @return the created task
     * @throws TaskException if description is invalid or operation fails
     */
    Task addTask(String description) throws TaskException;
    
    /**
     * Update an existing task's description
     * @param id task ID
     * @param description new description (non-empty)
     * @return the updated task
     * @throws TaskException if ID is invalid, description is invalid, or operation fails
     */
    Task updateTask(int id, String description) throws TaskException;
    
    /**
     * Delete a task by ID
     * @param id task ID
     * @throws TaskException if ID is invalid or operation fails
     */
    void deleteTask(int id) throws TaskException;
    
    /**
     * Mark a task as in-progress
     * @param id task ID
     * @return the updated task
     * @throws TaskException if ID is invalid or operation fails
     */
    Task markInProgress(int id) throws TaskException;
    
    /**
     * Mark a task as done
     * @param id task ID
     * @return the updated task
     * @throws TaskException if ID is invalid or operation fails
     */
    Task markDone(int id) throws TaskException;
    
    /**
     * List all tasks
     * @return list of all tasks
     * @throws TaskException if operation fails
     */
    List<Task> listTasks() throws TaskException;
    
    /**
     * List tasks filtered by status
     * @param status the status to filter by
     * @return list of tasks with the specified status
     * @throws TaskException if operation fails
     */
    List<Task> listTasks(TaskStatus status) throws TaskException;
    
    /**
     * Get the next available task ID
     * @return next sequential ID
     * @throws TaskException if unable to determine next ID
     */
    int getNextId() throws TaskException;
}