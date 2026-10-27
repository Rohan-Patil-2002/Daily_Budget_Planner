========================================================================
Daily Budget Planner - Comprehensive User & Developer Guide
========================================================================

Daily Budget Planner is a full-featured Java Web Application designed to 
help users take control of their personal finances by breaking monthly budgets 
into realistic, achievable daily spending limits with smart roll-over calculations.

Built using Java Servlets (Java EE 8 / javax.servlet 4.0), JSP, JDBC, MySQL, 
and modern responsive Vanilla CSS.

------------------------------------------------------------------------
Features & Highlights
------------------------------------------------------------------------
- User Authentication: Secure Login & Registration with BCrypt hashing and session protection.
- Visual Dashboard: KPI summary cards, progress bars, card grid and table views.
- Dynamic Budgeting: Monthly budget allocated across the exact days in that month (with leap year support).
- Smart Daily Recalculations: Overspending or underspending dynamically updates subsequent days using BigDecimal precision.
- Skip Day Rollover: Skipping a day logs ₹0 spend and rolls that day's target into upcoming days.
- Itemized Spend Logging: Multi-transaction support per day with notes, categories, and quick chips.
- Comprehensive Spend History: Day-by-day running balance audit and individual transaction breakdown.
- Plan Management: Full Create, Read, Update, and Delete (CRUD) operations for plans with transaction safety.
- Print & Export: Dedicated print view for financial reporting.
- Strict Security: POST-only state changes, CSRF token validation, user ownership enforcement, HttpOnly cookies, and XSS sanitization.

------------------------------------------------------------------------
Database Schema (MySQL)
------------------------------------------------------------------------
You can execute the included `schema.sql` script directly in MySQL:
  mysql -u root -p < schema.sql

Tables:
1. `users` Table:
   CREATE TABLE users (
       userId INT PRIMARY KEY AUTO_INCREMENT,
       username VARCHAR(100) NOT NULL UNIQUE,
       password VARCHAR(255) NOT NULL
   );

2. `plans` Table:
   CREATE TABLE plans (
       planId INT PRIMARY KEY AUTO_INCREMENT,
       userId INT NOT NULL,
       month VARCHAR(20) NOT NULL,
       year INT NOT NULL,
       amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
       UNIQUE KEY uq_user_month_year (userId, month, year),
       FOREIGN KEY (userId) REFERENCES users(userId) ON DELETE CASCADE
   );

3. `spend_amount` Table:
   CREATE TABLE spend_amount (
       spendId INT PRIMARY KEY AUTO_INCREMENT,
       planId INT NOT NULL,
       day INT NOT NULL,
       day_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
       current_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
       daily_budget DECIMAL(10,2) NOT NULL DEFAULT 0.00,
       UNIQUE KEY uq_plan_day (planId, day),
       FOREIGN KEY (planId) REFERENCES plans(planId) ON DELETE CASCADE
   );

4. `spended_day` Table:
   CREATE TABLE spended_day (
       dayId INT PRIMARY KEY AUTO_INCREMENT,
       spendId INT NOT NULL,
       day INT NOT NULL,
       spended_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
       note VARCHAR(255) DEFAULT NULL,
       FOREIGN KEY (spendId) REFERENCES spend_amount(spendId) ON DELETE CASCADE
   );

------------------------------------------------------------------------
Project Structure
------------------------------------------------------------------------
Daily_Budget_Planner/
├── src/main/java/com/jsp/
│   ├── DAO/
│   │   ├── UserDAO.java
│   │   ├── PlanDAO.java
│   │   ├── SpendAmountDAO.java
│   │   └── HistoryDAO.java
│   ├── Models/
│   │   ├── User.java
│   │   ├── Plans.java
│   │   ├── SpendAmount.java
│   │   └── SpendedByDay.java
│   ├── Servlets/
│   │   ├── LoginServlet.java
│   │   ├── RegisterServlet.java
│   │   ├── LogoutServlet.java
│   │   ├── DashboardServlet.java
│   │   ├── MenuServlet.java
│   │   ├── AddPlanServlet.java
│   │   ├── UpdatePlanServlet.java
│   │   ├── DeletePlanServlet.java
│   │   ├── SpendAmountServlet.java
│   │   └── HistoryServlet.java
│   └── Utils/
│       ├── DBConnection.java
│       ├── CSRFTokenUtil.java
│       ├── InputValidator.java
│       └── SecurityUtil.java
│
├── src/main/webapp/
│   ├── Css/
│   │   ├── headerfooter.css (Design System & Global Layout)
│   │   ├── indexStyle.css (Auth Pages Glassmorphism)
│   │   ├── dashboardStyle.css (Dashboard & KPI Styles)
│   │   ├── AddPlanStyle.css (Plan Forms & Live Estimator)
│   │   └── spendAmount.css (Daily Spend Console)
│   ├── WEB-INF/
│   │   └── web.xml (Servlet mappings, error pages, and session cookie security)
│   ├── index.jsp (Login)
│   ├── register.jsp (Registration)
│   ├── dashboard.jsp (Main Financial Command Center)
│   ├── menu.jsp (Features Showcase)
│   ├── AddPlan.jsp (New Budget Plan Setup)
│   ├── EditPlan.jsp (Modify Plan)
│   ├── SpendAmount.jsp (Daily Spend Recording & Skip Day)
│   ├── History.jsp (Audit Timeline & Details)
│   ├── SpendHistory.jsp (Legacy Redirect Helper)
│   ├── About.jsp (User Guide & Architectural Docs)
│   ├── error.jsp (Custom Friendly 404/500 Error Page)
│   └── header.jsp & footer.jsp
│
├── src/test/java/com/jsp/Utils/
│   ├── BudgetCalculationTest.java (Unit tests for budgets and rollover logic)
│   └── SecurityAndValidationTest.java (Unit tests for BCrypt and validation)
│
├── schema.sql (Full MySQL Database Setup Script)
├── pom.xml (Maven Build Configuration)
├── .gitignore
└── README.txt

------------------------------------------------------------------------
How to Configure and Run Locally
------------------------------------------------------------------------
1. Ensure MySQL Server is running and create the schema:
   mysql -u root -p < schema.sql

2. Database Credentials Configuration:
   Set environment variables (recommended) or pass Java system properties:
     DB_URL       (Default: jdbc:mysql://localhost:3306/budget_planner?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC)
     DB_USER      (Default: root)
     DB_PASSWORD  (Default: empty)

3. Build with Maven:
   mvn clean verify
   (Generates target/Daily_Budget_Planner.war)

4. Run on Apache Tomcat 9:
   - Deploy `target/Daily_Budget_Planner.war` to Tomcat's `webapps/` folder, or
   - Import the project into Eclipse as a "Maven Project" / "Dynamic Web Project" and add it to Tomcat 9 Server.

5. Access the app at:
   http://localhost:8080/Daily_Budget_Planner/
