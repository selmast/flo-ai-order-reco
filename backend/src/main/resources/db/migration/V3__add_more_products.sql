-- Add extra catalog items if they don't exist
INSERT INTO products (name, description, brand, category)
SELECT 'Water Bottle','Insulated bottle','Stanley','Accessories'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name='Water Bottle');

INSERT INTO products (name, description, brand, category)
SELECT 'Socks','Breathable running socks','Nike','Accessories'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name='Socks');
