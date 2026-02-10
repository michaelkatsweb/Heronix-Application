# Heronix Scheduling System - REST API Documentation

**Version:** 1.0.0
**Last Updated:** December 30, 2025
**Base URL:** `http://localhost:9590/api` (Development) | `https://api.heronixedu.com/api` (Production)

## Table of Contents

1. [Authentication](#authentication)
2. [Rate Limiting](#rate-limiting)
3. [API Endpoints](#api-endpoints)
4. [Error Handling](#error-handling)
5. [Security Best Practices](#security-best-practices)

---

## Authentication

The Heronix API supports two authentication methods:

### 1. JWT Token Authentication

**Workflow:**
1. Login with username/password → Receive JWT tokens
2. Use access token in requests
3. Refresh token when access token expires

**Login:**
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "john.doe",
  "password": "password123"
}

Response (200 OK):
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "userId": "john.doe",
    "roles": ["TEACHER", "ADMIN"]
  }
}
```

**Using JWT Token:**
```http
GET /api/attendance-analytics/dashboard
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Token Refresh:**
```http
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Token Expiration:**
- Access Token: 1 hour
- Refresh Token: 7 days

### 2. API Key Authentication

**Create API Key:**
```http
POST /api/api-keys
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "name": "Mobile App Production",
  "description": "API key for mobile application",
  "scopes": ["read:students", "write:grades", "read:attendance"],
  "rateLimit": 5000,
  "expiresAt": "2026-12-31T23:59:59",
  "ipWhitelist": ["203.0.113.0/24"],
  "isTestKey": false
}

Response (201 Created):
{
  "success": true,
  "apiKey": { /* API key details */ },
  "plainTextKey": "hx_live_a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6",
  "message": "API key created successfully. Save the key now - it won't be shown again."
}
```

**Using API Key (Method 1 - Recommended):**
```http
GET /api/students
X-API-Key: hx_live_a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6
```

**Using API Key (Method 2 - Bearer Token):**
```http
GET /api/students
Authorization: Bearer hx_live_a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6
```

**API Key Format:**
- Production: `hx_live_{32-byte-random-base64}`
- Test: `hx_test_{32-byte-random-base64}`

---

## Rate Limiting

All API requests are subject to rate limiting to prevent abuse.

### Rate Limit Tiers

| Authentication Type | Rate Limit | Identifier |
|-------------------|------------|------------|
| API Key (Custom) | Configured per key | API Key ID |
| JWT Authenticated | 1,000 req/hour | User ID |
| Unauthenticated | 100 req/hour | IP Address |

### Rate Limit Headers

Every response includes rate limit information:

```http
X-RateLimit-Limit: 1000
X-RateLimit-Remaining: 847
X-RateLimit-Reset: 1735579200
```

### Rate Limit Exceeded (429)

```json
HTTP/1.1 429 Too Many Requests
X-RateLimit-Limit: 1000
X-RateLimit-Remaining: 0
X-RateLimit-Reset: 1735579200

{
  "success": false,
  "error": "Rate limit exceeded",
  "limit": 1000,
  "remaining": 0,
  "resetAt": 1735579200
}
```

---

## API Endpoints

### Authentication Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/auth/login` | User login | No |
| POST | `/api/auth/refresh` | Refresh access token | No |
| POST | `/api/auth/logout` | Logout and revoke tokens | Yes |
| GET | `/api/auth/me` | Get current user info | Yes |

### API Key Management

| Method | Endpoint | Description | Permission |
|--------|----------|-------------|------------|
| POST | `/api/api-keys` | Create new API key | `write:api-keys` |
| GET | `/api/api-keys` | List user's API keys | `read:api-keys` |
| GET | `/api/api-keys/{id}` | Get API key details | `read:api-keys` |
| DELETE | `/api/api-keys/{id}` | Revoke API key | `delete:api-keys` |
| POST | `/api/api-keys/{id}/rotate` | Rotate API key | `write:api-keys` |

### Attendance Analytics (Phase 38)

| Method | Endpoint | Description | Permission |
|--------|----------|-------------|------------|
| GET | `/api/attendance-analytics/dashboard` | School-wide dashboard | `read:analytics` |
| GET | `/api/attendance-analytics/ada` | Average Daily Attendance | `read:analytics` |
| GET | `/api/attendance-analytics/adm` | Average Daily Membership | `read:analytics` |
| GET | `/api/attendance-analytics/trends` | Attendance trends | `read:analytics` |
| GET | `/api/attendance-analytics/chronic-absenteeism` | Chronic absenteeism analysis | `read:analytics` |
| GET | `/api/attendance-analytics/at-risk` | At-risk student identification | `read:analytics` |

### Webhooks (Phase 37)

| Method | Endpoint | Description | Permission |
|--------|----------|-------------|------------|
| POST | `/api/webhooks` | Register webhook | `write:webhooks` |
| GET | `/api/webhooks` | List webhooks | `read:webhooks` |
| GET | `/api/webhooks/{id}` | Get webhook details | `read:webhooks` |
| DELETE | `/api/webhooks/{id}` | Delete webhook | `delete:webhooks` |
| POST | `/api/webhooks/{id}/test` | Test webhook delivery | `write:webhooks` |
| GET | `/api/webhooks/events` | List event types | `read:webhooks` |

---

## Error Handling

### Standard Error Response

```json
{
  "success": false,
  "timestamp": "2025-12-30T10:15:30",
  "status": 400,
  "error": "Validation Failed",
  "message": "Invalid request data",
  "errors": {
    "username": "Username is required",
    "password": "Password must be at least 8 characters"
  }
}
```

### HTTP Status Codes

| Code | Meaning | Description |
|------|---------|-------------|
| 200 | OK | Request successful |
| 201 | Created | Resource created successfully |
| 400 | Bad Request | Invalid request data |
| 401 | Unauthorized | Authentication required or failed |
| 403 | Forbidden | Insufficient permissions |
| 404 | Not Found | Resource not found |
| 429 | Too Many Requests | Rate limit exceeded |
| 500 | Internal Server Error | Server error |

---

## Security Best Practices

### API Key Security

✅ **DO:**
- Store API keys in secure environment variables
- Use different keys for development/production
- Set appropriate scopes (principle of least privilege)
- Configure IP whitelisting when possible
- Rotate keys regularly (every 90 days)
- Monitor API key usage

❌ **DON'T:**
- Commit API keys to version control
- Share API keys via email or chat
- Use production keys in development
- Grant excessive permissions
- Ignore rate limit warnings

### JWT Token Security

✅ **DO:**
- Store tokens in secure storage (not localStorage)
- Implement token refresh before expiration
- Clear tokens on logout
- Use HTTPS for all API requests

❌ **DON'T:**
- Store tokens in localStorage (XSS vulnerable)
- Share refresh tokens
- Skip token validation
- Use HTTP (always use HTTPS)

### Permission Scopes

| Scope | Description | Use Case |
|-------|-------------|----------|
| `read:students` | View student data | Mobile app, reports |
| `write:students` | Create/update students | Student enrollment system |
| `read:grades` | View grades | Parent portal |
| `write:grades` | Enter/modify grades | Teacher gradebook |
| `read:attendance` | View attendance | Attendance reports |
| `write:attendance` | Record attendance | Attendance kiosk |
| `read:analytics` | View analytics | Dashboard, reports |
| `admin` | Full system access | Administrative tools |

---

## API Versioning

Current Version: **v1**

**URL Format:** `/api/{endpoint}` (no version in URL currently)

**Version Headers:** Added to all responses
```http
X-API-Version: v1
```

**Future Versioning:**
- Breaking changes will introduce v2
- v1 will be maintained for 6 months after v2 release
- Deprecation warnings will be sent 3 months before v1 sunset

---

## OpenAPI / Swagger Documentation

Interactive API documentation available at:
- Swagger UI: `http://localhost:9590/swagger-ui.html`
- OpenAPI JSON: `http://localhost:9590/v3/api-docs`

---

## Support

For API support, bug reports, or feature requests:
- Email: support@heronixedu.com
- GitHub: https://github.com/heronix/heronix-scheduler/issues
- Documentation: https://docs.heronixedu.com

---

**© 2025 Heronix Educational Systems LLC. All rights reserved.**
