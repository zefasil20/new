/**
 * Exception thrown when storage operations fail.
 * Based on Requirements 4.4, 7.1: Error handling for file system operations
 */
public class StorageException extends Exception {
    
    public StorageException(String message) {
        super(message);
    }
    
    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}