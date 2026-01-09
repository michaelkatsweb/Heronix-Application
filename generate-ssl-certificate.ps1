# ============================================================
# SSL Certificate Generation Script for Heronix-SIS
# ============================================================
# This script generates a self-signed SSL certificate for development
# and testing purposes. For production, use a certificate from a
# trusted Certificate Authority (Let's Encrypt, DigiCert, etc.).
#
# Author: Heronix Development Team
# Version: 1.0.0
# Date: 2025-12-28
# ============================================================

Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "Heronix-SIS SSL Certificate Generator" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host ""

# ============================================================
# Configuration
# ============================================================

$KEYSTORE_DIR = "src\main\resources\keystore"
$KEYSTORE_FILE = "$KEYSTORE_DIR\heronix-keystore.p12"
$KEYSTORE_ALIAS = "heronix"
$VALIDITY_DAYS = 365
$KEY_SIZE = 2048
$KEY_ALG = "RSA"

# Certificate Information
$COMMON_NAME = "localhost"
$ORGANIZATION_UNIT = "IT Department"
$ORGANIZATION = "Heronix School District"
$CITY = "Your City"
$STATE = "Your State"
$COUNTRY = "US"

# ============================================================
# Check if keytool is available
# ============================================================

Write-Host "Checking for Java keytool..." -ForegroundColor Yellow

try {
    $keytoolVersion = keytool -version 2>&1
    Write-Host "✓ Found keytool: $keytoolVersion" -ForegroundColor Green
} catch {
    Write-Host "✗ ERROR: keytool not found!" -ForegroundColor Red
    Write-Host "  Please ensure Java JDK is installed and in your PATH" -ForegroundColor Red
    Write-Host "  Download from: https://adoptium.net/" -ForegroundColor Yellow
    exit 1
}

# ============================================================
# Create keystore directory if it doesn't exist
# ============================================================

Write-Host ""
Write-Host "Creating keystore directory..." -ForegroundColor Yellow

if (!(Test-Path $KEYSTORE_DIR)) {
    New-Item -ItemType Directory -Path $KEYSTORE_DIR -Force | Out-Null
    Write-Host "✓ Created directory: $KEYSTORE_DIR" -ForegroundColor Green
} else {
    Write-Host "✓ Directory already exists: $KEYSTORE_DIR" -ForegroundColor Green
}

# ============================================================
# Check if keystore already exists
# ============================================================

if (Test-Path $KEYSTORE_FILE) {
    Write-Host ""
    Write-Host "WARNING: Keystore file already exists!" -ForegroundColor Yellow
    Write-Host "File: $KEYSTORE_FILE" -ForegroundColor Yellow
    $response = Read-Host "Do you want to overwrite it? (yes/no)"

    if ($response -ne "yes") {
        Write-Host "Aborted by user." -ForegroundColor Yellow
        exit 0
    }

    Remove-Item $KEYSTORE_FILE -Force
    Write-Host "✓ Removed existing keystore" -ForegroundColor Green
}

# ============================================================
# Generate keystore password
# ============================================================

Write-Host ""
Write-Host "Keystore Password Options:" -ForegroundColor Yellow
Write-Host "1. Generate random password (recommended)" -ForegroundColor Cyan
Write-Host "2. Enter custom password" -ForegroundColor Cyan
$passwordOption = Read-Host "Choose option (1 or 2)"

if ($passwordOption -eq "1") {
    # Generate random password (20 characters, alphanumeric + special)
    $PASSWORD = -join ((48..57) + (65..90) + (97..122) + (33, 35, 36, 37, 38, 42, 43, 45, 61) | Get-Random -Count 20 | ForEach-Object {[char]$_})
    Write-Host "✓ Generated random password" -ForegroundColor Green
} else {
    $PASSWORD = Read-Host "Enter keystore password" -AsSecureString
    $PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
        [Runtime.InteropServices.Marshal]::SecureStringToBSTR($PASSWORD)
    )
}

# ============================================================
# Build Distinguished Name (DN)
# ============================================================

$DN = "CN=$COMMON_NAME, OU=$ORGANIZATION_UNIT, O=$ORGANIZATION, L=$CITY, ST=$STATE, C=$COUNTRY"

# ============================================================
# Generate Self-Signed Certificate
# ============================================================

Write-Host ""
Write-Host "Generating self-signed SSL certificate..." -ForegroundColor Yellow
Write-Host "  Common Name: $COMMON_NAME" -ForegroundColor Cyan
Write-Host "  Organization: $ORGANIZATION" -ForegroundColor Cyan
Write-Host "  Validity: $VALIDITY_DAYS days" -ForegroundColor Cyan
Write-Host "  Key Size: $KEY_SIZE bits" -ForegroundColor Cyan
Write-Host ""

$keytoolArgs = @(
    "-genkeypair",
    "-alias", $KEYSTORE_ALIAS,
    "-keyalg", $KEY_ALG,
    "-keysize", $KEY_SIZE,
    "-validity", $VALIDITY_DAYS,
    "-keystore", $KEYSTORE_FILE,
    "-storetype", "PKCS12",
    "-storepass", $PASSWORD,
    "-keypass", $PASSWORD,
    "-dname", $DN,
    "-ext", "SAN=dns:localhost,ip:127.0.0.1"
)

try {
    $output = & keytool $keytoolArgs 2>&1

    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ SSL certificate generated successfully!" -ForegroundColor Green
    } else {
        Write-Host "✗ ERROR: Failed to generate certificate" -ForegroundColor Red
        Write-Host $output
        exit 1
    }
} catch {
    Write-Host "✗ ERROR: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# ============================================================
# List Certificate Details
# ============================================================

Write-Host ""
Write-Host "Certificate Details:" -ForegroundColor Yellow

$listArgs = @(
    "-list",
    "-v",
    "-keystore", $KEYSTORE_FILE,
    "-storepass", $PASSWORD,
    "-alias", $KEYSTORE_ALIAS
)

keytool $listArgs

# ============================================================
# Export Certificate (optional - for importing into browsers)
# ============================================================

Write-Host ""
$exportCert = Read-Host "Do you want to export the certificate for browser import? (yes/no)"

if ($exportCert -eq "yes") {
    $CERT_FILE = "$KEYSTORE_DIR\heronix-certificate.crt"

    $exportArgs = @(
        "-exportcert",
        "-alias", $KEYSTORE_ALIAS,
        "-keystore", $KEYSTORE_FILE,
        "-storepass", $PASSWORD,
        "-rfc",
        "-file", $CERT_FILE
    )

    keytool $exportArgs

    Write-Host "✓ Certificate exported to: $CERT_FILE" -ForegroundColor Green
    Write-Host "  Import this file into your browser's trusted certificates" -ForegroundColor Cyan
}

# ============================================================
# Save Configuration to .env file
# ============================================================

Write-Host ""
Write-Host "Saving configuration to .env file..." -ForegroundColor Yellow

$envFile = ".env.prod"
$envContent = @"
# ============================================================
# Heronix-SIS Production Environment Variables
# Generated: $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")
# ============================================================

# SSL/TLS Configuration
SSL_KEYSTORE_PATH=$KEYSTORE_FILE
SSL_KEYSTORE_PASSWORD=$PASSWORD

# Server Ports
SERVER_PORT=8443
HTTP_PORT=8080

# Database Configuration (UPDATE THESE!)
DB_URL=jdbc:postgresql://localhost:5432/heronix_sis
DB_USERNAME=heronix_user
DB_PASSWORD=CHANGE_ME_TO_SECURE_PASSWORD

# Admin User (UPDATE THESE!)
ADMIN_USERNAME=admin
ADMIN_PASSWORD=CHANGE_ME_TO_SECURE_PASSWORD
ADMIN_EMAIL=admin@heronix.edu

# Email Configuration (UPDATE THESE!)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@example.com
MAIL_PASSWORD=CHANGE_ME_TO_APP_PASSWORD
MAIL_FROM=noreply@heronix.edu

# File Paths
TOMCAT_BASE_DIR=./tomcat
LOG_FILE_PATH=./logs/heronix-sis.log
FILE_UPLOAD_DIR=./uploads
BACKUP_DIR=./backups

# External Portal URLs
STAGING_SERVER_URL=https://staging.heronix.edu
TEACHER_PORTAL_URL=https://teachers.heronix.edu
PARENT_PORTAL_URL=https://portal.heronix.edu

# Spring Profile
SPRING_PROFILES_ACTIVE=prod
"@

$envContent | Out-File -FilePath $envFile -Encoding UTF8

Write-Host "✓ Configuration saved to: $envFile" -ForegroundColor Green

# ============================================================
# Success Summary
# ============================================================

Write-Host ""
Write-Host "============================================================" -ForegroundColor Green
Write-Host "SUCCESS! SSL Certificate Generated" -ForegroundColor Green
Write-Host "============================================================" -ForegroundColor Green
Write-Host ""
Write-Host "Files Created:" -ForegroundColor Yellow
Write-Host "  ✓ Keystore: $KEYSTORE_FILE" -ForegroundColor Green
Write-Host "  ✓ Environment: $envFile" -ForegroundColor Green
if ($exportCert -eq "yes") {
    Write-Host "  ✓ Certificate: $CERT_FILE" -ForegroundColor Green
}
Write-Host ""
Write-Host "Keystore Password: $PASSWORD" -ForegroundColor Cyan
Write-Host "IMPORTANT: Save this password securely!" -ForegroundColor Yellow
Write-Host ""
Write-Host "Next Steps:" -ForegroundColor Yellow
Write-Host "  1. Update the passwords in $envFile" -ForegroundColor Cyan
Write-Host "  2. Load environment variables: source $envFile (Linux/Mac) or set from file (Windows)" -ForegroundColor Cyan
Write-Host "  3. Run application with prod profile: mvn spring-boot:run -Dspring-boot.run.profiles=prod" -ForegroundColor Cyan
Write-Host "  4. Access application at: https://localhost:8443" -ForegroundColor Cyan
Write-Host ""
Write-Host "For Production Deployment:" -ForegroundColor Yellow
Write-Host "  - Replace self-signed certificate with CA-signed certificate" -ForegroundColor Cyan
Write-Host "  - Use Let's Encrypt (free) or commercial CA (DigiCert, etc.)" -ForegroundColor Cyan
Write-Host "  - See PRODUCTION_SECURITY_HARDENING_GUIDE.md for details" -ForegroundColor Cyan
Write-Host ""
Write-Host "============================================================" -ForegroundColor Green
