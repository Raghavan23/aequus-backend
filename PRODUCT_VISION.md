# Aequus — Product Vision & Architecture Statement

**Aequus is an AI-powered Personal Financial Operating System that helps people understand, plan, and optimize their financial lives—not just track expenses.**

Unlike traditional budgeting apps that focus on recording the past, Aequus combines financial management, intelligent planning, and conversational AI to help users make better financial decisions for the future.

Our mission is to become the most trusted AI financial companion for individuals by transforming raw financial data into personalized insights, recommendations, and action plans.

---

## 🎯 Product Mission & North Star

Enable every individual to confidently manage their money through intelligent automation, predictive analytics, and AI-driven financial guidance.

Aequus reduces financial anxiety by answering one simple question:
> **"What should I do with my money next?"**

---

## 💡 The Problem & Our Solution

### Problems with Traditional Apps
- Too much tedious manual transaction entry.
- Static, rigid budgets that fail when unexpected life events happen.
- Raw charts and tables that leave users guessing what to do.
- Descriptive only ("what happened") rather than predictive ("what will happen").

### Our Solution
- **Automate** financial organization with smart categorization and receipt intelligence.
- **Explain** spending trends with contextual financial intelligence.
- **Predict** cash flow, runway, and financial trajectories.
- **Advise** dynamically with conversational RAG-based AI and adaptive budget rebalancing.

---

## 🏛️ System Architecture

Aequus is built as a **Modular Monolith** using **Domain-Driven Design (DDD)** in the Java Backend and a clean **Feature-based Component Architecture** in Angular.

```
com.aequus
│
├── core             # Common utilities, base entities, error handling, security filter chain
├── user             # Auth (JWT/OAuth2), user profiles, preferences, currencies
├── transaction      # Accounts, payment methods, transactions, categories, recurring rules
├── budget           # Dynamic & envelope budgeting, category allocation, rollover
├── goal             # Target savings, milestones, automatic allocations
├── investment       # Portfolio tracking, asset allocation, ROI calculations
├── report           # Analytics aggregation, cash flow projections, net-worth timeline
├── notification     # Push, email, and in-app smart alerts (bill reminders, anomalies)
└── ai               # LLM integration (Gemini/OpenAI), RAG engine, receipt OCR, prompt orchestrator
```

Each backend module encapsulates:
- **API (Controllers)** — REST endpoints
- **Service Layer** — Business logic & domain events
- **Repository Layer** — Spring Data JPA / PostgreSQL queries
- **Domain Entities & Value Objects**
- **DTOs & Mappers** (MapStruct)
- **Domain Exceptions**

---

## 🤖 AI Roadmap & Capabilities

1. **Conversational Financial Advisor (RAG & LLM)**: Ask questions in plain English (*"Can I afford a MacBook next month?"*) powered by secure, anonymized financial context retrieval.
2. **Smart Receipt OCR & Auto-Categorization**: Merchant extraction and automatic categorization from receipts and statements.
3. **Predictive Cash Flow & Anomaly Detection**: Forecast month-end balances and alert on price hikes, duplicate charges, or erratic spikes.
4. **Dynamic Budget Rebalancing**: One-click adaptive adjustments to absorb unexpected expenses without breaking long-term goals.
