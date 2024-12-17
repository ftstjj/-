package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class CompetitionManagementSystemGUI {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/competition_db?useSSL=false&serverTimezone=Asia/Shanghai";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "201205";

    private static JTextArea displayArea;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CompetitionManagementSystemGUI::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            JFrame frame = new JFrame("学生科技竞赛管理系统");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(600, 500);
            frame.setLocationRelativeTo(null);

            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());

            displayArea = new JTextArea();
            displayArea.setEditable(false);
            panel.add(new JScrollPane(displayArea), BorderLayout.CENTER);

            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new GridLayout(7, 1));  // 修改为7行，以容纳删除按钮

            JButton addButton = new JButton("录入学生竞赛信息");
            JButton displayButton = new JButton("查看所有竞赛信息");
            JButton searchButton = new JButton("按学号或姓名查询竞赛结果");
            JButton editButton = new JButton("修改学生竞赛信息");
            JButton deleteButton = new JButton("删除学生竞赛信息");  // 新增删除按钮
            JButton exitButton = new JButton("退出");

            buttonPanel.add(addButton);
            buttonPanel.add(displayButton);
            buttonPanel.add(searchButton);
            buttonPanel.add(editButton);
            buttonPanel.add(deleteButton);  // 将删除按钮添加到面板
            buttonPanel.add(exitButton);

            panel.add(buttonPanel, BorderLayout.WEST);

            frame.add(panel);
            frame.setVisible(true);

            addButton.addActionListener(e -> showAddCompetitionDialog(connection));
            displayButton.addActionListener(e -> displayAllCompetitions(connection));
            searchButton.addActionListener(e -> showSearchDialog(connection));
            editButton.addActionListener(e -> showEditCompetitionDialog(connection));
            deleteButton.addActionListener(e -> showDeleteCompetitionDialog(connection));  // 删除按钮的监听器
            exitButton.addActionListener(e -> System.exit(0));

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "数据库连接失败: " + e.getMessage());
        }
    }

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

    private static void showSearchDialog(Connection connection) {
        JTextField searchField = new JTextField();
        Object[] fields = {
                "输入学生学号或姓名:", searchField
        };
        int option = JOptionPane.showConfirmDialog(null, fields, "查询学生竞赛信息", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String keyword = searchField.getText();
            searchCompetitionInfo(connection, keyword);
        }
    }

    private static void searchCompetitionInfo(Connection connection, String keyword) {
        String querySQL = "SELECT * FROM competitions WHERE student_id = ? OR student_name = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(querySQL)) {
            preparedStatement.setString(1, keyword);
            preparedStatement.setString(2, keyword);

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

    private static void showEditCompetitionDialog(Connection connection) {
        JTextField searchField = new JTextField();
        Object[] fields = {
                "输入学生学号或姓名:", searchField
        };

        int option = JOptionPane.showConfirmDialog(null, fields, "修改学生竞赛信息", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String keyword = searchField.getText();
            editCompetitionInfo(connection, keyword);
        }
    }

    private static void editCompetitionInfo(Connection connection, String keyword) {
        String querySQL = "SELECT * FROM competitions WHERE student_id = ? OR student_name = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(querySQL)) {
            preparedStatement.setString(1, keyword);
            preparedStatement.setString(2, keyword);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    JTextField studentIdField = new JTextField(resultSet.getString("student_id"));
                    JTextField studentNameField = new JTextField(resultSet.getString("student_name"));
                    JTextField competitionNameField = new JTextField(resultSet.getString("competition_name"));
                    JTextField resultField = new JTextField(resultSet.getString("result"));
                    JTextField yearField = new JTextField(String.valueOf(resultSet.getInt("year")));

                    Object[] fields = {
                            "学生学号:", studentIdField,
                            "学生姓名:", studentNameField,
                            "竞赛名称:", competitionNameField,
                            "竞赛结果:", resultField,
                            "竞赛年份:", yearField
                    };

                    int option = JOptionPane.showConfirmDialog(null, fields, "修改学生竞赛信息", JOptionPane.OK_CANCEL_OPTION);
                    if (option == JOptionPane.OK_OPTION) {
                        String updateSQL = "UPDATE competitions SET student_id = ?, student_name = ?, competition_name = ?, result = ?, year = ? WHERE student_id = ? OR student_name = ?";
                        try (PreparedStatement updateStatement = connection.prepareStatement(updateSQL)) {
                            updateStatement.setString(1, studentIdField.getText());
                            updateStatement.setString(2, studentNameField.getText());
                            updateStatement.setString(3, competitionNameField.getText());
                            updateStatement.setString(4, resultField.getText());
                            updateStatement.setInt(5, Integer.parseInt(yearField.getText()));
                            updateStatement.setString(6, keyword);
                            updateStatement.setString(7, keyword);

                            updateStatement.executeUpdate();
                            JOptionPane.showMessageDialog(null, "竞赛信息修改成功！");
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "未找到相关竞赛信息！");
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "查询失败: " + e.getMessage());
        }
    }

    private static void showDeleteCompetitionDialog(Connection connection) {
        JTextField searchField = new JTextField();
        Object[] fields = {
                "输入学生学号或姓名:", searchField
        };

        int option = JOptionPane.showConfirmDialog(null, fields, "删除学生竞赛信息", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String keyword = searchField.getText();
            deleteCompetitionInfo(connection, keyword);
        }
    }

    private static void deleteCompetitionInfo(Connection connection, String keyword) {
        String deleteSQL = "DELETE FROM competitions WHERE student_id = ? OR student_name = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(deleteSQL)) {
            preparedStatement.setString(1, keyword);
            preparedStatement.setString(2, keyword);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(null, "竞赛信息删除成功！");
            } else {
                JOptionPane.showMessageDialog(null, "未找到相关竞赛信息！");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "删除失败: " + e.getMessage());
        }
    }
}