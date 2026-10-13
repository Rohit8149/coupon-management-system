# Git Branching Strategy & Contribution Guidelines

**Coupon Management System (CMS)**  
**Student:** Rohit Thatikonda (Roll No: 23102A0050, Serial No: 47)  
**Subject:** DevOps Lab – B.E. Semester VII (Computer Engineering)  
**Subject In-charge:** Dr. Snehal Andhare  

---

## 1. Branch Architecture

We follow a structured GitFlow-inspired branching model optimized for continuous integration and automated test quality gates:

```
[main] -----------------------------------------> Tag: v1.0.0 (Production)
   ^                                                 ^
   |                                                 |
[dev]  -------------------[Merge PR]-----------------+ (Integration)
         \                 /
[feature/coupon-create] --+ (Feature Work)
```

### 1.1 `main` Branch (Production)
* **Protection Level:** Protected branch. Direct commits are strictly prohibited.
* **Purpose:** Contains only thoroughly verified, production-ready code.
* **Promotion Rule:** Only receives merges from `dev` after passing all Selenium automated quality gates in Jenkins.
* **Tagging:** Releases are tagged using semantic versioning (e.g., `v1.0.0`).

### 1.2 `dev` Branch (Integration)
* **Purpose:** Active development integration branch.
* **Trigger:** Every push or PR merge into `dev` automatically triggers the Jenkins Continuous Integration build job (`mvn clean test package`).
* **Stability:** Must remain in a green, buildable state at all times.

### 1.3 Supporting Branches

| Branch Type | Naming Convention | Base Branch | Merge Target | Lifecycle |
| :--- | :--- | :--- | :--- | :--- |
| **Feature** | `feature/<module-name>` | `dev` | `dev` | Deleted after PR merge |
| **Bugfix** | `bugfix/<issue-id>-<desc>` | `dev` | `dev` | Deleted after PR merge |
| **Hotfix** | `hotfix/<version>-<desc>` | `main` | `main` and `dev` | Tagged and deleted |
| **Release** | `release/v<version>` | `dev` | `main` and `dev` | Tagged and deleted |

---

## 2. Branch Naming Rules
Branch names must be lowercase, hyphen-separated, and descriptive:
* `feature/coupon-create` – Implementation of coupon creation and validation form.
* `feature/coupon-search-dashboard` – Search filtering and KPI summary metrics dashboard.
* `feature/selenium-pipeline` – Selenium WebDriver automated UI testing suite.
* `feature/docker-jenkins-cd` – Docker container packaging and CD pipeline.

---

## 3. Commit Message Standards (Conventional Commits)
All commit messages must adhere to the Conventional Commits specification:

```
<type>(<scope>): <short description in present tense>

[optional body explaining rationale]

[optional issue reference, e.g., Closes #1]
```

### Allowed Types:
* `feat`: A new user-facing capability or functionality.
* `fix`: A bug fix for an existing behavior.
* `docs`: Documentation changes only (README, diagrams, reports).
* `style`: Code style/formatting changes that do not affect logic.
* `refactor`: Code refactoring without adding features or fixing bugs.
* `test`: Adding or correcting Selenium / JUnit tests.
* `ci`: Changes to Jenkinsfile, Maven build scripts, Dockerfile, or Ansible playbooks.
* `chore`: Maintenance tasks, dependency updates, configuration adjustments.

### Examples:
* `feat(coupon): implement coupon creation form with field validations`
* `test(selenium): add critical user journey test for coupon directory search`
* `ci(jenkins): add quality gate stage halting pipeline on test failure`
* `docs(readme): add architecture diagram and local execution instructions`

---

## 4. Pull Request (PR) & Code Review Process
1. Create a dedicated branch from `dev`:
   ```bash
   git checkout dev
   git checkout -b feature/coupon-create
   ```
2. Make granular, meaningful commits adhering to Conventional Commits.
3. Push to GitHub and open a Pull Request targeting `dev`.
4. Ensure PR description references relevant issue tickets (e.g., `Closes #1`).
5. Verify automated CI build status is green before requesting review.
6. Obtain approval, resolve any merge conflicts manually, and perform a clean merge.
