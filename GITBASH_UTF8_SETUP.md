# Permanent UTF-8 Setup for Git Bash

## Problem

When first opening Git Bash terminal, Unicode characters are displayed incorrectly. You need to run `rmi.bat` or `chcp 65001` each time to fix this.

## Solution: Automatic UTF-8 Setup

### Option 1: Setup via .bashrc (RECOMMENDED)

Add the following lines to the `~/.bashrc` file:

```bash
# Automatic UTF-8 setup for Windows/Git Bash
if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "win32" ]]; then
    # Set UTF-8 locale
    export LANG=en_US.UTF-8
    export LC_ALL=en_US.UTF-8
    
    # Set Windows code page to UTF-8
    chcp.com 65001 > /dev/null 2>&1
fi
```

**How to do this:**

1. Open Git Bash
2. Execute command:
   ```bash
   nano ~/.bashrc
   ```
3. Add the lines above to the end of the file
4. Save: `Ctrl+O`, `Enter`, `Ctrl+X`
5. Restart Git Bash or execute:
   ```bash
   source ~/.bashrc
   ```

### Option 2: Setup via .bash_profile

If `.bashrc` doesn't work, try `.bash_profile`:

```bash
nano ~/.bash_profile
```

Add the same lines as above.

### Option 3: Create Git Bash shortcut with automatic setup

1. Find the Git Bash shortcut on desktop or Start menu
2. Right-click → Properties
3. In the "Target" field, change to:
   ```
   "C:\Program Files\Git\git-bash.exe" --cd-to-home -c "chcp.com 65001 > /dev/null 2>&1; exec bash"
   ```
4. Click OK

### Option 4: Use Windows Terminal (BEST OPTION)

Windows Terminal automatically supports UTF-8. Install it from Microsoft Store and configure Git Bash profile:

1. Open Windows Terminal
2. Settings → Profiles → Git Bash
3. Advanced → Encoding → UTF-8
4. Save

## Check

After setup, check that UTF-8 works:

```bash
# Check locale
locale

# Check code page (should be 65001)
chcp

# Check Unicode output
echo "╔═══════╗"
echo "║ Test  ║"
echo "╚═══════╝"
```

If you see correct frame characters - setup works! ✅

## Now you can run without bat file

After setup, you can run the application directly:

```bash
# Without bat file - now works!
java -Dfile.encoding=UTF-8 -jar target/repo-maintainability-index-1.0.0.jar analyze owner/repo

# With LLM
java -Dfile.encoding=UTF-8 -jar target/repo-maintainability-index-1.0.0.jar analyze owner/repo --llm
```

## Important: -Dfile.encoding=UTF-8 Flag

Even after terminal setup, **always** use the `-Dfile.encoding=UTF-8` flag when starting Java, otherwise JVM won't use UTF-8 inside the application.

**Correct:**
```bash
java -Dfile.encoding=UTF-8 -jar target/repo-maintainability-index-1.0.0.jar analyze owner/repo
```

**Incorrect (characters will break):**
```bash
java -jar target/repo-maintainability-index-1.0.0.jar analyze owner/repo
```

## Bonus: Create alias for convenience

Add to `~/.bashrc` an alias for quick launch:

```bash
# Alias for RMI with automatic UTF-8
alias rmi='java -Dfile.encoding=UTF-8 -jar target/repo-maintainability-index-1.0.0.jar'
```

Now you can run simply:

```bash
rmi analyze owner/repo
rmi analyze owner/repo --llm
```

## Alternative: Always use bat file

If you don't want to configure the terminal, just always use `rmi.bat`:

```bash
./rmi.bat analyze owner/repo
./rmi.bat analyze owner/repo --llm
```

The bat file automatically:
- Sets `chcp 65001`
- Adds flag `-Dfile.encoding=UTF-8`
- Starts the application with correct parameters
