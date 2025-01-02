CREATE TABLE `user_settings`
(
    `user_id`    bigint(20) UNSIGNED NOT NULL,
    `key`        varchar(255)        NOT NULL,
    `type`       varchar(255)        NOT NULL,
    `value`      varchar(255)        NOT NULL,
    `created_at` timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` timestamp           NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
);

ALTER TABLE `user_settings`
    ADD PRIMARY KEY (`user_id`, `key`);

CREATE INDEX user_settings_by_type ON user_settings (user_id, type);
