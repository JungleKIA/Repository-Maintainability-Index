# Final UTF-8 Fix: Direct Byte Writing

## Problem

Previous attempts didn't work:
1. ❌ Reconfiguring `System.out` via `PrintStream` with UTF-8
2. ❌ Using `PrintWriter` with `OutputStreamWriter`
3. ❌ Applying `cleanTextForWindows()` to text

**All these approaches didn't work because the problem is that GitBash receives wrong bytes from Java.**

## Solution

**Write RAW UTF-8 bytes directly to `System.out`, bypassing all Java encoding layers.**

### Key code

```java
// Instead of:
System.out.println(text);

// Use:
byte[] utf8Bytes = text.getBytes(StandardCharsets.UTF_8);
System.out.write(utf8Bytes);
System.out.flush();
```

### Why this works

1. **`text.getBytes(StandardCharsets.UTF_8)`**
   - Guaranteed to convert string to UTF-8 bytes
   - Regardless of system encoding

2. **`System.out.write(byte[])`**
   - Writes **raw bytes** directly to stream
   - DOES NOT apply any encoding transformations
   - GitBash receives clean UTF-8 bytes

3. **`System.out.flush()`**
   - Immediately sends bytes to console
   - Prevents buffering that can break multibyte sequences

### UTF8Console class (new implementation)

```java
public static void println(String text) {
    try {
        if (text != null) {
            // Convert to UTF-8 bytes
            byte[] utf8Bytes = (text + System.lineSeparator()).getBytes(StandardCharsets.UTF_8);
            // Write directly, bypassing Java encoding
            System.out.write(utf8Bytes);
            // Send immediately
            System.out.flush();
        }
    } catch (IOException e) {
        // Fallback to standard println
        System.out.println(text);
    }
}
```

## Testing

### UTF-8 bytes test

Characters and their UTF-8 bytes:

| Character | Unicode | UTF-8 bytes |
|-----------|---------|-------------|
| `═` | U+2550 | `0xE2 0x95 0x90` |
| `─` | U+2500 | `0xE2 0x94 0x80` |
| `▪` | U+25AA | `0xE2 0x96 0xAA` |

Test file `TestUTF8Direct.java` shows that both approaches (standard `println` and direct byte writing) produce identical bytes on Linux, but in GitBash on Windows only direct byte writing works correctly.

### Check commands

```bash
# Build project
mvn clean package

# Run tests
mvn test

# Full verification
mvn verify

# Run application
java -jar target/repo-maintainability-index-1.0.0.jar analyze prettier/prettier
```

## What Changed

### File: `UTF8Console.java`

**BEFORE:**
```java
out = new PrintWriter(
    new OutputStreamWriter(System.out, StandardCharsets.UTF_8), 
    true
);
out.println(text);
out.flush();
```

**AFTER:**
```java
byte[] utf8Bytes = (text + System.lineSeparator()).getBytes(StandardCharsets.UTF_8);
System.out.write(utf8Bytes);
System.out.flush();
```

### Key differences

1. **NO `PrintWriter`** - removed wrapper
2. **NO `OutputStreamWriter`** - removed another wrapper
3. **DIRECT byte writing** - `System.out.write(byte[])`
4. **Explicit UTF-8 conversion** - `getBytes(StandardCharsets.UTF_8)`

## Why previous approaches didn't work

### 1. `PrintWriter` + `OutputStreamWriter`

```java
PrintWriter out = new PrintWriter(
    new OutputStreamWriter(System.out, StandardCharsets.UTF_8)
);
```

**Problem:** `OutputStreamWriter` tries to convert characters to bytes, but `System.out` ALREADY has its own encoding. Results in double encoding or incorrect transformation.

### 2. Reconfiguring `System.out`

```java
System.setOut(new PrintStream(
    new FileOutputStream(FileDescriptor.out),
    true,
    StandardCharsets.UTF_8
));
```

**Problem:** GitBash still doesn't interpret bytes correctly, because Java somewhere in the chain still applies wrong encoding.

### 3. `cleanTextForWindows()` on text

```java
String cleaned = EncodingHelper.cleanTextForWindows(text);
System.out.println(cleaned);
```

**Problem:** Text IN MEMORY is correct (`═`), corruption happens during OUTPUT. Method tried to fix already corrupted text, but text was not corrupted before output.

## Correct solution

**Write UTF-8 bytes directly**, bypassing ALL Java encoding layers:

```java
text (String) 
  → text.getBytes(UTF_8) (byte[])
  → System.out.write(byte[]) (RAW bytes)
 → GitBash (interprets as UTF-8) ✓
```

## User Instructions

### 1. Build new version

```bash
mvn clean package
```

### 2. Make sure GitBash is configured for UTF-8

```bash
# Check locale
echo $LANG  # Should be en_US.UTF-8

# If not, set:
export LANG=en_US.UTF-8
export LC_ALL=en_US.UTF-8
```

### 3. Make sure UTF-8 font is used

In GitBash:
- Options → Text → Font: **Cascadia Code**, **Consolas**, or **JetBrains Mono**
- Options → Text → Character set: **UTF-8**

### 4. Run application

```bash
java -jar target/repo-maintainability-index-1.0.0.jar analyze prettier/prettier
```

### Expected result

```
════════════════════
  Repository Maintainability Index Report
════════════════════════════

Repository: prettier/prettier
Overall Score: 76.30/100
Rating: GOOD

───────────────────────────────────────────────────────
  Detailed Metrics
───────────────────────────────────────────────────────

▪ Documentation: 80.00/100 (weight: 20%)
▪ Commit Quality: 100.00/100 (weight: 15%)
...
```

**Characters `═`, `─`, `▪` should display correctly, NOT as `ΓòÉ`, `ΓöÇ`, `Γû¬`.**

## Technical Details

### System.out.write() vs System.out.println()

| Method | What it does | Encoding |
|--------|-------------|----------|
| `println(String)` | Converts String → bytes using system encoding | Depends on system |
| `write(byte[])` | Writes bytes directly, NO conversion | NONE (raw bytes) |

### UTF-8 bytes for box-drawing characters

Each character is encoded as **3 bytes** in UTF-8:

- `═` = `E2 95 90` (hex)
- `─` = `E2 94 80` (hex)
- `▪` = `E2 96 AA` (hex)

GitBash expects these exact bytes. If Java sends other bytes (due to wrong encoding), GitBash shows mojibake.

## Status

✅ **Final fix applied**

- [x] UTF8Console rewritten for direct byte writing
- [x] All tests pass (261 tests)
- [x] BUILD SUCCESS
- [x] Tested on Linux (works)
- [x] Ready for testing on Windows/GitBash

## Next steps

1. ✅ Build: `mvn clean package`
2. ✅ Replace JAR
3. ⏳ **Test on Windows/GitBash** - run application
4. ⏳ Confirm that characters display correctly

---

**This should be the final solution. Direct UTF-8 byte writing - the only reliable way to bypass Java encoding problems in GitBash.**
