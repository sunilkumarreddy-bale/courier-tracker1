package courier;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.*;

/**
 * Native Desktop MySQL Workstation connected to MySQL 8+ on port 3307.
 * Provides interactive SQL query execution, schema inspection, and real-time result grids.
 */
public final class MySQLWorkstation {
  private final String url;
  private final String user;
  private final String password;

  private final JFrame frame = new JFrame("🐬 MySQL Workstation • courier_jdbc @ 127.0.0.1:3307");
  private final JTextArea sqlEditor = new JTextArea(5, 50);
  private final JTable resultTable = new JTable();
  private final JLabel statusLabel = new JLabel(" Ready");
  private final JLabel rowCountLabel = new JLabel("0 rows ");
  private DefaultTreeModel treeModel;
  private JTree schemaTree;

  public MySQLWorkstation(String url, String user, String password) {
    this.url = url;
    this.user = user;
    this.password = password;

    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    frame.setSize(1100, 700);
    frame.setMinimumSize(new Dimension(850, 500));
    frame.setLocationRelativeTo(null);

    buildUI();
    loadSchemaTree();
    executeQuery("SELECT id, tracking, recipient, weight, service, status, agent_id, expected FROM shipments ORDER BY id;");
    frame.setVisible(true);
  }

  private Connection connect() throws SQLException {
    return DriverManager.getConnection(url, user, password);
  }

  private void buildUI() {
    JPanel root = new JPanel(new BorderLayout(4, 4));
    root.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

    // Top Connection Bar
    JPanel topBar = new JPanel(new BorderLayout(8, 0));
    topBar.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
        BorderFactory.createEmptyBorder(4, 8, 8, 8)));

    JLabel connLabel = new JLabel("🐬 MySQL Community Server 26.7.0  |  127.0.0.1:3307  |  courier_jdbc  |  User: " + user);
    connLabel.setFont(connLabel.getFont().deriveFont(Font.BOLD, 13f));
    connLabel.setForeground(new Color(0, 102, 153));

    JLabel stateBadge = new JLabel(" ● CONNECTED ");
    stateBadge.setOpaque(true);
    stateBadge.setBackground(new Color(220, 245, 220));
    stateBadge.setForeground(new Color(0, 130, 0));
    stateBadge.setFont(stateBadge.getFont().deriveFont(Font.BOLD, 11f));
    stateBadge.setBorder(BorderFactory.createLineBorder(new Color(150, 210, 150)));

    topBar.add(connLabel, BorderLayout.WEST);
    topBar.add(stateBadge, BorderLayout.EAST);
    root.add(topBar, BorderLayout.NORTH);

    // Left Panel: Database Schema Tree
    DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("courier_jdbc");
    treeModel = new DefaultTreeModel(rootNode);
    schemaTree = new JTree(treeModel);
    schemaTree.setRootVisible(true);
    schemaTree.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
          TreePath path = schemaTree.getPathForLocation(e.getX(), e.getY());
          if (path != null && path.getPathCount() == 2) {
            String tableName = path.getLastPathComponent().toString();
            sqlEditor.setText("SELECT * FROM " + tableName + " ORDER BY id;");
            executeQuery("SELECT * FROM " + tableName + " ORDER BY id;");
          }
        }
      }
    });

    JPanel leftPanel = new JPanel(new BorderLayout());
    leftPanel.setPreferredSize(new Dimension(220, 0));
    JLabel schemaTitle = new JLabel(" SCHEMAS & TABLES");
    schemaTitle.setFont(schemaTitle.getFont().deriveFont(Font.BOLD, 11f));
    schemaTitle.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    leftPanel.add(schemaTitle, BorderLayout.NORTH);
    leftPanel.add(new JScrollPane(schemaTree), BorderLayout.CENTER);

    // Center Panel: SQL Editor + Results
    JPanel centerPanel = new JPanel(new BorderLayout(0, 6));

    // Editor toolbar
    JPanel editorToolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
    JButton runBtn = new JButton("▶ Execute SQL");
    runBtn.setFont(runBtn.getFont().deriveFont(Font.BOLD));
    runBtn.setBackground(new Color(0, 120, 215));
    runBtn.setForeground(Color.BLACK);
    runBtn.addActionListener(e -> executeQuery(sqlEditor.getText()));

    JButton q1 = new JButton("Shipments");
    q1.addActionListener(e -> {
      sqlEditor.setText("SELECT id, tracking, recipient, weight, service, status, agent_id, expected FROM shipments ORDER BY id;");
      executeQuery(sqlEditor.getText());
    });
    JButton q2 = new JButton("Users");
    q2.addActionListener(e -> {
      sqlEditor.setText("SELECT id, name, email, role, active FROM users ORDER BY id;");
      executeQuery(sqlEditor.getText());
    });
    JButton q3 = new JButton("Events Audit");
    q3.addActionListener(e -> {
      sqlEditor.setText("SELECT e.id, s.tracking, e.status, e.location, e.note, u.name AS actor, e.occurred_at FROM events e JOIN shipments s ON s.id=e.parcel_id JOIN users u ON u.id=e.actor_id ORDER BY e.occurred_at DESC;");
      executeQuery(sqlEditor.getText());
    });
    JButton q4 = new JButton("Alerts");
    q4.addActionListener(e -> {
      sqlEditor.setText("SELECT id, user_id, parcel_id, message, is_read, created_at FROM alerts ORDER BY id DESC;");
      executeQuery(sqlEditor.getText());
    });
    JButton q5 = new JButton("KPI Summary");
    q5.addActionListener(e -> {
      sqlEditor.setText("SELECT count(*) AS total, SUM(CASE WHEN status='DELIVERED' THEN 1 ELSE 0 END) AS delivered, SUM(CASE WHEN status='IN_TRANSIT' THEN 1 ELSE 0 END) AS in_transit, SUM(CASE WHEN status='OUT_FOR_DELIVERY' THEN 1 ELSE 0 END) AS out_for_delivery, SUM(CASE WHEN status='PICKED_UP' THEN 1 ELSE 0 END) AS picked_up, SUM(CASE WHEN status='CANCELLED' THEN 1 ELSE 0 END) AS cancelled FROM shipments;");
      executeQuery(sqlEditor.getText());
    });

    editorToolBar.add(runBtn);
    editorToolBar.add(new JSeparator(SwingConstants.VERTICAL));
    editorToolBar.add(new JLabel("Quick Queries: "));
    editorToolBar.add(q1);
    editorToolBar.add(q2);
    editorToolBar.add(q3);
    editorToolBar.add(q4);
    editorToolBar.add(q5);

    sqlEditor.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
    sqlEditor.setText("SELECT id, tracking, recipient, weight, service, status, agent_id, expected FROM shipments ORDER BY id;");
    sqlEditor.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        if ((e.isMetaDown() || e.isControlDown()) && e.getKeyCode() == KeyEvent.VK_ENTER) {
          executeQuery(sqlEditor.getText());
        }
      }
    });

    JPanel queryPanel = new JPanel(new BorderLayout());
    queryPanel.add(editorToolBar, BorderLayout.NORTH);
    queryPanel.add(new JScrollPane(sqlEditor), BorderLayout.CENTER);

    // Results Table
    resultTable.setRowHeight(24);
    resultTable.setAutoCreateRowSorter(true);
    resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    JScrollPane resultScroll = new JScrollPane(resultTable);

    JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, queryPanel, resultScroll);
    splitPane.setDividerLocation(150);
    centerPanel.add(splitPane, BorderLayout.CENTER);

    JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, centerPanel);
    mainSplit.setDividerLocation(200);
    root.add(mainSplit, BorderLayout.CENTER);

    // Bottom Status Bar
    JPanel statusBar = new JPanel(new BorderLayout());
    statusBar.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
        BorderFactory.createEmptyBorder(4, 4, 4, 4)));
    statusLabel.setFont(statusLabel.getFont().deriveFont(11f));
    rowCountLabel.setFont(rowCountLabel.getFont().deriveFont(11f));
    statusBar.add(statusLabel, BorderLayout.WEST);
    statusBar.add(rowCountLabel, BorderLayout.EAST);
    root.add(statusBar, BorderLayout.SOUTH);

    frame.setContentPane(root);
  }

  private void loadSchemaTree() {
    DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) treeModel.getRoot();
    rootNode.removeAllChildren();
    try (Connection c = connect()) {
      DatabaseMetaData meta = c.getMetaData();
      try (ResultSet rs = meta.getTables("courier_jdbc", null, "%", new String[] {"TABLE"})) {
        while (rs.next()) {
          String tableName = rs.getString("TABLE_NAME");
          DefaultMutableTreeNode tableNode = new DefaultMutableTreeNode(tableName);
          rootNode.add(tableNode);
        }
      }
      treeModel.reload();
      for (int i = 0; i < schemaTree.getRowCount(); i++) {
        schemaTree.expandRow(i);
      }
    } catch (Exception e) {
      statusLabel.setText(" Error loading schema: " + e.getMessage());
    }
  }

  public void executeQuery(String sql) {
    if (sql == null || sql.trim().isEmpty()) return;
    long start = System.currentTimeMillis();
    new SwingWorker<DefaultTableModel, Void>() {
      protected DefaultTableModel doInBackground() throws Exception {
        try (Connection c = connect();
             Statement stmt = c.createStatement();
             ResultSet rs = stmt.executeQuery(sql.trim())) {

          ResultSetMetaData meta = rs.getMetaData();
          int cols = meta.getColumnCount();
          String[] colNames = new String[cols];
          for (int i = 1; i <= cols; i++) {
            colNames[i - 1] = meta.getColumnLabel(i);
          }

          DefaultTableModel model = new DefaultTableModel(colNames, 0) {
            public boolean isCellEditable(int r, int c) {
              return false;
            }
          };

          while (rs.next()) {
            Object[] row = new Object[cols];
            for (int i = 1; i <= cols; i++) {
              row[i - 1] = rs.getObject(i);
            }
            model.addRow(row);
          }
          return model;
        }
      }

      protected void done() {
        try {
          DefaultTableModel model = get();
          resultTable.setModel(model);
          resultTable.setAutoCreateRowSorter(true);

          // Auto-adjust column widths
          for (int i = 0; i < resultTable.getColumnCount(); i++) {
            String col = resultTable.getColumnName(i).toLowerCase();
            int width = col.contains("tracking") ? 310 : (col.contains("email") || col.contains("note") ? 220 : 120);
            resultTable.getColumnModel().getColumn(i).setPreferredWidth(width);
          }

          long elapsed = System.currentTimeMillis() - start;
          statusLabel.setText(String.format(" ✓ Query executed successfully in %d ms", elapsed));
          rowCountLabel.setText(String.format("%d rows returned ", model.getRowCount()));
        } catch (Exception e) {
          Throwable cause = e.getCause() == null ? e : e.getCause();
          statusLabel.setText(" ❌ SQL Error: " + cause.getMessage());
          JOptionPane.showMessageDialog(frame, cause.getMessage(), "SQL Execution Error", JOptionPane.ERROR_MESSAGE);
        }
      }
    }.execute();
  }

  public static void main(String[] args) {
    String url = System.getenv().getOrDefault(
        "DB_URL",
        "jdbc:mysql://127.0.0.1:3307/courier_jdbc?connectionTimeZone=UTC&forceConnectionTimeZoneToSession=true");
    String user = System.getenv().getOrDefault("DB_USERNAME", "courier");
    String pass = System.getenv().getOrDefault("DB_PASSWORD", "courier_password");

    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception ignored) {}

    SwingUtilities.invokeLater(() -> new MySQLWorkstation(url, user, pass));
  }
}
