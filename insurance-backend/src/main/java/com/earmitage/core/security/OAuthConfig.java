package com.earmitage.core.security;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

@Configuration
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
@EnableConfigurationProperties(SecurityConfig.class)
public class OAuthConfig {

    @Value("${jwt.public.key}")
    RSAPublicKey publicKey;

    @Value("${jwt.private.key}")
    RSAPrivateKey privateKey;

    @Value("${app.url}")
    private String appUrl;

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // @formatter:off
		http.   headers(headers -> {
            // Setting a custom Content-Security-Policy (CSP)
            headers.addHeaderWriter((request, response) -> 
                response.setHeader("Content-Security-Policy", 
                    "default-src 'self'; script-src 'self'; style-src 'self'; img-src 'self'; " +
                    "object-src 'none'; connect-src 'self'; frame-ancestors 'none';"));

            // Setting HTTP Strict Transport Security (HSTS)
            headers.addHeaderWriter((request, response) -> 
                response.setHeader("Strict-Transport-Security", 
                    "max-age=31536000; includeSubDomains; preload"));

            // Prevent MIME-sniffing
            headers.addHeaderWriter((request, response) -> 
                response.setHeader("X-Content-Type-Options", "nosniff"));

            // Deny framing of the app (Clickjacking protection)
            headers.addHeaderWriter((request, response) -> 
                response.setHeader("X-Frame-Options", "DENY"));

            // Enable XSS protection in the browser
            headers.addHeaderWriter((request, response) -> 
                response.setHeader("X-XSS-Protection", "1; mode=block"));

            // Referrer policy (no referrer sent on downgrade)
            headers.addHeaderWriter((request, response) -> 
                response.setHeader("Referrer-Policy", "no-referrer-when-downgrade"));

            // Cache control headers
            headers.addHeaderWriter((request, response) -> 
                response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, proxy-revalidate"));

            // Permissions policy (Feature policy) to restrict browser features
            headers.addHeaderWriter((request, response) -> 
                response.setHeader("Permissions-Policy", "geolocation=(self), microphone=()"));
        })
		.cors(cors -> cors.configurationSource(
				request -> setCors(new CorsConfiguration().setAllowedOriginPatterns(Collections.singletonList("*")))))
				
		.authorizeHttpRequests(requests -> requests
				.requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/h2-console/**", "/swagger-ui.html").permitAll()
				.requestMatchers(appUrl+"/unsecured/**").permitAll()
				.requestMatchers("/actuator/health/**").permitAll()
				.requestMatchers(new AntPathRequestMatcher("/h2-console/**")).permitAll()
				.requestMatchers(appUrl+"/admin/**").hasRole("ADMIN")
				.anyRequest().authenticated())
		.csrf((csrf) ->csrf.disable())
				.csrf((csrf) -> csrf.ignoringRequestMatchers(appUrl+"/unsecured/**"))
				//.httpBasic(Customizer.withDefaults())
				.oauth2ResourceServer((oauth2) -> oauth2
					    .jwt(Customizer.withDefaults())
					)
				.sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling((exceptions) -> exceptions
						.authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
						.accessDeniedHandler(new BearerTokenAccessDeniedHandler())
				);
		// @formatter:on
        return http.build();
    }

    @Bean
    JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withPublicKey(publicKey).build();
    }

    @Bean
    JwtEncoder jwtEncoder() throws Exception {
        JWK jwk = new RSAKey.Builder(publicKey).privateKey(privateKey).build();
        JWKSource<SecurityContext> jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
        return new NimbusJwtEncoder(jwks);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    private CorsConfiguration setCors(CorsConfiguration config) {
        config.setAllowedMethods(Collections.unmodifiableList(Arrays.asList(HttpMethod.GET.name(),
                HttpMethod.HEAD.name(), HttpMethod.POST.name(), HttpMethod.DELETE.name(), HttpMethod.PUT.name())));
        config.setAllowedHeaders(Collections.singletonList("*"));
        config.setMaxAge(1800L);

        return config;
    }

}
