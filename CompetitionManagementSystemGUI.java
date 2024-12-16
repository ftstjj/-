package org.example;

import javax.swing.*;//用于导入必要的库和创建一个简单的图形界面
import java.awt.*;//创建图形界面所需的基本组件和布局管理器等。
import java.awt.event.*;//响应用户鼠标键盘等操作
import java.sql.*;//连接数据库接口

public class CompetitionManagementSystemGUI {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/competition_db?useSSL=false&serverTimezone=Asia/Shanghai";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "201205";

    private static JTextArea displayArea;//显示多行文本内容

    public static void main(String[] args) {
        // 初始化并显示GUI
        SwingUtilities.invokeLater(CompetitionManagementSystemGUI::createAndShowGUI);// Swing 库提供方法
    }

    private static void createAndShowGUI() {
        try {
            // 加载MySQL JDBC驱动
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);//JBDC数据库连接方法

            // 创建并设置主窗口(JFrame)
            JFrame frame = new JFrame("学生科技竞赛管理系统");//创建一个新的面板容器
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//当窗口关闭时，退出应用程序。
            frame.setSize(600, 500);//窗口高度宽度
            frame.setLocationRelativeTo(null);//窗口显示的位置 null（中间）

            // 创建面板 (JPanel) 用于组织组件
            JPanel panel = new JPanel();//定义面板按钮
            panel.setLayout(new BorderLayout());//控制按钮排序方式

            // 创建一个文本区域用于显示结果
            displayArea = new JTextArea();
            displayArea.setEditable(false);
            panel.add(new JScrollPane(displayArea), BorderLayout.CENTER);

            // 创建按钮面板 (JPanel)
            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new GridLayout(6, 1));  // 修改为6行，以容纳新按钮

            // 创建按钮
            JButton addButton = new JButton("录入学生竞赛信息");
            JButton displayButton = new JButton("查看所有竞赛信息");
            JButton searchButton = new JButton("按学号或姓名查询竞赛结果");
            JButton editButton = new JButton("修改学生竞赛信息");  // 新增修改按钮
            JButton exitButton = new JButton("退出");

            buttonPanel.add(addButton);
            buttonPanel.add(displayButton);
            buttonPanel.add(searchButton);
            buttonPanel.add(editButton);  // 将修改按钮添加到面板
            buttonPanel.add(exitButton);

            panel.add(buttonPanel, BorderLayout.WEST);

            // 将面板添加到主窗口
            frame.add(panel);
            frame.setVisible(true);//使窗口可见

            // 为按钮添加事件监听器
            addButton.addActionListener(e -> showAddCompetitionDialog(connection)); //当用户点击“录入学生竞赛信息”按钮时，调用 showAddCompetitionDialog(connection) 方法，打开一个对话框，允许用户输入竞赛信息并将其存储到数据库。
            displayButton.addActionListener(e -> displayAllCompetitions(connection));//当用户点击“查看所有竞赛信息”按钮时，调用 displayAllCompetitions(connection) 方法，查询数据库并展示所有竞赛信息。
            searchButton.addActionListener(e -> showSearchDialog(connection));//当用户点击“按学号或姓名查询竞赛结果”按钮时，调用 showSearchDialog(connection) 方法，弹出查询对话框，允许用户输入学号或姓名并查询竞赛结果。
            editButton.addActionListener(e -> showEditCompetitionDialog(connection));  // 添加监听器 当用户点击“修改学生竞赛信息”按钮时，调用 showEditCompetitionDialog(connection) 方法，弹出修改对话框，允许用户修改竞赛信息。
            exitButton.addActionListener(e -> System.exit(0));//退出按钮

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "数据库连接失败: " + e.getMessage());
        }
    }

    private static void showAddCompetitionDialog(Connection connection) {
        // 创建一个对话框用于输入学生竞赛信息
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

        int option = JOptionPane.showConfirmDialog(null, fields, "录入学生竞赛信息", JOptionPane.OK_CANCEL_OPTION);//弹出对话框 确认or取消

        if (option == JOptionPane.OK_OPTION) /*检查用户按钮点击  */ {
            try {
                String studentId = studentIdField.getText();
                String studentName = studentNameField.getText();
                String competitionName = competitionNameField.getText();
                String result = resultField.getText();
                int year = Integer.parseInt(yearField.getText());
                //获取用户输入信息

                String insertSQL = "INSERT INTO competitions (student_id, student_name, competition_name, result, year) VALUES (?, ?, ?, ?, ?)";//构建sql插入语句
                try (PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {
                    preparedStatement.setString(1, studentId);
                    preparedStatement.setString(2, studentName);
                    preparedStatement.setString(3, competitionName);
                    preparedStatement.setString(4, result);
                    preparedStatement.setInt(5, year);  //将输入信息绑定‘？’ 依次绑定然后录入
                    preparedStatement.executeUpdate();
                    JOptionPane.showMessageDialog(null, "学生竞赛信息录入成功！");
                }
            } catch (SQLException | NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "录入失败: " + e.getMessage());//提示录入失败原因
            }
        }
    }

    private static void displayAllCompetitions(Connection connection) {
        //定义所有查询竞赛信息的sql语句
        String querySQL = "SELECT * FROM competitions";
        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(querySQL)) {
            StringBuilder resultText = new StringBuilder();
            resultText.append("=== 所有学生竞赛信息 ===\n");
            //遍历结果
            while (resultSet.next()) {
                resultText.append(String.format("学号: %s, 姓名: %s, 竞赛: %s, 成绩: %s, 年份: %d\n",
                        resultSet.getString("student_id"),
                        resultSet.getString("student_name"),
                        resultSet.getString("competition_name"),
                        resultSet.getString("result"),
                        resultSet.getInt("year")));
            }
            //设置文本区域为查询内容
            displayArea.setText(resultText.toString());
        } catch (SQLException e) {
            //显示查询失败的原因
            JOptionPane.showMessageDialog(null, "查询失败: " + e.getMessage());
        }
    }

    private static void showSearchDialog(Connection connection) {
        //创建一个文本框用于输入学号或姓名
        JTextField searchField = new JTextField();
        Object[] fields = {
                "输入学生学号或姓名:", searchField
        };
        // 弹出对话框，显示输入框和提示信息
        int option = JOptionPane.showConfirmDialog(null, fields, "查询学生竞赛信息", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String keyword = searchField.getText();   // 获取用户输入的关键字
            searchCompetitionInfo(connection, keyword);       // 调用查询方法，执行竞赛信息查询
        }
    }

    private static void searchCompetitionInfo(Connection connection, String keyword) {
        String querySQL = "SELECT * FROM competitions WHERE student_id = ? OR student_name = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(querySQL)) {
            // 将用户输入的学号或姓名绑定到查询语句中的占位符
            preparedStatement.setString(1, keyword);
            preparedStatement.setString(2, keyword);

            // 执行查询
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                // 创建一个 StringBuilder 用于存储查询结果
                StringBuilder resultText = new StringBuilder();
                resultText.append("=== 查询结果 ===\n");

                // 遍历查询结果集
                while (resultSet.next()) {
                    // 将查询结果逐行添加到 resultText 中
                    resultText.append(String.format("学号: %s, 姓名: %s, 竞赛: %s, 成绩: %s, 年份: %d\n",
                            resultSet.getString("student_id"),
                            resultSet.getString("student_name"),
                            resultSet.getString("competition_name"),
                            resultSet.getString("result"),
                            resultSet.getInt("year")));
                }
                // 将查询结果显示在文本区域中
                displayArea.setText(resultText.toString());
            }
        } catch (SQLException e) {
            //如果查询结果失败 弹出提示信息
            JOptionPane.showMessageDialog(null, "查询失败: " + e.getMessage());
        }
    }

    private static void showEditCompetitionDialog(Connection connection) {
        // 创建一个对话框，输入学号或姓名查找记录
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
        // 查询学生竞赛信息
        String querySQL = "SELECT * FROM competitions WHERE student_id = ? OR student_name = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(querySQL)) {
            preparedStatement.setString(1, keyword);
            preparedStatement.setString(2, keyword);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    // 显示当前的竞赛信息，并允许用户编辑
                    JTextField studentIdField = new JTextField(resultSet.getString("student_id"));
                    JTextField studentNameField = new JTextField(resultSet.getString("student_name"));
                    JTextField competitionNameField = new JTextField(resultSet.getString("competition_name"));
                    JTextField resultField = new JTextField(resultSet.getString("result"));
                    JTextField yearField = new JTextField(String.valueOf(resultSet.getInt("year")));

                    // 构建输入框对象数组
                    Object[] fields = {
                            "学生学号:", studentIdField,
                            "学生姓名:", studentNameField,
                            "竞赛名称:", competitionNameField,
                            "竞赛结果:", resultField,
                            "竞赛年份:", yearField
                    };

                    int option = JOptionPane.showConfirmDialog(null, fields, "修改学生竞赛信息", JOptionPane.OK_CANCEL_OPTION);
                    if (option == JOptionPane.OK_OPTION) {
                        // 更新数据库
                        String updateSQL = "UPDATE competitions SET student_id = ?, student_name = ?, competition_name = ?, result = ?, year = ? WHERE student_id = ? OR student_name = ?";
                        try (PreparedStatement updateStatement = connection.prepareStatement(updateSQL)) {
                            updateStatement.setString(1, studentIdField.getText());
                            updateStatement.setString(2, studentNameField.getText());
                            updateStatement.setString(3, competitionNameField.getText());
                            updateStatement.setString(4, resultField.getText());
                            updateStatement.setInt(5, Integer.parseInt(yearField.getText()));
                            updateStatement.setString(6, keyword);  // 用学号或姓名作为条件
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
}
