package com.example.my_test_app.config;

import com.example.my_test_app.service.UserService; // これはまだ必要かもしれません、UserDetailsServiceを実装していることを示すため
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService; // ★ 追加: UserDetailsServiceをインポート
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // ★ このフィールドとコンストラクタは削除します。
    // private final UserService userService;
    // public SecurityConfig(UserService userService) {
    //     this.userService = userService;
    // }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ★ UserDetailsServiceを引数として受け取るように変更
    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserDetailsService userDetailsService) { // UserServiceはUserDetailsServiceを実装しているので、ここでSpringがUserServiceのBeanを注入します
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService); // ここでuserServiceの代わりにuserDetailsServiceを使用
        authProvider.setPasswordEncoder(passwordEncoder()); // passwordEncoder()はそのまま呼び出しでOK
        return authProvider;
    }

    // AuthenticationManagerは変更なし
    @Bean
    public AuthenticationManager authenticationManager(DaoAuthenticationProvider daoAuthenticationProvider) {
        return new ProviderManager(Collections.singletonList(daoAuthenticationProvider));
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF保護を無効化（ステートレスAPIのため）
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/users/register", "/api/users/login").permitAll() // ユーザー登録・ログインは認証不要
                        .requestMatchers("/api/products").authenticated() // 全ての /api/products に認証が必要
                        .requestMatchers("/api/products/{id}").authenticated() // 全ての /api/products/{id} に認証が必要
                        .requestMatchers("/api/cart/**").authenticated() // カート関連APIは認証が必要
                        .anyRequest().authenticated() // その他の全てのリクエストは認証が必要
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // セッションを使わない
                .httpBasic(httpBasic -> {}); // Basic認証を有効にする

        // ここでauthenticationProvider()を直接呼び出す必要はありません。
        // Spring SecurityはauthenticationManager Beanが定義されていればそれを自動的に使用します。
        // http.authenticationProvider(authenticationProvider()); // この行は削除しても問題ありません
        return http.build();
    }
}