# 🗂️ Task Tracker CLI
![Java](https://img.shields.io/badge/Java-8%2B-orange)
![CLI](https://img.shields.io/badge/Type-CLI-blue)
![License](https://img.shields.io/badge/License-MIT-green)

> A simple, file-backed **command-line Task Tracker** application implemented in **Java**.  
> Designed as a minimal yet complete example of a real-world CLI tool with persistence.

---

## 📖 Overview

**Task Tracker CLI** allows users to manage tasks directly from the terminal.  
It supports adding, updating, deleting, listing, and tracking task status while persisting data safely to a local JSON file.

This project is intended as a **teaching and demonstration tool** for:
- 🎓 Software engineering students  
- 👨‍🏫 Lecturers reviewing design decisions  
- 👨‍💻 Recruiters assessing backend & CLI fundamentals  

---

## ✨ Key Features

- ➕ Add, update, and delete tasks from the command line  
- ⏳ Mark tasks as **In-Progress** or **Done**  
- 📋 List all tasks or filter by status  
- 💾 File-based JSON persistence (`tasks.json`)  
- 🔒 Atomic write handling (Windows-aware)  
- 🧩 Clear separation of concerns (CLI · Logic · Storage)  
- 🧪 JUnit test scaffolding (auto-download supported)

---

## 🏗️ Project Structure

```text
project-root/
│
├── src/
│   ├── main/java/
│   │   ├── TaskTrackerCLI.java   # Application entry point
│   │   ├── CLIParser*.java       # CLI parsing & help
│   │   ├── TaskManager*.java     # Business logic
│   │   └── JSONStore*.java       # Persistence layer
│   │
│   └── test/java/                # JUnit test sources
│
├── lib/                          # Third-party JARs (JUnit, etc.)
├── build/                        # Generated build artifacts
├── build.bat                     # Windows build script
├── build.sh                      # Linux/macOS build script
└── tasks.json                    # Runtime data file (generated)


## 🧰 Technologies & Tools
- Java (language/runtime) — compatible with Java 8+ (JDK 11+ recommended)
- JUnit 5 — used by the test sources (scripts download JUnit jars)
- Shell / Batch scripts — `build.sh` and `build.bat` for convenience

## ⚙️ Prerequisites
- JDK installed (11+ recommended). Verify with:

```bash
java -version
javac -version
```

- `curl` (Linux/macOS) is used by `build.sh` to download JUnit jars if needed.

## 🛠️ Build Instructions

🪟 Windows (quick):

```powershell
# from repository root (where build.bat is located)
.\\build.bat
```

🐧Unix / Linux / macOS (quick):

```bash
# from repository root
chmod +x build.sh
./build.sh
```

Notes:
- `build.bat` compiles the main classes and creates `build/classes/main`. It prints a note about JUnit setup; tests are skipped unless you add JUnit jars in `lib/`.
- `build.sh` will attempt to download JUnit artifacts into `lib/`, compile main and test classes, and run tests via the JUnit Console Launcher.

🔧 Manual Build (javac):

Windows:

```powershell
mkdir -Force build\\classes\\main
javac -d build\\classes\\main src\\main\\java\\*.java
```

Linux / macOS:

```bash
mkdir -p build/classes/main
javac -d build/classes/main src/main/java/*.java
```

## ▶️ Running the Application
After building, run the CLI from the repository root.

Windows (class path separator `;`):

```powershell
java -cp build\\classes\\main TaskTrackerCLI <command> [args]
```

Linux / macOS (class path separator `:`):

```bash
java -cp build/classes/main TaskTrackerCLI <command> [args]
```

🧪 Example Usage (from project root):

```bash
# Add a task
java -cp build/classes/main TaskTrackerCLI add "Buy groceries"

# Update task 1
java -cp build/classes/main TaskTrackerCLI update 1 "Buy groceries and cook dinner"

# Mark as in-progress
java -cp build/classes/main TaskTrackerCLI mark-in-progress 1

# Mark as done
java -cp build/classes/main TaskTrackerCLI mark-done 1

# List all tasks
java -cp build/classes/main TaskTrackerCLI list

# List tasks with status 'todo'
java -cp build/classes/main TaskTrackerCLI list todo

# Delete a task
java -cp build/classes/main TaskTrackerCLI delete 1
```

📤 Sample Output
```
Task added successfully (ID: 1)
Tasks:
======
ID: 1   | Status: TODO        | Description: Buy groceries
Total: 1 task(s)
```

## 🧠 Development Notes

- Storage: `JSONStoreImpl` implements a lightweight, dependency-free JSON persistence layer. It uses manual string parsing and a simple JSON generator to avoid an external JSON library, and employs a `ReentrantReadWriteLock` plus atomic write (temp file + move) to reduce corruption risk on concurrent access and on Windows.
- Architecture: Clear separation of responsibilities:
  - `CLIParser` handles argument parsing and user interaction
  - `TaskManager` contains business logic and validation
  - `JSONStore` encapsulates persistence concerns
  This makes the code easy to test and extend.
- Error handling: Custom exception types (`CLIException`, `TaskException`, `StorageException`) are used to distinguish failure modes and present clearer error messages to users.
- Tests: Basic JUnit tests are present under `src/test/java/`. `build.sh` can fetch JUnit jars and run tests with the JUnit Console Launcher.

## 🚧 Limitations & Future Improvements

- JSON handling is implemented manually — replace with a robust JSON library (Jackson / Gson) for production use.
- No dependency management system (Maven/Gradle). Migrating to Maven or Gradle would simplify dependency handling, builds, and packaging.
- Packaging: currently runs from compiled classes; add a reproducible `jar` (fat JAR or proper manifest) for easier distribution.
- Concurrency: while atomic writes are used, consider stronger transactional guarantees for multi-process scenarios.
- CLI UX: add richer parsing (picocli / commons-cli), command descriptions, and interactive mode.
- Tests: expand unit and integration tests, and add CI integration (GitHub Actions, etc.).

## 🤝 Contributing
Contributions are welcome. When proposing changes, please:
- Open an issue to discuss the change
- Send small, focused PRs with tests where applicable

## 👤 Author
- Name: Zefasil Mulu
- Email: zefasilmulu@gmail.com
- GitHub: https://github.com/zefasil20

If you are a recruiter or lecturer: feel free to ask for a walkthrough or a focused summary of design/implementation choices.

---

Happy hacking! 🚀
# new
