CREATE TABLE household (
    id BIGINT PRIMARY KEY,
    display_name VARCHAR(120) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE household_member (
    id BIGSERIAL PRIMARY KEY,
    household_id BIGINT NOT NULL REFERENCES household (id) ON DELETE CASCADE,
    external_subject VARCHAR(255) NOT NULL,
    display_name VARCHAR(120) NOT NULL,
    role VARCHAR(20) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT household_member_subject_unique UNIQUE (household_id, external_subject)
);

CREATE TABLE category (
    id BIGSERIAL PRIMARY KEY,
    household_id BIGINT NOT NULL REFERENCES household (id) ON DELETE CASCADE,
    stable_key VARCHAR(80) NOT NULL,
    display_name VARCHAR(120) NOT NULL,
    display_order INTEGER NOT NULL,
    CONSTRAINT category_key_unique UNIQUE (household_id, stable_key)
);

CREATE TABLE catalog_product (
    id BIGSERIAL PRIMARY KEY,
    household_id BIGINT NOT NULL REFERENCES household (id) ON DELETE CASCADE,
    category_id BIGINT NOT NULL REFERENCES category (id),
    name VARCHAR(160) NOT NULL,
    normalized_name VARCHAR(160) NOT NULL,
    visual_reference VARCHAR(255),
    origin VARCHAR(20) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE shopping_list (
    id BIGSERIAL PRIMARY KEY,
    household_id BIGINT NOT NULL REFERENCES household (id) ON DELETE CASCADE,
    name VARCHAR(160) NOT NULL,
    latest_confirmed_change_sequence BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE shopping_item (
    id BIGSERIAL PRIMARY KEY,
    shopping_list_id BIGINT NOT NULL REFERENCES shopping_list (id) ON DELETE CASCADE,
    catalog_product_id BIGINT NOT NULL REFERENCES catalog_product (id),
    variant VARCHAR(160),
    normalized_variant VARCHAR(160) NOT NULL DEFAULT '',
    quantity NUMERIC(10, 2),
    unit VARCHAR(40),
    package_size NUMERIC(10, 2),
    package_unit VARCHAR(40),
    package_descriptor VARCHAR(80),
    state VARCHAR(20) NOT NULL,
    latest_confirmed_change_sequence BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX shopping_item_active_identity_unique
    ON shopping_item (
        shopping_list_id,
        catalog_product_id,
        normalized_variant,
        COALESCE(quantity, -1),
        COALESCE(unit, ''),
        COALESCE(package_size, -1),
        COALESCE(package_unit, ''),
        COALESCE(package_descriptor, '')
    )
    WHERE state = 'ACTIVE';

CREATE TABLE shopping_history_entry (
    id BIGSERIAL PRIMARY KEY,
    household_id BIGINT NOT NULL REFERENCES household (id) ON DELETE CASCADE,
    catalog_product_id BIGINT NOT NULL REFERENCES catalog_product (id),
    variant VARCHAR(160),
    quantity NUMERIC(10, 2),
    unit VARCHAR(40),
    package_size NUMERIC(10, 2),
    package_unit VARCHAR(40),
    package_descriptor VARCHAR(80),
    purchased_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO household (id, display_name) VALUES (1, 'Primary household');
