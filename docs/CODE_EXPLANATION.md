# ClinicQueue MVP — Codebase Reference & Technical Explanation

This document contains an easy-to-understand, comprehensive breakdown of every single class and method in the ClinicQueue codebase. Use this to prepare for your project explanation or viva.

Every section follows the **WHAT, WHY, HOW** methodology.

---

## 1. Entry Point: `in.clinicqueue.ClinicQueueApp`

### `ClinicQueueApp.java`
- **WHAT**: The main JavaFX desktop application class. Inherits from `javafx.application.Application`.
- **WHY**: Serves as the bootstrap entry point that initializes the database and creates the main window.
- **HOW**:
  - `main(String[] args)`:
    1. Calls `SchemaInitializer.initDatabase()` to create SQLite tables if they don't exist.
    2. Calls `SchemaInitializer.seedDemoVisitsIfEmpty()` to populate today's sample data if visits are empty.
    3. Calls `launch(args)` to boot the JavaFX UI runtime.
  - `start(Stage primaryStage)`:
    1. Instantiates `MainLayoutView`.
    2. Creates a `Scene` sized at $1200 \times 750$ (resizable, optimized for $1366 \times 768$ laptops).
    3. Attaches `styles.css`.
    4. Shows the desktop window.

---

## 2. Database Layer: `in.clinicqueue.database`

### `DatabaseManager.java`
- **WHAT**: Singleton database connection factory for SQLite.
- **WHY**:
  1. Centralizes database URL configuration.
  2. SQLite disables foreign keys by default. We must run `PRAGMA foreign_keys = ON;` on **every single connection**, otherwise foreign key violations will silently succeed.
  3. Allows switching to a test database (`clinicqueue_test.db`) during unit tests.
- **HOW**:
  - `getConnection()`: Calls `DriverManager.getConnection(dbUrl)` and executes `stmt.execute("PRAGMA foreign_keys = ON;");` before returning the connection.
  - `setDbUrl(String newUrl)`: Used by JUnit test classes to redirect to an isolated test database.

### `SchemaInitializer.java`
- **WHAT**: Runs database schema creation scripts (DDL) and seeds base clinical specialties, doctors, and sample visits.
- **WHY**: Ensures that anyone running the project gets a fully working system immediately without running manual SQL setup scripts.
- **HOW**:
  - `initDatabase()`: Runs `CREATE TABLE IF NOT EXISTS` for `departments`, `doctors`, `patients`, and `visits`.
  - `seedBaseSetup()`: Checks `SELECT COUNT(*) FROM departments`. If 0, inserts 2 departments (General Medicine, Pediatrics) and 3 doctors (Dr. Sarah Sharma, Dr. Rajesh Patel, Dr. Ananya Iyer).
  - `seedDemoVisitsIfEmpty()`: Checks if visits exist for today's date. If empty, creates 6 sample visits across `WAITING`, `IN_CONSULTATION`, and `COMPLETED`.
  - `resetAndReseedDemoData()`: Wraps `DELETE FROM visits; DELETE FROM patients;` and sample re-seeding in an atomic transaction for the one-click demo reset button.

---

## 3. Domain Model Layer: `in.clinicqueue.model`

- **`VisitStatus.java` (Enum)**:
  - Defines the 4 official states: `WAITING`, `IN_CONSULTATION`, `COMPLETED`, `CANCELLED`.
  - Includes user-friendly display labels (e.g. `"In Consultation"`).
- **`VisitType.java` (Enum)**:
  - Defines `APPOINTMENT` and `WALK_IN`.
- **`Department.java`**:
  - POJO holding `id`, `name`, and `active` flag.
- **`Doctor.java`**:
  - POJO holding `id`, `departmentId`, `displayName`, `active`, and joined `departmentName`.
- **`Patient.java`**:
  - POJO holding `id`, `fullName`, optional `phone`, and `createdAt`.
- **`Visit.java`**:
  - The core queue record holding `patientId`, `doctorId`, `visitDate`, `visitType`, `tokenSequence`, `tokenNumber`, `status`, `waitingPosition`, and `estimatedWaitMinutes`.
- **`DoctorQueueSummary.java` (DTO)**:
  - Lightweight data object used by the Dashboard to display a doctor's card (doctor name, department, waiting count, active patient token).
- **`DashboardSummary.java` (DTO)**:
  - Holds total visits, waiting visits, in-consultation visits, completed visits, and the list of `DoctorQueueSummary` items.

---

## 4. Repository Layer (Data Access): `in.clinicqueue.repository`

### `DepartmentRepository.java`
- **WHAT**: Fetches clinical departments.
- **WHY**: Keeps SQL separated from business logic.
- **HOW**: `getAllActive()` runs `SELECT id, name, active FROM departments WHERE active = 1 ORDER BY name ASC` and maps rows into `Department` objects.

### `DoctorRepository.java`
- **WHAT**: Fetches doctors.
- **WHY**: Allows dynamic filtering of doctors based on chosen clinical department.
- **HOW**:
  - `getAllActive()`: Joins `doctors` and `departments` to get all active doctors with department names.
  - `getByDepartmentId(int deptId)`: Parameterized query filtering by department.
  - `getById(int id)`: Single doctor lookup.

### `PatientRepository.java`
- **WHAT**: Handles patient database insertion.
- **WHY**: Patient registration needs to participate in atomic database transactions.
- **HOW**:
  - `insert(Connection conn, Patient patient)`: Takes an existing connection, executes `INSERT INTO patients (full_name, phone) VALUES (?, ?)`, retrieves the auto-generated primary key via `getGeneratedKeys()`, and assigns it to the patient.

### `VisitRepository.java`
- **WHAT**: The core data access class for all visit and queue operations.
- **WHY**: Centralizes all SQL statements for visits, ensuring parameterized inputs and zero SQL injection.
- **HOW**:
  - `getNextTokenSequence(Connection conn, int doctorId, String date)`: Queries `SELECT COALESCE(MAX(token_sequence), 0) + 1 FROM visits WHERE doctor_id = ? AND visit_date = ?`.
  - `insert(Connection conn, Visit visit)`: Inserts a new visit row with initial `WAITING` status.
  - `hasActiveConsultation(int doctorId, String date)`: Checks if `COUNT(*) > 0` for `status = 'IN_CONSULTATION'`.
  - `findOldestWaitingVisit(int doctorId, String date)`: Selects the oldest waiting patient via `ORDER BY v.created_at ASC, v.id ASC LIMIT 1`.
  - `updateStatus(int visitId, VisitStatus newStatus)`: Updates `status` and `updated_at`.
  - `searchVisits(date, doctorId, status, queryText)`: Dynamically appends `AND` clauses with prepared statement parameters to filter by doctor, status, and patient name / token wildcard.

---

## 5. Service Layer (Business Rules): `in.clinicqueue.service`

### `TokenService.java`
- **WHAT**: Formats standardized token strings.
- **WHY**: Ensures consistent display tokens across all screens.
- **HOW**: `formatToken(doctorId, date, sequence)` returns `String.format("D%02d-%s-%03d", doctorId, date, sequence)` (e.g. `D01-2026-10-20-001`).

### `VisitService.java`
- **WHAT**: Handles patient registration, input validation, and atomic token generation.
- **WHY**: Prevents invalid data and guarantees ACID transactional integrity (FR-01, FR-02, FR-03, FR-15).
- **HOW**:
  - `registerVisit(name, phone, doctorId, visitType)`:
    1. Checks if `name` is null or blank $\rightarrow$ throws `IllegalArgumentException`.
    2. Checks if `phone` is valid if provided.
    3. Checks if `doctorId > 0` and `visitType != null`.
    4. Opens a connection and turns off auto-commit: `conn.setAutoCommit(false)`.
    5. Inserts patient $\rightarrow$ retrieves `patientId`.
    6. Gets `sequence` $\rightarrow$ formats `tokenNumber`.
    7. Inserts visit record $\rightarrow$ retrieves `visitId`.
    8. Calls `conn.commit()`.
    9. In case of any error, calls `conn.rollback()` and rethrows the exception.

### `QueueService.java`
- **WHAT**: Manages queue movement, the state machine, and wait time calculations.
- **WHY**: Enforces all business rules and safety invariants (FR-06, FR-07, FR-08, FR-11).
- **HOW**:
  - `callNext(doctorId, date)`:
    1. Checks `hasActiveConsultation(doctorId, date)`. If true, rejects with `IllegalStateException("Doctor already has an active patient in consultation")`.
    2. Finds oldest waiting visit. If none, rejects with `"No waiting patients in queue"`.
    3. Updates status to `IN_CONSULTATION`.
  - `completeConsultation(visitId)`:
    1. Checks if current status is `IN_CONSULTATION`. If not, rejects.
    2. Updates status to `COMPLETED`.
  - `cancelVisit(visitId)`:
    1. Checks if current status is `WAITING`. If not, rejects.
    2. Updates status to `CANCELLED`.
  - `calculateWaitTimes(List<Visit> visits)`:
    - Counts how many `WAITING` patients are ahead in line.
    - Sets `waitingPosition = count + 1`.
    - Sets `estimatedWaitMinutes = count * 10`.

### `DashboardService.java`
- **WHAT**: Calculates live queue metrics for the dashboard.
- **WHY**: Provides high-level operational visibility without requiring raw SQL queries in UI controllers.
- **HOW**:
  - `getDashboardSummary(date)`:
    1. Fetches all active doctors and today's visits.
    2. Tally counts for Total, Waiting, In Consultation, and Completed visits.
    3. For each doctor, calculates their waiting queue size and finds their active consultation patient (if any).
    4. Returns a `DashboardSummary` DTO.

---

## 6. Presentation Layer: `in.clinicqueue.view`

### `MainLayoutView.java`
- **WHAT**: Top-level UI layout containing the header, navigation buttons, active content view, and bottom status bar.
- **WHY**: Provides a seamless single-window desktop experience with instantaneous view switching.
- **HOW**:
  - `showDashboardView()`: Refreshes and displays `DashboardView`.
  - `showRegisterView()`: Resets and displays `RegisterVisitView`.
  - `showQueueView()`: Refreshes and displays `QueueBoardView`.
  - `handleResetDemoData()`: Displays a confirmation alert and executes `SchemaInitializer.resetAndReseedDemoData()`.

### `DashboardView.java`
- **WHAT**: Shows 4 metric cards (Total, Waiting, In Consultation, Completed) and a grid of doctor status cards.
- **HOW**: Built using `GridPane` and `FlowPane`. Binds values from `DashboardService.getDashboardSummary()`.

### `RegisterVisitView.java`
- **WHAT**: Form with Patient Name, Phone, Department, Doctor, and Visit Type controls.
- **HOW**: On submit, calls `VisitService.registerVisit()`. Upon success, hides the form and displays a prominent card with the issued token number.

### `QueueBoardView.java`
- **WHAT**: The doctor queue table with filter controls (Doctor dropdown, Status filter, Search box) and action buttons (Call Next, Complete, Cancel).
- **HOW**:
  - Table columns display Token, Patient Name, Phone, Doctor, Type, Queue Position, Approx. Wait, Status Chip, and Registration Time.
  - Action buttons call `QueueService` methods and display feedback alerts.
