import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import java.nio.file.*;

/**
 * Unit tests for CLIParserImpl class.
 * Tests command parsing and execution.
 */
public class CLIParserImplTest {
    
    private static final String TEST_FILE = "test_cli_tasks.json";
    private CLIParserImpl cliParser;
    private TaskManagerImpl taskManager;
    private JSONStoreImpl store;
    private Path testFilePath;
    private ByteArrayOutputStream outputStream;
    private ByteArrayOutputStream errorStream;
    private PrintStream originalOut;
    private PrintStream originalErr;
    
    @BeforeEach
    public void setUp() {
        store = new JSONStoreImpl(TEST_FILE);
        taskManager = new TaskManagerImpl(store);
        cliParser = new CLIParserImpl(taskManager);
        testFilePath = Paths.get(TEST_FILE);
        
        // Capture output streams
        outputStream = new ByteArrayOutputStream();
        errorStream = new ByteArrayOutputStream();
        originalOut = System.out;
        originalErr = System.err;
        System.setOut(new PrintStream(outputStream));
        System.setErr(new PrintStream(errorStream));
        
        // Clean up any existing test file
        try {
            Files.deleteIfExists(testFilePath);
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }
    
    @AfterEach
    public void tearDown() {
        // Restore output streams
        System.setOut(originalOut);
        System.setErr(originalErr);
        
        // Clean up test file
        try {
            Files.deleteIfExists(testFilePath);
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }
    
    @Test
    @DisplayName("Should display help when no arguments provided")
    public void testNoArguments() throws CLIException {
        cliParser.parseAndExecute(new String[]{});
        
        String output = outputStream.toString();
        assertTrue(output.contains("Task Tracker CLI"));
        assertTrue(output.contains("Usage:"));
        assertTrue(output.contains("Available commands:"));
    }
    
    @Test
    @DisplayName("Should parse add command correctly")
    public void testAddCommand() throws CLIException {
        cliParser.parseAndExecute(new String[]{"add", "Test task"});
        
        String output = outputStream.toString();
        assertTrue(output.contains("Task added successfully"));
        assertTrue(output.contains("ID: 1"));
    }
    
    @Test
    @DisplayName("Should parse add command with multiple words")
    public void testAddCommandMultipleWords() throws CLIException {
        cliParser.parseAndExecute(new String[]{"add", "Buy", "groceries", "and", "cook", "dinner"});
        
        String output = outputStream.toString();
        assertTrue(output.contains("Task added successfully"));
    }
    
    @Test
    @DisplayName("Should handle add command with missing description")
    public void testAddCommandMissingDescription() {
        assertThrows(CLIException.class, () -> cliParser.parseAndExecute(new String[]{"add"}));
    }
    
    @Test
    @DisplayName("Should parse update command correctly")
    public void testUpdateCommand() throws CLIException {
        // First add a task
        cliParser.parseAndExecute(new String[]{"add", "Original task"});
        outputStream.reset(); // Clear output
        
        // Then update it
        cliParser.parseAndExecute(new String[]{"update", "1", "Updated task"});
        
        String output = outputStream.toString();
        assertTrue(output.contains("Task 1 updated successfully"));
    }
    
    @Test
    @DisplayName("Should handle update command with invalid ID")
    public void testUpdateCommandInvalidId() {
        assertThrows(CLIException.class, () -> cliParser.parseAndExecute(new String[]{"update", "abc", "New description"}));
    }
    
    @Test
    @DisplayName("Should handle update command with missing arguments")
    public void testUpdateCommandMissingArguments() {
        assertThrows(CLIException.class, () -> cliParser.parseAndExecute(new String[]{"update", "1"}));
        assertThrows(CLIException.class, () -> cliParser.parseAndExecute(new String[]{"update"}));
    }
    
    @Test
    @DisplayName("Should parse delete command correctly")
    public void testDeleteCommand() throws CLIException {
        // First add a task
        cliParser.parseAndExecute(new String[]{"add", "Task to delete"});
        outputStream.reset(); // Clear output
        
        // Then delete it
        cliParser.parseAndExecute(new String[]{"delete", "1"});
        
        String output = outputStream.toString();
        assertTrue(output.contains("Task 1 deleted successfully"));
    }
    
    @Test
    @DisplayName("Should handle delete command with invalid ID")
    public void testDeleteCommandInvalidId() {
        assertThrows(CLIException.class, () -> cliParser.parseAndExecute(new String[]{"delete", "abc"}));
    }
    
    @Test
    @DisplayName("Should handle delete command with wrong argument count")
    public void testDeleteCommandWrongArguments() {
        assertThrows(CLIException.class, () -> cliParser.parseAndExecute(new String[]{"delete"}));
        assertThrows(CLIException.class, () -> cliParser.parseAndExecute(new String[]{"delete", "1", "2"}));
    }
    
    @Test
    @DisplayName("Should parse mark-in-progress command correctly")
    public void testMarkInProgressCommand() throws CLIException {
        // First add a task
        cliParser.parseAndExecute(new String[]{"add", "Task to mark"});
        outputStream.reset(); // Clear output
        
        // Then mark it in progress
        cliParser.parseAndExecute(new String[]{"mark-in-progress", "1"});
        
        String output = outputStream.toString();
        assertTrue(output.contains("Task 1 marked as in-progress"));
    }
    
    @Test
    @DisplayName("Should parse mark-done command correctly")
    public void testMarkDoneCommand() throws CLIException {
        // First add a task
        cliParser.parseAndExecute(new String[]{"add", "Task to complete"});
        outputStream.reset(); // Clear output
        
        // Then mark it done
        cliParser.parseAndExecute(new String[]{"mark-done", "1"});
        
        String output = outputStream.toString();
        assertTrue(output.contains("Task 1 marked as done"));
    }
    
    @Test
    @DisplayName("Should parse list command without filter")
    public void testListCommandAll() throws CLIException {
        // Add some tasks
        cliParser.parseAndExecute(new String[]{"add", "Task 1"});
        cliParser.parseAndExecute(new String[]{"add", "Task 2"});
        outputStream.reset(); // Clear output
        
        // List all tasks
        cliParser.parseAndExecute(new String[]{"list"});
        
        String output = outputStream.toString();
        assertTrue(output.contains("Tasks:"));
        assertTrue(output.contains("Task 1"));
        assertTrue(output.contains("Task 2"));
        assertTrue(output.contains("Total: 2 task(s)"));
    }
    
    @Test
    @DisplayName("Should parse list command with status filter")
    public void testListCommandWithFilter() throws CLIException {
        // Add tasks and change status
        cliParser.parseAndExecute(new String[]{"add", "TODO task"});
        cliParser.parseAndExecute(new String[]{"add", "In progress task"});
        cliParser.parseAndExecute(new String[]{"mark-in-progress", "2"});
        outputStream.reset(); // Clear output
        
        // List only TODO tasks
        cliParser.parseAndExecute(new String[]{"list", "todo"});
        
        String output = outputStream.toString();
        assertTrue(output.contains("TODO task"));
        assertFalse(output.contains("In progress task"));
        assertTrue(output.contains("Total: 1 task(s)"));
    }
    
    @Test
    @DisplayName("Should handle list command with invalid status")
    public void testListCommandInvalidStatus() {
        assertThrows(CLIException.class, () -> cliParser.parseAndExecute(new String[]{"list", "invalid"}));
    }
    
    @Test
    @DisplayName("Should handle list command with too many arguments")
    public void testListCommandTooManyArguments() {
        assertThrows(CLIException.class, () -> cliParser.parseAndExecute(new String[]{"list", "todo", "extra"}));
    }
    
    @Test
    @DisplayName("Should handle unknown command")
    public void testUnknownCommand() throws CLIException {
        cliParser.parseAndExecute(new String[]{"unknown"});
        
        String errorOutput = errorStream.toString();
        assertTrue(errorOutput.contains("Unknown command: unknown"));
        
        String output = outputStream.toString();
        assertTrue(output.contains("Task Tracker CLI")); // Help should be displayed
    }
    
    @Test
    @DisplayName("Should display empty list message when no tasks")
    public void testEmptyTaskList() throws CLIException {
        cliParser.parseAndExecute(new String[]{"list"});
        
        String output = outputStream.toString();
        assertTrue(output.contains("No tasks found"));
    }
    
    @Test
    @DisplayName("Should handle task operation errors gracefully")
    public void testTaskOperationError() {
        // Try to update non-existent task
        assertThrows(CLIException.class, () -> cliParser.parseAndExecute(new String[]{"update", "999", "New description"}));
    }
}