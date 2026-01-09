# Heronix-SchedulerV2 Integration Guide

## Overview

Heronix-SIS includes optional integration with **Heronix-SchedulerV2**, an advanced schedule generation add-on that provides:

- **Automated Schedule Generation**: AI-powered scheduling algorithms
- **Conflict Resolution**: Automatic detection and resolution of scheduling conflicts
- **Multi-Constraint Optimization**: Balance teacher preferences, room assignments, and student needs
- **What-If Scenarios**: Test different scheduling approaches before committing
- **Real-Time Validation**: Instant feedback on schedule validity

## Integration Architecture

The integration is designed to be **pluggable** - Heronix-SIS works standalone, but when Heronix-SchedulerV2 is added, scheduling features become available seamlessly.

### Integration Modes

#### 1. **API Integration** (Recommended)
- SchedulerV2 calls SIS REST APIs to fetch student, course, and teacher data
- No database sharing required
- Clean separation of concerns
- Easy to deploy across different servers

#### 2. **Shared Database**
- Both applications access the same database
- Fastest data access
- Requires both apps on same network/infrastructure

#### 3. **Import/Export**
- SIS exports data as CSV or JSON
- SchedulerV2 imports the data
- Suitable for one-time schedule generation
- Manual data transfer

#### 4. **Real-Time Sync**
- Automatic bidirectional synchronization
- Changes in one system reflect in the other
- Configurable sync intervals
- Best for highly integrated deployments

### Authentication Modes

#### SSO (Single Sign-On)
- Users log into SIS once
- Seamlessly access SchedulerV2 without re-authentication
- JWT tokens passed securely
- Best user experience

#### Separate Authentication
- Users log into each application independently
- More secure for multi-tenant environments
- SchedulerV2 can have different user base

## Installation & Configuration

### Step 1: Obtain License

Contact Heronix Sales to purchase Heronix-SchedulerV2:
- Website: https://heronix.com
- Email: sales@heronix.com
- You will receive a license key

### Step 2: Install Heronix-SchedulerV2

Download and install Heronix-SchedulerV2 following its installation guide.

Default port: 9090 (configurable)

### Step 3: Configure Heronix-SIS

Edit `src/main/resources/application.properties`:

```properties
# Enable SchedulerV2 integration
heronix.scheduler.enabled=true

# Add your license key
heronix.scheduler.license-key=YOUR-LICENSE-KEY-HERE

# Set SchedulerV2 URL
heronix.scheduler.url=http://localhost:9090

# Choose integration mode (API recommended)
heronix.scheduler.mode=API

# Choose authentication mode (SSO recommended)
heronix.scheduler.auth-mode=SSO

# Set SSO secret (change to strong random value!)
heronix.scheduler.sso-token-secret=CHANGE-THIS-TO-STRONG-SECRET
```

### Step 4: Restart Applications

1. Restart Heronix-SIS
2. Ensure Heronix-SchedulerV2 is running
3. Verify connection in SIS under **Academics > Schedule Generation**

## Usage

### Accessing Schedule Generation

1. Log into Heronix-SIS
2. Navigate to **Academics** menu
3. Click **Schedule Generation**

**If SchedulerV2 is NOT enabled:**
- Menu item is disabled with tooltip explaining requirement
- Clicking shows information dialog about the add-on

**If SchedulerV2 IS enabled and running:**
- Menu item is active
- Clicking launches SchedulerV2 in your web browser
- With SSO: Automatic login
- Without SSO: Login screen appears

### Workflow

1. **Prepare Data in SIS**:
   - Add students, courses, teachers
   - Set course requirements and prerequisites
   - Configure room capacities and equipment

2. **Launch SchedulerV2**:
   - Click Academics > Schedule Generation
   - SchedulerV2 opens in browser

3. **Generate Schedule**:
   - SchedulerV2 fetches data from SIS via API
   - Run schedule generation algorithms
   - Review results, make adjustments
   - Publish schedule

4. **View in SIS**:
   - Generated schedule appears in SIS
   - Students can view their schedules
   - Teachers see their class assignments

## Troubleshooting

### Menu Item is Disabled

**Cause**: SchedulerV2 not enabled or not configured

**Solution**: Check `application.properties`:
```properties
heronix.scheduler.enabled=true
heronix.scheduler.license-key=YOUR-KEY
```

### "SchedulerV2 Not Running" Message

**Cause**: SchedulerV2 application is not accessible

**Solutions**:
1. Start Heronix-SchedulerV2 application
2. Check URL in `application.properties` is correct
3. Verify port 9090 (or custom port) is not blocked by firewall
4. Check SchedulerV2 logs for errors

### SSO Not Working

**Cause**: SSO token secret mismatch or not configured

**Solutions**:
1. Ensure `sso-token-secret` is set in BOTH applications
2. Secrets must match exactly
3. Check token expiration settings
4. Review SchedulerV2 logs for token validation errors

### Data Not Syncing

**Cause**: API integration or sync configuration issue

**Solutions**:
1. Verify `heronix.scheduler.mode=API` or `mode=SYNC`
2. Check SchedulerV2 can access SIS APIs
3. Review API authentication/authorization
4. Check network connectivity between servers
5. Enable debug logging: `logging.level.com.heronix.service.SchedulerIntegrationService=DEBUG`

## Security Considerations

### License Key Security
- Store license key in environment variable: `SCHEDULER_LICENSE_KEY`
- Never commit license keys to version control
- Rotate keys if compromised

### SSO Token Secret
- Use strong random string (min 32 characters)
- Store in secure configuration
- Same secret must be in both applications
- Rotate periodically

### Network Security
- Use HTTPS for production deployments
- Configure firewall rules between SIS and SchedulerV2
- Consider VPN for inter-server communication
- Enable API authentication/authorization

## API Endpoints (For SchedulerV2 Integration)

When using API integration mode, SchedulerV2 calls these SIS endpoints:

### Student Data
```
GET /api/students
GET /api/students/{id}
GET /api/students/by-grade/{grade}
```

### Course Data
```
GET /api/courses
GET /api/courses/{id}
GET /api/courses/by-subject/{subject}
```

### Teacher Data
```
GET /api/staff/teachers
GET /api/staff/teachers/{id}
```

### Room Data
```
GET /api/rooms
GET /api/rooms/{id}
```

### Authentication
All API calls require API key authentication:
```
Header: X-API-Key: <your-api-key>
```

API keys are configured in SIS admin panel.

## Performance Optimization

### For Large Schools (1000+ students)

1. **Use API Mode with Caching**:
   ```properties
   heronix.scheduler.mode=API
   heronix.scheduler.connection-timeout=10000
   ```

2. **Dedicated Server for SchedulerV2**:
   - Deploy SchedulerV2 on separate server
   - Ensure adequate RAM (8GB minimum for AI algorithms)
   - Fast CPU for optimization calculations

3. **Database Optimization**:
   - If using SHARED_DB mode, ensure database is tuned
   - Add indexes on frequently queried fields
   - Consider PostgreSQL over H2 for production

## Support

For technical support with Heronix-SchedulerV2 integration:

- **Documentation**: https://docs.heronix.com
- **Support Email**: support@heronix.com
- **Community Forum**: https://community.heronix.com
- **GitHub Issues**: https://github.com/heronix/heronix-sis/issues

## Version Compatibility

| Heronix-SIS Version | Compatible SchedulerV2 Versions |
|---------------------|----------------------------------|
| 1.0.0+              | 2.0.0+                          |
| 1.5.0+              | 2.1.0+                          |
| 2.0.0+              | 2.2.0+                          |

Always use matching major versions for best compatibility.

## License

Heronix-SchedulerV2 is a **separate licensed product**. The integration code in Heronix-SIS is included in the SIS license, but SchedulerV2 itself requires a separate purchase.

Contact sales@heronix.com for pricing information.
