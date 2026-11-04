# 💳 Consumer Finance System - Backend

A **Java 21 + Spring Boot microservices backend** implementing the complete Consumer Finance lifecycle.

Customer Registration
        ↓
KYC & Onboarding
        ↓
Finance Application
        ↓
Credit Assessment
        ↓
Approval
        ↓
Disbursement
        ↓
EMI Schedule
        ↓
Repayment
        ↓
Notifications
```
---

# 🏗️ Architecture

                         🌐 React + Vite
                        localhost:5173
                              │
                    REST / JSON + JWT
                              │
                              ▼
                    🚪 API Gateway :8080
                              │
                ┌─────────────┴─────────────┐
                │                           │
                ▼                           ▼
        🔎 Eureka Server :8761       ⚖️ LoadBalancer
          Service Discovery         Instance Selection
                │                           │
                └─────────────┬─────────────┘
                              │
       ┌──────────────────────┼──────────────────────────┐
       │          │           │          │               │
       ▼          ▼           ▼          ▼               ▼

   🔐 Auth     👤 Customer   💼 Finance   📊 Credit    ✅ Approval
    :8081        :8082        :8083       :8084        :8085
      │            │            │           │            │
      ▼            ▼            ▼           ▼            ▼
   auth_db     customer_db  application_db credit_db  approval_db


             💸 Disbursement      🧮 EMI / Repayment
                  :8086                 :8087
                    │                     │
                    ▼                     ▼
             disbursement_db            emi_db


                         🔔 Notification
                              :8088
                                │
                                ▼
                        notification_db
```

### Infrastructure

```text
React
   ↓
API Gateway
   ↓
Eureka Service Discovery
   ↓
Spring Cloud LoadBalancer
   ↓
Microservice
   ↓
Service-Owned MySQL Database
```

> **Note:** Business requests do not travel through Eureka.  
> Eureka provides service-discovery information, and Spring Cloud LoadBalancer selects the appropriate registered service instance.

---

# ⚙️ Microservices

| Service | Port | Database |
|---|---:|---|
| 🔎 Eureka Server | `8761` | - |
| 🚪 API Gateway | `8080` | - |
| 🔐 Authentication | `8081` | `auth_db` |
| 👤 Customer | `8082` | `customer_db` |
| 💼 Finance Application | `8083` | `application_db` |
| 📊 Credit Assessment | `8084` | `credit_assessment_db` |
| ✅ Approval | `8085` | `approval_db` |
| 💸 Disbursement | `8086` | `disbursement_db` |
| 🧮 EMI / Repayment | `8087` | `emi_db` |
| 🔔 Notification | `8088` | `notification_db` |

---

# 🔄 Service-to-Service Communication

The backend uses **Spring Cloud OpenFeign** for synchronous communication.

💼 Finance Application
        │
        └──────► 👤 Customer

📊 Credit Assessment
        │
        └──────► 💼 Finance Application

✅ Approval
        │
        └──────► 📊 Credit Assessment

💸 Disbursement
        │
        └──────► ✅ Approval

🧮 EMI / Repayment
        ├──────► 💸 Disbursement
        └──────► 👤 Customer


🔔 Notification
        │
        └──────► 👤 Customer
```

Each service accesses **only its own database**.
Cross-service data is accessed through secured APIs, not direct database joins.
---

# 🔐 Security

The system uses:

Spring Security
+
JWT
+
Role-Based Authorization
```

### Authentication Flow

User Login
    ↓
Authentication Service
    ↓
BCrypt Password Verification
    ↓
JWT Generated
    ↓
Frontend
    ↓
Authorization: Bearer <JWT>
    ↓
API Gateway
    ↓
Protected Microservice
    ↓
JWT Validation
    ↓
Role / Ownership Check
```

Protected microservices independently validate JWTs.

---

# 👥 Roles

👤 CUSTOMER
🛡️ ADMIN
📊 CREDIT_ANALYST
✅ CREDIT_MANAGER
🏦 LOAN_OFFICER
```

### CUSTOMER
- Profile & onboarding
- KYC
- Loan products
- Finance applications
- EMI schedule
- Repayment
- Notifications

### ADMIN
- KYC review
- Internal users
- Loan products
- Applications
- Credit assessments
- Approvals
- Disbursements
- EMI schedule generation
- Internal notifications

### CREDIT_ANALYST
- Submitted application queue
- Credit assessment
- `ELIGIBLE`
- `NOT_ELIGIBLE`

### CREDIT_MANAGER
- Assessment review
- Approval / rejection
- Disbursement

### LOAN_OFFICER
Authenticated role with limited functionality in the current capstone scope.

---

# 🗄️ Database Design

The application uses the **Database-per-Service** pattern.

```text
Authentication      → auth_db

Customer            → customer_db

Finance Application → application_db

Credit Assessment   → credit_assessment_db

Approval            → approval_db

Disbursement        → disbursement_db

EMI / Repayment     → emi_db

Notification        → notification_db
```

Cross-service relationships use IDs:

customerId
applicationId
creditAssessmentId
approvalId
disbursementId
loanAccountId
emiId
repaymentId
```

The database design was reviewed for normalization and is broadly aligned with:

✅ 1NF
✅ 2NF
✅ 3NF
```

---

# 💰 Financial Processing

All monetary calculations use:

BigDecimal
```

The EMI service manages:

Principal
Interest Rate
Monthly EMI
Principal Component
Interest Component
Outstanding Balance
Amount Paid
Repayment
```

Current payment mode: SIMULATED
---

# 🛡️ Duplicate Protection

The application prevents:

✅ Duplicate Credit Assessment

✅ Duplicate Approval

✅ Duplicate Disbursement

✅ Duplicate EMI Schedule

✅ Repayment of an already-paid EMI
```

---

# 🧪 Testing
Final backend verification:

✅ 10 / 10 modules compiled

✅ 330 tests passed

✅ 0 failures

✅ 0 errors

✅ 0 skipped
```

Additional verification:

✅ Eureka registration

✅ Postman API testing

✅ JWT role-security testing

✅ Frontend integration testing

✅ Database normalization review

---

# 🚀 Running Locally

## Requirements

Java 21
MySQL
Git
Maven

---

## Start Services

Recommended order:

1️⃣ Eureka Server

2️⃣ Authentication Service

3️⃣ Customer Service

4️⃣ Finance Application Service

5️⃣ Credit Assessment Service

6️⃣ Approval Service

7️⃣ Disbursement Service

8️⃣ EMI Service

9️⃣ Notification Service

🔟 API Gateway
```

---

## Useful URLs

### Eureka Dashboard
http://localhost:8761
```

### API Gateway
http://localhost:8080
```

### React Frontend
http://localhost:5173

---

# 🌿 Final Backend Branch

feature/java-modernization
```
This branch contains the final:

✅ Java 21 modernization
✅ Java Records
✅ Lombok improvements
✅ Constructor-injection modernization
✅ Secure assessment queue
✅ Complete backend integration
```

# 🔮 Future Enhancements

📨 Kafka / RabbitMQ

⚡ Redis Idempotency

💳 Real Payment Gateway

📧 Email / SMS Providers

📈 OpenTelemetry + Grafana

🗃️ Flyway / Liquibase

🐳 Docker

☸️ Kubernetes

☁️ Cloud Deployment
```

---

# 👨‍💻 Author
**Chaitanya Kassetty**
**Rambabu Sunkara

Consumer Finance System  
Full-Stack Microservices Capstone Project

