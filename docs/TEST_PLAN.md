# ClinicQueue MVP — Acceptance Test Plan

This document details the test matrix and verification criteria based on the build specification.

## Automated JUnit 5 Test Suite

| Test Suite | Component Under Test | Acceptance Criterion Verified |
| :--- | :--- | :--- |
| **TokenServiceTest** | `TokenService` | Daily sequential numbering per doctor starts at `001`; distinct doctors have independent sequences; date change resets sequence. |
| **VisitServiceTest** | `VisitService` | Rejects blank or whitespace patient names; saves valid visit transactionally with atomic token allocation; persists required fields. |
| **QueueServiceTest** | `QueueService` | "Call Next" selects the oldest `WAITING` patient by `created_at` then `id`; blocks calling next if doctor already has an `IN_CONSULTATION` patient. |
| **StateTransitionTest**| `QueueService` | `IN_CONSULTATION` $\rightarrow$ `COMPLETED` succeeds; `WAITING` $\rightarrow$ `CANCELLED` succeeds; illegal transitions (`COMPLETED` $\rightarrow$ `CANCELLED`, `WAITING` $\rightarrow$ `COMPLETED`) fail with explicit exception. |
| **WaitEstimateTest** | `DashboardService` | Correctly computes wait time as $\text{position} \times \text{averageDuration}$ excluding cancelled and completed records. |
| **PersistenceTest** | SQLite Repositories | Reconnect preserves all tables, foreign keys, and seeded rows without duplicates (idempotent initialization). |

---

## Manual Verification Checklist

- [x] Application launches cleanly via `.\mvnw.cmd clean javafx:run`
- [x] Navigation switching between Dashboard, Register Visit, and Queue Board works without UI flickering
- [x] Resizing window from 1024×600 up to full HD does not clip controls or buttons
- [x] Theme colors match the specified clinical navy/slate palette
- [x] No unhandled exceptions or stack traces appear in console or UI
