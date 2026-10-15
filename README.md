# Courier JDBC Desktop Application

High-performance, secure Java 17 + Swing + JDBC + MySQL / Embedded H2 application for courier parcel tracking and delivery operations.

---

## ⚡ Quick Start (Instant Demo)

You can run the entire system instantly without configuring a database:

```bash
git clone https://github.com/sunilkumarreddy-bale/courier-tracker1.git
cd courier-tracker1
./run-demo.sh
```

Or run directly with Java:
```bash
java -jar target/courier-jdbc-1.0.0.jar --demo
```

The application automatically uses an embedded H2 engine in MySQL compatibility mode if no external MySQL database is specified, bootstrapping a complete delivery management simulation with zero setup.

---

## 🚀 Running with MySQL 8+

To connect to an external production MySQL server, configure the database first:

```sql
CREATE DATABASE courier_jdbc CHARACTER SET utf8mb4;
CREATE USER 'courier'@'localhost' IDENTIFIED BY 'replace-with-a-unique-password';
GRANT SELECT, INSERT, UPDATE, CREATE, REFERENCES ON courier_jdbc.* TO 'courier'@'localhost';
```

Export your environment variables:

```bash
export DB_URL='jdbc:mysql://localhost:3306/courier_jdbc?connectionTimeZone=UTC&forceConnectionTimeZoneToSession=true'
export DB_USERNAME='courier'
export DB_PASSWORD='replace-with-a-unique-password'
export ADMIN_EMAIL='admin@yourdomain.com'
export ADMIN_PASSWORD='replace-with-a-unique-admin-password'
```

*PowerShell users: use `$env:DB_USERNAME='courier'` (same pattern for all variables).*

Compile and launch the graphical Swing desktop client:

```bash
mvn package
java -jar target/courier-jdbc-1.0.0.jar
```

---

## 📋 Features & Workflow

1. **Administrator Access**: Secure salted PBKDF2 authentication with 210,000 hash iterations.
2. **Fleet Management**: Create delivery agents with isolated role permissions.
3. **Parcel Registration**: Generate unique UUID tracking codes with contact info, addresses, weight, and delivery SLA dates.
4. **Agent Dispatch**: Assign shipments to specific agents. Agents see only their assigned workload.
5. **Real-time Lifecycle Transitions**:
   `REGISTERED` ➔ `PICKED_UP` ➔ `IN_TRANSIT` ➔ `OUT_FOR_DELIVERY` ➔ `DELIVERED` / `DELIVERY_FAILED`
   *(Failed deliveries can be retried or returned. Picked-up/in-transit parcels can be marked returned. Terminal shipments cannot progress further).*
6. **Zero-Trust Public Tracking**: Customers can track parcel progress without logging in. Strict privacy enforcement ensures recipient personal addresses and internal delivery notes remain confidential; only public city/hub waypoints and UTC timestamps are displayed.
7. **Agent Notifications**: Persistent in-app alerts on dispatch assignment and shipment updates.
8. **Live Metrics Dashboard**: Real-time totals, active counts, unassigned alerts, overdue shipments, and delivery success percentage.

---

## 🧪 Automated Integration Tests

Run the full integration test suite:

```bash
mvn test
```

Or run the standalone executable integration suite directly:

```bash
mvn test-compile dependency:copy-dependencies -DincludeScope=test
java -cp 'target/classes:target/test-classes:target/dependency/*' courier.StoreSmokeTest
```

---

## 📂 Architecture & Files

- `App.java`: Swing presentation layer, thread-safe asynchronous JDBC background workers, and automated on-screen demo runner.
- `Store.java`: JDBC operations, transaction management, pessimistic locking (`FOR UPDATE`), optimistic version checks, audit logging, and notification storage.
- `Passwords.java`: Salted PBKDF2-HMAC-SHA256 password hashing with constant-time hash comparisons.
- `schema.sql`: DDL for users, shipments, append-only event logs, and alerts.
- `StoreSmokeTest.java`: 25 integration checks covering authentication, authorization, registration, state machine transitions, transactions, privacy, and dashboard totals.
- `run-demo.sh`: One-click build and live on-screen demonstration runner.
