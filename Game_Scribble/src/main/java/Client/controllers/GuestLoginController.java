package Client.controllers;

import Client.ClientSocket;
import Client.manager.ClientSocketManager;
import Client.manager.MessageListener;
import Client.models.User;
import Client.sessions.UserSession;
import Client.views.GuestLoginView;
import Client.views.HomeView;

import javax.swing.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GuestLoginController implements MessageListener {
    private GuestLoginView guestLoginView;
    private ClientSocket clientSocket;
    private ClientSocketManager clientSocketManager;
    public GuestLoginController(GuestLoginView guestLoginView, ClientSocketManager clientSocketManager, ClientSocket clientSocket) {
        this.guestLoginView = guestLoginView;
        this.clientSocketManager = clientSocketManager;
        this.clientSocket = clientSocket;
        
        // Đăng ký controller này là listener cho các tin nhắn từ server
        this.clientSocketManager.addMessageListener(this);

        // Thêm hành động khi nhấn nút Play
        this.guestLoginView.getPlayButton().addActionListener(e -> loginAsGuest());

        // Thêm hành động khi cửa sổ đóng
        this.guestLoginView.addWindowCloseListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                closeClientSocket();
                System.exit(0);
            }
        });
    }


    // Implement phương thức từ giao diện MessageListener
    @Override
    public void onMessageReceived(String message) {
        // Xử lý tin nhắn nhận được từ server
        if (message.startsWith("ADD_USER")) {
            System.out.println("Khong nhan dc");
            // Giả sử phản hồi từ server là "ADD_USER:SUCCESS" hoặc "ADD_USER:FAILURE"
            String[] response = message.split(":");
            String[] data = response[1].split(",");
            User user = new User(data[0], data[1], data[2]); // Dữ liệu từ server (vd: response[2] là userID)
            UserSession.createSession(user);
            String online = "ONLINE:"+data[1];
            try {
                clientSocket.sendMessage(online);
            } catch (IOException ex) {
                Logger.getLogger(GuestLoginController.class.getName()).log(Level.SEVERE, null, ex);
            }
            // Chuyển sang giao diện HomeView
            showHomeView(data[1], data[2]);
        }
    }

    // Đăng nhập dưới dạng khách
    private void loginAsGuest() {
        String nickname = guestLoginView.getNickname();
        ImageIcon avatarIcon = guestLoginView.getAvatar();
        String avatar = avatarIcon.getDescription();

        if (nickname.equals("")) {
            JOptionPane.showMessageDialog(guestLoginView, "Biệt danh đang để trống!");
            return;
        }

        try {
            // Gửi yêu cầu thêm người dùng đến server
            clientSocket.sendMessage("ADD_USER:" + nickname + "," + avatar);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // Chuyển sang màn hình HomeView
    private void showHomeView(String username, String avatar) {
        SwingUtilities.invokeLater(() -> {
            HomeView homeView = new HomeView(username, avatar);
            new HomeController(homeView,this.clientSocketManager , this.clientSocket); // Truyền clientSocket cho HomeController
            homeView.setVisible(true);
            guestLoginView.setVisible(false);
        });
    }

    // Đóng ClientSocket
    private void closeClientSocket() {
        clientSocketManager.stopReceivingMessages();
    }
}
