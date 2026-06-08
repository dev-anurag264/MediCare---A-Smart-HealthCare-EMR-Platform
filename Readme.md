# Smart Healthcare Platform

A production-grade RESTful backend for a Healthcare Appointment and Electronic Medical Records (EMR) system inspired by platforms like Practo and Apollo Health. Built with Java 17 and Spring Boot 3. FrontEnd is still under development, as of now using postman for API testing and debugging.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2 |
| Security | Spring Security + JWT (JJWT) |
| ORM | Spring Data JPA + Hibernate |
| Database | PostgreSQL |
| Build Tool | Maven |
| Utilities | Lombok |
| API Docs | Swagger / OpenAPI 3 |

---

## Features

### Authentication & Authorization
- User registration and login with **JWT Bearer tokens**
- **Role-based access control**: `DOCTOR`, `PATIENT`, `ADMIN`
- BCrypt password hashing (strength 12)
- Global exception handling with consistent error responses
- Soft delete for users (`is_active` flag)

### Appointment Booking
- Doctor profile creation with working hours and slot duration
- Available slot generation based on doctor's schedule
- Appointment booking with **double-booking prevention**:
    - Application-level overlap check inside `@Transactional`
    - `@Version` optimistic locking against race conditions
    - PostgreSQL partial unique index as the final safety net
- **Appointment state machine**: `PENDING : CONFIRMED : COMPLETED / CANCELLED / NO_SHOW`
- Paginated appointment history for patients and doctors

### Electronic Medical Records
- EMR creation only for `COMPLETED` appointments
- Structured **Diagnoses** with ICD-10 codes and severity levels
- **Prescriptions** with dosage, frequency, and duration
- **File upload** (PDF, images) with:
    - MIME type validation
    - File size limits
    - UUID-based stored names (prevents path traversal)
    - Metadata stored in DB, files stored on disk
- Streaming file download

---

## API ENDPOINTS

### Authentication
```
POST   /api/v1/auth/register     Register as PATIENT, DOCTOR, or ADMIN
POST   /api/v1/auth/login        Login and receive JWT token
```

### Users
```
GET    /api/v1/users/me          Get current user profile
GET    /api/v1/users/{id}        Get user by ID              [ADMIN]
GET    /api/v1/users/role/{role} List users by role          [ADMIN]
DELETE /api/v1/users/{id}        Deactivate user             [ADMIN]
```

### Doctors
```
POST   /api/v1/doctors/profile         Create doctor profile   [DOCTOR]
PUT    /api/v1/doctors/profile         Update doctor profile   [DOCTOR]
GET    /api/v1/doctors                 List all doctors
GET    /api/v1/doctors/search          Search by specialty
GET    /api/v1/doctors/{id}/profile    Get doctor profile
GET    /api/v1/doctors/{id}/slots      Get available slots for a date
```

### Appointments
```
POST   /api/v1/appointments                Book appointment         [PATIENT]
GET    /api/v1/appointments/my             My appointments          [PATIENT]
GET    /api/v1/appointments/doctor/schedule Doctor's schedule       [DOCTOR]
GET    /api/v1/appointments/{id}           Get appointment detail
GET    /api/v1/appointments                All appointments         [ADMIN]
PUT    /api/v1/appointments/{id}/confirm   Confirm appointment      [DOCTOR, ADMIN]
PUT    /api/v1/appointments/{id}/complete  Mark completed           [DOCTOR, ADMIN]
PUT    /api/v1/appointments/{id}/no-show   Mark no-show             [DOCTOR, ADMIN]
PUT    /api/v1/appointments/{id}/cancel    Cancel appointment
```

### Medical Records
```
POST   /api/v1/medical-records                        Create EMR           [DOCTOR]
GET    /api/v1/medical-records/{id}                   Get EMR by ID
GET    /api/v1/medical-records/appointment/{id}       Get EMR by appointment
GET    /api/v1/medical-records/patient/{patientId}    Patient EMR history
GET    /api/v1/medical-records/my                     Doctor's created records [DOCTOR]
```

### Documents
```
POST   /api/v1/documents/upload          Upload file (multipart/form-data)
GET    /api/v1/documents/{id}/download   Download file
```

---

## Database Schema

```
users
 └── appointments (patient_id, doctor_id)
 └── doctor_profiles (user_id — shared PK)

appointments
 └── medical_records (one-to-one)
      ├── diagnoses
      ├── prescriptions
      └── medical_documents
```

---

## Roadmap

- [ ] Phase 4 — Email notifications, async processing with `@Async`
- [ ] Phase 5 — Docker, environment configs, structured logging
- [ ] Phase 6 — Redis caching, Kafka events, WebSocket queue updates, CI/CD

---

## Running Tests

```bash
mvn test
```

Tests cover:
- `AuthServiceTest` — registration, duplicate email, email normalization
- `AppointmentServiceTest` — booking, conflict detection, state machine transitions, ownership checks

---

## Author

Built as a portfolio project demonstrating production-grade Spring Boot backend development practices. Still in building phase. 