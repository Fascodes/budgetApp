# budgetApp

REST API do zarządzania budżetem osobistym — śledzenie przychodów i wydatków przypisanych do kont.

## Wymagania

- Java 25
- Maven
- Docker (do uruchomienia PostgreSQL)

## Uruchomienie lokalne

### 1. Konfiguracja środowiska

Skopiuj plik z przykładową konfiguracją i uzupełnij zmienne:

```bash
cp .env.example .env
```

Zawartość `.env.example`:

```env
DB_PORT=5432
POSTGRES_PASSWORD=budgetapppass
POSTGRES_DB=budgetAppDB
POSTGRES_USER=user2
```

### 2. Uruchomienie bazy danych

```bash
docker compose up -d
```

### 3. Uruchomienie aplikacji

```bash
./mvnw spring-boot:run
```

Aplikacja startuje na `http://localhost:8080`. Flyway automatycznie tworzy schemat i ładuje dane przykładowe przy pierwszym uruchomieniu.

---

## Endpointy

### Konta — `/api/account`

| Metoda | Ścieżka | Opis | Kody |
|--------|---------|------|------|
| `GET` | `/api/account/all` | Lista wszystkich kont | 200 |
| `GET` | `/api/account/{id}` | Szczegóły konta z saldem | 200, 404 |
| `POST` | `/api/account` | Utwórz nowe konto | 201, 400 |
| `DELETE` | `/api/account/{id}` | Usuń konto (tylko bez transakcji) | 204, 404, 409 |
| `GET` | `/api/account/{id}/summary` | Podsumowanie przychodów i wydatków | 200, 404 |

**POST `/api/account` — przykładowe body:**
```json
{
  "name": "Konto główne",
  "balance": 1000.00
}
```

### Transakcje — `/api/transaction`

| Metoda | Ścieżka | Opis | Kody |
|--------|---------|------|------|
| `GET` | `/api/transaction?accountId=` | Lista transakcji z opcjonalnymi filtrami | 200 |
| `POST` | `/api/transaction` | Dodaj transakcję (saldo aktualizuje się automatycznie) | 201, 400, 404 |
| `DELETE` | `/api/transaction/{id}` | Usuń transakcję (saldo cofa się) | 204, 404 |

**GET — opcjonalne parametry filtrowania:**
```
?accountId=1&from=2024-03-01&to=2024-03-31&category=Food
```

**POST `/api/transaction` — przykładowe body:**
```json
{
  "accountId": 1,
  "categoryId": 2,
  "type": "EXPENSE",
  "amount": 45.50,
  "description": "Zakupy spożywcze",
  "date": "2024-03-02T12:30:00"
}
```

**GET `/api/account/{id}/summary` — przykładowa odpowiedź:**
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

## Testy

Projekt zawiera testy jednostkowe dla logiki biznesowej w serwisach.

Testy jednostkowe z Mockito weryfikujące logikę aktualizacji salda konta:

| Test | Weryfikuje |
|------|-----------|
| `addTransaction_income_increasesBalance` | Dodanie INCOME zwiększa saldo |
| `addTransaction_expense_decreasesBalance` | Dodanie EXPENSE zmniejsza saldo |
| `addTransaction_accountNotFound_throwsException` | Brak konta → 404 |
| `addTransaction_categoryNotFound_throwsException` | Brak kategorii → 404 |
| `deleteTransaction_income_revertsBalance` | Usunięcie INCOME cofa saldo |
| `deleteTransaction_expense_revertsBalance` | Usunięcie EXPENSE cofa saldo |
| `deleteTransaction_notFound_throwsException` | Brak transakcji → 404 |

### `AccountServiceTest`

Testy jednostkowe weryfikujące warunki usunięcia konta oraz poprawność podsumowania:

| Test | Weryfikuje |
|------|-----------|
| `deleteAccount_accountNotFound_throwsNotFoundException` | Brak konta → 404 |
| `deleteAccount_hasTransactions_throwsConflictException` | Konto z transakcjami → 409 |
| `deleteAccount_noTransactions_deletesSuccessfully` | Poprawne usunięcie pustego konta |
| `getAccountSummary_accountNotFound_throwsNotFoundException` | Brak konta → 404 |
| `getAccountSummary_returnsCorrectTotals` | Poprawne sumy przychodów i wydatków |

---

## Struktura projektu

```
src/main/java/dev/fascodes/budgetApp/
├── account/
│   ├── controller/      # AccountController
│   ├── dto/             # żądania i odpowiedzi
│   ├── exception/       # AccountNotFoundException, AccountHasTransactionsException
│   ├── mapper/          # AccountMapper
│   ├── model/           # Account
│   ├── repository/      # AccountRepository
│   └── service/         # AccountService
├── transaction/
│   ├── controller/      # TransactionController
│   ├── dto/             # żądania i odpowiedzi
│   ├── exception/       # TransactionNotFoundException, CategoryNotFoundException
│   ├── mapper/          # TransactionMapper
│   ├── model/           # Transaction, Category, TransactionType
│   ├── repository/      # TransactionRepository, CategoryRepository, TransactionSpecification
│   └── service/         # TransactionService
└── common/              # GlobalExceptionHandler, ResourceNotFoundException

src/main/resources/
├── application.properties
└── db/migration/
    ├── V1__schema.sql   # schemat bazy danych
    └── V2__seed.sql     # przykładowe dane
```
