/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Client.controllers;

import Client.ClientSocket;
import Client.manager.ClientSocketManager;
import Client.manager.MessageListener;
import Server.model.User;
import Client.sessions.UserSession;
import Client.views.*;
import Server.model.Player;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;

public class HomeController implements MessageListener {
    private HomeView homeView;
    private ClientSocketManager clientSocketManager;
    private ClientSocket clientSocket;

    public HomeController(HomeView homeView, ClientSocketManager clientSocketManager, ClientSocket clientSocket) {
        this.homeView = homeView;
        this.clientSocketManager = clientSocketManager;
        this.clientSocket = clientSocket;

        // Đăng ký HomeController làm listener cho các tin nhắn từ server
        clientSocketManager.addMessageListener(this);

        // Đăng ký sự kiện cho các nút trên giao diện
        this.homeView.getCreateRoomButton().addActionListener(new CreateRoomListener());
        this.homeView.getJoinRoomButton().addActionListener(new JoinRoomListener());
        this.homeView.getLogoutButton().addActionListener(new LogoutListener());
        this.homeView.getSendChatButton().addActionListener(new SendChatListener());
    }

    // Hủy bỏ lắng nghe từ ClientSocketManager khi không cần nữa
    private void stopListening() {
        clientSocketManager.removeMessageListener(this);
    }

    // Đóng kết nối ClientSocket khi cần
    private void closeClientSocket() {
        clientSocketManager.stopReceivingMessages(); // Dừng nhận tin nhắn từ server
    }

    // Xử lý sự kiện khi người dùng nhấn nút Create Room
    private class CreateRoomListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            stopListening(); // Hủy lắng nghe trước khi chuyển sang màn hình khác
            SwingUtilities.invokeLater(() -> {
                homeView.dispose();
                SettingsRoomView settingsRoomView = new SettingsRoomView();
                new SettingsRoomController(settingsRoomView, clientSocketManager, clientSocket);
                settingsRoomView.setVisible(true);
                System.out.println("Create Room button clicked");
            });
        }
    }

    // Xử lý sự kiện khi người dùng nhấn nút Join Room
    private class JoinRoomListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            SwingUtilities.invokeLater(() -> {
                showJoinRoomDialog();
                System.out.println("Join Room button clicked");
            });

        }
    }

    // Xử lý sự kiện khi người dùng nhấn nút Logout
    private void showJoinRoomDialog() {
        // Tạo JDialog modal để khóa tương tác với các thành phần khác
        JDialog dialog = new JDialog(homeView, "Join Room", true);
        dialog.setSize(300, 150);
        dialog.setLocationRelativeTo(homeView);
        dialog.setLayout(new GridBagLayout());

        // Tạo các thành phần trong dialog
        JLabel roomIdLabel = new JLabel("Room ID: ");
        JTextField roomIdField = new JTextField(10);

        JButton cancelButton = new JButton("Cancel");
        JButton okButton = new JButton("Join");

        // Đặt hành động khi nhấn nút "Cancel"
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose(); // Đóng dialog
            }
        });

        // Đặt hành động khi nhấn nút "OK"
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String roomId = roomIdField.getText();
                try {
                    // Gửi yêu cầu tham gia phòng
                    User user = UserSession.getInstance().getUser();
                    clientSocket.sendMessage("JOIN_ROOM:" + roomId+":"+user.getUserId()+":"+user.getNickname()+":"+user.getAvatar());
                    System.out.println("Sent JOIN_ROOM request for room ID: " + roomId);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        // Sử dụng GridBagLayout để sắp xếp các thành phần
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Thêm label "Room ID: "
        gbc.gridx = 0;
        gbc.gridy = 0;
        dialog.add(roomIdLabel, gbc);

        // Thêm text field để nhập Room ID
        gbc.gridx = 1;
        dialog.add(roomIdField, gbc);

        // Thêm nút "Cancel"
        gbc.gridx = 0;
        gbc.gridy = 1;
        dialog.add(cancelButton, gbc);

        // Thêm nút "OK"
        gbc.gridx = 1;
        dialog.add(okButton, gbc);

        // Hiển thị dialog
        dialog.setVisible(true);
    }

    private class LogoutListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            stopListening(); // Hủy lắng nghe trước khi chuyển sang màn hình khác
            SwingUtilities.invokeLater(() -> {
                homeView.dispose();
                GuestLoginView guestLoginView = new GuestLoginView();
                closeClientSocket(); // Đóng kết nối client socket
                UserSession.clearSession();
                new GuestLoginController(guestLoginView, clientSocketManager, clientSocket);
                guestLoginView.setVisible(true);
            });
        }
    }

    // Xử lý sự kiện khi người dùng gửi tin nhắn chat
    private class SendChatListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String message = homeView.getChatInputField().getText();
            if (!message.isEmpty()) {
                try {
                    String fullMessage = "CHAT:" + UserSession.getInstance().getUser().getNickname() + ": " + message;
                    clientSocket.sendMessage(fullMessage);
                    homeView.getChatInputField().setText("");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    // Implement phương thức từ MessageListener để nhận tin nhắn từ server
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

    // Phân tích và xử lý tin nhắn từ server
    private void processMessage(String message) throws IOException {
        System.out.println("Du lieu:" + message);
        if (message.startsWith("CHAT:")) {
            String chatMessage = message.substring(5);
            String[] command = chatMessage.split(":");
            if (command[0].equals(UserSession.getInstance().getUser().getNickname())) {
                homeView.getChatArea().append("Tôi: " + command[1] + "\n");
            } else {
                homeView.getChatArea().append(chatMessage + "\n");
            }
        } else if (message.startsWith("ONLINE:")) {
            System.out.println("Hehe online");
            String chatMessage = message.substring(7);
            System.out.println("Ten online: " + chatMessage);
            System.out.println(UserSession.getInstance().getUser().getNickname());
            if (chatMessage.equals(UserSession.getInstance().getUser().getNickname())) {
                homeView.getChatArea().append("Bạn đang online!\n");
            } else {
                homeView.getChatArea().append(chatMessage + " đang online!\n");
            }
        } else if (message.startsWith("OFFLINE:")) {
            String chatMessage = message.substring(7);
            homeView.getChatArea().append(chatMessage + " đã offline!\n");
        } else if (message.startsWith("JOIN_ROOM_SUCCESS:")) {
            String[] parts = message.split(":");
            String roomId = parts[1]; // Lấy roomId từ phản hồi server
            System.out.println("Join Room successful, Room ID: " + roomId);
            // Mở RoomView khi vào phòng thành công
            ArrayList<Player> players = new ArrayList<>();
            for (int i = 2; i < parts.length; i++) {
                String[] playerInfo = parts[i].split(",");
                players.add(new Player(playerInfo[0], playerInfo[1], playerInfo[2], Integer.parseInt(playerInfo[3])));
            }
            RoomView roomView = new RoomView(roomId, false); // false: không phải chủ trì
            RoomController controller = new RoomController(roomView, clientSocketManager, clientSocket, false, players); // false: không phải chủ trì
            roomView.updatePlayersList(players);
            controller.updatePlayersList(players);
            clientSocket.sendMessage("ADD_PLAYER_TO_ROOM:"+roomId);
            roomView.setVisible(true);
            homeView.dispose(); // Đóng giao diện join room
            // Hủy lắng nghe sau khi tham gia thành công
            stopListening();
        }
        else if (message.startsWith("ROOM_NOT_FOUND")) {
            JOptionPane.showMessageDialog(homeView, "Không tìm thấy phòng với ID này!");
        }
        else if(message.startsWith("ROOM_IS_FULL")){
            JOptionPane.showMessageDialog(homeView, "Phòng đã đầy!");
        }
        else if(message.startsWith("ROOM_IS_PLAYING")){
            JOptionPane.showMessageDialog(homeView, "Phòng đang chơi!");
        }
        else if(message.startsWith("ROOM_IS_ENDING")){
            JOptionPane.showMessageDialog(homeView, "Phòng đã kết thúc!");
        }
    }
}
