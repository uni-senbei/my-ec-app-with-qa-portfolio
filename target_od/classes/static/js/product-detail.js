document.addEventListener('DOMContentLoaded', () => {
    const productDetailContentDiv = document.getElementById('product-detail-content');
    const loadingMessage = document.getElementById('loading-message-detail');
    const errorMessage = document.getElementById('error-message-detail');
    const notFoundMessage = document.getElementById('not-found-message');
    const cartCountSpan = document.getElementById('cart-count'); // ヘッダーのカート数

    // 仮のカートアイテム数（UIデモ用）
    let currentCartCount = 0; // index.htmlと別々に管理されますが、これはデモ用です

    // カートの数を更新する関数
    function updateCartCountUI() {
        cartCountSpan.textContent = currentCartCount;
    }


    // URLから商品IDを取得する関数
    function getProductIdFromUrl() {
        const params = new URLSearchParams(window.location.search);
        return params.get('id');
    }

    // 商品詳細データをAPIから取得し表示する関数
    async function fetchProductDetail(productId) {
        if (!productId) {
            errorMessage.style.display = 'block';
            productDetailContentDiv.innerHTML = '';
            productDetailContentDiv.appendChild(notFoundMessage); // NotFoundメッセージを表示
            notFoundMessage.style.display = 'block';
            return;
        }

        try {
            loadingMessage.style.display = 'block'; // ローディングメッセージを表示
            errorMessage.style.display = 'none'; // エラーメッセージを非表示
            notFoundMessage.style.display = 'none'; // NotFoundメッセージを非表示

            const response = await fetch(`/api/products/${productId}`); // 特定の商品詳細API

            loadingMessage.style.display = 'none'; // ローディングメッセージを非表示

            if (response.status === 404) { // 商品が見つからなかった場合
                productDetailContentDiv.innerHTML = '';
                productDetailContentDiv.appendChild(notFoundMessage);
                notFoundMessage.style.display = 'block';
                return;
            }

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const product = await response.json();

            // 取得した商品をHTMLに表示
            productDetailContentDiv.innerHTML = `
                <h3>${product.name}</h3>
                <p><strong>商品説明:</strong> ${product.description || '説明なし'}</p>
                <p class="price"><strong>価格:</strong> ¥${product.price.toLocaleString()}</p>
                <p class="type"><strong>タイプ:</strong> ${product.type}</p>
                <button class="add-to-cart-button" data-product-id="${product.id}">カートに追加</button>
            `;

            // ★ここから追加/修正: カート追加ボタンのイベントリスナー設定★
            const addToCartButton = productDetailContentDiv.querySelector('.add-to-cart-button');
            if (addToCartButton) { // ボタンが存在することを確認
                addToCartButton.addEventListener('click', (event) => {
                    const productId = event.target.getAttribute('data-product-id');
                    // ここにカート追加のダミー処理を記述
                    console.log(`[商品詳細] 商品ID: ${productId} をカートに追加しました (ダミー処理)`);

                    // UI上のカート数をインクリメント（デモ用）
                    currentCartCount++;
                    updateCartCountUI();

                    // 例えば、簡単なアラート表示
                    alert(`商品ID: ${productId} をカートに追加しました！`);
                });
            }
            // ★追加/修正ここまで★


        } catch (error) {
            console.error('商品詳細データの取得中にエラーが発生しました:', error);
            loadingMessage.style.display = 'none'; // ローディングメッセージを非表示
            errorMessage.style.display = 'block'; // エラーメッセージを表示
            productDetailContentDiv.innerHTML = ''; // エラー時はコンテンツをクリア
        }
    }

    // 初期表示時にカート数を更新
    updateCartCountUI();

    // ページロード時に商品IDを取得して詳細をフェッチ
    const productId = getProductIdFromUrl();
    fetchProductDetail(productId);
});