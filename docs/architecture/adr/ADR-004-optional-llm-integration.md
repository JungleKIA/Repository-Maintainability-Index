# ADR-004: Optional LLM Integration

## Status

**Accepted** (2024)

## Context

We have deterministic metrics for repository quality, but they have limitations:
- **Documentation metric** counts file presence but not quality
- **Commit quality** uses regex patterns, misses semantic issues
- **Community health** counts numbers, not sentiment

Large Language Models (LLMs) could provide:
- README readability and clarity analysis
- Commit message semantic quality
- Community tone and responsiveness assessment
- Actionable recommendations

However, LLM integration adds:
- External API dependency
- API costs (per request)
- Latency (2-10 seconds)
- Potential failures
- User concerns about AI

## Decision

We will implement **optional LLM integration** using the OpenRouter API.

### Key Characteristics:
- **Opt-in via `--llm` flag**: Disabled by default
- **Graceful fallback**: LLM failures don't fail the analysis
- **Fail-safe defaults**: Return sensible defaults on API errors
- **Multiple providers**: OpenRouter supports GPT, Claude, etc.
- **No persistent state**: Each analysis is independent

### Design Principles:
1. **Deterministic metrics always work** (core value)
2. **LLM enhances but doesn't replace** (supplementary)
3. **Failures are silent to users** (logged, not shown)
4. **Performance is acceptable** (2-10s for AI insights is reasonable)

## Alternatives Considered

### 1. Always-on LLM Integration
**Pros:**
- Simpler UX (no flag needed)
- All users get AI insights

**Cons:**
- Forces API costs on all users
- Slower for users who don't want AI
- Fails if user has no API key
- Privacy concerns (sends repo data to AI)

**Verdict:** Rejected - too invasive

### 2. Local LLM Integration
**Pros:**
- No external API dependency
- No API costs
- Privacy-friendly
- Offline capable

**Cons:**
- Requires downloading large models (>4GB)
- Slower inference on CPU
- Complex setup for users
- Inconsistent results across hardware

**Verdict:** Rejected - too complex for CLI tool

### 3. No LLM Integration
**Pros:**
- Simplest implementation
- No external dependencies
- Fast and deterministic

**Cons:**
- Misses opportunity for deeper insights
- Can't assess quality, only presence
- Less actionable recommendations

**Verdict:** Rejected - leaves value on the table

### 4. Built-in OpenAI Integration
**Pros:**
- Direct API, no middleman
- Well-documented
- Reliable service

**Cons:**
- Locks users into OpenAI
- More expensive than alternatives
- No model choice flexibility

**Verdict:** Rejected - OpenRouter gives more flexibility

## Rationale for Optional OpenRouter Integration

1. **User Choice**: Deterministic analysis always available, AI optional
2. **Cost Control**: Users control when to incur API costs
3. **Provider Flexibility**: OpenRouter supports 20+ models
4. **Graceful Degradation**: LLM failures don't break the tool
5. **Privacy Respect**: Only sends data when user opts in
6. **Performance Balance**: AI latency acceptable for opt-in feature

## Consequences

### Positive:
✅ **Best of both worlds**: Fast deterministic + deep AI insights
✅ **User control**: Users choose when to use AI
✅ **Cost-effective**: Pay only when needed
✅ **Flexible**: Support multiple AI providers
✅ **Fail-safe**: Core functionality independent of LLM
✅ **Privacy-friendly**: Opt-in data sharing

### Negative:
❌ **Added complexity**: Two code paths (with/without LLM)
❌ **Testing burden**: Need to test fallback scenarios
❌ **Documentation overhead**: Explain how to use LLM features
❌ **Inconsistent experience**: Users without API key get less
❌ **External dependency**: OpenRouter availability matters

### Mitigation:
- Comprehensive testing of fallback paths
- Clear documentation of LLM features
- Demo mode with fallback data for testing
- Graceful error messages about missing API key

## Implementation Details

### Architecture:
```
AnalysisService (core)
    ├─▶ Deterministic Metrics (always)
    └─▶ LLMAnalysisService (optional)
            ├─▶ OpenRouterAPIClient
            └─▶ Fallback on errors
```

### LLM Analysis Components:
1. **README Analysis**:
   - Clarity score (0-100)
   - Completeness score (0-100)
   - Newcomer-friendliness score (0-100)
   - Specific feedback

2. **Commit Quality Analysis**:
   - Pattern consistency
   - Semantic quality
   - Actionable feedback

3. **Community Health Analysis**:
   - Responsiveness assessment
   - Tone evaluation
   - Helpfulness score

4. **AI Recommendations**:
   - Prioritized action items
   - Impact scores (High/Medium/Low)
   - Confidence scores

### Error Handling:
- **No API key**: Show helpful message, continue with deterministic
- **API failure**: Log warning, return defaults, continue
- **Invalid response**: Use fallback values, log for debugging
- **Rate limit**: Show clear message, don't retry

### Performance:
- Deterministic: ~1-2 seconds
- With LLM: ~5-12 seconds (acceptable for opt-in)
- Parallel could reduce (future optimization)

## Security Considerations

1. **API Key Storage**: Environment variable only, never in code
2. **Data Privacy**: User consents by using `--llm` flag
3. **Key Exposure**: OpenRouter disables exposed keys automatically
4. **Transmission**: HTTPS only
5. **Retention**: No data retention on our side

## Cost Analysis

OpenRouter pricing (approximate):
- GPT-3.5 Turbo: $0.002 per analysis
- GPT-4: $0.03 per analysis
- Claude 3: $0.01 per analysis

User cost for 100 analyses:
- With GPT-3.5: $0.20 (negligible)
- With GPT-4: $3.00 (reasonable)

Verdict: **Cost is acceptable** for the value provided

## When to Reconsider

This decision should be reconsidered if:
- OpenRouter becomes unreliable or shuts down
- LLM costs increase dramatically (>10x)
- Local LLMs become fast enough for real-time use
- Users demand always-on AI (unlikely)
- Privacy regulations restrict LLM use

## Future Enhancements

1. **Caching**: Cache LLM results for same commit SHA
2. **Local LLM option**: Support for local models
3. **Parallel requests**: Speed up by parallelizing LLM calls
4. **Custom prompts**: Allow users to customize AI prompts
5. **Streaming**: Stream AI responses for perceived speed

## Related Decisions

- [ADR-001: Monolithic CLI Architecture](ADR-001-monolithic-cli-architecture.md)
- [ADR-003: Weighted Metrics System](ADR-003-weighted-metrics-system.md)

## References

- [OpenRouter API Documentation](https://openrouter.ai/docs)
- [LLM Security Best Practices](https://owasp.org/www-project-top-10-for-large-language-model-applications/)
