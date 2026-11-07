# ClinicQueue — Hospital Outpatient Queue Management System (MVP)

> A polished, presentation-ready JavaFX desktop queue management prototype designed for an outpatient clinic demonstration.

---

## 🌟 Overview & Key Features

ClinicQueue solves the chaotic patient waiting room problem with a clean, fast, offline-capable desktop application:
1. **Patient Registration**: Register patients in under a minute with real-time validation (blank names rejected, optional phone).
2. **Doctor-Specific Sequential Daily Tokens**: Automatically assigns structured tokens formatted as `D{doctorId}-{YYYY-MM-DD}-{sequence}` (e.g. `D01-2026-10-20-001`).
3. **Queue State Machine**:
   - `WAITING` $\rightarrow$ `IN_CONSULTATION` (via "Call Next" picking oldest waiting patient).
   - `IN_CONSULTATION` $\rightarrow$ `COMPLETED`.
   - `WAITING` $\rightarrow$ `CANCELLED` (requires user confirmation).
   - **Constraint**: Prevents a doctor from calling another patient while one is already in consultation.
4. **Live Operational Dashboard**: Real-time counts for Total, Waiting, In Consultation, and Completed visits, with per-doctor live status cards.
5. **Transparent Wait Estimation**: Displays queue position and estimated wait ($\text{waiting position} \times 10\text{ min average}$), clearly labeled *Approximate*.
6. **Persistence**: Saves records locally in SQLite (`clinicqueue.db`) across restarts.
7. **One-Click Demo Reset**: Confirmed reset and re-seed action to restore sample data for presentations.

---

## 🛠️ Technology Stack

- **Language**: Java 17 / 21+ (Tested on Java 17 LTS and Java 25 LTS)
- **UI Framework**: JavaFX 21 (Desktop interface, CSS styled)
- **Database**: SQLite (`clinicqueue.db`) with `PRAGMA foreign_keys = ON;`
- **Data Access**: JDBC with `PreparedStatement` (Zero SQL injection, ACID transactions)
- **Testing**: JUnit 5 (Headless automated tests)
- **Build Tool**: Maven / Maven Wrapper (`.\mvnw.cmd`)

---

## 🚀 How to Run the Application

The project includes an embedded Maven wrapper, so no manual Maven installation is needed!

### 1. Launch the Desktop Application
Open a terminal in the project directory and run:
```powershell
.\mvnw.cmd javafx:run
```
*(Or if you have global Maven installed: `mvn javafx:run`)*

### 2. Run Automated Unit Tests
To execute all JUnit 5 tests:
```powershell
.\mvnw.cmd test
```

---

## 📁 Project Structure

```
ClinicQueue/
├── pom.xml                               # Maven build configuration
├── README.md                             # Main documentation
├── mvnw.cmd / .mvn/                      # Bundled Maven wrapper
├── docs/                                 # Complete documentation guides
│   ├── ARCHITECTURE.md                   # System design & SQL schema (What, Why, How)
│   ├── STEP_BY_STEP_IMPLEMENTATION.md    # Phase-by-phase implementation log
│   ├── CODE_EXPLANATION.md               # Code breakdown (What, Why, How)
│   ├── EVALUATOR_QUESTIONS_AND_ANSWERS.md# Viva & Evaluator defense Q&A
│   ├── DEMO_SCRIPT.md                    # 3-minute live presentation guide
│   └── TEST_PLAN.md                      # Acceptance test matrix (FR-01 to FR-15)
└── src/
    ├── main/java/in/clinicqueue/
    │   ├── ClinicQueueApp.java           # JavaFX entry point
    │   ├── model/                        # Domain entities & DTOs
    │   ├── database/                     # Database connection & schema setup
    │   ├── repository/                   # JDBC repositories
    │   ├── service/                      # Business logic & state machine
    │   └── view/                         # JavaFX UI views
    ├── main/resources/in/clinicqueue/
    │   └── css/styles.css                # Professional navy clinic styling
    └── test/java/in/clinicqueue/
        └── service/                      # JUnit 5 test suites
```

---

## 🎯 3-Minute Live Presentation Summary

1. **Dashboard (0:00 - 0:30)**: Point out total visits, waiting counts, and doctor status cards.
2. **Register Patient (0:30 - 1:00)**: Register `"Aarav Demo"` under Dr. Sarah Sharma; show the instant sequential token.
3. **Doctor Queue Board (1:00 - 1:40)**: Select Dr. Sarah Sharma; click **"Call Next"**; show status change to `In Consultation`.
4. **Complete Consultation (1:40 - 2:10)**: Select the patient and click **"Complete Consultation"**; observe dashboard counts update.
5. **Search & Wait Estimate (2:10 - 2:40)**: Search by name/token and demonstrate approximate wait time calculation.
6. **Persistence & Reset (2:40 - 3:00)**: Restart the application to demonstrate persistent SQLite storage, or demonstrate the **"Reset Demo Data"** button.

*(See [`docs/DEMO_SCRIPT.md`](docs/DEMO_SCRIPT.md) for full presenter script).*
