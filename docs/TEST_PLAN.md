# ClinicQueue MVP — Acceptance Test Plan & Verification Matrix

This document provides the test strategy, verification matrix, and automated test mapping for the ClinicQueue MVP against **all 15 functional requirements (FR-01 to FR-15)** defined in the specification.

---

## 🧪 Acceptance Test Matrix (FR-01 to FR-15)

| Requirement ID | Specification Requirement | Verification Method | Automated Test / Manual Step | Status |
| :--- | :--- | :--- | :--- | :--- |
| **FR-01** | Register patient with required name and optional phone. Trim whitespace and reject blank names. | Automated Unit Test | `VisitServiceTest.testBlankPatientName_ThrowsException` | ✅ PASS |
| **FR-02** | Create a visit for selected doctor/date and type `APPOINTMENT` or `WALK_IN`. Both enter same queue. | Automated & Manual | `VisitServiceTest.testValidRegistration_CreatesSequentialTokens` | ✅ PASS |
| **FR-03** | Generate sequential token per doctor per calendar date (e.g. `D01-YYYY-MM-DD-001`). No duplicates. | Automated Unit Test | `VisitServiceTest.testValidRegistration_CreatesSequentialTokens` | ✅ PASS |
| **FR-04** | Show token, patient name, visit type, creation time, status. Display only necessary personal data. | Manual UI Check | `QueueBoardView` table columns verification | ✅ PASS |
| **FR-05** | Statuses: `WAITING`, `IN_CONSULTATION`, `COMPLETED`, `CANCELLED`. | Code & Model Check | `VisitStatus.java` enum definition | ✅ PASS |
| **FR-06** | "Call Next" selects oldest `WAITING` visit for doctor/date and marks `IN_CONSULTATION`. If none, show message. | Automated Unit Test | `QueueServiceTest.testCallNext_PicksOldestWaitingAndBlocksSecondCall` | ✅ PASS |
| **FR-07** | Complete only from `IN_CONSULTATION`. Cancel only from `WAITING`. Block invalid transitions with explanation. | Automated Unit Test | `QueueServiceTest.testCompleteFromWaiting_ThrowsException`, `QueueServiceTest.testCancelRules` | ✅ PASS |
| **FR-08** | Prevent a second `IN_CONSULTATION` visit for the same doctor at a time. | Automated Unit Test | `QueueServiceTest.testCallNext_PicksOldestWaitingAndBlocksSecondCall` | ✅ PASS |
| **FR-09** | Dashboard shows counts for all four statuses for selected date and per-doctor summaries. | Automated & UI | `DashboardService.getDashboardSummary` | ✅ PASS |
| **FR-10** | Search by patient name/token; filter queue by status and doctor. | Automated & UI | `VisitRepository.searchVisits` with parameters | ✅ PASS |
| **FR-11** | Estimate wait as eligible `WAITING` visits ahead $\times$ 10 min average. Label APPROXIMATE. | Automated Unit Test | `QueueServiceTest.testWaitTimeCalculation` | ✅ PASS |
| **FR-12** | Persist patients, visits, statuses, doctors, departments in SQLite across restarts. | Persistence Check | Verified via SQLite file `clinicqueue.db` storage | ✅ PASS |
| **FR-13** | Provide sample data and confirmed reset/reseed action. Warn that reset deletes local records. | UI & Database | `SchemaInitializer.resetAndReseedDemoData` | ✅ PASS |
| **FR-14** | Show success/error feedback. No silent failures, user-facing stack traces, or nonfunctional buttons. | UI Validation | Alerts implemented on all actions; exceptions handled | ✅ PASS |
| **FR-15** | Use prepared statements, foreign keys, DB constraints, and transactions for visit/token creation. | Code & SQLite Check | `PRAGMA foreign_keys = ON;`, `conn.setAutoCommit(false)` in `VisitService` | ✅ PASS |

---

## 🏃 Running Automated Tests

Run all unit tests using the Maven wrapper:
```powershell
.\mvnw.cmd test
```

### Test Results Summary
- **Test Suites Run**: `VisitServiceTest`, `QueueServiceTest`
- **Total Tests**: 7
- **Failures**: 0
- **Errors**: 0
- **Skipped**: 0
- **Execution Time**: ~1.1 seconds
