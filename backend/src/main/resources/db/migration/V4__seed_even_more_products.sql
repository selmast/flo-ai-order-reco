
INSERT INTO products (name, description, brand, category)
SELECT 'T-Shirt','Dry-fit tee','Nike','Apparel'
    WHERE NOT EXISTS (SELECT 1 FROM products WHERE name='T-Shirt');

INSERT INTO products (name, description, brand, category)
SELECT 'Cap','Adjustable running cap','Adidas','Accessories'
    WHERE NOT EXISTS (SELECT 1 FROM products WHERE name='Cap');

INSERT INTO products (name, description, brand, category)
SELECT 'Yoga Mat','Non-slip mat','Lululemon','Fitness'
    WHERE NOT EXISTS (SELECT 1 FROM products WHERE name='Yoga Mat');

INSERT INTO products (name, description, brand, category)
SELECT 'Hoodie','Lightweight hoodie','Puma','Apparel'
    WHERE NOT EXISTS (SELECT 1 FROM products WHERE name='Hoodie');

INSERT INTO products (name, description, brand, category)
SELECT 'Waterproof Jacket','Packable shell','The North Face','Apparel'
    WHERE NOT EXISTS (SELECT 1 FROM products WHERE name='Waterproof Jacket');
