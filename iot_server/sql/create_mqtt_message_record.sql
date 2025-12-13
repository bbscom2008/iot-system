CREATE TABLE `mqtt_message_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `payload` text NOT NULL,
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
