/**
 * Interface for command-line argument parsing and processing.
 * Based on Requirements 5.1-5.5: Command-line interface operations
 */
public interface CLIParser {
    
    /**
     * Parse command-line arguments and execute the corresponding operation
     * @param args command-line arguments
     * @throws CLIException if arguments are invalid or operation fails
     */
    void parseAndExecute(String[] args) throws CLIException;
    
    /**
     * Display help information with available commands
     */
    void displayHelp();
    
    /**
     * Display error message with usage information
     * @param message error message to display
     */
    void displayError(String message);
    
    /**
     * Display success message for completed operations
     * @param message success message to display
     */
    void displaySuccess(String message);
}