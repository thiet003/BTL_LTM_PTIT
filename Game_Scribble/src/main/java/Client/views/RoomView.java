package Client.views;

import Server.model.Player;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class RoomView extends JFrame {
    private JLabel roomIdLabel;
    private JTextArea chatArea;
    private JTextPane guessArea;
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
    private JLabel timerLabel; // Thêm label hiển thị thời gian đếm ngược
    private JButton drawButton;
    private JButton eraserButton;
    private JButton colorPickerButton;
    public RoomView(String roomId, boolean isHost) {
        setTitle("Phòng chơi: " + roomId);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Mở rộng cửa sổ tối đa
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Tạo một panel chính để sắp xếp các thành phần
        JPanel mainPanel = new JPanel(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);

        // Bảng ID phòng và thời gian đếm ngược ở trên cùng
        JPanel topPanel = new JPanel(new BorderLayout());
        roomIdLabel = new JLabel("ID Phòng: " + roomId, SwingConstants.CENTER);
        roomIdLabel.setFont(new Font("Arial", Font.BOLD, 16));
        topPanel.add(roomIdLabel, BorderLayout.WEST);

// Panel chứa nút "Copy mã" và nhãn thời gian đếm ngược
        JPanel copyAndTimerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

// Nút "Copy mã"
        JButton copyButton = new JButton("Copy mã phòng");
        copyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                StringSelection stringSelection = new StringSelection(roomId);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(stringSelection, null);
                JOptionPane.showMessageDialog(null, "Mã phòng đã được sao chép vào clipboard!");
            }
        });
        copyAndTimerPanel.add(copyButton);

// Thêm label hiển thị thời gian đếm ngược vào `copyAndTimerPanel` thay vì `topPanel`
        timerLabel = new JLabel("Thời gian: 00:00", SwingConstants.CENTER);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        copyAndTimerPanel.add(timerLabel);

// Thêm copyAndTimerPanel vào bên phải của topPanel
        topPanel.add(copyAndTimerPanel, BorderLayout.EAST);
        mainPanel.add(topPanel, BorderLayout.NORTH);
        // Cột bên trái: Danh sách người chơi
        playersListModel = new DefaultListModel<>();
        playersList = new JList<>(playersListModel);
        playersList.setCellRenderer(new PlayerListCellRenderer());
        JScrollPane playersScrollPane = new JScrollPane(playersList);
        playersScrollPane.setPreferredSize(new Dimension(200, 600));
        mainPanel.add(playersScrollPane, BorderLayout.WEST);

        // Bên phải: Vùng trắng để vẽ
        drawingPanel = new DrawingPanel();
        drawingPanel.setBackground(Color.WHITE);
        drawingPanel.setPreferredSize(new Dimension(600, 600));
        mainPanel.add(drawingPanel, BorderLayout.CENTER);

        // Phần dưới: Khu vực chat và đoán từ
        JPanel bottomPanel = new JPanel(new GridLayout(2, 1));

// Khu vực chat
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatArea = new JTextArea();
        chatArea.setEditable(false); // Không cho phép người dùng chỉnh sửa
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        chatScrollPane.setPreferredSize(new Dimension(400, 150));
        chatPanel.add(chatScrollPane, BorderLayout.CENTER);

        JPanel chatInputPanel = new JPanel(new BorderLayout());
        chatInputField = new JTextField(20);
        sendChatButton = new JButton("Gửi");
        chatInputPanel.add(chatInputField, BorderLayout.CENTER);
        chatInputPanel.add(sendChatButton, BorderLayout.EAST);
        chatPanel.add(chatInputPanel, BorderLayout.SOUTH);

        bottomPanel.add(chatPanel);

// Khu vực đoán từ
        JPanel guessPanel = new JPanel(new BorderLayout());
        guessArea = new JTextPane(); // Sử dụng JTextPane thay vì JTextArea
        guessArea.setEditable(false); // Không cho phép người dùng chỉnh sửa

// Thêm guessArea vào JScrollPane
        JScrollPane guessScrollPane = new JScrollPane(guessArea);
        guessScrollPane.setPreferredSize(new Dimension(400, 150));
        guessPanel.add(guessScrollPane, BorderLayout.CENTER);

// Tạo khu vực nhập đoán từ
        JPanel guessInputPanel = new JPanel(new BorderLayout());
        guessInputField = new JTextField(20);
        sendGuessButton = new JButton("Đoán");
        guessInputPanel.add(new JLabel("Đoán từ: "), BorderLayout.WEST);
        guessInputPanel.add(guessInputField, BorderLayout.CENTER);
        guessInputPanel.add(sendGuessButton, BorderLayout.EAST);
        guessPanel.add(guessInputPanel, BorderLayout.SOUTH);

// Thêm guessPanel vào bottomPanel
        bottomPanel.add(guessPanel);

// Thêm bottomPanel vào mainPanel
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Nút bắt đầu trò chơi (chỉ hiện với người chủ trì)
        if (isHost) {
            startGameButton = new JButton("Bắt đầu trò chơi");
            add(startGameButton, BorderLayout.SOUTH);
        }

        // Bảng chọn màu và tẩy
        JPanel toolsPanel = new JPanel();
        toolsPanel.setLayout(new FlowLayout());

        colorPickerButton = new JButton("Chọn màu");
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

        eraserButton = new JButton("Tẩy");
        eraserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentColor = Color.WHITE;
                isEraserMode = true; // Chuyển sang chế độ tẩy
            }
        });

        drawButton = new JButton("Vẽ");
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

    public void updateTimerLabel(String text) {
        timerLabel.setText(text);
    }

    public void showWordChoiceDialog() {
        // Hiển thị dialog để chọn từ
    }

    // Phương thức cập nhật danh sách người chơi
    public void updatePlayersList(ArrayList<Player> players) {
        playersListModel.clear();
        for (Player player : players) {
            playersListModel.addElement(player);
        }
    }
    public void updatePlayersListWithSort(ArrayList<Player> players) {
        playersListModel.clear();
        players.sort((p1, p2) -> p2.getScore() - p1.getScore());
        for (Player player : players) {
            playersListModel.addElement(player);
        }
    }

    public class DrawingPanel extends JPanel {
        private DrawingListener drawingListener;
        private int prevX, prevY;
        private boolean isDrawingEnabled = true;
        public DrawingPanel() {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (isDrawingEnabled) { // Kiểm tra cờ trước khi vẽ
                        prevX = e.getX();
                        prevY = e.getY();
                    }
                }
            });

            addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (isDrawingEnabled) { // Kiểm tra cờ trước khi vẽ
                        int x = e.getX();
                        int y = e.getY();
                        drawLine(prevX, prevY, x, y, currentColor.getRGB());
                        if (drawingListener != null) {
                            drawingListener.onDraw(prevX, prevY, x, y, currentColor.getRGB());
                        }
                        prevX = x;
                        prevY = y;
                    }
                }
            });
        }
        // Phương thức xóa sạch panel
        public void clearPanel() {
            repaint(); // Xóa toàn bộ nội dung bằng cách sơn lại nền
        }
        // Phương thức để vẽ dòng
        public void drawLine(int x1, int y1, int x2, int y2, int colorValue) {
            Graphics2D g = (Graphics2D) getGraphics();
            g.setColor(new Color(colorValue));
            g.setStroke(new BasicStroke(12));
            g.drawLine(x1, y1, x2, y2);
        }

        // Phương thức để bật hoặc tắt chế độ vẽ
        public void setDrawingEnabled(boolean enabled) {
            isDrawingEnabled = enabled;
        }

        public void addDrawingListener(DrawingListener listener) {
            this.drawingListener = listener;
        }
    }

    // Phương thức thêm tin nhắn vào khu vực chat
    public void appendChatMessage(String message) {
        chatArea.append(message + "\n");
    }
    public void appendGuessMessage(String message) {
        StyledDocument doc = guessArea.getStyledDocument();

        // Tạo kiểu văn bản màu xanh dương
        SimpleAttributeSet style = new SimpleAttributeSet();
        StyleConstants.setForeground(style, Color.BLUE); // Đặt màu xanh dương

        try {
            doc.insertString(doc.getLength(), message + "\n", style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
    public void appendGuessMessageCorrect(String message) {
        StyledDocument doc = guessArea.getStyledDocument();

        // Tạo kiểu mới cho chữ màu xanh và đậm
        SimpleAttributeSet style = new SimpleAttributeSet();
        StyleConstants.setForeground(style, Color.GREEN); // Đặt màu xanh
        StyleConstants.setBold(style, true); // Đặt chữ đậm

        try {
            doc.insertString(doc.getLength(), message + "\n", style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
    public void appendGuessMessageGuess(String message) {
        StyledDocument doc = guessArea.getStyledDocument();

        // Tạo kiểu mới cho chữ màu xanh và đậm
        SimpleAttributeSet style = new SimpleAttributeSet();
        StyleConstants.setForeground(style, Color.GRAY); // Đặt màu xanh
        StyleConstants.setBold(style, true); // Đặt chữ đậm

        try {
            doc.insertString(doc.getLength(), message + "\n", style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
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

    public JLabel getTimerLabel() {
        return timerLabel;
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
    public String getRoomId() {
        return roomIdLabel.getText().substring(10);
    }
    public JPanel getGuessPanel() {
        return (JPanel) ((JPanel) ((JPanel) getContentPane().getComponent(1)).getComponent(1)).getComponent(1);
    }
    public JButton getDrawButton() {
        return drawButton;
    }

    public JButton getEraserButton() {
        return eraserButton;
    }

    public JButton getColorPickerButton() {
        return colorPickerButton;
    }
    public void addWindowCloseListener(java.awt.event.WindowAdapter adapter) {
        addWindowListener(adapter);
    }
}
