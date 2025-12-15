# Database Migration Instructions

## Quick Migration (Recommended)

**Option 1: Using Docker Exec**
```bash
# Copy the migration script into the database container and execute it
docker cp database-migration.sql allergy-passport-db:/tmp/migration.sql
docker exec -i allergy-passport-db psql -U allergypassport -d allergypassport -f /tmp/migration.sql
```

**Option 2: Using Docker Exec with Pipe**
```bash
# Execute directly without copying file
docker exec -i allergy-passport-db psql -U allergypassport -d allergypassport < database-migration.sql
```

**Option 3: Using psql from host (if PostgreSQL client installed)**
```bash
psql -h localhost -p 5432 -U allergypassport -d allergypassport -f database-migration.sql
# Password: allergypassport (default)
```

## After Migration

1. **Restart the application**:
   ```bash
   docker-compose restart app
   ```

2. **Verify initialization**:
   Check the application logs to confirm `DataInitializationService` has populated allergen data:
   ```bash
   docker logs -f allergy-passport-app | grep -i "allergen"
   ```

   You should see messages like:
   - "Loading allergen keyword mappings from database..."
   - "Created category: legally_mandated"
   - "Created allergen: peanuts"

3. **Test the application**:
   - Users will need to re-add their allergies (old data was cleared)
   - The new category-grouped allergen selector should appear
   - OCR scanning should work with database keywords

## What the Migration Does

1. ✅ Drops the old `allergy_type` enum column
2. ✅ Adds new `allergen_id` foreign key column
3. ✅ Updates the unique constraint
4. ✅ Sets up proper foreign key relationships

## Important Notes

⚠️ **This migration will delete existing user allergy data!** The old enum-based allergies cannot be automatically migrated to the new system. Users will need to re-add their allergies after the migration.

If you need to preserve user data, you would need a more complex migration that:
1. Creates a mapping table between old enum values and new allergen IDs
2. Migrates existing data based on this mapping
3. Then removes the old column

For development/testing purposes, the simple migration above is recommended.
