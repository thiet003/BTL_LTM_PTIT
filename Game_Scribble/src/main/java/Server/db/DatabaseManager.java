package Server.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private static DatabaseManager instance;
    private Connection connection;

    public DatabaseManager() {
        connect();
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    // Kết nối đến MySQL trực tiếp từ thông tin trong code
    private void connect() {
        try {
            // Thông tin kết nối trực tiếp
            String url = "jdbc:mysql://localhost:3306/scribbleit"; // Thay đổi url nếu cần
            String user = "root"; // Thay đổi username nếu cần
            String password = "12345678"; // Thay đổi mật khẩu cho phù hợp

            // Kết nối với MySQL
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to MySQL database.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }
}
