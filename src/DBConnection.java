import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;

/**
 * Connects this program to the MySQL database.
 *
 * IMPORTANT: change DB_USER and DB_PASSWORD below if your MySQL
 * username / password is different.
 */
public class DBConnection {

    // ===== CHANGE THESE 3 LINES FOR YOUR MySQL SETUP =====
    private static final String DB_URL =
            "jdbc:mysql://localhost:3306/personal_knowledge_vault"
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root";
    // ====================================================

    // Every page of the program calls this method to get a connection
    public static Connection getConnection() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    // Small helper: loads all category names into a combo box
    public static void fillCategoryBox(JComboBox<String> box, boolean addAllOption) {
        box.removeAllItems();
        if (addAllOption) {
            box.addItem("All Categories");
        }
        String sql = "SELECT category_name FROM categories ORDER BY category_name";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                box.addItem(rs.getString("category_name"));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Could not load categories:\n" + e.getMessage());
        }
    }

    // Returns the id of a category, or -1 when it does not exist
    public static int getCategoryId(String categoryName) {
        if (categoryName == null || categoryName.equals("All Categories")) {
            return -1;
        }
        String sql = "SELECT category_id FROM categories WHERE category_name = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, categoryName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("category_id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    // Writes a category id into a PreparedStatement,
    // or NULL when the user selected no category
    public static void setCategory(PreparedStatement ps, int index, int categoryId)
            throws Exception {
        if (categoryId > 0) {
            ps.setInt(index, categoryId);
        } else {
            ps.setNull(index, Types.INTEGER);
        }
    }
}
