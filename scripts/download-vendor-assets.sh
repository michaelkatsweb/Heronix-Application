#!/bin/bash
# ============================================================================
# Heronix SIS - Download Vendor Assets for Offline Operation
# Run this script once to download all required frontend libraries
# ============================================================================

set -e

VENDOR_DIR="src/main/resources/static/vendor"

echo "═══════════════════════════════════════════════════════════"
echo "Downloading vendor assets for offline operation..."
echo "═══════════════════════════════════════════════════════════"

# Create vendor directories
mkdir -p "$VENDOR_DIR/bootstrap/css"
mkdir -p "$VENDOR_DIR/bootstrap/js"
mkdir -p "$VENDOR_DIR/fontawesome/css"
mkdir -p "$VENDOR_DIR/fontawesome/webfonts"
mkdir -p "$VENDOR_DIR/chartjs"

# Download Bootstrap 5.3.0
echo "Downloading Bootstrap 5.3.0..."
curl -sL "https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" \
    -o "$VENDOR_DIR/bootstrap/css/bootstrap.min.css"
curl -sL "https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js" \
    -o "$VENDOR_DIR/bootstrap/js/bootstrap.bundle.min.js"

# Download Chart.js 4.4.0
echo "Downloading Chart.js 4.4.0..."
curl -sL "https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js" \
    -o "$VENDOR_DIR/chartjs/chart.umd.min.js"

# Download Font Awesome 6.4.0
echo "Downloading Font Awesome 6.4.0..."
curl -sL "https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" \
    -o "$VENDOR_DIR/fontawesome/css/all.min.css"

# Download Font Awesome webfonts
echo "Downloading Font Awesome webfonts..."
curl -sL "https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/webfonts/fa-solid-900.woff2" \
    -o "$VENDOR_DIR/fontawesome/webfonts/fa-solid-900.woff2"
curl -sL "https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/webfonts/fa-regular-400.woff2" \
    -o "$VENDOR_DIR/fontawesome/webfonts/fa-regular-400.woff2"
curl -sL "https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/webfonts/fa-brands-400.woff2" \
    -o "$VENDOR_DIR/fontawesome/webfonts/fa-brands-400.woff2"

# Fix Font Awesome CSS paths for local hosting
echo "Fixing Font Awesome CSS paths..."
sed -i 's|../webfonts/|/vendor/fontawesome/webfonts/|g' "$VENDOR_DIR/fontawesome/css/all.min.css"

echo ""
echo "═══════════════════════════════════════════════════════════"
echo "Vendor assets downloaded successfully!"
echo "═══════════════════════════════════════════════════════════"
echo ""
echo "Directory structure:"
find "$VENDOR_DIR" -type f | head -20
echo ""
echo "Total size:"
du -sh "$VENDOR_DIR"
