# ADR-005: Immutable Domain Models

## Status

**Accepted** (2024)

## Context

We need to design domain model classes (AnalysisResult, MetricResult, RepositoryInfo, etc.). There are several design approaches:

1. **Mutable POJOs** - Standard Java beans with getters/setters
2. **Immutable objects** - No setters, all fields final
3. **Records (Java 14+)** - Built-in immutable records
4. **Mutable with defensive copying** - Setters but copy on return
5. **Hybrid** - Some mutable, some immutable

Our model requirements:
- Thread-safe (potential for parallelization)
- Predictable behavior (no surprises)
- Clear construction (complex objects need validation)
- Collection safety (lists shouldn't be modified after creation)

## Decision

We will use **immutable domain models** implemented with:
- **Private final fields**
- **No setters**
- **Builder pattern** for complex objects
- **Defensive copying** of collections
- **Package-private constructors** (force builder use)

### Pattern:
```java
public class AnalysisResult {
    private final String repositoryName;
    private final double overallScore;
    private final List<MetricResult> metrics; // unmodifiable
    
    // Package-private constructor
    AnalysisResult(Builder builder) {
        this.repositoryName = requireNonNull(builder.repositoryName);
        this.overallScore = builder.overallScore;
        this.metrics = List.copyOf(builder.metrics); // defensive copy
    }
    
    // Only getters, no setters
    public String getRepositoryName() { return repositoryName; }
    public double getOverallScore() { return overallScore; }
    public List<MetricResult> getMetrics() { return metrics; } // already unmodifiable
    
    // Builder
    public static class Builder { ... }
}
```

## Alternatives Considered

### 1. Mutable POJOs (Java Beans)
**Pros:**
- Familiar pattern
- Less code (just getters/setters)
- Easy serialization
- Flexible

**Cons:**
- Not thread-safe
- Hard to reason about (state changes)
- Can't enforce invariants
- Nullability issues
- Collections can be modified

**Verdict:** Rejected - too many footguns

### 2. Java Records (Java 14+)
**Pros:**
- Concise syntax
- Immutable by default
- Auto-generated equals/hashCode
- Pattern matching support

**Cons:**
- Can't use Builder pattern cleanly
- No validation in constructor
- All fields public (no encapsulation)
- Can't have private constructor
- Less control over construction

**Verdict:** Rejected - need builder pattern for complex objects

### 3. Mutable with Defensive Copying
**Pros:**
- Collection safety on return
- Flexibility to modify internally

**Cons:**
- Still not thread-safe
- Performance cost of copying
- Inconsistent (mutable with immutable facade)

**Verdict:** Rejected - half-measure

### 4. Hybrid Approach
**Pros:**
- Use records for simple DTOs
- Use immutable builders for complex objects

**Cons:**
- Inconsistent patterns
- Confusing for contributors
- Mixed thread-safety guarantees

**Verdict:** Rejected - consistency is important

## Rationale for Immutable Models

1. **Thread Safety**: Immutable objects are inherently thread-safe
2. **Predictability**: Object state never changes after construction
3. **Testability**: Easy to test, no hidden state mutations
4. **Debugging**: State at creation time is state forever
5. **Functional Style**: Enables functional programming patterns
6. **Cache Safety**: Safe to cache without copying
7. **Temporal Coupling**: No order-of-operations issues

## Consequences

### Positive:
✅ **Thread-safe**: No synchronization needed
✅ **Predictable**: State never changes
✅ **Safe sharing**: Can pass references without copying
✅ **Easier testing**: No need to reset state between tests
✅ **No temporal coupling**: Builder validates everything up front
✅ **Collection safety**: Collections truly unmodifiable
✅ **Intent clear**: Constructor + builder show required fields

### Negative:
❌ **More verbose**: Builder pattern adds boilerplate
❌ **No modification**: Must create new object to "change" values
❌ **Learning curve**: Contributors need to understand pattern
❌ **Memory**: Each "change" creates new object (minimal impact)

### Mitigation:
- Use builder pattern to reduce construction verbosity
- Document pattern clearly in contributing guide
- Provide clear examples
- Memory impact is negligible for our use case

## Implementation Guidelines

### 1. Field Requirements:
- All fields must be `private final`
- Collections must be unmodifiable (`List.copyOf()`)
- Use `Objects.requireNonNull()` for non-null fields

### 2. Constructor:
- Package-private (force builder use)
- Takes builder as parameter
- Validates and copies fields

### 3. Getters:
- Public getters for all fields
- NO setters
- Collections returned are already unmodifiable

### 4. Builder:
- Public static inner class
- Mutable during building
- Validates on build()
- Returns immutable instance

### 5. Example:
```java
public class MetricResult {
    private final String metricName;
    private final double score;
    private final List<String> details;
    
    MetricResult(Builder builder) {
        this.metricName = requireNonNull(builder.metricName, "metricName");
        this.score = builder.score;
        this.details = List.copyOf(builder.details);
    }
    
    public String getMetricName() { return metricName; }
    public double getScore() { return score; }
    public List<String> getDetails() { return details; }
    
    public static class Builder {
        private String metricName;
        private double score;
        private List<String> details = new ArrayList<>();
        
        public Builder metricName(String name) {
            this.metricName = name;
            return this;
        }
        
        public Builder score(double score) {
            this.score = score;
            return this;
        }
        
        public Builder details(List<String> details) {
            this.details = new ArrayList<>(details);
            return this;
        }
        
        public MetricResult build() {
            return new MetricResult(this);
        }
    }
}
```

## Special Cases

### 1. Optional Fields:
- Use `@Nullable` annotation or Optional<T>
- Document clearly which fields are optional
- Provide sensible defaults in builder

### 2. Collections:
- ALWAYS use `List.copyOf()` or `Collections.unmodifiableList()`
- NEVER return mutable collections
- Document collection immutability

### 3. Nested Objects:
- Nested objects should also be immutable
- Validate nested objects in constructor

## Testing Immutability

Every model class should have tests verifying:
1. Collections are unmodifiable
2. Getters return consistent values
3. Objects are thread-safe (if relevant)

Example:
```java
@Test
void shouldReturnUnmodifiableMetricsList() {
    AnalysisResult result = new AnalysisResult.Builder()
        .metrics(List.of(metric1, metric2))
        .build();
    
    assertThatThrownBy(() -> result.getMetrics().add(metric3))
        .isInstanceOf(UnsupportedOperationException.class);
}
```

## When to Reconsider

This decision should be reconsidered if:
- We need high-frequency mutations (unlikely for analysis results)
- Memory pressure becomes an issue (very unlikely)
- Team strongly prefers records (could switch for simple DTOs)
- Performance profiling shows copying overhead (never seen in practice)

## Performance Considerations

- **Memory**: Negligible - we create dozens of objects, not millions
- **CPU**: Defensive copying is O(n) but n is small (<100 items)
- **GC**: Modern GCs handle short-lived objects efficiently
- **Conclusion**: Immutability cost is negligible for our use case

## Related Decisions

- [ADR-001: Monolithic CLI Architecture](ADR-001-monolithic-cli-architecture.md)

## References

- [Effective Java, 3rd Edition - Item 17: Minimize Mutability](https://www.oreilly.com/library/view/effective-java/9780134686097/)
- [Java Language Specification - Immutable Objects](https://docs.oracle.com/javase/tutorial/essential/concurrency/immutable.html)
- [Google Java Style Guide - Immutability](https://google.github.io/styleguide/javaguide.html)
