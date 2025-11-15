# GitHub Branch Protection Setup

## Required Configuration

To ensure code quality and prevent broken code from being merged, configure the following branch protection rules:

### 1. Navigate to Repository Settings

1. Go to your repository on GitHub
2. Click **Settings** → **Branches**
3. Click **Add rule** or edit existing rule for `main` branch

### 2. Configure Protection Rules

#### Branch name pattern
```
main
```

#### Required Status Checks

Enable: **Require status checks to pass before merging**

Select the following required checks:
- ✅ `build-and-test` - Ensures code compiles and all tests pass
- ✅ `code-quality` - Ensures no critical bugs (SpotBugs)
- ✅ `security-scan` - Ensures no critical vulnerabilities

Optional checks (informational only):
- ℹ️ `sbom-generation` - Generates Software Bill of Materials
- ℹ️ `package` - Creates JAR artifact

#### Additional Recommended Settings

- ✅ **Require branches to be up to date before merging**
  - Ensures the PR is tested against the latest main branch
  
- ✅ **Require pull request reviews before merging**
  - Minimum: 1 approving review
  
- ✅ **Dismiss stale pull request approvals when new commits are pushed**
  - Ensures reviews are for the latest code
  
- ✅ **Require conversation resolution before merging**
  - All review comments must be resolved

- ✅ **Do not allow bypassing the above settings**
  - Applies to administrators too

### 3. Repeat for `develop` Branch

Create the same rules for the `develop` branch to maintain quality across all main branches.

## What This Protects Against

| Protection | Prevents |
|------------|----------|
| `build-and-test` | Broken code, failing tests, insufficient code coverage |
| `code-quality` | Critical bugs detected by SpotBugs |
| `security-scan` | Known security vulnerabilities in dependencies |
| Require reviews | Unreviewed code changes |
| Up-to-date branch | Merge conflicts and integration issues |

## Testing the Setup

1. Create a test branch: `git checkout -b test-protection`
2. Make a change that breaks tests
3. Push and create a PR
4. Verify that GitHub blocks merging until checks pass

## Troubleshooting

### "Required status check is not present"

If you see this error, it means the CI workflow hasn't run yet for this branch. Push a commit to trigger the workflow.

### "Status check failed"

Click on "Details" next to the failed check to see the error logs. Common issues:
- Test failures → Fix the failing tests
- SpotBugs errors → Fix the reported bugs
- Coverage below threshold → Add more tests

## Emergency Bypass

In rare cases where you need to bypass protection (e.g., critical hotfix):

1. Only administrators can bypass if configured
2. Document the reason in the PR description
3. Create a follow-up issue to address the bypassed checks

## Monitoring

- Check the **Actions** tab regularly for failed workflows
- Review **Security** tab for vulnerability alerts
- Monitor **Insights** → **Pulse** for merge activity

---

**Last Updated**: 2025-11-14  
**Maintained By**: DevOps Team
