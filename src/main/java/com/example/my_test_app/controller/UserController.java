package com.example.my_test_app.controller;

import com.example.my_test_app.model.User;
import com.example.my_test_app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
            return new ResponseEntity<>(Collections.singletonMap("message", e.getMessage()), HttpStatus.BAD_REQUEST); // 400 Bad Request
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
                // 実際にはJWTトークンなどを生成して返すことが多いが、
                // 今回は認証成功を示すシンプルなメッセージとユーザー情報の一部を返す
                Map<String, Object> response = Map.of(
                        "message", "Login successful!",
                        "id", user.getId(),
                        "username", user.getUsername(),
                        "email", user.getEmail(),
                        "role", user.getRole()
                );
                return new ResponseEntity<>(response, HttpStatus.OK); // 200 OK
            } else {
                // 認証失敗
                return new ResponseEntity<>(Collections.singletonMap("message", "Invalid username or password."), HttpStatus.UNAUTHORIZED); // 401 Unauthorized
            }
        } catch (Exception e) {
            // その他の予期せぬエラー
            return new ResponseEntity<>(Collections.singletonMap("message", "An unexpected error occurred during login."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}