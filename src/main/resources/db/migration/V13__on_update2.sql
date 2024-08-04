ALTER TABLE `models`
    MODIFY `updated_at` timestamp DEFAULT current_timestamp() NOT NULL ON UPDATE current_timestamp();

ALTER TABLE `model_links`
    MODIFY `updated_at` timestamp DEFAULT current_timestamp() NOT NULL ON UPDATE current_timestamp();

ALTER TABLE `collections`
    MODIFY `updated_at` timestamp DEFAULT current_timestamp() NOT NULL ON UPDATE current_timestamp();
