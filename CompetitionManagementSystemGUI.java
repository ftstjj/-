package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;

public class CompetitionManagementSystemGUI {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/competition_db?useSSL=false&serverTimezone=Asia/Shanghai";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "201205";

    private static JTextArea displayArea;
    private static JFrame frame;
    private static JPanel mainPanel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> showLoginDialog());
    }

    // 登录页面
    private static void showLoginDialog() {
        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new GridLayout(2, 2));

        JLabel usernameLabel = new JLabel("用户名:");
        JTextField usernameField = new JTextField("root");
        JLabel passwordLabel = new JLabel("密码:");
        JPasswordField passwordField = new JPasswordField("201205");

        loginPanel.add(usernameLabel);
        loginPanel.add(usernameField);
        loginPanel.add(passwordLabel);
        loginPanel.add(passwordField);

        int option = JOptionPane.showConfirmDialog(null, loginPanel, "登录", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            if (username.equals("root") && password.equals("201205")) {
                showMainWindow();
            } else {
                JOptionPane.showMessageDialog(null, "用户名或密码错误！");
                System.exit(0);
            }
        }
    }

    // 主窗口
    private static void showMainWindow() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            frame = new JFrame("学生科技竞赛管理系统");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 500);
            frame.setLocationRelativeTo(null);

            mainPanel = new JPanel() {
                private Image backgroundImage = new ImageIcon("D:\\java\\untitled1\\OIP-C.jpg").getImage();

                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);

                    // 获取面板的尺寸
                    int panelWidth = getWidth();
                    int panelHeight = getHeight();

                    // 获取图片的尺寸
                    int imageWidth = backgroundImage.getWidth(this);
                    int imageHeight = backgroundImage.getHeight(this);

                    // 计算背景图片的绘制位置，使其居中
                    int x = (panelWidth - imageWidth) / 2;
                    int y = (panelHeight - imageHeight) / 2;

                    try {
                        // 绘制居中的背景图片
                        g.drawImage(backgroundImage, x, y, this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            mainPanel.setLayout(new BorderLayout());

            displayArea = new JTextArea();
            displayArea.setEditable(false);
            mainPanel.add(new JScrollPane(displayArea), BorderLayout.CENTER);

            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new GridLayout(9, 1)); // 修改为 9 行

            JButton addButton = new JButton("录入学生竞赛信息");
            JButton displayButton = new JButton("查看所有竞赛信息");
            JButton searchButton = new JButton("按学号或姓名查询竞赛结果");
            JButton editButton = new JButton("修改学生竞赛信息");
            JButton deleteButton = new JButton("删除学生竞赛信息");
            JButton batchAddButton = new JButton("批量录入学生竞赛信息");
            JButton settingsButton = new JButton("设置"); // 新增设置按钮
            JButton exitButton = new JButton("退出");

            buttonPanel.add(addButton);
            buttonPanel.add(displayButton);
            buttonPanel.add(searchButton);
            buttonPanel.add(editButton);
            buttonPanel.add(deleteButton);
            buttonPanel.add(batchAddButton);
            buttonPanel.add(settingsButton); // 添加设置按钮
            buttonPanel.add(exitButton);

            mainPanel.add(buttonPanel, BorderLayout.WEST);
            frame.add(mainPanel);
            frame.setVisible(true);

            addButton.addActionListener(e -> showAddCompetitionDialog(connection));
            displayButton.addActionListener(e -> displayAllCompetitions(connection));
            searchButton.addActionListener(e -> showSearchDialog(connection));
            editButton.addActionListener(e -> showEditCompetitionDialog(connection));
            deleteButton.addActionListener(e -> showDeleteCompetitionDialog(connection));
            batchAddButton.addActionListener(e -> showBatchAddCompetitionDialog(connection));
            exitButton.addActionListener(e -> System.exit(0));

            // 设置按钮的点击事件
            settingsButton.addActionListener(e -> showSettingsDialog(connection));

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "数据库连接失败: " + e.getMessage());
        }
    }

    // 设置按钮弹出的下拉菜单
    private static void showSettingsDialog(Connection connection) {
        // 创建下拉菜单
        String[] options = {"修改密码", "查看作者信息"};
        int choice = JOptionPane.showOptionDialog(
                null,
                "请选择操作:",
                "设置",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 0) {
            // 修改密码
            showChangePasswordDialog(connection);
        } else if (choice == 1) {
            // 显示作者信息
            showAuthorInfoDialog();
        }
    }

    // 修改密码功能
    private static void showChangePasswordDialog(Connection connection) {
        JTextField oldPasswordField = new JTextField();
        JTextField newPasswordField = new JTextField();
        Object[] fields = {
                "旧密码:", oldPasswordField,
                "新密码:", newPasswordField
        };

        int option = JOptionPane.showConfirmDialog(null, fields, "修改密码", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String oldPassword = oldPasswordField.getText();
            String newPassword = newPasswordField.getText();

            if (!oldPassword.equals(DB_PASSWORD)) {
                JOptionPane.showMessageDialog(null, "旧密码不正确！");
                return;
            }

            String updateSQL = "UPDATE users SET password = ? WHERE username = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(updateSQL)) {
                preparedStatement.setString(1, newPassword);
                preparedStatement.setString(2, DB_USER);
                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(null, "密码修改成功！");
                } else {
                    JOptionPane.showMessageDialog(null, "密码修改失败！");
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "数据库操作失败: " + e.getMessage());
            }
        }
    }

    // 显示作者信息
    private static void showAuthorInfoDialog() {
        String authorInfo = "作者: 石鑫\n学号: 241305436\n创作日期: 2024/12/15";
        JOptionPane.showMessageDialog(null, authorInfo, "作者信息", JOptionPane.INFORMATION_MESSAGE);
    }

    // 批量录入功能
    private static void showBatchAddCompetitionDialog(Connection connection) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择批量录入文件");

        int userSelection = fileChooser.showOpenDialog(null);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                List<String[]> data = readDataFromFile(selectedFile);
                batchInsertCompetitions(connection, data);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "文件读取失败: " + e.getMessage());
            }
        }
    }

    private static List<String[]> readDataFromFile(File file) throws IOException {
        List<String[]> data = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields.length == 5) {
                    data.add(fields);
                }
            }
        }
        return data;
    }

    private static void batchInsertCompetitions(Connection connection, List<String[]> data) {
        String insertSQL = "INSERT INTO competitions (student_id, student_name, competition_name, result, year) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {
            for (String[] fields : data) {
                preparedStatement.setString(1, fields[0]);
                preparedStatement.setString(2, fields[1]);
                preparedStatement.setString(3, fields[2]);
                preparedStatement.setString(4, fields[3]);
                preparedStatement.setInt(5, Integer.parseInt(fields[4]));
                preparedStatement.addBatch();
            }

            int[] results = preparedStatement.executeBatch();
            int successCount = 0;
            for (int result : results) {
                if (result == PreparedStatement.SUCCESS_NO_INFO) {
                    successCount++;
                }
            }

            JOptionPane.showMessageDialog(null, "录入成功！");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "批量录入失败: " + e.getMessage());
        }
    }

    // 录入学生竞赛信息
    private static void showAddCompetitionDialog(Connection connection) {
        JTextField studentIdField = new JTextField();
        JTextField studentNameField = new JTextField();
        JTextField competitionNameField = new JTextField();
        JTextField resultField = new JTextField();
        JTextField yearField = new JTextField();

        Object[] fields = {
                "学生学号:", studentIdField,
                "学生姓名:", studentNameField,
                "竞赛名称:", competitionNameField,
                "竞赛结果:", resultField,
                "竞赛年份:", yearField
        };

        int option = JOptionPane.showConfirmDialog(null, fields, "录入学生竞赛信息", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            try {
                String studentId = studentIdField.getText();
                String studentName = studentNameField.getText();
                String competitionName = competitionNameField.getText();
                String result = resultField.getText();
                int year = Integer.parseInt(yearField.getText());

                String insertSQL = "INSERT INTO competitions (student_id, student_name, competition_name, result, year) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {
                    preparedStatement.setString(1, studentId);
                    preparedStatement.setString(2, studentName);
                    preparedStatement.setString(3, competitionName);
                    preparedStatement.setString(4, result);
                    preparedStatement.setInt(5, year);
                    preparedStatement.executeUpdate();
                    JOptionPane.showMessageDialog(null, "学生竞赛信息录入成功！");
                }
            } catch (SQLException | NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "录入失败: " + e.getMessage());
            }
        }
    }

    // 显示所有竞赛信息
    private static void displayAllCompetitions(Connection connection) {
        String querySQL = "SELECT * FROM competitions";
        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(querySQL)) {
            StringBuilder resultText = new StringBuilder();
            resultText.append("=== 所有学生竞赛信息 ===\n");
            while (resultSet.next()) {
                resultText.append(String.format("学号: %s, 姓名: %s, 竞赛: %s, 成绩: %s, 年份: %d\n",
                        resultSet.getString("student_id"),
                        resultSet.getString("student_name"),
                        resultSet.getString("competition_name"),
                        resultSet.getString("result"),
                        resultSet.getInt("year")));
            }
            displayArea.setText(resultText.toString());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "查询失败: " + e.getMessage());
        }
    }

    // 查询竞赛信息
    private static void showSearchDialog(Connection connection) {
        JTextField searchField = new JTextField();
        Object[] fields = {"请输入学号或姓名进行查询:", searchField};
        int option = JOptionPane.showConfirmDialog(null, fields, "查询学生竞赛信息", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            String searchText = searchField.getText();
            String querySQL = "SELECT * FROM competitions WHERE student_id LIKE ? OR student_name LIKE ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(querySQL)) {
                preparedStatement.setString(1, "%" + searchText + "%");
                preparedStatement.setString(2, "%" + searchText + "%");
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    StringBuilder resultText = new StringBuilder();
                    resultText.append("=== 查询结果 ===\n");
                    while (resultSet.next()) {
                        resultText.append(String.format("学号: %s, 姓名: %s, 竞赛: %s, 成绩: %s, 年份: %d\n",
                                resultSet.getString("student_id"),
                                resultSet.getString("student_name"),
                                resultSet.getString("competition_name"),
                                resultSet.getString("result"),
                                resultSet.getInt("year")));
                    }
                    displayArea.setText(resultText.toString());
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "查询失败: " + e.getMessage());
            }
        }
    }

    // 修改学生竞赛信息
    private static void showEditCompetitionDialog(Connection connection) {
        JTextField studentIdField = new JTextField();
        Object[] fields = {"请输入要编辑的学生学号:", studentIdField};
        int option = JOptionPane.showConfirmDialog(null, fields, "编辑学生竞赛信息", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            String studentId = studentIdField.getText();
            String querySQL = "SELECT * FROM competitions WHERE student_id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(querySQL)) {
                preparedStatement.setString(1, studentId);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        JTextField studentNameField = new JTextField(resultSet.getString("student_name"));
                        JTextField competitionNameField = new JTextField(resultSet.getString("competition_name"));
                        JTextField resultField = new JTextField(resultSet.getString("result"));
                        JTextField yearField = new JTextField(String.valueOf(resultSet.getInt("year")));

                        Object[] editFields = {
                                "学生姓名:", studentNameField,
                                "竞赛名称:", competitionNameField,
                                "竞赛结果:", resultField,
                                "竞赛年份:", yearField
                        };

                        int editOption = JOptionPane.showConfirmDialog(null, editFields, "修改学生竞赛信息", JOptionPane.OK_CANCEL_OPTION);
                        if (editOption == JOptionPane.OK_OPTION) {
                            String updateSQL = "UPDATE competitions SET student_name = ?, competition_name = ?, result = ?, year = ? WHERE student_id = ?";
                            try (PreparedStatement updateStatement = connection.prepareStatement(updateSQL)) {
                                updateStatement.setString(1, studentNameField.getText());
                                updateStatement.setString(2, competitionNameField.getText());
                                updateStatement.setString(3, resultField.getText());
                                updateStatement.setInt(4, Integer.parseInt(yearField.getText()));
                                updateStatement.setString(5, studentId);
                                updateStatement.executeUpdate();
                                JOptionPane.showMessageDialog(null, "学生竞赛信息修改成功！");
                            }
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "未找到该学号的竞赛记录！");
                    }
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "编辑失败: " + e.getMessage());
            }
        }
    }

    // 删除学生竞赛信息
    private static void showDeleteCompetitionDialog(Connection connection) {
        JTextField studentIdField = new JTextField();
        Object[] fields = {"请输入要删除的学生学号:", studentIdField};
        int option = JOptionPane.showConfirmDialog(null, fields, "删除学生竞赛信息", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            String studentId = studentIdField.getText();
            String deleteSQL = "DELETE FROM competitions WHERE student_id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(deleteSQL)) {
                preparedStatement.setString(1, studentId);
                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(null, "学生竞赛信息删除成功！");
                } else {
                    JOptionPane.showMessageDialog(null, "未找到该学号的竞赛记录！");
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "删除失败: " + e.getMessage());
            }
        }
    }
}
