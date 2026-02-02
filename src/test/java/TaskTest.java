import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.time.Instant;

/**
 * Unit tests for Task class.
 * Tests the basic functionality of the Task data model.
 */
public class TaskTest {

    @Test
    @DisplayName("Should create task with valid description")
    public void testTaskCreationValid() {
        Task task = new Task(1, "Buy groceries");
        
        assertEquals(1, task.getId());
        assertEquals("Buy groceries", task.getDescription());
        assertEquals(TaskStatus.TODO, task.getStatus());
        assertNotNull(task.getCreatedAt());
        assertNotNull(task.getUpdatedAt());
    }

    @Test
    @DisplayName("Should trim whitespace from description")
    public void testDescriptionTrimming() {
        Task task = new Task(1, "  Buy groceries  ");
        assertEquals("Buy groceries", task.getDescription());
    }

    @Test
    @DisplayName("Should reject empty description")
    public void testEmptyDescription() {
        assertThrows(IllegalArgumentException.class, () -> new Task(1, ""));
        assertThrows(IllegalArgumentException.class, () -> new Task(1, "   "));
        assertThrows(IllegalArgumentException.class, () -> new Task(1, null));
    }

    @Test
    @DisplayName("Should update description and timestamp")
    public void testDescriptionUpdate() {
        Task task = new Task(1, "Original description");
        Instant originalUpdatedAt = task.getUpdatedAt();
        
        // Small delay to ensure timestamp difference
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            // Ignore
        }
        
        task.setDescription("Updated description");
        
        assertEquals("Updated description", task.getDescription());
        assertTrue(task.getUpdatedAt().isAfter(originalUpdatedAt));
    }

    @Test
    @DisplayName("Should update status and timestamp")
    public void testStatusUpdate() {
        Task task = new Task(1, "Test task");
        Instant originalUpdatedAt = task.getUpdatedAt();
        
        // Small delay to ensure timestamp difference
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            // Ignore
        }
        
        task.setStatus(TaskStatus.IN_PROGRESS);
        
        assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());
        assertTrue(task.getUpdatedAt().isAfter(originalUpdatedAt));
    }

    @Test
    @DisplayName("Should have correct equality based on ID")
    public void testEquality() {
        Task task1 = new Task(1, "Task 1");
        Task task2 = new Task(1, "Task 1 different description");
        Task task3 = new Task(2, "Task 2");
        
        assertEquals(task1, task2); // Same ID
        assertNotEquals(task1, task3); // Different ID
        assertEquals(task1.hashCode(), task2.hashCode()); // Same hash for same ID
    }
}