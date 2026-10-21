# 🎨 Coding Guidelines & Style

This document defines the micro-level coding standards, aesthetics, and syntax rules for the project.

## Build & IDE Setup
- **Java Version:** OpenJDK 21 (or later).
- **IDE:** IntelliJ IDEA (Gradle project).
- **Linting:** Use SonarLint plugin to auto-detect code smells.
- **CI/CD:** Always make the build pass (`./gradlew build`) locally before pushing.

---

## Clean Code & Syntax Rules

#### Always make the build pass
- Run `./gradlew build` locally before pushing code.

#### Avoid redundancy
Avoid using redundant keywords. To list a few:
- `final` method modifier in a `final` class
- `static` or `public` modifier in an `interface`
- `public` method modifier in a package-local or private class
- `private` constructor modifier in an `enum`
- field access prefixed with `this.` where unnecessary

#### Use `public` only when necessary
The classes, methods and fields that are not meant to be used by a user should not be public. Use the most restrictive modifier wherever possible, such as `private`, package-local and `protected`, so that static analysis tools can find dead code easily.

#### Organize
Organize class members carefully for readability, using **top-down** approach. Although there's no absolute rule of thumb, it's usually like:
- `static` fields
- `static` methods
- member fields
- constructors
- member methods
- utility methods (both `static` and member)
- inner classes

#### Meaningful exception messages
When raising an exception, specify a meaningful message which gives an explicit clue about what went wrong.

#### Prefer JDK API
Prefer using plain JDK API when the same behavior can be achieved with the same amount of code. *(Note for Java 17+: native JDK methods like `Stream.toList()` now replace many old Guava utilities)*.

```java
// Prefer A (JDK) - less indirection
Map<String, String> map = new HashMap<>();   // A (JDK)
Map<String, String> map = Maps.newHashMap(); // B (Guava)

// Prefer B (Guava) - simpler yet more efficient
List<String> list = Collections.unmodifiableList(  // A (JDK 8)
        otherList.stream().filter(...).collect(Collectors.toList()));
List<String> list = otherList.stream().filter(...) // B (Guava)
        .collect(toImmutableList());
// Note: In Java 16+, simply use .stream().toList()
```

#### Prefer early-return style

Prefer 'early return' code style for readability.

```java
// Great
public void doSomething(String value) {
    if (value == null) {
        return;
    }

    // Do the actual job
}

// Not great
public void doSomething(String value) {
    if (value != null) {
        // Do the actual job
    }
}
```

However, when the 'normal' execution path is very simple, this may also look beautiful:

```java
public void doSomething(String value) {
    if (value != null) {
        return value.trim();
    } else {
        return null;
    }
}
```

#### Prefer `MoreObjects.toStringHelper()`

Prefer `MoreObjects.toStringHelper()` to hand-written `toString()` implementation.
However, consider writing hand-written or caching `toString()` implementation
in performance-sensitive places.

#### Think aesthetics

Do not insert an empty line that hurts code aesthetics.

```java
// OK
if (...) {
    doSomething();
}

// Not OK
if (...) {
    doSomething();
                        // <-- Remove this extra line.
}
```

Similarly, do not use two or more consecutive empty lines.

```java
// OK
public void a() { ... }

public void b() { ... }

// Not OK
public void a() { ... }

                        // <-- Remove this extra line.
public void b() { ... }
```
### [Working with JPA and Lombok](https://jpa-buddy.com/blog/lombok-and-jpa-what-may-go-wrong/)

- Avoid using `@EqualsAndHashCode` and @Data with JPA entities;
- Always exclude lazy attributes when using `@ToString`;
- Don’t forget to add `@NoArgsConstructor` to entities with `@Builder` or `@AllArgsConstructor`.