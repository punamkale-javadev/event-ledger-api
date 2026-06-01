# Event Ledger API

A Spring Boot REST API for ingesting financial transaction events, supporting idempotent event processing, account balance computation, and retrieval of transaction history.

## Features

* Submit transaction events
* Idempotent event processing using `eventId`
* Retrieve events by ID
* Retrieve account event history ordered by timestamp
* Compute account balance from transaction history
* Validation and error handling
* Embedded H2 database
* Automated integration tests

---

## Technology Stack

* Java 21
* Spring Boot 3.x
* Spring Data JPA
* H2 Database
* Maven
* JUnit 5
* MockMvc

---

## Prerequisites

Before running the application, ensure the following are installed:

* Java 21 (or compatible version configured for the project)
* Maven 3.9+

Verify installation:

```bash
java -version
mvn -version
```

---

## Installation

Clone the repository:

```bash
git clone <repository-url>
cd event-ledger-api
```

Install dependencies and build:

```bash
mvn clean install
```

---

## Running the Application

Start the application:

```bash
mvn spring-boot:run
```

The application will start on:

```text
http://localhost:8080
```

---

## H2 Database Console

The H2 console is available at:

```text
http://localhost:8080/h2-console
```

Connection details:

```text
JDBC URL: jdbc:h2:file:./data/eventledgerdb
Username: sa
Password:
```

If using an in-memory database:

```text
JDBC URL: jdbc:h2:mem:eventledgerdb
```

---

## API Endpoints

### Create Event

```http
POST /events
```

### Get Event By ID

```http
GET /events/{id}
```

### Get Events For Account

```http
GET /events?account={accountId}
```

### Get Account Balance

```http
GET /accounts/{accountId}/balance
```

---

## Example Event Payload

```json
{
  "eventId": "evt-001",
  "accountId": "acct-123",
  "type": "CREDIT",
  "amount": 150.00,
  "currency": "USD",
  "eventTimestamp": "2026-05-15T14:02:11Z",
  "metadata": {
    "source": "mainframe-batch",
    "batchId": "B-9042"
  }
}
```

---

## Idempotency

The API guarantees idempotent event processing.

Submitting the same `eventId` multiple times:

* Does not create duplicate records
* Does not alter account balance
* Returns the original event

Response codes:

* `201 Created` – New event stored
* `200 OK` – Duplicate event submission

---

## Running Tests

Execute all tests:

```bash
mvn test
```

The test suite covers:

* Idempotency (duplicate event submissions)
* Out-of-order event arrival
* Balance computation accuracy
* Input validation
* Error handling

---

## Project Structure

```text
src
├── main
│   ├── java
│   │   ├── controller
│   │   ├── service
│   │   ├── repository
│   │   ├── entity
│   │   ├── dto
│   │   ├── exception
│   │   └── enums
│   └── resources
│       └── application.yml
└── test
    └── java
```

---

## Assumptions

* Event uniqueness is determined by `eventId`.
* Events are returned in ascending order of `eventTimestamp`.
* Account balance is calculated as:

```text
Balance = Sum(CREDIT amounts) - Sum(DEBIT amounts)
```

* Metadata is stored and returned but is not used in balance calculations.
* Currency conversion is out of scope.

```
```
