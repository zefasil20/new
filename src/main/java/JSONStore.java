import java.util.List;

/**
 * Interface for JSON-based task storage operations.
 * Based on Requirements 4.1-4.5: Data persistence with JSON file storage
 */
public interface JSONStore {
    
    /**
     * Load all tasks from the JSON file
     * @return list of tasks, empty list if no tasks exist
     * @throws StorageException if file operations fail
     */
    List<Task> loadTasks() throws StorageException;
    
    /**
     * Save all tasks to the JSON file
     * @param tasks list of tasks to save
     * @throws StorageException if file operations fail
     */
    void saveTasks(List<Task> tasks) throws StorageException;
    
    /**
     * Check if the JSON file exists
     * @return true if file exists, false otherwise
     */
    boolean fileExists();
    
    /**
     * Create the JSON file with empty task structure
     * @throws StorageException if file creation fails
     */
    void createFile() throws StorageException;
    
    /**
     * Get the next available task ID
     * @return next sequential ID
     * @throws StorageException if unable to determine next ID
     */
    int getNextId() throws StorageException;
    
    /**
     * Update the next available task ID
     * @param nextId the next ID to use
     * @throws StorageException if unable to update next ID
     */
    void setNextId(int nextId) throws StorageException;
}