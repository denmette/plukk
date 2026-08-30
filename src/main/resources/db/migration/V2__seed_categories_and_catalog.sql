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
