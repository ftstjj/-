package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.*;

public class CompetitionManagementSystemGUI {

    // 数据库连接信息
    private static final String DB_URL = "jdbc:mysql://localhost:3306/competition_db?useSSL=false&serverTimezone=Asia/Shanghai";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "201205";

    private static JTable competitionTable;  // 使用 JTable 显示竞赛信息
    private static JLabel infoLabel;         // 显示当前操作状态
    private static JFrame frame;             // 主窗口
    private static JPanel mainPanel;         // 主面板

    public static void main(String[] args) {
        // 使用 SwingUtilities 确保在事件派发线程中启动程序
        SwingUtilities.invokeLater(() -> showLoginDialog());
    }

    // 登录页面
    private static void showLoginDialog() {
        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new GridLayout(2, 2, 10, 10)); // 增加组件之间的间距，优化布局

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
            // 连接数据库
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            // 创建主窗口
            frame = new JFrame("学生科技竞赛管理系统");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 500);
            frame.setLocationRelativeTo(null); // 窗口居中显示

            // 设置主面板
            mainPanel = new JPanel();
            mainPanel.setLayout(new BorderLayout());

            // 创建表格模型，列名和数据
            String[] columnNames = {"学号", "姓名", "竞赛名称", "竞赛成绩", "竞赛年份"};
            DefaultTableModel tableModel = new DefaultTableModel(null, columnNames);
            competitionTable = new JTable(tableModel);
            competitionTable.setFillsViewportHeight(true); // 填充整个视口

            // 将表格包装进滚动面板
            JScrollPane scrollPane = new JScrollPane(competitionTable);
            mainPanel.add(scrollPane, BorderLayout.CENTER);

            // 按钮面板，用于选择操作
            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new GridLayout(1, 1));

            // 创建一个下拉框，包含各种操作选项
            String[] options = {"录入竞赛信息", "查看所有竞赛信息", "按学号或姓名查询竞赛结果", "修改竞赛信息", "删除竞赛信息", "批量录入竞赛信息", "修改密码", "退出"};
            JComboBox<String> comboBox = new JComboBox<>(options);
            buttonPanel.add(comboBox);

            // 将按钮面板添加到主面板的顶部
            mainPanel.add(buttonPanel, BorderLayout.NORTH);

            // 信息标签，显示当前操作状态
            infoLabel = new JLabel("请选择一个操作...");
            mainPanel.add(infoLabel, BorderLayout.SOUTH);

            // 添加主面板到窗口并显示
            frame.add(mainPanel);
            frame.setVisible(true);

            // 为下拉框添加操作监听器
            comboBox.addActionListener(e -> {
                String selectedOption = (String) comboBox.getSelectedItem();
                switch (selectedOption) {
                    case "录入竞赛信息":
                        showAddCompetitionDialog(connection);
                        break;
                    case "查看所有竞赛信息":
                        displayAllCompetitions(connection);
                        break;
                    case "按学号或姓名查询竞赛结果":
                        showSearchDialog(connection);
                        break;
                    case "修改竞赛信息":
                        showEditCompetitionDialog(connection);
                        break;
                    case "删除竞赛信息":
                        showDeleteCompetitionDialog(connection);
                        break;
                    case "批量录入竞赛信息":
                        showBatchAddCompetitionDialog(connection);
                        break;
                    case "修改密码":
                        showChangePasswordDialog(connection);
                        break;
                    case "退出":
                        System.exit(0);
                        break;
                }
            });

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "数据库连接失败: " + e.getMessage());
        }
    }

    // 显示所有竞赛信息
    private static void displayAllCompetitions(Connection connection) {
        String selectSQL = "SELECT * FROM competitions";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(selectSQL)) {
            DefaultTableModel tableModel = (DefaultTableModel) competitionTable.getModel();
            tableModel.setRowCount(0); // 清空当前表格数据

            while (resultSet.next()) {
                String studentId = resultSet.getString("student_id");
                String studentName = resultSet.getString("student_name");
                String competitionName = resultSet.getString("competition_name");
                String result = resultSet.getString("result");
                int year = resultSet.getInt("year");

                tableModel.addRow(new Object[]{studentId, studentName, competitionName, result, year});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "查询失败: " + e.getMessage());
        }
    }

    // 显示按学号或姓名查询竞赛信息的对话框
    private static void showSearchDialog(Connection connection) {
        JTextField searchField = new JTextField();
        Object[] searchFields = {"请输入学号或姓名查询:", searchField};

        int option = JOptionPane.showConfirmDialog(null, searchFields, "查询竞赛信息", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String searchTerm = searchField.getText();
            String searchSQL = "SELECT * FROM competitions WHERE student_id LIKE ? OR student_name LIKE ?";

            try (PreparedStatement preparedStatement = connection.prepareStatement(searchSQL)) {
                preparedStatement.setString(1, "%" + searchTerm + "%");
                preparedStatement.setString(2, "%" + searchTerm + "%");

                ResultSet resultSet = preparedStatement.executeQuery();
                DefaultTableModel tableModel = (DefaultTableModel) competitionTable.getModel();
                tableModel.setRowCount(0); // 清空当前表格数据

                while (resultSet.next()) {
                    String studentId = resultSet.getString("student_id");
                    String studentName = resultSet.getString("student_name");
                    String competitionName = resultSet.getString("competition_name");
                    String result = resultSet.getString("result");
                    int year = resultSet.getInt("year");

                    tableModel.addRow(new Object[]{studentId, studentName, competitionName, result, year});
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "查询失败: " + e.getMessage());
            }
        }
    }

    // 显示录入竞赛信息的对话框
    private static void showAddCompetitionDialog(Connection connection) {
        JTextField studentIdField = new JTextField();
        JTextField studentNameField = new JTextField();
        JTextField competitionNameField = new JTextField();
        JTextField resultField = new JTextField();
        JTextField yearField = new JTextField();

        Object[] fields = {
                "学号:", studentIdField,
                "姓名:", studentNameField,
                "竞赛名称:", competitionNameField,
                "竞赛成绩:", resultField,
                "竞赛年份:", yearField
        };

        int option = JOptionPane.showConfirmDialog(null, fields, "录入竞赛信息", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String studentId = studentIdField.getText();
            String studentName = studentNameField.getText();
            String competitionName = competitionNameField.getText();
            String result = resultField.getText();
            int year = Integer.parseInt(yearField.getText());

            // 插入竞赛信息的 SQL
            String insertSQL = "INSERT INTO competitions (student_id, student_name, competition_name, result, year) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {
                preparedStatement.setString(1, studentId);
                preparedStatement.setString(2, studentName);
                preparedStatement.setString(3, competitionName);
                preparedStatement.setString(4, result);
                preparedStatement.setInt(5, year);
                preparedStatement.executeUpdate();
                JOptionPane.showMessageDialog(null, "竞赛信息录入成功！");
                infoLabel.setText("竞赛信息录入成功！");
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "录入失败: " + e.getMessage());
                infoLabel.setText("录入失败: " + e.getMessage());
            }
        }
    }

    // 显示修改竞赛信息的对话框
    private static void showEditCompetitionDialog(Connection connection) {
        JTextField studentIdField = new JTextField();
        JTextField competitionNameField = new JTextField();
        JTextField resultField = new JTextField();

        Object[] fields = {
                "请输入学号修改竞赛信息:", studentIdField,
                "请输入竞赛名称修改信息:", competitionNameField,
                "请输入新的竞赛成绩:", resultField
        };

        int option = JOptionPane.showConfirmDialog(null, fields, "修改竞赛信息", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            String studentId = studentIdField.getText();
            String competitionName = competitionNameField.getText();
            String newResult = resultField.getText();

            String updateSQL = "UPDATE competitions SET result = ? WHERE student_id = ? AND competition_name = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(updateSQL)) {
                preparedStatement.setString(1, newResult);
                preparedStatement.setString(2, studentId);
                preparedStatement.setString(3, competitionName);

                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(null, "竞赛信息修改成功！");
                    infoLabel.setText("竞赛信息修改成功！");
                } else {
                    JOptionPane.showMessageDialog(null, "未找到符合条件的竞赛信息！");
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "修改失败: " + e.getMessage());
                infoLabel.setText("修改失败: " + e.getMessage());
            }
        }
    }

    // 显示删除竞赛信息的对话框
    private static void showDeleteCompetitionDialog(Connection connection) {
        JTextField studentIdField = new JTextField();
        JTextField competitionNameField = new JTextField();

        Object[] fields = {
                "请输入学号删除竞赛信息:", studentIdField,
                "请输入竞赛名称删除信息:", competitionNameField
        };

        int option = JOptionPane.showConfirmDialog(null, fields, "删除竞赛信息", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            String studentId = studentIdField.getText();
            String competitionName = competitionNameField.getText();

            String deleteSQL = "DELETE FROM competitions WHERE student_id = ? AND competition_name = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(deleteSQL)) {
                preparedStatement.setString(1, studentId);
                preparedStatement.setString(2, competitionName);

                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(null, "竞赛信息删除成功！");
                    infoLabel.setText("竞赛信息删除成功！");
                } else {
                    JOptionPane.showMessageDialog(null, "未找到符合条件的竞赛信息！");
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "删除失败: " + e.getMessage());
                infoLabel.setText("删除失败: " + e.getMessage());
            }
        }
    }

    // 显示批量导入竞赛信息的对话框
    private static void showBatchAddCompetitionDialog(Connection connection) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择批量导入的CSV文件");
        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try (BufferedReader br = new BufferedReader(new FileReader(selectedFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] data = line.split(",");
                    if (data.length == 5) {
                        String studentId = data[0].trim();
                        String studentName = data[1].trim();
                        String competitionName = data[2].trim();
                        String resultField = data[3].trim();
                        int year = Integer.parseInt(data[4].trim());

                        String insertSQL = "INSERT INTO competitions (student_id, student_name, competition_name, result, year) VALUES (?, ?, ?, ?, ?)";
                        try (PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {
                            preparedStatement.setString(1, studentId);
                            preparedStatement.setString(2, studentName);
                            preparedStatement.setString(3, competitionName);
                            preparedStatement.setString(4, resultField);
                            preparedStatement.setInt(5, year);
                            preparedStatement.executeUpdate();
                        }
                    }
                }
                JOptionPane.showMessageDialog(null, "批量导入成功！");
            } catch (IOException | SQLException e) {
                JOptionPane.showMessageDialog(null, "批量导入失败: " + e.getMessage());
            }
        }
    }

    // 显示修改密码的对话框
    private static void showChangePasswordDialog(Connection connection) {
        JTextField oldPasswordField = new JTextField();
        JTextField newPasswordField = new JTextField();
        JTextField confirmNewPasswordField = new JTextField();

        Object[] fields = {
                "请输入旧密码:", oldPasswordField,
                "请输入新密码:", newPasswordField,
                "请确认新密码:", confirmNewPasswordField
        };

        int option = JOptionPane.showConfirmDialog(null, fields, "修改密码", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String oldPassword = oldPasswordField.getText();
            String newPassword = newPasswordField.getText();
            String confirmNewPassword = confirmNewPasswordField.getText();

            if (newPassword.equals(confirmNewPassword)) {
                String updateSQL = "UPDATE users SET password = ? WHERE password = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(updateSQL)) {
                    preparedStatement.setString(1, newPassword);
                    preparedStatement.setString(2, oldPassword);
                    int rowsAffected = preparedStatement.executeUpdate();
                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(null, "密码修改成功！");
                    } else {
                        JOptionPane.showMessageDialog(null, "旧密码错误！");
                    }
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(null, "修改密码失败: " + e.getMessage());
                }
            } else {
                JOptionPane.showMessageDialog(null, "新密码和确认密码不一致！");
            }
        }
    }
}

