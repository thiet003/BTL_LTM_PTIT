package Client.views;

import Server.model.Player;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class RoomView extends JFrame {
    private JLabel roomIdLabel;
    private JTextArea chatArea;
    private JTextField chatInputField;
    private JTextField guessInputField; // Ô nhập để đoán từ
    private JButton sendChatButton;
    private JButton sendGuessButton; // Nút gửi đoán từ
    private JButton startGameButton; // Chỉ hiện khi người chơi là chủ phòng
    private JList<Player> playersList;
    private DefaultListModel<Player> playersListModel;
    private Color currentColor = Color.BLACK; // Màu vẽ hiện tại
    private DrawingPanel drawingPanel; // Vùng trắng để vẽ
    private boolean isEraserMode = false; // Trạng thái tẩy

    public RoomView(String roomId, boolean isHost) {
        setTitle("Phòng chơi: " + roomId);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Mở rộng cửa sổ tối đa
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Bảng ID phòng
        roomIdLabel = new JLabel("ID Phòng: " + roomId);
        add(roomIdLabel, BorderLayout.NORTH);

        // Cột bên trái: Danh sách người chơi
        // Tạo model cho danh sách người chơi
        playersListModel = new DefaultListModel<>();
        playersList = new JList<>(playersListModel);
        playersList.setCellRenderer(new PlayerListCellRenderer());
        JScrollPane playersScrollPane = new JScrollPane(playersList);
        playersScrollPane.setPreferredSize(new Dimension(200, 600));
        add(playersScrollPane, BorderLayout.WEST);

        // Bên phải: Vùng trắng để vẽ
        drawingPanel = new DrawingPanel();
        drawingPanel.setBackground(Color.WHITE);
        drawingPanel.setPreferredSize(new Dimension(600, 600));
        add(drawingPanel, BorderLayout.EAST);

        // Phần giữa: Khu vực chat và đoán từ
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(2, 1));

        // Khu vực chat
        JPanel chatPanel = new JPanel();
        chatPanel.setLayout(new BorderLayout());

        // Hiển thị tin nhắn
        chatArea = new JTextArea();
        chatArea.setEditable(false); // Không cho phép người dùng chỉnh sửa
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        chatScrollPane.setPreferredSize(new Dimension(400, 200));
        chatPanel.add(chatScrollPane, BorderLayout.CENTER);

        // Khung nhập tin nhắn
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());
        chatInputField = new JTextField(20);
        sendChatButton = new JButton("Gửi");
        inputPanel.add(chatInputField, BorderLayout.CENTER);
        inputPanel.add(sendChatButton, BorderLayout.EAST);
        chatPanel.add(inputPanel, BorderLayout.SOUTH);

        centerPanel.add(chatPanel);

        // Khu vực đoán từ
        JPanel guessPanel = new JPanel();
        guessPanel.setLayout(new BorderLayout());
        JLabel guessLabel = new JLabel("Đoán từ: ");
        guessInputField = new JTextField(20);
        sendGuessButton = new JButton("Đoán");
        JPanel guessInputPanel = new JPanel(new BorderLayout());
        guessInputPanel.add(guessLabel, BorderLayout.WEST);
        guessInputPanel.add(guessInputField, BorderLayout.CENTER);
        guessInputPanel.add(sendGuessButton, BorderLayout.EAST);
        guessPanel.add(guessInputPanel, BorderLayout.SOUTH);

        centerPanel.add(guessPanel);

        add(centerPanel, BorderLayout.CENTER);

        // Nút bắt đầu trò chơi (chỉ hiện với người chủ trì)
        if (isHost) {
            startGameButton = new JButton("Bắt đầu trò chơi");
            add(startGameButton, BorderLayout.SOUTH);
        }

        // Bảng chọn màu và tẩy
        JPanel toolsPanel = new JPanel();
        toolsPanel.setLayout(new FlowLayout());

            JButton colorPickerButton = new JButton("Chọn màu");
        colorPickerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Color selectedColor = JColorChooser.showDialog(null, "Chọn màu vẽ", currentColor);
                if (selectedColor != null) {
                    currentColor = selectedColor;
                    isEraserMode = false; // Chuyển sang chế độ vẽ
                }
            }
        });

        JButton eraserButton = new JButton("Tẩy");
        eraserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentColor = Color.WHITE;
                isEraserMode = true; // Chuyển sang chế độ tẩy
            }
        });

        JButton drawButton = new JButton("Vẽ");
        drawButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentColor = Color.BLACK; // Đặt lại màu vẽ thành màu đen
                isEraserMode = false; // Chuyển sang chế độ vẽ
            }
        });

        toolsPanel.add(colorPickerButton);
        toolsPanel.add(eraserButton);
        toolsPanel.add(drawButton);
        add(toolsPanel, BorderLayout.NORTH);
    }

    // Phương thức cập nhật danh sách người chơi
    public void updatePlayersList(ArrayList<Player> players) {
        playersListModel.clear();
        for (Player player : players) {
            playersListModel.addElement(player);
        }
    }

    public class DrawingPanel extends JPanel {
        private DrawingListener drawingListener;
        private int prevX, prevY;

        public DrawingPanel() {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    prevX = e.getX();
                    prevY = e.getY();
                }
            });

            addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    int x = e.getX();
                    int y = e.getY();
                    drawLine(prevX, prevY, x, y, currentColor.getRGB());
                    if (drawingListener != null) {
                        drawingListener.onDraw(prevX, prevY, x, y, currentColor.getRGB());
                    }
                    prevX = x;
                    prevY = y;
                }
            });
        }

        public void drawLine(int x1, int y1, int x2, int y2, int colorValue) {
            Graphics2D g = (Graphics2D) getGraphics();
            g.setColor(new Color(colorValue));
            g.setStroke(new BasicStroke(12));
            g.drawLine(x1, y1, x2, y2);
        }

        public void addDrawingListener(DrawingListener listener) {
            this.drawingListener = listener;
        }
    }


    // Phương thức thêm tin nhắn vào khu vực chat
    public void appendChatMessage(String message) {
        chatArea.append(message + "\n");
    }

    public DrawingPanel getDrawingPanel() {
        return drawingPanel;
    }

    // Getters cho các thành phần
    public JTextField getChatInputField() {
        return chatInputField;
    }

    public JButton getSendChatButton() {
        return sendChatButton;
    }

    public JButton getSendGuessButton() {
        return sendGuessButton;
    }

    public JTextField getGuessInputField() {
        return guessInputField;
    }

    public JButton getStartGameButton() {
        return startGameButton;
    }

    public JLabel getRoomIdLabel() {
        return roomIdLabel;
    }

    // Đăng ký các listener
    public void addSendChatListener(ActionListener listener) {
        sendChatButton.addActionListener(listener);
    }

    public void addSendGuessListener(ActionListener listener) {
        sendGuessButton.addActionListener(listener);
    }

    public void addStartGameListener(ActionListener listener) {
        if (startGameButton != null) {
            startGameButton.addActionListener(listener);
        }
    }

    // Phương thức bắt đầu trò chơi
    public void startGame() {
        drawingPanel.setVisible(true);
    }

    public interface DrawingListener {
        void onDraw(int x1, int y1, int x2, int y2, int colorValue);
    }
}
