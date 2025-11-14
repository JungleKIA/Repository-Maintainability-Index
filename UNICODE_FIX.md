# Unicode Display Fix for Windows/Git Bash

## Problem

When running `java -jar target/repo-maintainability-index-1.0.0.jar` directly, you see garbled characters like:
- `ΓòÉ` instead of `═`
- `ΓöÇ` instead of `─`
- `Γû¬` instead of `▪`

## Why This Happens

Java requires the `-Dfile.encoding=UTF-8` flag to be set **at JVM startup**. This is a fundamental JVM limitation - the encoding cannot be changed after the JVM has started, even programmatically.

## Solutions (Choose One)

### ✅ Solution 1: Use Provided Scripts (EASIEST)

```bash
# Quick launcher
rmi.bat analyze owner/repo

# Full-featured launcher with LLM support
run-with-encoding.bat analyze owner/repo --llm
```

### ✅ Solution 2: Add Flag Manually

```bash
java -Dfile.encoding=UTF-8 -jar target/repo-maintainability-index-1.0.0.jar analyze owner/repo
```

### ✅ Solution 3: Set Global Environment Variable (PERMANENT)

**Git Bash:**
```bash
# Add to ~/.bashrc
echo 'export JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8"' >> ~/.bashrc
source ~/.bashrc
```

**Windows Command Prompt:**
```cmd
setx JAVA_TOOL_OPTIONS "-Dfile.encoding=UTF-8"
```

After setting, restart your terminal and run normally:
```bash
java -jar target/repo-maintainability-index-1.0.0.jar analyze owner/repo
```

## Verification

After applying any solution, you should see proper Unicode characters:
- `╔═══╗` (box-drawing)
- `📊` (emojis)
- `✅` (check marks)

## Why Can't This Be Fixed in Code?

Java's `file.encoding` property is read-only after JVM initialization. Even setting `System.setProperty("file.encoding", "UTF-8")` in code doesn't work because:

1. The JVM reads `file.encoding` at startup
2. All I/O streams (`System.out`, `System.err`) are initialized with this encoding
3. Logging frameworks (SLF4J/Logback) are initialized with this encoding
4. These cannot be changed after initialization

This is why major Java tools (Maven, Gradle, etc.) also require the `-Dfile.encoding=UTF-8` flag for proper Unicode support.
