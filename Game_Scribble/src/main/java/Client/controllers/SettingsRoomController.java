package Client.controllers;

import Client.ClientSocket;
import Client.manager.ClientSocketManager;
import Client.manager.MessageListener;
import Server.model.User;
import Client.sessions.UserSession;
import Client.views.SettingsRoomView;
import Client.views.RoomView;
import Server.model.Player;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;

public class SettingsRoomController implements MessageListener {
    private SettingsRoomView settingsRoomView;
    private ClientSocketManager clientSocketManager;
    private ClientSocket clientSocket;

    public SettingsRoomController(SettingsRoomView settingsRoomView, ClientSocketManager clientSocketManager, ClientSocket clientSocket) {
        this.settingsRoomView = settingsRoomView;
        this.clientSocketManager = clientSocketManager;
        this.clientSocket = clientSocket;

        // Đăng ký listener cho CreateRoomController
        clientSocketManager.addMessageListener(this);

        // Thêm sự kiện khi nhấn nút "Create Room"
        this.settingsRoomView.getCreateRoomButton().addActionListener(new CreateRoomListener());

        // Xử lý sự kiện khi cửa sổ bị đóng
        this.settingsRoomView.addWindowListener(new java.awt.event.WindowAdapter() {
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
            int playerLimit = settingsRoomView.getSelectedPlayerLimit();
            int targetScore = settingsRoomView.getSelectedTargetScore();
            try {
                // Tạo thông điệp yêu cầu tạo phòng
                User user = UserSession.getInstance().getUser();
                String createRoomRequest = "CREATE_ROOM:" + playerLimit + ":" + targetScore + ":" + user.getUserId()+":"+user.getNickname()+":"+user.getAvatar();
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
            String[] parts = message.split(":");
            String roomId = parts[1]; // Lấy roomId từ phản hồi server
            System.out.println("Phòng tạo thành công với mã ID: " + roomId);
            ArrayList<Player> players = new ArrayList<>();
            for(int i = 2; i < parts.length; i++){
                if(parts[i].isEmpty()) continue;
                String[] playerInfo = parts[i].split(",");
                Player player = new Player(playerInfo[0], playerInfo[1], playerInfo[2], Integer.parseInt(playerInfo[3]));
                players.add(player);
            }
            // Chuyển đến giao diện RoomView
            RoomView roomView = new RoomView(roomId, true);
            new RoomController(roomView, clientSocketManager, clientSocket, true, players);
            roomView.updatePlayersList(players);
            roomView.setVisible(true);
            settingsRoomView.dispose(); // Đóng giao diện tạo phòng
        } else {
            System.out.println("Tạo phòng thất bại: " + message);
        }
    }
}
