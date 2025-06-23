/*
package com.example.my_test_app.controller;

import com.example.my_test_app.model.User;
import com.example.my_test_app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController // このクラスがRESTful APIコントローラーであることを示す
@RequestMapping("/api/users") // このコントローラーのベースURL
public class UserController {

    @Autowired // UserRepositoryのインスタンスを自動的に注入
    private UserRepository userRepository;

    // 全てのユーザーを取得するAPI: GET /api/users
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll(); // 全てのユーザーを検索して返す
    }

    // 新しいユーザーを作成するAPI: POST /api/users
    @PostMapping
    public User createUser(@RequestBody User user) { // リクエストボディからUserオブジェクトを受け取る
        return userRepository.save(user); // ユーザーをデータベースに保存
    }

    // 特定のユーザーを取得するAPI: GET /api/users/{id}
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) { // パス変数からIDを受け取る
        return userRepository.findById(id) // IDでユーザーを検索
                .map(user -> ResponseEntity.ok(user)) // 見つかればOKステータスとユーザーを返す
                .orElse(ResponseEntity.notFound().build()); // 見つからなければ404を返す
    }

    // ユーザーを更新するAPI: PUT /api/users/{id}
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setName(userDetails.getName());
                    user.setEmail(userDetails.getEmail());
                    User updatedUser = userRepository.save(user);
                    return ResponseEntity.ok(updatedUser);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ユーザーを削除するAPI: DELETE /api/users/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    userRepository.delete(user);
                    return ResponseEntity.ok().build(); // 削除成功なら200 OKを返す
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
*/
