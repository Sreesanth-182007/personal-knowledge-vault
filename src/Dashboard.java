import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class Dashboard extends JFrame {

    public Dashboard() {
        setTitle("Personal Knowledge Vault - Dashboard");
        setSize(720, 360);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JLabel lblWelcome = new JLabel("Welcome, " + Session.username + " !", SwingConstants.CENTER);
        lblWelcome.setFont(new Font("Arial", Font.BOLD, 22));
        lblWelcome.setBorder(BorderFactory.createEmptyBorder(25, 10, 10, 10));
        add(lblWelcome, BorderLayout.NORTH);

        // 8 buttons in 2 rows and 4 columns
        JPanel menu = new JPanel(new GridLayout(2, 4, 15, 15));
        menu.setBorder(BorderFactory.createEmptyBorder(10, 25, 25, 25));

        String[] labels = {"Add Note", "View Notes", "Add Resource", "View Resources",
                "Search", "Favorites", "Categories", "Logout"};

        for (String label : labels) {
            JButton button = new JButton(label);
            button.setFont(new Font("Arial", Font.BOLD, 14));
            button.setFocusPainted(false);
            button.addActionListener(e -> openPage(label));
            menu.add(button);
        }

        add(menu, BorderLayout.CENTER);
    }

    // Opens the page of the button that was clicked
    private void openPage(String label) {
        switch (label) {
            case "Add Note":
                new AddNote().setVisible(true);
                break;
            case "View Notes":
                new ViewNotes().setVisible(true);
                break;
            case "Add Resource":
                new AddResource().setVisible(true);
                break;
            case "View Resources":
                new ViewResources().setVisible(true);
                break;
            case "Search":
                new SearchPage().setVisible(true);
                break;
            case "Favorites":
                new FavoritesPage().setVisible(true);
                break;
            case "Categories":
                new CategoriesPage().setVisible(true);
                break;
            case "Logout":
                Session.userId = 0;
                Session.username = "";
                new LoginPage().setVisible(true);
                dispose();
                break;
        }
    }
}
