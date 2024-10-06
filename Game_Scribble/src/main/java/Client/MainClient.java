/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Client;

import Client.controllers.LoginController;
import Client.views.LoginView;



/**
 *
 * @author ADMIN
 */
public class MainClient {
    public static void main(String[] args) {
        LoginView loginView = new LoginView();
        LoginController controller = new LoginController(loginView);
        loginView.setVisible(true);
    }
}
