package com.example.my_test_app.service;

import com.example.my_test_app.model.User;
import com.example.my_test_app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails; // ★追加
import org.springframework.security.core.userdetails.UserDetailsService; // ★追加
import org.springframework.security.core.userdetails.UsernameNotFoundException; // ★追加
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Collections; // ★追加: ユーザーの権限（ロール）を表すために使用

@Service
// ★UserDetailsServiceインターフェースを実装する
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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
        // ここで正規表現などを使ってより厳密なパスワードポリシーを適用することも可能

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

    // ★UserDetailsServiceインターフェースの必須メソッド
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // データベースからユーザー名でユーザー情報を取得
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // Spring SecurityのUserDetailsオブジェクトを構築して返す
        // org.springframework.security.core.userdetails.Userクラスを使用
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + user.getRole()))
                // ロールは "ROLE_" プレフィックスを付けるのがSpring Securityの慣習
        );
    }

    // ログイン処理のための簡易メソッド（UserControllerで直接呼び出す用。Spring Securityの認証フローとは別）
    public Optional<User> authenticateUser(String username, String plainPassword) {
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (passwordEncoder.matches(plainPassword, user.getPassword())) {
                return Optional.of(user); // 認証成功
            }
        }
        return Optional.empty(); // 認証失敗
    }
}