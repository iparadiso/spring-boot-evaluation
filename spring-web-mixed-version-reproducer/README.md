# A Spring Boot 3 library using `HttpHeaders` blows up on Spring Boot 4

Perfectly valid, textbook Spring Boot 3 code fails at runtime on Spring Boot 4 with
`IncompatibleClassChangeError` — thrown from **inside Spring's own `RestTemplate`**, not from the
library.

```bash
./gradlew :server:test
```

```
GET  with HttpHeaders            FAILED
POST with HttpHeaders            FAILED
GET  with LinkedMultiValueMap    PASSED
POST with LinkedMultiValueMap    PASSED
```

**The suite is supposed to fail. That is the report.**

## The code

This fails:

```java
HttpHeaders headers = new HttpHeaders();
headers.setContentType(MediaType.APPLICATION_JSON);

HttpEntity<Void> entity = new HttpEntity<>(headers);
restTemplate.exchange(URI.create(url), HttpMethod.GET, entity, String.class);
```

```
java.lang.IncompatibleClassChangeError: Class org.springframework.http.HttpHeaders
    does not implement the requested interface org.springframework.util.MultiValueMap
        at org.springframework.http.HttpHeaders.isEmpty(HttpHeaders.java:1902)
        at org.springframework.web.client.RestTemplate$HttpEntityRequestCallback.doWithRequest(RestTemplate.java:947)
        at org.springframework.web.client.RestTemplate.doExecute(RestTemplate.java:752)
        at org.springframework.web.client.RestTemplate.exchange(RestTemplate.java:580)
```

This works — the *only* difference is the type holding the headers:

```java
MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

HttpEntity<Void> entity = new HttpEntity<>(headers);
restTemplate.exchange(URI.create(url), HttpMethod.GET, entity, String.class);
```

The library does nothing exotic. It never calls a `Map` method on `HttpHeaders` — only
`setContentType`. The first snippet is the example in `HttpEntity`'s own Javadoc, which still
reads that way in Spring Framework 7.

## The workaround is the wrong way round

To send HTTP headers from a library, you must **not** use the class called `HttpHeaders`. You must
use a raw `LinkedMultiValueMap` instead — the lower-level, untyped, less discoverable option, with
no `setContentType`, no `setAccept`, no `setBearerAuth`. Every piece of API design intuition points
at `HttpHeaders`, and `HttpHeaders` is the one that breaks.

There is nothing at the source level to warn you. The broken and working versions compile to the
*same* constructor descriptor:

```
invokespecial org/springframework/http/HttpEntity."<init>":(Lorg/springframework/util/MultiValueMap;)V
```

The only difference is the runtime class of the argument.

## Why it happens

**1. Spring 6 gave the library no choice.** `HttpEntity` declared only
`HttpEntity(T)` and `HttpEntity(T, MultiValueMap<String,String>)` — there was no
`HttpEntity(T, HttpHeaders)` until 7.0. Since `HttpHeaders implements MultiValueMap` in 6.x, javac
had exactly one applicable constructor and pinned the `MultiValueMap` descriptor into the jar.

**2. Spring 7 kept that constructor but stores the argument unchecked.** `HttpHeaders` no longer
implements `MultiValueMap`; it now *holds* one. The retained constructor funnels into:

```java
public HttpHeaders(MultiValueMap<String, String> headers) {
    Assert.notNull(headers, "MultiValueMap must not be null");   // takes Object — no type check
    this.headers = headers;                                      // putfield — no checkcast
}
```

A `HttpHeaders` is now sitting in a field declared `MultiValueMap` that it does not implement.
Nothing throws yet: the JVM verifier does not check interface assignability (JVMS §4.10.1.2),
deferring it to the first `invokeinterface`.

**3. Spring then breaks itself.** `HttpHeaders.isEmpty()` is `return this.headers.isEmpty()`, and
`RestTemplate.doWithRequest` calls it at line 947. The library had already handed the object over
and was not involved.

## Suggested fix

`HttpHeaders` already contains a private helper for exactly this state — note that its loop
condition tests whether the delegate field is itself an `HttpHeaders`:

```java
private static MultiValueMap<String, String> unwrap(HttpHeaders headers) {
    while (headers.headers instanceof HttpHeaders httpHeaders) {
        headers = httpHeaders;
    }
    return headers.headers;
}
```

It is called from `HttpHeaders(HttpHeaders)` and from `readOnlyHttpHeaders`. It is not called from
`HttpHeaders(MultiValueMap)` — the constructor every already-compiled jar funnels through. Calling
it there fixes `HttpEntity`, `RequestEntity` and `ResponseEntity` at once:

```java
public HttpHeaders(MultiValueMap<String, String> headers) {
    Assert.notNull(headers, "MultiValueMap must not be null");
    this.headers = (headers instanceof HttpHeaders hh ? unwrap(hh) : headers);
}
```

Note that the fix has to be here, at ingestion. It cannot be done at the call site by preferring
Spring 7's header-native API: `containsHeader`, `headerNames`, `headerSet` and
`putAll(HttpHeaders)` all read the same delegate field and throw the same error. Once that field
is wrong, the whole object is unusable.

If unwrapping is unacceptable, failing fast in the constructor would still beat an
`IncompatibleClassChangeError` three frames deep inside `RestTemplate`.

## Setup

| Module | Compiled against | Role |
|---|---|---|
| `legacy-library` | Spring Boot **3.0.13** / Spring Framework **6.0.13** | An ordinary starter with an `@AutoConfiguration`. Built once, never recompiled. |
| `server` | Spring Boot **4.1.1** / Spring Framework **7.0.9** | The application. Consumes the library jar. |

The application does nothing special: the library's `@AutoConfiguration` contributes the client
bean, discovered from the jar's
`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`. That
mechanism is identical in Boot 3 and Boot 4, so the Boot 3 starter is picked up unmodified.

Tests hit a local `com.sun.net.httpserver` stub — no network, no extra dependencies.

**This is not a mixed-jar problem.** `legacy-library` declares its Spring 6 dependencies
`compileOnly`, so they are not exported. The application's runtime classpath has exactly one
`spring-web`:

```bash
./gradlew :server:dependencies --configuration runtimeClasspath
```

## Notes

- Recompiling the library against Spring 7 fixes it, because javac then selects the new
  `HttpHeaders` overloads. This is purely a **binary** compatibility problem — so static "does
  this method still exist" scanners will not catch it. The method exists, with a matching
  descriptor.
- Also reproduces with libraries built against Spring Framework 6.2.x and 5.3.x.
- `RequestEntity` and `ResponseEntity` fail the same way. `ResponseEntity` matters server-side: a
  library returning one built from `HttpHeaders` fails in
  `HttpEntityMethodProcessor.handleReturnValue`.
- Not tested here: `WebClient` and `RestClient`.
- Separate and expected: the removed `Map` methods (`containsKey`, `keySet`, `entrySet`) produce
  an honest `NoSuchMethodError`. That is documented migration, and not what this report is about.

## Related issues

- [spring-framework#33913](https://github.com/spring-projects/spring-framework/issues/33913) — the
  change that dropped the `MultiValueMap` contract. Binary compatibility for already-compiled
  jars and the retained constructors are not discussed.
- [spring-framework#36280](https://github.com/spring-projects/spring-framework/issues/36280) — the
  same error from `HttpEntityMethodProcessor`, closed as `status: invalid`. The reporter had a
  third-party jar compiled against an older Spring, which is consistent with this bug rather than
  with classpath contamination.
- [spring-security#17060](https://github.com/spring-projects/spring-security/issues/17060),
  [spring-restdocs#964](https://github.com/spring-projects/spring-restdocs/issues/964) —
  first-party Spring projects hit by the same removal.
