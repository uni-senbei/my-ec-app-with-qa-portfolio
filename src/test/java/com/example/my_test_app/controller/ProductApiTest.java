package com.example.my_test_app.controller;

// JUnit関連
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// Spring関連
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

// RestAssured関連
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import static io.restassured.RestAssured.given;

// Hamcrest関連
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Product API テスト")
class ProductApiTest {
    @LocalServerPort
    private int port;

    @BeforeEach
    void setup() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
    }

    @Test
    @DisplayName("全商品を取得できること")
    void shouldGetAllProducts() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/products")
                .then()
                .statusCode(200)
                .body("$", hasSize(greaterThanOrEqualTo(0)))
                .body("[0].id", notNullValue())
                .body("[0].name", notNullValue());
    }

    @TestConfiguration
    @Order(1)
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            return http
                    .securityMatcher("/api/products/**")
                    .authorizeHttpRequests(auth ->
                            auth.anyRequest().permitAll()
                    )
                    .csrf(AbstractHttpConfigurer::disable)
                    .sessionManagement(session ->
                            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    )
                    .build();
        }
    }
    @Test
    @DisplayName("個別の商品を取得できること")
    void shouldGetSingleProduct() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/products/{id}", 1) // 実際のIDに応じて変更
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("name", notNullValue());
    }

    @Test
    @DisplayName("存在しない商品IDの場合は404が返却されること")
    void shouldReturn404WhenProductNotFound() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/products/{id}", 999) // 存在しないID
                .then()
                .statusCode(404);
    }
}