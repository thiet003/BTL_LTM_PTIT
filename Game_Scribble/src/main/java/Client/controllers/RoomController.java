package Client.controllers;

import Client.ClientSocket;
import Client.manager.ClientSocketManager;
import Client.manager.MessageListener;
import Client.sessions.UserSession;
import Client.views.RoomView;

import javax.swing.SwingUtilities;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class RoomController implements MessageListener {
    private RoomView roomView;
    private ClientSocketManager clientSocketManager;
    private ClientSocket clientSocket;
    private boolean isHost;

    public RoomController(RoomView roomView, ClientSocketManager clientSocketManager, ClientSocket clientSocket, boolean isHost) {
        this.roomView = roomView;
        this.clientSocketManager = clientSocketManager;
        this.clientSocket = clientSocket;
        this.isHost = isHost;

        // Đăng ký RoomController làm listener cho các tin nhắn từ server
        clientSocketManager.addMessageListener(this);
        
        // Đăng ký listener cho nút gửi tin nhắn
        this.roomView.getSendChatButton().addActionListener(new SendChatListener());

        // Đăng ký listener cho nút bắt đầu trò chơi (nếu là chủ phòng)
        if (isHost) {
            this.roomView.getStartGameButton().addActionListener(new StartGameListener());
        }
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
                    String fullMessage = "CHAT_ROOM:" +roomId+":"+ UserSession.getInstance().getUser().getNickname() + ": " + message;
                    System.out.println("Full: "+fullMessage);
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
                clientSocket.sendMessage("START_GAME");
                roomView.appendChatMessage("Trò chơi bắt đầu!");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    // Implement phương thức từ MessageListener để nhận tin nhắn từ server
    @Override
    public void onMessageReceived(String message) {
        processMessage(message);
//        SwingUtilities.invokeLater(() -> {
//            processMessage(message);
//        });
    }

    // Phân tích và xử lý tin nhắn từ server
    private void processMessage(String message) {
        System.out.println("fukk nhan:"+message);
        if (message.startsWith("CHATROOM:")) {
            System.out.println("chat_room: "+message);
            String chatMessage = message.substring(9); // Lấy nội dung tin nhắn từ server
            roomView.appendChatMessage(chatMessage);
        } else if (message.startsWith("START_GAME")) {
            roomView.appendChatMessage("Trò chơi đã bắt đầu!");
            // Thêm các xử lý khác khi trò chơi bắt đầu
        }
        // Xử lý các loại tin nhắn khác (người chơi tham gia, rời phòng, cập nhật trò chơi, v.v.)
    }
}
