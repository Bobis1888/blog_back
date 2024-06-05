--liquibase formatted sql
--changeset nelmin:2024-06-30-23-00

CREATE TABLE if not exists public.article
(
    id             bigserial    NOT NULL,
    user_id        bigserial    NOT NULL references "user",
    title          varchar(255) NOT NULL,
    pre_view       varchar(255) NULL,
    content        text         NULL,
    created_date   timestamp    NOT NULL,
    updated_date   timestamp    NULL,
    published_date timestamp    NULL,
    status         varchar(255) NULL,

    CONSTRAINT article_pkey primary key (id)
);

--changeset nelmin:2024-07-03-23-00
CREATE SEQUENCE IF NOT EXISTS hibernate_sequence START WITH 1;

--changeset nelmin:2024-07-05-23-00
ALTER TABLE article ADD COLUMN IF NOT EXISTS tags text;
