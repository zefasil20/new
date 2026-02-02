import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import java.util.*;
import java.time.Instant;

/**
 * Unit tests for JSONStoreImpl class.
 * Tests JSON storage operations and file handling.
 */
public class JSONStoreImplTest {
    
    private static final String TEST_FILE = "test_tasks.json";
    private JSONStoreImpl store;
    private Path testFilePath;
    
    @BeforeEach
    public void setUp() {
        store = new JSONStoreImpl(TEST_FILE);
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
    @DisplayName("Should return empty list when file does not exist")
    public void testLoadTasksFileNotExists() throws StorageException {
        List<Task> tasks = store.loadTasks();
        
        assertNotNull(tasks);
        assertTrue(tasks.isEmpty());
    }
    
    @Test
    @DisplayName("Should create file when it does not exist")
    public void testCreateFile() throws StorageException {
        assertFalse(store.fileExists());
        
        store.createFile();
        
        assertTrue(store.fileExists());
        assertTrue(Files.exists(testFilePath));
    }
    
    @Test
    @DisplayName("Should save and load tasks correctly (round-trip)")
    public void testSaveAndLoadTasks() throws StorageException {
        // Create test tasks
        List<Task> originalTasks = Arrays.asList(
            new Task(1, "Task 1"),
            new Task(2, "Task 2")
        );
        originalTasks.get(1).setStatus(TaskStatus.IN_PROGRESS);
        
        // Save tasks
        store.saveTasks(originalTasks);
        
        // Load tasks
        List<Task> loadedTasks = store.loadTasks();
        
        // Verify
        assertEquals(2, loadedTasks.size());
        
        Task task1 = loadedTasks.get(0);
        assertEquals(1, task1.getId());
        assertEquals("Task 1", task1.getDescription());
        assertEquals(TaskStatus.TODO, task1.getStatus());
        
        Task task2 = loadedTasks.get(1);
        assertEquals(2, task2.getId());
        assertEquals("Task 2", task2.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, task2.getStatus());
    }
    
    @Test
    @DisplayName("Should handle empty task list")
    public void testSaveAndLoadEmptyList() throws StorageException {
        List<Task> emptyTasks = new ArrayList<>();
        
        store.saveTasks(emptyTasks);
        List<Task> loadedTasks = store.loadTasks();
        
        assertNotNull(loadedTasks);
        assertTrue(loadedTasks.isEmpty());
    }
    
    @Test
    @DisplayName("Should get next ID correctly")
    public void testGetNextId() throws StorageException {
        // Initially should return 1
        assertEquals(1, store.getNextId());
        
        // After saving tasks, should return max ID + 1
        List<Task> tasks = Arrays.asList(
            new Task(1, "Task 1"),
            new Task(3, "Task 3"),
            new Task(2, "Task 2")
        );
        
        store.saveTasks(tasks);
        assertEquals(4, store.getNextId());
    }
    
    @Test
    @DisplayName("Should set next ID correctly")
    public void testSetNextId() throws StorageException {
        store.createFile();
        
        store.setNextId(5);
        assertEquals(5, store.getNextId());
        
        store.setNextId(10);
        assertEquals(10, store.getNextId());
    }
    
    @Test
    @DisplayName("Should handle tasks with special characters in description")
    public void testSpecialCharactersInDescription() throws StorageException {
        List<Task> tasks = Arrays.asList(
            new Task(1, "Task with \"quotes\" and \\backslashes"),
            new Task(2, "Task with\nnewlines\tand\ttabs")
        );
        
        store.saveTasks(tasks);
        List<Task> loadedTasks = store.loadTasks();
        
        assertEquals(2, loadedTasks.size());
        assertEquals("Task with \"quotes\" and \\backslashes", loadedTasks.get(0).getDescription());
        assertEquals("Task with\nnewlines\tand\ttabs", loadedTasks.get(1).getDescription());
    }
    
    @Test
    @DisplayName("Should preserve timestamps correctly")
    public void testTimestampPreservation() throws StorageException {
        Instant now = Instant.now();
        Instant earlier = now.minusSeconds(3600); // 1 hour earlier
        
        Task task = new Task(1, "Test task", TaskStatus.DONE, earlier, now);
        List<Task> tasks = Arrays.asList(task);
        
        store.saveTasks(tasks);
        List<Task> loadedTasks = store.loadTasks();
        
        assertEquals(1, loadedTasks.size());
        Task loadedTask = loadedTasks.get(0);
        
        assertEquals(earlier, loadedTask.getCreatedAt());
        assertEquals(now, loadedTask.getUpdatedAt());
        assertEquals(TaskStatus.DONE, loadedTask.getStatus());
    }
    
    @Test
    @DisplayName("Should handle corrupted JSON gracefully")
    public void testCorruptedJSON() throws Exception {
        // Write invalid JSON to file
        Files.writeString(testFilePath, "{ invalid json content }");
        
        // Should throw StorageException
        assertThrows(StorageException.class, () -> store.loadTasks());
    }
    
    @Test
    @DisplayName("Should handle file with empty content")
    public void testEmptyFileContent() throws Exception {
        // Create empty file
        Files.writeString(testFilePath, "");
        
        List<Task> tasks = store.loadTasks();
        assertNotNull(tasks);
        assertTrue(tasks.isEmpty());
    }
    
    @Test
    @DisplayName("Should handle file with whitespace only")
    public void testWhitespaceOnlyFile() throws Exception {
        // Create file with only whitespace
        Files.writeString(testFilePath, "   \n\t  \n  ");
        
        List<Task> tasks = store.loadTasks();
        assertNotNull(tasks);
        assertTrue(tasks.isEmpty());
    }
    
    @Test
    @DisplayName("Should maintain data consistency across multiple operations")
    public void testDataConsistency() throws StorageException {
        // Create initial tasks
        List<Task> tasks1 = Arrays.asList(new Task(1, "Task 1"));
        store.saveTasks(tasks1);
        
        // Add more tasks
        List<Task> tasks2 = Arrays.asList(
            new Task(1, "Task 1"),
            new Task(2, "Task 2")
        );
        store.saveTasks(tasks2);
        
        // Verify consistency
        List<Task> loaded = store.loadTasks();
        assertEquals(2, loaded.size());
        assertEquals(3, store.getNextId());
    }
}