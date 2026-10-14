# Blood Bank Management System — Core Java

## Requirements
Java 17 or later recommended. No external libraries.

## Compile and run
From the project root:

```bash
javac -d out $(find src -name "*.java")
java -cp out bloodbank.Main
```

On Windows PowerShell, use:

```powershell
Get-ChildItem -Recurse src -Filter *.java | ForEach-Object { $_.FullName } | Set-Content sources.txt
javac -d out @sources.txt
java -cp out bloodbank.Main
```

The program creates a `data` directory in the working directory. Do not delete it if you want to keep saved records. Demo admin login: `admin` / `admin123`.

## Main workflow
1. Register a hospital through Hospital menu.
2. Log in as admin and approve the hospital.
3. Register a donor, record donation, accept it in the donation review menu.
4. Hospital logs in and submits a request.
5. Admin approves request and issues units.
6. Inventory and issue records update and persist.

## OOP syllabus mapping
- Classes/objects: all model and service classes.
- Constructors: parameterized constructors, plus overloaded `User` constructor.
- Arrays of objects: fixed-size arrays in `BloodBankSystem`.
- Encapsulation: private model fields with controlled accessors.
- Inheritance: `Admin` and `HospitalUser` extend abstract `User`.
- Polymorphism: overridden `getRole()` and `displayName()` using a `User` reference.
- Abstract class: `User`.
- Interface: `FileRecord` is included as a persistence contract example; file persistence is currently centralized in `FileManager`/`BloodBankSystem`.
- Exceptions: custom `InsufficientStockException` and `InvalidOperationException`.
- Multithreading: `LowStockMonitor` implements `Runnable` and runs on a daemon thread.
- File handling: text files in `data/` loaded at startup and saved on changes.

## Educational limitation
This is a console-based academic simulation, not a real blood bank system. The donation expiry period and workflows are illustrative only. Do not use it for medical decisions. Real screening, compatibility, issue authorization, and blood-bank record handling must follow approved clinical procedures and regulations. Demo passwords are not suitable for real deployment.
