package courier;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

/**
 * Populates a file-based H2 database with realistic courier data,
 * then dumps every table so the user can see stored rows.
 */
public class DataLoader {

  public static void main(String[] args) throws Exception {
    // ---- database location on disk ----
    String dbDir  = System.getProperty("user.dir");
    String dbFile = dbDir + "/courier_data";          // H2 creates courier_data.mv.db
    String url    = "jdbc:h2:" + dbFile + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE";

    System.out.println("================================================================================");
    System.out.println("  📦  COURIER DATABASE — DATA LOADER & STORAGE VIEWER");
    System.out.println("================================================================================");
    System.out.println();
    System.out.printf("  Database engine : H2 (MySQL-compatibility mode)%n");
    System.out.printf("  JDBC URL        : %s%n", url);
    System.out.printf("  Storage file    : %s.mv.db%n", dbFile);
    System.out.printf("  Directory       : %s%n", dbDir);
    System.out.println();

    Store store = new Store(url, "sa", "");

    // ── 1. Initialize schema & admin ──────────────────────────────────
    store.initialize("admin@courier.local", "Admin@Courier123!".toCharArray());
    System.out.println("[OK] Schema created. Admin bootstrapped.");

    // ── 2. Create delivery agents ─────────────────────────────────────
    Store.User admin = store.login("admin@courier.local", "Admin@Courier123!".toCharArray());

    String[][] agentData = {
        {"Ravi Kumar",    "ravi.kumar@courier.local",    "Agent@Ravi12345!"},
        {"Priya Sharma",  "priya.sharma@courier.local",  "Agent@Priya1234!"},
        {"Arjun Reddy",   "arjun.reddy@courier.local",   "Agent@Arjun1234!"},
    };
    for (String[] a : agentData) {
      store.createAgent(admin, a[0], a[1], a[2].toCharArray());
    }
    System.out.println("[OK] 3 delivery agents created.");

    Store.User ravi  = store.login("ravi.kumar@courier.local",   "Agent@Ravi12345!".toCharArray());
    Store.User priya = store.login("priya.sharma@courier.local", "Agent@Priya1234!".toCharArray());
    Store.User arjun = store.login("arjun.reddy@courier.local",  "Agent@Arjun1234!".toCharArray());

    // ── 3. Register parcels ───────────────────────────────────────────
    LocalDate today = LocalDate.now(ZoneOffset.UTC);

    Store.Registration[] parcels = {
        new Store.Registration("TechNova Labs",     "tech@nova.in",        "42 MG Road, Bangalore",
                               "DataSync Corp",     "data@sync.com",       "88 Jubilee Hills, Hyderabad",
                               new BigDecimal("2.75"), "EXPRESS", today.plusDays(2)),
        new Store.Registration("FreshMart India",   "orders@freshmart.in", "15 Anna Nagar, Chennai",
                               "GreenLeaf Stores",  "green@leaf.co",       "22 Banjara Hills, Hyderabad",
                               new BigDecimal("5.00"), "STANDARD", today.plusDays(5)),
        new Store.Registration("CloudByte Systems", "ship@cloudbyte.io",   "101 Whitefield, Bangalore",
                               "NetPulse Inc",      "recv@netpulse.com",   "7 Hitech City, Hyderabad",
                               new BigDecimal("1.20"), "EXPRESS", today.plusDays(1)),
        new Store.Registration("BookWorld Online",  "dispatch@bookworld.in","9 Connaught Place, Delhi",
                               "ReadMore Library",  "lib@readmore.org",    "33 Ameerpet, Hyderabad",
                               new BigDecimal("8.50"), "STANDARD", today.plusDays(7)),
        new Store.Registration("MediQuick Pharma",  "ship@mediquick.in",   "5 Koramangala, Bangalore",
                               "HealthFirst Clinic", "clinic@healthfirst.in","12 Gachibowli, Hyderabad",
                               new BigDecimal("0.45"), "EXPRESS", today.plusDays(1)),
    };

    String[] trackingIds = new String[parcels.length];
    for (int i = 0; i < parcels.length; i++) {
      trackingIds[i] = store.register(admin, parcels[i]);
    }
    System.out.printf("[OK] %d parcels registered.%n", parcels.length);

    // ── 4. Get parcel internal IDs ────────────────────────────────────
    long[] ids = new long[parcels.length];
    for (int i = 0; i < trackingIds.length; i++) {
      ids[i] = ((Number) store.parcels(admin, trackingIds[i], "", 0).get(0).get("id")).longValue();
    }

    // ── 5. Assign agents ──────────────────────────────────────────────
    store.change(admin, ids[0], ravi.id(),  null, null, null);   // Parcel 1 → Ravi
    store.change(admin, ids[1], priya.id(), null, null, null);   // Parcel 2 → Priya
    store.change(admin, ids[2], arjun.id(), null, null, null);   // Parcel 3 → Arjun
    store.change(admin, ids[3], ravi.id(),  null, null, null);   // Parcel 4 → Ravi
    store.change(admin, ids[4], priya.id(), null, null, null);   // Parcel 5 → Priya
    System.out.println("[OK] Agents assigned to all parcels.");

    // ── 6. Progress parcels through delivery lifecycle ─────────────────
    // Parcel 1: fully delivered
    store.change(ravi, ids[0], null, "PICKED_UP",        "Bangalore Hub",      "Collected from TechNova office");
    store.change(ravi, ids[0], null, "IN_TRANSIT",       "Kurnool Sorting",    "On highway, ETA 6 hours");
    store.change(ravi, ids[0], null, "OUT_FOR_DELIVERY", "Hyderabad Depot",    "Loaded on delivery bike");
    store.change(ravi, ids[0], null, "DELIVERED",        "Jubilee Hills",      "Signed by receptionist");

    // Parcel 2: in transit
    store.change(priya, ids[1], null, "PICKED_UP",  "Chennai Warehouse",  "Bulk pickup done");
    store.change(priya, ids[1], null, "IN_TRANSIT",  "Nellore Junction",   "Truck departed at 6 AM");

    // Parcel 3: out for delivery
    store.change(arjun, ids[2], null, "PICKED_UP",        "Whitefield Office",  "Scanned at pickup");
    store.change(arjun, ids[2], null, "IN_TRANSIT",        "Anantapur Hub",      "Via express air route");
    store.change(arjun, ids[2], null, "OUT_FOR_DELIVERY",  "Hitech City Hub",    "Last mile delivery started");

    // Parcel 4: picked up only
    store.change(ravi, ids[3], null, "PICKED_UP", "Delhi Dispatch Center", "Heavy book box, handle with care");

    // Parcel 5: cancelled
    store.change(admin, ids[4], null, "CANCELLED", "Origin", "Customer requested cancellation");

    System.out.println("[OK] Parcel statuses updated through various lifecycle stages.");
    System.out.println();

    // ══════════════════════════════════════════════════════════════════
    //  DUMP ALL DATABASE TABLES
    // ══════════════════════════════════════════════════════════════════
    try (Connection c = store.connect()) {
      System.out.println("════════════════════════════════════════════════════════════════════════════════");
      System.out.println("  📂  DATABASE CONTENTS (all tables)");
      System.out.println("════════════════════════════════════════════════════════════════════════════════");

      dumpTable(c, "users",     "SELECT id, name, email, role, active FROM users ORDER BY id");
      dumpTable(c, "shipments", "SELECT id, tracking, sender, recipient, weight, service, status, agent_id, version, expected, created_at FROM shipments ORDER BY id");
      dumpTable(c, "events",    "SELECT id, parcel_id, status, location, note, actor_id, occurred_at FROM events ORDER BY id");
      dumpTable(c, "alerts",    "SELECT id, user_id, parcel_id, message, is_read, created_at FROM alerts ORDER BY id");
    }

    // ── Show file on disk ─────────────────────────────────────────────
    java.io.File dbOnDisk = new java.io.File(dbFile + ".mv.db");
    System.out.println("════════════════════════════════════════════════════════════════════════════════");
    System.out.println("  💾  PHYSICAL STORAGE ON DISK");
    System.out.println("════════════════════════════════════════════════════════════════════════════════");
    System.out.printf("  File path : %s%n", dbOnDisk.getAbsolutePath());
    System.out.printf("  File size : %,d bytes (%.1f KB)%n", dbOnDisk.length(), dbOnDisk.length() / 1024.0);
    System.out.printf("  Exists    : %s%n", dbOnDisk.exists());
    System.out.println();
    System.out.println("  🎉  All data loaded and verified. Open this file with any H2 / DBeaver / JDBC tool.");
    System.out.println("================================================================================");
  }

  /** Pretty-prints a SQL query result as an ASCII table. */
  static void dumpTable(Connection c, String tableName, String sql) throws SQLException {
    System.out.println();
    System.out.printf("┌─── TABLE: %s ───%n", tableName.toUpperCase());
    try (PreparedStatement ps = c.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

      ResultSetMetaData meta = rs.getMetaData();
      int cols = meta.getColumnCount();

      // Column widths
      int[] widths = new int[cols];
      String[] headers = new String[cols];
      for (int i = 0; i < cols; i++) {
        headers[i] = meta.getColumnLabel(i + 1).toLowerCase();
        widths[i] = Math.max(headers[i].length(), 8);
      }

      // Read all rows first to compute max widths
      List<String[]> rows = new ArrayList<>();
      while (rs.next()) {
        String[] row = new String[cols];
        for (int i = 0; i < cols; i++) {
          Object val = rs.getObject(i + 1);
          row[i] = val == null ? "NULL" : val.toString();
          widths[i] = Math.max(widths[i], Math.min(row[i].length(), 42));
        }
        rows.add(row);
      }

      // Print header
      StringBuilder sep = new StringBuilder("│ ");
      StringBuilder hdr = new StringBuilder("│ ");
      for (int i = 0; i < cols; i++) {
        String fmt = "%-" + widths[i] + "s";
        hdr.append(String.format(fmt, headers[i])).append(" │ ");
        sep.append("─".repeat(widths[i])).append("─┼─");
      }
      System.out.println(hdr);
      System.out.println(sep);

      // Print data rows
      for (String[] row : rows) {
        StringBuilder line = new StringBuilder("│ ");
        for (int i = 0; i < cols; i++) {
          String val = row[i].length() > 42 ? row[i].substring(0, 39) + "..." : row[i];
          line.append(String.format("%-" + widths[i] + "s", val)).append(" │ ");
        }
        System.out.println(line);
      }
      System.out.printf("│ (%d rows)%n", rows.size());
    }
  }
}
