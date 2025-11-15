# Automated Release Process

## Overview

The CI/CD pipeline automatically creates GitHub Releases when you push a version tag. This includes:
- 📦 JAR artifact
- 📋 SBOM (Software Bill of Materials)
- 📝 Auto-generated release notes

## Creating a Release

### 1. Update Version in pom.xml

```xml
<version>1.1.0</version>
```

### 2. Commit and Push

```bash
git add pom.xml
git commit -m "chore: bump version to 1.1.0"
git push origin main
```

### 3. Create and Push Tag

```bash
# Create annotated tag
git tag -a v1.1.0 -m "Release version 1.1.0"

# Push tag to trigger release workflow
git push origin v1.1.0
```

### 4. Automatic Release Creation

The CI/CD pipeline will:
1. ✅ Run all tests and quality checks
2. ✅ Build the JAR artifact
3. ✅ Generate SBOM
4. ✅ Create GitHub Release with:
   - JAR file: `repo-maintainability-index-1.1.0.jar`
   - SBOM files: `bom.json`, `bom.xml`
   - Auto-generated release notes from commits

## Version Naming Convention

Follow [Semantic Versioning](https://semver.org/):

- **Major** (v2.0.0): Breaking changes
- **Minor** (v1.1.0): New features, backward compatible
- **Patch** (v1.0.1): Bug fixes, backward compatible

### Examples

```bash
# Major release (breaking changes)
git tag -a v2.0.0 -m "Release 2.0.0 - New API structure"

# Minor release (new features)
git tag -a v1.1.0 -m "Release 1.1.0 - Add LLM analysis"

# Patch release (bug fixes)
git tag -a v1.0.1 -m "Release 1.0.1 - Fix Unicode display"
```

## Pre-release Versions

For beta/RC versions:

```bash
# Beta release
git tag -a v1.1.0-beta.1 -m "Release 1.1.0 Beta 1"

# Release candidate
git tag -a v1.1.0-rc.1 -m "Release 1.1.0 RC1"
```

These will be marked as "pre-release" in GitHub.

## Editing Release Notes

After automatic creation, you can:

1. Go to **Releases** page
2. Click **Edit** on the release
3. Add custom description, highlights, breaking changes
4. Update release notes

### Recommended Release Notes Template

```markdown
## 🎉 What's New

- Feature 1: Description
- Feature 2: Description

## 🐛 Bug Fixes

- Fix 1: Description
- Fix 2: Description

## 🔧 Improvements

- Improvement 1: Description

## ⚠️ Breaking Changes

- Breaking change 1: Migration guide

## 📦 Installation

Download the JAR file and run:
\`\`\`bash
java -Dfile.encoding=UTF-8 -jar repo-maintainability-index-1.1.0.jar analyze owner/repo
\`\`\`

## 📊 Metrics

- Test Coverage: 89%
- Code Quality: A+
- Security: No vulnerabilities
```

## Deleting a Release

If you need to delete a release:

```bash
# Delete remote tag
git push --delete origin v1.1.0

# Delete local tag
git tag -d v1.1.0
```

Then manually delete the GitHub Release from the web interface.

## Troubleshooting

### Release workflow didn't trigger

**Cause**: Tag doesn't match pattern `v*`

**Solution**: Ensure tag starts with `v`:
```bash
git tag -a v1.1.0 -m "Release 1.1.0"  # ✅ Correct
git tag -a 1.1.0 -m "Release 1.1.0"   # ❌ Won't trigger
```

### Release failed during creation

**Cause**: One of the required jobs failed

**Solution**: 
1. Check the Actions tab for errors
2. Fix the issues
3. Delete the tag and recreate after fixes

### Missing artifacts in release

**Cause**: Artifact upload failed

**Solution**: Check the `package` job logs in Actions tab

## Best Practices

1. ✅ Always test locally before creating a release tag
2. ✅ Run `mvn clean verify` to ensure all tests pass
3. ✅ Update CHANGELOG.md before releasing
4. ✅ Use annotated tags (`-a`) for better Git history
5. ✅ Write meaningful tag messages
6. ✅ Create releases from `main` branch only
7. ✅ Never force-push tags

## Monitoring Releases

- **GitHub Releases**: https://github.com/YOUR_ORG/YOUR_REPO/releases
- **Actions**: https://github.com/YOUR_ORG/YOUR_REPO/actions
- **Tags**: https://github.com/YOUR_ORG/YOUR_REPO/tags

---

**Last Updated**: 2025-11-14  
**Maintained By**: DevOps Team
