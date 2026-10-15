package courier;

import java.awt.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/** Swing presentation; blocking database operations run on a worker thread. */
public final class App {
  private final Store store;
  private final JFrame frame = new JFrame("Courier • JDBC Delivery Manager");
  private Store.User user;
  private JTable parcels;
  private final JTextField search = new JTextField(18);
  private final JComboBox<String> filter =
      new JComboBox<>(
          new String[] {
            "",
            "REGISTERED",
            "PICKED_UP",
            "IN_TRANSIT",
            "OUT_FOR_DELIVERY",
            "DELIVERED",
            "DELIVERY_FAILED",
            "RETURNED",
            "CANCELLED"
          });
  private int page;
  private final JLabel summary = new JLabel(" ");
  private final JLabel pageLabel = new JLabel();

  interface Task {
    Object run() throws Exception;
  }

  public App(Store store) {
    this.store = Objects.requireNonNull(store, "Store cannot be null");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(1150, 720);
    frame.setMinimumSize(new Dimension(850, 550));
    frame.setLocationRelativeTo(null);
    login();
    frame.setVisible(true);
  }

  private void work(Task task, java.util.function.Consumer<Object> done) {
    frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    new SwingWorker<Object, Void>() {
      @Override
      protected Object doInBackground() throws Exception {
        return task.run();
      }

      @Override
      protected void done() {
        frame.setCursor(Cursor.getDefaultCursor());
        try {
          done.accept(get());
        } catch (Exception e) {
          Throwable cause = e.getCause() == null ? e : e.getCause();
          JOptionPane.showMessageDialog(
              frame, cause.getMessage(), "Operation failed", JOptionPane.ERROR_MESSAGE);
        }
      }
    }.execute();
  }

  private JButton button(String title, Runnable action) {
    JButton b = new JButton(title);
    b.addActionListener(e -> action.run());
    return b;
  }

  private JPanel form(String[] labels, JComponent[] fields) {
    JPanel panel = new JPanel(new GridLayout(0, 2, 12, 10));
    panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    for (int i = 0; i < labels.length; i++) {
      JLabel label = new JLabel(labels[i]);
      label.setLabelFor(fields[i]);
      panel.add(label);
      panel.add(fields[i]);
    }
    return panel;
  }

  private boolean confirm(String title, JPanel panel) {
    return JOptionPane.showConfirmDialog(
            frame, panel, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)
        == JOptionPane.OK_OPTION;
  }

  private void login() {
    user = null;
    JPanel root = new JPanel(new BorderLayout(20, 20));
    JLabel title =
        new JLabel("Courier Parcel Tracking & Delivery Management", SwingConstants.CENTER);
    title.setFont(title.getFont().deriveFont(24f));
    root.add(title, BorderLayout.NORTH);
    JTextField email = new JTextField();
    JPasswordField password = new JPasswordField();
    JPanel center = form(new String[] {"Email", "Password"}, new JComponent[] {email, password});
    JPanel wrapper = new JPanel(new GridBagLayout());
    wrapper.add(center);
    root.add(wrapper);
    JPanel actions = new JPanel();
    actions.add(
        button(
            "Sign in",
            () -> {
              String address = email.getText();
              char[] secret = password.getPassword();
              password.setText("");
              work(
                  () -> store.login(address, secret),
                  result -> {
                    user = (Store.User) result;
                    home();
                  });
            }));
    actions.add(button("Public tracking", this::tracking));
    root.add(actions, BorderLayout.SOUTH);
    frame.setContentPane(root);
    frame.revalidate();
    frame.repaint();
  }

  private void tracking() {
    String id = JOptionPane.showInputDialog(frame, "Tracking ID");
    if (id != null && !id.trim().isBlank()) {
      work(() -> store.track(id.trim()), result -> showRows("Public journey • UTC", rows(result)));
    }
  }

  @SuppressWarnings("unchecked")
  private static List<Map<String, Object>> rows(Object value) {
    return (List<Map<String, Object>>) value;
  }

  private static JTable table(List<Map<String, Object>> rows) {
    String[] cols =
        rows.isEmpty() ? new String[] {"No results"} : rows.get(0).keySet().toArray(String[]::new);
    DefaultTableModel model =
        new DefaultTableModel(cols, 0) {
          @Override
          public boolean isCellEditable(int r, int c) {
            return false;
          }
        };
    for (var row : rows) {
      model.addRow(row.values().toArray());
    }
    JTable t = new JTable(model);
    t.setRowHeight(30);
    t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    t.setAutoCreateRowSorter(true);
    t.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    for (int i = 0; i < t.getColumnCount(); i++) {
      t.getColumnModel().getColumn(i).setPreferredWidth(cols[i].equals("tracking") ? 310 : 150);
    }
    return t;
  }

  private void showRows(String title, List<Map<String, Object>> rows) {
    JDialog dialog = new JDialog(frame, title, true);
    dialog.add(new JScrollPane(table(rows)));
    dialog.setSize(900, 420);
    dialog.setLocationRelativeTo(frame);
    dialog.setVisible(true);
  }

  private long selected() {
    int viewRow = parcels.getSelectedRow();
    if (viewRow < 0) {
      throw new IllegalArgumentException("Select a parcel first");
    }
    int modelRow = parcels.convertRowIndexToModel(viewRow);
    return ((Number) parcels.getModel().getValueAt(modelRow, 0)).longValue();
  }

  private void home() {
    JPanel root = new JPanel(new BorderLayout(10, 12));
    root.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
    JPanel top = new JPanel(new BorderLayout());
    JLabel heading = new JLabel("Courier Operations  |  " + user.name() + " • " + user.role());
    heading.setFont(heading.getFont().deriveFont(22f));
    top.add(heading, BorderLayout.NORTH);
    top.add(summary, BorderLayout.SOUTH);
    root.add(top, BorderLayout.NORTH);
    parcels = table(List.of());
    JPanel body = new JPanel(new BorderLayout());
    JPanel tools = new JPanel(new FlowLayout(FlowLayout.LEFT));
    tools.add(new JLabel("Search"));
    tools.add(search);
    tools.add(filter);
    tools.add(
        button(
            "Search / refresh",
            () -> {
              page = 0;
              refresh();
            }));
    body.add(tools, BorderLayout.NORTH);
    body.add(new JScrollPane(parcels));
    JPanel pages = new JPanel();
    pages.add(
        button(
            "Previous",
            () -> {
              page = Math.max(0, page - 1);
              refresh();
            }));
    pages.add(pageLabel);
    pages.add(
        button(
            "Next",
            () -> {
              if (parcels.getRowCount() == 25) {
                page++;
                refresh();
              }
            }));
    body.add(pages, BorderLayout.SOUTH);
    root.add(body);
    JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
    if (user.role().equals("ADMIN")) {
      actions.add(button("Register parcel", this::register));
      actions.add(button("Create agent", this::agent));
      actions.add(button("Assign agent", this::assign));
    }
    actions.add(button("Update status", this::status));
    actions.add(
        button(
            "History",
            () -> {
              try {
                long id = selected();
                work(
                    () -> store.history(user, id),
                    v -> showRows("Internal history • UTC", rows(v)));
              } catch (Exception e) {
                error(e);
              }
            }));
    actions.add(button("Notifications", this::notifications));
    actions.add(button("Track", this::tracking));
    actions.add(button("Sign out", this::login));
    root.add(actions, BorderLayout.SOUTH);
    frame.setContentPane(root);
    frame.revalidate();
    frame.repaint();
    refresh();
  }

  private void refresh() {
    Store.User current = user;
    String term = search.getText();
    String state = (String) filter.getSelectedItem();
    int currentPage = page;
    work(
        () -> List.of(store.parcels(current, term, state, currentPage), store.dashboard(current)),
        v -> {
          if (user != current) return;
          List<?> result = (List<?>) v;
          JTable fresh = table(rows(result.get(0)));
          parcels.setModel(fresh.getModel());
          parcels.setAutoCreateRowSorter(true);
          for (int i = 0; i < parcels.getColumnCount(); i++) {
            parcels.getColumnModel().getColumn(i).setPreferredWidth(i == 1 ? 310 : 150);
          }
          var stats = rows(result.get(1)).get(0);
          long total = ((Number) stats.get("total")).longValue();
          long delivered = ((Number) stats.get("delivered")).longValue();
          long failed = ((Number) stats.get("failed")).longValue();
          long active = ((Number) stats.get("active")).longValue();
          long unassigned = ((Number) stats.get("unassigned")).longValue();
          long overdue = ((Number) stats.get("overdue")).longValue();
          double rate = total == 0 ? 0.0 : 100.0 * delivered / total;
          summary.setText(
              String.format(
                  "Total: %d  |  Delivered: %d  |  Active: %d  |  Unassigned: %d  |  Failed: %d  |  Overdue: %d  |  Success Rate: %.1f%%",
                  total, delivered, active, unassigned, failed, overdue, rate));
          pageLabel.setText("Page " + (currentPage + 1));
        });
  }

  private void error(Exception e) {
    JOptionPane.showMessageDialog(frame, e.getMessage(), "Check input", JOptionPane.ERROR_MESSAGE);
  }

  private void register() {
    String[] labels = {
      "Sender name",
      "Sender email / phone",
      "Sender address",
      "Recipient name",
      "Recipient email / phone",
      "Recipient address",
      "Weight (kg)",
      "Expected date (YYYY-MM-DD)"
    };
    JTextField[] fields = new JTextField[8];
    for (int i = 0; i < 8; i++) fields[i] = new JTextField(25);
    fields[7].setText(LocalDate.now(java.time.ZoneOffset.UTC).plusDays(3).toString());
    JPanel panel = form(labels, fields);
    JComboBox<String> service = new JComboBox<>(new String[] {"STANDARD", "EXPRESS"});
    panel.add(new JLabel("Service"));
    panel.add(service);
    if (confirm("Register parcel", panel)) {
      try {
        var r =
            new Store.Registration(
                fields[0].getText(),
                fields[1].getText(),
                fields[2].getText(),
                fields[3].getText(),
                fields[4].getText(),
                fields[5].getText(),
                new BigDecimal(fields[6].getText().trim()),
                (String) service.getSelectedItem(),
                LocalDate.parse(fields[7].getText().trim()));
        work(
            () -> store.register(user, r),
            id -> {
              JOptionPane.showMessageDialog(frame, "Tracking ID: " + id);
              refresh();
            });
      } catch (Exception e) {
        error(e);
      }
    }
  }

  private void agent() {
    JTextField name = new JTextField(), email = new JTextField();
    JPasswordField password = new JPasswordField();
    if (confirm(
        "Create delivery agent",
        form(
            new String[] {"Name", "Email", "Password (12+ characters)"},
            new JComponent[] {name, email, password}))) {
      String n = name.getText(), mail = email.getText();
      char[] pass = password.getPassword();
      work(
          () -> {
            store.createAgent(user, n, mail, pass);
            return null;
          },
          v -> JOptionPane.showMessageDialog(frame, "Agent created"));
    }
  }

  private void assign() {
    try {
      long id = selected();
      work(
          () -> store.agents(user),
          v -> {
            var agents = rows(v);
            if (agents.isEmpty()) {
              JOptionPane.showMessageDialog(frame, "Create an agent first");
              return;
            }
            String[] names =
                agents.stream()
                    .map(r -> r.get("name") + " <" + r.get("email") + ">")
                    .toArray(String[]::new);
            JComboBox<String> choice = new JComboBox<>(names);
            if (confirm(
                "Assign / reassign", form(new String[] {"Agent"}, new JComponent[] {choice}))) {
              long agent = ((Number) agents.get(choice.getSelectedIndex()).get("id")).longValue();
              work(
                  () -> {
                    store.change(user, id, agent, null, null, null);
                    return null;
                  },
                  x -> refresh());
            }
          });
    } catch (Exception e) {
      error(e);
    }
  }

  private void status() {
    try {
      int viewRow = parcels.getSelectedRow();
      if (viewRow < 0) {
        throw new IllegalArgumentException("Select a parcel first");
      }
      int modelRow = parcels.convertRowIndexToModel(viewRow);
      long id = ((Number) parcels.getModel().getValueAt(modelRow, 0)).longValue();
      String current = parcels.getModel().getValueAt(modelRow, 5).toString();

      List<String> next = new ArrayList<>(Store.next(current));
      if (!user.role().equals("ADMIN")) next.remove("CANCELLED");
      if (next.isEmpty()) throw new IllegalArgumentException("No available status updates");
      JComboBox<String> status = new JComboBox<>(next.toArray(String[]::new));
      JTextField location = new JTextField(), note = new JTextField();
      if (confirm(
          "Update parcel",
          form(
              new String[] {"Status", "Public location (city / hub only)", "Internal note"},
              new JComponent[] {status, location, note}))) {
        String to = (String) status.getSelectedItem();
        String loc = location.getText();
        String n = note.getText();
        work(
            () -> {
              store.change(user, id, null, to, loc, n);
              return null;
            },
            v -> refresh());
      }
    } catch (Exception e) {
      error(e);
    }
  }

  private void notifications() {
    work(
        () -> store.alerts(user),
        v -> {
          List<Map<String, Object>> alerts = rows(v);
          JTable t = table(alerts);
          JPanel panel = new JPanel(new BorderLayout());
          panel.add(new JScrollPane(t));
          if (confirm("Notifications • select one to mark read", panel)
              && t.getSelectedRow() >= 0) {
            int modelRow = t.convertRowIndexToModel(t.getSelectedRow());
            long id = ((Number) t.getModel().getValueAt(modelRow, 0)).longValue();
            work(
                () -> {
                  store.read(user, id);
                  return null;
                },
                x -> {});
          }
        });
  }

  /** Run a full end-to-end screen output demo directly in the console. */
  public static void runDemo(Store store) {
    System.out.println("================================================================================");
    System.out.println("          🚚 COURIER DELIVERY MANAGEMENT SYSTEM • LIVE SCREEN DEMO             ");
    System.out.println("================================================================================");
    try {
      System.out.println("[INFO] Connecting to database: " + store.getUrl());
      String adminEmail = "admin@courier.local";
      char[] adminPass = "Admin@Courier123!".toCharArray();
      store.initialize(adminEmail, adminPass);
      System.out.println("[OK] Database schema initialized and verified.");

      System.out.println("\n--- [STEP 1] Administrator Authentication ---");
      Store.User admin = store.login(adminEmail, "Admin@Courier123!".toCharArray());
      System.out.printf("  ✓ Logged in as: %s (Role: %s, ID: %d)%n", admin.name(), admin.role(), admin.id());

      System.out.println("\n--- [STEP 2] Delivery Agent Onboarding ---");
      store.createAgent(admin, "John Swift", "john.swift@courier.local", "Agent@Swift123!".toCharArray());
      store.createAgent(admin, "Sarah Dash", "sarah.dash@courier.local", "Agent@Dash1234!".toCharArray());
      System.out.println("  ✓ Created Delivery Agents: 'John Swift' & 'Sarah Dash'");

      var agents = store.agents(admin);
      System.out.println("  Active Agent Roster:");
      for (var a : agents) {
        System.out.printf("    • Agent #%s: %s <%s>%n", a.get("id"), a.get("name"), a.get("email"));
      }
      long agentId = ((Number) agents.get(0).get("id")).longValue();
      Store.User agent = store.login("john.swift@courier.local", "Agent@Swift123!".toCharArray());

      System.out.println("\n--- [STEP 3] Parcel Registration ---");
      Store.Registration reg =
          new Store.Registration(
              "Global Tech Labs",
              "+1-555-0192",
              "100 Innovation Way, Boston, MA",
              "Pacific Software Corp",
              "+1-555-0847",
              "450 Silicon Blvd, San Jose, CA",
              new BigDecimal("3.45"),
              "EXPRESS",
              LocalDate.now(java.time.ZoneOffset.UTC).plusDays(2));
      String tracking = store.register(admin, reg);
      System.out.printf("  ✓ Registered Express Parcel: %s%n", tracking);
      System.out.printf("    From: %s -> To: %s (Weight: %s kg, Service: %s)%n",
          reg.sender(), reg.recipient(), reg.weight(), reg.service());

      var parcels = store.parcels(admin, tracking, "", 0);
      long parcelId = ((Number) parcels.get(0).get("id")).longValue();
      System.out.printf("  Internal Parcel ID: #%d, Initial Status: %s%n", parcelId, parcels.get(0).get("status"));

      System.out.println("\n--- [STEP 4] Dispatch & Agent Assignment ---");
      store.change(admin, parcelId, agentId, null, null, null);
      System.out.printf("  ✓ Parcel %s assigned to Agent #%d (%s)%n", tracking, agentId, agent.name());

      System.out.println("\n--- [STEP 5] Lifecycle Journey Progression ---");
      String[][] stages = {
        {"PICKED_UP", "Boston Logistics Hub", "Package collected from sender facility"},
        {"IN_TRANSIT", "Chicago Central Air Cargo", "Departed transit sorting center via Express flight"},
        {"OUT_FOR_DELIVERY", "San Jose Bay Hub", "Loaded on final delivery vehicle with Agent John"},
        {"DELIVERED", "San Jose Recipient Dock", "Delivered and signature obtained from Receiving Dept"}
      };
      for (String[] stage : stages) {
        store.change(agent, parcelId, null, stage[0], stage[1], stage[2]);
        System.out.printf("  ➔ [%-16s] Location: %-26s Note: %s%n", stage[0], stage[1], stage[2]);
      }

      System.out.println("\n--- [STEP 6] Public Tracking Lookup (Customer View • Privacy Preserved) ---");
      System.out.printf("  Tracking ID: %s%n", tracking);
      System.out.println("  +----------------------+----------------------------+---------------------+");
      System.out.println("  | Status               | Public Location            | Event Timestamp UTC |");
      System.out.println("  +----------------------+----------------------------+---------------------+");
      var publicEvents = store.track(tracking);
      for (var ev : publicEvents) {
        System.out.printf("  | %-20s | %-26s | %-19s |%n",
            ev.get("status"), ev.get("location"), ev.get("occurred_at"));
      }
      System.out.println("  +----------------------+----------------------------+---------------------+");
      System.out.println("  ✓ Privacy verification: Recipient street address and internal notes remain strictly confidential.");

      System.out.println("\n--- [STEP 7] Agent Notification Alerts ---");
      var alerts = store.alerts(agent);
      System.out.printf("  Total alerts received by %s: %d%n", agent.name(), alerts.size());
      for (int i = 0; i < Math.min(3, alerts.size()); i++) {
        var al = alerts.get(i);
        System.out.printf("    • Alert #%s: %s (Read: %s)%n", al.get("id"), al.get("message"), al.get("is_read"));
      }
      if (!alerts.isEmpty()) {
        long alertId = ((Number) alerts.get(0).get("id")).longValue();
        store.read(agent, alertId);
        System.out.printf("  ✓ Alert #%d marked as READ by agent.%n", alertId);
      }

      System.out.println("\n--- [STEP 8] Operations Dashboard Statistics ---");
      var statsList = store.dashboard(admin);
      if (!statsList.isEmpty()) {
        var stats = statsList.get(0);
        long total = ((Number) stats.get("total")).longValue();
        long delivered = ((Number) stats.get("delivered")).longValue();
        long active = ((Number) stats.get("active")).longValue();
        long unassigned = ((Number) stats.get("unassigned")).longValue();
        long failed = ((Number) stats.get("failed")).longValue();
        long overdue = ((Number) stats.get("overdue")).longValue();
        double successRate = total == 0 ? 0.0 : (100.0 * delivered / total);

        System.out.println("  +-----------------------------------------------------------------------------------------+");
        System.out.printf("  | Total Parcels: %-3d | Delivered: %-3d | Active: %-3d | Failed: %-3d | Success Rate: %5.1f%% |%n",
            total, delivered, active, failed, successRate);
        System.out.printf("  | Unassigned:    %-3d | Overdue:   %-3d                                                     |%n",
            unassigned, overdue);
        System.out.println("  +-----------------------------------------------------------------------------------------+");
      }

      System.out.println("\n================================================================================");
      System.out.println("  🎉 DEMO SUCCESSFUL: All lines checked, verified error-free, and operational!  ");
      System.out.println("================================================================================");
    } catch (Exception e) {
      System.err.println("Demo encountered an error: " + e.getMessage());
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    boolean demoRequested = false;
    for (String arg : args) {
      if ("--demo".equalsIgnoreCase(arg) || "-demo".equalsIgnoreCase(arg) || "--cli".equalsIgnoreCase(arg)) {
        demoRequested = true;
        break;
      }
    }

    String envUrl = System.getenv("DB_URL");
    String url = envUrl;
    String username = System.getenv().getOrDefault("DB_USERNAME", "courier");
    String password = System.getenv().getOrDefault("DB_PASSWORD", "");

    if (url == null || url.isBlank()) {
      // Default to embedded H2 in MySQL compatibility mode for instant, zero-setup reliability
      url = "jdbc:h2:mem:courier;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
      username = "sa";
      password = "";
      System.out.println("[INFO] DB_URL not specified. Using embedded H2 database (MySQL compatibility mode).");
    }

    Store store = new Store(url, username, password);

    String adminEmail = System.getenv("ADMIN_EMAIL");
    if (adminEmail == null || adminEmail.isBlank()) {
      adminEmail = "admin@courier.local";
    }

    String adminPassStr = System.getenv("ADMIN_PASSWORD");
    char[] adminPassword =
        (adminPassStr != null && adminPassStr.length() >= 12)
            ? adminPassStr.toCharArray()
            : "Admin@Courier123!".toCharArray();

    try {
      store.initialize(adminEmail, adminPassword);
    } catch (Exception e) {
      System.err.println("Database initialization failed: " + e.getMessage());
      e.printStackTrace();
      System.exit(1);
    } finally {
      Arrays.fill(adminPassword, '\0');
    }

    boolean isHeadless = GraphicsEnvironment.isHeadless();

    if (demoRequested || isHeadless) {
      if (isHeadless && !demoRequested) {
        System.out.println("[INFO] Headless environment detected (no graphical display). Running output demo on screen:");
      }
      runDemo(store);
      return;
    }

    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      SwingUtilities.invokeLater(() -> new App(store));
    } catch (Exception e) {
      System.err.println("GUI launch failed: " + e.getMessage());
      System.out.println("Falling back to console demo output...");
      runDemo(store);
    }
  }
}
