import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class RegisterPage extends JFrame {

    private JTextField txtUsername = new JTextField();
    private JPasswordField txtPassword = new JPasswordField();
    private JTextField txtEmail = new JTextField();

    public RegisterPage() {
        setTitle("Personal Knowledge Vault - Register");
        setSize(440, 360);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 15, 30));

        JLabel title = new JLabel("Create New Account", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 18));

        JButton btnRegister = new JButton("Register");
        JButton btnBack = new JButton("Back to Login");

        panel.add(title);
        panel.add(new JLabel(" "));
        panel.add(new JLabel("Username:"));
        panel.add(txtUsername);
        panel.add(new JLabel("Password:"));
        panel.add(txtPassword);
        panel.add(new JLabel("Email:"));
        panel.add(txtEmail);
        panel.add(btnBack);
        panel.add(btnRegister);

        setLayout(new BorderLayout());
        add(panel, BorderLayout.CENTER);

        btnRegister.addActionListener(e -> register());
        btnBack.addActionListener(e -> dispose());
    }

    // Saves the new user into the users table
    private void register() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();
        String email = txtEmail.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and password are required.");
            return;
        }

        String checkSql = "SELECT user_id FROM users WHERE username = ?";
        String insertSql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {

            // Username must be unique
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setString(1, username);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    JOptionPane.showMessageDialog(this, "That username already exists.");
                    return;
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setString(1, username);
                ps.setString(2, password);
                ps.setString(3, email);
                ps.executeUpdate();
            }

            JOptionPane.showMessageDialog(this, "Registration successful! Please login.");
            txtUsername.setText("");
            txtPassword.setText("");
            txtEmail.setText("");
            dispose(); // closes this window, the login window is still behind it

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
}
