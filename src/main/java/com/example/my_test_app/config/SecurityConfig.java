package com.example.my_test_app.config;

import com.example.my_test_app.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserDetailsService userDetailsService) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(DaoAuthenticationProvider daoAuthenticationProvider) {
        return new ProviderManager(Collections.singletonList(daoAuthenticationProvider));
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF保護を無効化（ステートレスAPIのため）
                .authorizeHttpRequests(authorize -> authorize
                        // ★修正: パスワードリセット要求APIも認証不要にする
                        .requestMatchers("/api/users/register", "/api/users/login", "/api/users/request-password-reset").permitAll() // ユーザー登録・ログイン・パスワードリセット要求は認証不要
                        .requestMatchers("/api/products/**").authenticated() // 全ての /api/products および /api/products/{id} に認証が必要
                        .requestMatchers("/api/cart/**").authenticated() // カート関連APIは認証が必要
                        .anyRequest().authenticated() // その他の全てのリクエストは認証が必要
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // セッションを使わない
                .httpBasic(httpBasic -> {}); // Basic認証を有効にする

        return http.build();
    }
}