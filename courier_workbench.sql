-- ============================================================================
-- Courier Delivery Management System • MySQL Workbench Query Console
-- Host: 127.0.0.1:3307 | Database: courier_jdbc | User: root
-- ============================================================================

USE `courier_jdbc`;

-- 1. All Shipments and Current Delivery Statuses
SELECT id, tracking, sender, recipient, weight, service, status, agent_id, expected
FROM shipments
ORDER BY id;

-- 2. System Users and Delivery Agents
SELECT id, name, email, role, active
FROM users
ORDER BY id;

-- 3. Operations Management KPI Dashboard
SELECT 
    COUNT(*) AS total_shipments,
    SUM(CASE WHEN status='DELIVERED' THEN 1 ELSE 0 END) AS delivered,
    SUM(CASE WHEN status='IN_TRANSIT' THEN 1 ELSE 0 END) AS in_transit,
    SUM(CASE WHEN status='OUT_FOR_DELIVERY' THEN 1 ELSE 0 END) AS out_for_delivery,
    SUM(CASE WHEN status='PICKED_UP' THEN 1 ELSE 0 END) AS picked_up,
    SUM(CASE WHEN status='CANCELLED' THEN 1 ELSE 0 END) AS cancelled
FROM shipments;

-- 4. Live Shipment Journey Timeline with Actors
SELECT e.id, s.tracking, e.status, e.location, e.note, u.name AS updated_by, e.occurred_at
FROM events e
JOIN shipments s ON s.id = e.parcel_id
JOIN users u ON u.id = e.actor_id
ORDER BY e.occurred_at DESC
LIMIT 10;
