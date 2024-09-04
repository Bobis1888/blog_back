--liquibase formatted sql
--changeset nelmin:2024-09-03-23-00
CREATE TABLE if not exists public.notification(
    id bigserial NOT NULL,
    user_id bigint NOT NULL REFERENCES "user",
    type varchar(255),
    payload text NOT NULL,
    created_date timestamp without time zone,
    is_read boolean default false,
    CONSTRAINT notification_pkey PRIMARY KEY (id)
)