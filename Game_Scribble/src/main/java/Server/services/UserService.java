/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server.services;

import Server.db.DatabaseManager;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserService {
    private Connection connection;

    public UserService() throws IOException {
        // Kết nối với database thông qua DatabaseManager
        connection = DatabaseManager.getInstance().getConnection();
    }

    // Đăng ký người dùng mới
    public String registerUser(String username, String password, String avatar) {
    String checkUserQuery = "SELECT COUNT(*) FROM users WHERE username = ?";
    String insertUserQuery = "INSERT INTO users (username, password_hash, avatar) VALUES (?, ?, ?)";
    
    try (PreparedStatement checkStmt = connection.prepareStatement(checkUserQuery)) {
        // Kiểm tra xem username đã tồn tại chưa
        checkStmt.setString(1, username);
        try (ResultSet rs = checkStmt.executeQuery()) {
            if (rs.next() && rs.getInt(1) > 0) {
                // Username đã tồn tại
                return "REGISTER_FAIL:Tên đăng nhập đã tồn tại!";
            }
        }

        // Thêm người dùng mới vào cơ sở dữ liệu và lấy userId
        try (PreparedStatement insertStmt = connection.prepareStatement(insertUserQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {
            insertStmt.setString(1, username);
            insertStmt.setString(2, password); // Nên mã hóa mật khẩu thực tế
            insertStmt.setString(3, avatar);   // Thêm avatar vào
            
            int rowsInserted = insertStmt.executeUpdate();
            
            if (rowsInserted > 0) {
                // Lấy userId vừa được tạo
                try (ResultSet generatedKeys = insertStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int userId = generatedKeys.getInt(1); // Lấy userId từ cột đầu tiên
                        return String.format("REGISTER_SUCCESS:%d,%s,%s", userId, username, avatar);
                    } else {
                        return "REGISTER_FAIL:Không thể lấy userId sau khi đăng ký!";
                    }
                }
            } else {
                return "REGISTER_FAIL:Không thể thêm người dùng!";
            }
        }
        
    } catch (SQLException e) {
        e.printStackTrace();
        return "REGISTER_FAIL:" + e.toString();
    }
}

    // Xác thực người dùng khi đăng nhập
    public String authenticateUser(String username, String password) {
    String query = "SELECT id, username, avatar FROM users WHERE username = ? AND password_hash = ?";
    
    try (PreparedStatement stmt = connection.prepareStatement(query)) {
        stmt.setString(1, username);
        stmt.setString(2, password);
        
        ResultSet rs = stmt.executeQuery();
        
        if (rs.next()) {
            // Lấy thông tin người dùng từ ResultSet
            int userId = rs.getInt("id");
            String userName = rs.getString("username");
            String avatar = rs.getString("avatar");
            System.out.println(avatar);
            // Trả về chuỗi thông báo đăng nhập thành công
            return String.format("LOGIN_SUCCESS:%d,%s,%s", userId, userName, avatar);
        } else {
            // Người dùng không tồn tại hoặc mật khẩu không đúng
            return "LOGIN_FAIL:Invalid username or password";
        }
    } catch (SQLException e) {
        // Trả về lỗi khi có vấn đề với cơ sở dữ liệu
        return "LOGIN_FAIL:" + e.getMessage();
    }
}
}