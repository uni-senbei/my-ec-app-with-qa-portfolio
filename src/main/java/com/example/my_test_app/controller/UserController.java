package com.example.my_test_app.controller;

import com.example.my_test_app.model.User;
import com.example.my_test_app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * ユーザー登録API
     * POST /api/users/register
     * リクエストボディ: { "username": "testuser", "email": "test@example.com", "password": "password123" }
     *
     * @param requestMap ユーザー名、メールアドレス、パスワードを含むマップ
     * @return 登録されたユーザー情報またはエラーレスポンス
     */
    @PostMapping("/register")
    public ResponseEntity<Object> registerUser(@RequestBody Map<String, String> requestMap) {
        String username = requestMap.get("username");
        String email = requestMap.get("email");
        String password = requestMap.get("password");

        if (username == null || email == null || password == null) {
            return new ResponseEntity<>(Collections.singletonMap("message", "Required fields (username, email, password) are missing."), HttpStatus.BAD_REQUEST);
        }

        try {
            User registeredUser = userService.registerNewUser(username, email, password);
            // パスワードはセキュリティのためレスポンスに含めない
            Map<String, Object> response = Map.of(
                    "id", registeredUser.getId(),
                    "username", registeredUser.getUsername(),
                    "email", registeredUser.getEmail(),
                    "role", registeredUser.getRole()
            );
            return new ResponseEntity<>(response, HttpStatus.CREATED); // 201 Created
        } catch (IllegalArgumentException | IllegalStateException e) {
            // UserServiceで発生したビジネスロジックのエラーを捕捉
            return new ResponseEntity<>(Collections.singletonMap("message", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            // その他の予期せぬエラー
            return new ResponseEntity<>(Collections.singletonMap("message", "An unexpected error occurred during registration."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * ユーザーログインAPI
     * POST /api/users/login
     * リクエストボディ: { "username": "testuser", "password": "password123" }
     *
     * @param requestMap ユーザー名、パスワードを含むマップ
     * @return 認証されたユーザー情報（トークンなど）またはエラーレスポンス
     */
    @PostMapping("/login")
    public ResponseEntity<Object> loginUser(@RequestBody Map<String, String> requestMap) {
        String username = requestMap.get("username");
        String password = requestMap.get("password");

        if (username == null || password == null) {
            return new ResponseEntity<>(Collections.singletonMap("message", "Required fields (username, password) are missing."), HttpStatus.BAD_REQUEST);
        }

        try {
            Optional<User> authenticatedUser = userService.authenticateUser(username, password);

            if (authenticatedUser.isPresent()) {
                // 認証成功
                User user = authenticatedUser.get();
                Map<String, Object> response = Map.of(
                        "message", "Login successful!",
                        "id", user.getId(),
                        "username", user.getUsername(),
                        "email", user.getEmail(),
                        "role", user.getRole()
                );
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                // 認証失敗
                return new ResponseEntity<>(Collections.singletonMap("message", "Invalid username or password."), HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            // その他の予期せぬエラー
            return new ResponseEntity<>(Collections.singletonMap("message", "An unexpected error occurred during login."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * パスワードをリセットするAPI (トークンと新しいパスワードを受け取る)
     * PUT /api/users/reset-password
     * リクエストボディ: { "token": "xxxx-xxxx-xxxx", "newPassword": "NewPassword123!" }
     *
     * @param requestMap トークンと新しいパスワードを含むマップ
     * @return 処理結果メッセージ
     */
    @PutMapping("/reset-password")
    public ResponseEntity<Object> resetPassword(@RequestBody Map<String, String> requestMap) {
        String token = requestMap.get("token");
        String newPassword = requestMap.get("newPassword");

        if (token == null || token.isEmpty() || newPassword == null || newPassword.isEmpty()) {
            return new ResponseEntity<>(Collections.singletonMap("message", "Token or new password is missing."), HttpStatus.BAD_REQUEST);
        }

        // パスワードの要件チェック（Controller側でも簡易的に行う）
        if (newPassword.length() < 8) {
            return new ResponseEntity<>(Collections.singletonMap("message", "New password must be at least 8 characters long."), HttpStatus.BAD_REQUEST);
        }

        boolean resetSuccess = userService.resetPassword(token, newPassword);

        if (resetSuccess) {
            return new ResponseEntity<>(Collections.singletonMap("message", "Password has been reset successfully."), HttpStatus.OK);
        } else {
            // resetPasswordメソッドの内部で、トークンが見つからない、期限切れ、ユーザーが見つからないなどの
            // 詳細な理由がコンソールに出力されるため、ここでは一般的な失敗メッセージを返す
            return new ResponseEntity<>(Collections.singletonMap("message", "Password reset failed. Invalid or expired token, or invalid password."), HttpStatus.BAD_REQUEST); // 400 Bad Request
        }
    }
}