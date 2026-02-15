-- Add ON DELETE CASCADE to all user-related foreign keys
-- so that DELETE FROM users WHERE id = ? cascades to all dependent data.

-- sessions: add missing FK with CASCADE
ALTER TABLE `sessions`
    ADD CONSTRAINT `sessions_user_id_foreign` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

-- user_settings: add missing FK with CASCADE
ALTER TABLE `user_settings`
    ADD CONSTRAINT `user_settings_user_id_foreign` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

-- models: drop existing FK, re-add with CASCADE
ALTER TABLE `models`
    DROP FOREIGN KEY `models_user_id_foreign`;
ALTER TABLE `models`
    ADD CONSTRAINT `models_user_id_foreign` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

-- model_files: drop existing FKs, re-add with CASCADE
ALTER TABLE `model_files`
    DROP FOREIGN KEY `model_files_user_id_foreign`,
    DROP FOREIGN KEY `model_files_model_id_foreign`;
ALTER TABLE `model_files`
    ADD CONSTRAINT `model_files_user_id_foreign` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    ADD CONSTRAINT `model_files_model_id_foreign` FOREIGN KEY (`model_id`) REFERENCES `models` (`id`) ON DELETE CASCADE;

-- model_tags: drop existing FKs, re-add with CASCADE
ALTER TABLE `model_tags`
    DROP FOREIGN KEY `model_tags_user_id_foreign`,
    DROP FOREIGN KEY `model_tags_model_id_foreign`;
ALTER TABLE `model_tags`
    ADD CONSTRAINT `model_tags_user_id_foreign` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    ADD CONSTRAINT `model_tags_model_id_foreign` FOREIGN KEY (`model_id`) REFERENCES `models` (`id`) ON DELETE CASCADE;

-- model_links: drop existing FKs, re-add with CASCADE
ALTER TABLE `model_links`
    DROP FOREIGN KEY `model_links_user_id_foreign`,
    DROP FOREIGN KEY `model_links_model_id_foreign`;
ALTER TABLE `model_links`
    ADD CONSTRAINT `model_links_user_id_foreign` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    ADD CONSTRAINT `model_links_model_id_foreign` FOREIGN KEY (`model_id`) REFERENCES `models` (`id`) ON DELETE CASCADE;

-- collections: drop existing FKs, re-add user_id with CASCADE and main_model with SET NULL
ALTER TABLE `collections`
    DROP FOREIGN KEY `collections_user_id_foreign`,
    DROP FOREIGN KEY `collections_main_model_foreign`;
ALTER TABLE `collections`
    ADD CONSTRAINT `collections_user_id_foreign` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    ADD CONSTRAINT `collections_main_model_foreign` FOREIGN KEY (`main_model`) REFERENCES `models` (`id`) ON DELETE SET NULL;

-- model_collections: drop existing FKs, re-add with CASCADE
ALTER TABLE `model_collections`
    DROP FOREIGN KEY `model_collections_collection_id_foreign`,
    DROP FOREIGN KEY `model_collections_model_id_foreign`;
ALTER TABLE `model_collections`
    ADD CONSTRAINT `model_collections_collection_id_foreign` FOREIGN KEY (`collection_id`) REFERENCES `collections` (`id`) ON DELETE CASCADE,
    ADD CONSTRAINT `model_collections_model_id_foreign` FOREIGN KEY (`model_id`) REFERENCES `models` (`id`) ON DELETE CASCADE;
