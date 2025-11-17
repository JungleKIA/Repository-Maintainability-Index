#!/bin/bash
# Docker Run Script for Repository Maintainability Index
# Use: ./run-docker.sh [args]

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
IMAGE_NAME="repository-maintainability-index"
CONTAINER_NAME="rmi-app"

echo -e "${BLUE}🚀 Repository Maintainability Index - Docker Runner${NC}"
echo -e "${BLUE}==================================================${NC}"

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}❌ Docker daemon is not running. Please start Docker first.${NC}"
    exit 1
fi

# Check if .env file exists and has required variables
if [ ! -f .env ]; then
    echo -e "${YELLOW}⚠️  .env file not found. Creating template...${NC}"
    cat > .env << EOF
# GitHub API Token (required)
GITHUB_TOKEN=your_github_token_here

# Optional: LLM API Key for enhanced analysis
OPENAI_API_KEY=your_openai_key_here

# Application settings
LOG_LEVEL=INFO
MAX_THREADS=4
TIMEOUT_SECONDS=60
EOF
    echo -e "${YELLOW}⚠️  Please edit .env file with your credentials before running.${NC}"
    echo -e "${YELLOW}   Required: GITHUB_TOKEN${NC}"
    exit 1
fi

# Create logs directory if it doesn't exist
mkdir -p logs

# Function to build Docker image
build_image() {
    echo -e "${BLUE}🏗️  Building Docker image...${NC}"
    if docker build -t $IMAGE_NAME:latest .; then
        echo -e "${GREEN}✅ Docker image built successfully${NC}"
    else
        echo -e "${RED}❌ Failed to build Docker image${NC}"
        exit 1
    fi
}

# Function to run container
run_container() {
    local cmd_args="$@"
    local container_args=""

    # Parse command line arguments for container execution
    if [ $# -eq 0 ]; then
        container_args="analyze --help"
    else
        container_args="$cmd_args"
    fi

    echo -e "${BLUE}🐳 Running container with command: ${container_args}${NC}"

    # Run the container
    docker run --rm \
        --name $CONTAINER_NAME \
        --env-file .env \
        -v "$(pwd)/logs:/app/logs:rw" \
        -e JAVA_OPTS="-Xmx512m -Xms256m" \
        $IMAGE_NAME:latest \
        $container_args
}

# Function to show usage
show_usage() {
    echo "Usage: $0 [COMMAND] [OPTIONS]"
    echo ""
    echo "Commands:"
    echo "  build          - Build Docker image"
    echo "  run [args]     - Run analysis with arguments (default: analyze --help)"
    echo "  help           - Show this help"
    echo ""
    echo "Examples:"
    echo "  $0 build                                   # Build image"
    echo "  $0 run analyze --repo octocat/hello-world  # Run analysis"
    echo "  $0 run --help                              # Show app help"
    echo ""
    echo "Environment:"
    echo "  .env file must exist with GITHUB_TOKEN"
    echo "  Logs will be written to ./logs/ directory"
}

# Main logic
case "${1:-help}" in
    build)
        build_image
        ;;
    run)
        shift
        run_container "$@"
        ;;
    help|--help|-h)
        show_usage
        ;;
    *)
        # If no command specified but arguments given, assume run
        if [ $# -eq 0 ]; then
            run_container "--help"
        else
            run_container "$@"
        fi
        ;;
esac
