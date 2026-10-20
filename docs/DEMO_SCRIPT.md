# ClinicQueue MVP — 3-Minute Live Presentation Script

This script outlines the exact timing, actions, and talking points for a live classroom presentation.

| Elapsed Time | Presenter Action | Talking Points | Proves To Audience |
| :--- | :--- | :--- | :--- |
| **0:00 – 0:25** | Open application to **Dashboard** | "Welcome to ClinicQueue. Here is today's live outpatient overview. We have four real-time queue states and active doctor workload summaries." | Operational overview, real-time counters, clean UI. |
| **0:25 – 1:00** | Navigate to **Register Visit**; enter "Aarav Demo", select Cardiology / Dr. Sarah Patel, select Walk-in, click Register. | "A new patient arrives at the front desk. The system validates the input and issues daily sequential token D01-2026-10-20-001 in an atomic transaction." | Input validation, unique token generator, transactional consistency. |
| **1:00 – 1:40** | Navigate to **Queue Board**; select Dr. Sarah Patel; click **Call Next Patient**. | "Dr. Patel calls the next patient. The oldest waiting patient is selected (FIFO), status updates to `IN_CONSULTATION`, and a second call is guarded." | Queue selection rules, concurrency guard, state transition. |
| **1:40 – 2:10** | Click **Complete Consultation**; check Dashboard counters. | "Consultation concludes. The status transitions to `COMPLETED`. Notice the dashboard counters immediately reflect 1 completed and decremented waiting queue." | Cross-screen state consistency, valid completion lifecycle. |
| **2:10 – 2:35** | Type token / patient name in search; show approximate wait time banner. | "Receptionists can instantly locate patients by name or token. The approximate wait is computed using an honest linear formula (~10 min/patient ahead)." | Search usability, transparent estimation without fake AI. |
| **2:35 – 3:00** | Close and relaunch application. Point out records remain intact. | "We restart the app: all patient records, visits, and doctor statuses persist in SQLite across restarts. Future roadmap includes SMS and appointment slotting." | True offline database persistence, project robustness. |
