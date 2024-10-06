/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server.model;

import Server.services.UserService;
import java.io.IOException;

public class User {
    private String username;
    private String password;    
    private String avatar;
    private UserService userService;

    public User(String username, String password) throws IOException {
        this.username = username;
        this.password = password;
        this.userService = new UserService();
    }

    // Đăng ký người dùng mới
    public String register() {
        return userService.registerUser(username, password, avatar);
    }

    // Đăng nhập người dùng
    public String login() {
        return userService.authenticateUser(username, password);
    }
}