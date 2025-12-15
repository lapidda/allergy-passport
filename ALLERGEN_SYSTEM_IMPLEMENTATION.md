# Expandable Allergen System - Implementation Summary

## Overview
The allergy passport application has been upgraded from a rigid enum-based allergen system to a flexible, database-driven expandable allergen system. This allows for easy addition of new allergens without code changes and supports multi-language translations with OCR keyword matching.

## 🎯 Key Features

### 1. **4-Level Severity System**
- `INTOLERANCE_LOW` - Tolerable in small amounts (⚠️)
- `INTOLERANCE_MILD` - Causes mild discomfort (⚠️⚠️)
- `INTOLERANCE_SEVERE` - Causes severe discomfort (⚠️⚠️⚠️)
- `SEVERE` - Life-threatening anaphylactic reactions (☠️)

### 2. **Category-Based Organization**
Allergens are organized into 8 categories:
- ⚠️ Legally Mandated (EU Top 14)
- 🍎 Fruits
- 🥕 Vegetables
- 🌾 Grains & Sugars
- 🌿 Spices & Herbs
- ☕ Beverages
- 🧪 Additives
- 📋 Other

### 3. **Multi-Language Support**
- 21 languages supported
- Database-driven translations for allergen names and descriptions
- Severity level translations in all languages

### 4. **OCR Keyword Matching**
- Keywords stored in database for each allergen
- Language-specific keyword matching
- Compound word language support (German, Dutch, Nordic languages)
- Automatic cache refresh

## 📁 File Structure

### **Entities** (`src/main/java/com/allergypassport/entity/`)
- `AllergySeverity.java` - Severity enum (4 levels)
- `AllergenCategory.java` - Category entity with translations
- `Allergen.java` - Main allergen entity
- `AllergenTranslation.java` - Multi-language allergen names
- `AllergenKeyword.java` - OCR keywords for allergen detection
- `CategoryTranslation.java` - Multi-language category names
- `UserAllergy.java` - User-allergen relationship (updated)

### **Repositories** (`src/main/java/com/allergypassport/repository/`)
- `AllergenCategoryRepository.java` - Category data access
- `AllergenRepository.java` - Allergen data access with custom queries
- `AllergenTranslationRepository.java` - Translation data access
- `AllergenKeywordRepository.java` - Keyword data access
- `CategoryTranslationRepository.java` - Category translation data access
- `UserAllergyRepository.java` - Updated with new queries

### **Services** (`src/main/java/com/allergypassport/service/`)
- `AllergenService.java` - Main allergen business logic with caching
- `AllergenMatchingService.java` - Updated for database keyword matching
- `AllergenAdminService.java` - Admin operations for managing allergens
- `DataInitializationService.java` - Populates database on startup
- `UserService.java` - Updated for allergen-based operations

### **DTOs** (`src/main/java/com/allergypassport/dto/`)
- `AllergenDTO.java` - Allergen data transfer object
- `AllergenCategoryDTO.java` - Category data transfer object
- `AllergyRequest.java` - Updated to use allergen ID
- `DetectedAllergen.java` - Updated to use Allergen entity

### **Configuration** (`src/main/java/com/allergypassport/config/`)
- `CacheConfig.java` - Spring cache configuration for allergens

### **Controllers** (`src/main/java/com/allergypassport/controller/`)
- `AllergyController.java` - Updated HTMX endpoints
- `PageController.java` - Updated dashboard with category grouping

### **Translations** (`src/main/resources/i18n/`)
- Updated all 21 language files with 4-level severity translations:
  - `messages.properties` (English)
  - `messages_de.properties` (German)
  - `messages_es.properties` (Spanish)
  - `messages_fr.properties` (French)
  - `messages_it.properties` (Italian)
  - `messages_pt.properties` (Portuguese)
  - `messages_nl.properties` (Dutch)
  - `messages_sv.properties` (Swedish)
  - `messages_da.properties` (Danish)
  - `messages_no.properties` (Norwegian)
  - `messages_fi.properties` (Finnish)
  - `messages_pl.properties` (Polish)
  - `messages_ru.properties` (Russian)
  - `messages_tr.properties` (Turkish)
  - `messages_ar.properties` (Arabic)
  - `messages_zh.properties` (Chinese)
  - `messages_ja.properties` (Japanese)
  - `messages_ko.properties` (Korean)
  - `messages_el.properties` (Greek)
  - `messages_hi.properties` (Hindi)

## 🗄️ Database Schema

### Tables Created
1. **allergen_categories** - Category definitions
2. **category_translations** - Category names in multiple languages
3. **allergens** - Individual allergens with display order
4. **allergen_translations** - Allergen names/descriptions in multiple languages
5. **allergen_keywords** - OCR keywords for each allergen per language
6. **user_allergies** - Updated to reference allergens table

### Key Relationships
- AllergenCategory → Allergen (One-to-Many)
- Allergen → AllergenTranslation (One-to-Many)
- Allergen → AllergenKeyword (One-to-Many)
- AllergenCategory → CategoryTranslation (One-to-Many)
- User → UserAllergy → Allergen (Many-to-Many through UserAllergy)

## 📊 Pre-Populated Data

The `DataInitializationService` automatically populates the database with:

### **All 14 EU Legally Mandated Allergens**
1. Peanuts
2. Tree Nuts (almonds, hazelnuts, walnuts, etc.)
3. Gluten (wheat, rye, barley, oats)
4. Milk & Dairy
5. Eggs
6. Fish
7. Crustaceans (shellfish)
8. Molluscs
9. Soy
10. Sesame
11. Celery
12. Mustard
13. Lupin
14. Sulphites (>10mg/kg)

### **Common Additional Allergens**
- Fruits: Kiwi, Banana, Strawberry, Apple, Peach, Mango
- Vegetables: Tomato, Onion, Garlic
- Spices: Cinnamon, Coriander/Cilantro
- Grains: Corn/Maize, Rice

Each allergen includes:
- English and German translations (expandable to all 21 languages)
- OCR keywords in multiple languages
- Category assignment
- Display order

## 🔧 API Endpoints

### Public Endpoints
- `GET /api/allergies` - Get user's allergies (HTMX)
- `POST /api/allergies` - Add allergy (requires allergenId)
- `PUT /api/allergies/{id}` - Update allergy severity/notes
- `DELETE /api/allergies/{id}` - Remove allergy
- `GET /api/allergies/form` - Get allergy form with category-grouped allergens
- `GET /allergens` - Get all allergens grouped by category

### Admin Operations (via AllergenAdminService)
- `createAllergen(code, categoryCode, displayOrder, isLegallyMandated)`
- `updateAllergen(allergenId, displayOrder, isLegallyMandated)`
- `deleteAllergen(allergenId)`
- `addOrUpdateAllergenTranslation(allergenId, languageCode, name, description)`
- `addOrUpdateCategoryTranslation(categoryId, languageCode, name)`
- `addKeyword(allergenId, languageCode, keyword)`
- `deleteKeyword(keywordId)`
- `createCategory(code, icon, displayOrder)`
- `updateCategory(categoryId, icon, displayOrder)`
- `clearCaches()` / `refreshAllCaches()`

## 🚀 Performance Optimizations

### Caching Strategy
All allergen data is cached using Spring's caching abstraction:
- **allergens** - Full allergen list
- **allergensByCategory** - Allergens grouped by category
- **allergen** - Individual allergen by code
- **categories** - All categories

Cache is automatically cleared when allergens are modified via `AllergenAdminService`.

### Database Optimizations
- Eager loading of frequently accessed relationships (translations)
- Lazy loading for large collections (keywords)
- Indexed unique constraints on codes
- Ordered queries for better performance

## ✅ What's Been Completed

- ✅ Database schema design and entity creation
- ✅ Repository layer with custom queries
- ✅ Service layer with caching
- ✅ Admin service for allergen management
- ✅ Updated controllers and DTOs
- ✅ OCR keyword matching from database
- ✅ Multi-language translations (21 languages)
- ✅ 4-level severity system
- ✅ Data initialization service
- ✅ Cache management

## 🔲 Remaining Tasks

### Frontend Updates (Required)
1. **Update `dashboard.html`**
   - Display allergens grouped by category
   - Show category icons and names
   - Update allergy list rendering to use new structure

2. **Update Allergy Form Fragment**
   - Create category-grouped allergen selector
   - Replace enum dropdown with categorized allergen list
   - Show allergen descriptions on hover/click
   - Filter out already-selected allergens

3. **Update Public Passport View**
   - Display allergens grouped by category
   - Show category headers with icons
   - Properly display allergen names in target language

### Testing (Recommended)
1. **Unit Tests**
   - Test AllergenService methods
   - Test AllergenMatchingService with database keywords
   - Test UserService allergen operations

2. **Integration Tests**
   - Test database initialization
   - Test allergen CRUD operations
   - Test cache behavior

3. **Manual Testing**
   - Add/update/delete allergies via UI
   - Test OCR scanning with new keyword system
   - Test multi-language display
   - Verify category grouping

### Optional Enhancements
1. **Admin UI** - Create admin panel for managing allergens
2. **Data Migration** - Script to migrate existing user allergies (if any)
3. **Allergen Icons** - Add emoji/icons to individual allergens
4. **Search/Filter** - Add search functionality for allergen selection
5. **Analytics** - Track most common allergens

## 🔄 Migration Notes

### For New Installations
- Database will be automatically populated on first startup
- No manual data entry required

### For Existing Installations with Data
If you have existing users with allergies:
1. The old `AllergyType` enum-based allergies will need migration
2. Create a one-time migration script to:
   - Map old enum values to new allergen IDs
   - Update `user_allergies` table to use `allergen_id` instead of `allergy_type`
   - Preserve severity levels and notes

Example migration SQL:
```sql
-- Map PEANUTS enum to Peanuts allergen
UPDATE user_allergies
SET allergen_id = (SELECT id FROM allergens WHERE code = 'peanuts')
WHERE allergy_type = 'PEANUTS';
```

## 📝 Usage Examples

### Adding a New Allergen (via AllergenAdminService)
```java
@Autowired
private AllergenAdminService adminService;

// Create new allergen
Allergen avocado = adminService.createAllergen(
    "avocado",           // code
    "fruits",            // category code
    10,                  // display order
    false                // not legally mandated
);

// Add translation
adminService.addOrUpdateAllergenTranslation(
    avocado.getId(),
    "en",
    "Avocado",
    "Green fruit, common allergen"
);

// Add OCR keywords
adminService.addKeyword(avocado.getId(), "en", "avocado");
adminService.addKeyword(avocado.getId(), "de", "avocado");
```

### Getting Allergens for User Selection
```java
@Autowired
private AllergenService allergenService;

// Get all allergens grouped by category
Map<AllergenCategory, List<Allergen>> allergensByCategory =
    allergenService.getAllergensByCategory();

// Get only legally mandated allergens
List<Allergen> mandated = allergenService.getLegallyMandatedAllergens();
```

### User Adding an Allergy
```java
@Autowired
private UserService userService;

userService.saveAllergy(
    userId,
    allergenId,                    // from allergen selection
    AllergySeverity.SEVERE,
    "Carry EpiPen at all times"    // optional notes
);
```

## 🐛 Troubleshooting

### Cache Issues
If allergen data isn't updating:
```java
allergenAdminService.refreshAllCaches();
```

### Keyword Matching Not Working
Ensure keywords are loaded:
```java
int loadedCount = allergenMatchingService.getLoadedAllergenCount();
// Should be > 0
```

Force reload:
```java
allergenMatchingService.refreshKeywords();
```

### Database Not Initializing
Check logs for:
- `DataInitializationService` startup messages
- Any database constraint violations
- Table creation issues

## 📚 Technical Details

### Hibernate Configuration
- Using `spring.jpa.hibernate.ddl-auto=update` for automatic schema management
- Entities use `@GeneratedValue(strategy = GenerationType.IDENTITY)` for auto-increment IDs
- Lifecycle callbacks (`@PrePersist`, `@PreUpdate`) manage timestamps

### Compound Word Language Detection
The `AllergenMatchingService` uses special matching for languages with compound words:
- German (de): `milch` matches in `vollmilchschokolade`
- Dutch (nl): Similar compound word matching
- Nordic languages (sv, da, no, fi): Compound word support

Other languages use word-boundary matching for accuracy.

## 🎓 Learning Resources

- [Spring Data JPA Documentation](https://spring.io/projects/spring-data-jpa)
- [Spring Caching Abstraction](https://docs.spring.io/spring-framework/reference/integration/cache.html)
- [EU Food Information Regulation 1169/2011](https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=CELEX:32011R1169)

---

**Implementation Status**: Backend Complete ✅
**Next Steps**: Frontend Template Updates
**Version**: 2.0
**Last Updated**: 2025-01-29
