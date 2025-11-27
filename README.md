# 🛡️ Allergy Passport

A web application that allows users to create and share their food allergy information via a QR code. Perfect for travelers who need to communicate dietary restrictions in any language.

## Features

- **Google OAuth2 Authentication** - Secure sign-in with your Google account
- **Allergy Management** - Select from 15 common allergens with severity levels
- **Multi-language Support** - View allergy information in English, German, French, Italian, or Spanish
- **QR Code Generation** - Share your allergy passport with a scannable QR code
- **Mobile-Responsive** - Works great on phones for easy restaurant use
- **High-Contrast Design** - Clear visual distinction between severe allergies and intolerances

## Tech Stack

- **Backend**: Java 17+, Spring Boot 3.2
- **Frontend**: Thymeleaf + HTMX + Tailwind CSS
- **Database**: PostgreSQL
- **QR Code**: ZXing library
- **Deployment**: Docker & Docker Compose

## Quick Start

### Prerequisites

- Java 17 or higher
- Maven 3.8+
- PostgreSQL 14+ (or use Docker)
- Google OAuth2 credentials

### Setting Up Google OAuth2

1. Go to [Google Cloud Console](https://console.cloud.google.com/apis/credentials)
2. Create a new project or select an existing one
3. Enable the "Google+ API" or "Google Identity"
4. Go to "Credentials" → "Create Credentials" → "OAuth 2.0 Client ID"
5. Configure the consent screen if prompted
6. Set the application type to "Web application"
7. Add authorized redirect URI: `http://localhost:8080/login/oauth2/code/google`
8. Copy the Client ID and Client Secret

### Running Locally

#### Option 1: With Docker Compose (Recommended)

```bash
# Clone the repository
git clone https://github.com/yourusername/allergy-passport.git
cd allergy-passport

# Copy environment file and configure
cp .env.example .env
# Edit .env with your Google OAuth credentials

# Start the application
docker-compose up -d

# View logs
docker-compose logs -f app
```

#### Option 2: Manual Setup

```bash
# Start PostgreSQL (or use your existing instance)
docker run -d \
  --name allergy-db \
  -e POSTGRES_DB=allergypassport \
  -e POSTGRES_USER=allergypassport \
  -e POSTGRES_PASSWORD=allergypassport \
  -p 5432:5432 \
  postgres:16-alpine

# Set environment variables
export GOOGLE_CLIENT_ID=your-client-id
export GOOGLE_CLIENT_SECRET=your-client-secret

# Run the application
./mvnw spring-boot:run
```

The application will be available at http://localhost:8080

## Project Structure

```
allergy-passport/
├── src/main/java/com/allergypassport/
│   ├── config/           # Security & Web configuration
│   ├── controller/       # Web & API controllers
│   ├── dto/              # Data Transfer Objects
│   ├── entity/           # JPA entities
│   ├── repository/       # Spring Data repositories
│   ├── service/          # Business logic
│   │   └── impl/         # Service implementations
│   └── util/             # Utility classes (QR generation)
├── src/main/resources/
│   ├── i18n/             # Internationalization messages
│   ├── templates/        # Thymeleaf templates
│   │   ├── fragments/    # Reusable template fragments
│   │   └── public/       # Public-facing templates
│   └── application.properties
├── docker-compose.yml
├── Dockerfile
└── pom.xml
```

## Database Schema

```sql
-- Users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    public_id VARCHAR(8) UNIQUE NOT NULL,
    google_id VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) NOT NULL,
    display_name VARCHAR(100),
    bio VARCHAR(500),
    profile_picture BYTEA,
    profile_picture_content_type VARCHAR(50),
    google_picture_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- User allergies table
CREATE TABLE user_allergies (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    allergy_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    UNIQUE(user_id, allergy_type)
);
```

## API Endpoints

### Public Endpoints (No Auth Required)
- `GET /` - Landing page
- `GET /u/{publicId}` - Public allergy passport view
- `GET /qr/{publicId}` - QR code image
- `GET /profile-picture/{publicId}` - User profile picture

### Protected Endpoints (Auth Required)
- `GET /dashboard` - User dashboard
- `GET /profile` - Profile settings
- `POST /api/allergies` - Add allergy
- `PUT /api/allergies/{id}` - Update allergy
- `DELETE /api/allergies/{id}` - Remove allergy
- `POST /api/profile` - Update profile
- `POST /api/profile/picture` - Upload profile picture

## Extending the Translation Service

The application includes a `TranslationService` interface with a mock implementation. To add real translation support:

### DeepL Implementation Example

```java
@Service
@ConditionalOnProperty(name = "app.translation.provider", havingValue = "deepl")
public class DeepLTranslationService implements TranslationService {
    
    @Value("${deepl.api-key}")
    private String apiKey;
    
    @Override
    public String translate(String text, Locale sourceLocale, Locale targetLocale) {
        // Implement DeepL API call here
    }
}
```

## Configuration Options

| Property | Description | Default |
|----------|-------------|---------|
| `DB_HOST` | PostgreSQL host | localhost |
| `DB_PORT` | PostgreSQL port | 5432 |
| `DB_NAME` | Database name | allergypassport |
| `GOOGLE_CLIENT_ID` | OAuth2 client ID | (required) |
| `GOOGLE_CLIENT_SECRET` | OAuth2 client secret | (required) |
| `APP_BASE_URL` | Application base URL | http://localhost:8080 |
| `app.qr.width` | QR code width | 300 |
| `app.qr.height` | QR code height | 300 |

## Deployment to Production

### Using Docker Compose

1. Set up a domain and SSL certificate
2. Configure environment variables
3. Update `APP_BASE_URL` to your domain
4. Add authorized redirect URI in Google Console
5. Deploy with `docker-compose up -d`

### Recommended: Add Nginx Reverse Proxy

```nginx
server {
    listen 80;
    server_name allergypassport.yourdomain.com;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl;
    server_name allergypassport.yourdomain.com;
    
    ssl_certificate /path/to/cert.pem;
    ssl_certificate_key /path/to/key.pem;
    
    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

MIT License - feel free to use this project for personal or commercial purposes.

## Support

If you find this project helpful, please give it a ⭐ on GitHub!
