package com.allergypassport.service;

import com.allergypassport.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Custom OAuth2User implementation that wraps the standard OAuth2User/OidcUser
 * and includes our application's User entity data.
 *
 * This class implements both OAuth2User and OidcUser to support both standard OAuth2
 * and OpenID Connect (OIDC) flows. It stores only primitive/immutable data (not JPA entities)
 * to enable proper serialization in HTTP sessions. Controllers fetch fresh User entities
 * from the database using the stored user ID.
 */
public class CustomOAuth2User implements OAuth2User, OidcUser, Serializable {

    private static final long serialVersionUID = 1L;

    // Store primitives and immutable objects, not JPA entities
    private final Long userId;
    private final String publicId;
    private final String email;
    private final String displayName;
    private final String googleId;
    private final String oauthName;
    private final HashMap<String, Object> attributes;
    private final ArrayList<GrantedAuthority> authorities;

    // OIDC-specific fields (for OpenID Connect)
    private final Map<String, Object> claims;
    private final OidcIdToken idToken;
    private final OidcUserInfo userInfo;

    public CustomOAuth2User(OAuth2User oauth2User, User user) {
        // Extract primitive values from User entity
        this.userId = user.getId();
        this.publicId = user.getPublicId();
        this.email = user.getEmail();
        this.displayName = user.getDisplayName();
        this.googleId = user.getGoogleId();
        this.oauthName = oauth2User.getName();

        // Only store serializable String attributes from OAuth2User
        // Filter out any non-serializable objects that Google might include
        this.attributes = new HashMap<>();
        oauth2User.getAttributes().forEach((key, value) -> {
            // Only store primitive types and Strings to ensure serializability
            if (value == null || value instanceof String ||
                value instanceof Number || value instanceof Boolean) {
                this.attributes.put(key, value);
            }
        });

        // Convert authorities to SimpleGrantedAuthority (which is Serializable)
        this.authorities = oauth2User.getAuthorities().stream()
                .map(auth -> new SimpleGrantedAuthority(auth.getAuthority()))
                .collect(Collectors.toCollection(ArrayList::new));

        // Handle OIDC-specific data if the user is an OidcUser
        if (oauth2User instanceof OidcUser) {
            OidcUser oidcUser = (OidcUser) oauth2User;
            this.idToken = oidcUser.getIdToken();
            this.userInfo = oidcUser.getUserInfo();
            this.claims = new HashMap<>(oidcUser.getClaims());
        } else {
            this.idToken = null;
            this.userInfo = null;
            this.claims = this.attributes;  // Use attributes as claims for non-OIDC
        }
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getName() {
        return oauthName;
    }

    /**
     * Get the user's database ID.
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * Get the user's public ID (used in URLs).
     */
    public String getPublicId() {
        return publicId;
    }

    /**
     * Get the user's email.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Get the user's display name.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get the user's Google ID.
     */
    public String getGoogleId() {
        return googleId;
    }

    // OidcUser interface methods

    @Override
    public Map<String, Object> getClaims() {
        return claims;
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return userInfo;
    }

    @Override
    public OidcIdToken getIdToken() {
        return idToken;
    }
}
