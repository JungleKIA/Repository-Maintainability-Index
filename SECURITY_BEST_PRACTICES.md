# 🔒 Security Best Practices for API Keys

## ⚠️ CRITICAL: Never Commit API Keys to Git!

### What Happened (Important Lesson)

During development, API keys were accidentally committed to this repository's documentation files. **OpenRouter detected them and automatically disabled all keys for security.**

This is why the keys you tried didn't work - they were deactivated by OpenRouter's security system.

## How API Key Exposure Happens

### GitHub Push Protection
GitHub automatically scans commits for secrets and blocks pushes containing:
- API keys
- Tokens
- Passwords
- Private keys

### OpenRouter Security Scanning
OpenRouter continuously scans GitHub for exposed API keys and:
1. Detects keys in public repositories
2. Sends email notification to owner
3. **Automatically disables the key**
4. Key can no longer be used

**Example notification:**
```
IMPORTANT: One of your OpenRouter API keys has been found exposed.
The affected key ends in: ...6ea0
Location: github.com/user/repo/file.md
We have disabled this api key and it can no longer be used.
```

## ✅ Correct Way to Use API Keys

### 1. Environment Variables (Recommended)
```bash
# In your terminal or .bashrc
export OPENROUTER_API_KEY=your_actual_key_here
export GITHUB_TOKEN=your_github_token_here

# Then run the tool
java -jar target/repo-maintainability-index-1.0.0.jar analyze owner/repo --llm
```

### 2. .env File (Good for Local Development)
```bash
# Create .env file (NEVER commit this file!)
cat > .env << 'EOF'
OPENROUTER_API_KEY=sk-or-v1-your-actual-key
GITHUB_TOKEN=ghp_your_token
EOF

# Add to .gitignore
echo ".env" >> .gitignore

# Load and use
source .env
java -jar target/repo-maintainability-index-1.0.0.jar analyze owner/repo --llm
```

### 3. Configuration File Outside Repo
```bash
# Store in ~/.config/rmi/config
mkdir -p ~/.config/rmi
cat > ~/.config/rmi/config << 'EOF'
OPENROUTER_API_KEY=your_key_here
GITHUB_TOKEN=your_token_here
EOF

chmod 600 ~/.config/rmi/config

# Load before running
source ~/.config/rmi/config
```

## ❌ NEVER Do This

### ❌ Don't Commit Keys in Code
```java
// WRONG!
String apiKey = "sk-or-v1-actual-key-here";
```

### ❌ Don't Put Keys in README
```markdown
<!-- WRONG! -->
## Setup
export OPENROUTER_API_KEY=sk-or-v1-abc123...
```

### ❌ Don't Put Keys in Documentation
```markdown
<!-- WRONG! -->
Tested with key: sk-or-v1-xyz789...
```

### ❌ Don't Put Keys in Test Files
```java
// WRONG!
@Test
void testWithRealKey() {
    String key = "sk-or-v1-real-key";
}
```

## ✅ DO This Instead

### ✅ Use Placeholders in Documentation
```markdown
## Setup
export OPENROUTER_API_KEY=your_key_here
export GITHUB_TOKEN=your_token_here
```

### ✅ Use Environment Variables in Code
```java
// CORRECT!
String apiKey = System.getenv("OPENROUTER_API_KEY");
```

### ✅ Use Mock Keys in Tests
```java
// CORRECT!
@Test
void testApiKeyHandling() {
    String mockKey = "sk-or-v1-mock-key-for-testing";
}
```

### ✅ Add .env to .gitignore
```gitignore
# API keys and secrets
.env
.env.local
.env.*.local
*.key
*.secret
config.json
secrets.yml
```

## What to Do If You Exposed a Key

### Step 1: Assume Key is Compromised
- The key is already exposed
- It will be found and disabled
- Do NOT try to remove it from history (too late)

### Step 2: Rotate the Key Immediately
1. Go to: https://openrouter.ai/keys
2. Delete the exposed key
3. Create a new key
4. Update your local environment

### Step 3: Update Your Applications
```bash
# Update environment variable
export OPENROUTER_API_KEY=new_key_here

# Test that it works
java -jar target/repo-maintainability-index-1.0.0.jar analyze test/repo --llm
```

### Step 4: Learn and Prevent
- Add .env to .gitignore
- Use environment variables
- Never hardcode secrets
- Review commits before pushing

## How to Get a Working Key

### Step 1: Create Account
1. Visit: https://openrouter.ai
2. Sign up (free)

### Step 2: Purchase Credits
1. Go to: https://openrouter.ai/settings/credits
2. Purchase minimum $5
3. Note: Even "free" models require account validation via credit purchase

### Step 3: Generate Key
1. Go to: https://openrouter.ai/keys
2. Click "Create Key"
3. **Copy it immediately** (shown only once)
4. **Store it securely** in environment variable

### Step 4: Use Securely
```bash
# Set environment variable
export OPENROUTER_API_KEY=sk-or-v1-your-new-key

# Test it works
java -jar target/repo-maintainability-index-1.0.0.jar analyze owner/repo --llm

# If it works, you'll see real LLM analysis!
```

## Security Checklist

Before committing code:
- [ ] No API keys in code
- [ ] No tokens in documentation
- [ ] No passwords in config files
- [ ] .env in .gitignore
- [ ] Using environment variables
- [ ] Tested with mock keys
- [ ] Reviewed git diff

Before pushing to GitHub:
- [ ] No secrets in commit
- [ ] .gitignore is correct
- [ ] Documentation uses placeholders
- [ ] No hardcoded credentials

## Project's .gitignore

This project already includes proper .gitignore:
```gitignore
# API keys and secrets
.env
.env.local
*.key
*.secret

# IDE files
.idea/
.vscode/

# Build files
target/
```

## Testing Without Exposing Keys

### Option 1: Environment Variables
```bash
# Terminal 1
export OPENROUTER_API_KEY=your_key
java -jar target/repo-maintainability-index-1.0.0.jar analyze owner/repo --llm
```

### Option 2: Inline (One Command)
```bash
OPENROUTER_API_KEY=your_key java -jar target/repo-maintainability-index-1.0.0.jar analyze owner/repo --llm
```

### Option 3: Script File (Not in Git)
```bash
# Create run.sh (add to .gitignore)
cat > run.sh << 'EOF'
#!/bin/bash
export OPENROUTER_API_KEY=your_key_here
java -jar target/repo-maintainability-index-1.0.0.jar analyze "$@" --llm
EOF

chmod +x run.sh
echo "run.sh" >> .gitignore

# Use it
./run.sh owner/repo
```

## Why This Matters

### Financial Risk
- Exposed keys can be used by attackers
- They can rack up charges on your account
- You're responsible for all usage

### Security Risk
- Keys grant access to your account
- Can read your data
- Can make API calls
- Can cost you money

### Reputation Risk
- Shows poor security practices
- Can affect trust in your project
- Can violate company policies

## Summary

| Method | Security | Convenience |
|--------|----------|-------------|
| Environment Variables | ✅ High | ✅ High |
| .env File (gitignored) | ✅ High | ✅ High |
| Config File (outside repo) | ✅ High | 🟡 Medium |
| Hardcoded in Code | ❌ None | ✅ High |
| In Documentation | ❌ None | ✅ High |
| In Git History | ❌ None | 🟡 Medium |

## Learn More

- GitHub: https://docs.github.com/en/code-security/secret-scanning
- OpenRouter Security: https://openrouter.ai/docs/security
- API Key Best Practices: https://owasp.org/www-project-api-security/

---

**Remember**: Once a key is committed to a public repo, assume it's compromised forever. Rotate immediately!

**This Project**: All keys mentioned in documentation are examples/disabled. Always generate your own keys and keep them private.
