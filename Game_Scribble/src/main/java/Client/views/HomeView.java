/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Client.views;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class HomeView extends JFrame {
    private JLabel usernameLabel;
    private JLabel avatarLabel;
    private JButton createRoomButton;
    private JButton joinRoomButton;
    private JButton logoutButton;
    private JTextArea chatArea;
    private JTextField chatInputField;
    private JButton sendChatButton;

    public HomeView(String username, String avatarPath) {
        initComponents(username, avatarPath);
    }

    private void initComponents(String username, String avatarPath) {
        setTitle("Trang chủ");
        setSize(1000, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.BOTH;

        // Logout Button
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        logoutButton = new JButton("Thoát");
        add(logoutButton, gbc);

        gbc.anchor = GridBagConstraints.CENTER;

        // Avatar Label
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridheight = 2;
        try {
            Image image = ImageIO.read(new File("avatars/" + avatarPath));
            Image scaledImage = image.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            avatarLabel = new JLabel(new ImageIcon(scaledImage));
        } catch (IOException e) {
            e.printStackTrace();
            avatarLabel = new JLabel();
        }
        avatarLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        add(avatarLabel, gbc);

        // Username Label
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridheight = 1;
        usernameLabel = new JLabel("Xin chào, " + username + "!");
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 24));
        add(usernameLabel, gbc);

        // Create Room Button
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        createRoomButton = new JButton("Tạo phòng");
        createRoomButton.setPreferredSize(new Dimension(200, 50));
        add(createRoomButton, gbc);

        // Join Room Button
        gbc.gridy = 4;
        joinRoomButton = new JButton("Tham gia phòng");
        joinRoomButton.setPreferredSize(new Dimension(200, 50));
        add(joinRoomButton, gbc);

        // Chat Area
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 3;
        gbc.gridheight = 4;
        chatArea = new JTextArea(15, 50);
        chatArea.setEditable(false);
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        add(chatScrollPane, gbc);

        // Chat Input Field and Send Button
        gbc.gridy = 9;
        gbc.gridheight = 1;
        gbc.gridwidth = 2;
        chatInputField = new JTextField(40);
        add(chatInputField, gbc);

        gbc.gridx = 2;
        sendChatButton = new JButton("Gửi");
        add(sendChatButton, gbc);
    }

    public JButton getCreateRoomButton() {
        return createRoomButton;
    }

    public JButton getJoinRoomButton() {
        return joinRoomButton;
    }

    public JButton getLogoutButton() {
        return logoutButton;
    }

    public JTextField getChatInputField() {
        return chatInputField;
    }

    public JButton getSendChatButton() {
        return sendChatButton;
    }

    public JTextArea getChatArea() {
        return chatArea;
    }

    public void addWindowCloseListener(java.awt.event.WindowAdapter adapter) {
        addWindowListener(adapter);
    }
}

