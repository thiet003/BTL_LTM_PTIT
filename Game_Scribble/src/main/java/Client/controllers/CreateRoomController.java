package Client.controllers;

import Client.ClientSocket;
import Client.manager.ClientSocketManager;
import Client.manager.MessageListener;
import Client.views.CreateRoomView;
import Client.views.RoomView;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class CreateRoomController implements MessageListener {
    private CreateRoomView createRoomView;
    private ClientSocketManager clientSocketManager;
    private ClientSocket clientSocket;

    public CreateRoomController(CreateRoomView createRoomView, ClientSocketManager clientSocketManager, ClientSocket clientSocket) {
        this.createRoomView = createRoomView;
        this.clientSocketManager = clientSocketManager;
        this.clientSocket = clientSocket;

        // Đăng ký listener cho CreateRoomController
        clientSocketManager.addMessageListener(this);

        // Thêm sự kiện khi nhấn nút "Create Room"
        this.createRoomView.getCreateRoomButton().addActionListener(new CreateRoomListener());

        // Xử lý sự kiện khi cửa sổ bị đóng
        this.createRoomView.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                stopListening(); // Hủy lắng nghe khi cửa sổ đóng
            }
        });
    }

    // Hủy lắng nghe khi không cần nữa
    private void stopListening() {
        clientSocketManager.removeMessageListener(this);
    }

    // Xử lý sự kiện khi nhấn "Create Room"
    private class CreateRoomListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int playerLimit = (int) createRoomView.getPlayerLimitComboBox().getSelectedItem();
            int targetScore = (int) createRoomView.getTargetScoreComboBox().getSelectedItem();
            try {
                // Tạo thông điệp yêu cầu tạo phòng
                String createRoomRequest = "CREATE_ROOM:" + playerLimit + ":" + targetScore;
                clientSocket.sendMessage(createRoomRequest); // Gửi yêu cầu tạo phòng
                System.out.println("Sent request: " + createRoomRequest);
            } catch (IOException ex) {
                System.out.println("Error sending request: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    // Xử lý tin nhắn nhận được từ server
    @Override
    public void onMessageReceived(String message) {
        SwingUtilities.invokeLater(() -> {
            processMessage(message); // Xử lý tin nhắn trong thread UI
        });
    }

    // Phân tích và xử lý tin nhắn
    private void processMessage(String message) {
        if (message.startsWith("ROOMID:")) {
            String roomId = message.substring(7);
            System.out.println("Phòng tạo thành công với mã ID: " + roomId);

            // Chuyển đến giao diện RoomView
            RoomView roomView = new RoomView(roomId, true);
            new RoomController(roomView, clientSocketManager, clientSocket, true);
            roomView.setVisible(true);
            createRoomView.dispose(); // Đóng giao diện tạo phòng
        } else {
            System.out.println("Tạo phòng thất bại: " + message);
        }
    }
}
