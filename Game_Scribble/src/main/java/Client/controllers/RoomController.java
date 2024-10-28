package Client.controllers;

import Client.ClientSocket;
import Client.manager.ClientSocketManager;
import Client.manager.MessageListener;
import Client.sessions.UserSession;
import Client.views.HomeView;
import Client.views.RoomView;
import Server.model.Player;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;

public class RoomController implements MessageListener {
    private RoomView roomView;
    private ClientSocketManager clientSocketManager;
    private ClientSocket clientSocket;
    private boolean isHost;
    private ArrayList<Player> players;
    private int currentPlayerIndex = 0;
    private Player currentDrawer; // Người đang vẽ
    private Timer drawingTimer;
    private Timer chooseWordTimer;
    private Timer guessWordTimer;
    private Timer countdownTimer;
    private String phaseId;
    private boolean isChoosingWord = false;
    private boolean isGuessCorrect = false;
    private boolean isDrawing = false;
    private boolean isChoosing = false;
    private boolean isEndGame = false;
    private boolean isEndTurn = false;
    private int countEndGame = 0;
    public RoomController(RoomView roomView, ClientSocketManager clientSocketManager, ClientSocket clientSocket, boolean isHost, ArrayList<Player> players) {
        this.roomView = roomView;
        this.clientSocketManager = clientSocketManager;
        this.clientSocket = clientSocket;
        this.isHost = isHost;
        this.players = players;
        this.countEndGame = 0;
        clientSocketManager.addMessageListener(this);
        this.roomView.getSendChatButton().addActionListener(new SendChatListener());
        this.roomView.getSendGuessButton().addActionListener(new GuestListener());
        if (isHost) {
            this.roomView.getStartGameButton().addActionListener(new StartGameListener());
        }
        this.roomView.getDrawingPanel().addDrawingListener(new DrawingListener());
        hideDrawingPanel();
        hideGuessInputField();
        // Thêm hành động khi cửa sổ đóng
        this.roomView.addWindowCloseListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                try {
                    String host = "false";
                    if(isHost) host = "true";
                    String rq  = "LEAVE_ROOM:" + roomView.getRoomId()+":"+UserSession.getInstance().getUser().getNickname() + ":" + host;
                    clientSocket.sendMessage(rq);
                    clientSocket.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
//                HomeView homeView = new HomeView(UserSession.getInstance().getUser().getNickname(), UserSession.getInstance().getUser().getAvatar());
//                new HomeController(homeView, clientSocketManager, clientSocket);
//                homeView.setVisible(true);
//                roomView.dispose();
            }
        });
    }
    //    Thời gian đợi trước khi chọn từ để vẽ
    private void waitBeforeChoosingWord() {
        isEndTurn = true;
        Timer waitTimer = new Timer(1000, new ActionListener() {
            int timeLeft = 5; // 5 giây để chờ
            @Override
            public void actionPerformed(ActionEvent e) {
                if (timeLeft > 0) {
                    hideDrawingPanel();
                    hideGuessInputField();
                    roomView.updateTimerLabel("Thời gian chờ còn lại: " + timeLeft + " giây");
                    timeLeft--;
                    if (isEndGame) {
                        timeLeft = 0;
                    }

                } else {
                    ((Timer) e.getSource()).stop();
                    if (isChoosing) {
                        showWordChoiceDialog();
                    }
                    else {
                        waitChoosingWord();
                    }
                }
            }
        });
        waitTimer.start();
    }
    // Luồng chờ chọn từ
    private void waitChoosingWord() {
        Timer chooseWordTimer = new Timer(1000, new ActionListener() {
            int timeLeft = 10; // 10 giây để chọn từ
            @Override
            public void actionPerformed(ActionEvent e) {
                if (timeLeft > 0) {
                    hideDrawingPanel();
                    hideGuessInputField();
                    if(!isChoosingWord) {
                        ((Timer) e.getSource()).stop();
                    }
                    currentDrawer = players.get(currentPlayerIndex);
                    roomView.updateTimerLabel("Thời gian "+currentDrawer.getNickname()+ " chọn từ còn lại: " + timeLeft + " giây");
                    timeLeft--;
                } else {
                    ((Timer) e.getSource()).stop();

                }
            }

            private boolean isChoosing() {
                return isChoosing;
            }
        });
        chooseWordTimer.start();
    }
//   Đơi 5s trước khi vẽ và đoán
    private void waitBeforeDrawingAndGuessing() {
        Timer waitTimer = new Timer(1000, new ActionListener() {
            int timeLeft = 5; // 5 giây để chờ
            @Override
            public void actionPerformed(ActionEvent e) {
                if (timeLeft > 0) {
                    hideDrawingPanel();
                    hideGuessInputField();
                    roomView.updateTimerLabel("Thời gian chờ còn lại: " + timeLeft + " giây");
                    timeLeft--;
                    if (isEndGame) {
                        timeLeft = 0;
                    }
                } else {
                    ((Timer) e.getSource()).stop();
                    if (isDrawing) {
                        startDrawingPhase();
                    } else {
                        startGuessWord();
                    }
                }
            }
        });
        waitTimer.start();
    }
    // Bắt đầu giai đoạn vẽ
    private void startDrawingPhase() {
        isEndTurn = false;
        sendNotify("Lượt vẽ của " + currentDrawer.getNickname() + " đã bắt đầu!");
        Timer drawingTimer = new Timer(1000, new ActionListener() {
            int timeLeft = 30; // 30 giây để vẽ

            @Override
            public void actionPerformed(ActionEvent e) {
                if (timeLeft > 0) {
                    showDrawingPanel();
                    hideGuessInputField();
                    roomView.updateTimerLabel("Thời gian vẽ còn lại: " + timeLeft + " giây");
                    timeLeft--;
                    if (isEndGame) {
                        timeLeft = 0;
                    }
                    System.out.println("End turn: " + isEndTurn);
                    if (isEndTurn)
                    {
                        ((Timer) e.getSource()).stop();
                    }
                } else {
                    ((Timer) e.getSource()).stop();
                    System.out.println("Ket thuc ve");
                    try {
                        isEndTurn = true;
                        endTurn();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });
        drawingTimer.start();
    }
//    Bắt đầu giai đoạn đoán từ
    private void startGuessWord(){
        isGuessCorrect = false;
        isEndTurn = false;
        guessWordTimer = new Timer(1000, new ActionListener() {
            int timeLeft = 30; // 30 giây để đoán
            @Override
            public void actionPerformed(ActionEvent e) {
                if (timeLeft > 0) {
                    hideDrawingPanel();
                    if (!isGuessCorrect)
                    {
                        showGuessInputField();
                    }
                    else hideGuessInputField();
                    if (isEndGame) {
                        timeLeft = 0;
                    }
                    if (isEndTurn)
                    {
                        ((Timer) e.getSource()).stop();
                    }
                    roomView.updateTimerLabel("Thời gian đoán còn lại: " + timeLeft + " giây");
                    timeLeft--;
                } else {
                    ((Timer) e.getSource()).stop();
                    isEndTurn = true;
                }
            }
        });
        guessWordTimer.start();
    }
    // Kết thúc 1 phiên vẽ
    private void endTurn() throws IOException {
        roomView.getDrawingPanel().clearPanel();
        clientSocket.sendMessage("CLEAR_PANEL:" + roomView.getRoomId());
        if(isEndGame)
        {
            String myNickname = UserSession.getInstance().getUser().getNickname();
            if (players.get(currentPlayerIndex).getNickname().equals(myNickname) && countEndGame == 0) {
                countEndGame++;
                clientSocket.sendMessage("END_GAME:" + roomView.getRoomId());
            }
            return;
        }
        int previousIndex = currentPlayerIndex;
        sendNotify("Lượt vẽ của " + currentDrawer.getNickname() + " đã kết thúc!");
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        String myNickname = UserSession.getInstance().getUser().getNickname();
        System.out.println("Index: " + currentPlayerIndex);
        if (players.get(previousIndex).getNickname().equals(myNickname)) {
            String rq = "CHOOSE_PHASE:" + roomView.getRoomId() + ":" + players.get(currentPlayerIndex).getNickname() + ":" + currentPlayerIndex;
            clientSocket.sendMessage(rq);
        }

    }
    // Hủy lắng nghe khi rời phòng hoặc kết thúc trò chơi
    private void stopListening() {
        clientSocketManager.removeMessageListener(this);
    }
    //    Bắt đầu game
    private class StartGameListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                String roomId = roomView.getRoomIdLabel().getText().substring(10);
                clientSocket.sendMessage("START_GAME:" + roomId);
                currentPlayerIndex = 0;
                String rq = "CHOOSE_PHASE:" + roomId + ":" + players.get(currentPlayerIndex).getNickname() + ":" + currentPlayerIndex;
                clientSocket.sendMessage(rq);
                roomView.getStartGameButton().setVisible(false);
                sendNotify("Trò chơi đã bắt đầu!");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
//    Gửi tin nhắn trong phòng
    private class SendChatListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String message = roomView.getChatInputField().getText();
            String roomId = roomView.getRoomIdLabel().getText().substring(10);
            if (!message.isEmpty()) {
                try {
                    String fullMessage = "CHAT_ROOM:" + roomId + ":" + UserSession.getInstance().getUser().getNickname() + ": " + message;
                    clientSocket.sendMessage(fullMessage);
                    roomView.getChatInputField().setText("");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
//    Gửi từ đoán
    private class GuestListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String message = roomView.getGuessInputField().getText();
            String roomId = roomView.getRoomIdLabel().getText().substring(10);
            if (!message.isEmpty()) {
                try {
                    String currentPhaseNickname = players.get(currentPlayerIndex).getNickname();
                    String myNickname = UserSession.getInstance().getUser().getNickname();
                    String fullMessage = "GUESS:" + roomId + ":" +phaseId + ":" + myNickname + ":" + message + ":" + currentPhaseNickname;
                    clientSocket.sendMessage(fullMessage);
                    roomView.getGuessInputField().setText("");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
//    Vẽ trên panel
    private class DrawingListener implements RoomView.DrawingListener {
        @Override
        public void onDraw(int x1, int y1, int x2, int y2, int colorValue) {
            String roomId = roomView.getRoomIdLabel().getText().substring(10);
            try {
                String drawMessage = "DRAW:" + roomId + ":" + x1 + ":" + y1 + ":" + x2 + ":" + y2 + ":" + colorValue;
                clientSocket.sendMessage(drawMessage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
// Lắng nghe tin nhắn từ server
    @Override
    public void onMessageReceived(String message) {
        SwingUtilities.invokeLater(() -> {
            try {
                processMessage(message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
//    Xử lý tin nhắn từ server
    private void processMessage(String message) throws IOException {
        if (message.startsWith("CHATROOM:")) {
            String chatMessage = message.substring(9);
            roomView.appendChatMessage(chatMessage);
        } else if (message.startsWith("START_GAME")) {
            roomView.startGame();
        }
        else if (message.startsWith("NOTIFY:")) {
            String[] parts = message.split(":");
            roomView.appendGuessMessage(parts[1]);
        } else if (message.startsWith("DRAW:")) {
            String[] drawData = message.split(":");
            int x1 = Integer.parseInt(drawData[1]);
            int y1 = Integer.parseInt(drawData[2]);
            int x2 = Integer.parseInt(drawData[3]);
            int y2 = Integer.parseInt(drawData[4]);
            int colorValue = Integer.parseInt(drawData[5]);
            drawOnPanel(x1, y1, x2, y2, colorValue);
        }
        else if(message.startsWith("ADD_PLAYER_TO_ROOM"))
        {
            String[] parts = message.split(":");
            ArrayList<Player> players = new ArrayList<>();
            for(int i = 2; i < parts.length; i++){
                if(parts[i].isEmpty()) continue;
                String[] playerInfo = parts[i].split(",");
                Player player = new Player(playerInfo[0], playerInfo[1], playerInfo[2], Integer.parseInt(playerInfo[3]));
                players.add(player);
            }
            roomView.updatePlayersList(players);
            updatePlayersList(players);
        }
        else if(message.startsWith("UPDATE_PLAYER"))
        {
            String[] parts = message.split(":");
            ArrayList<Player> players = new ArrayList<>();
            for(int i = 2; i < parts.length; i++){
                if(parts[i].isEmpty()) continue;
                String[] playerInfo = parts[i].split(",");
                Player player = new Player(playerInfo[0], playerInfo[1], playerInfo[2], Integer.parseInt(playerInfo[3]));
                players.add(player);
            }
            roomView.updatePlayersList(players);
            updatePlayersList(players);
        }
        else if (message.startsWith("CHOOSE_PHASE")) {
            System.out.println("Luot moi:");
            isChoosingWord = true;
            String[] parts = message.split(":");
            String nickname = parts[2];
            String currentPlayerId = parts[3];
            this.currentPlayerIndex = Integer.parseInt(currentPlayerId);
//            Nếu là lượt của mình
            String myNickname = UserSession.getInstance().getUser().getNickname();
            if (nickname.equals(myNickname)) {
                isChoosing = true;
            } else { // còn không thì chờ người khác chọn từ
                isChoosing = false;
            }
            waitBeforeChoosingWord();
        } else if (message.startsWith("DRAW_PHASE")) {
            isChoosingWord = false;
            String[] parts = message.split(":");
            String drawerNickname = parts[2];
            String word = parts[3];
            this.currentPlayerIndex = Integer.parseInt(parts[4]);
            String phaseID = parts[5];
            this.phaseId = phaseID;
            String myNickname = UserSession.getInstance().getUser().getNickname();
            if (drawerNickname.equals(myNickname)) {
                isDrawing = true;
            } else {
                isDrawing = false;
            }
            waitBeforeDrawingAndGuessing();
        }
        else if(message.startsWith("GUESS:"))
        {
            String[] parts = message.split(":");
            String isCorrect = parts[1];
            String guesserNickname = parts[2];
            String word = parts[3];
            System.out.println("isCorrect: " + isCorrect);
            System.out.println("guesserNickname: " + guesserNickname);
            System.out.println("word: " + word);
            if (isCorrect.equals("TRUE")) {
                int countCorrectPlayer = Integer.parseInt(parts[4]);
                int targetScore = Integer.parseInt(parts[5]);
                if(UserSession.getInstance().getUser().getNickname().equals(guesserNickname)) {
                    isGuessCorrect = true;
                    roomView.appendGuessMessageCorrect("Bạn đã đoán đúng từ!");
                } else {
                    roomView.appendGuessMessageGuess(guesserNickname + " đã đoán đúng từ!");
                }
//                Cập nhật lại danh sách người chơi
                ArrayList<Player> players = new ArrayList<>();
                for(int i = 6; i < parts.length; i++){
                    if(parts[i].isEmpty()) continue;
                    String[] playerInfo = parts[i].split(",");
                    Player player = new Player(playerInfo[0], playerInfo[1], playerInfo[2], Integer.parseInt(playerInfo[3]));
                    players.add(player);
                    if (targetScore <= player.getScore()) {
                        isEndGame = true;
                        System.out.println("Ket thuc");
                    }
                }
                System.out.println("Target score: " + targetScore);
                System.out.println("Current score: " + players.get(0).getScore());
                roomView.updatePlayersListWithSort(players);
                ArrayList<Player> playersControls = new ArrayList<>();
                for(int i = 6; i < parts.length; i++){
                    if(parts[i].isEmpty()) continue;
                    String[] playerInfo = parts[i].split(",");
                    Player player = new Player(playerInfo[0], playerInfo[1], playerInfo[2], Integer.parseInt(playerInfo[3]));
                    playersControls.add(player);
                }
                updatePlayersList(playersControls);
                if(countCorrectPlayer == players.size() - 1)
                {
                    isEndTurn = true;
                    endTurn();
                }
                if (isEndGame) {
                    endTurn();
                }
            }
            else {
                if(UserSession.getInstance().getUser().getNickname().equals(guesserNickname)) {
                    roomView.appendGuessMessageGuess("Bạn: " + word);
                } else {
                    roomView.appendGuessMessageGuess(guesserNickname + ": " + word);
                }
            }
        }
        else if(message.startsWith("CLEAR_PANEL:"))
        {
            roomView.getDrawingPanel().clearPanel();
        }
        else if(message.startsWith("END_GAME:"))
        {
            isEndGame = true;
            System.out.println("End game");
            showDialogEndGame();
        }
        else if (message.startsWith("DELETE_ROOM"))
        {
            roomView.dispose();
            stopListening();
            String myNickname = UserSession.getInstance().getUser().getNickname();
            String avatar = UserSession.getInstance().getUser().getAvatar();
            HomeView homeView = new HomeView(myNickname, avatar);
            new HomeController(homeView, clientSocketManager, clientSocket);
            homeView.setVisible(true);
        }
    }

    // Show dialog endgame
    public void showDialogEndGame() {
        ArrayList<Player> listPlayers = players;
        // Sort players by score in descending order
        players.sort((p1, p2) -> p2.getScore() - p1.getScore());
        Player winner = listPlayers.get(0); // Get the top player as the winner

        // Set up the main dialog
        JDialog dialog = new JDialog((Frame) null, "Kết thúc trò chơi", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 400);

        // Title label
        JLabel titleLabel = new JLabel("Trò chơi đã kết thúc", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.BLUE);
        dialog.add(titleLabel, BorderLayout.NORTH);

        // Winner panel at the top
        JPanel winnerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel winnerLabel = new JLabel("Người chiến thắng: " + winner.getNickname() + " với số điểm: " + winner.getScore());
        winnerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        winnerLabel.setForeground(Color.RED);

        // Display winner's avatar (loading from file path)
        JLabel winnerAvatar = new JLabel(loadAvatarIcon(winner.getAvatar()));
        winnerPanel.add(winnerAvatar);
        winnerPanel.add(winnerLabel);
        dialog.add(winnerPanel, BorderLayout.NORTH);

        // Player list model
        DefaultListModel<Player> playersListModel = new DefaultListModel<>();
        listPlayers.forEach(playersListModel::addElement);
        JList<Player> playersList = new JList<>(playersListModel);
        playersList.setCellRenderer(new PlayerListCellRenderer());
        JScrollPane playersScrollPane = new JScrollPane(playersList);
        playersScrollPane.setPreferredSize(new Dimension(250, 200));
        dialog.add(playersScrollPane, BorderLayout.CENTER);

        // Bottom area with confirmation button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton confirmButton = new JButton("Xác nhận");
        confirmButton.setPreferredSize(new Dimension(100, 30));
        confirmButton.addActionListener(e -> {
            dialog.dispose();
            roomView.dispose();
            stopListening();
//            Tro lai trang chu
            String myNickname = UserSession.getInstance().getUser().getNickname();
            String avatar = UserSession.getInstance().getUser().getAvatar();
            HomeView homeView = new HomeView(myNickname, avatar);
            new HomeController(homeView, clientSocketManager, clientSocket);
            homeView.setVisible(true);
            roomView.dispose();
        });
        bottomPanel.add(confirmButton);

        dialog.add(bottomPanel, BorderLayout.SOUTH);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    // Helper method to load avatar icons from file paths
    private ImageIcon loadAvatarIcon(String path) {
        ImageIcon icon = new ImageIcon(path);
        Image image = icon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH); // Resize for consistency
        return new ImageIcon(image);
    }

    // Custom cell renderer to display player information with avatars
    private class PlayerListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Player player = (Player) value;

            // Use JPanel to combine avatar and text
            JPanel panel = new JPanel(new BorderLayout(5, 5));

            // Player's avatar
            JLabel avatarLabel = new JLabel(loadAvatarIcon("avatars/"+player.getAvatar()));
            panel.add(avatarLabel, BorderLayout.WEST);

            // Player information text
            String playerText = String.format("%s - Score: %d", player.getNickname(), player.getScore());
            JLabel textLabel = new JLabel(playerText);
            textLabel.setFont(new Font("Arial", Font.PLAIN, 14));

            // Style selection colors
            if (isSelected) {
                panel.setBackground(Color.LIGHT_GRAY);
                textLabel.setForeground(Color.BLACK);
            } else {
                panel.setBackground(Color.WHITE);
                textLabel.setForeground(Color.DARK_GRAY);
            }

            panel.add(textLabel, BorderLayout.CENTER);
            panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            return panel;
        }
    }

    private void drawOnPanel(int x1, int y1, int x2, int y2, int colorValue) {
        SwingUtilities.invokeLater(() -> {
            roomView.getDrawingPanel().drawLine(x1, y1, x2, y2, colorValue);
        });
    }

    public void updatePlayersList(ArrayList<Player> players) {
        this.players = new ArrayList<>(players);
    }

    public void showWordChoiceDialog() {
        JDialog dialog = new JDialog((Frame) null, "Chọn từ", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(300, 200);

        JLabel countdownLabel = new JLabel("Thời gian còn lại để bạn chọn từ: 10 giây", SwingConstants.CENTER);
        countdownLabel.setFont(new Font("Arial", Font.BOLD, 16));
        dialog.add(countdownLabel, BorderLayout.NORTH);

        // Tạo các nút để chọn từ
        String[] options = {"ngôi nhà", "biển", "ô tô", "cây", "mặt trời"}; // Danh sách từ để chọn
        JComboBox<String> wordComboBox = new JComboBox<>(options);
        dialog.add(wordComboBox, BorderLayout.CENTER);

        // Nút xác nhận chọn từ
        JButton confirmButton = new JButton("Xác nhận");
        dialog.add(confirmButton, BorderLayout.SOUTH);

        currentDrawer = players.get(currentPlayerIndex);
        // Tạo Timer để cập nhật đồng hồ đếm ngược
        countdownTimer = new Timer(1000, new ActionListener() {
            int timeLeft = 10; // 10 giây để chọn từ

            @Override
            public void actionPerformed(ActionEvent e) {
                if (timeLeft > 0) {
                    hideDrawingPanel();
                    hideGuessInputField();
                    countdownLabel.setText("Thời gian còn lại để bạn chọn từ: " + timeLeft + " giây");
                    roomView.updateTimerLabel("");
                    timeLeft--;
                } else {
                    ((Timer) e.getSource()).stop();
                    // Hết thời gian, gửi từ mặc định và đóng dialog
                    if(players.get(currentPlayerIndex).getNickname().equals(UserSession.getInstance().getUser().getNickname())) {
                        sendPhaseToServer("ngôi nhà", clientSocket, roomView.getRoomId(), players.get(currentPlayerIndex).getNickname());
                        dialog.dispose();
                    }
                }
            }
        });
        countdownTimer.start();

        // Xử lý sự kiện khi người dùng chọn từ và nhấn nút "Xác nhận"
        confirmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedWord = (String) wordComboBox.getSelectedItem();
                if (selectedWord != null) {
                    countdownTimer.stop(); // Dừng timer khi người dùng chọn từ
                    if(players.get(currentPlayerIndex).getNickname().equals(UserSession.getInstance().getUser().getNickname())) {
                        sendPhaseToServer(selectedWord, clientSocket, roomView.getRoomId(), players.get(currentPlayerIndex).getNickname());
                        dialog.dispose(); // Đóng dialog sau khi chọn
                    }
                }
            }
        });
        // Hiển thị dialog và chờ người dùng chọn từ
        dialog.setLocationRelativeTo(null); // Đặt dialog giữa màn hình
        dialog.setVisible(true); // Hiển thị dialog
    }
    // Hàm gửi phase lên server
    private void sendPhaseToServer(String word, ClientSocket clientSocket, String roomId, String nickname) {
        try {
            // Chuyển phase thành chuỗi để gửi đi
            String phaseMessage = "DRAW_PHASE:" + roomId + ":" + nickname + ":" + word + ":" + this.currentPlayerIndex;
            // Gửi thông điệp phase tới server
            clientSocket.sendMessage(phaseMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Phương thức để ẩn các công cụ vẽ khi không cần thiết
    public void hideDrawingPanel() {
        roomView.getDrawingPanel().setDrawingEnabled(false);
        roomView.getDrawButton().setVisible(false);
        roomView.getEraserButton().setVisible(false);
        roomView.getColorPickerButton().setVisible(false);
    }

    // Phương thức để hiển thị các công cụ vẽ khi cần
    public void showDrawingPanel() {
        roomView.getDrawingPanel().setDrawingEnabled(true);
        roomView.getDrawButton().setVisible(true);
        roomView.getEraserButton().setVisible(true);
        roomView.getColorPickerButton().setVisible(true);
    }

    // Phương thức để ẩn bảng đoán từ khi không cần thiết
    public void hideGuessInputField() {
        roomView.getGuessInputField().setEnabled(false);
        roomView.getSendGuessButton().setEnabled(false);
    }

    // Phương thức để hiển thị bảng đoán từ khi cần
    public void showGuessInputField() {
        roomView.getGuessInputField().setEnabled(true);
        roomView.getSendGuessButton().setEnabled(true);
    }

    // Hàm tìm người chơi theo nickname
    private Player findPlayerByNickname(String nickname) {
        for (Player player : players) {
            if (player.getNickname().equals(nickname)) {
                return player;
            }
        }
        return null;
    }
    public void sendNotify(String message)
    {
        try {
            String roomId = roomView.getRoomIdLabel().getText().substring(10);
            clientSocket.sendMessage("NOTIFY:" + roomId + ":" + message);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
