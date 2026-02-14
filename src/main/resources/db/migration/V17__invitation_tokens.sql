CREATE TABLE `invitation_tokens`
(
    `id`         bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT,
    `token`      varchar(64)         NOT NULL,
    `created_by` bigint(20) UNSIGNED NOT NULL,
    `used_by`    bigint(20) UNSIGNED          DEFAULT NULL,
    `expires_at` timestamp           NULL     DEFAULT NULL,
    `used_at`    timestamp           NULL     DEFAULT NULL,
    `created_at` timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (`id`),
    UNIQUE KEY `invitation_tokens_token_unique` (`token`),

    CONSTRAINT `invitation_tokens_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `invitation_tokens_used_by_fk` FOREIGN KEY (`used_by`) REFERENCES `users` (`id`) ON DELETE SET NULL
);
