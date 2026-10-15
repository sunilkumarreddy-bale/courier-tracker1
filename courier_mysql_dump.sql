-- ============================================================================
--  COURIER DELIVERY MANAGEMENT SYSTEM — MYSQL DATABASE DUMP
--  Generated: Thu Oct 15 20:40:38 IST 2026
--  Target: MySQL 8.0+ / MariaDB / MySQL Workbench / phpMyAdmin
-- ============================================================================

CREATE DATABASE IF NOT EXISTS `courier_jdbc` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `courier_jdbc`;

SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------------------------------------------------------
-- Table structure for table `users`
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(100) NOT NULL,
  `email` VARCHAR(254) NOT NULL UNIQUE,
  `password_hash` VARCHAR(300) NOT NULL,
  `role` VARCHAR(10) NOT NULL,
  `active` BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Data for table `users`
INSERT INTO `users` (`id`, `name`, `email`, `password_hash`, `role`, `active`) VALUES (1, 'Administrator', 'admin@courier.local', '210000:i3PJoiT2tWuzWZIzzQqVpw==:bUB9C11JF6J5XpH9agbF/UD83Maq/H2gh5ARwWksDtI=', 'ADMIN', true);
INSERT INTO `users` (`id`, `name`, `email`, `password_hash`, `role`, `active`) VALUES (2, 'Ravi Kumar', 'ravi.kumar@courier.local', '210000:fBG2vPshP2fWxnMVz3vqAg==:4/zv4gqj/5RmxMq5xHE/OlbIGYaZNkJRa/b6FHXVNN0=', 'AGENT', true);
INSERT INTO `users` (`id`, `name`, `email`, `password_hash`, `role`, `active`) VALUES (3, 'Priya Sharma', 'priya.sharma@courier.local', '210000:qZIvruuWr6D1gnfuvFd+PA==:OFBcA0B3ROR9cGu+JOjkV03YO8RJKyGfn9s2AErPTqU=', 'AGENT', true);
INSERT INTO `users` (`id`, `name`, `email`, `password_hash`, `role`, `active`) VALUES (4, 'Arjun Reddy', 'arjun.reddy@courier.local', '210000:0M0lOl7MPjB7pqU0FzT5Qg==:hFNgp7wkzgLnvm1sf6rx4/x1tpDPj1GeIIyx5Ede50k=', 'AGENT', true);

-- ----------------------------------------------------------------------------
-- Table structure for table `shipments`
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS `shipments`;
CREATE TABLE `shipments` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `tracking` VARCHAR(40) NOT NULL UNIQUE,
  `sender` VARCHAR(100) NOT NULL,
  `sender_contact` VARCHAR(254) NOT NULL,
  `sender_address` VARCHAR(300) NOT NULL,
  `recipient` VARCHAR(100) NOT NULL,
  `recipient_contact` VARCHAR(254) NOT NULL,
  `recipient_address` VARCHAR(300) NOT NULL,
  `weight` DECIMAL(10,2) NOT NULL,
  `service` VARCHAR(20) NOT NULL,
  `expected` DATE NOT NULL,
  `status` VARCHAR(30) NOT NULL,
  `agent_id` BIGINT,
  `version` BIGINT NOT NULL DEFAULT 0,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`agent_id`) REFERENCES `users`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Data for table `shipments`
INSERT INTO `shipments` (`id`, `tracking`, `sender`, `sender_contact`, `sender_address`, `recipient`, `recipient_contact`, `recipient_address`, `weight`, `service`, `expected`, `status`, `agent_id`, `version`, `created_at`) VALUES (1, 'CT-51694e34-5c68-49fa-8a48-1dee34012f12', 'TechNova Labs', 'tech@nova.in', '42 MG Road, Bangalore', 'DataSync Corp', 'data@sync.com', '88 Jubilee Hills, Hyderabad', 2.75, 'EXPRESS', '2026-10-17', 'DELIVERED', 2, 5, '2026-10-15 19:51:10.994803');
INSERT INTO `shipments` (`id`, `tracking`, `sender`, `sender_contact`, `sender_address`, `recipient`, `recipient_contact`, `recipient_address`, `weight`, `service`, `expected`, `status`, `agent_id`, `version`, `created_at`) VALUES (2, 'CT-1fe5eb62-1b44-4d1f-b573-3232023bb0d1', 'FreshMart India', 'orders@freshmart.in', '15 Anna Nagar, Chennai', 'GreenLeaf Stores', 'green@leaf.co', '22 Banjara Hills, Hyderabad', 5.00, 'STANDARD', '2026-10-20', 'IN_TRANSIT', 3, 3, '2026-10-15 19:51:11.060132');
INSERT INTO `shipments` (`id`, `tracking`, `sender`, `sender_contact`, `sender_address`, `recipient`, `recipient_contact`, `recipient_address`, `weight`, `service`, `expected`, `status`, `agent_id`, `version`, `created_at`) VALUES (3, 'CT-10f949cb-030c-412d-99de-c5e3f9f2b1bf', 'CloudByte Systems', 'ship@cloudbyte.io', '101 Whitefield, Bangalore', 'NetPulse Inc', 'recv@netpulse.com', '7 Hitech City, Hyderabad', 1.20, 'EXPRESS', '2026-10-16', 'OUT_FOR_DELIVERY', 4, 4, '2026-10-15 19:51:11.122686');
INSERT INTO `shipments` (`id`, `tracking`, `sender`, `sender_contact`, `sender_address`, `recipient`, `recipient_contact`, `recipient_address`, `weight`, `service`, `expected`, `status`, `agent_id`, `version`, `created_at`) VALUES (4, 'CT-6937081a-c70c-4d7f-bac1-ea9123083a2a', 'BookWorld Online', 'dispatch@bookworld.in', '9 Connaught Place, Delhi', 'ReadMore Library', 'lib@readmore.org', '33 Ameerpet, Hyderabad', 8.50, 'STANDARD', '2026-10-22', 'PICKED_UP', 2, 2, '2026-10-15 19:51:11.192467');
INSERT INTO `shipments` (`id`, `tracking`, `sender`, `sender_contact`, `sender_address`, `recipient`, `recipient_contact`, `recipient_address`, `weight`, `service`, `expected`, `status`, `agent_id`, `version`, `created_at`) VALUES (5, 'CT-06be95d1-6848-483f-b7e0-866806648e1d', 'MediQuick Pharma', 'ship@mediquick.in', '5 Koramangala, Bangalore', 'HealthFirst Clinic', 'clinic@healthfirst.in', '12 Gachibowli, Hyderabad', 0.45, 'EXPRESS', '2026-10-16', 'CANCELLED', 3, 2, '2026-10-15 19:51:11.254501');

-- ----------------------------------------------------------------------------
-- Table structure for table `events`
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS `events`;
CREATE TABLE `events` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `parcel_id` BIGINT NOT NULL,
  `status` VARCHAR(30) NOT NULL,
  `location` VARCHAR(100) NOT NULL,
  `note` VARCHAR(500) NOT NULL,
  `actor_id` BIGINT NOT NULL,
  `occurred_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`parcel_id`) REFERENCES `shipments`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`actor_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Data for table `events`
INSERT INTO `events` (`id`, `parcel_id`, `status`, `location`, `note`, `actor_id`, `occurred_at`) VALUES (1, 1, 'REGISTERED', 'Origin', 'Parcel registered', 1, '2026-10-15 19:51:10.995359');
INSERT INTO `events` (`id`, `parcel_id`, `status`, `location`, `note`, `actor_id`, `occurred_at`) VALUES (2, 2, 'REGISTERED', 'Origin', 'Parcel registered', 1, '2026-10-15 19:51:11.060422');
INSERT INTO `events` (`id`, `parcel_id`, `status`, `location`, `note`, `actor_id`, `occurred_at`) VALUES (3, 3, 'REGISTERED', 'Origin', 'Parcel registered', 1, '2026-10-15 19:51:11.123174');
INSERT INTO `events` (`id`, `parcel_id`, `status`, `location`, `note`, `actor_id`, `occurred_at`) VALUES (4, 4, 'REGISTERED', 'Origin', 'Parcel registered', 1, '2026-10-15 19:51:11.192761');
INSERT INTO `events` (`id`, `parcel_id`, `status`, `location`, `note`, `actor_id`, `occurred_at`) VALUES (5, 5, 'REGISTERED', 'Origin', 'Parcel registered', 1, '2026-10-15 19:51:11.254855');
INSERT INTO `events` (`id`, `parcel_id`, `status`, `location`, `note`, `actor_id`, `occurred_at`) VALUES (6, 1, 'REGISTERED', 'Dispatch', 'Assigned to agent #2', 1, '2026-10-15 19:51:11.696992');
INSERT INTO `events` (`id`, `parcel_id`, `status`, `location`, `note`, `actor_id`, `occurred_at`) VALUES (7, 2, 'REGISTERED', 'Dispatch', 'Assigned to agent #3', 1, '2026-10-15 19:51:11.9186');
INSERT INTO `events` (`id`, `parcel_id`, `status`, `location`, `note`, `actor_id`, `occurred_at`) VALUES (8, 3, 'REGISTERED', 'Dispatch', 'Assigned to agent #4', 1, '2026-10-15 19:51:11.983564');
INSERT INTO `events` (`id`, `parcel_id`, `status`, `location`, `note`, `actor_id`, `occurred_at`) VALUES (9, 4, 'REGISTERED', 'Dispatch', 'Assigned to agent #2', 1, '2026-10-15 19:51:12.205572');
INSERT INTO `events` (`id`, `parcel_id`, `status`, `location`, `note`, `actor_id`, `occurred_at`) VALUES (10, 5, 'REGISTERED', 'Dispatch', 'Assigned to agent #3', 1, '2026-10-15 19:51:12.26763');
INSERT INTO `events` (`id`, `parcel_id`, `status`, `location`, `note`, `actor_id`, `occurred_at`) VALUES (11, 1, 'PICKED_UP', 'Bangalore Hub', 'Collected from TechNova office', 2, '2026-10-15 19:51:12.492354');
INSERT INTO `events` (`id`, `parcel_id`, `status`, `location`, `note`, `actor_id`, `occurred_at`) VALUES (12, 1, 'IN_TRANSIT', 'Kurnool Sorting', 'On highway, ETA 6 hours', 2, '2026-10-15 19:51:12.556075');
INSERT INTO `events` (`id`, `parcel_id`, `status`, `location`, `note`, `actor_id`, `occurred_at`) VALUES (13, 1, 'OUT_FOR_DELIVERY', 'Hyderabad Depot', 'Loaded on delivery bike', 2, '2026-10-15 19:51:12.651248');
INSERT INTO `events` (`id`, `parcel_id`, `status`, `location`, `note`, `actor_id`, `occurred_at`) VALUES (14, 1, 'DELIVERED', 'Jubilee Hills', 'Signed by receptionist', 2, '2026-10-15 19:51:12.740989');
INSERT INTO `events` (`id`, `parcel_id`, `status`, `location`, `note`, `actor_id`, `occurred_at`) VALUES (15, 2, 'PICKED_UP', 'Chennai Warehouse', 'Bulk pickup done', 3, '2026-10-15 19:51:12.831727');
INSERT INTO `events` (`id`, `parcel_id`, `status`, `location`, `note`, `actor_id`, `occurred_at`) VALUES (16, 2, 'IN_TRANSIT', 'Nellore Junction', 'Truck departed at 6 AM', 3, '2026-10-15 19:51:12.923586');
INSERT INTO `events` (`id`, `parcel_id`, `status`, `location`, `note`, `actor_id`, `occurred_at`) VALUES (17, 3, 'PICKED_UP', 'Whitefield Office', 'Scanned at pickup', 4, '2026-10-15 19:51:13.019525');
INSERT INTO `events` (`id`, `parcel_id`, `status`, `location`, `note`, `actor_id`, `occurred_at`) VALUES (18, 3, 'IN_TRANSIT', 'Anantapur Hub', 'Via express air route', 4, '2026-10-15 19:51:13.109583');
INSERT INTO `events` (`id`, `parcel_id`, `status`, `location`, `note`, `actor_id`, `occurred_at`) VALUES (19, 3, 'OUT_FOR_DELIVERY', 'Hitech City Hub', 'Last mile delivery started', 4, '2026-10-15 19:51:13.199822');
INSERT INTO `events` (`id`, `parcel_id`, `status`, `location`, `note`, `actor_id`, `occurred_at`) VALUES (20, 4, 'PICKED_UP', 'Delhi Dispatch Center', 'Heavy book box, handle with care', 2, '2026-10-15 19:51:13.288459');
INSERT INTO `events` (`id`, `parcel_id`, `status`, `location`, `note`, `actor_id`, `occurred_at`) VALUES (21, 5, 'CANCELLED', 'Origin', 'Customer requested cancellation', 1, '2026-10-15 19:51:13.38062');

-- ----------------------------------------------------------------------------
-- Table structure for table `alerts`
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS `alerts`;
CREATE TABLE `alerts` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `user_id` BIGINT NOT NULL,
  `parcel_id` BIGINT NOT NULL,
  `message` VARCHAR(700) NOT NULL,
  `is_read` BOOLEAN NOT NULL DEFAULT FALSE,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`parcel_id`) REFERENCES `shipments`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Data for table `alerts`
INSERT INTO `alerts` (`id`, `user_id`, `parcel_id`, `message`, `is_read`, `created_at`) VALUES (1, 2, 1, 'Assigned: CT-51694e34-5c68-49fa-8a48-1dee34012f12', false, '2026-10-15 19:51:11.697279');
INSERT INTO `alerts` (`id`, `user_id`, `parcel_id`, `message`, `is_read`, `created_at`) VALUES (2, 3, 2, 'Assigned: CT-1fe5eb62-1b44-4d1f-b573-3232023bb0d1', false, '2026-10-15 19:51:11.918825');
INSERT INTO `alerts` (`id`, `user_id`, `parcel_id`, `message`, `is_read`, `created_at`) VALUES (3, 4, 3, 'Assigned: CT-10f949cb-030c-412d-99de-c5e3f9f2b1bf', false, '2026-10-15 19:51:11.983788');
INSERT INTO `alerts` (`id`, `user_id`, `parcel_id`, `message`, `is_read`, `created_at`) VALUES (4, 2, 4, 'Assigned: CT-6937081a-c70c-4d7f-bac1-ea9123083a2a', false, '2026-10-15 19:51:12.20587');
INSERT INTO `alerts` (`id`, `user_id`, `parcel_id`, `message`, `is_read`, `created_at`) VALUES (5, 3, 5, 'Assigned: CT-06be95d1-6848-483f-b7e0-866806648e1d', false, '2026-10-15 19:51:12.267781');
INSERT INTO `alerts` (`id`, `user_id`, `parcel_id`, `message`, `is_read`, `created_at`) VALUES (6, 2, 1, 'CT-51694e34-5c68-49fa-8a48-1dee34012f12: PICKED_UP', false, '2026-10-15 19:51:12.49267');
INSERT INTO `alerts` (`id`, `user_id`, `parcel_id`, `message`, `is_read`, `created_at`) VALUES (7, 2, 1, 'CT-51694e34-5c68-49fa-8a48-1dee34012f12: IN_TRANSIT', false, '2026-10-15 19:51:12.556264');
INSERT INTO `alerts` (`id`, `user_id`, `parcel_id`, `message`, `is_read`, `created_at`) VALUES (8, 2, 1, 'CT-51694e34-5c68-49fa-8a48-1dee34012f12: OUT_FOR_DELIVERY', false, '2026-10-15 19:51:12.651419');
INSERT INTO `alerts` (`id`, `user_id`, `parcel_id`, `message`, `is_read`, `created_at`) VALUES (9, 2, 1, 'CT-51694e34-5c68-49fa-8a48-1dee34012f12: DELIVERED', false, '2026-10-15 19:51:12.741153');
INSERT INTO `alerts` (`id`, `user_id`, `parcel_id`, `message`, `is_read`, `created_at`) VALUES (10, 3, 2, 'CT-1fe5eb62-1b44-4d1f-b573-3232023bb0d1: PICKED_UP', false, '2026-10-15 19:51:12.831951');
INSERT INTO `alerts` (`id`, `user_id`, `parcel_id`, `message`, `is_read`, `created_at`) VALUES (11, 3, 2, 'CT-1fe5eb62-1b44-4d1f-b573-3232023bb0d1: IN_TRANSIT', false, '2026-10-15 19:51:12.923795');
INSERT INTO `alerts` (`id`, `user_id`, `parcel_id`, `message`, `is_read`, `created_at`) VALUES (12, 4, 3, 'CT-10f949cb-030c-412d-99de-c5e3f9f2b1bf: PICKED_UP', false, '2026-10-15 19:51:13.019864');
INSERT INTO `alerts` (`id`, `user_id`, `parcel_id`, `message`, `is_read`, `created_at`) VALUES (13, 4, 3, 'CT-10f949cb-030c-412d-99de-c5e3f9f2b1bf: IN_TRANSIT', false, '2026-10-15 19:51:13.109735');
INSERT INTO `alerts` (`id`, `user_id`, `parcel_id`, `message`, `is_read`, `created_at`) VALUES (14, 4, 3, 'CT-10f949cb-030c-412d-99de-c5e3f9f2b1bf: OUT_FOR_DELIVERY', false, '2026-10-15 19:51:13.200002');
INSERT INTO `alerts` (`id`, `user_id`, `parcel_id`, `message`, `is_read`, `created_at`) VALUES (15, 2, 4, 'CT-6937081a-c70c-4d7f-bac1-ea9123083a2a: PICKED_UP', false, '2026-10-15 19:51:13.288634');
INSERT INTO `alerts` (`id`, `user_id`, `parcel_id`, `message`, `is_read`, `created_at`) VALUES (16, 3, 5, 'CT-06be95d1-6848-483f-b7e0-866806648e1d: CANCELLED', false, '2026-10-15 19:51:13.380866');

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================================
--  END OF MYSQL DUMP
-- ============================================================================
