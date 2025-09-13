-- Seed only if empty
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM products) THEN
    INSERT INTO products (name, description, brand, category)
    VALUES
      ('Running Shoes','Lightweight running shoes','Nike','Shoes'),
      ('Backpack','Durable travel backpack','Adidas','Accessories');
END IF;

  IF NOT EXISTS (SELECT 1 FROM orders) THEN
    INSERT INTO orders (status) VALUES ('CREATED');
    -- Order id just inserted:
INSERT INTO order_items (order_id, product_id, quantity)
SELECT o.id, p.id, 1
FROM orders o, products p
WHERE o.status='CREATED' AND p.name='Running Shoes'
    LIMIT 1;

INSERT INTO order_items (order_id, product_id, quantity)
SELECT o.id, p.id, 2
FROM orders o, products p
WHERE o.status='CREATED' AND p.name='Backpack'
    LIMIT 1;
END IF;
END $$;
