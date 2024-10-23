package Client.controllers;

import Client.ClientSocket;
import Client.manager.ClientSocketManager;
import Client.manager.MessageListener;
import Client.sessions.UserSession;
import Client.views.RoomView;
import Server.model.Player;

import javax.swing.SwingUtilities;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;

public class RoomController implements MessageListener {
    private RoomView roomView;
    private ClientSocketManager clientSocketManager;
    private ClientSocket clientSocket;
    private boolean isHost;
    private final ArrayList<Player> players;

    public RoomController(RoomView roomView, ClientSocketManager clientSocketManager, ClientSocket clientSocket, boolean isHost, ArrayList<Player> players) {
        this.roomView = roomView;
        this.clientSocketManager = clientSocketManager;
        this.clientSocket = clientSocket;
        this.isHost = isHost;
        this.players = players;

        // Đăng ký RoomController làm listener cho các tin nhắn từ server
        clientSocketManager.addMessageListener(this);

        // Đăng ký listener cho nút gửi tin nhắn
        this.roomView.getSendChatButton().addActionListener(new SendChatListener());

        // Đăng ký listener cho nút bắt đầu trò chơi (nếu là chủ phòng)
        if (isHost) {
            this.roomView.getStartGameButton().addActionListener(new StartGameListener());
        }

        this.roomView.getDrawingPanel().addDrawingListener(new DrawingListener());
    }

    // Hủy lắng nghe khi rời phòng hoặc kết thúc trò chơi
    private void stopListening() {
        clientSocketManager.removeMessageListener(this);
    }

    // Xử lý sự kiện khi gửi tin nhắn trong phòng
    private class SendChatListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String message = roomView.getChatInputField().getText();
            String roomId = roomView.getRoomIdLabel().getText().substring(10);
            if (!message.isEmpty()) {
                try {
                    String fullMessage = "CHAT_ROOM:" + roomId + ":" + UserSession.getInstance().getUser().getNickname() + ": " + message;
                    System.out.println("Full: " + fullMessage);
                    clientSocket.sendMessage(fullMessage);
                    roomView.getChatInputField().setText(""); // Xóa nội dung chat sau khi gửi
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    // Xử lý sự kiện khi chủ phòng bắt đầu trò chơi
    private class StartGameListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                String roomId = roomView.getRoomIdLabel().getText().substring(10);
                clientSocket.sendMessage("START_GAME:" + roomId);
                roomView.appendChatMessage("Trò chơi bắt đầu!");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    // Xử lý sự kiện vẽ trên panel
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

    // Implement phương thức từ MessageListener để nhận tin nhắn từ server
    @Override
    public void onMessageReceived(String message) {
        SwingUtilities.invokeLater(() -> {
            processMessage(message);
        });
    }

    // Phân tích và xử lý tin nhắn từ server
    private void processMessage(String message) {
        System.out.println("Received message: " + message);
        if (message.startsWith("CHATROOM:")) {
            String chatMessage = message.substring(9); // Lấy nội dung tin nhắn từ server
            roomView.appendChatMessage(chatMessage);
        } else if (message.startsWith("START_GAME")) {
            roomView.appendChatMessage("Trò chơi đã bắt đầu!");
            roomView.startGame();
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
        }
        // Xử lý các loại tin nhắn khác (người chơi tham gia, rời phòng, cập nhật trò chơi, v.v.)
    }

    // Phương thức để vẽ lên panel
    private void drawOnPanel(int x1, int y1, int x2, int y2, int colorValue) {
        SwingUtilities.invokeLater(() -> {
            roomView.getDrawingPanel().drawLine(x1, y1, x2, y2, colorValue);
        });
    }
}