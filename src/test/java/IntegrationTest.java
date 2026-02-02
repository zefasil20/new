import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import java.nio.file.*;

/**
 * Integration tests for complete application flows.
 * Tests end-to-end functionality and data persistence.
 */
public class IntegrationTest {
    
    private static final String TEST_FILE = "integration_test_tasks.json";
    private Path testFilePath;
    private ByteArrayOutputStream outputStream;
    private ByteArrayOutputStream errorStream;
    private PrintStream originalOut;
    private PrintStream originalErr;
    
    @BeforeEach
    public void setUp() {
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
    @DisplayName("Should handle complete task lifecycle")
    public void testCompleteTaskLifecycle() {
        // Create components with test file
        JSONStore store = new JSONStoreImpl(TEST_FILE);
        TaskManager taskManager = new TaskManagerImpl(store);
        CLIParser cliParser = new CLIParserImpl(taskManager);
        
        try {
            // Add a task
            cliParser.parseAndExecute(new String[]{"add", "Complete project"});
            String output1 = outputStream.toString();
            assertTrue(output1.contains("Task added successfully (ID: 1)"));
            outputStream.reset();
            
            // List tasks
            cliParser.parseAndExecute(new String[]{"list"});
            String output2 = outputStream.toString();
            assertTrue(output2.contains("Complete project"));
            assertTrue(output2.contains("todo"));
            outputStream.reset();
            
            // Mark in progress
            cliParser.parseAndExecute(new String[]{"mark-in-progress", "1"});
            String output3 = outputStream.toString();
            assertTrue(output3.contains("Task 1 marked as in-progress"));
            outputStream.reset();
            
            // Update description
            cliParser.parseAndExecute(new String[]{"update", "1", "Complete project with tests"});
            String output4 = outputStream.toString();
            assertTrue(output4.contains("Task 1 updated successfully"));
            outputStream.reset();
            
            // Mark done
            cliParser.parseAndExecute(new String[]{"mark-done", "1"});
            String output5 = outputStream.toString();
            assertTrue(output5.contains("Task 1 marked as done"));
            outputStream.reset();
            
            // List done tasks
            cliParser.parseAndExecute(new String[]{"list", "done"});
            String output6 = outputStream.toString();
            assertTrue(output6.contains("Complete project with tests"));
            assertTrue(output6.contains("done"));
            
        } catch (Exception e) {
            fail("Integration test failed: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("Should persist data across application restarts")
    public void testDataPersistenceAcrossRestarts() {
        try {
            // First session - add tasks
            {
                JSONStore store1 = new JSONStoreImpl(TEST_FILE);
                TaskManager taskManager1 = new TaskManagerImpl(store1);
                CLIParser cliParser1 = new CLIParserImpl(taskManager1);
                
                cliParser1.parseAndExecute(new String[]{"add", "Persistent task 1"});
                cliParser1.parseAndExecute(new String[]{"add", "Persistent task 2"});
                cliParser1.parseAndExecute(new String[]{"mark-in-progress", "2"});
            }
            
            outputStream.reset();
            
            // Second session - verify data persisted
            {
                JSONStore store2 = new JSONStoreImpl(TEST_FILE);
                TaskManager taskManager2 = new TaskManagerImpl(store2);
                CLIParser cliParser2 = new CLIParserImpl(taskManager2);
                
                cliParser2.parseAndExecute(new String[]{"list"});
                String output = outputStream.toString();
                
                assertTrue(output.contains("Persistent task 1"));
                assertTrue(output.contains("Persistent task 2"));
                assertTrue(output.contains("todo"));
                assertTrue(output.contains("in-progress"));
                assertTrue(output.contains("Total: 2 task(s)"));
            }
            
        } catch (Exception e) {
            fail("Data persistence test failed: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("Should handle multiple tasks with different statuses")
    public void testMultipleTasksWithFiltering() {
        JSONStore store = new JSONStoreImpl(TEST_FILE);
        TaskManager taskManager = new TaskManagerImpl(store);
        CLIParser cliParser = new CLIParserImpl(taskManager);
        
        try {
            // Add multiple tasks
            cliParser.parseAndExecute(new String[]{"add", "TODO task 1"});
            cliParser.parseAndExecute(new String[]{"add", "TODO task 2"});
            cliParser.parseAndExecute(new String[]{"add", "Progress task"});
            cliParser.parseAndExecute(new String[]{"add", "Done task"});
            
            // Change statuses
            cliParser.parseAndExecute(new String[]{"mark-in-progress", "3"});
            cliParser.parseAndExecute(new String[]{"mark-done", "4"});
            
            outputStream.reset();
            
            // Test filtering
            cliParser.parseAndExecute(new String[]{"list", "todo"});
            String todoOutput = outputStream.toString();
            assertTrue(todoOutput.contains("TODO task 1"));
            assertTrue(todoOutput.contains("TODO task 2"));
            assertFalse(todoOutput.contains("Progress task"));
            assertFalse(todoOutput.contains("Done task"));
            assertTrue(todoOutput.contains("Total: 2 task(s)"));
            
            outputStream.reset();
            
            cliParser.parseAndExecute(new String[]{"list", "in-progress"});
            String progressOutput = outputStream.toString();
            assertTrue(progressOutput.contains("Progress task"));
            assertTrue(progressOutput.contains("Total: 1 task(s)"));
            
            outputStream.reset();
            
            cliParser.parseAndExecute(new String[]{"list", "done"});
            String doneOutput = outputStream.toString();
            assertTrue(doneOutput.contains("Done task"));
            assertTrue(doneOutput.contains("Total: 1 task(s)"));
            
        } catch (Exception e) {
            fail("Multiple tasks test failed: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("Should handle task deletion and ID consistency")
    public void testTaskDeletionAndIdConsistency() {
        JSONStore store = new JSONStoreImpl(TEST_FILE);
        TaskManager taskManager = new TaskManagerImpl(store);
        CLIParser cliParser = new CLIParserImpl(taskManager);
        
        try {
            // Add tasks
            cliParser.parseAndExecute(new String[]{"add", "Task 1"});
            cliParser.parseAndExecute(new String[]{"add", "Task 2"});
            cliParser.parseAndExecute(new String[]{"add", "Task 3"});
            
            // Delete middle task
            cliParser.parseAndExecute(new String[]{"delete", "2"});
            
            outputStream.reset();
            
            // Verify remaining tasks
            cliParser.parseAndExecute(new String[]{"list"});
            String output = outputStream.toString();
            
            assertTrue(output.contains("Task 1"));
            assertFalse(output.contains("Task 2"));
            assertTrue(output.contains("Task 3"));
            assertTrue(output.contains("Total: 2 task(s)"));
            
            outputStream.reset();
            
            // Add new task - should get next available ID
            cliParser.parseAndExecute(new String[]{"add", "Task 4"});
            String addOutput = outputStream.toString();
            assertTrue(addOutput.contains("Task added successfully (ID: 4)"));
            
        } catch (Exception e) {
            fail("Task deletion test failed: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("Should handle error scenarios gracefully")
    public void testErrorHandling() {
        JSONStore store = new JSONStoreImpl(TEST_FILE);
        TaskManager taskManager = new TaskManagerImpl(store);
        CLIParser cliParser = new CLIParserImpl(taskManager);
        
        try {
            // Test invalid operations
            cliParser.parseAndExecute(new String[]{"update", "999", "Non-existent task"});
            fail("Should have thrown CLIException for non-existent task");
        } catch (CLIException e) {
            assertTrue(e.getMessage().contains("Task operation failed"));
        }
        
        try {
            cliParser.parseAndExecute(new String[]{"delete", "999"});
            fail("Should have thrown CLIException for non-existent task");
        } catch (CLIException e) {
            assertTrue(e.getMessage().contains("Task operation failed"));
        }
        
        try {
            cliParser.parseAndExecute(new String[]{"mark-done", "999"});
            fail("Should have thrown CLIException for non-existent task");
        } catch (CLIException e) {
            assertTrue(e.getMessage().contains("Task operation failed"));
        }
    }
    
    @Test
    @DisplayName("Should handle file system operations correctly")
    public void testFileSystemOperations() {
        JSONStore store = new JSONStoreImpl(TEST_FILE);
        TaskManager taskManager = new TaskManagerImpl(store);
        CLIParser cliParser = new CLIParserImpl(taskManager);
        
        try {
            // Initially file should not exist
            assertFalse(Files.exists(testFilePath));
            
            // Add task should create file
            cliParser.parseAndExecute(new String[]{"add", "First task"});
            assertTrue(Files.exists(testFilePath));
            
            // Verify file content is valid JSON
            String content = Files.readString(testFilePath);
            assertTrue(content.contains("\"tasks\""));
            assertTrue(content.contains("\"nextId\""));
            assertTrue(content.contains("First task"));
            
        } catch (Exception e) {
            fail("File system operations test failed: " + e.getMessage());
        }
    }
}