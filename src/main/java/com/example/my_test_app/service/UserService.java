package com.example.my_test_app.service;

import com.example.my_test_app.model.User;
import com.example.my_test_app.repository.UserRepository;
import com.example.my_test_app.repository.PasswordResetTokenRepository;
import com.example.my_test_app.model.PasswordResetToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    // ログイン試行回数制限の定数を定義
    private static final int MAX_FAILED_ATTEMPTS = 5; // アカウントロックされるまでの最大失敗回数
    private static final long LOCK_TIME_DURATION_MINUTES = 5; // アカウントロック期間（分）

    @Autowired
    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            PasswordResetTokenRepository passwordResetTokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }

    @Transactional
    public User registerNewUser(String username, String email, String plainPassword) {
        // ユーザー名またはメールアドレスが既に存在するかチェック
        if (userRepository.existsByUsername(username)) {
            throw new IllegalStateException("Username is already taken.");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalStateException("Email is already registered.");
        }

        // パスワードの要件チェック（簡易版）
        if (plainPassword.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long.");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(plainPassword));
        user.setRole("USER"); // デフォルトで一般ユーザーロールを設定

        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );
    }

    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> resetTokenOptional = passwordResetTokenRepository.findByToken(token);

        if (!resetTokenOptional.isPresent()) {
            System.out.println("Invalid token: Token not found.");
            return false; // トークンが見つからない
        }

        PasswordResetToken resetToken = resetTokenOptional.get();

        if (resetToken.isExpired()) {
            // 期限切れトークンを削除（クリーンアップ）
            passwordResetTokenRepository.delete(resetToken);
            System.out.println("Invalid token: Token expired.");
            return false; // トークンが期限切れ
        }

        User user = resetToken.getUser();
        if (user == null) {
            System.out.println("Invalid token: User not found for token.");
            return false; // トークンに関連付けられたユーザーが見つからない
        }

        // 新しいパスワードの要件チェック（簡易版）
        if (newPassword == null || newPassword.length() < 8) {
            System.out.println("Password reset failed: New password must be at least 8 characters long.");
            return false;
        }

        // パスワードをハッシュ化して更新
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // 使用済みトークンを削除（無効化）
        passwordResetTokenRepository.delete(resetToken);
        System.out.println("Password for user " + user.getUsername() + " has been reset successfully.");
        return true; // パスワード更新成功
    }

    @Transactional
    public Optional<User> authenticateUser(String username, String plainPassword) {
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // アカウントロック状態のチェックと自動解除
            if (user.isAccountLocked()) {
                if (user.getLockTime() != null) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(user.getLockTime());
                    calendar.add(Calendar.MINUTE, (int) LOCK_TIME_DURATION_MINUTES);

                    if (calendar.getTime().before(new Date())) {
                        // ロック期間が経過した場合、ロックを解除し、失敗回数をリセット
                        user.setAccountLocked(false);
                        user.setLockTime(null);
                        user.setFailedLoginAttempts(0);
                        userRepository.save(user);
                    } else {
                        return Optional.empty(); // アカウントロック中のため認証失敗
                    }
                }
            }

            // アカウントがロックされていなかった、またはロックが解除された場合にのみパスワードをチェック
            if (passwordEncoder.matches(plainPassword, user.getPassword())) {
                // 認証成功
                user.setFailedLoginAttempts(0);
                user.setAccountLocked(false);
                user.setLockTime(null);
                userRepository.save(user);
                return Optional.of(user);
            } else {
                // 認証失敗
                user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);

                if (user.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS) {
                    user.setAccountLocked(true);
                    user.setLockTime(new Date());
                }
                userRepository.save(user);
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
}