# Allergy Passport - Debian Server Deployment Guide

Complete guide for deploying the Allergy Passport application to a Debian server in production.

---

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Initial Server Setup](#initial-server-setup)
3. [Install Dependencies](#install-dependencies)
4. [Clone and Configure Application](#clone-and-configure-application)
5. [Setup Google OAuth2](#setup-google-oauth2)
6. [Setup Google Cloud Translation](#setup-google-cloud-translation)
7. [Deploy with Docker Compose](#deploy-with-docker-compose)
8. [Setup Nginx Reverse Proxy](#setup-nginx-reverse-proxy)
9. [Setup SSL/TLS with Let's Encrypt](#setup-ssltls-with-lets-encrypt)
10. [Security Hardening](#security-hardening)
11. [Monitoring and Logging](#monitoring-and-logging)
12. [Backup Strategy](#backup-strategy)
13. [Maintenance](#maintenance)
14. [Troubleshooting](#troubleshooting)

---

## Prerequisites

### Server Requirements

- **OS**: Debian 11 (Bullseye) or 12 (Bookworm)
- **RAM**: Minimum 2GB (4GB recommended)
- **Storage**: Minimum 20GB
- **CPU**: 2 cores minimum
- **Domain**: A registered domain name pointing to your server IP

### Required Accounts

- Google Cloud Console account (for OAuth2 and Translation API)
- Domain registrar access (for DNS configuration)

---

## Initial Server Setup

### 1. Update System

```bash
# SSH into your server
ssh root@your-server-ip

# Update package list and upgrade system
apt update && apt upgrade -y

# Install basic utilities
apt install -y curl wget git vim ufw fail2ban
```

### 2. Create Application User

```bash
# Create user for running the application
adduser allergypassport
usermod -aG sudo allergypassport

# Switch to the new user
su - allergypassport
```

### 3. Setup Firewall

```bash
# Allow SSH, HTTP, and HTTPS
sudo ufw allow OpenSSH
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw enable
sudo ufw status
```

---

## Install Dependencies

### 1. Install Docker

```bash
# Remove old versions
sudo apt remove docker docker-engine docker.io containerd runc

# Install prerequisites
sudo apt install -y ca-certificates curl gnupg lsb-release

# Add Docker's official GPG key
sudo mkdir -p /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/debian/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg

# Set up repository
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/debian \
  $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# Install Docker Engine
sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# Add user to docker group
sudo usermod -aG docker $USER

# Logout and login again for group changes to take effect
exit
su - allergypassport

# Verify installation
docker --version
docker compose version
```

### 2. Install Nginx

```bash
sudo apt install -y nginx

# Start and enable Nginx
sudo systemctl start nginx
sudo systemctl enable nginx
```

---

## Clone and Configure Application

### 1. Clone Repository

```bash
cd ~
git clone https://github.com/yourusername/allergypassport.git
cd allergypassport
```

### 2. Create Environment File

```bash
# Create .env file with production configuration
nano .env
```

Add the following content:

```env
# Database Configuration
DB_NAME=allergypassport
DB_USERNAME=allergypassport
DB_PASSWORD=CHANGE_THIS_TO_STRONG_PASSWORD

# Google OAuth2 (get from Google Cloud Console)
GOOGLE_CLIENT_ID=your-client-id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=your-client-secret

# Google Cloud Translation
TRANSLATION_PROVIDER=google-cloud
GOOGLE_CLOUD_PROJECT_ID=your-project-id

# Application URL (your domain)
APP_BASE_URL=https://yourdomain.com

# Spring Profile
SPRING_PROFILES_ACTIVE=prod
```

**Important**:
- Replace `CHANGE_THIS_TO_STRONG_PASSWORD` with a strong password
- Use a password generator: `openssl rand -base64 32`

### 3. Secure the Environment File

```bash
chmod 600 .env
```

---

## Setup Google OAuth2

### 1. Create OAuth2 Credentials

1. Go to [Google Cloud Console](https://console.cloud.google.com)
2. Create a new project or select existing one
3. Navigate to **APIs & Services** → **Credentials**
4. Click **Create Credentials** → **OAuth 2.0 Client ID**
5. Configure OAuth consent screen if prompted
6. Application type: **Web application**
7. Name: `Allergy Passport Production`

### 2. Configure Authorized Redirect URIs

Add these URIs:
```
https://yourdomain.com/login/oauth2/code/google
https://www.yourdomain.com/login/oauth2/code/google
```

### 3. Update .env File

Copy the Client ID and Client Secret to your `.env` file.

---

## Setup Google Cloud Translation

### 1. Enable Translation API

```bash
# In Google Cloud Console:
# 1. Navigate to "APIs & Services" → "Library"
# 2. Search for "Cloud Translation API"
# 3. Click "Enable"
```

### 2. Create Service Account

```bash
# In Google Cloud Console:
# 1. Navigate to "IAM & Admin" → "Service Accounts"
# 2. Click "Create Service Account"
# 3. Name: "allergy-passport-translation"
# 4. Grant role: "Cloud Translation API User"
# 5. Click "Create Key" → JSON
# 6. Save the JSON file
```

### 3. Upload Service Account Key

```bash
# On your local machine, upload the key to the server
scp translation-key.json allergypassport@your-server-ip:~/allergypassport/

# On the server, secure the file
cd ~/allergypassport
chmod 600 translation-key.json
```

### 4. Update docker-compose.yml

Ensure the `GOOGLE_CLOUD_PROJECT_ID` in docker-compose.yml matches your project ID.

---

## Deploy with Docker Compose

### 1. Build and Start Services

```bash
cd ~/allergypassport

# Build the application
docker compose build

# Start all services
docker compose up -d

# Check status
docker compose ps

# View logs
docker compose logs -f app
```

### 2. Verify Application

```bash
# Wait for application to start (30-60 seconds)
# Check health
curl http://localhost:8080/actuator/health

# Expected output: {"status":"UP"}
```

### 3. Create Database Backup Directory

```bash
mkdir -p ~/backups
chmod 700 ~/backups
```

---

## Setup Nginx Reverse Proxy

### 1. Create Nginx Configuration

```bash
sudo nano /etc/nginx/sites-available/allergypassport
```

Add this configuration:

```nginx
# Rate limiting
limit_req_zone $binary_remote_addr zone=login_limit:10m rate=5r/m;
limit_req_zone $binary_remote_addr zone=api_limit:10m rate=30r/m;

# Upstream backend
upstream allergypassport_backend {
    server localhost:8080;
    keepalive 32;
}

# Redirect HTTP to HTTPS
server {
    listen 80;
    listen [::]:80;
    server_name yourdomain.com www.yourdomain.com;

    # Let's Encrypt challenge
    location /.well-known/acme-challenge/ {
        root /var/www/certbot;
    }

    # Redirect all other traffic to HTTPS
    location / {
        return 301 https://$server_name$request_uri;
    }
}

# HTTPS Server
server {
    listen 443 ssl http2;
    listen [::]:443 ssl http2;
    server_name yourdomain.com www.yourdomain.com;

    # SSL certificates (will be generated in next step)
    ssl_certificate /etc/letsencrypt/live/yourdomain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/yourdomain.com/privkey.pem;

    # SSL configuration
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers 'ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256:ECDHE-ECDSA-AES256-GCM-SHA384:ECDHE-RSA-AES256-GCM-SHA384';
    ssl_prefer_server_ciphers off;
    ssl_session_cache shared:SSL:10m;
    ssl_session_timeout 10m;
    ssl_stapling on;
    ssl_stapling_verify on;

    # Security headers
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header Referrer-Policy "strict-origin-when-cross-origin" always;

    # Logging
    access_log /var/log/nginx/allergypassport_access.log;
    error_log /var/log/nginx/allergypassport_error.log;

    # Max upload size (for profile pictures)
    client_max_body_size 10M;

    # Proxy settings
    location / {
        proxy_pass http://allergypassport_backend;
        proxy_http_version 1.1;

        # Headers
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header X-Forwarded-Host $host;
        proxy_set_header X-Forwarded-Port $server_port;

        # WebSocket support (if needed in future)
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";

        # Timeouts
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;

        # Buffering
        proxy_buffering on;
        proxy_buffer_size 4k;
        proxy_buffers 8 4k;
        proxy_busy_buffers_size 8k;
    }

    # Rate limit login endpoint
    location /oauth2/authorization/google {
        limit_req zone=login_limit burst=3 nodelay;
        proxy_pass http://allergypassport_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # Static files caching
    location ~* \.(jpg|jpeg|png|gif|ico|css|js|svg|woff|woff2|ttf|eot)$ {
        proxy_pass http://allergypassport_backend;
        expires 30d;
        add_header Cache-Control "public, immutable";
    }
}
```

**Important**: Replace `yourdomain.com` with your actual domain!

### 2. Enable Site

```bash
# Create symlink
sudo ln -s /etc/nginx/sites-available/allergypassport /etc/nginx/sites-enabled/

# Remove default site
sudo rm /etc/nginx/sites-enabled/default

# Test configuration
sudo nginx -t

# Don't reload yet - we need SSL certificates first
```

---

## Setup SSL/TLS with Let's Encrypt

### 1. Install Certbot

```bash
sudo apt install -y certbot python3-certbot-nginx
```

### 2. Obtain SSL Certificate

```bash
# Create directory for ACME challenge
sudo mkdir -p /var/www/certbot

# Temporarily modify Nginx config to allow HTTP for certificate issuance
sudo nano /etc/nginx/sites-available/allergypassport
# Comment out the SSL server block temporarily

# Reload Nginx
sudo systemctl reload nginx

# Obtain certificate
sudo certbot certonly --webroot -w /var/www/certbot \
  -d yourdomain.com \
  -d www.yourdomain.com \
  --email your-email@example.com \
  --agree-tos \
  --no-eff-email

# Uncomment the SSL server block in Nginx config
sudo nano /etc/nginx/sites-available/allergypassport

# Test Nginx configuration
sudo nginx -t

# Reload Nginx with SSL
sudo systemctl reload nginx
```

### 3. Setup Auto-Renewal

```bash
# Test renewal
sudo certbot renew --dry-run

# Certbot automatically sets up a cron job or systemd timer
# Verify with:
sudo systemctl status certbot.timer
```

---

## Security Hardening

### 1. Setup Fail2Ban for SSH Protection

```bash
sudo nano /etc/fail2ban/jail.local
```

Add:

```ini
[DEFAULT]
bantime = 3600
findtime = 600
maxretry = 5

[sshd]
enabled = true
port = ssh
logpath = /var/log/auth.log
```

```bash
sudo systemctl restart fail2ban
sudo systemctl enable fail2ban
```

### 2. Disable Root Login

```bash
sudo nano /etc/ssh/sshd_config
```

Change:
```
PermitRootLogin no
PasswordAuthentication no  # Use SSH keys only
```

```bash
sudo systemctl restart sshd
```

### 3. Setup Automatic Security Updates

```bash
sudo apt install -y unattended-upgrades
sudo dpkg-reconfigure -plow unattended-upgrades
```

### 4. Configure Docker Security

```bash
# Limit Docker daemon exposure
sudo nano /etc/docker/daemon.json
```

Add:
```json
{
  "live-restore": true,
  "userland-proxy": false,
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "10m",
    "max-file": "3"
  }
}
```

```bash
sudo systemctl restart docker
```

---

## Monitoring and Logging

### 1. Setup Log Rotation

```bash
sudo nano /etc/logrotate.d/allergypassport
```

Add:
```
/var/log/nginx/allergypassport_*.log {
    daily
    rotate 14
    compress
    delaycompress
    notifempty
    create 0640 www-data adm
    sharedscripts
    postrotate
        if [ -f /var/run/nginx.pid ]; then
            kill -USR1 `cat /var/run/nginx.pid`
        fi
    endscript
}
```

### 2. Monitor Application Logs

```bash
# View application logs
docker compose logs -f app

# View Nginx logs
sudo tail -f /var/log/nginx/allergypassport_access.log
sudo tail -f /var/log/nginx/allergypassport_error.log
```

### 3. Setup Disk Space Monitoring

```bash
# Add to crontab
crontab -e
```

Add:
```bash
# Check disk space daily at 2 AM
0 2 * * * df -h | grep -E '^/dev/' | awk '{ if($5+0 > 80) print "Disk space warning: "$0 }' | mail -s "Disk Space Alert" your-email@example.com
```

---

## Backup Strategy

### 1. Database Backup Script

```bash
nano ~/backup-database.sh
```

Add:
```bash
#!/bin/bash

# Configuration
BACKUP_DIR=~/backups
DATE=$(date +%Y%m%d_%H%M%S)
RETENTION_DAYS=30

# Create backup
docker compose exec -T postgres pg_dump -U allergypassport allergypassport | gzip > $BACKUP_DIR/db_backup_$DATE.sql.gz

# Remove old backups
find $BACKUP_DIR -name "db_backup_*.sql.gz" -mtime +$RETENTION_DAYS -delete

echo "Backup completed: db_backup_$DATE.sql.gz"
```

```bash
chmod +x ~/backup-database.sh
```

### 2. Schedule Automatic Backups

```bash
crontab -e
```

Add:
```bash
# Daily database backup at 3 AM
0 3 * * * /home/allergypassport/backup-database.sh >> /home/allergypassport/backup.log 2>&1
```

### 3. Backup Uploaded Files

```bash
# Backup profile pictures and uploads
tar -czf ~/backups/uploads_$(date +%Y%m%d).tar.gz /home/allergypassport/allergypassport/uploads/

# Or use rsync to remote server
rsync -avz ~/allergypassport/uploads/ backup-server:/backups/allergypassport/uploads/
```

---

## Maintenance

### 1. Update Application

```bash
cd ~/allergypassport

# Pull latest changes
git pull origin main

# Rebuild and restart
docker compose down
docker compose build
docker compose up -d

# Check logs
docker compose logs -f app
```

### 2. Update Docker Images

```bash
# Pull latest base images
docker compose pull

# Rebuild
docker compose up -d --build
```

### 3. Clean Up Docker

```bash
# Remove unused images and containers
docker system prune -a

# Remove unused volumes (be careful!)
docker volume prune
```

### 4. Database Maintenance

```bash
# Vacuum database
docker compose exec postgres psql -U allergypassport -d allergypassport -c "VACUUM ANALYZE;"

# Check database size
docker compose exec postgres psql -U allergypassport -d allergypassport -c "SELECT pg_size_pretty(pg_database_size('allergypassport'));"
```

---

## Troubleshooting

### Application Won't Start

```bash
# Check logs
docker compose logs app

# Check if port 8080 is available
sudo netstat -tulpn | grep 8080

# Restart services
docker compose restart
```

### Database Connection Issues

```bash
# Check database is running
docker compose ps postgres

# Check database logs
docker compose logs postgres

# Test connection
docker compose exec app nc -zv postgres 5432
```

### SSL Certificate Issues

```bash
# Check certificate validity
sudo certbot certificates

# Force renewal
sudo certbot renew --force-renewal

# Check Nginx config
sudo nginx -t
```

### High Memory Usage

```bash
# Check Docker memory usage
docker stats

# Restart application
docker compose restart app

# Adjust JVM memory in docker-compose.yml
# Change JAVA_OPTS: "-Xmx512m -Xms256m" to lower values
```

### Translation API Errors

```bash
# Check Google Cloud credentials
cat ~/allergypassport/translation-key.json

# Verify project ID
grep GOOGLE_CLOUD_PROJECT_ID ~/allergypassport/.env

# Check API quota
# Go to Google Cloud Console → APIs & Services → Dashboard
```

### View Application Health

```bash
# Health check
curl https://yourdomain.com/actuator/health

# Detailed health (if actuator is exposed)
curl https://yourdomain.com/actuator/health/detailed
```

---

## Emergency Procedures

### Roll Back to Previous Version

```bash
cd ~/allergypassport

# View commit history
git log --oneline

# Roll back to previous version
git checkout <previous-commit-hash>

# Rebuild and restart
docker compose down
docker compose build
docker compose up -d
```

### Restore Database from Backup

```bash
# Stop application
docker compose down app

# Restore database
gunzip < ~/backups/db_backup_YYYYMMDD_HHMMSS.sql.gz | docker compose exec -T postgres psql -U allergypassport -d allergypassport

# Start application
docker compose up -d
```

---

## Performance Optimization

### 1. Enable Nginx Caching

Add to Nginx config:
```nginx
proxy_cache_path /var/cache/nginx levels=1:2 keys_zone=allergypassport_cache:10m max_size=100m;

location / {
    proxy_cache allergypassport_cache;
    proxy_cache_valid 200 10m;
    proxy_cache_use_stale error timeout updating http_500 http_502 http_503 http_504;
    # ... rest of proxy config
}
```

### 2. Database Performance

```bash
# Increase PostgreSQL shared buffers
docker compose exec postgres psql -U postgres -c "ALTER SYSTEM SET shared_buffers = '256MB';"
docker compose restart postgres
```

---

## Support and Additional Resources

- **Application Repository**: https://github.com/yourusername/allergypassport
- **Docker Documentation**: https://docs.docker.com
- **Nginx Documentation**: https://nginx.org/en/docs
- **Let's Encrypt**: https://letsencrypt.org/docs
- **Google Cloud Translation**: https://cloud.google.com/translate/docs

---

## Quick Reference Commands

```bash
# Start application
docker compose up -d

# Stop application
docker compose down

# View logs
docker compose logs -f app

# Restart application
docker compose restart app

# Backup database
./backup-database.sh

# Check health
curl http://localhost:8080/actuator/health

# View Nginx logs
sudo tail -f /var/log/nginx/allergypassport_access.log

# Reload Nginx
sudo systemctl reload nginx

# Check SSL certificate
sudo certbot certificates

# Update application
git pull && docker compose up -d --build
```

---

**Last Updated**: 2025-11-27
**Version**: 1.0
