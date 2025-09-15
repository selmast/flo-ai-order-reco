-- V6: make feedback constraints/indexes idempotent

-- Add FK only if it doesn't already exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_feedback_product'
    ) THEN
ALTER TABLE feedback_events
    ADD CONSTRAINT fk_feedback_product
        FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE;
END IF;
END
$$;

-- Ensure helpful indexes exist (safe to re-run)
CREATE INDEX IF NOT EXISTS idx_feedback_product
    ON feedback_events (product_id);

CREATE INDEX IF NOT EXISTS idx_feedback_product_action
    ON feedback_events (product_id, action);

CREATE INDEX IF NOT EXISTS idx_feedback_created_at
    ON feedback_events (created_at);
