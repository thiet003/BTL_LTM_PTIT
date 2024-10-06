package Server.handle;

import Server.services.UserService;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private static ArrayList<Socket> clientSockets;

    public ClientHandler(Socket socket, ArrayList<Socket> clientSockets) {
        this.clientSocket = socket;
        ClientHandler.clientSockets = clientSockets;
    }

    @Override
    public void run() {
        DataInputStream in = null;
        DataOutputStream out = null;
        try {
            in = new DataInputStream(clientSocket.getInputStream());
            out = new DataOutputStream(clientSocket.getOutputStream());

            String request;
            while ((request = in.readUTF()) != null) {
                String[] command = request.split(":"); // Yêu cầu theo định dạng "ACTION:DATA"

                if (command[0].equals("LOGIN")) {
                    String data = command[1];
                    String[] dtList = data.split(",");
                    String username = dtList[0];
                    String password = dtList[1];
                    UserService us = new UserService();
                    out.writeUTF(us.authenticateUser(username, password));
                    out.flush();
                } else if (command[0].equals("REGISTER")) {
                    String data = command[1];
                    String[] dtList = data.split(",");
                    String username = dtList[0];
                    String password = dtList[1];
                    String avatar = dtList[2];
                    UserService uss = new UserService();
                    out.writeUTF(uss.registerUser(username, password, avatar));
                    out.flush();
                } else if (command[0].equals("CHAT")) {
                    String username = command[1];
                    String message = command[2];
                    broadcastMessage("CHAT:" + username + ": " + message);
                }
            }
        } catch (EOFException e) {
            // Client closed connection
            System.out.println("Client disconnected: " + clientSocket.getRemoteSocketAddress());
        } catch (IOException e) {
            System.err.println("IOException occurred: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
                if (clientSocket != null) {
                    clientSockets.remove(clientSocket);
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.err.println("Error while closing resources: " + e.getMessage());
            }
        }
    }

    private void broadcastMessage(String message) {
        for (Socket socket : clientSockets) {
            try {
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                out.writeUTF(message);
                out.flush();
            } catch (IOException e) {
                System.err.println("Error broadcasting message: " + e.getMessage());
            }
        }
    }
}
