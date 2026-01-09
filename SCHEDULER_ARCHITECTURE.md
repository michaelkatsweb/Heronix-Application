# Heronix-SchedulerV2 Integration Architecture

## System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                         Heronix-SIS                                  │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │                    User Interface Layer                        │  │
│  │  ┌──────────────────────────────────────────────────────────┐ │  │
│  │  │   Academics Menu → Schedule Generation (MenuItem)       │ │  │
│  │  │                                                          │ │  │
│  │  │   States:                                                │ │  │
│  │  │   ❌ Disabled → Tooltip: "Requires SchedulerV2"         │ │  │
│  │  │   ✅ Enabled  → Launches SchedulerV2                    │ │  │
│  │  └──────────────────────────────────────────────────────────┘ │  │
│  │                           ↓                                    │  │
│  │  ┌──────────────────────────────────────────────────────────┐ │  │
│  │  │    ScheduleGenerationController                          │ │  │
│  │  │    - handleScheduleGeneration()                          │ │  │
│  │  │    - showSchedulerNotAvailableDialog()                   │ │  │
│  │  │    - launchScheduler(url)                                │ │  │
│  │  └──────────────────────────────────────────────────────────┘ │  │
│  └───────────────────────────────────────────────────────────────┘  │
│                                                                       │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │                    Service Layer                               │  │
│  │  ┌──────────────────────────────────────────────────────────┐ │  │
│  │  │    SchedulerIntegrationService                           │ │  │
│  │  │    - isSchedulerEnabled()         → Check config        │ │  │
│  │  │    - isSchedulerAvailable()       → Port check          │ │  │
│  │  │    - validateLicenseKey()         → License validation  │ │  │
│  │  │    - getSchedulerLaunchUrl()      → Generate URL+token  │ │  │
│  │  │    - generateSsoToken()           → JWT generation      │ │  │
│  │  └──────────────────────────────────────────────────────────┘ │  │
│  └───────────────────────────────────────────────────────────────┘  │
│                                                                       │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │                  Configuration Layer                           │  │
│  │  ┌──────────────────────────────────────────────────────────┐ │  │
│  │  │    SchedulerIntegrationProperties                        │ │  │
│  │  │    - enabled: boolean                                    │ │  │
│  │  │    - licenseKey: String                                  │ │  │
│  │  │    - url: String                                         │ │  │
│  │  │    - mode: IntegrationMode (API/SHARED_DB/IMPORT/SYNC)  │ │  │
│  │  │    - authMode: AuthMode (SSO/SEPARATE)                   │ │  │
│  │  └──────────────────────────────────────────────────────────┘ │  │
│  └───────────────────────────────────────────────────────────────┘  │
│                                                                       │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │                    Data Layer                                  │  │
│  │    Students, Courses, Teachers, Rooms → Database              │  │
│  └───────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
                                  │
                                  │ Integration Methods:
                                  │
        ┌─────────────────────────┼─────────────────────────┐
        │                         │                         │
        │ Mode 1: API            │ Mode 2: SHARED_DB      │ Mode 3: IMPORT/SYNC
        ↓                         ↓                         ↓
┌───────────────┐         ┌───────────────┐         ┌───────────────┐
│ REST API      │         │ Database      │         │ CSV/JSON      │
│ Calls         │         │ Connection    │         │ Export/Import │
│               │         │               │         │               │
│ GET /api/     │         │ Same DB       │         │ Manual/Auto   │
│  students     │         │ JDBC          │         │ Files         │
│  courses      │         │ Connection    │         │               │
│  teachers     │         │               │         │               │
│  rooms        │         │               │         │               │
└───────────────┘         └───────────────┘         └───────────────┘
        │                         │                         │
        └─────────────────────────┴─────────────────────────┘
                                  │
                                  ↓
        ┌───────────────────────────────────────────────────────────┐
        │          Authentication: SSO vs SEPARATE                   │
        │                                                            │
        │  SSO Mode:                    SEPARATE Mode:               │
        │  1. SIS generates JWT token   1. User clicks launch       │
        │  2. Token passed in URL       2. Browser opens SchV2      │
        │  3. SchV2 validates token     3. SchV2 login screen       │
        │  4. User auto-logged-in       4. User enters credentials  │
        │                                                            │
        │  URL: /sso?token=JWT123      URL: /login                  │
        └───────────────────────────────────────────────────────────┘
                                  │
                                  ↓
┌─────────────────────────────────────────────────────────────────────┐
│                      Heronix-SchedulerV2                             │
│                                                                      │
│  ┌───────────────────────────────────────────────────────────────┐ │
│  │   Web Interface (Port 9090)                                   │ │
│  │   - Schedule Generation UI                                    │ │
│  │   - AI-Powered Algorithms                                     │ │
│  │   - Conflict Resolution                                       │ │
│  │   - What-If Scenarios                                         │ │
│  └───────────────────────────────────────────────────────────────┘ │
│                                                                      │
│  ┌───────────────────────────────────────────────────────────────┐ │
│  │   Data Integration Layer                                      │ │
│  │   - Fetch data from SIS (API/DB/Import)                       │ │
│  │   - Validate constraints                                      │ │
│  │   - Sync generated schedules back                             │ │
│  └───────────────────────────────────────────────────────────────┘ │
│                                                                      │
│  ┌───────────────────────────────────────────────────────────────┐ │
│  │   Scheduling Engine                                           │ │
│  │   - Genetic Algorithms                                        │ │
│  │   - Constraint Satisfaction                                   │ │
│  │   - Optimization                                              │ │
│  └───────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────┘
```

## Component Interaction Flow

### Flow 1: Feature Check (At Startup)

```
1. Application Starts
   ↓
2. SchedulerIntegrationProperties loads from application.properties
   ↓
3. Check: enabled = true?
   ├─ NO  → Feature disabled, menu item disabled
   └─ YES → Continue
      ↓
4. Check: license key valid?
   ├─ NO  → Feature disabled, show error
   └─ YES → Continue
      ↓
5. Check: SchedulerV2 running? (Health check)
   ├─ NO  → Menu item active but warns when clicked
   └─ YES → Menu item active and ready
```

### Flow 2: User Launches Schedule Generation

```
User clicks "Schedule Generation" menu item
   ↓
1. ScheduleGenerationController.handleScheduleGeneration()
   ↓
2. SchedulerIntegrationService.isSchedulerEnabled()?
   ├─ NO  → Show "Not Available" dialog
   └─ YES → Continue
      ↓
3. SchedulerIntegrationService.isSchedulerAvailable()?
   ├─ NO  → Show "Not Running" dialog
   └─ YES → Continue
      ↓
4. Get current user info (username, role)
   ↓
5. SchedulerIntegrationService.getSchedulerLaunchUrl(user, role)
   ↓
6. Check auth mode:
   ├─ SSO      → Generate JWT token → URL: /sso?token=JWT123
   └─ SEPARATE → No token           → URL: /login
   ↓
7. Show confirmation dialog
   ↓
8. User confirms?
   ├─ NO  → Cancel
   └─ YES → Open browser with URL
      ↓
9. SchedulerV2 receives request:
   ├─ SSO      → Validate token → Auto-login user
   └─ SEPARATE → Show login screen
   ↓
10. User accesses SchedulerV2 interface
```

### Flow 3: Data Integration (API Mode)

```
User in SchedulerV2 clicks "Generate Schedule"
   ↓
1. SchedulerV2 needs student/course data
   ↓
2. SchedulerV2 → HTTP GET /api/students (with API key)
   ↓
3. Heronix-SIS receives request
   ↓
4. API Security Filter validates API key
   ↓
5. StudentController returns student data as JSON
   ↓
6. SchedulerV2 receives data
   ↓
7. Repeat for courses, teachers, rooms
   ↓
8. SchedulerV2 runs scheduling algorithms
   ↓
9. Generated schedule → Store in SchedulerV2 DB
   ↓
10. Optional: Sync back to SIS via POST /api/schedules
```

## Configuration Decision Tree

```
Do you have Heronix-SchedulerV2 license?
├─ NO  → enabled=false (default)
│        Feature disabled, menu shows info dialog
└─ YES → enabled=true
         ↓
         Are both apps on same server?
         ├─ YES → mode=SHARED_DB (fastest)
         │        authMode=SSO (convenient)
         │        URL=http://localhost:9090
         └─ NO  → Are apps on same network?
                  ├─ YES → mode=API (recommended)
                  │        authMode=SSO
                  │        URL=http://scheduler-server:9090
                  └─ NO  → mode=API or IMPORT
                           authMode=SEPARATE (more secure)
                           URL=https://scheduler.remote.edu

                           Need real-time sync?
                           ├─ YES → mode=SYNC
                           │        autoSync=true
                           │        syncInterval=300
                           └─ NO  → mode=API or IMPORT
```

## Security Layers

```
┌─────────────────────────────────────────────────────────────┐
│ Layer 1: Configuration Security                             │
│ - License key validation                                    │
│ - Environment variable storage                              │
│ - No keys in version control                                │
└─────────────────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────────────┐
│ Layer 2: Network Security                                   │
│ - HTTPS for production                                      │
│ - Firewall rules (port 9090)                                │
│ - Connection timeouts                                       │
└─────────────────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────────────┐
│ Layer 3: Authentication Security                            │
│ SSO Mode:                    SEPARATE Mode:                 │
│ - JWT tokens with expiration  - Independent credentials     │
│ - HMAC-SHA256 signing         - Password hashing            │
│ - Shared secret validation    - Session management          │
└─────────────────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────────────┐
│ Layer 4: API Security (API Integration Mode)                │
│ - API key authentication                                    │
│ - Request rate limiting                                     │
│ - Input validation                                          │
│ - Authorization checks                                      │
└─────────────────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────────────┐
│ Layer 5: Data Security                                      │
│ - Database encryption                                       │
│ - Audit logging                                             │
│ - Data validation                                           │
│ - Transaction integrity                                     │
└─────────────────────────────────────────────────────────────┘
```

## Deployment Topologies

### Topology 1: Single Server

```
┌────────────────────────────────────────┐
│        School Server                    │
│                                         │
│  ┌──────────────┐  ┌─────────────────┐│
│  │ Heronix-SIS  │  │ SchedulerV2     ││
│  │ Port: 9580   │  │ Port: 9090      ││
│  └──────┬───────┘  └────────┬────────┘│
│         │                    │         │
│         └─────────┬──────────┘         │
│                   │                    │
│           ┌───────▼────────┐           │
│           │   Database     │           │
│           │   (H2/PgSQL)   │           │
│           └────────────────┘           │
└────────────────────────────────────────┘
        │
        └─→ Users access via http://server:9580
```

### Topology 2: Separate Servers

```
┌─────────────────┐         ┌─────────────────┐
│  SIS Server     │         │ Scheduler Server │
│                 │         │                  │
│ Heronix-SIS     │◄───────►│ SchedulerV2     │
│ Port: 9580      │  API    │ Port: 9090      │
│                 │  Calls  │                  │
└────────┬────────┘         └────────┬─────────┘
         │                           │
         │                           │
    ┌────▼─────┐               ┌────▼─────┐
    │ SIS DB   │               │ Sched DB │
    │          │               │          │
    └──────────┘               └──────────┘

Users → SIS → Click Schedule Gen → Browser → SchedulerV2
```

### Topology 3: Cloud Deployment

```
┌──────────────── CLOUD INFRASTRUCTURE ─────────────────┐
│                                                        │
│  ┌────────────┐                 ┌─────────────────┐  │
│  │ Load       │                 │ Load            │  │
│  │ Balancer   │                 │ Balancer        │  │
│  └─────┬──────┘                 └──────┬──────────┘  │
│        │                                │             │
│  ┌─────▼──────────┐           ┌────────▼──────────┐  │
│  │ SIS Cluster    │           │ Scheduler Cluster │  │
│  │ ┌───┐ ┌───┐   │◄─────────►│ ┌───┐ ┌───┐      │  │
│  │ │SIS│ │SIS│   │   HTTPS   │ │Sch│ │Sch│      │  │
│  │ └───┘ └───┘   │   API     │ └───┘ └───┘      │  │
│  └────────────────┘           └──────────────────┘  │
│         │                              │             │
│         │                              │             │
│  ┌──────▼───────────────┐   ┌─────────▼──────────┐  │
│  │ PostgreSQL Primary   │   │ PostgreSQL Primary │  │
│  │ + Read Replicas      │   │ + Read Replicas    │  │
│  └──────────────────────┘   └────────────────────┘  │
│                                                      │
└──────────────────────────────────────────────────────┘

Users → https://sis.school.edu → SSO → https://scheduler.school.edu
```

## Key Architecture Principles

1. **Loose Coupling**: SIS and SchedulerV2 can run independently
2. **Pluggability**: Feature can be enabled/disabled via configuration
3. **Flexibility**: Multiple integration modes for different needs
4. **Security**: Multiple layers of security controls
5. **Scalability**: Can deploy from single server to cloud cluster
6. **User Experience**: Seamless integration with SSO or separate auth
7. **Maintainability**: Clear separation of concerns, well-documented

This architecture ensures that Heronix-SIS remains fully functional as a standalone system while providing a smooth, optional integration path for advanced schedule generation capabilities when the SchedulerV2 add-on is purchased and installed.
