package com.allergypassport.service;

import com.allergypassport.entity.User;
import com.allergypassport.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Custom OIDC user service that handles Google OpenID Connect login.
 * Creates or updates user records based on Google profile information.
 */
@Service
public class CustomOidcUserService extends OidcUserService {

    private static final Logger log = LoggerFactory.getLogger(CustomOidcUserService.class);

    private final UserRepository userRepository;

    public CustomOidcUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("=== OIDC loadUser called ===");

        // Delegate to the parent class to load the user from Google
        OidcUser oidcUser = super.loadUser(userRequest);
        log.debug("OidcUser loaded from Google: {}", oidcUser.getName());

        Map<String, Object> attributes = oidcUser.getAttributes();

        String googleId = (String) attributes.get("sub");
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String pictureUrl = (String) attributes.get("picture");

        log.info("Processing OIDC login for user: {} (googleId: {})", email, googleId);

        // Find existing user or create new one
        User user = userRepository.findByGoogleId(googleId)
                .map(existingUser -> {
                    log.debug("Updating existing user: {}", existingUser.getId());
                    return updateExistingUser(existingUser, email, name, pictureUrl);
                })
                .orElseGet(() -> {
                    log.info("Creating new user for: {}", email);
                    return createNewUser(googleId, email, name, pictureUrl);
                });

        log.info("User authenticated successfully: {} (ID: {}, PublicID: {})", user.getEmail(), user.getId(), user.getPublicId());

        CustomOAuth2User customUser = new CustomOAuth2User(oidcUser, user);
        log.debug("Created CustomOAuth2User - userId: {}, email: {}, googleId: {}",
                customUser.getUserId(), customUser.getEmail(), customUser.getGoogleId());
        log.debug("CustomOAuth2User is OidcUser: {}", customUser instanceof OidcUser);
        log.debug("CustomOAuth2User is Serializable: {}", customUser instanceof java.io.Serializable);

        // Test serialization
        try {
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(baos);
            oos.writeObject(customUser);
            oos.close();
            log.info("✓ CustomOAuth2User successfully serialized ({} bytes)", baos.size());
        } catch (Exception e) {
            log.error("✗ CustomOAuth2User serialization FAILED: {}", e.getMessage(), e);
        }

        return customUser;
    }

    private User updateExistingUser(User user, String email, String name, String pictureUrl) {
        // Update email and name in case they changed on Google side
        user.setEmail(email);
        if (user.getDisplayName() == null || user.getDisplayName().isBlank()) {
            user.setDisplayName(name);
        }
        user.setGooglePictureUrl(pictureUrl);
        return userRepository.save(user);
    }

    private User createNewUser(String googleId, String email, String name, String pictureUrl) {
        log.info("Creating new user for Google ID: {}", googleId);

        User user = new User(googleId, email, name);
        user.setGooglePictureUrl(pictureUrl);

        return userRepository.save(user);
    }
}
