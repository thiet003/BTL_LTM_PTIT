package Client.views;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class RoomView extends JFrame {
    private JLabel roomIdLabel;
    private JList<String> playersList;
    private JTextArea chatArea;
    private JTextField chatInputField;
    private JButton sendChatButton;
    private JButton startGameButton; // Chỉ hiện khi người chơi là chủ phòng

    public RoomView(String roomId, boolean isHost) {
        setTitle("Phòng chơi: " + roomId);
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Bảng ID phòng
        roomIdLabel = new JLabel("ID Phòng: " + roomId);
        add(roomIdLabel, BorderLayout.NORTH);

        // Cột bên trái: Danh sách người chơi
        playersList = new JList<>();
        JScrollPane playersScrollPane = new JScrollPane(playersList);
        playersScrollPane.setPreferredSize(new Dimension(150, 400));
        add(playersScrollPane, BorderLayout.WEST);

        // Bên phải: Khung để chơi (chỉ là khung placeholder)
        JPanel gamePanel = new JPanel();
        gamePanel.setBackground(Color.LIGHT_GRAY);
        gamePanel.setPreferredSize(new Dimension(300, 400));
        add(gamePanel, BorderLayout.EAST);

        // Phần giữa: Khu vực chat
        JPanel chatPanel = new JPanel();
        chatPanel.setLayout(new BorderLayout());

        // Hiển thị tin nhắn
        chatArea = new JTextArea();
        chatArea.setEditable(false); // Không cho phép người dùng chỉnh sửa
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        chatPanel.add(chatScrollPane, BorderLayout.CENTER);

        // Khung nhập tin nhắn
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());
        chatInputField = new JTextField();
        sendChatButton = new JButton("Gửi");
        inputPanel.add(chatInputField, BorderLayout.CENTER);
        inputPanel.add(sendChatButton, BorderLayout.EAST);
        chatPanel.add(inputPanel, BorderLayout.SOUTH);

        add(chatPanel, BorderLayout.CENTER);

        // Nút bắt đầu trò chơi (chỉ hiện với người chủ trì)
        if (isHost) {
            startGameButton = new JButton("Bắt đầu trò chơi");
            add(startGameButton, BorderLayout.SOUTH);
        }
    }

    // Phương thức cập nhật danh sách người chơi
    public void updatePlayersList(String[] players) {
        playersList.setListData(players);
    }

    // Phương thức thêm tin nhắn vào khu vực chat
    public void appendChatMessage(String message) {
        chatArea.append(message + "\n");
    }

    // Getters cho các thành phần
    public JTextField getChatInputField() {
        return chatInputField;
    }

    public JButton getSendChatButton() {
        return sendChatButton;
    }

    public JButton getStartGameButton() {
        return startGameButton;
    }

    // Đăng ký các listener
    public void addSendChatListener(ActionListener listener) {
        sendChatButton.addActionListener(listener);
    }

    public void addStartGameListener(ActionListener listener) {
        if (startGameButton != null) {
            startGameButton.addActionListener(listener);
        }
    }

    public JLabel getRoomIdLabel() {
        return roomIdLabel;
    }

    public void setRoomIdLabel(JLabel roomIdLabel) {
        this.roomIdLabel = roomIdLabel;
    }
    
}
