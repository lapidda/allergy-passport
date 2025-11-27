package com.allergypassport.service;

import com.allergypassport.entity.User;
import com.allergypassport.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Custom OAuth2 user service that handles Google OAuth2 login.
 * Creates or updates user records based on Google profile information.
 */
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger log = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("=== OAuth2 loadUser called ===");

        OAuth2User oauth2User = super.loadUser(userRequest);
        log.debug("OAuth2User loaded from Google: {}", oauth2User.getName());

        Map<String, Object> attributes = oauth2User.getAttributes();

        String googleId = (String) attributes.get("sub");
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String pictureUrl = (String) attributes.get("picture");

        log.info("Processing OAuth2 login for user: {} (googleId: {})", email, googleId);

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

        CustomOAuth2User customUser = new CustomOAuth2User(oauth2User, user);
        log.debug("Created CustomOAuth2User - userId: {}, email: {}, googleId: {}",
                customUser.getUserId(), customUser.getEmail(), customUser.getGoogleId());
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
