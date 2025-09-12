package NetworkPackage;

import DirectoryPackage.DirectoryTree;
import MainPackage.Main;
import MainPackage.Setting;
import FileDisplayPackage.FileDisplayBottomBar;
import FileDisplayPackage.FileDisplayPopupMenu;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static DirectoryPackage.DirectoryTree.bottomTipWindow;
import static DirectoryPackage.DirectoryTree.createBottomTipWindow;
import static MainPackage.Main.spikeVisionCloudPath;
import static MainPackage.Setting.*;
import static MainPackage.ThemeColor.*;
import static FileDisplayPackage.FileDisplayBottomBar.historyManipulationButtonEnableJudgement;
import static FileDisplayPackage.FileDisplayBottomBar.recordFileOperation;
import static FileDisplayPackage.FileDisplayMainPanel.*;
import static FileDisplayPackage.FileDisplayTopBar.*;
import static java.awt.Font.PLAIN;

public class User {//用户类
    public static JDialog userDialog = new JDialog(Main.diskManagementSystemFrame, Main.SettingState.systemLanguage ? "User" : "用户", true);//用户对话窗口
    public static JDialog logInDialog = new JDialog(Main.diskManagementSystemFrame, Main.SettingState.systemLanguage ? "Log In" : "登录", true);//登录对话窗口
    public static JDialog registerDialog = new JDialog(Main.diskManagementSystemFrame, Main.SettingState.systemLanguage ? "Register" : "注册", true);//注册对话窗口
    public static JDialog changeUserAccountDialog = new JDialog(Main.diskManagementSystemFrame, Main.SettingState.systemLanguage ? "Change Account" : "修改用户名", true);//修改用户名对话窗口
    public static JDialog changeUserPhoneDialog = new JDialog(Main.diskManagementSystemFrame, Main.SettingState.systemLanguage ? "Change Phone Number" : "修改手机号", true);//修改用户手机号对话窗口
    public static JDialog changeUserPasswordDialog = new JDialog(Main.diskManagementSystemFrame, Main.SettingState.systemLanguage ? "Change Password" : "修改密码", true);//修改用户密码对话窗口
    public static JDialog logOutDialog = new JDialog(Main.diskManagementSystemFrame, Main.SettingState.systemLanguage ? "Log Out" : "注销", true);//注销对话窗口

    private static final JPanel userInformationPanel = new JPanel(new BorderLayout());//用户信息面板
    private static String userPhone = "";//用户手机号
    private static CustomUserTextField userAccountVerificationTextField;//用户账号验证码登录文本域
    private static CustomUserTextField userPhoneTextField;//用户手机号注册文本域

    private static Socket userSocket;//用户接口
    private static DataInputStream dis;//用户数据输入流
    private static DataOutputStream dos;//用户数据输出流
    private static final String serverAddress = "127.0.0.1";//服务器ip地址常量（回环）
//        private static final String serverAddress = "172.16.68.52";//服务器ip地址常量（ipv4）
//    private static final String serverAddress = "2001:da8:2004:2109:dba4:c1ac:216f:e5d0";//服务器ip地址常量（ipv6）

    public static class CustomUserTextField extends JPanel {//自定义用户文本域面板
        public boolean isWrong = false;//是否出错
        private boolean isDescryPassword = false;//是否查看密码
        private final String emptyText;//空白时绘制文本
        private final JButton verificationButton = new JButton(Main.SettingState.systemLanguage ? "Send Verification Code" : "发送验证码");//发送验证码按钮
        private final JButton descryPasswordButton = new JButton();//查看密码按钮
        public final JTextField inputTextField = new JTextField() {//输入文本域
            @Override
            protected void paintComponent(Graphics g) {//重写绘制方法
                super.paintComponent(g);//调用父类重写清除背景
                Graphics2D g2d = (Graphics2D) g;//创建二维绘制工具类
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);//消除画图锯齿
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);//消除文字锯齿
                if (this.getText().isEmpty()) {//如果文本为空
                    g.setFont(new Font("楷体", PLAIN, 23));//设置字体
                    g2d.setColor(Color.GRAY);//设置字体颜色
                    g2d.drawString(emptyText, 0, 25);//绘制文字
                }
                if (isWrong) {//如果出错
                    g2d.setStroke(new BasicStroke(2));//设置边框厚度
                    g2d.setColor(PICTURE_SELECTED_BORDER_COLOR);//设置边框颜色
                    g2d.drawRect(0, 0, inputTextField.getWidth() - 1, inputTextField.getHeight() - 1);//绘制矩形
                }
                g2d.dispose();//释放
            }
        };
        private final JPasswordField passwordField = new JPasswordField() {//输入密码文本域
            @Override
            protected void paintComponent(Graphics g) {//重写绘制方法
                super.paintComponent(g);//调用父类重写清除背景
                Graphics2D g2d = (Graphics2D) g;//创建二维绘制工具类
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);//消除画图锯齿
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);//消除文字锯齿
                if (this.getText().isEmpty()) {//如果文本为空
                    g.setFont(new Font("楷体", PLAIN, 23));//设置字体
                    g2d.setColor(Color.GRAY);//设置字体颜色
                    g2d.drawString(emptyText, 0, 25);//绘制文字
                }
                if (isWrong) {//如果出错
                    g2d.setStroke(new BasicStroke(2));//设置边框厚度
                    g2d.setColor(PICTURE_SELECTED_BORDER_COLOR);//设置边框颜色
                    g2d.drawRect(0, 0, passwordField.getWidth() - 1, passwordField.getHeight() - 1);//绘制矩形
                }
                g2d.dispose();//释放
            }
        };

        public CustomUserTextField(String text, boolean verification, boolean descryPassword, boolean isRename) {//构造方法
            emptyText = text;//设置空白时绘制文本
            if (verification) {//如果需要发送验证码
                inputTextField.setPreferredSize(new Dimension(265, 35));//设置大小
                verificationButton.setSize(35, 35);//设置大小
                verificationButton.setBackground(SETTING_BUTTON_COLOR);//设置背景颜色
                verificationButton.setFont(new Font("楷体", PLAIN, 20));//设置字体
                verificationButton.setFocusable(false);//设置不可聚焦
                verificationButton.addActionListener(_ -> {//为验证码按钮添加事件监听
                    if (logInDialog.isVisible()) {//如果是在登录界面
                        userPhone = userAccountVerificationTextField.inputTextField.getText();//获取手机号
                    } else {//否则在注册或修改手机号界面
                        userPhone = userPhoneTextField.inputTextField.getText();//获取手机号
                    }
                    if (userPhone.isEmpty()) {//如果手机号为空
                        if (logInDialog.isVisible()) {//如果在登录界面
                            createBottomTipWindow(Main.SettingState.systemLanguage ? "Phone Number Cannot Be Empty" : "手机号不可为空");//提示
                            userAccountVerificationTextField.isWrong = true;//错误
                            userAccountVerificationTextField.revalidate();//重新验证
                            userAccountVerificationTextField.repaint();//重新绘制
                        } else if (registerDialog.isVisible()) {//如果在注册界面
                            createBottomTipWindow(Main.SettingState.systemLanguage ? "Phone Number Cannot Be Empty" : "手机号不可为空");//提示
                            userPhoneTextField.isWrong = true;//错误
                            userPhoneTextField.revalidate();//重新验证
                            userPhoneTextField.repaint();//重新绘制
                        } else {//否则在修改手机号界面
                            createBottomTipWindow(Main.SettingState.systemLanguage ? "New Phone Number Cannot Be Empty" : "新手机号不可为空");//提示
                            userPhoneTextField.isWrong = true;//错误
                            userPhoneTextField.revalidate();//重新验证
                            userPhoneTextField.repaint();//重新绘制
                        }
                        return;//直接返回
                    } else if (userPhone.length() != 11) {//如果手机号不为11位
                        if (logInDialog.isVisible()) {//如果在登录界面
                            createBottomTipWindow(Main.SettingState.systemLanguage ? "Please Enter 11-Digit Phone Number" : "请输入11位手机号");//提示
                            userAccountVerificationTextField.isWrong = true;//错误
                            userAccountVerificationTextField.revalidate();//重新验证
                            userAccountVerificationTextField.repaint();//重新绘制
                        } else if (registerDialog.isVisible()) {//如果在注册界面
                            createBottomTipWindow(Main.SettingState.systemLanguage ? "Please Enter 11-Digit Phone Number" : "请输入11位手机号");//提示
                            userPhoneTextField.isWrong = true;//错误
                            userPhoneTextField.revalidate();//重新验证
                            userPhoneTextField.repaint();//重新绘制
                        } else {//否则在修改手机号界面
                            createBottomTipWindow(Main.SettingState.systemLanguage ? "Please Enter 11-Digit New Phone Number" : "请输入11位新手机号");//提示
                            userPhoneTextField.isWrong = true;//错误
                            userPhoneTextField.revalidate();//重新验证
                            userPhoneTextField.repaint();//重新绘制
                        }
                        return;//直接返回
                    } else if (userPhone.equals(Main.SettingState.userPhone)) {//如果输入手机号相同
                        if (changeUserPhoneDialog.isVisible()) {//如果在修改手机号界面
                            createBottomTipWindow(Main.SettingState.systemLanguage ? "New Phone Number Cannot Same As The Old One" : "输入新手机号与旧手机号相同");//提示
                            userPhoneTextField.isWrong = true;//错误
                            userPhoneTextField.revalidate();//重新验证
                            userPhoneTextField.repaint();//重新绘制
                            return;//直接返回
                        }
                    } else {//否则
                        try {
                            if (logInDialog.isVisible()) {//如果在登录界面
                                try {
                                    if (!handleUserValidateUserInformation(userPhone)) {//如果用户不存在
                                        userAccountVerificationTextField.isWrong = true;//错误
                                        userAccountVerificationTextField.revalidate();//重新验证
                                        userAccountVerificationTextField.repaint();//重新绘制
                                        return;//直接返回
                                    }
                                } catch (IOException e) {
                                    handleErrorLog(e.getMessage());//处理错误日志
                                    throw new RuntimeException(e);//捕获异常
                                }
                            } else if (registerDialog.isVisible()) {//如果在注册界面
                                if (handleUserValidateUserUnique(userPhone, true)) {//如果手机号已被使用
                                    createBottomTipWindow(Main.SettingState.systemLanguage ? "The Phone Number Has Been Used" : "手机号已被使用");//提示
                                    userPhoneTextField.isWrong = true;//错误
                                    userPhoneTextField.revalidate();//重新验证
                                    userPhoneTextField.repaint();//重新绘制
                                    return;//直接返回
                                }
                            } else if (changeUserPhoneDialog.isVisible()) {//如果在修改手机号界面
                                if (handleUserValidateUserUnique(userPhone, true)) {//如果手机号已被使用
                                    createBottomTipWindow(Main.SettingState.systemLanguage ? "The New Phone Number Has Been Used" : "新手机号已被使用");//提示
                                    userPhoneTextField.isWrong = true;//错误
                                    userPhoneTextField.revalidate();//重新验证
                                    userPhoneTextField.repaint();//重新绘制
                                    return;//直接返回
                                }
                            }
                        } catch (IOException e) {
                            handleErrorLog(e.getMessage());//处理错误日志
                            throw new RuntimeException(e);//捕获异常
                        }
                    }
                    if (logInDialog.isVisible()) {//如果在登录界面
                        userAccountVerificationTextField.isWrong = false;//正确
                        userAccountVerificationTextField.revalidate();//重新验证
                        userAccountVerificationTextField.repaint();//重新绘制
                    } else {//否则在注册或修改手机号界面
                        userPhoneTextField.isWrong = false;//正确
                        userPhoneTextField.revalidate();//重新验证
                        userPhoneTextField.repaint();//重新绘制
                    }
                    String verificationCode;//验证码
                    try {
                        verificationCode = handleUserGenerateVerificationCode();//发送验证码
                    } catch (IOException e) {
                        handleErrorLog(e.getMessage());//处理错误日志
                        throw new RuntimeException(e);//捕获异常
                    }
                    createBottomTipWindow(Main.SettingState.systemLanguage ? "Verification Code Has Been Sent To " + userPhone + ", Which Is Valid For 5 Minute, Please Receive In Time" : "验证码已发送至" + userPhone + "，有效期5分钟，请及时接收");//提示
                    JOptionPane.showMessageDialog(logInDialog.isVisible() ? logInDialog : (registerDialog.isVisible() ? registerDialog : changeUserPhoneDialog), Main.SettingState.systemLanguage ? "Verification Code" + verificationCode : "验证码：" + verificationCode, Main.SettingState.systemLanguage ? "Verification Code" : "验证码", JOptionPane.INFORMATION_MESSAGE);//暂时使用
                    verificationButton.setEnabled(false);//设置不可点击
                    new Thread(() -> {//启动60秒倒计时
                        for (int i = 60; i >= 0; i--) {//循环60次
                            int finalI = i;//lambda表达式需要
                            SwingUtilities.invokeLater(() -> verificationButton.setText(String.format(Main.SettingState.systemLanguage ? "Resend(%d)" : "重新发送（%d）", finalI)));//设置倒计时文本
                            try {
                                Thread.sleep(1000);//睡眠1秒
                            } catch (InterruptedException e) {
                                handleErrorLog(e.getMessage());//处理错误日志
                                e.printStackTrace();//捕获异常
                            }
                        }
                        SwingUtilities.invokeLater(() -> {//倒计时结束
                            verificationButton.setText(Main.SettingState.systemLanguage ? "Send Verification Code" : "发送验证码");//重新设置文本
                            verificationButton.setEnabled(true);//设置可以点击
                        });
                    }).start();//开始线程
                });
                verificationButton.addMouseListener(new MouseAdapter() {//为验证码按钮添加鼠标监听
                    @Override
                    public void mouseEntered(MouseEvent e) {//如果鼠标进入
                        hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Send Verification Code (Which Is 6 Digit, If You Do Not Receive, Verification Code Expire, Or Incorrect Verification Code, Please Resend)" : "发送验证码（验证码为6位，如果没有收到验证码、验证码过期或验证码错误，请重新发送验证码）", verificationButton));//展示提示窗口（鼠标悬浮一秒后展示）
                        hoverTimer.setRepeats(false);//设置计时器不重复
                        hoverTimer.start();//开始计时
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {//如果鼠标离开
                        if (hoverTimer != null) {//如果不为空
                            hoverTimer.stop();//计时器结束
                        }
                        if (buttonHoverTipWindow != null) {//如果提示信息不为空
                            buttonHoverTipWindow.dispose();//释放提示信息
                            buttonHoverTipWindow = null;//提示信息置空
                        }
                    }
                });
                setLayout(new BorderLayout());//设置布局
                add(inputTextField, BorderLayout.CENTER);//把文本域放在中心
                add(verificationButton, BorderLayout.EAST);//发送验证码按钮放在东部
            } else if (descryPassword) {//如果需要查看密码
                passwordField.setPreferredSize(new Dimension(260, 35));//设置大小
                passwordField.setFont(new Font("微软雅黑", PLAIN, 23));//设置字体
                passwordField.setEchoChar('•');//隐藏密码
                descryPasswordButton.setPreferredSize(new Dimension(40, 35));//设置大小
                descryPasswordButton.setBackground(SETTING_BUTTON_COLOR);//设置背景颜色
                descryPasswordButton.setIcon(new ImageIcon("src/material/image/showPassword.png"));
                descryPasswordButton.setFocusable(false);//设置不可聚焦
                descryPasswordButton.addActionListener(_ -> {//为查看密码按钮添加事件监听
                    if (hoverTimer != null) {//如果不为空
                        hoverTimer.stop();//计时器结束
                    }
                    if (buttonHoverTipWindow != null) {//如果提示信息不为空
                        buttonHoverTipWindow.dispose();//释放提示信息
                        buttonHoverTipWindow = null;//提示信息置空
                    }
                    if (isDescryPassword) {//如果正在查看密码
                        descryPasswordButton.setIcon(new ImageIcon("src/material/image/showPassword.png"));//设置图标为展示密码
                        passwordField.setEchoChar('•');//隐藏密码
                        isDescryPassword = false;//隐藏密码
                        hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Show Password" : "查看密码", descryPasswordButton));//展示提示窗口（鼠标悬浮一秒后展示）
                    } else {//否则正在隐藏密码
                        descryPasswordButton.setIcon(new ImageIcon("src/material/image/hidePassword.png"));//设置图标为隐藏密码
                        passwordField.setEchoChar((char) 0);//显示密码
                        isDescryPassword = true;//显示密码
                        hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Hide Password" : "隐藏密码", descryPasswordButton));//展示提示窗口（鼠标悬浮一秒后展示）
                    }
                    hoverTimer.setRepeats(false);//设置计时器不重复
                    hoverTimer.start();//开始计时
                });
                descryPasswordButton.addMouseListener(new MouseAdapter() {//为查看密码按钮添加鼠标监听
                    @Override
                    public void mouseEntered(MouseEvent e) {//如果鼠标进入
                        if (isDescryPassword) {//如果正在查看密码
                            hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Hide Password" : "隐藏密码", descryPasswordButton));//展示提示窗口（鼠标悬浮一秒后展示）
                        } else {//否则
                            hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Show Password" : "查看密码", descryPasswordButton));//展示提示窗口（鼠标悬浮一秒后展示）
                        }
                        hoverTimer.setRepeats(false);//设置计时器不重复
                        hoverTimer.start();//开始计时
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {//如果鼠标离开
                        if (hoverTimer != null) {//如果不为空
                            hoverTimer.stop();//计时器结束
                        }
                        if (buttonHoverTipWindow != null) {//如果提示信息不为空
                            buttonHoverTipWindow.dispose();//释放提示信息
                            buttonHoverTipWindow = null;//提示信息置空
                        }
                    }
                });
                setLayout(new BorderLayout());//设置布局
                add(passwordField, BorderLayout.CENTER);//把密码文本域放在中心
                add(descryPasswordButton, BorderLayout.EAST);//查看密码按钮放在东部
            } else {//否则
                setLayout(new BorderLayout());//设置布局
                inputTextField.setPreferredSize(new Dimension(450, 35));//设置大小
                add(inputTextField, BorderLayout.CENTER);//把文本域放在中心
            }
            inputTextField.setFont(new Font("微软雅黑", PLAIN, 23));//设置字体
            setPreferredSize(new Dimension(450, 35));//设置大小，固定高度与按钮一致

            PlainDocument plainDocument = new PlainDocument();//创建plainDocument对象
            plainDocument.setDocumentFilter(new DocumentFilter() {//设置plainDocument的文档过滤器
                @Override
                public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr) throws BadLocationException {//重写插入字符串方法
                    if (isRename) {//如果是重命名
                        if (fb.getDocument().getLength() + text.length() <= 250) {//检查插入的字符串是否超出长度限制
                            super.insertString(fb, offset, text, attr);//没有超出长度限制且没有被过滤的字符才可以输入
                        }
                    } else {//否则
                        if (fb.getDocument().getLength() + text.length() <= 24) {//检查插入的字符串是否超出长度限制
                            StringBuilder filteredStr = new StringBuilder();//字符串构造者
                            for (char c : text.toCharArray()) {//遍历所有字符
                                if (c != ' ') {//不允许空格字符
                                    filteredStr.append(c);//经过过滤后才添加到字符串
                                }
                            }
                            super.insertString(fb, offset, filteredStr.toString(), attr);//没有超出长度限制且没有被过滤的字符才可以输入
                        }
                    }
                }

                @Override
                public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attr) throws BadLocationException {//重写替换方法
                    if (isRename) {//如果是重命名
                        if (text != null) {//如果文本非空
                            if (fb.getDocument().getLength() + text.length() <= 250) {//检查插入的字符串是否超出长度限制（windows长度限制260，这里限制250）
                                super.insertString(fb, offset, text, attr);//没有超出长度限制且没有被过滤的字符才可以输入
                            }
                        }
                    } else {//否则
                        if (fb.getDocument().getLength() + text.length() <= 24) {//检查插入的字符串是否超出长度限制
                            StringBuilder filteredStr = new StringBuilder();//字符串构造者
                            for (char c : text.toCharArray()) {//遍历所有字符
                                if (c != ' ') {//不允许空格字符
                                    filteredStr.append(c);//经过过滤后才添加到字符串
                                }
                            }
                            super.insertString(fb, offset, filteredStr.toString(), attr);//没有超出长度限制且没有被过滤的字符才可以输入
                        }
                    }
                }
            });
            inputTextField.setDocument(plainDocument);//把输入文本域文档设置为plainDocument
            passwordField.setDocument(plainDocument);//把密码输入文本域文档设置为plainDocument
        }
    }

    public static void handleUser() {//处理用户
        if (itemHoverTipWindow != null) {//如果提示信息不为空
            itemHoverTipWindow.dispose();//释放提示信息
            itemHoverTipWindow = null;//提示信息置空
        }
        if (Main.SettingState.userAccount.isEmpty()) {//如果没有账户信息
            logInDialog.setVisible(true);//登录菜单可见
        } else {//否则
            updateUserDialog(1);//更新时间
            userDialog.setVisible(true);//用户菜单可见
        }
    }

    public static void createUserSocket() throws IOException {//创建用户接口
        userSocket = new Socket(serverAddress, 27568);//创建接口并连接服务器
        dis = new DataInputStream(userSocket.getInputStream());//提供接口的输入流创建数据输入流
        dos = new DataOutputStream(userSocket.getOutputStream());//提供接口的输出流创建数据输出流
    }

    public static void closeUserSocket() throws IOException {//关闭用户接口
        if (dis != null) dis.close();//如果不为空，就关闭输入流
        if (dos != null) dos.close();//如果不为空，就关闭输出流
        userSocket.close();//关闭接口：端口与服务器连接
    }

    public static boolean handleUserSaveUserInformation(String userAccount, String userPhone, String userPassword, long userAvailableCloudCapacity) throws IOException {//处理存储用户信息：操作码0：注册存储用户信息时使用
        createUserSocket();//创建用户接口
        dos.writeInt(0);//操作码
        dos.writeUTF(userAccount);//输出用户名
        dos.writeUTF(userPhone);//输出手机号
        dos.writeUTF(userPassword);//输出密码
        dos.writeLong(userAvailableCloudCapacity);//输出云盘可用容量
        dos.writeLong(Main.SettingState.recentSuggestionFeedbackTime);//输出
        dos.writeBoolean(Main.SettingState.windowState);//输出
        dos.writeBoolean(Main.SettingState.systemLanguage);//输出
        dos.writeBoolean(Main.SettingState.themeColor);//输出
        dos.writeBoolean(Main.SettingState.dbclickBehavior);//输出
        dos.writeBoolean(Main.SettingState.hoverTip);//输出
        dos.writeBoolean(Main.SettingState.deleteTip);//输出
        dos.writeBoolean(Main.SettingState.pictureSuffix);//输出
        dos.writeBoolean(Main.SettingState.renameStrategy);//输出
        dos.writeBoolean(Main.SettingState.searchStrategy);//输出
        dos.writeInt(Main.SettingState.customRecycleCleanTime);//输出
        dos.writeBoolean(Main.SettingState.recycleStrategy);//输出
        dos.writeInt(Main.SettingState.masterVolume);//输出
        dos.writeBoolean(Main.SettingState.masterState);//输出
        dos.writeInt(Main.SettingState.bgmVolume);//输出
        dos.writeBoolean(Main.SettingState.bgmState);//输出
        dos.writeInt(Main.SettingState.effectVolume);//输出
        dos.writeBoolean(Main.SettingState.effectState);//输出
        dos.writeInt(Main.SettingState.utilizeTimes);//输出
        if (dis.readBoolean()) {//如果成功
            createBottomTipWindow(Main.SettingState.systemLanguage ? "Successful Registration, Offer You 1GB Cloud Disk Space As Gift" : "注册成功，赠送您1GB云盘空间");//提示
            closeUserSocket();//关闭连接
            return true;//返回成功
        } else {//否则失败
            createBottomTipWindow(Main.SettingState.systemLanguage ? "Failure Registration" : "注册失败");//提示
            closeUserSocket();//关闭连接
            return false;//返回失败
        }
    }

    public static void handleUserLoadUserAccount(String input, boolean type) throws IOException {//处理加载用户信息：操作码1：两种登录方式补全用户信息时使用
        createUserSocket();//创建用户接口
        dos.writeInt(1);//操作码
        if (type) {//通过手机号加载
            dos.writeBoolean(true);//输出类型
            dos.writeUTF(input);//输出手机号
            Main.SettingState.userAccount = dis.readUTF();//补全用户名
            Main.SettingState.userPassword = dis.readUTF();//补全密码
        } else {//否则通过用户名加载
            dos.writeBoolean(false);//输出类型
            dos.writeUTF(input);//输出用户名
            Main.SettingState.userPhone = dis.readUTF();//补全手机号
        }
        Main.SettingState.userAvailableCloudCapacity = dis.readLong();//补全云盘可用容量
        Main.SettingState.recentSuggestionFeedbackTime = dis.readLong();//补全
        Main.SettingState.windowState = dis.readBoolean();//补全
        Main.SettingState.systemLanguage = dis.readBoolean();//补全
        Main.SettingState.themeColor = dis.readBoolean();//补全
        Main.SettingState.dbclickBehavior = dis.readBoolean();//补全
        Main.SettingState.hoverTip = dis.readBoolean();//补全
        Main.SettingState.deleteTip = dis.readBoolean();//补全
        Main.SettingState.pictureSuffix = dis.readBoolean();//补全
        Main.SettingState.renameStrategy = dis.readBoolean();//补全
        Main.SettingState.searchStrategy = dis.readBoolean();//补全
        Main.SettingState.customRecycleCleanTime = dis.readInt();//补全
        Main.SettingState.recycleStrategy = dis.readBoolean();//补全
        Main.SettingState.masterVolume = dis.readInt();//补全
        Main.SettingState.masterState = dis.readBoolean();//补全
        Main.SettingState.bgmVolume = dis.readInt();//补全
        Main.SettingState.bgmState = dis.readBoolean();//补全
        Main.SettingState.effectVolume = dis.readInt();//补全
        Main.SettingState.effectState = dis.readBoolean();//补全
        Main.SettingState.utilizeTimes = dis.readInt();//补全
        initSettingDialog();//重新加载

        if (bgm1GainControl != null && bgm2GainControl != null && bgm3GainControl != null && bgm4GainControl != null && bgm5GainControl != null) {//如果音频控制非空
            bgm1GainControl.setValue(((Main.SettingState.bgmVolume - bgm1MinGain) * ((float) Main.SettingState.masterVolume / 100)) + bgm1MinGain);//设置音频控制音量值
            bgm2GainControl.setValue(((Main.SettingState.bgmVolume - bgm1MinGain) * ((float) Main.SettingState.masterVolume / 100)) + bgm1MinGain);//设置音频控制音量值
            bgm3GainControl.setValue(((Main.SettingState.bgmVolume - bgm1MinGain) * ((float) Main.SettingState.masterVolume / 100)) + bgm1MinGain);//设置音频控制音量值
            bgm4GainControl.setValue(((Main.SettingState.bgmVolume - bgm1MinGain) * ((float) Main.SettingState.masterVolume / 100)) + bgm1MinGain);//设置音频控制音量值
            bgm5GainControl.setValue(((Main.SettingState.bgmVolume - bgm1MinGain) * ((float) Main.SettingState.masterVolume / 100)) + bgm1MinGain);//设置音频控制音量值
        }
        if (removeTipEffectGainControl != null && volumeAdjustEffectGainControl != null && switchPictureEffectGainControl != null) {//如果音频控制非空
            removeTipEffectGainControl.setValue((Main.SettingState.effectVolume - removeTipEffectMinGain) * Main.SettingState.masterVolume / 100 + removeTipEffectMinGain);//设置音频控制音量值
            volumeAdjustEffectGainControl.setValue((Main.SettingState.effectVolume - removeTipEffectMinGain) * Main.SettingState.masterVolume / 100 + removeTipEffectMinGain);//设置音频控制音量值
            switchPictureEffectGainControl.setValue((Main.SettingState.effectVolume - removeTipEffectMinGain) * Main.SettingState.masterVolume / 100 + removeTipEffectMinGain);//设置音频控制音量值
        }
        switchBGM();//切换BGM

        if (Main.SettingState.customRecycleCleanTime == 0 && !Main.SettingState.recycleStrategy) {//如果立即清理且没有关闭清理
            FileDisplayPopupMenu.handleAutoEmptyRecycleBin();//调用自动清空图片回收站
        }

        closeUserSocket();//关闭连接
    }

    public static boolean handleUserModifyUserAccount(String input1, String input2, int type) throws IOException {//处理修改用户信息：操作码2：修改用户信息时使用，输入1永远是用户名，用于查找用户；输入2根据输入类型是新用户名、新手机号或新密码
        createUserSocket();//创建用户接口
        dos.writeInt(2);//操作码
        dos.writeInt(type);//输出类型
        dos.writeUTF(input1);//输出输入1
        dos.writeUTF(input2);//输出输入2
        switch (type) {//根据类型选择
            case 1://修改用户名
                if (dis.readBoolean()) {//如果成功
                    createBottomTipWindow(Main.SettingState.systemLanguage ? "Success To Modify Username" : "用户名修改成功");//提示
                    closeUserSocket();//关闭连接
                    return true;//返回成功
                } else {//否则失败
                    createBottomTipWindow(Main.SettingState.systemLanguage ? "Failure To Modify Username" : "用户名修改失败");//提示
                    closeUserSocket();//关闭连接
                    return false;//返回失败
                }
            case 2://修改手机号
                if (dis.readBoolean()) {//如果成功
                    createBottomTipWindow(Main.SettingState.systemLanguage ? "Success To Modify Phone Number" : "手机号修改成功");//提示
                    closeUserSocket();//关闭连接
                    return true;//返回成功
                } else {//否则失败
                    createBottomTipWindow(Main.SettingState.systemLanguage ? "Failure To Modify Phone Number" : "手机号修改失败");//提示
                    closeUserSocket();//关闭连接
                    return false;//返回失败
                }
            case 3://修改密码
                if (dis.readBoolean()) {//如果成功
                    createBottomTipWindow(Main.SettingState.systemLanguage ? "Success To Modify Password" : "密码修改成功");//提示
                    closeUserSocket();//关闭连接
                    return true;//返回成功
                } else {//否则失败
                    createBottomTipWindow(Main.SettingState.systemLanguage ? "Failure To Modify Password" : "密码修改失败");//提示
                    closeUserSocket();//关闭连接
                    return false;//返回失败
                }
        }
        closeUserSocket();//关闭连接
        return false;//返回失败
    }

    public static boolean handleUserValidateUserUnique(String input, boolean type) throws IOException {//处理验证用户名和手机号唯一性：操作码3：注册或修改用户名或修改手机号时使用
        createUserSocket();//创建用户接口
        dos.writeInt(3);//操作码
        dos.writeBoolean(type);//输出类型
        dos.writeUTF(input);//输入输入
        boolean result = dis.readBoolean();//获取结果
        closeUserSocket();//关闭连接
        return result;//返回结果
    }

    public static int handleUserValidateUserInformation(String input, String userPassword, boolean type) throws IOException {//处理验证用户信息：操作码4：登录验证用户是否存在时使用
        createUserSocket();//创建用户接口
        dos.writeInt(4);//操作码
        if (type) {//通过手机号验证
            dos.writeBoolean(true);//输出类型
            dos.writeUTF(input);//输出手机号
            if (dis.readInt() == 0) {//如果存在该用户返回成功
                createBottomTipWindow(Main.SettingState.systemLanguage ? "Successful Login" : "登录成功");//提示
                closeUserSocket();//关闭连接
                return 0;//返回成功
            } else {//否则
                createBottomTipWindow(Main.SettingState.systemLanguage ? "Failure Login, The Phone Number Is Not Registered" : "登录失败，该手机号未注册");//输出错误信息
                closeUserSocket();//关闭连接
                return 2;//返回失败
            }
        } else {//否则通过用户名验证
            dos.writeBoolean(false);//输出类型
            dos.writeUTF(input);//输出用户名
            dos.writeUTF(userPassword);//输出密码
            int code = dis.readInt();//输入码
            if (code == 0 || code == 1) {//如果存在该用户且用户名密码映射正确
                createBottomTipWindow(Main.SettingState.systemLanguage ? "Successful Login" : "登录成功");//提示
                closeUserSocket();//关闭连接
                return code;//返回成功
            } else if (code == 2) {//如果是用户名不存在
                createBottomTipWindow(Main.SettingState.systemLanguage ? "Failure Login, The Username Is Not Exist" : "登录失败，该用户名不存在");//输出错误信息
                closeUserSocket();//关闭连接
                return 2;//返回失败
            } else {//否则是用户名密码映射失败
                createBottomTipWindow(Main.SettingState.systemLanguage ? "Failure Login, The Password Is Incorrect" : "登录失败，密码错误");//输出错误信息
                closeUserSocket();//关闭连接
                return 3;//返回失败
            }
        }
    }

    public static boolean handleUserValidateUserInformation(String input) throws IOException {//处理验证用户信息：操作码4：重写，发送验证码验证用户是否存在时使用
        createUserSocket();//创建用户接口
        dos.writeInt(4);//操作码
        dos.writeBoolean(true);//输出类型
        dos.writeUTF(input);//输出手机号
        if (dis.readInt() == 0) {//如果存在该用户返回成功
            closeUserSocket();//关闭连接
            return true;//返回成功
        } else {//否则
            createBottomTipWindow(Main.SettingState.systemLanguage ? "Failure Login, The Phone Number Is Not Registered" : "登录失败，该手机号未注册");//输出错误信息
            closeUserSocket();//关闭连接
            return false;//返回失败
        }
    }

    public static boolean handleUserRemoveUserInformation(String userAccount) throws IOException {//处理删除用户信息：操作码5：注销删除用户名对应的信息和云盘数据时使用
        createUserSocket();//创建用户接口
        dos.writeInt(5);//操作码
        dos.writeUTF(userAccount);//输出用户名
        if (dis.readBoolean()) {//如果注销成功
            createBottomTipWindow(Main.SettingState.systemLanguage ? "Successful Logout" : "注销成功");//提示
            closeUserSocket();//关闭连接
            return true;//返回成功
        } else {//否则注销失败
            createBottomTipWindow(Main.SettingState.systemLanguage ? "Failure Logout" : "注销失败");//提示
            closeUserSocket();//关闭连接
            return false;//返回失败
        }
    }

    public static List<String> handleUserSaveUserUploadPicture(List<File> sourceFilelist, boolean record) {//处理存储用户上传图片：操作码6：上传本地图片到云端时使用
        if (!Main.SettingState.userAccount.isEmpty()) {//如果有账户信息
            try {
                long fileTotalSize = 0;//文件总大小
                for (File file : sourceFilelist) {//遍历原文件列表
                    fileTotalSize += file.length();//加上文件大小
                }
                if (Main.SettingState.userAvailableCloudCapacity >= fileTotalSize) {//如果没有超过云端容量
                    createUserSocket();//创建用户接口
                    dos.writeInt(6);//操作码
                    dos.writeUTF(Main.SettingState.userAccount);//输出用户名
                    dos.writeBoolean(Main.SettingState.renameStrategy);//输出命名策略
                    long userAvailableCloudCapacity = Main.SettingState.userAvailableCloudCapacity;//获取用户可用云盘容量
                    List<String> targetStringList = new ArrayList<>();//目标字符串列表
                    dos.writeInt(sourceFilelist.size());//输出列表大小
                    if (!sourceFilelist.isEmpty()) {//如果非空
                        for (File file : sourceFilelist) {//遍历项目列表
                            String fileName = file.getName();//获取文件名
                            dos.writeUTF(fileName);//输出文件名
                            dos.writeLong(file.length());//输出文件大小
                            userAvailableCloudCapacity -= file.length();//减去文件大小

                            try {
                                FileInputStream fis = new FileInputStream(file);//通过文件创建文件输入流
                                ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);//创建有1024个缓存字节的字节数组输出流
                                byte[] buffer = new byte[1024];//创建大小为1024字节的字节缓存数组
                                int len;//单次读取长度
                                while ((len = fis.read(buffer)) != -1) {//文件输入流读取到字节缓存数组，读取长度为len，如果读取没有结束
                                    bos.write(buffer, 0, len);//往字节数组输出流写入长度为len的字节缓存数组
                                }
                                byte[] bytes = bos.toByteArray();//把字节数组输出流的全部数据转化成完整的字节数组
                                dos.writeInt(bytes.length);//输出文件字节数组长度
                                dos.write(bytes);//输出文件字节数组
                                fis.close();//释放文件输入流
                                bos.close();//释放字节数组输出流
                            } catch (Exception e) {
                                handleErrorLog(e.getMessage());//处理错误日志
                                e.printStackTrace();//捕获异常
                            }
                            targetStringList.add(dis.readUTF());//记录服务端回写处理后文件名
                        }
                    }
                    if (dis.readBoolean()) {//如果上传成功
                        if (record) {//如果需要记录
                            recordFileOperation(new FileDisplayBottomBar.FileOperation(FileDisplayBottomBar.HistoryOperationType.UPLOAD, sourceFilelist, null, null, null, targetStringList, true));//记录
                        }
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "Successful Cloud Picture Upload" : "云盘图片上传成功");//提示
                    } else {//否则上传失败
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "Failure Cloud Picture Upload" : "云盘图片上传失败");//提示
                    }
                    Main.SettingState.userAvailableCloudCapacity = userAvailableCloudCapacity;//修改用户可用云盘容量
                    updateUserDialog(3);//重新加载
                    closeUserSocket();//关闭连接
                    fileManipulationButtonEnableJudgement(getSelectionThumbnailItemList().size());//更新文件操作按钮状态
                    return targetStringList;//返回目标字符串列表
                } else {//否则
                    createBottomTipWindow(Main.SettingState.systemLanguage ? "Insufficient Cloud Storage Capacity" : "您的云端存储容量不足");//提示
                }
            } catch (IOException e) {
                handleErrorLog(e.getMessage());//处理错误日志
                throw new RuntimeException(e);//捕获异常
            }
        }
        return null;//失败，返回空
    }

    public static File[] handleUserLoadUserUploadPicture(List<String> fileNameList) throws IOException {//处理加载用户上传图片：操作码7：加载云端图片至本地时使用
        createUserSocket();//创建用户接口
        dos.writeInt(7);//操作码、
        dos.writeUTF(Main.SettingState.userAccount);//输出用户
        Path bufferFilePath = Path.of(spikeVisionCloudPath + "/.buffer");//创建缓存文件路径
        if (!Files.exists(bufferFilePath)) {//如果缓存文件路径不存在
            Files.createDirectory(bufferFilePath);//就创建缓存文件路径
        }
        if (fileNameList == null) {//如果为空
            dos.writeBoolean(true);//写入类型为加载所有文件
            int listSize = dis.readInt();//输入列表长度
            File[] pictureFileList = new File[listSize];//根据列表长度创建图片文件数组
            for (int i = 0; i < listSize; i++) {//根据列表长度遍历
                String fileName = dis.readUTF();//输入文件名
                int bytesLength = dis.readInt();//输入文件字节数组长度
                byte[] fileData = new byte[bytesLength];//根据文件字节数组长度创建文件字节数组
                dis.readFully(fileData, 0, bytesLength);//读取完整字节数组（确保读取到所有数据）

                File bufferFile = new File(bufferFilePath + "\\" + fileName);//创建缓存文件
                try (FileOutputStream fos = new FileOutputStream(bufferFile)) {//根据缓存文件创建文件输出流
                    fos.write(fileData);//向文件输出流写入文件字节数组数据
                }
                pictureFileList[i] = bufferFile;//保存缓存文件
            }
            if (dis.readBoolean()) {//如果加载成功
                if (listSize == 0) {//如果没有图片
                    createBottomTipWindow(Main.SettingState.systemLanguage ? "You Have Not Upload Picture To Cloud Storage Yet" : "您还没有上传图片到云端");//提示
                } else {//否则有图片
                    createBottomTipWindow(Main.SettingState.systemLanguage ? "Successful Cloud Picture Load" : "云盘图片加载成功");//提示
                }
                closeUserSocket();//关闭连接
                return pictureFileList;//返回图片文件数组
            } else {//否则加载失败
                createBottomTipWindow(Main.SettingState.systemLanguage ? "Failure Cloud Picture Load" : "云盘图片加载失败");//提示
                Files.deleteIfExists(Path.of(spikeVisionCloudPath + "/.buffer"));//删除缓存
                closeUserSocket();//关闭连接
                return null;//返回空
            }
        } else {//否则
            dos.writeBoolean(false);//写入类型为加载指定文件
            dos.writeInt(fileNameList.size());//输出文件名列表大小
            File[] pictureFileList = new File[fileNameList.size()];//根据文件名列表长度创建图片文件数组
            int i = 0;//计数器
            for (String fileName : fileNameList) {//遍历文件名列表
                dos.writeUTF(fileName);//输出文件名
                int bytesLength = dis.readInt();//输入文件字节数组长度
                byte[] fileData = new byte[bytesLength];//根据文件字节数组长度创建文件字节数组
                dis.readFully(fileData, 0, bytesLength);//读取完整字节数组（确保读取到所有数据）

                File bufferFile = new File(bufferFilePath + "\\" + fileName);//创建缓存文件
                try (FileOutputStream fos = new FileOutputStream(bufferFile)) {//根据缓存文件创建文件输出流
                    fos.write(fileData);//向文件输出流写入文件字节数组数据
                }
                pictureFileList[i] = bufferFile;//保存缓存文件
                i++;//自增
            }
            if (dis.readBoolean()) {//如果加载成功
                closeUserSocket();//关闭连接
                return pictureFileList;//返回图片文件数组
            } else {//否则加载失败
                Files.deleteIfExists(Path.of(spikeVisionCloudPath + "/.buffer"));//删除缓存
                closeUserSocket();//关闭连接
                return null;//返回空
            }
        }
    }

    public static boolean handleUserRemoveUserUploadPicture(List<String> sourceFilelist, boolean type) throws IOException {//处理删除用户上传图片：操作码8
        createUserSocket();//创建用户接口
        dos.writeInt(8);//操作码
        dos.writeUTF(Main.SettingState.userAccount);//输出用户名
        if (type) {//如果是注销账户
            dos.writeBoolean(true);//输出注销账户
            boolean result = dis.readBoolean();//输入结果
            closeUserSocket();//关闭连接
            return result;//返回结果
        } else {//否则
            dos.writeBoolean(false);//输出删除图片
            dos.writeInt(sourceFilelist.size());//输出列表大小
            if (!sourceFilelist.isEmpty()) {//如果非空
                for (String fileName : sourceFilelist) {//遍历项目列表
                    dos.writeUTF(fileName);//输出文件名
                }
            }
            Main.SettingState.userAvailableCloudCapacity = dis.readLong();//更新云盘可用大小
            updateUserDialog(3);//重新加载
            if (dis.readBoolean()) {//如果删除成功
                createBottomTipWindow(Main.SettingState.systemLanguage ? "Successful Cloud Picture Remove" : "云盘图片删除成功");//提示
                closeUserSocket();//关闭连接
                return true;//返回成功
            } else {//否则删除失败
                createBottomTipWindow(Main.SettingState.systemLanguage ? "Failure Cloud Picture Remove" : "云盘图片删除失败");//提示
                closeUserSocket();//关闭连接
                return false;//返回失败
            }
        }
    }

    public static void handleUserUploadUserErrorLog(String errorMessage) throws IOException {//处理上传用户错误日志：操作码9
        createUserSocket();//创建用户接口
        dos.writeInt(9);//操作码
        dos.writeUTF(Main.SettingState.userAccount);//输出用户名
        dos.writeLong(Main.SettingState.userAvailableCloudCapacity);//输出用户可用云盘空间
        dos.writeUTF(errorMessage);//输出错误日志
        closeUserSocket();//关闭连接
    }

    public static void handleUserUploadUserSuggestionFeedback(String suggestionFeedback) throws IOException {//处理上传用户建议反馈：操作码10
        createUserSocket();//创建用户接口
        dos.writeInt(10);//操作码
        dos.writeUTF(Main.SettingState.userAccount);//输出用户名
        if (!Main.SettingState.userAccount.isEmpty()) {//如果用户登录
            dos.writeLong(Main.SettingState.userAvailableCloudCapacity);//输出用户可用云盘空间
        }
        dos.writeUTF(suggestionFeedback);//输出建议反馈
        closeUserSocket();//关闭连接
    }

    public static void handleUserUploadUserSettingSate() throws IOException {//处理上传用户设置状态：操作码11
        createUserSocket();//创建用户接口
        dos.writeInt(11);//操作码
        dos.writeUTF(Main.SettingState.userAccount);//输出用户名
        dos.writeLong(Main.SettingState.recentSuggestionFeedbackTime);//输出
        dos.writeBoolean(Main.SettingState.windowState);//输出
        dos.writeBoolean(Main.SettingState.systemLanguage);//输出
        dos.writeBoolean(Main.SettingState.themeColor);//输出
        dos.writeBoolean(Main.SettingState.dbclickBehavior);//输出
        dos.writeBoolean(Main.SettingState.hoverTip);//输出
        dos.writeBoolean(Main.SettingState.deleteTip);//输出
        dos.writeBoolean(Main.SettingState.pictureSuffix);//输出
        dos.writeBoolean(Main.SettingState.renameStrategy);//输出
        dos.writeBoolean(Main.SettingState.searchStrategy);//输出
        dos.writeInt(Main.SettingState.customRecycleCleanTime);//输出
        dos.writeBoolean(Main.SettingState.recycleStrategy);//输出
        dos.writeInt(Main.SettingState.masterVolume);//输出
        dos.writeBoolean(Main.SettingState.masterState);//输出
        dos.writeInt(Main.SettingState.bgmVolume);//输出
        dos.writeBoolean(Main.SettingState.bgmState);//输出
        dos.writeInt(Main.SettingState.effectVolume);//输出
        dos.writeBoolean(Main.SettingState.effectState);//输出
        dos.writeInt(Main.SettingState.utilizeTimes);//输出
        closeUserSocket();//关闭连接
    }

    public static String handleUserGenerateVerificationCode() throws IOException {//处理生成用户验证码：操作码12
        createUserSocket();//创建用户接口
        dos.writeInt(12);//操作码
        dos.writeUTF(userPhone);//输出手机号
        String verificationCode = dis.readUTF();//读取验证码
        closeUserSocket();//关闭连接
        return verificationCode;//返回验证码
    }

    public static int handleUserVerifyVerificationCode(String userVerificationCodeInput) throws IOException {//处理校验用户验证码：操作码13
        createUserSocket();//创建用户接口
        dos.writeInt(13);//操作码
        dos.writeUTF(userPhone);//输出手机号
        dos.writeUTF(userVerificationCodeInput);//输出验证码
        int result = dis.readInt();//输入结果
        closeUserSocket();//关闭连接
        if (result == 1) {//如果是验证码错误
            createBottomTipWindow(Main.SettingState.systemLanguage ? "Incorrect Verification Code, Please Re-Enter" : "验证码错误，请重新输入");//提示
        } else if (result == 2) {//如果是验证码过期
            createBottomTipWindow(Main.SettingState.systemLanguage ? "Expired Verification Code, Please Resend" : "验证码已过期，请重新发送");//提示
        }
        return result;//返回结果
    }

    public static void updateUserDialog(int type) {//更新用户菜单
        BorderLayout layout = (BorderLayout) userInformationPanel.getLayout();//获取布局
        switch (type) {//根据类型选择
            case 1:
                int hour = new Date().getHours();//获取当前时间
                JLabel userAccountLabel = new JLabel(hour >= 6 && hour < 12 ? Main.SettingState.systemLanguage ? "Good Morning! " : "早上好！" : (hour >= 12 && hour < 18 ? Main.SettingState.systemLanguage ? "Good Afternoon! " : "下午好！" : Main.SettingState.systemLanguage ? "Good Evening! " : "晚上好！") + Main.SettingState.userAccount);//用户信息标签
                userAccountLabel.setPreferredSize(new Dimension(Main.SettingState.systemLanguage ? 490 : 250, 35));//设置大小
                userAccountLabel.setFont(new Font("楷体", PLAIN, 21));//设置字体
                userAccountLabel.setForeground(Main.SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//设置字体颜色
                userInformationPanel.remove(layout.getLayoutComponent(BorderLayout.NORTH));//移除原北部
                userInformationPanel.add(userAccountLabel, BorderLayout.NORTH);//添加新北部
                break;
            case 2:
                JLabel userPhoneLabel = new JLabel(Main.SettingState.systemLanguage ? "Phone Number: " + Main.SettingState.userPhone : "手机号：" + Main.SettingState.userPhone);//用户信息标签
                userPhoneLabel.setPreferredSize(new Dimension(Main.SettingState.systemLanguage ? 490 : 250, 35));//设置大小
                userPhoneLabel.setFont(new Font("楷体", PLAIN, 21));//设置字体
                userPhoneLabel.setForeground(Main.SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//设置字体颜色
                userInformationPanel.remove(layout.getLayoutComponent(BorderLayout.CENTER));//移除原中心
                userInformationPanel.add(userPhoneLabel, BorderLayout.CENTER);//添加新中心
                break;
            case 3:
                String userAvailableCloudCapacityDisplay;//展示用户云端可用容量
                if (Main.SettingState.userAvailableCloudCapacity < 1024) {//如果在1B内
                    userAvailableCloudCapacityDisplay = Main.SettingState.userAvailableCloudCapacity + "B";//展示
                } else {//否则根据大小展示
                    int exp = (int) (Math.log(Main.SettingState.userAvailableCloudCapacity) / Math.log(1024));//计算次方
                    userAvailableCloudCapacityDisplay = String.format("%.2f%sB", Main.SettingState.userAvailableCloudCapacity / Math.pow(1024, exp), "KMGTPE".charAt(exp - 1));//展示
                }
                JLabel cloudAvailableLabel = new JLabel(Main.SettingState.systemLanguage ? "Available Cloud Storage Capacity: " + userAvailableCloudCapacityDisplay : "云盘空间剩余：" + userAvailableCloudCapacityDisplay);//云盘空间标签
                cloudAvailableLabel.setPreferredSize(new Dimension(Main.SettingState.systemLanguage ? 490 : 250, 35));//设置大小
                cloudAvailableLabel.setFont(new Font("楷体", PLAIN, 21));//设置字体
                cloudAvailableLabel.setForeground(Main.SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//设置字体颜色
                userInformationPanel.remove(layout.getLayoutComponent(BorderLayout.SOUTH));//移除原南部
                userInformationPanel.add(cloudAvailableLabel, BorderLayout.SOUTH);//添加新南部
                break;
        }
        userInformationPanel.setBackground(Main.SettingState.themeColor ? DARK_DIALOG_MAIN_COLOR : LIGHT_DIALOG_MAIN_COLOR);//设置背景颜色
    }

    public static void initUserDialog() {//初始化用户菜单
        int hour = new Date().getHours();//获取当前时间
        JLabel userAccountLabel = new JLabel(hour >= 6 && hour < 12 ? Main.SettingState.systemLanguage ? "Good Morning! " : "早上好！" : (hour >= 12 && hour < 18 ? Main.SettingState.systemLanguage ? "Good Afternoon! " : "下午好！" : Main.SettingState.systemLanguage ? "Good Evening! " : "晚上好！") + Main.SettingState.userAccount);//用户信息标签
        userAccountLabel.setPreferredSize(new Dimension(Main.SettingState.systemLanguage ? 490 : 250, 35));//设置大小
        userAccountLabel.setFont(new Font("楷体", PLAIN, 21));//设置字体
        userAccountLabel.setForeground(Main.SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//设置字体颜色
        JLabel userPhoneLabel = new JLabel(Main.SettingState.systemLanguage ? "Phone Number: " + Main.SettingState.userPhone : "手机号：" + Main.SettingState.userPhone);//用户信息标签
        userPhoneLabel.setPreferredSize(new Dimension(250, 35));//设置大小
        userPhoneLabel.setFont(new Font("楷体", PLAIN, 21));//设置字体
        userPhoneLabel.setForeground(Main.SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//设置字体颜色
        String userAvailableCloudCapacityDisplay;//展示用户云端可用容量
        if (Main.SettingState.userAvailableCloudCapacity < 1024) {//如果在1B内
            userAvailableCloudCapacityDisplay = Main.SettingState.userAvailableCloudCapacity + "B";//展示
        } else {//否则根据大小展示
            int exp = (int) (Math.log(Main.SettingState.userAvailableCloudCapacity) / Math.log(1024));//计算次方
            userAvailableCloudCapacityDisplay = String.format("%.2f%sB", Main.SettingState.userAvailableCloudCapacity / Math.pow(1024, exp), "KMGTPE".charAt(exp - 1));//展示
        }
        JLabel cloudAvailableLabel = new JLabel(Main.SettingState.systemLanguage ? "Available Cloud Storage Capacity: " + userAvailableCloudCapacityDisplay : "云盘空间剩余：" + userAvailableCloudCapacityDisplay);//云盘空间标签
        cloudAvailableLabel.setPreferredSize(new Dimension(250, 35));//设置大小
        cloudAvailableLabel.setFont(new Font("楷体", PLAIN, 21));//设置字体
        cloudAvailableLabel.setForeground(Main.SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//设置字体颜色
        userInformationPanel.removeAll();//清空
        userInformationPanel.add(userAccountLabel, BorderLayout.NORTH);//用户名添加到北部
        userInformationPanel.add(userPhoneLabel, BorderLayout.CENTER);//手机号添加到中心
        userInformationPanel.add(cloudAvailableLabel, BorderLayout.SOUTH);//云盘可用容量添加到南部
        userInformationPanel.setBackground(Main.SettingState.themeColor ? DARK_DIALOG_MAIN_COLOR : LIGHT_DIALOG_MAIN_COLOR);//设置背景颜色

        JButton changeAccountButton = new JButton(Main.SettingState.systemLanguage ? "Modify Username" : "修改用户名");//修改用户名按钮
        changeAccountButton.setPreferredSize(new Dimension(250, 35));//设置大小
        changeAccountButton.setFont(new Font("楷体", PLAIN, 21));//设置字体
        changeAccountButton.setForeground(Main.SettingState.themeColor ? DARK_USER_OPERATION_BUTTON_FONT_COLOR : LIGHT_USER_OPERATION_BUTTON_FONT_COLOR);//设置前景颜色
        changeAccountButton.setBackground(Main.SettingState.themeColor ? DARK_USER_OPERATION_BUTTON_COLOR : LIGHT_USER_OPERATION_BUTTON_COLOR);//设置背景颜色
        changeAccountButton.setFocusable(false);//不可聚焦
        JButton changePhoneButton = new JButton(Main.SettingState.systemLanguage ? "Modify Phone Number" : "修改手机号");//修改手机号按钮
        changePhoneButton.setPreferredSize(new Dimension(250, 35));//设置大小
        changePhoneButton.setFont(new Font("楷体", PLAIN, 21));//设置字体
        changePhoneButton.setForeground(Main.SettingState.themeColor ? DARK_USER_OPERATION_BUTTON_FONT_COLOR : LIGHT_USER_OPERATION_BUTTON_FONT_COLOR);//设置前景颜色
        changePhoneButton.setBackground(Main.SettingState.themeColor ? DARK_USER_OPERATION_BUTTON_COLOR : LIGHT_USER_OPERATION_BUTTON_COLOR);//设置背景颜色
        changePhoneButton.setFocusable(false);//不可聚焦
        JButton changePasswordButton = new JButton(Main.SettingState.systemLanguage ? "Modify Password" : "修改密码");//修改密码按钮
        changePasswordButton.setPreferredSize(new Dimension(250, 35));//设置大小
        changePasswordButton.setFont(new Font("楷体", PLAIN, 21));//设置字体
        changePasswordButton.setForeground(Main.SettingState.themeColor ? DARK_USER_OPERATION_BUTTON_FONT_COLOR : LIGHT_USER_OPERATION_BUTTON_FONT_COLOR);//设置前景颜色
        changePasswordButton.setBackground(Main.SettingState.themeColor ? DARK_USER_OPERATION_BUTTON_COLOR : LIGHT_USER_OPERATION_BUTTON_COLOR);//设置背景颜色
        changePasswordButton.setFocusable(false);//不可聚焦
        JButton exitLogInButton = new JButton(Main.SettingState.systemLanguage ? "Exit Login" : "退出登录");//退出登录按钮
        exitLogInButton.setPreferredSize(new Dimension(250, 35));//设置大小
        exitLogInButton.setFont(new Font("楷体", PLAIN, 21));//设置字体
        exitLogInButton.setForeground(Main.SettingState.themeColor ? DARK_USER_OPERATION_BUTTON_FONT_COLOR : LIGHT_USER_OPERATION_BUTTON_FONT_COLOR);//设置前景颜色
        exitLogInButton.setBackground(Main.SettingState.themeColor ? DARK_USER_OPERATION_BUTTON_COLOR : LIGHT_USER_OPERATION_BUTTON_COLOR);//设置背景颜色
        exitLogInButton.setFocusable(false);//不可聚焦
        JButton logOutButton = new JButton(Main.SettingState.systemLanguage ? "Logout" : "注销账户");//注销账户按钮
        logOutButton.setPreferredSize(new Dimension(250, 35));//设置大小
        logOutButton.setFont(new Font("楷体", PLAIN, 21));//设置字体
        logOutButton.setForeground(Main.SettingState.themeColor ? DARK_USER_OPERATION_BUTTON_FONT_COLOR : LIGHT_USER_OPERATION_BUTTON_FONT_COLOR);//设置前景颜色
        logOutButton.setBackground(Main.SettingState.themeColor ? DARK_USER_OPERATION_BUTTON_COLOR : LIGHT_USER_OPERATION_BUTTON_COLOR);//设置背景颜色
        logOutButton.setFocusable(false);//不可聚焦
        changeAccountButton.addMouseListener(new MouseAdapter() {//为修改用户名按钮添加鼠标监听
            @Override
            public void mouseClicked(MouseEvent e) {//如果鼠标点击
                initChangeAccountDialog();//初始化
                changeUserAccountDialog.setVisible(true);//可见
            }

            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "You Can Change Your Username At Any Time" : "您可以随时修改用户名", changeAccountButton));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
            }

            @Override
            public void mouseExited(MouseEvent e) {//如果鼠标离开
                if (hoverTimer != null) {//如果不为空
                    hoverTimer.stop();//计时器结束
                }
                if (buttonHoverTipWindow != null) {//如果提示信息不为空
                    buttonHoverTipWindow.dispose();//释放提示信息
                    buttonHoverTipWindow = null;//提示信息置空
                }
            }
        });
        changePhoneButton.addMouseListener(new MouseAdapter() {//为修改手机号按钮添加鼠标监听
            @Override
            public void mouseClicked(MouseEvent e) {//如果鼠标点击
                initChangePhoneDialog();//初始化
                changeUserPhoneDialog.setVisible(true);//可见
            }

            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "You Can Change Your Phone Number At Any Time" : "您可以随时修改手机号", changePhoneButton));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
            }

            @Override
            public void mouseExited(MouseEvent e) {//如果鼠标离开
                if (hoverTimer != null) {//如果不为空
                    hoverTimer.stop();//计时器结束
                }
                if (buttonHoverTipWindow != null) {//如果提示信息不为空
                    buttonHoverTipWindow.dispose();//释放提示信息
                    buttonHoverTipWindow = null;//提示信息置空
                }
            }
        });
        changePasswordButton.addMouseListener(new MouseAdapter() {//为修改密码按钮添加鼠标监听
            @Override
            public void mouseClicked(MouseEvent e) {//如果鼠标点击
                initChangePasswordDialog();//初始化
                changeUserPasswordDialog.setVisible(true);//可见
            }

            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "You Can Change Your Password At Any Time" : "您可以随时修改密码", changePasswordButton));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
            }

            @Override
            public void mouseExited(MouseEvent e) {//如果鼠标离开
                if (hoverTimer != null) {//如果不为空
                    hoverTimer.stop();//计时器结束
                }
                if (buttonHoverTipWindow != null) {//如果提示信息不为空
                    buttonHoverTipWindow.dispose();//释放提示信息
                    buttonHoverTipWindow = null;//提示信息置空
                }
            }
        });
        exitLogInButton.addMouseListener(new MouseAdapter() {//为退出登录按钮添加鼠标监听
            @Override
            public void mouseClicked(MouseEvent e) {//如果鼠标点击
                int confirm = JOptionPane.showConfirmDialog(userDialog, (Main.SettingState.systemLanguage ? "Are You Sure To Exit Login?" : "确定要退出登录吗？"), Main.SettingState.systemLanguage ? "Exit Login Confirm" : "确认退出登录", JOptionPane.YES_NO_OPTION);//创建确认信息
                if (confirm == JOptionPane.YES_OPTION) {//如果确认
                    try {
                        User.handleUserUploadUserSettingSate();//上传用户设置状态
                    } catch (IOException ex) {
                        handleErrorLog(ex.getMessage());//处理错误日志
                        throw new RuntimeException(ex);//捕获异常
                    }
                    logOut();//登出
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "You Can Log Back In After Exit Login" : "退出登录后可以重新登录", exitLogInButton));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
            }

            @Override
            public void mouseExited(MouseEvent e) {//如果鼠标离开
                if (hoverTimer != null) {//如果不为空
                    hoverTimer.stop();//计时器结束
                }
                if (buttonHoverTipWindow != null) {//如果提示信息不为空
                    buttonHoverTipWindow.dispose();//释放提示信息
                    buttonHoverTipWindow = null;//提示信息置空
                }
            }
        });
        logOutButton.addMouseListener(new MouseAdapter() {//为注销按钮添加鼠标监听
            @Override
            public void mouseClicked(MouseEvent e) {//如果鼠标点击
                initLogOutDialog();//初始化
                logOutDialog.setVisible(true);//可见
            }

            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Please Note That Logout Cannot Be Reversed, All Your Cloud Storage Data Will Lost, Please Be Cautious" : "请注意，注销账户操作无法撤销，您的云端数据将全部丢失，请谨慎操作", logOutButton));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
            }

            @Override
            public void mouseExited(MouseEvent e) {//如果鼠标离开
                if (hoverTimer != null) {//如果不为空
                    hoverTimer.stop();//计时器结束
                }
                if (buttonHoverTipWindow != null) {//如果提示信息不为空
                    buttonHoverTipWindow.dispose();//释放提示信息
                    buttonHoverTipWindow = null;//提示信息置空
                }
            }
        });
        JPanel userInformationManipulationPanel = new JPanel(new GridLayout(5, 1, 0, 0));//用户信息面板
        userInformationManipulationPanel.add(changeAccountButton);
        userInformationManipulationPanel.add(changePhoneButton);
        userInformationManipulationPanel.add(changePasswordButton);
        userInformationManipulationPanel.add(exitLogInButton);
        userInformationManipulationPanel.add(logOutButton);

        userDialog = new JDialog(Main.diskManagementSystemFrame, Main.SettingState.systemLanguage ? "User" : "用户", true);//创建用户对话窗口
        userDialog.setIconImage(new ImageIcon("src/material/image/dialogUser.png").getImage());//设置图标
        userDialog.setLayout(new BorderLayout());//设置布局
        userDialog.add(userInformationPanel, BorderLayout.NORTH);//把用户信息面板添加到北部
        userDialog.add(userInformationManipulationPanel, BorderLayout.CENTER);//把用户信息面板添加到北部
        userDialog.setAlwaysOnTop(Main.SettingState.windowState);//设置永远在最上层
        userDialog.pack();//设置合适
        userDialog.setLocation(Main.screenSize.width / 2 - userDialog.getWidth() / 2, Main.screenSize.height / 2 - userDialog.getHeight() / 2);//设置位置
        userDialog.setVisible(false);//设置不可见
        JRootPane userDialogRoot = userDialog.getRootPane();//获取用户窗口的根
        userDialogRoot.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "closeUserDialog");//为根设置窗口关闭ESC按键绑定
        userDialogRoot.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_U, KeyEvent.CTRL_DOWN_MASK), "closeUserDialog");//为根设置窗口关闭Ctrl+U按键绑定
        userDialogRoot.getActionMap().put("closeUserDialog", new AbstractAction() {//当ESC按键执行时
            public void actionPerformed(ActionEvent event) {//行为执行
                userDialog.dispatchEvent(new WindowEvent(userDialog, WindowEvent.WINDOW_CLOSING));//关闭窗口
            }
        });
        userDialog.addWindowListener(new WindowAdapter() {//为用户窗口添加窗口监听
            @Override
            public void windowClosing(WindowEvent e) {//如果窗口正在关闭
                if (bottomTipWindow != null && bottomTipWindow.isVisible()) {//如果底部提示窗口可见
                    bottomTipWindow.dispose();//底部提示窗口置空
                }
                if (buttonHoverTipWindow != null && buttonHoverTipWindow.isVisible()) {//如果按钮提示窗口不为空
                    buttonHoverTipWindow.dispose();//释放按钮提示窗口
                }
            }
        });
    }

    public static void initChangeAccountDialog() {//初始化修改用户名菜单
        CustomUserTextField userAccountTextField = new CustomUserTextField(Main.SettingState.systemLanguage ? "Please Enter New Username (Which Is 24-Digit At Most)" : "请输入新用户名（至多为24位）", false, false, false);//用户账号文本域
        JButton confirmChangeAccountButton = new JButton(Main.SettingState.systemLanguage ? "Confirm Change Username" : "确认修改用户名");//确认修改用户名按钮
        confirmChangeAccountButton.setPreferredSize(new Dimension(Main.SettingState.systemLanguage ? 760 : 450, 35));//设置大小
        confirmChangeAccountButton.setFont(new Font("微软雅黑", PLAIN, 21));//设置字体
        confirmChangeAccountButton.setBackground(USER_LOG_BUTTON_COLOR);//背景颜色
        confirmChangeAccountButton.setFocusable(false);//不可聚焦
        confirmChangeAccountButton.setBorder(null);//无边框
        userAccountTextField.inputTextField.addKeyListener(new KeyAdapter() {//为用户账号文本域添加键盘监听
            @Override
            public void keyPressed(KeyEvent e) {//如果键盘按下
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {//如果按下回车
                    String userAccountInput = userAccountTextField.inputTextField.getText();//获取输入文本
                    if (userAccountInput.isEmpty()) {//如果输入为空
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "The New Username Cannot Be Empty" : "新用户名不可为空");//提示
                        userAccountTextField.isWrong = true;//错误
                    } else if (Objects.equals(userAccountInput, Main.SettingState.userAccount)) {//如果输入新用户名与旧用户名相同
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "New Username Cannot Same As The Old One" : "输入新用户名与旧用户名相同");//提示
                        userAccountTextField.isWrong = true;//错误
                    } else {//否则
                        for (int i = 0; i < userAccountInput.length(); i++) {//遍历输入文本
                            char c = userAccountInput.charAt(i);//获取当前字符
                            if (c == '/' || c == '\\' || c == '"' || c == '{' || c == '}' || c == ':') {//如果是特殊字符
                                createBottomTipWindow(Main.SettingState.systemLanguage ? "No Special Character" : "请勿输入特殊字符");//提示
                                userAccountTextField.isWrong = true;//错误
                                userAccountTextField.revalidate();//重新验证
                                userAccountTextField.repaint();//重新绘制
                                return;//直接返回
                            }
                        }
                        try {
                            if (handleUserValidateUserUnique(userAccountTextField.inputTextField.getText(), false)) {//如果用户名重复
                                createBottomTipWindow(Main.SettingState.systemLanguage ? "Duplicated New Username" : "新用户名重复");//提示
                                userAccountTextField.isWrong = true;//错误
                            } else {//否则
                                userAccountTextField.isWrong = false;//正确
                                confirmChangeAccountButton.doClick();//点击
                            }
                        } catch (IOException ex) {
                            handleErrorLog(ex.getMessage());//处理错误日志
                            throw new RuntimeException(ex);//捕获异常
                        }
                    }
                    userAccountTextField.revalidate();//重新验证
                    userAccountTextField.repaint();//重新绘制
                    e.consume();//阻止默认行为
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {//如果按下ESC
                    userAccountTextField.isWrong = false;//正确
                    userAccountTextField.revalidate();//重新验证
                    userAccountTextField.repaint();//重新绘制
                    changeUserAccountDialog.requestFocusInWindow();//焦点返回菜单
                    e.consume();//阻止默认行为
                }
            }
        });
        userAccountTextField.inputTextField.addFocusListener(new FocusAdapter() {//为用户账号文本域添加聚焦监听
            @Override
            public void focusLost(FocusEvent e) {//如果失去聚焦
                if (changeUserAccountDialog.isVisible()) {//如果可见
                    String userAccountInput = userAccountTextField.inputTextField.getText();//获取输入文本
                    if (userAccountInput.isEmpty()) {//如果输入为空
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "The New Username Cannot Be Empty" : "新用户名不可为空");//提示
                        userAccountTextField.isWrong = true;//错误
                    } else if (Objects.equals(userAccountInput, Main.SettingState.userAccount)) {//如果输入新用户名与旧用户名相同
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "New Username Cannot Same As The Old One" : "输入新用户名与旧用户名相同");//提示
                        userAccountTextField.isWrong = true;//错误
                    } else {//否则
                        for (int i = 0; i < userAccountInput.length(); i++) {//遍历输入文本
                            char c = userAccountInput.charAt(i);//获取当前字符
                            if (c == '/' || c == '\\' || c == '"' || c == '{' || c == '}' || c == ':') {//如果是特殊字符
                                createBottomTipWindow(Main.SettingState.systemLanguage ? "No Special Character" : "请勿输入特殊字符");//提示
                                userAccountTextField.isWrong = true;//错误
                                userAccountTextField.revalidate();//重新验证
                                userAccountTextField.repaint();//重新绘制
                                return;//直接返回
                            }
                        }
                        try {
                            if (handleUserValidateUserUnique(userAccountTextField.inputTextField.getText(), false)) {//如果用户名重复
                                createBottomTipWindow(Main.SettingState.systemLanguage ? "Duplicated New Username" : "新用户名重复");//提示
                                userAccountTextField.isWrong = true;//错误
                            } else {//否则
                                userAccountTextField.isWrong = false;//正确
                            }
                        } catch (IOException ex) {
                            handleErrorLog(ex.getMessage());//处理错误日志
                            throw new RuntimeException(ex);//捕获异常
                        }
                    }
                    userAccountTextField.revalidate();//重新验证
                    userAccountTextField.repaint();//重新绘制
                }
            }
        });
        userAccountTextField.inputTextField.addMouseListener(new MouseAdapter() {//为用户账号文本域添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Please Enter New Username (Which Is 24-Digit At Most, Cannot Be Duplicated, And Cannot Contain Special Character '/' '\\' '\"' '{' '}' ':')" : "请输入新用户名（新用户名至多为24位、新用户名不可重复且不可包含特殊字符 '/' '\\' '\"' '{' '}' ':'）", userAccountTextField.inputTextField));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
            }

            @Override
            public void mouseExited(MouseEvent e) {//如果鼠标离开
                if (hoverTimer != null) {//如果不为空
                    hoverTimer.stop();//计时器结束
                }
                if (buttonHoverTipWindow != null) {//如果提示信息不为空
                    buttonHoverTipWindow.dispose();//释放提示信息
                    buttonHoverTipWindow = null;//提示信息置空
                }
            }
        });
        confirmChangeAccountButton.addActionListener(_ -> {//为确认更改用户名按钮添加事件监听
            String userAccountInput = userAccountTextField.inputTextField.getText().trim();//获取新用户名输入
            if (userAccountInput.isEmpty()) {//新用户名为空
                createBottomTipWindow(Main.SettingState.systemLanguage ? "The New Username Cannot Be Empty" : "新用户名不可为空");//提示
                userAccountTextField.isWrong = true;//错误
                userAccountTextField.revalidate();//重新验证
                userAccountTextField.repaint();//重新绘制
                return;//直接返回
            } else if (Objects.equals(userAccountTextField.inputTextField.getText(), Main.SettingState.userAccount)) {//如果输入新用户名与旧用户名相同
                createBottomTipWindow(Main.SettingState.systemLanguage ? "New Username Cannot Same As The Old One" : "输入新用户名与旧用户名相同");//提示
                userAccountTextField.isWrong = true;//错误
                userAccountTextField.revalidate();//重新验证
                userAccountTextField.repaint();//重新绘制
                return;//直接返回
            } else {//否则
                for (int i = 0; i < userAccountInput.length(); i++) {//遍历输入文本
                    char c = userAccountInput.charAt(i);//获取当前字符
                    if (c == '/' || c == '\\' || c == '"' || c == '{' || c == '}' || c == ':') {//如果是特殊字符
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "No Special Character" : "请勿输入特殊字符");//提示
                        userAccountTextField.isWrong = true;//错误
                        userAccountTextField.revalidate();//重新验证
                        userAccountTextField.repaint();//重新绘制
                        return;//直接返回
                    }
                }
                try {
                    if (handleUserValidateUserUnique(userAccountTextField.inputTextField.getText(), false)) {//如果新用户名重复
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "Duplicated New Username" : "新用户名重复");//提示
                        userAccountTextField.isWrong = true;//错误
                        userAccountTextField.revalidate();//重新验证
                        userAccountTextField.repaint();//重新绘制
                        return;//直接返回
                    }
                } catch (IOException e) {
                    handleErrorLog(e.getMessage());//处理错误日志
                    throw new RuntimeException(e);//捕获异常
                }
            }
            try {
                if (handleUserModifyUserAccount(Main.SettingState.userAccount, userAccountInput, 1)) {//修改云端数据：如果成功
                    Main.SettingState.userAccount = userAccountInput;//修改
                    changeUserAccountDialog.dispose();//释放
                    userDialog.dispose();//释放
                    updateUserDialog(1);//重新加载
                    userDialog.setVisible(true);//可见
                }
            } catch (IOException e) {
                handleErrorLog(e.getMessage());//处理错误日志
                throw new RuntimeException(e);//捕获异常
            }
        });
        confirmChangeAccountButton.addMouseListener(new MouseAdapter() {//为确认修改用户名添加事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Confirm Change Username" : "确认修改用户名", confirmChangeAccountButton));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
            }

            @Override
            public void mouseExited(MouseEvent e) {//如果鼠标离开
                if (hoverTimer != null) {//如果不为空
                    hoverTimer.stop();//计时器结束
                }
                if (buttonHoverTipWindow != null) {//如果提示信息不为空
                    buttonHoverTipWindow.dispose();//释放提示信息
                    buttonHoverTipWindow = null;//提示信息置空
                }
            }
        });
        JPanel contentPanel = new JPanel(new GridLayout(0, 1, 5, 5));//创建内容面板
        contentPanel.setBorder(BorderFactory.createEmptyBorder(7, 10, 7, 10));//创建边框
        contentPanel.setBackground(Main.SettingState.themeColor ? DARK_DIALOG_MAIN_COLOR : LIGHT_DIALOG_MAIN_COLOR);//设置背景颜色
        contentPanel.add(userAccountTextField);
        contentPanel.add(confirmChangeAccountButton);

        changeUserAccountDialog = new JDialog(Main.diskManagementSystemFrame, Main.SettingState.systemLanguage ? "Change Account" : "修改用户名", true);//创建修改用户名对话窗口
        changeUserAccountDialog.setIconImage(new ImageIcon("src/material/image/dialogChangeUser.png").getImage());//设置图标
        changeUserAccountDialog.setLayout(new BorderLayout());//设置布局
        changeUserAccountDialog.add(contentPanel, BorderLayout.CENTER);//内容面板添加到中心
        changeUserAccountDialog.setAlwaysOnTop(Main.SettingState.windowState);//设置永远在最上层
        changeUserAccountDialog.pack();//设置合适
        changeUserAccountDialog.setVisible(false);//设置不可见
        changeUserAccountDialog.setLocation(Main.screenSize.width / 2 - changeUserAccountDialog.getWidth() / 2, Main.screenSize.height / 2 - changeUserAccountDialog.getHeight() / 2);//设置位置
        JRootPane changeAccountDialogRoot = changeUserAccountDialog.getRootPane();//获取修改用户名窗口的根
        changeAccountDialogRoot.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "closeChangeAccountDialog");//为根设置窗口关闭ESC按键绑定
        changeAccountDialogRoot.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_U, KeyEvent.CTRL_DOWN_MASK), "closeChangeAccountDialog");//为根设置窗口关闭Ctrl+U按键绑定
        changeAccountDialogRoot.getActionMap().put("closeChangeAccountDialog", new AbstractAction() {//当ESC按键执行时
            public void actionPerformed(ActionEvent event) {//行为执行
                changeUserAccountDialog.dispatchEvent(new WindowEvent(changeUserAccountDialog, WindowEvent.WINDOW_CLOSING));//关闭窗口
            }
        });
        changeUserAccountDialog.addWindowListener(new WindowAdapter() {//为修改用户名窗口添加窗口监听
            @Override
            public void windowClosing(WindowEvent e) {//如果窗口正在关闭
                if (bottomTipWindow != null && bottomTipWindow.isVisible()) {//如果底部提示窗口可见
                    bottomTipWindow.dispose();//底部提示窗口置空
                }
                if (buttonHoverTipWindow != null && buttonHoverTipWindow.isVisible()) {//如果按钮提示窗口不为空
                    buttonHoverTipWindow.dispose();//释放按钮提示窗口
                }
            }
        });
    }

    public static void initChangePhoneDialog() {//初始化修改手机号菜单
        userPhoneTextField = new CustomUserTextField(Main.SettingState.systemLanguage ? "Please Enter New Phone Number" : "请输入新手机号", false, false, false);//用户手机号文本域
        CustomUserTextField userVerificationTextField = new CustomUserTextField(Main.SettingState.systemLanguage ? "Please Enter Verification Code" : "请输入验证码", true, false, false);//用户验证码注册文本域
        JButton confirmChangePhoneButton = new JButton(Main.SettingState.systemLanguage ? "Confirm Change Phone Number" : "确认修改手机号");//确认修改手机号按钮
        confirmChangePhoneButton.setPreferredSize(new Dimension(Main.SettingState.systemLanguage ? 760 : 450, 35));//设置大小
        confirmChangePhoneButton.setFont(new Font("微软雅黑", PLAIN, 21));//设置字体
        confirmChangePhoneButton.setBackground(USER_LOG_BUTTON_COLOR);//背景颜色
        confirmChangePhoneButton.setFocusable(false);//不可聚焦
        confirmChangePhoneButton.setBorder(null);//无边框
        userPhoneTextField.inputTextField.addKeyListener(new KeyAdapter() {//为用户手机号文本域添加键盘监听
            @Override
            public void keyPressed(KeyEvent e) {//如果键盘按下
                if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_DOWN) {//如果按下回车或向下
                    String userPhoneInput = userPhoneTextField.inputTextField.getText();//获取输入文本
                    if (userPhoneInput.isEmpty()) {//如果输入为空
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "New Phone Number Cannot Be Empty" : "新手机号不可为空");//提示
                        userPhoneTextField.isWrong = true;//错误
                    } else if (userPhoneInput.length() != 11) {//如果手机号不为11位
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "Please Enter New 11-Digit Phone Number" : "请输入11位新手机号");//提示
                        userPhoneTextField.isWrong = true;//错误
                    } else if (Objects.equals(userPhoneInput, Main.SettingState.userPhone)) {//如果输入新手机号与旧手机号相同
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "New Phone Number Cannot Same As The Old One" : "输入新手机号与旧手机号相同");//提示
                        userPhoneTextField.isWrong = true;//错误
                    } else {//否则
                        for (int i = 0; i < userPhoneInput.length(); i++) {//遍历输入文本
                            if (!Character.isDigit(userPhoneInput.charAt(i))) {//如果非数字字符
                                createBottomTipWindow(Main.SettingState.systemLanguage ? "No Non Digit Character" : "请勿输入非数字字符");//提示
                                userPhoneTextField.isWrong = true;//错误
                                userPhoneTextField.revalidate();//重新验证
                                userPhoneTextField.repaint();//重新绘制
                                return;//直接返回
                            }
                        }
                        try {
                            if (handleUserValidateUserUnique(userPhoneInput, true)) {//如果手机号已被使用
                                createBottomTipWindow(Main.SettingState.systemLanguage ? "The New Phone Number Has Been Used" : "新手机号已被使用");//提示
                                userPhoneTextField.isWrong = true;//错误
                            } else {//否则
                                userPhone = userPhoneInput;//获取手机号
                                userPhoneTextField.isWrong = false;//正确
                                userVerificationTextField.inputTextField.requestFocusInWindow();//焦点到下一行
                            }
                        } catch (IOException ex) {
                            handleErrorLog(ex.getMessage());//处理错误日志
                            throw new RuntimeException(ex);//捕获异常
                        }
                    }
                    userPhoneTextField.revalidate();//重新验证
                    userPhoneTextField.repaint();//重新绘制
                    e.consume();//阻止默认行为
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {//如果按下ESC
                    userPhoneTextField.isWrong = false;//正确
                    userPhoneTextField.revalidate();//重新验证
                    userPhoneTextField.repaint();//重新绘制
                    changeUserPhoneDialog.requestFocusInWindow();//焦点返回菜单
                    e.consume();//阻止默认行为
                }
            }
        });
        userPhoneTextField.inputTextField.addFocusListener(new FocusAdapter() {//为用户手机号文本域添加聚焦监听
            @Override
            public void focusLost(FocusEvent e) {//如果失去聚焦
                if (changeUserPhoneDialog.isVisible()) {//如果可见
                    String userPhoneInput = userPhoneTextField.inputTextField.getText();//获取输入文本
                    if (userPhoneInput.isEmpty()) {//如果输入为空
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "New Phone Number Cannot Be Empty" : "新手机号不可为空");//提示
                        userPhoneTextField.isWrong = true;//错误
                    } else if (userPhoneInput.length() != 11) {//如果手机号不为11位
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "Please Enter New 11-Digit Phone Number" : "请输入11位新手机号");//提示
                        userPhoneTextField.isWrong = true;//错误
                    } else if (Objects.equals(userPhoneInput, Main.SettingState.userPhone)) {//如果输入新手机号与旧手机号相同
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "New Phone Number Cannot Same As The Old One" : "输入新手机号与旧手机号相同");//提示
                        userPhoneTextField.isWrong = true;//错误
                    } else {//否则
                        for (int i = 0; i < userPhoneInput.length(); i++) {//遍历输入文本
                            if (!Character.isDigit(userPhoneInput.charAt(i))) {//如果非数字字符
                                createBottomTipWindow(Main.SettingState.systemLanguage ? "No Non Digit Character" : "请勿输入非数字字符");//提示
                                userPhoneTextField.isWrong = true;//错误
                                userPhoneTextField.revalidate();//重新验证
                                userPhoneTextField.repaint();//重新绘制
                                return;//直接返回
                            }
                        }
                        try {
                            if (handleUserValidateUserUnique(userPhoneInput, true)) {//如果手机号已被使用
                                createBottomTipWindow(Main.SettingState.systemLanguage ? "The New Phone Number Has Been Used" : "新手机号已被使用");//提示
                                userPhoneTextField.isWrong = true;//错误
                            } else {//否则
                                userPhone = userPhoneInput;//获取手机号
                                userPhoneTextField.isWrong = false;//正确
                            }
                        } catch (IOException ex) {
                            handleErrorLog(ex.getMessage());//处理错误日志
                            throw new RuntimeException(ex);//捕获异常
                        }
                    }
                    userPhoneTextField.revalidate();//重新验证
                    userPhoneTextField.repaint();//重新绘制
                }
            }
        });
        userPhoneTextField.inputTextField.addMouseListener(new MouseAdapter() {//为用户手机号文本域添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Please Enter New Phone Number (Which Is 11-Digit)" : "请输入新手机号（手机号为11位）", userPhoneTextField.inputTextField));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
            }

            @Override
            public void mouseExited(MouseEvent e) {//如果鼠标离开
                if (hoverTimer != null) {//如果不为空
                    hoverTimer.stop();//计时器结束
                }
                if (buttonHoverTipWindow != null) {//如果提示信息不为空
                    buttonHoverTipWindow.dispose();//释放提示信息
                    buttonHoverTipWindow = null;//提示信息置空
                }
            }
        });
        userPhoneTextField.verificationButton.addMouseListener(new MouseAdapter() {//为用户手机号文本域验证码按钮添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Click To Send Verification Code (Which Is 6 Digit, If You Do Not Receive, Verification Code Expire, Or Incorrect Verification Code, Please Wait For 1 Minute Countdown To Resend)" : "点击发送验证码（验证码为6位，有效期为5分钟，如果没有收到验证码、验证码过期或验证码错误，请等待1分钟倒计时结束重新发送验证码）", userPhoneTextField.inputTextField));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
            }

            @Override
            public void mouseExited(MouseEvent e) {//如果鼠标离开
                if (hoverTimer != null) {//如果不为空
                    hoverTimer.stop();//计时器结束
                }
                if (buttonHoverTipWindow != null) {//如果提示信息不为空
                    buttonHoverTipWindow.dispose();//释放提示信息
                    buttonHoverTipWindow = null;//提示信息置空
                }
            }
        });
        userVerificationTextField.inputTextField.addKeyListener(new KeyAdapter() {//为用户验证码文本域添加键盘监听
            @Override
            public void keyPressed(KeyEvent e) {//如果键盘按下
                if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_UP) {//如果按下回车或向上
                    if (userVerificationTextField.inputTextField.getText().isEmpty()) {//如果输入为空
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "Verification Code Cannot Be Empty" : "验证码不可为空");//提示
                        userVerificationTextField.isWrong = true;//错误
                    } else {//否则
                        userVerificationTextField.isWrong = false;//正确
                        if (e.getKeyCode() == KeyEvent.VK_ENTER) {//如果是回车
                            confirmChangePhoneButton.doClick();//点击
                        } else {//否则
                            userPhoneTextField.inputTextField.requestFocusInWindow();//焦点到上一行
                        }
                    }
                    userVerificationTextField.revalidate();//重新验证
                    userVerificationTextField.repaint();//重新绘制
                    e.consume();//阻止默认行为
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {//如果按下ESC
                    userVerificationTextField.isWrong = false;//正确
                    userVerificationTextField.revalidate();//重新验证
                    userVerificationTextField.repaint();//重新绘制
                    changeUserPhoneDialog.requestFocusInWindow();//焦点返回菜单
                    e.consume();//阻止默认行为
                }
            }
        });
        userVerificationTextField.inputTextField.addFocusListener(new FocusAdapter() {//为用户验证码文本域添加聚焦监听
            @Override
            public void focusLost(FocusEvent e) {//如果失去聚焦
                if (changeUserPhoneDialog.isVisible()) {//如果可见
                    if (userVerificationTextField.inputTextField.getText().isEmpty()) {//如果输入为空
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "Verification Code Cannot Be Empty" : "验证码不可为空");//提示
                        userVerificationTextField.isWrong = true;//错误
                    } else {//否则
                        userVerificationTextField.isWrong = false;//正确
                    }
                    userVerificationTextField.revalidate();//重新验证
                    userVerificationTextField.repaint();//重新绘制
                }
            }
        });
        userVerificationTextField.inputTextField.addMouseListener(new MouseAdapter() {//为用户验证码文本域添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Please Enter Verification Code (Which Is 6 Digit, If You Do Not Receive, Verification Code Expire, Or Incorrect Verification Code, Please Resend)" : "请输入验证码（验证码为6位，如果没有收到验证码、验证码过期或验证码错误，请重新发送验证码）", userVerificationTextField.inputTextField));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
            }

            @Override
            public void mouseExited(MouseEvent e) {//如果鼠标离开
                if (hoverTimer != null) {//如果不为空
                    hoverTimer.stop();//计时器结束
                }
                if (buttonHoverTipWindow != null) {//如果提示信息不为空
                    buttonHoverTipWindow.dispose();//释放提示信息
                    buttonHoverTipWindow = null;//提示信息置空
                }
            }
        });
        confirmChangePhoneButton.addActionListener(_ -> {//为确认修改手机号按钮添加事件监听
            userPhoneTextField.isWrong = false;//正确
            userPhoneTextField.revalidate();//重新验证
            userPhoneTextField.repaint();//重新绘制
            userVerificationTextField.isWrong = false;//正确
            userVerificationTextField.revalidate();//重新验证
            userVerificationTextField.repaint();//重新绘制
            String userPhoneInput = userPhoneTextField.inputTextField.getText().trim();//获取手机号输入
            String userVerificationCodeInput = userVerificationTextField.inputTextField.getText().trim();//获取用户验证码输入
            if (userPhoneInput.isEmpty() && userVerificationCodeInput.isEmpty()) {//如果新手机号和验证码都为空
                createBottomTipWindow(Main.SettingState.systemLanguage ? "New Phone Number And Verification Code Cannot Be Empty" : "新手机号和验证码不可为空");//提示
                userPhoneTextField.isWrong = true;//错误
                userPhoneTextField.revalidate();//重新验证
                userPhoneTextField.repaint();//重新绘制
                userVerificationTextField.isWrong = true;//错误
                userVerificationTextField.revalidate();//重新验证
                userVerificationTextField.repaint();//重新绘制
                return;//直接返回
            } else if (userPhoneInput.isEmpty()) {//如果手机号为空
                createBottomTipWindow(Main.SettingState.systemLanguage ? "New Phone Number Cannot Be Empty" : "新手机号不可为空");//提示
                userPhoneTextField.isWrong = true;//错误
                userPhoneTextField.revalidate();//重新验证
                userPhoneTextField.repaint();//重新绘制
                return;//直接返回
            } else if (userVerificationCodeInput.isEmpty()) {//如果验证码为空
                createBottomTipWindow(Main.SettingState.systemLanguage ? "Verification Code Cannot Be Empty" : "验证码不可为空");//提示
                userVerificationTextField.isWrong = true;//错误
                userVerificationTextField.revalidate();//重新验证
                userVerificationTextField.repaint();//重新绘制
                return;//直接返回
            }
            if (userPhoneInput.length() != 11) {//如果手机号不为11位
                createBottomTipWindow(Main.SettingState.systemLanguage ? "Please Enter New 11-Digit Phone Number" : "请输入11位新手机号");//提示
                userPhoneTextField.isWrong = true;//错误
                userPhoneTextField.revalidate();//重新验证
                userPhoneTextField.repaint();//重新绘制
                return;//直接返回
            } else if (Objects.equals(userPhoneInput, Main.SettingState.userPhone)) {//如果输入新手机号与旧手机号相同
                createBottomTipWindow(Main.SettingState.systemLanguage ? "New Phone Number Cannot Same As The Old One" : "输入新手机号与旧手机号相同");//提示
                userPhoneTextField.isWrong = true;//错误
                userPhoneTextField.revalidate();//重新验证
                userPhoneTextField.repaint();//重新绘制
                return;//直接返回
            } else {//否则
                for (int i = 0; i < userPhoneInput.length(); i++) {//遍历输入文本
                    if (!Character.isDigit(userPhoneInput.charAt(i))) {//如果非数字字符
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "No Non Digit Character" : "请勿输入非数字字符");//提示
                        userPhoneTextField.isWrong = true;//错误
                        userPhoneTextField.revalidate();//重新验证
                        userPhoneTextField.repaint();//重新绘制
                        return;//直接返回
                    }
                }
                try {
                    if (handleUserValidateUserUnique(userPhoneInput, true)) {//如果新手机号已被使用
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "The New Phone Number Has Been Used" : "新手机号已被使用");//提示
                        userPhoneTextField.isWrong = true;//错误
                        userPhoneTextField.revalidate();//重新验证
                        userPhoneTextField.repaint();//重新绘制
                        return;//直接返回
                    }
                } catch (IOException e) {
                    handleErrorLog(e.getMessage());//处理错误日志
                    throw new RuntimeException(e);//捕获异常
                }
            }
            userPhone = userPhoneInput;//获取手机号
            try {
                if (handleUserVerifyVerificationCode(userVerificationCodeInput) != 0) {//如果验证验证码失败
                    return;//直接返回
                }
            } catch (IOException e) {
                handleErrorLog(e.getMessage());//处理错误日志
                throw new RuntimeException(e);//捕获异常
            }
            try {
                if (handleUserModifyUserAccount(Main.SettingState.userAccount, userPhone, 2)) {//修改云端数据：如果成功
                    Main.SettingState.userPhone = userPhone;//修改
                    changeUserPhoneDialog.dispose();//释放
                    userDialog.dispose();//释放
                    updateUserDialog(2);//重新加载
                    userDialog.setVisible(true);//可见
                }
            } catch (IOException e) {
                handleErrorLog(e.getMessage());//处理错误日志
                throw new RuntimeException(e);//捕获异常
            }
        });
        confirmChangePhoneButton.addMouseListener(new MouseAdapter() {//为确认修改手机号按钮添加鼠标监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Confirm Change Phone Number" : "确认修改手机号", confirmChangePhoneButton));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
            }

            @Override
            public void mouseExited(MouseEvent e) {//如果鼠标离开
                if (hoverTimer != null) {//如果不为空
                    hoverTimer.stop();//计时器结束
                }
                if (buttonHoverTipWindow != null) {//如果提示信息不为空
                    buttonHoverTipWindow.dispose();//释放提示信息
                    buttonHoverTipWindow = null;//提示信息置空
                }
            }
        });
        JPanel contentPanel = new JPanel(new GridLayout(0, 1, 5, 5));//创建内容面板
        contentPanel.setBorder(BorderFactory.createEmptyBorder(7, 10, 7, 10));//创建边框
        contentPanel.setBackground(Main.SettingState.themeColor ? DARK_DIALOG_MAIN_COLOR : LIGHT_DIALOG_MAIN_COLOR);//设置背景颜色
        contentPanel.add(userPhoneTextField);
        contentPanel.add(userVerificationTextField);
        contentPanel.add(confirmChangePhoneButton);

        changeUserPhoneDialog = new JDialog(Main.diskManagementSystemFrame, Main.SettingState.systemLanguage ? "Change Phone Number" : "修改手机号", true);//创建修改用户手机号对话窗口
        changeUserPhoneDialog.setIconImage(new ImageIcon("src/material/image/dialogChangeUser.png").getImage());//设置图标
        changeUserPhoneDialog.setLayout(new BorderLayout());//设置布局
        changeUserPhoneDialog.add(contentPanel, BorderLayout.CENTER);//内容面板添加到中心
        changeUserPhoneDialog.setAlwaysOnTop(Main.SettingState.windowState);//设置永远在最上层
        changeUserPhoneDialog.pack();//设置合适
        changeUserPhoneDialog.setVisible(false);//设置不可见
        changeUserPhoneDialog.setLocation(Main.screenSize.width / 2 - changeUserPhoneDialog.getWidth() / 2, Main.screenSize.height / 2 - changeUserPhoneDialog.getHeight() / 2);//设置位置
        JRootPane changePhoneDialogRoot = changeUserPhoneDialog.getRootPane();//获取修改手机号窗口的根
        changePhoneDialogRoot.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "closeChangePhoneDialog");//为根设置窗口关闭ESC按键绑定
        changePhoneDialogRoot.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_U, KeyEvent.CTRL_DOWN_MASK), "closeChangePhoneDialog");//为根设置窗口关闭Ctrl+U按键绑定
        changePhoneDialogRoot.getActionMap().put("closeChangePhoneDialog", new AbstractAction() {//当ESC按键执行时
            public void actionPerformed(ActionEvent event) {//行为执行
                changeUserPhoneDialog.dispatchEvent(new WindowEvent(changeUserPhoneDialog, WindowEvent.WINDOW_CLOSING));//关闭窗口
            }
        });
        changeUserPhoneDialog.addWindowListener(new WindowAdapter() {//为修改手机号窗口添加窗口监听
            @Override
            public void windowClosing(WindowEvent e) {//如果窗口正在关闭
                if (bottomTipWindow != null && bottomTipWindow.isVisible()) {//如果底部提示窗口可见
                    bottomTipWindow.dispose();//底部提示窗口置空
                }
                if (buttonHoverTipWindow != null && buttonHoverTipWindow.isVisible()) {//如果按钮提示窗口不为空
                    buttonHoverTipWindow.dispose();//释放按钮提示窗口
                }
            }
        });
    }

    public static void initChangePasswordDialog() {//初始化修改密码菜单
        CustomUserTextField userPasswordTextField = new CustomUserTextField(Main.SettingState.systemLanguage ? "Please Enter New Password (Which Is At Least 6-Digit)" : "请输入新密码（新密码至少为6位）", false, true, false);//用户密码文本域
        CustomUserTextField userConfirmPasswordTextField = new CustomUserTextField(Main.SettingState.systemLanguage ? "Please Confirm New Password" : "请确认新密码", false, true, false);//用户确认密码文本域
        JButton confirmChangePasswordButton = new JButton(Main.SettingState.systemLanguage ? "Confirm Change Password" : "确认修改密码");//确认修改密码按钮
        confirmChangePasswordButton.setPreferredSize(new Dimension(Main.SettingState.systemLanguage ? 760 : 450, 35));//设置大小
        confirmChangePasswordButton.setFont(new Font("微软雅黑", PLAIN, 21));//设置字体
        confirmChangePasswordButton.setBackground(USER_LOG_BUTTON_COLOR);//背景颜色
        confirmChangePasswordButton.setFocusable(false);//不可聚焦
        confirmChangePasswordButton.setBorder(null);//无边框
        userPasswordTextField.passwordField.addKeyListener(new KeyAdapter() {//为用户密码文本域添加键盘监听
            @Override
            public void keyPressed(KeyEvent e) {//如果键盘按下
                if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_DOWN) {//如果按下回车或向下
                    String userPasswordInput = userPasswordTextField.passwordField.getText();//获取输入文本
                    if (userPasswordInput.isEmpty()) {//如果输入为空
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "New Password Cannot Be Empty" : "新密码不可为空");//提示
                        userPasswordTextField.isWrong = true;//错误
                    } else if (userPasswordInput.length() < 6) {//如果密码长度小于6位
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "New Password Is At Least 6-Digit" : "新密码至少为6位");//提示
                        userPasswordTextField.isWrong = true;//错误
                    } else if (Objects.equals(userPasswordInput, Main.SettingState.userPassword)) {//如果输入新密码与旧密码相同
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "New Password Cannot Same As The Old One" : "输入新密码与旧密码相同");//提示
                        userPasswordTextField.isWrong = true;//错误
                    } else {//否则
                        for (int i = 0; i < userPasswordInput.length(); i++) {//遍历输入文本
                            char c = userPasswordInput.charAt(i);//获取当前字符
                            if (c == '/' || c == '\\' || c == '"' || c == '{' || c == '}' || c == ':') {//如果是特殊字符
                                createBottomTipWindow(Main.SettingState.systemLanguage ? "No Special Character" : "请勿输入特殊字符");//提示
                                userPasswordTextField.isWrong = true;//错误
                                userPasswordTextField.revalidate();//重新验证
                                userPasswordTextField.repaint();//重新绘制
                                return;//直接返回
                            }
                        }
                        userPasswordTextField.isWrong = false;//正确
                        userConfirmPasswordTextField.passwordField.requestFocusInWindow();//焦点到下一行
                    }
                    userPasswordTextField.revalidate();//重新验证
                    userPasswordTextField.repaint();//重新绘制
                    e.consume();//阻止默认行为
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {//如果按下ESC
                    userPasswordTextField.isWrong = false;//正确
                    userPasswordTextField.revalidate();//重新验证
                    userPasswordTextField.repaint();//重新绘制
                    changeUserPasswordDialog.requestFocusInWindow();//焦点返回菜单
                    e.consume();//阻止默认行为
                }
            }
        });
        userPasswordTextField.passwordField.addFocusListener(new FocusAdapter() {//为用户密码文本域添加聚焦监听
            @Override
            public void focusLost(FocusEvent e) {//如果失去聚焦
                if (changeUserPasswordDialog.isVisible()) {//如果可见
                    String passwordInput = userPasswordTextField.passwordField.getText();//获取输入文本
                    if (passwordInput.isEmpty()) {//如果输入为空
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "New Password Cannot Be Empty" : "新密码不可为空");//提示
                        userPasswordTextField.isWrong = true;//错误
                    } else if (passwordInput.length() < 6) {//如果密码长度小于6位
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "New Password Is At Least 6-Digit" : "新密码至少为6位");//提示
                        userPasswordTextField.isWrong = true;//错误
                    } else if (Objects.equals(passwordInput, Main.SettingState.userPassword)) {//如果输入新密码与旧密码相同
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "New Password Cannot Same As The Old One" : "输入新密码与旧密码相同");//提示
                        userPasswordTextField.isWrong = true;//错误
                    } else {//否则
                        for (int i = 0; i < passwordInput.length(); i++) {//遍历输入文本
                            char c = passwordInput.charAt(i);//获取当前字符
                            if (c == '/' || c == '\\' || c == '"' || c == '{' || c == '}' || c == ':') {//如果是特殊字符
                                createBottomTipWindow(Main.SettingState.systemLanguage ? "No Special Character" : "请勿输入特殊字符");//提示
                                userPasswordTextField.isWrong = true;//错误
                                userPasswordTextField.revalidate();//重新验证
                                userPasswordTextField.repaint();//重新绘制
                                return;//直接返回
                            }
                        }
                        userPasswordTextField.isWrong = false;//正确
                    }
                    userPasswordTextField.revalidate();//重新验证
                    userPasswordTextField.repaint();//重新绘制
                }
            }
        });
        userPasswordTextField.passwordField.addMouseListener(new MouseAdapter() {//为用户密码文本域添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Please Enter New Password (To Ensure Strength Of Password, It Must Be At Least 6-Digit And Cannot Contain Special Character '/' '\\' '\"' '{' '}' ':')" : "请输入新密码（为保证密码强度，新密码至少为6位且新密码不可包含特殊字符 '/' '\\' '\"' '{' '}' ':'）", userPasswordTextField.passwordField));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
            }

            @Override
            public void mouseExited(MouseEvent e) {//如果鼠标离开
                if (hoverTimer != null) {//如果不为空
                    hoverTimer.stop();//计时器结束
                }
                if (buttonHoverTipWindow != null) {//如果提示信息不为空
                    buttonHoverTipWindow.dispose();//释放提示信息
                    buttonHoverTipWindow = null;//提示信息置空
                }
            }
        });
        userConfirmPasswordTextField.passwordField.addKeyListener(new KeyAdapter() {//为用户确认密码文本域添加键盘监听
            @Override
            public void keyPressed(KeyEvent e) {//如果键盘按下
                if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_UP) {//如果按下回车或向上
                    if (userConfirmPasswordTextField.passwordField.getText().isEmpty()) {//如果输入为空
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "Confirm New Password Cannot Be Empty" : "确认新密码不可为空");//提示
                        userConfirmPasswordTextField.isWrong = true;//错误
                    } else {//否则
                        userConfirmPasswordTextField.isWrong = false;//正确
                        if (e.getKeyCode() == KeyEvent.VK_ENTER) {//如果是回车
                            confirmChangePasswordButton.doClick();//点击
                        } else {//否则
                            userPasswordTextField.passwordField.requestFocusInWindow();//焦点到上一行
                        }
                    }
                    userConfirmPasswordTextField.revalidate();//重新验证
                    userConfirmPasswordTextField.repaint();//重新绘制
                    e.consume();//阻止默认行为
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {//如果按下ESC
                    userConfirmPasswordTextField.isWrong = false;//正确
                    userConfirmPasswordTextField.revalidate();//重新验证
                    userConfirmPasswordTextField.repaint();//重新绘制
                    changeUserPasswordDialog.requestFocusInWindow();//焦点返回菜单
                    e.consume();//阻止默认行为
                }
            }
        });
        userConfirmPasswordTextField.passwordField.addFocusListener(new FocusAdapter() {//为用户确认密码文本域添加聚焦监听
            @Override
            public void focusLost(FocusEvent e) {//如果失去聚焦
                if (changeUserPasswordDialog.isVisible()) {//如果可见
                    if (userConfirmPasswordTextField.passwordField.getText().isEmpty()) {//如果输入为空
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "Confirm New Password Cannot Be Empty" : "确认新密码不可为空");//提示
                        userConfirmPasswordTextField.isWrong = true;//错误
                    } else {//否则
                        userConfirmPasswordTextField.isWrong = false;//正确
                    }
                    userConfirmPasswordTextField.revalidate();//重新验证
                    userConfirmPasswordTextField.repaint();//重新绘制
                }
            }
        });
        userConfirmPasswordTextField.passwordField.addMouseListener(new MouseAdapter() {//为用户确认密码文本域添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Please Confirm New Password (Make Sure That Password Is The Same)" : "请确认新密码（请保证两次输入密码一致）", userConfirmPasswordTextField.passwordField));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
            }

            @Override
            public void mouseExited(MouseEvent e) {//如果鼠标离开
                if (hoverTimer != null) {//如果不为空
                    hoverTimer.stop();//计时器结束
                }
                if (buttonHoverTipWindow != null) {//如果提示信息不为空
                    buttonHoverTipWindow.dispose();//释放提示信息
                    buttonHoverTipWindow = null;//提示信息置空
                }
            }
        });
        confirmChangePasswordButton.addActionListener(_ -> {//为确认修改密码按钮添加事件监听
            userPasswordTextField.isWrong = false;//正确
            userPasswordTextField.revalidate();//重新验证
            userPasswordTextField.repaint();//重新绘制
            userConfirmPasswordTextField.isWrong = false;//正确
            userConfirmPasswordTextField.revalidate();//重新验证
            userConfirmPasswordTextField.repaint();//重新绘制
            String passwordInput = userPasswordTextField.passwordField.getText().trim();//获取用户密码密码
            String userConfirmPasswordInput = userConfirmPasswordTextField.passwordField.getText().trim();//获取用户确认密码输入
            if (passwordInput.isEmpty() && userConfirmPasswordInput.isEmpty()) {//如果密码和确认密码都为空
                createBottomTipWindow(Main.SettingState.systemLanguage ? "New Password And Confirm New Password Cannot Be Empty" : "新密码和确认新密码不可为空");//提示
                userPasswordTextField.isWrong = true;//错误
                userPasswordTextField.revalidate();//重新验证
                userPasswordTextField.repaint();//重新绘制
                userConfirmPasswordTextField.isWrong = true;//错误
                userConfirmPasswordTextField.revalidate();//重新验证
                userConfirmPasswordTextField.repaint();//重新绘制
                return;//直接返回
            } else if (passwordInput.isEmpty()) {//如果密码为空
                createBottomTipWindow(Main.SettingState.systemLanguage ? "New Password Cannot Be Empty" : "新密码不可为空");//提示
                userPasswordTextField.isWrong = true;//错误
                userPasswordTextField.revalidate();//重新验证
                userPasswordTextField.repaint();//重新绘制
                return;//直接返回
            } else if (userConfirmPasswordInput.isEmpty()) {//如果确认密码为空
                createBottomTipWindow(Main.SettingState.systemLanguage ? "Confirm New Password Cannot Be Empty" : "确认新密码不可为空");//提示
                userConfirmPasswordTextField.isWrong = true;//错误
                userConfirmPasswordTextField.revalidate();//重新验证
                userConfirmPasswordTextField.repaint();//重新绘制
                return;//直接返回
            }
            if (passwordInput.length() < 6) {//如果密码长度小于6位
                createBottomTipWindow(Main.SettingState.systemLanguage ? "New Password Is At Least 6-Digit" : "新密码至少为6位");//提示
                userPasswordTextField.isWrong = true;//错误
                userPasswordTextField.revalidate();//重新验证
                userPasswordTextField.repaint();//重新绘制
                return;//直接返回
            } else if (Objects.equals(passwordInput, Main.SettingState.userPassword)) {//如果输入新密码与旧密码相同
                createBottomTipWindow(Main.SettingState.systemLanguage ? "New Password Cannot Same As The Old One" : "输入新密码与旧密码相同");//提示
                userPasswordTextField.isWrong = true;//错误
                userPasswordTextField.revalidate();//重新验证
                userPasswordTextField.repaint();//重新绘制
                return;//直接返回
            } else {//否则
                for (int i = 0; i < passwordInput.length(); i++) {//遍历输入文本
                    char c = passwordInput.charAt(i);//获取当前字符
                    if (c == '/' || c == '\\' || c == '"' || c == '{' || c == '}' || c == ':') {//如果是特殊字符
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "No Special Character" : "请勿输入特殊字符");//提示
                        userPasswordTextField.isWrong = true;//错误
                        userPasswordTextField.revalidate();//重新验证
                        userPasswordTextField.repaint();//重新绘制
                        return;//直接返回
                    }
                }
            }
            if (!passwordInput.equals(userConfirmPasswordInput)) {//验证密码是否一致
                createBottomTipWindow(Main.SettingState.systemLanguage ? "The Password Entered Twice Is Inconsistent" : "两次输入的密码不一致");//提示
                return;//直接返回
            }
            try {
                if (handleUserModifyUserAccount(Main.SettingState.userAccount, passwordInput, 3)) {//修改云端数据：如果成功
                    Main.SettingState.userPassword = passwordInput;//修改
                    changeUserPasswordDialog.dispose();//释放
                }
            } catch (IOException e) {
                handleErrorLog(e.getMessage());//处理错误日志
                throw new RuntimeException(e);//捕获异常
            }
        });
        confirmChangePasswordButton.addMouseListener(new MouseAdapter() {//为确认修改密码添加事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Confirm Change Password (Make Sure That Password Is The Same)" : "确认修改密码（请保证两次输入密码一致）", confirmChangePasswordButton));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
            }

            @Override
            public void mouseExited(MouseEvent e) {//如果鼠标离开
                if (hoverTimer != null) {//如果不为空
                    hoverTimer.stop();//计时器结束
                }
                if (buttonHoverTipWindow != null) {//如果提示信息不为空
                    buttonHoverTipWindow.dispose();//释放提示信息
                    buttonHoverTipWindow = null;//提示信息置空
                }
            }
        });
        JPanel contentPanel = new JPanel(new GridLayout(0, 1, 5, 5));//创建内容面板
        contentPanel.setBorder(BorderFactory.createEmptyBorder(7, 10, 7, 10));//创建边框
        contentPanel.setBackground(Main.SettingState.themeColor ? DARK_DIALOG_MAIN_COLOR : LIGHT_DIALOG_MAIN_COLOR);//设置背景颜色
        contentPanel.add(userPasswordTextField);
        contentPanel.add(userConfirmPasswordTextField);
        contentPanel.add(confirmChangePasswordButton);

        changeUserPasswordDialog = new JDialog(Main.diskManagementSystemFrame, Main.SettingState.systemLanguage ? "Change Password" : "修改密码", true);//创建修改用户密码对话窗口
        changeUserPasswordDialog.setIconImage(new ImageIcon("src/material/image/dialogChangeUser.png").getImage());//设置图标
        changeUserPasswordDialog.setLayout(new BorderLayout());//设置布局
        changeUserPasswordDialog.add(contentPanel, BorderLayout.CENTER);//内容面板添加到中心
        changeUserPasswordDialog.setAlwaysOnTop(Main.SettingState.windowState);//设置永远在最上层
        changeUserPasswordDialog.pack();//设置合适
        changeUserPasswordDialog.setVisible(false);//设置不可见
        changeUserPasswordDialog.setLocation(Main.screenSize.width / 2 - changeUserPasswordDialog.getWidth() / 2, Main.screenSize.height / 2 - changeUserPasswordDialog.getHeight() / 2);//设置位置
        JRootPane changePasswordDialogRoot = changeUserPasswordDialog.getRootPane();//获取修改密码窗口的根
        changePasswordDialogRoot.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "closeChangePasswordDialog");//为根设置窗口关闭ESC按键绑定
        changePasswordDialogRoot.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_U, KeyEvent.CTRL_DOWN_MASK), "closeChangePasswordDialog");//为根设置窗口关闭Ctrl+U按键绑定
        changePasswordDialogRoot.getActionMap().put("closeChangePasswordDialog", new AbstractAction() {//当ESC按键执行时
            public void actionPerformed(ActionEvent event) {//行为执行
                changeUserPasswordDialog.dispatchEvent(new WindowEvent(changeUserPasswordDialog, WindowEvent.WINDOW_CLOSING));//关闭窗口
            }
        });
        changeUserPasswordDialog.addWindowListener(new WindowAdapter() {//为修改密码窗口添加窗口监听
            @Override
            public void windowClosing(WindowEvent e) {//如果窗口正在关闭
                if (bottomTipWindow != null && bottomTipWindow.isVisible()) {//如果底部提示窗口可见
                    bottomTipWindow.dispose();//底部提示窗口置空
                }
                if (buttonHoverTipWindow != null && buttonHoverTipWindow.isVisible()) {//如果按钮提示窗口不为空
                    buttonHoverTipWindow.dispose();//释放按钮提示窗口
                }
            }
        });
    }

    public static void initLogOutDialog() {//初始化注销菜单
        CustomUserTextField userPasswordTextField = new CustomUserTextField(Main.SettingState.systemLanguage ? "Please Enter Password" : "请确认密码", false, true, false);//用户密码文本域
        JButton confirmLogOutButton = new JButton(Main.SettingState.systemLanguage ? "Confirm Logout" : "确认注销");//确认注销按钮
        confirmLogOutButton.setPreferredSize(new Dimension(Main.SettingState.systemLanguage ? 760 : 450, 35));//设置大小
        confirmLogOutButton.setFont(new Font("微软雅黑", PLAIN, 21));//设置字体
        confirmLogOutButton.setBackground(USER_LOG_BUTTON_COLOR);//背景颜色
        confirmLogOutButton.setFocusable(false);//不可聚焦
        confirmLogOutButton.setBorder(null);//无边框
        userPasswordTextField.passwordField.addKeyListener(new KeyAdapter() {//为用户密码文本域添加键盘监听
            @Override
            public void keyPressed(KeyEvent e) {//如果键盘按下
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {//如果按下回车
                    String userPasswordInput = userPasswordTextField.passwordField.getText();//获取输入文本
                    if (userPasswordInput.isEmpty()) {//如果输入为空
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "Password Cannot Be Empty" : "密码不可为空");//提示
                        userPasswordTextField.isWrong = true;//错误
                    } else if (userPasswordInput.length() < 6) {//如果密码长度小于6位
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "Password Is At Least 6-Digit" : "密码至少为6位");//提示
                        userPasswordTextField.isWrong = true;//错误
                    } else if (!Objects.equals(userPasswordInput, Main.SettingState.userPassword)) {//如果密码不一致
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "Incorrect Password, Please Re-Enter" : "密码错误，请重新输入");//提示
                        userPasswordTextField.isWrong = true;//错误
                    } else {//否则
                        userPasswordTextField.isWrong = false;//正确
                        confirmLogOutButton.doClick();//点击
                    }
                    userPasswordTextField.revalidate();//重新验证
                    userPasswordTextField.repaint();//重新绘制
                    e.consume();//阻止默认行为
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {//如果按下ESC
                    userPasswordTextField.isWrong = false;//正确
                    userPasswordTextField.revalidate();//重新验证
                    userPasswordTextField.repaint();//重新绘制
                    logOutDialog.requestFocusInWindow();//焦点返回菜单
                    e.consume();//阻止默认行为
                }
            }
        });
        userPasswordTextField.passwordField.addFocusListener(new FocusAdapter() {//为用户密码注册文本域添加聚焦监听
            @Override
            public void focusLost(FocusEvent e) {//如果失去聚焦
                if (logOutDialog.isVisible()) {//如果可见
                    String userPasswordInput = userPasswordTextField.passwordField.getText();//获取输入文本
                    if (userPasswordInput.isEmpty()) {//如果输入为空
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "Password Cannot Be Empty" : "密码不可为空");//提示
                        userPasswordTextField.isWrong = true;//错误
                    } else if (userPasswordInput.length() < 6) {//如果密码长度小于6位
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "Password Is At Least 6-Digit" : "密码至少为6位");//提示
                        userPasswordTextField.isWrong = true;//错误
                    } else if (!Objects.equals(userPasswordInput, Main.SettingState.userPassword)) {//如果密码不一致
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "Incorrect Password, Please Re-Enter" : "密码错误，请重新输入");//提示
                        userPasswordTextField.isWrong = true;//错误
                    } else {//否则
                        userPasswordTextField.isWrong = false;//正确
                    }
                    userPasswordTextField.revalidate();//重新验证
                    userPasswordTextField.repaint();//重新绘制
                }
            }
        });
        userPasswordTextField.passwordField.addMouseListener(new MouseAdapter() {//为用户密码注册文本域添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Please Enter Password (Please Note That Logout Cannot Be Reversed, All Your Cloud Storage Data Will Lost, Please Be Cautious)" : "请输入密码（请注意，注销账户操作无法撤销，您的云盘数据将全部丢失，请谨慎操作）", userPasswordTextField.passwordField));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
            }

            @Override
            public void mouseExited(MouseEvent e) {//如果鼠标离开
                if (hoverTimer != null) {//如果不为空
                    hoverTimer.stop();//计时器结束
                }
                if (buttonHoverTipWindow != null) {//如果提示信息不为空
                    buttonHoverTipWindow.dispose();//释放提示信息
                    buttonHoverTipWindow = null;//提示信息置空
                }
            }
        });
        confirmLogOutButton.addActionListener(_ -> {//为确认注销按钮添加事件监听
            String userPasswordInput = userPasswordTextField.passwordField.getText().trim();//获取用户密码密码
            if (userPasswordInput.isEmpty()) {//密码为空
                createBottomTipWindow(Main.SettingState.systemLanguage ? "Password Cannot Be Empty" : "密码不可为空");//提示
                userPasswordTextField.isWrong = true;//错误
                userPasswordTextField.revalidate();//重新验证
                userPasswordTextField.repaint();//重新绘制
                return;//直接返回
            } else if (userPasswordInput.length() < 6) {//如果密码长度小于6位
                createBottomTipWindow(Main.SettingState.systemLanguage ? "Password Is At Least 6-Digit" : "密码至少为6位");//提示
                userPasswordTextField.isWrong = true;//错误
                userPasswordTextField.revalidate();//重新验证
                userPasswordTextField.repaint();//重新绘制
                return;//直接返回
            } else if (!Objects.equals(userPasswordInput, Main.SettingState.userPassword)) {//如果密码不一致
                createBottomTipWindow(Main.SettingState.systemLanguage ? "Incorrect Password, Please Re-Enter" : "密码错误，请重新输入");//提示
                userPasswordTextField.isWrong = true;//错误
                userPasswordTextField.revalidate();//重新验证
                userPasswordTextField.repaint();//重新绘制
                return;//直接返回
            }
            try {
                if (handleUserRemoveUserUploadPicture(null, true) && handleUserRemoveUserInformation(Main.SettingState.userAccount)) {//如果注销成功：云端数据和用户数据删除成功
                    logOutDialog.dispose();//释放
                    logOut();//注销
                }
            } catch (IOException e) {
                handleErrorLog(e.getMessage());//处理错误日志
                throw new RuntimeException(e);//捕获异常
            }
        });
        confirmLogOutButton.addMouseListener(new MouseAdapter() {//为确认注销按钮添加鼠标监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Please Note That Logout Cannot Be Reversed, All Your Cloud Storage Data Will Lost, Please Be Cautious" : "请注意，注销账户操作无法撤销，您的云盘数据将全部丢失，请谨慎操作", confirmLogOutButton));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
            }

            @Override
            public void mouseExited(MouseEvent e) {//如果鼠标离开
                if (hoverTimer != null) {//如果不为空
                    hoverTimer.stop();//计时器结束
                }
                if (buttonHoverTipWindow != null) {//如果提示信息不为空
                    buttonHoverTipWindow.dispose();//释放提示信息
                    buttonHoverTipWindow = null;//提示信息置空
                }
            }
        });
        JPanel contentPanel = new JPanel(new GridLayout(0, 1, 5, 5));//创建内容面板
        contentPanel.setBorder(BorderFactory.createEmptyBorder(7, 10, 7, 10));//创建边框
        contentPanel.setBackground(Main.SettingState.themeColor ? DARK_DIALOG_MAIN_COLOR : LIGHT_DIALOG_MAIN_COLOR);//设置背景颜色
        contentPanel.add(userPasswordTextField);
        contentPanel.add(confirmLogOutButton);

        logOutDialog = new JDialog(Main.diskManagementSystemFrame, Main.SettingState.systemLanguage ? "Log Out" : "注销", true);//创建注销对话窗口
        logOutDialog.setIconImage(new ImageIcon("src/material/image/dialogLogOut.png").getImage());//设置图标
        logOutDialog.setLayout(new BorderLayout());//设置布局
        logOutDialog.add(contentPanel, BorderLayout.CENTER);//内容面板添加到中心
        logOutDialog.setAlwaysOnTop(Main.SettingState.windowState);//设置永远在最上层
        logOutDialog.pack();//设置合适
        logOutDialog.setVisible(false);//设置不可见
        logOutDialog.setLocation(Main.screenSize.width / 2 - logOutDialog.getWidth() / 2, Main.screenSize.height / 2 - logOutDialog.getHeight() / 2);//设置位置
        JRootPane logOutDialogRoot = logOutDialog.getRootPane();//获取注销窗口的根
        logOutDialogRoot.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "closeLogOutDialog");//为根设置窗口关闭ESC按键绑定
        logOutDialogRoot.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_U, KeyEvent.CTRL_DOWN_MASK), "closeLogOutDialog");//为根设置窗口关闭Ctrl+U按键绑定
        logOutDialogRoot.getActionMap().put("closeLogOutDialog", new AbstractAction() {//当ESC按键执行时
            public void actionPerformed(ActionEvent event) {//行为执行
                logOutDialog.dispatchEvent(new WindowEvent(logOutDialog, WindowEvent.WINDOW_CLOSING));//关闭窗口
            }
        });
        logOutDialog.addWindowListener(new WindowAdapter() {//为注销窗口添加窗口监听
            @Override
            public void windowClosing(WindowEvent e) {//如果窗口正在关闭
                if (bottomTipWindow != null && bottomTipWindow.isVisible()) {//如果底部提示窗口可见
                    bottomTipWindow.dispose();//底部提示窗口置空
                }
                if (buttonHoverTipWindow != null && buttonHoverTipWindow.isVisible()) {//如果按钮提示窗口不为空
                    buttonHoverTipWindow.dispose();//释放按钮提示窗口
                }
            }
        });
    }

    public static void logOut() {//登出或注销
        Main.SettingState.userAccount = "";//清空
        Main.SettingState.userPhone = "";
        Main.SettingState.userPassword = "";
        Main.SettingState.userAvailableCloudCapacity = 0;
        userDialog.dispose();//释放
        User.initLogInDialog();//初始化登录菜单
        User.initRegisterDialog();//初始化注册菜单
        DirectoryTree.removeCloudNode();//删除云盘结点
        directoryManipulationButtonEnableJudgement();//状态判断
        fileManipulationButtonEnableJudgement(selectionThumbnailItemList.size());//状态判断
        historyManipulationButtonEnableJudgement();//状态判断
        handleUser();//重新登录
    }

    public static void initLogInDialog() {//初始化登录菜单
        JPanel passwordLogInPanel = new JPanel(new GridLayout(3, 1, 5, 5));//创建密码登录面板
        CustomUserTextField userAccountPasswordTextField = new CustomUserTextField(Main.SettingState.systemLanguage ? "Please Enter Username / Phone Number" : "请输入用户名/手机号", false, false, false);//用户账号密码登录文本域：可以用户名和手机号登录
        CustomUserTextField userPasswordTextField = new CustomUserTextField(Main.SettingState.systemLanguage ? "Please Enter Password" : "请输入密码", false, true, false);//用户密码登录文本域
        JButton passwordLogInButton = new JButton(Main.SettingState.systemLanguage ? "Login" : "登录");//密码登录按钮
        passwordLogInButton.setPreferredSize(new Dimension(Main.SettingState.systemLanguage ? 760 : 450, 35));//设置大小
        passwordLogInButton.setFont(new Font("微软雅黑", PLAIN, 21));//设置字体
        passwordLogInButton.setBackground(USER_LOG_BUTTON_COLOR);//背景颜色
        passwordLogInButton.setFocusable(false);//不可聚焦
        passwordLogInButton.setBorder(null);//无边框
        userAccountPasswordTextField.inputTextField.addKeyListener(new KeyAdapter() {//为用户账号密码登录文本域添加键盘监听
            @Override
            public void keyPressed(KeyEvent e) {//如果键盘按下
                if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_DOWN) {//如果按下回车或向下
                    if (userAccountPasswordTextField.inputTextField.getText().isEmpty()) {//如果输入为空
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "Username Or Phone Number Cannot Be Empty" : "用户名或手机号不可为空");//提示
                        userAccountPasswordTextField.isWrong = true;//错误
                    } else {//否则
                        userAccountPasswordTextField.isWrong = false;//正确
                        userPasswordTextField.passwordField.requestFocusInWindow();//焦点到下一行
                    }
                    userAccountPasswordTextField.revalidate();//重新验证
                    userAccountPasswordTextField.repaint();//重新绘制
                    e.consume();//阻止默认行为
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {//如果按下ESC
                    userAccountPasswordTextField.isWrong = false;//正确
                    userAccountPasswordTextField.revalidate();//重新验证
                    userAccountPasswordTextField.repaint();//重新绘制
                    logInDialog.requestFocusInWindow();//焦点返回登录菜单
                    e.consume();//阻止默认行为
                }
            }
        });
        userAccountPasswordTextField.inputTextField.addFocusListener(new FocusAdapter() {//为用户账号密码登录文本域添加聚焦监听
            @Override
            public void focusLost(FocusEvent e) {//如果失去聚焦
                if (logInDialog.isVisible() && passwordLogInPanel.getParent() != null) {//如果可见且密码登录面板有父组件（即有登录菜单）
                    if (userAccountPasswordTextField.inputTextField.getText().isEmpty()) {//如果输入为空
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "Username Or Phone Number Cannot Be Empty" : "用户名或手机号不可为空");//提示
                        userAccountPasswordTextField.isWrong = true;//错误
                    } else {//否则
                        userAccountPasswordTextField.isWrong = false;//正确
                    }
                    userAccountPasswordTextField.revalidate();//重新验证
                    userAccountPasswordTextField.repaint();//重新绘制
                }
            }
        });
        userAccountPasswordTextField.inputTextField.addMouseListener(new MouseAdapter() {//为用户账号密码登录文本域添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Please Enter Username Or Phone Number" : "请输入用户名或手机号", userAccountPasswordTextField.inputTextField));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
            }

            @Override
            public void mouseExited(MouseEvent e) {//如果鼠标离开
                if (hoverTimer != null) {//如果不为空
                    hoverTimer.stop();//计时器结束
                }
                if (buttonHoverTipWindow != null) {//如果提示信息不为空
                    buttonHoverTipWindow.dispose();//释放提示信息
                    buttonHoverTipWindow = null;//提示信息置空
                }
            }
        });
        userPasswordTextField.passwordField.addKeyListener(new KeyAdapter() {//为用户密码登录文本域添加键盘监听
            @Override
            public void keyPressed(KeyEvent e) {//如果键盘按下
                if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_UP) {//如果按下回车或向上
                    if (userPasswordTextField.passwordField.getText().isEmpty()) {//如果输入为空
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "Password Cannot Be Empty" : "密码不可为空");//提示
                        userPasswordTextField.isWrong = true;//错误
                    } else {//否则
                        userPasswordTextField.isWrong = false;//正确
                        if (e.getKeyCode() == KeyEvent.VK_ENTER) {//如果是回车
                            passwordLogInButton.doClick();//按下登录
                        } else {//否则
                            userAccountPasswordTextField.inputTextField.requestFocusInWindow();//焦点到上一行
                        }
                    }
                    userPasswordTextField.revalidate();//重新验证
                    userPasswordTextField.repaint();//重新绘制
                    e.consume();//阻止默认行为
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {//如果按下ESC
                    userPasswordTextField.isWrong = false;//正确
                    userPasswordTextField.revalidate();//重新验证
                    userPasswordTextField.repaint();//重新绘制
                    logInDialog.requestFocusInWindow();//焦点返回登录菜单
                    e.consume();//阻止默认行为
                }
            }
        });
        userPasswordTextField.passwordField.addFocusListener(new FocusAdapter() {//为用户密码登录文本域添加聚焦监听
            @Override
            public void focusLost(FocusEvent e) {//如果失去聚焦
                if (logInDialog.isVisible() && passwordLogInPanel.getParent() != null) {//如果可见且密码登录面板有父组件（即有登录菜单）
                    if (userPasswordTextField.passwordField.getText().isEmpty()) {//如果输入为空
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "Password Cannot Be Empty" : "密码不可为空");//提示
                        userPasswordTextField.isWrong = true;//错误
                    } else {//否则
                        userPasswordTextField.isWrong = false;//正确
                    }
                    userPasswordTextField.revalidate();//重新验证
                    userPasswordTextField.repaint();//重新绘制
                }
            }
        });
        userPasswordTextField.passwordField.addMouseListener(new MouseAdapter() {//为用户密码登录文本域添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Please Enter Password (Which Is 6-Digit At Least)" : "请输入密码（密码至少为6位）", userPasswordTextField.passwordField));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
            }

            @Override
            public void mouseExited(MouseEvent e) {//如果鼠标离开
                if (hoverTimer != null) {//如果不为空
                    hoverTimer.stop();//计时器结束
                }
                if (buttonHoverTipWindow != null) {//如果提示信息不为空
                    buttonHoverTipWindow.dispose();//释放提示信息
                    buttonHoverTipWindow = null;//提示信息置空
                }
            }
        });
        passwordLogInButton.addActionListener(_ -> {//为密码登录按钮添加事件监听
            userAccountPasswordTextField.isWrong = false;//正确
            userAccountPasswordTextField.revalidate();//重新验证
            userAccountPasswordTextField.repaint();//重新绘制
            userPasswordTextField.isWrong = false;//正确
            userPasswordTextField.revalidate();//重新验证
            userPasswordTextField.repaint();//重新绘制
            String userAccountInput = userAccountPasswordTextField.inputTextField.getText().trim();//获取用户名输入
            String userPasswordInput = userPasswordTextField.passwordField.getText().trim();//获取用户密码输入
            if (userAccountInput.isEmpty() && userPasswordInput.isEmpty()) {//如果用户名和密码都为空
                createBottomTipWindow(Main.SettingState.systemLanguage ? "Username Or Phone Number And Password Cannot Be Empty" : "用户名或手机号和密码不可为空");//提示
                userAccountPasswordTextField.isWrong = true;//错误
                userAccountPasswordTextField.revalidate();//重新验证
                userAccountPasswordTextField.repaint();//重新绘制
                userPasswordTextField.isWrong = true;//错误
                userPasswordTextField.revalidate();//重新验证
                userPasswordTextField.repaint();//重新绘制
                return;//直接返回
            } else if (userAccountInput.isEmpty()) {//如果用户名为空
                createBottomTipWindow(Main.SettingState.systemLanguage ? "Username Or Phone Number Cannot Be Empty" : "用户名或手机号不可为空");//提示
                userAccountPasswordTextField.isWrong = true;//错误
                userAccountPasswordTextField.revalidate();//重新验证
                userAccountPasswordTextField.repaint();//重新绘制
                return;//直接返回
            } else if (userPasswordInput.isEmpty()) {//如果验证码为空
                createBottomTipWindow(Main.SettingState.systemLanguage ? "Password Cannot Be Empty" : "密码不可为空");//提示
                userPasswordTextField.isWrong = true;//错误
                userPasswordTextField.revalidate();//重新验证
                userPasswordTextField.repaint();//重新绘制
                return;//直接返回
            }
            try {
                int code = handleUserValidateUserInformation(userAccountInput, userPasswordInput, false);//获取返回值
                if (code == 0) {//如果用户存在且用户名和密码映射正确
                    handleUserLoadUserAccount(userAccountInput, false);//根据用户用户名获取用户手机号
                    Main.SettingState.userAccount = userAccountInput;//验证成功，保存用户名
                    Main.SettingState.userPassword = userPasswordInput;//验证成功，保存用户密码
                    logIn();//登录
                } else if (code == 1) {//如果用户存在且用户名和密码映射正确且是通过手机号登录
                    handleUserLoadUserAccount(userAccountInput, true);//根据用户手机号获取用户用户名
                    Main.SettingState.userPhone = userAccountInput;//验证成功，保存手机号
                    logIn();//登录
                }
            } catch (IOException e) {
                handleErrorLog(e.getMessage());//处理错误日志
                throw new RuntimeException(e);//捕获异常
            }
        });
        passwordLogInButton.addMouseListener(new MouseAdapter() {//为密码登录按钮添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Login" : "登录", passwordLogInButton));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
            }

            @Override
            public void mouseExited(MouseEvent e) {//如果鼠标离开
                if (hoverTimer != null) {//如果不为空
                    hoverTimer.stop();//计时器结束
                }
                if (buttonHoverTipWindow != null) {//如果提示信息不为空
                    buttonHoverTipWindow.dispose();//释放提示信息
                    buttonHoverTipWindow = null;//提示信息置空
                }
            }
        });
        passwordLogInPanel.setBorder(BorderFactory.createEmptyBorder(7, 10, 7, 10));//创建边框
        passwordLogInPanel.setBackground(Main.SettingState.themeColor ? DARK_DIALOG_MAIN_COLOR : LIGHT_DIALOG_MAIN_COLOR);//设置背景颜色
        passwordLogInPanel.add(userAccountPasswordTextField);
        passwordLogInPanel.add(userPasswordTextField);
        passwordLogInPanel.add(passwordLogInButton);

        JPanel verificationLogInPanel = new JPanel(new GridLayout(3, 1, 5, 5));//创建验证码登录面板
        userAccountVerificationTextField = new CustomUserTextField(Main.SettingState.systemLanguage ? "Please Enter Phone Number" : "请输入手机号", false, false, false);//用户账号验证码登录文本域：只能手机号登录
        CustomUserTextField userVerificationTextField = new CustomUserTextField(Main.SettingState.systemLanguage ? "Please Enter Verification Code" : "请输入验证码", true, false, false);//用户验证码登录文本域
        JButton verificationLogInButton = new JButton(Main.SettingState.systemLanguage ? "Login" : "登录");//验证码登录按钮
        verificationLogInButton.setPreferredSize(new Dimension(Main.SettingState.systemLanguage ? 760 : 450, 35));//设置大小
        verificationLogInButton.setFont(new Font("微软雅黑", PLAIN, 21));//设置字体
        verificationLogInButton.setBackground(USER_LOG_BUTTON_COLOR);//背景颜色
        verificationLogInButton.setFocusable(false);//不可聚焦
        verificationLogInButton.setBorder(null);//无边框
        userAccountVerificationTextField.inputTextField.addKeyListener(new KeyAdapter() {//为用户账号验证码登录文本域添加键盘监听
            @Override
            public void keyPressed(KeyEvent e) {//如果键盘按下
                if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_DOWN) {//如果按下回车或向下
                    String userPhoneInput = userAccountVerificationTextField.inputTextField.getText();//获取输入文本
                    if (userPhoneInput.isEmpty()) {//如果输入为空
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "Phone Number Cannot Be Empty" : "手机号不可为空");//提示
                        userAccountVerificationTextField.isWrong = true;//错误
                    } else if (userPhoneInput.length() != 11) {//如果手机号不为11位
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "Please Enter 11-Digit Phone Number" : "请输入11位手机号");//提示
                        userAccountVerificationTextField.isWrong = true;//错误
                    } else {//否则
                        userPhone = userPhoneInput;//获取手机号
                        userAccountVerificationTextField.isWrong = false;//正确
                    }
                    userAccountVerificationTextField.revalidate();//重新验证
                    userAccountVerificationTextField.repaint();//重新绘制
                    e.consume();//阻止默认行为
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {//如果按下ESC
                    userAccountVerificationTextField.isWrong = false;//正确
                    userAccountVerificationTextField.revalidate();//重新验证
                    userAccountVerificationTextField.repaint();//重新绘制
                    logInDialog.requestFocusInWindow();//焦点返回登录菜单
                    e.consume();//阻止默认行为
                }
            }
        });
        userAccountVerificationTextField.inputTextField.addFocusListener(new FocusAdapter() {//为用户账号验证码登录文本域添加聚焦监听
            @Override
            public void focusLost(FocusEvent e) {//如果失去聚焦
                if (logInDialog.isVisible() && verificationLogInPanel.getParent() != null) {//如果可见且验证码登录面板有父组件（即有登录菜单）
                    String userPhoneInput = userAccountVerificationTextField.inputTextField.getText();//获取输入文本
                    if (userPhoneInput.isEmpty()) {//如果输入为空
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "Phone Number Cannot Be Empty" : "手机号不可为空");//提示
                        userAccountVerificationTextField.isWrong = true;//错误
                    } else if (userPhoneInput.length() != 11) {//如果手机号不为11位
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "Please Enter 11-Digit Phone Number" : "请输入11位手机号");//提示
                        userAccountVerificationTextField.isWrong = true;//错误
                    } else {//否则
                        userPhone = userPhoneInput;//获取手机号
                        userAccountVerificationTextField.isWrong = false;//正确
                    }
                    userAccountVerificationTextField.revalidate();//重新验证
                    userAccountVerificationTextField.repaint();//重新绘制
                }
            }
        });
        userAccountVerificationTextField.inputTextField.addMouseListener(new MouseAdapter() {//为用户账号验证码登录文本域添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Please Enter Phone Number (Which Is 11-Digit)" : "请输入手机号（手机号为11位）", userAccountVerificationTextField.inputTextField));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
            }

            @Override
            public void mouseExited(MouseEvent e) {//如果鼠标离开
                if (hoverTimer != null) {//如果不为空
                    hoverTimer.stop();//计时器结束
                }
                if (buttonHoverTipWindow != null) {//如果提示信息不为空
                    buttonHoverTipWindow.dispose();//释放提示信息
                    buttonHoverTipWindow = null;//提示信息置空
                }
            }
        });
        userAccountVerificationTextField.verificationButton.addMouseListener(new MouseAdapter() {//为用户账号验证码登录文本域验证码按钮添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Click To Send Verification Code (Which Is 6 Digit, If You Do Not Receive, Verification Code Expire, Or Incorrect Verification Code, Please Wait For 1 Minute Countdown To Resend)" : "点击发送验证码（验证码为6位，有效期为5分钟，如果没有收到验证码、验证码过期或验证码错误，请等待1分钟倒计时结束重新发送验证码）", userAccountVerificationTextField.inputTextField));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
            }

            @Override
            public void mouseExited(MouseEvent e) {//如果鼠标离开
                if (hoverTimer != null) {//如果不为空
                    hoverTimer.stop();//计时器结束
                }
                if (buttonHoverTipWindow != null) {//如果提示信息不为空
                    buttonHoverTipWindow.dispose();//释放提示信息
                    buttonHoverTipWindow = null;//提示信息置空
                }
            }
        });
        userVerificationTextField.inputTextField.addKeyListener(new KeyAdapter() {//为用户验证码登录文本域添加键盘监听
            @Override
            public void keyPressed(KeyEvent e) {//如果键盘按下
                if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_UP) {//如果按下回车或向上
                    if (userVerificationTextField.inputTextField.getText().isEmpty()) {//如果输入为空
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "Verification Code Cannot Be Empty" : "验证码不可为空");//提示
                        userVerificationTextField.isWrong = true;//错误
                    } else {//否则
                        userVerificationTextField.isWrong = false;//正确
                        if (e.getKeyCode() == KeyEvent.VK_ENTER) {//如果是回车
                            verificationLogInButton.doClick();//按下登录
                        } else {//否则
                            userAccountVerificationTextField.inputTextField.requestFocusInWindow();//焦点到上一行
                        }
                    }
                    userVerificationTextField.revalidate();//重新验证
                    userVerificationTextField.repaint();//重新绘制
                    e.consume();//阻止默认行为
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {//如果按下ESC
                    userVerificationTextField.isWrong = false;//正确
                    userVerificationTextField.revalidate();//重新验证
                    userVerificationTextField.repaint();//重新绘制
                    logInDialog.requestFocusInWindow();//焦点返回登录菜单
                    e.consume();//阻止默认行为
                }
            }
        });
        userVerificationTextField.inputTextField.addFocusListener(new FocusAdapter() {//为用户验证码登录文本域添加聚焦监听
            @Override
            public void focusLost(FocusEvent e) {//如果失去聚焦
                if (logInDialog.isVisible() && verificationLogInPanel.getParent() != null) {//如果可见且验证码登录面板有父组件（即有登录菜单）
                    if (userVerificationTextField.inputTextField.getText().isEmpty()) {//如果输入为空
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "Verification Code Cannot Be Empty" : "验证码不可为空");//提示
                        userVerificationTextField.isWrong = true;//错误
                    } else {//否则
                        userVerificationTextField.isWrong = false;//正确
                    }
                    userVerificationTextField.revalidate();//重新验证
                    userVerificationTextField.repaint();//重新绘制
                }
            }
        });
        userVerificationTextField.inputTextField.addMouseListener(new MouseAdapter() {//为用户验证码登录文本域添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Please Enter Verification Code (Which Is 6 Digit, If You Do Not Receive, Verification Code Expire, Or Incorrect Verification Code, Please Resend)" : "请输入验证码（验证码为6位，如果没有收到验证码、验证码过期或验证码错误，请重新发送验证码）", userVerificationTextField.inputTextField));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
            }

            @Override
            public void mouseExited(MouseEvent e) {//如果鼠标离开
                if (hoverTimer != null) {//如果不为空
                    hoverTimer.stop();//计时器结束
                }
                if (buttonHoverTipWindow != null) {//如果提示信息不为空
                    buttonHoverTipWindow.dispose();//释放提示信息
                    buttonHoverTipWindow = null;//提示信息置空
                }
            }
        });
        verificationLogInButton.addActionListener(_ -> {//为验证码登录按钮添加事件监听
            userAccountVerificationTextField.isWrong = false;//正确
            userAccountVerificationTextField.revalidate();//重新验证
            userAccountVerificationTextField.repaint();//重新绘制
            userVerificationTextField.isWrong = false;//正确
            userVerificationTextField.revalidate();//重新验证
            userVerificationTextField.repaint();//重新绘制
            String userPhoneNumberInput = userAccountVerificationTextField.inputTextField.getText().trim();//获取用户手机号输入
            String userVerificationCodeInput = userVerificationTextField.inputTextField.getText().trim();//获取用户验证码输入
            if (userPhoneNumberInput.isEmpty() && userVerificationCodeInput.isEmpty()) {//如果手机号和验证码都为空
                createBottomTipWindow(Main.SettingState.systemLanguage ? "Phone Number And Verification Code Cannot Be Empty" : "手机号和验证码不可为空");//提示
                userAccountVerificationTextField.isWrong = true;//错误
                userAccountVerificationTextField.revalidate();//重新验证
                userAccountVerificationTextField.repaint();//重新绘制
                userVerificationTextField.isWrong = true;//错误
                userVerificationTextField.revalidate();//重新验证
                userVerificationTextField.repaint();//重新绘制
                return;//直接返回
            } else if (userPhoneNumberInput.isEmpty()) {//如果手机号为空
                createBottomTipWindow(Main.SettingState.systemLanguage ? "Phone Number Cannot Be Empty" : "手机号不可为空");//提示
                userAccountVerificationTextField.isWrong = true;//错误
                userAccountVerificationTextField.revalidate();//重新验证
                userAccountVerificationTextField.repaint();//重新绘制
                return;//直接返回
            } else if (userVerificationCodeInput.isEmpty()) {//如果验证码为空
                createBottomTipWindow(Main.SettingState.systemLanguage ? "Verification Code Cannot Be Empty" : "验证码不可为空");//提示
                userVerificationTextField.isWrong = true;//错误
                userVerificationTextField.revalidate();//重新验证
                userVerificationTextField.repaint();//重新绘制
                return;//直接返回
            }
            if (userPhoneNumberInput.length() != 11) {//如果手机号不为11位
                createBottomTipWindow(Main.SettingState.systemLanguage ? "Please Enter 11-Digit Phone Number" : "请输入11位手机号");//提示
                userAccountVerificationTextField.isWrong = true;//错误
                userAccountVerificationTextField.revalidate();//重新验证
                userAccountVerificationTextField.repaint();//重新绘制
                return;//直接返回
            }
            userPhone = userPhoneNumberInput;//获取手机号
            try {
                if (handleUserVerifyVerificationCode(userVerificationCodeInput) != 0) {//如果验证验证码失败
                    return;//直接返回
                }
            } catch (IOException e) {
                handleErrorLog(e.getMessage());//处理错误日志
                throw new RuntimeException(e);//捕获异常
            }
            try {
                if (handleUserValidateUserInformation(userPhone, null, true) == 0) {//如果用户存在
                    handleUserLoadUserAccount(userPhone, true);//根据手机号获取用户名和密码
                    Main.SettingState.userPhone = userPhone;//验证成功，保存用户手机号
                    logIn();//登录
                }
            } catch (IOException e) {
                handleErrorLog(e.getMessage());//处理错误日志
                throw new RuntimeException(e);//捕获异常
            }
        });
        verificationLogInButton.addMouseListener(new MouseAdapter() {//为验证码登录按钮添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Login" : "登录", verificationLogInButton));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
            }

            @Override
            public void mouseExited(MouseEvent e) {//如果鼠标离开
                if (hoverTimer != null) {//如果不为空
                    hoverTimer.stop();//计时器结束
                }
                if (buttonHoverTipWindow != null) {//如果提示信息不为空
                    buttonHoverTipWindow.dispose();//释放提示信息
                    buttonHoverTipWindow = null;//提示信息置空
                }
            }
        });
        verificationLogInPanel.setBorder(BorderFactory.createEmptyBorder(7, 10, 7, 10));//创建边框
        verificationLogInPanel.setBackground(Main.SettingState.themeColor ? DARK_DIALOG_MAIN_COLOR : LIGHT_DIALOG_MAIN_COLOR);//设置背景颜色
        verificationLogInPanel.add(userAccountVerificationTextField);
        verificationLogInPanel.add(userVerificationTextField);
        verificationLogInPanel.add(verificationLogInButton);

        JLabel logInTipLabel = new JLabel(Main.SettingState.systemLanguage ? "You Haven't Login Yet, You Can Enjoy Setting Cloud Synchronization And Picture Cloud Disk After Login" : "您还没有登录，登录后即可享受设置云同步和图片云盘功能");//登录提示标签
        logInTipLabel.setFont(new Font("楷体", PLAIN, 17));//设置字体
        logInTipLabel.setForeground(Main.SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//设置字体颜色
        JPanel logInTipPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 3));//登录提示面板：设置布局管理器为中心流式布局
        logInTipPanel.setBackground(Main.SettingState.themeColor ? DARK_DIALOG_MAIN_COLOR : LIGHT_DIALOG_MAIN_COLOR);//设置背景颜色
        logInTipPanel.add(logInTipLabel);

        JButton passwordLogInSwitchButton = new JButton(Main.SettingState.systemLanguage ? "Login By Password" : "密码登录");//密码登录切换按钮
        JButton verificationLogInSwitchButton = new JButton(Main.SettingState.systemLanguage ? "Login By Verification Code" : "验证码登录");//验证码登录切换按钮
        passwordLogInSwitchButton.setPreferredSize(new Dimension(Main.SettingState.systemLanguage ? 380 : 225, 35));//设置大小
        verificationLogInSwitchButton.setPreferredSize(new Dimension(Main.SettingState.systemLanguage ? 380 : 225, 35));//设置大小
        passwordLogInSwitchButton.setFont(new Font("微软雅黑", PLAIN, 20));//设置字体
        verificationLogInSwitchButton.setFont(new Font("微软雅黑", PLAIN, 20));//设置字体
        passwordLogInSwitchButton.setForeground(Main.SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//设置字体颜色
        verificationLogInSwitchButton.setForeground(Main.SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//设置字体颜色
        passwordLogInSwitchButton.setBackground(Main.SettingState.themeColor ? DARK_LOG_IN_ACTIVATE_COLOR : LIGHT_LOG_IN_ACTIVATE_COLOR);//背景颜色
        verificationLogInSwitchButton.setBackground(Main.SettingState.themeColor ? DARK_LOG_IN_DEACTIVATE_COLOR : LIGHT_LOG_IN_DEACTIVATE_COLOR);//背景颜色
        passwordLogInSwitchButton.setFocusable(false);//不可聚焦
        verificationLogInSwitchButton.setFocusable(false);//不可聚焦
        passwordLogInSwitchButton.setBorder(null);//无边框
        verificationLogInSwitchButton.setBorder(null);//无边框
        passwordLogInSwitchButton.addMouseListener(new MouseAdapter() {//为密码登录切换按钮添加鼠标监听
            @Override
            public void mouseClicked(MouseEvent e) {//如果鼠标点击
                if (!userAccountPasswordTextField.isShowing()) {//如果不展示
                    userAccountPasswordTextField.isWrong = false;//正确
                    userAccountPasswordTextField.revalidate();//重新验证
                    userAccountPasswordTextField.repaint();//重新绘制
                    userPasswordTextField.isWrong = false;//正确
                    userPasswordTextField.revalidate();//重新验证
                    userPasswordTextField.repaint();//重新绘制
                    passwordLogInSwitchButton.setBackground(Main.SettingState.themeColor ? DARK_LOG_IN_ACTIVATE_COLOR : LIGHT_LOG_IN_ACTIVATE_COLOR);//背景颜色
                    verificationLogInSwitchButton.setBackground(Main.SettingState.themeColor ? DARK_LOG_IN_DEACTIVATE_COLOR : LIGHT_LOG_IN_DEACTIVATE_COLOR);//背景颜色
                    logInDialog.remove(verificationLogInPanel);//移除
                    logInDialog.add(passwordLogInPanel, BorderLayout.CENTER);//把密码登录面板添加到中心
                    logInDialog.revalidate();//重新验证
                    logInDialog.repaint();//重新绘制
                    userAccountPasswordTextField.inputTextField.requestFocusInWindow();//聚焦
                }
            }
        });
        verificationLogInSwitchButton.addMouseListener(new MouseAdapter() {//为验证码登录切换按钮添加鼠标监听
            @Override
            public void mouseClicked(MouseEvent e) {//如果鼠标点击
                if (!userAccountVerificationTextField.isShowing()) {//如果不展示
                    userAccountVerificationTextField.isWrong = false;//正确
                    userAccountVerificationTextField.revalidate();//重新验证
                    userAccountVerificationTextField.repaint();//重新绘制
                    userVerificationTextField.isWrong = false;//正确
                    userVerificationTextField.revalidate();//重新验证
                    userVerificationTextField.repaint();//重新绘制
                    passwordLogInSwitchButton.setBackground(Main.SettingState.themeColor ? DARK_LOG_IN_DEACTIVATE_COLOR : LIGHT_LOG_IN_DEACTIVATE_COLOR);//背景颜色
                    verificationLogInSwitchButton.setBackground(Main.SettingState.themeColor ? DARK_LOG_IN_ACTIVATE_COLOR : LIGHT_LOG_IN_ACTIVATE_COLOR);//背景颜色
                    logInDialog.remove(passwordLogInPanel);//移除
                    logInDialog.add(verificationLogInPanel, BorderLayout.CENTER);//把验证码登录面板添加到中心
                    logInDialog.revalidate();//重新验证
                    logInDialog.repaint();//重新绘制
                    userAccountVerificationTextField.inputTextField.requestFocusInWindow();//聚焦
                }
            }
        });
        JPanel logInSwitchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));//登录切换面板：设置布局管理器为中心流式布局
        logInSwitchPanel.setBackground(Main.SettingState.themeColor ? DARK_DIALOG_MAIN_COLOR : LIGHT_DIALOG_MAIN_COLOR);//设置背景颜色
        logInSwitchPanel.add(passwordLogInSwitchButton);
        logInSwitchPanel.add(verificationLogInSwitchButton);
        logInSwitchPanel.addComponentListener(new ComponentAdapter() {//为登录切换面板添加组件监听
            @Override
            public void componentResized(ComponentEvent e) {//如果大小变化
                passwordLogInSwitchButton.setPreferredSize(new Dimension(logInSwitchPanel.getWidth() / 2, 35));//重新设置按钮大小
                verificationLogInSwitchButton.setPreferredSize(new Dimension(logInSwitchPanel.getWidth() / 2, 35));//重新设置按钮大小
            }
        });
        JPanel topPanel = new JPanel(new BorderLayout());//顶部面板
        topPanel.add(logInTipPanel, BorderLayout.NORTH);//把登录提示面板添加到北部
        topPanel.add(logInSwitchPanel, BorderLayout.CENTER);//把登录切换面板添加到中心

        JLabel registerTipLabel = new JLabel(Main.SettingState.systemLanguage ? "Register Now" : "立即注册");//注册提示标签
        registerTipLabel.setFont(new Font("微软雅黑", PLAIN, 15));//设置字体
        registerTipLabel.setForeground(Main.SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//设置字体颜色
        registerTipLabel.addMouseListener(new MouseAdapter() {//为注册提醒标签添加鼠标监听
            @Override
            public void mouseClicked(MouseEvent e) {//如果鼠标点击
                userAccountPasswordTextField.isWrong = false;//正确
                userAccountPasswordTextField.revalidate();//重新验证
                userAccountPasswordTextField.repaint();//重新绘制
                userPasswordTextField.isWrong = false;//正确
                userPasswordTextField.revalidate();//重新验证
                userPasswordTextField.repaint();//重新绘制
                userAccountVerificationTextField.isWrong = false;//正确
                userAccountVerificationTextField.revalidate();//重新验证
                userAccountVerificationTextField.repaint();//重新绘制
                userVerificationTextField.isWrong = false;//正确
                userVerificationTextField.revalidate();//重新验证
                userVerificationTextField.repaint();//重新绘制
                registerTipLabel.setForeground(Main.SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//恢复颜色
                logInDialog.setVisible(false);//登录不可见
                registerDialog.setVisible(true);//注册可见
            }

            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                registerTipLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));//设置鼠标为手指，提升交互体验
                registerTipLabel.setForeground(Color.RED);//悬浮颜色
            }

            @Override
            public void mouseExited(MouseEvent e) {//如果鼠标离开
                registerTipLabel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));//设置提示信息鼠标为默认光标（不会变成字体编辑光标）
                registerTipLabel.setForeground(Main.SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//恢复颜色
            }
        });
        JPanel registerTipPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 3));//注册提示面板：设置布局管理器为右侧流式布局
        registerTipPanel.setBackground(Main.SettingState.themeColor ? DARK_DIALOG_MAIN_COLOR : LIGHT_DIALOG_MAIN_COLOR);//设置背景颜色
        registerTipPanel.add(registerTipLabel);

        logInDialog = new JDialog(Main.diskManagementSystemFrame, Main.SettingState.systemLanguage ? "Log In" : "登录", true);//创建登录对话窗口
        logInDialog.setIconImage(new ImageIcon("src/material/image/dialogRegister.png").getImage());//设置图标
        logInDialog.setLayout(new BorderLayout());//设置布局
        logInDialog.add(topPanel, BorderLayout.NORTH);//把顶部面板添加到北部
        logInDialog.add(passwordLogInPanel, BorderLayout.CENTER);//把密码登录面板添加到中心
        logInDialog.add(registerTipPanel, BorderLayout.SOUTH);//把注册提示标签添加到南部
        logInDialog.setAlwaysOnTop(Main.SettingState.windowState);//设置永远在最上层
        logInDialog.pack();//设置合适
        logInDialog.setVisible(false);//设置不可见
        logInDialog.setLocation(Main.screenSize.width / 2 - logInDialog.getWidth() / 2, Main.screenSize.height / 2 - logInDialog.getHeight() / 2);//设置位置
        JRootPane logInDialogRoot = logInDialog.getRootPane();//获取登录窗口的根
        logInDialogRoot.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "closeLogInDialog");//为根设置窗口关闭ESC按键绑定
        logInDialogRoot.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_U, KeyEvent.CTRL_DOWN_MASK), "closeLogInDialog");//为根设置窗口关闭Ctrl+U按键绑定
        logInDialogRoot.getActionMap().put("closeLogInDialog", new AbstractAction() {//当ESC按键执行时
            public void actionPerformed(ActionEvent event) {//行为执行
                logInDialog.dispatchEvent(new WindowEvent(logInDialog, WindowEvent.WINDOW_CLOSING));//关闭窗口
            }
        });
        logInDialog.addWindowListener(new WindowAdapter() {//为登录窗口添加窗口监听
            @Override
            public void windowClosing(WindowEvent e) {//如果窗口正在关闭
                if (bottomTipWindow != null && bottomTipWindow.isVisible()) {//如果底部提示窗口可见
                    bottomTipWindow.dispose();//底部提示窗口置空
                }
                if (buttonHoverTipWindow != null && buttonHoverTipWindow.isVisible()) {//如果按钮提示窗口不为空
                    buttonHoverTipWindow.dispose();//释放按钮提示窗口
                }
            }
        });
    }

    public static void logIn() {//登录
        Setting.switchSystemLanguage();//切换系统语言
        switchThemeColor();//切换主题颜色
        DirectoryTree.createCloudNode();//创建云盘结点
        directoryManipulationButtonEnableJudgement();//状态判断
        fileManipulationButtonEnableJudgement(selectionThumbnailItemList.size());//状态判断
        historyManipulationButtonEnableJudgement();//状态判断
        logInDialog.dispose();//关闭登录菜单
        userDialog.setVisible(true);//打开用户菜单
    }

    public static void initRegisterDialog() {//初始化注册菜单
        JLabel registerTipLabel = new JLabel(Main.SettingState.systemLanguage ? "You Haven't Register Yet, You Can Enjoy Setting Cloud Synchronization And Picture Cloud Disk After Register" : "您还没有注册，注册后即可享受设置云同步和图片云盘功能");//注册提示标签
        registerTipLabel.setFont(new Font("楷体", PLAIN, 17));//设置字体
        registerTipLabel.setForeground(Main.SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//设置字体颜色
        JPanel registerTipPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 3));//注册提示面板：设置布局管理器为中心流式布局
        registerTipPanel.setBackground(Main.SettingState.themeColor ? DARK_DIALOG_MAIN_COLOR : LIGHT_DIALOG_MAIN_COLOR);//设置背景颜色
        registerTipPanel.add(registerTipLabel);

        CustomUserTextField userAccountTextField = new CustomUserTextField(Main.SettingState.systemLanguage ? "Please Enter Username (Which Is 24-Digit At Most)" : "请输入用户名（至多为24位）", false, false, false);//用户账号注册文本域
        userPhoneTextField = new CustomUserTextField(Main.SettingState.systemLanguage ? "Please Enter Phone Number" : "请输入手机号", false, false, false);//用户手机号注册文本域
        CustomUserTextField userVerificationTextField = new CustomUserTextField(Main.SettingState.systemLanguage ? "Please Enter Verification Code" : "请输入验证码", true, false, false);//用户验证码注册文本域
        CustomUserTextField userPasswordTextField = new CustomUserTextField(Main.SettingState.systemLanguage ? "Please Enter Password (Which Is 6-Digit At Least)" : "请输入密码（密码至少为6位）", false, true, false);//用户密码注册文本域
        CustomUserTextField userConfirmPasswordTextField = new CustomUserTextField(Main.SettingState.systemLanguage ? "Please Confirm Password" : "请确认密码", false, true, false);//用户确认密码注册文本域
        JButton registerButton = new JButton(Main.SettingState.systemLanguage ? "Register" : "注册");//注册按钮
        registerButton.setPreferredSize(new Dimension(Main.SettingState.systemLanguage ? 760 : 450, 35));//设置大小
        registerButton.setFont(new Font("微软雅黑", PLAIN, 21));//设置字体
        registerButton.setBackground(USER_LOG_BUTTON_COLOR);//背景颜色
        registerButton.setFocusable(false);//不可聚焦
        registerButton.setBorder(null);//无边框
        userAccountTextField.inputTextField.addKeyListener(new KeyAdapter() {//为用户账号注册文本域添加键盘监听
            @Override
            public void keyPressed(KeyEvent e) {//如果键盘按下
                if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_DOWN) {//如果按下回车或向下
                    String userAccountInput = userAccountTextField.inputTextField.getText();//获取输入文本
                    if (userAccountInput.isEmpty()) {//如果输入为空
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "Username Cannot Be Empty" : "用户名不可为空");//提示
                        userAccountTextField.isWrong = true;//错误
                    } else {//否则
                        for (int i = 0; i < userAccountInput.length(); i++) {//遍历输入文本
                            char c = userAccountInput.charAt(i);//获取当前字符
                            if (c == '/' || c == '\\' || c == '"' || c == '{' || c == '}' || c == ':') {//如果是特殊字符
                                createBottomTipWindow(Main.SettingState.systemLanguage ? "No Special Character" : "请勿输入特殊字符");//提示
                                userAccountTextField.isWrong = true;//错误
                                userAccountTextField.revalidate();//重新验证
                                userAccountTextField.repaint();//重新绘制
                                return;//直接返回
                            }
                        }
                        try {
                            if (handleUserValidateUserUnique(userAccountTextField.inputTextField.getText(), false)) {//如果用户名重复
                                createBottomTipWindow(Main.SettingState.systemLanguage ? "Duplicated Username" : "用户名重复");//提示
                                userAccountTextField.isWrong = true;//错误
                            } else {//否则
                                userAccountTextField.isWrong = false;//正确
                                userPhoneTextField.inputTextField.requestFocusInWindow();//焦点到下一行
                            }
                        } catch (IOException ex) {
                            handleErrorLog(ex.getMessage());//处理错误日志
                            throw new RuntimeException(ex);//捕获异常
                        }
                    }
                    userAccountTextField.revalidate();//重新验证
                    userAccountTextField.repaint();//重新绘制
                    e.consume();//阻止默认行为
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {//如果按下ESC
                    userAccountTextField.isWrong = false;//正确
                    userAccountTextField.revalidate();//重新验证
                    userAccountTextField.repaint();//重新绘制
                    registerDialog.requestFocusInWindow();//焦点返回登录菜单
                    e.consume();//阻止默认行为
                }
            }
        });
        userAccountTextField.inputTextField.addFocusListener(new FocusAdapter() {//为用户账号注册文本域添加聚焦监听
            @Override
            public void focusLost(FocusEvent e) {//如果失去聚焦
                if (registerDialog.isVisible()) {//如果可见
                    String userAccountInput = userAccountTextField.inputTextField.getText();//获取输入文本
                    if (userAccountInput.isEmpty()) {//如果输入为空
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "Username Cannot Be Empty" : "用户名不可为空");//提示
                        userAccountTextField.isWrong = true;//错误
                    } else {//否则
                        for (int i = 0; i < userAccountInput.length(); i++) {//遍历输入文本
                            char c = userAccountInput.charAt(i);//获取当前字符
                            if (c == '/' || c == '\\' || c == '"' || c == '{' || c == '}' || c == ':') {//如果是特殊字符
                                createBottomTipWindow(Main.SettingState.systemLanguage ? "No Special Character" : "请勿输入特殊字符");//提示
                                userAccountTextField.isWrong = true;//错误
                                userAccountTextField.revalidate();//重新验证
                                userAccountTextField.repaint();//重新绘制
                                return;//直接返回
                            }
                        }
                        try {
                            if (handleUserValidateUserUnique(userAccountTextField.inputTextField.getText(), false)) {//如果用户名重复
                                createBottomTipWindow(Main.SettingState.systemLanguage ? "Duplicated Username" : "用户名重复");//提示
                                userAccountTextField.isWrong = true;//错误
                            } else {//否则
                                userAccountTextField.isWrong = false;//正确
                            }
                        } catch (IOException ex) {
                            handleErrorLog(ex.getMessage());//处理错误日志
                            throw new RuntimeException(ex);//捕获异常
                        }
                    }
                    userAccountTextField.revalidate();//重新验证
                    userAccountTextField.repaint();//重新绘制
                }
            }
        });
        userAccountTextField.inputTextField.addMouseListener(new MouseAdapter() {//为用户账号注册文本域添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Please Enter Username (Which Is 24-Digit At Most, Cannot Be Duplicated, And Cannot Contain Special Character '/' '\\' '\"' '{' '}' ':')" : "请输入用户名（用户名至多为24位、用户名不可重复且不可包含特殊字符 '/' '\\' '\"' '{' '}' ':'）", userAccountTextField.inputTextField));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
            }

            @Override
            public void mouseExited(MouseEvent e) {//如果鼠标离开
                if (hoverTimer != null) {//如果不为空
                    hoverTimer.stop();//计时器结束
                }
                if (buttonHoverTipWindow != null) {//如果提示信息不为空
                    buttonHoverTipWindow.dispose();//释放提示信息
                    buttonHoverTipWindow = null;//提示信息置空
                }
            }
        });
        userPhoneTextField.inputTextField.addKeyListener(new KeyAdapter() {//为用户手机号注册文本域添加键盘监听
            @Override
            public void keyPressed(KeyEvent e) {//如果键盘按下
                if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN) {//如果按下回车或向上或向下
                    String userPhoneInput = userPhoneTextField.inputTextField.getText();//获取输入文本
                    if (userPhoneInput.isEmpty()) {//如果输入为空
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "Phone Number Cannot Be Empty" : "手机号不可为空");//提示
                        userPhoneTextField.isWrong = true;//错误
                    } else if (userPhoneInput.length() != 11) {//如果手机号不为11位
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "Please Enter 11-Digit Phone Number" : "请输入11位手机号");//提示
                        userPhoneTextField.isWrong = true;//错误
                    } else {//否则
                        for (int i = 0; i < userPhoneInput.length(); i++) {//遍历输入文本
                            if (!Character.isDigit(userPhoneInput.charAt(i))) {//如果非数字字符
                                createBottomTipWindow(Main.SettingState.systemLanguage ? "No Non Digit Character" : "请勿输入非数字字符");//提示
                                userPhoneTextField.isWrong = true;//错误
                                userPhoneTextField.revalidate();//重新验证
                                userPhoneTextField.repaint();//重新绘制
                                return;//直接返回
                            }
                        }
                        try {
                            if (handleUserValidateUserUnique(userPhoneInput, true)) {//如果手机号已被使用
                                createBottomTipWindow(Main.SettingState.systemLanguage ? "Phone Number Has Been Used" : "手机号已被使用");//提示
                                userPhoneTextField.isWrong = true;//错误
                            } else {//否则
                                userPhone = userPhoneInput;//获取手机号
                                userPhoneTextField.isWrong = false;//正确
                                if (e.getKeyCode() == KeyEvent.VK_UP) {//如果是向上
                                    userAccountTextField.inputTextField.requestFocusInWindow();//焦点到上一行
                                } else {//否则
                                    userVerificationTextField.inputTextField.requestFocusInWindow();//焦点到下一行
                                }
                            }
                        } catch (IOException ex) {
                            handleErrorLog(ex.getMessage());//处理错误日志
                            throw new RuntimeException(ex);//捕获异常
                        }
                    }
                    userPhoneTextField.revalidate();//重新验证
                    userPhoneTextField.repaint();//重新绘制
                    e.consume();//阻止默认行为
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {//如果按下ESC
                    userPhoneTextField.isWrong = false;//正确
                    userPhoneTextField.revalidate();//重新验证
                    userPhoneTextField.repaint();//重新绘制
                    registerDialog.requestFocusInWindow();//焦点返回登录菜单
                    e.consume();//阻止默认行为
                }
            }
        });
        userPhoneTextField.inputTextField.addFocusListener(new FocusAdapter() {//为用户手机号注册文本域添加聚焦监听
            @Override
            public void focusLost(FocusEvent e) {//如果失去聚焦
                if (registerDialog.isVisible()) {//如果可见
                    String userPhoneInput = userPhoneTextField.inputTextField.getText();//获取输入文本
                    if (userPhoneInput.isEmpty()) {//如果输入为空
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "Phone Number Cannot Be Empty" : "手机号不可为空");//提示
                        userPhoneTextField.isWrong = true;//错误
                    } else if (userPhoneInput.length() != 11) {//如果手机号不为11位
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "Please Enter 11-Digit Phone Number" : "请输入11位手机号");//提示
                        userPhoneTextField.isWrong = true;//错误
                    } else {//否则
                        for (int i = 0; i < userPhoneInput.length(); i++) {//遍历输入文本
                            if (!Character.isDigit(userPhoneInput.charAt(i))) {//如果非数字字符
                                createBottomTipWindow(Main.SettingState.systemLanguage ? "No Non Digit Character" : "请勿输入非数字字符");//提示
                                userPhoneTextField.isWrong = true;//错误
                                userPhoneTextField.revalidate();//重新验证
                                userPhoneTextField.repaint();//重新绘制
                                return;//直接返回
                            }
                        }
                        try {
                            if (handleUserValidateUserUnique(userPhoneInput, true)) {//如果手机号已被使用
                                createBottomTipWindow(Main.SettingState.systemLanguage ? "Phone Number Has Been Used" : "手机号已被使用");//提示
                                userPhoneTextField.isWrong = true;//错误
                            } else {//否则
                                userPhone = userPhoneInput;//获取手机号
                                userPhoneTextField.isWrong = false;//正确
                            }
                        } catch (IOException ex) {
                            handleErrorLog(ex.getMessage());//处理错误日志
                            throw new RuntimeException(ex);//捕获异常
                        }
                    }
                    userPhoneTextField.revalidate();//重新验证
                    userPhoneTextField.repaint();//重新绘制
                }
            }
        });
        userPhoneTextField.inputTextField.addMouseListener(new MouseAdapter() {//为用户手机号注册文本域添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Please Enter Phone Number (Which Is 11-Digit)" : "请输入手机号（手机号为11位）", userPhoneTextField.inputTextField));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
            }

            @Override
            public void mouseExited(MouseEvent e) {//如果鼠标离开
                if (hoverTimer != null) {//如果不为空
                    hoverTimer.stop();//计时器结束
                }
                if (buttonHoverTipWindow != null) {//如果提示信息不为空
                    buttonHoverTipWindow.dispose();//释放提示信息
                    buttonHoverTipWindow = null;//提示信息置空
                }
            }
        });
        userPhoneTextField.verificationButton.addMouseListener(new MouseAdapter() {//为用户手机号注册文本域验证码按钮添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Click To Send Verification Code (Which Is 6 Digit, If You Do Not Receive, Verification Code Expire, Or Incorrect Verification Code, Please Wait For 1 Minute Countdown To Resend)" : "点击发送验证码（验证码为6位，有效期为5分钟，如果没有收到验证码、验证码过期或验证码错误，请等待1分钟倒计时结束重新发送验证码）", userPhoneTextField.inputTextField));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
            }

            @Override
            public void mouseExited(MouseEvent e) {//如果鼠标离开
                if (hoverTimer != null) {//如果不为空
                    hoverTimer.stop();//计时器结束
                }
                if (buttonHoverTipWindow != null) {//如果提示信息不为空
                    buttonHoverTipWindow.dispose();//释放提示信息
                    buttonHoverTipWindow = null;//提示信息置空
                }
            }
        });
        userVerificationTextField.inputTextField.addKeyListener(new KeyAdapter() {//为用户验证码注册文本域添加键盘监听
            @Override
            public void keyPressed(KeyEvent e) {//如果键盘按下
                if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN) {//如果按下回车或向上或向下
                    if (userVerificationTextField.inputTextField.getText().isEmpty()) {//如果输入为空
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "Verification Code Cannot Be Empty" : "验证码不可为空");//提示
                        userVerificationTextField.isWrong = true;//错误
                    } else {//否则
                        userVerificationTextField.isWrong = false;//正确
                        if (e.getKeyCode() == KeyEvent.VK_UP) {//如果是向上
                            userPhoneTextField.inputTextField.requestFocusInWindow();//焦点到上一行
                        } else {//否则
                            userPasswordTextField.passwordField.requestFocusInWindow();//焦点到下一行
                        }
                    }
                    userVerificationTextField.revalidate();//重新验证
                    userVerificationTextField.repaint();//重新绘制
                    e.consume();//阻止默认行为
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {//如果按下ESC
                    userVerificationTextField.isWrong = false;//正确
                    userVerificationTextField.revalidate();//重新验证
                    userVerificationTextField.repaint();//重新绘制
                    registerDialog.requestFocusInWindow();//焦点返回登录菜单
                    e.consume();//阻止默认行为
                }
            }
        });
        userVerificationTextField.inputTextField.addFocusListener(new FocusAdapter() {//为用户验证码注册文本域添加聚焦监听
            @Override
            public void focusLost(FocusEvent e) {//如果失去聚焦
                if (registerDialog.isVisible()) {//如果可见
                    if (userVerificationTextField.inputTextField.getText().isEmpty()) {//如果输入为空
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "Verification Code Cannot Be Empty" : "验证码不可为空");//提示
                        userVerificationTextField.isWrong = true;//错误
                    } else {//否则
                        userVerificationTextField.isWrong = false;//正确
                    }
                    userVerificationTextField.revalidate();//重新验证
                    userVerificationTextField.repaint();//重新绘制
                }
            }
        });
        userVerificationTextField.inputTextField.addMouseListener(new MouseAdapter() {//为用户验证码注册文本域添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Please Enter Verification Code (Which Is 6 Digit, If You Do Not Receive, Verification Code Expire, Or Incorrect Verification Code, Please Resend)" : "请输入验证码（验证码为6位，如果没有收到验证码、验证码过期或验证码错误，请重新发送验证码）", userVerificationTextField.inputTextField));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
            }

            @Override
            public void mouseExited(MouseEvent e) {//如果鼠标离开
                if (hoverTimer != null) {//如果不为空
                    hoverTimer.stop();//计时器结束
                }
                if (buttonHoverTipWindow != null) {//如果提示信息不为空
                    buttonHoverTipWindow.dispose();//释放提示信息
                    buttonHoverTipWindow = null;//提示信息置空
                }
            }
        });
        userPasswordTextField.passwordField.addKeyListener(new KeyAdapter() {//为用户密码注册文本域添加键盘监听
            @Override
            public void keyPressed(KeyEvent e) {//如果键盘按下
                if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN) {//如果按下回车或向上或向下
                    String userPasswordInput = userPasswordTextField.passwordField.getText();//获取输入文本
                    if (userPasswordInput.isEmpty()) {//如果输入为空
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "Password Cannot Be Empty" : "密码不可为空");//提示
                        userPasswordTextField.isWrong = true;//错误
                    } else if (userPasswordInput.length() < 6) {//如果密码长度小于6位
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "Password Is At Least 6-Digit" : "密码至少为6位");//提示
                        userPasswordTextField.isWrong = true;//错误
                    } else {//否则
                        for (int i = 0; i < userPasswordInput.length(); i++) {//遍历输入文本
                            char c = userPasswordInput.charAt(i);//获取当前字符
                            if (c == '/' || c == '\\' || c == '"' || c == '{' || c == '}' || c == ':') {//如果是特殊字符
                                createBottomTipWindow(Main.SettingState.systemLanguage ? "No Special Character" : "请勿输入特殊字符");//提示
                                userPasswordTextField.isWrong = true;//错误
                                userPasswordTextField.revalidate();//重新验证
                                userPasswordTextField.repaint();//重新绘制
                                return;//直接返回
                            }
                        }
                        userPasswordTextField.isWrong = false;//正确
                        if (e.getKeyCode() == KeyEvent.VK_UP) {//如果是向上
                            userVerificationTextField.inputTextField.requestFocusInWindow();//焦点到上一行
                        } else {//否则
                            userConfirmPasswordTextField.passwordField.requestFocusInWindow();//焦点到下一行
                        }
                    }
                    userPasswordTextField.revalidate();//重新验证
                    userPasswordTextField.repaint();//重新绘制
                    e.consume();//阻止默认行为
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {//如果按下ESC
                    userPasswordTextField.isWrong = false;//正确
                    userPasswordTextField.revalidate();//重新验证
                    userPasswordTextField.repaint();//重新绘制
                    registerDialog.requestFocusInWindow();//焦点返回登录菜单
                    e.consume();//阻止默认行为
                }
            }
        });
        userPasswordTextField.passwordField.addFocusListener(new FocusAdapter() {//为用户密码注册文本域添加聚焦监听
            @Override
            public void focusLost(FocusEvent e) {//如果失去聚焦
                if (registerDialog.isVisible()) {//如果可见
                    String userPasswordInput = userPasswordTextField.passwordField.getText();//获取输入文本
                    if (userPasswordInput.isEmpty()) {//如果输入为空
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "Password Cannot Be Empty" : "密码不可为空");//提示
                        userPasswordTextField.isWrong = true;//错误
                    } else if (userPasswordInput.length() < 6) {//如果密码长度小于6位
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "Password Is At Least 6-Digit" : "密码至少为6位");//提示
                        userPasswordTextField.isWrong = true;//错误
                    } else {//否则
                        for (int i = 0; i < userPasswordInput.length(); i++) {//遍历输入文本
                            char c = userPasswordInput.charAt(i);//获取当前字符
                            if (c == '/' || c == '\\' || c == '"' || c == '{' || c == '}' || c == ':') {//如果是特殊字符
                                createBottomTipWindow(Main.SettingState.systemLanguage ? "No Special Character" : "请勿输入特殊字符");//提示
                                userPasswordTextField.isWrong = true;//错误
                                userPasswordTextField.revalidate();//重新验证
                                userPasswordTextField.repaint();//重新绘制
                                return;//直接返回
                            }
                        }
                        userPasswordTextField.isWrong = false;//正确
                    }
                    userPasswordTextField.revalidate();//重新验证
                    userPasswordTextField.repaint();//重新绘制
                }
            }
        });
        userPasswordTextField.passwordField.addMouseListener(new MouseAdapter() {//为用户密码注册文本域添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Please Enter Password (Please Note That Logout Cannot Be Reversed, All Your Cloud Storage Data Will Lost, Please Be Cautious)" : "请输入密码（为保证密码强度，密码至少为6位且密码不可包含特殊字符 '/' '\\' '\"' '{' '}' ':'）", userPasswordTextField.passwordField));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
            }

            @Override
            public void mouseExited(MouseEvent e) {//如果鼠标离开
                if (hoverTimer != null) {//如果不为空
                    hoverTimer.stop();//计时器结束
                }
                if (buttonHoverTipWindow != null) {//如果提示信息不为空
                    buttonHoverTipWindow.dispose();//释放提示信息
                    buttonHoverTipWindow = null;//提示信息置空
                }
            }
        });
        userConfirmPasswordTextField.passwordField.addKeyListener(new KeyAdapter() {//为用户确认密码注册文本域添加键盘监听
            @Override
            public void keyPressed(KeyEvent e) {//如果键盘按下
                if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_UP) {//如果按下回车或向上
                    if (userConfirmPasswordTextField.passwordField.getText().isEmpty()) {//如果输入为空
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "Confirm Password Cannot Be Empty" : "确认密码不可为空");//提示
                        userConfirmPasswordTextField.isWrong = true;//错误
                    } else {//否则
                        userConfirmPasswordTextField.isWrong = false;//正确
                        if (e.getKeyCode() == KeyEvent.VK_ENTER) {//如果是回车
                            registerButton.doClick();//按下注册按钮
                        } else {//否则
                            userPasswordTextField.passwordField.requestFocusInWindow();//焦点到上一行
                        }
                    }
                    userConfirmPasswordTextField.revalidate();//重新验证
                    userConfirmPasswordTextField.repaint();//重新绘制
                    e.consume();//阻止默认行为
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {//如果按下ESC
                    userConfirmPasswordTextField.isWrong = false;//正确
                    userConfirmPasswordTextField.revalidate();//重新验证
                    userConfirmPasswordTextField.repaint();//重新绘制
                    registerDialog.requestFocusInWindow();//焦点返回登录菜单
                    e.consume();//阻止默认行为
                }
            }
        });
        userConfirmPasswordTextField.passwordField.addFocusListener(new FocusAdapter() {//为用户确认密码注册文本域添加聚焦监听
            @Override
            public void focusLost(FocusEvent e) {//如果失去聚焦
                if (registerDialog.isVisible()) {//如果可见
                    if (userConfirmPasswordTextField.passwordField.getText().isEmpty()) {//如果输入为空
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "Confirm Password Cannot Be Empty" : "确认密码不可为空");//提示
                        userConfirmPasswordTextField.isWrong = true;//错误
                    } else {//否则
                        userConfirmPasswordTextField.isWrong = false;//正确
                    }
                    userConfirmPasswordTextField.revalidate();//重新验证
                    userConfirmPasswordTextField.repaint();//重新绘制
                }
            }
        });
        userConfirmPasswordTextField.passwordField.addMouseListener(new MouseAdapter() {//为用户确认密码注册文本域添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Confirm Change Password (Make Sure That Password Is The Same)" : "请确认密码（请保证两次输入密码一致）", userConfirmPasswordTextField.passwordField));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
            }

            @Override
            public void mouseExited(MouseEvent e) {//如果鼠标离开
                if (hoverTimer != null) {//如果不为空
                    hoverTimer.stop();//计时器结束
                }
                if (buttonHoverTipWindow != null) {//如果提示信息不为空
                    buttonHoverTipWindow.dispose();//释放提示信息
                    buttonHoverTipWindow = null;//提示信息置空
                }
            }
        });
        registerButton.addActionListener(_ -> {//为注册按钮添加事件监听
            userAccountTextField.isWrong = false;//正确
            userAccountTextField.revalidate();//重新验证
            userAccountTextField.repaint();//重新绘制
            userPhoneTextField.isWrong = false;//正确
            userPhoneTextField.revalidate();//重新验证
            userPhoneTextField.repaint();//重新绘制
            userVerificationTextField.isWrong = false;//正确
            userVerificationTextField.revalidate();//重新验证
            userVerificationTextField.repaint();//重新绘制
            userPasswordTextField.isWrong = false;//正确
            userPasswordTextField.revalidate();//重新验证
            userPasswordTextField.repaint();//重新绘制
            userConfirmPasswordTextField.isWrong = false;//正确
            userConfirmPasswordTextField.revalidate();//重新验证
            userConfirmPasswordTextField.repaint();//重新绘制
            String userAccountInput = userAccountTextField.inputTextField.getText().trim();//获取用户名输入
            String userPhoneInput = userPhoneTextField.inputTextField.getText().trim();//获取手机号输入
            String userVerificationCodeInput = userVerificationTextField.inputTextField.getText().trim();//获取用户验证码输入
            String userPasswordInput = userPasswordTextField.passwordField.getText().trim();//获取用户密码密码
            String userConfirmPasswordInput = userConfirmPasswordTextField.passwordField.getText().trim();//获取用户确认密码输入
            if (userAccountInput.isEmpty()) {//用户名为空
                userAccountTextField.isWrong = true;//错误
                userAccountTextField.revalidate();//重新验证
                userAccountTextField.repaint();//重新绘制
            }
            if (userPhoneInput.isEmpty()) {//手机号为空
                userPhoneTextField.isWrong = true;//错误
                userPhoneTextField.revalidate();//重新验证
                userPhoneTextField.repaint();//重新绘制
            }
            if (userVerificationCodeInput.isEmpty()) {//验证码为空
                userVerificationTextField.isWrong = true;//错误
                userVerificationTextField.revalidate();//重新验证
                userVerificationTextField.repaint();//重新绘制
            }
            if (userPasswordInput.isEmpty()) {//密码为空
                userPasswordTextField.isWrong = true;//错误
                userPasswordTextField.revalidate();//重新验证
                userPasswordTextField.repaint();//重新绘制
            }
            if (userConfirmPasswordInput.isEmpty()) {//确认密码为空
                userConfirmPasswordTextField.isWrong = true;//错误
                userConfirmPasswordTextField.revalidate();//重新验证
                userConfirmPasswordTextField.repaint();//重新绘制
            }
            if (userAccountInput.isEmpty() || userPhoneInput.isEmpty() || userVerificationCodeInput.isEmpty() || userPasswordInput.isEmpty() || userConfirmPasswordInput.isEmpty()) {//如果全部为空
                createBottomTipWindow(Main.SettingState.systemLanguage ? "None Of Fields Can Be Empty" : "所有字段都不可为空");//提示
                return;//直接返回
            }
            for (int i = 0; i < userAccountInput.length(); i++) {//遍历输入文本
                char c = userAccountInput.charAt(i);//获取当前字符
                if (c == '/' || c == '\\' || c == '"' || c == '{' || c == '}' || c == ':') {//如果是特殊字符
                    createBottomTipWindow(Main.SettingState.systemLanguage ? "No Special Character" : "请勿输入特殊字符");//提示
                    userAccountTextField.isWrong = true;//错误
                    userAccountTextField.revalidate();//重新验证
                    userAccountTextField.repaint();//重新绘制
                    return;//直接返回
                }
            }
            try {
                if (handleUserValidateUserUnique(userAccountInput, false)) {//如果用户名重复
                    createBottomTipWindow(Main.SettingState.systemLanguage ? "Duplicated Username" : "用户名重复");//提示
                    userAccountTextField.isWrong = true;//错误
                    userAccountTextField.revalidate();//重新验证
                    userAccountTextField.repaint();//重新绘制
                    return;//直接返回
                }
            } catch (IOException e) {
                handleErrorLog(e.getMessage());//处理错误日志
                throw new RuntimeException(e);//捕获异常
            }
            if (userPhoneInput.length() != 11) {//如果手机号不为11位
                createBottomTipWindow(Main.SettingState.systemLanguage ? "Please Enter 11-Digit Phone Number" : "请输入11位手机号");//提示
                userPhoneTextField.isWrong = true;//错误
                userPhoneTextField.revalidate();//重新验证
                userPhoneTextField.repaint();//重新绘制
                return;//直接返回
            } else {//否则
                for (int i = 0; i < userPhoneInput.length(); i++) {//遍历输入文本
                    if (!Character.isDigit(userPhoneInput.charAt(i))) {//如果非数字字符
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "No Non Digit Character" : "请勿输入非数字字符");//提示
                        userPhoneTextField.isWrong = true;//错误
                        userPhoneTextField.revalidate();//重新验证
                        userPhoneTextField.repaint();//重新绘制
                        return;//直接返回
                    }
                }
                try {
                    if (handleUserValidateUserUnique(userPhoneInput, true)) {//如果手机号已被使用
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "Phone Number Has Been Used" : "手机号已被使用");//提示
                        userPhoneTextField.isWrong = true;//错误
                        userPhoneTextField.revalidate();//重新验证
                        userPhoneTextField.repaint();//重新绘制
                        return;//直接返回
                    }
                } catch (IOException e) {
                    handleErrorLog(e.getMessage());//处理错误日志
                    throw new RuntimeException(e);//捕获异常
                }
            }
            userPhone = userPhoneInput;//获取手机号
            try {
                if (handleUserVerifyVerificationCode(userVerificationCodeInput) != 0) {//如果验证验证码失败
                    return;//直接返回
                }
            } catch (IOException e) {
                handleErrorLog(e.getMessage());//处理错误日志
                throw new RuntimeException(e);//捕获异常
            }
            if (userPasswordInput.length() < 6) {//如果密码长度小于6位
                userPasswordTextField.isWrong = true;//错误
                createBottomTipWindow(Main.SettingState.systemLanguage ? "Password Is At Least 6-Digit" : "密码至少为6位");//提示
                userPasswordTextField.revalidate();//重新验证
                userPasswordTextField.repaint();//重新绘制
                return;//直接返回
            }
            for (int i = 0; i < userPasswordInput.length(); i++) {//遍历输入文本
                char c = userPasswordInput.charAt(i);//获取当前字符
                if (c == '/' || c == '\\' || c == '"' || c == '{' || c == '}' || c == ':') {//如果是特殊字符
                    createBottomTipWindow(Main.SettingState.systemLanguage ? "No Special Character" : "请勿输入特殊字符");//提示
                    userPasswordTextField.isWrong = true;//错误
                    userPasswordTextField.revalidate();//重新验证
                    userPasswordTextField.repaint();//重新绘制
                    return;//直接返回
                }
            }
            if (!userPasswordInput.equals(userConfirmPasswordInput)) {//验证密码是否一致
                createBottomTipWindow(Main.SettingState.systemLanguage ? "The Password Entered Twice Is Inconsistent" : "两次输入的密码不一致");//提示
                userConfirmPasswordTextField.isWrong = true;//错误
                userConfirmPasswordTextField.revalidate();//重新验证
                userConfirmPasswordTextField.repaint();//重新绘制
                return;//直接返回
            }
            try {
                if (handleUserSaveUserInformation(userAccountInput, userPhone, userPasswordInput, 1073741824)) {//账户数据保存到服务器，如果成功
                    Main.SettingState.userAccount = userAccountInput;//验证成功，保存用户名
                    Main.SettingState.userPhone = userPhone;//验证成功，保存用户手机号
                    Main.SettingState.userPassword = userPasswordInput;//验证成功，保存用户密码
                    Main.SettingState.userAvailableCloudCapacity = 1073741824;//云盘可以字节数：1G
                    DirectoryTree.createCloudNode();//创建云盘结点
                    directoryManipulationButtonEnableJudgement();//状态判断
                    fileManipulationButtonEnableJudgement(selectionThumbnailItemList.size());//状态判断
                    historyManipulationButtonEnableJudgement();//状态判断
                    initUserDialog();//初始化用户菜单
                    registerDialog.dispose();//关闭注册菜单
                    userDialog.setVisible(true);//打开用户菜单
                }
            } catch (IOException e) {
                handleErrorLog(e.getMessage());//处理错误日志
                throw new RuntimeException(e);//捕获异常
            }
        });
        registerButton.addMouseListener(new MouseAdapter() {//为注册按钮添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Register" : "注册", registerButton));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
            }

            @Override
            public void mouseExited(MouseEvent e) {//如果鼠标离开
                if (hoverTimer != null) {//如果不为空
                    hoverTimer.stop();//计时器结束
                }
                if (buttonHoverTipWindow != null) {//如果提示信息不为空
                    buttonHoverTipWindow.dispose();//释放提示信息
                    buttonHoverTipWindow = null;//提示信息置空
                }
            }
        });

        JPanel registerPanel = new JPanel(new GridLayout(6, 1, 5, 5));//创建注册面板
        registerPanel.setBorder(BorderFactory.createEmptyBorder(7, 10, 7, 10));//创建边框
        registerPanel.setBackground(Main.SettingState.themeColor ? DARK_DIALOG_MAIN_COLOR : LIGHT_DIALOG_MAIN_COLOR);//设置背景颜色
        registerPanel.add(userAccountTextField);
        registerPanel.add(userPhoneTextField);
        registerPanel.add(userVerificationTextField);
        registerPanel.add(userPasswordTextField);
        registerPanel.add(userConfirmPasswordTextField);
        registerPanel.add(registerButton);

        JLabel logInTipLabel = new JLabel(Main.SettingState.systemLanguage ? "Return Login" : "返回登录");//登录提示标签
        logInTipLabel.setFont(new Font("微软雅黑", PLAIN, 15));//设置字体
        logInTipLabel.setForeground(Main.SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//设置字体颜色
        logInTipLabel.addMouseListener(new MouseAdapter() {//为登录提示标签添加鼠标监听
            @Override
            public void mouseClicked(MouseEvent e) {//如果鼠标点击
                userAccountTextField.isWrong = false;//正确
                userAccountTextField.revalidate();//重新验证
                userAccountTextField.repaint();//重新绘制
                userPhoneTextField.isWrong = false;//正确
                userPhoneTextField.revalidate();//重新验证
                userPhoneTextField.repaint();//重新绘制
                userVerificationTextField.isWrong = false;//正确
                userVerificationTextField.revalidate();//重新验证
                userVerificationTextField.repaint();//重新绘制
                userPasswordTextField.isWrong = false;//正确
                userAccountTextField.revalidate();//重新验证
                userAccountTextField.repaint();//重新绘制
                userConfirmPasswordTextField.isWrong = false;//正确
                userPhoneTextField.revalidate();//重新验证
                userPhoneTextField.repaint();//重新绘制
                logInTipLabel.setForeground(Main.SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//恢复颜色
                registerDialog.setVisible(false);//注册不可见
                logInDialog.setVisible(true);//登录可见
            }

            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                logInTipLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));//设置鼠标为手指，提升交互体验
                logInTipLabel.setForeground(Color.RED);//悬浮颜色
            }

            @Override
            public void mouseExited(MouseEvent e) {//如果鼠标离开
                logInTipLabel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));//设置提示信息鼠标为默认光标（不会变成字体编辑光标）
                logInTipLabel.setForeground(Main.SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//恢复颜色
            }
        });
        JPanel logInTipPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 3));//登录提示面板：设置布局管理器为右侧流式布局
        logInTipPanel.setBackground(Main.SettingState.themeColor ? DARK_DIALOG_MAIN_COLOR : LIGHT_DIALOG_MAIN_COLOR);//设置背景颜色
        logInTipPanel.add(logInTipLabel);

        registerDialog = new JDialog(Main.diskManagementSystemFrame, Main.SettingState.systemLanguage ? "Register" : "注册", true);//创建注册对话窗口
        registerDialog.setIconImage(new ImageIcon("src/material/image/dialogRegister.png").getImage());//设置图标
        registerDialog.setLayout(new BorderLayout());//设置布局
        registerDialog.add(registerTipPanel, BorderLayout.NORTH);//把注册提示标签添加到北部
        registerDialog.add(registerPanel, BorderLayout.CENTER);//把注册面板添加到中心
        registerDialog.add(logInTipPanel, BorderLayout.SOUTH);//把登录提示标签添加到南部
        registerDialog.setAlwaysOnTop(Main.SettingState.windowState);//设置永远在最上层
        registerDialog.pack();//设置合适
        registerDialog.setVisible(false);//设置不可见
        registerDialog.setLocation(Main.screenSize.width / 2 - registerDialog.getWidth() / 2, Main.screenSize.height / 2 - registerDialog.getHeight() / 2);//设置位置
        JRootPane registerRoot = registerDialog.getRootPane();//获取注册窗口的根
        registerRoot.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "closeRegisterDialog");//为根设置窗口关闭ESC按键绑定
        registerRoot.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_U, KeyEvent.CTRL_DOWN_MASK), "closeRegisterDialog");//为根设置窗口关闭Ctrl+U按键绑定
        registerRoot.getActionMap().put("closeRegisterDialog", new AbstractAction() {//当ESC按键执行时
            public void actionPerformed(ActionEvent event) {//行为执行
                registerDialog.dispatchEvent(new WindowEvent(registerDialog, WindowEvent.WINDOW_CLOSING));//关闭窗口
            }
        });
        registerDialog.addWindowListener(new WindowAdapter() {//为注册窗口添加窗口监听
            @Override
            public void windowClosing(WindowEvent e) {//如果窗口正在关闭
                if (bottomTipWindow != null && bottomTipWindow.isVisible()) {//如果底部提示窗口可见
                    bottomTipWindow.dispose();//底部提示窗口置空
                }
                if (buttonHoverTipWindow != null && buttonHoverTipWindow.isVisible()) {//如果按钮提示窗口不为空
                    buttonHoverTipWindow.dispose();//释放按钮提示窗口
                }
            }
        });
    }
}
