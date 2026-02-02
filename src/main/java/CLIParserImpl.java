import java.util.List;

/**
 * Implementation of CLIParser interface for command-line argument processing.
 * Based on Requirements 5.1-5.5: Command-line interface operations
 */
public class CLIParserImpl implements CLIParser {
    
    private final TaskManager taskManager;
    
    public CLIParserImpl(TaskManager taskManager) {
        this.taskManager = taskManager;
    }
    
    @Override
    public void parseAndExecute(String[] args) throws CLIException {
        if (args.length == 0) {
            displayHelp();
            return;
        }
        
        String command = args[0].toLowerCase();
        
        try {
            switch (command) {
                case "add":
                    handleAddCommand(args);
                    break;
                case "update":
                    handleUpdateCommand(args);
                    break;
                case "delete":
                    handleDeleteCommand(args);
                    break;
                case "mark-in-progress":
                    handleMarkInProgressCommand(args);
                    break;
                case "mark-done":
                    handleMarkDoneCommand(args);
                    break;
                case "list":
                    handleListCommand(args);
                    break;
                default:
                    displayError("Unknown command: " + command);
                    displayHelp();
            }
        } catch (TaskException e) {
            throw new CLIException("Task operation failed: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void displayHelp() {
        System.out.println("Task Tracker CLI");
        System.out.println("================");
        System.out.println();
        System.out.println("Usage: java TaskTrackerCLI <command> [arguments]");
        System.out.println();
        System.out.println("Available commands:");
        System.out.println("  add <description>           - Add a new task");
        System.out.println("  update <id> <description>   - Update an existing task");
        System.out.println("  delete <id>                 - Delete a task");
        System.out.println("  mark-in-progress <id>       - Mark task as in-progress");
        System.out.println("  mark-done <id>              - Mark task as done");
        System.out.println("  list [status]               - List tasks (all, todo, in-progress, done)");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  java TaskTrackerCLI add \"Buy groceries\"");
        System.out.println("  java TaskTrackerCLI update 1 \"Buy groceries and cook dinner\"");
        System.out.println("  java TaskTrackerCLI mark-in-progress 1");
        System.out.println("  java TaskTrackerCLI list todo");
        System.out.println("  java TaskTrackerCLI delete 1");
    }
    
    @Override
    public void displayError(String message) {
        System.err.println("Error: " + message);
    }
    
    @Override
    public void displaySuccess(String message) {
        System.out.println(message);
    }
    
    private void handleAddCommand(String[] args) throws CLIException, TaskException {
        if (args.length < 2) {
            throw new CLIException("Add command requires a description. Usage: add <description>");
        }
        
        // Join all arguments after "add" as the description
        StringBuilder description = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (i > 1) description.append(" ");
            description.append(args[i]);
        }
        
        Task newTask = taskManager.addTask(description.toString());
        displaySuccess("Task added successfully (ID: " + newTask.getId() + ")");
    }
    
    private void handleUpdateCommand(String[] args) throws CLIException, TaskException {
        if (args.length < 3) {
            throw new CLIException("Update command requires ID and description. Usage: update <id> <description>");
        }
        
        int id;
        try {
            id = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            throw new CLIException("Invalid task ID: " + args[1] + ". ID must be a number.");
        }
        
        // Join all arguments after ID as the description
        StringBuilder description = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            if (i > 2) description.append(" ");
            description.append(args[i]);
        }
        
        Task updatedTask = taskManager.updateTask(id, description.toString());
        displaySuccess("Task " + id + " updated successfully");
    }
    
    private void handleDeleteCommand(String[] args) throws CLIException, TaskException {
        if (args.length != 2) {
            throw new CLIException("Delete command requires exactly one ID. Usage: delete <id>");
        }
        
        int id;
        try {
            id = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            throw new CLIException("Invalid task ID: " + args[1] + ". ID must be a number.");
        }
        
        taskManager.deleteTask(id);
        displaySuccess("Task " + id + " deleted successfully");
    }
    
    private void handleMarkInProgressCommand(String[] args) throws CLIException, TaskException {
        if (args.length != 2) {
            throw new CLIException("Mark-in-progress command requires exactly one ID. Usage: mark-in-progress <id>");
        }
        
        int id;
        try {
            id = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            throw new CLIException("Invalid task ID: " + args[1] + ". ID must be a number.");
        }
        
        Task updatedTask = taskManager.markInProgress(id);
        displaySuccess("Task " + id + " marked as in-progress");
    }
    
    private void handleMarkDoneCommand(String[] args) throws CLIException, TaskException {
        if (args.length != 2) {
            throw new CLIException("Mark-done command requires exactly one ID. Usage: mark-done <id>");
        }
        
        int id;
        try {
            id = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            throw new CLIException("Invalid task ID: " + args[1] + ". ID must be a number.");
        }
        
        Task updatedTask = taskManager.markDone(id);
        displaySuccess("Task " + id + " marked as done");
    }
    
    private void handleListCommand(String[] args) throws CLIException, TaskException {
        List<Task> tasks;
        
        if (args.length == 1) {
            // List all tasks
            tasks = taskManager.listTasks();
        } else if (args.length == 2) {
            // List tasks by status
            String statusStr = args[1].toLowerCase();
            TaskStatus status;
            
            switch (statusStr) {
                case "todo":
                    status = TaskStatus.TODO;
                    break;
                case "in-progress":
                    status = TaskStatus.IN_PROGRESS;
                    break;
                case "done":
                    status = TaskStatus.DONE;
                    break;
                default:
                    throw new CLIException("Invalid status: " + statusStr + ". Valid statuses: todo, in-progress, done");
            }
            
            tasks = taskManager.listTasks(status);
        } else {
            throw new CLIException("List command takes at most one argument. Usage: list [status]");
        }
        
        displayTaskList(tasks);
    }
    
    private void displayTaskList(List<Task> tasks) {
        if (tasks.isEmpty()) {
            System.out.println("No tasks found.");
            return;
        }
        
        System.out.println();
        System.out.println("Tasks:");
        System.out.println("======");
        
        for (Task task : tasks) {
            System.out.printf("ID: %-3d | Status: %-11s | Description: %s%n",
                    task.getId(),
                    task.getStatus().getValue(),
                    task.getDescription());
        }
        
        System.out.println();
        System.out.println("Total: " + tasks.size() + " task(s)");
    }
}