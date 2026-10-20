import sys
import os
from PyQt6.QtWidgets import QApplication
from PyQt6.QtGui import QTextDocument, QPdfWriter, QPageLayout, QPageSize
from PyQt6.QtCore import QMarginsF

def build_html_content():
    return """<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<style>
  body {
    font-family: 'Segoe UI', Arial, sans-serif;
    font-size: 10pt;
    line-height: 1.5;
    color: #1e293b;
    margin: 0;
    padding: 0;
  }
  
  /* Headings */
  h1 {
    font-size: 22pt;
    color: #0f172a;
    margin: 0 0 6px 0;
    font-weight: bold;
  }
  
  .subtitle {
    font-size: 12pt;
    color: #2563eb;
    margin: 0 0 16px 0;
    font-weight: 600;
  }
  
  .meta-box {
    background-color: #f1f5f9;
    border-left: 4px solid #2563eb;
    padding: 8px 14px;
    font-size: 9pt;
    color: #475569;
    margin-bottom: 20px;
  }
  
  h2 {
    font-size: 14pt;
    color: #0f172a;
    border-bottom: 1.5px solid #cbd5e1;
    padding-bottom: 4px;
    margin-top: 22px;
    margin-bottom: 10px;
  }
  
  h3 {
    font-size: 11pt;
    color: #1e3a8a;
    margin-top: 14px;
    margin-bottom: 6px;
  }
  
  p {
    margin: 0 0 8px 0;
    text-align: justify;
  }
  
  /* Tables */
  table {
    width: 100%;
    border-collapse: collapse;
    margin: 10px 0 16px 0;
    font-size: 9pt;
  }
  
  th, td {
    border: 1px solid #cbd5e1;
    padding: 6px 9px;
    vertical-align: top;
  }
  
  th {
    background-color: #0f172a;
    color: #ffffff;
    font-weight: bold;
    text-align: left;
  }
  
  tr:nth-child(even) {
    background-color: #f8fafc;
  }
  
  /* Callout & Cards */
  .callout {
    background-color: #eff6ff;
    border-left: 4px solid #3b82f6;
    padding: 10px 14px;
    margin: 12px 0;
    font-size: 9.5pt;
  }
  
  .callout-title {
    font-weight: bold;
    color: #1e40af;
    margin-bottom: 4px;
  }
  
  .analogy-card {
    background-color: #fefce8;
    border-left: 4px solid #eab308;
    padding: 10px 14px;
    margin: 12px 0;
    font-size: 9.5pt;
  }
  
  .badge {
    background-color: #2563eb;
    color: #ffffff;
    padding: 2px 6px;
    font-size: 8pt;
    font-weight: bold;
  }
  
  .code-term {
    font-family: 'Consolas', 'Courier New', monospace;
    background-color: #e2e8f0;
    padding: 1px 4px;
    font-size: 8.5pt;
    color: #0f172a;
  }
  
  .footer {
    font-size: 8pt;
    color: #94a3b8;
    border-top: 1px solid #e2e8f0;
    padding-top: 6px;
    margin-top: 24px;
    text-align: center;
  }
</style>
</head>
<body>

  <!-- Title & Metadata -->
  <h1>ClinicQueue — Phase 1 Comprehensive Architectural Guide</h1>
  <div class="subtitle">A Layman-to-Engineer Walkthrough of the Project Foundation, Structure & Design Decisions</div>
  
  <div class="meta-box">
    <strong>Author:</strong> Vaibhav Puvaladas & Antigravity IDE &nbsp;|&nbsp; 
    <strong>Repository:</strong> https://github.com/vaibhavpuvaladas-byte/ClinicQueue &nbsp;|&nbsp;
    <strong>Commit:</strong> e53c26d &nbsp;|&nbsp;
    <strong>Milestone:</strong> Phase 1 Complete
  </div>

  <!-- Section 1: Layman Executive Summary -->
  <h2>1. Executive Summary: What is ClinicQueue & What Did We Do in Phase 1?</h2>
  
  <div class="analogy-card">
    <div class="callout-title">🏥 The Real-World Hospital Analogy:</div>
    Imagine entering a bustling hospital outpatient department. If there were no reception desk, no tokens, and no orderly queue, dozens of patients would crowd outside doctors' consultation rooms. Patients would argue over who came first, doctors would be constantly interrupted, and receptionists would be overwhelmed answering <em>"How long will it take?"</em>.
    <br><br>
    <strong>ClinicQueue</strong> is an offline, resilient desktop software system built to solve this exact chaos:
    <ul>
      <li>It gives the receptionist a fast intake screen to register patients and immediately hand them a unique daily token (e.g. <code>D01-2026-10-20-001</code>).</li>
      <li>It gives each doctor a dedicated queue board to call the next waiting patient in exact arrival order (First-In, First-Out).</li>
      <li>It gives clinic managers an overview dashboard displaying waiting times, completed visits, and active consultations.</li>
    </ul>
  </div>

  <p>
    <strong>What is "Phase 1"?:</strong> If you think of building a house, you cannot install chandeliers, water pipes, or kitchen appliances until you have cleared the land, poured the concrete foundation, erected the steel framework, and installed the doors and windows. 
    <strong>Phase 1 is that rock-solid architectural skeleton.</strong> We created the project blueprint, configured the build tools, built the entire window navigation system, and styled the visual appearance so the application can run smoothly on any computer.
  </p>

  <!-- Section 2: Complete Folder Guide -->
  <h2>2. The Anatomy of the Codebase: Complete Folder-by-Folder Guide</h2>
  <p>
    Every directory in the <code>ClinicQueue</code> project was deliberately placed according to industry-standard <strong>Separation of Concerns</strong>. Here is what every single folder does, why it is relevant, and why it is critical:
  </p>

  <table>
    <tr>
      <th style="width: 25%;">Directory Path</th>
      <th style="width: 35%;">What Goes Inside?</th>
      <th style="width: 40%;">Why is it Critical? (Layman & Technical Rationale)</th>
    </tr>
    <tr>
      <td><strong><code>/</code> (Project Root)</strong></td>
      <td>Master build files (<code>pom.xml</code>), launch wrappers (<code>mvnw</code>, <code>mvnw.cmd</code>), <code>.gitignore</code>, and <code>README.md</code>.</td>
      <td>The command center of the project. It tells the computer how to compile the code, what libraries to download, and provides first-time setup instructions.</td>
    </tr>
    <tr>
      <td><strong><code>docs/</code></strong></td>
      <td>Architectural specifications, live demonstration scripts, and acceptance test plans.</td>
      <td><strong>The Blueprint.</strong> Professional software requires verified documentation so any engineer or stakeholder can understand the design rules without reading thousands of lines of code.</td>
    </tr>
    <tr>
      <td><strong><code>src/main/java/</code></strong></td>
      <td>All production Java source code files.</td>
      <td>The standard Maven location where the compiler searches for executable application code.</td>
    </tr>
    <tr>
      <td><strong><code>.../clinicqueue/view/</code></strong></td>
      <td>Visual user interface classes (<code>DashboardView</code>, <code>RegisterVisitView</code>, <code>QueueBoardView</code>).</td>
      <td><strong>The Display Department.</strong> Contains only what the user sees on their monitor. By isolating UI layouts in their own folder, visual redesigns never risk breaking core business rules or database storage.</td>
    </tr>
    <tr>
      <td><strong><code>.../clinicqueue/controller/</code></strong></td>
      <td>UI event handlers and action coordinators.</td>
      <td><strong>The Traffic Police.</strong> When a user clicks "Register" or "Call Next", controllers take that click, validate it, pass it to the business service, and update the screen.</td>
    </tr>
    <tr>
      <td><strong><code>.../clinicqueue/service/</code></strong></td>
      <td>Business logic rules (wait time calculator, token allocator, state transition guards).</td>
      <td><strong>The Policy Enforcers.</strong> This folder holds the hospital's operational brain. It ensures a doctor cannot call two patients at once, and guarantees sequential tokens.</td>
    </tr>
    <tr>
      <td><strong><code>.../clinicqueue/repository/</code></strong></td>
      <td>Pure JDBC database access classes for Patients, Visits, Doctors, and Departments.</td>
      <td><strong>The Filing Clerks.</strong> The only part of the application permitted to speak SQL to the database. This prevents database queries from being scattered across UI screens.</td>
    </tr>
    <tr>
      <td><strong><code>.../clinicqueue/database/</code></strong></td>
      <td>SQLite connection manager, schema migrations, and table initializers.</td>
      <td><strong>The Secure Vault.</strong> Ensures SQLite foreign keys are always turned on (<code>PRAGMA foreign_keys = ON;</code>) and that tables are safely created when the app launches.</td>
    </tr>
    <tr>
      <td><strong><code>.../clinicqueue/model/</code></strong></td>
      <td>Data structures and entity objects (Doctor, Department, Patient, Visit, Status Enums).</td>
      <td><strong>The Standardized Forms.</strong> Represents the medical entities in clean Java records/classes across all layers.</td>
    </tr>
    <tr>
      <td><strong><code>src/main/resources/</code></strong></td>
      <td>Static assets, fonts, icons, and CSS stylesheets.</td>
      <td>Separates visual design assets from compiled Java code.</td>
    </tr>
    <tr>
      <td><strong><code>.../css/</code></strong></td>
      <td>Application stylesheet (<code>app.css</code>).</td>
      <td><strong>The Interior Designer.</strong> Defines the clinic's color palette, button hover effects, font typography, and status chips in one centralized stylesheet.</td>
    </tr>
    <tr>
      <td><strong><code>src/test/java/</code></strong></td>
      <td>Automated JUnit 5 test classes.</td>
      <td><strong>The Quality Inspector.</strong> Houses automated tests that verify queue logic and token numbering without needing to manually click through the UI.</td>
    </tr>
    <tr>
      <td><strong><code>target/</code></strong> (Auto-generated)</td>
      <td>Compiled <code>.class</code> files and packaged JAR binaries.</td>
      <td>Created automatically by Maven during compilation. Kept out of Git via <code>.gitignore</code> to keep the repository lightweight.</td>
    </tr>
  </table>

  <!-- Section 3: File-by-File Lineage -->
  <h2>3. Detailed Breakdown of Every File Created in Phase 1</h2>

  <h3>1. <code>pom.xml</code> (Project Object Model)</h3>
  <p>
    <strong>What it is:</strong> The recipe and bill-of-materials for the entire application. It instructs Apache Maven on how to assemble ClinicQueue.
  </p>
  <ul>
    <li><strong>Java 21 LTS Target:</strong> Configured <code>&lt;release&gt;21&lt;/release&gt;</code> to utilize the latest Long-Term Support Java features, enhanced pattern matching, and superior runtime performance.</li>
    <li><strong>JavaFX 21 (Controls & FXML):</strong> Pulls the official OpenJFX desktop graphical engine so modern UI components (buttons, tables, labels) render natively on Windows, macOS, and Linux.</li>
    <li><strong>SQLite JDBC (3.49.1):</strong> Bundles the lightweight SQLite database driver so ClinicQueue runs as a self-contained desktop app with zero server installation.</li>
    <li><strong>JUnit 5 (Jupiter):</strong> Modern unit testing framework configured to run during automated builds.</li>
  </ul>

  <h3>2. <code>mvnw.cmd</code> and <code>mvnw</code> (Maven Universal Wrappers)</h3>
  <p>
    <strong>What they are:</strong> Intelligent shell scripts (one for Windows <code>.cmd</code>, one for Linux/macOS <code>.sh</code>) that enable any developer to launch or build ClinicQueue immediately using <code>.\mvnw.cmd clean javafx:run</code>, even if they don't have Maven installed globally on their system.
  </p>

  <h3>3. <code>.gitignore</code></h3>
  <p>
    <strong>What it is:</strong> A security and hygiene filter for Git. It explicitly forbids compiled bytecodes (<code>target/</code>), local SQLite database files (<code>*.db</code>), and personal IDE cache (<code>.idea/</code>, <code>.vscode/</code>) from being accidentally uploaded to GitHub.
  </p>

  <h3>4. <code>Main.java</code> & <code>ClinicQueueApp.java</code> (The Application Core)</h3>
  <p>
    <strong>How they work together:</strong>
  </p>
  <ul>
    <li><strong><code>Main.java</code>:</strong> A lightweight bootstrap launcher. In modern Java, launching a JavaFX application from a class that directly extends <code>Application</code> can trigger a module-path warning on non-modular classpaths. <code>Main.java</code> calls <code>ClinicQueueApp.main(args)</code>, completely bypassing this JVM quirk.</li>
    <li><strong><code>ClinicQueueApp.java</code>:</strong> The primary JavaFX shell. It creates the main desktop window (1280×750 pixels), loads <code>app.css</code>, creates the top branding banner, sets today's date, and builds the top navigation bar.</li>
    <li><strong>The <code>StackPane</code> View Switcher:</strong> Instead of opening multiple clunky popups, <code>ClinicQueueApp</code> holds <code>DashboardView</code>, <code>RegisterVisitView</code>, and <code>QueueBoardView</code> inside a single central stack. Clicking navigation buttons simply toggles visibility, delivering instant, zero-flicker transitions.</li>
  </ul>

  <h3>5. <code>DashboardView.java</code> (The Control Tower)</h3>
  <p>
    Renders high-level clinic metrics: four distinct KPI stat cards (<strong>WAITING</strong>, <strong>IN CONSULTATION</strong>, <strong>COMPLETED</strong>, <strong>CANCELLED</strong>) and a container for per-doctor workload summaries. In Phase 1, it establishes the visual layout ready to receive live database counts in Phase 5.
  </p>

  <h3>6. <code>RegisterVisitView.java</code> (The Intake Counter)</h3>
  <p>
    Provides the patient registration form: Patient Full Name (required), Phone (optional), Department selector, Doctor selector (filtered by department), and Visit Type radio buttons (Walk-in vs Scheduled Appointment).
  </p>

  <h3>7. <code>QueueBoardView.java</code> (The Doctor's Cockpit)</h3>
  <p>
    Provides the doctor's queue management workspace: Doctor selection dropdown, Status filter, search box, "Call Next Patient" primary action button, "Complete Consultation", "Cancel Visit", and the prominent <em>Approximate Wait Time</em> banner.
  </p>

  <h3>8. <code>app.css</code> (Clinical Design System)</h3>
  <p>
    Eliminates generic default browser styling in favor of an intentional, clinical palette:
  </p>
  <ul>
    <li><strong>Deep Navy Header (<code>#0f172a</code>):</strong> Conveys authority, professionalism, and calmness.</li>
    <li><strong>Vibrant Primary Blue (<code>#2563eb</code>):</strong> Highlights active navigation and primary action buttons.</li>
    <li><strong>High-Contrast Status Chips:</strong> Amber for Waiting (<code>#fef3c7</code>), Blue for In Consultation (<code>#dbeafe</code>), Emerald Green for Completed (<code>#dcfce7</code>), and Slate for Cancelled (<code>#f1f5f9</code>).</li>
    <li><strong>Neutral Card Elevations:</strong> Clean white cards on a soft off-white background (<code>#f8fafc</code>) for maximum readability during classroom presentations.</li>
  </ul>

  <!-- Section 4: Architectural Decisions -->
  <h2>4. Layman's Answers to Key Engineering Questions</h2>

  <div class="callout">
    <div class="callout-title">Q: Why build a Desktop App with JavaFX instead of a Web Application in React/Chrome?</div>
    <strong>Answer:</strong> In a real hospital or outpatient clinic, front-desk intake terminals and doctor workstations must work 100% reliably even if the hospital internet drops. A local JavaFX desktop application starts in milliseconds, talks directly to the local database, has zero network latency, and requires no web servers or cloud subscriptions.
  </div>

  <div class="callout">
    <div class="callout-title">Q: Why use Code-Built JavaFX views instead of FXML drag-and-drop files?</div>
    <strong>Answer:</strong> FXML uses XML text files that look up controllers at runtime using reflection. A single typo in an XML file can crash the app with a confusing error during a live presentation. Code-built Java UI is checked by the Java compiler at compile time—if something is wrong, it won't even compile, guaranteeing stability.
  </div>

  <div class="callout">
    <div class="callout-title">Q: Why SQLite instead of MySQL or MongoDB?</div>
    <strong>Answer:</strong> SQLite lives in a single local file on disk (<code>clinicqueue.db</code>). It requires zero server setup, zero passwords, zero port configurations, and supports full ACID relational transactions and foreign keys. It is the gold standard for reliable, single-computer desktop software.
  </div>

  <div class="callout">
    <div class="callout-title">Q: Why did we separate Views, Services, and Repositories?</div>
    <strong>Answer:</strong> Imagine a restaurant. The waiter (View) takes your order. The chef (Service) cooks the meal following recipes and safety rules. The pantry manager (Repository) fetches the ingredients from the storeroom. If the waiter tries to cook or rummage through the storeroom, the kitchen devolves into chaos. Separating Views, Services, and Repositories ensures each part of our code does only one job exceptionally well.
  </div>

  <!-- Section 5: Verification & Results -->
  <h2>5. Phase 1 Verification & Quality Audit</h2>
  <table>
    <tr>
      <th>Verification Check</th>
      <th>Method Used</th>
      <th>Result</th>
    </tr>
    <tr>
      <td><strong>Source Code Compilation</strong></td>
      <td>Executed <code>.\mvnw.cmd clean compile</code> targeting Java 21 release</td>
      <td><strong>BUILD SUCCESS</strong> (11.7s, 0 errors, 5 source files compiled)</td>
    </tr>
    <tr>
      <td><strong>Desktop Window Launch</strong></td>
      <td>Executed <code>.\mvnw.cmd javafx:run</code></td>
      <td><strong>PASSED</strong> (Main window rendered cleanly at 1280x750)</td>
    </tr>
    <tr>
      <td><strong>Version Control Tracking</strong></td>
      <td>Pushed to GitHub: <code>vaibhavpuvaladas-byte/ClinicQueue</code></td>
      <td><strong>COMMITTED & PUSHED</strong> (Commit <code>e53c26d</code>)</td>
    </tr>
  </table>

  <!-- Section 6: Phase 2 Preview -->
  <h2>6. What's Next in Phase 2: Persistence & Database Layer</h2>
  <p>
    With the skeleton, UI layout, and navigation fully operational, <strong>Phase 2</strong> will connect the plumbing:
  </p>
  <ol>
    <li>Establishing the SQLite connection manager with mandatory <code>PRAGMA foreign_keys = ON;</code>.</li>
    <li>Creating database tables for <code>departments</code>, <code>doctors</code>, <code>patients</code>, and <code>visits</code>.</li>
    <li>Implementing pure JDBC repositories using prepared statements (no raw string SQL concatenation).</li>
    <li>Pre-seeding realistic clinic departments (e.g. Cardiology, Pediatrics) and doctors for immediate demonstration.</li>
  </ol>

  <div class="footer">
    ClinicQueue MVP &bull; Phase 1 Technical Specification &bull; Generated for Vaibhav Puvaladas &bull; October 2026
  </div>

</body>
</html>"""

def generate_pdf():
    app = QApplication(sys.argv)
    output_pdf_path = os.path.abspath("ClinicQueue_Phase_1_Detailed_Guide.pdf")
    
    writer = QPdfWriter(output_pdf_path)
    writer.setPageSize(QPageSize(QPageSize.PageSizeId.A4))
    
    # 15mm margins
    layout = QPageLayout(
        QPageSize(QPageSize.PageSizeId.A4),
        QPageLayout.Orientation.Portrait,
        QMarginsF(15, 15, 15, 15)
    )
    writer.setPageLayout(layout)
    
    doc = QTextDocument()
    html = build_html_content()
    doc.setHtml(html)
    
    doc.print(writer)
    print(f"[SUCCESS] PDF Generated at: {output_pdf_path} (Size: {os.path.getsize(output_pdf_path)} bytes)")

if __name__ == "__main__":
    generate_pdf()
