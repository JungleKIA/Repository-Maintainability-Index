# Javadoc Reference Guide for LLM Code Generation

> **Purpose**: This is a comprehensive reference for generating doclint-compliant Javadoc comments in Java. Use this guide to ensure all generated documentation passes `javadoc -Xdoclint:all` validation.

---

## Core Principles

### 1. Summary Sentence (CRITICAL)
- **MUST** end with period (`.`), exclamation (`!`), or question mark (`?`)
- First sentence = summary (appears in listings)
- Keep concise (ideally < 80 chars)
- Must be grammatically complete

### 2. What vs How
- Document **what** the code does, NOT **how** it does it
- Focus on contract, behavior, side effects
- Avoid implementation details

### 3. Required Documentation
Every `public` and `protected` element MUST have Javadoc:
- Classes, interfaces, enums, records, annotations
- Methods, constructors
- Fields (especially public/protected/constants)
- Packages (via package-info.java)
- Modules (via module-info.java)

---

## Tag Order (STRICT)

### For Classes/Interfaces/Enums/Records/Annotations:
```
1. @param (type parameters, record components)
2. @see
3. @since
4. @deprecated
```

### For Methods/Constructors:
```
1. @param (all parameters, including type parameters)
2. @return (methods only, NOT void or constructors)
3. @throws/@exception (ALL exceptions)
4. @see
5. @since
6. @deprecated
```

**Note**: `@author` and `@version` are **DISCOURAGED** in modern development (use Git instead).

---

## HTML Rules

### Allowed Tags
- Paragraphs: `<p>`, `</p>`
- Lists: `<ul>`, `<ol>`, `<li>`
- Formatting: `<b>`, `<i>`, `<strong>`, `<em>`
- Line breaks: `<br>`
- Tables: `<table>`, `<tr>`, `<td>`, `<th>`
- Links: `<a href="...">`

### CRITICAL: All tags MUST be closed
```java
// ❌ BAD
/**
 * Returns <b>bold text
 * <p>Next paragraph
 */

// ✅ GOOD
/**
 * Returns <b>bold text</b>.
 * <p>
 * Next paragraph.
 * </p>
 */
```

### Special Characters
- Use `{@code}` for code (auto-escapes HTML)
- Use `{@literal}` for literal text with special chars
- Or escape: `&lt;`, `&gt;`, `&amp;`

---

## Inline Tags

### {@code text}
- Displays in monospace font
- Auto-escapes HTML special characters
- Use for: code snippets, identifiers, keywords

```java
/**
 * Returns {@code true} if {@code a < b}.
 */
```

### {@literal text}
- Normal font (not monospace)
- Auto-escapes HTML
- Use for: special characters without code semantics

### {@link Target} and {@linkplain Target}
- `{@link}` = monospace link
- `{@linkplain}` = normal font link
- Syntax: `{@link Class#method(Type)}`

```java
/**
 * See {@link java.util.List#add(Object)} for details.
 */
```

### {@value}
- Shows constant value
- `{@value}` = current constant
- `{@value Class#CONSTANT}` = reference another

### {@inheritDoc}
- Copies documentation from overridden method/interface

### {@snippet} (Java 18+)
- For multi-line code examples with highlighting

---

## Element-Specific Rules

### Classes & Interfaces

```java
/**
 * Single-line summary ending with period.
 * <p>
 * Detailed description explaining purpose, usage, and constraints.
 * Multiple paragraphs separated by {@code <p>} tags.
 * <p>
 * Thread safety: Specify if thread-safe or not.
 *
 * @param <T> type parameter description with constraints
 * @since 1.0
 * @see RelatedClass
 */
public class MyClass<T extends Comparable<T>> {
}
```

**Critical Points**:
- Always specify thread safety
- Document all type parameters with `@param <T>`
- Use `@since` to indicate when introduced
- DON'T use `@author` or `@version` (unless required by org policy)

### Enums

```java
/**
 * Enum class summary.
 *
 * @since 1.0
 */
public enum Status {
    
    /**
     * Active status - user can perform all operations.
     */
    ACTIVE,
    
    /**
     * Suspended status - user access is temporarily disabled.
     */
    SUSPENDED;
    
    /**
     * Checks if this status allows login.
     *
     * @return {@code true} if login allowed, {@code false} otherwise
     */
    public boolean allowsLogin() {
        return this == ACTIVE;
    }
}
```

**Every enum constant MUST be documented.**

### Records (Java 16+)

```java
/**
 * Record summary with purpose.
 * <p>
 * Records are immutable and provide automatic implementations
 * of {@code equals()}, {@code hashCode()}, and {@code toString()}.
 *
 * @param id the unique identifier, must not be null
 * @param name the name, must not be null or empty
 * @throws NullPointerException if any parameter is null
 * @since 2.0
 */
public record User(String id, String name) {
    
    /**
     * Compact constructor with validation.
     *
     * @throws NullPointerException if id or name is null
     * @throws IllegalArgumentException if name is empty
     */
    public User {
        if (id == null || name == null) {
            throw new NullPointerException("Parameters cannot be null");
        }
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
    }
}
```

**Document record components with `@param` in class-level Javadoc.**

### Annotations

```java
/**
 * Annotation summary explaining purpose and usage.
 * <p>
 * Example usage:
 * <pre>{@code
 * @MyAnnotation(value = "test", timeout = 5000)
 * public void myMethod() { }
 * }</pre>
 *
 * @since 1.5
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MyAnnotation {
    
    /**
     * The value attribute description.
     * <p>
     * Must not be null or empty. Defaults to empty string.
     *
     * @return the value
     */
    String value() default "";
    
    /**
     * Timeout in milliseconds.
     * <p>
     * Must be positive. Default is 5000ms (5 seconds).
     *
     * @return timeout in milliseconds
     */
    long timeout() default 5000;
}
```

### Fields

```java
/**
 * Configuration constants.
 */
public class Config {
    
    /**
     * Maximum retry attempts before giving up.
     * <p>
     * Value: {@value}
     */
    public static final int MAX_RETRIES = 3;
    
    /**
     * The database connection timeout in milliseconds.
     * <p>
     * Must be positive. Default is {@value}.
     */
    public static final long TIMEOUT_MS = 30_000L;
    
    /**
     * The current user session, or null if not logged in.
     * <p>
     * This field is updated by {@link #login(String)} and
     * cleared by {@link #logout()}.
     */
    private Session currentSession;
}
```

### Methods

```java
/**
 * Single-line method summary ending with period.
 * <p>
 * Detailed description of what the method does. Include:
 * - Side effects (modifies state, I/O, network calls)
 * - Thread safety
 * - Performance characteristics if relevant
 * - Blocking behavior
 *
 * @param <T> type parameter description
 * @param param1 description of param1, constraints (null/not null, range, format)
 * @param param2 description of param2, must not be null
 * @return description of return value, what it represents,
 *         special values (null, empty, -1), never null / can be null
 * @throws NullPointerException if param1 or param2 is null
 * @throws IllegalArgumentException if param1 violates constraints
 * @throws IOException if I/O error occurs during operation
 * @since 1.0
 */
public <T> Result<T> processData(String param1, List<T> param2) 
        throws IOException {
    // implementation
}
```

**Critical for methods**:
- Document ALL parameters (no `@param` = doclint error)
- Document ALL exceptions (checked AND important unchecked)
- Document return value for non-void methods
- Specify null handling: "must not be null" or "can be null"
- Always specify "never null" or "can be null" for return values

### Constructors

```java
/**
 * Creates a new instance with specified configuration.
 * <p>
 * The instance is initialized in a ready state. Call {@link #start()}
 * to begin operations.
 *
 * @param config the configuration, must not be null
 * @throws NullPointerException if config is null
 * @throws IllegalArgumentException if config is invalid
 */
public MyClass(Config config) {
    // implementation
}
```

### Getters and Setters

**YES, document them!** Level of detail depends on complexity.

#### Simple Getters/Setters
```java
/**
 * Returns the user's email address.
 *
 * @return the email, or null if not set
 */
public String getEmail() { return email; }

/**
 * Sets the user's email address.
 *
 * @param email the email, can be null
 * @throws IllegalArgumentException if email format is invalid
 */
public void setEmail(String email) { this.email = email; }
```

#### Complex Getters (with computation/caching)
```java
/**
 * Returns the full name.
 * <p>
 * Constructs name from first and last name components.
 * Result is cached after first computation.
 *
 * @return the full name, never null but can be empty
 */
public String getFullName() { }
```

#### Complex Setters (with side effects)
```java
/**
 * Sets the user status.
 * <p>
 * Changing status triggers: timestamp update, session clearing,
 * user notification, and audit logging.
 *
 * @param status the new status, must not be null
 * @throws NullPointerException if status is null
 * @throws IllegalStateException if transition not allowed
 */
public void setStatus(UserStatus status) { }
```

#### Boolean Getters (is/has)
```java
/**
 * Checks if account is active.
 *
 * @return {@code true} if active, {@code false} otherwise
 */
public boolean isActive() { }

/**
 * Checks if user has admin privileges.
 *
 * @return {@code true} if admin, {@code false} otherwise
 */
public boolean hasAdminPrivileges() { }
```

#### Collection Getters
```java
/**
 * Returns the user's roles.
 * <p>
 * The returned list is unmodifiable.
 *
 * @return unmodifiable list of roles, never null but can be empty
 */
public List<Role> getRoles() {
    return Collections.unmodifiableList(roles);
}
```

#### Fluent Setters (Builder pattern)
```java
/**
 * Sets the email address.
 * <p>
 * Fluent setter for method chaining.
 *
 * @param email the email, must not be null
 * @return this instance for method chaining
 * @throws NullPointerException if email is null
 */
public User setEmail(String email) {
    this.email = email;
    return this;
}
```

**Key points for getters/setters:**
1. **Always specify null handling**: "can be null", "never null", "or null if not set"
2. **Document validation rules** in setters
3. **Document side effects** (notifications, logging, cascading updates)
4. **Document thread safety** if accessing shared state
5. **Document immutability constraints** ("cannot be changed after initial setting")
6. **For boolean getters**, use clear true/false descriptions
7. **For collection getters**, specify if returned collection is modifiable
8. **Document performance** if getter does expensive computation

### Package (package-info.java)

```java
/**
 * Package summary describing overall purpose.
 * <p>
 * This package provides functionality for:
 * <ul>
 *   <li>Feature 1</li>
 *   <li>Feature 2</li>
 *   <li>Feature 3</li>
 * </ul>
 * <p>
 * Thread safety: All classes in this package are thread-safe
 * unless explicitly documented otherwise.
 * <p>
 * Example usage:
 * <pre>{@code
 * MyClass obj = new MyClass();
 * obj.doSomething();
 * }</pre>
 *
 * @since 1.0
 * @see MainClass
 */
package com.example.mypackage;
```

### Module (module-info.java)

```java
/**
 * Module summary describing purpose and exports.
 * <p>
 * This module provides core functionality for the application.
 * <p>
 * Exported packages:
 * <ul>
 *   <li>{@code com.example.api} - Public API</li>
 *   <li>{@code com.example.model} - Domain models</li>
 * </ul>
 *
 * @since 3.0
 */
module com.example.mymodule {
    exports com.example.api;
    exports com.example.model;
    requires java.logging;
}
```

---

## Generics Documentation

### Class-Level Type Parameters

```java
/**
 * Generic container summary.
 *
 * @param <T> the type of value stored, must not be null
 * @param <K> the type of key used for lookups
 * @since 1.0
 */
public class Container<T, K> { }
```

### Method-Level Type Parameters

```java
/**
 * Transforms a list using the provided function.
 *
 * @param <S> the source element type
 * @param <T> the target element type
 * @param source the source list, must not be null
 * @param mapper the transformation function, must not be null
 * @return a new list with transformed elements, never null
 * @throws NullPointerException if source or mapper is null
 */
public static <S, T> List<T> map(List<S> source, Function<S, T> mapper) { }
```

### Bounded Type Parameters

```java
/**
 * Sorted collection that maintains natural order.
 *
 * @param <E> the element type, must implement {@link Comparable}
 * @since 1.0
 */
public class SortedList<E extends Comparable<E>> { }
```

---

## Common doclint Errors & Fixes

### Error: Missing @param
```java
// ❌ BAD - doclint error: @param not found
/**
 * Calculates sum.
 * @return the sum
 */
public int add(int a, int b) { }

// ✅ GOOD
/**
 * Calculates sum of two integers.
 *
 * @param a the first number
 * @param b the second number
 * @return the sum of a and b
 */
public int add(int a, int b) { }
```

### Error: Missing @return
```java
// ❌ BAD - doclint error: @return not found
/**
 * Gets user name.
 * @param id user ID
 */
public String getName(int id) { }

// ✅ GOOD
/**
 * Gets user name by ID.
 *
 * @param id the user ID
 * @return the user name, or null if not found
 */
public String getName(int id) { }
```

### Error: Unclosed HTML tag
```java
// ❌ BAD - doclint error: element not closed
/**
 * Returns <b>bold text
 */

// ✅ GOOD
/**
 * Returns <b>bold text</b>.
 */
```

### Error: No summary sentence ending
```java
// ❌ BAD - no period at end
/**
 * Calculates sum
 */

// ✅ GOOD
/**
 * Calculates sum.
 */
```

### Error: Invalid @link reference
```java
// ❌ BAD - doclint error: reference not found
/**
 * @see MyClass#nonExistentMethod()
 */

// ✅ GOOD - verify method exists and signature matches
/**
 * @see MyClass#actualMethod(String)
 */
```

### Error: Dangling Javadoc
```java
// ❌ BAD - blank line between comment and element
/**
 * My class.
 */

public class MyClass { }

// ✅ GOOD - no blank line
/**
 * My class.
 */
public class MyClass { }
```

---

## Null Handling Documentation

**Always specify null behavior explicitly:**

### Parameters
```java
/**
 * Processes user data.
 *
 * @param user the user object, must not be null
 * @param config optional configuration, can be null (uses defaults)
 * @throws NullPointerException if user is null
 */
public void process(User user, Config config) { }
```

### Return Values
```java
/**
 * Finds user by ID.
 *
 * @param id the user ID
 * @return the user if found, or null if not found
 */
public User findById(String id) { }

/**
 * Gets all active users.
 *
 * @return list of active users, empty if none exist, never null
 */
public List<User> getActiveUsers() { }
```

---

## Exception Documentation

**Document ALL exceptions (checked and unchecked):**

```java
/**
 * Processes payment transaction.
 *
 * @param amount the payment amount, must be positive
 * @return transaction receipt
 * @throws NullPointerException if amount is null
 * @throws IllegalArgumentException if amount is negative or zero
 * @throws PaymentException if payment processing fails
 * @throws NetworkException if network error occurs
 */
public Receipt processPayment(BigDecimal amount) 
        throws PaymentException, NetworkException { }
```

**For each exception, specify the CONDITION that causes it.**

---

## Deprecated Elements

```java
/**
 * Legacy authentication method using MD5.
 *
 * @param password the password to hash
 * @return MD5 hash of password
 * @deprecated since 2.0, use {@link #authenticateSecure(String)} instead.
 *             MD5 is cryptographically broken and should not be used.
 */
@Deprecated(since = "2.0", forRemoval = true)
public String authenticate(String password) { }

/**
 * Secure authentication using SHA-256.
 *
 * @param password the password to hash
 * @return SHA-256 hash of password
 * @since 2.0
 */
public String authenticateSecure(String password) { }
```

**Always suggest alternative when deprecating.**

---

## Thread Safety Documentation

**Always document thread safety characteristics:**

```java
/**
 * Manages user sessions.
 * <p>
 * This class is thread-safe. All methods can be called concurrently
 * from multiple threads without external synchronization.
 *
 * @since 1.0
 */
public class SessionManager { }

/**
 * User data transfer object.
 * <p>
 * This class is NOT thread-safe. External synchronization is required
 * if instances are accessed from multiple threads.
 *
 * @since 1.0
 */
public class UserDTO { }
```

---

## Writing Style Guidelines

### Tense & Voice
- Use **present tense**: "Returns the user" (not "Will return")
- Use **third person**: "Calculates the sum" (not "Calculate")
- Start method docs with **verb**: "Validates", "Creates", "Retrieves"

### Clarity
- Be **concise** but **complete**
- Avoid redundancy
- Include all necessary constraints
- Specify ranges for numeric values
- Mention edge cases

### Examples
```java
// ❌ BAD - too vague
/**
 * Processes data.
 */

// ✅ GOOD - specific and informative
/**
 * Processes user data and updates the database.
 * <p>
 * This method validates the input, transforms it according to
 * business rules, and persists changes. If validation fails,
 * no database updates occur.
 *
 * @param data the user data to process, must not be null
 * @return processing result with status and any errors
 * @throws NullPointerException if data is null
 * @throws ValidationException if data fails validation
 */
```

---

## Code Examples in Javadoc

### Using {@code}
```java
/**
 * Checks if value is positive.
 * <p>
 * Returns {@code true} if {@code value > 0}.
 *
 * @param value the value to check
 * @return {@code true} if positive, {@code false} otherwise
 */
```

### Using <pre>{@code}
```java
/**
 * Creates a new user.
 * <p>
 * Example usage:
 * <pre>{@code
 * UserService service = new UserService();
 * User user = service.createUser("john", "john@example.com");
 * System.out.println("Created: " + user.getId());
 * }</pre>
 *
 * @param name the username
 * @param email the email address
 * @return the created user
 */
```

---

## Validation

### Command to check Javadoc
```bash
# Full validation
javadoc -Xdoclint:all -d target/javadoc -sourcepath src/main/java -subpackages com.example

# With Maven
mvn javadoc:javadoc -Djavadoc.lint=all

# With Gradle
./gradlew javadoc -Djavadoc.lint=all
```

---

## Quick Checklist for LLM

When generating Javadoc, ensure:

- [ ] Summary sentence ends with period/exclamation/question mark
- [ ] All `@param` tags present (matches method signature)
- [ ] `@return` present for non-void methods
- [ ] All exceptions documented with `@throws`
- [ ] Null behavior specified ("must not be null" / "can be null" / "never null")
- [ ] All HTML tags properly closed
- [ ] Tag order correct (param → return → throws → see → since → deprecated)
- [ ] Thread safety documented for classes
- [ ] Type parameters documented with `@param <T>`
- [ ] No `@author` or `@version` tags (unless specifically requested)
- [ ] Record components documented in class-level Javadoc
- [ ] Enum constants all have Javadoc
- [ ] Code examples use `{@code}` or `<pre>{@code}`
- [ ] Links use `{@link}` with correct syntax
- [ ] No blank lines between Javadoc and element

---

## Modern Best Practices Summary

1. **DON'T use `@author`** - Git provides better history
2. **DON'T use `@version`** - Use build system versioning
3. **DO use `@since`** - Document when features were added
4. **DO specify null handling** - Critical for API contracts
5. **DO document thread safety** - Prevents concurrency bugs
6. **DO provide examples** - Makes API easier to understand
7. **DO document side effects** - I/O, state changes, network calls
8. **DO validate with doclint** - Catch errors early

---

## Examples: All Java Elements

### Complete Class Example
```java
/**
 * Service for managing user accounts and authentication.
 * <p>
 * This service provides comprehensive user management including:
 * <ul>
 *   <li>User registration and validation</li>
 *   <li>Authentication and session management</li>
 *   <li>Password reset and recovery</li>
 * </ul>
 * <p>
 * All methods are thread-safe and can be called concurrently.
 * Methods that modify state are transactional.
 *
 * @since 1.0
 * @see User
 * @see Session
 */
public class UserService {
    
    /**
     * Maximum login attempts before account lockout.
     * <p>
     * Value: {@value}
     */
    public static final int MAX_LOGIN_ATTEMPTS = 3;
    
    /**
     * Registers a new user account.
     * <p>
     * This method validates the input, checks for duplicate usernames
     * and emails, and persists the new user. Validation includes:
     * <ul>
     *   <li>Username length and format</li>
     *   <li>Email format and uniqueness</li>
     *   <li>Password strength requirements</li>
     * </ul>
     *
     * @param username the unique username, must be 3-20 alphanumeric characters
     * @param email the email address, must be valid format and unique
     * @param password the password, must meet strength requirements
     * @return the created user with generated ID, never null
     * @throws NullPointerException if any parameter is null
     * @throws IllegalArgumentException if validation fails
     * @throws DuplicateUserException if username or email already exists
     * @since 1.0
     */
    public User registerUser(String username, String email, String password) {
        // implementation
        return null;
    }
    
    /**
     * Authenticates user with username and password.
     * <p>
     * Failed attempts are tracked. After {@value #MAX_LOGIN_ATTEMPTS}
     * failed attempts, the account is temporarily locked.
     *
     * @param username the username, must not be null
     * @param password the password, must not be null
     * @return authentication session if successful, never null
     * @throws NullPointerException if username or password is null
     * @throws AuthenticationException if credentials are invalid
     * @throws AccountLockedException if account is locked due to failed attempts
     * @since 1.0
     */
    public Session authenticate(String username, String password) {
        // implementation
        return null;
    }
}
```

---

This reference guide covers all critical aspects of Javadoc generation for doclint compliance. Use it to ensure generated documentation is complete, accurate, and passes all validation checks.
