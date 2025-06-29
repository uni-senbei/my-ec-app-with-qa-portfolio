document.addEventListener('DOMContentLoaded', () => {
    const productListDiv = document.getElementById('product-list');
    const loadingMessage = document.getElementById('loading-message');
    const errorMessage = document.getElementById('error-message');
    const cartCountSpan = document.getElementById('cart-count'); // カートのアイテム数を表示するspan要素

    // 仮のカートアイテム数（UIデモ用）
    let currentCartCount = 0;

    // カートの数を更新する関数
    function updateCartCountUI() {
        cartCountSpan.textContent = currentCartCount;
    }


    // 商品データをAPIから取得する関数
    async function fetchProducts() {
        try {
            loadingMessage.style.display = 'block'; // ローディングメッセージを表示
            errorMessage.style.display = 'none'; // エラーメッセージを非表示

            const response = await fetch('/api/products'); // バックエンドAPIのエンドポイント
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            const products = await response.json();

            loadingMessage.style.display = 'none'; // ローディングメッセージを非表示

            if (products.length === 0) {
                productListDiv.innerHTML = '<p>商品が見つかりませんでした。</p>';
                return;
            }

            // 取得した商品をHTMLに表示
            productListDiv.innerHTML = ''; // 既存のコンテンツをクリア
            products.forEach(product => {
                const productCard = document.createElement('div');
                productCard.className = 'product-card';
                // 商品カード全体を商品詳細ページへのリンクとして設定
                // data-product-id 属性で商品IDを保持
                productCard.setAttribute('data-product-id', product.id);
                productCard.style.cursor = 'pointer'; // カーソルをポインターにしてクリック可能であることを示す

                productCard.innerHTML = `
                    <h3>${product.name}</h3>
                    <p>${product.description || '説明なし'}</p>
                    <p class="price">¥${product.price.toLocaleString()}</p>
                    <p class="type">タイプ: ${product.type}</p>
                    <button class="add-to-cart-button" data-product-id="${product.id}">カートに追加</button>
                `;
                productListDiv.appendChild(productCard);

                // 商品カードクリックで詳細ページへ遷移
                productCard.addEventListener('click', (event) => {
                    // カートに追加ボタンがクリックされた場合は詳細ページに遷移しない
                    if (event.target.classList.contains('add-to-cart-button')) {
                        return;
                    }
                    const productId = productCard.getAttribute('data-product-id');
                    window.location.href = `product-detail.html?id=${productId}`;
                });
            });

            // ★ここから追加/修正: カート追加ボタンのイベントリスナー設定★
            // productListDiv内の全ての「カートに追加」ボタンを取得
            const addToCartButtons = productListDiv.querySelectorAll('.add-to-cart-button');
            addToCartButtons.forEach(button => {
                button.addEventListener('click', (event) => {
                    const productId = event.target.getAttribute('data-product-id');
                    // ここにカート追加のダミー処理を記述
                    console.log(`[商品一覧] 商品ID: ${productId} をカートに追加しました (ダミー処理)`);

                    // UI上のカート数をインクリメント（デモ用）
                    currentCartCount++;
                    updateCartCountUI();

                    // 例えば、簡単なアラート表示
                    alert(`商品ID: ${productId} をカートに追加しました！`);
                });
            });
            // ★追加/修正ここまで★

        } catch (error) {
            console.error('商品データの取得中にエラーが発生しました:', error);
            loadingMessage.style.display = 'none'; // ローディングメッセージを非表示
            errorMessage.style.display = 'block'; // エラーメッセージを表示
            productListDiv.innerHTML = ''; // エラー時は商品リストをクリア
        }
    }

    // 初期表示時にカート数を更新
    updateCartCountUI();
    // ページロード時に商品をフェッチ
    fetchProducts();
});