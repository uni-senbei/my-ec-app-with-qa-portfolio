package com.example.my_test_app; // ※ MyTestAppApplicationと同じパッケージか、それより上位のパッケージに配置

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice // これにより、このクラスがアプリケーション全体で例外を処理するようになる
public class GlobalExceptionHandler {

    // MethodArgumentNotValidException (バリデーションエラー) をハンドルする
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage()); // フィールド名とエラーメッセージを取得
        });
        // HTTPステータス 400 Bad Request と、エラー詳細のJSONを返す
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    // 必要に応じて、他の種類の例外ハンドラーもここに追加できる
    // 例: RuntimeExceptionなど
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntimeException(RuntimeException ex, WebRequest request) {
        Map<String, String> errorDetails = new HashMap<>();
        errorDetails.put("error", "サーバー内部エラー");
        errorDetails.put("message", ex.getMessage());
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR); // 500 Internal Server Error
    }
}