# Learning Guide: Direct Debit Sandbox in Java + Spring Boot

This guide is written for someone who has never written Java or Spring Boot before.
Every concept is explained from scratch, using examples drawn directly from this codebase.
If you see a piece of syntax in a file and wonder "what does that even mean?" — look it up here.

---

## Table of Contents

1. [What is Java, and why does it look like this?](#1-what-is-java-and-why-does-it-look-like-this)
2. [Packages and imports](#2-packages-and-imports)
3. [Classes, and why everything is inside one](#3-classes-and-why-everything-is-inside-one)
4. [Access modifiers: public, private, protected](#4-access-modifiers-public-private-protected)
5. [Data types: String, int, boolean, List, Map](#5-data-types-string-int-boolean-list-map)
6. [Methods: how functions work in Java](#6-methods-how-functions-work-in-java)
7. [null, and why it causes so many bugs](#7-null-and-why-it-causes-so-many-bugs)
8. [What is Spring Boot?](#8-what-is-spring-boot)
9. [Annotations: those @ symbols everywhere](#9-annotations-those--symbols-everywhere)
10. [Dependency Injection: the magic of @RequiredArgsConstructor](#10-dependency-injection-the-magic-of-requiredargsconstructor)
11. [The three layers: Controller, Service, Store](#11-the-three-layers-controller-service-store)
12. [@RestController and how HTTP requests arrive](#12-restcontroller-and-how-http-requests-arrive)
13. [@Service: where the real logic lives](#13-service-where-the-real-logic-lives)
14. [DTOs: why we have so many small classes](#14-dtos-why-we-have-so-many-small-classes)
15. [Lombok: @Data, @Builder, @Slf4j](#15-lombok-data-builder-slf4j)
16. [Enums: a fixed list of choices](#16-enums-a-fixed-list-of-choices)
17. [Interfaces: a contract, not an implementation](#17-interfaces-a-contract-not-an-implementation)
18. [ConcurrentHashMap: thread-safe storage](#18-concurrenthashmap-thread-safe-storage)
19. [UUID: generating unique IDs](#19-uuid-generating-unique-ids)
20. [Async: doing work in the background](#20-async-doing-work-in-the-background)
21. [The Stream API: filter, map, toList](#21-the-stream-api-filter-map-tolist)
22. [The switch expression (the modern kind)](#22-the-switch-expression-the-modern-kind)
23. [RestTemplate: making HTTP requests from Java](#23-resttemplate-making-http-requests-from-java)
24. [Validation: @NotBlank, @NotNull, @Valid](#24-validation-notblank-notnull-valid)
25. [The Builder pattern](#25-the-builder-pattern)
26. [How a subscription request flows end-to-end](#26-how-a-subscription-request-flows-end-to-end)
27. [How async callbacks work in this project](#27-how-async-callbacks-work-in-this-project)
28. [The Scenario Engine: simulating outcomes without a real bank](#28-the-scenario-engine-simulating-outcomes-without-a-real-bank)
29. [Common mistakes and what the errors mean](#29-common-mistakes-and-what-the-errors-mean)
30. [The Provision pattern: registering configuration once](#30-the-provision-pattern-registering-configuration-once)
31. [Composite map keys](#31-composite-map-keys)
32. [Extending an existing interface](#32-extending-an-existing-interface)
33. [Fallback resolution: trying one source, falling back to another](#33-fallback-resolution-trying-one-source-falling-back-to-another)
34. [Boolean.TRUE.equals() — handling nullable booleans safely](#34-booleantrueequals--handling-nullable-booleans-safely)
35. [ResponseEntity — returning HTTP status codes alongside a body](#35-responseentity--returning-http-status-codes-alongside-a-body)
36. [required = false on headers — manual vs automatic validation](#36-required--false-on-headers--manual-vs-automatic-validation)
37. [The .http request file format](#37-the-http-request-file-format)
38. [Conditional field preservation in builders](#38-conditional-field-preservation-in-builders)
39. [Build tools: Maven vs Gradle](#39-build-tools-maven-vs-gradle)
40. [Project folder structure explained](#40-project-folder-structure-explained)
41. [Gradle and Java Version Compatibility](#41-gradle-and-java-version-compatibility)
42. [Where do installed packages live?](#42-where-do-installed-packages-live)
43. [What to commit to Git (and what to ignore)](#43-what-to-commit-to-git-and-what-to-ignore)
44. [The hidden danger of Spring Security dependency](#44-the-hidden-danger-of-spring-security-dependency)
45. [Secondary indexes — fast lookups at scale](#45-secondary-indexes--fast-lookups-at-scale)
46. [Separating create from update in the store](#46-separating-create-from-update-in-the-store)
47. [Data normalization — keeping your data lean](#47-data-normalization--keeping-your-data-lean)

---

## 1. What is Java, and why does it look like this?

Java is a **statically typed** language. That means you must tell it the type of every variable before you use it. You cannot just write:

```
name = "Kingsley"   // This is Python/JavaScript. Java will refuse.
```

In Java you write:

```java
String name = "Kingsley";  // You declare the type (String) first.
```

Java is also **compiled**. Your code is converted into bytecode before it runs. This is why errors show up at "compile time" before the program even starts — the compiler checks everything first.

Java is **verbose** compared to Python or JavaScript. You will see a lot of `{}` braces, semicolons, and type declarations. This is intentional — it makes large codebases easier to understand because everything is explicit.

---

## 2. Packages and imports

Every Java file starts with a `package` declaration:

```java
package com.itc.direct_debit_sandbox.subscriptions;
```

A package is just a folder path. This file lives in:
`src/main/java/com/itc/direct_debit_sandbox/subscriptions/`

If you want to use a class from a different package, you must `import` it:

```java
import com.itc.direct_debit_sandbox.store.SubscriptionRecord;
```

Without the import, Java doesn't know what `SubscriptionRecord` refers to. Think of imports as "hey Java, this is where that class lives."

---

## 3. Classes, and why everything is inside one

In Java, **all code lives inside a class**. There is no such thing as a loose function floating around. Every method must belong to a class.

```java
public class SubscriptionService {
    // everything in here belongs to this class
}
```

The file name must match the class name exactly. `SubscriptionService.java` must contain `class SubscriptionService`.

---

## 4. Access modifiers: public, private, protected

These control who can see and use things.

| Modifier    | Who can access it                           |
|-------------|---------------------------------------------|
| `public`    | Anyone, from anywhere                       |
| `private`   | Only code inside the same class             |
| `protected` | Same class + subclasses                     |
| (none)      | Only code in the same package               |

In this project you will see:

```java
private final InMemoryStore store;       // Only SubscriptionService can use this
public Map<String, Object> subscribe()   // Anyone can call this (Spring needs it to be public)
private Map<String, Object> validateHeaders()  // Helper used internally only
```

Rule of thumb: make things `private` unless they absolutely need to be public.

---

## 5. Data types: String, int, boolean, List, Map

Java has two kinds of types: **primitives** and **objects**.

Primitives:
```java
int count = 5;           // whole numbers
boolean active = true;   // true or false
double price = 9.99;     // decimal numbers
```

Objects (capital letter, more features):
```java
String name = "Kingsley";          // text
Integer count = 5;                 // int but as an object (can be null)
Boolean active = true;             // boolean but as an object (can be null)
```

Collections:
```java
List<String> names = new ArrayList<>();    // an ordered list of Strings
Map<String, Object> response = new HashMap<>();  // key-value pairs, like a dictionary
```

`Map<String, Object>` means: keys are Strings, values can be anything. You see this heavily used in service return types in this project because the API responses have varying shapes.

---

## 6. Methods: how functions work in Java

A method signature looks like this:

```java
public Map<String, Object> subscribe(String transflowId, String apiKey) {
    // body
}
```

Breaking it down:
- `public` — access modifier (anyone can call this)
- `Map<String, Object>` — the **return type** (what this method gives back)
- `subscribe` — the method name
- `(String transflowId, String apiKey)` — parameters (inputs)

`void` means the method returns nothing:

```java
public void sendCallback(String url) {
    // does something, returns nothing
}
```

---

## 7. null, and why it causes so many bugs

`null` means "no value". In Java, any object variable can be `null`.

```java
SubscriptionRecord record = store.getSubscription("some-id");
// If "some-id" doesn't exist, record is null

record.getStatus();  // CRASH — NullPointerException, because record is null
```

This is why you see null checks everywhere in the services:

```java
if (record == null) {
    // return an error response instead of crashing
    return errorMap;
}
```

The most common Java runtime error is `NullPointerException` (NPE). It means you tried to call a method on something that was null.

---

## 8. What is Spring Boot?

Spring Boot is a framework that eliminates enormous amounts of boilerplate. Without it, you would need to:
- Write your own HTTP server
- Manually wire every class to every other class
- Configure JSON parsing by hand

Spring Boot handles all of that. You just annotate your classes and it figures out the rest.

The entry point is [DirectDebitSandboxApplication.java](src/main/java/com/itc/direct_debit_sandbox/DirectDebitSandboxApplication.java):

```java
@SpringBootApplication
public class DirectDebitSandboxApplication {
    public static void main(String[] args) {
        SpringApplication.run(DirectDebitSandboxApplication.class, args);
    }
}
```

`SpringApplication.run(...)` boots the entire server. Spring scans all classes, finds annotations, creates objects, wires them together, and starts listening on port 8080.

---

## 9. Annotations: those @ symbols everywhere

An annotation is metadata attached to a class, method, or field. It tells Spring (or Java itself) to treat that thing specially.

```java
@RestController          // "This class handles HTTP requests"
@Service                 // "This class contains business logic"
@Component               // "This is a general-purpose Spring-managed object"
@RequestMapping("/subscription")   // "All endpoints in this class start with /subscription"
@PostMapping("/subscribe")         // "This method handles POST /subscription/subscribe"
@RequestBody             // "Read the JSON body and turn it into this object"
@RequestHeader("x-key")  // "Read the HTTP header named x-key"
@Async                   // "Run this method in a background thread"
@NotBlank                // "Validation: this field must not be blank"
```

Annotations don't do anything by themselves. Spring reads them at startup and decides what to do. If you forget an annotation, Spring doesn't know about your class and nothing will work.

---

## 10. Dependency Injection: the magic of @RequiredArgsConstructor

Dependency Injection (DI) means: instead of creating your own dependencies, you declare what you need and Spring hands them to you.

Without DI you would write:
```java
public class SubscriptionService {
    private InMemoryStore store = new InMemoryStore();  // you make it yourself
}
```

With DI you write:
```java
@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final InMemoryStore store;  // Spring gives this to you
}
```

`@RequiredArgsConstructor` is a Lombok annotation that generates a constructor for every `private final` field. Spring sees that constructor and calls it, passing in the objects it manages.

Why is this better? Because:
- You don't create objects, Spring does — so you can easily swap implementations
- Tests can inject fake/mock versions of dependencies
- You can't accidentally have two different stores with different data

The `private final` is important: `final` means the field cannot be reassigned after the constructor runs. This prevents accidental bugs where someone replaces your store mid-request.

---

## 11. The three layers: Controller, Service, Store

This project follows a standard layered architecture:

```
HTTP Request
     ↓
Controller   (receives the request, reads headers/body, calls the service)
     ↓
Service      (business logic: validate, process, decide what to do)
     ↓
Store        (saves/retrieves data from in-memory maps)
     ↓
(async) CallbackService  (fires HTTP callbacks to the merchant's server)
```

Each layer only talks to the layer directly below it. The controller doesn't touch the store directly. The store doesn't know about HTTP. This separation makes each piece easy to understand and change independently.

---

## 12. @RestController and how HTTP requests arrive

```java
@RestController
@RequestMapping("/subscription")
public class SubscriptionController {

    @PostMapping("/subscribe")
    public Map<String, Object> subscribe(
            @RequestHeader("x-transflowId") String transflowId,
            @RequestBody SubscriptionRequestDto req) {
        ...
    }
}
```

When a POST request arrives at `/subscription/subscribe`:

1. Spring finds this controller because of `@RequestMapping("/subscription")`
2. It finds this method because of `@PostMapping("/subscribe")`
3. It reads the `x-transflowId` header and passes it as `transflowId`
4. It takes the JSON body, converts it into a `SubscriptionRequestDto` object, and passes it as `req`
5. The method runs, returns a `Map<String, Object>`
6. Spring converts that Map back to JSON and sends it as the HTTP response

`@RestController` = `@Controller` + automatically convert return values to JSON. Without `@RestController` you would get an HTML page, not JSON.

---

## 13. @Service: where the real logic lives

The service layer is where decisions happen. Look at `subscribe()` in [SubscriptionService.java](src/main/java/com/itc/direct_debit_sandbox/subscriptions/SubscriptionService.java):

```java
// 1. Check if headers are valid
Map<String, Object> authError = validateHeaders(transflowId, apiKey, country);
if (authError != null) return authError;

// 2. Check for duplicate reference
if (store.getSubscriptionByReference(req.getReferenceNo()) != null) { ... }

// 3. Generate IDs
String subscriptionId = "SUB" + UUID.randomUUID()...

// 4. Build the record
SubscriptionRecord record = SubscriptionRecord.builder()...build();

// 5. Save it
store.createSubscription(subscriptionId, record);

// 6. Fire callbacks asynchronously
callbackService.fireCallbacks(record);

// 7. Return processing response
return response;
```

Every business rule lives here, not in the controller and not in the store. If you need to change what happens when someone subscribes, you change the service.

---

## 14. DTOs: why we have so many small classes

DTO stands for **Data Transfer Object**. It's a class whose only job is to carry data.

When a JSON body arrives:
```json
{
  "merchantId": "MERCH_123",
  "debitAccount": "0241234567",
  "debitAmount": "50.00"
}
```

Spring needs somewhere to put this data. It creates a `SubscriptionRequestDto` and fills in the fields.

Why not use the `SubscriptionRecord` directly? Because:
- The request shape is different from the stored shape (e.g., `subscriptionId` and `mandateId` are generated, not sent by the caller)
- DTOs can have validation annotations without polluting the storage model
- Input and output can evolve independently

You will see many DTOs: `SubscriptionRequestDto`, `UpdateRequest`, `CancelRequest`, `CustomerSubRequest`, etc. Each matches exactly what one endpoint needs.

---

## 15. Lombok: @Data, @Builder, @Slf4j

Lombok is a library that generates Java boilerplate at compile time. It saves hundreds of lines.

**@Data** generates: getters, setters, `equals()`, `hashCode()`, `toString()`.

Without Lombok:
```java
public class CancelRequest {
    private String subscriptionId;
    
    public String getSubscriptionId() { return subscriptionId; }
    public void setSubscriptionId(String s) { this.subscriptionId = s; }
    // ... equals, hashCode, toString ...
}
```

With Lombok:
```java
@Data
public class CancelRequest {
    private String subscriptionId;
}
```

**@Builder** generates a fluent builder pattern (explained in section 25).

**@Slf4j** generates a logger field called `log`. Instead of writing:
```java
private static final Logger log = LoggerFactory.getLogger(CallbackService.class);
```
You just write `@Slf4j` on the class and then use `log.info(...)`, `log.error(...)` anywhere.

**@RequiredArgsConstructor** generates a constructor for all `final` fields (used for DI).

---

## 16. Enums: a fixed list of choices

An enum is a type with a fixed set of allowed values.

```java
public enum FrequencyType {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
}
```

Usage in code:
```java
FrequencyType type = FrequencyType.MONTHLY;
```

Usage in JSON: when Jackson (Spring's JSON library) sees `"frequencyType": "MONTHLY"` in a request body, it converts the string `"MONTHLY"` into `FrequencyType.MONTHLY` automatically — because the field in the DTO is typed as `FrequencyType`.

Why use an enum instead of a `String`? Because you can't accidentally send `"MONTHLYYY"` — it will fail validation. The compiler also catches typos.

---

## 17. Interfaces: a contract, not an implementation

An interface says "any class that implements me must have these methods." It doesn't contain any logic itself.

```java
public interface Store {
    void createSubscription(String id, SubscriptionRecord record);
    void updateSubscription(String id, SubscriptionRecord record);
    SubscriptionRecord getSubscription(String id);
    // ... etc
}
```

`InMemoryStore` implements this:
```java
public class InMemoryStore implements Store {
    // provides the actual code for every method in Store
}
```

Why? Because if you later want a database-backed store, you create `DatabaseStore implements Store`. All the services that depend on `Store` work without any changes — they don't care how data is stored, just that it has those methods.

---

## 18. ConcurrentHashMap: thread-safe storage

A regular `HashMap` is not safe when multiple threads read and write at the same time. This project fires async callbacks (background threads) that write transaction records while incoming HTTP requests also write subscription records.

```java
private final Map<String, SubscriptionRecord> subscriptions = new ConcurrentHashMap<>();
```

`ConcurrentHashMap` handles concurrent access internally so two threads can write without corrupting the data. If you used a plain `HashMap`, you could get data corruption or crashes under load — a class of bug that only shows up in production and is very hard to reproduce.

---

## 19. UUID: generating unique IDs

`UUID` (Universally Unique Identifier) generates a random 128-bit number that is statistically guaranteed to be unique.

```java
String subscriptionId = "SUB" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
// e.g. "SUBA3F82C1D9B47"
```

Breaking that chain down:
- `UUID.randomUUID()` — generates something like `a3f82c1d-9b47-4e3a-bc12-...`
- `.toString()` — converts it to a String
- `.replace("-", "")` — removes the dashes to get `a3f82c1d9b47...`
- `.substring(0, 12)` — takes only the first 12 characters
- `.toUpperCase()` — makes it uppercase

The prefix `"SUB"` makes it obvious at a glance that this ID belongs to a subscription.

---

## 20. Async: doing work in the background

When a merchant subscribes, this project needs to fire a callback to the merchant's server. But the merchant should get a response immediately — they shouldn't have to wait.

The solution is `@Async`:

```java
@Async("callbackExecutor")
public void fireCallbacks(SubscriptionRecord record) {
    Thread.sleep(2000);      // wait 2 seconds (simulating processing time)
    firePreapprovalCallback(record);
    Thread.sleep(5000);      // wait 5 more seconds
    fireTransactionCallback(record);
}
```

`@Async` tells Spring: "don't run this on the HTTP request thread — put it in the background thread pool named callbackExecutor and continue immediately."

The thread pool is configured in [AsyncConfig.java](src/main/java/com/itc/direct_debit_sandbox/config/AsyncConfig.java):

```java
executor.setCorePoolSize(5);    // always keep 5 threads ready
executor.setMaxPoolSize(10);    // grow up to 10 under load
executor.setQueueCapacity(100); // queue up to 100 tasks waiting for a thread
```

Without this, every subscription would block for 7 seconds before the merchant got a response.

Note: `Thread.sleep()` pauses the current thread. It takes **milliseconds** as input. `Thread.sleep(2000)` = wait 2 seconds.

---

## 21. The Stream API: filter, map, toList

Java 8 introduced Streams — a way to process collections in a readable, pipeline style.

```java
return subscriptions.values().stream()
        .filter(s -> s.getDebitAccount().equals(debitAccount)
                  && s.getProductId().equals(productId))
        .toList();
```

Reading it left to right:
- `subscriptions.values()` — all SubscriptionRecord objects in the map
- `.stream()` — start processing them as a stream
- `.filter(s -> ...)` — keep only records where the condition is true (`s` is each record)
- `.toList()` — collect the surviving records into a List

The `s -> s.getDebitAccount().equals(debitAccount)` part is a **lambda** — an inline anonymous function. `s` is the parameter, `s.getDebitAccount()...` is the body.

Without streams you would write:
```java
List<SubscriptionRecord> result = new ArrayList<>();
for (SubscriptionRecord s : subscriptions.values()) {
    if (s.getDebitAccount().equals(debitAccount) && s.getProductId().equals(productId)) {
        result.add(s);
    }
}
return result;
```

Same thing, more lines.

---

## 22. The switch expression (the modern kind)

Old Java switch (still valid, but verbose):
```java
String message;
switch (responseCode) {
    case "01":
        message = "Success";
        break;
    case "100":
        message = "Failed";
        break;
    default:
        message = "Unknown";
}
```

Modern Java switch expression (used in [ScenarioEngine.java](src/main/java/com/itc/direct_debit_sandbox/scenarios/ScenarioEngine.java)):
```java
String message = switch (responseCode) {
    case "01"  -> "Transaction processed successfully";
    case "100" -> "Payment failed";
    default    -> "Payment failed";
};
```

- No `break` needed
- The whole thing is an expression — it produces a value you can assign directly
- `->` means "for this case, produce this value"

---

## 23. RestTemplate: making HTTP requests from Java

`RestTemplate` is Spring's built-in HTTP client. In `CallbackService`, after processing a debit, the sandbox needs to POST a notification to the merchant's server:

```java
HttpHeaders headers = new HttpHeaders();
headers.setContentType(MediaType.APPLICATION_JSON);
HttpEntity<Object> entity = new HttpEntity<>(payload, headers);

ResponseEntity<String> response = restTemplate.exchange(
    callbackUrl, HttpMethod.POST, entity, String.class
);
```

Breaking it down:
- `HttpHeaders` — the HTTP headers to attach
- `HttpEntity<Object>` — wraps the payload (body) + headers into one object
- `restTemplate.exchange(...)` — makes the HTTP call
  - `callbackUrl` — the URL to POST to
  - `HttpMethod.POST` — the HTTP method
  - `entity` — the body + headers
  - `String.class` — what type to deserialize the response body as

The `RestTemplate` bean is created once in [AppConfig.java](src/main/java/com/itc/direct_debit_sandbox/config/AppConfig.java) and injected wherever it's needed.

---

## 24. Validation: @NotBlank, @NotNull, @Valid

Spring's validation layer lets you declare rules on DTO fields and have them checked automatically before your method even runs.

```java
@Data
public class SubscriptionRequestDto {
    @NotBlank
    private String merchantId;    // must not be null, empty, or whitespace-only

    @NotNull
    private FrequencyType frequencyType;  // must not be null (but empty string doesn't apply to enums)

    private String endDate;  // no annotation = optional, can be null
}
```

In the controller, `@Valid` triggers the check:

```java
public Map<String, Object> subscribe(@Valid @RequestBody SubscriptionRequestDto req) {
```

If `merchantId` is blank, Spring automatically returns a 400 error before your code runs. Without `@Valid`, annotations on the DTO are ignored.

---

## 25. The Builder pattern

The Builder pattern solves a problem: constructors with many parameters are hard to read.

Without Builder:
```java
new SubscriptionRecord("SUB123", "MAND456", "MERCH789", "PROD001",
    "0241234567", "50.00", FrequencyType.MONTHLY, "2026-01-01",
    "2027-01-01", "15", "14:30", "REF001", "MTN", "GHS", "GH",
    "0241234567", "https://callback.url", "ACTIVE", true, true, null, "2026-05-07T12:00:00Z");
```

Which argument is which? Impossible to tell.

With `@Builder` (Lombok generates this):
```java
SubscriptionRecord record = SubscriptionRecord.builder()
        .subscriptionId("SUB123")
        .mandateId("MAND456")
        .merchantId("MERCH789")
        .debitAccount("0241234567")
        .debitAmount("50.00")
        .status("ACTIVE")
        .build();  // <-- the builder assembles the object
```

Every field is explicitly named. You can skip optional fields. You can read it without counting positions.

The `.build()` call at the end is required — it actually creates the object. Before `.build()`, you're talking to the Builder helper object, not the real SubscriptionRecord.

---

## 26. How a subscription request flows end-to-end

Here is the full journey of `POST /subscription/subscribe`:

```
1. HTTP POST arrives at port 8080
         ↓
2. Spring routes it to SubscriptionController.subscribe()
   - Reads headers: x-transflowId, x-key, x-country
   - Reads JSON body → SubscriptionRequestDto (Spring does this automatically)
   - @Valid checks all @NotBlank fields
         ↓
3. SubscriptionService.subscribe() is called
   a. validateHeaders() — are headers non-blank?
   b. Duplicate check — does a subscription with this referenceNo already exist?
   c. Generate subscriptionId = "SUB" + random
   d. Generate mandateId     = "MAND" + random
   e. Build SubscriptionRecord with all fields
   f. store.createSubscription(id, record)  — saved in primary map + secondary indexes
   g. callbackService.fireCallbacks(record)  — starts ASYNC background thread
   h. Return { "responseCode": "03", "responseMessage": "your request is being processed" }
         ↓
4. Spring converts the Map to JSON and sends the HTTP response.
   The caller gets the response IMMEDIATELY, while step 3g is still running.
         ↓
5. In the background (2 seconds later):
   CallbackService.firePreapprovalCallback()
   - resolveCallbackUrl() checks provision store for merchantId+productId first
   - Falls back to record.callbackUrl if no provision found
   - Sends POST to the resolved URL
         ↓
6. In the background (5 more seconds later):
   CallbackService.fireTransactionCallback()
   - ScenarioEngine determines outcome from debitAccount's last 3 digits
   - Saves TransactionRecord to store
   - resolveCallbackUrl() resolves the URL the same way as step 5
   - Sends POST with transaction result to the resolved URL
```

---

## 27. How async callbacks work in this project

The real ITC API processes debits with mobile networks asynchronously. The sandbox mimics this by using Java threads.

Key files:
- [CallbackService.java](src/main/java/com/itc/direct_debit_sandbox/callbacks/CallbackService.java) — fires the callbacks
- [AsyncConfig.java](src/main/java/com/itc/direct_debit_sandbox/config/AsyncConfig.java) — configures the thread pool
- [SandboxConfig.java](src/main/java/com/itc/direct_debit_sandbox/config/SandboxConfig.java) — holds delay values (2000ms, 5000ms)

The delays are configurable in `application.properties`:
```properties
sandbox.callback-delay-preapproval=2000
sandbox.callback-delay-transaction=5000
```

`@ConfigurationProperties` in `SandboxConfig` reads these values and makes them available as Java fields.

The thread pool prevents the sandbox from creating unlimited threads. If 100 subscriptions come in at once, 10 threads handle callbacks while the rest wait in a queue of 100 slots.

---

## 28. The Scenario Engine: simulating outcomes without a real bank

In a real payment system, the bank decides whether a transaction succeeds. In a sandbox, we simulate this with a simple rule: **the last 3 digits of the debit account number determine the outcome**.

```java
case "001" -> "01";   // Success — use account ending in 001
case "101" -> "101";  // Insufficient funds — use account ending in 101
case "131" -> "131";  // Timeout — use account ending in 131
```

This means a tester can control which scenario fires just by choosing their account number:
- Account `0241234001` → always succeeds
- Account `0241234101` → always fails with insufficient funds
- Account `0241234131` → always times out

This is a common sandbox design pattern. It makes testing predictable without needing a real bank to cooperate.

See [ScenarioEngine.java](src/main/java/com/itc/direct_debit_sandbox/scenarios/ScenarioEngine.java).

---

## 29. Common mistakes and what the errors mean

**`NullPointerException`**
You called a method on an object that is `null`. Check for null before using it:
```java
if (record != null) { record.getStatus(); }
```

**`HttpMessageNotReadableException`**
Spring couldn't parse the JSON body. Usually means:
- JSON is malformed (missing comma, wrong quotes)
- A field has the wrong type (e.g., sending `"true"` as a string for a `Boolean` field)
- An enum value doesn't match (e.g., `"monthly"` instead of `"MONTHLY"`)

**`MethodArgumentNotValidException`**
A `@NotBlank` or `@NotNull` check failed. One of the required fields was empty or missing.

**`NoSuchBeanDefinitionException`**
Spring can't find a dependency to inject. Check that the class you depend on has `@Service`, `@Component`, or `@Bean`, and that it's in a package Spring scans.

**`@Async` method doesn't run asynchronously**
`@Async` only works when the method is called from **outside** the class. If a method calls another async method in the same class, it runs synchronously. This is a Spring limitation.

**`ConcurrentModificationException`**
You're modifying a plain `HashMap` while iterating over it from another thread. Use `ConcurrentHashMap` instead.

**Port 8080 already in use**
Another process is using port 8080. Either stop it, or change `server.port` in `application.properties`.

---

## 30. The Provision pattern: registering configuration once

In the original design, every subscribe request had to include a `callbackUrl` field. This is awkward — if the merchant has 1000 subscribers, every single request needs to carry the same URL.

The **provision pattern** solves this: the merchant calls `POST /provision` once to register their callback URL for a given `merchantId + productId` combination. Every callback fired for that merchant+product automatically goes to the registered URL, forever, without the subscriber needing to send it again.

```
First (once):   POST /provision  → stores callbackUrl for MERCH_123 + PROD_001
Later (many):   POST /subscription/subscribe → no callbackUrl needed in body
                Callback fires → resolveCallbackUrl looks up MERCH_123 + PROD_001 → gets the URL
```

This is a real-world pattern used when a single piece of config (URL, API key, webhook secret) belongs to the merchant, not to each individual transaction. The merchant registers it once at onboarding; every subsequent operation inherits it automatically.

In this project the provision also stores catalogue configuration (`retryAttempts`, `skipFactor`, `daysToDebitDayNotice`) — again, things set once at the merchant level rather than repeated on every request.

See [ProvisionController.java](src/main/java/com/itc/direct_debit_sandbox/provision/ProvisionController.java) and [ProvisionService.java](src/main/java/com/itc/direct_debit_sandbox/provision/ProvisionService.java).

---

## 31. Composite map keys

The provision store needs to look up a record by **two** values: `merchantId` and `productId`. But `ConcurrentHashMap` only takes one key.

The solution is to combine them into a single String:

```java
// Saving
provisions.put(merchantId + ":" + productId, record);
// e.g. key = "MERCH_123:PROD_001"

// Retrieving
provisions.get(merchantId + ":" + productId);
```

The `:` separator is important — it stops `"MERCH1"` + `"23:PROD"` from accidentally colliding with `"MERCH123"` + `":PROD"`. As long as your IDs never contain `:`, this is safe and simple.

This is called a **composite key**. You will see it everywhere in systems that use simple maps instead of databases. A database would use a multi-column primary key; here we simulate it by gluing strings together.

---

## 32. Extending an existing interface

When we added the provision feature, we needed to add two new methods to the data store: `saveProvision` and `getProvision`. Because `InMemoryStore` implements the `Store` interface, we had to add the methods to **both** places.

Step 1 — declare the contract in the interface:

```java
// Store.java
void saveProvision(String merchantId, String productId, ProvisionRecord record);
ProvisionRecord getProvision(String merchantId, String productId);
```

Step 2 — provide the implementation in the class:

```java
// InMemoryStore.java
public void saveProvision(String merchantId, String productId, ProvisionRecord record) {
    provisions.put(merchantId + ":" + productId, record);
}

public ProvisionRecord getProvision(String merchantId, String productId) {
    return provisions.get(merchantId + ":" + productId);
}
```

If you add methods to the interface but forget to add them to the implementing class, **the compiler will refuse to build**. You'll see:

```
InMemoryStore is not abstract and does not override abstract method saveProvision(...)
```

This is one of the main benefits of interfaces — they enforce that every implementation stays complete. You can never forget to implement a method.

---

## 33. Fallback resolution: trying one source, falling back to another

`resolveCallbackUrl()` in `CallbackService` is a classic example of a **fallback pattern**:

```java
private String resolveCallbackUrl(String merchantId, String productId, String fallback) {
    ProvisionRecord provision = store.getProvision(merchantId, productId);
    if (provision != null && provision.getCallbackUrl() != null) {
        return provision.getCallbackUrl();   // primary source
    }
    return fallback;                         // secondary source
}
```

Reading this: "try to get the URL from the provision store first. If it's not there (maybe the merchant hasn't provisioned yet), use whatever was passed in as a fallback."

This pattern appears constantly in real-world code:
- Try the cache → fall back to the database
- Try an environment variable → fall back to a default value
- Try a user-provided config → fall back to the system default

The method takes `fallback` as a parameter rather than hardcoding `null`. This makes the caller decide what "no provision" means in their context — some callers might have a per-request URL to fall back to; others might have nothing at all.

---

## 34. Boolean.TRUE.equals() — handling nullable booleans safely

In Java, `boolean` (lowercase) is a primitive — it can only be `true` or `false`, never `null`.
But `Boolean` (uppercase) is an object — it can be `true`, `false`, **or `null`**.

In DTOs you often use `Boolean` (uppercase) so that a missing field deserializes as `null` rather than defaulting to `false`. But then you have to check for null before using it.

This crashes if `req.getTriggerDebitStatus()` is `null`:
```java
if (req.getTriggerDebitStatus() == true)  // NullPointerException if null
```

This is safe:
```java
Boolean.TRUE.equals(req.getTriggerDebitStatus())
// Returns true  if the value is Boolean.TRUE
// Returns false if the value is Boolean.FALSE
// Returns false if the value is null  — no crash
```

You see this in `SubscriptionService.subscribe()`:
```java
.triggerDebitStatus(Boolean.TRUE.equals(req.getTriggerDebitStatus()))
```

The `SubscriptionRecord` field is a primitive `boolean` — it can't be null. So we convert the nullable `Boolean` from the DTO into a non-nullable `boolean` by using `Boolean.TRUE.equals()`, which turns `null` into `false` safely.

---

## 35. ResponseEntity — returning HTTP status codes alongside a body

Most endpoints in this project return a plain `Map<String, Object>`, which Spring serialises to JSON with a `200 OK` status automatically.

But `LifecycleController` uses `ResponseEntity<?>`:

```java
@PostMapping("/pause")
public ResponseEntity<?> pause(...) {
    if (isUnauthorized(transflowId, key, country)) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponseDto.builder()
                        .responseCode("401")
                        .responseMessage("Unauthorized.")
                        .build());
    }
    return ResponseEntity.ok(lifecycleService.pause(request));
}
```

`ResponseEntity` lets you control:
- The **HTTP status code** (200, 401, 404, 500, etc.)
- The **response body**
- The **response headers** (if needed)

`ResponseEntity.ok(body)` is a shortcut for `ResponseEntity.status(200).body(body)`.

The `<?>` means "I don't know the exact type of the body at compile time" — it could be an `ApiResponseDto` (for errors) or a `Map` (for success). The `?` is a **wildcard** that says "any type is acceptable here."

When should you use `ResponseEntity` vs returning the object directly?
- Return `ResponseEntity` when different outcomes produce **different HTTP status codes** (200 vs 401 vs 404)
- Return the object directly when it's always `200 OK` and you just want the JSON body

---

## 36. required = false on headers — manual vs automatic validation

There are two ways to read request headers in Spring:

**Automatic (strict) — Spring throws an error if the header is missing:**
```java
@RequestHeader("x-transflowId") String transflowId
```
If the caller doesn't send `x-transflowId`, Spring returns a `400 Bad Request` before your code even runs.

**Manual (lenient) — your code receives null and decides what to do:**
```java
@RequestHeader(value = "x-transflowId", required = false) String transflowId
```
If the header is missing, `transflowId` is `null`. Your method runs, and you decide the response.

`LifecycleController` uses `required = false` and then calls `isUnauthorized()` manually:

```java
private boolean isUnauthorized(String transflowId, String key, String country) {
    return transflowId == null || transflowId.isEmpty() || ...;
}
```

This gives you control over the **exact error response** — instead of Spring's generic 400 message, you return a structured JSON body with your own `responseCode` and `responseMessage`.

`SubscriptionController` uses the strict version (no `required = false`). Both approaches work; the strict version is less code, the lenient version gives a friendlier error message.

---

## 37. The .http request file format

The [requests.http](requests.http) file is an **HTTP client script** supported natively by IntelliJ IDEA and by the REST Client extension in VS Code.

**Variables** are declared at the top:
```
@baseUrl = http://localhost:8080
@merchantId = MERCH_12345
```

And used anywhere with `{{variableName}}`:
```
POST {{baseUrl}}/subscription/subscribe
```

**A single request** looks like this:
```
POST {{baseUrl}}/provision
Content-Type: application/json
x-transflowId: {{transflowId}}
x-key: {{apiKey}}

{
  "merchantId": "{{merchantId}}"
}
```

The blank line between headers and body is **required** — it tells the parser "headers are done, body starts here."

**Requests are separated** by `###`. Everything between two `###` lines is one request.

```
### First request
GET {{baseUrl}}/health

### Second request
POST {{baseUrl}}/provision
...
```

The `###` line can also include a description that shows up as the request name in the IDE.

Why use `.http` files instead of Postman? They live **inside the repository** — anyone who clones the project gets the test requests immediately, with no import/export steps.

---

## 38. Conditional field preservation in builders

When `ProvisionService` updates an existing provision record, it needs to preserve any config values the caller didn't send:

```java
ProvisionRecord existing = store.getProvision(req.getMerchantId(), req.getProductId());

ProvisionRecord record = ProvisionRecord.builder()
        .callbackUrl(req.getCallbackUrl())   // always overwrite this
        .retryAttempts(
            req.getRetryAttempts() != null
                ? req.getRetryAttempts()              // caller sent a new value → use it
                : (existing != null ? existing.getRetryAttempts() : null)  // keep old value
        )
        .build();
```

Reading the ternary `? :` operator for the first time:
```java
condition ? valueIfTrue : valueIfFalse
```

So:
```java
req.getRetryAttempts() != null ? req.getRetryAttempts() : existing.getRetryAttempts()
```
means: "if the request included `retryAttempts`, use it; otherwise keep whatever was there before."

This is called a **partial update** or **merge** pattern. It means callers can update a single field without accidentally wiping out fields they didn't mention. You saw the same pattern earlier in `SubscriptionService.update()`:

```java
if (req.getDebitAmount() != null) existing.setDebitAmount(req.getDebitAmount());
if (req.getDebitDay()    != null) existing.setDebitDay(req.getDebitDay());
```

Both approaches do the same thing — only overwrite what was explicitly provided. The builder version combines everything into one construction step; the setter version mutates an existing object field by field. Neither is wrong; the builder is generally preferred for new objects, setters for patching existing ones.

---

## 39. Build tools: Maven vs Gradle

A **build tool** automates the repetitive tasks every Java project needs:
- Downloading dependencies (libraries your code uses)
- Compiling your source files
- Running tests
- Packaging everything into a runnable `.jar` file

Without a build tool, you would have to do all of that by hand for every change. Both Maven and Gradle solve this problem — they just approach it differently.

### Maven (what this project started with)

Maven uses an XML file called `pom.xml` ("Project Object Model"). It looks like this:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

Maven has a strict, rigid lifecycle: `validate → compile → test → package → install → deploy`. Every project follows the same steps in the same order. This predictability is Maven's biggest strength — experienced Java developers know exactly how any Maven project builds.

The downside: it is verbose, XML is unpleasant to write, and you cannot easily customise the build without writing plugins.

### Gradle (what this project now uses)

Gradle uses either a Groovy DSL or a **Kotlin DSL** (`build.gradle.kts`). Kotlin is a modern language that your IDE understands — you get autocomplete, type checking, and error highlighting:

```kotlin
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
}
```

Gradle's three core advantages over Maven:

**1. Incremental builds** — Gradle tracks which files changed. If you change one file, it only recompiles files that depend on it. Maven recompiles everything. On large projects, this can mean seconds vs minutes.

**2. Build cache** — Gradle can cache build outputs and reuse them even across machines (with a remote cache). If your CI server already built a module, developers can download the cached output instead of rebuilding from scratch.

**3. Flexibility** — Gradle's build scripts are real code. You can write loops, conditions, and functions. Maven forces you to use predefined lifecycle phases and XML plugins for everything custom.

### The Gradle wrapper

When you run `./gradlew build`, you are not running Gradle directly — you are running the **Gradle wrapper**. The wrapper is three files committed to the repository:

```
gradlew                          ← shell script (Unix)
gradlew.bat                      ← batch script (Windows)
gradle/wrapper/gradle-wrapper.jar        ← small bootstrap JAR
gradle/wrapper/gradle-wrapper.properties ← points to the Gradle version to download
```

The first time anyone runs `./gradlew`, it reads the `.properties` file, downloads exactly the right version of Gradle, caches it in `~/.gradle/wrapper/dists/`, and uses it. This means **every developer and every CI server automatically uses the same Gradle version** without needing to install anything.

This is the right approach. Never assume Gradle is installed globally.

### Key Gradle commands

```bash
./gradlew build            # compile + test + package
./gradlew bootRun          # start the Spring Boot app
./gradlew test             # run tests only
./gradlew dependencies     # print the full dependency tree
./gradlew clean            # delete build output
./gradlew tasks            # list all available tasks
```

### What `build.gradle.kts` means

The `.kts` extension means **Kotlin Script**. It is Kotlin, but executed as a script by Gradle rather than compiled into a standalone program. The standard extension without `.kts` uses Groovy. Kotlin is preferred in new projects because it has better IDE support.

---

## 40. Project folder structure explained

When you open this project, the folders can look overwhelming. Here is what each one is for and why it is organised that way.

### The standard Maven/Gradle Java layout

```
project-root/
├── build.gradle.kts         ← Gradle build config (dependencies, plugins, Java version)
├── settings.gradle.kts      ← Gradle project name
├── gradlew / gradlew.bat    ← Gradle wrapper scripts
├── gradle/wrapper/          ← Gradle wrapper JAR and properties
├── src/
│   ├── main/
│   │   ├── java/            ← All your application source code
│   │   └── resources/       ← Config files (application.properties, etc.)
│   └── test/
│       ├── java/            ← Test source code
│       └── resources/       ← Test config
└── build/                   ← Generated output (compiled classes, .jar). Never commit this.
```

This layout is a **convention**. Maven and Gradle both expect `src/main/java` for source and `src/main/resources` for config. If you follow the convention, the tools need zero extra configuration to find your files.

### Inside `src/main/java/com/itc/direct_debit_sandbox/`

The package path (`com.itc.direct_debit_sandbox`) mirrors the directory path. Inside that root, the code is split by **feature**:

```
direct_debit_sandbox/
├── callbacks/           ← CallbackService: fires HTTP callbacks to merchant webhooks
├── config/              ← App-wide config: thread pool, RestTemplate, SandboxConfig
├── provision/           ← /provision endpoint: merchant registration
├── scenarios/           ← ScenarioEngine: maps account suffixes to outcomes
├── store/               ← In-memory data: all the records and the store interface
├── subscriptions/       ← /subscription/* endpoints: the core domain
└── transactions/        ← /transaction/* endpoints: status checks
```

This is called **feature-based packaging** (also called "package by feature"). The alternative is **layer-based packaging**:

```
controllers/   ← all controllers in one folder
services/      ← all services in one folder
repositories/  ← all repositories in one folder
```

Layer-based is what most tutorials show. Feature-based is what experienced engineers prefer. Here is why:

| Scenario | Layer-based | Feature-based |
|----------|-------------|---------------|
| You want to understand how subscriptions work | Jump between 3 folders | Everything is in `subscriptions/` |
| You want to add a new endpoint | Touch 3 separate folders | Add to one folder |
| A junior asks "where does this all live?" | "spread across the whole project" | "it's all in `subscriptions/`" |

The rule: **things that change together should live together**. `SubscriptionController`, `SubscriptionService`, `SubscriptionRecord`, and the DTOs all change when subscription behaviour changes — so they belong in one folder.

### The `store/` folder as a special case

`store/` is shared infrastructure, not a feature. It holds:
- `Store.java` — the interface (the contract)
- `InMemoryStore.java` — the implementation
- `SubscriptionRecord.java`, `TransactionRecord.java`, etc. — data models

In a real project with a database, `store/` would contain JPA repositories and entities. The feature packages (`subscriptions/`, `transactions/`) would call those repositories. The separation keeps features independent of each other — `subscriptions/` does not import from `transactions/` and vice versa.

### What to put in `resources/`

`src/main/resources/application.properties` is where Spring Boot looks for configuration:

```properties
sandbox.callback-delay-preapproval=2000
sandbox.callback-delay-transaction=7000
```

You can also have `application-dev.properties`, `application-prod.properties` for environment-specific overrides. Spring loads the right one based on the active profile (`spring.profiles.active=dev`).

Never hardcode values like delay times or URLs directly in Java code. Put them in `application.properties` so they can be changed without recompiling.


---

## 41. Gradle and Java Version Compatibility

Even though Java is designed to be backwards compatible, Gradle actually hooks deeply into Java's internal APIs to compile code, manage its daemon process, and read bytecode. 

Whenever a new major version of Java is released (like Java 25), it brings changes to the bytecode format and internal APIs. Because of this, **every specific version of Gradle has a maximum supported Java version**. 

If your project's Gradle wrapper (e.g., `8.14`) was released before a new Java version (like Java 25) was fully supported, starting the Gradle daemon with that newer Java version will cause an immediate crash (often showing a cryptic version string error like `25.0.2`). 

To fix this, you either:
1. Upgrade the Gradle wrapper to a newer version that supports the Java version you have installed (`./gradlew wrapper --gradle-version <newer-version>`).
2. Force Gradle to use an older, compatible Java version (like the Long-Term Support Java 21) by setting `org.gradle.java.home=/path/to/java-21` in your `gradle.properties`.

---

## 42. Where do installed packages live?

In Maven, your dependencies are listed in `pom.xml`. In Gradle, they live in the **`build.gradle.kts`** file inside the `dependencies { ... }` block.
It looks like this:
```kotlin
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
}
```
Whenever you add a new line here, Gradle automatically downloads the package from Maven Central and makes it available to your code.

---

## 43. What to commit to Git (and what to ignore)

When working with Java build tools, a lot of temporary files are generated locally. You must **never commit these to Git** because they bloat the repository and cause conflicts.

**Files you SHOULD commit:**
- `build.gradle.kts` and `settings.gradle.kts` (your build instructions)
- `gradle.properties` (local project configuration)
- `gradlew` and `gradlew.bat` (wrapper scripts so others can run gradle without installing it)
- `gradle/wrapper/gradle-wrapper.jar` and `gradle-wrapper.properties`
- `src/` (all your actual code)

**Files you MUST IGNORE (add to `.gitignore`):**
- `.gradle/` — This is a hidden folder where Gradle caches downloaded dependencies, build outputs, and daemon state. It is massive and specific to your computer.
- `build/` — The compiled output (e.g., your `.class` and `.jar` files). Git is for source code, not compiled output.
- `.mvn/` and `target/` — If you switched from Maven to Gradle, these are leftovers.

If you accidentally commit `.gradle/`, you can un-track it without deleting it from your computer by running:
`git rm -r --cached .gradle`

---

## 44. The hidden danger of Spring Security dependency

Spring Boot embraces "convention over configuration," which means it automatically configures things based on what dependencies are on your classpath.

If you add:
```kotlin
implementation("org.springframework.boot:spring-boot-starter-security")
```
Spring Boot assumes you want a secure application. Without any extra code, it instantly locks down **every single endpoint** behind HTTP Basic Authentication. It even generates a random password and prints it to the console when you start the app: `Using generated security password: d9f15...`

If your API uses its own custom authentication (like checking `x-transflowId` and `x-key` headers), you don't need this dependency. Simply removing it from `build.gradle.kts` disables the automatic Basic Auth lockdown.

---

## 45. Secondary indexes — fast lookups at scale

When you store records in a `Map<String, SubscriptionRecord>` keyed by `subscriptionId`, looking up a record by its ID is **O(1)** — the map jumps straight to it. But what if you need to find a record by a *different* field, like `referenceNo`?

Without an index, you scan every value:

```java
subscriptions.values().stream()
    .filter(s -> s.getReferenceNo().equals(ref))
    .findFirst();
```

This is **O(n)** — it reads every record in the map until it finds a match. With 10 records it doesn't matter. With 1,000,000 records, that is up to one million reads per lookup.

The solution is a **secondary index**: a second map maintained alongside the primary one. Its key is the field you want to search by; its value is the primary key.

```java
// Primary map:      subscriptionId → SubscriptionRecord
Map<String, SubscriptionRecord> subscriptions = new ConcurrentHashMap<>();

// Secondary index:  referenceNo → subscriptionId
Map<String, String> referenceIndex = new ConcurrentHashMap<>();
```

Now a lookup by reference is a **two-hop O(1)**:

```java
String id     = referenceIndex.get(referenceNo);  // hop 1: ref → id
return subscriptions.get(id);                      // hop 2: id → record
```

Two instant hash lookups instead of scanning millions of records.

**The index must stay in sync.** Every time you create a record, add its entry to the index. Every time you delete a record, remove its entry from the index. If the indexes drift out of sync, lookups return wrong or null results.

This is the same concept databases use internally. A database index is a separate data structure (usually a B-tree) that maps a column value to the row ID — enabling fast lookups without a full table scan. Here we do the same thing manually with Java maps.

**Cost:** Each index is an extra map in memory, and every write must update all indexes. For a sandbox this is trivial. For very large datasets it is a deliberate tradeoff: more memory and more write work in exchange for faster reads. In systems that read far more than they write — like a payment sandbox with millions of lookups per subscription — it is always worth it.

---

## 46. Separating create from update in the store

The original `InMemoryStore` had a single `saveSubscription(id, record)` method used for both creating a new subscription and overwriting an existing one after an update.

This became a problem once secondary indexes were added: on **create**, the indexes must be populated. On **update**, they must NOT be touched — the fields the indexes track (`referenceNo`, `debitAccount`, `productId`) never change on update, so re-adding them is harmless, but if they ever *could* change it would create stale entries.

The solution is two explicit methods:

```java
// Creates a new record — populates primary map AND both indexes
void createSubscription(String id, SubscriptionRecord record);

// Updates an existing record — only touches the primary map
void updateSubscription(String id, SubscriptionRecord record);
```

Each method's name now tells the caller exactly what side effects it has. The caller no longer needs to decide whether to "save" — `save` is a meaningless word that describes the *how*. `create` and `update` describe the *why*.

This is a broader principle: **method names should reflect intent, not implementation**. A reader seeing `store.createSubscription(...)` immediately knows a new record is being inserted and indexes are being populated. Seeing `store.updateSubscription(...)` they know only data fields are changing and no index work is needed.

This also makes the code safer: if someone accidentally calls `createSubscription` on an already-existing ID, it would double-write the indexes (harmless here since it is the same key, but clearly wrong behaviour). With two methods, the distinction is encoded in the name and the code is self-documenting.

---

## 47. Data normalization — keeping your data lean

**Normalization** is the process of structuring your data so that every piece of information is stored in exactly one place. Your supervisor's description — "getting your data as lean as possible" — captures the goal: no redundancy, no duplication, no field that says the same thing twice.

The motivation is practical. If the same value (say, a merchant's callback URL) is stored in a hundred different records and the merchant changes it, you have to update a hundred records. If you stored it once, you change it once. Normalization eliminates that class of bug.

---

### The three normal forms

Normalization is usually described in levels called **normal forms**. Each one builds on the previous.

#### First Normal Form (1NF) — atomic values

Every field must hold a single, indivisible value. No field should contain a list or a comma-separated bundle of things.

**Violation:**
```
| subscriptionId | debitAccounts              |
|----------------|----------------------------|
| SUB001         | 0241234567, 0209876543     |  ← two values in one field
```

**1NF:**
```
| subscriptionId | debitAccount  |
|----------------|---------------|
| SUB001         | 0241234567    |
| SUB001         | 0209876543    |  ← separate rows, one value each
```

In this project every field in `SubscriptionRecord` holds one value — `debitAccount` is a single String, not a comma-separated list. That is 1NF.

---

#### Second Normal Form (2NF) — no partial dependencies

Every non-key field must depend on the **entire** primary key, not just part of it.

This only matters when your primary key is a composite key (two or more fields together). If your key is just one field, you are automatically 2NF once you are 1NF.

**Example violation** (hypothetical — imagine a transaction table keyed by `subscriptionId + date`):
```
| subscriptionId | date       | debitAmount | merchantName  |
|----------------|------------|-------------|---------------|
| SUB001         | 2026-05-01 | 50.00       | Acme Corp     |
```

`merchantName` depends only on `subscriptionId`, not on the combination of `subscriptionId + date`. That is a partial dependency — a 2NF violation. If the merchant changes their name, you have to update every row for every transaction date, not just one place.

**Fix:** move `merchantName` to a separate merchants table keyed by `subscriptionId` (or better, `merchantId`).

---

#### Third Normal Form (3NF) — no transitive dependencies

Every non-key field must depend **directly** on the primary key, not on another non-key field.

**Example violation:**
```
| subscriptionId | merchantId | callbackUrl              |
|----------------|------------|--------------------------|
| SUB001         | MERCH_123  | https://acme.com/webhook |
| SUB002         | MERCH_123  | https://acme.com/webhook |  ← repeated
| SUB003         | MERCH_456  | https://other.com/hook   |
```

Here `callbackUrl` depends on `merchantId`, not on `subscriptionId`. The dependency chain is:

```
subscriptionId → merchantId → callbackUrl
```

`callbackUrl` reaches the primary key only *through* `merchantId`. That is a transitive dependency — a 3NF violation. If Acme changes their URL, every one of their subscriptions must be updated.

**Fix:** move `callbackUrl` to a separate table keyed by `merchantId` (or `merchantId + productId`).

---

### How this project applies normalization

#### The provision pattern is a textbook 3NF fix

`SubscriptionRecord` originally carried a `callbackUrl` field. But that URL belongs to the merchant + product combination, not to any individual subscription. The same URL would be copied across every subscription that merchant registered — a transitive dependency.

The **provision pattern** is the normalized solution:

```java
// callbackUrl lives here — one record per merchant+product
ProvisionRecord {
    merchantId
    productId
    callbackUrl        ← stored once
    retryAttempts
    skipFactor
}

// SubscriptionRecord no longer needs to own the URL
SubscriptionRecord {
    id (subscriptionId)
    merchantId         ← points back to the provision record
    productId          ← together, these are the "foreign key"
    debitAccount
    debitAmount
    ...
}
```

When `CallbackService` needs the URL, it resolves it via `store.getProvision(merchantId, productId)` — one lookup, one source of truth. If the merchant changes their URL, they call `POST /provision` once. All future callbacks use the new URL automatically, for all their subscriptions, with no data migration.

This is 3NF applied directly: the field that was transitively dependent (`callbackUrl → merchantId`) has been moved to the table it actually belongs to.

---

### Where this project intentionally breaks normalization

Look at `TransactionRecord`:

```java
public class TransactionRecord {
    private String subscriptionId;  // ← link back to the subscription
    private String merchantId;      // ← also in SubscriptionRecord
    private String productId;       // ← also in SubscriptionRecord
    private String debitAccount;    // ← also in SubscriptionRecord
    private String debitAmount;     // ← also in SubscriptionRecord
    private String channel;         // ← also in SubscriptionRecord
    ...
}
```

All those fields already exist in the linked `SubscriptionRecord`. A fully normalised design would remove them from `TransactionRecord` and look them up via `subscriptionId` whenever needed. So why are they duplicated here?

This is a deliberate **denormalization** — a conscious choice to break the rule for a performance reason.

In a real system, transaction records are read far more often than they are written. Every time someone queries a transaction, they want to see the account number, the merchant, the amount — everything in one place. If that data lived only in the subscription record, every transaction query would require a second lookup. Under high read volume, that cost adds up.

By copying the fields into `TransactionRecord` at write time (when the transaction is created), reads become a single map lookup instead of two. The tradeoff is that if a subscription's fields ever change (the account is updated, for example), the already-recorded transactions will show the old values — but for a financial audit trail, that is actually correct behaviour. You want the transaction to record what the account was *at the time*, not what it is now.

---

### Summary: normalization tradeoffs at a glance

| Approach | Storage | Write complexity | Read complexity | Risk |
|----------|---------|------------------|-----------------|------|
| Fully normalised | Lean, no duplication | Simple | Requires joins/lookups | Stale reads impossible — one source of truth |
| Denormalized | Some duplication | Must update all copies | Single lookup, fast | Copies can drift out of sync |

The right answer is almost always: **normalize by default**, then **denormalize deliberately** where read performance justifies it — and document why.

---

## Things this project taught you

**Java fundamentals**
- Static typing forces you to be explicit — the compiler catches whole classes of bugs before the program runs
- `null` is the most dangerous value in Java; always check before using an object
- `Boolean` (object) vs `boolean` (primitive) — use `Boolean.TRUE.equals()` to safely handle nullable booleans
- Interfaces enforce completeness — if you add a method to an interface, every implementing class must implement it or the build fails

**Spring Boot**
- How Spring Boot routes HTTP requests to controller methods via `@RequestMapping` and `@PostMapping`
- How Java's annotation system works as a configuration mechanism — annotations are instructions to the framework, not the compiler
- `@RestController` automatically serialises return values to JSON; without it you get HTML
- `ResponseEntity` gives you control over HTTP status codes, not just the response body
- `required = false` on `@RequestHeader` lets you control your own error responses instead of Spring's generic 400
- The hidden danger of Spring Security: adding the dependency automatically locks down all endpoints with Basic Auth

**Architecture**
- Why layering (controller → service → store) makes code maintainable — each layer has one job
- Why DTOs exist — they protect internal models from external API changes and keep validation separate from storage
- The provision pattern: register shared configuration once at onboarding rather than repeating it on every request
- The fallback resolution pattern: try a primary source, fall back to a secondary source — useful for cache/database, provision/request, env/default

**Data and storage**
- `ConcurrentHashMap` vs `HashMap` — thread safety matters the moment you add async operations
- Composite map keys (`"merchantId:productId"`) — how to store and retrieve by multiple fields using a simple string map
- The partial update / merge pattern — only overwrite fields the caller explicitly provided
- Secondary indexes: a second map keyed by the lookup field points to the primary key, turning O(n) full-map scans into O(1) two-hop lookups — but the indexes must be kept in sync on every write and delete
- Separating `createSubscription` from `updateSubscription`: method names should reflect intent (what the caller is trying to do), not implementation (what the method does internally)
- Data normalization: store each fact in exactly one place — 1NF (atomic values), 2NF (no partial dependencies), 3NF (no transitive dependencies); the provision pattern is a real application of 3NF
- Deliberate denormalization: copying fields into `TransactionRecord` at write time trades storage for read speed and preserves a point-in-time audit trail — acceptable when the reason is explicit

**Tools and testing**
- How Lombok eliminates boilerplate (`@Data`, `@Builder`, `@Slf4j`, `@RequiredArgsConstructor`) while keeping code readable
- How the Builder pattern prevents bugs from argument ordering mistakes — named fields are unambiguous
- The `.http` file format — test requests that live inside the repository alongside the code
- The Scenario Engine pattern — deterministic test outcomes via account number suffixes, no real third-party required

**Build tools and project structure**
- Gradle vs Maven: Gradle is faster (incremental builds, build cache), more flexible, and uses Kotlin DSL with IDE autocomplete
- The Gradle wrapper (`./gradlew`) pins a specific Gradle version so every developer and CI server builds identically — no global install needed
- Feature-based packaging (`subscriptions/`, `transactions/`) keeps related code together; layer-based packaging (`controllers/`, `services/`) splits it apart
- `application.properties` is for config that changes per environment — never hardcode delays, URLs, or secrets in Java source files
- Gradle heavily depends on internal Java APIs, meaning every Gradle version has a maximum supported Java version. Using an unsupported Java version causes immediate crashes.
- Dependencies live in the `build.gradle.kts` `dependencies {}` block.
- Never commit `.gradle/` or `build/` to Git, but do commit your Gradle wrapper scripts and `.properties` so others can build easily.

**Async and callbacks**
- Why `@Async` only works when called from **outside** the class — Spring uses a proxy, and internal calls bypass it
- How thread pools work — core size, max size, queue capacity
- Why async matters for API responsiveness — return immediately, process in the background
- How to simulate realistic API behaviour (delays, staged callbacks) without a real payment network

---

*This guide reflects the state of the project as of May 2026.*
