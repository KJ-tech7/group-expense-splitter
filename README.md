# Group Expense Splitter

A web-based application built with Java and Spring Boot to manage shared group expenses, calculate individual balances, and suggest settlements that minimize the number of payments needed.

## Features

- Create groups and add members.
- Record and manage group expenses.
- Calculate how much each member owes or should receive.
- Generate suggested settlements to reduce the number of payments.
- Manage groups and expenses through a web dashboard.
- Store application data using PostgreSQL.

## Technology Stack

- Java 21
- Spring Boot
- Spring Data JPA and Hibernate
- PostgreSQL
- HTML, CSS, JavaScript, and Thymeleaf
- Maven
- JUnit and Spring Boot Test

## Getting Started

### Prerequisites

- Java 21
- PostgreSQL

### 1. Clone the repository

```bash
git clone https://github.com/KJ-tech7/group-expense-splitter.git
cd group-expense-splitter
```

### 2. Configure PostgreSQL

Create a PostgreSQL database named `expense_splitter`.

Set your database password through the `DB_PASSWORD` environment variable in PowerShell:

```powershell
$env:DB_PASSWORD = "YOUR_LOCAL_DATABASE_PASSWORD"
```

Replace the placeholder with your local password. Never commit passwords to GitHub.

### 3. Run the application

On Windows, run:

```powershell
.\mvnw.cmd spring-boot:run
```

Open http://localhost:8082 in your browser.

## How to Use

1. Create a group and add its members.
2. Add expenses and record who paid.
3. View the calculated balances.
4. Review the suggested settlements.

## Project Structure

```text
group-expense-splitter/
├── src/
│   ├── main/
│   │   ├── java/
│   │   └── resources/
│   │       ├── templates/
│   │       └── application.properties
│   └── test/
├── pom.xml
├── mvnw
├── mvnw.cmd
└── README.md
```

## Future Improvements

- User authentication and authorization
- Expense categories and filtering
- Additional automated tests
- Responsive design improvements
- Deployment to a hosting platform

## Author

Keshav

GitHub: https://github.com/KJ-tech7

---

Developed as a Java and Spring Boot project demonstrating backend development, database integration, and expense-settlement logic.
