#!/bin/bash
# Automatic UTF-8 setup for Git Bash

echo "🔧 Setting up UTF-8 for Git Bash..."
echo ""

# Check that we are in Windows/Git Bash
if [[ "$OSTYPE" != "msys" && "$OSTYPE" != "win32" ]]; then
    echo "❌ This script is designed for Git Bash on Windows"
    exit 1
fi

# Create backup of .bashrc if it exists
if [ -f ~/.bashrc ]; then
    cp ~/.bashrc ~/.bashrc.backup
    echo "✅ Backup created: ~/.bashrc.backup"
fi

# Add UTF-8 settings to .bashrc
cat >> ~/.bashrc << 'EOF'

# ============================================================================
# Automatic UTF-8 setup for Windows/Git Bash
# Added by setup-gitbash-utf8.sh script
# ============================================================================
if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "win32" ]]; then
    # Set UTF-8 locale
    export LANG=en_US.UTF-8
    export LC_ALL=en_US.UTF-8
    
    # Set Windows code page to UTF-8 (suppress output)
    chcp.com 65001 > /dev/null 2>&1
fi

# Alias for RMI with automatic UTF-8
alias rmi='java -Dfile.encoding=UTF-8 -jar target/repo-maintainability-index-1.0.0.jar'
# ============================================================================
EOF

echo "✅ Settings added to ~/.bashrc"
echo ""
echo "📝 What was added:"
echo "   - Automatic UTF-8 locale setting"
echo "   - Automatic code page 65001 setting"
echo "   - 'rmi' alias for quick launch"
echo ""
echo "🔄 Applying settings..."
source ~/.bashrc

echo ""
echo "✅ Done! Settings applied."
echo ""
echo "🧪 UTF-8 test:"
echo "╔═════════════════════╗"
echo "║  If you see the frame - it works! ✅  ║"
echo "╚═══════════════════╝"
echo ""
echo "📌 Now you can use:"
echo "   rmi analyze owner/repo"
echo "   rmi analyze owner/repo --llm"
echo ""
echo "💡 Or with full path:"
echo "   java -Dfile.encoding=UTF-8 -jar target/repo-maintainability-index-1.0.0.jar analyze owner/repo"
echo ""
echo "⚠️  If the frame doesn't display, restart Git Bash"
