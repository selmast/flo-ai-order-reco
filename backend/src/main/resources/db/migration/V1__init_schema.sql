-- Products
CREATE TABLE IF NOT EXISTS products (
                                        id          BIGSERIAL PRIMARY KEY,
                                        name        TEXT        NOT NULL,
                                        description TEXT,
                                        brand       TEXT,
                                        category    TEXT
);

-- Orders
CREATE TABLE IF NOT EXISTS orders (
                                      id     BIGSERIAL PRIMARY KEY,
                                      status VARCHAR(32) NOT NULL
    );

-- Order items
CREATE TABLE IF NOT EXISTS order_items (
                                           id         BIGSERIAL PRIMARY KEY,
                                           order_id   BIGINT NOT NULL REFERENCES orders(id)   ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
    quantity   INT    NOT NULL
    );

-- Helpful indexes
CREATE INDEX IF NOT EXISTS idx_order_items_order_id   ON order_items(order_id);
CREATE INDEX IF NOT EXISTS idx_order_items_product_id ON order_items(product_id);
