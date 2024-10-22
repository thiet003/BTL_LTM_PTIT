/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Client;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

/**
 *
 * @author ADMIN
 */
public class ClientSocket {
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;

    public ClientSocket(String host, int port) {
        try {
            this.socket = new Socket(host, port);
            this.out = new DataOutputStream(socket.getOutputStream());
            this.in = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) throws IOException {
        out.writeUTF(message);
        out.flush();
    }

    public String receiveMessage() throws IOException {
        return in.readUTF();
    }

    public void close() {
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public DataOutputStream getOut() {
        return out;
    }

    public void setOut(DataOutputStream out) {
        this.out = out;
    }

    public DataInputStream getIn() {
        return in;
    }

    public void setIn(DataInputStream in) {
        this.in = in;
    }
    public void closeCon() throws IOException {
        this.socket.close();
        this.in.close();
        this.out.close();
    }

    public String sendCreateRoom(int playerLimit, int targetScore) {
        try {
            // Tạo thông điệp yêu cầu tạo phòng
            String createRoomRequest = "CREATE_ROOM:" + playerLimit + ":" + targetScore;
            sendMessage(createRoomRequest); // Gửi yêu cầu tạo phòng

            // Nhận phản hồi từ server - ID của phòng
            String response = receiveMessage();
            System.out.println(response);

            if (response.startsWith("ROOM_ID:")) {
                // Server trả về ID phòng dưới dạng "ROOM_ID:<id>"
                String roomId = response.substring(8); // Lấy phần ID từ thông báo
                return roomId; // Trả về mã phòng
            } else {
                System.out.println("Tạo phòng thất bại: " + response);
                return null; // Trả về null nếu không có phản hồi hợp lệ
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
