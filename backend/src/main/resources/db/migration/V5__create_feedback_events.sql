-- Create table to store per-interaction feedback
CREATE TABLE IF NOT EXISTS feedback_events (
  id          BIGSERIAL PRIMARY KEY,
  product_id  BIGINT NOT NULL,
  order_id    BIGINT,
  action      VARCHAR(64) NOT NULL, -- viewed | ignored | added_to_cart | purchased
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),

  CONSTRAINT fk_feedback_product
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- Helpful indexes
CREATE INDEX IF NOT EXISTS idx_feedback_product          ON feedback_events(product_id);
CREATE INDEX IF NOT EXISTS idx_feedback_product_action   ON feedback_events(product_id, action);
CREATE INDEX IF NOT EXISTS idx_feedback_created_at       ON feedback_events(created_at);
