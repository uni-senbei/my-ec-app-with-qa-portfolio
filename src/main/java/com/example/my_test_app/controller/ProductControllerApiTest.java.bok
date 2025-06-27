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

    @Test
    public void testUpdateProduct() {
        // 1. まず更新対象となる商品を作成する (または既存のものを利用)
        Map<String, Object> initialProduct = new HashMap<>();
        initialProduct.put("name", "Original Product");
        initialProduct.put("description", "This is the original description.");
        initialProduct.put("price", 50.00);
        initialProduct.put("type", "SUBSCRIPTION"); // または "ONE_TIME"

        Integer productId = given()
                .contentType(ContentType.JSON)
                .body(initialProduct)
                .when()
                .post("/api/products")
                .then()
                .statusCode(201)
                .extract().path("id"); // 作成された商品のIDを抽出

        assertNotNull(productId);

        // 2. 更新用のデータを作成
        Map<String, Object> updatedProductData = new HashMap<>();
        updatedProductData.put("name", "Updated Product Name");
        updatedProductData.put("description", "New and improved description.");
        updatedProductData.put("price", 75.25);
        updatedProductData.put("type", "ONE_TIME"); // タイプも更新してみる

        // 3. PUTリクエストを送信し、ステータスコードとレスポンスボディを検証
        given()
                .contentType(ContentType.JSON)
                .body(updatedProductData)
                .when()
                .put("/api/products/" + productId) // 作成した商品のIDを指定してPUTリクエスト
                .then()
                .statusCode(200) // 更新成功は通常200 OK
                .contentType(ContentType.JSON)
                .body("id", equalTo(productId)) // IDが変わっていないことを確認
                .body("name", equalTo("Updated Product Name")) // 名前が更新されたことを確認
                .body("description", equalTo("New and improved description."))
                .body("price", equalTo(75.25f)) // floatとして比較
                .body("type", equalTo("ONE_TIME"));

        // 4. 更新された商品をGETリクエストで取得し、正しく更新されているか最終確認
        given()
                .when()
                .get("/api/products/" + productId)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", equalTo(productId))
                .body("name", equalTo("Updated Product Name"))
                .body("description", equalTo("New and improved description."))
                .body("price", equalTo(75.25f))
                .body("type", equalTo("ONE_TIME"));
    }

    @Test
    public void testDeleteProduct() {
        // 1. まず削除対象となる商品を作成する
        Map<String, Object> productToDelete = new HashMap<>();
        productToDelete.put("name", "Product to Delete");
        productToDelete.put("description", "This product will be deleted.");
        productToDelete.put("price", 99.99);
        productToDelete.put("type", "ONE_TIME"); // または "SUBSCRIPTION"

        Integer productId = given()
                .contentType(ContentType.JSON)
                .body(productToDelete)
                .when()
                .post("/api/products")
                .then()
                .statusCode(201)
                .extract().path("id"); // 作成された商品のIDを抽出

        assertNotNull(productId);

        // 2. 作成した商品がGETできることを確認
        given()
                .when()
                .get("/api/products/" + productId)
                .then()
                .statusCode(200); // 存在するはずなので200 OK

        // 3. DELETEリクエストを送信
        given()
                .when()
                .delete("/api/products/" + productId) // 作成した商品のIDを指定してDELETEリクエスト
                .then()
                .statusCode(204); // 削除成功は204 No Content

        // 4. 削除された商品がGETできないことを確認 (404 Not Foundを期待)
        given()
                .when()
                .get("/api/products/" + productId)
                .then()
                .statusCode(404); // 存在しないはずなので404 Not Found
    }

    @Test
    public void testDeleteNonExistingProduct() {
        // 存在しないIDに対してDELETEリクエストを送信した場合のテスト
        Long nonExistingId = 99999L; // 確実に存在しないであろう大きなID
        given()
                .when()
                .delete("/api/products/" + nonExistingId)
                .then()
                .statusCode(404); // 存在しないIDに対する削除は404 Not Found
    }
}
