# 🔐 Spring OAuth 2.0 & OpenID Connect Demo

A **multi-module Spring Boot** project demonstrating a complete **OAuth 2.0 + OpenID Connect (OIDC)** ecosystem — Authorization Server, Resource Server, and Client Application — all wired together with Spring Security.

> Built for learning purposes. Ideal for understanding how OAuth 2.0 Authorization Code flow works end-to-end with Spring Boot 3.x.

---

## 📐 Architecture

```
┌─────────────────────┐       ┌──────────────────────────┐       ┌───────────────────────┐
│  spring-auth-client │──────▶│  spring-auth-server      │       │ spring-resource-server │
│  (OAuth2 Client)    │       │  (Authorization Server)  │       │  (Resource Server)     │
│  Port: 8080         │       │  Port: 9000              │       │  Port: 8083            │
│                     │       │                          │       │                        │
│  • OAuth2 Login     │       │  • Issues JWT Tokens     │       │  • Validates JWTs      │
│  • WebClient calls  │───────┼──────────────────────────┼──────▶│  • @PreAuthorize       │
│  • OIDC User Info   │       │  • OIDC Discovery        │       │  • Protected Endpoints │
└─────────────────────┘       └──────────────────────────┘       └───────────────────────┘
```

**Flow:**
1. User accesses a protected endpoint on the **Client** (`:8080`)
2. Client redirects to the **Authorization Server** (`:9000`) for login
3. User authenticates → Auth Server issues an **Authorization Code**
4. Client exchanges the code for **Access Token + ID Token**
5. Client uses the Access Token to call the **Resource Server** (`:8083`)
6. Resource Server validates the JWT and returns the protected resource

---

## 🧩 Modules

| Module | Port | Description |
|---|---|---|
| **`spring-auth-server`** | `9000` | Spring Authorization Server — issues OAuth2/OIDC tokens, manages clients & users |
| **`spring-auth-client`** | `8080` | OAuth2 Client — performs login via Authorization Code grant, calls Resource Server |
| **`spring-resource-server`** | `8083` | OAuth2 Resource Server — exposes protected REST APIs, validates JWT tokens |

---

## 🛠️ Tech Stack

| Technology | Version |
|---|---|
| Java | 21 |
| Spring Boot | 3.5.x |
| Spring Authorization Server | via `spring-boot-starter-oauth2-authorization-server` |
| Spring Security OAuth2 Client | via `spring-boot-starter-oauth2-client` |
| Spring Security OAuth2 Resource Server | via `spring-boot-starter-oauth2-resource-server` |
| Spring WebFlux (WebClient) | via `spring-boot-starter-webflux` |
| Lombok | 1.18.42 |
| Maven (Multi-module) | 3.x |

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

### Run (start all three servers)

Start each module **in order** in separate terminals:

```bash
# 1. Authorization Server (port 9000)
./mvnw spring-boot:run -pl spring-auth-server

# 2. Resource Server (port 8083)
./mvnw spring-boot:run -pl spring-resource-server

# 3. Client Application (port 8080)
./mvnw spring-boot:run -pl spring-auth-client
```

> **Windows?** Use `mvnw.cmd` instead of `./mvnw`.

---

## 🔑 Default Credentials

### User (for login)
| Field | Value |
|---|---|
| Username | `abc` |
| Password | `abc` |

### OAuth2 Client
| Field | Value |
|---|---|
| Client ID | `myclient` |
| Client Secret | `secret` |
| Auth Method | `client_secret_basic` |
| Grant Type | `authorization_code`, `refresh_token` |
| Redirect URI | `http://localhost:8080/login/oauth2/code/myclient` |
| Scopes | `openid`, `profile`, `email`, `user.read`, `user.write` |

---

## 📡 API Endpoints

### Authorization Server (`:9000`)

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/` | Welcome page |
| `POST` | `/auth/login` | Programmatic token endpoint (client credentials + user auth) |
| `GET` | `/.well-known/openid-configuration` | OIDC Discovery |
| `GET` | `/oauth2/authorize` | Authorization endpoint |
| `POST` | `/oauth2/token` | Token endpoint |
| `GET` | `/oauth2/jwks` | JWK Set (public keys) |
| `GET` | `/userinfo` | OIDC UserInfo |

### Client Application (`:8080`)

| Method | Endpoint | Auth Required | Description |
|---|---|---|---|
| `GET` | `/` | ❌ | Public home page |
| `GET` | `/api/hello` | ✅ | Greets the authenticated user |
| `GET` | `/resource` | ✅ | Proxies call to Resource Server's `/read-resource` |

### Resource Server (`:8083`)

| Method | Endpoint | Auth Required | Description |
|---|---|---|---|
| `GET` | `/resource` | ✅ Bearer Token | Generic protected resource |
| `GET` | `/read-resource` | ✅ `SCOPE_user.read` | Requires `user.read` scope |
| `GET` | `/write-resource` | ✅ `user.write` | Requires `user.write` authority |

---

## 🧪 Testing the Flow

### Browser-Based (Authorization Code Flow)

1. Open **http://localhost:8080/api/hello**
2. You'll be redirected to the Auth Server login page at `:9000`
3. Log in with `abc` / `abc`
4. You'll be redirected back to the client and see:  
   `Hello abc, Welcome to the Spring Security Oauth2 Client Application`

### Client → Resource Server

1. Open **http://localhost:8080/resource** (after logging in)
2. The Client fetches the access token from the session, calls `:8083/read-resource`, and returns:  
   `This resource is available to users with 'user.read' authority.`

### Direct Token Request (Programmatic)

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

### Call Resource Server directly with a token

```bash
curl http://localhost:8083/resource \
  -H "Authorization: Bearer <access_token>"
```

---

## 🏗️ Project Structure

```
spring-auth-server/                          # Root (parent POM)
├── pom.xml                                  # Parent POM with dependency management
│
├── spring-auth-server/                      # 🔐 Authorization Server
│   ├── pom.xml
│   └── src/main/java/oauthserver/
│       ├── SpringAuthServerApplication.java
│       ├── config/
│       │   └── AuthorizationServerConfig.java   # OAuth2 server config, clients, users, JWK
│       └── controller/
│           ├── HomeController.java              # GET /
│           └── AuthController.java              # POST /auth/login
│
├── spring-auth-client/                      # 🖥️ OAuth2 Client
│   ├── pom.xml
│   └── src/main/java/com/ss/ac/
│       ├── SpringAuthClientApplication.java
│       ├── config/
│       │   ├── WebSecurityConfig.java           # Security filter chain, OAuth2 login
│       │   └── JwtDecoderFactory.java           # Cached JWT decoder per registration
│       ├── controller/
│       │   ├── HomeController.java              # GET /
│       │   ├── HelloController.java             # GET /api/hello
│       │   └── ResourceClientController.java    # GET /resource (proxy)
│       └── service/
│           ├── WebClientService.java            # WebClient with Bearer token
│           └── CustomOidcUserService.java       # Extracts roles from access token
│
└── spring-resource-server/                  # 🛡️ Resource Server
    ├── pom.xml
    └── src/main/java/com/ss/rs/
        ├── SpringResourceServerApplication.java
        ├── config/
        │   └── ResourceConfig.java              # JWT resource server config
        └── controller/
            └── ResourceController.java          # Protected endpoints
```

---

## 🔍 Key Implementation Details

### Authorization Server (`spring-auth-server`)

- **In-memory client registration** — `myclient` with BCrypt-encoded secret
- **In-memory user store** — single user `abc` with `ROLE_USER`
- **RSA key pair** generated at startup for JWT signing (2048-bit)
- **Custom JWT claims** — adds `authorities` and `bank-id` to access tokens
- **OIDC enabled** — supports OpenID Connect discovery, UserInfo endpoint
- **Dual security filter chains** — one for OAuth2 protocol endpoints, one for form login

### Client Application (`spring-auth-client`)

- **OAuth2 Login** via Authorization Code Grant
- **WebClient** calls the Resource Server with the access token from the OAuth2 session
- **CustomOidcUserService** — extracts roles from `resource_access` claim in the access token (Keycloak-compatible pattern)
- **JwtDecoderFactory** — caches `JwtDecoder` instances per client registration for efficient token validation

### Resource Server (`spring-resource-server`)

- **JWT validation** against the Auth Server's issuer URI (`http://localhost:9000`)
- **Method-level security** with `@PreAuthorize` annotations
- **Scope-based access control** — e.g., `SCOPE_user.read`, `user.write`

---

## ⚙️ Configuration Overview

| Property | Auth Server | Client | Resource Server |
|---|---|---|---|
| `server.port` | `9000` | `8080` | `8083` |
| Issuer URI | `http://localhost:9000` | — | `http://localhost:9000` |
| Security logging | `DEBUG` | `DEBUG` | `DEBUG` |

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

