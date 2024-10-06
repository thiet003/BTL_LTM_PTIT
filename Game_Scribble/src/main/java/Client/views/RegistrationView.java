package Client.views;

// View
import javax.swing.*;
import java.awt.*;
import java.io.File;
import javax.imageio.ImageIO;
import java.io.IOException;

public class RegistrationView extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField retypePasswordField;
    private JComboBox<ImageIcon> avatarComboBox;
    private JButton registerButton;
    private JButton loginButton;

    public RegistrationView() {
        initComponents();
    }

    private void initComponents() {
        setTitle("Đăng ký");
        setSize(600, 600);
        setLocationRelativeTo(null); // Căn giữa màn hình
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title Label
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JLabel titleLabel = new JLabel("Đăng ký", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;

        // Username Label and Field
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(new JLabel("Tên đăng nhập:"), gbc);

        gbc.gridx = 1;
        usernameField = new JTextField(15);
        add(usernameField, gbc);

        // Password Label and Field
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(new JLabel("Mật khẩu:"), gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        add(passwordField, gbc);

        // Retype Password Label and Field
        gbc.gridx = 0;
        gbc.gridy = 3;
        add(new JLabel("Nhập lại mật khẩu:"), gbc);

        gbc.gridx = 1;
        retypePasswordField = new JPasswordField(15);
        add(retypePasswordField, gbc);

        // Avatar Label and ComboBox
        gbc.gridx = 0;
        gbc.gridy = 4;
        add(new JLabel("Ảnh đại diện:"), gbc);

        gbc.gridx = 1;
        avatarComboBox = new JComboBox<>();
        loadAvatars();
        add(avatarComboBox, gbc);

        // Register Button
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.CENTER;
        registerButton = new JButton("Đăng ký");
        add(registerButton, gbc);

        // Already have an account label and Login Button
        gbc.gridx = 0;
        gbc.gridy = 6;
        add(new JLabel("Bạn đã có tài khoản?"), gbc);

        gbc.gridx = 1;
        loginButton = new JButton("Đăng nhập");
        add(loginButton, gbc);
    }

    private void loadAvatars() {
        File avatarFolder = new File("avatars");
        if (avatarFolder.exists() && avatarFolder.isDirectory()) {
            File[] avatarFiles = avatarFolder.listFiles();
            if (avatarFiles != null) {
                for (File avatarFile : avatarFiles) {
                    if (avatarFile.isFile()) {
                        try {
                            Image image = ImageIO.read(avatarFile);
                            Image scaledImage = image.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
                            ImageIcon avatarIcon = new ImageIcon(scaledImage);
                            avatarIcon.setDescription(avatarFile.getName());
                            avatarComboBox.addItem(avatarIcon);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public String getUsername() {
        return usernameField.getText();
    }

    public String getPassword() {
        return new String(passwordField.getPassword());
    }

    public String getRetypePassword() {
        return new String(retypePasswordField.getPassword());
    }

    public ImageIcon getAvatar() {
        return (ImageIcon) avatarComboBox.getSelectedItem();
    }

    public JButton getRegisterButton() {
        return registerButton;
    }

    public JButton getLoginButton() {
        return loginButton;
    }

    public void addWindowCloseListener(java.awt.event.WindowAdapter adapter) {
        addWindowListener(adapter);
    }
}