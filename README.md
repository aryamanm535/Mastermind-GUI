# Mastermind-GUI
## Setup & Verification

### Prerequisites

- Java 17 or higher (JDK)
- Terminal/command prompt
- Text editor or IDE (IntelliJ IDEA, Eclipse, VS Code recommended)

### Step 1: Verify Java Installation

```bash
java -version    # Should show 11 or higher
javac -version   # Should show 11 or higher
```

If not installed: Download from [Oracle](https://www.oracle.com/java/technologies/downloads/) or [OpenJDK](https://openjdk.org/)

### Step 2: Extract and Build

**Linux/Mac:**
```bash
cd scripts 
chmod +x *.sh
./build_script.sh
```

**Expected output:**
```
=========================================
   Mastermind Multiplayer - Build
=========================================
Creating bin directory...
Compiling source files...
√ Build successful!
```

**If compilation fails**: Check Java version, verify all .java files are in `src/` directory.

### Step 3: Run one or more clients to test single/multi player

```bash
./run-client.sh    
```

### Step 4: Start Server

**Terminal 2:**
```bash
cd scripts 
./run-server.sh
```

Server should start and wait for connections

### Step 5: Read the rules, start a game on the server (or singleplayer), and enjoy!
