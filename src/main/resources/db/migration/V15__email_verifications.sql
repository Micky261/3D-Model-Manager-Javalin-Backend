CREATE TABLE `email_verifications`
(
    `id`         bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT,
    `user_id`    bigint(20) UNSIGNED NOT NULL,
    `token`      varchar(64)         NOT NULL,
    `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (`id`),
    UNIQUE KEY `email_verifications_token_unique` (`token`),
    KEY `email_verifications_user_id_foreign` (`user_id`),

    CONSTRAINT `email_verifications_user_id_foreign` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
);
