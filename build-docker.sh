#!/bin/bash
# Docker Build Script for CI/CD
# Optimized for automated builds

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Configuration
IMAGE_NAME="ghcr.io/junglekia/repository-maintainability-index"
VERSION=${VERSION:-latest}
PORTS="-p 8080:8080"

echo -e "${BLUE}🏗️  RMI Docker Build Script v2.0${NC}"
echo -e "${BLUE}=====================================${NC}"

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}❌ Docker daemon is not running${NC}"
    exit 1
fi

# Functions
build_image() {
    echo -e "${BLUE}📦 Building Docker image: ${IMAGE_NAME}:${VERSION}${NC}"

    if docker build -t "${IMAGE_NAME}:${VERSION}" .; then
        echo -e "${GREEN}✅ Docker image built successfully${NC}"

        # Show image info
        docker images "${IMAGE_NAME}" --format "table {{.Repository}}\t{{.Tag}}\t{{.Size}}"

        # Optional: Run basic smoke test
        if [ "${RUN_SMOKE_TEST:-false}" = "true" ]; then
            run_smoke_test
        fi

    else
        echo -e "${RED}❌ Failed to build Docker image${NC}"
        exit 1
    fi
}

push_image() {
    if [ -z "${GITHUB_TOKEN}" ]; then
        echo -e "${YELLOW}⚠️  GITHUB_TOKEN not set. Skipping push to GHCR.${NC}"
        return 0
    fi

    echo -e "${BLUE}🚀 Pushing to GitHub Container Registry${NC}"

    # Login to GHCR
    echo "${GITHUB_TOKEN}" | docker login ghcr.io -u "${GITHUB_ACTOR:-dummy}" --password-stdin

    if docker push "${IMAGE_NAME}:${VERSION}"; then
        echo -e "${GREEN}✅ Docker image pushed to GHCR successfully${NC}"
        echo -e "${BLUE}📍 Available at: https://ghcr.io/junglekia/repository-maintainability-index${NC}"
    else
        echo -e "${RED}❌ Failed to push Docker image${NC}"
        exit 1
    fi
}

run_smoke_test() {
    echo -e "${BLUE}🧪 Running smoke test...${NC}"

    # Start container for smoke test
    docker run -d --name rmi-smoke-test "${IMAGE_NAME}:${VERSION}" --help

    # Wait a bit and check if container is running
    sleep 3
    if docker ps | grep -q rmi-smoke-test; then
        echo -e "${GREEN}✅ Smoke test passed${NC}"
        docker rm -f rmi-smoke-test >/dev/null 2>&1
    else
        echo -e "${RED}❌ Smoke test failed${NC}"
        docker logs rmi-smoke-test
        docker rm -f rmi-smoke-test >/dev/null 2>&1
        exit 1
    fi
}

show_usage() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  --build-only        Build image only (no push)"
    echo "  --push-only         Push existing image only"
    echo "  --smoke-test        Run smoke test after build"
    echo "  --version VERSION   Set image version tag (default: latest)"
    echo "  --help              Show this help"
    echo ""
    echo "Environment variables:"
    echo "  GITHUB_TOKEN        Required for pushing to GHCR"
    echo "  GITHUB_ACTOR        GitHub username for GHCR login"
    echo "  RUN_SMOKE_TEST     Set to 'true' to run smoke test"
    echo ""
    echo "Examples:"
    echo "  $0                          # Build only"
    echo "  $0 --push-only             # Push existing image"
    echo "  $0 --smoke-test            # Build with smoke test"
    echo "  VERSION=v1.0.0 $0          # Build with custom version"
}

# Parse command line arguments
BUILD_ONLY=false
PUSH_ONLY=false

while [[ $# -gt 0 ]]; do
    case $1 in
        --build-only)
            BUILD_ONLY=true
            shift
            ;;
        --push-only)
            PUSH_ONLY=true
            shift
            ;;
        --smoke-test)
            export RUN_SMOKE_TEST=true
            shift
            ;;
        --version)
            if [ -n "$2" ] && [[ $2 != --* ]]; then
                VERSION="$2"
                shift 2
            else
                echo -e "${RED}Error: --version requires a value${NC}"
                exit 1
            fi
            ;;
        --help|-h)
            show_usage
            exit 0
            ;;
        *)
            echo -e "${RED}Unknown option: $1${NC}"
            show_usage
            exit 1
            ;;
    esac
done

# Main logic
if [ "$PUSH_ONLY" = true ]; then
    push_image
elif [ "$BUILD_ONLY" = true ]; then
    build_image
else
    build_image
    push_image
fi

echo -e "${GREEN}🎉 Docker operations completed successfully!${NC}"
