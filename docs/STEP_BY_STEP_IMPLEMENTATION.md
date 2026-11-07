# ClinicQueue MVP — Step-by-Step Implementation Log

This document tracks **every single step performed**, explaining **WHAT** was built, **WHY** it was built that way, and **HOW** it works under the hood.

---

## Phase 0: Workspace & Environment Inspection

### Step 0.1: Environment Verification
- **WHAT**: Inspected the local Java runtime and build tool environment.
- **WHY**: The specification mandates Java 21+ and Maven. We needed to ensure compatible tooling without breaking or modifying existing user system settings.
- **HOW**:
  - Executed `java -version`: Found `OpenJDK 25.0.1 LTS` and `JDK 17 LTS`.
  - Executed `mvn -version`: Maven was not present in the global Windows `PATH`.
  - Discovered local Maven Wrapper binaries inside the local IDE directory (`resources/maven-wrapper`), and deployed `mvnw.cmd` and `.mvn` directly into the project root.

### Step 0.2: Specification Analysis & Scope Definition
- **WHAT**: Read and dissected `ClinicQueue_MVP_Antigravity_Build_Spec.pdf` (all 7 pages).
- **WHY**: Prevents scope creep, ensures strict adherence to requirements (FR-01 to FR-15), and prevents building out-of-scope features (e.g. AI, cloud, auth).
- **HOW**: Synthesized architectural requirements into `docs/ARCHITECTURE.md` and prepared viva/evaluator defense answers in `docs/EVALUATOR_QUESTIONS_AND_ANSWERS.md`.

---

## Phase 1: Project Skeleton & Build Configuration

### Step 1.1: Maven Configuration (`pom.xml`)
- **WHAT**: Configured Maven `pom.xml` with JavaFX 21, SQLite-JDBC, SLF4J, and JUnit 5.
- **WHY**: Enables reproducible builds, automated testing via `mvn test`, and running the desktop app via `mvn javafx:run`.
- **HOW**: Targeted Java 17 bytecode (runs cleanly across Java 17, 21, and 25).

### Step 1.2: Domain Models & DTOs
- **WHAT**: Created clean POJO models:
  - `VisitStatus.java`: WAITING, IN_CONSULTATION, COMPLETED, CANCELLED.
  - `VisitType.java`: APPOINTMENT, WALK_IN.
  - `Department.java`: Clinical specialties (General Medicine, Pediatrics).
  - `Doctor.java`: Consulting physicians.
  - `Patient.java`: Registered patient records.
  - `Visit.java`: Queue entries with token numbers and wait time estimates.
  - `DoctorQueueSummary.java`: DTO for live dashboard cards.
  - `DashboardSummary.java`: DTO for aggregated clinic metrics.
- **WHY**: Establishes clean domain contracts without coupling to database or UI libraries.
- **HOW**: Standard Java classes with encapsulated fields, getters, setters, and readable `toString()` implementations.

---

## Phase 2: Persistence & SQLite Engine

### Step 2.1: Database Connection Factory (`DatabaseManager.java`)
- **WHAT**: Connection utility providing SQLite connections to `clinicqueue.db`.
- **WHY**: SQLite disables foreign key enforcement by default. Every connection must execute `PRAGMA foreign_keys = ON;` to prevent orphaned visits.
- **HOW**: `DatabaseManager.getConnection()` creates the connection and immediately executes the foreign key PRAGMA. Supports switching URLs dynamically for isolated test suites (`clinicqueue_test.db`).

### Step 2.2: Schema Initialization & Seed Data (`SchemaInitializer.java`)
- **WHAT**: Idempotent DDL and initial data seeding.
- **WHY**: The application must be 100% self-initializing on fresh launch without manual SQL imports (FR-12, FR-13).
- **HOW**:
  - Executes `CREATE TABLE IF NOT EXISTS` for `departments`, `doctors`, `patients`, and `visits`.
  - Sets unique constraint `UNIQUE (doctor_id, visit_date, token_sequence)`.
  - Pre-seeds 2 departments and 3 doctors if tables are empty.
  - Provides `resetAndReseedDemoData()` for the confirmed presentation reset button.

---

## Phase 3: Repositories & Transactional Registration

### Step 3.1: Data Repositories
- **WHAT**:
  - `DepartmentRepository.java`: Retrieves active departments.
  - `DoctorRepository.java`: Queries doctors by department and ID.
  - `PatientRepository.java`: Inserts patient records within active transactions.
  - `VisitRepository.java`: Queries queue rows, searches, and atomic token increments.
- **WHY**: Isolates SQL queries and SQLite JDBC drivers from the UI and services.
- **HOW**: 100% prepared statements (`PreparedStatement`) with parameter binding (`?`) preventing SQL injection.

### Step 3.2: Transactional Registration Service (`VisitService.java`)
- **WHAT**: Validates patient input and allocates sequential daily tokens.
- **WHY**: Satisfies FR-01, FR-02, FR-03, and FR-15. Failed writes must not leave partial patient records.
- **HOW**:
  - Trims name, rejects blank names.
  - Opens transaction: `conn.setAutoCommit(false)`.
  - Inserts patient, queries next token sequence `SELECT COALESCE(MAX(token_sequence), 0) + 1`, inserts visit, and commits.
  - Rolls back cleanly if any step fails.

---

## Phase 4: Queue Engine & State Machine

### Step 4.1: Queue Service (`QueueService.java`)
- **WHAT**: Enforces queue rules, "Call Next" selection, and status transitions.
- **WHY**: Satisfies FR-05, FR-06, FR-07, and FR-08. Prevents invalid workflow actions.
- **HOW**:
  - `callNext()`: Checks if doctor already has an `IN_CONSULTATION` patient. If yes, throws `IllegalStateException`. If no, selects oldest `WAITING` patient (FIFO) and transitions them to `IN_CONSULTATION`.
  - `completeConsultation()`: Verifies visit is currently `IN_CONSULTATION` before transitioning to `COMPLETED`.
  - `cancelVisit()`: Verifies visit is `WAITING` before transitioning to `CANCELLED`.
  - `calculateWaitTimes()`: Computes `waitingPosition * 10` minutes for waiting visits.

---

## Phase 5: Dashboard & Search

### Step 5.1: Dashboard Service (`DashboardService.java`)
- **WHAT**: Computes total registered, waiting, in consultation, and completed counts, plus per-doctor status.
- **WHY**: Satisfies FR-09 and FR-11.
- **HOW**: Iterates through today's visits in memory or SQL, builds `DashboardSummary` and `DoctorQueueSummary` DTOs.

### Step 5.2: Search & Filter Logic
- **WHAT**: Real-time filtering in `VisitRepository.searchVisits()`.
- **WHY**: Satisfies FR-10. Allows receptionists to quickly locate a patient by name or token.
- **HOW**: Dynamic parameterized SQL query filtering by `doctor_id`, `status`, and case-insensitive wildcard pattern (`%query%`).

---

## Phase 6: JavaFX UI & Theme Design

### Step 6.1: Styling (`styles.css`)
- **WHAT**: Modern clinic-operations theme (Navy, Slate, White, Amber, Green).
- **WHY**: Delivers a polished, professional look suitable for an academic presentation.
- **HOW**: JavaFX CSS with custom classes for metric cards, status chips (`chip-waiting`, `chip-completed`), buttons, and tables.

### Step 6.2: Views
- **WHAT**:
  - `DashboardView.java`: Operational metrics and doctor cards.
  - `RegisterVisitView.java`: Registration form and token confirmation card.
  - `QueueBoardView.java`: Filterable table with Call Next, Complete, and Cancel buttons.
  - `MainLayoutView.java`: Persistent header, navigation bar, content area, and status bar.
  - `ClinicQueueApp.java`: Main application entry point.
- **WHY**: Clean separation of views with thin event handlers invoking services.
- **HOW**: Built using standard JavaFX controls (`BorderPane`, `TableView`, `GridPane`, `ComboBox`, `Button`).

---

## Phase 7: Automated Testing & Verification

### Step 7.1: Unit & Integration Tests
- **WHAT**: JUnit 5 test suites:
  - `VisitServiceTest.java` (token generation, blank name validation, doctor separation).
  - `QueueServiceTest.java` (Call Next FIFO, single active consultation rule, completion/cancellation transitions, wait time estimation).
- **WHY**: Verifies all business rules and invariants automatically without requiring a display server or manual clicking.
- **HOW**: Tests run against an isolated test SQLite database (`clinicqueue_test.db`), resetting tables before each test.
- **RESULT**: **7 of 7 tests PASS with zero failures.**
