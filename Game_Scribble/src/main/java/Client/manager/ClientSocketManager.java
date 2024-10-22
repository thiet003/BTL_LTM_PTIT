package Client.manager;

import Client.ClientSocket;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClientSocketManager {
    private ClientSocket clientSocket;
    private Thread receiveThread;
    private boolean running = true;
    private List<MessageListener> listeners = new ArrayList<>(); // Danh sách các controller lắng nghe tin nhắn

    // Khởi tạo ClientSocketManager với ClientSocket cụ thể
    public ClientSocketManager(ClientSocket clientSocket) {
        this.clientSocket = clientSocket;
    }

    // Khởi tạo luồng nhận tin nhắn
    public void startReceivingMessages() {
        receiveThread = new Thread(() -> {
            try {
                while (running) {
                    String message = clientSocket.receiveMessage();
                    System.out.println("Mess: " + message);
                    if (message != null) {
                        notifyListeners(message); // Gửi tin nhắn đến các controller lắng nghe
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        receiveThread.start();
    }

    // Dừng luồng nhận tin nhắn
    public void stopReceivingMessages() {
        running = false;
        receiveThread.interrupt();
        closeClientSocket();
    }

    // Đăng ký controller lắng nghe tin nhắn
    public void addMessageListener(MessageListener listener) {
        listeners.add(listener);
    }

    // Xóa controller không cần lắng nghe tin nhắn nữa
    public void removeMessageListener(MessageListener listener) {
        listeners.remove(listener);
    }

    // Gửi tin nhắn đến tất cả các listener
    private void notifyListeners(String message) {
        for (MessageListener listener : listeners) {
            listener.onMessageReceived(message);
        }
    }

    // Đóng client socket
    private void closeClientSocket() {
        try {
            if (clientSocket != null && !clientSocket.getSocket().isClosed()) {
                clientSocket.getSocket().close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
