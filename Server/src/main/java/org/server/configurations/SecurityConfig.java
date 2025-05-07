package org.server.configurations;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.server.jwt.JwtService;
import org.server.jwt.JwtTokenFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfig {
  private final JwtService jwtService;
  private final UserDetailsService userDetailsService;

  @Bean
  public JwtTokenFilter jwtTokenFilter() {
    return new JwtTokenFilter(jwtService, userDetailsService);
  }

//  @Bean
//  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//    return http
//            .csrf(AbstractHttpConfigurer::disable)
//            .httpBasic(b -> b.realmName("meowRealm"))
//            .authorizeHttpRequests(a->a
//                    .anyRequest().authenticated())
//            .addFilterBefore(jwtTokenFilter(), BasicAuthenticationFilter.class)
//            .build();
//  }

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(withDefaults())
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/login", "/registration", "/meow").permitAll()
                    .anyRequest().authenticated())
            .addFilterBefore(jwtTokenFilter(), UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(exceptions -> exceptions
                    .authenticationEntryPoint((request, response, authException) -> {
                      response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    }))
            .build();
  }

//  @Bean
//  PasswordEncoder passwordEncoder() {
//    return new BCryptPasswordEncoder(8);
//  }
}
