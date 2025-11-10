# ADR-003: Weighted Metrics System

## Status

**Accepted** (2024)

## Context

We need to calculate an overall "maintainability score" from multiple individual metrics. There are several approaches:

1. **Equal weighting** - All metrics contribute equally
2. **Weighted scoring** - Different metrics have different importance
3. **Categorical assessment** - No numeric score, just categories (Good/Fair/Poor)
4. **ML-based scoring** - Train model to predict maintainability
5. **Checklist approach** - Boolean pass/fail for each metric

Each metric evaluates a different aspect:
- Documentation (README, LICENSE, etc.)
- Commit Quality (message conventions)
- Activity (recent commits)
- Issue Management (closure rate)
- Community (stars, forks, contributors)
- Branch Management (number of branches)

## Decision

We will use a **weighted scoring system** where:
- Each metric produces a score from 0-100
- Each metric has a weight (sum = 100%)
- Overall score = Σ(metric_score × weight)

### Weight Distribution:

| Metric | Weight | Rationale |
|--------|--------|-----------|
| Documentation | 20% | Critical for maintainability |
| Issue Management | 20% | Indicates responsiveness |
| Commit Quality | 15% | Reflects discipline |
| Activity | 15% | Shows project health |
| Community | 15% | Indicates adoption |
| Branch Management | 15% | Reduces complexity |

### Rating Scale:
- 90-100: Excellent
- 75-89: Good
- 60-74: Fair
- 0-59: Poor

## Alternatives Considered

### 1. Equal Weighting
**Pros:**
- Simpler to understand
- No subjective weight decisions
- All metrics "equal importance"

**Cons:**
- Doesn't reflect reality (documentation > branch count)
- Can't express domain knowledge
- Less actionable results

**Verdict:** Rejected - oversimplification

### 2. Categorical Assessment
**Pros:**
- No arbitrary numeric scores
- Clear buckets
- Less false precision

**Cons:**
- Harder to compare repositories
- Loss of information
- Difficult to show improvement

**Verdict:** Rejected - users want numeric scores

### 3. ML-Based Scoring
**Pros:**
- Data-driven weights
- Could learn from real maintainability issues
- Potentially more accurate

**Cons:**
- Requires training data
- Black box model
- Hard to explain to users
- Overkill for 6 metrics

**Verdict:** Rejected - unnecessary complexity

### 4. Checklist Approach
**Pros:**
- Binary is clear
- Easy to implement
- No scoring ambiguity

**Cons:**
- All-or-nothing is too harsh
- Can't show gradual improvement
- Doesn't reflect partial compliance

**Verdict:** Rejected - too rigid

## Rationale for Weighted System

1. **Expresses Domain Knowledge**: Documentation and issue management are more important than branch count
2. **Granular Feedback**: Scores show degree of compliance, not just pass/fail
3. **Comparable**: Numeric scores enable repository comparison
4. **Actionable**: Users can see which low-scoring metrics to improve
5. **Industry Standard**: Many tools use weighted scoring (SonarQube, CodeClimate)
6. **Tunable**: Weights can be adjusted based on feedback

## Consequences

### Positive:
✅ **Meaningful scores**: Reflect real importance of metrics
✅ **Actionable insights**: Users can prioritize improvements
✅ **Comparable results**: Can benchmark repositories
✅ **Industry familiar**: Similar to other quality tools
✅ **Flexible**: Easy to adjust weights

### Negative:
❌ **Subjective weights**: Weight choices are judgment calls
❌ **False precision**: Score 73 vs 74 isn't meaningfully different
❌ **Gaming potential**: Users might optimize for score, not quality
❌ **Context blind**: Same weights for all project types

### Mitigation:
- Document weight rationale clearly
- Consider allowing custom weights (future feature)
- Emphasize scores are guidelines, not absolutes
- Provide detailed per-metric breakdowns

## Weight Selection Process

Weights were chosen based on:
1. **Literature review**: Industry best practices
2. **Expert judgment**: Software engineering principles
3. **Balance**: No single metric dominates
4. **Round numbers**: Easy to understand (15%, 20%)

### Top Tier (20%):
- **Documentation**: Universal agreement on importance
- **Issue Management**: Direct indicator of maintainability

### Mid Tier (15%):
- **Commit Quality**: Important but not critical
- **Activity**: Shows health but can be misleading (mature projects)
- **Community**: Indicates adoption but not quality
- **Branch Management**: Relevant but lower impact

## Future Considerations

Potential enhancements:
1. **Configurable weights**: Allow users to specify custom weights
2. **Project type profiles**: Different weights for libraries vs applications
3. **Time-based weighting**: Recent issues weighted more
4. **Confidence intervals**: Show score uncertainty
5. **Trend analysis**: Compare score over time

## When to Reconsider

This decision should be reconsidered if:
- User feedback indicates weights are significantly off
- Research shows better weighting schemes
- We add many more metrics (might need factor analysis)
- Specific use cases need different weight profiles

## Related Decisions

- [ADR-001: Monolithic CLI Architecture](ADR-001-monolithic-cli-architecture.md)
- [ADR-004: Optional LLM Integration](ADR-004-optional-llm-integration.md)

## References

- [SonarQube Quality Model](https://docs.sonarqube.org/latest/user-guide/metric-definitions/)
- [Code Climate Maintainability](https://docs.codeclimate.com/docs/maintainability)
- ISO/IEC 25010 Software Quality Model
