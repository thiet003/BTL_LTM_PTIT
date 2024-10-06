/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Client.controllers;

import Client.ClientSocket;
import Client.sessions.UserSession;
import Client.views.HomeView;
import Client.views.LoginView;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class HomeController {
    private HomeView homeView;
    private ClientSocket clientSocket;

    public HomeController(HomeView homeView, ClientSocket clientSocket) {
        this.homeView = homeView;
        this.clientSocket = clientSocket;

        this.homeView.getCreateRoomButton().addActionListener(new CreateRoomListener());
        this.homeView.getJoinRoomButton().addActionListener(new JoinRoomListener());
        this.homeView.getLogoutButton().addActionListener(new LogoutListener());
        this.homeView.getSendChatButton().addActionListener(new SendChatListener());
        this.homeView.addWindowCloseListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                closeClientSocket();
                System.exit(0);
            }
        });
        // Start a new thread to receive messages from the server
        new Thread(new ReceiveMessagesTask()).start();
    }

    private class CreateRoomListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // Handle create room logic here
            System.out.println("Create Room button clicked");
        }
    }

    private class JoinRoomListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // Handle join room logic here
            System.out.println("Join Room button clicked");
        }
    }

    private class LogoutListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            UserSession.clearSession();
            homeView.dispose();
            LoginView loginView = new LoginView();
            new LoginController(loginView, clientSocket);
            loginView.setVisible(true);
        }
    }

    private class SendChatListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // Handle sending chat message logic here
            String message = homeView.getChatInputField().getText();
            System.out.println(message);
            if (!message.isEmpty()) {
                try {
                    String fullMessage = "CHAT:" + UserSession.getInstance().getUser().getUsername() + ": " + message;
                    clientSocket.sendMessage(fullMessage);
                    System.out.println(fullMessage);
                    homeView.getChatInputField().setText("");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private class ReceiveMessagesTask implements Runnable {
        @Override
        public void run() {
            try {
                while (true) {
                    String message = clientSocket.receiveMessage();
                    if (message.startsWith("CHAT:")) {
                        String chatMessage = message.substring(5);
                        String[] command = chatMessage.split(":");
                        if(command[0].equals(UserSession.getInstance().getUser().getUsername()))
                        {
                            homeView.getChatArea().append("Tôi:"+command[1] + "\n");
                        }
                        else homeView.getChatArea().append(chatMessage + "\n");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void closeClientSocket() {
        try {
            if (clientSocket != null && !clientSocket.getSocket().isClosed()) {
                clientSocket.getSocket().close();
                clientSocket.getOut().close();
                clientSocket.getIn().close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

