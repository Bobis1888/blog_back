--liquibase formatted sql
--changeset nelmin:2024-06-30-23-00

CREATE TABLE if not exists public.article
(
    id           bigserial    NOT NULL,
    user_id      bigserial    NOT NULL,
    title        varchar(255) NOT NULL,
    content      text         NULL,
    created_date timestamp    NULL,
    updated_date timestamp    NULL,

    CONSTRAINT article_pkey primary key (id)
);
