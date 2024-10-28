package Client.views;

import Server.model.Player;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class PlayerListCellRenderer extends JPanel implements ListCellRenderer<Player> {
    private JLabel avatarLabel;
    private JLabel nicknameLabel;
    private JLabel scoreLabel;

    public PlayerListCellRenderer() {
        setLayout(new BorderLayout());

        avatarLabel = new JLabel();
        avatarLabel.setPreferredSize(new Dimension(50, 50));

        nicknameLabel = new JLabel();
        scoreLabel = new JLabel();

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new GridLayout(2, 1));
        infoPanel.add(nicknameLabel);
        infoPanel.add(scoreLabel);

        add(avatarLabel, BorderLayout.WEST);
        add(infoPanel, BorderLayout.CENTER);
        setBorder(new EmptyBorder(10, 10, 10, 10));
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends Player> list, Player player, int index, boolean isSelected, boolean cellHasFocus) {
        // Hiển thị avatar
        ImageIcon avatarIcon = new ImageIcon("avatars/"+player.getAvatar());
        avatarLabel.setIcon(new ImageIcon(avatarIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH)));

        // Hiển thị tên và điểm số
        nicknameLabel.setText(player.getNickname());
        scoreLabel.setText("Score: " + player.getScore());

        // Đổi màu nền khi chọn
        if (isSelected) {
            setBackground(list.getSelectionBackground());
        } else {
            setBackground(list.getBackground());
        }

        return this;
    }
}