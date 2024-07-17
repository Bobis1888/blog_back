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

--changeset nelmin:2024-07-17-15-00
CREATE TABLE bookmark
(
    id           BIGINT NOT NULL,
    user_id      BIGINT NOT NULL,
    article_id   BIGINT NOT NULL,
    created_date TIMESTAMP WITHOUT TIME ZONE,
    updated_date TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT bookmark_pkey PRIMARY KEY (id)
);

--changeset nelmin:2024-07-17-15-10
CREATE TABLE "like"
(
    id           BIGINT  NOT NULL,
    user_id      BIGINT  NOT NULL,
    article_id   BIGINT  NOT NULL,
    value        BOOLEAN NOT NULL default false,
    created_date TIMESTAMP WITHOUT TIME ZONE,
    updated_date TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT like_pkey PRIMARY KEY (id)
);

--changeset nelmin:2024-07-29-22-00
CREATE TABLE subscription
(
    id           BIGINT NOT NULL,
    user_id      BIGINT NOT NULL references "user",
    author_id    BIGINT NOT NULL references "user",
    created_date TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT subscription_pkey PRIMARY KEY (id)
);

--changeset nelmin:2024-08-14-02-00
CREATE TABLE private_link
(
    id           BIGINT                      NOT NULL,
    user_id      BIGINT                      NOT NULL,
    article_id   BIGINT                      NOT NULL,
    link         VARCHAR(255)                NOT NULL,
    expired_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_date TIMESTAMP WITHOUT TIME ZONE,
    updated_date TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT private_link_pkey PRIMARY KEY (id)
);

--changeset nelmin:2024-08-15-10-00
ALTER TABLE private_link DROP COLUMN IF EXISTS user_id;

--changeset nelmin:2024-08-17-13-00
ALTER TABLE "like" rename to reaction;
ALTER TABLE reaction ALTER COLUMN value TYPE VARCHAR(255);
