package com.example.my_test_app.service;

import com.example.my_test_app.model.User;
import com.example.my_test_app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Collections;
import java.util.Date;     // ★追加済みかもしれませんが、念のため確認
import java.util.Calendar; // ★追加

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ★ログイン試行回数制限の定数を定義
    private static final int MAX_FAILED_ATTEMPTS = 5; // アカウントロックされるまでの最大失敗回数
    private static final long LOCK_TIME_DURATION_MINUTES = 5; // アカウントロック期間（分）

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
        // 新規ユーザー作成時は、ログイン試行回数とロック状態を初期値に設定 (Userモデルのデフォルト値で対応)
        // user.setFailedLoginAttempts(0);
        // user.setAccountLocked(false);
        // user.setLockTime(null);

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
    public Optional<User> authenticateUser(String username, String plainPassword) {
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // アカウントロック状態のチェックと自動解除
            if (user.isAccountLocked()) {
                if (user.getLockTime() != null) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(user.getLockTime());
                    calendar.add(Calendar.MINUTE, (int) LOCK_TIME_DURATION_MINUTES); // ロック期間を加算

                    if (calendar.getTime().before(new Date())) {
                        // ロック期間が経過した場合、ロックを解除し、失敗回数をリセット
                        user.setAccountLocked(false);
                        user.setLockTime(null);
                        user.setFailedLoginAttempts(0);
                        userRepository.save(user); // ロック解除状態を保存
                        // ロックが解除されたので、次のパスワードチェックに進む
                    } else {
                        // ★★★ 修正点: ロック期間中の場合は、ここで即座に認証失敗を返す ★★★
                        // このreturnを追加することで、ロック中のアカウントはパスワードが正しくてもログインできないようになる
                        return Optional.empty(); // アカウントロック中のため認証失敗
                    }
                }
            }

            // アカウントがロックされていなかった、またはロックが解除された場合にのみパスワードをチェック
            if (passwordEncoder.matches(plainPassword, user.getPassword())) {
                // 認証成功
                user.setFailedLoginAttempts(0); // 失敗回数をリセット
                user.setAccountLocked(false);   // ロック状態を解除
                user.setLockTime(null);         // ロック時間をリセット
                userRepository.save(user);      // 更新をDBに保存
                return Optional.of(user);
            } else {
                // 認証失敗
                user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1); // 失敗回数をインクリメント

                if (user.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS) {
                    user.setAccountLocked(true); // アカウントをロック
                    user.setLockTime(new Date()); // ロック時刻を記録
                }
                userRepository.save(user); // 更新をDBに保存
                return Optional.empty(); // 認証失敗
            }
        }
        return Optional.empty(); // ユーザーが見つからない場合も認証失敗
    }
}