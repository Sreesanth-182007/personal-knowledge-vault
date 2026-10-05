import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

// Shows all notes and resources that were marked as favorite
public class FavoritesPage extends JFrame {

    private JComboBox<String> cmbType = new JComboBox<>(new String[]{"Notes", "Resources"});
    private DefaultTableModel model;
    private JTable table;

    public FavoritesPage() {
        setTitle("My Favorites");
        setSize(820, 450);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        model = createModel(new String[]{"ID", "Title", "Category", "Created At"});
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton btnShow = new JButton("Show Favorites");
        JButton btnRemove = new JButton("Remove Favorite");
        JButton btnBack = new JButton("Back");

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        top.add(new JLabel("Show:"));
        top.add(cmbType);
        add(top, BorderLayout.NORTH);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttons.add(btnShow);
        buttons.add(btnRemove);
        buttons.add(btnBack);
        add(buttons, BorderLayout.SOUTH);

        btnShow.addActionListener(e -> loadFavorites());
        btnRemove.addActionListener(e -> removeFavorite());
        btnBack.addActionListener(e -> dispose());
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

    private void loadFavorites() {
        if (cmbType.getSelectedIndex() == 0) {
            loadFavoriteNotes();
        } else {
            loadFavoriteResources();
        }
    }

    private void loadFavoriteNotes() {
        String sql = "SELECT n.note_id, n.title, "
                + "COALESCE(c.category_name, '-') AS category_name, n.created_at "
                + "FROM notes n LEFT JOIN categories c ON n.category_id = c.category_id "
                + "WHERE n.user_id = ? AND n.favorite = 1 "
                + "ORDER BY n.created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, Session.userId);
            ResultSet rs = ps.executeQuery();

            model = createModel(new String[]{"ID", "Title", "Category", "Created At"});
            table.setModel(model);

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("note_id"),
                    rs.getString("title"),
                    rs.getString("category_name"),
                    String.valueOf(rs.getTimestamp("created_at"))
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void loadFavoriteResources() {
        String sql = "SELECT r.resource_id, r.title, "
                + "COALESCE(c.category_name, '-') AS category_name, r.created_at "
                + "FROM resources r LEFT JOIN categories c ON r.category_id = c.category_id "
                + "WHERE r.user_id = ? AND r.favorite = 1 "
                + "ORDER BY r.created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, Session.userId);
            ResultSet rs = ps.executeQuery();

            model = createModel(new String[]{"ID", "Title", "Category", "Created At"});
            table.setModel(model);

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("resource_id"),
                    rs.getString("title"),
                    rs.getString("category_name"),
                    String.valueOf(rs.getTimestamp("created_at"))
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private int getSelectedId() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a row first.");
            return -1;
        }
        return Integer.parseInt(model.getValueAt(row, 0).toString());
    }

    private void removeFavorite() {
        int id = getSelectedId();
        if (id < 0) {
            return;
        }

        String sql;
        if (cmbType.getSelectedIndex() == 0) {
            sql = "UPDATE notes SET favorite = 0 WHERE note_id = ? AND user_id = ?";
        } else {
            sql = "UPDATE resources SET favorite = 0 WHERE resource_id = ? AND user_id = ?";
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setInt(2, Session.userId);
            ps.executeUpdate();
            loadFavorites();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
}
