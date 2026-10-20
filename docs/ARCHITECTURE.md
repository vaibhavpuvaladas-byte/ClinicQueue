# ClinicQueue MVP — Architecture & Design Specification

## 1. Architectural Philosophy & Layer Separation

The application strictly adheres to a clean 3-tier desktop architectural pattern:

```
┌────────────────────────────────────────────────────────┐
│                      JavaFX Views                      │
│        (DashboardView, RegisterVisitView, QueueBoard)  │
└───────────────────────────▲────────────────────────────┘
                            │ (Events & Observables)
┌───────────────────────────▼────────────────────────────┐
│                    UI Controllers                      │
│        (UI coordination, thin event listeners)         │
└───────────────────────────▲────────────────────────────┘
                            │ (DTOs / Domain Models)
┌───────────────────────────▼────────────────────────────┐
│                  Business Services                     │
│  (VisitService, QueueService, TokenService, Dashboard) │
└───────────────────────────▲────────────────────────────┘
                            │ (Prepared Statements / Transactions)
┌───────────────────────────▼────────────────────────────┐
│                     Repositories                       │
│    (PatientRepo, VisitRepo, DoctorRepo, DepartmentRepo)│
└───────────────────────────▲────────────────────────────┘
                            │ (JDBC)
┌───────────────────────────▼────────────────────────────┐
│              SQLite Database (clinicqueue.db)          │
└────────────────────────────────────────────────────────┘
```

### Architectural Guardrails:
1. **Controllers must not execute SQL directly:** All persistence is mediated via repositories.
2. **Repositories must not contain UI code:** Repositories handle purely JDBC operations, prepared statements, and result mapping.
3. **Services define transaction boundaries:** Multi-table operations (such as allocating a sequential token and inserting a visit) execute within an atomic SQLite transaction.
4. **Foreign Keys Enabled:** Every SQLite connection executes `PRAGMA foreign_keys = ON;` to guarantee relational integrity.
5. **Thread Safety:** Database queries do not freeze the JavaFX Application Thread.

---

## 2. Data Model & Constraints

### Tables:
* **`departments`**: `id` (PK), `name` (UNIQUE), `active` (BOOLEAN)
* **`doctors`**: `id` (PK), `department_id` (FK), `display_name` (TEXT), `active` (BOOLEAN)
* **`patients`**: `id` (PK), `full_name` (TEXT NOT NULL), `phone` (TEXT NULL), `created_at` (TIMESTAMP)
* **`visits`**: 
  * `id` (PK)
  * `patient_id` (FK NOT NULL)
  * `doctor_id` (FK NOT NULL)
  * `visit_date` (TEXT NOT NULL, `YYYY-MM-DD`)
  * `visit_type` (TEXT NOT NULL, `APPOINTMENT` | `WALK_IN`)
  * `token_number` (INTEGER NOT NULL)
  * `token_display` (TEXT NOT NULL, `D01-YYYY-MM-DD-001`)
  * `status` (TEXT NOT NULL, `WAITING` | `IN_CONSULTATION` | `COMPLETED` | `CANCELLED`)
  * `created_at` (TIMESTAMP NOT NULL)
  * `updated_at` (TIMESTAMP NOT NULL)
  * **Constraint:** `UNIQUE(doctor_id, visit_date, token_number)`

---

## 3. Queue State Machine

```
         ┌───────────────┐
         │    WAITING    │◄────────── Visit Registration
         └───┬───────┬───┘
             │       │
   Call Next │       │ Cancel Visit
             ▼       ▼
┌──────────────────┐ ┌───────────────┐
│ IN_CONSULTATION  │ │   CANCELLED   │
└────────┬─────────┘ └───────────────┘
         │
Complete │
         ▼
 ┌───────────────┐
 │   COMPLETED   │
 └───────────────┘
```

### State Machine Invariants:
* A doctor can have at most **one** patient in `IN_CONSULTATION` status at any given time.
* Calling next patient picks the oldest `WAITING` record ordered by `created_at ASC, id ASC`.
* `CANCELLED` is allowed solely from `WAITING`.
* `COMPLETED` is allowed solely from `IN_CONSULTATION`.
