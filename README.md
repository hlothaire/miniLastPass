# Mini LastPass

A minimal password-vault style application with a Spring Boot 3 backend and Angular 17 frontend. Users can register, authenticate with JWT cookies, store encrypted secrets, and reveal them on demand with audit logging and rate limiting.

## Key features

- **Secure credential management:** Save entries with a title, username, URL, and a server-side encrypted secret.
- **Modern authentication:** Sign up and log in with Argon2id-protected email/password credentials, then keep the session via HttpOnly JWT cookies.
- **On-demand reveal:** Display a secret only when needed, with audit logging and request rate limiting.
- **Angular web interface:** Browse the vault, add or update entries, and spin up the frontend quickly with `npm start`.
- **Flexible deployment:** Use the default H2 database or switch to Postgres using the provided Docker scripts.

```bash
# From the repository root
export JWT_SECRET=please-change-me
mvn spring-boot:run
```


```bash
cd frontend
npm install
npm start
```
