# ClinicQueue MVP — System Architecture Document

This document outlines the **Architecture, Design Decisions, Data Flow, and Database Schema** for the ClinicQueue MVP desktop application. Every section follows the **WHAT, WHY, HOW** framework.

---

## 1. Architectural Style & Layering

The application strictly follows a **Layered Separation-of-Concerns (SoC)** architecture.

```
┌────────────────────────────────────────────────────────┐
│               JavaFX Presentation Layer                │
│    (DashboardView, RegisterVisitView, QueueBoardView)  │
└───────────────────────────┬────────────────────────────┘
                            │ (Events & UI binding)
                            ▼
┌────────────────────────────────────────────────────────┐
│                   Controllers Layer                    │
│    (DashboardController, RegisterController, etc.)     │
└───────────────────────────┬────────────────────────────┘
                            │ (Coordinates requests)
                            ▼
┌────────────────────────────────────────────────────────┐
│                 Business Services Layer                │
│  (VisitService, QueueService, TokenService, Analytics) │
└───────────────────────────┬────────────────────────────┘
                            │ (Validation, Rules, Transactions)
                            ▼
┌────────────────────────────────────────────────────────┐
│                Repositories Layer (JDBC)               │
│ (PatientRepository, VisitRepository, DoctorRepository) │
└───────────────────────────┬────────────────────────────┘
                            │ (PreparedStatements, SQL)
                            ▼
┌────────────────────────────────────────────────────────┐
│           SQLite Database Engine (clinicqueue.db)      │
│            PRAGMA foreign_keys = ON; ACID Locks        │
└────────────────────────────────────────────────────────┘
```

### Layer Breakdown (What, Why, How)

#### 1. Presentation Layer (JavaFX Views)
- **WHAT**: JavaFX layout controls (`BorderPane`, `TableView`, `VBox`, `HBox`, `Label`, `Button`) styled with modern CSS (`styles.css`).
- **WHY**: Isolates UI visuals from logic. If UI controls change or resize, business rules are not impacted.
- **HOW**: Built either via code or FXML, binding data models to observable collections (`ObservableList`).

#### 2. Controller Layer
- **WHAT**: Mediators between UI widgets and Business Services.
- **WHY**: Keeps UI handlers thin. Controllers never execute SQL, never calculate wait times, and never directly modify database records.
- **HOW**: Captures user events (button clicks, dropdown selections), gathers form inputs, calls corresponding service methods, and updates UI state based on responses.

#### 3. Service Layer (Business Logic & Transactions)
- **WHAT**: The core engine of ClinicQueue containing domain logic:
  - `VisitService`: Manages patient registration and atomic visit creation.
  - `QueueService`: Manages queue retrieval, state transitions, "Call Next" rules, and cancellations.
  - `TokenService`: Daily sequential token generation (`D01-YYYY-MM-DD-001`).
  - `DashboardService`: Aggregates statistics and calculates approximate wait times.
- **WHY**: Centralizes all business rules in one place so they can be independently unit-tested without needing JavaFX.
- **HOW**: Validates business invariants (e.g., patient name not blank, single active consultation per doctor), opens JDBC transactions, calls repositories, and commits or rolls back.

#### 4. Repository Layer (Data Access via JDBC)
- **WHAT**: Data access objects (`PatientRepository`, `VisitRepository`, `DoctorRepository`, `DepartmentRepository`).
- **WHY**: Completely shields the domain model and services from SQL syntax, SQLite nuances, and database driver details.
- **HOW**: Executes parameterized SQL queries using `java.sql.PreparedStatement`, maps `ResultSet` rows into domain entities, and handles SQL exceptions.

---

## 2. Database Schema & Integrity

The database is an embedded SQLite file named `clinicqueue.db`. Foreign keys are enabled on every connection using `PRAGMA foreign_keys = ON;`.

### Entity Relationship Diagram (ERD)

```
┌──────────────────┐             ┌─────────────────────────┐
│   departments    │             │         doctors         │
├──────────────────┤             ├─────────────────────────┤
│ id (PK)          │1           *│ id (PK)                 │
│ name (UNIQUE)    ├────────────►│ department_id (FK)      │
│ active           │             │ display_name            │
└──────────────────┘             │ active                  │
                                 └───────────┬─────────────┘
                                             │ 1
                                             │
                                             │ *
┌──────────────────┐             ┌───────────▼─────────────┐
│     patients     │             │         visits          │
├──────────────────┤             ├─────────────────────────┤
│ id (PK)          │1           *│ id (PK)                 │
│ full_name        ├────────────►│ patient_id (FK)         │
│ phone            │             │ doctor_id (FK)          │
│ created_at       │             │ visit_date (YYYY-MM-DD) │
└──────────────────┘             │ visit_type (APPT/WALK)  │
                                 │ token_sequence (INTEGER)│
                                 │ token_number (TEXT)     │
                                 │ status (WAIT/IN_CONS/..)│
                                 │ created_at (TIMESTAMP)  │
                                 │ updated_at (TIMESTAMP)  │
                                 └─────────────────────────┘
```

### Table Definitions & Constraints

```sql
CREATE TABLE IF NOT EXISTS departments (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    active INTEGER NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS doctors (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    department_id INTEGER NOT NULL,
    display_name TEXT NOT NULL,
    active INTEGER NOT NULL DEFAULT 1,
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS patients (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    full_name TEXT NOT NULL,
    phone TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime'))
);

CREATE TABLE IF NOT EXISTS visits (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    patient_id INTEGER NOT NULL,
    doctor_id INTEGER NOT NULL,
    visit_date TEXT NOT NULL,
    visit_type TEXT NOT NULL CHECK(visit_type IN ('APPOINTMENT', 'WALK_IN')),
    token_sequence INTEGER NOT NULL,
    token_number TEXT NOT NULL,
    status TEXT NOT NULL CHECK(status IN ('WAITING', 'IN_CONSULTATION', 'COMPLETED', 'CANCELLED')),
    created_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES doctors(id) ON DELETE RESTRICT,
    UNIQUE (doctor_id, visit_date, token_sequence)
);

CREATE INDEX IF NOT EXISTS idx_visits_doctor_date_status 
ON visits(doctor_id, visit_date, status);
```

---

## 3. Transaction Boundary & Token Sequence

### Atomic Registration Flow
- **WHAT**: When a receptionist registers a visit, creating the patient and creating the visit/token must succeed together or fail together.
- **WHY**: If patient creation succeeds but token allocation fails, orphaned patient records clutter the database.
- **HOW**:
  ```java
  connection.setAutoCommit(false);
  try {
      long patientId = patientRepo.insert(connection, patient);
      int nextSeq = visitRepo.getNextTokenSequence(connection, doctorId, date);
      String token = String.format("D%02d-%s-%03d", doctorId, date, nextSeq);
      visitRepo.insert(connection, new Visit(patientId, doctorId, date, token, nextSeq, ...));
      connection.commit();
  } catch (Exception e) {
      connection.rollback();
      throw e;
  }
  ```

---

## 4. State Machine Transition Matrix

| Current State | Target State | Trigger Action | Allowed? | Business Preconditions |
| :--- | :--- | :--- | :--- | :--- |
| `WAITING` | `IN_CONSULTATION` | Call Next | **YES** | Doctor must NOT have any other patient currently in `IN_CONSULTATION`. |
| `IN_CONSULTATION` | `COMPLETED` | Complete | **YES** | None. Marks consultation finished. |
| `WAITING` | `CANCELLED` | Cancel | **YES** | Requires user confirmation prompt. Excluded from queue. |
| `IN_CONSULTATION` | `CANCELLED` | Cancel | **NO** | Cannot cancel once consultation has begun. |
| `WAITING` | `COMPLETED` | Complete | **NO** | Must enter consultation before being marked completed. |
| `COMPLETED` | *Any* | Any | **NO** | Terminal state; records are archived/immutable. |
| `CANCELLED` | *Any* | Any | **NO** | Terminal state; records are archived/immutable. |

---

## 5. Wait Time Calculation Formula
- **Formula**:
  $$\text{Wait Time} = \text{Count of WAITING patients ahead for Doctor } D \times \text{Average Minutes (10)}$$
- **Assumption Display**: The UI always displays `Approximate (10 min/patient avg)`.
- **Exclusions**: Any visits with status `CANCELLED` or `COMPLETED` are excluded from the queue count.
