import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import java.util.*;

/**
 * Unit tests for TaskManagerImpl class.
 * Tests business logic and CRUD operations.
 */
public class TaskManagerImplTest {
    
    private static final String TEST_FILE = "test_manager_tasks.json";
    private TaskManagerImpl taskManager;
    private JSONStoreImpl store;
    private Path testFilePath;
    
    @BeforeEach
    public void setUp() {
        store = new JSONStoreImpl(TEST_FILE);
        taskManager = new TaskManagerImpl(store);
        testFilePath = Paths.get(TEST_FILE);
        
        // Clean up any existing test file
        try {
            Files.deleteIfExists(testFilePath);
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }
    
    @AfterEach
    public void tearDown() {
        // Clean up test file
        try {
            Files.deleteIfExists(testFilePath);
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }
    
    @Test
    @DisplayName("Should add task with unique ID assignment")
    public void testAddTask() throws TaskException {
        Task task1 = taskManager.addTask("First task");
        Task task2 = taskManager.addTask("Second task");
        
        assertEquals(1, task1.getId());
        assertEquals("First task", task1.getDescription());
        assertEquals(TaskStatus.TODO, task1.getStatus());
        
        assertEquals(2, task2.getId());
        assertEquals("Second task", task2.getDescription());
        assertEquals(TaskStatus.TODO, task2.getStatus());
        
        // Verify tasks are persisted
        List<Task> tasks = taskManager.listTasks();
        assertEquals(2, tasks.size());
    }
    
    @Test
    @DisplayName("Should reject empty description when adding task")
    public void testAddTaskEmptyDescription() {
        assertThrows(TaskException.class, () -> taskManager.addTask(""));
        assertThrows(TaskException.class, () -> taskManager.addTask("   "));
        assertThrows(TaskException.class, () -> taskManager.addTask(null));
    }
    
    @Test
    @DisplayName("Should update task description and timestamp")
    public void testUpdateTask() throws TaskException {
        Task originalTask = taskManager.addTask("Original description");
        
        // Small delay to ensure timestamp difference
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            // Ignore
        }
        
        Task updatedTask = taskManager.updateTask(originalTask.getId(), "Updated description");
        
        assertEquals(originalTask.getId(), updatedTask.getId());
        assertEquals("Updated description", updatedTask.getDescription());
        assertTrue(updatedTask.getUpdatedAt().isAfter(originalTask.getUpdatedAt()));
        
        // Verify task is persisted
        List<Task> tasks = taskManager.listTasks();
        assertEquals(1, tasks.size());
        assertEquals("Updated description", tasks.get(0).getDescription());
    }
    
    @Test
    @DisplayName("Should reject invalid ID when updating task")
    public void testUpdateTaskInvalidId() throws TaskException {
        taskManager.addTask("Test task");
        
        assertThrows(TaskException.class, () -> taskManager.updateTask(999, "New description"));
    }
    
    @Test
    @DisplayName("Should reject empty description when updating task")
    public void testUpdateTaskEmptyDescription() throws TaskException {
        Task task = taskManager.addTask("Test task");
        
        assertThrows(TaskException.class, () -> taskManager.updateTask(task.getId(), ""));
        assertThrows(TaskException.class, () -> taskManager.updateTask(task.getId(), "   "));
        assertThrows(TaskException.class, () -> taskManager.updateTask(task.getId(), null));
    }
    
    @Test
    @DisplayName("Should delete task completely")
    public void testDeleteTask() throws TaskException {
        Task task1 = taskManager.addTask("Task 1");
        Task task2 = taskManager.addTask("Task 2");
        Task task3 = taskManager.addTask("Task 3");
        
        // Delete middle task
        taskManager.deleteTask(task2.getId());
        
        List<Task> remainingTasks = taskManager.listTasks();
        assertEquals(2, remainingTasks.size());
        
        // Verify correct tasks remain
        List<Integer> remainingIds = Arrays.asList(
            remainingTasks.get(0).getId(),
            remainingTasks.get(1).getId()
        );
        assertTrue(remainingIds.contains(task1.getId()));
        assertTrue(remainingIds.contains(task3.getId()));
        assertFalse(remainingIds.contains(task2.getId()));
    }
    
    @Test
    @DisplayName("Should reject invalid ID when deleting task")
    public void testDeleteTaskInvalidId() throws TaskException {
        taskManager.addTask("Test task");
        
        assertThrows(TaskException.class, () -> taskManager.deleteTask(999));
    }
    
    @Test
    @DisplayName("Should mark task as in-progress and update timestamp")
    public void testMarkInProgress() throws TaskException {
        Task task = taskManager.addTask("Test task");
        
        // Small delay to ensure timestamp difference
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            // Ignore
        }
        
        Task updatedTask = taskManager.markInProgress(task.getId());
        
        assertEquals(TaskStatus.IN_PROGRESS, updatedTask.getStatus());
        assertTrue(updatedTask.getUpdatedAt().isAfter(task.getUpdatedAt()));
        
        // Verify task is persisted
        List<Task> tasks = taskManager.listTasks();
        assertEquals(TaskStatus.IN_PROGRESS, tasks.get(0).getStatus());
    }
    
    @Test
    @DisplayName("Should mark task as done and update timestamp")
    public void testMarkDone() throws TaskException {
        Task task = taskManager.addTask("Test task");
        
        // Small delay to ensure timestamp difference
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            // Ignore
        }
        
        Task updatedTask = taskManager.markDone(task.getId());
        
        assertEquals(TaskStatus.DONE, updatedTask.getStatus());
        assertTrue(updatedTask.getUpdatedAt().isAfter(task.getUpdatedAt()));
        
        // Verify task is persisted
        List<Task> tasks = taskManager.listTasks();
        assertEquals(TaskStatus.DONE, tasks.get(0).getStatus());
    }
    
    @Test
    @DisplayName("Should reject invalid ID for status operations")
    public void testStatusOperationsInvalidId() throws TaskException {
        taskManager.addTask("Test task");
        
        assertThrows(TaskException.class, () -> taskManager.markInProgress(999));
        assertThrows(TaskException.class, () -> taskManager.markDone(999));
    }
    
    @Test
    @DisplayName("Should list all tasks accurately")
    public void testListAllTasks() throws TaskException {
        Task task1 = taskManager.addTask("Task 1");
        Task task2 = taskManager.addTask("Task 2");
        taskManager.markInProgress(task2.getId());
        Task task3 = taskManager.addTask("Task 3");
        taskManager.markDone(task3.getId());
        
        List<Task> allTasks = taskManager.listTasks();
        assertEquals(3, allTasks.size());
        
        // Verify all tasks are present
        Set<Integer> taskIds = allTasks.stream()
                .map(Task::getId)
                .collect(java.util.stream.Collectors.toSet());
        assertTrue(taskIds.contains(task1.getId()));
        assertTrue(taskIds.contains(task2.getId()));
        assertTrue(taskIds.contains(task3.getId()));
    }
    
    @Test
    @DisplayName("Should list tasks filtered by status")
    public void testListTasksByStatus() throws TaskException {
        Task todoTask = taskManager.addTask("TODO task");
        Task inProgressTask = taskManager.addTask("In Progress task");
        taskManager.markInProgress(inProgressTask.getId());
        Task doneTask = taskManager.addTask("Done task");
        taskManager.markDone(doneTask.getId());
        
        // Test TODO filter
        List<Task> todoTasks = taskManager.listTasks(TaskStatus.TODO);
        assertEquals(1, todoTasks.size());
        assertEquals(todoTask.getId(), todoTasks.get(0).getId());
        
        // Test IN_PROGRESS filter
        List<Task> inProgressTasks = taskManager.listTasks(TaskStatus.IN_PROGRESS);
        assertEquals(1, inProgressTasks.size());
        assertEquals(inProgressTask.getId(), inProgressTasks.get(0).getId());
        
        // Test DONE filter
        List<Task> doneTasks = taskManager.listTasks(TaskStatus.DONE);
        assertEquals(1, doneTasks.size());
        assertEquals(doneTask.getId(), doneTasks.get(0).getId());
    }
    
    @Test
    @DisplayName("Should handle empty task list scenarios")
    public void testEmptyTaskList() throws TaskException {
        List<Task> allTasks = taskManager.listTasks();
        assertTrue(allTasks.isEmpty());
        
        List<Task> todoTasks = taskManager.listTasks(TaskStatus.TODO);
        assertTrue(todoTasks.isEmpty());
        
        List<Task> inProgressTasks = taskManager.listTasks(TaskStatus.IN_PROGRESS);
        assertTrue(inProgressTasks.isEmpty());
        
        List<Task> doneTasks = taskManager.listTasks(TaskStatus.DONE);
        assertTrue(doneTasks.isEmpty());
    }
    
    @Test
    @DisplayName("Should get correct next ID")
    public void testGetNextId() throws TaskException {
        assertEquals(1, taskManager.getNextId());
        
        taskManager.addTask("Task 1");
        assertEquals(2, taskManager.getNextId());
        
        taskManager.addTask("Task 2");
        assertEquals(3, taskManager.getNextId());
        
        // Delete a task - next ID should still be 3
        taskManager.deleteTask(1);
        assertEquals(3, taskManager.getNextId());
    }
    
    @Test
    @DisplayName("Should maintain data consistency across operations")
    public void testDataConsistency() throws TaskException {
        // Add tasks
        Task task1 = taskManager.addTask("Task 1");
        Task task2 = taskManager.addTask("Task 2");
        
        // Update task
        taskManager.updateTask(task1.getId(), "Updated Task 1");
        
        // Change status
        taskManager.markInProgress(task2.getId());
        
        // Verify consistency
        List<Task> tasks = taskManager.listTasks();
        assertEquals(2, tasks.size());
        
        Task retrievedTask1 = tasks.stream()
                .filter(t -> t.getId() == task1.getId())
                .findFirst()
                .orElse(null);
        assertNotNull(retrievedTask1);
        assertEquals("Updated Task 1", retrievedTask1.getDescription());
        
        Task retrievedTask2 = tasks.stream()
                .filter(t -> t.getId() == task2.getId())
                .findFirst()
                .orElse(null);
        assertNotNull(retrievedTask2);
        assertEquals(TaskStatus.IN_PROGRESS, retrievedTask2.getStatus());
    }
}