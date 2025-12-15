package com.allergypassport.controller;

import com.allergypassport.entity.Allergen;
import com.allergypassport.entity.AllergenCategory;
import com.allergypassport.entity.User;
import com.allergypassport.repository.UserRepository;
import com.allergypassport.service.AllergenService;
import com.allergypassport.service.CustomOAuth2User;
import com.allergypassport.service.TranslationService;
import com.allergypassport.service.UserService;
import com.allergypassport.util.QRCodeService;
import com.google.zxing.WriterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Main controller for page rendering (Thymeleaf views).
 */
@Controller
public class PageController {

    private static final Logger log = LoggerFactory.getLogger(PageController.class);

    private final UserService userService;
    private final UserRepository userRepository;
    private final QRCodeService qrCodeService;
    private final MessageSource messageSource;
    private final TranslationService translationService;
    private final AllergenService allergenService;

    // Supported languages for the public view - 20 languages for restaurants/travel
    private static final List<Locale> SUPPORTED_LOCALES = List.of(
            Locale.ENGLISH,         // en - English
            new Locale("es"),       // es - Spanish
            Locale.FRENCH,          // fr - French
            Locale.GERMAN,          // de - German
            Locale.ITALIAN,         // it - Italian
            new Locale("pt"),       // pt - Portuguese
            new Locale("ru"),       // ru - Russian
            Locale.SIMPLIFIED_CHINESE,  // zh - Chinese (Simplified)
            Locale.JAPANESE,        // ja - Japanese
            Locale.KOREAN,          // ko - Korean
            new Locale("ar"),       // ar - Arabic
            new Locale("tr"),       // tr - Turkish
            new Locale("nl"),       // nl - Dutch
            new Locale("pl"),       // pl - Polish
            new Locale("sv"),       // sv - Swedish
            new Locale("da"),       // da - Danish
            new Locale("no"),       // no - Norwegian
            new Locale("fi"),       // fi - Finnish
            new Locale("el"),       // el - Greek
            new Locale("hi")        // hi - Hindi
    );

    public PageController(UserService userService,
                          UserRepository userRepository,
                          QRCodeService qrCodeService,
                          MessageSource messageSource,
                          TranslationService translationService,
                          AllergenService allergenService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.qrCodeService = qrCodeService;
        this.messageSource = messageSource;
        this.translationService = translationService;
        this.allergenService = allergenService;
    }

    /**
     * Home page - shows landing page for unauthenticated users,
     * redirects to dashboard for authenticated users.
     */
    @GetMapping("/")
    public String home(@AuthenticationPrincipal CustomOAuth2User principal) {
        if (principal != null) {
            return "redirect:/dashboard";
        }
        return "index";
    }

    /**
     * Login page with Google OAuth2.
     */
    @GetMapping("/login")
    public String login(@AuthenticationPrincipal CustomOAuth2User principal) {
        if (principal != null) {
            return "redirect:/dashboard";
        }
        return "login";
    }

    /**
     * User dashboard - main authenticated area for managing allergies.
     */
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomOAuth2User principal, Model model) {
        log.debug("Dashboard accessed - principal: {}", principal != null ? principal.getName() : "null");

        // Defensive null check - should not happen if security is properly configured
        if (principal == null) {
            log.error("Principal is null in dashboard - authentication failed or session lost");
            return "redirect:/login?error=session";
        }

        User user = userService.findByGoogleIdWithAllergies(principal.getGoogleId())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        // Get allergens grouped by category
        Map<AllergenCategory, List<Allergen>> allergensByCategory = allergenService.getAllergensByCategory();

        // Get list of already selected allergen IDs to filter them out
        List<Long> selectedAllergenIds = user.getAllergies().stream()
                .map(ua -> ua.getAllergen().getId())
                .toList();

        model.addAttribute("user", user);
        model.addAttribute("allergies", user.getAllergies());
        model.addAttribute("allergensByCategory", allergensByCategory);
        model.addAttribute("selectedAllergenIds", selectedAllergenIds);
        model.addAttribute("publicUrl", qrCodeService.buildPublicUrl(user.getPublicId()));

        // Generate QR code
        try {
            String qrDataUrl = qrCodeService.generateQRCodeAsDataUrl(user.getPublicId());
            model.addAttribute("qrCodeDataUrl", qrDataUrl);
        } catch (WriterException | IOException e) {
            log.error("Failed to generate QR code for user {}", user.getPublicId(), e);
            model.addAttribute("qrCodeError", true);
        }

        return "dashboard";
    }

    /**
     * Profile settings page.
     */
    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal CustomOAuth2User principal, Model model) {
        User user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        model.addAttribute("user", user);
        return "profile";
    }

    /**
     * Public allergy passport view - accessible without login.
     */
    @GetMapping("/u/{publicId}")
    public String publicPassport(@PathVariable String publicId,
                                  @RequestParam(name = "lang", required = false) String langParam,
                                  Locale locale,
                                  Model model) {
        User user = userService.findByPublicId(publicId).orElse(null);

        if (user == null) {
            return "error/404";
        }

        // Use the locale from the request (set by LocaleChangeInterceptor)
        Locale currentLocale = locale;
        if (langParam != null && !langParam.isBlank()) {
            currentLocale = Locale.forLanguageTag(langParam);
        }

        model.addAttribute("user", user);
        model.addAttribute("allergies", user.getAllergies());
        model.addAttribute("currentLocale", currentLocale);
        model.addAttribute("supportedLocales", SUPPORTED_LOCALES);
        model.addAttribute("translationProvider", translationService.getProviderName());

        // Translate user content (bio and allergy notes) if needed
        translateUserContent(user, currentLocale, model);

        return "public/passport";
    }

    /**
     * Translate user bio and allergy notes to the target locale.
     * Detects source language first to avoid unnecessary translations.
     * Uses caching to minimize API calls.
     */
    private void translateUserContent(User user, Locale targetLocale, Model model) {
        String targetLang = normalizeLanguageCode(targetLocale.getLanguage());

        // Translate user bio with language detection
        if (user.getBio() != null && !user.getBio().isBlank()) {
            String detectedLang = translationService.detectLanguage(user.getBio());

            // Only translate if the detected language is different from target language
            if (detectedLang != null && detectedLang.equals(targetLang)) {
                log.debug("Bio is already in target language ({}), skipping translation", targetLang);
                model.addAttribute("translatedBio", user.getBio());
            } else {
                log.debug("Translating bio from {} to {}",
                          detectedLang != null ? detectedLang : "auto-detect", targetLang);
                String translatedBio = translationService.translate(user.getBio(), null, targetLocale);
                model.addAttribute("translatedBio", translatedBio);
            }
        }

        // Translate allergy notes with language detection for each note
        java.util.Map<Long, String> translatedNotes = new java.util.HashMap<>();
        for (var allergy : user.getAllergies()) {
            if (allergy.getNotes() != null && !allergy.getNotes().isBlank()) {
                String detectedLang = translationService.detectLanguage(allergy.getNotes());

                // Only translate if the detected language is different from target language
                if (detectedLang != null && detectedLang.equals(targetLang)) {
                    log.debug("Allergy note {} is already in target language ({}), skipping translation",
                              allergy.getId(), targetLang);
                    translatedNotes.put(allergy.getId(), allergy.getNotes());
                } else {
                    log.debug("Translating allergy note {} from {} to {}",
                              allergy.getId(),
                              detectedLang != null ? detectedLang : "auto-detect",
                              targetLang);
                    String translatedNote = translationService.translate(allergy.getNotes(), null, targetLocale);
                    translatedNotes.put(allergy.getId(), translatedNote);
                }
            }
        }
        model.addAttribute("translatedNotes", translatedNotes);
    }

    /**
     * Normalize language code to handle special cases like Chinese.
     */
    private String normalizeLanguageCode(String langCode) {
        if (langCode == null || langCode.isBlank()) {
            return "en";
        }

        // Handle locale formats like "en_US", "de_DE"
        if (langCode.contains("_")) {
            langCode = langCode.split("_")[0];
        }

        langCode = langCode.toLowerCase();

        // Special case for Chinese - default to Simplified
        if (langCode.startsWith("zh")) {
            return "zh-CN";
        }

        return langCode;
    }

    /**
     * Get available allergens grouped by category (for HTMX).
     */
    @GetMapping("/allergens")
    public String getAllergens(Model model) {
        Map<AllergenCategory, List<Allergen>> allergensByCategory = allergenService.getAllergensByCategory();
        model.addAttribute("allergensByCategory", allergensByCategory);
        return "fragments/allergens :: allergensList";
    }
}
