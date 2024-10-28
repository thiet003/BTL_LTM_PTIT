package Client.views;

import javax.swing.*;
import java.awt.*;

public class SettingsRoomView extends JFrame {
    private JComboBox<Integer> playerLimitComboBox;
    private JComboBox<Integer> targetScoreComboBox;
    private ButtonGroup themeGroup;

    private JButton createRoomButton;

    public SettingsRoomView() {
        initComponent();
    }

    private void initComponent() {
        setTitle("Setting");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Tạo panel chính với padding
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Padding viền

        // Title ở trên cùng
        JLabel title = new JLabel("Setting", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.add(title, BorderLayout.NORTH);

        // Panel chứa hai tab hiển thị cùng lúc
        JPanel tabPanel = new JPanel();
        tabPanel.setLayout(new BoxLayout(tabPanel, BoxLayout.X_AXIS)); // Sử dụng BoxLayout để chia thành 2 phần ngang

        // Tab trái: Cấu hình
        JPanel configPanel = new JPanel();
        configPanel.setLayout(new BoxLayout(configPanel, BoxLayout.Y_AXIS)); // Sắp xếp theo chiều dọc
        configPanel.setBackground(new Color(220, 220, 220)); // Màu nền xám nhạt cho tab cấu hình
        configPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Padding cho tab
        configPanel.setPreferredSize(new Dimension(290, 300)); // Chiều cao cố định
        configPanel.setMaximumSize(new Dimension(290, 300)); // Đặt chiều cao tối đa

        // Title cho tab Cấu hình
        JLabel configTitle = new JLabel("Config", JLabel.CENTER);
        configTitle.setFont(new Font("Arial", Font.BOLD, 18));
        configPanel.add(configTitle);

        // GroupLayout để tạo layout ngang hàng
        GroupLayout layout = new GroupLayout(configPanel);
        configPanel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        JLabel playerLimitLabel = new JLabel("Limit players:");
        playerLimitComboBox = new JComboBox<>(new Integer[]{4, 5, 6, 7, 8, 9, 10});
        playerLimitComboBox.setPreferredSize(new Dimension(100, 25)); // Giới hạn chiều cao

        JLabel targetScoreLabel = new JLabel("Target point:");
        targetScoreComboBox = new JComboBox<>(new Integer[]{20, 90, 100, 120});
        targetScoreComboBox.setPreferredSize(new Dimension(100, 25)); // Giới hạn chiều cao

        JLabel isPrivateLabel = new JLabel("Is Private:");
        JToggleButton privateSwitch = new JToggleButton("OFF");
        privateSwitch.addItemListener(e -> privateSwitch.setText(privateSwitch.isSelected() ? "ON" : "OFF"));
        privateSwitch.setPreferredSize(new Dimension(100, 25)); // Giới hạn chiều cao

        // Định nghĩa layout ngang hàng cho các thành phần
        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(playerLimitLabel)
                                .addComponent(targetScoreLabel)
                                .addComponent(isPrivateLabel))
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(playerLimitComboBox)
                                .addComponent(targetScoreComboBox)
                                .addComponent(privateSwitch))
        );

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(playerLimitLabel)
                                .addComponent(playerLimitComboBox))
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(targetScoreLabel)
                                .addComponent(targetScoreComboBox))
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(isPrivateLabel)
                                .addComponent(privateSwitch))
        );

        tabPanel.add(configPanel); // Thêm tab cấu hình vào panel tab

        // Tab phải: Danh sách chủ đề
        JPanel themePanel = new JPanel();
        themePanel.setLayout(new BoxLayout(themePanel, BoxLayout.Y_AXIS)); // Sắp xếp theo chiều dọc
        themePanel.setBackground(new Color(255, 235, 205)); // Màu nền cam nhạt cho tab chủ đề
        themePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Padding cho tab
        themePanel.setPreferredSize(new Dimension(290, 300)); // Chiều cao cố định
        themePanel.setMaximumSize(new Dimension(290, 300)); // Đặt chiều cao tối đa

        // Title cho tab Chủ đề
        JLabel themeTitle = new JLabel("Topic", JLabel.CENTER);
        themeTitle.setFont(new Font("Arial", Font.BOLD, 18));
        themePanel.add(themeTitle); // Thêm tiêu đề vào panel chủ đề

        // Tạo ButtonGroup để chỉ cho phép chọn 1 checkbox
        themeGroup = new ButtonGroup();

        // Danh sách chủ đề với checkbox
        JCheckBox animalCheckBox = new JCheckBox("Động vật");
        JCheckBox foodCheckBox = new JCheckBox("Đồ ăn");
        JCheckBox treeCheckBox = new JCheckBox("Cây cối");
        animalCheckBox.setSelected(true);
        // Thêm checkbox vào ButtonGroup
        themeGroup.add(animalCheckBox);
        themeGroup.add(foodCheckBox);
        themeGroup.add(treeCheckBox);

        // Thêm các checkbox vào theme panel
        themePanel.add(animalCheckBox);
        themePanel.add(foodCheckBox);
        themePanel.add(treeCheckBox);

        tabPanel.add(themePanel); // Thêm tab chủ đề vào panel tab

        mainPanel.add(tabPanel, BorderLayout.CENTER); // Thêm panel tab vào panel chính

        // Tạo panel chứa button
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS)); // Sắp xếp theo chiều dọc
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0)); // Thêm khoảng cách phía trên

        // Button Tạo phòng
        createRoomButton = new JButton("Tạo phòng");
        createRoomButton.setAlignmentX(Component.CENTER_ALIGNMENT); // Canh giữa
        buttonPanel.add(createRoomButton); // Thêm button vào panel

        mainPanel.add(buttonPanel, BorderLayout.SOUTH); // Thêm panel chứa button vào panel chính
        add(mainPanel);

        setVisible(true);
    }

    public int getSelectedPlayerLimit() {
        // if null return 0
        return playerLimitComboBox.getSelectedItem() == null ? 0 : (int) playerLimitComboBox.getSelectedItem();
    }

    // Phương thức để lấy giá trị từ targetScoreComboBox
    public int getSelectedTargetScore() {
        return targetScoreComboBox.getSelectedItem() == null ? 0 : (int) targetScoreComboBox.getSelectedItem();
    }

    // Phương thức để lấy giá trị chủ đề (topic) từ themeGroup
    public String getSelectedTopic() {
        return themeGroup.getSelection().getActionCommand();
    }

    public JButton getCreateRoomButton() {
        return createRoomButton;
    }


    public void addWindowCloseListener(java.awt.event.WindowAdapter adapter) {
        addWindowListener(adapter);
    }
}
