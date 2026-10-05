import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class SearchPage extends JFrame {

    private JTextField txtKeyword = new JTextField();
    private JComboBox<String> cmbType = new JComboBox<>(new String[]{"Notes", "Resources"});
    private JComboBox<String> cmbCategory = new JComboBox<>();
    private DefaultTableModel model;
    private JTable table;
    private JLabel lblStatus = new JLabel("Enter a keyword and press Search.");

    public SearchPage() {
        setTitle("Search");
        setSize(840, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        DBConnection.fillCategoryBox(cmbCategory, true);

        // Top form: label / field pairs
        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(15, 20, 5, 20));
        form.add(new JLabel("Keyword:"));
        form.add(txtKeyword);
        form.add(new JLabel("Search in:"));
        form.add(cmbType);
        form.add(new JLabel("Category:"));
        form.add(cmbCategory);
        add(form, BorderLayout.NORTH);

        // Empty table, filled after a search
        model = createModel(new String[]{"ID", "Title", "Details", "Category", "Favorite"});
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton btnSearch = new JButton("Search");
        JButton btnClear = new JButton("Clear");
        JButton btnBack = new JButton("Back");

        JPanel south = new JPanel(new BorderLayout());
        south.add(lblStatus, BorderLayout.WEST);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttons.add(btnSearch);
        buttons.add(btnClear);
        buttons.add(btnBack);
        south.add(buttons, BorderLayout.EAST);
        add(south, BorderLayout.SOUTH);

        btnSearch.addActionListener(e -> search());
        btnClear.addActionListener(e -> clear());
        btnBack.addActionListener(e -> dispose());
        // Search also when the user presses Enter in the text field
        txtKeyword.addActionListener(e -> search());
    }

    // A table model where the cells cannot be typed in
    private DefaultTableModel createModel(String[] columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private void search() {
        String type = (String) cmbType.getSelectedItem();
        if (type.equals("Notes")) {
            searchNotes();
        } else {
            searchResources();
        }
    }

    // Finds notes where the title, content or category matches the keyword
    private void searchNotes() {
        String keyword = txtKeyword.getText().trim();
        String category = (String) cmbCategory.getSelectedItem();

        String sql = "SELECT n.note_id, n.title, n.content, "
                + "COALESCE(c.category_name, '-') AS category_name, n.favorite "
                + "FROM notes n LEFT JOIN categories c ON n.category_id = c.category_id "
                + "WHERE n.user_id = ? "
                + "AND (n.title LIKE ? OR COALESCE(n.content, '') LIKE ? "
                + "     OR COALESCE(c.category_name, '') LIKE ?) "
                + "AND COALESCE(c.category_name, '') LIKE ? "
                + "ORDER BY n.title";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, Session.userId);
            ps.setString(2, "%" + keyword + "%");
            ps.setString(3, "%" + keyword + "%");
            ps.setString(4, "%" + keyword + "%");
            ps.setString(5, category.equals("All Categories") ? "%" : category);

            ResultSet rs = ps.executeQuery();

            model = createModel(new String[]{"ID", "Title", "Details", "Category", "Favorite"});
            table.setModel(model);

            int count = 0;
            while (rs.next()) {
                String content = rs.getString("content");
                if (content == null) {
                    content = "";
                }
                if (content.length() > 50) {
                    content = content.substring(0, 50) + "...";
                }
                model.addRow(new Object[]{
                    rs.getInt("note_id"),
                    rs.getString("title"),
                    content,
                    rs.getString("category_name"),
                    rs.getBoolean("favorite") ? "Yes" : "No"
                });
                count++;
            }
            lblStatus.setText("Found " + count + " note(s).");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    // Finds resources where the title, URL, description or category matches the keyword
    private void searchResources() {
        String keyword = txtKeyword.getText().trim();
        String category = (String) cmbCategory.getSelectedItem();

        String sql = "SELECT r.resource_id, r.title, r.url, "
                + "COALESCE(c.category_name, '-') AS category_name, r.favorite "
                + "FROM resources r LEFT JOIN categories c ON r.category_id = c.category_id "
                + "WHERE r.user_id = ? "
                + "AND (r.title LIKE ? OR r.url LIKE ? "
                + "     OR COALESCE(r.description, '') LIKE ? "
                + "     OR COALESCE(c.category_name, '') LIKE ?) "
                + "AND COALESCE(c.category_name, '') LIKE ? "
                + "ORDER BY r.title";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, Session.userId);
            ps.setString(2, "%" + keyword + "%");
            ps.setString(3, "%" + keyword + "%");
            ps.setString(4, "%" + keyword + "%");
            ps.setString(5, "%" + keyword + "%");
            ps.setString(6, category.equals("All Categories") ? "%" : category);

            ResultSet rs = ps.executeQuery();

            model = createModel(new String[]{"ID", "Title", "Details", "Category", "Favorite"});
            table.setModel(model);

            int count = 0;
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("resource_id"),
                    rs.getString("title"),
                    rs.getString("url"),
                    rs.getString("category_name"),
                    rs.getBoolean("favorite") ? "Yes" : "No"
                });
                count++;
            }
            lblStatus.setText("Found " + count + " resource(s).");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void clear() {
        txtKeyword.setText("");
        cmbType.setSelectedIndex(0);
        cmbCategory.setSelectedIndex(0);
        model.setRowCount(0);
        lblStatus.setText("Enter a keyword and press Search.");
    }
}
