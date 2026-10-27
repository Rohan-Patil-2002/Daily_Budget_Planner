<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Features & Overview - Daily Budget Planner</title>
    <meta name="description" content="Explore key features of Daily Budget Planner: daily spending limits, rollover days, and dynamic budget calculations.">
    <link rel="stylesheet" href="Css/headerfooter.css">
    <link rel="stylesheet" href="Css/dashboardStyle.css">
    <style>
        .menu-hero {
            background: linear-gradient(135deg, #064e3b 0%, #047857 50%, #059669 100%);
            border-radius: var(--radius-lg);
            padding: 48px 36px;
            color: #ffffff;
            margin-bottom: 36px;
            box-shadow: 0 15px 30px rgba(6, 78, 59, 0.25);
            display: flex;
            align-items: center;
            justify-content: space-between;
            flex-wrap: wrap;
            gap: 24px;
        }
        .menu-hero-text {
            max-width: 650px;
        }
        .menu-hero-badge {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            background: rgba(255, 255, 255, 0.2);
            border: 1px solid rgba(255, 255, 255, 0.3);
            color: #ffffff;
            padding: 5px 14px;
            border-radius: var(--radius-full);
            font-size: 12px;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 0.05em;
            margin-bottom: 16px;
        }
        .menu-hero-title {
            font-family: 'Outfit', sans-serif;
            font-size: 32px;
            font-weight: 800;
            line-height: 1.2;
            margin-bottom: 12px;
        }
        .menu-hero-desc {
            font-size: 16px;
            color: var(--primary-100);
            line-height: 1.6;
            margin-bottom: 24px;
        }
        .features-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
            gap: 24px;
            margin-bottom: 40px;
        }
        .feature-card {
            background: #ffffff;
            border: 1px solid var(--border-light);
            border-radius: var(--radius-md);
            padding: 24px;
            box-shadow: var(--shadow-sm);
            transition: var(--transition);
        }
        .feature-card:hover {
            transform: translateY(-4px);
            box-shadow: var(--shadow-md);
            border-color: var(--primary-400);
        }
        .feature-icon {
            font-size: 32px;
            margin-bottom: 14px;
            display: inline-block;
        }
        .feature-title {
            font-family: 'Outfit', sans-serif;
            font-size: 18px;
            font-weight: 700;
            color: var(--text-main);
            margin-bottom: 8px;
        }
        .feature-desc {
            font-size: 14px;
            color: var(--text-muted);
            line-height: 1.6;
        }
        .media-showcase {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(360px, 1fr));
            gap: 24px;
            margin-top: 30px;
        }
        .media-card {
            background: #ffffff;
            border: 1px solid var(--border-light);
            border-radius: var(--radius-md);
            overflow: hidden;
            box-shadow: var(--shadow-sm);
        }
        .media-card img {
            width: 100%;
            height: auto;
            display: block;
            margin: 0;
            border-radius: 0;
            box-shadow: none;
        }
        .media-caption {
            padding: 14px 18px;
            font-size: 13px;
            font-weight: 600;
            color: var(--text-muted);
            background: var(--bg-surface-alt);
        }
    </style>
</head>
<body>
    <jsp:include page="header.jsp" />

    <main class="page-container">
        <!-- Hero Section -->
        <div class="menu-hero">
            <div class="menu-hero-text">
                <div class="menu-hero-badge">💡 Smarter Financial Habit</div>
                <h1 class="menu-hero-title">Take Control of Your Money, One Day at a Time</h1>
                <p class="menu-hero-desc">
                    Daily Budget Planner automatically breaks down your monthly budget into realistic, achievable daily spending limits. Stop guessing and start knowing exactly how much you can spend every day.
                </p>
                <div style="display: flex; gap: 12px; flex-wrap: wrap;">
                    <a href="dashboard" class="btn btn-primary" style="background: #ffffff; color: var(--primary-800); box-shadow: 0 4px 14px rgba(0,0,0,0.15);">
                        <span>📊</span> Open My Dashboard
                    </a>
                    <a href="AddPlan.jsp" class="btn" style="background: rgba(255,255,255,0.2); color: #ffffff; border: 1px solid rgba(255,255,255,0.4);">
                        <span>➕</span> Create New Plan
                    </a>
                </div>
            </div>
        </div>

        <!-- Features Grid -->
        <div class="section-header">
            <h2 class="section-title">
                <span>🌟</span> Core Features & Capabilities
            </h2>
        </div>

        <div class="features-grid">
            <div class="feature-card">
                <span class="feature-icon">📆</span>
                <h3 class="feature-title">Automated Daily Limits</h3>
                <p class="feature-desc">Every monthly budget is distributed across the exact days of the selected month, giving you a crystal-clear daily allowance target.</p>
            </div>

            <div class="feature-card">
                <span class="feature-icon">⚡</span>
                <h3 class="feature-title">Dynamic Adjustments</h3>
                <p class="feature-desc">When you spend more or less than your target, future daily budgets recalculate in real time to keep your total monthly budget safe.</p>
            </div>

            <div class="feature-card">
                <span class="feature-icon">🔄</span>
                <h3 class="feature-title">Skip Day Rollover</h3>
                <p class="feature-desc">Zero spend today? Use the Skip Day button to roll today's allowance into tomorrow, instantly boosting your upcoming daily budgets.</p>
            </div>

            <div class="feature-card">
                <span class="feature-icon">📜</span>
                <h3 class="feature-title">Day-wise History</h3>
                <p class="feature-desc">View an organized timeline of your daily expenses, complete with custom notes, status badges, and itemized transaction logs.</p>
            </div>

            <div class="feature-card">
                <span class="feature-icon">📊</span>
                <h3 class="feature-title">Visual Progress Meters</h3>
                <p class="feature-desc">Color-coded budget meters alert you when spending approaches critical levels so you never accidentally overspend.</p>
            </div>

            <div class="feature-card">
                <span class="feature-icon">🔐</span>
                <h3 class="feature-title">Secure & Confidential</h3>
                <p class="feature-desc">Your financial data is protected with session-based authentication and BCrypt password encryption for total privacy.</p>
            </div>
        </div>

        <!-- Showcase Media -->
        <div class="section-header">
            <h2 class="section-title">
                <span>📸</span> Application Visual Preview
            </h2>
        </div>

        <div class="media-showcase">
            <div class="media-card">
                <img src="img/1.jpeg" alt="Budget Planning Overview" />
                <div class="media-caption">Smart Daily Expense Allocation Concept</div>
            </div>
            <div class="media-card">
                <img src="img/2.jpeg" alt="Financial Tracking Console" />
                <div class="media-caption">Dynamic Budget Tracking & History System</div>
            </div>
        </div>
    </main>

    <jsp:include page="footer.jsp" />
</body>
</html>
