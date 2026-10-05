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
 *   new AddResource()      -> add a new resource
 *   new AddResource(id)    -> edit an existing resource
 */
public class AddResource extends JFrame {

    private int resourceId = 0; // 0 means "new resource"
    private JTextField txtTitle = new JTextField();
    private JTextField txtUrl = new JTextField();
    private JTextArea txtDescription = new JTextArea(6, 30);
    private JComboBox<String> cmbCategory = new JComboBox<>();
    private JCheckBox chkFavorite = new JCheckBox("Mark as favorite");

    public AddResource() {
        this(0);
    }

    public AddResource(int resourceId) {
        this.resourceId = resourceId;

        setTitle(resourceId == 0 ? "Add Resource" : "Edit Resource");
        setSize(540, 560);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridLayout(0, 1, 6, 6));
        form.setBorder(BorderFactory.createEmptyBorder(15, 20, 5, 20));

        DBConnection.fillCategoryBox(cmbCategory, false);

        form.add(new JLabel("Title:"));
        form.add(txtTitle);
        form.add(new JLabel("URL (website / YouTube / GitHub):"));
        form.add(txtUrl);
        form.add(new JLabel("Description:"));
        form.add(new JScrollPane(txtDescription));
        form.add(new JLabel("Category:"));
        form.add(cmbCategory);
        form.add(chkFavorite);

        add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new GridLayout(1, 2, 10, 10));
        buttons.setBorder(BorderFactory.createEmptyBorder(10, 20, 15, 20));
        JButton btnBack = new JButton("Back");
        JButton btnSave = new JButton(resourceId == 0 ? "Save Resource" : "Update Resource");
        buttons.add(btnBack);
        buttons.add(btnSave);
        add(buttons, BorderLayout.SOUTH);

        if (resourceId > 0) {
            loadResource();
        }

        btnSave.addActionListener(e -> saveResource());
        btnBack.addActionListener(e -> dispose());
    }

    // Reads the resource from the database and puts it in the form
    private void loadResource() {
        String sql = "SELECT r.title, r.url, r.description, r.favorite, c.category_name "
                + "FROM resources r LEFT JOIN categories c ON r.category_id = c.category_id "
                + "WHERE r.resource_id = ? AND r.user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, resourceId);
            ps.setInt(2, Session.userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                txtTitle.setText(rs.getString("title"));
                txtUrl.setText(rs.getString("url"));
                String description = rs.getString("description");
                txtDescription.setText(description == null ? "" : description);
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

    // Inserts a new resource, or updates it when resourceId > 0
    private void saveResource() {
        String title = txtTitle.getText().trim();
        String url = txtUrl.getText().trim();

        if (title.isEmpty() || url.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a title and a URL.");
            return;
        }

        String description = txtDescription.getText();
        int categoryId = DBConnection.getCategoryId((String) cmbCategory.getSelectedItem());
        boolean favorite = chkFavorite.isSelected();

        try (Connection conn = DBConnection.getConnection()) {

            if (resourceId == 0) {
                String sql = "INSERT INTO resources (user_id, title, url, description, category_id, favorite) "
                        + "VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, Session.userId);
                    ps.setString(2, title);
                    ps.setString(3, url);
                    ps.setString(4, description);
                    DBConnection.setCategory(ps, 5, categoryId);
                    ps.setBoolean(6, favorite);
                    ps.executeUpdate();
                }
                JOptionPane.showMessageDialog(this, "Resource saved.");

            } else {
                String sql = "UPDATE resources SET title = ?, url = ?, description = ?, "
                        + "category_id = ?, favorite = ? WHERE resource_id = ? AND user_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, title);
                    ps.setString(2, url);
                    ps.setString(3, description);
                    DBConnection.setCategory(ps, 4, categoryId);
                    ps.setBoolean(5, favorite);
                    ps.setInt(6, resourceId);
                    ps.setInt(7, Session.userId);
                    ps.executeUpdate();
                }
                JOptionPane.showMessageDialog(this, "Resource updated.");
            }

            dispose();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
}
