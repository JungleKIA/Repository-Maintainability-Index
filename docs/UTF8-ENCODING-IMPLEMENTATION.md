# UTF-8 Encoding Implementation for Windows/GitBash

## Overview

This document describes the technical implementation of UTF-8/Unicode support in the Repository Maintainability Index (RMI) application, specifically addressing encoding issues on Windows and GitBash environments.

## Problem Statement

Java applications that output Unicode characters (like box-drawing characters: ═, ─, │, ▪) to the console often encounter encoding issues on Windows, particularly in GitBash. The symptoms include:

- **Mojibake**: Corrupted Unicode sequences appearing as `ΓòÉ` instead of `═`, or `ΓöÇ` instead of `─`
- **Character Substitution**: Box-drawing characters replaced with ASCII equivalents or question marks
- **Inconsistent Rendering**: Unicode works on Linux/macOS but fails on Windows

## Solution Architecture

Our implementation uses a **four-layer defense strategy** to ensure Unicode characters display correctly:

### Layer 1: System Property Configuration

**Location**: `EncodingHelper.setupUTF8ConsoleStreams()` - Step 1

```java
System.setProperty("file.encoding", "UTF-8");
System.setProperty("sun.jnu.encoding", "UTF-8");
System.setProperty("console.encoding", "UTF-8");
```

**Purpose**: Instructs the JVM to prefer UTF-8 encoding for file I/O and console operations.

**Limitations**: These properties don't affect already-initialized streams, so we need additional steps.

---

### Layer 2: Windows Console Code Page Setup

**Location**: `EncodingHelper.setupUTF8Output()`

```java
if (isWindows()) {
    ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", "chcp", "65001");
    pb.redirectErrorStream(true);
    Process process = pb.start();
    
    // Consume output to prevent blocking
    try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(process.getInputStream()))) {
        while (reader.readLine() != null) {
            // Consume and discard output
        }
    }
    
    process.waitFor();
}
```

**Purpose**: Sets the Windows console code page to 65001 (UTF-8).

**How it works**:
- Executes `chcp 65001` command via `cmd.exe`
- Code page 65001 tells Windows console to interpret output as UTF-8
- Helps with Command Prompt and sometimes with GitBash

**Why consume output**: Prevents the process from blocking if its output buffer fills up.

---

### Layer 3: PrintStream Reconfiguration (THE CRITICAL FIX)

**Location**: `EncodingHelper.setupUTF8ConsoleStreams()` - Step 3

```java
System.setOut(new PrintStream(
    new FileOutputStream(FileDescriptor.out),
    true,  // autoFlush - CRITICAL for GitBash
    StandardCharsets.UTF_8
));

System.setErr(new PrintStream(
    new FileOutputStream(FileDescriptor.err),
    true,  // autoFlush - CRITICAL for GitBash  
    StandardCharsets.UTF_8
));
```

**Purpose**: Replaces default `System.out` and `System.err` with UTF-8 aware streams.

**Critical Implementation Details**:

1. **Direct FileDescriptor Access**: 
   - Uses `FileDescriptor.out` and `FileDescriptor.err` directly
   - Bypasses Java's default encoding assumptions
   - GitBash expects UTF-8 by default, so we give it UTF-8

2. **AutoFlush Enabled**: 
   - The `true` parameter enables automatic flushing after each write
   - **CRITICAL for GitBash**: Prevents corruption of multi-byte UTF-8 sequences
   - Without autoFlush, buffered writes can split UTF-8 characters across buffer boundaries

3. **No BufferedOutputStream**: 
   - We do NOT wrap `FileOutputStream` in `BufferedOutputStream`
   - Buffering can corrupt UTF-8 multi-byte sequences
   - AutoFlush on `PrintStream` provides sufficient performance

**Why this works for GitBash**:
- GitBash's terminal emulator (MinTTY) expects UTF-8 encoded input
- By providing a UTF-8 encoded stream with autoFlush, we ensure:
  - UTF-8 characters are encoded correctly
  - Multi-byte sequences are not split across writes
  - No intermediate re-encoding that could corrupt the data

---

### Layer 4: Logging Configuration

**Location A**: `EncodingHelper.setupUTF8ConsoleStreams()` - Step 4 (java.util.logging)

```java
java.util.logging.Logger rootLogger = java.util.logging.Logger.getLogger("");
java.util.logging.Handler[] handlers = rootLogger.getHandlers();
for (java.util.logging.Handler handler : handlers) {
    if (handler instanceof java.util.logging.ConsoleHandler) {
        handler.setEncoding("UTF-8");
    }
}
```

**Location B**: `src/main/resources/logback.xml` (Logback/SLF4J)

```xml
<appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
    <encoder>
        <charset>UTF-8</charset>
        <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
    </encoder>
</appender>
```

**Purpose**: Ensures logging frameworks also use UTF-8 encoding.

**Why needed**: If loggers output before our encoding setup, or use their own streams, they need explicit UTF-8 configuration.

---

### Layer 5: Mojibake Repair (Safety Net)

**Location**: `EncodingHelper.cleanTextForWindows()`

```java
public static String cleanTextForWindows(String text) {
    if (text == null || !isWindows()) {
        return text;
    }

    if (containsMojibake(text)) {
        try {
            // Fix mojibake by re-encoding:
            // 1. Get bytes as if text was incorrectly decoded as ISO-8859-1
            byte[] bytes = text.getBytes(StandardCharsets.ISO_8859_1);
            // 2. Re-decode as UTF-8 (the original intended encoding)
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return text;
        }
    }
    
    return text;
}
```

**Purpose**: Detects and repairs corrupted UTF-8 sequences (mojibake) if they occur.

**How mojibake occurs**:
1. Application writes UTF-8 bytes: `\xE2\x95\x90` (═)
2. Terminal incorrectly interprets as Windows-1252/ISO-8859-1
3. Displays as: `ΓòÉ` (3 separate characters)

**How repair works**:
1. Detect mojibake patterns (e.g., "ΓòÉ", "ΓöÇ", "Γû¬")
2. Extract bytes as if string was ISO-8859-1 encoded
3. Re-interpret those bytes as UTF-8
4. Result: Original Unicode character restored

**Mojibake Detection Patterns**:
```java
private static boolean containsMojibake(String text) {
    return text.contains("ΓòÉ") ||  // corrupted ═ (U+2550)
           text.contains("ΓöÇ") ||  // corrupted ─ (U+2500)
           text.contains("Γû¬") ||  // corrupted ▪ (U+25AA)
           text.contains("Γöé") ||  // corrupted │ (U+2502)
           text.contains("ΓöÇΓöÇ") || // multiple corrupted ─
           text.contains("ΓòÉΓòÉ");  // multiple corrupted ═
}
```

**Usage**: Applied in `ReportFormatter` and `LLMReportFormatter` before returning text.

---

## Initialization Sequence

**Location**: `Main.main()`

```java
public static void main(String[] args) {
    // MUST be first line - configure encoding before ANY output
    EncodingHelper.setupUTF8ConsoleStreams();
    
    // Rest of application initialization...
    int exitCode = new CommandLine(new Main()).execute(args);
    System.exit(exitCode);
}
```

**Critical**: `setupUTF8ConsoleStreams()` MUST be called before:
- Any logging initialization
- Any console output
- Any framework initialization that might write to console

---

## Unicode Characters Used

The application uses the following Unicode box-drawing characters:

| Character | Unicode | Decimal | Name | Usage |
|-----------|---------|---------|------|-------|
| `═` | U+2550 | 9552 | Box Drawings Double Horizontal | Report headers |
| `─` | U+2500 | 9472 | Box Drawings Light Horizontal | Section dividers |
| `│` | U+2502 | 9474 | Box Drawings Light Vertical | Borders |
| `┌` | U+250C | 9484 | Box Drawings Light Down and Right | Top-left corner |
| `┐` | U+2510 | 9488 | Box Drawings Light Down and Left | Top-right corner |
| `└` | U+2514 | 9492 | Box Drawings Light Up and Right | Bottom-left corner |
| `┘` | U+2518 | 9496 | Box Drawings Light Up and Left | Bottom-right corner |
| `▪` | U+25AA | 9642 | Black Small Square | Bullet points |

**Design Decision**: We keep these Unicode characters rather than falling back to ASCII because:
1. Modern terminals (including GitBash) support them
2. They provide better visual clarity and professionalism
3. Our encoding setup ensures they work correctly
4. Fallback to ASCII would compromise user experience

---

## Testing

### Unit Tests

**Location**: `src/test/java/com/kaicode/rmi/util/EncodingHelperTest.java`

Key tests:
- `shouldSetupUTF8ConsoleStreamsWithoutException()`: Verifies initialization doesn't throw
- `shouldSetupUTF8StreamsProperly()`: Verifies system properties are set
- `shouldCleanMojibakeOnWindows()`: Tests mojibake repair on Windows
- `shouldHandleBoxDrawingCharacters()`: Tests Unicode character handling

### Platform-Specific Tests

Tests use JUnit 5's `@EnabledOnOs` annotation:

```java
@Test
@EnabledOnOs(OS.WINDOWS)
void shouldCleanMojibakeOnWindows() {
    // Windows-specific test
}

@Test
@EnabledOnOs({OS.LINUX, OS.MAC})
void shouldNotModifyTextOnNonWindows() {
    // Unix-specific test
}
```

### Manual Testing

**Command**:
```bash
java -jar target/repo-maintainability-index-1.0.0.jar analyze prettier/prettier
```

**Expected output**: Clean box-drawing characters (═══, ───, ▪)

**Failure indicators**: Corrupted characters (ΓòÉ, ΓöÇ, Γû¬)

---

## Troubleshooting

### Issue: Still seeing mojibake after implementation

**Possible causes**:
1. GitBash locale not set to UTF-8
2. Terminal font doesn't support box-drawing characters
3. JVM launched with explicit encoding parameter that overrides our settings

**Solutions**:
```bash
# Set GitBash locale
export LANG=en_US.UTF-8

# Or force UTF-8 at JVM level (though our code should handle this)
java -Dfile.encoding=UTF-8 -jar target/repo-maintainability-index-1.0.0.jar analyze owner/repo
```

### Issue: Works in CMD but not GitBash

**Cause**: GitBash uses MinTTY terminal emulator which has different encoding behavior.

**Solution**: Our Layer 3 (PrintStream reconfiguration with autoFlush) specifically addresses this. Ensure:
1. `setupUTF8ConsoleStreams()` is called first in `main()`
2. No code writes to console before encoding setup
3. GitBash locale is set to UTF-8

### Issue: Some characters work, others don't

**Cause**: Font doesn't have glyphs for all Unicode characters.

**Solution**: Install a font with good Unicode coverage:
- **Windows**: Consolas, Cascadia Code, JetBrains Mono
- **GitBash**: Configure MinTTY to use one of the above fonts

---

## Performance Considerations

**AutoFlush Impact**: Enabling autoFlush on PrintStream adds minimal overhead:
- Each write operation flushes the buffer
- For console output (not high-frequency), this is negligible
- Benefit (correct encoding) far outweighs cost (microseconds per write)

**Mojibake Detection**: 
- Only runs on Windows (platform check first)
- Uses simple `String.contains()` checks (O(n) with early exit)
- Only repairs if mojibake detected (most cases: no repair needed)

**Memory**: No additional memory overhead - uses existing streams.

---

## References

- [UTF-8 Encoding Specification (RFC 3629)](https://tools.ietf.org/html/rfc3629)
- [Windows Code Pages](https://docs.microsoft.com/en-us/windows/win32/intl/code-pages)
- [Java Character Encoding](https://docs.oracle.com/javase/tutorial/i18n/text/stream.html)
- [Mojibake on Wikipedia](https://en.wikipedia.org/wiki/Mojibake)

---

## Related Files

- `src/main/java/com/kaicode/rmi/util/EncodingHelper.java` - Core encoding utilities
- `src/main/java/com/kaicode/rmi/Main.java` - Initialization
- `src/main/java/com/kaicode/rmi/util/ReportFormatter.java` - Text output with encoding
- `src/main/java/com/kaicode/rmi/util/LLMReportFormatter.java` - LLM report output with encoding
- `src/main/resources/logback.xml` - Logging UTF-8 configuration
- `src/test/java/com/kaicode/rmi/util/EncodingHelperTest.java` - Comprehensive tests
- `README.md` - User-facing UTF-8 setup instructions
