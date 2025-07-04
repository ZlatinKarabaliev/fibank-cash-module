
# 💼 Cash Desk Module

A Spring Boot application for managing cashier cash operations (deposits, withdrawals, and balance checks) in BGN and EUR. Designed as part of a backend assignment for Fibank.

## ✅ Features

- Manage operations for multiple cashiers
- Track balances and denominations
- Store transactions in text files
- API key authentication via custom header
- Postman collection included
- Logs all actions using SLF4J

## 🚀 Technologies

- Java 17
- Spring Boot
- Maven
- SLF4J Logging
- File-based persistence

## 🏁 Getting Started

### 📦 Prerequisites
- Java 17+
- Maven 3.8+
- Git (for cloning)

### ▶️ Run the App

```bash
git clone <your-repo-url>
cd fibank-cashdesk
mvn clean install
mvn spring-boot:run
```

## 🔐 Authentication

All API requests must include a custom header:

```
FIB-X-AUTH: f9Uie8nNf112hx8s
```

## 📬 API Endpoints

### ➕ Deposit & Withdrawal

`POST /api/v1/cash/operation`

```json
{
  "cashierName": "MARTINA",
  "currency": "BGN",
  "operationType": "DEPOSIT",
  "denominations": [
    { "value": 10, "count": 10 },
    { "value": 50, "count": 10 }
  ]
}
```

### 📊 Check Balance

`GET /api/v1/cash/balance`

Query params:
- `cashier` (optional)
- `dateFrom`, `dateTo` (optional, format: yyyy-MM-dd)

## 🧪 Postman

Use the included Postman collection and environment files:
- `PostmanCollection.json`
- `PostmanEnvironment.json`

## 📁 Data Files

- `history_<date>.txt` — stores transaction history
- `cash-balances.txt` — stores current balances and denominations

## 🛡️ Security

- Stateless security
- Only requests with valid API key header are processed
- No session or CSRF used

## ✍️ Author

This project was developed as part of a technical challenge for Fibank.
#   f i b a n k - c a s h - m o d u l e  
 