package courier;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.*;
import java.util.*;

/**
 * Exports all tables and data from the Courier database into a clean,
 * standard MySQL dump file (courier_mysql_dump.sql) compatible with MySQL 8.x,
 * MariaDB, and all standard MySQL GUI clients (MySQL Workbench, phpMyAdmin, DBeaver).
 */
public class MySQLExporter {

  public static void main(String[] args) throws Exception {
    String dbDir = System.getProperty("user.dir");
    String url = "jdbc:h2:" + dbDir + "/courier_data;MODE=MySQL;DATABASE_TO_LOWER=TRUE";
    String outFile = dbDir + "/courier_mysql_dump.sql";

    System.out.println("Exporting Courier database to MySQL dump: " + outFile);

    try (Connection c = DriverManager.getConnection(url, "sa", "");
         PrintWriter out = new PrintWriter(new FileWriter(outFile))) {

      out.println("-- ============================================================================");
      out.println("--  COURIER DELIVERY MANAGEMENT SYSTEM — MYSQL DATABASE DUMP");
      out.println("--  Generated: " + new java.util.Date());
      out.println("--  Target: MySQL 8.0+ / MariaDB / MySQL Workbench / phpMyAdmin");
      out.println("-- ============================================================================");
      out.println();
      out.println("CREATE DATABASE IF NOT EXISTS `courier_jdbc` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;");
      out.println("USE `courier_jdbc`;");
      out.println();
      out.println("SET FOREIGN_KEY_CHECKS = 0;");
      out.println();

      // Table 1: users
      out.println("-- ----------------------------------------------------------------------------");
      out.println("-- Table structure for table `users`");
      out.println("-- ----------------------------------------------------------------------------");
      out.println("DROP TABLE IF EXISTS `users`;");
      out.println("CREATE TABLE `users` (");
      out.println("  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,");
      out.println("  `name` VARCHAR(100) NOT NULL,");
      out.println("  `email` VARCHAR(254) NOT NULL UNIQUE,");
      out.println("  `password_hash` VARCHAR(300) NOT NULL,");
      out.println("  `role` VARCHAR(10) NOT NULL,");
      out.println("  `active` BOOLEAN NOT NULL DEFAULT TRUE");
      out.println(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");
      out.println();
      dumpInserts(c, "users", out);

      // Table 2: shipments
      out.println("-- ----------------------------------------------------------------------------");
      out.println("-- Table structure for table `shipments`");
      out.println("-- ----------------------------------------------------------------------------");
      out.println("DROP TABLE IF EXISTS `shipments`;");
      out.println("CREATE TABLE `shipments` (");
      out.println("  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,");
      out.println("  `tracking` VARCHAR(40) NOT NULL UNIQUE,");
      out.println("  `sender` VARCHAR(100) NOT NULL,");
      out.println("  `sender_contact` VARCHAR(254) NOT NULL,");
      out.println("  `sender_address` VARCHAR(300) NOT NULL,");
      out.println("  `recipient` VARCHAR(100) NOT NULL,");
      out.println("  `recipient_contact` VARCHAR(254) NOT NULL,");
      out.println("  `recipient_address` VARCHAR(300) NOT NULL,");
      out.println("  `weight` DECIMAL(10,2) NOT NULL,");
      out.println("  `service` VARCHAR(20) NOT NULL,");
      out.println("  `expected` DATE NOT NULL,");
      out.println("  `status` VARCHAR(30) NOT NULL,");
      out.println("  `agent_id` BIGINT,");
      out.println("  `version` BIGINT NOT NULL DEFAULT 0,");
      out.println("  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,");
      out.println("  FOREIGN KEY (`agent_id`) REFERENCES `users`(`id`) ON DELETE SET NULL");
      out.println(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");
      out.println();
      dumpInserts(c, "shipments", out);

      // Table 3: events
      out.println("-- ----------------------------------------------------------------------------");
      out.println("-- Table structure for table `events`");
      out.println("-- ----------------------------------------------------------------------------");
      out.println("DROP TABLE IF EXISTS `events`;");
      out.println("CREATE TABLE `events` (");
      out.println("  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,");
      out.println("  `parcel_id` BIGINT NOT NULL,");
      out.println("  `status` VARCHAR(30) NOT NULL,");
      out.println("  `location` VARCHAR(100) NOT NULL,");
      out.println("  `note` VARCHAR(500) NOT NULL,");
      out.println("  `actor_id` BIGINT NOT NULL,");
      out.println("  `occurred_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,");
      out.println("  FOREIGN KEY (`parcel_id`) REFERENCES `shipments`(`id`) ON DELETE CASCADE,");
      out.println("  FOREIGN KEY (`actor_id`) REFERENCES `users`(`id`) ON DELETE CASCADE");
      out.println(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");
      out.println();
      dumpInserts(c, "events", out);

      // Table 4: alerts
      out.println("-- ----------------------------------------------------------------------------");
      out.println("-- Table structure for table `alerts`");
      out.println("-- ----------------------------------------------------------------------------");
      out.println("DROP TABLE IF EXISTS `alerts`;");
      out.println("CREATE TABLE `alerts` (");
      out.println("  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,");
      out.println("  `user_id` BIGINT NOT NULL,");
      out.println("  `parcel_id` BIGINT NOT NULL,");
      out.println("  `message` VARCHAR(700) NOT NULL,");
      out.println("  `is_read` BOOLEAN NOT NULL DEFAULT FALSE,");
      out.println("  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,");
      out.println("  FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,");
      out.println("  FOREIGN KEY (`parcel_id`) REFERENCES `shipments`(`id`) ON DELETE CASCADE");
      out.println(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");
      out.println();
      dumpInserts(c, "alerts", out);

      out.println("SET FOREIGN_KEY_CHECKS = 1;");
      out.println();
      out.println("-- ============================================================================");
      out.println("--  END OF MYSQL DUMP");
      out.println("-- ============================================================================");
    }

    System.out.println("Successfully generated: " + outFile);
  }

  private static void dumpInserts(Connection c, String table, PrintWriter out) throws SQLException {
    String sql = "SELECT * FROM " + table + " ORDER BY id";
    try (PreparedStatement ps = c.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

      ResultSetMetaData meta = rs.getMetaData();
      int cols = meta.getColumnCount();

      out.println("-- Data for table `" + table + "`");
      int rowCount = 0;
      while (rs.next()) {
        rowCount++;
        StringBuilder colsList = new StringBuilder();
        StringBuilder valsList = new StringBuilder();
        for (int i = 1; i <= cols; i++) {
          if (i > 1) {
            colsList.append(", ");
            valsList.append(", ");
          }
          colsList.append("`").append(meta.getColumnLabel(i).toLowerCase()).append("`");
          Object val = rs.getObject(i);
          if (val == null) {
            valsList.append("NULL");
          } else if (val instanceof Number || val instanceof Boolean) {
            valsList.append(val);
          } else {
            String str = val.toString().replace("\\", "\\\\").replace("'", "\\'");
            valsList.append("'").append(str).append("'");
          }
        }
        out.println("INSERT INTO `" + table + "` (" + colsList + ") VALUES (" + valsList + ");");
      }
      out.println();
      System.out.printf("  ✓ Exported %d rows from table `%s`%n", rowCount, table);
    }
  }
}
