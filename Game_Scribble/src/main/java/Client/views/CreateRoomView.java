package Client.views;

import javax.swing.*;
import java.awt.*;

public class CreateRoomView extends JFrame {
    private JComboBox<Integer> playerLimitComboBox;
    private JComboBox<Integer> targetScoreComboBox;
    private JButton createRoomButton;

    public CreateRoomView() {
        setTitle("Tạo phòng chơi");
        setSize(300, 200);
        setLayout(new GridLayout(3, 2));

        JLabel playerLimitLabel = new JLabel("Giới hạn người chơi:");
        playerLimitComboBox = new JComboBox<>(new Integer[]{4, 5, 6, 7, 8, 9, 10});

        JLabel targetScoreLabel = new JLabel("Mục tiêu điểm:");
        targetScoreComboBox = new JComboBox<>(new Integer[]{80, 90, 100, 120});

        createRoomButton = new JButton("Tạo phòng");

        add(playerLimitLabel);
        add(playerLimitComboBox);
        add(targetScoreLabel);
        add(targetScoreComboBox);
        add(new JLabel()); // empty space
        add(createRoomButton);
    }

    public JComboBox<Integer> getPlayerLimitComboBox() {
        return playerLimitComboBox;
    }

    public JComboBox<Integer> getTargetScoreComboBox() {
        return targetScoreComboBox;
    }

    public JButton getCreateRoomButton() {
        return createRoomButton;
    }
}
