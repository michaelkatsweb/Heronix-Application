# Simple SSL Certificate Generator for Heronix-SIS
# Author: Heronix Development Team
# Date: 2025-12-28

Write-Host "Heronix-SIS SSL Certificate Generator" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
Write-Host ""

# Configuration
$KEYSTORE_DIR = "src\main\resources\keystore"
$KEYSTORE_FILE = "$KEYSTORE_DIR\heronix-keystore.p12"
$KEYSTORE_ALIAS = "heronix"
$VALIDITY_DAYS = 365
$KEY_SIZE = 2048
$PASSWORD = "heronix123"  # Change this to a secure password

# Create directory
if (!(Test-Path $KEYSTORE_DIR)) {
    New-Item -ItemType Directory -Path $KEYSTORE_DIR -Force | Out-Null
    Write-Host "Created directory: $KEYSTORE_DIR" -ForegroundColor Green
}

# Remove existing keystore if present
if (Test-Path $KEYSTORE_FILE) {
    Remove-Item $KEYSTORE_FILE -Force
    Write-Host "Removed existing keystore" -ForegroundColor Yellow
}

# Generate certificate
Write-Host "Generating SSL certificate..." -ForegroundColor Yellow

$DN = "CN=localhost, OU=IT, O=Heronix School District, L=City, ST=State, C=US"

keytool -genkeypair `
    -alias $KEYSTORE_ALIAS `
    -keyalg RSA `
    -keysize $KEY_SIZE `
    -validity $VALIDITY_DAYS `
    -keystore $KEYSTORE_FILE `
    -storetype PKCS12 `
    -storepass $PASSWORD `
    -keypass $PASSWORD `
    -dname $DN `
    -ext "SAN=dns:localhost,ip:127.0.0.1"

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "SUCCESS! Certificate generated" -ForegroundColor Green
    Write-Host "Location: $KEYSTORE_FILE" -ForegroundColor Green
    Write-Host "Password: $PASSWORD" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "IMPORTANT: Update SSL_KEYSTORE_PASSWORD in your .env file with this password"  -ForegroundColor Yellow
} else {
    Write-Host "ERROR: Failed to generate certificate" -ForegroundColor Red
}
