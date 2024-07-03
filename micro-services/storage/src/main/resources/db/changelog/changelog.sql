--liquibase formatted sql

--changeset nelmin:2024-07-03-19-00
CREATE TABLE storage
(
    id           BIGINT NOT NULL,
    uuid         VARCHAR(255),
    user_id      BIGINT,
    type VARCHAR(255),
    content_type VARCHAR(255),
    file         BYTEA,
    created_date TIMESTAMP WITHOUT TIME ZONE,
    updated_date TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT storage_pkey PRIMARY KEY (id)
);
