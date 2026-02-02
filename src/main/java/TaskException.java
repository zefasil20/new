/**
 * Exception thrown when task operations fail.
 * Based on Requirements 1.4, 1.5, 2.3, 7.1: Error handling for task operations
 */
public class TaskException extends Exception {
    
    public TaskException(String message) {
        super(message);
    }
    
    public TaskException(String message, Throwable cause) {
        super(message, cause);
    }
}