# ClinicQueue MVP — 3-Minute Live Presentation Script

Use this exact script during your project presentation or viva demo. It guides you step-by-step through the application, with **exact click instructions** and **what to say to the evaluator**.

---

## ⏱️ Presentation Timeline & Steps

### 1. Introduction & Dashboard Overview (0:00 – 0:25)
* **What to click**: App launches on the **Dashboard** view.
* **What to say**:
  > *"Good morning/afternoon evaluators. This is ClinicQueue, an outpatient clinic queue management system built in Java and JavaFX with an embedded SQLite database.
  > On our dashboard, receptionists and clinic managers get a real-time operational overview of today's clinic.
  > At the top, we see the total visits registered today, the number of patients currently waiting, active consultations, and completed visits.
  > Below, each consulting doctor has a live status card showing how many patients are queued up for them and who is currently in consultation."*
* **What it proves**: Operational overview, real-time dashboard calculations, clean clinic UI design.

---

### 2. Patient Registration & Token Allocation (0:25 – 1:00)
* **What to click**:
  1. Click **"➕ Register Patient"** in the top navigation bar.
  2. Enter Patient Full Name: `Aarav Demo`.
  3. (Optional) Enter Phone: `9876543210`.
  4. Select Department: `General Medicine`.
  5. Select Doctor: `Dr. Sarah Sharma`.
  6. Leave Visit Type as `Walk-In`.
  7. Click **"Register & Generate Token"**.
* **What to say**:
  > *"Now let's simulate a patient arriving at the clinic reception desk. 
  > I enter the patient's name 'Aarav Demo' and select Dr. Sarah Sharma in General Medicine.
  > Notice that our input is strictly validated: blank names are rejected.
  > When I click 'Register & Generate Token', the system runs an atomic database transaction in SQLite. It creates the patient and generates a sequential daily token specific to Dr. Sharma.
  > Here is the generated token card. Notice the format: D01 represents Doctor 1, followed by today's date and a sequential 3-digit sequence."*
* **What it proves**: Input validation, atomic database transactions, doctor-specific sequential token allocation.

---

### 3. Queue Board & "Call Next" State Machine (1:00 – 1:40)
* **What to click**:
  1. Click **"Go to Queue Board"** (or click **"📋 Doctor Queue Board"** in the navigation bar).
  2. Select Doctor dropdown: `Dr. Sarah Sharma`.
  3. Notice the waiting patients in line.
  4. Notice whether Dr. Sharma already has someone in consultation.
     - *If she has an active consultation*: Click **"📞 Call Next Patient"** to demonstrate the safety check! An alert appears: *"Doctor already has an active patient in consultation. Complete the ongoing visit first."*
     - Point this out to the evaluator!
* **What to say**:
  > *"Next, we move to the Doctor's Queue Board. Here, doctors or room nurses manage their queue.
  > A key safety rule in our state machine is that a doctor cannot attend to two patients simultaneously.
  > If I attempt to call the next patient while another consultation is already ongoing, the system blocks the action and displays an informative alert.
  > This enforces our queue state machine invariants."*
* **What it proves**: State machine enforcement, single-consultation constraint (FR-08), FIFO queue ordering.

---

### 4. Completing Consultation & Cross-Screen Consistency (1:40 – 2:10)
* **What to click**:
  1. Select the `In Consultation` row in the table.
  2. Click **"✅ Complete Consultation"**.
  3. The status immediately updates to `Completed`.
  4. Now click **"📞 Call Next Patient"**.
  5. The oldest waiting patient is now promoted to `In Consultation`.
  6. Click **"📊 Dashboard"** in the top navigation bar.
* **What to say**:
  > *"Now that Dr. Sharma has finished with the patient, we select the visit and click 'Complete Consultation'. The state transitions to COMPLETED.
  > Now that the doctor is free, clicking 'Call Next' picks the oldest waiting patient in strict FIFO order and transitions them to IN_CONSULTATION.
  > When we navigate back to the Dashboard, all counts and doctor cards reflect the updated status immediately."*
* **What it proves**: Valid status transitions (FR-07), cross-screen data synchronization.

---

### 5. Search, Filter & Approximate Wait Time (2:10 – 2:35)
* **What to click**:
  1. Return to **"📋 Doctor Queue Board"**.
  2. In the Search field, type `Aarav` or type the token number.
  3. Clear the search field.
  4. Point out the **"Queue Pos."** and **"Approx. Wait"** columns.
* **What to say**:
  > *"Receptionists can quickly locate any patient using the real-time search box, matching by name or token number.
  > For waiting patients, ClinicQueue calculates an estimated wait time.
  > As you can see, the first waiting patient has 0 ahead (0 mins), the second has 1 ahead (approx. 10 mins), and so on.
  > We explicitly label this as APPROXIMATE based on a transparent 10-minute outpatient average, avoiding misleading AI claims."*
* **What it proves**: Search usability, queue position calculation, transparent wait approximation (FR-11).

---

### 6. Persistence & Demo Reset (2:35 – 3:00)
* **What to click**:
  1. Mention SQLite persistence (`clinicqueue.db`).
  2. Click **"🔄 Reset Demo Data"** in the top right.
  3. When the confirmation prompt appears, click **OK**.
* **What to say**:
  > *"All records are saved directly to local SQLite database file clinicqueue.db. If we close and relaunch the application right now, every patient and token is fully preserved.
  > Finally, for classroom and demo purposes, we have a confirmed 'Reset Demo Data' button. It cleans test records and re-seeds the sample clinic data with one click.
  > In conclusion, ClinicQueue is fully tested with JUnit 5, runs 100% offline, and strictly adheres to all requirements in the build specification.
  > We are now ready for your questions."*
* **What it proves**: SQLite persistence across restarts, safe confirmed reset/reseed (FR-13), confident presentation close.
