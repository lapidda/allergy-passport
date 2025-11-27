package com.allergypassport.config;

import com.allergypassport.service.CustomOAuth2UserService;
import com.allergypassport.service.CustomOidcUserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for the Allergy Passport application.
 * Configures Google OAuth2 login and public/protected routes.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomOidcUserService customOidcUserService;

    public SecurityConfig(CustomOAuth2UserService customOAuth2UserService,
                          CustomOidcUserService customOidcUserService) {
        this.customOAuth2UserService = customOAuth2UserService;
        this.customOidcUserService = customOidcUserService;
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF configuration - enabled for form submissions
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**") // Disable for API endpoints if needed
            )

            // Explicitly configure security context to use HTTP session
            .securityContext(context -> context
                .securityContextRepository(securityContextRepository())
                .requireExplicitSave(false)  // Auto-save to session
            )

            // Session management - ensure session is created and maintained
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)  // Create session if needed
                .sessionFixation().migrateSession()  // Migrate session on login for security
                .maximumSessions(1)                   // Allow only one session per user
                .maxSessionsPreventsLogin(false)      // New login invalidates old session
            )

            // Authorization rules
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - accessible without authentication
                .requestMatchers("/").permitAll()
                .requestMatchers("/u/**").permitAll()              // Public passport view
                .requestMatchers("/api/public/**").permitAll()      // Public API endpoints
                .requestMatchers("/qr/**").permitAll()              // QR code generation
                .requestMatchers("/profile-picture/**").permitAll() // Profile pictures

                // Static resources
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                .requestMatchers("/favicon.ico").permitAll()

                // Error pages
                .requestMatchers("/error").permitAll()

                // OAuth2 endpoints
                .requestMatchers("/login", "/oauth2/**").permitAll()

                // Everything else requires authentication
                .anyRequest().authenticated()
            )

            // OAuth2 login configuration
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService)
                    .oidcUserService(customOidcUserService)  // Use custom OIDC service
                )
            )

            // Logout configuration
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
            );

        return http.build();
    }
}
