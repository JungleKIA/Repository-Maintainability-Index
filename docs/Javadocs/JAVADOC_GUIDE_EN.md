# Comprehensive Javadoc Guide (English)

## Table of Contents
1. [General Principles and Structure](#1-general-principles-and-structure)
2. [Documenting Classes and Interfaces](#2-documenting-classes-and-interfaces)
3. [Documenting Enums](#3-documenting-enums)
4. [Documenting Records](#4-documenting-records)
5. [Documenting Annotations](#5-documenting-annotations)
6. [Documenting Fields](#6-documenting-fields)
7. [Documenting Methods and Constructors](#7-documenting-methods-and-constructors)
   - 7.7 [Documenting Getters and Setters](#77-documenting-getters-and-setters)
8. [Documenting Generic Types and Type Parameters](#8-documenting-generic-types-and-type-parameters)
9. [Package Documentation (package-info.java)](#9-package-documentation-package-infojava)
10. [Module Documentation (module-info.java)](#10-module-documentation-module-infojava)
11. [Block Tags Usage](#11-block-tags-usage)
12. [Inline Tags Usage](#12-inline-tags-usage)
13. [Good vs Bad Examples](#13-good-vs-bad-examples)
14. [Common Mistakes and Best Practices Checklist](#14-common-mistakes-and-best-practices-checklist)

---

## 1. General Principles and Structure

### 1.1 Summary Sentence Rule

The first sentence of every Javadoc comment is the **summary sentence**. It must:
- Be a complete, grammatically correct sentence
- End with a period (`.`), exclamation mark (`!`), or question mark (`?`)
- Provide a concise overview of the documented element
- Appear in summary listings and search results

**Rules:**
- The summary sentence ends at the first period followed by a space, tab, or line terminator
- Use `{@literal .}` if you need a period that doesn't end the summary
- Keep it concise (ideally under 80 characters)

**Example:**
```java
/**
 * Calculates the factorial of a given number. This method uses
 * an iterative approach for better performance.
 *
 * @param n the number to calculate factorial for
 * @return the factorial result
 */
public long factorial(int n) {
    // implementation
}
```

### 1.2 Main Description

After the summary sentence, provide a detailed description:
- Use multiple sentences to explain functionality, behavior, and context
- Separate paragraphs with `<p>` tags
- Use lists (`<ul>`, `<ol>`) for structured information
- Keep descriptions focused on **what** the code does, not **how**

**Example:**
```java
/**
 * Manages user authentication and session tracking.
 * <p>
 * This class provides methods to:
 * <ul>
 *   <li>Authenticate users against multiple identity providers</li>
 *   <li>Manage session lifecycle and timeout</li>
 *   <li>Handle password reset workflows</li>
 * </ul>
 * <p>
 * All methods in this class are thread-safe and can be called
 * from multiple threads concurrently.
 */
public class AuthenticationManager {
    // implementation
}
```

### 1.3 Safe HTML Tag Usage

**Allowed HTML tags** (most common):
- Paragraphs: `<p>`, `</p>`
- Lists: `<ul>`, `<ol>`, `<li>`
- Text formatting: `<b>`, `<i>`, `<strong>`, `<em>`, `<code>`
- Line breaks: `<br>`
- Tables: `<table>`, `<tr>`, `<td>`, `<th>`
- Headings: `<h1>` through `<h6>`
- Links: `<a href="...">`

**Important rules:**
- Always close tags properly (`<p>...</p>`)
- Use `{@code ...}` instead of `<code>` for Java code snippets
- Don't use deprecated HTML tags (`<font>`, `<center>`, etc.)

### 1.4 Special Character Escaping

Special characters that need escaping:
- `<` → use `&lt;` or `{@literal <}`
- `>` → use `&gt;` or `{@literal >}`
- `&` → use `&amp;` or `{@literal &}`
- `@` → use `{@literal @}` (when not used as a tag)

**Example:**
```java
/**
 * Compares two values using the {@literal <} operator.
 * Returns true if {@code a < b}, false otherwise.
 *
 * @param a the first value
 * @param b the second value
 * @return true if a is less than b
 */
public boolean isLessThan(int a, int b) {
    return a < b;
}
```

---

## 2. Documenting Classes and Interfaces

### 2.1 Purpose Description

Every class and interface must have Javadoc that explains:
- **What** it represents or does
- **Why** it exists (its role in the system)
- **When** to use it
- Key constraints or assumptions

### 2.2 @author and @version Tags - Modern Approach

**Modern consensus: These tags are OPTIONAL and often discouraged.**

#### Why @version is problematic:

❌ **Redundant**: Version info should be managed at project/artifact level (Maven/Gradle)
❌ **Maintenance burden**: Requires manual updates (everyone forgets)
❌ **Conflicts**: Duplicates build system versioning
❌ **No practical value**: Git history provides better tracking

**When to use @version:**
- ✅ Public APIs with formal versioning policies
- ✅ Libraries with semantic versioning commitments
- ✅ When explicitly required by organizational standards

#### Why @author is problematic:

❌ **Quickly outdated**: Code changes hands, multiple contributors
❌ **Version control is better**: Git blame/log provides accurate history
❌ **Ownership issues**: Can create false sense of "ownership"
❌ **Maintenance nightmare**: Who updates when refactoring?

**When to use @author:**
- ✅ Academic or educational code
- ✅ When required by organizational policy
- ✅ Initial implementation credit (but Git is still better)

**Recommended approach:**
```java
/**
 * Manages user authentication and session tracking.
 * <p>
 * This service handles authentication through multiple providers
 * including OAuth2, SAML, and legacy username/password.
 * <p>
 * Thread-safe and designed for high-concurrency environments.
 *
 * @since 1.0
 * @see AuthenticationProvider
 * @see SessionManager
 */
public class AuthenticationService {
    // implementation
}
```

### 2.3 Usage Examples

Include code examples for complex classes:

```java
/**
 * A builder for creating immutable {@link UserProfile} instances.
 * <p>
 * This builder provides a fluent API for constructing user profiles
 * with validation and default values.
 * <p>
 * Example usage:
 * <pre>{@code
 * UserProfile profile = new UserProfileBuilder()
 *     .withUsername("john.doe")
 *     .withEmail("john@example.com")
 *     .withRole(Role.ADMIN)
 *     .build();
 * }</pre>
 *
 * @since 1.5
 * @see UserProfile
 */
public class UserProfileBuilder {
    // implementation
}
```

### 2.4 Interface Documentation

Document interfaces with focus on contract and expectations:

```java
/**
 * Defines the contract for data persistence operations.
 * <p>
 * Implementations of this interface must ensure that:
 * <ul>
 *   <li>All operations are atomic</li>
 *   <li>Failed operations throw specific exceptions</li>
 *   <li>Null values are handled according to method documentation</li>
 * </ul>
 * <p>
 * Implementations should be thread-safe unless explicitly documented otherwise.
 *
 * @param <T> the type of entity this repository manages
 * @since 1.0
 * @see Repository
 */
public interface DataRepository<T> {
    // method declarations
}
```

---

## 3. Documenting Enums

### 3.1 Enum Class Documentation

Document the overall purpose and usage of the enum:

```java
/**
 * Defines the possible states of a user account.
 * <p>
 * These states control what operations a user can perform
 * and how the system interacts with the account. State transitions
 * are managed by {@link AccountService}.
 *
 * @since 1.0
 * @see AccountService#changeStatus(Account, AccountStatus)
 */
public enum AccountStatus {
    // constants
}
```

### 3.2 Enum Constants Documentation

**Every enum constant must be documented:**

```java
/**
 * Defines HTTP request methods as per RFC 7231.
 *
 * @since 1.0
 * @see <a href="https://tools.ietf.org/html/rfc7231#section-4">RFC 7231 Section 4</a>
 */
public enum HttpMethod {
    
    /**
     * The GET method requests transfer of a current selected representation
     * for the target resource.
     * <p>
     * GET is the primary mechanism for information retrieval and is
     * considered safe and idempotent.
     */
    GET,
    
    /**
     * The POST method requests that the target resource process the
     * representation enclosed in the request.
     * <p>
     * POST is typically used for creating new resources. It is neither
     * safe nor idempotent.
     */
    POST,
    
    /**
     * The PUT method requests that the state of the target resource be
     * created or replaced with the state defined by the representation.
     * <p>
     * PUT is idempotent but not safe.
     */
    PUT,
    
    /**
     * The DELETE method requests that the target resource be removed.
     * <p>
     * DELETE is idempotent but not safe.
     */
    DELETE,
    
    /**
     * The PATCH method requests that a set of changes described in the
     * request be applied to the target resource.
     * <p>
     * PATCH is neither safe nor idempotent.
     */
    PATCH;
    
    /**
     * Checks if this HTTP method is idempotent.
     * <p>
     * An idempotent method produces the same result when called multiple
     * times with the same parameters.
     *
     * @return {@code true} if this method is idempotent, {@code false} otherwise
     */
    public boolean isIdempotent() {
        return this == GET || this == PUT || this == DELETE;
    }
}
```

### 3.3 Enums with Fields and Methods

Document additional members:

```java
/**
 * HTTP status codes as defined in RFC 7231 and related specifications.
 *
 * @since 1.0
 */
public enum HttpStatus {
    
    /**
     * 200 OK - The request has succeeded.
     */
    OK(200, "OK"),
    
    /**
     * 201 Created - The request has been fulfilled and resulted in a new
     * resource being created.
     */
    CREATED(201, "Created"),
    
    /**
     * 400 Bad Request - The request could not be understood by the server
     * due to malformed syntax.
     */
    BAD_REQUEST(400, "Bad Request"),
    
    /**
     * 404 Not Found - The server has not found anything matching the
     * Request-URI.
     */
    NOT_FOUND(404, "Not Found"),
    
    /**
     * 500 Internal Server Error - The server encountered an unexpected
     * condition that prevented it from fulfilling the request.
     */
    INTERNAL_SERVER_ERROR(500, "Internal Server Error");
    
    /**
     * The numeric status code.
     */
    private final int code;
    
    /**
     * The reason phrase associated with this status code.
     */
    private final String reasonPhrase;
    
    /**
     * Constructs a new HTTP status with the specified code and reason phrase.
     *
     * @param code the numeric status code
     * @param reasonPhrase the textual reason phrase
     */
    HttpStatus(int code, String reasonPhrase) {
        this.code = code;
        this.reasonPhrase = reasonPhrase;
    }
    
    /**
     * Returns the numeric status code.
     *
     * @return the status code
     */
    public int getCode() {
        return code;
    }
    
    /**
     * Returns the reason phrase associated with this status code.
     *
     * @return the reason phrase, never null
     */
    public String getReasonPhrase() {
        return reasonPhrase;
    }
    
    /**
     * Checks if this status code represents a successful response (2xx).
     *
     * @return {@code true} if status code is between 200 and 299 inclusive
     */
    public boolean isSuccessful() {
        return code >= 200 && code < 300;
    }
}
```

---

## 4. Documenting Records

Records (introduced in Java 14, finalized in Java 16) are special classes for immutable data. They require specific documentation approaches.

### 4.1 Record Class Documentation

```java
/**
 * Represents an immutable point in 2D space.
 * <p>
 * This record provides a compact representation of a coordinate
 * pair with automatic implementations of {@code equals()},
 * {@code hashCode()}, and {@code toString()}.
 * <p>
 * Example usage:
 * <pre>{@code
 * Point p1 = new Point(10, 20);
 * Point p2 = new Point(10, 20);
 * assert p1.equals(p2); // true
 * }</pre>
 *
 * @param x the x-coordinate, can be any double value including infinity
 * @param y the y-coordinate, can be any double value including infinity
 * @since 2.0
 */
public record Point(double x, double y) {
    // compact constructor or methods
}
```

**Important for records:**
- Document record components using `@param` tags in the class-level Javadoc
- These automatically apply to the canonical constructor and accessor methods

### 4.2 Record with Validation (Compact Constructor)

```java
/**
 * Represents an email address with validation.
 * <p>
 * This record ensures that the email address conforms to basic
 * format requirements. Validation is performed in the compact constructor.
 *
 * @param address the email address string, must not be null or empty
 *                and must contain exactly one {@literal @} symbol
 * @throws NullPointerException if address is null
 * @throws IllegalArgumentException if address is empty or invalid format
 * @since 2.1
 */
public record Email(String address) {
    
    /**
     * Compact constructor that validates the email address format.
     *
     * @throws NullPointerException if address is null
     * @throws IllegalArgumentException if address is empty or invalid
     */
    public Email {
        if (address == null) {
            throw new NullPointerException("Email address cannot be null");
        }
        if (address.isBlank()) {
            throw new IllegalArgumentException("Email address cannot be empty");
        }
        if (address.chars().filter(ch -> ch == '@').count() != 1) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }
    
    /**
     * Returns the domain part of the email address.
     * <p>
     * For example, for "user@example.com" this returns "example.com".
     *
     * @return the domain part, never null or empty
     */
    public String domain() {
        return address.substring(address.indexOf('@') + 1);
    }
}
```

### 4.3 Record with Custom Methods

```java
/**
 * Represents a rational number as an immutable fraction.
 * <p>
 * This record stores a numerator and denominator and ensures
 * the fraction is always in simplified form with positive denominator.
 *
 * @param numerator the numerator of the fraction
 * @param denominator the denominator of the fraction, must not be zero
 * @throws IllegalArgumentException if denominator is zero
 * @since 2.0
 */
public record Rational(int numerator, int denominator) {
    
    /**
     * Canonical constructor that normalizes the fraction.
     * <p>
     * Ensures denominator is positive and reduces the fraction to
     * lowest terms.
     *
     * @throws IllegalArgumentException if denominator is zero
     */
    public Rational {
        if (denominator == 0) {
            throw new IllegalArgumentException("Denominator cannot be zero");
        }
        
        // Normalize: ensure denominator is positive
        if (denominator < 0) {
            numerator = -numerator;
            denominator = -denominator;
        }
        
        // Reduce to lowest terms
        int gcd = gcd(Math.abs(numerator), denominator);
        numerator /= gcd;
        denominator /= gcd;
    }
    
    /**
     * Adds this rational number to another.
     *
     * @param other the rational number to add, must not be null
     * @return a new {@code Rational} representing the sum
     * @throws NullPointerException if other is null
     */
    public Rational add(Rational other) {
        return new Rational(
            this.numerator * other.denominator + other.numerator * this.denominator,
            this.denominator * other.denominator
        );
    }
    
    /**
     * Computes the greatest common divisor using Euclid's algorithm.
     *
     * @param a the first number, must be non-negative
     * @param b the second number, must be non-negative
     * @return the GCD of a and b
     */
    private static int gcd(int a, int b) {
        return b == 0 ? a : gcd(b, a % b);
    }
}
```

---

## 5. Documenting Annotations

Annotations require documentation for the annotation itself and each of its elements.

### 5.1 Marker Annotation (No Elements)

```java
/**
 * Indicates that a method should not be called directly by application code.
 * <p>
 * This annotation serves as documentation that a method is intended for
 * framework or infrastructure use only. Methods marked with this annotation
 * may have special requirements or side effects.
 * <p>
 * Example usage:
 * <pre>{@code
 * @Internal
 * public void initializeFramework() {
 *     // framework initialization
 * }
 * }</pre>
 *
 * @since 1.5
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Internal {
}
```

### 5.2 Single-Value Annotation

```java
/**
 * Specifies the maximum number of retry attempts for an operation.
 * <p>
 * This annotation can be applied to methods to configure automatic
 * retry behavior in case of transient failures.
 * <p>
 * Example usage:
 * <pre>{@code
 * @MaxRetries(3)
 * public void connectToDatabase() {
 *     // connection logic
 * }
 * }</pre>
 *
 * @since 2.0
 * @see RetryPolicy
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MaxRetries {
    
    /**
     * The maximum number of retry attempts.
     * <p>
     * Must be a non-negative integer. A value of 0 means no retries
     * (fail immediately on first error).
     *
     * @return the maximum retry count
     */
    int value();
}
```

### 5.3 Multi-Element Annotation

```java
/**
 * Configures caching behavior for a method or class.
 * <p>
 * This annotation enables automatic caching of method results with
 * configurable expiration and key generation strategies.
 * <p>
 * Example usage:
 * <pre>{@code
 * @Cacheable(
 *     key = "user",
 *     ttl = 3600,
 *     unit = TimeUnit.SECONDS
 * )
 * public User findUserById(Long id) {
 *     return database.query(id);
 * }
 * }</pre>
 *
 * @since 2.5
 * @see CacheManager
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Cacheable {
    
    /**
     * The cache key prefix.
     * <p>
     * This value is combined with method parameters to generate
     * unique cache keys. If empty, the fully qualified method name
     * is used.
     *
     * @return the cache key prefix
     */
    String key() default "";
    
    /**
     * The time-to-live for cached values.
     * <p>
     * After this duration, cached values are considered stale and
     * will be recomputed. Must be positive.
     *
     * @return the TTL value
     */
    long ttl() default 300;
    
    /**
     * The time unit for the TTL value.
     *
     * @return the time unit, never null
     */
    TimeUnit unit() default TimeUnit.SECONDS;
    
    /**
     * Whether to cache null values.
     * <p>
     * If {@code false}, methods returning null will not be cached
     * and will be invoked on every call.
     *
     * @return {@code true} to cache null values, {@code false} otherwise
     */
    boolean cacheNulls() default false;
    
    /**
     * Conditions under which caching should be applied.
     * <p>
     * This is a SpEL expression evaluated at runtime. Caching only
     * occurs if the expression evaluates to {@code true}.
     *
     * @return the condition expression, empty string means always cache
     */
    String condition() default "";
}
```

### 5.4 Annotation with Array Elements

```java
/**
 * Specifies roles required to access a resource or execute a method.
 * <p>
 * This annotation is used for declarative security configuration.
 * The user must have at least one of the specified roles.
 * <p>
 * Example usage:
 * <pre>{@code
 * @RequiresRoles({"ADMIN", "MANAGER"})
 * public void deleteUser(Long userId) {
 *     userRepository.delete(userId);
 * }
 * }</pre>
 *
 * @since 1.0
 * @see SecurityContext
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface RequiresRoles {
    
    /**
     * The roles required to access the annotated element.
     * <p>
     * The user must have at least one of these roles. An empty
     * array means no roles are required (equivalent to public access).
     * <p>
     * Role names are case-sensitive and should match the roles
     * defined in your security configuration.
     *
     * @return array of required role names, never null but can be empty
     */
    String[] value();
    
    /**
     * Whether the user must have ALL specified roles (AND logic)
     * or just ONE of them (OR logic).
     * <p>
     * Default is OR logic (user needs any one role).
     *
     * @return {@code true} for AND logic, {@code false} for OR logic
     */
    boolean requireAll() default false;
}
```

---

## 6. Documenting Fields

Fields should be documented, especially:
- Public and protected fields
- Public static final constants
- Fields with non-obvious purpose

### 6.1 Constant Fields

```java
/**
 * Configuration constants for the application.
 */
public class AppConstants {
    
    /**
     * The maximum number of connection attempts before giving up.
     * <p>
     * Value: {@value}
     */
    public static final int MAX_CONNECTION_ATTEMPTS = 3;
    
    /**
     * Default timeout for network operations in milliseconds.
     * <p>
     * This timeout applies to both connection and read operations
     * unless overridden by specific configuration.
     * <p>
     * Value: {@value}
     */
    public static final long DEFAULT_TIMEOUT_MS = 30_000L;
    
    /**
     * The application version string in semantic versioning format.
     * <p>
     * Format: MAJOR.MINOR.PATCH
     * <p>
     * Value: {@value}
     */
    public static final String VERSION = "2.1.0";
    
    /**
     * Regular expression pattern for validating email addresses.
     * <p>
     * This is a simplified pattern for basic email validation.
     * For production use, consider more comprehensive validation.
     * <p>
     * Value: {@value}
     */
    public static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    
    /**
     * Private constructor to prevent instantiation.
     * <p>
     * This is a utility class with only static members.
     *
     * @throws UnsupportedOperationException always
     */
    private AppConstants() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
}
```

### 6.2 Instance Fields (when public/protected)

```java
/**
 * Represents a configuration for database connections.
 */
public class DatabaseConfig {
    
    /**
     * The database host name or IP address.
     * <p>
     * Must not be null or empty. Default is "localhost".
     */
    public String host = "localhost";
    
    /**
     * The database port number.
     * <p>
     * Must be a valid port number (1-65535). Default is 5432 (PostgreSQL).
     */
    public int port = 5432;
    
    /**
     * The database name to connect to.
     * <p>
     * Must not be null or empty.
     */
    public String databaseName;
    
    /**
     * The username for database authentication.
     * <p>
     * Can be null for databases that don't require authentication.
     */
    public String username;
    
    /**
     * The password for database authentication.
     * <p>
     * Can be null for databases that don't require authentication.
     * Should be handled securely and not logged.
     */
    public String password;
    
    /**
     * Maximum number of connections in the pool.
     * <p>
     * Must be positive. Higher values allow more concurrent database
     * operations but consume more resources. Default is 10.
     */
    public int maxPoolSize = 10;
    
    /**
     * Whether to enable SSL/TLS for the connection.
     * <p>
     * When true, all communication with the database is encrypted.
     * Default is false.
     */
    public boolean useSsl = false;
}
```

### 6.3 Fields with Complex Types

```java
/**
 * Manages application-wide caching.
 */
public class CacheManager {
    
    /**
     * The underlying cache storage.
     * <p>
     * This map is thread-safe and supports concurrent access.
     * Keys are cache names, values are the actual cache instances.
     * <p>
     * Never null, but may be empty if no caches are configured.
     */
    private final ConcurrentHashMap<String, Cache> caches = new ConcurrentHashMap<>();
    
    /**
     * The executor service for asynchronous cache operations.
     * <p>
     * Used for background cache refresh and eviction tasks.
     * Initialized lazily on first use.
     * <p>
     * May be null if no async operations have been scheduled yet.
     */
    private volatile ExecutorService asyncExecutor;
    
    /**
     * Statistics collector for cache performance metrics.
     * <p>
     * Tracks hits, misses, evictions, and other metrics.
     * <p>
     * Never null after construction.
     */
    private final CacheStatistics statistics = new CacheStatistics();
}
```

---

## 7. Documenting Methods and Constructors

### 7.1 Describe What, Not How

Focus on the method's contract and behavior, not implementation details:

**Bad:**
```java
/**
 * Loops through the array and adds each element to a sum variable.
 */
public int calculateSum(int[] numbers) {
    // implementation
}
```

**Good:**
```java
/**
 * Calculates the sum of all elements in the given array.
 *
 * @param numbers the array of numbers to sum, must not be null
 * @return the sum of all elements, or 0 if array is empty
 * @throws NullPointerException if numbers is null
 */
public int calculateSum(int[] numbers) {
    // implementation
}
```

### 7.2 Side Effects and Behavior Specifics

Always document:
- Thread safety characteristics
- Modifications to parameters or object state
- Network or I/O operations
- Long-running operations
- Caching behavior

**Example:**
```java
/**
 * Sends an email notification to the specified recipient.
 * <p>
 * This method performs network I/O and may block for several seconds.
 * Consider calling from a background thread for better responsiveness.
 * <p>
 * This method is thread-safe and can be called concurrently from
 * multiple threads.
 *
 * @param recipient the email address of the recipient, must not be null or empty
 * @param subject the email subject line, must not be null
 * @param body the email body content, must not be null
 * @throws IllegalArgumentException if recipient is not a valid email address
 * @throws EmailServiceException if the email cannot be sent due to network
 *         or service errors
 */
public void sendEmail(String recipient, String subject, String body) {
    // implementation
}
```

### 7.3 Detailed @param Documentation

Each `@param` tag must:
- Describe what the parameter represents
- Specify constraints (null/not null, range, format)
- Explain the parameter's role in the operation

**Example:**
```java
/**
 * Creates a new user account with the specified details.
 *
 * @param username the unique username for the account, must not be null,
 *                 must be between 3 and 20 characters, and contain only
 *                 alphanumeric characters and underscores
 * @param email the user's email address, must not be null and must be
 *              a valid email format
 * @param age the user's age in years, must be between 13 and 150 (inclusive)
 * @return the newly created user account with generated ID
 * @throws IllegalArgumentException if any parameter violates its constraints
 * @throws DuplicateUsernameException if the username is already taken
 */
public UserAccount createUser(String username, String email, int age) {
    // implementation
}
```

### 7.4 Clear @return Documentation

Document the return value including:
- What it represents
- Possible special values (null, empty, -1, etc.)
- Edge cases
- When null or empty might be returned

**Example:**
```java
/**
 * Searches for a user by their username.
 *
 * @param username the username to search for, must not be null or empty
 * @return the user account if found, or null if no user exists with
 *         the given username
 * @throws IllegalArgumentException if username is null or empty
 */
public UserAccount findUserByUsername(String username) {
    // implementation
}

/**
 * Retrieves all active user accounts.
 *
 * @return an unmodifiable list of active users, empty if no active
 *         users exist, never null
 */
public List<UserAccount> getActiveUsers() {
    // implementation
}
```

### 7.5 Comprehensive @throws Documentation

Document **all** exceptions that the method can throw:
- **Checked exceptions**: must be documented
- **Unchecked exceptions**: should be documented if they're part of the contract
- Include the **condition** that causes each exception

**Example:**
```java
/**
 * Processes a payment transaction.
 *
 * @param accountId the account to charge, must not be null
 * @param amount the payment amount, must be positive
 * @param currency the currency code (e.g., "USD", "EUR"), must not be null
 * @return the transaction receipt with confirmation number
 * @throws NullPointerException if accountId or currency is null
 * @throws IllegalArgumentException if amount is zero or negative, or if
 *         currency is not a valid ISO 4217 code
 * @throws AccountNotFoundException if no account exists with the given ID
 * @throws InsufficientFundsException if the account balance is less than
 *         the requested amount
 * @throws PaymentProcessingException if the payment cannot be processed
 *         due to a system error
 */
public Receipt processPayment(String accountId, BigDecimal amount, String currency)
        throws AccountNotFoundException, InsufficientFundsException,
               PaymentProcessingException {
    // implementation
}
```

### 7.6 Constructor Documentation

Document constructors similarly to methods:

```java
/**
 * Creates a new shopping cart for the specified user.
 * <p>
 * The cart is initialized empty and ready to accept items.
 *
 * @param userId the ID of the user who owns this cart, must not be null
 * @param sessionId the current session identifier, must not be null or empty
 * @throws NullPointerException if userId is null
 * @throws IllegalArgumentException if sessionId is null or empty
 */
public ShoppingCart(String userId, String sessionId) {
    // implementation
}
```

### 7.7 Documenting Getters and Setters

Getters and setters **should be documented**, but the level of detail depends on their complexity.

#### Simple Getters

For **simple getters** (direct field access with no logic), provide concise documentation:

**Minimum required:**
```java
/**
 * Returns the user's email address.
 *
 * @return the email address, or null if not set
 */
public String getEmail() {
    return email;
}
```

**Better with details:**
```java
/**
 * Returns the user's email address.
 * <p>
 * The email is validated at the time of setting and guaranteed
 * to be in valid format if not null.
 *
 * @return the email address, or null if not set
 */
public String getEmail() {
    return email;
}
```

#### Simple Setters

For **simple setters** (direct field assignment), document constraints and validation:

**Minimum required:**
```java
/**
 * Sets the user's email address.
 *
 * @param email the email address, can be null
 */
public void setEmail(String email) {
    this.email = email;
}
```

**Better with validation rules:**
```java
/**
 * Sets the user's email address.
 * <p>
 * The email must be in valid format (user@domain.tld).
 * Setting null clears the email address.
 *
 * @param email the email address, can be null to clear
 * @throws IllegalArgumentException if email is not null and invalid format
 */
public void setEmail(String email) {
    if (email != null && !isValidEmail(email)) {
        throw new IllegalArgumentException("Invalid email format");
    }
    this.email = email;
}
```

#### Complex Getters

For getters with **computation, caching, or lazy initialization**:

```java
/**
 * Returns the user's full name.
 * <p>
 * This method constructs the full name from first and last name
 * components. If either component is null, returns only the non-null
 * part. If both are null, returns empty string.
 * <p>
 * The result is cached after first computation for performance.
 *
 * @return the full name, never null but can be empty
 */
public String getFullName() {
    if (fullNameCache == null) {
        fullNameCache = buildFullName();
    }
    return fullNameCache;
}
```

#### Complex Setters

For setters with **validation, side effects, or cascading updates**:

```java
/**
 * Sets the user's status.
 * <p>
 * Changing the status triggers several side effects:
 * <ul>
 *   <li>Updates the last modified timestamp</li>
 *   <li>Clears the session if status is SUSPENDED or INACTIVE</li>
 *   <li>Sends notification to user if status changes</li>
 *   <li>Logs the status change for audit purposes</li>
 * </ul>
 * <p>
 * This method is thread-safe but may block briefly for notification delivery.
 *
 * @param status the new status, must not be null
 * @throws NullPointerException if status is null
 * @throws IllegalStateException if current status doesn't allow transition
 *         to the new status
 */
public void setStatus(UserStatus status) {
    validateStatusTransition(this.status, status);
    this.status = status;
    this.lastModified = Instant.now();
    handleStatusChange(status);
}
```

#### Boolean Getters (is/has prefixes)

Boolean getters commonly use `is` or `has` prefixes:

```java
/**
 * Checks if the user account is active.
 * <p>
 * An account is considered active if the status is ACTIVE
 * and the account is not expired.
 *
 * @return {@code true} if account is active, {@code false} otherwise
 */
public boolean isActive() {
    return status == Status.ACTIVE && !isExpired();
}

/**
 * Checks if the user has administrative privileges.
 *
 * @return {@code true} if user is admin, {@code false} otherwise
 */
public boolean hasAdminPrivileges() {
    return role == Role.ADMIN || role == Role.SUPER_ADMIN;
}
```

#### Collection Getters

For getters returning collections, specify mutability:

```java
/**
 * Returns the list of user's roles.
 * <p>
 * The returned list is unmodifiable. Use {@link #addRole(Role)}
 * to add roles.
 *
 * @return unmodifiable list of roles, never null but can be empty
 */
public List<Role> getRoles() {
    return Collections.unmodifiableList(roles);
}
```

**With defensive copy:**
```java
/**
 * Returns the user's addresses.
 * <p>
 * Returns a defensive copy of the internal address list.
 * Modifications to the returned list do not affect this user.
 *
 * @return a new list containing all addresses, never null but can be empty
 */
public List<Address> getAddresses() {
    return new ArrayList<>(addresses);
}
```

#### Fluent Setters (Builder Pattern)

For setters that return `this` for chaining:

```java
/**
 * Sets the user's email address.
 * <p>
 * This is a fluent setter that returns this instance for method chaining.
 *
 * @param email the email address, must not be null
 * @return this user instance for method chaining
 * @throws NullPointerException if email is null
 * @throws IllegalArgumentException if email format is invalid
 */
public User setEmail(String email) {
    if (email == null) {
        throw new NullPointerException("Email cannot be null");
    }
    if (!isValidEmail(email)) {
        throw new IllegalArgumentException("Invalid email format");
    }
    this.email = email;
    return this;
}
```

#### When to Skip Javadoc for Getters/Setters

**Never skip in public APIs**, but for **internal DTOs or simple POJOs**, you might use:

```java
// For very simple, self-explanatory getters/setters in internal code:

/** @return the name */
public String getName() { return name; }

/** @param name the name */
public void setName(String name) { this.name = name; }
```

**However, even for simple cases, it's better to be explicit:**

```java
/**
 * Returns the user name.
 *
 * @return the name, or null if not set
 */
public String getName() { return name; }

/**
 * Sets the user name.
 *
 * @param name the name, can be null
 */
public void setName(String name) { this.name = name; }
```

#### Complete Example: JavaBean with Getters/Setters

```java
/**
 * Represents a user account in the system.
 * <p>
 * This is a mutable JavaBean with standard getter/setter methods.
 * All setters validate their input before assignment.
 *
 * @since 1.0
 */
public class User {
    
    private String id;
    private String username;
    private String email;
    private UserStatus status;
    private Instant createdAt;
    
    /**
     * Returns the unique user identifier.
     *
     * @return the user ID, never null after persistence
     */
    public String getId() {
        return id;
    }
    
    /**
     * Sets the unique user identifier.
     * <p>
     * This should only be called by the persistence layer.
     *
     * @param id the user ID, must not be null
     * @throws NullPointerException if id is null
     */
    public void setId(String id) {
        if (id == null) {
            throw new NullPointerException("ID cannot be null");
        }
        this.id = id;
    }
    
    /**
     * Returns the username.
     * <p>
     * Username is unique within the system and cannot be changed
     * after initial setting.
     *
     * @return the username, never null
     */
    public String getUsername() {
        return username;
    }
    
    /**
     * Sets the username.
     * <p>
     * Username must be 3-20 alphanumeric characters.
     * This method should only be called during user creation.
     *
     * @param username the username, must not be null
     * @throws NullPointerException if username is null
     * @throws IllegalArgumentException if username doesn't meet requirements
     * @throws IllegalStateException if username is already set
     */
    public void setUsername(String username) {
        if (username == null) {
            throw new NullPointerException("Username cannot be null");
        }
        if (this.username != null) {
            throw new IllegalStateException("Username already set");
        }
        validateUsername(username);
        this.username = username;
    }
    
    /**
     * Returns the email address.
     *
     * @return the email address, or null if not set
     */
    public String getEmail() {
        return email;
    }
    
    /**
     * Sets the email address.
     * <p>
     * Email must be in valid format. Setting null clears the email.
     *
     * @param email the email address, can be null
     * @throws IllegalArgumentException if email is not null and invalid
     */
    public void setEmail(String email) {
        if (email != null && !isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format");
        }
        this.email = email;
    }
    
    /**
     * Returns the current account status.
     *
     * @return the status, never null
     */
    public UserStatus getStatus() {
        return status;
    }
    
    /**
     * Sets the account status.
     * <p>
     * Status transitions are validated. Not all transitions are allowed.
     * See {@link UserStatus} for valid transitions.
     *
     * @param status the new status, must not be null
     * @throws NullPointerException if status is null
     * @throws IllegalStateException if transition is not allowed
     */
    public void setStatus(UserStatus status) {
        if (status == null) {
            throw new NullPointerException("Status cannot be null");
        }
        validateStatusTransition(this.status, status);
        this.status = status;
    }
    
    /**
     * Returns the account creation timestamp.
     *
     * @return the creation time, never null after persistence
     */
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    /**
     * Sets the account creation timestamp.
     * <p>
     * This should only be called by the persistence layer.
     *
     * @param createdAt the creation time, must not be null
     * @throws NullPointerException if createdAt is null
     */
    public void setCreatedAt(Instant createdAt) {
        if (createdAt == null) {
            throw new NullPointerException("CreatedAt cannot be null");
        }
        this.createdAt = createdAt;
    }
    
    /**
     * Checks if the account is currently active.
     *
     * @return {@code true} if status is ACTIVE, {@code false} otherwise
     */
    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }
}
```

#### Key Points for Getters/Setters

1. **Always document null handling**: "can be null", "never null", "or null if not set"
2. **Document validation rules** in setters
3. **Document side effects** (notifications, logging, cascading updates)
4. **Document thread safety** if getters/setters access shared state
5. **Document immutability constraints** (e.g., "cannot be changed after initial setting")
6. **For boolean getters**, use clear true/false descriptions
7. **For collection getters**, specify if returned collection is modifiable
8. **Document performance characteristics** if getter does expensive computation

---

## 8. Documenting Generic Types and Type Parameters

### 8.1 Class-Level Type Parameters

```java
/**
 * A generic container that holds a single value of any type.
 * <p>
 * This class is immutable and thread-safe. The contained value
 * is set at construction time and cannot be changed.
 *
 * @param <T> the type of value held by this container
 * @since 1.0
 */
public class Container<T> {
    
    private final T value;
    
    /**
     * Creates a container with the specified value.
     *
     * @param value the value to contain, can be null
     */
    public Container(T value) {
        this.value = value;
    }
    
    /**
     * Returns the contained value.
     *
     * @return the value, can be null if null was provided to constructor
     */
    public T getValue() {
        return value;
    }
}
```

### 8.2 Multiple Type Parameters

```java
/**
 * A generic pair that holds two related values.
 * <p>
 * This class is immutable and can hold any combination of types
 * including null values.
 *
 * @param <L> the type of the left (first) value
 * @param <R> the type of the right (second) value
 * @since 1.0
 */
public class Pair<L, R> {
    
    private final L left;
    private final R right;
    
    /**
     * Creates a pair with the specified left and right values.
     *
     * @param left the left value, can be null
     * @param right the right value, can be null
     */
    public Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }
    
    /**
     * Returns the left value.
     *
     * @return the left value, can be null
     */
    public L getLeft() {
        return left;
    }
    
    /**
     * Returns the right value.
     *
     * @return the right value, can be null
     */
    public R getRight() {
        return right;
    }
    
    /**
     * Creates a new pair with the left and right values swapped.
     *
     * @return a new pair with swapped values, never null
     */
    public Pair<R, L> swap() {
        return new Pair<>(right, left);
    }
}
```

### 8.3 Bounded Type Parameters

```java
/**
 * A sorted collection that maintains elements in natural order.
 * <p>
 * Elements must implement {@link Comparable} to be added to this
 * collection. The collection uses the natural ordering defined by
 * {@link Comparable#compareTo(Object)}.
 *
 * @param <E> the type of elements, must be comparable
 * @since 1.0
 */
public class SortedList<E extends Comparable<E>> {
    
    private final List<E> elements = new ArrayList<>();
    
    /**
     * Adds an element to the collection in sorted order.
     * <p>
     * The element is inserted at the appropriate position to maintain
     * sort order. This operation is O(n) due to the insertion.
     *
     * @param element the element to add, must not be null
     * @throws NullPointerException if element is null
     */
    public void add(E element) {
        if (element == null) {
            throw new NullPointerException("Element cannot be null");
        }
        
        int insertionPoint = Collections.binarySearch(elements, element);
        if (insertionPoint < 0) {
            insertionPoint = -(insertionPoint + 1);
        }
        elements.add(insertionPoint, element);
    }
}
```

### 8.4 Method-Level Type Parameters

```java
/**
 * Utility methods for working with collections.
 */
public class CollectionUtils {
    
    /**
     * Finds the first element in the collection that matches the predicate.
     * <p>
     * This method iterates through the collection and returns the first
     * element for which the predicate returns {@code true}.
     *
     * @param <T> the type of elements in the collection
     * @param collection the collection to search, must not be null
     * @param predicate the condition to test, must not be null
     * @return the first matching element, or null if no match is found
     * @throws NullPointerException if collection or predicate is null
     */
    public static <T> T findFirst(Collection<T> collection, Predicate<T> predicate) {
        for (T element : collection) {
            if (predicate.test(element)) {
                return element;
            }
        }
        return null;
    }
    
    /**
     * Transforms each element in the source list using the provided function.
     * <p>
     * Creates a new list containing the transformed elements. The source
     * list is not modified.
     *
     * @param <S> the type of elements in the source list
     * @param <T> the type of elements in the resulting list
     * @param source the source list, must not be null
     * @param mapper the transformation function, must not be null
     * @return a new list with transformed elements, never null
     * @throws NullPointerException if source or mapper is null
     */
    public static <S, T> List<T> map(List<S> source, Function<S, T> mapper) {
        List<T> result = new ArrayList<>(source.size());
        for (S element : source) {
            result.add(mapper.apply(element));
        }
        return result;
    }
}
```

### 8.5 Wildcard Type Parameters

```java
/**
 * A repository for managing entities of any type.
 */
public class EntityRepository {
    
    /**
     * Checks if the given entity exists in the repository.
     * <p>
     * This method accepts any entity type that extends the base
     * {@code Entity} class.
     *
     * @param entity the entity to check, must not be null
     * @return {@code true} if the entity exists, {@code false} otherwise
     * @throws NullPointerException if entity is null
     */
    public boolean exists(Entity<?> entity) {
        // implementation
        return false;
    }
    
    /**
     * Copies all elements from the source list to the destination list.
     * <p>
     * The destination can be a supertype of the source element type,
     * allowing flexible usage.
     *
     * @param <T> the type of elements
     * @param source the source list to copy from, must not be null
     * @param destination the destination list to copy to, must not be null
     * @throws NullPointerException if source or destination is null
     */
    public <T> void copyAll(List<? extends T> source, List<? super T> destination) {
        destination.addAll(source);
    }
}
```

---

## 9. Package Documentation (package-info.java)

Package documentation provides an overview of the package's purpose and contents.

### 9.1 Basic Package Documentation

```java
/**
 * Provides classes for user authentication and authorization.
 * <p>
 * This package contains the core authentication framework including:
 * <ul>
 *   <li>{@link com.example.auth.AuthenticationService} - Main authentication service</li>
 *   <li>{@link com.example.auth.AuthenticationProvider} - Provider interface</li>
 *   <li>{@link com.example.auth.Credentials} - User credentials representation</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>{@code
 * AuthenticationService service = new AuthenticationService();
 * boolean authenticated = service.authenticate(
 *     new Credentials("username", "password")
 * );
 * }</pre>
 *
 * @since 1.0
 * @see com.example.auth.AuthenticationService
 */
package com.example.auth;
```

### 9.2 Package with External References

```java
/**
 * HTTP client implementation for RESTful web services.
 * <p>
 * This package provides a fluent API for making HTTP requests and
 * processing responses. It supports:
 * <ul>
 *   <li>All standard HTTP methods (GET, POST, PUT, DELETE, PATCH)</li>
 *   <li>Automatic JSON serialization/deserialization</li>
 *   <li>Request/response interceptors</li>
 *   <li>Connection pooling and keep-alive</li>
 *   <li>Automatic retry with exponential backoff</li>
 * </ul>
 * <p>
 * Thread safety: All classes in this package are thread-safe unless
 * explicitly documented otherwise.
 * <p>
 * Example usage:
 * <pre>{@code
 * HttpClient client = HttpClient.builder()
 *     .baseUrl("https://api.example.com")
 *     .timeout(Duration.ofSeconds(30))
 *     .build();
 *
 * Response<User> response = client.get("/users/123")
 *     .execute(User.class);
 * }</pre>
 *
 * @since 2.0
 * @see com.example.http.HttpClient
 * @see <a href="https://tools.ietf.org/html/rfc7231">RFC 7231 - HTTP/1.1</a>
 */
package com.example.http;

import java.time.Duration;
```

### 9.3 Package with Security Considerations

```java
/**
 * Cryptographic utilities for secure data handling.
 * <p>
 * This package provides cryptographic functions for:
 * <ul>
 *   <li>Data encryption and decryption (AES-256)</li>
 *   <li>Secure hashing (SHA-256, SHA-512)</li>
 *   <li>Digital signatures (RSA, ECDSA)</li>
 *   <li>Key generation and management</li>
 * </ul>
 * <p>
 * <b>Security Warning:</b> Improper use of cryptographic functions can
 * lead to security vulnerabilities. Always:
 * <ul>
 *   <li>Use strong, randomly generated keys</li>
 *   <li>Never hardcode keys in source code</li>
 *   <li>Use appropriate key storage mechanisms</li>
 *   <li>Keep cryptographic libraries up to date</li>
 * </ul>
 * <p>
 * This package requires Java Cryptography Extension (JCE) Unlimited
 * Strength Jurisdiction Policy Files for full functionality.
 *
 * @since 1.5
 * @see javax.crypto.Cipher
 * @see java.security.MessageDigest
 */
package com.example.crypto;
```

---

## 10. Module Documentation (module-info.java)

Module documentation (Java 9+) describes the module's purpose, dependencies, and exports.

### 10.1 Basic Module Documentation

```java
/**
 * Core domain model and business logic.
 * <p>
 * This module contains the fundamental domain entities and services
 * for the application. It has no dependencies on infrastructure
 * or presentation concerns.
 * <p>
 * Exported packages:
 * <ul>
 *   <li>{@code com.example.domain.model} - Domain entities</li>
 *   <li>{@code com.example.domain.service} - Domain services</li>
 *   <li>{@code com.example.domain.repository} - Repository interfaces</li>
 * </ul>
 *
 * @since 3.0
 */
module com.example.domain {
    exports com.example.domain.model;
    exports com.example.domain.service;
    exports com.example.domain.repository;
    
    requires java.logging;
}
```

### 10.2 Module with Qualified Exports

```java
/**
 * Data access layer implementation.
 * <p>
 * This module provides concrete implementations of repository
 * interfaces using JPA/Hibernate. It includes:
 * <ul>
 *   <li>JPA entity mappings</li>
 *   <li>Repository implementations</li>
 *   <li>Database migration scripts</li>
 *   <li>Transaction management</li>
 * </ul>
 * <p>
 * The {@code com.example.data.internal} package is only exported
 * to the test module and should not be used by application code.
 * <p>
 * Database support:
 * <ul>
 *   <li>PostgreSQL 12+</li>
 *   <li>MySQL 8.0+</li>
 *   <li>H2 (for testing)</li>
 * </ul>
 *
 * @since 3.0
 */
module com.example.data {
    requires transitive com.example.domain;
    requires java.persistence;
    requires org.hibernate.orm.core;
    requires java.sql;
    
    exports com.example.data.repository;
    exports com.example.data.internal to com.example.test;
    
    opens com.example.data.entity to org.hibernate.orm.core;
}
```

### 10.3 Module with Service Providers

```java
/**
 * Plugin system for extensible functionality.
 * <p>
 * This module defines the plugin API and provides the plugin
 * loading mechanism. Plugins can be added to the classpath and
 * will be automatically discovered and loaded.
 * <p>
 * Creating a plugin:
 * <ol>
 *   <li>Implement the {@code Plugin} interface</li>
 *   <li>Provide a {@code module-info.java} with {@code provides} directive</li>
 *   <li>Package as a JAR and add to module path</li>
 * </ol>
 * <p>
 * Example plugin module:
 * <pre>{@code
 * module com.example.myplugin {
 *     requires com.example.plugin.api;
 *     provides com.example.plugin.Plugin
 *         with com.example.myplugin.MyPluginImpl;
 * }
 * }</pre>
 *
 * @since 3.5
 * @see com.example.plugin.Plugin
 * @see java.util.ServiceLoader
 */
module com.example.plugin.api {
    exports com.example.plugin;
    
    uses com.example.plugin.Plugin;
}
```

---

## 11. Block Tags Usage

### 11.1 Standard Block Tags

Block tags must appear after the main description and follow strict ordering:

**Standard order:**
1. `@param` (methods and constructors, type parameters)
2. `@return` (methods only)
3. `@throws` or `@exception` (methods and constructors)
4. `@see`
5. `@since`
6. `@deprecated`

**Note:** `@author` and `@version` are intentionally not listed as they are discouraged in modern development.

### 11.2 @param

Used for method and constructor parameters, type parameters, and record components:

```java
/**
 * Stores a key-value pair in the cache.
 *
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of mapped values
 * @param key the key with which the specified value is to be associated,
 *            must not be null
 * @param value the value to be associated with the specified key,
 *              null values are allowed
 * @throws NullPointerException if key is null
 */
public <K, V> void put(K key, V value) {
    // implementation
}
```

### 11.3 @return

Used to describe the return value of methods (not constructors or void methods):

```java
/**
 * Calculates the average of the given numbers.
 *
 * @param numbers the array of numbers to average, must not be null or empty
 * @return the arithmetic mean of all numbers in the array
 * @throws IllegalArgumentException if numbers is null or empty
 */
public double calculateAverage(double[] numbers) {
    // implementation
}
```

### 11.4 @throws / @exception

Documents exceptions that may be thrown:

```java
/**
 * Reads the contents of a file.
 *
 * @param filePath the path to the file to read, must not be null
 * @return the file contents as a string, never null
 * @throws NullPointerException if filePath is null
 * @throws FileNotFoundException if the file does not exist
 * @throws IOException if an I/O error occurs during reading
 * @throws SecurityException if a security manager exists and denies
 *         read access to the file
 */
public String readFile(Path filePath) throws IOException {
    // implementation
}
```

**Note:** `@throws` and `@exception` are synonymous; use `@throws` for consistency.

### 11.5 @see

Creates links to related elements:

```java
/**
 * Encrypts sensitive data using AES-256 algorithm.
 *
 * @param plaintext the data to encrypt, must not be null
 * @param key the encryption key, must be exactly 32 bytes
 * @return the encrypted data
 * @throws NullPointerException if plaintext or key is null
 * @throws IllegalArgumentException if key length is not 32 bytes
 * @see #decrypt(byte[], byte[])
 * @see EncryptionUtils
 * @see <a href="https://en.wikipedia.org/wiki/Advanced_Encryption_Standard">AES on Wikipedia</a>
 */
public byte[] encrypt(byte[] plaintext, byte[] key) {
    // implementation
}
```

### 11.6 @since

Indicates when an element was added:

```java
/**
 * Validates an email address using RFC 5322 rules.
 *
 * @param email the email address to validate, must not be null
 * @return true if the email is valid, false otherwise
 * @since 2.1
 */
public boolean isValidEmail(String email) {
    // implementation
}
```

### 11.7 @deprecated

Marks deprecated elements and suggests alternatives:

```java
/**
 * Calculates a hash code using MD5 algorithm.
 *
 * @param input the input string, must not be null
 * @return the MD5 hash
 * @throws NullPointerException if input is null
 * @deprecated since 3.0, MD5 is cryptographically broken. Use
 *             {@link #calculateSHA256(String)} instead
 */
@Deprecated(since = "3.0", forRemoval = true)
public String calculateMD5(String input) {
    // implementation
}

/**
 * Calculates a hash code using SHA-256 algorithm.
 *
 * @param input the input string, must not be null
 * @return the SHA-256 hash
 * @throws NullPointerException if input is null
 * @since 3.0
 */
public String calculateSHA256(String input) {
    // implementation
}
```

---

## 12. Inline Tags Usage

Inline tags can appear anywhere in the description or block tag comments.

### 12.1 {@code}

Used for code snippets, keywords, and identifiers:
- Displays text in monospace font
- Disables HTML interpretation
- No need to escape HTML special characters inside

**Example:**
```java
/**
 * Checks if the collection contains the specified element.
 * <p>
 * This method returns {@code true} if this collection contains at least
 * one element {@code e} such that {@code Objects.equals(o, e)}.
 *
 * @param element the element to search for
 * @return {@code true} if element is found, {@code false} otherwise
 */
public boolean contains(Object element) {
    // implementation
}
```

### 12.2 {@literal}

Similar to `{@code}` but displays in normal font (not monospace):
- Disables HTML interpretation
- Use for displaying special characters without code semantics

**Example:**
```java
/**
 * Compares two strings lexicographically.
 * <p>
 * Returns a negative integer if {@literal this < that}, zero if
 * {@literal this == that}, or a positive integer if {@literal this > that}.
 *
 * @param other the string to compare with
 * @return comparison result
 */
public int compareTo(String other) {
    // implementation
}
```

### 12.3 {@link} and {@linkplain}

Creates hyperlinks to other documented elements:
- `{@link}` displays in monospace (code font)
- `{@linkplain}` displays in normal font

**Syntax:**
- `{@link ClassName}` - link to class
- `{@link ClassName#methodName}` - link to method
- `{@link ClassName#methodName(Type)}` - link to specific overload
- `{@link #methodName}` - link to method in current class
- `{@link ClassName label text}` - custom link text

**Example:**
```java
/**
 * A specialized list implementation.
 * <p>
 * This class extends {@link java.util.ArrayList} and provides additional
 * functionality for bulk operations. For standard list operations, see
 * {@link java.util.List}.
 * <p>
 * Use {@link #addAll(Collection)} for efficient bulk insertion, or
 * {@linkplain #addOne(Object) the single-element add method} for
 * individual elements.
 *
 * @param <E> the type of elements in this list
 * @see java.util.ArrayList
 * @see java.util.List
 */
public class EnhancedList<E> extends ArrayList<E> {
    // implementation
}
```

### 12.4 {@value}

Displays the value of a constant:

**Example:**
```java
/**
 * Configuration constants for the application.
 */
public class AppConfig {
    
    /**
     * The maximum number of retry attempts.
     * Value: {@value}
     */
    public static final int MAX_RETRIES = 3;
    
    /**
     * The default timeout in milliseconds.
     * Value: {@value}
     */
    public static final long DEFAULT_TIMEOUT = 5000L;
}

/**
 * Attempts to connect with automatic retries.
 * <p>
 * This method will retry up to {@value AppConfig#MAX_RETRIES} times
 * with a timeout of {@value AppConfig#DEFAULT_TIMEOUT} milliseconds.
 *
 * @return true if connection succeeds, false otherwise
 */
public boolean connect() {
    // implementation
}
```

### 12.5 {@inheritDoc}

Copies documentation from an overridden method or implemented interface:

```java
public interface DataSource {
    /**
     * Opens a connection to the data source.
     *
     * @return a new connection instance
     * @throws ConnectionException if the connection cannot be established
     */
    Connection open() throws ConnectionException;
}

public class DatabaseDataSource implements DataSource {
    /**
     * {@inheritDoc}
     * <p>
     * This implementation uses connection pooling for better performance.
     */
    @Override
    public Connection open() throws ConnectionException {
        // implementation
    }
}
```

### 12.6 {@snippet} (Java 18+)

For multi-line code examples with highlighting:

```java
/**
 * Processes user data with validation.
 * <p>
 * Example usage:
 * {@snippet :
 * UserProcessor processor = new UserProcessor();
 * User user = new User("john", "john@example.com");
 * 
 * try {
 *     Result result = processor.process(user);  // @highlight substring="process"
 *     System.out.println("Success: " + result);
 * } catch (ValidationException e) {
 *     System.err.println("Validation failed: " + e.getMessage());
 * }
 * }
 *
 * @param user the user to process, must not be null
 * @return processing result, never null
 * @throws ValidationException if user data is invalid
 * @since 4.0
 */
public Result process(User user) throws ValidationException {
    // implementation
}
```

---

## 13. Good vs Bad Examples

### 13.1 Class Documentation

#### ❌ Bad Example

```java
/**
 * User service.
 * 
 * @version 1.0
 * @author John
 */
public class UserService {
    public void addUser(String name, String email) {
        // implementation
    }
}
```

**Problems:**
- No period at end of summary sentence
- Unnecessary `@version` and `@author` tags (modern anti-pattern)
- Too brief, no useful information
- Method not documented
- No information about behavior or constraints

#### ✅ Good Example

```java
/**
 * Provides business logic for managing user accounts.
 * <p>
 * This service handles user creation, retrieval, updates, and deletion
 * operations. It enforces business rules such as username uniqueness
 * and email validation.
 * <p>
 * All methods are thread-safe and transactional.
 *
 * @since 1.0
 * @see UserRepository
 * @see UserValidator
 */
public class UserService {
    
    /**
     * Adds a new user to the system.
     * <p>
     * This method validates the input parameters, checks for username
     * and email uniqueness, and persists the new user account.
     *
     * @param name the full name of the user, must not be null or empty
     * @param email the email address, must not be null and must be valid
     * @return the created user with generated ID
     * @throws NullPointerException if name or email is null
     * @throws IllegalArgumentException if name is empty or email is invalid
     * @throws DuplicateEmailException if the email is already registered
     */
    public User addUser(String name, String email) {
        // implementation
    }
}
```

### 13.2 Record Documentation

#### ❌ Bad Example

```java
/**
 * A point
 * @param x x
 * @param y y
 */
public record Point(double x, double y) {}
```

**Problems:**
- No period at end of summary
- Parameter descriptions are useless
- No information about usage or constraints

#### ✅ Good Example

```java
/**
 * Represents an immutable point in 2D Cartesian space.
 * <p>
 * This record provides value-based semantics with automatic
 * implementations of {@code equals()}, {@code hashCode()}, and {@code toString()}.
 * <p>
 * Example usage:
 * <pre>{@code
 * Point origin = new Point(0.0, 0.0);
 * Point p = new Point(3.0, 4.0);
 * double distance = origin.distanceTo(p); // 5.0
 * }</pre>
 *
 * @param x the x-coordinate, can be any finite or infinite double value
 * @param y the y-coordinate, can be any finite or infinite double value
 * @since 2.0
 */
public record Point(double x, double y) {
    
    /**
     * Calculates the Euclidean distance from this point to another.
     *
     * @param other the other point, must not be null
     * @return the distance between the points
     * @throws NullPointerException if other is null
     */
    public double distanceTo(Point other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
}
```

### 13.3 Annotation Documentation

#### ❌ Bad Example

```java
/**
 * Marks as cached
 */
@interface Cached {
    int value();
}
```

**Problems:**
- No period at end
- No explanation of what it does or how to use it
- Element not documented

#### ✅ Good Example

```java
/**
 * Indicates that method results should be cached.
 * <p>
 * When applied to a method, the framework will cache the return value
 * based on the method parameters. Subsequent calls with the same parameters
 * will return the cached value without executing the method.
 * <p>
 * Example usage:
 * <pre>{@code
 * @Cached(ttl = 3600)
 * public User findUserById(Long id) {
 *     return database.query(id);
 * }
 * }</pre>
 *
 * @since 2.5
 * @see CacheManager
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Cached {
    
    /**
     * The time-to-live for cached values in seconds.
     * <p>
     * After this duration, the cached value expires and the method
     * will be re-executed on the next call. Must be positive.
     * <p>
     * Default is 300 seconds (5 minutes).
     *
     * @return the TTL in seconds
     */
    int ttl() default 300;
}
```

### 13.4 Fixing Common doclint Errors

#### ❌ Bad: Missing @param Tag

```java
/**
 * Calculates the total price.
 *
 * @return the total price
 */
public double calculateTotal(double price, double taxRate) {
    // implementation
}
```

**doclint errors:**
- `error: @param for "price" not found`
- `error: @param for "taxRate" not found`

#### ✅ Good: All Parameters Documented

```java
/**
 * Calculates the total price including tax.
 *
 * @param price the base price before tax, must be non-negative
 * @param taxRate the tax rate as a decimal (e.g., 0.08 for 8%), must
 *                be between 0 and 1
 * @return the total price including tax
 * @throws IllegalArgumentException if price is negative or taxRate is
 *         outside the valid range
 */
public double calculateTotal(double price, double taxRate) {
    // implementation
}
```

#### ❌ Bad: Incorrect HTML

```java
/**
 * Formats the text with <b>bold formatting.
 * <p>Uses the <br> tag.
 *
 * @param text the input text
 * @return formatted text
 */
public String formatText(String text) {
    // implementation
}
```

**doclint errors:**
- `error: element not closed: b`
- `error: unexpected end tag: p`

#### ✅ Good: Proper HTML

```java
/**
 * Formats the text with <b>bold formatting</b>.
 * <p>
 * Uses line breaks where needed.
 * </p>
 *
 * @param text the input text, must not be null
 * @return the formatted text with HTML markup, never null
 * @throws NullPointerException if text is null
 */
public String formatText(String text) {
    // implementation
}
```

#### ❌ Bad: Invalid Reference

```java
/**
 * Processes data.
 *
 * @see DataProcessor#process(Data)
 * @param data the input
 * @return result
 */
public Result processData(Object data) {
    // implementation
}
```

**doclint error:** `error: reference not found: DataProcessor#process(Data)`

#### ✅ Good: Valid References

```java
/**
 * Processes input data and produces a result.
 *
 * @param data the input data to process, must not be null
 * @return the processing result containing status and output data,
 *         never null
 * @throws NullPointerException if data is null
 * @see DataProcessor#process(java.lang.Object)
 */
public Result processData(Object data) {
    // implementation
}
```

---

## 14. Common Mistakes and Best Practices Checklist

### 14.1 Common doclint Errors

#### Missing Elements

- [ ] **Missing Javadoc comment**: Every public/protected class, interface, enum, record, annotation, method, and constructor must have Javadoc
- [ ] **Missing summary sentence**: First sentence must end with period, exclamation, or question mark
- [ ] **Missing @param tag**: Every parameter (including type parameters and record components) must be documented
- [ ] **Missing @return tag**: Every non-void method must have @return
- [ ] **Missing @throws tag**: All declared exceptions and important runtime exceptions must be documented

#### HTML Errors

- [ ] **Unclosed HTML tags**: All tags must be properly closed (e.g., `<p>...</p>`, `<b>...</b>`)
- [ ] **Bad HTML entities**: Use `&lt;`, `&gt;`, `&amp;` or `{@literal}` for special characters
- [ ] **Deprecated HTML tags**: Avoid `<font>`, `<center>`, etc.

#### Reference Errors

- [ ] **Invalid @see reference**: Ensure referenced classes and methods exist and are fully qualified if needed
- [ ] **Invalid {@link} reference**: Check spelling and include parameter types for overloaded methods
- [ ] **Broken external links**: Verify URLs in `<a href="...">` tags

#### Structure Errors

- [ ] **Wrong tag order**: Follow standard order (param, return, throws, see, since, deprecated)
- [ ] **Dangling Javadoc**: Ensure comment is directly attached to the element without blank lines
- [ ] **Javadoc on wrong element**: Don't put class-level tags on methods or vice versa

### 14.2 Best Practices Checklist

#### Writing Style

- [ ] Use **present tense** (e.g., "Returns the user" not "Will return the user")
- [ ] Use **third person** (e.g., "Calculates the sum" not "Calculate the sum")
- [ ] Start method descriptions with a **verb** (e.g., "Validates", "Creates", "Retrieves")
- [ ] Be **concise** but **complete** - include all necessary information without redundancy
- [ ] Avoid **implementation details** - describe what, not how

#### Content

- [ ] Document **preconditions** (what must be true before calling)
- [ ] Document **postconditions** (what will be true after calling)
- [ ] Document **side effects** (state changes, I/O operations, etc.)
- [ ] Document **thread safety** for concurrent code
- [ ] Document **null handling** (can parameters be null? Can return be null?)
- [ ] Document **empty cases** (empty collections, zero values, etc.)
- [ ] Include **usage examples** for complex APIs

#### Parameters and Returns

- [ ] Every @param must explain **what** the parameter is and **constraints** on its value
- [ ] Use "must not be null" or "can be null" for every reference parameter
- [ ] Specify **ranges** for numeric parameters (e.g., "must be positive", "between 0 and 100")
- [ ] Explain special @return values (null, empty, -1, etc.)
- [ ] Use "never null" or "can be null" for return values

#### Exceptions

- [ ] Document **all checked exceptions** in throws clause
- [ ] Document **important runtime exceptions** (NullPointerException, IllegalArgumentException, etc.)
- [ ] Explain the **condition** that causes each exception
- [ ] Use specific exception types, not generic Exception

#### Tags and Links

- [ ] Use `{@code}` for code elements (keywords, class names, variables)
- [ ] Use `{@link}` for references to other documented elements
- [ ] Use `{@literal}` for special characters in regular text
- [ ] Include `@see` links to related classes and methods
- [ ] Mark deprecated elements with both `@deprecated` tag and `@Deprecated` annotation
- [ ] Always suggest **alternatives** in @deprecated comments

#### Modern Practices

- [ ] **Avoid @author tags** - Git history provides better information
- [ ] **Avoid @version tags** - Use build system versioning instead
- [ ] Use `@since` to indicate when features were introduced
- [ ] Document record components in class-level Javadoc
- [ ] Provide package-info.java for non-trivial packages
- [ ] Consider module-info.java documentation for Java 9+ modules

#### Validation Commands

Before committing, validate your Javadoc:

```bash
# Generate Javadoc with doclint enabled (all checks)
javadoc -Xdoclint:all -d target/javadoc -sourcepath src/main/java \
    -subpackages com.example

# With specific doclint checks
javadoc -Xdoclint:html,syntax,reference -d target/javadoc \
    -sourcepath src/main/java -subpackages com.example

# With Maven
mvn javadoc:javadoc -Djavadoc.lint=all

# With Gradle
./gradlew javadoc -Djavadoc.lint=all
```

### 14.3 Quick Reference: Tag Order

For **classes, interfaces, enums, records, annotations**:
```
@param (for type parameters and record components)
@see
@since
@deprecated
```

For **methods and constructors**:
```
@param (all parameters)
@return (methods only)
@throws (all exceptions)
@see
@since
@deprecated
```

### 14.4 Pre-Commit Checklist

Before committing code with Javadoc:

1. [ ] All public/protected elements have Javadoc
2. [ ] All summary sentences end with proper punctuation
3. [ ] All parameters (including type parameters) are documented
4. [ ] All exceptions are documented
5. [ ] HTML tags are properly closed
6. [ ] Links and references are valid
7. [ ] No doclint warnings or errors when running with `-Xdoclint:all`
8. [ ] Code examples compile and run
9. [ ] Spelling and grammar are correct
10. [ ] Documentation matches actual behavior
11. [ ] No `@author` or `@version` tags (unless required by policy)
12. [ ] Record components documented in class-level Javadoc
13. [ ] Package-info.java exists for public packages

---

## Conclusion

This guide provides comprehensive coverage of Javadoc best practices that comply with `doclint` requirements for **all** Java element types:

- ✅ **Classes and Interfaces** - Purpose, contracts, usage
- ✅ **Enums** - Constants, fields, methods
- ✅ **Records** - Components, validation, methods
- ✅ **Annotations** - Elements, usage, examples
- ✅ **Fields** - Constants, instance fields
- ✅ **Methods and Constructors** - Parameters, returns, exceptions
- ✅ **Generic Types** - Type parameters, bounds, wildcards
- ✅ **Packages** - package-info.java documentation
- ✅ **Modules** - module-info.java documentation

**Key Takeaways:**

1. **Modern approach**: Avoid `@author` and `@version` - they're maintenance burdens
2. **Complete coverage**: Document ALL public/protected API elements
3. **Null handling**: Always specify if parameters/returns can be null
4. **Examples**: Provide usage examples for complex APIs
5. **doclint compliance**: Validate with `-Xdoclint:all` before committing

Remember: Good documentation is as important as good code. Well-documented APIs are easier to use, maintain, and evolve. Invest the time to write clear, complete Javadoc comments - your users (and future self) will thank you!
