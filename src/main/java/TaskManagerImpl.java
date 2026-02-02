import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of TaskManager interface for task business logic.
 * Based on Requirements 1.1-1.5, 2.1-2.4, 3.1-3.4: Core business logic for task operations
 */
public class TaskManagerImpl implements TaskManager {
    
    private final JSONStore store;
    
    public TaskManagerImpl(JSONStore store) {
        this.store = store;
    }
    
    @Override
    public Task addTask(String description) throws TaskException {
        try {
            // Validate description
            if (description == null || description.trim().isEmpty()) {
                throw new TaskException("Task description cannot be empty or whitespace-only");
            }
            
            // Get next available ID
            int nextId = store.getNextId();
            
            // Create new task
            Task newTask = new Task(nextId, description.trim());
            
            // Load existing tasks
            List<Task> tasks = store.loadTasks();
            
            // Add new task
            tasks.add(newTask);
            
            // Save updated tasks
            store.saveTasks(tasks);
            
            // Update next ID
            store.setNextId(nextId + 1);
            
            return newTask;
            
        } catch (StorageException e) {
            throw new TaskException("Failed to add task: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Task updateTask(int id, String description) throws TaskException {
        try {
            // Validate description
            if (description == null || description.trim().isEmpty()) {
                throw new TaskException("Task description cannot be empty or whitespace-only");
            }
            
            // Load existing tasks
            List<Task> tasks = store.loadTasks();
            
            // Find task by ID
            Task taskToUpdate = null;
            for (Task task : tasks) {
                if (task.getId() == id) {
                    taskToUpdate = task;
                    break;
                }
            }
            
            if (taskToUpdate == null) {
                throw new TaskException("Task with ID " + id + " not found");
            }
            
            // Update task description (this also updates the timestamp)
            taskToUpdate.setDescription(description.trim());
            
            // Save updated tasks
            store.saveTasks(tasks);
            
            return taskToUpdate;
            
        } catch (StorageException e) {
            throw new TaskException("Failed to update task: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void deleteTask(int id) throws TaskException {
        try {
            // Load existing tasks
            List<Task> tasks = store.loadTasks();
            
            // Find and remove task by ID
            boolean removed = tasks.removeIf(task -> task.getId() == id);
            
            if (!removed) {
                throw new TaskException("Task with ID " + id + " not found");
            }
            
            // Save updated tasks
            store.saveTasks(tasks);
            
        } catch (StorageException e) {
            throw new TaskException("Failed to delete task: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Task markInProgress(int id) throws TaskException {
        return updateTaskStatus(id, TaskStatus.IN_PROGRESS);
    }
    
    @Override
    public Task markDone(int id) throws TaskException {
        return updateTaskStatus(id, TaskStatus.DONE);
    }
    
    @Override
    public List<Task> listTasks() throws TaskException {
        try {
            return store.loadTasks();
        } catch (StorageException e) {
            throw new TaskException("Failed to list tasks: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<Task> listTasks(TaskStatus status) throws TaskException {
        try {
            List<Task> allTasks = store.loadTasks();
            return allTasks.stream()
                    .filter(task -> task.getStatus() == status)
                    .collect(Collectors.toList());
        } catch (StorageException e) {
            throw new TaskException("Failed to list tasks by status: " + e.getMessage(), e);
        }
    }
    
    @Override
    public int getNextId() throws TaskException {
        try {
            return store.getNextId();
        } catch (StorageException e) {
            throw new TaskException("Failed to get next ID: " + e.getMessage(), e);
        }
    }
    
    /**
     * Helper method to update task status
     */
    private Task updateTaskStatus(int id, TaskStatus newStatus) throws TaskException {
        try {
            // Load existing tasks
            List<Task> tasks = store.loadTasks();
            
            // Find task by ID
            Task taskToUpdate = null;
            for (Task task : tasks) {
                if (task.getId() == id) {
                    taskToUpdate = task;
                    break;
                }
            }
            
            if (taskToUpdate == null) {
                throw new TaskException("Task with ID " + id + " not found");
            }
            
            // Update task status (this also updates the timestamp)
            taskToUpdate.setStatus(newStatus);
            
            // Save updated tasks
            store.saveTasks(tasks);
            
            return taskToUpdate;
            
        } catch (StorageException e) {
            throw new TaskException("Failed to update task status: " + e.getMessage(), e);
        }
    }
}