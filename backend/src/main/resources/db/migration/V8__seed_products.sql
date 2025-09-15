INSERT INTO products (name, brand, category, description)
VALUES
    ('AirZoom 5', 'Nike', 'shoes', 'Light daily trainer'),
    ('UltraBoost 22', 'Adidas', 'shoes', 'Cushioned road runner'),
    ('Metcon 9', 'Nike', 'training', 'Stable for lifting')
    ON CONFLICT DO NOTHING;