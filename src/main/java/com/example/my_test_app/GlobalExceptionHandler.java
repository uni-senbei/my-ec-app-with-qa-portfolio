package com.example.my_test_app; // ※ MyTestAppApplicationと同じパッケージか、それより上位のパッケージに配置

import com.example.my_test_app.exceptions.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import com.example.my_test_app.exceptions.CartLimitExceededException; // ★追加: 複数形の'exceptions'であることを確認


import java.util.Collections;
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

    // 例外ハンドリングメソッドの例 (既存のものをそのまま残すか、必要に応じて追加)

    // リソースが見つからない場合 (404 Not Found)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFoundException(ResourceNotFoundException ex) {
        Map<String, String> errorResponse = Collections.singletonMap("message", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    // 不正な状態の操作 (例: ユーザー登録時の重複) (400 Bad Request または 409 Conflict)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Object> handleIllegalStateException(IllegalStateException ex) {
        Map<String, String> errorResponse = Collections.singletonMap("message", ex.getMessage());
        // ユーザー登録の重複など、特定のケースではCONFLICTも適切
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST); // 一般的にはBAD_REQUEST
    }

    // ★追加: カート上限超過例外のハンドリング
    @ExceptionHandler(CartLimitExceededException.class)
    public ResponseEntity<Object> handleCartLimitExceededException(CartLimitExceededException ex) {
        Map<String, String> errorResponse = Collections.singletonMap("message", ex.getMessage());
        // カートの制約違反なので、400 Bad Requestが適切
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // その他の予期せぬ例外に対する汎用的なハンドリング (500 Internal Server Error)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllUncaughtException(Exception ex) {
        Map<String, String> errorResponse = Collections.singletonMap("message", "An unexpected error occurred: " + ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
