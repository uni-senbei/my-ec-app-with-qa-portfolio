package com.example.my_test_app.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Calendar;

@Entity
@Table(name = "password_reset_tokens")
@Data
@NoArgsConstructor
public class PasswordResetToken {

    // 有効期限のデフォルト設定（例: 24時間）
    private static final int EXPIRATION_TIME_HOURS = 24;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token; // 生成されるリセットトークン

    // Userエンティティとの関連付け
    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id") // 外部キーとしてuser_idを持つ
    private User user;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date expiryDate; // トークンの有効期限

    // コンストラクタ
    public PasswordResetToken(String token, User user) {
        this.token = token;
        this.user = user;
        this.expiryDate = calculateExpiryDate(); // コンストラクタで有効期限を設定
    }

    // 有効期限を計算するヘルパーメソッド
    private Date calculateExpiryDate() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.HOUR, EXPIRATION_TIME_HOURS); // 現在時刻に指定時間を加算
        return new Date(cal.getTime().getTime());
    }

    // トークンが期限切れかどうかをチェックするメソッド
    public boolean isExpired() {
        return expiryDate.before(new Date());
    }
}