# Unicode Support Documentation

## Overview

The Repository Maintainability Index (RMI) application provides comprehensive Unicode support for displaying special characters, box-drawing symbols, and emojis in console output across different platforms and terminals.

## Problem Statement

When running Java applications in Windows terminals (especially Git Bash), Unicode characters often display incorrectly as "mojibake" - garbled text sequences. For example:
- `═` (U+2550) displays as `ΓòÉ`
- `─` (U+2500) displays as `ΓöÇ`
- `▪` (U+25AA) displays as `Γû¬`
- Smart quotes and dashes display as `â€œ`, `â€"`, etc.

This occurs due to encoding mismatches between:
1. The application (generating UTF-8 output)
2. The Windows console (using Windows-1252 or CP-866 by default)
3. The terminal emulator (Git Bash, CMD, PowerShell)

## Solution Architecture

RMI implements a multi-layered approach to ensure correct Unicode display:

### Layer 1: JVM Configuration ⚠️ **CRITICAL**
- **MUST** set `-Dfile.encoding=UTF-8` at JVM startup via command line
- Cannot be changed after JVM starts - this is a Java limitation
- Use provided launch scripts or add flag manually
- Configures `sun.stdout.encoding` and `sun.stderr.encoding`
- Ensures all text generation uses UTF-8

### Layer 2: Console Configuration
- Automatically detects Windows OS
- Executes `chcp 65001` to set UTF-8 code page
- Configures console before any output

### Layer 3: Stream Wrapping
- Wraps `System.out` and `System.err` with UTF-8 `PrintStream`
- Ensures explicit UTF-8 encoding for all console output
- Flushes buffers before reconfiguration

### Layer 4: Text Cleaning
- Programmatically fixes mojibake artifacts
- Replaces corrupted sequences with correct Unicode characters
- Applied at multiple points: LLM responses, model construction, report generation

### Layer 5: Launch Scripts
- Provides `run-with-encoding.bat` (Windows) and `run-with-encoding.sh` (Unix)
- Automatically configures environment before running application
- Simplifies usage for end users

## Implementation Details

### EncodingHelper Utility

The `EncodingHelper` class provides core functionality:

```java
// Setup UTF-8 console streams (call at application startup)
EncodingHelper.setupUTF8ConsoleStreams();

// Clean text from mojibake artifacts
String cleanText = EncodingHelper.cleanTextForWindows(dirtyText);

// Check if running on Windows
boolean isWindows = EncodingHelper.isWindows();
```

### Mojibake Cleaning Patterns

The following mojibake patterns are automatically cleaned:

| Corrupted | Correct | Unicode | Description |
|-----------|---------|---------|-------------|
| `ΓòÉ` | `═` | U+2550 | Box Drawings Double Horizontal |
| `ΓöÇ` | `─` | U+2500 | Box Drawings Light Horizontal |
| `Γû¬` | `▪` | U+25AA | Black Small Square |
| `Γöé` | `│` | U+2502 | Box Drawings Light Vertical |
| `ΓÇæ` | `-` | - | Dash variant (common in LLM responses) |
| `ΓÇô` | `-` | - | Dash variant (common in LLM responses) |
| `â€œ` | `"` | U+201C | Left Double Quotation Mark |
| `â€"` | `"` | U+201D | Right Double Quotation Mark |
| `â€˜` | `'` | U+2018 | Left Single Quotation Mark |
| `â€™` | `'` | U+2019 | Right Single Quotation Mark |
| `â€"` | `–` | U+2013 | En Dash |
| `â€"` | `—` | U+2014 | Em Dash |

### Integration Points

Text cleaning is applied at multiple points:

1. **LLMClient**: Cleans API responses before returning
2. **LLMAnalyzer**: Cleans parsed JSON fields and fallback values
3. **LLMAnalysis Models**: Cleans text in constructors
4. **ReportFormatter**: Cleans final report output
5. **LLMReportFormatter**: Cleans LLM-enhanced reports

## Usage

### Automatic (Recommended)

Use the provided launch scripts which automatically set `-Dfile.encoding=UTF-8`:

```bash
# Windows - Quick launcher
rmi.bat analyze owner/repo

# Windows - Full-featured launcher
run-with-encoding.bat analyze owner/repo

# Linux / macOS / Git Bash
./run-with-encoding.sh analyze owner/repo
```

### Manual Configuration

If not using launch scripts, configure manually:

**Windows Command Prompt:**
```cmd
chcp 65001
java -Dfile.encoding=UTF-8 -jar target/repo-maintainability-index-1.0.0.jar analyze owner/repo
```

**Git Bash:**
```bash
export LANG=en_US.UTF-8
java -Dfile.encoding=UTF-8 -jar target/repo-maintainability-index-1.0.0.jar analyze owner/repo
```

**PowerShell:**
```powershell
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
java -Dfile.encoding=UTF-8 -jar target/repo-maintainability-index-1.0.0.jar analyze owner/repo
```

## Troubleshooting

### Problem: Characters display as `ΓòÉ`, `ΓöÇ`, etc.

**Cause:** JVM was started without `-Dfile.encoding=UTF-8` flag

**Solutions:**
1. **Use provided launch scripts** (recommended): `rmi.bat` or `run-with-encoding.bat`
2. **Add `-Dfile.encoding=UTF-8` flag manually:**
   ```bash
   java -Dfile.encoding=UTF-8 -jar target/repo-maintainability-index-1.0.0.jar analyze owner/repo
   ```
3. Set console code page: `chcp 65001` (Windows)

**❌ This will NOT work:**
```bash
java -jar target/repo-maintainability-index-1.0.0.jar analyze owner/repo
# Missing -Dfile.encoding=UTF-8 flag!
```

### Problem: Characters display as empty squares `□`

**Cause:** Terminal font doesn't support Unicode characters

**Solution:** Change terminal font to:
- **Consolas** (recommended for Windows)
- **Cascadia Code** (modern, supports ligatures)
- **Fira Code** (popular among developers)

**How to change font in Git Bash:**
1. Right-click title bar → Options
2. Text → Select...
3. Choose font from list
4. Click OK and restart

### Problem: Characters display as question marks `?`

**Cause:** Terminal doesn't support UTF-8 encoding

**Solution:**
```bash
export LANG=en_US.UTF-8
export LC_ALL=en_US.UTF-8
```

### Problem: Mixed correct and incorrect characters

**Cause:** Inconsistent encoding configuration

**Solution:**
1. Close terminal completely
2. Reopen terminal
3. Run configuration commands again
4. Verify with test output

## Verification

To verify Unicode support is working correctly:

```bash
java -jar target/repo-maintainability-index-1.0.0.jar --help
```

You should see:
- `═══════` (double horizontal lines)
- `───────` (single horizontal lines)
- `▪` (bullet points)
- Emojis: 🤖 📖 📝 👥 💡 ✅ ❌

If these display correctly, Unicode support is working!

## Platform-Specific Notes

### Windows

- **Automatic:** Application automatically sets `chcp 65001`
- **Manual:** Run `chcp 65001` before launching
- **Persistent:** Add to Windows Terminal settings for permanent configuration

### Git Bash on Windows

- **Locale:** Set `LANG=en_US.UTF-8` in `~/.bashrc`
- **Font:** Use Consolas or Cascadia Code
- **Scripts:** Use provided `run-with-encoding.sh` script

### Linux / macOS

- **Usually works:** UTF-8 is typically default
- **If needed:** Set `LANG=en_US.UTF-8` in shell profile
- **Verification:** Run `locale` to check current settings

## Technical Details

### Why Multiple Layers?

Each layer addresses a specific part of the problem:

1. **JVM Layer:** Ensures text is generated in UTF-8
2. **Console Layer:** Configures Windows to accept UTF-8
3. **Stream Layer:** Ensures output uses UTF-8 encoding
4. **Cleaning Layer:** Fixes any remaining corruption
5. **Script Layer:** Simplifies configuration for users

This redundancy ensures Unicode works even if one layer fails.

### Performance Impact

Text cleaning has minimal performance impact:
- **Complexity:** O(n) where n is text length
- **Overhead:** < 1ms for typical output
- **Optimization:** Cleaning happens once at entry points

### Backward Compatibility

The solution maintains backward compatibility:
- Works on systems without UTF-8 support (via text cleaning)
- Doesn't break existing functionality
- Gracefully degrades if configuration fails

## Testing

The implementation includes comprehensive tests:

- **EncodingHelperTest:** 47 tests for text cleaning
- **LLMClientTest:** 11 tests including mojibake cleaning
- **LLMAnalyzerTest:** 11 tests for parse method cleaning
- **LLMAnalysisTest:** 13 tests for constructor cleaning

All tests verify:
- Mojibake patterns are cleaned correctly
- Clean text remains unchanged
- Empty/null inputs are handled gracefully
- Idempotent behavior (repeated cleaning produces same result)

## References

- **Java Internationalization:** https://docs.oracle.com/javase/tutorial/i18n/
- **Windows Code Pages:** https://docs.microsoft.com/en-us/windows/console/
- **UTF-8 Specification:** https://tools.ietf.org/html/rfc3629
- **Mojibake Explanation:** https://en.wikipedia.org/wiki/Mojibake

## Support

If you encounter Unicode display issues not covered here:

1. Check terminal font supports Unicode
2. Verify locale settings: `locale` (Unix) or `chcp` (Windows)
3. Try provided launch scripts
4. Check Java version: `java -version` (requires Java 11+)
5. Report issue with:
   - Operating system and version
   - Terminal type (CMD, PowerShell, Git Bash, etc.)
   - Example of incorrect output
   - Output of `java -XshowSettings:properties | grep file.encoding`
