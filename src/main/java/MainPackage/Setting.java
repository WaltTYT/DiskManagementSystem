package MainPackage;

import DirectoryPackage.DirectoryTree;
import NetworkPackage.User;
import FileDisplayPackage.FileDisplayBottomBar;
import FileDisplayPackage.FileDisplayPopupMenu;

import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Objects;

import static DirectoryPackage.DirectoryTree.*;
import static MainPackage.Main.*;
import static MainPackage.ThemeColor.*;
import static FileDisplayPackage.FileDisplayMainPanel.*;
import static FileDisplayPackage.FileDisplayTopBar.*;
import static FileEditPackage.FileEditPanel.*;
import static java.awt.Font.PLAIN;
import static java.awt.event.InputEvent.CTRL_DOWN_MASK;
import static java.awt.event.InputEvent.SHIFT_DOWN_MASK;

public class Setting {//设置类
    public static JDialog settingDialog = new JDialog(diskManagementSystemFrame, SettingState.systemLanguage ? "Setting" : "设置", true);//设置对话窗口
    public static JDialog insertImageDialog = new JDialog(diskManagementSystemFrame, SettingState.systemLanguage ? "Insert Image" : "插入图片", true);//插入图片对话窗口
    public static JDialog suggestionFeedbackDialog = new JDialog(diskManagementSystemFrame, SettingState.systemLanguage ? "Suggestion and Feedback" : "建议与反馈", true);//建议反馈对话窗口

    public static JRadioButton closeAudioRadioButton;//关闭音频单选按钮
    public static JRadioButton closeMusicRadioButton;//关闭音乐单选按钮
    public static JRadioButton closeEffectRadioButton;//关闭音效单选按钮
    public static JRadioButton displayTitleRadioButton;//显示标题单选按钮
    public static JRadioButton hideTitleRadioButton;//隐藏标题单选按钮

    public static Clip bgmClip1;//背景音乐1
    public static Clip bgmClip2;//背景音乐2
    public static Clip bgmClip3;//背景音乐3
    public static Clip bgmClip4;//背景音乐4
    public static Clip bgmClip5;//背景音乐5
    public static Clip removeTipClip;//删除提示音效
    public static Clip volumeAdjustClip;//音量调节音效
    public static Clip switchPictureClip;//切换图片音效
    public static FloatControl bgm1GainControl;//背景音乐1音乐音频控制
    public static FloatControl bgm2GainControl;//背景音乐2音乐音频控制
    public static FloatControl bgm3GainControl;//背景音乐3音乐音频控制
    public static FloatControl bgm4GainControl;//背景音乐4音乐音频控制
    public static FloatControl bgm5GainControl;//背景音乐5音乐音频控制
    public static float bgm1MinGain;//音乐音频最小音量
    public static float bgm1MaxGain;//音乐音频最大音量
    public static FloatControl removeTipEffectGainControl;//删除提示音效音频控制
    public static FloatControl volumeAdjustEffectGainControl;//音量调节音效音频控制
    public static FloatControl switchPictureEffectGainControl;//切换图片音效音频控制
    public static float removeTipEffectMinGain;//音效音频最小音量
    public static float removeTipEffectMaxGain;//音效音频最大音量
    public static int currentBGM = 0;//当前背景音乐
    public static int tutorialStep = 0;//教程步骤
    public static JWindow tutorialWindow = new JWindow();//教程窗口
    private static final ArrayList<TutorialStep> tutorialStepRecordList = new ArrayList<>();//教程步骤记录列表

    private record TutorialStep(String[] tutorialText, int borderX, int borderY, int borderWidth, int borderHeight,
                                int textX, int textY) {//教程文本，边框左上角x坐标，边框左上角y坐标，边框宽度，边框高度，文本左下角x坐标，文本左下角y坐标
    }//教程步骤记录类

    private static class TutorialPanel extends JPanel {//教程面板（继承JPanel）
        private final TutorialStep step;//教程步骤

        public TutorialPanel(TutorialStep step) {//构造方法
            this.step = step;//记录步骤
            this.setForeground(new Color(0, 0, 0, 0));//设置前景
            this.setBackground(new Color(0, 0, 0, 0));//设置背景
        }

        @Override
        protected void paintComponent(Graphics g) {//重写绘制方法
            super.paintComponent(g);//调用父类重写清除背景
            Graphics2D g2d = (Graphics2D) g;//创建二维绘制工具类
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);//消除画图锯齿
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);//消除文字锯齿

            g2d.setColor(new Color(150, 150, 150, 120));//设置背景颜色
            g2d.fillRect(0, 0, getWidth(), getHeight());//覆盖全屏幕
            g2d.clearRect(step.borderX, step.borderY, step.borderWidth, step.borderHeight);//清空需要展示的区域
            g2d.setColor(new Color(0, 0, 0, 1));//设置透明颜色（保留1透明度防止穿透）
            g2d.fillRect(step.borderX, step.borderY, step.borderWidth, step.borderHeight);//重新填充需要展示的区域

            g2d.setStroke(new BasicStroke(3.0f));//设置画笔粗细
            g2d.setColor(Color.RED);//设置边框颜色
            g2d.drawRect(step.borderX, step.borderY, step.borderWidth, step.borderHeight);//绘制矩形边框

            g.setFont(new Font("楷体", PLAIN, 30));//设置字体
            g2d.setColor(SettingState.themeColor ? Color.white : Color.black);//设置字体颜色
            for (int i = 0; i < step.tutorialText.length; i++) {//遍历所有教程
                g2d.drawString(step.tutorialText[i], step.textX, step.textY + 40 * i);//绘制文字
            }
            g2d.dispose();//释放
        }
    }

    public static void handleSetting() {//处理设置
        if (itemHoverTipWindow != null) {//如果提示信息不为空
            itemHoverTipWindow.dispose();//释放提示信息
            itemHoverTipWindow = null;//提示信息置空
        }
        settingDialog.setVisible(true);//设置可见
    }

    public static void handleUtilizeTutorial() {//处理使用教程
        componentFocusable(false);//不可聚焦
        tutorialStep = 1;//初始重置步骤
        tutorialStepRecordList.clear();//清空
        tutorialStepRecordList.add(new TutorialStep(//初始化边框和教程文本
                new String[]{
                        SettingState.systemLanguage ? "This Is The System Disk Directory Tree" : "这是系统磁盘目录树",
                        SettingState.systemLanguage ? "The Folder Can Be Opened By Double Clicking Mouse" : "可以通过鼠标双击打开文件夹",
                        SettingState.systemLanguage ? "(Press Any Key To Continue...)" : "（按任意键继续...）"},
                directoryTreeScrollPane.getLocationOnScreen().x, directoryTreeScrollPane.getLocationOnScreen().y, panelSplitPane.getDividerLocation() - 2, directoryTreeScrollPane.getHeight(), panelSplitPane.getDividerLocation() + 5, screenSize.height / 2 - 25
        ));
        tutorialStepRecordList.add(new TutorialStep(
                new String[]{
                        SettingState.systemLanguage ? "This Is Thumbnail Picture Panel" : "这是图片缩略图面板",
                        SettingState.systemLanguage ? "The Pictures Would Be Placed Here After Opening Folder" : "文件夹打开后的图片会放在这里",
                        SettingState.systemLanguage ? "The Right Mouse Menu Can Be Opened By Clicking Right Mouse:" : "鼠标右键可以打开右键菜单",
                        SettingState.systemLanguage ? "(Press Any Key To Continue...)" : "（按任意键继续...）"},
                fileDisplayMainPanelScrollPane.getLocationOnScreen().x - 1, fileDisplayMainPanelScrollPane.getLocationOnScreen().y, fileDisplayMainPanelScrollPane.getWidth() - 1, fileDisplayMainPanelScrollPane.getHeight(), panelSplitPane.getDividerLocation() + 5, screenSize.height / 2 - 45
        ));
        tutorialStepRecordList.add(new TutorialStep(
                new String[]{
                        SettingState.systemLanguage ? "    This Is Thumbnail Picture Top Bar" : "           这是图片缩略图顶部栏",
                        SettingState.systemLanguage ? "You Can Copy,Paste,Rename,Remove And So On" : "你可以对图片进行复制、粘贴、重命名、删除等操作",
                        SettingState.systemLanguage ? "       (Press Any Key To Continue...)" : "           （按任意键继续...）"},
                fileDisplayTopBarPanel.getLocationOnScreen().x - 1, fileDisplayTopBarPanel.getLocationOnScreen().y, fileDisplayTopBarPanel.getWidth() - 1, fileDisplayTopBarPanel.getHeight(), panelSplitPane.getDividerLocation() + fileDisplayTopBarPanel.getWidth() / 2 - (SettingState.systemLanguage ? 290 : 315), fileDisplayTopBarPanel.getLocationOnScreen().y + fileDisplayTopBarPanel.getHeight() + 30
        ));
        tutorialStepRecordList.add(new TutorialStep(
                new String[]{
                        SettingState.systemLanguage ? "                  This Is Thumbnail Picture Bottom Bar" : "                 这是图片缩略图底部栏",
                        SettingState.systemLanguage ? "You Can Undo and Redo,Setting,Open Slide,Change Picture Scaling And So On" : "你可以进行撤销和恢复、设置、打开幻灯片、改变图片缩放等操作",
                        SettingState.systemLanguage ? "                      (Press Any Key To Continue...)" : "                 （按任意键继续...）"},
                fileDisplayBottomBarPanel.getLocationOnScreen().x - 1, fileDisplayBottomBarPanel.getLocationOnScreen().y, fileDisplayBottomBarPanel.getWidth() - 1, fileDisplayBottomBarPanel.getHeight(), panelSplitPane.getDividerLocation() + fileDisplayBottomBarPanel.getWidth() / 2 - (SettingState.systemLanguage ? 540 : 420), fileDisplayBottomBarPanel.getLocationOnScreen().y - 85
        ));
        tutorialStepRecordList.add(new TutorialStep(
                new String[]{
                        SettingState.systemLanguage ? "That's All For The Tutorial" : "这就是教程的全部内容",
                        SettingState.systemLanguage ? "        Have Fun!" : "   祝您使用愉快！",
                        SettingState.systemLanguage ? "(Press Any Key To Continue...)" : "（按任意键继续...）"},
                -5, -5, screenSize.width + 5, screenSize.height + 5, screenSize.width / 2 - 150, screenSize.height / 2 - 25
        ));

        tutorialWindow = new JWindow(diskManagementSystemFrame);//创建教程窗口（设置父窗口为主窗口防止覆盖）
        tutorialWindow.setVisible(true);//设置可见
        tutorialWindow.setSize(screenSize);//设置大小
        tutorialWindow.setLocation(0, 0);//设置位置
        tutorialWindow.setForeground(new Color(0, 0, 0, 0));//透明前景
        tutorialWindow.setBackground(new Color(0, 0, 0, 0));//透明背景
        showTutorialStep(tutorialStep);//初始化第一个教程步骤
        tutorialWindow.addMouseListener(new MouseAdapter() {//为教程窗口添加鼠标事件监听
            @Override
            public void mousePressed(MouseEvent e) {//如果鼠标按下
                if (tutorialStep <= tutorialStepRecordList.size()) {//如果在范围内
                    tutorialStep++;//步骤自增
                    showTutorialStep(tutorialStep);//展示教程步骤
                }
            }
        });
        JRootPane tutorialWindowRoot = tutorialWindow.getRootPane();//获取教程窗口的根
        for (Field field : KeyEvent.class.getFields()) {//使反射遍历KeyEvent类的所有public static final int字段（field获取成员变量）
            if (field.getName().startsWith("VK_")) {//如果是VK开头
                try {
                    tutorialWindowRoot.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.class.getField("VK_" + field.getName().substring(3)).getInt(null), 0), "anyKey");//任何按键
                } catch (IllegalAccessException | NoSuchFieldException e) {
                    handleErrorLog(e.getMessage());//处理错误日志
                    throw new RuntimeException(e);//捕获异常
                }
            }
        }
        tutorialWindowRoot.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_WINDOWS, 0), "none");//手动排除
        tutorialWindowRoot.getActionMap().put("anyKey", new AbstractAction() {//按下任何按键
            @Override
            public void actionPerformed(ActionEvent e) {//如果行为执行
                if (tutorialWindow != null && tutorialStep <= tutorialStepRecordList.size()) {//如果在范围内
                    tutorialStep++;//步骤自增
                    showTutorialStep(tutorialStep);//展示教程步骤
                }
            }
        });
    }

    private static void showTutorialStep(int stepIndex) {//展示教程步骤
        if (stepIndex > tutorialStepRecordList.size()) {//如果索引大于范围
            tutorialStep = 0;//重置为0
            tutorialWindow.dispose();//释放
            componentFocusable(true);//可聚焦
            mainPanel.requestFocusInWindow();//请求焦点
            return;//返回
        }
        tutorialWindow.setContentPane(new TutorialPanel(tutorialStepRecordList.get(stepIndex - 1)));//设置内容面板为通过记录类创建的教程面板
        tutorialWindow.revalidate();//重新验证
        tutorialWindow.repaint();//重新绘制
    }

    public static void componentFocusable(boolean focusable) {//组件是否可聚焦
        retreatButton.setFocusable(focusable);
        advanceButton.setFocusable(focusable);
        upperLayerButton.setFocusable(focusable);
        refreshButton.setFocusable(focusable);
        directoryField.getInputTextField().setFocusable(focusable);
        directoryField.getActionButton().setFocusable(focusable);
        cutButton.setFocusable(focusable);
        copyButton.setFocusable(focusable);
        pasteButton.setFocusable(focusable);
        renameButton.setFocusable(focusable);
        removeButton.setFocusable(focusable);
        sortComboBox.setFocusable(focusable);
        searchField.getInputTextField().setFocusable(focusable);
        searchField.getActionButton().setFocusable(focusable);
        mainPanel.setFocusable(focusable);
        FileDisplayBottomBar.undoButton.setFocusable(focusable);
        FileDisplayBottomBar.redoButton.setFocusable(focusable);
        FileDisplayBottomBar.editButton.setFocusable(focusable);
        FileDisplayBottomBar.userButton.setFocusable(focusable);
        FileDisplayBottomBar.uploadToCloudButton.setFocusable(focusable);
        FileDisplayBottomBar.settingButton.setFocusable(focusable);
        FileDisplayBottomBar.zoomSlider.setFocusable(focusable);
    }

    public static void handleErrorLog(String errorMessage) {//处理错误日志
        Path errorLogPath = Path.of(String.valueOf(spikeVisionCloudPath), "appErrorLog.txt");//获取错误日志路径
        try {
            if (!Files.exists(errorLogPath)) {//如果不存在错误日志路径
                Files.createFile(errorLogPath);//就创建路径
            }
        } catch (IOException e) {
            throw new RuntimeException(e);//捕获异常
        }
        if (errorMessage != null) {//如果不为空：添加错误日志
            errorMessage = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒：").format(System.currentTimeMillis()) + errorMessage;//为错误信息添加时间前缀
            try {
                try (FileOutputStream fos = new FileOutputStream(errorLogPath.toFile(), true)) {//创建文件输出流，且为追加信息
                    fos.write(errorMessage.getBytes());//写入信息
                    fos.write("\n".getBytes());//写入换行
                }
            } catch (IOException e) {
                throw new RuntimeException(e);//捕获异常
            }
            if (!Objects.equals(SettingState.userAccount, "")) {//如果用户登录
                SettingState.userAvailableCloudCapacity += 52428800;//加50MB
                createBottomTipWindow(SettingState.systemLanguage ? "Sorry, An Error Occurred, The Error Message Has Been Sent To Developer, To Express Our Apology, We Would Give You 50 MB Cloud Storage Capacity" : "抱歉，发生了错误，已经向开发者发送错误信息，为表达歉意，我们将赠送您50MB云盘空间");//提示
                try {
                    User.handleUserUploadUserErrorLog(errorMessage);//上传错误信息
                    User.updateUserDialog(3);//重新加载
                } catch (IOException e) {
                    throw new RuntimeException(e);//捕获异常
                }
            } else {//否则
                createBottomTipWindow(SettingState.systemLanguage ? "Sorry, An Error Occurred, The Error Message Has Been Sent To Developer" : "抱歉，发生了错误，已经向开发者发送错误信息");//提示
            }
        }
    }

    public static void handleSuggestionFeedback() {//处理建议反馈
        int maxChars = 1000;//最大输入字符数
        JTextArea suggestionFeedbackTextArea = new JTextArea();//建议反馈文本框
        suggestionFeedbackTextArea.setFont(new Font("楷体", PLAIN, 20));//设置字体
        suggestionFeedbackTextArea.setPreferredSize(new Dimension(500, 500));//设置大小
        suggestionFeedbackTextArea.setLineWrap(true);//自动换行
        suggestionFeedbackTextArea.setAutoscrolls(true);//自动滚动
        suggestionFeedbackTextArea.setWrapStyleWord(true);//整个单词换行

        JLabel counterLabel = new JLabel("0/" + maxChars);//限制输入字符标签
        counterLabel.setFont(new Font("楷体", PLAIN, 15));//设置字体
        counterLabel.setForeground(Color.GRAY);//设置文本颜色
        JPanel counterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));//限制输入字符面板
        counterPanel.setBackground(Color.white);//背景颜色
        counterPanel.add(counterLabel);

        ((AbstractDocument) suggestionFeedbackTextArea.getDocument()).setDocumentFilter(new DocumentFilter() {//设置DocumentFilter以限制输入并更新计数
            @Override
            public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr) throws BadLocationException {//如果输入字符串
                if (fb.getDocument().getLength() + text.length() <= maxChars) {//如果没有超过限制
                    super.insertString(fb, offset, text, attr);//替换字符串
                }
                updateCounterLabel(fb);//更新限制输入字符标签
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attr) throws BadLocationException {//如果更换字符串
                if (fb.getDocument().getLength() + text.length() <= maxChars) {//如果没有超过限制
                    super.insertString(fb, offset, text, attr);//替换字符串
                }
                updateCounterLabel(fb);//更新限制输入字符标签
            }

            @Override
            public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {//如果移除字符
                super.remove(fb, offset, length);//直接移除字符串不作其他处理
                updateCounterLabel(fb);//更新限制输入字符标签
            }

            private void updateCounterLabel(FilterBypass fb) {//更新限制输入字符标签
                int current = fb.getDocument().getLength();//获取当前字符数量
                counterLabel.setText(current + "/" + maxChars);//更新
                if (current == maxChars) {//如果到达上限
                    counterLabel.setForeground(Color.RED);//字体变为红色
                } else {//否则
                    counterLabel.setForeground(Color.GRAY);//恢复字体颜色
                }
            }
        });

        JButton sendSuggestionFeedbackButton = new JButton(SettingState.systemLanguage ? "Send" : "发送");//发送建议反馈按钮
        sendSuggestionFeedbackButton.setPreferredSize(new Dimension(500, 35));//设置大小
        sendSuggestionFeedbackButton.setFont(new Font("微软雅黑", PLAIN, 23));//设置字体
        sendSuggestionFeedbackButton.setBackground(USER_LOG_BUTTON_COLOR);//背景颜色
        sendSuggestionFeedbackButton.setFocusable(false);//不可聚焦
        sendSuggestionFeedbackButton.setBorder(null);//无边框

        suggestionFeedbackTextArea.addKeyListener(new KeyAdapter() {//为建议反馈文本框添加键盘监听
            @Override
            public void keyPressed(KeyEvent e) {//如果键盘按下
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {//如果按下ESC
                    suggestionFeedbackDialog.requestFocusInWindow();//焦点返回登录菜单
                    e.consume();//阻止默认行为
                }
            }
        });
        suggestionFeedbackTextArea.addMouseListener(new MouseAdapter() {//为建议反馈文本框添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(SettingState.systemLanguage ? "Please Enter Your Suggestion Or Feedback, Please Do Not Enter More Than 1000 Characters" : "请输入您的建议或反馈，请不要输入超过1000个字符", suggestionFeedbackTextArea));//展示提示窗口（鼠标悬浮一秒后展示）
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

        sendSuggestionFeedbackButton.addActionListener(_ -> {//为发送建议反馈按钮添加事件监听
            String suggestionFeedback = suggestionFeedbackTextArea.getText().trim();//获取建议反馈
            if (suggestionFeedback.isEmpty()) {//如果为空
                createBottomTipWindow(SettingState.systemLanguage ? "Input Cannot Be Empty" : "输入不可为空");//提示
            } else {//否则
                try {
                    if (System.currentTimeMillis() - SettingState.recentSuggestionFeedbackTime <= 3600000) {//如果用户建议反馈频率过高（限制一小时一条）
                        createBottomTipWindow(SettingState.systemLanguage ? "Your Suggestion Or Feedback Are Too Frequent, Please Send Back Later" : "您的建议或反馈频率过高，请稍后再发送");//提示
                    } else {//否则
                        User.handleUserUploadUserSuggestionFeedback(suggestionFeedback);//处理用户上传建议反馈
                        if (SettingState.userAccount.isEmpty()) {//如果没有登录
                            createBottomTipWindow(SettingState.systemLanguage ? "Successful Send, Thank You For Your Support" : "发送成功，感谢您的支持");//提示
                        } else {//否则
                            SettingState.recentSuggestionFeedbackTime = System.currentTimeMillis();//更新发送时间
                            SettingState.userAvailableCloudCapacity += 52428800;//加50MB
                            createBottomTipWindow(SettingState.systemLanguage ? "Successful Send, In Order To Thank You For Your Support, We Would Give You 50 MB Cloud Storage Capacity" : "发送成功，为了感谢您的支持，我们将赠送您50MB云盘空间");//提示
                            User.updateUserDialog(3);//重新加载
                        }
                    }
                } catch (IOException e) {
                    handleErrorLog(e.getMessage());//处理错误日志
                    throw new RuntimeException(e);//捕获异常
                }
            }
        });
        sendSuggestionFeedbackButton.addMouseListener(new MouseAdapter() {//为发送建议反馈按钮添加鼠标监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(SettingState.systemLanguage ? "Send Your Suggestion Or Feedback" : "发送您的建议或反馈", sendSuggestionFeedbackButton));//展示提示窗口（鼠标悬浮一秒后展示）
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

        suggestionFeedbackDialog = new JDialog(diskManagementSystemFrame, SettingState.systemLanguage ? "Suggestion and Feedback" : "建议与反馈", true);//创建建议反馈对话窗口
        suggestionFeedbackDialog.setIconImage(new ImageIcon("src/material/image/suggestion.png").getImage());//设置图标
        suggestionFeedbackDialog.setLayout(new BorderLayout());//设置布局
        suggestionFeedbackDialog.add(suggestionFeedbackTextArea, BorderLayout.NORTH);
        suggestionFeedbackDialog.add(counterPanel, BorderLayout.CENTER);
        suggestionFeedbackDialog.add(sendSuggestionFeedbackButton, BorderLayout.SOUTH);
        suggestionFeedbackDialog.pack();//合适
        suggestionFeedbackDialog.setLocation(screenSize.width / 2 - suggestionFeedbackDialog.getWidth() / 2, screenSize.height / 2 - suggestionFeedbackDialog.getHeight() / 2);//设置位置
        suggestionFeedbackDialog.setVisible(true);//最后设置可见
        JRootPane suggestionFeedbackRoot = suggestionFeedbackDialog.getRootPane();//获取建议反馈窗口的根
        suggestionFeedbackRoot.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "closeSuggestionFeedbackDialog");//为根设置窗口关闭ESC按键绑定
        suggestionFeedbackRoot.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_U, KeyEvent.CTRL_DOWN_MASK), "closeSuggestionFeedbackDialog");//为根设置窗口关闭Ctrl+U按键绑定
        suggestionFeedbackRoot.getActionMap().put("closeSuggestionFeedbackDialog", new AbstractAction() {//当ESC按键执行时
            public void actionPerformed(ActionEvent event) {//行为执行
                suggestionFeedbackDialog.dispatchEvent(new WindowEvent(suggestionFeedbackDialog, WindowEvent.WINDOW_CLOSING));//关闭窗口
            }
        });
        suggestionFeedbackDialog.addWindowListener(new WindowAdapter() {//为建议反馈窗口添加窗口监听
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

    public static void switchBGM() {//切换背景音乐
        if (!SettingState.masterState && !SettingState.bgmState) {//如果没有关闭音频或音乐
            if (currentBGM == 0) {//如果没有指定当前BGM
                currentBGM = SettingState.utilizeTimes % 5 + 1;//根据使用次数设置第一个BGM
                switch (currentBGM) {//根据当前音乐选择
                    case 1:
                        bgmClip1.setFramePosition(0);//重置播放位置
                        bgmClip1.start();//开始播放音乐
                        break;
                    case 2:
                        bgmClip2.setFramePosition(0);//重置播放位置
                        bgmClip2.start();//开始播放音乐
                        break;
                    case 3:
                        bgmClip3.setFramePosition(0);//重置播放位置
                        bgmClip3.start();//开始播放音乐
                        break;
                    case 4:
                        bgmClip4.setFramePosition(0);//重置播放位置
                        bgmClip4.start();//开始播放音乐
                        break;
                    case 5:
                        bgmClip5.setFramePosition(0);//重置播放位置
                        bgmClip5.start();//开始播放音乐
                        break;
                }
            } else {//否则继续播放当前BGM
                switch (currentBGM) {//根据当前音乐选择
                    case 1:
                        bgmClip1.start();//开始播放音乐
                        break;
                    case 2:
                        bgmClip2.start();//开始播放音乐
                        break;
                    case 3:
                        bgmClip3.start();//开始播放音乐
                        break;
                    case 4:
                        bgmClip4.start();//开始播放音乐
                        break;
                    case 5:
                        bgmClip5.start();//开始播放音乐
                        break;
                }
            }
        } else {//否则停止音频
            if (bgmClip1.isRunning()) {//如果正在运行
                bgmClip1.stop();//关闭
            } else if (bgmClip2.isRunning()) {//如果正在运行
                bgmClip2.stop();//关闭
            } else if (bgmClip3.isRunning()) {//如果正在运行
                bgmClip3.stop();//关闭
            } else if (bgmClip4.isRunning()) {//如果正在运行
                bgmClip4.stop();//关闭
            } else if (bgmClip5.isRunning()) {//如果正在运行
                bgmClip5.stop();//关闭
            }
        }
    }

    public static void switchSystemLanguage() {//切换系统语言
        boolean systemLanguage = SettingState.systemLanguage;//系统语言
        diskManagementSystemFrame.setTitle(systemLanguage ? "Picture Management System" : "图片管理系统");//设置标题
        rootNode.setUserObject(systemLanguage ? "Device" : "设备");//根结点
        computerNode.setUserObject(systemLanguage ? "My Computer" : "我的电脑");//电脑结点
        cloudNode.setUserObject(systemLanguage ? "My Cloud" : "我的云盘");//云盘结点
        DirectoryTree.directoryTree.revalidate();//重新验证
        DirectoryTree.directoryTree.repaint();//重新绘制
        DirectoryTree.directoryTree.updateUI();//更新UI

        emptyLabel.setText(SettingState.systemLanguage ? "No Image Available" : "暂无图片文件");//空状态提示标签：居中展示
        initBottomTipInformation();//刷新底部提示信息
        updateBottomTipInformation();//刷新底部提示信息

        FileDisplayPopupMenu.cutButton.setText(systemLanguage ? "Cut (Ctrl + X)" : "剪切（Ctrl + X）");//剪切按钮（有选中文件时有效）
        FileDisplayPopupMenu.copyButton.setText(systemLanguage ? "Copy (Ctrl + C)" : "复制（Ctrl + C）");//复制按钮（有选中文件时有效）
        FileDisplayPopupMenu.pasteButton.setText(systemLanguage ? "Paste (Ctrl + V)" : "粘贴（Ctrl + V）");//粘贴按钮（剪贴板有文件时有效）
        FileDisplayPopupMenu.renameButton.setText(systemLanguage ? "Rename (F2)" : "重命名（F2）");//重命名按钮（选中文件为一时有效）
        FileDisplayPopupMenu.removeButton.setText(systemLanguage ? "Delete (Delete)" : "删除（Delete）");//删除按钮（有选中文件时有效）
        FileDisplayPopupMenu.refreshButton.setText(systemLanguage ? "Refresh (F5)" : "刷新（F5）");//刷新按钮（任何时候有效）
        FileDisplayPopupMenu.recycleBinMenu.setText(systemLanguage ? "Recycle Bin" : "回收站");//回收站父级菜单项（任何时候有效）
        FileDisplayPopupMenu.openRecycleBinButton.setText(systemLanguage ? "Open Recycle Bin (Ctrl + O)" : "打开回收站（Ctrl + O）");//打开回收站按钮（任何时候有效）
        FileDisplayPopupMenu.emptyRecycleBinButton.setText(systemLanguage ? "Empty Recycle Bin (Ctrl + E)" : "清空回收站（Ctrl + E）");//清空回收站按钮（任何时候有效）
        FileDisplayPopupMenu.getPathButton.setText(systemLanguage ? "Get File Path (Ctrl + Shift + C)" : "获取文件路径（Ctrl + Shift + C）");//获取路径按钮（有选中图片时有效）
        FileDisplayPopupMenu.uploadToCloudButton.setText(systemLanguage ? "Upload File To Cloud (Ctrl + P)" : "上传文件至云端（Ctrl + P）");//上传至云端按钮（用户登录且云端未满且有选中图片）

        sortComboBox.removeAllItems();//清空项目
        sortComboBox.addItem(sortTypeList[0] == SortType.ANAME ? (SettingState.systemLanguage ? "Sort By Name (Ascending Order)" : "按名称排序（升序）") : (SettingState.systemLanguage ? "Sort By Name (Descending Order)" : "按名称排序（降序）"));//重新添加
        sortComboBox.addItem(sortTypeList[1] == SortType.ADATE ? (SettingState.systemLanguage ? "Sort By Date (Ascending Order)" : "按日期排序（升序）") : (SettingState.systemLanguage ? "Sort By Date (Descending Order)" : "按日期排序（降序）"));//重新添加
        sortComboBox.addItem(sortTypeList[2] == SortType.ATYPE ? (SettingState.systemLanguage ? "Sort By Type (Ascending Order)" : "按类型排序（升序）") : (SettingState.systemLanguage ? "Sort By Type (Descending Order)" : "按类型排序（降序）"));//重新添加
        sortComboBox.addItem(sortTypeList[3] == SortType.ASIZE ? (SettingState.systemLanguage ? "Sort By Size (Ascending Order)" : "按大小排序（升序）") : (SettingState.systemLanguage ? "Sort By Size (Descending Order)" : "按大小排序（降序）"));//重新添加
        sortComboBox.setPreferredSize(new Dimension(SettingState.systemLanguage ? 400 : 235, 34));//设置大小
        directoryField.setEmptyText(SettingState.systemLanguage ? "Current File Folder Directory" : "当前文件夹路径");//设置空白文本
        directoryField.getInputTextField().revalidate();//重新验证布局
        directoryField.getInputTextField().repaint();//重新绘制
        searchField.setEmptyText(SettingState.systemLanguage ? "Search For Image" : "搜索图片");//设置空白文本
        searchField.getInputTextField().revalidate();//重新验证布局
        searchField.getInputTextField().repaint();//重新绘制

        Setting.initSettingDialog();//刷新设置菜单
        if (SettingState.userAccount.isEmpty()) {//如果没有用户名
            User.initLogInDialog();//刷新登录菜单
            User.initRegisterDialog();//刷新注册菜单
        } else {
            User.initUserDialog();//刷新用户菜单
        }
    }

    public static void initSettingDialog() {//初始化设置菜单
        JPanel contentPanel = new JPanel(new GridLayout(0, 3, 5, 5));//创建设置内容面板
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));//创建边框
        contentPanel.setBackground(SettingState.themeColor ? DARK_DIALOG_MAIN_COLOR : LIGHT_DIALOG_MAIN_COLOR);//设置背景颜色

        if (fileDisplayPopupMenu == null) {//如果为空，说明是初始化阶段
            try {
                AudioInputStream bgm1AudioInputStream = AudioSystem.getAudioInputStream(new File("src/material/bgm/Tear - Daydream.wav"));//通过音频输入流获取音乐文件
                bgmClip1 = AudioSystem.getClip();//创建音乐播放器
                bgmClip1.open(bgm1AudioInputStream);//将加载的音乐设置给音效播放器
                AudioInputStream bgm2AudioInputStream = AudioSystem.getAudioInputStream(new File("src/material/bgm/美春の告白 - 菅野祐悟.wav"));//通过音频输入流获取音乐文件
                bgmClip2 = AudioSystem.getClip();//创建音乐播放器
                bgmClip2.open(bgm2AudioInputStream);//将加载的音乐设置给音效播放器
                AudioInputStream bgm3AudioInputStream = AudioSystem.getAudioInputStream(new File("src/material/bgm/True Love - May Second.wav"));//通过音频输入流获取音乐文件
                bgmClip3 = AudioSystem.getClip();//创建音乐播放器
                bgmClip3.open(bgm3AudioInputStream);//将加载的音乐设置给音效播放器
                AudioInputStream bgm4AudioInputStream = AudioSystem.getAudioInputStream(new File("src/material/bgm/Sundial Dreams - Kevin Kern.wav"));//通过音频输入流获取音乐文件
                bgmClip4 = AudioSystem.getClip();//创建音乐播放器
                bgmClip4.open(bgm4AudioInputStream);//将加载的音乐设置给音效播放器
                AudioInputStream bgm5AudioInputStream = AudioSystem.getAudioInputStream(new File("src/material/bgm/月下美人 - Soul Hug.wav"));//通过音频输入流获取音乐文件
                bgmClip5 = AudioSystem.getClip();//创建音乐播放器
                bgmClip5.open(bgm5AudioInputStream);//将加载的音乐设置给音效播放器
            } catch (LineUnavailableException | UnsupportedAudioFileException | IOException e) {
                handleErrorLog(e.getMessage());//处理错误日志
                throw new RuntimeException(e);//捕获异常
            }
            if (bgmClip1.isControlSupported(FloatControl.Type.MASTER_GAIN)) {//检查是否支持音量控制（MASTER_GAIN）
                bgm1GainControl = (FloatControl) bgmClip1.getControl(FloatControl.Type.MASTER_GAIN);//获取音频控制
                bgm1MinGain = bgm1GainControl.getMinimum();//获取最小音量（通常为-80.0dB）
                bgm1MaxGain = bgm1GainControl.getMaximum();//获取最大音量（通常为6.02dB）
            } else {//否则不支持
                bgm1MinGain = 0.0f;//置0
                bgm1MaxGain = 0.0f;//置0
                bgm1GainControl = null;//为空
            }
            if (bgmClip2.isControlSupported(FloatControl.Type.MASTER_GAIN)) {//检查是否支持音量控制（MASTER_GAIN）
                bgm2GainControl = (FloatControl) bgmClip2.getControl(FloatControl.Type.MASTER_GAIN);//获取音频控制
            } else {//否则不支持
                bgm2GainControl = null;//为空
            }
            if (bgmClip3.isControlSupported(FloatControl.Type.MASTER_GAIN)) {//检查是否支持音量控制（MASTER_GAIN）
                bgm3GainControl = (FloatControl) bgmClip3.getControl(FloatControl.Type.MASTER_GAIN);//获取音频控制
            } else {//否则不支持
                bgm3GainControl = null;//为空
            }
            if (bgmClip4.isControlSupported(FloatControl.Type.MASTER_GAIN)) {//检查是否支持音量控制（MASTER_GAIN）
                bgm4GainControl = (FloatControl) bgmClip4.getControl(FloatControl.Type.MASTER_GAIN);//获取音频控制
            } else {//否则不支持
                bgm4GainControl = null;//为空
            }
            if (bgmClip5.isControlSupported(FloatControl.Type.MASTER_GAIN)) {//检查是否支持音量控制（MASTER_GAIN）
                bgm5GainControl = (FloatControl) bgmClip5.getControl(FloatControl.Type.MASTER_GAIN);//获取音频控制
            } else {//否则不支持
                bgm5GainControl = null;//为空
            }

            try {
                AudioInputStream removeTipAudioInputStream = AudioSystem.getAudioInputStream(new File("src/material/soundEffect/removeTip.wav"));//通过音频输入流获取音效文件
                removeTipClip = AudioSystem.getClip();//创建音效播放器
                removeTipClip.open(removeTipAudioInputStream);//将加载的音效设置给音效播放器
                AudioInputStream volumeAdjustAudioInputStream = AudioSystem.getAudioInputStream(new File("src/material/soundEffect/volumeAdjust.wav"));//通过音频输入流获取音效文件
                volumeAdjustClip = AudioSystem.getClip();//创建音效播放器
                volumeAdjustClip.open(volumeAdjustAudioInputStream);//将加载的音效设置给音效播放器
                AudioInputStream switchPictureAudioInputStream = AudioSystem.getAudioInputStream(new File("src/material/soundEffect/switchPicture.wav"));//通过音频输入流获取音效文件
                switchPictureClip = AudioSystem.getClip();//创建音效播放器
                switchPictureClip.open(switchPictureAudioInputStream);//将加载的音效设置给音效播放器
            } catch (LineUnavailableException | UnsupportedAudioFileException | IOException e) {
                handleErrorLog(e.getMessage());//处理错误日志
                throw new RuntimeException(e);//捕获异常
            }
            if (removeTipClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {//检查是否支持音量控制（MASTER_GAIN）
                removeTipEffectGainControl = (FloatControl) removeTipClip.getControl(FloatControl.Type.MASTER_GAIN);//获取音频控制
                removeTipEffectMinGain = removeTipEffectGainControl.getMinimum();//获取最小音量（通常为-80.0dB）
                removeTipEffectMaxGain = removeTipEffectGainControl.getMaximum();//获取最大音量（通常为6.02dB）
            } else {//否则不支持
                removeTipEffectMinGain = 0.0f;//置0
                removeTipEffectMaxGain = 0.0f;//置0
                removeTipEffectGainControl = null;//为空
            }
            if (volumeAdjustClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {//检查是否支持音量控制（MASTER_GAIN）
                volumeAdjustEffectGainControl = (FloatControl) volumeAdjustClip.getControl(FloatControl.Type.MASTER_GAIN);//获取音频控制
            } else {//否则不支持
                volumeAdjustEffectGainControl = null;//为空
            }
            if (switchPictureClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {//检查是否支持音量控制（MASTER_GAIN）
                switchPictureEffectGainControl = (FloatControl) switchPictureClip.getControl(FloatControl.Type.MASTER_GAIN);//获取音频控制
            } else {//否则不支持
                switchPictureEffectGainControl = null;//为空
            }
        }

        JLabel audioAdjustLabel = new JLabel(SettingState.systemLanguage ? "Master Volume Adjust: " : "总体音量调节：");//总体音量调节标签
        audioAdjustLabel.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//设置字体颜色
        audioAdjustLabel.setFont(new Font("楷体", PLAIN, 20));//设置字体
        contentPanel.add(audioAdjustLabel);//总体音量调节
        audioAdjustLabel.addMouseListener(new MouseAdapter() {//为总体音量调节标签添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow((SettingState.systemLanguage ? "You Can Adjust Master Audio Volume Of The Software (Ctrl + Shift + M : Switch Volume)" : "您可以调节该软件的总体音量（Ctrl + Shift + M：开关音量）"), audioAdjustLabel));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
                audioAdjustLabel.setForeground(Color.RED);//悬浮颜色
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
                audioAdjustLabel.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//恢复颜色
            }
        });
        JSlider audioAdjustSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, SettingState.masterVolume);//总体音量调节拖动条
        audioAdjustSlider.setBackground(SettingState.themeColor ? DARK_DIALOG_MAIN_COLOR : LIGHT_DIALOG_MAIN_COLOR);//设置字体颜色
        closeAudioRadioButton = new JRadioButton(SettingState.systemLanguage ? "Turn Off Audio" : "关闭音频");//关闭音频单选按钮
        closeAudioRadioButton.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//设置字体颜色
        closeAudioRadioButton.setBackground(SettingState.themeColor ? DARK_DIALOG_MAIN_COLOR : LIGHT_DIALOG_MAIN_COLOR);//设置背景颜色
        closeAudioRadioButton.setFont(new Font("楷体", PLAIN, 20));//设置字体
        if (SettingState.masterState) {//根据设置状态判断
            closeAudioRadioButton.setSelected(true);
        }
        audioAdjustSlider.addChangeListener(_ -> {//为总体音量调节拖动条添加变化监听
            int value = audioAdjustSlider.getValue();//获取拖动条值
            if (bgm1GainControl != null && bgm2GainControl != null && bgm3GainControl != null && bgm4GainControl != null && bgm5GainControl != null
                    && removeTipEffectGainControl != null && volumeAdjustEffectGainControl != null && switchPictureEffectGainControl != null) {//如果音频控制非空
                bgm1GainControl.setValue((SettingState.bgmVolume - bgm1MinGain) * value / 100 + bgm1MinGain);//设置音频控制音量值
                bgm2GainControl.setValue((SettingState.bgmVolume - bgm1MinGain) * value / 100 + bgm1MinGain);//设置音频控制音量值
                bgm3GainControl.setValue((SettingState.bgmVolume - bgm1MinGain) * value / 100 + bgm1MinGain);//设置音频控制音量值
                bgm4GainControl.setValue((SettingState.bgmVolume - bgm1MinGain) * value / 100 + bgm1MinGain);//设置音频控制音量值
                bgm5GainControl.setValue((SettingState.bgmVolume - bgm1MinGain) * value / 100 + bgm1MinGain);//设置音频控制音量值
                removeTipEffectGainControl.setValue((SettingState.effectVolume - removeTipEffectMinGain) * value / 100 + removeTipEffectMinGain);//设置音频控制音量值
                volumeAdjustEffectGainControl.setValue((SettingState.effectVolume - removeTipEffectMinGain) * value / 100 + removeTipEffectMinGain);//设置音频控制音量值
                switchPictureEffectGainControl.setValue((SettingState.effectVolume - removeTipEffectMinGain) * value / 100 + removeTipEffectMinGain);//设置音频控制音量值
            }
            SettingState.masterVolume = value;//存储
        });
        audioAdjustSlider.addMouseListener(new MouseAdapter() {//为总体音量调节拖动条添加鼠标事件监听
            @Override
            public void mouseReleased(MouseEvent e) {//如果鼠标松开
                if (!SettingState.effectState && !SettingState.masterState) {//如果没有关闭音效
                    if (volumeAdjustClip.isRunning()) {//如果正在运行
                        volumeAdjustClip.stop();//停止
                    }
                    volumeAdjustClip.setFramePosition(0);//重置播放位置
                    volumeAdjustClip.start();//开始播放音效
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow((SettingState.systemLanguage ? "You Can Adjust Master Audio Volume Of The Software By Dragging Slider" : "您可以拖动调节该软件的总体音量"), audioAdjustSlider));//展示提示窗口（鼠标悬浮一秒后展示）
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
        closeAudioRadioButton.addActionListener(_ -> {//为关闭音频单选按钮添加事件监听
            SettingState.masterState = closeAudioRadioButton.isSelected();//更新
            switchBGM();//切换BGM
        });
        closeAudioRadioButton.addMouseListener(new MouseAdapter() {//为关闭音频单选按钮添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow((SettingState.systemLanguage ? "Turn Off All Audio (Contain Music And Sound Effect  Ctrl + Shift + M : Switch Volume)" : "关闭所有音频（包括音乐和音效 Ctrl + Shift + M：开关音量）"), closeAudioRadioButton));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
                closeAudioRadioButton.setForeground(Color.RED);//悬浮颜色
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
                closeAudioRadioButton.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//恢复颜色
            }
        });
        contentPanel.add(audioAdjustSlider);//面板添加总体音频调节拖动条
        contentPanel.add(closeAudioRadioButton);//面板添加关闭音频单选按钮

        JLabel bgmAdjustLabel = new JLabel(SettingState.systemLanguage ? "Music Volume Adjust: " : "音乐音量调节：");//音乐音量调节标签
        bgmAdjustLabel.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//设置字体颜色
        bgmAdjustLabel.setFont(new Font("楷体", PLAIN, 20));//设置字体
        contentPanel.add(bgmAdjustLabel);//音乐音量调节
        bgmAdjustLabel.addMouseListener(new MouseAdapter() {//为音乐音量调节标签添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow((SettingState.systemLanguage ? "You Can Adjust Music Volume Of The Software (Ctrl + Shift + P : Switch Music)" : "您可以调节该软件的音乐音量（Ctrl + Shift + P：开关音乐）"), bgmAdjustLabel));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
                bgmAdjustLabel.setForeground(Color.RED);//悬浮颜色
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
                bgmAdjustLabel.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//恢复颜色
            }
        });
        JSlider musicAdjustSlider = new JSlider();//音乐音量调节拖动条
        musicAdjustSlider.setBackground(SettingState.themeColor ? DARK_DIALOG_MAIN_COLOR : LIGHT_DIALOG_MAIN_COLOR);//设置字体颜色
        musicAdjustSlider.setMinimum((int) (bgm1MinGain) + 36);//初始化滑块的最小值（映射到音量范围），加36排除该低音量区间
        musicAdjustSlider.setMaximum((int) (bgm1MaxGain));//初始化滑块的最大值（映射到音量范围）
        musicAdjustSlider.setValue(SettingState.bgmVolume);//初始化拖动条
        closeMusicRadioButton = new JRadioButton(SettingState.systemLanguage ? "Turn Off Music" : "关闭音乐");//关闭音乐单选按钮
        closeMusicRadioButton.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//设置字体颜色
        closeMusicRadioButton.setBackground(SettingState.themeColor ? DARK_DIALOG_MAIN_COLOR : LIGHT_DIALOG_MAIN_COLOR);//设置背景颜色
        closeMusicRadioButton.setFont(new Font("楷体", PLAIN, 20));//设置字体
        if (SettingState.bgmState) {//根据设置状态判断
            closeMusicRadioButton.setSelected(true);
        } else if (fileDisplayPopupMenu == null) {//如果为空，说明是初始化阶段
            switchBGM();//开始播放BGM
        }
        if (fileDisplayPopupMenu == null) {//如果为空，说明是初始化阶段
            int musicAdjustSliderValue = musicAdjustSlider.getValue();//获取拖动条值
            if (bgm1GainControl != null && bgm2GainControl != null && bgm3GainControl != null && bgm4GainControl != null && bgm5GainControl != null) {//如果音频控制非空
                if (musicAdjustSliderValue == musicAdjustSlider.getMinimum()) {//如果在最小值
                    bgm1GainControl.setValue(bgm1MinGain);//直接设置音量为理论最小值，排除大范围低音量区间
                    bgm2GainControl.setValue(bgm1MinGain);//直接设置音量为理论最小值，排除大范围低音量区间
                    bgm3GainControl.setValue(bgm1MinGain);//直接设置音量为理论最小值，排除大范围低音量区间
                    bgm4GainControl.setValue(bgm1MinGain);//直接设置音量为理论最小值，排除大范围低音量区间
                    bgm5GainControl.setValue(bgm1MinGain);//直接设置音量为理论最小值，排除大范围低音量区间
                } else {//否则
                    bgm1GainControl.setValue(((musicAdjustSliderValue - bgm1MinGain) * ((float) SettingState.masterVolume / 100)) + bgm1MinGain);//设置音频控制音量值
                    bgm2GainControl.setValue(((musicAdjustSliderValue - bgm1MinGain) * ((float) SettingState.masterVolume / 100)) + bgm1MinGain);//设置音频控制音量值
                    bgm3GainControl.setValue(((musicAdjustSliderValue - bgm1MinGain) * ((float) SettingState.masterVolume / 100)) + bgm1MinGain);//设置音频控制音量值
                    bgm4GainControl.setValue(((musicAdjustSliderValue - bgm1MinGain) * ((float) SettingState.masterVolume / 100)) + bgm1MinGain);//设置音频控制音量值
                    bgm5GainControl.setValue(((musicAdjustSliderValue - bgm1MinGain) * ((float) SettingState.masterVolume / 100)) + bgm1MinGain);//设置音频控制音量值
                }
            }
        }
        musicAdjustSlider.addChangeListener(_ -> {//为音乐音量调节拖动条添加变化监听
            int value = musicAdjustSlider.getValue();//获取拖动条值
            if (bgm1GainControl != null && bgm2GainControl != null && bgm3GainControl != null && bgm4GainControl != null && bgm5GainControl != null) {//如果音频控制非空
                if (value == musicAdjustSlider.getMinimum()) {//如果在最小值
                    bgm1GainControl.setValue(bgm1MinGain);//直接设置音量为理论最小值，排除大范围低音量区间
                    bgm2GainControl.setValue(bgm1MinGain);//直接设置音量为理论最小值，排除大范围低音量区间
                    bgm3GainControl.setValue(bgm1MinGain);//直接设置音量为理论最小值，排除大范围低音量区间
                    bgm4GainControl.setValue(bgm1MinGain);//直接设置音量为理论最小值，排除大范围低音量区间
                    bgm5GainControl.setValue(bgm1MinGain);//直接设置音量为理论最小值，排除大范围低音量区间
                } else {//否则
                    bgm1GainControl.setValue((value - bgm1MinGain) * SettingState.masterVolume / 100 + bgm1MinGain);//设置音频控制音量值
                    bgm2GainControl.setValue((value - bgm1MinGain) * SettingState.masterVolume / 100 + bgm1MinGain);//设置音频控制音量值
                    bgm3GainControl.setValue((value - bgm1MinGain) * SettingState.masterVolume / 100 + bgm1MinGain);//设置音频控制音量值
                    bgm4GainControl.setValue((value - bgm1MinGain) * SettingState.masterVolume / 100 + bgm1MinGain);//设置音频控制音量值
                    bgm5GainControl.setValue((value - bgm1MinGain) * SettingState.masterVolume / 100 + bgm1MinGain);//设置音频控制音量值
                }
            }
            SettingState.bgmVolume = value;//存储
        });
        musicAdjustSlider.addMouseListener(new MouseAdapter() {//为音乐音量调节拖动条添加鼠标事件监听
            @Override
            public void mouseReleased(MouseEvent e) {//如果鼠标松开
                if (!SettingState.effectState && !SettingState.masterState) {//如果没有关闭音效
                    if (volumeAdjustClip.isRunning()) {//如果正在运行
                        volumeAdjustClip.stop();//停止
                    }
                    volumeAdjustClip.setFramePosition(0);//重置播放位置
                    volumeAdjustClip.start();//开始播放音效
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow((SettingState.systemLanguage ? "You Can Adjust Music Volume Of The Software By Dragging Slider" : "您可以拖动调节该软件的音乐音量"), musicAdjustSlider));//展示提示窗口（鼠标悬浮一秒后展示）
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
        closeMusicRadioButton.addActionListener(_ -> {//为关闭音乐单选按钮添加事件监听
            SettingState.bgmState = closeMusicRadioButton.isSelected();//更新
            switchBGM();//切换BGM
        });
        closeMusicRadioButton.addMouseListener(new MouseAdapter() {//为关闭音乐单选按钮添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow((SettingState.systemLanguage ? "Turn Off All Music (Ctrl + Shift + P : Switch Music)" : "关闭所有音乐（Ctrl + Shift + P：开关音乐）"), closeMusicRadioButton));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
                closeMusicRadioButton.setForeground(Color.RED);//悬浮颜色
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
                closeMusicRadioButton.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//恢复颜色
            }
        });
        contentPanel.add(musicAdjustSlider);//面板添加音乐音频调节拖动条
        contentPanel.add(closeMusicRadioButton);//面板添加关闭音乐单选按钮

        JLabel effectAdjustLabel = new JLabel(SettingState.systemLanguage ? "Sound Effect Volume Adjust: " : "音效音量调节：");//音效音量调节标签
        effectAdjustLabel.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//设置字体颜色
        effectAdjustLabel.setFont(new Font("楷体", PLAIN, 20));//设置字体
        contentPanel.add(effectAdjustLabel);//音效音量调节
        effectAdjustLabel.addMouseListener(new MouseAdapter() {//为音效音量调节标签添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow((SettingState.systemLanguage ? "You Can Adjust All Sound Effect Volume Of The Software (Ctrl + Shift + E : Switch Sound Effect)" : "您可以调节该软件的所有音效音量（Ctrl + Shift + E : 开关音效）"), effectAdjustLabel));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
                effectAdjustLabel.setForeground(Color.RED);//悬浮颜色
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
                effectAdjustLabel.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//恢复颜色
            }
        });
        JSlider effectAdjustSlider = new JSlider();//音效音量调节拖动条
        effectAdjustSlider.setBackground(SettingState.themeColor ? DARK_DIALOG_MAIN_COLOR : LIGHT_DIALOG_MAIN_COLOR);//设置字体颜色
        effectAdjustSlider.setMinimum((int) (removeTipEffectMinGain) + 36);//初始化滑块的最小值（映射到音量范围），加36排除该低音量区间
        effectAdjustSlider.setMaximum((int) (removeTipEffectMaxGain));//初始化滑块的最大值（映射到音量范围）
        effectAdjustSlider.setValue(SettingState.effectVolume);//初始化拖动条
        closeEffectRadioButton = new JRadioButton(SettingState.systemLanguage ? "Turn Off Sound Effect" : "关闭音效");//关闭音效单选按钮
        closeEffectRadioButton.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//设置字体颜色
        closeEffectRadioButton.setBackground(SettingState.themeColor ? DARK_DIALOG_MAIN_COLOR : LIGHT_DIALOG_MAIN_COLOR);//设置背景颜色
        closeEffectRadioButton.setFont(new Font("楷体", PLAIN, 20));//设置字体
        if (SettingState.effectState) {//根据设置状态判断
            closeEffectRadioButton.setSelected(true);
        }
        if (fileDisplayPopupMenu == null) {//如果为空，说明是初始化阶段
            int effectAdjustSliderValue = effectAdjustSlider.getValue();//获取拖动条值
            if (removeTipEffectGainControl != null && volumeAdjustEffectGainControl != null && switchPictureEffectGainControl != null) {//如果音频控制非空
                if (effectAdjustSliderValue == effectAdjustSlider.getMinimum()) {//如果在最小值
                    removeTipEffectGainControl.setValue(removeTipEffectMinGain);//直接设置音量为理论最小值，排除大范围低音量区间
                    volumeAdjustEffectGainControl.setValue(removeTipEffectMinGain);//直接设置音量为理论最小值，排除大范围低音量区间
                    switchPictureEffectGainControl.setValue(removeTipEffectMinGain);//直接设置音量为理论最小值，排除大范围低音量区间
                } else {//否则
                    removeTipEffectGainControl.setValue((effectAdjustSliderValue - removeTipEffectMinGain) * SettingState.masterVolume / 100 + removeTipEffectMinGain);//设置音频控制音量值
                    volumeAdjustEffectGainControl.setValue((effectAdjustSliderValue - removeTipEffectMinGain) * SettingState.masterVolume / 100 + removeTipEffectMinGain);//设置音频控制音量值
                    switchPictureEffectGainControl.setValue((effectAdjustSliderValue - removeTipEffectMinGain) * SettingState.masterVolume / 100 + removeTipEffectMinGain);//设置音频控制音量值
                }
            }
        }
        effectAdjustSlider.addChangeListener(_ -> {//为音效音量调节拖动条添加变化监听
            int value = effectAdjustSlider.getValue();//获取拖动条值
            if (removeTipEffectGainControl != null && volumeAdjustEffectGainControl != null && switchPictureEffectGainControl != null) {//如果音频控制非空
                if (value == effectAdjustSlider.getMinimum()) {//如果在最小值
                    removeTipEffectGainControl.setValue(removeTipEffectMinGain);//直接设置音量为理论最小值，排除大范围低音量区间
                    volumeAdjustEffectGainControl.setValue(removeTipEffectMinGain);//直接设置音量为理论最小值，排除大范围低音量区间
                    switchPictureEffectGainControl.setValue(removeTipEffectMinGain);//直接设置音量为理论最小值，排除大范围低音量区间
                } else {//否则
                    removeTipEffectGainControl.setValue((value - removeTipEffectMinGain) * SettingState.masterVolume / 100 + removeTipEffectMinGain);//设置音频控制音量值
                    volumeAdjustEffectGainControl.setValue((value - removeTipEffectMinGain) * SettingState.masterVolume / 100 + removeTipEffectMinGain);//设置音频控制音量值
                    switchPictureEffectGainControl.setValue((value - removeTipEffectMinGain) * SettingState.masterVolume / 100 + removeTipEffectMinGain);//设置音频控制音量值
                }
            }
            SettingState.effectVolume = value;//存储
        });
        effectAdjustSlider.addMouseListener(new MouseAdapter() {//为音效音量调节拖动条添加鼠标事件监听
            @Override
            public void mouseReleased(MouseEvent e) {//如果鼠标松开
                if (!SettingState.effectState && !SettingState.masterState) {//如果没有关闭音效
                    if (volumeAdjustClip.isRunning()) {//如果正在运行
                        volumeAdjustClip.stop();//停止
                    }
                    volumeAdjustClip.setFramePosition(0);//重置播放位置
                    volumeAdjustClip.start();//开始播放音效
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow((SettingState.systemLanguage ? "You Can Adjust All Sound Effect Volume Of The Software By Dragging Slider" : "您可以拖动调节该软件的所有音效音量"), effectAdjustSlider));//展示提示窗口（鼠标悬浮一秒后展示）
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
        closeEffectRadioButton.addActionListener(_ -> {//为关闭音效单选按钮添加事件监听
            SettingState.effectState = closeEffectRadioButton.isSelected();//更新
        });
        closeEffectRadioButton.addMouseListener(new MouseAdapter() {//为关闭音效单选按钮添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow((SettingState.systemLanguage ? "Turn Off All Sound Effect (Ctrl + Shift + E : Switch Sound Effect)" : "关闭所有音效（Ctrl + Shift + E : 开关音效）"), closeEffectRadioButton));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
                closeEffectRadioButton.setForeground(Color.RED);//悬浮颜色
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
                closeEffectRadioButton.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//恢复颜色
            }
        });
        contentPanel.add(effectAdjustSlider);//面板添加音效音频调节拖动条
        contentPanel.add(closeEffectRadioButton);//面板添加关闭音效单选按钮

        JLabel windowStateLabel = new JLabel(SettingState.systemLanguage ? "Setting Window State: " : "设置窗口状态：");//窗口状态标签
        windowStateLabel.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//设置字体颜色
        windowStateLabel.setFont(new Font("楷体", PLAIN, 20));//设置字体
        windowStateLabel.addMouseListener(new MouseAdapter() {//为窗口状态标签添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow((SettingState.systemLanguage ? "You Can Choose To Modify Software Window To Display Or Hide Title" : "您可以选择修改窗口状态为显示标题或隐藏标题"), windowStateLabel));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
                windowStateLabel.setForeground(Color.RED);//悬浮颜色
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
                windowStateLabel.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//恢复颜色
            }
        });
        contentPanel.add(windowStateLabel);//窗口状态选择
        displayTitleRadioButton = new JRadioButton(SettingState.systemLanguage ? "Display Title" : "显示标题");//显示标题单选按钮
        hideTitleRadioButton = new JRadioButton(SettingState.systemLanguage ? "Hide Title" : "隐藏标题");//隐藏标题单选按钮
        displayTitleRadioButton.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//设置字体颜色
        displayTitleRadioButton.setBackground(SettingState.themeColor ? DARK_DIALOG_MAIN_COLOR : LIGHT_DIALOG_MAIN_COLOR);//设置背景颜色
        displayTitleRadioButton.setFont(new Font("楷体", PLAIN, 20));//设置字体
        hideTitleRadioButton.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//设置字体颜色
        hideTitleRadioButton.setBackground(SettingState.themeColor ? DARK_DIALOG_MAIN_COLOR : LIGHT_DIALOG_MAIN_COLOR);//设置背景颜色
        hideTitleRadioButton.setFont(new Font("楷体", PLAIN, 20));//设置字体
        if (SettingState.windowState) {//根据设置状态判断
            hideTitleRadioButton.setSelected(true);
        } else {
            displayTitleRadioButton.setSelected(true);
        }
        ButtonGroup windowStateButtonGroup = new ButtonGroup();//窗口状态按钮组
        windowStateButtonGroup.add(displayTitleRadioButton);//组添加显示标题单选按钮
        windowStateButtonGroup.add(hideTitleRadioButton);//组添加隐藏标题单选按钮
        displayTitleRadioButton.addActionListener(_ -> {//为显示标题单选按钮添加事件监听
            GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(null);//取消窗口全屏
            diskManagementSystemFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);//设置窗口直接最大化
            SettingState.windowState = false;//更新
            settingDialog.setAlwaysOnTop(false);//设置不永远在最上层
            progressWindow.setAlwaysOnTop(false);//设置不永远在最上层
            if (SettingState.userAccount.isEmpty()) {//如果没有登录
                User.logInDialog.setAlwaysOnTop(false);//设置不永远在最上层
                User.registerDialog.setAlwaysOnTop(false);//设置不永远在最上层
            } else {//否则
                User.userDialog.setAlwaysOnTop(false);//设置不永远在最上层
            }
            picturePanel.setPreferredSize(new Dimension(screenSize.width, PANEL_DEFAULT_HEIGHT));//设置大小
        });
        displayTitleRadioButton.addMouseListener(new MouseAdapter() {//为显示标题单选按钮添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow((SettingState.systemLanguage ? "Display Window Title (ESC Or F11)" : "显示窗口标题（ESC 或 F11）"), displayTitleRadioButton));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
                displayTitleRadioButton.setForeground(Color.RED);//悬浮颜色
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
                displayTitleRadioButton.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//恢复颜色
            }
        });
        hideTitleRadioButton.addActionListener(_ -> {//为隐藏标题单选按钮添加事件监听
            GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(diskManagementSystemFrame);//设置窗口全屏
            createBottomTipWindow(SettingState.systemLanguage ? "Press ESC Or F11 To Exit Fullscreen" : "按下ESC或F11即可退出全屏");//提示
            SettingState.windowState = true;//更新
            settingDialog.setAlwaysOnTop(true);//设置永远在最上层
            progressWindow.setAlwaysOnTop(true);//设置永远在最上层
            if (SettingState.userAccount.isEmpty()) {//如果没有登录
                User.logInDialog.setAlwaysOnTop(true);//设置永远在最上层
                User.registerDialog.setAlwaysOnTop(true);//设置永远在最上层
            } else {//否则
                User.userDialog.setAlwaysOnTop(true);//设置永远在最上层
            }
            picturePanel.setPreferredSize(new Dimension(screenSize.width, PANEL_FULLSCREEN_HEIGHT));//设置大小
        });
        hideTitleRadioButton.addMouseListener(new MouseAdapter() {//为隐藏标题单选按钮添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow((SettingState.systemLanguage ? "Hide Window Title (F11)" : "隐藏窗口标题（F11）"), hideTitleRadioButton));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
                hideTitleRadioButton.setForeground(Color.RED);//悬浮颜色
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
                hideTitleRadioButton.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//恢复颜色
            }
        });
        contentPanel.add(displayTitleRadioButton);//面板添加显示标题单选按钮
        contentPanel.add(hideTitleRadioButton);//面板添加隐藏标题单选按钮

        JLabel systemLanguageLabel = new JLabel(SettingState.systemLanguage ? "System Language Selection: " : "系统语言选择：");//系统语言标签
        systemLanguageLabel.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//设置字体颜色
        systemLanguageLabel.setFont(new Font("楷体", PLAIN, 20));//设置字体
        systemLanguageLabel.addMouseListener(new MouseAdapter() {//为系统语言标签添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow((SettingState.systemLanguage ? "You Can Choose To Modify Software Language To Chinese Or English" : "您可以选择修改软件语言为中文或英文"), systemLanguageLabel));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
                systemLanguageLabel.setForeground(Color.RED);//悬浮颜色
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
                systemLanguageLabel.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//恢复颜色
            }
        });
        contentPanel.add(systemLanguageLabel);//系统语言选择
        JRadioButton chineseLanguageRadioButton = new JRadioButton(SettingState.systemLanguage ? "Chinese" : "中文语言");//中文语言单选按钮
        JRadioButton englishLanguageRadioButton = new JRadioButton(SettingState.systemLanguage ? "English" : "英文语言");//英文语言单选按钮
        chineseLanguageRadioButton.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//设置字体颜色
        chineseLanguageRadioButton.setBackground(SettingState.themeColor ? DARK_DIALOG_MAIN_COLOR : LIGHT_DIALOG_MAIN_COLOR);//设置背景颜色
        chineseLanguageRadioButton.setFont(new Font("楷体", PLAIN, 20));//设置字体
        englishLanguageRadioButton.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//设置字体颜色
        englishLanguageRadioButton.setBackground(SettingState.themeColor ? DARK_DIALOG_MAIN_COLOR : LIGHT_DIALOG_MAIN_COLOR);//设置背景颜色
        englishLanguageRadioButton.setFont(new Font("楷体", PLAIN, 20));//设置字体
        if (SettingState.systemLanguage) {//根据设置状态判断
            englishLanguageRadioButton.setSelected(true);
        } else {
            chineseLanguageRadioButton.setSelected(true);
        }
        ButtonGroup systemLanguageButtonGroup = new ButtonGroup();//系统语言按钮组
        systemLanguageButtonGroup.add(chineseLanguageRadioButton);//组添加中文语言单选按钮
        systemLanguageButtonGroup.add(englishLanguageRadioButton);//组添加英文语言单选按钮
        chineseLanguageRadioButton.addActionListener(_ -> {//为中文语言单选按钮添加事件监听
            SettingState.systemLanguage = false;//更新
            settingDialog.dispose();//关闭
            switchSystemLanguage();//切换系统语言
            settingDialog.setVisible(true);//开启
        });
        chineseLanguageRadioButton.addMouseListener(new MouseAdapter() {//为中文语言单选按钮添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow((SettingState.systemLanguage ? "Change Software Language To Chinese (It Can Only Take Full Effect After Restarting)" : "修改软件语言为中文（注意只有重启软件后才能完全生效）"), chineseLanguageRadioButton));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
                chineseLanguageRadioButton.setForeground(Color.RED);//悬浮颜色
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
                chineseLanguageRadioButton.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//恢复颜色
            }
        });
        englishLanguageRadioButton.addActionListener(_ -> {//为英文语言单选按钮添加事件监听
            SettingState.systemLanguage = true;//更新
            settingDialog.dispose();//关闭
            switchSystemLanguage();//切换系统语言
            settingDialog.setVisible(true);//开启
        });
        englishLanguageRadioButton.addMouseListener(new MouseAdapter() {//为英文语言单选按钮添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow((SettingState.systemLanguage ? "Change Software Language To English (It Can Only Take Full Effect After Restarting)" : "修改软件语言为英文（注意只有重启软件后才能完全生效）"), englishLanguageRadioButton));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
                englishLanguageRadioButton.setForeground(Color.RED);//悬浮颜色
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
                englishLanguageRadioButton.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//恢复颜色
            }
        });
        contentPanel.add(chineseLanguageRadioButton);//面板添加中文语言单选按钮
        contentPanel.add(englishLanguageRadioButton);//面板添加英文语言单选按钮

        JLabel themeColorLabel = new JLabel(SettingState.systemLanguage ? "Theme Color Selection: " : "主题颜色选择：");//主题颜色标签
        themeColorLabel.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//设置字体颜色
        themeColorLabel.setFont(new Font("楷体", PLAIN, 20));//设置字体
        themeColorLabel.addMouseListener(new MouseAdapter() {//为主题颜色标签添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow((SettingState.systemLanguage ? "You Can Choose To Modify Software Theme Color To Light Or Dark" : "您可以选择修改软件主题颜色为浅色或深色"), themeColorLabel));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
                themeColorLabel.setForeground(Color.RED);//悬浮颜色
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
                themeColorLabel.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//恢复颜色
            }
        });
        contentPanel.add(themeColorLabel);//主题颜色选择
        JRadioButton lightThemeRadioButton = new JRadioButton(SettingState.systemLanguage ? "Light Theme" : "浅色主题");//浅色主题单选按钮
        JRadioButton darkThemeRadioButton = new JRadioButton(SettingState.systemLanguage ? "Dark Theme" : "深色主题");//深色主题单选按钮
        lightThemeRadioButton.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//设置字体颜色
        lightThemeRadioButton.setBackground(SettingState.themeColor ? DARK_DIALOG_MAIN_COLOR : LIGHT_DIALOG_MAIN_COLOR);//设置背景颜色
        lightThemeRadioButton.setFont(new Font("楷体", PLAIN, 20));//设置字体
        darkThemeRadioButton.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//设置字体颜色
        darkThemeRadioButton.setBackground(SettingState.themeColor ? DARK_DIALOG_MAIN_COLOR : LIGHT_DIALOG_MAIN_COLOR);//设置背景颜色
        darkThemeRadioButton.setFont(new Font("楷体", PLAIN, 20));//设置字体
        if (SettingState.themeColor) {//根据设置状态判断
            darkThemeRadioButton.setSelected(true);
        } else {
            lightThemeRadioButton.setSelected(true);
        }
        ButtonGroup themeColorButtonGroup = new ButtonGroup();//主题颜色按钮组
        themeColorButtonGroup.add(lightThemeRadioButton);//组添加浅色主题单选按钮
        themeColorButtonGroup.add(darkThemeRadioButton);//组添加深色主题单选按钮
        lightThemeRadioButton.addActionListener(_ -> {//为浅色主题单选按钮添加事件监听
            SettingState.themeColor = false;//更新
            settingDialog.dispose();//关闭
            switchThemeColor();//切换主题颜色
            settingDialog.setVisible(true);//打开
        });
        lightThemeRadioButton.addMouseListener(new MouseAdapter() {//为浅色主题单选按钮添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow((SettingState.systemLanguage ? "Change Software Theme To Light (It Can Only Take Full Effect After Restarting)" : "修改软件主题为浅色（注意只有重启软件后才能完全生效）"), lightThemeRadioButton));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
                lightThemeRadioButton.setForeground(Color.RED);//悬浮颜色
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
                lightThemeRadioButton.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//恢复颜色
            }
        });
        darkThemeRadioButton.addActionListener(_ -> {//为深色主题单选按钮添加事件监听
            SettingState.themeColor = true;//更新
            settingDialog.dispose();//关闭
            switchThemeColor();//切换主题颜色
            settingDialog.setVisible(true);//打开
        });
        darkThemeRadioButton.addMouseListener(new MouseAdapter() {//为深色主题单选按钮添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow((SettingState.systemLanguage ? "Change Software Theme To Dark (It Can Only Take Full Effect After Restarting)" : "修改软件主题为深色（注意只有重启软件后才能完全生效）"), darkThemeRadioButton));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
                darkThemeRadioButton.setForeground(Color.RED);//悬浮颜色
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
                darkThemeRadioButton.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//恢复颜色
            }
        });
        contentPanel.add(lightThemeRadioButton);//面板添加浅色主题单选按钮
        contentPanel.add(darkThemeRadioButton);//面板添加深色主题单选按钮

        JLabel dbclickBehaviorLabel = new JLabel(SettingState.systemLanguage ? "File Double Click Behavior: " : "文件双击行为：");//双击行为标签
        dbclickBehaviorLabel.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//设置字体颜色
        dbclickBehaviorLabel.setFont(new Font("楷体", PLAIN, 20));//设置字体
        dbclickBehaviorLabel.addMouseListener(new MouseAdapter() {//为双击行为标签添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow((SettingState.systemLanguage ? "You Can Choose Whether To Deselect Or Open In Other App When You Double-Click On A File" : "您可以选择当鼠标双击文件时是取消选择文件还是编辑文件"), dbclickBehaviorLabel));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
                dbclickBehaviorLabel.setForeground(Color.RED);//悬浮颜色
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
                dbclickBehaviorLabel.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//恢复颜色
            }
        });
        contentPanel.add(dbclickBehaviorLabel);//文件双击行为
        JRadioButton deselectBehaviorRadioButton = new JRadioButton(SettingState.systemLanguage ? "Deselect File" : "取消选择");//取消选择单选按钮
        JRadioButton openBehaviorRadioButton = new JRadioButton(SettingState.systemLanguage ? "Edit File" : "编辑文件");//打开图片单选按钮
        deselectBehaviorRadioButton.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//设置字体颜色
        deselectBehaviorRadioButton.setBackground(SettingState.themeColor ? DARK_DIALOG_MAIN_COLOR : LIGHT_DIALOG_MAIN_COLOR);//设置背景颜色
        deselectBehaviorRadioButton.setFont(new Font("楷体", PLAIN, 20));//设置字体
        openBehaviorRadioButton.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//设置字体颜色
        openBehaviorRadioButton.setBackground(SettingState.themeColor ? DARK_DIALOG_MAIN_COLOR : LIGHT_DIALOG_MAIN_COLOR);//设置背景颜色
        openBehaviorRadioButton.setFont(new Font("楷体", PLAIN, 20));//设置字体
        if (SettingState.dbclickBehavior) {//根据设置状态判断
            openBehaviorRadioButton.setSelected(true);
        } else {
            deselectBehaviorRadioButton.setSelected(true);
        }
        ButtonGroup dbclickBehaviorButtonGroup = new ButtonGroup();//双击行为按钮组
        dbclickBehaviorButtonGroup.add(deselectBehaviorRadioButton);//组添加取消选择单选按钮
        dbclickBehaviorButtonGroup.add(openBehaviorRadioButton);//组添加打开图片单选按钮
        deselectBehaviorRadioButton.addActionListener(_ -> {//为取消选择单选按钮添加事件监听
            SettingState.dbclickBehavior = false;//更新
        });
        deselectBehaviorRadioButton.addMouseListener(new MouseAdapter() {//为取消选择单选按钮添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow((SettingState.systemLanguage ? "Deselect Thumbnail Image When Double-Click" : "双击缩略图图片时取消选择"), deselectBehaviorRadioButton));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
                deselectBehaviorRadioButton.setForeground(Color.RED);//悬浮颜色
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
                deselectBehaviorRadioButton.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//恢复颜色
            }
        });
        openBehaviorRadioButton.addActionListener(_ -> {//为打开图片单选按钮添加事件监听
            SettingState.dbclickBehavior = true;//更新
        });
        openBehaviorRadioButton.addMouseListener(new MouseAdapter() {//为打开图片单选按钮添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow((SettingState.systemLanguage ? "Play Picture Slide When Double-click" : "双击缩略图图片时播放图片幻灯片"), openBehaviorRadioButton));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
                openBehaviorRadioButton.setForeground(Color.RED);//悬浮颜色
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
                openBehaviorRadioButton.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//恢复颜色
            }
        });
        contentPanel.add(deselectBehaviorRadioButton);//面板添加取消选择单选按钮
        contentPanel.add(openBehaviorRadioButton);//面板添加打开图片单选按钮

        JLabel hoverTipLabel = new JLabel(SettingState.systemLanguage ? "Hover Tip State: " : "悬浮提示状态：");//悬浮提示标签
        hoverTipLabel.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//设置字体颜色
        hoverTipLabel.setFont(new Font("楷体", PLAIN, 20));//设置字体
        hoverTipLabel.addMouseListener(new MouseAdapter() {//为悬浮提示标签添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow((SettingState.systemLanguage ? "You Can Choose To Turn Image Detail On Or Off When Mouse Hover Over Thumbnail Image" : "您可以选择开启或关闭鼠标在缩略图上悬浮时展示的图片详情"), hoverTipLabel));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
                hoverTipLabel.setForeground(Color.RED);//悬浮颜色
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
                hoverTipLabel.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//恢复颜色
            }
        });
        contentPanel.add(hoverTipLabel);//悬浮提示状态
        JRadioButton openHoverRadioButton = new JRadioButton(SettingState.systemLanguage ? "Open" : "开启提示");//开启提示单选按钮
        JRadioButton closeHoverRadioButton = new JRadioButton(SettingState.systemLanguage ? "Close" : "关闭提示");//关闭提示单选按钮
        openHoverRadioButton.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//设置字体颜色
        openHoverRadioButton.setBackground(SettingState.themeColor ? DARK_DIALOG_MAIN_COLOR : LIGHT_DIALOG_MAIN_COLOR);//设置背景颜色
        openHoverRadioButton.setFont(new Font("楷体", PLAIN, 20));//设置字体
        closeHoverRadioButton.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//设置字体颜色
        closeHoverRadioButton.setBackground(SettingState.themeColor ? DARK_DIALOG_MAIN_COLOR : LIGHT_DIALOG_MAIN_COLOR);//设置背景颜色
        closeHoverRadioButton.setFont(new Font("楷体", PLAIN, 20));//设置字体
        if (SettingState.hoverTip) {//根据设置状态判断
            closeHoverRadioButton.setSelected(true);
        } else {
            openHoverRadioButton.setSelected(true);
        }
        ButtonGroup hoverTipButtonGroup = new ButtonGroup();//悬浮提示组
        hoverTipButtonGroup.add(openHoverRadioButton);//组添加开启提示单选按钮
        hoverTipButtonGroup.add(closeHoverRadioButton);//组添加关闭提示单选按钮
        openHoverRadioButton.addActionListener(_ -> {//为开启提示单选按钮添加事件监听
            SettingState.hoverTip = false;//更新
        });
        openHoverRadioButton.addMouseListener(new MouseAdapter() {//为开启提示单选按钮添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow((SettingState.systemLanguage ? "Turn On Hover Over Image Detail" : "开启鼠标悬浮图片详情"), openHoverRadioButton));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
                openHoverRadioButton.setForeground(Color.RED);//悬浮颜色
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
                openHoverRadioButton.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//恢复颜色
            }
        });
        closeHoverRadioButton.addActionListener(_ -> {//为关闭提示单选按钮添加事件监听
            SettingState.hoverTip = true;//更新
        });
        closeHoverRadioButton.addMouseListener(new MouseAdapter() {//为关闭提示单选按钮添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow((SettingState.systemLanguage ? "Turn Off Hover Over Image Detail" : "关闭鼠标悬浮图片详情"), closeHoverRadioButton));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
                closeHoverRadioButton.setForeground(Color.RED);//悬浮颜色
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
                closeHoverRadioButton.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//恢复颜色
            }
        });
        contentPanel.add(openHoverRadioButton);//面板添加开启提示单选按钮
        contentPanel.add(closeHoverRadioButton);//面板添加关闭提示单选按钮

        JLabel deleteTipLabel = new JLabel(SettingState.systemLanguage ? "Remove Confirm State: " : "删除确认状态：");//删除提示标签
        deleteTipLabel.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//设置字体颜色
        deleteTipLabel.setFont(new Font("楷体", PLAIN, 20));//设置字体
        deleteTipLabel.addMouseListener(new MouseAdapter() {//为删除提示标签添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow((SettingState.systemLanguage ? "You Can Choose Whether To Turn The Prompt On Or Off When Remove Image" : "您可以选择在删除图片时是开启还是关闭提示"), deleteTipLabel));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
                deleteTipLabel.setForeground(Color.RED);//悬浮颜色
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
                deleteTipLabel.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//恢复颜色
            }
        });
        contentPanel.add(deleteTipLabel);//删除确认状态
        JRadioButton openDeleteRadioButton = new JRadioButton(SettingState.systemLanguage ? "Open" : "开启提示");//开启提示单选按钮
        JRadioButton closeDeleteRadioButton = new JRadioButton(SettingState.systemLanguage ? "Close" : "关闭提示");//关闭提示单选按钮
        openDeleteRadioButton.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//设置字体颜色
        openDeleteRadioButton.setBackground(SettingState.themeColor ? DARK_DIALOG_MAIN_COLOR : LIGHT_DIALOG_MAIN_COLOR);//设置背景颜色
        openDeleteRadioButton.setFont(new Font("楷体", PLAIN, 20));//设置字体
        closeDeleteRadioButton.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//设置字体颜色
        closeDeleteRadioButton.setBackground(SettingState.themeColor ? DARK_DIALOG_MAIN_COLOR : LIGHT_DIALOG_MAIN_COLOR);//设置背景颜色
        closeDeleteRadioButton.setFont(new Font("楷体", PLAIN, 20));//设置字体
        if (SettingState.deleteTip) {//根据设置状态判断
            closeDeleteRadioButton.setSelected(true);
        } else {
            openDeleteRadioButton.setSelected(true);
        }
        ButtonGroup deleteTipButtonGroup = new ButtonGroup();//删除提示组
        deleteTipButtonGroup.add(openDeleteRadioButton);//组添加开启提示单选按钮
        deleteTipButtonGroup.add(closeDeleteRadioButton);//组添加关闭提示单选按钮
        openDeleteRadioButton.addActionListener(_ -> {//为开启提示单选按钮添加事件监听
            SettingState.deleteTip = false;//更新
        });
        openDeleteRadioButton.addMouseListener(new MouseAdapter() {//为开启提示单选按钮添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow((SettingState.systemLanguage ? "Turn On Remove Image Prompt" : "开启删除图片提示"), openDeleteRadioButton));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
                openDeleteRadioButton.setForeground(Color.RED);//悬浮颜色
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
                openDeleteRadioButton.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//恢复颜色
            }
        });
        closeDeleteRadioButton.addActionListener(_ -> {//为关闭提示单选按钮添加事件监听
            SettingState.deleteTip = true;//更新
        });
        closeDeleteRadioButton.addMouseListener(new MouseAdapter() {//为关闭提示单选按钮添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow((SettingState.systemLanguage ? "Turn Off Remove Image Prompt" : "关闭删除图片提示"), closeDeleteRadioButton));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
                closeDeleteRadioButton.setForeground(Color.RED);//悬浮颜色
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
                closeDeleteRadioButton.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//恢复颜色
            }
        });
        contentPanel.add(openDeleteRadioButton);//面板添加开启提示单选按钮
        contentPanel.add(closeDeleteRadioButton);//面板添加关闭提示单选按钮

        JLabel pictureSuffixLabel = new JLabel(SettingState.systemLanguage ? "File Suffix State: " : "文件后缀状态：");//文件后缀标签
        pictureSuffixLabel.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//设置字体颜色
        pictureSuffixLabel.setFont(new Font("楷体", PLAIN, 20));//设置字体
        pictureSuffixLabel.addMouseListener(new MouseAdapter() {//为文件后缀标签添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow((SettingState.systemLanguage ? "You Can Choose To Turn Thumbnail Suffix On Or Off" : "您可以选择开启或关闭缩略图的后缀"), pictureSuffixLabel));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
                pictureSuffixLabel.setForeground(Color.RED);//悬浮颜色
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
                pictureSuffixLabel.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//恢复颜色
            }
        });
        contentPanel.add(pictureSuffixLabel);//文件后缀状态
        JRadioButton openPictureSuffixRadioButton = new JRadioButton(SettingState.systemLanguage ? "Open" : "开启后缀");//开启文件后缀单选按钮
        JRadioButton closePictureSuffixRadioButton = new JRadioButton(SettingState.systemLanguage ? "Close" : "关闭后缀");//关闭文件后缀单选按钮
        openPictureSuffixRadioButton.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//设置字体颜色
        openPictureSuffixRadioButton.setBackground(SettingState.themeColor ? DARK_DIALOG_MAIN_COLOR : LIGHT_DIALOG_MAIN_COLOR);//设置背景颜色
        openPictureSuffixRadioButton.setFont(new Font("楷体", PLAIN, 20));//设置字体
        closePictureSuffixRadioButton.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//设置字体颜色
        closePictureSuffixRadioButton.setBackground(SettingState.themeColor ? DARK_DIALOG_MAIN_COLOR : LIGHT_DIALOG_MAIN_COLOR);//设置背景颜色
        closePictureSuffixRadioButton.setFont(new Font("楷体", PLAIN, 20));//设置字体
        if (SettingState.pictureSuffix) {//根据设置状态判断
            closePictureSuffixRadioButton.setSelected(true);
        } else {
            openPictureSuffixRadioButton.setSelected(true);
        }
        ButtonGroup pictureSuffixButtonGroup = new ButtonGroup();//文件后缀提示组
        pictureSuffixButtonGroup.add(openPictureSuffixRadioButton);//组添加开启文件后缀单选按钮
        pictureSuffixButtonGroup.add(closePictureSuffixRadioButton);//组添加关闭文件后缀单选按钮
        openPictureSuffixRadioButton.addActionListener(_ -> {//为开启文件后缀单选按钮添加事件监听
            SettingState.pictureSuffix = false;//更新
            updateFileDisplayMainPanel(false);//更新
        });
        openPictureSuffixRadioButton.addMouseListener(new MouseAdapter() {//为开启文件后缀单选按钮添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow((SettingState.systemLanguage ? "Turn On Picture Suffix Tip" : "开启文件后缀提示"), openPictureSuffixRadioButton));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
                openPictureSuffixRadioButton.setForeground(Color.RED);//悬浮颜色
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
                openPictureSuffixRadioButton.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//恢复颜色
            }
        });
        closePictureSuffixRadioButton.addActionListener(_ -> {//为关闭文件后缀单选按钮添加事件监听
            SettingState.pictureSuffix = true;//更新
            updateFileDisplayMainPanel(false);//更新
        });
        closePictureSuffixRadioButton.addMouseListener(new MouseAdapter() {//为关闭文件后缀单选按钮添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow((SettingState.systemLanguage ? "Turn Off Picture Suffix Tip" : "关闭文件后缀提示"), closePictureSuffixRadioButton));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
                closePictureSuffixRadioButton.setForeground(Color.RED);//悬浮颜色
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
                closePictureSuffixRadioButton.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//恢复颜色
            }
        });
        contentPanel.add(openPictureSuffixRadioButton);//面板添加开启文件后缀单选按钮
        contentPanel.add(closePictureSuffixRadioButton);//面板添加关闭文件后缀单选按钮

        JLabel renameStrategyLabel = new JLabel(SettingState.systemLanguage ? "Repetitive Rename Strategy: " : "重复命名策略：");//命名策略标签
        renameStrategyLabel.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//设置字体颜色
        renameStrategyLabel.setFont(new Font("楷体", PLAIN, 20));//设置字体
        renameStrategyLabel.addMouseListener(new MouseAdapter() {//为命名策略标签添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow((SettingState.systemLanguage ? "Picture Will Automatically Prefix Or Suffix When Duplicated, You Can Choose Your Preferred Growth Strategy" : "当图片重复时应用会自动给图片增加前缀或后缀，您可以选择您喜欢的增加策略"), renameStrategyLabel));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
                renameStrategyLabel.setForeground(Color.RED);//悬浮颜色
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
                renameStrategyLabel.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//恢复颜色
            }
        });
        contentPanel.add(renameStrategyLabel);//重复命名策略
        JRadioButton englishPrefixRadioButton = new JRadioButton(SettingState.systemLanguage ? "English Prefix" : "英文前缀");//英文前缀单选按钮
        JRadioButton digitSuffixRadioButton = new JRadioButton(SettingState.systemLanguage ? "Digit Suffix" : "数字后缀");//数字后缀单选按钮
        englishPrefixRadioButton.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//设置字体颜色
        englishPrefixRadioButton.setBackground(SettingState.themeColor ? DARK_DIALOG_MAIN_COLOR : LIGHT_DIALOG_MAIN_COLOR);//设置背景颜色
        englishPrefixRadioButton.setFont(new Font("楷体", PLAIN, 20));//设置字体
        digitSuffixRadioButton.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//设置字体颜色
        digitSuffixRadioButton.setBackground(SettingState.themeColor ? DARK_DIALOG_MAIN_COLOR : LIGHT_DIALOG_MAIN_COLOR);//设置背景颜色
        digitSuffixRadioButton.setFont(new Font("楷体", PLAIN, 20));//设置字体
        if (SettingState.renameStrategy) {//根据设置状态判断
            digitSuffixRadioButton.setSelected(true);
        } else {
            englishPrefixRadioButton.setSelected(true);
        }
        ButtonGroup renameStrategyButtonGroup = new ButtonGroup();//命名策略组
        renameStrategyButtonGroup.add(englishPrefixRadioButton);//组添加英文前缀单选按钮
        renameStrategyButtonGroup.add(digitSuffixRadioButton);//组添加数字后缀单选按钮
        englishPrefixRadioButton.addActionListener(_ -> {//为英文前缀单选按钮添加事件监听
            SettingState.renameStrategy = false;//更新
        });
        englishPrefixRadioButton.addMouseListener(new MouseAdapter() {//为英文前缀单选按钮添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow((SettingState.systemLanguage ? "Add Prefix \"NewName number\" To Image Name When Duplicated" : "设置图片重名时为图片名称添加前缀“NewName+数字”"), englishPrefixRadioButton));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
                englishPrefixRadioButton.setForeground(Color.RED);//悬浮颜色
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
                englishPrefixRadioButton.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//恢复颜色
            }
        });
        digitSuffixRadioButton.addActionListener(_ -> {//为数字后缀单选按钮添加事件监听
            SettingState.renameStrategy = true;//更新
        });
        digitSuffixRadioButton.addMouseListener(new MouseAdapter() {//为数字后缀单选按钮添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow((SettingState.systemLanguage ? "Add Suffix \"(number)\" Yo Image Name When Duplicated" : "设置图片重名时为图片名称添加后缀“(数字)”"), digitSuffixRadioButton));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
                digitSuffixRadioButton.setForeground(Color.RED);//悬浮颜色
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
                digitSuffixRadioButton.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//恢复颜色
            }
        });
        contentPanel.add(englishPrefixRadioButton);//面板添加英文前缀单选按钮
        contentPanel.add(digitSuffixRadioButton);//面板添加数字后缀单选按钮

        JLabel searchStrategyLabel = new JLabel(SettingState.systemLanguage ? "Search Letter Strategy: " : "搜索字母策略：");//搜索策略标签
        searchStrategyLabel.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//设置字体颜色
        searchStrategyLabel.setFont(new Font("楷体", PLAIN, 20));//设置字体
        searchStrategyLabel.addMouseListener(new MouseAdapter() {//为搜索策略标签添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow((SettingState.systemLanguage ? "" : "选择在搜索图片文件时是否忽略大小写"), searchStrategyLabel));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
                searchStrategyLabel.setForeground(Color.RED);//悬浮颜色
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
                searchStrategyLabel.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//恢复颜色
            }
        });
        contentPanel.add(searchStrategyLabel);//搜索字母策略
        JRadioButton openIgnoreRadioButton = new JRadioButton(SettingState.systemLanguage ? "Open Ignore" : "开启忽略");//开启忽略单选按钮
        JRadioButton closeIgnoreRadioButton = new JRadioButton(SettingState.systemLanguage ? "Close Ignore" : "关闭忽略");//关闭忽略单选按钮
        openIgnoreRadioButton.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//设置字体颜色
        openIgnoreRadioButton.setBackground(SettingState.themeColor ? DARK_DIALOG_MAIN_COLOR : LIGHT_DIALOG_MAIN_COLOR);//设置背景颜色
        openIgnoreRadioButton.setFont(new Font("楷体", PLAIN, 20));//设置字体
        closeIgnoreRadioButton.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//设置字体颜色
        closeIgnoreRadioButton.setBackground(SettingState.themeColor ? DARK_DIALOG_MAIN_COLOR : LIGHT_DIALOG_MAIN_COLOR);//设置背景颜色
        closeIgnoreRadioButton.setFont(new Font("楷体", PLAIN, 20));//设置字体
        if (SettingState.searchStrategy) {//根据设置状态判断
            closeIgnoreRadioButton.setSelected(true);
        } else {
            openIgnoreRadioButton.setSelected(true);
        }
        ButtonGroup searchStrategyButtonGroup = new ButtonGroup();//搜索策略组
        searchStrategyButtonGroup.add(openIgnoreRadioButton);//组添加开启忽略单选按钮
        searchStrategyButtonGroup.add(closeIgnoreRadioButton);//组添加关闭忽略单选按钮
        openIgnoreRadioButton.addActionListener(_ -> {//为开启忽略单选按钮添加事件监听
            SettingState.searchStrategy = false;//更新
        });
        openIgnoreRadioButton.addMouseListener(new MouseAdapter() {//为开启忽略单选按钮添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow((SettingState.systemLanguage ? "Open Ignore Case" : "开启大小写忽略"), openIgnoreRadioButton));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
                openIgnoreRadioButton.setForeground(Color.RED);//悬浮颜色
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
                openIgnoreRadioButton.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//恢复颜色
            }
        });
        closeIgnoreRadioButton.addActionListener(_ -> {//为关闭忽略单选按钮添加事件监听
            SettingState.searchStrategy = true;//更新
        });
        closeIgnoreRadioButton.addMouseListener(new MouseAdapter() {//为关闭忽略单选按钮添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow((SettingState.systemLanguage ? "Close Ignore Case" : "关闭大小写忽略"), closeIgnoreRadioButton));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
                closeIgnoreRadioButton.setForeground(Color.RED);//悬浮颜色
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
                closeIgnoreRadioButton.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//恢复颜色
            }
        });
        contentPanel.add(openIgnoreRadioButton);//面板添加开启忽略单选按钮
        contentPanel.add(closeIgnoreRadioButton);//面板添加关闭忽略单选按钮

        JLabel cleanTimeLabel = new JLabel(SettingState.systemLanguage ? "Recycle Bin Clean Time: " : "回收清理时间：");//清理时间标签
        cleanTimeLabel.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//设置字体颜色
        cleanTimeLabel.setFont(new Font("楷体", PLAIN, 20));//设置字体
        cleanTimeLabel.addMouseListener(new MouseAdapter() {//为清理时间标签添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow((SettingState.systemLanguage ? "When Remove Picture, It Will Not Remove Directly, But Will Put Into Picture Recycle Bin, You Can Set Automatic Clean Time And Whether To Turn Off Clean" : "当图片删除图片后，不会直接把图片删除，而是会放入图片回收站中，您可以设置图片回收站自动清理时间和是否关闭清理"), cleanTimeLabel));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
                cleanTimeLabel.setForeground(Color.RED);//悬浮颜色
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
                cleanTimeLabel.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//恢复颜色
            }
        });
        contentPanel.add(cleanTimeLabel);//回收清理时间
        JTextField customCleanTimeText = new JTextField(String.valueOf(SettingState.customRecycleCleanTime));//自定义清理时间文本域
        customCleanTimeText.setFont(new Font("楷体", PLAIN, 20));//设置字体
        JRadioButton closeRecycleCleanRadioButton = new JRadioButton(SettingState.systemLanguage ? "Close Clean" : "关闭清理");//关闭清理单选按钮
        closeRecycleCleanRadioButton.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//设置字体颜色
        closeRecycleCleanRadioButton.setBackground(SettingState.themeColor ? DARK_DIALOG_MAIN_COLOR : LIGHT_DIALOG_MAIN_COLOR);//设置背景颜色
        closeRecycleCleanRadioButton.setFont(new Font("楷体", PLAIN, 20));//设置字体
        if (SettingState.recycleStrategy) {//根据设置状态判断
            closeRecycleCleanRadioButton.setSelected(true);
        }
        customCleanTimeText.addKeyListener(new KeyAdapter() {//为自定义清理时间文本域添加键盘监听
            @Override
            public void keyPressed(KeyEvent e) {//如果键盘按下
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {//如果按下回车
                    String inputText = customCleanTimeText.getText();//获取输入文本
                    boolean legalFlag = true;//合法输入标志
                    if (inputText.isEmpty()) {//如果输入为空
                        customCleanTimeText.setText(String.valueOf(SettingState.customRecycleCleanTime));//返回原先数据
                        createBottomTipWindow(SettingState.systemLanguage ? "No Empty Input" : "输入不可为空");//提示
                        legalFlag = false;//非法
                    } else {//否则不为空
                        if (inputText.length() > 5) {//如果输入过多
                            customCleanTimeText.setText(String.valueOf(SettingState.customRecycleCleanTime));//返回原先数据
                            createBottomTipWindow(SettingState.systemLanguage ? "Do Not Enter Too Much" : "请勿输入过多");//提示
                            return;//直接返回
                        }
                        for (int i = 0; i < inputText.length(); i++) {//遍历输入文本
                            if (inputText.charAt(i) == '-' && i == 0) {//如果输入负数
                                customCleanTimeText.setText(String.valueOf(SettingState.customRecycleCleanTime));//返回原先数据
                                createBottomTipWindow(SettingState.systemLanguage ? "No Negative" : "请勿输入负数");//提示
                                legalFlag = false;//非法
                                break;//直接结束
                            } else if (!Character.isDigit(inputText.charAt(i))) {//如果不是数字
                                customCleanTimeText.setText(String.valueOf(SettingState.customRecycleCleanTime));//返回原先数据
                                createBottomTipWindow(SettingState.systemLanguage ? "No Illegal Character" : "请勿输入非法字符");//提示
                                legalFlag = false;//非法
                                break;//直接结束
                            }
                        }
                    }
                    if (legalFlag) {//如果合法
                        int value = Integer.parseInt(inputText);//获取数值
                        if (value > 10000) {//如果数值过大
                            customCleanTimeText.setText(String.valueOf(SettingState.customRecycleCleanTime));//返回原先数据
                            createBottomTipWindow(SettingState.systemLanguage ? "Do Not Enter Too Large" : "请勿输入过大的数");//提示
                            return;//直接返回
                        }
                        SettingState.recycleStrategy = false;//更新
                        closeRecycleCleanRadioButton.setSelected(false);//取消选中
                        SettingState.customRecycleCleanTime = value;//存储
                        settingDialog.requestFocusInWindow();//返回聚焦
                    }
                    e.consume();//阻止默认行为
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {//如果按下ESC
                    settingDialog.requestFocusInWindow();//返回聚焦
                    e.consume();//阻止默认行为
                }
            }
        });
        customCleanTimeText.addFocusListener(new FocusAdapter() {//为自定义清理时间文本域添加聚焦监听
            @Override
            public void focusLost(FocusEvent e) {//如果失去聚焦
                String inputText = customCleanTimeText.getText();//获取输入文本
                boolean legalFlag = true;//合法输入标志
                if (inputText.isEmpty()) {//如果输入为空
                    customCleanTimeText.setText(String.valueOf(SettingState.customRecycleCleanTime));//返回原先数据
                    createBottomTipWindow(SettingState.systemLanguage ? "No Empty Input" : "输入不可为空");//提示
                    legalFlag = false;//非法
                } else {//否则不为空
                    if (inputText.length() > 5) {//如果输入过多
                        customCleanTimeText.setText(String.valueOf(SettingState.customRecycleCleanTime));//返回原先数据
                        createBottomTipWindow(SettingState.systemLanguage ? "Do Not Enter Too Much" : "请勿输入过多");//提示
                        return;//直接返回
                    }
                    for (int i = 0; i < inputText.length(); i++) {//遍历输入文本
                        if (inputText.charAt(i) == '-' && i == 0) {//如果输入负数
                            customCleanTimeText.setText(String.valueOf(SettingState.customRecycleCleanTime));//返回原先数据
                            createBottomTipWindow(SettingState.systemLanguage ? "No Negative" : "请勿输入负数");//提示
                            legalFlag = false;//非法
                            break;//直接结束
                        } else if (!Character.isDigit(inputText.charAt(i))) {//如果不是数字
                            customCleanTimeText.setText(String.valueOf(SettingState.customRecycleCleanTime));//返回原先数据
                            createBottomTipWindow(SettingState.systemLanguage ? "No Illegal Character" : "请勿输入非法字符");//提示
                            legalFlag = false;//非法
                            break;//直接结束
                        }
                    }
                }
                if (legalFlag) {//如果合法
                    int value = Integer.parseInt(inputText);//获取数值
                    if (value > 10000) {//如果数值过大
                        customCleanTimeText.setText(String.valueOf(SettingState.customRecycleCleanTime));//返回原先数据
                        createBottomTipWindow(SettingState.systemLanguage ? "Do Not Enter Too Large" : "请勿输入过大的数");//提示
                        return;//直接返回
                    }
                    if (value == 0) {//如果立即清理
                        FileDisplayPopupMenu.handleAutoEmptyRecycleBin();//调用自动清空图片回收站
                    }
                    SettingState.recycleStrategy = false;//更新
                    closeRecycleCleanRadioButton.setSelected(false);//取消选中
                    SettingState.customRecycleCleanTime = value;//存储
                    settingDialog.requestFocusInWindow();//返回聚焦
                }
            }
        });
        customCleanTimeText.addMouseListener(new MouseAdapter() {//为自定义清理时间文本域添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow((SettingState.systemLanguage ? "Set Number Of Day After Picture Is Remove, Please Do Not Enter Negative Or Illegal Character, Picture Will Remove Directly When Input Is 0" : "设置图片删除后多少天清理，请不要输入负数或非法字符，当输入为0时将直接删除图片"), customCleanTimeText));//展示提示窗口（鼠标悬浮一秒后展示）
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
        closeRecycleCleanRadioButton.addActionListener(_ -> {//为关闭清理单选按钮添加事件监听
            if (closeRecycleCleanRadioButton.isSelected()) {//如果被选中
                SettingState.recycleStrategy = true;//更新
                createBottomTipWindow(SettingState.systemLanguage ? "Please Note That Close Automatic Clean Of Picture Recycle Bin May Take Up Plenty Of Disk Space, Carefully" : "请注意，关闭图片回收站自动清理可能会占用大量磁盘空间，请谨慎关闭");//提示
            } else {//否则
                SettingState.recycleStrategy = false;//更新
            }
        });
        closeRecycleCleanRadioButton.addMouseListener(new MouseAdapter() {//为关闭清理单选按钮添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow((SettingState.systemLanguage ? "Set Not Clean Picture Recycle Bin, Please Turn On This Item With Caution, This Will Tremendously Take Up Plenty Of Disk Space, And May Trigger Freeze Even Crash" : "设置不清理图片回收站，请谨慎打开此项，这可能会占用大量磁盘空间"), closeRecycleCleanRadioButton));//展示提示窗口（鼠标悬浮一秒后展示）
                hoverTimer.setRepeats(false);//设置计时器不重复
                hoverTimer.start();//开始计时
                closeRecycleCleanRadioButton.setForeground(Color.RED);//悬浮颜色
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
                closeRecycleCleanRadioButton.setForeground(SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//恢复颜色
            }
        });
        contentPanel.add(customCleanTimeText);//面板添加自定义清理时间文本域
        contentPanel.add(closeRecycleCleanRadioButton);//面板添加关闭清理单选按钮

        JButton aboutButton = new JButton(SettingState.systemLanguage ? "About Us" : "关于我们");//关于我们按钮
        aboutButton.setBackground(SETTING_BUTTON_COLOR);//设置背景颜色
        aboutButton.setFont(new Font("楷体", PLAIN, 20));//设置字体
        aboutButton.addActionListener(_ -> {//为关于我们按钮添加事件监听
            JTextArea aboutTextArea = new JTextArea();//关于文本域
            aboutTextArea.setLineWrap(true);//自动换行
            aboutTextArea.setEditable(false);//设置不可编辑
            aboutTextArea.setFocusable(false);//设置不可聚焦
            aboutTextArea.setWrapStyleWord(true);//整个单词换行
            aboutTextArea.setBackground(LIGHT_DIALOG_MAIN_COLOR);//设置背景颜色
            aboutTextArea.setFont(new Font("楷体", PLAIN, 15));//设置字体
            if (SettingState.systemLanguage) {//英文版本
                aboutTextArea.setPreferredSize(new Dimension(825, 325));//设置大小
                aboutTextArea.setText("""
                        The application is a Java course design for Class 5 Computer Science and Technology Class 2023 of SCAU
                        Author: sing Jiahao、Liang Yongli、Li Guanyu   Version: 1.0.0   Issue Date: 2025 May 10th
                        Shortcut Key List:
                        Thumbnail:
                        F11 : Toggle Fullscreen   ESC : Exit Fullscreen   Ctrl + ← : Retreat   Ctrl + → : Advance
                        Ctrl + ↑ : Upper Layer   F5 : Refresh  Ctrl + L / F4 : Directory   Ctrl + A : Select All
                        Ctrl + X : Cut   Ctrl + C : Copy   Ctrl + V : Paste   F2 : Rename   Delete : Remove
                        Ctrl + F / F3 : Search For Picture   Ctrl + Z : Undo   Ctrl + Y : Redo   Ctrl + D : Open Slide
                        Ctrl + U : Open User   Ctrl +  P : Upload To Cloud   Ctrl + S : Open Setting   Enter : Open In Other
                        Ctrl + Enter : Open In Explorer   Ctrl + O : Open Recycle Bin   Ctrl + E : Empty Recycle Bin
                        Ctrl + Shift + C : Get Path   Ctrl + B : Background   Ctrl + Q : Lockscreen   Ctrl + W : Wallpaper
                        Ctrl + Shift + M : Switch Volume   Ctrl + Shift + P : Switch Music   Ctrl + Shift + E : Switch Effect
                        Slide:
                        F11 : Toggle Fullscreen   ESC : Exit Fullscreen   F : Toggle Scroll Pane Strategy
                        Ctrl + ← / Ctrl + ↑ : Go To First Picture   ← / ↑ : Previous Picture
                        → / ↓ : Next Picture   Ctrl + → / Ctrl + ↓ : Go To Last Picture
                        Ctrl + R : Anti Clock Wise Rotate Picture   Ctrl + T : Clock Wise Rotate Picture
                        Space : Auto Play   Ctrl + 1 : Zoom To Actual Size   Ctrl + 0 : Zoom To Adapt
                        Ctrl + - : Shrink Picture   Ctrl + + : Magnify Picture   Ctrl + T ：Input Zoom Ratio
                        """);//设置文本
            } else {//中文版本
                aboutTextArea.setPreferredSize(new Dimension(685, 377));//设置大小
                aboutTextArea.setText("""
                        该应用为华南农业大学2023届计算机科学与技术5班Java课程设计作品
                        作者：幸嘉豪、梁永立、黎冠煜   版本：1.0.0   发行时间：2025年5月10日
                        快捷键一览：
                        缩略图：
                        F11 ：切换全屏   ESC ：退出全屏   Ctrl + ← ：后退   Ctrl + → ：前进
                        Ctrl + ↑ ：上移   Ctrl + R / F5 ：刷新   Ctrl + L / F4 ：路径
                        Ctrl + A ：全选   Ctrl + X ：剪切   Ctrl + C ：复制   Ctrl + V ：粘贴
                        F2 ：重命名   Delete ：删除   Ctrl + F / F3 ：搜索图片
                        Ctrl + Z ：撤销   Ctrl + Y ：恢复   Ctrl + D ：打开幻灯片
                        Ctrl + U ：打开用户   Ctrl + P ：上传云盘   Ctrl + S ：打开设置
                        Enter ：在其他软件中打开图片   Ctrl + Enter ：在资源管理器中打开图片
                        Ctrl + O ：打开图片回收站   Ctrl + E ：清空图片回收站   Ctrl + Shift + C ：获取路径
                        Ctrl + B ：将图片设为背景   Ctrl + Q ：将图片设为锁屏   Ctrl + W ：将图片设为壁纸
                        Ctrl + Shift + M ：开关音量   Ctrl + Shift + P ：开关音乐   Ctrl + Shift + E ：开关音效
                        幻灯片：
                        F11 ：切换全屏   ESC ：退出全屏   F ：切换滚动栏策略
                        Ctrl + ← / Ctrl + ↑ ：转至第一张图片   ← / ↑ ：上一张图片
                        → / ↓ ：下一张图片   Ctrl + → / Ctrl + ↓ ：转至最后一张图片
                        Ctrl + R ：逆时针旋转图片   Ctrl + T ：顺时针旋转图片
                        Space ：自动播放   Ctrl + 1 ：缩放到实际大小   Ctrl + 0 ：缩放以适应
                        Ctrl + - ：缩小图片   Ctrl + + ：放大图片   Ctrl + T ：输入缩放比例
                        """);//设置文本
            }
            JDialog aboutDialog = new JDialog(settingDialog, SettingState.systemLanguage ? "About" : "关于", true);//创建关于对话窗口
            aboutDialog.setIconImage(new ImageIcon("src/material/image/about.png").getImage());//设置图标
            aboutDialog.add(aboutTextArea);//添加关于文本域
            aboutDialog.setAlwaysOnTop(SettingState.windowState);//设置永远在最上层
            aboutDialog.pack();//设置合适
            aboutDialog.setLocation(screenSize.width / 2 - aboutDialog.getWidth() / 2, screenSize.height / 2 - aboutDialog.getHeight() / 2);//设置位置
            aboutDialog.setVisible(true);//设置可见
            JRootPane aboutDialogRoot = aboutDialog.getRootPane();//获取关于窗口的根
            aboutDialogRoot.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "closeAboutDialog");//为根设置窗口关闭ESC按键绑定
            aboutDialogRoot.getActionMap().put("closeAboutDialog", new AbstractAction() {//当ESC按键执行时
                public void actionPerformed(ActionEvent event) {//行为执行
                    aboutDialog.dispatchEvent(new WindowEvent(aboutDialog, WindowEvent.WINDOW_CLOSING));//关闭窗口
                }
            });
        });
        aboutButton.addMouseListener(new MouseAdapter() {//为关于我们按钮添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow((SettingState.systemLanguage ? "Detailed Information About Software And List Of Shortcut" : "关于本软件的详情信息和快捷键一览"), aboutButton));//展示提示窗口（鼠标悬浮一秒后展示）
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
        contentPanel.add(aboutButton);//面板添加关于我们按钮

        JButton tutorialButton = new JButton(SettingState.systemLanguage ? "Tutorial" : "使用教程");//使用教程按钮
        tutorialButton.setBackground(SETTING_BUTTON_COLOR);//设置背景颜色
        tutorialButton.setFont(new Font("楷体", PLAIN, 20));//设置字体
        tutorialButton.addActionListener(_ -> {//为使用教程按钮添加事件监听
            settingDialog.setVisible(false);//关闭设置
            handleUtilizeTutorial();//处理使用教程
        });
        tutorialButton.addMouseListener(new MouseAdapter() {//为使用教程按钮添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow((SettingState.systemLanguage ? "If You Still Not Clear About Basic Software Usage, You Can Review Tutorial" : "如果您仍对该软件的基础使用方法尚不明了，可以重新观看教程"), tutorialButton));//展示提示窗口（鼠标悬浮一秒后展示）
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
        contentPanel.add(tutorialButton);//面板添加使用教程按钮

        JButton errorLogButton = new JButton(SettingState.systemLanguage ? "Error Log" : "错误日志");//错误日志按钮
        errorLogButton.setBackground(SETTING_BUTTON_COLOR);//设置背景颜色
        errorLogButton.setFont(new Font("楷体", PLAIN, 20));//设置字体
        errorLogButton.addActionListener(_ -> {//为错误日志按钮添加事件监听
            Path errorLogPath = Path.of(String.valueOf(spikeVisionCloudPath), "appErrorLog.txt");//获取错误日志路径
            try {
                if (!Files.exists(errorLogPath)) {//如果不存在错误日志路径
                    Files.createFile(errorLogPath);//就创建路径
                }
            } catch (IOException e) {
                throw new RuntimeException(e);//捕获异常
            }
            try {
                settingDialog.setVisible(false);//关闭
                Desktop.getDesktop().open(errorLogPath.toFile());//打开错误日志
            } catch (IOException e) {
                throw new RuntimeException(e);//捕获异常
            }
        });
        errorLogButton.addMouseListener(new MouseAdapter() {//为错误日志按钮添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow((SettingState.systemLanguage ? "Open Error Log" : "打开错误日志"), errorLogButton));//展示提示窗口（鼠标悬浮一秒后展示）
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
        contentPanel.add(errorLogButton);//面板添加错误日志按钮

        JButton suggestionFeedbackButton = new JButton(SettingState.systemLanguage ? "Suggestion Feedback" : "建议反馈");//建议反馈按钮
        suggestionFeedbackButton.setBackground(SETTING_BUTTON_COLOR);//设置背景颜色
        suggestionFeedbackButton.setFont(new Font("楷体", PLAIN, 20));//设置字体
        suggestionFeedbackButton.addActionListener(_ -> {//为插入图片按钮添加事件监听
            handleSuggestionFeedback();//处理建议反馈
        });
        suggestionFeedbackButton.addMouseListener(new MouseAdapter() {//为插入图片按钮添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow((SettingState.systemLanguage ? "We Would Love To Hear Any Valuable Suggestion Or Feedback You May Have About The Software" : "我们非常欢迎您对该软件的任何宝贵建议或反馈"), suggestionFeedbackButton));//展示提示窗口（鼠标悬浮一秒后展示）
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
        contentPanel.add(suggestionFeedbackButton);//面板添加建议反馈按钮

        JButton resetSettingButton = new JButton(SettingState.systemLanguage ? "Reset Setting" : "重置设置");//重置设置按钮
        resetSettingButton.setBackground(SETTING_BUTTON_COLOR);//设置背景颜色
        resetSettingButton.setFont(new Font("楷体", PLAIN, 20));//设置字体
        resetSettingButton.addActionListener(_ -> {//为插入图片按钮添加事件监听
            int confirm = JOptionPane.showConfirmDialog(settingDialog, (SettingState.systemLanguage ? "Are You Sure To Reset Setting State?" : "确定要重置设置吗？"), SettingState.systemLanguage ? "Reset Setting Confirm" : "重置设置确认", JOptionPane.YES_NO_OPTION);//创建确认信息
            if (confirm == JOptionPane.YES_OPTION) {//如果确认
                SettingState.windowState = false;
                SettingState.systemLanguage = false;
                SettingState.themeColor = false;
                SettingState.dbclickBehavior = false;
                SettingState.hoverTip = false;
                SettingState.deleteTip = false;
                SettingState.pictureSuffix = false;
                SettingState.renameStrategy = false;
                SettingState.searchStrategy = false;
                SettingState.customRecycleCleanTime = 30;
                SettingState.recycleStrategy = false;
                SettingState.masterVolume = 88;
                SettingState.masterState = false;
                SettingState.bgmVolume = 0;
                SettingState.bgmState = false;
                SettingState.effectVolume = 0;
                SettingState.effectState = false;

                if (bgm1GainControl != null && bgm2GainControl != null && bgm3GainControl != null && bgm4GainControl != null && bgm5GainControl != null) {//如果音频控制非空
                    bgm1GainControl.setValue(((-bgm1MinGain) * ((float) 88 / 100)) + bgm1MinGain);//设置音频控制音量值
                    bgm2GainControl.setValue(((-bgm1MinGain) * ((float) 88 / 100)) + bgm1MinGain);//设置音频控制音量值
                    bgm3GainControl.setValue(((-bgm1MinGain) * ((float) 88 / 100)) + bgm1MinGain);//设置音频控制音量值
                    bgm4GainControl.setValue(((-bgm1MinGain) * ((float) 88 / 100)) + bgm1MinGain);//设置音频控制音量值
                    bgm5GainControl.setValue(((-bgm1MinGain) * ((float) 88 / 100)) + bgm1MinGain);//设置音频控制音量值
                }
                if (removeTipEffectGainControl != null && volumeAdjustEffectGainControl != null && switchPictureEffectGainControl != null) {//如果音频控制非空
                    removeTipEffectGainControl.setValue((-removeTipEffectMinGain) * 88 / 100 + removeTipEffectMinGain);//设置音频控制音量值
                    volumeAdjustEffectGainControl.setValue((-removeTipEffectMinGain) * 88 / 100 + removeTipEffectMinGain);//设置音频控制音量值
                    switchPictureEffectGainControl.setValue((-removeTipEffectMinGain) * 88 / 100 + removeTipEffectMinGain);//设置音频控制音量值
                }
                switchBGM();//切换BGM

                GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(null);//取消窗口全屏
                diskManagementSystemFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);//设置窗口直接最大化
                progressWindow.setAlwaysOnTop(false);//设置不永远在最上层

                settingDialog.dispose();//释放
                switchSystemLanguage();//切换系统语言
                settingDialog.setVisible(true);//可见
            }
        });
        resetSettingButton.addMouseListener(new MouseAdapter() {//为插入图片按钮添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow((SettingState.systemLanguage ? "Reset Setting To Initial Value (Not Contain Picture Directory)" : "重置设置为初始值（不包括图片路径）"), resetSettingButton));//展示提示窗口（鼠标悬浮一秒后展示）
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
        contentPanel.add(resetSettingButton);//面板添加重置设置按钮

        settingDialog = new JDialog(diskManagementSystemFrame, SettingState.systemLanguage ? "Setting" : "设置", true);//创建设置对话窗口
        settingDialog.setIconImage(new ImageIcon("src/material/image/dialogSetting.png").getImage());//设置图标
        settingDialog.setLayout(new BorderLayout());//设置布局
        settingDialog.add(contentPanel, BorderLayout.CENTER);//把内容面板添加到中心
        settingDialog.setAlwaysOnTop(SettingState.windowState);//设置永远在最上层
        settingDialog.pack();//设置合适
        settingDialog.setVisible(false);//设置不可见
        settingDialog.setLocation(screenSize.width / 2 - settingDialog.getWidth() / 2, screenSize.height / 2 - settingDialog.getHeight() / 2);//设置位置
        JRootPane settingDialogRoot = settingDialog.getRootPane();//获取设置窗口的根
        InputMap inputMap = settingDialogRoot.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);//获取输入映射
        ActionMap actionMap = settingDialogRoot.getActionMap();//获取行动映射
        bindKey(inputMap, actionMap, KeyEvent.VK_M, CTRL_DOWN_MASK + SHIFT_DOWN_MASK, "muteMaster");//静音总音量
        bindKey(inputMap, actionMap, KeyEvent.VK_P, CTRL_DOWN_MASK + SHIFT_DOWN_MASK, "muteMusic");//静音音乐
        bindKey(inputMap, actionMap, KeyEvent.VK_E, CTRL_DOWN_MASK + SHIFT_DOWN_MASK, "muteSoundEffect");//静音音乐
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "closeSettingDialog");//为根设置窗口关闭ESC按键绑定
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK), "closeSettingDialog");//为根设置窗口关闭Ctrl+S按键绑定
        actionMap.put("closeSettingDialog", new AbstractAction() {//当ESC按键执行时
            public void actionPerformed(ActionEvent event) {//行为执行
                settingDialog.dispatchEvent(new WindowEvent(settingDialog, WindowEvent.WINDOW_CLOSING));//关闭窗口
            }
        });
        settingDialog.addWindowListener(new WindowAdapter() {//为设置窗口添加窗口监听
            private static boolean reopenSettingsDialog = false;//判断是否需要重新打开窗口

            @Override
            public void windowDeactivated(WindowEvent e) {//如果窗口没有激活
                if (reopenSettingsDialog) {//如果需要重新打开窗口
                    reopenSettingsDialog = false;//已重新打开
                    settingDialog.setVisible(true);//重新打开
                }
            }
        });
    }
}
