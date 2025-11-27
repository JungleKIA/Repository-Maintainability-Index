#!/bin/bash
set -e

# Enterprise Modernization Audit Pipeline
# Runs a comprehensive suite of analysis tools inside a Docker container
# to ensure consistent execution regardless of host environment.

# Directory setup
mkdir -p docs/audit-reports

echo "Starting Enterprise Audit Pipeline..."
echo "====================================="

# Run the audit in Docker
docker run --rm \
    -v "$(pwd):/project" \
    -w /project \
    maven:3.8-openjdk-17-slim \
    mvn clean verify \
    cyclonedx:makeAggregateBom \
    dependency-check:check \
    spotbugs:check \
    checkstyle:check \
    -DskipTests=true \
    -Dmaven.repo.local=/project/.m2/repository

echo "====================================="
echo "Audit Complete."
echo "Artifacts generated:"
echo "- SBOM: target/bom.json"
echo "- Dependency Check: target/dependency-check-report.html"
echo "- SpotBugs: target/spotbugsXml.xml"
echo "- Checkstyle: target/checkstyle-result.xml"

# Move reports to docs/audit-reports for persistence
cp target/bom.json docs/audit-reports/sbom.json 2>/dev/null || echo "Warning: SBOM not found"
cp target/dependency-check-report.html docs/audit-reports/vulnerability-report.html 2>/dev/null || echo "Warning: Vuln report not found"
cp target/spotbugsXml.xml docs/audit-reports/static-analysis-spotbugs.xml 2>/dev/null || echo "Warning: SpotBugs report not found"
cp target/checkstyle-result.xml docs/audit-reports/code-style-report.xml 2>/dev/null || echo "Warning: Checkstyle report not found"

echo "Reports archived in docs/audit-reports/"
