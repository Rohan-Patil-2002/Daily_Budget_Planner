<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>User Guide & Architecture - Daily Budget Planner</title>
    <meta name="description" content="Complete documentation and user guide for the Daily Budget Planner Java Web Application.">
    <link rel="stylesheet" href="Css/headerfooter.css">
    <link rel="stylesheet" href="Css/dashboardStyle.css">
    <style>
        .about-hero {
            background: #ffffff;
            border: 1px solid var(--border-light);
            border-radius: var(--radius-lg);
            padding: 36px 32px;
            margin-bottom: 32px;
            box-shadow: var(--shadow-sm);
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 24px;
            flex-wrap: wrap;
        }
        .about-hero-text {
            flex: 1;
            min-width: 300px;
        }
        .about-hero-text h1 {
            font-family: 'Outfit', sans-serif;
            font-size: 28px;
            font-weight: 800;
            color: var(--text-main);
            margin-bottom: 10px;
            letter-spacing: -0.02em;
        }
        .about-hero-text p {
            font-size: 15px;
            color: var(--text-muted);
            line-height: 1.6;
        }
        .step-grid {
            display: flex;
            flex-direction: column;
            gap: 24px;
            margin-bottom: 40px;
        }
        .step-card {
            background: #ffffff;
            border: 1px solid var(--border-light);
            border-radius: var(--radius-md);
            padding: 24px;
            box-shadow: var(--shadow-sm);
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 24px;
            align-items: center;
        }
        .step-number {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            background: var(--primary-50);
            color: var(--primary-700);
            font-weight: 700;
            font-size: 12px;
            padding: 4px 12px;
            border-radius: var(--radius-full);
            margin-bottom: 10px;
        }
        .step-info h3 {
            font-family: 'Outfit', sans-serif;
            font-size: 18px;
            font-weight: 700;
            color: var(--text-main);
            margin-bottom: 8px;
        }
        .step-info p {
            font-size: 14px;
            color: var(--text-muted);
            line-height: 1.6;
        }
        .step-img-box {
            border-radius: var(--radius-md);
            overflow: hidden;
            border: 1px solid var(--border-light);
            box-shadow: var(--shadow-sm);
        }
        .step-img-box img {
            width: 100%;
            height: auto;
            display: block;
            margin: 0;
            border-radius: 0;
            box-shadow: none;
            transition: transform 0.3s ease;
        }
        .step-img-box:hover img {
            transform: scale(1.02);
        }
        .tech-badges {
            display: flex;
            flex-wrap: wrap;
            gap: 10px;
            margin-top: 12px;
        }
        .tech-pill {
            background: #ffffff;
            border: 1px solid var(--border-light);
            padding: 6px 14px;
            border-radius: var(--radius-full);
            font-size: 13px;
            font-weight: 600;
            color: var(--text-main);
            box-shadow: var(--shadow-sm);
        }
        @media (max-width: 768px) {
            .step-card {
                grid-template-columns: 1fr;
            }
        }
    </style>
</head>
<body>
    <jsp:include page="header.jsp" />

    <main class="page-container">
        <!-- Hero Overview -->
        <div class="about-hero">
            <div class="about-hero-text">
                <span class="form-header-badge">📖 Application Documentation</span>
                <h1>About Daily Budget Planner</h1>
                <p>
                    <strong>Daily Budget Planner</strong> is a comprehensive personal finance web application built using <strong>Java Servlets, JSP, JDBC, and MySQL</strong>. It breaks down monthly budgets into automated daily spend targets, providing smart roll-overs and preventing end-of-month financial strain.
                </p>
            </div>
            <div>
                <img src="img/3.png" alt="Budget Planner Architecture" style="max-width: 280px; border-radius: var(--radius-md);" />
            </div>
        </div>

        <!-- Step-by-Step Usage Guide -->
        <div class="section-header">
            <h2 class="section-title">
                <span>🛠️</span> Step-by-Step User Guide
            </h2>
        </div>

        <div class="step-grid">
            <!-- Step 1 -->
            <div class="step-card">
                <div class="step-info">
                    <span class="step-number">Step 01</span>
                    <h3>Secure Authentication</h3>
                    <p>Register a unique username and password or sign in with your existing account. All user plans and expenses are isolated and securely encrypted.</p>
                </div>
                <div class="step-img-box">
                    <img src="img/login and register.jpg" alt="Login and Registration" />
                </div>
            </div>

            <!-- Step 2 -->
            <div class="step-card">
                <div class="step-info">
                    <span class="step-number">Step 02</span>
                    <h3>Create Monthly Budget Plan</h3>
                    <p>Select your target month, enter the year, and set your total budget. The application immediately calculates the optimal daily spending allowance based on the number of days in that month.</p>
                </div>
                <div class="step-img-box">
                    <img src="img/Dashboard.png" alt="Dashboard & Plan Creation" />
                </div>
            </div>

            <!-- Step 3 -->
            <div class="step-card">
                <div class="step-info">
                    <span class="step-number">Step 03</span>
                    <h3>Edit & Manage Budget Plans</h3>
                    <p>Need to increase or decrease your monthly allocation? Edit your plan anytime. Future days are instantly recomputed to match the revised budget.</p>
                </div>
                <div class="step-img-box">
                    <img src="img/Edit plan.png" alt="Edit Plan Interface" />
                </div>
            </div>

            <!-- Step 4 -->
            <div class="step-card">
                <div class="step-info">
                    <span class="step-number">Step 04</span>
                    <h3>Daily Spend Recording & Skip Day</h3>
                    <p>Log transactions with custom notes and categories. If you don't spend anything, use the Skip Day feature to roll your daily budget into future days.</p>
                </div>
                <div class="step-img-box">
                    <img src="img/Spend Amount.png" alt="Spend Entry" />
                </div>
            </div>

            <!-- Step 5 -->
            <div class="step-card">
                <div class="step-info">
                    <span class="step-number">Step 05</span>
                    <h3>Timeline History & Day Breakdown</h3>
                    <p>Inspect your daily financial history, review spending patterns against target budgets, and drill down into individual day transaction notes.</p>
                </div>
                <div class="step-img-box">
                    <img src="img/History.png" alt="History View" />
                </div>
            </div>
        </div>

        <!-- Tech Stack -->
        <div class="section-header">
            <h2 class="section-title">
                <span>💻</span> Technology Architecture
            </h2>
        </div>

        <div style="background: #ffffff; border: 1px solid var(--border-light); border-radius: var(--radius-md); padding: 24px; box-shadow: var(--shadow-sm); margin-bottom: 30px;">
            <p style="color: var(--text-muted); font-size: 14px;">This application adheres to the classic Model-View-Controller (MVC) architectural design pattern:</p>
            <div class="tech-badges">
                <span class="tech-pill">☕ Java 8 / 11 / 17 / 21</span>
                <span class="tech-pill">🌐 Java EE Servlets 4.0</span>
                <span class="tech-pill">📄 JSP & JSTL 1.2</span>
                <span class="tech-pill">🐬 MySQL 8.0 Database</span>
                <span class="tech-pill">🔌 JDBC Connector/J</span>
                <span class="tech-pill">🔐 BCrypt Password Hashing</span>
                <span class="tech-pill">📦 Apache Tomcat 9.0+</span>
                <span class="tech-pill">🎨 Vanilla Modern CSS</span>
            </div>
        </div>
    </main>

    <jsp:include page="footer.jsp" />
</body>
</html>
