# Google Cloud Translation API Integration

## Overview

The Allergy Passport application uses a **2-tier translation system** to minimize costs while supporting 20 languages:

### Tier 1: Static UI Translation (No API Cost)
- Navigation menus, buttons, labels, form fields
- Translated using Spring i18n `.properties` files
- **Cost: $0** - No API calls needed
- Languages: English, German, French, Italian, Spanish (can expand to all 20)

### Tier 2: User Content Translation (API with Caching)
- User bios, allergy notes
- Translated using Google Cloud Translation API
- **Cost Optimization: Database caching** - Each unique text translated only once
- Languages: 20 most relevant for restaurants/travel

## Supported Languages (20)

| Code | Language | Region | Why Important |
|------|----------|--------|---------------|
| `en` | English | Global | International standard |
| `es` | Spanish | Spain, Latin America | 2nd most spoken |
| `fr` | French | France, Canada, Africa | Major tourist destinations |
| `de` | German | Germany, Austria, Switzerland | Central Europe travel |
| `it` | Italian | Italy | Major tourist destination |
| `pt` | Portuguese | Portugal, Brazil | Growing markets |
| `ru` | Russian | Russia, Eastern Europe | Large market |
| `zh` | Chinese (Simplified) | China, Singapore | Largest population |
| `ja` | Japanese | Japan | Major tourist destination |
| `ko` | Korean | South Korea | Growing tourist market |
| `ar` | Arabic | Middle East, North Africa | Large region |
| `tr` | Turkish | Turkey | Tourist destination |
| `nl` | Dutch | Netherlands, Belgium | Western Europe |
| `pl` | Polish | Poland | Eastern Europe |
| `sv` | Swedish | Sweden | Scandinavia |
| `da` | Danish | Denmark | Scandinavia |
| `no` | Norwegian | Norway | Scandinavia |
| `fi` | Finnish | Finland | Scandinavia |
| `el` | Greek | Greece | Tourist destination |
| `hi` | Hindi | India | Large population |

## Cost Optimization Strategy

### Database Caching
Every translation is stored in the `translation_cache` table:

```sql
CREATE TABLE translation_cache (
    id BIGSERIAL PRIMARY KEY,
    source_text_hash VARCHAR(64) NOT NULL,  -- SHA-256 hash for fast lookup
    source_text TEXT NOT NULL,
    source_lang VARCHAR(10) NOT NULL,
    target_lang VARCHAR(10) NOT NULL,
    translated_text TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    last_accessed TIMESTAMP NOT NULL,
    access_count BIGINT NOT NULL DEFAULT 0,
    INDEX idx_translation_lookup (source_text_hash, source_lang, target_lang)
);
```

### How It Works
1. **User writes bio in English**: "I have severe peanut allergy"
2. **First visitor views in German**:
   - Not in cache → Call API → Store translation
   - Cost: ~$0.00002 (20 characters × $20/million characters)
3. **All future visitors**:
   - Found in cache → Return instantly
   - Cost: $0

### Example Cost Analysis
- 1,000 users with average 100-character bios
- Translated to 19 other languages
- Total characters: 1,000 × 100 × 19 = 1.9 million characters
- **Total cost: $0.038** (one-time)
- All subsequent views: **$0**

## Setup Instructions

### 1. Create Google Cloud Project

```bash
# Install Google Cloud SDK
# Visit: https://cloud.google.com/sdk/docs/install

# Login
gcloud auth login

# Create project
gcloud projects create allergy-passport-12345

# Set active project
gcloud config set project allergy-passport-12345
```

### 2. Enable Translation API

```bash
# Enable the Cloud Translation API
gcloud services enable translate.googleapis.com

# Create service account
gcloud iam service-accounts create translation-service \
    --display-name="Translation Service Account"

# Grant Translation API User role
gcloud projects add-iam-policy-binding allergy-passport-12345 \
    --member="serviceAccount:translation-service@allergy-passport-12345.iam.gserviceaccount.com" \
    --role="roles/cloudtranslate.user"

# Create and download key
gcloud iam service-accounts keys create translation-key.json \
    --iam-account=translation-service@allergy-passport-12345.iam.gserviceaccount.com
```

### 3. Configure Application

#### Docker Environment Variables

Add to `docker-compose.yml`:

```yaml
services:
  app:
    environment:
      # Translation Configuration
      - TRANSLATION_PROVIDER=google-cloud
      - GOOGLE_CLOUD_PROJECT_ID=allergy-passport-12345
      - GOOGLE_APPLICATION_CREDENTIALS=/app/config/translation-key.json
    volumes:
      - ./translation-key.json:/app/config/translation-key.json:ro
```

#### Local Development

Add to `.env` or set environment variables:

```bash
export TRANSLATION_PROVIDER=google-cloud
export GOOGLE_CLOUD_PROJECT_ID=allergy-passport-12345
export GOOGLE_APPLICATION_CREDENTIALS=/path/to/translation-key.json
```

### 4. Test Configuration

```bash
# Check if API is accessible
curl -H "Authorization: Bearer $(gcloud auth print-access-token)" \
  -H "x-goog-user-project: allergy-passport-12345" \
  -H "Content-Type: application/json" \
  "https://translation.googleapis.com/v3/projects/allergy-passport-12345/locations/global:translateText" \
  -X POST \
  -d '{
    "sourceLanguageCode": "en",
    "targetLanguageCode": "de",
    "contents": ["Hello World"],
    "mimeType": "text/plain"
  }'
```

## Development Mode (Mock Translation)

For development without API costs, the application defaults to mock mode:

```properties
# application.properties (default)
translation.provider=mock
```

Mock mode returns the original text without calling the API.

## Monitoring & Cost Control

### Check Cache Statistics

```sql
-- Total translations cached
SELECT COUNT(*) FROM translation_cache;

-- Most accessed translations
SELECT source_text, target_lang, access_count
FROM translation_cache
ORDER BY access_count DESC
LIMIT 10;

-- Cache efficiency (should be high)
SELECT
    target_lang,
    COUNT(*) as unique_translations,
    SUM(access_count) as total_accesses,
    AVG(access_count) as avg_reuse
FROM translation_cache
GROUP BY target_lang;
```

### Cache Cleanup (Optional)

Remove old, rarely accessed translations:

```sql
-- Delete translations not accessed in 6 months
DELETE FROM translation_cache
WHERE last_accessed < NOW() - INTERVAL '6 months'
AND access_count < 5;
```

## Adding UI Translations for New Languages

To add a new language (e.g., Turkish - `tr`):

1. Copy existing messages file:
```bash
cp src/main/resources/i18n/messages_en.properties \
   src/main/resources/i18n/messages_tr.properties
```

2. Translate all values in `messages_tr.properties`

3. Add to language switcher (`language-switcher.html`):
```html
<a th:href="@{''(lang='tr')}" class="flex items-center...">
    <span class="mr-3">🇹🇷</span>
    <span>Türkçe</span>
</a>
```

4. User content (bios, notes) will auto-translate via API

## Security Notes

- **Never commit** `translation-key.json` to Git
- Add to `.gitignore`:
  ```
  translation-key.json
  *-key.json
  ```
- Use environment variables in production
- Restrict service account to Translation API only
- Consider setting up billing alerts in Google Cloud Console

## Pricing Reference

- **Google Cloud Translation API v3**: $20 per million characters
- **Free tier**: First 500,000 characters per month are free
- **Typical usage**: With 1,000 users and caching, monthly cost < $1

## Architecture Diagram

```
User writes bio in English
         ↓
[Check translation_cache]
         ↓
    Not Found? → [Google Cloud Translation API] → Store in cache
         ↓
    Found? → Return from cache (instant, $0)
         ↓
Display translated text
```

## Troubleshooting

### "Google Cloud Project ID not configured"
- Set `GOOGLE_CLOUD_PROJECT_ID` environment variable
- Verify it matches your Google Cloud project

### "Authentication failed"
- Check `GOOGLE_APPLICATION_CREDENTIALS` path is correct
- Verify service account has `roles/cloudtranslate.user` role

### "Translation returns original text"
- Check logs for API errors
- Verify billing is enabled in Google Cloud Console
- Confirm Translation API is enabled

### High API costs
- Check cache hit rate in database
- Most translations should come from cache
- If seeing many API calls, check for cache misses

## Next Steps

1. Enable Google Cloud Translation API
2. Set up service account and credentials
3. Configure environment variables
4. Test with a few translations
5. Monitor cache performance
6. Add UI translations for additional languages as needed
