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

public class LoginPage extends JFrame {

    private JTextField txtUsername = new JTextField();
    private JPasswordField txtPassword = new JPasswordField();

    public LoginPage() {
        setTitle("Personal Knowledge Vault - Login");
        setSize(420, 340);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(0, 1, 8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 35, 15, 35));

        JLabel title = new JLabel("Personal Knowledge Vault", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 18));

        JButton btnLogin = new JButton("Login");
        JButton btnRegister = new JButton("Register");

        panel.add(title);
        panel.add(new JLabel(" "));
        panel.add(new JLabel("Username:"));
        panel.add(txtUsername);
        panel.add(new JLabel("Password:"));
        panel.add(txtPassword);
        panel.add(btnLogin);
        panel.add(btnRegister);

        setLayout(new BorderLayout());
        add(panel, BorderLayout.CENTER);

        btnLogin.addActionListener(e -> login());
        btnRegister.addActionListener(e -> {
            // The login window stays open behind the register window
            new RegisterPage().setVisible(true);
        });
    }

    // Checks the username and password in the users table
    private void login() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter username and password.");
            return;
        }

        String sql = "SELECT user_id FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Session.userId = rs.getInt("user_id");
                Session.username = username;
                JOptionPane.showMessageDialog(this, "Login successful!");
                new Dashboard().setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Wrong username or password.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
}
