/**
 * Main application entry point for the Task Tracker CLI.
 * Based on Requirements 7.1, 7.4: Application-level integration and error handling
 */
public class TaskTrackerCLI {
    
    public static void main(String[] args) {
        try {
            // Initialize components
            JSONStore store = new JSONStoreImpl();
            TaskManager taskManager = new TaskManagerImpl(store);
            CLIParser cliParser = new CLIParserImpl(taskManager);
            
            // Parse and execute command
            cliParser.parseAndExecute(args);
            
        } catch (CLIException e) {
            System.err.println("CLI Error: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}