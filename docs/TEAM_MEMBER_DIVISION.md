# ClinicQueue MVP — 5-Member Team Role & Folder Division

Yeh guide aapke **5 team members** ke beech project ko **equally divide** karti hai. Har member ka apna **specific folder/files**, **bolne ka script**, aur **evaluator ke questions** assigned hain.

---

```
┌────────────────────────────────────────────────────────────────────────┐
│                        CLINICQUEUE TEAM ROSTER                         │
├──────────┬─────────────────────────────────────┬───────────────────────┤
│ Member 1 │ Database & Persistence Lead         │ database/ + repo/     │
│ Member 2 │ Registration & Token Engine Lead    │ service/ (Visit/Token)│
│ Member 3 │ Queue Engine & State Machine Lead   │ service/ (Queue)      │
│ Member 4 │ Analytics & Live Dashboard Lead     │ service/ (Dashboard)  │
│ Member 5 │ UI/UX & Desktop Application Lead    │ view/ + css/ + App    │
└──────────┴─────────────────────────────────────┴───────────────────────┘
```

---

## 👨‍💻 MEMBER 1: Database & Persistence Lead

### 📂 Assigned Files & Folders
- `src/main/java/in/clinicqueue/database/`
  - [`DatabaseManager.java`](file:///c:/Users/P%20Sai%20Vaibhav/OneDrive/Attachments/Desktop/ClinicQueue/src/main/java/in/clinicqueue/database/DatabaseManager.java)
  - [`SchemaInitializer.java`](file:///c:/Users/P%20Sai%20Vaibhav/OneDrive/Attachments/Desktop/ClinicQueue/src/main/java/in/clinicqueue/database/SchemaInitializer.java)
- `src/main/java/in/clinicqueue/repository/`
  - [`DepartmentRepository.java`](file:///c:/Users/P%20Sai%20Vaibhav/OneDrive/Attachments/Desktop/ClinicQueue/src/main/java/in/clinicqueue/repository/DepartmentRepository.java)
  - [`DoctorRepository.java`](file:///c:/Users/P%20Sai%20Vaibhav/OneDrive/Attachments/Desktop/ClinicQueue/src/main/java/in/clinicqueue/repository/DoctorRepository.java)
  - [`PatientRepository.java`](file:///c:/Users/P%20Sai%20Vaibhav/OneDrive/Attachments/Desktop/ClinicQueue/src/main/java/in/clinicqueue/repository/PatientRepository.java)
- Local Database file: `clinicqueue.db`

### 🗣️ Member 1 ka Speaking Script (1 Minute)
> *"Sir, maine ClinicQueue ka **Database & Persistence Layer** implement kiya hai.*
> 1. *Humne embedded **SQLite** use kiya hai jo 100% offline aur portable hai.*
> 2. *SQLite me foreign keys by default OFF hoti hain, isliye `DatabaseManager` me maine har connection par `PRAGMA foreign_keys = ON;` execute karwaya hai.*
> 3. *`SchemaInitializer` automatically startup par `CREATE TABLE IF NOT EXISTS` run karta hai aur default clinical specialties (General Medicine, Pediatrics) aur doctors seed karta hai.*
> 4. *Saari SQL queries `PreparedStatement` ke saath likhi hain, isliye application 100% SQL-injection proof hai."*

### ❓ Evaluator Questions for Member 1
1. **Q: Foreign keys kaise enable kiye?**
   - *Ans: `DatabaseManager.getConnection()` ke andar `stmt.execute("PRAGMA foreign_keys = ON;");` har connection pe execute hota hai.*
2. **Q: Agar app restart karein toh data rehta hai?**
   - *Ans: Haan Sir, SQLite `clinicqueue.db` file me saara data ACID-compliant tareeqe se persist hota hai.*
3. **Q: SQL Injection kaise prevent kiya?**
   - *Ans: Kisi bhi query me string concatenation nahi kiya, sab jagah `PreparedStatement` aur `?` parameters use kiye hain.*

---

## 👨‍💻 MEMBER 2: Patient Registration & Token Engine Lead

### 📂 Assigned Files & Folders
- `src/main/java/in/clinicqueue/model/`
  - [`Patient.java`](file:///c:/Users/P%20Sai%20Vaibhav/OneDrive/Attachments/Desktop/ClinicQueue/src/main/java/in/clinicqueue/model/Patient.java)
  - [`Department.java`](file:///c:/Users/P%20Sai%20Vaibhav/OneDrive/Attachments/Desktop/ClinicQueue/src/main/java/in/clinicqueue/model/Department.java)
  - [`Doctor.java`](file:///c:/Users/P%20Sai%20Vaibhav/OneDrive/Attachments/Desktop/ClinicQueue/src/main/java/in/clinicqueue/model/Doctor.java)
  - [`VisitType.java`](file:///c:/Users/P%20Sai%20Vaibhav/OneDrive/Attachments/Desktop/ClinicQueue/src/main/java/in/clinicqueue/model/VisitType.java)
- `src/main/java/in/clinicqueue/service/`
  - [`VisitService.java`](file:///c:/Users/P%20Sai%20Vaibhav/OneDrive/Attachments/Desktop/ClinicQueue/src/main/java/in/clinicqueue/service/VisitService.java)
  - [`TokenService.java`](file:///c:/Users/P%20Sai%20Vaibhav/OneDrive/Attachments/Desktop/ClinicQueue/src/main/java/in/clinicqueue/service/TokenService.java)
- `src/test/java/in/clinicqueue/service/`
  - [`VisitServiceTest.java`](file:///c:/Users/P%20Sai%20Vaibhav/OneDrive/Attachments/Desktop/ClinicQueue/src/test/java/in/clinicqueue/service/VisitServiceTest.java)

### 🗣️ Member 2 ka Speaking Script (1 Minute)
> *"Sir, maine **Patient Registration and Token Allocation Engine** develop kiya hai.*
> 1. *Registration me strict validation lagayi hai: blank ya whitespace names reject hote hain aur optional phone validate hota hai.*
> 2. *Token generation atomic transaction me hota hai: `conn.setAutoCommit(false)` shuru hota hai, patient insert hota hai, aur doctor ka aaj ka sequence number nikal kar token generate hota hai.*
> 3. *Token format standardized hai: `D01-YYYY-MM-DD-001`. Do alag doctors ka token numbering independent 001 se shuru hota hai.*
> 4. *Agar beech me koi bhi failure ho, toh transaction rollback ho jata hai taki adha data na bache."*

### ❓ Evaluator Questions for Member 2
1. **Q: Token duplicate hone se kaise roka?**
   - *Ans: SQLite me `UNIQUE(doctor_id, visit_date, token_sequence)` constraint hai aur generation transaction ke andar hoti hai.*
2. **Q: Appointment aur Walk-In dono ko ek hi queue me kyun rakha?**
   - *Ans: Day-of-visit clinic OPD me sabhi patients ek hi physical counter pe report karte hain. Dono queue me alag badge ke sath dikhte hain.*
3. **Q: Aapne automated tests kaise likhe?**
   - *Ans: `VisitServiceTest.java` me JUnit 5 se test kiya hai ki blank name reject hota hai aur sequential tokens bante hain.*

---

## 👨‍💻 MEMBER 3: Queue Engine & State Machine Lead

### 📂 Assigned Files & Folders
- `src/main/java/in/clinicqueue/model/`
  - [`VisitStatus.java`](file:///c:/Users/P%20Sai%20Vaibhav/OneDrive/Attachments/Desktop/ClinicQueue/src/main/java/in/clinicqueue/model/VisitStatus.java)
  - [`Visit.java`](file:///c:/Users/P%20Sai%20Vaibhav/OneDrive/Attachments/Desktop/ClinicQueue/src/main/java/in/clinicqueue/model/Visit.java)
- `src/main/java/in/clinicqueue/service/`
  - [`QueueService.java`](file:///c:/Users/P%20Sai%20Vaibhav/OneDrive/Attachments/Desktop/ClinicQueue/src/main/java/in/clinicqueue/service/QueueService.java)
- `src/main/java/in/clinicqueue/repository/`
  - [`VisitRepository.java`](file:///c:/Users/P%20Sai%20Vaibhav/OneDrive/Attachments/Desktop/ClinicQueue/src/main/java/in/clinicqueue/repository/VisitRepository.java) (Queue transition methods)
- `src/test/java/in/clinicqueue/service/`
  - [`QueueServiceTest.java`](file:///c:/Users/P%20Sai%20Vaibhav/OneDrive/Attachments/Desktop/ClinicQueue/src/test/java/in/clinicqueue/service/QueueServiceTest.java)

### 🗣️ Member 3 ka Speaking Script (1 Minute)
> *"Sir, maine **Queue State Machine aur 'Call Next' Engine** build kiya hai.*
> 1. *Queue me 4 official statuses hain: `WAITING`, `IN_CONSULTATION`, `COMPLETED`, aur `CANCELLED`.*
> 2. *Humne hospital safety constraint enforce kiya hai: ek doctor ek time par sirf ek patient dekh sakta hai. Agar koi patient already `IN_CONSULTATION` me hai, toh 'Call Next' block ho jata hai.*
> 3. *'Call Next' sabse purane waiting patient ko **FIFO order** (`created_at ASC, id ASC`) me uthata hai.*
> 4. *Sirf `IN_CONSULTATION` wala visit `COMPLETED` ho sakta hai, aur sirf `WAITING` wala visit `CANCELLED` ho sakta hai. Invalid transitions par system alert deta hai."*

### ❓ Evaluator Questions for Member 3
1. **Q: Agar doctor ke paas already patient hai aur Call Next dabayein toh kya hoga?**
   - *Ans: `IllegalStateException` throw hoti hai aur user ko clear alert milta hai ki pehle current patient complete karo.*
2. **Q: FIFO order kaise maintain kiya?**
   - *Ans: SQL query me `ORDER BY v.created_at ASC, v.id ASC LIMIT 1` use kiya hai.*
3. **Q: Wait time ka formula kya hai?**
   - *Ans: `Waiting Position Ahead * 10 minutes average`. Isko transparently APPROXIMATE label kiya gaya hai.*

---

## 👨‍💻 MEMBER 4: Analytics, Live Dashboard & Search Lead

### 📂 Assigned Files & Folders
- `src/main/java/in/clinicqueue/model/`
  - [`DashboardSummary.java`](file:///c:/Users/P%20Sai%20Vaibhav/OneDrive/Attachments/Desktop/ClinicQueue/src/main/java/in/clinicqueue/model/DashboardSummary.java)
  - [`DoctorQueueSummary.java`](file:///c:/Users/P%20Sai%20Vaibhav/OneDrive/Attachments/Desktop/ClinicQueue/src/main/java/in/clinicqueue/model/DoctorQueueSummary.java)
- `src/main/java/in/clinicqueue/service/`
  - [`DashboardService.java`](file:///c:/Users/P%20Sai%20Vaibhav/OneDrive/Attachments/Desktop/ClinicQueue/src/main/java/in/clinicqueue/service/DashboardService.java)
- Search Algorithm in:
  - [`VisitRepository.java`](file:///c:/Users/P%20Sai%20Vaibhav/OneDrive/Attachments/Desktop/ClinicQueue/src/main/java/in/clinicqueue/repository/VisitRepository.java) (`searchVisits` method)

### 🗣️ Member 4 ka Speaking Script (1 Minute)
> *"Sir, maine **Live Analytics, Dashboard Metrics aur Search/Filter Engine** handle kiya hai.*
> 1. *`DashboardService` real-time me calculate karta hai: Total visits, currently waiting patients, ongoing consultations, aur completed visits.*
> 2. *Har consulting doctor ke liye live summary card generate hota hai jisme dikhta hai ki unke room ke bahar kitne log wait kar rahe hain aur andar kaun sa patient hai.*
> 3. *`searchVisits()` method real-time filter provide karta hai: Receptionist doctor ke basis par, status ke basis par, ya patient name aur token number search karke kisi bhi record ko turant dhoondh sakti hai.*
> 4. *Saari search queries parameterized hain taaki performance fast rahe."*

### ❓ Evaluator Questions for Member 4
1. **Q: Dashboard metrics calculate kaise hote hain?**
   - *Ans: `DashboardService.getDashboardSummary()` aaj ke saare visits ko filter karke status-wise counts aur per-doctor DTOs prepare karta hai.*
2. **Q: Search kaise kaam karti hai?**
   - *Ans: `LOWER(p.full_name) LIKE ? OR LOWER(v.token_number) LIKE ?` with wildcard parameters, bina UI thread ko freeze kiye.*
3. **Q: Cancelled visits dashboard me count hote hain?**
   - *Ans: Haan, cancelled counts separate metric me aate hain par waiting queue aur wait time se exclude ho jate hain.*

---

## 👨‍💻 MEMBER 5: UI/UX & Desktop Application Lifecycle Lead

### 📂 Assigned Files & Folders
- `src/main/java/in/clinicqueue/`
  - [`ClinicQueueApp.java`](file:///c:/Users/P%20Sai%20Vaibhav/OneDrive/Attachments/Desktop/ClinicQueue/src/main/java/in/clinicqueue/ClinicQueueApp.java)
- `src/main/java/in/clinicqueue/view/`
  - [`MainLayoutView.java`](file:///c:/Users/P%20Sai%20Vaibhav/OneDrive/Attachments/Desktop/ClinicQueue/src/main/java/in/clinicqueue/view/MainLayoutView.java)
  - [`DashboardView.java`](file:///c:/Users/P%20Sai%20Vaibhav/OneDrive/Attachments/Desktop/ClinicQueue/src/main/java/in/clinicqueue/view/DashboardView.java)
  - [`RegisterVisitView.java`](file:///c:/Users/P%20Sai%20Vaibhav/OneDrive/Attachments/Desktop/ClinicQueue/src/main/java/in/clinicqueue/view/RegisterVisitView.java)
  - [`QueueBoardView.java`](file:///c:/Users/P%20Sai%20Vaibhav/OneDrive/Attachments/Desktop/ClinicQueue/src/main/java/in/clinicqueue/view/QueueBoardView.java)
- `src/main/resources/in/clinicqueue/css/`
  - [`styles.css`](file:///c:/Users/P%20Sai%20Vaibhav/OneDrive/Attachments/Desktop/ClinicQueue/src/main/resources/in/clinicqueue/css/styles.css)
- Build files: `pom.xml` & `mvnw.cmd`

### 🗣️ Member 5 ka Speaking Script (1 Minute)
> *"Sir, maine **JavaFX Desktop UI, Styling aur Application Lifecycle** develop kiya hai.*
> 1. *`ClinicQueueApp` application entrypoint hai jo database initialize karta hai aur primary stage setup karta hai.*
> 2. *`MainLayoutView` ek seamless single-window layout hai jisme Header, Navigation bar, View-switcher (`StackPane`), aur Status bar hai.*
> 3. *UI styling ke liye humne custom `styles.css` likha hai: professional clinic navy theme, status badges (Amber for Waiting, Blue for Consultation, Green for Completed), aur responsive metric cards.*
> 4. *Maine presentation ke liye ek confirmed 'Reset Demo Data' button bhi banaya hai jo sample records ko instantly restore kar deta hai bina database file delete kiye."*

### ❓ Evaluator Questions for Member 5
1. **Q: Electron ya Web ke badle JavaFX kyun use kiya?**
   - *Ans: JavaFX native desktop performance deta hai, zero-latency hai, memory footprint low hai, aur hospital OPD counters ke liye 100% offline operate karta hai.*
2. **Q: Screen switch hone par page reload hota hai kya?**
   - *Ans: Nahi, `StackPane.getChildren().setAll(view)` use karke in-memory view switch hota hai instantly.*
3. **Q: Responsive design kaise ensure kiya?**
   - *Ans: `BorderPane`, `GridPane`, aur `VBox` ke dynamic percentage sizing se window resize hone par koi bhi control clip nahi hota.*

---

## 🏆 Presentation Coordination (Kaise Start Karna Hai)

1. **Member 5** app launch karega (`.\mvnw.cmd javafx:run`) aur intro dega.
2. **Member 1** database architecture aur SQLite setup explain karega.
3. **Member 2** "Register Patient" tab par jaakar "Aarav Demo" register karega aur token dikhayega.
4. **Member 3** "Doctor Queue Board" tab par jaakar "Call Next" aur "Complete Consultation" karke rules dikhayega.
5. **Member 4** search filter aur dashboard live metrics dikhayega.
6. **Member 1 ya 2** tests run karke dikhayega: `.\mvnw.cmd test`.
