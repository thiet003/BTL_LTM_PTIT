package Client.controllers;

import Client.ClientSocket;
import Client.manager.ClientSocketManager;
import Client.manager.MessageListener;
import Client.views.JoinRoomView;
import Client.views.RoomView;

import javax.swing.SwingUtilities;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class JoinRoomController implements MessageListener {
    private JoinRoomView joinRoomView;
    private ClientSocketManager clientSocketManager;
    private ClientSocket clientSocket;

    public JoinRoomController(JoinRoomView joinRoomView, ClientSocketManager clientSocketManager, ClientSocket clientSocket) {
        this.joinRoomView = joinRoomView;
        this.clientSocketManager = clientSocketManager;
        this.clientSocket = clientSocket;

        // Đăng ký JoinRoomController làm listener cho các tin nhắn từ server
        clientSocketManager.addMessageListener(this);

        // Đăng ký listener cho nút Join Room
        this.joinRoomView.getJoinRoomButton().addActionListener(new JoinRoomListener());
    }

    // Hủy lắng nghe khi không cần nữa
    private void stopListening() {
        clientSocketManager.removeMessageListener(this);
    }

    // Xử lý sự kiện khi nhấn "Join Room"
    private class JoinRoomListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String roomId = joinRoomView.getRoomIdField().getText();
            try {
                // Gửi yêu cầu tham gia phòng
                clientSocket.sendMessage("JOIN_ROOM:" + roomId);
                System.out.println("Sent JOIN_ROOM request for room ID: " + roomId);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    // Xử lý tin nhắn nhận được từ server
    @Override
    public void onMessageReceived(String message) {
        SwingUtilities.invokeLater(() -> {
            processMessage(message);
        });
    }

    // Phân tích và xử lý tin nhắn từ server
    private void processMessage(String message) {
        if (message.startsWith("JOIN_ROOM_SUCCESS:")) {
            String roomId = message.substring(18); // Lấy roomId từ phản hồi server
            System.out.println("Join Room successful, Room ID: " + roomId);
            
            // Mở RoomView khi vào phòng thành công
            RoomView roomView = new RoomView(roomId, false); // false: không phải chủ trì
            new RoomController(roomView, clientSocketManager, clientSocket, false); // false: không phải chủ trì
            roomView.setVisible(true);
            joinRoomView.dispose(); // Đóng giao diện join room

            // Hủy lắng nghe sau khi tham gia thành công
            stopListening();
        } else if (message.startsWith("JOIN_ROOM_FAIL")) {
            System.out.println("Join Room failed: " + message);
            // Có thể thêm thông báo lỗi cho người dùng tại đây
        }
    }
}
