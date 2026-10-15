# Coupon Management System (CMS) – Selenium Testing Pipeline

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)]()
[![Java](https://img.shields.io/badge/Java-17%20LTS-orange.svg)]()
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-brightgreen.svg)]()
[![Database](https://img.shields.io/badge/Database-In--Memory%20H2-blue.svg)]()
[![Automation](https://img.shields.io/badge/Testing-Selenium%20WebDriver%204-red.svg)]()
[![CI/CD](https://img.shields.io/badge/CI%2FCD-Jenkins%20Pipeline-blue.svg)]()
[![Container](https://img.shields.io/badge/Container-Docker-2496ED.svg)]()
[![Config](https://img.shields.io/badge/Config-Ansible-EE0000.svg)]()

---

## 1. Project Overview

The **Coupon Management System (CMS)** is a lightweight, responsive enterprise web application engineered to manage promotional discount vouchers across their complete operational lifecycle (`DRAFT` &rarr; `ACTIVE` &rarr; `EXPIRED` / `REDEEMED`). 

The application is built with zero external runtime dependencies using an embedded in-memory database and embedded web server, providing a frictionless platform for automated Selenium WebDriver regression testing, Jenkins CI/CD quality gates, Docker containerization, and Ansible configuration management across 15 laboratory tasks.

---

## 2. Technology Stack

| Component | Selected Technology | Purpose & Architectural Justification |
| :--- | :--- | :--- |
| **Backend Framework** | Java 17 LTS / Spring Boot 4.1.1 | Enterprise application framework with built-in embedded Tomcat web server. |
| **Build & Dependency Tool** | Apache Maven Wrapper (`mvnw`) | Standardized, reproducible builds across developer and Jenkins environments. |
| **Database** | In-Memory H2 Database | Instant startup (< 1 sec) with zero external setup, eliminating CI flakiness. |
| **Object-Relational Mapping** | Spring Data JPA / Hibernate | Automated schema creation and query execution for coupon entities. |
| **Frontend UI** | Thymeleaf + Bootstrap 5 | Server-rendered responsive UI with unique IDs for robust Selenium locators. |
| **Health & Monitoring** | Spring Boot Actuator | Exposes `/actuator/health` JSON endpoint for Jenkins and Ansible liveness probes. |
| **Automated Testing** | Selenium WebDriver 4 + JUnit 5 | Headless browser automation across 4 critical user journeys. |
| **Continuous Integration** | Jenkins Declarative Pipeline | Multi-stage pipeline executing build, test quality gates, and packaging. |
| **Containerization** | Docker (Multi-stage build) | Lightweight, portable container runtime mapped to port 8080. |
| **Configuration Management**| Ansible (`playbook.yml`) | Automated server provisioning, idempotency validation, and health rollback. |

---

## 3. Application Architecture

The system follows a clean 4-tier monolithic architecture:

```
[Presentation Tier]        Thymeleaf Server Templates + Bootstrap 5 (Responsive UI)
        |
[Controller Tier]          Spring MVC (@Controller) + REST API (@RestController)
        |
[Service & Logic Tier]     CouponService (Validation, Lifecycle Rules, Redemptions)
        |
[Data & Persistence Tier]  CouponRepository (Spring Data JPA) -> Embedded H2 Database
```

---

## 4. Repository Structure

```
.
├── .github/
│   └── ISSUE_TEMPLATE/
│       ├── bug_report.md            # Standardized bug reporting template
│       └── feature_request.md       # Standardized feature suggestion template
├── app/
│   ├── .mvn/wrapper/                # Maven wrapper binaries and properties
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/devops/coupon/
│   │   │   │   ├── controller/      # Web and REST API controllers
│   │   │   │   ├── model/           # Coupon JPA domain entity
│   │   │   │   ├── repository/      # Spring Data JPA repository
│   │   │   │   └── service/         # Business logic and lifecycle validation
│   │   │   └── resources/
│   │   │       ├── templates/       # Thymeleaf HTML views (dashboard, coupons, create)
│   │   │       └── application.properties # H2, Actuator, and server configuration
│   │   └── test/java/               # Unit tests and Selenium WebDriver suites
│   ├── mvnw                         # Maven wrapper shell script (Linux/macOS)
│   ├── mvnw.cmd                     # Maven wrapper batch script (Windows)
│   └── pom.xml                      # Maven project configuration and dependencies
├── docs/                            # Department task submission documents (.docx)
├── planning/                        # Syllabus allocation, sprint backlog & tracker
├── screenshots/                     # Architecture diagrams and execution evidence
├── scripts/                         # Python automation for document and diagram generation
├── templates/                       # Department-approved Word document templates
├── .gitignore                       # Git ignore rules for build and temp files
├── CONTRIBUTING.md                  # Branching strategy and PR guidelines
└── README.md                        # Primary project documentation
```

---

## 5. Quick Start & Local Execution

### Prerequisites
* **Java Development Kit:** Eclipse Adoptium JDK 17 LTS (or JDK 21)
* **Git:** 2.40+ installed and configured

### Running the Application Locally
1. Clone the repository:
   ```bash
   git clone https://github.com/Rohit8149/coupon-management-system.git
   cd coupon-management-system
   ```
2. Navigate to the application module:
   ```bash
   cd app
   ```
3. Set your `JAVA_HOME` environment variable (if not already set globally):
   ```powershell
   $env:JAVA_HOME = "C:\Users\rohit\AppData\Local\Programs\Eclipse Adoptium\jdk-17.0.11.9-hotspot"
   ```
4. Build and run via Maven Wrapper:
   ```bash
   ./mvnw.cmd spring-boot:run
   ```
5. Open your web browser and navigate to:
   * **Dashboard:** [http://localhost:8080/](http://localhost:8080/)
   * **Coupons Directory:** [http://localhost:8080/coupons](http://localhost:8080/coupons)
   * **Create Coupon Form:** [http://localhost:8080/coupons/create](http://localhost:8080/coupons/create)
   * **Health Endpoint:** [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)
   * **H2 Console:** [http://localhost:8080/h2-console](http://localhost:8080/h2-console) (JDBC URL: `jdbc:h2:mem:coupondb`)

---

## 6. REST API Endpoints

| Method | Endpoint URI | Description |
| :--- | :--- | :--- |
| `GET` | `/` | Renders dashboard with KPI metrics and recent coupons |
| `GET` | `/coupons` | Directory table with code search and status filters |
| `GET` | `/coupons/create` | Renders coupon creation and validation form |
| `POST`| `/coupons/create` | Submits form to create and persist coupon in H2 |
| `POST`| `/coupons/{id}/activate` | Transitions coupon lifecycle status to `ACTIVE` |
| `POST`| `/coupons/{id}/redeem` | Increments usage tally; marks `REDEEMED` if cap reached |
| `POST`| `/coupons/{id}/expire` | Manually flags coupon status as `EXPIRED` |
| `GET` | `/api/coupons` | Returns JSON array of all coupons |
| `GET` | `/api/coupons/{code}` | Returns JSON object for single coupon by code |
| `GET` | `/actuator/health` | System health probe returning `{"status":"UP"}` |

---

## 7. 15-Task DevOps Milestone Summary

* **Task 1:** Problem Definition and Frozen MVP Scope
* **Task 2:** Agile Planning, User Stories, Scrum Plan & DevOps Lifecycle Diagram
* **Task 3:** Requirements (SRS), 4-Tier Architecture, Data Model & Local Spring Boot Setup
* **Task 4:** Git & GitHub Repository Initialization, Branch Policy & Issue Templates
* **Task 5:** Feature Development with Branching (`feature/coupon-create`) & PR Review
* **Task 6:** MVP Completion, Merge Conflict Simulation/Resolution & Tag `v1.0.0`
* **Task 7:** Jenkins Installation & Maven Continuous Integration Job
* **Task 8:** Declarative Jenkinsfile & Automated Server Deployment
* **Task 9:** Selenium WebDriver Test Design (4 Critical Journeys) & Failure Screenshot Hook
* **Task 10:** Continuous Testing in Jenkins, Quality Gate Enforcement & Defect Experiment
* **Task 11:** Multi-stage Dockerfile & Complete Container Lifecycle Management
* **Task 12:** Jenkins-Docker Continuous Deployment Pipeline & Versioned Tags
* **Task 13:** Server Configuration Specification & Ansible Playbook Setup
* **Task 14:** Automated Provisioning, Idempotency Verification (`changed=0`) & Rollback
* **Task 15:** Final End-to-End Pipeline Demonstration, Technical Report & Viva

---

