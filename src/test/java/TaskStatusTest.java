import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TaskStatus enum.
 * Tests the basic functionality of the TaskStatus enumeration.
 */
public class TaskStatusTest {

    @Test
    @DisplayName("Should create TaskStatus with correct string values")
    public void testTaskStatusValues() {
        assertEquals("todo", TaskStatus.TODO.getValue());
        assertEquals("in-progress", TaskStatus.IN_PROGRESS.getValue());
        assertEquals("done", TaskStatus.DONE.getValue());
    }

    @Test
    @DisplayName("Should parse valid string values to TaskStatus")
    public void testFromStringValidValues() {
        assertEquals(TaskStatus.TODO, TaskStatus.fromString("todo"));
        assertEquals(TaskStatus.IN_PROGRESS, TaskStatus.fromString("in-progress"));
        assertEquals(TaskStatus.DONE, TaskStatus.fromString("done"));
    }

    @Test
    @DisplayName("Should throw exception for invalid string values")
    public void testFromStringInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> TaskStatus.fromString("invalid"));
        assertThrows(IllegalArgumentException.class, () -> TaskStatus.fromString(""));
        assertThrows(IllegalArgumentException.class, () -> TaskStatus.fromString(null));
    }

    @Test
    @DisplayName("Should return correct string representation")
    public void testToString() {
        assertEquals("todo", TaskStatus.TODO.toString());
        assertEquals("in-progress", TaskStatus.IN_PROGRESS.toString());
        assertEquals("done", TaskStatus.DONE.toString());
    }
}