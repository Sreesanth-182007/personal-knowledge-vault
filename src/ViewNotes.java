import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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

public class ViewNotes extends JFrame {

    private DefaultTableModel model;
    private JTable table;

    public ViewNotes() {
        setTitle("My Notes");
        setSize(820, 430);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        String[] columns = {"ID", "Title", "Content", "Category", "Favorite", "Created At"};
        model = createModel(columns);
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton btnRefresh = new JButton("Refresh");
        JButton btnEdit = new JButton("Edit");
        JButton btnDelete = new JButton("Delete");
        JButton btnFavorite = new JButton("Toggle Favorite");
        JButton btnBack = new JButton("Back");

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttons.add(btnRefresh);
        buttons.add(btnEdit);
        buttons.add(btnDelete);
        buttons.add(btnFavorite);
        buttons.add(btnBack);
        add(buttons, BorderLayout.SOUTH);

        btnRefresh.addActionListener(e -> loadNotes());
        btnEdit.addActionListener(e -> editNote());
        btnDelete.addActionListener(e -> deleteNote());
        btnFavorite.addActionListener(e -> toggleFavorite());
        btnBack.addActionListener(e -> dispose());

        // Reloads the table whenever the user comes back to this window
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                loadNotes();
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

    // Shows all notes of the logged in user
    private void loadNotes() {
        String sql = "SELECT n.note_id, n.title, n.content, "
                + "COALESCE(c.category_name, '-') AS category_name, n.favorite, n.created_at "
                + "FROM notes n LEFT JOIN categories c ON n.category_id = c.category_id "
                + "WHERE n.user_id = ? ORDER BY n.created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, Session.userId);
            ResultSet rs = ps.executeQuery();

            model.setRowCount(0); // remove old rows
            while (rs.next()) {
                String content = rs.getString("content");
                if (content == null) {
                    content = "";
                }
                if (content.length() > 40) {
                    content = content.substring(0, 40) + "...";
                }
                model.addRow(new Object[]{
                    rs.getInt("note_id"),
                    rs.getString("title"),
                    content,
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
            JOptionPane.showMessageDialog(this, "Please select a note first.");
            return -1;
        }
        return Integer.parseInt(model.getValueAt(row, 0).toString());
    }

    private void editNote() {
        int id = getSelectedId();
        if (id > 0) {
            // This window stays open, so the table refreshes when it is used again
            new AddNote(id).setVisible(true);
        }
    }

    private void deleteNote() {
        int id = getSelectedId();
        if (id < 0) {
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this, "Delete this note?", "Confirm",
                JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        String sql = "DELETE FROM notes WHERE note_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setInt(2, Session.userId);
            ps.executeUpdate();
            loadNotes();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void toggleFavorite() {
        int id = getSelectedId();
        if (id < 0) {
            return;
        }

        String sql = "UPDATE notes SET favorite = NOT favorite WHERE note_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setInt(2, Session.userId);
            ps.executeUpdate();
            loadNotes();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
}
