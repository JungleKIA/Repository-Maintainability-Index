# Why do we need the -Dfile.encoding=UTF-8 flag?

## Short answer

Java application **cannot change JVM encoding after startup**. The `-Dfile.encoding=UTF-8` flag must be set **when starting the JVM**, otherwise all internal text operations will use the default encoding (usually Windows-1252 on Windows).

## Detailed explanation

### Problem 1: JVM initializes with default encoding

When you run:
```bash
java -jar app.jar
```

JVM initializes with the system's default encoding:
- **Windows**: usually `Windows-1252` or `Cp1252`
- **Linux/Mac**: usually `UTF-8`

This encoding is used for:
- Reading files
- Writing to console
- String conversions
- Logging

### Problem 2: Cannot change after startup

Even if you write in code:
```java
System.setProperty("file.encoding", "UTF-8");
```

This **DOESN'T WORK**! JVM has already initialized all internal structures with the original encoding.

### Problem 3: Git Bash requires chcp 6501

Git Bash on Windows by default uses code page 437 or 866, which don't support Unicode. The command `chcp 65001` switches the terminal to UTF-8.

But there's a problem: **Java process cannot change the parent terminal's code page**. This is an OS limitation - a child process cannot change the parent's settings.

## Solutions

### Solution 1: Use bat file (RECOMMENDED for one-time runs)

```bash
./rmi.bat analyze owner/repo
```

The bat file runs **in the current terminal**, so it can:
1. Execute `chcp 65001` (changes terminal's code page)
2. Start Java with `-Dfile.encoding=UTF-8` flag

### Solution 2: Configure Git Bash once (RECOMMENDED for permanent use)

```bash
./setup-gitbash-utf8.sh
```

This script adds to `~/.bashrc`:
- Automatic `chcp 65001` setting when terminal starts
- `rmi` alias with `-Dfile.encoding=UTF-8` flag

After this you can simply:
```bash
rmi analyze owner/repo
```

### Solution 3: Always add flag manually

```bash
java -Dfile.encoding=UTF-8 -jar target/repo-maintainability-index-1.0.0.jar analyze owner/repo
```

## Why does everything work after first bat file run?

When you run `rmi.bat`, it executes `chcp 65001` which **changes the current terminal's code page**. This setting **persists until the terminal is closed**.

So:
1. **First run**: `rmi.bat` → sets `chcp 65001` → works ✅
2. **Subsequent runs** in the same terminal: code page is already UTF-8 → works ✅
3. **New terminal**: code page is 437 again → need to run bat file again ❌

## Why can't we fix this in code?

### Attempt 1: Run chcp from Java

```java
ProcessBuilder pb = new ProcessBuilder("chcp", "65001");
pb.start();
```

**Doesn't work**: Creates new CMD process, changes its code page, process ends. Parent terminal doesn't change.

### Attempt 2: Change System.setProperty

```java
System.setProperty("file.encoding", "UTF-8");
```

**Doesn't work**: JVM is already initialized with different encoding. Changing property doesn't affect already created objects.

### Attempt 3: Wrap System.out

```java
System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
```

**Partially works**: Helps with output, but doesn't solve terminal code page problem and internal JVM operations.

## Conclusion

The only reliable way is **set flag when starting JVM**:

```bash
java -Dfile.encoding=UTF-8 -jar app.jar
```

And configure terminal to UTF-8 (once):

```bash
./setup-gitbash-utf8.sh
```

## Additional resources

- [GITBASH_UTF8_SETUP.md](../GITBASH_UTF8_SETUP.md) - detailed setup guide
- [UNICODE_SUPPORT.md](UNICODE_SUPPORT.md) - Unicode technical documentation
- [Oracle: Internationalization](https://docs.oracle.com/javase/tutorial/i18n/) - official Java documentation
