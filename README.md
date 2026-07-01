# Alsothify Backend

An authentication and user-profile backend built with Spring Boot 3.5 (Java 21) + PostgreSQL. JWT-based auth (Bearer header or `httpOnly` cookie), with email-OTP account verification and password reset.

## Stack

- Java 21, Spring Boot 3.5.6
- Spring Web, Spring Data JPA, Spring Security, Spring Mail
- PostgreSQL
- JJWT (`io.jsonwebtoken`) for JWT
- Lombok, Bean Validation

## Getting Started

### 1. Requirements
- JDK 21
- PostgreSQL
- An SMTP account for sending emails (e.g. Gmail with an app password)

### 2. Environment setup

Create a `.env` file in the project root (picked up via `spring.config.import`):

```env
DATABASE_URL=jdbc:postgresql://localhost:5432/alsothify
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=postgres

MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
MAIL_FORM_EMAIL=your-email@gmail.com

JWT_SECRET=<base64 string used as the JWT signing key (HS256)>
```

> `application.yml` also hardcodes `mail.smtp.from` — replace it with your own address if needed.

The DB schema is created/updated automatically (`ddl-auto: update`); there are no separate migrations in the project.

### 3. Build and run

```bash
./mvnw spring-boot:run
```

or

```bash
./mvnw clean package
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

The app starts on `http://localhost:8080`; all endpoints are under the `/api/v1` prefix (set via `server.servlet.context-path`).

### 4. CORS

Only `http://localhost:5173` is allowed as an origin by default (see `SecurityConfig.corsConfigurationSource`). Update it in code if you deploy the frontend elsewhere.

## Authentication

- The JWT is passed either via the `Authorization: Bearer <token>` header, or via the `httpOnly` `jwt` cookie (set automatically on login).
- Tokens are valid for 10 hours and signed with HS256 using the `JWT_SECRET` secret.
- Public (no token required) paths: `/login`, `/register`, `/send-reset-otp`, `/reset-password`, `/logout`. Everything else requires authentication.

## REST API

Base prefix: `/api/v1`

### Auth

| Method | Path | Access | Description |
|---|---|---|---|
| `POST` | `/login` | public | Login with `{"email", "password"}`. Returns the JWT in the body and sets an `httpOnly` `jwt` cookie |
| `GET` | `/is-authenticated` | any | Checks whether the current request is authenticated (`true`/`false`) |
| `POST` | `/send-reset-otp?email=...` | public | Sends a password-reset OTP to the email (valid for 15 minutes) |
| `POST` | `/reset-password` | public | Resets the password: `{"email", "otp", "newPassword"}` |
| `POST` | `/send-otp` | authenticated | Sends an OTP to verify the current account (valid for 24 hours) |
| `POST` | `/verify-otp` | authenticated | Verifies the account: `{"otp": "..."}` |

### Profile

| Method | Path | Access | Description |
|---|---|---|---|
| `POST` | `/register` | public | Registration: `{"name", "email", "password"}`. A welcome email is sent after the profile is created |
| `GET` | `/profile` | authenticated | Returns the current user's profile |

## Database schema

```
tbl_users (UserEntity)
├── id (PK, identity)
├── user_id (unique, UUID string)
├── name
├── email (unique)
├── password (bcrypt hash)
├── verify_otp
├── is_account_verified
├── verify_otp_expire_at (epoch millis)
├── reset_otp
├── reset_otp_expire_at (epoch millis)
├── created_at (set automatically)
└── updated_at (updated automatically)
```

There's no separate roles/tokens table — the whole data model is a single users table.

## How it works

1. **Registration** (`/register`) creates a user with `isAccountVerified = false`, the password is hashed with BCrypt, and a welcome email is sent.
2. **Login** (`/login`) checks email/password via `AuthenticationManager`, issues a JWT, and sets it as a cookie.
3. **Account verification**: an authenticated user requests an OTP (`/send-otp`), receives it by email, and confirms it (`/verify-otp`).
4. **Password reset**: a logged-out user requests an OTP by email (`/send-reset-otp`), then submits email + OTP + new password (`/reset-password`).
5. Every protected request goes through `JwtRequestFilter`, which extracts the token from the `Authorization` header or the `jwt` cookie, validates it, and populates the `SecurityContext`.

## Known limitations (as-is)

- `sendVerifyOtp`/`verifyOtp` don't null-check before calling `.toString()` on `request.get("otp")` — a missing `otp` field in the request body can throw a `NullPointerException` instead of a proper `400` error.
- `AppUserDetailsService` doesn't grant any roles/authorities to the user (`new ArrayList<>()`), so there's effectively no role model in the project — just "authenticated / not authenticated".
- The `mail.smtp.from` value is hardcoded in `application.yml`.
- Logout is disabled in Spring Security (`.logout(AbstractHttpConfigurer::disable)`), yet `/logout` is listed as a public path — there's no actual logout endpoint in the controllers.
