/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

import Server.handle.ClientHandler;
import Server.model.GameRoom;

import java.awt.List;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
    private int port;
    private static ArrayList<Socket> clientSockets = new ArrayList<>();
    private static ArrayList<GameRoom> rooms = new ArrayList<>();

    public Server(int port) {
        this.port = port;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept(); // Chấp nhận kết nối từ client
                System.out.println("New client connected: " + clientSocket.getInetAddress());
                  
                // Thêm clientSocket vào danh sách clientSockets
                synchronized (clientSockets) {
                    clientSockets.add(clientSocket);
                }
                
                // Tạo ClientHandler để xử lý client trên một thread riêng
                ClientHandler clientHandler = new ClientHandler(clientSocket, clientSockets, rooms);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

