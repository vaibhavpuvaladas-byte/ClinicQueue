# ClinicQueue — Hospital Queue Management System (MVP)

A reliable, presentation-ready JavaFX desktop queue management prototype designed for hospital outpatient departments and classroom demonstrations.

---

## 🏥 Product Overview

**ClinicQueue** streamlines the outpatient intake and queue flow:
1. **Intake & Token Generation:** Register a patient, select department and doctor, and issue an atomic daily sequential token (`D{id}-{YYYY-MM-DD}-{seq}`).
2. **Doctor-Specific Queue Management:** Doctors call the next waiting patient in strict FIFO order, transition them to `IN_CONSULTATION`, and mark them `COMPLETED`.
3. **Queue State Guarding:** Restricts illegal transitions, enforces a single active consultation per doctor, and allows cancellations strictly from `WAITING`.
4. **Live Operational Dashboard:** Real-time visibility into counts across all four statuses (`WAITING`, `IN_CONSULTATION`, `COMPLETED`, `CANCELLED`) and doctor load.
5. **Wait Time Estimation:** Displays a transparent approximate wait time based on queue position and average consultation duration.
6. **Persistence & Demo Reset:** Fully offline SQLite persistence across restarts, with an idempotent one-click demo dataset reset.

---

## 🛠 Technology Stack

* **Language:** Java 21 LTS
* **Desktop UI:** JavaFX 21 (code-built with responsive CSS styling)
* **Build System:** Apache Maven 3.9+ with bundled `mvnw.cmd` / `mvnw`
* **Persistence:** SQLite local database (`clinicqueue.db`) via standard JDBC
* **Testing:** JUnit 5 (Jupiter) for business services, state transitions, and token allocation

---

## 🚀 Getting Started

### Prerequisites
* Java Development Kit (JDK 21 or later) installed.
* No internet connection required after Maven dependencies are cached.

### Launching the Application

Using the bundled Maven wrapper:

**On Windows (PowerShell / Command Prompt):**
```bash
.\mvnw.cmd clean javafx:run
```

**On Linux / macOS:**
```bash
./mvnw clean javafx:run
```

If global Maven is installed on your PATH:
```bash
mvn clean javafx:run
```

### Running Automated Tests
```bash
.\mvnw.cmd test
```

---

## 📁 Project Architecture

```
ClinicQueue/
├── pom.xml                                 # Maven dependencies & plugins
├── mvnw.cmd / mvnw                         # Cross-platform Maven wrapper scripts
├── docs/                                   # Architecture, Demo Script, and Test Plan
│   ├── ARCHITECTURE.md
│   ├── DEMO_SCRIPT.md
│   └── TEST_PLAN.md
├── src/main/java/in/clinicqueue/
│   ├── Main.java                           # JavaFX bootstrap launcher
│   ├── ClinicQueueApp.java                 # Main JavaFX application shell
│   ├── view/                               # JavaFX UI screens
│   │   ├── DashboardView.java              # Live counts & summary cards
│   │   ├── RegisterVisitView.java          # Patient intake form
│   │   └── QueueBoardView.java             # Doctor queue management console
│   ├── controller/                         # UI controllers & event coordination
│   ├── service/                            # Core business logic & transactions
│   ├── repository/                         # Pure JDBC data access
│   ├── database/                           # SQLite connection & schema migrations
│   └── model/                              # Entities and Enums
└── src/main/resources/in/clinicqueue/
    └── css/
        └── app.css                         # Clinical navy/blue UI design system
```

---

## ⚖️ Scope & Non-Goals

* **Fictional Data Only:** Designed for single-computer demonstration using fictional patient records.
* **No Clinical Scope:** Not intended for medical prescriptions, diagnostic decision support, or electronic health records (EHR).
* **No Authentication:** Doctors and departments are pre-seeded; no login screens or role-based auth required in MVP.
* **Illustrative Wait Time:** Approximate wait times are computed linearly ($\text{position} \times 10\text{ min}$) and clearly labeled as estimates.
