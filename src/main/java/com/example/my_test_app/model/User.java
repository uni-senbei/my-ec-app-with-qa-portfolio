package com.example.my_test_app.model;

import jakarta.persistence.*;
import lombok.Data; // @Getter, @Setter, @EqualsAndHashCode, @ToStringを含む
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date; // ★追加

@Entity
@Table(name = "users") // テーブル名を"user"ではなく"users"にする（SQL予約語との衝突を避けるため）
@Data // @Getter, @Setter, @EqualsAndHashCode, @ToStringを自動生成
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true) // ユーザー名は必須、かつユニーク
    private String username; // 'name' ではなく 'username' に変更

    @Column(nullable = false, unique = true) // メールアドレスも必須、かつユニーク
    private String email;

    @Column(nullable = false)
    private String password; // ★ハッシュ化されたパスワードを格納

    @Column(nullable = false)
    private String role; // 例: "USER", "ADMIN" など。★ユーザーの役割

    // ★ここから追加フィールド
    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts = 0; // デフォルト値は0

    @Column(name = "account_locked", nullable = false)
    private boolean accountLocked = false; // デフォルト値はfalse

    @Column(name = "lock_time") // ロックされた日時。nullable=trueにすることで、ロックされていない場合はNULLを許容
    @Temporal(TemporalType.TIMESTAMP) // 日時型を指定
    private Date lockTime;
    // ★ここまで追加フィールド

    // ユーザーとカートは1対1の関係（このマッピングは、UserエンティティではUser認証に直接は必要ないが、
    // 将来的にユーザー情報取得時に紐づくカート情報を参照したい場合に必要となる。
    // 今回の認証フローには含めず、コメントアウトか、後で追加することも可能だが、
    // ここでUserがCartを持つのではなく、CartがUserを持つ方が一般的な設計。
    // まずは認証機能に集中し、このOneToOneマッピングは後ほど再検討しましょう。）

    // ★以前のCartとのOneToOneマッピングと createCartForUser() メソッドは、
    // ★ユーザー認証には直接不要なので、今回の更新では削除します。
    // ★認証機能が完成した後、必要に応じてCartエンティティ側からUserへの参照を張る形などで再構築します。
}