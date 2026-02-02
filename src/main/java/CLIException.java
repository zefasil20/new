/**
 * Exception thrown when CLI parsing or execution fails.
 * Based on Requirements 5.2, 7.1: Error handling for command-line operations
 */
public class CLIException extends Exception {
    
    public CLIException(String message) {
        super(message);
    }
    
    public CLIException(String message, Throwable cause) {
        super(message, cause);
    }
}