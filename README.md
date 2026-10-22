# budgetApp

REST API for managing a personal budget — tracking incomes and expenses assigned to accounts.

## Requirements

- Java 25
- Maven
- Docker (to run PostgreSQL)

## Running locally

### 1. Environment configuration

Copy the example configuration file and fill in the variables:

```bash
cp .env.example .env
```

Contents of `.env.example`:

```env
DB_PORT=5432
POSTGRES_PASSWORD=budgetapppass
POSTGRES_DB=budgetAppDB
POSTGRES_USER=user2
```

### 2. Starting the database

```bash
docker compose up -d
```

### 3. Starting the application

```bash
./mvnw spring-boot:run
```

The application starts on `http://localhost:8080`. Flyway creates the database schema automatically on the first run.

API documentation (Swagger UI) is available at: `http://localhost:8080/swagger-ui.html`

---

## Endpoints

### Accounts — `/api/account`

| Method | Path | Description | Codes |
|--------|------|-------------|-------|
| `GET` | `/api/account/all` | List all accounts | 200 |
| `GET` | `/api/account/{id}` | Account details with balance | 200, 404 |
| `POST` | `/api/account` | Create a new account | 201, 400 |
| `DELETE` | `/api/account/{id}` | Delete an account (only if it has no transactions) | 204, 404, 409 |
| `GET` | `/api/account/{id}/summary` | Summary of incomes and expenses | 200, 404 |
| `GET` | `/api/account/{id}/transactions/export` | Export the account's transactions to a CSV file | 200, 404 |

**POST `/api/account` — example body:**
```json
{
  "name": "Main account",
  "balance": 1000.00
}
```

### Transactions — `/api/transaction`

| Method | Path | Description | Codes |
|--------|------|-------------|-------|
| `GET` | `/api/transaction?accountId=` | List transactions with optional filters | 200 |
| `POST` | `/api/transaction` | Add a transaction (the balance is updated automatically) | 201, 400, 404 |
| `DELETE` | `/api/transaction/{id}` | Delete a transaction (the balance is reverted) | 204, 404 |

**GET — optional filter parameters:**
```
?accountId=1&from=2024-03-01&to=2024-03-31&category=Food
```

**POST `/api/transaction` — example body:**
```json
{
  "accountId": 1,
  "categoryId": 2,
  "type": "EXPENSE",
  "amount": 45.50,
  "description": "Groceries",
  "date": "2024-03-02T12:30:00"
}
```

**GET `/api/account/{id}/summary` — example response:**
```json
{
  "totalIncome": 6500.00,
  "totalExpenses": 380.50,
  "expensesByCategory": [
    { "categoryName": "Food", "total": 45.50 },
    { "categoryName": "Transport", "total": 15.00 }
  ]
}
```

---

## Tests

```bash
./mvnw test
```

### `TransactionServiceTest`

Mockito unit tests verifying the account balance update logic:

| Test | Verifies |
|------|----------|
| `addTransaction_income_increasesBalance` | Adding an INCOME increases the balance |
| `addTransaction_expense_decreasesBalance` | Adding an EXPENSE decreases the balance |
| `addTransaction_accountNotFound_throwsException` | Missing account → 404 |
| `addTransaction_categoryNotFound_throwsException` | Missing category → 404 |
| `deleteTransaction_income_revertsBalance` | Deleting an INCOME reverts the balance |
| `deleteTransaction_expense_revertsBalance` | Deleting an EXPENSE reverts the balance |
| `deleteTransaction_notFound_throwsException` | Missing transaction → 404 |

### `AccountServiceTest`

Unit tests verifying the account deletion conditions and the correctness of the summary:

| Test | Verifies |
|------|----------|
| `deleteAccount_accountNotFound_throwsNotFoundException` | Missing account → 404 |
| `deleteAccount_hasTransactions_throwsConflictException` | Account with transactions → 409 |
| `deleteAccount_noTransactions_deletesSuccessfully` | An empty account is deleted correctly |
| `getAccountSummary_accountNotFound_throwsNotFoundException` | Missing account → 404 |
| `getAccountSummary_returnsCorrectTotals` | Correct income and expense totals |

### `TransactionServiceRaceConditionTest`

Integration tests running against a real PostgreSQL instance (Testcontainers, see
`CommonTestConfiguration`) that verify the pessimistic lock guarding the account balance
(`AccountRepository.findByIdWithLock`):

| Test | Verifies |
|------|----------|
| `secondWithdrawalWaitsForLockAndSeesCommittedBalance` | A second transaction blocks on `SELECT ... FOR UPDATE` until the first one commits, and then reads the committed balance |
| `concurrentDebitsPreserveBalance` | 32 threads debiting the same account concurrently leave no lost updates |

### `AccountControllerTest`

`@WebMvcTest` tests verifying incoming request validation and response status codes:

| Test | Verifies |
|------|----------|
| `addAccount_blankName_returns400WithMessage` | Blank name → 400 with a message |
| `addAccount_nullName_returns400` | Missing name → 400 |
| `addAccount_negativeBalance_returns400WithMessage` | Negative balance → 400 with a message |
| `addAccount_valid_returns201` | Valid request → 201 |
| `getAccountDetails_notFound_returns404WithMessage` | Missing account → 404 with a message |
| `deleteAccount_hasTransactions_returns409WithMessage` | Account with transactions → 409 with a message |
| `deleteAccount_notFound_returns404` | Missing account → 404 |

### `TransactionControllerTest`

| Test | Verifies |
|------|----------|
| `addTransaction_missingAccountId_returns400WithMessage` | Missing `accountId` → 400 with a message |
| `addTransaction_zeroAmount_returns400WithMessage` | Amount of 0 → 400 with a message |
| `addTransaction_missingType_returns400WithMessage` | Missing `type` → 400 with a message |
| `addTransaction_missingDate_returns400WithMessage` | Missing `date` → 400 with a message |
| `deleteTransaction_notFound_returns404WithMessage` | Missing transaction → 404 with a message |

---

## Project structure

```
src/main/java/dev/fascodes/budgetApp/
├── account/
│   ├── controller/      # AccountController
│   ├── dto/             # requests and responses
│   ├── exception/       # AccountNotFoundException, AccountHasTransactionsException
│   ├── mapper/          # AccountMapper
│   ├── model/           # Account
│   ├── repository/      # AccountRepository
│   └── service/         # AccountService
├── transaction/
│   ├── controller/      # TransactionController
│   ├── dto/             # requests and responses
│   ├── exception/       # TransactionNotFoundException, CategoryNotFoundException
│   ├── mapper/          # TransactionMapper
│   ├── model/           # Transaction, Category, TransactionType
│   ├── repository/      # TransactionRepository, CategoryRepository, TransactionSpecification
│   └── service/         # TransactionService
└── common/              # GlobalExceptionHandler, ResourceNotFoundException, OpenApiConfig

src/main/resources/
├── application.properties
└── db/migration/
    └── V1__schema.sql   # database schema
```
