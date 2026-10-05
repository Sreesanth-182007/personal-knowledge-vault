import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * Used for two jobs:
 *   new AddNote()      -> add a new note
 *   new AddNote(id)    -> edit an existing note
 */
public class AddNote extends JFrame {

    private int noteId = 0; // 0 means "new note"
    private JTextField txtTitle = new JTextField();
    private JTextArea txtContent = new JTextArea(8, 30);
    private JComboBox<String> cmbCategory = new JComboBox<>();
    private JCheckBox chkFavorite = new JCheckBox("Mark as favorite");

    public AddNote() {
        this(0);
    }

    public AddNote(int noteId) {
        this.noteId = noteId;

        setTitle(noteId == 0 ? "Add Note" : "Edit Note");
        setSize(520, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridLayout(0, 1, 6, 6));
        form.setBorder(BorderFactory.createEmptyBorder(15, 20, 5, 20));

        DBConnection.fillCategoryBox(cmbCategory, false);

        form.add(new JLabel("Title:"));
        form.add(txtTitle);
        form.add(new JLabel("Content:"));
        form.add(new JScrollPane(txtContent));
        form.add(new JLabel("Category:"));
        form.add(cmbCategory);
        form.add(chkFavorite);

        add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new GridLayout(1, 2, 10, 10));
        buttons.setBorder(BorderFactory.createEmptyBorder(10, 20, 15, 20));
        JButton btnBack = new JButton("Back");
        JButton btnSave = new JButton(noteId == 0 ? "Save Note" : "Update Note");
        buttons.add(btnBack);
        buttons.add(btnSave);
        add(buttons, BorderLayout.SOUTH);

        if (noteId > 0) {
            loadNote();
        }

        btnSave.addActionListener(e -> saveNote());
        btnBack.addActionListener(e -> dispose());
    }

    // Reads the note from the database and puts it in the form
    private void loadNote() {
        String sql = "SELECT n.title, n.content, n.favorite, c.category_name "
                + "FROM notes n LEFT JOIN categories c ON n.category_id = c.category_id "
                + "WHERE n.note_id = ? AND n.user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, noteId);
            ps.setInt(2, Session.userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                txtTitle.setText(rs.getString("title"));
                String content = rs.getString("content");
                txtContent.setText(content == null ? "" : content);
                chkFavorite.setSelected(rs.getBoolean("favorite"));

                String category = rs.getString("category_name");
                if (category != null) {
                    cmbCategory.setSelectedItem(category);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    // Inserts a new note, or updates the note when noteId > 0
    private void saveNote() {
        String title = txtTitle.getText().trim();
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a title.");
            return;
        }

        String content = txtContent.getText();
        int categoryId = DBConnection.getCategoryId((String) cmbCategory.getSelectedItem());
        boolean favorite = chkFavorite.isSelected();

        try (Connection conn = DBConnection.getConnection()) {

            if (noteId == 0) {
                String sql = "INSERT INTO notes (user_id, title, content, category_id, favorite) "
                        + "VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, Session.userId);
                    ps.setString(2, title);
                    ps.setString(3, content);
                    DBConnection.setCategory(ps, 4, categoryId);
                    ps.setBoolean(5, favorite);
                    ps.executeUpdate();
                }
                JOptionPane.showMessageDialog(this, "Note saved.");

            } else {
                String sql = "UPDATE notes SET title = ?, content = ?, category_id = ?, favorite = ? "
                        + "WHERE note_id = ? AND user_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, title);
                    ps.setString(2, content);
                    DBConnection.setCategory(ps, 3, categoryId);
                    ps.setBoolean(4, favorite);
                    ps.setInt(5, noteId);
                    ps.setInt(6, Session.userId);
                    ps.executeUpdate();
                }
                JOptionPane.showMessageDialog(this, "Note updated.");
            }

            dispose();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
}
