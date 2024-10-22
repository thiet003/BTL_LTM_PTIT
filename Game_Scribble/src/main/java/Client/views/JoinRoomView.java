package Client.views;

import javax.swing.*;

public class JoinRoomView extends JFrame {
    private JTextField roomIdField;
    private JButton joinRoomButton;

    public JoinRoomView() {
        setTitle("Tham gia phòng chơi");
        setSize(300, 150);
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        roomIdField = new JTextField();
        joinRoomButton = new JButton("Tham gia phòng");

        add(new JLabel("Nhập mã phòng:"));
        add(roomIdField);
        add(joinRoomButton);
    }

    public JTextField getRoomIdField() {
        return roomIdField;
    }

    public JButton getJoinRoomButton() {
        return joinRoomButton;
    }
}
