# 🔐 Spring OAuth 2.0 & OpenID Connect Demo

A **multi-module Spring Boot** project demonstrating a complete **OAuth 2.0 + OpenID Connect (OIDC)** ecosystem — Authorization Server, Resource Servers, and Client Application — all wired together with Spring Security.

> Built for learning purposes. Ideal for understanding OAuth 2.0 flows including Authorization Code, Client Credentials, and JWT-based client authentication.

---

## 📐 Architecture

```
┌─────────────────────┐       ┌──────────────────────────┐       ┌───────────────────────────────┐
│  spring-auth-client │──────▶│  spring-auth-server      │       │ spring-resource-server        │
│  (OAuth2 Client)    │       │  (Authorization Server)  │       │ (Resource Server + OAuth2     │
│  Port: 8080         │       │  Port: 9000              │       │  Client for service-to-service)│
│                     │       │                          │       │ Port: 8083                    │
│  • OAuth2 Login     │       │  • Issues JWT Tokens     │       │                               │
│  • WebClient calls  │───────┼──────────────────────────┼──────▶│  • Validates JWTs             │
│  • OIDC User Info   │       │  • OIDC Discovery        │       │  • @PreAuthorize              │
└─────────────────────┘       │  • Multiple Clients      │       │  • Calls Shopping Service     │
                              │  • Permission-based      │       └───────────────────────────────┘
                              │    Token Customization   │                      │
                              └──────────────────────────┘                      │ client_credentials
                                                                                │ (private_key_jwt /
                                                                                │  client_secret_jwt)
                                                                                ▼
                                                                ┌───────────────────────────────┐
                                                                │ spring-resource-server-shopping│
                                                                │ (Resource Server)              │
                                                                │ Port: 8084                     │
                                                                │                                │
                                                                │  • Validates JWTs              │
                                                                │  • Product API                 │
                                                                └───────────────────────────────┘
```

**Flows:**

1. **Authorization Code Flow (User Login)**
   - User accesses protected endpoint on Client (`:8080`)
   - Redirects to Authorization Server (`:9000`) for login
   - User authenticates → Auth Server issues Authorization Code
   - Client exchanges code for Access Token + ID Token
   - Client calls Resource Server (`:8083`) with Access Token

2. **Client Credentials Flow (Service-to-Service)**
   - Resource Server (`:8083`) acts as OAuth2 Client
   - Authenticates to Auth Server using `private_key_jwt` or `client_secret_jwt`
   - Obtains token and calls Shopping Resource Server (`:8084`)

---

## 🧩 Modules

| Module                            | Port   | Description                                                                    |
|-----------------------------------|--------|--------------------------------------------------------------------------------|
| `spring-auth-server`              | `9000` | Authorization Server — issues OAuth2/OIDC tokens, manages clients & users      |
| `spring-auth-client`              | `8080` | OAuth2 Client — user login via Authorization Code grant, calls Resource Server |
| `spring-resource-server`          | `8083` | Resource Server + OAuth2 Client — validates JWTs, calls external services      |
| `spring-resource-server-shopping` | `8084` | Resource Server — exposes product APIs, validates JWTs                         |

---

## 🛠️ Tech Stack

| Technology                             | Version                                               |
|----------------------------------------|-------------------------------------------------------|
| Java                                   | 21                                                    |
| Spring Boot                            | 3.5.13                                                |
| Spring Authorization Server            | via `spring-boot-starter-oauth2-authorization-server` |
| Spring Security OAuth2 Client          | via `spring-boot-starter-oauth2-client`               |
| Spring Security OAuth2 Resource Server | via `spring-boot-starter-oauth2-resource-server`      |
| Spring WebFlux (WebClient)             | via `spring-boot-starter-webflux`                     |
| Lombok                                 | 1.18.42                                               |
| Maven (Multi-module)                   | 3.x                                                   |

---

## 🚀 Getting Started

### Prerequisites

- **Java 21** or later
- **Maven 3.8+** (or use the included Maven Wrapper)

### Build

```bash
# From the root directory
./mvnw clean install
```

### Run (start all four servers)

Start each module **in order** in separate terminals:

```bash
# 1. Authorization Server (port 9000)
./mvnw spring-boot:run -pl spring-auth-server

# 2. Shopping Resource Server (port 8084)
./mvnw spring-boot:run -pl spring-resource-server-shopping

# 3. Resource Server (port 8083)
./mvnw spring-boot:run -pl spring-resource-server

# 4. Client Application (port 8080)
./mvnw spring-boot:run -pl spring-auth-client
```

> **Windows?** Use `mvnw.cmd` instead of `./mvnw`.

---

## 🔑 Default Credentials

### Users (for login)

| Username | Password | Roles           |
|----------|----------|-----------------|
| `abc`    | `abc`    | `USER`          |
| `admin`  | `admin`  | `USER`, `ADMIN` |

### OAuth2 Clients

| Client ID                          | Secret                                      | Grant Types                           | Auth Method                                                    |
|------------------------------------|---------------------------------------------|---------------------------------------|----------------------------------------------------------------|
| `myclient`                         | `secret`                                    | `authorization_code`, `refresh_token` | `client_secret_basic`                                          |
| `resource-server-client`           | `secret123`                                 | `client_credentials`                  | `private_key_jwt`, `client_secret_post`, `client_secret_basic` |
| `resource-server-client-symmetric` | `my-oauth2-client-secret-min-32-chars-here` | `client_credentials`                  | `client_secret_jwt`                                            |

### Role-Based Permissions

| Role         | Permissions     |
|--------------|-----------------|
| `ROLE_USER`  | `read`          |
| `ROLE_ADMIN` | `read`, `write` |

---

## 📡 API Endpoints

### Authorization Server (`:9000`)

| Method | Endpoint                            | Description                           |
|--------|-------------------------------------|---------------------------------------|
| `GET`  | `/`                                 | Welcome page                          |
| `POST` | `/auth/login`                       | Direct token endpoint (dev mode only) |
| `GET`  | `/.well-known/openid-configuration` | OIDC Discovery                        |
| `GET`  | `/oauth2/authorize`                 | Authorization endpoint                |
| `POST` | `/oauth2/token`                     | Token endpoint                        |
| `GET`  | `/oauth2/jwks`                      | JWK Set (public keys)                 |
| `GET`  | `/userinfo`                         | OIDC UserInfo                         |

### Client Application (`:8080`)

| Method | Endpoint     | Auth Required | Description                     |
|--------|--------------|---------------|---------------------------------|
| `GET`  | `/`          | ❌             | Public home page                |
| `GET`  | `/api/hello` | ✅             | Greets authenticated user       |
| `GET`  | `/resource`  | ✅             | Proxies call to Resource Server |

### Resource Server (`:8083`)

| Method | Endpoint                | Auth Required | Description                                      |
|--------|-------------------------|---------------|--------------------------------------------------|
| `GET`  | `/resource`             | ✅ JWT         | Generic protected resource                       |
| `GET`  | `/read-resource`        | ✅ `read`      | Requires `read` permission                       |
| `GET`  | `/write-resource`       | ✅ `write`     | Requires `write` permission                      |
| `GET`  | `/products`             | ✅ `read`      | Fetches products from Shopping Service           |
| `GET`  | `/private_key_jwt/jwks` | ❌             | Exposes client public keys for `private_key_jwt` |

### Shopping Resource Server (`:8084`)

| Method | Endpoint        | Auth Required | Description          |
|--------|-----------------|---------------|----------------------|
| `GET`  | `/api/products` | ✅ `read`      | Returns product list |

---

## 🧪 Testing the Flow

### Browser-Based (Authorization Code Flow)

1. Open **http://localhost:8080/api/hello**
2. Redirected to Auth Server login page at `:9000`
3. Log in with `abc` / `abc`
4. Redirected back to client:
   ```
   Hello abc, Welcome to the Spring Security Oauth2 Client Application
   ```

### Client → Resource Server

1. Open **http://localhost:8080/resource** (after login)
2. Client fetches access token, calls `:8083/products`
3. Resource Server calls Shopping Service (`:8084`) using `client_credentials`
4. Returns product list

### Direct Token Request (Dev Mode)

> Enable with: `auth-server.dev-mode.enable-direct-login=true`

```bash
curl -X POST http://localhost:9000/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "client_id": "myclient",
    "client_secret": "secret",
    "username": "abc",
    "password": "abc"
  }'
```

**Response:**
```json
{
  "access_token": "eyJraWQ...",
  "token_type": "Bearer",
  "expires_in": 3600
}
```

### Call Resource Server with Token

```bash
curl http://localhost:8083/resource \
  -H "Authorization: Bearer <access_token>"
```

---

## 🔐 OAuth2 Client Authentication Methods

This project demonstrates all four standard client authentication methods:

| Method                | Sent As                         | Security | Used By                            | Best For                                       |
|-----------------------|---------------------------------|----------|------------------------------------|------------------------------------------------|
| `client_secret_basic` | `Authorization` header (Base64) | ⭐⭐       | `myclient`                         | Simple integrations, legacy systems            |
| `client_secret_post`  | Request Body (Plain text)       | ⭐⭐       | `resource-server-client`           | When header can't be modified                  |
| `client_secret_jwt`   | Signed JWT (Symmetric)          | ⭐⭐⭐      | `resource-server-client-symmetric` | Enhaced secret, shared secret                  |
| `private_key_jwt`     | Signed JWT (Asymmetric)         | ⭐⭐⭐⭐     | `resource-server-client`           | Highest security, no shared secret transmitted |

### Key Differences
- **Secret vs. Key**: `basic`, `post`, and `secret_jwt` use a shared secret. `private_key_jwt` uses a private key that never leaves the client.
- **Transmitted Credentials**: `basic` and `post` send the secret itself. `jwt` methods only send a signed assertion, making them immune to interception of the secret.
- **Recommended**: Use `private_key_jwt` for microservices/M2M and `client_secret_basic` for simple web apps.

---

## 🔍 Key Features

### Authorization Server

- **Externalized Configuration** — All clients, users, and permissions in `application.yml`
- **Multiple OAuth2 Clients** — Supports different authentication methods per client
- **Permission-based Token Customization** — Adds `permissions` claim based on user roles or client identity
- **OIDC Support** — OpenID Connect discovery, UserInfo endpoint
- **Dev Mode** — Optional direct login endpoint for testing

### Resource Server (`:8083`)

- **Dual Role** — Acts as both Resource Server and OAuth2 Client
- **JWT Client Authentication** — Supports `private_key_jwt` and `client_secret_jwt`
- **JWK Source Options** — Runtime key generation or keystore file
- **Permission-based Authorization** — Uses custom JWT converter for `permissions` claim
- **Service-to-Service Calls** — Calls external services with `client_credentials` grant

### Client Application

- **OAuth2 Login** — Authorization Code flow
- **WebClient Integration** — Calls Resource Server with access token
- **Custom OIDC User Service** — Extracts roles from token claims

---

## ⚙️ Configuration Highlights

### Authorization Server — Registering Clients

Configure clients with different authentication methods in `application.yml`:

```yaml
auth-server:
  clients:
    # Client using client_secret_basic (default for most web apps)
    - client-id: myclient
      client-secret: secret
      client-authentication-methods: client_secret_basic
      grant-types:
        - authorization_code
        - refresh_token
      redirect-uris:
        - http://localhost:8080/login/oauth2/code/myclient
      scopes: [openid, profile, email]

    # Client supporting multiple methods including private_key_jwt
    - client-id: resource-server-client
      client-secret: secret123
      client-authentication-methods:
        - private_key_jwt
        - client_secret_post
        - client_secret_basic
      grant-types: [client_credentials]
      scopes: [openid, profile, email]
      jwks:
        uri: http://localhost:8083/private_key_jwt/jwks  # Public key endpoint
        signing-algorithm: RS256

    # Client using client_secret_jwt (symmetric)
    - client-id: resource-server-client-symmetric
      client-secret: my-oauth2-client-secret-min-32-chars-here  # Min 32 chars for HS256
      client-authentication-methods: [client_secret_jwt]
      grant-types: [client_credentials]
      scopes: [openid, profile, email]
      jwks:
        signing-algorithm: HS256
```

### Resource Server — OAuth2 Client Configuration

Configure the Resource Server to authenticate using JWT-based methods:

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          internal-resource-server-id:
            client-id: resource-server-client-symmetric
            client-secret: my-oauth2-client-secret-min-32-chars-here
            authorization-grant-type: client_credentials
            client-authentication-method: client_secret_jwt  # or private_key_jwt
        provider:
          internal-resource-server-id:
            issuer-uri: http://localhost:9000

app:
  client:
    registration-id: internal-resource-server-id
    authentication-method: client_secret_jwt
    
    # For client_secret_jwt
    symmetric-key:
      algorithm: HmacSHA256
      jws-algorithm: HS256
    
    # For private_key_jwt
    jwk:
      source: runtime  # or 'keystore'
      keystore:
        path: classpath:client-keystore.p12
        password: password
        alias: spring-resource-server
        type: PKCS12
```

---

## 📚 Learning Resources

- [Spring Authorization Server Reference](https://docs.spring.io/spring-authorization-server/reference/)
- [Spring Security OAuth2 Client](https://docs.spring.io/spring-security/reference/servlet/oauth2/client/index.html)
- [Spring Security OAuth2 Resource Server](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/index.html)
- [OAuth 2.0 Specification (RFC 6749)](https://datatracker.ietf.org/doc/html/rfc6749)
- [OpenID Connect Core 1.0](https://openid.net/specs/openid-connect-core-1_0.html)

---

## 📝 License

This project is for **educational purposes** only.

