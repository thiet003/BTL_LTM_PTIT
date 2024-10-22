package Client;

import Client.controllers.GuestLoginController;
import Client.manager.ClientSocketManager;
import Client.views.GuestLoginView;

public class MainClient {
    public static void main(String[] args) {
        try {
            // Khởi tạo client socket và kết nối đến server
            ClientSocket clientSocket = new ClientSocket("localhost", 12345);


            // Tạo một ClientSocketManager mới cho mỗi instance của MainClient
            ClientSocketManager clientSocketManager = new ClientSocketManager(clientSocket);
            clientSocketManager.startReceivingMessages();

            // Khởi tạo giao diện đăng nhập
            GuestLoginView guestLoginView = new GuestLoginView();
            new GuestLoginController(guestLoginView, clientSocketManager, clientSocket);

            // Hiển thị giao diện đăng nhập
            guestLoginView.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
