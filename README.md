# Aurora SIEM Backend 🛡️

A Security Information and Event Management (SIEM) system backend designed to collect, analyze, and alert on security events from various sources.

## Table of Contents 📚

- [Project Title & Badges](#project-title--badges)
- [Description](#description)
- [Features](#features) ✨
- [Tech Stack](#tech-stack) 💻
- [Project Structure](#project-structure) 📁
- [Installation](#installation) ⚙️
- [Usage](#usage) 🚀
- [API Reference](#api-reference) 🔗
- [Contributing](#contributing) 🤝
- [License](#license) 📄
- [Important Links](#important-links) 🌐
- [Footer](#footer) 🌟

## Project Title & Badges 🏆

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-F7DF1E?style=for-the-badge&logo=springboot&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-336791?style=for-the-badge&logo=postgresql&logoColor=white)

## Description 📝

The Aurora SIEM Backend is a robust Java-based application built with the Spring Boot framework. It serves as the core of a Security Information and Event Management system. The primary responsibilities include ingesting log events from various sources, defining and executing correlation rules for threat detection, generating alerts, and managing incidents. It utilizes a PostgreSQL database for persistent storage and adheres to a layered architecture for maintainability and scalability.

## Features ✨

*   **Log Event Management:** Ingests and stores log events with details like source, message, severity, and raw data.
*   **Rule Engine:** Allows definition of rules for detecting specific patterns or conditions within log events.
*   **Alerting System:** Generates alerts based on triggered rules, categorizing them by severity and status.
*   **Incident Management:** Groups related alerts into incidents for streamlined investigation and resolution.
*   **Source Tracking:** Manages information about log sources (agents, hostnames, IPs, OS types).
*   **User Management:** Basic user management with roles (Analyst, Admin, Viewer).
*   **Data Persistence:** Utilizes Spring Data JPA with PostgreSQL for reliable data storage.
*   **DTOs and Entities:** Clear separation of API data transfer objects (DTOs) and domain entities.
*   **Validation:** Implements input validation for API requests.

## Tech Stack 💻

*   **Language:** Java
*   **Framework:** Spring Boot
*   **Database:** PostgreSQL
*   **Build Tool:** Maven
*   **Libraries:** Lombok, Jackson, Jakarta Validation

## Project Structure 📁

The project follows a modular structure, common in Spring Boot applications:

```
aurora_server_backend/
├── aurora-agent/         # Module for agent-related functionalities (if any)
├── aurora-api/           # Defines API contracts, DTOs
├── aurora-domain/        # Contains core domain entities and models
├── aurora-persistence/   # Data access layer using Spring Data JPA
├── aurora-server/        # Main application module with services and controllers
└── pom.xml               # Parent POM for multi-module project
```

## Installation ⚙️

**Prerequisites:**

*   Java Development Kit (JDK) 21 or later
*   Maven
*   PostgreSQL Database

**Steps:**

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/dzoraj/aurora_server_backend.git
    cd aurora_server_backend
    ```

2.  **Configure Database:**
    *   Ensure PostgreSQL is running.
    *   Create a database for the Aurora SIEM system.
    *   Update the `application.properties` or `application.yml` file within the `aurora-server` module (or a shared configuration file if applicable) with your PostgreSQL connection details:
        ```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/your_db_name
sprng.datasource.username=your_db_username
spring.datasource.password=your_db_password
spring.jpa.hibernate.ddl-auto=update # or create-drop for development
```
    *Note: The specific property file location and name might vary. For a multi-module project, configurations might be managed at the parent or module level.* 

3.  **Build the project:**
    Navigate to the root directory and run:
    ```bash
mvn clean install
```

4.  **Run the application:**
    The main application can be started from the `aurora-server` module:
    ```bash
    cd aurora-server
    mvn spring-boot:run
    ```
    Alternatively, you can build an executable JAR and run it:
    ```bash
mvn package
java -jar target/aurora-server-0.0.1-SNAPSHOT.jar # Adjust artifactId and version as needed
    ```

## Usage 🚀

This backend provides the core logic for a SIEM system. It exposes RESTful APIs to manage security data.

**Typical Workflow:**

1.  **Log Ingestion:** Log events are sent to the `/api/log-events` endpoint (e.g., by an agent).
2.  **Rule Processing:** The backend processes incoming logs against configured rules to detect potential threats.
3.  **Alert Generation:** If a rule is triggered, an alert is created and stored.
4.  **Incident Creation:** Alerts can be correlated and grouped into incidents.
5.  **User Interaction:** Analysts can interact with the system via a frontend application (not included in this backend project) to view logs, alerts, incidents, manage rules, and users.

**Example Use Cases:**

*   **Real-time Threat Detection:** Detect suspicious login attempts or unauthorized access patterns.
*   **Compliance Monitoring:** Ensure adherence to security policies by monitoring log data.
*   **Forensic Analysis:** Investigate security breaches by analyzing historical log events and incidents.

## How to use 💡

The `aurora-server` module contains the main Spring Boot application. The `LogEventService` provides key functionalities for processing log events.

**Example: Creating a Log Event via API (Conceptual - requires a controller implementation)**

```java
// Assume a controller endpoint /api/log-events exists
// POST request to this endpoint with a LogEventRequest body:
{
  "sourceId": "agent-123",
  "message": "Suspicious login attempt from 192.168.1.100",
  "severityId": 3, // Assuming ID for 'HIGH' severity
  "rawData": "{ \"user\": \"admin\", \"ip\": \"192.168.1.100\" }"
}
```

When a log event is created, the `LogEventService` will:
1. Find the `Source` entity corresponding to `agent-123`.
2. Find the `Severity` entity for ID `3`.
3. Save the new `LogEvent` entity to the database.
4. Optionally, evaluate detection rules against this new log event.

## API Reference 🔗

This project defines Data Transfer Objects (DTOs) in the `aurora-api` module that represent the structure of API requests and responses. The `aurora-server` module would typically implement controllers to expose these APIs.

**Key DTOs:**

*   **Requests:** `AlertRequest`, `IncidentRequest`, `LogEventRequest`, `RuleRequest`, `SourceRequest`, `UserRequest`
*   **Responses:** `AlertResponse`, `AlertStatusResponse`, `IncidentResponse`, `LogEventResponse`, `RuleResponse`, `RuleStatusResponse`, `SeverityResponse`, `SourceResponse`, `UserResponse`

**Example API Endpoints (Conceptual):**

*   `POST /api/log-events`: Create a new log event.
*   `GET /api/log-events`: Retrieve a paginated list of log events.
*   `GET /api/log-events/search?keyword=...`: Search log events by keyword.
*   `GET /api/alerts`: Retrieve alerts.
*   `POST /api/rules`: Create a new detection rule.
*   `GET /api/sources`: List registered sources.

## Contributing 🤝

Contributions are welcome! Please feel free to:

*   Fork the repository.
*   Create a new branch for your feature or bug fix.
*   Make your changes and submit a pull request.
*   Please ensure your code follows the project's coding style and includes relevant tests if applicable.

## License 📄

No license information was provided for this repository.

## Important Links 🌐

*   **Repository:** [https://github.com/dzoraj/aurora_server_backend](https://github.com/dzoraj/aurora_server_backend)

## Footer 🌟

This README was generated for the **aurora_server_backend** repository.

**URL:** [https://github.com/dzoraj/aurora_server_backend](https://github.com/dzoraj/aurora_server_backend)
**Author:** dzoraj (and contributors)

Fork this project, give it a star ⭐, and let's build a more secure digital world together! 🚀

For any questions or issues, please refer to the [Issues](https://github.com/dzoraj/aurora_server_backend/issues) tab.


---
**<p align="center">Generated by [ReadmeCodeGen](https://www.readmecodegen.com/)</p>**