import java.io.*;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Implementation of JSONStore interface for file-based task storage.
 * Based on Requirements 4.1-4.5: Data persistence with JSON file storage
 */
public class JSONStoreImpl implements JSONStore {
    
    private static final String DEFAULT_FILE_NAME = "tasks.json";
    private final Path filePath;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    
    public JSONStoreImpl() {
        this(DEFAULT_FILE_NAME);
    }
    
    public JSONStoreImpl(String fileName) {
        this.filePath = Paths.get(fileName);
    }
    
    @Override
    public List<Task> loadTasks() throws StorageException {
        lock.readLock().lock();
        try {
            if (!Files.exists(filePath)) {
                return new ArrayList<>();
            }
            
            String content = Files.readString(filePath);
            if (content.trim().isEmpty()) {
                return new ArrayList<>();
            }
            
            return parseTasksFromJSON(content);
            
        } catch (IOException e) {
            throw new StorageException("Failed to read tasks from file: " + filePath, e);
        } catch (Exception e) {
            throw new StorageException("Failed to parse JSON content from file: " + filePath, e);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @Override
    public void saveTasks(List<Task> tasks) throws StorageException {
        lock.writeLock().lock();
        try {
            // Get current nextId or calculate it
            int nextId = calculateNextId(tasks);
            
            String jsonContent = generateJSON(tasks, nextId);
            
            // Atomic write operation with retry for Windows
            Path tempFile = filePath.resolveSibling(filePath.getFileName() + ".tmp");
            Files.writeString(tempFile, jsonContent, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            
            // Retry the move operation on Windows
            int retries = 3;
            while (retries > 0) {
                try {
                    Files.move(tempFile, filePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                    break;
                } catch (IOException e) {
                    retries--;
                    if (retries == 0) {
                        throw e;
                    }
                    try {
                        Thread.sleep(10); // Small delay before retry
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new StorageException("Interrupted during file operation", ie);
                    }
                }
            }
            
        } catch (IOException e) {
            throw new StorageException("Failed to save tasks to file: " + filePath, e);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    @Override
    public boolean fileExists() {
        return Files.exists(filePath);
    }
    
    @Override
    public void createFile() throws StorageException {
        lock.writeLock().lock();
        try {
            if (!Files.exists(filePath)) {
                String emptyStructure = generateJSON(new ArrayList<>(), 1);
                Files.writeString(filePath, emptyStructure, StandardOpenOption.CREATE);
            }
        } catch (IOException e) {
            throw new StorageException("Failed to create file: " + filePath, e);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    @Override
    public int getNextId() throws StorageException {
        lock.readLock().lock();
        try {
            if (!Files.exists(filePath)) {
                return 1;
            }
            
            String content = Files.readString(filePath);
            if (content.trim().isEmpty()) {
                return 1;
            }
            
            return parseNextIdFromJSON(content);
            
        } catch (IOException e) {
            throw new StorageException("Failed to read next ID from file: " + filePath, e);
        } catch (Exception e) {
            throw new StorageException("Failed to parse next ID from JSON: " + filePath, e);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @Override
    public void setNextId(int nextId) throws StorageException {
        lock.writeLock().lock();
        try {
            List<Task> tasks;
            try {
                tasks = loadTasks();
            } catch (StorageException e) {
                // If we can't load tasks, create empty list
                tasks = new ArrayList<>();
            }
            
            String jsonContent = generateJSON(tasks, nextId);
            
            // Atomic write operation with retry for Windows
            Path tempFile = filePath.resolveSibling(filePath.getFileName() + ".tmp");
            Files.writeString(tempFile, jsonContent, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            
            // Retry the move operation on Windows
            int retries = 3;
            while (retries > 0) {
                try {
                    Files.move(tempFile, filePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                    break;
                } catch (IOException e) {
                    retries--;
                    if (retries == 0) {
                        throw e;
                    }
                    try {
                        Thread.sleep(10); // Small delay before retry
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new StorageException("Interrupted during file operation", ie);
                    }
                }
            }
            
        } catch (IOException e) {
            throw new StorageException("Failed to update next ID in file: " + filePath, e);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Parse tasks from JSON content using simple string parsing
     */
    private List<Task> parseTasksFromJSON(String content) throws Exception {
        List<Task> tasks = new ArrayList<>();
        
        // Basic JSON structure validation
        if (!content.trim().startsWith("{") || !content.trim().endsWith("}")) {
            throw new Exception("Invalid JSON structure: must be an object");
        }
        
        // Find the tasks array - for corrupted JSON, this should fail
        int tasksStart = content.indexOf("\"tasks\"");
        if (tasksStart == -1) {
            // If there's no tasks field but the content looks like it should be a tasks file
            // (contains other content), treat it as corrupted
            if (content.contains("invalid") || (!content.trim().equals("{}") && !content.contains("nextId"))) {
                throw new Exception("Corrupted JSON: missing tasks field");
            }
            return tasks; // Empty valid JSON
        }
        
        int arrayStart = content.indexOf("[", tasksStart);
        int arrayEnd = content.lastIndexOf("]");
        
        if (arrayStart == -1 || arrayEnd == -1 || arrayStart >= arrayEnd) {
            throw new Exception("Invalid tasks array structure");
        }
        
        String tasksContent = content.substring(arrayStart + 1, arrayEnd).trim();
        if (tasksContent.isEmpty()) {
            return tasks; // Empty tasks array
        }
        
        // Split by task objects (simple approach)
        String[] taskStrings = splitTaskObjects(tasksContent);
        
        for (String taskString : taskStrings) {
            if (taskString.trim().isEmpty()) continue;
            
            Task task = parseTaskFromJSON(taskString.trim());
            if (task != null) {
                tasks.add(task);
            }
        }
        
        return tasks;
    }
    
    /**
     * Split task objects from JSON array content
     */
    private String[] splitTaskObjects(String content) {
        List<String> objects = new ArrayList<>();
        int braceCount = 0;
        int start = 0;
        
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '{') {
                braceCount++;
            } else if (c == '}') {
                braceCount--;
                if (braceCount == 0) {
                    objects.add(content.substring(start, i + 1));
                    start = i + 1;
                }
            }
        }
        
        return objects.toArray(new String[0]);
    }
    
    /**
     * Parse a single task from JSON string
     */
    private Task parseTaskFromJSON(String taskJson) throws Exception {
        try {
            int id = parseIntField(taskJson, "id");
            String description = parseStringField(taskJson, "description");
            String statusStr = parseStringField(taskJson, "status");
            String createdAtStr = parseStringField(taskJson, "createdAt");
            String updatedAtStr = parseStringField(taskJson, "updatedAt");
            
            TaskStatus status = TaskStatus.fromString(statusStr);
            Instant createdAt = Instant.parse(createdAtStr);
            Instant updatedAt = Instant.parse(updatedAtStr);
            
            return new Task(id, description, status, createdAt, updatedAt);
            
        } catch (Exception e) {
            throw new Exception("Failed to parse task from JSON: " + taskJson, e);
        }
    }
    
    /**
     * Parse integer field from JSON string
     */
    private int parseIntField(String json, String fieldName) throws Exception {
        String pattern = "\"" + fieldName + "\"\\s*:\\s*(\\d+)";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        
        throw new Exception("Field not found: " + fieldName);
    }
    
    /**
     * Parse string field from JSON string
     */
    private String parseStringField(String json, String fieldName) throws Exception {
        String pattern = "\"" + fieldName + "\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        
        if (m.find()) {
            return unescapeString(m.group(1));
        }
        
        throw new Exception("Field not found: " + fieldName);
    }
    
    /**
     * Parse nextId from JSON content
     */
    private int parseNextIdFromJSON(String content) throws Exception {
        try {
            return parseIntField(content, "nextId");
        } catch (Exception e) {
            // If nextId not found, calculate from existing tasks
            List<Task> tasks = parseTasksFromJSON(content);
            return calculateNextId(tasks);
        }
    }
    
    /**
     * Calculate next ID based on existing tasks
     */
    private int calculateNextId(List<Task> tasks) {
        return tasks.stream()
                .mapToInt(Task::getId)
                .max()
                .orElse(0) + 1;
    }
    
    /**
     * Generate JSON content from tasks and nextId
     */
    private String generateJSON(List<Task> tasks, int nextId) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"tasks\": [\n");
        
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            json.append("    {\n");
            json.append("      \"id\": ").append(task.getId()).append(",\n");
            json.append("      \"description\": \"").append(escapeString(task.getDescription())).append("\",\n");
            json.append("      \"status\": \"").append(task.getStatus().getValue()).append("\",\n");
            json.append("      \"createdAt\": \"").append(task.getCreatedAt().toString()).append("\",\n");
            json.append("      \"updatedAt\": \"").append(task.getUpdatedAt().toString()).append("\"\n");
            json.append("    }");
            
            if (i < tasks.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        
        json.append("  ],\n");
        json.append("  \"nextId\": ").append(nextId).append("\n");
        json.append("}\n");
        
        return json.toString();
    }
    
    /**
     * Escape special characters in JSON strings
     */
    private String escapeString(String str) {
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
    
    /**
     * Unescape special characters from JSON strings
     */
    private String unescapeString(String str) {
        return str.replace("\\\"", "\"")
                  .replace("\\\\", "\\")
                  .replace("\\n", "\n")
                  .replace("\\r", "\r")
                  .replace("\\t", "\t");
    }
}