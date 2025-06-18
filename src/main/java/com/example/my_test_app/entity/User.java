package com.example.mytestapp.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity // このクラスがデータベースのテーブルに対応することを示す
public class User {

    @Id // 主キー
    @GeneratedValue(strategy = GenerationType.IDENTITY) // IDが自動生成されることを示す
    private Long id;
    private String name;
    private String email;

    // コンストラクタ (JPAでは引数なしのコンストラクタが必要)
    public User() {
    }

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    // GetterとSetter (Lombokを使わない場合は手動で記述)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}