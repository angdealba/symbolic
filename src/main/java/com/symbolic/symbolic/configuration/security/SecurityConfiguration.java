package com.symbolic.symbolic.configuration.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Defines the basic security configuration for enabling API access.
 */
@EnableWebSecurity
@Configuration
public class SecurityConfiguration {
  /**
   * Filters all endpoints under the /api/ root.
   */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
    httpSecurity.csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/**").permitAll()
            .anyRequest().authenticated()
        ).httpBasic(Customizer.withDefaults());
    return httpSecurity.build();
  }
}
