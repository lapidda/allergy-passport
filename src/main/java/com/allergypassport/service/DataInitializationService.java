package com.allergypassport.service;

import com.allergypassport.entity.*;
import com.allergypassport.repository.*;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service to initialize database with allergen data on startup.
 * Populates categories, allergens, translations, and keywords if database is empty.
 */
@Service
public class DataInitializationService {

    private static final Logger log = LoggerFactory.getLogger(DataInitializationService.class);

    private final AllergenCategoryRepository categoryRepository;
    private final AllergenRepository allergenRepository;
    private final AllergenTranslationRepository translationRepository;
    private final AllergenKeywordRepository keywordRepository;
    private final CategoryTranslationRepository categoryTranslationRepository;

    public DataInitializationService(
            AllergenCategoryRepository categoryRepository,
            AllergenRepository allergenRepository,
            AllergenTranslationRepository translationRepository,
            AllergenKeywordRepository keywordRepository,
            CategoryTranslationRepository categoryTranslationRepository) {
        this.categoryRepository = categoryRepository;
        this.allergenRepository = allergenRepository;
        this.translationRepository = translationRepository;
        this.keywordRepository = keywordRepository;
        this.categoryTranslationRepository = categoryTranslationRepository;
    }

    /**
     * Initialize database with allergen data if empty.
     */
    @PostConstruct
    @Transactional
    public void initializeData() {
        // Check if data already exists
        if (allergenRepository.count() > 0) {
            log.info("Allergen data already exists. Skipping initialization.");
            return;
        }

        log.info("Initializing allergen database...");

        try {
            // Create categories
            Map<String, AllergenCategory> categories = createCategories();

            // Create allergens with translations and keywords
            createAllergens(categories);

            log.info("Successfully initialized allergen database with {} categories and {} allergens",
                    categoryRepository.count(), allergenRepository.count());

        } catch (Exception e) {
            log.error("Failed to initialize allergen data", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    /**
     * Create allergen categories with translations.
     */
    private Map<String, AllergenCategory> createCategories() {
        Map<String, AllergenCategory> categories = new HashMap<>();

        // Category translations
        Map<String, Map<String, String>> categoryNames = Map.of(
                "legally_mandated", Map.of(
                        "en", "Legally Mandated Allergens",
                        "de", "Gesetzlich vorgeschriebene Allergene",
                        "es", "Alérgenos legalmente obligatorios",
                        "fr", "Allergènes légalement obligatoires",
                        "it", "Allergeni legalmente obbligatori"
                ),
                "fruits", Map.of(
                        "en", "Fruits",
                        "de", "Früchte",
                        "es", "Frutas",
                        "fr", "Fruits",
                        "it", "Frutta"
                ),
                "vegetables", Map.of(
                        "en", "Vegetables",
                        "de", "Gemüse",
                        "es", "Verduras",
                        "fr", "Légumes",
                        "it", "Verdure"
                ),
                "grains_sugars", Map.of(
                        "en", "Grains & Sugars",
                        "de", "Getreide & Zucker",
                        "es", "Granos y Azúcares",
                        "fr", "Céréales & Sucres",
                        "it", "Cereali & Zuccheri"
                ),
                "spices_herbs", Map.of(
                        "en", "Spices & Herbs",
                        "de", "Gewürze & Kräuter",
                        "es", "Especias y Hierbas",
                        "fr", "Épices & Herbes",
                        "it", "Spezie & Erbe"
                ),
                "beverages", Map.of(
                        "en", "Beverages",
                        "de", "Getränke",
                        "es", "Bebidas",
                        "fr", "Boissons",
                        "it", "Bevande"
                ),
                "additives", Map.of(
                        "en", "Additives",
                        "de", "Zusatzstoffe",
                        "es", "Aditivos",
                        "fr", "Additifs",
                        "it", "Additivi"
                ),
                "other", Map.of(
                        "en", "Other",
                        "de", "Andere",
                        "es", "Otros",
                        "fr", "Autres",
                        "it", "Altri"
                )
        );

        String[][] categoryData = {
                {"legally_mandated", "⚠️", "1"},
                {"fruits", "🍎", "2"},
                {"vegetables", "🥕", "3"},
                {"grains_sugars", "🌾", "4"},
                {"spices_herbs", "🌿", "5"},
                {"beverages", "☕", "6"},
                {"additives", "🧪", "7"},
                {"other", "📋", "8"}
        };

        for (String[] data : categoryData) {
            String code = data[0];
            String icon = data[1];
            int order = Integer.parseInt(data[2]);

            AllergenCategory category = new AllergenCategory();
            category.setCode(code);
            category.setIcon(icon);
            category.setDisplayOrder(order);
            category = categoryRepository.save(category);

            // Add translations
            Map<String, String> names = categoryNames.get(code);
            for (Map.Entry<String, String> entry : names.entrySet()) {
                CategoryTranslation translation = new CategoryTranslation();
                translation.setCategory(category);
                translation.setLanguageCode(entry.getKey());
                translation.setName(entry.getValue());
                categoryTranslationRepository.save(translation);
            }

            categories.put(code, category);
            log.debug("Created category: {}", code);
        }

        return categories;
    }

    /**
     * Create allergens with translations and keywords.
     */
    private void createAllergens(Map<String, AllergenCategory> categories) {
        // Legally Mandated Allergens (EU Regulation 1169/2011)
        createAllergen("peanuts", categories.get("legally_mandated"), 1, true,
                Map.of("en", "Peanuts", "de", "Erdnüsse", "es", "Cacahuetes", "fr", "Arachides", "it", "Arachidi"),
                Map.of("en", List.of("peanut", "peanuts", "groundnut", "arachis"),
                       "de", List.of("erdnuss", "erdnüsse"),
                       "es", List.of("cacahuete", "maní")));

        createAllergen("tree_nuts", categories.get("legally_mandated"), 2, true,
                Map.of("en", "Tree Nuts", "de", "Baumnüsse", "es", "Frutos de cáscara", "fr", "Fruits à coque", "it", "Frutta a guscio"),
                Map.of("en", List.of("almond", "hazelnut", "walnut", "cashew", "pecan", "pistachio", "macadamia"),
                       "de", List.of("mandel", "haselnuss", "walnuss", "cashew")));

        createAllergen("gluten", categories.get("legally_mandated"), 3, true,
                Map.of("en", "Gluten (Wheat, Rye, Barley, Oats)", "de", "Gluten (Weizen, Roggen, Gerste, Hafer)",
                       "es", "Gluten (Trigo, Centeno, Cebada, Avena)", "fr", "Gluten (Blé, Seigle, Orge, Avoine)"),
                Map.of("en", List.of("gluten", "wheat", "rye", "barley", "oats", "spelt"),
                       "de", List.of("gluten", "weizen", "roggen", "gerste", "hafer")));

        createAllergen("dairy", categories.get("legally_mandated"), 4, true,
                Map.of("en", "Milk & Dairy", "de", "Milch & Milchprodukte", "es", "Leche y Lácteos",
                       "fr", "Lait & Produits Laitiers", "it", "Latte e Latticini"),
                Map.of("en", List.of("milk", "dairy", "lactose", "cheese", "butter", "cream", "yogurt"),
                       "de", List.of("milch", "käse", "butter", "sahne", "joghurt", "laktose")));

        createAllergen("eggs", categories.get("legally_mandated"), 5, true,
                Map.of("en", "Eggs", "de", "Eier", "es", "Huevos", "fr", "Œufs", "it", "Uova"),
                Map.of("en", List.of("egg", "eggs", "albumin", "ovum"),
                       "de", List.of("ei", "eier")));

        createAllergen("fish", categories.get("legally_mandated"), 6, true,
                Map.of("en", "Fish", "de", "Fisch", "es", "Pescado", "fr", "Poisson", "it", "Pesce"),
                Map.of("en", List.of("fish", "salmon", "tuna", "cod", "anchovy"),
                       "de", List.of("fisch", "lachs", "thunfisch")));

        createAllergen("shellfish", categories.get("legally_mandated"), 7, true,
                Map.of("en", "Crustaceans (Shellfish)", "de", "Krebstiere", "es", "Crustáceos",
                       "fr", "Crustacés", "it", "Crostacei"),
                Map.of("en", List.of("shrimp", "prawn", "crab", "lobster", "crayfish"),
                       "de", List.of("garnele", "krabbe", "hummer", "krebs")));

        createAllergen("molluscs", categories.get("legally_mandated"), 8, true,
                Map.of("en", "Molluscs", "de", "Weichtiere", "es", "Moluscos", "fr", "Mollusques", "it", "Molluschi"),
                Map.of("en", List.of("mussel", "oyster", "clam", "scallop", "squid", "octopus"),
                       "de", List.of("muschel", "auster", "tintenfisch")));

        createAllergen("soy", categories.get("legally_mandated"), 9, true,
                Map.of("en", "Soy", "de", "Soja", "es", "Soja", "fr", "Soja", "it", "Soia"),
                Map.of("en", List.of("soy", "soya", "soybean", "tofu", "edamame"),
                       "de", List.of("soja", "tofu")));

        createAllergen("sesame", categories.get("legally_mandated"), 10, true,
                Map.of("en", "Sesame", "de", "Sesam", "es", "Sésamo", "fr", "Sésame", "it", "Sesamo"),
                Map.of("en", List.of("sesame", "tahini"),
                       "de", List.of("sesam")));

        createAllergen("celery", categories.get("legally_mandated"), 11, true,
                Map.of("en", "Celery", "de", "Sellerie", "es", "Apio", "fr", "Céleri", "it", "Sedano"),
                Map.of("en", List.of("celery", "celeriac"),
                       "de", List.of("sellerie")));

        createAllergen("mustard", categories.get("legally_mandated"), 12, true,
                Map.of("en", "Mustard", "de", "Senf", "es", "Mostaza", "fr", "Moutarde", "it", "Senape"),
                Map.of("en", List.of("mustard"),
                       "de", List.of("senf")));

        createAllergen("lupin", categories.get("legally_mandated"), 13, true,
                Map.of("en", "Lupin", "de", "Lupine", "es", "Altramuz", "fr", "Lupin", "it", "Lupino"),
                Map.of("en", List.of("lupin", "lupine"),
                       "de", List.of("lupine")));

        createAllergen("sulphites", categories.get("legally_mandated"), 14, true,
                Map.of("en", "Sulphites (>10mg/kg)", "de", "Sulfite (>10mg/kg)", "es", "Sulfitos (>10mg/kg)",
                       "fr", "Sulfites (>10mg/kg)", "it", "Solfiti (>10mg/kg)"),
                Map.of("en", List.of("sulphite", "sulfite", "sulfur dioxide"),
                       "de", List.of("sulfit", "schwefeldioxid")));

        // Common Fruits
        createAllergen("kiwi", categories.get("fruits"), 1, false,
                Map.of("en", "Kiwi", "de", "Kiwi", "es", "Kiwi", "fr", "Kiwi", "it", "Kiwi"),
                Map.of("en", List.of("kiwi"), "de", List.of("kiwi")));

        createAllergen("banana", categories.get("fruits"), 2, false,
                Map.of("en", "Banana", "de", "Banane", "es", "Plátano", "fr", "Banane", "it", "Banana"),
                Map.of("en", List.of("banana"), "de", List.of("banane")));

        createAllergen("strawberry", categories.get("fruits"), 3, false,
                Map.of("en", "Strawberry", "de", "Erdbeere", "es", "Fresa", "fr", "Fraise", "it", "Fragola"),
                Map.of("en", List.of("strawberry", "strawberries"), "de", List.of("erdbeere")));

        createAllergen("apple", categories.get("fruits"), 4, false,
                Map.of("en", "Apple", "de", "Apfel", "es", "Manzana", "fr", "Pomme", "it", "Mela"),
                Map.of("en", List.of("apple"), "de", List.of("apfel")));

        createAllergen("peach", categories.get("fruits"), 5, false,
                Map.of("en", "Peach", "de", "Pfirsich", "es", "Melocotón", "fr", "Pêche", "it", "Pesca"),
                Map.of("en", List.of("peach"), "de", List.of("pfirsich")));

        createAllergen("mango", categories.get("fruits"), 6, false,
                Map.of("en", "Mango", "de", "Mango", "es", "Mango", "fr", "Mangue", "it", "Mango"),
                Map.of("en", List.of("mango"), "de", List.of("mango")));

        // Vegetables
        createAllergen("tomato", categories.get("vegetables"), 1, false,
                Map.of("en", "Tomato", "de", "Tomate", "es", "Tomate", "fr", "Tomate", "it", "Pomodoro"),
                Map.of("en", List.of("tomato", "tomatoes"), "de", List.of("tomate")));

        createAllergen("onion", categories.get("vegetables"), 2, false,
                Map.of("en", "Onion", "de", "Zwiebel", "es", "Cebolla", "fr", "Oignon", "it", "Cipolla"),
                Map.of("en", List.of("onion"), "de", List.of("zwiebel")));

        createAllergen("garlic", categories.get("vegetables"), 3, false,
                Map.of("en", "Garlic", "de", "Knoblauch", "es", "Ajo", "fr", "Ail", "it", "Aglio"),
                Map.of("en", List.of("garlic"), "de", List.of("knoblauch")));

        // Spices & Herbs
        createAllergen("cinnamon", categories.get("spices_herbs"), 1, false,
                Map.of("en", "Cinnamon", "de", "Zimt", "es", "Canela", "fr", "Cannelle", "it", "Cannella"),
                Map.of("en", List.of("cinnamon"), "de", List.of("zimt")));

        createAllergen("coriander", categories.get("spices_herbs"), 2, false,
                Map.of("en", "Coriander/Cilantro", "de", "Koriander", "es", "Cilantro", "fr", "Coriandre", "it", "Coriandolo"),
                Map.of("en", List.of("coriander", "cilantro"), "de", List.of("koriander")));

        // Other common allergens
        createAllergen("corn", categories.get("grains_sugars"), 1, false,
                Map.of("en", "Corn/Maize", "de", "Mais", "es", "Maíz", "fr", "Maïs", "it", "Mais"),
                Map.of("en", List.of("corn", "maize"), "de", List.of("mais")));

        createAllergen("rice", categories.get("grains_sugars"), 2, false,
                Map.of("en", "Rice", "de", "Reis", "es", "Arroz", "fr", "Riz", "it", "Riso"),
                Map.of("en", List.of("rice"), "de", List.of("reis")));

        log.info("Created {} allergens", allergenRepository.count());
    }

    /**
     * Helper method to create an allergen with translations and keywords.
     */
    private void createAllergen(String code, AllergenCategory category, int displayOrder,
                                boolean isLegallyMandated, Map<String, String> names,
                                Map<String, List<String>> keywords) {
        Allergen allergen = new Allergen();
        allergen.setCode(code);
        allergen.setCategory(category);
        allergen.setDisplayOrder(displayOrder);
        allergen.setIsLegallyMandated(isLegallyMandated);
        allergen = allergenRepository.save(allergen);

        // Add translations
        for (Map.Entry<String, String> entry : names.entrySet()) {
            AllergenTranslation translation = new AllergenTranslation();
            translation.setAllergen(allergen);
            translation.setLanguageCode(entry.getKey());
            translation.setName(entry.getValue());
            translationRepository.save(translation);
        }

        // Add keywords
        for (Map.Entry<String, List<String>> entry : keywords.entrySet()) {
            String lang = entry.getKey();
            for (String keyword : entry.getValue()) {
                AllergenKeyword kw = new AllergenKeyword();
                kw.setAllergen(allergen);
                kw.setLanguageCode(lang);
                kw.setKeyword(keyword.toLowerCase());
                keywordRepository.save(kw);
            }
        }

        log.debug("Created allergen: {}", code);
    }
}
