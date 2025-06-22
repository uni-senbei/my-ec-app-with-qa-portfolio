package com.example.my_test_app.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*; // <-- これが既にありますが、念のため確認
import static org.junit.jupiter.api.Assertions.assertNotNull; // <-- この行を新しく追加してください

// Spring Bootアプリケーションを起動し、Web環境をエミュレートする
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProductControllerApiTest {

    @LocalServerPort // ランダムに割り当てられたポート番号を取得
    private int port;

    @BeforeEach // 各テストメソッド実行前に毎回実行される
    public void setUp() {
        RestAssured.port = port; // REST Assuredのベースポートを設定
        RestAssured.baseURI = "http://localhost"; // REST AssuredのベースURIを設定
    }

    @Test // これがテストメソッドであることを示すアノテーション
    public void testGetAllProducts() {
        given() // リクエストの準備
                .when() // リクエストの実行
                .get("/api/products") // GETリクエストを送信するパス
                .then() // レスポンスの検証
                .statusCode(200) // HTTPステータスコードが200 OKであることを確認
                .contentType(ContentType.JSON) // レスポンスのContent-TypeがJSONであることを確認
                .body("$", hasSize(greaterThanOrEqualTo(0))); // レスポンスボディがJSON配列であり、要素数が0以上であることを確認
        // $はJSON全体のルートを指し、hasSizeとgreaterThanOrEqualToはHamcrestのマッチャー
    }

    // --- ここから新しく追加するテストメソッド ---

    @Test
    public void testCreateProduct() {
        // 新しい商品のデータを作成
        Map<String, Object> newProduct = new HashMap<>();
        newProduct.put("name", "Test Product A");
        newProduct.put("description", "Description for Test Product A");
        newProduct.put("price", 100.50);
        newProduct.put("type", "ONE_TIME"); // Enumの文字列値

        // POSTリクエストを送信し、ステータスコードとレスポンスボディを検証
        Integer productId = given()
                .contentType(ContentType.JSON) // リクエストのContent-TypeをJSONに設定
                .body(newProduct) // リクエストボディに商品データを設定
                .when()
                .post("/api/products") // POSTリクエストを送信するパス
                .then()
                .statusCode(201) // HTTPステータスコードが201 Createdであることを確認
                .contentType(ContentType.JSON) // レスポンスのContent-TypeがJSONであることを確認
                .body("id", notNullValue()) // レスポンスボディにidが存在することを確認
                .body("name", equalTo("Test Product A")) // nameが正しいことを確認
                .body("description", equalTo("Description for Test Product A")) // descriptionが正しいことを確認
                .body("price", equalTo(100.50f)) // priceが正しいことを確認 (floatとして比較)
                .body("type", equalTo("ONE_TIME")) // typeが正しいことを確認
                .extract().path("id"); // 作成された商品のIDを抽出

        // IDがnullでないことを別途アサート（より明示的に）
        assertNotNull(productId);

        // 作成された商品をGETリクエストで取得し、正しく作成されているか確認
        given()
                .when()
                .get("/api/products/" + productId) // 作成した商品のIDでGETリクエスト
                .then()
                .statusCode(200) // HTTPステータスコードが200 OKであることを確認
                .contentType(ContentType.JSON)
                .body("id", equalTo(productId)) // 取得した商品のIDが正しいことを確認
                .body("name", equalTo("Test Product A")); // その他、いくつか主要なフィールドを確認
    }
}