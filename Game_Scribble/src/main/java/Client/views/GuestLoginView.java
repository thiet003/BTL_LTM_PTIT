package Client.views;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import javax.imageio.ImageIO;
import java.io.IOException;

public class GuestLoginView extends JFrame {
    private JTextField nicknameField;
    private JComboBox<ImageIcon> avatarComboBox;
    private JButton playButton;

    public GuestLoginView() {
        initComponents();
    }

    private void initComponents() {
        setTitle("Guess the Sketch");
        setSize(500, 500);
        setLocationRelativeTo(null); // Center the window
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
        JLabel titleLabel = new JLabel("Guess the Sketch", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(new Color(34, 139, 230)); // Set a nice color for the title
        add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;

        // Nickname Label and Field
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel nicknameLabel = new JLabel("Biệt danh:");
        nicknameLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        add(nicknameLabel, gbc);

        gbc.gridx = 1;
        nicknameField = new JTextField(15);
        nicknameField.setFont(new Font("Arial", Font.PLAIN, 16));
        add(nicknameField, gbc);

        // Avatar Label and ComboBox
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel avatarLabel = new JLabel("Ảnh đại diện:");
        avatarLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        add(avatarLabel, gbc);

        gbc.gridx = 1;
        avatarComboBox = new JComboBox<>();
        loadAvatars();
        add(avatarComboBox, gbc);

        // Play Button
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        playButton = new JButton("Chơi");
        playButton.setFont(new Font("Arial", Font.BOLD, 18));
        playButton.setBackground(new Color(50, 205, 50));
        playButton.setForeground(Color.WHITE);
        playButton.setFocusPainted(false);
        add(playButton, gbc);
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

    public String getNickname() {
        return nicknameField.getText();
    }

    public ImageIcon getAvatar() {
        return (ImageIcon) avatarComboBox.getSelectedItem();
    }

    public JButton getPlayButton() {
        return playButton;
    }

    public void addWindowCloseListener(java.awt.event.WindowAdapter adapter) {
        addWindowListener(adapter);
    }
}
