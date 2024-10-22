package Client.controllers;

import Client.ClientSocket;
import Client.manager.ClientSocketManager;
import Client.manager.MessageListener;
import Client.sessions.UserSession;
import Client.views.CreateRoomView;
import Client.views.GuestLoginView;
import Client.views.HomeView;
import Client.views.JoinRoomView;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

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
                CreateRoomView createRoomView = new CreateRoomView();
                new CreateRoomController(createRoomView, clientSocketManager, clientSocket);
                createRoomView.setVisible(true);
                System.out.println("Create Room button clicked");
            });
        }
    }

    // Xử lý sự kiện khi người dùng nhấn nút Join Room
    private class JoinRoomListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            stopListening(); // Hủy lắng nghe trước khi chuyển sang màn hình khác
            SwingUtilities.invokeLater(() -> {
                homeView.dispose();
                JoinRoomView joinRoomView = new JoinRoomView();
                new JoinRoomController(joinRoomView, clientSocketManager, clientSocket);
//                new JoinRoomController(joinRoomView,clientSocket);

                joinRoomView.setVisible(true);
                System.out.println("Join Room button clicked");
            });
        }
    }

    // Xử lý sự kiện khi người dùng nhấn nút Logout
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
            processMessage(message);
        });
    }

    // Phân tích và xử lý tin nhắn từ server
    private void processMessage(String message) {
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
            System.out.println("Ten online: "+chatMessage);
            System.out.println(UserSession.getInstance().getUser().getNickname());
            if (chatMessage.equals(UserSession.getInstance().getUser().getNickname())) {
                homeView.getChatArea().append("Bạn đang online!\n");
            } else {
                homeView.getChatArea().append(chatMessage + " đang online!\n");
            }
        } else if (message.startsWith("OFFLINE:")) {
            String chatMessage = message.substring(7);
            homeView.getChatArea().append(chatMessage + " đã offline!\n");
        }
    }
}
