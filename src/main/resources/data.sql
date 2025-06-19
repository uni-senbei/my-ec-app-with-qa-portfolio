-- src/main/resources/data.sql

-- products テーブルに初期データを挿入
INSERT INTO products (name, description, price, type) VALUES
('Tシャツ', 'シンプルなデザインの快適なTシャツです。', 2500.00, 'ONE_TIME'),
('コーヒー豆', 'スペシャルティコーヒー豆、200g。', 1500.00, 'ONE_TIME'),
('月額マガジン', '毎月最新の情報を配信するデジタルマガジン。', 980.00, 'SUBSCRIPTION'),
('オンラインフィットネス', '週3回のオンラインパーソナルトレーニング。', 4980.00, 'SUBSCRIPTION');

-- 必要に応じて、さらに商品を追加できます
-- INSERT INTO products (name, description, price, type) VALUES
-- ('エコバッグ', '環境に優しい素材で作られた、折りたたみ可能なエコバッグ。', 1200.00, 'ONE_TIME');