# ClinicQueue MVP — Evaluator & Viva Comprehensive Defense Guide

This guide contains **all possible questions** an evaluator, professor, or technical judge can ask during the defense of the ClinicQueue project, organized by **phase, architectural layer, and domain**. Each question includes the **exact technical rationale, underlying concepts, and model response**.

---

## Table of Contents
1. [General System & Architecture Questions (Phase 0 & 1)](#1-general-system--architecture-questions)
2. [Database Design, Concurrency & Transactions (Phase 2)](#2-database-design-concurrency--transactions)
3. [Patient Registration, Validation & Tokens (Phase 3)](#3-patient-registration-validation--tokens)
4. [Queue Engine & State Machine Rules (Phase 4)](#4-queue-engine--state-machine-rules)
5. [Dashboard, Search & Wait Estimation (Phase 5)](#5-dashboard-search--wait-estimation)
6. [Testing, Quality & Error Handling (Phase 6)](#6-testing-quality--error-handling)
7. [System Limitations & Production Scalability (Phase 7)](#7-system-limitations--production-scalability)
8. [Rapid-Fire Viva / Technical Gotchas](#8-rapid-fire-viva--technical-gotchas)

---

## 1. General System & Architecture Questions

### Q1.1: Why did you choose a layered architecture (MVC / Service-Repository) instead of writing database queries directly inside the UI event handlers?
- **What**: The application is separated into 4 distinct layers:
  1. **View (JavaFX)**: UI layout, widgets, rendering.
  2. **Controller**: Handles user UI events (clicks, keypresses), invokes services.
  3. **Service**: Encapsulates business logic, validation, state machine rules, and transaction boundaries.
  4. **Repository**: Pure data access using JDBC and PreparedStatements.
- **Why**: 
  - **Separation of Concerns (SoC)**: UI code should not know how data is stored, and database code should not know how buttons are clicked.
  - **Testability**: Services and repositories can be unit-tested without initializing the JavaFX desktop environment.
  - **Maintainability**: If we decide to swap SQLite for PostgreSQL or swap JavaFX for a Web UI, only the data or presentation layer changes; the business logic remains untouched.
- **How**: Controllers call Service interfaces; Services call Repository methods; Repositories interact directly with SQLite via JDBC connections.

### Q1.2: Why Java 21/modern Java and JavaFX instead of a Web-based stack (React/Electron)?
- **What**: A native desktop application running on modern Java LTS with JavaFX controls.
- **Why**:
  - **Offline reliability**: Hospital OPD reception desks and nursing stations require zero-latency, local-first operation without reliance on internet connectivity or external web servers.
  - **Lightweight footprint**: Electron packages a full Chromium browser and Node.js runtime, taking hundreds of megabytes of RAM. JavaFX runs smoothly with minimal memory.
  - **Strong Type Safety**: Java's strong static typing prevents common runtime JavaScript errors in critical healthcare workflows.

### Q1.3: Why avoid heavyweight frameworks like Spring Boot or Hibernate for this project?
- **What**: Built using core Java, direct JDBC, and pure JavaFX without Spring or Hibernate.
- **Why**:
  - **Startup Latency**: Plain Java + JDBC initializes in less than a second, ideal for quick desktop launches.
  - **Transparency**: ORM tools like Hibernate generate complex, hidden SQL queries and introduce lazy-loading issues and session caching complexities. Pure JDBC provides full visibility and control over every SQL query and transaction boundary.
  - **Architectural Scope**: The spec mandates an MVP prototype for presentation; adding Spring Boot would overengineer the solution without adding product value.

---

## 2. Database Design, Concurrency & Transactions (Phase 2)

### Q2.1: Why SQLite for this clinic queue system?
- **What**: An embedded, zero-configuration relational database stored in a local file (`clinicqueue.db`).
- **Why**:
  - Requires no background database server setup (like MySQL or Postgres), making the project 100% portable and runnable on any evaluator's computer.
  - Fully ACID compliant, supporting transactions, foreign keys, and unique constraints.
- **Limitations to acknowledge**: SQLite allows only one writer at a time (file-level lock). For a single-clinic desktop workstation, this is ideal. For a multi-terminal hospital, a client-server RDBMS like PostgreSQL would be used.

### Q2.2: How do you ensure foreign key constraints are enforced in SQLite?
- **What**: SQLite disables foreign key enforcement by default for backward compatibility.
- **Why**: Without foreign keys, orphaned visits could exist if a doctor or patient were deleted.
- **How**: In our `DatabaseManager`, every single time a connection is opened, we immediately execute:
  ```sql
  PRAGMA foreign_keys = ON;
  ```
  This is validated in our unit tests.

### Q2.3: How do you prevent race conditions and duplicate token generation?
- **What**: Token format is `D{doctorId}-{date}-{sequence}` (e.g. `D01-2026-10-20-001`).
- **Why**: If two visits are registered at almost the same instant, both might read the same highest sequence number and try to assign duplicate tokens.
- **How**:
  1. We execute token allocation and visit creation inside an **atomic database transaction** (`connection.setAutoCommit(false)`).
  2. We query the maximum sequence for that doctor and date:
     ```sql
     SELECT COALESCE(MAX(token_sequence), 0) + 1 FROM visits WHERE doctor_id = ? AND visit_date = ?;
     ```
  3. We enforce a database-level constraint:
     ```sql
     UNIQUE(doctor_id, visit_date, token_sequence)
     ```
  4. If any collision occurs, the transaction rolls back, preventing duplicate or corrupted records.

### Q2.4: How does schema initialization work on application startup?
- **What**: Idempotent DDL scripts execute automatically when the app launches.
- **Why**: Evaluators must be able to clone the project and run it immediately without running manual SQL scripts.
- **How**: We use `CREATE TABLE IF NOT EXISTS` for `departments`, `doctors`, `patients`, and `visits`. Then, we check if `departments` and `doctors` are empty; if so, we seed initial sample clinical specialties and doctors.

---

## 3. Patient Registration, Validation & Tokens (Phase 3)

### Q3.1: What validation rules are applied when registering a patient?
- **What**:
  - Patient Full Name: Required, trimmed of leading/trailing whitespace, cannot be empty or blank.
  - Phone: Optional, but if provided, must match valid phone format (e.g., 10 digits).
  - Department: Must be selected.
  - Doctor: Must be selected and belong to the chosen department.
  - Visit Type: Must be explicitly selected as `APPOINTMENT` or `WALK_IN`.
- **Why**: Prevents garbage entries, database constraint errors, and UI layout breaks.
- **How**: Validated at the `VisitService` layer before invoking any repository calls. If validation fails, a custom `ValidationException` is thrown and displayed in a user-friendly alert.

### Q3.2: Why do both `APPOINTMENT` and `WALK_IN` enter the same queue in this MVP?
- **What**: Both visit types are placed in the doctor's daily FIFO queue, but clearly labeled with visual badges.
- **Why**: In real outpatient clinics, scheduled appointments and walk-in patients typically report to the same triage desk on the day of consultation. Introducing time-slot scheduling and priority preemption is beyond the defined MVP scope. Transparently labeling them in the queue ensures clarity without unnecessary complexity.

---

## 4. Queue Engine & State Machine Rules (Phase 4)

### Q4.1: Explain the Queue State Machine. What are the allowed transitions?
- **What**: A visit progresses through 4 distinct statuses:
  - `WAITING` $\rightarrow$ `IN_CONSULTATION` (via "Call Next")
  - `IN_CONSULTATION` $\rightarrow$ `COMPLETED` (via "Complete Consultation")
  - `WAITING` $\rightarrow$ `CANCELLED` (via "Cancel Visit", requires user confirmation)
- **Forbidden Transitions**:
  - `WAITING` $\rightarrow$ `COMPLETED` (Cannot complete without consultation).
  - `IN_CONSULTATION` $\rightarrow$ `CANCELLED` (Active consultation cannot be cancelled directly).
  - `COMPLETED` / `CANCELLED` $\rightarrow$ any state (Terminal states are immutable).
- **How**: Enforced in `QueueService.updateStatus()`. Any invalid transition throws an `IllegalStateException` with a descriptive message.

### Q4.2: What happens if a doctor clicks "Call Next" while they already have a patient `IN_CONSULTATION`?
- **What**: The system blocks the action and displays an informative alert: *"Doctor already has a patient in consultation. Please complete the ongoing visit before calling the next patient."*
- **Why**: A doctor can physically consult with only one patient at a time. Allowing multiple active consultations leads to queue confusion and inaccurate wait calculations.
- **How**: `QueueService.callNext()` executes a query:
  ```sql
  SELECT COUNT(*) FROM visits WHERE doctor_id = ? AND visit_date = ? AND status = 'IN_CONSULTATION';
  ```
  If count > 0, it aborts immediately.

### Q4.3: What is the ordering rule for "Call Next"?
- **What**: FIFO (First-In, First-Out) ordered by `created_at ASC`, with `id ASC` as a deterministic tie-breaker.
- **Why**: Ensures fairness in outpatient queuing. Oldest waiting patient is always called first.

---

## 5. Dashboard, Search & Wait Estimation (Phase 5)

### Q5.1: How is the "Estimated Wait Time" calculated? Is it an AI prediction?
- **What**:
  $$\text{Estimated Wait (minutes)} = \text{Queue Position Ahead} \times \text{Average Duration (default 10 min)}$$
- **Why**: We explicitly **do not** claim this is an AI prediction. In outpatient clinics, queue transparency is crucial. Displaying a clear, rule-based estimate with disclosed assumptions is far more trustworthy for patients than a black-box estimate.
- **How**: If patient $P$ is 3rd in the `WAITING` line for Dr. Smith, their wait is $3 \times 10 = 30$ minutes. The UI explicitly displays: `Approx. 30 mins (based on 10 min/patient avg)`.

### Q5.2: How does the search and filter work?
- **What**: Real-time filtering by Doctor dropdown, Status filter (`ALL`, `WAITING`, `IN_CONSULTATION`, `COMPLETED`, `CANCELLED`), and text search (matching Patient Name or Token Number).
- **Why**: In busy clinics, receptionists frequently need to quickly locate a patient when they arrive or inquire about their token.
- **How**: Implemented through parameterized SQL queries and JavaFX filtered list bindings for instant responsiveness.

---

## 6. Testing, Quality & Error Handling (Phase 6)

### Q6.1: How are your automated tests structured? Do they require a UI to run?
- **What**: Unit and integration tests written in JUnit 5 testing `VisitService`, `QueueService`, `TokenService`, and repositories.
- **Why**: JavaFX UI tests (e.g. TestFX) can be brittle, slow, and fail in headless environments (like CI/CD or command-line grading). Business logic is completely decoupled from JavaFX, allowing all tests to execute in milliseconds via `mvn test`.
- **How**: Tests use an in-memory or isolated test database (`clinicqueue_test.db`), resetting tables before each test suite to guarantee 100% isolation.

### Q6.2: How do you prevent SQL injection?
- **What**: 100% of SQL statements in the repository layer use `java.sql.PreparedStatement` with parameterized placeholders (`?`).
- **Why**: Concatenating strings into SQL queries allows malicious input (e.g. `' OR '1'='1`) to manipulate queries.
- **How**: No SQL query is ever created via string concatenation with user input.

### Q6.3: What happens if an unexpected database error occurs? Does the app crash?
- **What**: All repository SQLExceptions are caught and wrapped in custom domain exceptions (e.g. `DataAccessException`).
- **Why**: Raw SQL exception stack traces must never be exposed to receptionists or doctors.
- **How**: The UI layer catches domain exceptions and presents an error dialog with actionable guidance (e.g., *"Database temporarily locked. Please retry in a few seconds."*), while the full technical stack trace is logged to standard error for developer debugging.

---

## 7. System Limitations & Production Scalability (Phase 7)

### Q7.1: What are the known limitations of this MVP?
- **Single-Computer Scope**: SQLite file-based locking prevents high-concurrency multi-computer setups.
- **No Role Authentication**: Pre-seeded without password login to prioritize queue flow during short classroom presentations.
- **Simplified Scheduling**: Does not support advance booking for future calendar dates with time slots.
- **Fictional Data**: Designed purely for demonstration; not certified under HIPAA or healthcare data regulations.

### Q7.2: If this were to be deployed in a 500-bed hospital, what architectural changes would you make?
1. **Database**: Replace SQLite with PostgreSQL or MySQL to support concurrent writes across dozens of reception desks.
2. **Backend**: Expose a REST / gRPC API using Spring Boot or Quarkus running in a containerized environment (Docker/Kubernetes).
3. **Real-time Updates**: Implement WebSockets or Server-Sent Events (SSE) so display monitors in waiting rooms update automatically without manual refresh.
4. **Hardware Integration**: Connect thermal receipt printers for physical token slips and LED queue displays above consultation rooms.
5. **Security**: Role-Based Access Control (RBAC), OAuth2 / JWT authentication, audit logging, and encryption at rest.

---

## 8. Rapid-Fire Viva / Technical Gotchas

| Question | Short High-Impact Answer |
| :--- | :--- |
| **Q: Where is the database file stored?** | In the application working directory as `clinicqueue.db`. |
| **Q: Can two doctors have token number 001 on the same date?** | Yes! Token numbering is doctor-specific (`D01-...-001` and `D02-...-001`). |
| **Q: What happens if the app is closed and restarted?** | SQLite persists all records. Upon restart, today's visits, active consultations, and queue counts are fully restored. |
| **Q: How does Reset / Reseed work?** | Prompts for user confirmation, purges local demo visits, and re-seeds clean sample data for presentation. |
| **Q: Are cancelled visits included in the wait time calculation?** | No. Only visits with status `WAITING` ahead of the patient are counted. |
| **Q: How is the database connection managed?** | A singleton `DatabaseManager` maintains connection pooling or manages clean single connections with proper `AutoCloseable` resource handling. |
