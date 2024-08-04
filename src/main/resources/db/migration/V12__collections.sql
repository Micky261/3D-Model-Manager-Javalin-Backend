CREATE TABLE `collections`
(
    `id`          bigint(20) UNSIGNED NOT NULL,
    `user_id`     bigint(20) UNSIGNED NOT NULL,
    `name`        varchar(255)        NOT NULL,
    `description` text                         DEFAULT '',
    `main_model`  bigint(20) UNSIGNED          DEFAULT NULL,
    `created_at`  timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE `collections`
    ADD PRIMARY KEY (`id`),
    ADD KEY `collections_user_id_foreign` (`user_id`);

ALTER TABLE `collections`
    MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT,
    ADD CONSTRAINT `collections_user_id_foreign` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
    ADD CONSTRAINT `collections_main_model_foreign` FOREIGN KEY (`main_model`) REFERENCES `models` (`id`);


CREATE TABLE `model_collections`
(
    `collection_id` bigint(20) UNSIGNED NOT NULL,
    `model_id`      bigint(20) UNSIGNED NOT NULL
);

ALTER TABLE `model_collections`
    ADD PRIMARY KEY (`collection_id`, `model_id`),
    ADD KEY `model_collections_collection_id_foreign` (`collection_id`),
    ADD KEY `model_collections_model_id_foreign` (`model_id`);

ALTER TABLE `model_collections`
    ADD CONSTRAINT `model_collections_collection_id_foreign` FOREIGN KEY (`collection_id`) REFERENCES `collections` (`id`),
    ADD CONSTRAINT `model_collections_model_id_foreign` FOREIGN KEY (`model_id`) REFERENCES `models` (`id`);
COMMIT;
