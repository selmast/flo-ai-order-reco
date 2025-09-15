-- V7: add FK only if it's missing (idempotent)

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
