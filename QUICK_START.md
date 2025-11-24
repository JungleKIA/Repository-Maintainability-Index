# Quick Start Guide

## First Run (One-time setup)

### 1. Build the project

```bash
mvn clean package
```

### 2. UTF-8 setup for Git Bash (one time)

```bash
./setup-gitbash-utf8.sh
```

Restart Git Bash after setup.

### 3. Environment variables setup (optional)

Create `.env` file in project root:

```bash
# GitHub API token (recommended to avoid rate limits)
GITHUB_TOKEN=your_github_token_here

# OpenRouter API key (required for LLM analysis)
OPENROUTER_API_KEY=your_openrouter_api_key_here
```

## Usage

### Basic analysis

```bash
rmi analyze owner/repo
```

### With AI analysis

```bash
rmi analyze owner/repo --llm
```

### Quiet mode (for scripts)

```bash
# Suppress logs, show only results
rmi analyze owner/repo --quiet --llm
```

### With GitHub token

```bash
rmi analyze owner/repo --token YOUR_TOKEN
```

### JSON format

```bash
rmi analyze owner/repo --format json
```

## Examples

```bash
# Analyze popular repository
rmi analyze facebook/react

# Analyze with AI insights
rmi analyze prettier/prettier --llm

# Analyze with token and JSON output
rmi analyze microsoft/vscode --token ghp_xxx --format json
```

## Troubleshooting

### Problem: Characters display as ΓòÉ, ΓöÇ

**Solution**: Run setup script:
```bash
./setup-gitbash-utf8.sh
```

Or use the bat file:
```bash
./rmi.bat analyze owner/repo
```

### Problem: "GITHUB_TOKEN not set" warning

**Solution**: Create `.env` file or set environment variable:
```bash
export GITHUB_TOKEN=your_token_here
```

### Problem: "OPENROUTER_API_KEY not set" when using --llm

**Solution**: Add key to `.env` file:
```bash
OPENROUTER_API_KEY=your_key_here
```

## Additional Information

- [README.md](README.md) - full documentation
- [GITBASH_UTF8_SETUP.md](GITBASH_UTF8_SETUP.md) - detailed UTF-8 setup
- [docs/WHY_UTF8_FLAG_REQUIRED.md](docs/WHY_UTF8_FLAG_REQUIRED.md) - why UTF-8 flag is needed
- [docs/UNICODE_SUPPORT.md](docs/UNICODE_SUPPORT.md) - technical documentation
