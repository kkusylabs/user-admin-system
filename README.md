# User Administration Backend

A Spring Boot backend for managing users, roles, and departments with a focus on **authorization, data integrity, and real-world business rules**.

---

## Overview

This project models a typical internal admin system where user management is constrained by role- and department-based permissions.

It emphasizes:

* Centralized authorization logic
* Capability-driven API responses
* Partial update (PATCH-style) handling
* Enforcement of critical business invariants

---

## Architecture

The application follows a layered structure:

* **Controllers** → HTTP layer (request/response)
* **Services** → Business workflows (`UserService`)
* **Authorization Service** → Permission rules (`UserAuthorizationService`)
* **Repositories** → Persistence (Spring Data JPA)
* **DTOs / Mappers** → API contracts and transformations

### Key Design Choice

Authorization is handled in a dedicated service:

`UserAuthorizationService`

This service is responsible for:

* determining what actions an actor can perform
* validating create/update/delete requests
* producing capability objects for UI consumption

This keeps business workflows clean and avoids scattering permission logic.

---

## Authorization Model

The system defines three roles:

### ADMIN

* Full access to all users and departments
* Can assign any role
* Cannot remove the last active admin

### MANAGER

* Can manage **basic users** in their own department
* Can only assign the `USER` role
* Must belong to an active department

### USER

* No administrative privileges

---

## Business Rules

The system enforces several invariants:

* A user cannot delete their own account
* The system must always have at least **one active admin**
* Departments must be **active** to be assigned
* Managers are restricted to **their own department**
* Role assignment is restricted based on actor permissions

These rules are enforced in the service layer—not just at the API boundary.

---

## Capabilities-Based API

Responses include **capabilities** describing what the current user can do.

Example:

```json
{
  "canEditProfile": true,
  "canEditRole": false,
  "canEditDepartment": false
}
```

This allows the frontend to:

* dynamically enable/disable UI elements
* avoid duplicating permission logic
* stay consistent with backend rules

---

## Update Semantics

User updates follow **PATCH-style behavior**:

* Only fields present in the request are applied
* Missing fields are left unchanged
* Validation and authorization are applied per-field

This is implemented using `JsonNullable` to distinguish:

* “not provided” vs
* “explicitly set to null”

---

## API Overview

### Users

* `GET /api/users`
  Returns a paginated list with per-item capabilities

* `GET /api/users/{id}`
  Returns a single user with capabilities

* `GET /api/users/{id}/edit`
  Returns editable fields and allowed options

* `POST /api/users`
  Creates a user (with role/department validation)

* `PUT /api/users/{id}`
  Applies partial updates with field-level authorization

* `DELETE /api/users/{id}`
  Deletes a user with safeguards

---

### Authentication

* `POST /api/auth/login`
  Returns a JWT

* `GET /api/auth/me`
  Returns the current authenticated user

---

## Testing

The project includes:

* Integration tests for controllers
* Repository tests
* Coverage of authorization rules and edge cases

### Requirements

* Docker (required for Testcontainers)

### Run tests

From the **backend module**:

```bash
cd backend
./mvnw test
```

---

## Running the Project

### Requirements

* Java 21+
* Docker (for PostgreSQL)

### Run locally (Spring Boot via Maven)

From the **backend module**:
```bash
cd backend
```

1. Start PostgreSQL
   ```bash
   docker compose up -d postgres
   ```

2. Start the application:
   ```bash
   ./mvnw spring-boot:run
   ```
---
### Run full stack with Docker Compose

Runs both:
* Spring Boot app
* PostgreSQL

   ```bash
   cd backend
   docker compose up --build
   ```

## Using Swagger UI

After starting the application, open Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

Swagger UI lets you view and test the API endpoints from your browser.

### Log in
1. Open `POST /api/auth/login`
2. Click **Try it out**
3. Enter credentials:
   ```json
   {
      "username: "admin",
      "password: "admin"
   }
   ```
4. Click **Execute**
5. Copy the `accessToken` from the response

### Authorize request
1. Click **Authorize** near the top of the page
2. Paste the `accessToken` in the text field
3. Click **Authorize**
4. Close the dialog

You can now call protected endpoints from Swagger UI.

### Verify authentication

Open `GET /api/auth/me` and click **Execute**

If Authorization is working, it returns the currently authenticated user

### Example workflow

1. Log in
2. Authorize using your token
3. Call `GET /api/departments` to view departments
4. Call `POST /api/departments` (admin only) to create one
---

## Database

* PostgreSQL

---

## Notable Implementation Details

* **Authorization is centralized**

    * Improves maintainability and testability

* **Capabilities returned from backend**

    * Simplifies frontend logic

* **Explicit validation layer for updates**

    * Prevents invalid partial updates

* **Invariant enforcement**

    * e.g., last active admin protection

---

## Author

Kevin Kusy
GitHub: https://github.com/kkusylabs
