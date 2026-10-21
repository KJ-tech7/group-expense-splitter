# Group Expense Splitter

A web-based application built with Java and Spring Boot to manage shared group expenses, calculate individual balances, and suggest settlements that minimize the number of payments needed.

## Features

- **Group Management:** Create groups and add members.
- **Expense Management:** Record and view expenses for each group.
- **Balance Calculation:** Calculate how much each member owes or should receive.
- **Suggested Settlements:** Generate payments between members to settle outstanding balances.
- **Web Dashboard:** Manage groups and expenses through a browser-based interface.
- **Persistent Storage:** Store application data using PostgreSQL.

## Tech Stack

- **Language:** Java
- **Backend:** Spring Boot
- **Database:** PostgreSQL
- **Data Access:** Spring Data JPA / Hibernate
- **Frontend:** HTML, CSS, JavaScript, Thymeleaf (where applicable)
- **Build Tool:** Maven
- **Testing:** JUnit and Spring Boot Test

## Getting Started

### Prerequisites

Install the following:

- Java 21
- PostgreSQL
- Git (optional, for cloning the repository)
- Maven, or use the included Maven Wrapper

### 1. Clone the repository

```bash
git clone https://github.com/KJ-tech7/group-expense-splitter.git
cd group-expense-splitter
```

### 2. Create the database

Create a PostgreSQL database named `expense_splitter`, or update the database name to match your local configuration.

### 3. Configure database credentials

Open `src/main/resources/application.properties`.

Set your database URL and username, and provide the database password through the `DB_PASSWORD` environment variable.

**PowerShell:**

```powershell
$env:DB_PASSWORD
