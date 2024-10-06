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
import java.io.IOException;

public class LoginController {
    private LoginView loginView;
    private ClientSocket clientSocket;

    public LoginController(LoginView loginView) {
        this.loginView = loginView;
        this.clientSocket = new ClientSocket("localhost", 12345);
        
        this.loginView.getLoginButton().addActionListener(e -> loginUser());
        this.loginView.getRegisterButton().addActionListener(e -> showRegistrationView());
        this.loginView.addWindowCloseListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                closeClientSocket();
                System.exit(0);
            }
        });
    }
    public LoginController(LoginView loginView, ClientSocket clientSocket) {
        this.loginView = loginView;
        this.clientSocket = clientSocket;
        
        this.loginView.getLoginButton().addActionListener(e -> loginUser());
        this.loginView.getRegisterButton().addActionListener(e -> showRegistrationView());
        this.loginView.addWindowCloseListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                closeClientSocket();
                System.exit(0);
            }
        });
    }
    private void loginUser() {
        String username = loginView.getUsername();
        String password = loginView.getPassword();
        if(username.equals("") || password.equals("")) 
        {
            JOptionPane.showMessageDialog(loginView, "Tên đăng nhập hoặc mật khẩu đang để trống!");
            return;
        }
        try {
            clientSocket.sendMessage("LOGIN:" + username + "," + password);
            
            String response = clientSocket.receiveMessage();
            String[] command = response.split(":");
            String state = command[0];
            if (state.equals("LOGIN_SUCCESS")) {
                String[] data = command[1].split(",");
                String userID = data[0];
                String userName = data[1];
                String avatar = data[2];
                User user = new User(userID, username, avatar);
                UserSession.createSession(user);
                showHomeView(userName, avatar);
            } else {
                JOptionPane.showMessageDialog(loginView, command[1]);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    private void showHomeView(String username, String avatar){
        HomeView homeView = new HomeView(username, avatar);
        new HomeController(homeView, this.clientSocket);
        homeView.setVisible(true);
        loginView.setVisible(false);
    }
    private void showRegistrationView() {
        loginView.setVisible(false);
        RegistrationView registrationView = new RegistrationView();
        System.out.println(this.clientSocket);
        new RegistrationController(registrationView,this.clientSocket);
        registrationView.setVisible(true);
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