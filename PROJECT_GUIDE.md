# Blood Bank Management System — Complete Project Guide

> A console-based Java application built for an Object-Oriented Programming academic project.  
> This guide explains every concept used, exactly where it is implemented in the code, and **why** that concept was the right tool for the job.

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Package Structure](#2-package-structure)
3. [Application Workflow](#3-application-workflow)
4. [OOP Concepts In Detail](#4-oop-concepts-in-detail)
   - 4.1 Classes & Objects
   - 4.2 Constructors & Constructor Overloading
   - 4.3 Encapsulation
   - 4.4 Abstract Classes
   - 4.5 Inheritance
   - 4.6 Polymorphism
   - 4.7 Interfaces
   - 4.8 Arrays of Objects
   - 4.9 Enums
   - 4.10 Custom Exception Handling
   - 4.11 Multithreading
   - 4.12 File Handling & Persistence
   - 4.13 Java Date & Time API
5. [File-by-File Code Explanation](#5-file-by-file-code-explanation)
6. [Data Flow Diagram](#6-data-flow-diagram)
7. [How to Run](#7-how-to-run)

---

## 1. Project Overview

The **Blood Bank Management System** simulates the real-world operations of a blood bank. It manages:

| Actor | Capabilities |
|---|---|
| **Admin** | Register donors, record donations, screen donations, approve hospitals, manage blood requests, issue blood units, view reports |
| **Hospital** | Register, get approved, submit blood requests, view their own hospital inventory and issue records |

All data is **persisted** to plain-text files in the `data/` directory so records survive application restarts. The system is built entirely with **core Java** — no frameworks or external libraries.

---

## 2. Package Structure

```
src/bloodbank/
│
├── Main.java                         ← Entry point, console UI
│
├── model/                            ← Domain entities (the "things" in the system)
│   ├── User.java                     ← Abstract base class for all users
│   ├── Admin.java                    ← Inherits User; represents admin staff
│   ├── HospitalUser.java             ← Inherits User; represents a hospital
│   ├── Donor.java                    ← Blood donor record
│   ├── Donation.java                 ← A single donation event
│   ├── BloodUnit.java                ← A single bag/unit of blood in inventory
│   ├── BloodIssue.java               ← Audit record: blood issued to a hospital
│   ├── BloodRequest.java             ← Hospital's request for blood
│   ├── BloodGroup.java               ← Enum: A+, A-, B+, B-, AB+, AB-, O+, O-
│   ├── BloodUnitStatus.java          ← Enum: AVAILABLE, ISSUED, EXPIRED, RESERVED
│   ├── DonationStatus.java           ← Enum: PENDING, ACCEPTED, REJECTED
│   ├── HospitalStatus.java           ← Enum: PENDING, APPROVED, REJECTED
│   ├── RequestPriority.java          ← Enum: NORMAL, EMERGENCY
│   └── RequestStatus.java            ← Enum: PENDING, APPROVED, REJECTED, FULFILLED, etc.
│
├── service/
│   └── BloodBankSystem.java          ← Core business logic engine
│
├── persistence/
│   ├── FileManager.java              ← File read/write, Base64 encoding
│   └── FileRecord.java               ← Interface: contract for file-serializable objects
│
├── thread/
│   ├── ExpirySweeper.java            ← Background daemon thread for expired unit cleanup
│   └── LowStockMonitor.java          ← Background monitor thread (Runnable)
│
├── exception/
│   ├── InsufficientStockException.java   ← Thrown when not enough blood to issue
│   ├── InvalidOperationException.java    ← Thrown for invalid state transitions
│   └── DonorIneligibleException.java     ← Thrown when donor donates too early
│
└── util/
    └── IdGenerator.java              ← Thread-safe sequential ID generator
```

---

## 3. Application Workflow

```
                    ┌─────────────────────────────────────┐
                    │      BLOOD BANK MANAGEMENT SYSTEM   │
                    └──────────────┬──────────────────────┘
                                   │
              ┌────────────────────┴─────────────────────┐
              │                                           │
         1. Hospital                                  2. Admin
              │                                           │
    ┌─────────┴──────────┐                    ┌───────────┴──────────────────────────┐
    Register  │  Login   │                    Login (admin / admin123)                │
              │          │                               │                            │
         Admin must   Hospital                ┌──────────┼─────────────┐             │
         approve      Dashboard               Donors    Blood       Hospitals         │
         first           │                   & Donations Inventory   & Approval      │
                   ┌─────┴──────┐
                   View My   Request
                 Inventory    Blood
                              │
                         Admin reviews →
                         Approves →
                         Issues Units →
                         Hospital Inventory Updated
```

**Step-by-step:**
1. A hospital registers and waits for admin approval.
2. Admin logs in and approves the hospital.
3. Admin registers a donor, records a donation, and accepts it after "screening" — this creates individual `BloodUnit` objects in inventory.
4. The approved hospital logs in and requests blood of a specific group and quantity.
5. Admin reviews the request, approves it, then issues units — linking specific blood bag IDs to the hospital.
6. The hospital's own inventory (Option 2) now shows those received units with expiry dates.
7. All changes are saved to files immediately and loaded at the next startup.

---

## 4. OOP Concepts In Detail

---

### 4.1 Classes & Objects

**What it is:**  
A **class** is a blueprint that defines what data (fields) and behaviour (methods) a type has. An **object** is a real live instance created from that blueprint using `new`.

**Where it is used:**  
Every file in `model/` defines a class. Every time data is created (e.g., a hospital registers, a donation is recorded), the system creates a new object.

**Example — Creating a Donor object:**
```java
// In BloodBankSystem.java → addDonor()
Donor d = new Donor(donorIds.next(), name, age, phone, group);
```

**Why:**  
Each donor, donation, blood unit, hospital, and request is a distinct real-world entity that needs its own state (fields) and behaviour (methods). Classes model these entities cleanly. Without classes, we would need parallel arrays of unrelated data — messy and error-prone.

---

### 4.2 Constructors & Constructor Overloading

**What it is:**  
A **constructor** is a special method that runs when an object is created using `new`. **Overloading** means having multiple constructors with different parameters — Java picks the right one based on the arguments passed.

**Where it is used:**

**`User.java` — Two constructors (overloading):**
```java
// Constructor 1 — full details
public User(String id, String name, String username, String password) {
    this.id = req(id, "ID");
    this.name = req(name, "Name");
    this.username = req(username, "Username");
    this.password = req(password, "Password");
}

// Constructor 2 — no password supplied (defaults to "change-me")
public User(String id, String name, String username) {
    this(id, name, username, "change-me");  // delegates to Constructor 1
}
```

**`Donor.java` — Constructor with validation:**
```java
public Donor(String id, String name, int age, String phone, BloodGroup bloodGroup) {
    if (age < 18 || age > 100) {
        throw new IllegalArgumentException("Donor age must be between 18 and 100");
    }
    ...
}
```

**`DonorIneligibleException.java` — Overloaded exception constructors:**
```java
// Minimal — just a message
public DonorIneligibleException(String message) { super(message); ... }

// Full — carries structured deferral data
public DonorIneligibleException(String message, String donorId,
        LocalDate lastDonationDate, LocalDate nextEligibleDate, long daysRemaining) {
    super(message);
    this.donorId = donorId;
    ...
}
```

**Why:**  
Constructor overloading gives flexibility. A `User` can be created with or without a password depending on context. Validation in constructors ensures **no object can exist in an invalid state** — you can never accidentally create a Donor who is 15 years old.

---

### 4.3 Encapsulation

**What it is:**  
Hiding the internal details of a class (making fields `private`) and exposing them only through controlled **getters** and **setters**. This protects data integrity.

**Where it is used:**  
Every model class. All fields are `private` or `private final`. Data is only accessible through methods.

**Example — `HospitalUser.java`:**
```java
public class HospitalUser extends User {
    private final String contactPerson;   // immutable after creation
    private final String phone;
    private final String address;
    private HospitalStatus status;        // mutable, but only via setStatus()

    public HospitalStatus getStatus() { return status; }

    public void setStatus(HospitalStatus s) {
        if (s == null) throw new IllegalArgumentException("Status required");
        status = s;    // validated gate — null can never sneak in
    }
}
```

**Example — `BloodRequest.java` addIssuedUnits():**
```java
public void addIssuedUnits(int n) {
    if (n < 1 || n > getRemainingUnits()) {
        throw new IllegalArgumentException("Invalid issue quantity");
    }
    issuedUnits += n;
    // Status auto-updates here — logic encapsulated, not scattered in menus
    status = issuedUnits == units ? RequestStatus.FULFILLED : RequestStatus.PARTIALLY_FULFILLED;
}
```

**Why:**  
Without encapsulation, any part of the program could set `status = null` or give a negative age. Every mutation goes through a **controlled gate** that validates the change first. This is especially important in a multi-threaded application where multiple threads could try to modify the same object.

---

### 4.4 Abstract Classes

**What it is:**  
A class marked `abstract` cannot be instantiated directly. It provides shared implementation to subclasses, plus defines **abstract methods** that every subclass *must* override.

**Where it is used:**

**`User.java`:**
```java
public abstract class User {
    // Shared fields — all users have these
    private final String id;
    private final String name;
    private final String username;
    private final String password;

    // Concrete shared method — uses getRole() which subclasses define
    public String displayName() {
        return getRole() + ": " + name;   // polymorphic call
    }

    // Abstract method — subclasses MUST implement this
    public abstract String getRole();
}
```

You **cannot** write `new User(...)` — the compiler prevents it. You must use `new Admin(...)` or `new HospitalUser(...)`.

**Why:**  
`User` captures everything common to Admin and Hospital (id, name, username, password, displayName). But `getRole()` is inherently different for each persona. Marking it `abstract` forces every subclass to define it — the compiler catches any subclass that forgets. This prevents bugs from half-implemented subclasses.

---

### 4.5 Inheritance

**What it is:**  
A class can **extend** another class, inheriting all its fields and methods, then add or override behaviour. The extending class is the **subclass**; the one being extended is the **superclass**.

**Where it is used:**

**`Admin.java` extends `User`:**
```java
public class Admin extends User {
    public Admin(String id, String name, String username, String password) {
        super(id, name, username, password);   // calls User's constructor
    }

    @Override
    public String getRole() { return "ADMIN"; }
}
```

**`HospitalUser.java` extends `User`:**
```java
public class HospitalUser extends User {
    private final String contactPerson;    // hospital-specific extra fields
    private final String phone;
    private final String address;
    private HospitalStatus status;

    public HospitalUser(...) {
        super(id, name, username, password);   // reuses User's constructor
        this.contactPerson = contactPerson;
        ...
    }

    @Override
    public String getRole() { return "HOSPITAL"; }
}
```

**Inheritance Tree:**
```
        User  (abstract)
       /      \
   Admin    HospitalUser
```

**Why:**  
Without inheritance, both `Admin` and `HospitalUser` would duplicate the id, name, username, and password fields along with their constructors and getters — violating the **DRY (Don't Repeat Yourself)** principle. Inheritance puts shared code in one place. If you need to add an `email` field to all users, you only add it in `User.java` and both subclasses automatically get it.

---

### 4.6 Polymorphism

**What it is:**  
The ability to treat objects of different subclasses through a common superclass reference. The correct version of an overridden method is resolved **at runtime** based on the actual object type.

**Where it is used:**

**`Main.java` — `displayName()` called on both Admin and HospitalUser:**
```java
// admin is declared as Admin, but displayName() is defined in User
Admin admin = new Admin("ADMIN-1", "System Administrator", u, p);
System.out.println("Welcome, " + admin.displayName());
// Output: "Welcome, ADMIN: System Administrator"

// h is of type HospitalUser
System.out.println(h.displayName());
// Output: "Welcome, HOSPITAL: Balaji Hospital"
```

Both use `User.displayName()`:
```java
public String displayName() {
    return getRole() + ": " + name;    // getRole() is resolved at runtime
}
```

For `Admin` → `getRole()` returns `"ADMIN"`.  
For `HospitalUser` → `getRole()` returns `"HOSPITAL"`.  
**Same method call, different result** — that is polymorphism.

**Why:**  
Polymorphism lets you write generic code that works on `User` references without knowing the concrete type. If a third user type (`DonorUser`) were added in the future, `displayName()` would work for it automatically, with zero changes to existing code.

---

### 4.7 Interfaces

**What it is:**  
An **interface** is a pure contract — it declares method signatures but provides no implementation. Any class that `implements` an interface must provide an implementation for every method declared in it.

**Where it is used:**

**`FileRecord.java` — serialization contract:**
```java
public interface FileRecord {
    String toFileLine();   // Any implementing class must be able to produce a file line
}
```

**`BloodUnit.java` implements `FileRecord`:**
```java
public class BloodUnit implements FileRecord {
    @Override
    public String toFileLine() {
        return id + "|" + donationId + "|" + group.name() + "|"
             + collected + "|" + expiry + "|" + status.name();
    }
}
```

**`LowStockMonitor.java` implements `Runnable`:**
```java
public class LowStockMonitor implements Runnable {
    @Override
    public void run() {
        // Background monitoring logic
    }
}
```
`Runnable` is a built-in Java interface. Implementing it makes `LowStockMonitor` a task that can be handed off to a `Thread`.

**Why:**  
Interfaces define a **"can-do"** relationship. `BloodUnit` implementing `FileRecord` declares: "I can serialize myself to a file line." `LowStockMonitor` implementing `Runnable` declares: "I can be run as a background thread." Interfaces enforce a contract without dictating how a class does its job — they are the foundation of flexible, extensible design.

---

### 4.8 Arrays of Objects

**What it is:**  
Java allows arrays where every element is an object. This is how all records are stored in-memory — as fixed-size arrays of model objects.

**Where it is used:**

**`BloodBankSystem.java`:**
```java
private final HospitalUser[] hospitals  = new HospitalUser[50];
private final Donor[]         donors    = new Donor[100];
private final Donation[]      donations = new Donation[200];
private final BloodUnit[]     units     = new BloodUnit[300];
private final BloodRequest[]  requests  = new BloodRequest[100];
private final BloodIssue[]    issues    = new BloodIssue[300];

private int hc;   // hospital count (how many slots used)
private int dc;   // donor count
```

Adding a new object:
```java
hospitals[hc++] = new HospitalUser(...);
```

Searching:
```java
for (int i = 0; i < hc; i++) {
    if (hospitals[i].getId().equalsIgnoreCase(id)) {
        return hospitals[i];
    }
}
```

**Why:**  
The project deliberately uses arrays instead of `ArrayList` to demonstrate the OOP concept of **arrays of objects** required by the academic syllabus. Each slot holds a full, live object with its own state — not a primitive. The counter variable (`hc`, `dc`, etc.) tracks how many slots are actually filled, and searching iterates only up to the count — clean and efficient.

---

### 4.9 Enums

**What it is:**  
An **enum** (enumeration) is a type with a fixed set of named constants. In Java, enums are full classes — they can have methods and fields.

**Where it is used:**

**`BloodGroup.java` — with helper methods:**
```java
public enum BloodGroup {
    A_POS, A_NEG, B_POS, B_NEG, AB_POS, AB_NEG, O_POS, O_NEG;

    // Converts a menu number (1-8) to the corresponding enum constant
    public static BloodGroup fromNumber(int n) {
        if (n < 1 || n > values().length)
            throw new IllegalArgumentException("Choose 1-8");
        return values()[n - 1];
    }

    // Returns a human-readable label like "A+" or "O-"
    public String label() {
        return name().replace("_POS", "+").replace("_NEG", "-");
    }
}
```

**Other enums in the project:**
| Enum | Values |
|---|---|
| `BloodUnitStatus` | `AVAILABLE, RESERVED, ISSUED, EXPIRED` |
| `DonationStatus` | `PENDING, ACCEPTED, REJECTED` |
| `HospitalStatus` | `PENDING, APPROVED, REJECTED` |
| `RequestPriority` | `NORMAL, EMERGENCY` |
| `RequestStatus` | `PENDING, APPROVED, REJECTED, FULFILLED, PARTIALLY_FULFILLED, CANCELLED` |

**Why:**  
Without enums, statuses would be plain `String` values — a typo like `"PENDNG"` is valid Java but logically broken and undetectable at compile time. Enums restrict a field to only its valid values. The compiler immediately catches `hospital.setStatus("APPROVD")` as an error. Enums also make `switch` statements exhaustive and self-documenting.

---

### 4.10 Custom Exception Handling

**What it is:**  
Java allows creating domain-specific exception classes. **Checked exceptions** (extending `Exception`) force the caller to explicitly handle or declare them — the compiler enforces this, unlike runtime exceptions.

**Where it is used:**

**`InsufficientStockException.java`:**
```java
public class InsufficientStockException extends Exception {
    public InsufficientStockException(String message) {
        super(message);
    }
}
```
Thrown in `BloodBankSystem.issueBlood()` when there are not enough available blood units.

**`InvalidOperationException.java`:**
```java
public class InvalidOperationException extends Exception { ... }
```
Thrown when an operation makes no logical sense — e.g., re-screening an already-processed donation, or reviewing a request that is not in PENDING state.

**`DonorIneligibleException.java` — richest exception, carries structured data:**
```java
public class DonorIneligibleException extends Exception {
    private final String donorId;
    private final LocalDate lastDonationDate;
    private final LocalDate nextEligibleDate;
    private final long daysRemaining;

    public DonorIneligibleException(String message, String donorId,
            LocalDate lastDonationDate, LocalDate nextEligibleDate, long daysRemaining) {
        super(message);
        this.donorId = donorId;
        this.lastDonationDate = lastDonationDate;
        this.nextEligibleDate = nextEligibleDate;
        this.daysRemaining = daysRemaining;
    }
}
```
Thrown in `BloodBankSystem.validateDonationEligibility()` when a donor tries to donate before the 90-day waiting period expires. The exception object carries exactly when they last donated and when they are next eligible.

**How caught in `Main.java`:**
```java
} catch (DonorIneligibleException e) {
    // Handled separately — special deferral message
    System.out.println("DEFERRAL NOTICE: " + e.getMessage());
} catch (Exception e) {
    // Generic handler for all other errors
    System.out.println("Error: " + e.getMessage());
}
```

**Why:**  
Custom exceptions separate *what went wrong* from *how to respond*. The method that detects the problem throws a specific, named exception. The UI layer catches it and decides how to display it. This is far cleaner than returning `null` or integer error codes and checking them everywhere. The `DonorIneligibleException` specifically carries structured data (not just a message string) so callers can programmatically extract `nextEligibleDate` if needed.

---

### 4.11 Multithreading

**What it is:**  
Multithreading allows a program to run multiple tasks concurrently. Java's `Thread` class and `Runnable` interface make this possible. A **daemon thread** runs in the background and is automatically killed when the main program exits.

**Where it is used:**

1. **`ExpirySweeper.java` (Application-wide background daemon):**
Runs continuously in the background while the application is open. Every 30 seconds, it calls `sweepExpiredUnits()` to mark blood units that have exceeded their 42-day lifespan as `EXPIRED` and updates `units.txt` without disturbing the user console.

```java
public class ExpirySweeper implements Runnable {
    private final BloodBankSystem system;
    private final long intervalMillis;
    private volatile boolean running = true;

    public ExpirySweeper(BloodBankSystem system) {
        this(system, 30000); // Check every 30s
    }

    public void stop() { running = false; }

    @Override
    public void run() {
        while (running) {
            try {
                system.sweepExpiredUnits();
                Thread.sleep(intervalMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {}
        }
    }
}
```

2. **`LowStockMonitor.java` (Admin-session scoped monitor):**
Active specifically while the administrator is logged in to passively inspect inventory levels against configured thresholds.

```java
public class LowStockMonitor implements Runnable {
    private final BloodBankSystem system;
    private volatile boolean running = true;
    // volatile: ensures the main thread's change to 'running' is immediately visible
    // to the monitor thread (no CPU caching of the old value)

    public void stop() { running = false; }

    @Override
    public void run() {
        while (running) {
            try {
                for (BloodGroup g : BloodGroup.values()) {
                    int count = system.available(g);
                    int threshold = system.threshold(g);
                }
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();  // restore interrupt status
                break;
            } catch (Exception e) {}
        }
    }
}
```

**Starting and stopping the thread — `Main.java`:**
```java
// Application-wide daemon thread started in main():
ExpirySweeper sweeper = new ExpirySweeper(app);
Thread sweeperThread = new Thread(sweeper, "ExpirySweeper-Thread");
sweeperThread.setDaemon(true);     // daemon: exits automatically when main program finishes
sweeperThread.start();

// Scoped admin monitoring thread started in adminLogin():
LowStockMonitor monitor = new LowStockMonitor(app);
Thread thread = new Thread(monitor, "LowStockMonitor-Thread");
thread.setDaemon(true);
thread.start();

try {
    adminMenu();            // main thread blocks here while admin works
} finally {
    monitor.stop();         // always runs on logout, even if exception occurred
    thread.interrupt();     // wakes it from sleep immediately
}
```

**Thread-safety & Synchronization — `BloodBankSystem.java` & `FileManager.java`:**
```java
public synchronized int sweepExpiredUnits() { ... }
public synchronized void issueBlood(...) { ... }
public synchronized List<String> read(String name) { ... }
public synchronized void write(String name, List<String> lines) { ... }
```
`synchronized` ensures that when `ExpirySweeper` is scanning and marking units as expired on a background thread, the main thread cannot concurrently modify `units` or `issues` during an `issueBlood` operation. This prevents **race conditions** and data corruption.

**`IdGenerator.java` — thread-safe counter:**
```java
import java.util.concurrent.atomic.AtomicLong;

public class IdGenerator {
    private final AtomicLong counter;

    public String next() {
        return prefix + String.format("%04d", counter.getAndIncrement());
        // getAndIncrement() is one atomic CPU operation — can never produce duplicates
    }
}
```

**Why:**  
The `LowStockMonitor` conceptually runs in the background — like a real monitoring system — while staff continue working in the menus. Without threads, the program would have to pause the menu to check stock levels, making it unusable. The `volatile` keyword, `synchronized` methods, and `AtomicLong` are all tools to prevent **race conditions** — bugs that occur when two threads access or modify shared data simultaneously without coordination.

---

### 4.12 File Handling & Persistence

**What it is:**  
Java's `java.nio.file` package provides modern tools to read and write files. This project uses plain-text pipe-delimited (`|`) files as a lightweight database, with Base64 encoding to safely handle user-entered text.

**Where it is used:**

**`FileManager.java` — core I/O with try-with-resources:**
```java
public synchronized List<String> read(String name) {
    Path p = dir.resolve(name);                            // e.g., data/hospitals.txt
    return Files.readAllLines(p, StandardCharsets.UTF_8);  // reads all lines into a List
}

public synchronized void write(String name, List<String> lines) {
    Path p = dir.resolve(name);
    try (BufferedWriter w = Files.newBufferedWriter(p, StandardCharsets.UTF_8)) {
        // try-with-resources: file handle ALWAYS closed, even on exception
        for (String line : lines) {
            w.write(line);
            w.newLine();
        }
    }
}
```

**Base64 Encoding — why and how:**
```java
// When saving: encode text to protect against | in user input
public static String enc(String s) {
    return Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8));
}

// When loading: decode back to original text
public static String dec(String s) {
    return new String(Base64.getDecoder().decode(s), StandardCharsets.UTF_8);
}
```

**Real file line in `data/hospitals.txt`:**
```
SDAwMDE=|QmFsYWppIEhvc3BpdGFs|QmFsYWpp|OTY3NzIyNjY5Mg==|UENDT0UgQm95cyBIb3N0ZWw=|QmFsYWpp|MTIzNA==|PENDING
```
Decoded: `H0001 | Balaji Hospital | Balaji | 9677226692 | PCCOE Boys Hostel | Balaji | 1234 | PENDING`

**Save-on-change pattern — `BloodBankSystem.java`:**
```java
public synchronized HospitalUser registerHospital(...) {
    HospitalUser h = new HospitalUser(...);
    addHospital(h);
    saveHospitals();     // written to file immediately after every change
    return h;
}
```

**Load at startup:**
```java
public BloodBankSystem() {
    Arrays.fill(thresholds, 3);
    load();    // reads all .txt files and populates the in-memory arrays
}
```

**Why:**  
Without file persistence, all data would vanish when the program closes. The `|` delimiter separates fields. Base64 encoding prevents a hospital name like `"City|Hospital"` from corrupting the parser — encoded it becomes a single unambiguous token. `synchronized` on all I/O methods prevents two threads from writing to the same file simultaneously, which would corrupt data.

---

### 4.13 Java Date & Time API

**What it is:**  
`java.time.LocalDate` (Java 8+) represents a calendar date without time or timezone. It is **immutable** — every operation returns a new date, never modifying the original. `ChronoUnit.DAYS.between()` calculates the number of calendar days between two dates correctly across month and year boundaries.

**Where it is used:**

**Blood unit expiry — `BloodBankSystem.decideDonation()` and `BloodUnit.java`:**
```java
// When a donation is accepted, blood units are created with a 35-day expiry
units[uc++] = new BloodUnit(
    unitIds.next(), d.getId(), d.getBloodGroup(),
    d.getDate(),
    d.getDate().plusDays(35),     // expiry = collection date + 35 days
    BloodUnitStatus.AVAILABLE
);

// BloodUnit.java — auto-expire on check
public boolean isAvailableToday() {
    if (status == BloodUnitStatus.AVAILABLE && expiry.isBefore(LocalDate.now())) {
        status = BloodUnitStatus.EXPIRED;   // lazy expiry — marks expired on first check
    }
    return status == BloodUnitStatus.AVAILABLE;
}
```

**90-day donation deferral — `BloodBankSystem.validateDonationEligibility()`:**
```java
LocalDate last = getLastDonationDate(donorId);
if (last != null) {
    long daysPassed = ChronoUnit.DAYS.between(last, date);
    if (daysPassed < donationIntervalDays) {     // donationIntervalDays = 90
        long daysRemaining = donationIntervalDays - daysPassed;
        LocalDate nextEligible = last.plusDays(donationIntervalDays);
        throw new DonorIneligibleException(
            "Eligible on " + nextEligible + " (" + daysRemaining + " days remaining).",
            donorId, last, nextEligible, daysRemaining
        );
    }
}
```

**Donor eligibility display — `BloodBankSystem.printDonors()`:**
```java
long days = ChronoUnit.DAYS.between(last, LocalDate.now());
if (days >= donationIntervalDays) {
    eligibility = "Eligible (last donated: " + last + ", " + days + "d ago)";
} else {
    eligibility = "Ineligible until " + last.plusDays(donationIntervalDays)
                + " (" + (donationIntervalDays - days) + "d left)";
}
```

**Why:**  
The modern `java.time` API is immutable and far safer than the legacy `java.util.Date`. `LocalDate.plusDays(35)` creates a *new* date — it never modifies the original — preventing accidental mutation bugs. `ChronoUnit.DAYS.between()` handles all calendar complexities (February, leap years, month-end rollover) automatically. Every date stored in files is in `ISO-8601` format (`YYYY-MM-DD`), which `LocalDate.parse()` reads back without any formatter.

---

## 5. File-by-File Code Explanation

### `Main.java`
- **Role:** Application entry point and console UI controller.
- **Key design decisions:**
  - Holds the global `BloodBankSystem app` and `Scanner sc` as `static` fields so all menu methods share them without passing them as parameters.
  - `adminLogin()` wraps `adminMenu()` in `try/finally` — the `LowStockMonitor` thread is always stopped on logout, even if an exception crashes out of the menu.
  - `chooseGroup()` is a private helper method abstracting blood group menu selection. It is reused in 4 different menu options, avoiding repetition.
  - `DonorIneligibleException` is caught **before** the general `Exception` catch block so it gets a custom, more informative deferral message.

### `model/User.java`
- **Role:** Abstract superclass for all users.
- **Key design decisions:**
  - `private static String req(String s, String field)` is a private validation utility — internal implementation detail never exposed outside the class.
  - `displayName()` is a concrete method that calls the `abstract` `getRole()` — this is the classic **Template Method** pattern: define the algorithm structure in the superclass, defer one step to subclasses.
  - Constructor 2 uses `this(...)` to delegate to Constructor 1 — zero duplication of validation logic.

### `model/Admin.java` & `model/HospitalUser.java`
- **Role:** Concrete user personas.
- **Key design decisions:**
  - `Admin` is intentionally minimal — it has no extra fields because admin power comes from what the admin *does* in `Main.java`, not from fields on the object.
  - `HospitalUser` keeps `status` mutable (not `final`) because a hospital's approval status must change after registration.

### `model/BloodRequest.java`
- **Role:** Models a hospital's requisition for blood.
- **Key design decisions:**
  - `addIssuedUnits(int n)` auto-updates `status` to `PARTIALLY_FULFILLED` or `FULFILLED` based on how many units have been issued vs. requested. This business logic lives **inside the model** — not scattered across menu handlers.
  - `getRemainingUnits()` = `units - issuedUnits` — a derived value computed on the fly, never stored, which prevents the two values from ever becoming inconsistent.

### `model/BloodUnit.java`
- **Role:** One physical blood bag in inventory.
- **Key design decisions:**
  - `isAvailableToday()` has a **side effect**: it marks an expired unit as `EXPIRED` if its status is still `AVAILABLE`. This "lazy expiry" means units are marked expired the first time they are checked after their expiry date — without needing a scheduled job.
  - Implements `FileRecord` — declares this class can serialize itself; keeps serialization logic close to the model, not in a separate utility.

### `service/BloodBankSystem.java`
- **Role:** Central business logic engine.
- **Key design decisions:**
  - All public methods are `synchronized` — prevents race conditions if the background monitor thread and the UI main thread access the system simultaneously.
  - `load()` is called in the constructor — every `BloodBankSystem` always starts with all saved data already loaded. There is no separate "initialize" step to forget.
  - `advance(IdGenerator g, String id, String prefix)` fast-forwards each ID generator past the highest loaded ID so new records never reuse a previously assigned ID.
  - `pendingRequestCount()` is separate from `requestCount()` — Option 11 (Approve/Reject Request) needs to distinguish between "no requests at all" and "requests exist but none are pending" to avoid prompting the admin uselessly.

### `persistence/FileManager.java`
- **Role:** Low-level file I/O utility.
- **Key design decisions:**
  - `try-with-resources` ensures the file handle (`BufferedWriter`) is always closed — no resource leaks, even if an exception occurs mid-write.
  - `StandardCharsets.UTF_8` is always explicit — never relying on the platform's default encoding, which differs between Windows, Linux, and macOS.

### `persistence/FileRecord.java`
- **Role:** Serialization interface / contract.
- **Key design decisions:**
  - Intentionally has just one method. This embodies the **Interface Segregation Principle** — a small, focused interface. Only `BloodUnit` currently implements it; any future model class can opt in simply by adding `implements FileRecord`.

### `thread/LowStockMonitor.java`
- **Role:** Background daemon monitoring thread.
- **Key design decisions:**
  - `volatile boolean running` — ensures that when the main thread calls `monitor.stop()`, the flag change is **immediately visible** to the monitor thread. Without `volatile`, the JVM is allowed to cache the value in the monitor thread's CPU register, and it might never see the change.
  - `Thread.currentThread().interrupt()` inside the `InterruptedException` catch block — restores the interrupted status so higher-level code can detect that the thread was interrupted. Silently swallowing the exception is a known Java anti-pattern.

### `util/IdGenerator.java`
- **Role:** Thread-safe sequential ID generator.
- **Key design decisions:**
  - `AtomicLong.getAndIncrement()` is a single, indivisible (atomic) CPU instruction. Even if two threads call `next()` simultaneously, they get different values — guaranteed. A plain `long++` without synchronization would be a **race condition**: both threads could read the same value and produce duplicate IDs.

### `exception/DonorIneligibleException.java`
- **Role:** Domain-specific exception for the 90-day donation deferral rule.
- **Key design decisions:**
  - Carries `lastDonationDate`, `nextEligibleDate`, and `daysRemaining` as typed fields — not embedded in the message string. This allows code to extract structured data from the exception (`e.getDaysRemaining()`) rather than parsing a human-readable message.
  - Two constructors (overloading) — one minimal (just message for simple cases), one full (all deferral details for the normal case) — consistent with the rest of the project.

---

## 6. Data Flow Diagram

```
 User Input (Console)
         │
         ▼
     Main.java  ─────────────────────────────────────────────────┐
         │                                                         │
         │ calls methods on                                        │ starts/stops
         ▼                                                         ▼
 BloodBankSystem.java                               LowStockMonitor (Thread)
   ┌─────────────┐
   │ In-Memory   │ ◄──── load() at startup ─────────────────────────────┐
   │ Arrays:     │                                                        │
   │  hospitals[]│                                               FileManager.java
   │  donors[]   │ ──── save*() on every change ──────────────► (reads/writes)
   │  donations[]│                                                        │
   │  units[]    │                                               data/hospitals.txt
   │  requests[] │                                               data/donors.txt
   │  issues[]   │                                               data/donations.txt
   └─────────────┘                                               data/units.txt
         │                                                        data/requests.txt
         │ creates/mutates objects                                data/issues.txt
         ▼
   Model Objects:
   HospitalUser, Donor, Donation,
   BloodUnit, BloodRequest, BloodIssue
         │
         │ throws exceptions when rules violated
         ▼
   Custom Exceptions:
   InsufficientStockException
   InvalidOperationException
   DonorIneligibleException
         │
         │ caught by
         ▼
     Main.java (displays appropriate message to user)
```

---

## 7. How to Run

### Prerequisites
- Java 17 or later (developed on Java 26)

### Commands (from the project root directory)
```bash
# Compile all Java source files
javac -d out $(find src -name "*.java")

# Run the application
java -cp out bloodbank.Main
```

### Demo Credentials
- **Admin:** username = `admin`, password = `admin123`
- **Hospital (example registered):** username = `Balaji`, password = `1234`

### Data Files
All records are automatically saved to the `data/` folder. To start fresh, delete the `.txt` files inside `data/`.

---

> **Educational Disclaimer:** This is an academic simulation for an OOP course. Real blood banks require clinical-grade software, regulatory compliance, and strict medical protocols. This project demonstrates OOP concepts only and must never be used for actual medical decisions.
