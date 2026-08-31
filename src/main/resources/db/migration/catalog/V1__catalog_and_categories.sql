CREATE TABLE category (
    id BIGSERIAL PRIMARY KEY,
    household_id BIGINT NOT NULL,
    stable_key VARCHAR(80) NOT NULL,
    display_name VARCHAR(120) NOT NULL,
    display_order INTEGER NOT NULL,
    CONSTRAINT category_key_unique UNIQUE (household_id, stable_key)
);

CREATE TABLE catalog_product (
    id BIGSERIAL PRIMARY KEY,
    household_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL REFERENCES category (id),
    name VARCHAR(160) NOT NULL,
    normalized_name VARCHAR(160) NOT NULL,
    visual_reference VARCHAR(255),
    origin VARCHAR(20) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT catalog_product_household_name_unique UNIQUE (household_id, normalized_name)
);

INSERT INTO category (household_id, stable_key, display_name, display_order)
VALUES
    (1, 'produce', 'Produce', 10),
    (1, 'dairy', 'Dairy', 20),
    (1, 'meat', 'Meat', 30),
    (1, 'bakery', 'Bakery', 40),
    (1, 'drinks', 'Drinks', 50),
    (1, 'pantry', 'Pantry', 60);

INSERT INTO catalog_product (household_id, category_id, name, normalized_name, visual_reference, origin)
SELECT 1, id, display_name, lower(display_name), lower(stable_key), 'STARTER'
FROM category
WHERE stable_key IN ('produce', 'dairy', 'meat', 'bakery', 'drinks', 'pantry');

INSERT INTO catalog_product (household_id, category_id, name, normalized_name, visual_reference, origin)
SELECT 1, id, v.name, lower(v.name), v.visual_reference, 'STARTER'
FROM category c
JOIN (
    VALUES
        ('meat', 'Kip', 'kip'),
        ('dairy', 'Melk', 'melk'),
        ('produce', 'Appels', 'appels'),
        ('drinks', 'Water', 'water'),
        ('drinks', 'Cola', 'cola'),
        ('pantry', 'Kaas', 'kaas')
) AS v(stable_key, name, visual_reference)
    ON c.stable_key = v.stable_key;
