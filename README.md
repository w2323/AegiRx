# AegisRx: Pharmaceutical Supply Chain & Patient Safety Ecosystem

![AegisRx Banner](https://img.shields.io/badge/Status-Active-brightgreen) ![Java](https://img.shields.io/badge/Java-17-blue) ![JavaFX](https://img.shields.io/badge/JavaFX-21-orange) ![Maven](https://img.shields.io/badge/Build-Maven-red)

**AegisRx** is a comprehensive, Java-based enterprise ecosystem designed to secure the pharmaceutical supply chain and drastically improve patient safety at the point of dispensing. By tracking medication from the manufacturer to the patient's hands using unique Global Batch Identifiers (GBIs), AegisRx combats counterfeit drugs and prevents fatal drug interactions.

---

## 🌍 Real-World Application
In the modern world, counterfeit medications and adverse drug interactions are leading causes of preventable harm. AegisRx solves these problems by:
1. **Supply Chain Transparency:** Every batch of medication is registered by verified manufacturers and tracked across logistics nodes. Pharmacies can cryptographically verify if a batch is authentic before adding it to their inventory.
2. **Point-of-Sale AI Safety:** When a pharmacist sells medicine, the system runs an automated interaction check against the patient's existing medical profile. It will aggressively **block** the transaction if a severe drug-drug or drug-dietary conflict is detected.

---

## ⚙️ Core Functionalities & Modules

### 1. Manufacturer Operations
* **Register Medicine Batch (UC1):** Manufacturers register new productions of verified drugs. The system auto-generates a unique `GBI` (Global Batch Identifier) representing that specific lot.
* **Transfer Batch (UC2):** Seamlessly initiate logistics transfers to registered pharmacies, changing the batch status to `IN_TRANSIT` and securely handing over the chain of custody.

### 2. Pharmacy Operations
* **Verify Authenticity (UC3):** Pharmacies scan the GBI barcode. AegisRx queries the database to verify if the medicine is authentic, active, and not counterfeit.
* **Inventory Management (UC4 & UC5):** Receive `IN_TRANSIT` batches directly into local `IN_STOCK` inventory. Mark expired, recalled, or damaged stock for permanent destruction.
* **Smart Dispensing (UC6):** Process sales to patients using their AegisRx User ID. All transactions undergo an automated safety interaction check before finalizing.

### 3. Patient Portal
* **Medication Tracking:** Patients have full visibility over what drugs have been dispensed to them, including dosages and dates.
* **Dietary & Interaction Alerts:** The system warns patients of specific foods to avoid (e.g., Grapefruit juice with Statins) based on their active prescriptions.

### 4. System Administration
* Provides global oversight, allowing regulatory admins to monitor total registered users, active pharmacies, and system-wide batch statuses.

---

## 🛠 Dependencies & Tech Stack

This original project is built using modern Java architecture emphasizing OOP and GRASP design patterns (Controllers, Information Experts, Polymorphism).

* **Language:** Java 17
* **Build Tool:** Maven 3.11
* **User Interface:** JavaFX 21.0.2 (Controls, FXML, Graphics)
* **Database Driver:** Microsoft SQL Server JDBC Driver (`mssql-jdbc` 12.6.1)
* **Data Parsing:** Google Gson 2.10.1 (For AI/JSON API interactions)
* **Architecture:** MVC (Model-View-Controller) coupled with DAO (Data Access Object) persistence patterns.

---

## 🚧 Project Limitations
While AegisRx provides a robust architectural foundation, it currently possesses certain limitations intended for future expansion:
1. **Centralized Database:** It currently relies on a centralized MS SQL Server. In a production global supply chain, this architecture would ideally be migrated to a distributed ledger (Blockchain) to guarantee mathematical immutability of the chain of custody.
2. **AI Interaction Rules:** The drug-interaction checking system relies on internal rule-based algorithms or mocked local strategies rather than querying live, FDA-approved external API databases (like the NIH RxNorm database).
3. **Hardware Integration:** The system currently requires manual entry or clipboard pasting of GBI codes. It lacks native hardware drivers for physical barcode/QR scanners.

---

## 💻 Live GUI Demo (Web Version)

To easily present the system to stakeholders without requiring them to install Java 17, JavaFX, or an MS SQL Server, a **Web-based Graphical User Interface Demo** has been created in the `/demo` folder. 

**Demo Link:** [aegirx.netlify.app](https://aegirx.netlify.app/)

### 🚀 How to Run the Web Demo

#### Local Development
1. Ensure you have **Node.js** installed.
2. Open a terminal and navigate to the `demo` directory: `cd demo`.
3. Run `npm install` to install the dependencies.
4. Run `npm run dev` to start the local Vite development server.
5. Open the provided `localhost` URL in your browser.

#### Deployment (Netlify/Vercel)
This demo is entirely frontend-based and can be hosted for free on static hosting providers:
1. Navigate to the `demo` directory: `cd demo`.
2. Run `npm install`.
3. Run `npm run build` to generate the production-ready static files.
4. Upload the generated `dist` folder directly into Netlify's manual deployment drop-zone.

### ⚠️ Demo Limitations
Please note that the live web link is strictly a **frontend simulation**. 
* **No Real Backend:** The Java SQL DAOs and services are bypassed. The demo uses browser `localStorage` to emulate database persistence.
* **Simulated AI:** The drug interactions in the demo are hardcoded to specific scenarios (e.g., trying to sell Lisinopril to a patient who already bought Ibuprofen) to demonstrate the UI behavior.
* **Scope:** It demonstrates the UI/UX flows and structural logic. It is *not* a replacement for the secure Java application.

### 🔐 Demo Test Credentials
If you are testing the live web demo, the local mock database automatically seeds itself with the following accounts. You can also register a new account on the login page.

| Role | Username | Password |
| :--- | :--- | :--- |
| **System Admin** | `admin` | `admin` |
| **Manufacturer** | `manufacturer1` | `manufacturer` |
| **Pharmacy Staff** | `pharmacy1` | `pharmacy` |
| **Patient** | `patient1` | `patient` |

To reset the demo to its default state, simply clear your browser's site data/Local Storage.

---
*Developed as part of the AegisRx Supply Chain Initiative.*
