# 🚀 AI GST Auto-Filing System

An intelligent GST invoice processing and automation platform that simplifies invoice management, GST validation, and accounting workflows using OCR-based data extraction and smart validation mechanisms.

Built as a Proof of Concept (POC) using Angular, Spring Boot, and MySQL.

---

# 📌 Project Overview

The AI GST Auto-Filing System automates the manual GST invoice handling process by:

* Uploading invoice PDFs/images
* Extracting invoice details using OCR simulation
* Validating GST calculations
* Detecting GST errors and duplicate invoices
* Generating GST summaries and analytics dashboards
* Preparing data for future Tally integration

This project demonstrates how AI-assisted automation can improve accounting efficiency and reduce human errors in GST filing processes.

---

# 🛠 Tech Stack

## Frontend

* Angular 18
* Angular Material
* RxJS
* Chart.js

## Backend

* Spring Boot 3
* Java 17
* Spring Security + JWT
* Spring Data JPA

## Database

* MySQL

---

# ✨ Features

## 🔐 Authentication

* JWT-based login system
* Secure password encryption using BCrypt

## 📄 Invoice Upload

* Upload invoice PDFs/images
* Drag-and-drop upload UI
* File preview support

## 🤖 OCR Simulation

* Mock OCR extraction for POC
* Extract:

  * Invoice Number
  * GSTIN
  * Vendor Name
  * Tax Amounts
  * Total Amount

## ✅ GST Validation Engine

* GST mismatch detection
* Invalid GSTIN detection
* Duplicate invoice checking
* Validation status tracking

## 📊 Dashboard Analytics

* Total invoices
* GST collected
* Monthly invoice statistics
* Validation error summaries
* Invoice trend charts

## 📁 Invoice Management

* View uploaded invoices
* Search and filter invoices
* Detailed invoice information page

---

# 🏗 System Architecture

```text
Angular Frontend
        ↓
REST APIs
        ↓
Spring Boot Backend
        ↓
GST Validation Engine
        ↓
MySQL Database
```

---

# 📂 Project Structure

```text
kanani proj/
│
├── gst-auto-filing-frontend/
│
├── gst-auto-filing-backend/
│
└── README.md
```

---

# 🗄 Database Tables

## users

* id
* name
* email
* password

## invoices

* id
* invoice_number
* vendor_name
* gstin
* invoice_date
* taxable_amount
* cgst
* sgst
* igst
* total_amount
* validation_status
* created_at

## validation_errors

* id
* invoice_id
* error_message
* severity

---

# 🚀 Getting Started

## Backend Setup

### 1. Navigate to backend

```bash
cd gst-auto-filing-backend
```

### 2. Configure MySQL database

Update `application.properties`

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/gst_auto_filing
spring.datasource.username=root
spring.datasource.password=your_password
```

### 3. Run Spring Boot application

```bash
mvn spring-boot:run
```

Backend runs on:

```text
http://localhost:8080
```

---

## Frontend Setup

### 1. Navigate to frontend

```bash
cd gst-auto-filing-frontend
```

### 2. Install dependencies

```bash
npm install
```

### 3. Run Angular application

```bash
ng serve
```

Frontend runs on:

```text
http://localhost:4200
```

---

# 🔌 API Endpoints

## Authentication

```http
POST /api/auth/login
```

## Upload Invoice

```http
POST /api/invoices/upload
```

## Get All Invoices

```http
GET /api/invoices
```

## Dashboard Analytics

```http
GET /api/dashboard/summary
```

---

# 📈 Future Enhancements

* Real OCR integration using Tesseract
* TallyPrime XML integration
* AI-based GST anomaly detection
* Cloud deployment
* Email notifications
* Multi-user role management
* AI chatbot assistant
* Real-time GST filing support

---

# 🎯 Use Cases

* Small businesses
* Accountants
* GST consultants
* Invoice processing automation
* Financial analytics

---

# 👨‍💻 Developed By

Mukhi
B.E. Computer Science and Engineering
Coimbatore Institute of Technology

---

# 📜 License

This project is developed for educational and research purposes.
