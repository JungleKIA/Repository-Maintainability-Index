# UTF-8 Encoding Implementation

## Overview

The Repository Maintainability Index tool uses Unicode box-drawing characters in its output for better visual presentation:
- `═` (U+2550) - Box Drawings Double Horizontal
- `─` (U+2500) - Box Drawings Light Horizontal  
- `│` (U+2502) - Box Drawings Light Vertical
- `┌` (U+250C) - Box Drawings Light Down And Right
- `┐` (U+2510) - Box Drawings Light Down And Left
- `└` (U+2514) - Box Drawings Light Up And Right
- `┘` (U+2518) - Box Drawings Light Up And Left
- `▪` (U+25AA) - Black Small Square

## Problem: Windows/GitBash Unicode Display Issues

On Windows, especially in GitBash, Unicode characters may not display correctly without proper UTF-8 configuration. Common symptoms include:
- Characters displayed as `ΓòÉ`, `ΓöÇ`, etc. (mojibake)
- Question marks `?` or boxes `□` instead of Unicode characters
- Garbled output in the console

## Solution: Three-Step UTF-8 Initialization

The `EncodingHelper.setupUTF8ConsoleStreams()` method implements a comprehensive three-step approach:

### Step 1: System Properties Configuration

```java
System.setProperty("file.encoding", "UTF-8");
System.setProperty("sun.jnu.encoding", "UTF-8");
System.setProperty("console.encoding", "UTF-8");
```

This ensures the JVM prefers UTF-8 encoding for all I/O operations.

### Step 2: Windows Console Code Page (Windows Only)

```java
if (isWindows()) {
    ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", "chcp", "65001");
    pb.redirectErrorStream(true);
    Process process = pb.start();
    process.waitFor();
}
```

On Windows, this executes `chcp 65001` to set the console code page to UTF-8 (CP65001). This works for:
- Windows Command Prompt
- Sometimes for GitBash (depending on configuration)

**Note**: This may fail silently in some environments (e.g., when cmd.exe is not available), which is acceptable as we have fallbacks.

### Step 3: PrintStream Reconfiguration

```java
System.setOut(new java.io.PrintStream(
    new java.io.FileOutputStream(java.io.FileDescriptor.out),
    true,  // autoFlush - critical for GitBash
    StandardCharsets.UTF_8
));

System.setErr(new java.io.PrintStream(
    new java.io.FileOutputStream(java.io.FileDescriptor.err),
    true,  // autoFlush - critical for GitBash  
    StandardCharsets.UTF_8
));
```

Key implementation details:
- **Direct FileDescriptor access**: Using `FileDescriptor.out` instead of `System.out` ensures we get the actual console stream
- **No buffering layer**: We avoid `BufferedOutputStream` as it can interfere with GitBash's UTF-8 handling
- **Auto-flush enabled**: Critical for GitBash to ensure characters are flushed immediately with correct encoding

### Step 4: Java Logging Configuration (Bonus)

```java
java.util.logging.Logger rootLogger = java.util.logging.Logger.getLogger("");
java.util.logging.Handler[] handlers = rootLogger.getHandlers();
for (java.util.logging.Handler handler : handlers) {
    if (handler instanceof java.util.logging.ConsoleHandler) {
        handler.setEncoding("UTF-8");
    }
}
```

This ensures that SLF4J/java.util.logging output also uses UTF-8.

## Why This Approach Works

### GitBash Compatibility

GitBash is a MinGW terminal emulator that:
1. **Expects UTF-8 by default** but may receive incorrectly encoded data from Java
2. **Requires direct stream access** - buffered streams can corrupt encoding
3. **Needs auto-flush** - without it, partial multi-byte UTF-8 sequences can be broken

Our approach addresses all three requirements.

### Graceful Degradation

Each step is wrapped in try-catch blocks to ensure:
- No exceptions are thrown if a step fails
- The application works even if UTF-8 setup is incomplete
- Fallback to system default encoding if all steps fail

## Testing

### Manual Testing

```bash
# Test UTF-8 output
java -jar target/repo-maintainability-index-1.0.0.jar analyze prettier/prettier

# Expected output:
# ═══════════════════════════════════════════════════════════════
#   Repository Maintainability Index Report
# ═══════════════════════════════════════════════════════════════
```

### Unit Testing

The `EncodingHelperTest` class verifies:
- UTF-8 streams can be created without errors
- System properties are configurable
- Windows detection works correctly
- Stream restoration in tests (critical!)

### Platform-Specific Testing

- **Windows CMD**: `chcp 65001` + PrintStream reconfiguration
- **Windows PowerShell**: PrintStream reconfiguration only
- **GitBash**: PrintStream reconfiguration (most important)
- **Linux/macOS**: Usually works out-of-box, but PrintStream reconfiguration ensures consistency

## Alternatives Considered

### 1. ASCII Art Fallback ❌

**Rejected**: The requirement explicitly states "never replace Unicode with ASCII" as other similar tools successfully display Unicode.

### 2. System.console() ❌

**Rejected**: `System.console()` is often `null` in GitBash and IDE terminals.

### 3. JLine/Jansi Libraries ❌

**Rejected**: Adds external dependencies; our solution is zero-dependency.

### 4. Only System Properties ❌

**Insufficient**: Setting properties alone doesn't reconfigure existing streams.

### 5. BufferedOutputStream ❌

**Problematic for GitBash**: Buffering can corrupt multi-byte UTF-8 sequences.

## References

- [UTF-8 on Windows](https://docs.oracle.com/javase/tutorial/i18n/text/supplementaryChars.html)
- [Java UTF-8 Encoding](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/nio/charset/StandardCharsets.html)
- [Windows Code Pages](https://docs.microsoft.com/en-us/windows/console/console-code-pages)
- [GitBash Character Encoding](https://git-scm.com/docs/git-config#Documentation/git-config.txt-corequotePath)

## Best Practices for Developers

When modifying output code:

1. **Always call `EncodingHelper.setupUTF8ConsoleStreams()` in `Main.main()` first**
2. **Never modify streams in tests without saving/restoring**:
   ```java
   PrintStream original = System.out;
   try {
       // test code
   } finally {
       System.setOut(original);
   }
   ```
3. **Use native Unicode characters** - don't convert to ASCII
4. **Test on Windows/GitBash** if possible, or ensure CI includes Windows tests

## Known Limitations

1. **Very old Windows versions** (pre-Windows 10) may not support UTF-8 console output
2. **Some terminal emulators** may not support all Unicode characters
3. **SSH sessions** to Windows may need additional configuration
4. **IDE integrated terminals** may have their own encoding settings that override ours

In these cases, users can:
- Use the `-Dfile.encoding=UTF-8` JVM flag explicitly
- Configure their terminal emulator for UTF-8
- Use JSON output format (`--format json`) which is encoding-agnostic
