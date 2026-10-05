import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

// Create and delete the categories used by notes and resources
public class CategoriesPage extends JFrame {

    private JTextField txtName = new JTextField(15);
    private DefaultTableModel model;
    private JTable table;

    public CategoriesPage() {
        setTitle("Categories");
        setSize(500, 430);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        model = createModel(new String[]{"ID", "Category Name"});
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton btnAdd = new JButton("Add Category");
        JButton btnDelete = new JButton("Delete Selected");
        JButton btnBack = new JButton("Back");

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        top.add(new JLabel("Category name:"));
        top.add(txtName);
        top.add(btnAdd);
        add(top, BorderLayout.NORTH);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttons.add(btnDelete);
        buttons.add(btnBack);
        add(buttons, BorderLayout.SOUTH);

        btnAdd.addActionListener(e -> addCategory());
        btnDelete.addActionListener(e -> deleteCategory());
        btnBack.addActionListener(e -> dispose());

        loadCategories();
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

    private void loadCategories() {
        String sql = "SELECT category_id, category_name FROM categories ORDER BY category_name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            model.setRowCount(0);
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("category_id"),
                    rs.getString("category_name")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void addCategory() {
        String name = txtName.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a category name.");
            return;
        }

        String checkSql = "SELECT category_id FROM categories WHERE category_name = ?";
        String insertSql = "INSERT INTO categories (category_name) VALUES (?)";

        try (Connection conn = DBConnection.getConnection()) {

            // Category names must be unique
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setString(1, name);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    JOptionPane.showMessageDialog(this, "That category already exists.");
                    return;
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setString(1, name);
                ps.executeUpdate();
            }

            txtName.setText("");
            loadCategories();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void deleteCategory() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a category first.");
            return;
        }

        int id = Integer.parseInt(model.getValueAt(row, 0).toString());
        int choice = JOptionPane.showConfirmDialog(this, "Delete this category?", "Confirm",
                JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        String sql = "DELETE FROM categories WHERE category_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
            loadCategories();

        } catch (Exception e) {
            // The foreign key stops the delete while notes / resources use it
            JOptionPane.showMessageDialog(this,
                    "Cannot delete this category.\n"
                    + "Some notes or resources are still using it.");
        }
    }
}
