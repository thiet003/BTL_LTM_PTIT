/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Client.controllers;

import Client.ClientSocket;
import Client.models.User;
import Client.sessions.UserSession;
import Client.views.HomeView;
import Client.views.LoginView;
import Client.views.RegistrationView;
import javax.swing.*;
import java.io.*;
import java.net.Socket;
/**
 *
 * @author ADMIN
 */

public class RegistrationController {
    private RegistrationView registrationView;
    private ClientSocket clientSocket;

    public RegistrationController(RegistrationView registrationView, ClientSocket clientSocket) {
        this.registrationView = registrationView;
        this.clientSocket = clientSocket;
        System.out.println(clientSocket);
        this.registrationView.getRegisterButton().addActionListener(e -> registerUser());
        this.registrationView.getLoginButton().addActionListener(e -> showLoginView());
        this.registrationView.addWindowCloseListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                closeClientSocket();
                System.exit(0);
            }
        });
    }

    private void registerUser() {
        String username = registrationView.getUsername();
        String password = registrationView.getPassword();
        String retypePassword = registrationView.getRetypePassword();
        ImageIcon avatarIcon = registrationView.getAvatar();
        String avatar = avatarIcon.getDescription();
        if(username.equals("") || password.equals("") || retypePassword.equals("")){
            JOptionPane.showMessageDialog(registrationView, "Đang có trường bị để trống!");
            return;
        }
        if(!password.equals(retypePassword))
        {
            JOptionPane.showMessageDialog(registrationView, "Hai mật khẩu không trùng nhau!");
            return;
        }
        User user = new User(username, password, avatar);
        try {
            clientSocket.sendMessage("REGISTER:" + username + "," + password + "," + avatar);

            String response = clientSocket.receiveMessage();
            String[] command = response.split(":");
            if (command[0].equals("REGISTER_SUCCESS")) {
                String[] data = command[1].split(",");
                String userID = data[0];
                String userName = data[1];
                String avatarRes = data[2];
                System.out.println(command[1]);
                User userRes = new User(userID, username, avatar);
                UserSession.createSession(userRes);
                showHomeView(userName, avatarRes);
            } else {
                JOptionPane.showMessageDialog(registrationView, command[1]);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    private void showHomeView(String username, String avatar){
        HomeView homeView = new HomeView(username, avatar);
        new HomeController(homeView, this.clientSocket);
        homeView.setVisible(true);
        registrationView.setVisible(false);
    }
    private void showLoginView() {
        registrationView.setVisible(false);
        LoginView loginView = new LoginView();
        new LoginController(loginView, clientSocket);
        loginView.setVisible(true);
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
