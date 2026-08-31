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

INSERT INTO household (id, display_name) VALUES (1, 'Primary household');
