// src/test/java/com/example/my_test_app/MyTestAppApplicationTests.java

package com.example.my_test_app;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest // Spring Bootアプリケーションのコンテキストをロードしてテストすることを指定
class MyTestAppApplicationTests { // クラス名がファイル名と一致していることを確認

	@Test // JUnit 5のテストメソッドであることを示すアノテーション
	void contextLoads() {
		// このテストは、Spring Bootアプリケーションのコンテキストが
		// エラーなく正常にロードされることを確認するためのものです。
		// 空のままでも、コンテキストロードのテストとしては機能します。
	}

}