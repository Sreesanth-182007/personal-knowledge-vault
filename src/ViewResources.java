import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class ViewResources extends JFrame {

    private DefaultTableModel model;
    private JTable table;

    public ViewResources() {
        setTitle("My Resources");
        setSize(880, 430);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        String[] columns = {"ID", "Title", "URL", "Category", "Favorite", "Created At"};
        model = createModel(columns);
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton btnRefresh = new JButton("Refresh");
        JButton btnOpen = new JButton("Open Link");
        JButton btnEdit = new JButton("Edit");
        JButton btnDelete = new JButton("Delete");
        JButton btnFavorite = new JButton("Toggle Favorite");
        JButton btnBack = new JButton("Back");

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttons.add(btnRefresh);
        buttons.add(btnOpen);
        buttons.add(btnEdit);
        buttons.add(btnDelete);
        buttons.add(btnFavorite);
        buttons.add(btnBack);
        add(buttons, BorderLayout.SOUTH);

        btnRefresh.addActionListener(e -> loadResources());
        btnOpen.addActionListener(e -> openLink());
        btnEdit.addActionListener(e -> editResource());
        btnDelete.addActionListener(e -> deleteResource());
        btnFavorite.addActionListener(e -> toggleFavorite());
        btnBack.addActionListener(e -> dispose());

        // Reloads the table whenever the user comes back to this window
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                loadResources();
            }
        });
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

    // Shows all resources of the logged in user
    private void loadResources() {
        String sql = "SELECT r.resource_id, r.title, r.url, "
                + "COALESCE(c.category_name, '-') AS category_name, r.favorite, r.created_at "
                + "FROM resources r LEFT JOIN categories c ON r.category_id = c.category_id "
                + "WHERE r.user_id = ? ORDER BY r.created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, Session.userId);
            ResultSet rs = ps.executeQuery();

            model.setRowCount(0); // remove old rows
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("resource_id"),
                    rs.getString("title"),
                    rs.getString("url"),
                    rs.getString("category_name"),
                    rs.getBoolean("favorite") ? "Yes" : "No",
                    String.valueOf(rs.getTimestamp("created_at"))
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    // Returns the id of the selected row, or -1 when nothing is selected
    private int getSelectedId() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a resource first.");
            return -1;
        }
        return Integer.parseInt(model.getValueAt(row, 0).toString());
    }

    private void openLink() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a resource first.");
            return;
        }
        String url = model.getValueAt(row, 2).toString(); // URL column
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Cannot open link: " + e.getMessage());
        }
    }

    private void editResource() {
        int id = getSelectedId();
        if (id > 0) {
            // This window stays open, so the table refreshes when it is used again
            new AddResource(id).setVisible(true);
        }
    }

    private void deleteResource() {
        int id = getSelectedId();
        if (id < 0) {
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this, "Delete this resource?", "Confirm",
                JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        String sql = "DELETE FROM resources WHERE resource_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setInt(2, Session.userId);
            ps.executeUpdate();
            loadResources();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void toggleFavorite() {
        int id = getSelectedId();
        if (id < 0) {
            return;
        }

        String sql = "UPDATE resources SET favorite = NOT favorite WHERE resource_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setInt(2, Session.userId);
            ps.executeUpdate();
            loadResources();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
}
