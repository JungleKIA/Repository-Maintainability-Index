# CI Pipeline Fixes for PR #4

## Overview
This document describes the fixes applied to resolve failing CI checks in Pull Request #4 (enterprise-modernization-strategy branch).

## Problems Identified

### 1. Build and Test Job Failures
**Root Causes:**
- Build steps were split into multiple separate commands (compile, test, jacoco:report, jacoco:check)
- This approach was fragile and could fail between steps
- Using outdated artifact upload action (v3 instead of v4)

**Fixes Applied:**
- Consolidated all build steps into single `mvn clean verify -B` command
- This runs the full Maven lifecycle: compile → test → jacoco:report → jacoco:check
- Updated to `actions/upload-artifact@v4` with retention policies
- Added `retention-days: 30` for test results and coverage reports

### 2. SBOM Generation Job Failures
**Root Causes:**
- Missing compile step before SBOM generation
- Plugin was trying to analyze non-existent compiled classes
- Incorrect artifact path (`target/bom.json` instead of `target/bom.*`)
- CycloneDX Maven plugin was not configured in pom.xml

**Fixes Applied:**
- Added `mvn clean compile -B` step before SBOM generation
- Changed artifact path to `target/bom.*` to capture all formats (XML, JSON)
- Updated to `actions/upload-artifact@v4`
- Added `if: always()` to ensure SBOM upload even on failures
- Added `retention-days: 90` for SBOM artifacts
- **Added CycloneDX Maven plugin to pom.xml** (version 2.7.11)

### 3. Minor Consistency Issues
**Fixes:**
- Changed `cache: maven` to `cache: 'maven'` (quoted) in all jobs
- Added retention policies to all artifact uploads
- Ensured consistent use of latest action versions

## Changes Made

### 1. `.github/workflows/ci.yml`
- **build-and-test job**: Unified build commands, updated artifacts
- **security-scan job**: Fixed cache syntax
- **code-quality job**: Fixed cache syntax
- **sbom-generation job**: Added compile step, fixed paths and uploads
- **package job**: Updated artifact action, added retention

### 2. `pom.xml`
- Added CycloneDX Maven Plugin (org.cyclonedx:cyclonedx-maven-plugin:2.7.11)
- Configured plugin to generate SBOM in all formats (XML, JSON, etc.)
- Set schema version to 1.5
- Configured to include all relevant scopes except test

## Plugin Configuration Details

### CycloneDX Maven Plugin
```xml
<plugin>
    <groupId>org.cyclonedx</groupId>
    <artifactId>cyclonedx-maven-plugin</artifactId>
    <version>2.7.11</version>
    <executions>
        <execution>
            <phase>package</phase>
            <goals>
                <goal>makeAggregateBom</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <projectType>application</projectType>
        <schemaVersion>1.5</schemaVersion>
        <outputFormat>all</outputFormat>
        <outputName>bom</outputName>
    </configuration>
</plugin>
```

## Expected Results

After these fixes, all CI checks should pass:

✅ **Build and Test (pull_request)** - Single verify command ensures complete build
✅ **Build and Test (push)** - Same as above
✅ **Generate SBOM (pull_request)** - Compile step + correct plugin configuration
✅ **Generate SBOM (push)** - Same as above
✅ **Code Quality Analysis** - Already passing, fixed cache syntax
✅ **Security Scanning** - Already passing, fixed cache syntax

## Testing Recommendations

1. Push these changes to PR #4 branch (enterprise-modernization-strategy)
2. Monitor CI pipeline execution
3. Verify all four failing checks now pass
4. Check artifact uploads in GitHub Actions UI
5. Verify SBOM files are generated correctly

## Comparison with PR #5

These fixes align with the successful CI configuration from PR #5 (fix-ci-failures-investigate-and-fix-sbom-build-tests), ensuring consistency across the repository.

## Notes

- The changes follow best practices for GitHub Actions workflows
- Artifact retention policies help manage storage costs
- Single verify command is more reliable than split commands
- SBOM generation now follows industry standards with CycloneDX
