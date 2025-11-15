# GitHub Workflows & CI/CD

## 📁 Contents

- **[ci.yml](./workflows/ci.yml)** - Main CI/CD pipeline
- **[CI_CD_IMPROVEMENTS.md](./CI_CD_IMPROVEMENTS.md)** - Detailed changelog and improvements
- **[BRANCH_PROTECTION_SETUP.md](./BRANCH_PROTECTION_SETUP.md)** - How to configure branch protection
- **[RELEASE_PROCESS.md](./RELEASE_PROCESS.md)** - How to create automated releases

## 🚀 Quick Start

### For Developers

1. **Create a feature branch**
   ```bash
   git checkout -b feature/my-feature
   ```

2. **Make changes and push**
   ```bash
   git add .
   git commit -m "feat: add new feature"
   git push origin feature/my-feature
   ```

3. **Create Pull Request**
   - Go to GitHub and create PR
   - Wait for all CI checks to pass ✅
   - Request review from team members
   - Merge when approved

### For Maintainers

1. **Configure Branch Protection** (one-time setup)
   - See: [BRANCH_PROTECTION_SETUP.md](./BRANCH_PROTECTION_SETUP.md)

2. **Create Releases**
   ```bash
   # Update version in pom.xml
   git tag -a v1.1.0 -m "Release 1.1.0"
   git push origin v1.1.0
   ```
   - See: [RELEASE_PROCESS.md](./RELEASE_PROCESS.md)

## 🔍 CI/CD Pipeline

### Workflow Stages

```
┌─────────────────┐
│  Push / PR      │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Build & Test    │ ← Compile, run tests, check coverage
└────────┬────────┘
         │
    ┌────┴────┬────────────┬──────────────┐
    ▼         ▼            ▼              ▼
┌────────┐ ┌──────┐ ┌──────────┐ ┌──────────┐
│Security│ │Quality│ │   SBOM   │ │ Package  │
│ Scan   │ │Check │ │Generation│ │   JAR    │
└────────┘ └──────┘ └──────────┘ └─────┬────┘
                                        │
                                        ▼
                                  ┌──────────┐
                                  │ Release  │ (on tag)
                                  └──────────┘
```

### Jobs

| Job | Purpose | Blocks Merge? |
|-----|---------|---------------|
| **build-and-test** | Compile, test, coverage | ✅ Yes |
| **security-scan** | Trivy + OWASP checks | ✅ Yes |
| **code-quality** | SpotBugs + Checkstyle | ✅ Yes (SpotBugs) |
| **sbom-generation** | Generate SBOM | ℹ️ No |
| **package** | Create JAR | ℹ️ No |
| **release** | Auto-release on tag | ℹ️ No |

## 📊 Quality Gates

All PRs must pass:

- ✅ **Tests**: All unit tests pass
- ✅ **Coverage**: ≥89% instruction, ≥77% branch
- ✅ **SpotBugs**: No critical bugs
- ✅ **Security**: No critical/high vulnerabilities

## 🛠️ Local Testing

Before pushing, run locally:

```bash
# Full build with all checks
mvn clean verify

# Check coverage
mvn jacoco:check

# Check for bugs
mvn spotbugs:check

# Check style
mvn checkstyle:check
```

## 📈 Monitoring

- **Actions**: https://github.com/YOUR_ORG/YOUR_REPO/actions
- **Releases**: https://github.com/YOUR_ORG/YOUR_REPO/releases
- **Security**: https://github.com/YOUR_ORG/YOUR_REPO/security

## 🆘 Troubleshooting

### CI Failed - What to do?

1. **Click "Details"** on the failed check
2. **Read the error logs**
3. **Common fixes**:
   - Test failure → Fix the test
   - Coverage drop → Add more tests
   - SpotBugs error → Fix the bug
   - Security issue → Update dependency

### Need Help?

- Check: [CI_CD_IMPROVEMENTS.md](./CI_CD_IMPROVEMENTS.md)
- Ask: Team lead or DevOps

---

**Last Updated**: 2025-11-14
