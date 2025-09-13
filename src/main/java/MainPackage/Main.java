package MainPackage;

import DirectoryPackage.DirectoryTree;
import FileDisplayPackage.FileDisplayBottomBar;
import FileDisplayPackage.FileDisplayMainPanel;
import FileDisplayPackage.FileDisplayPopupMenu;
import FileDisplayPackage.FileDisplayTopBar;
import FileEditPackage.FileEditPanel;
import FileEditPackage.FileEditScrollPane;
import FileEditPackage.FileEditToolBar;
import NetworkPackage.User;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Objects;

import static DirectoryPackage.DirectoryTree.*;
import static FileDisplayPackage.FileDisplayBottomBar.bottomBarPanel;
import static FileDisplayPackage.FileDisplayMainPanel.*;
import static FileDisplayPackage.FileDisplayTopBar.*;
import static FileEditPackage.FileEditPanel.*;
import static FileEditPackage.FileEditScrollPane.scrollPane;
import static FileEditPackage.FileEditToolBar.toolBarPanel;
import static MainPackage.Setting.handleErrorLog;
import static MainPackage.ThemeColor.*;

public class Main {//主类 TODO 加载FAT 文件属性面板 文件编辑面板 创建文件和目录
    public static DirectoryTree directoryTree;//目录树类
    public static FileDisplayTopBar fileDisplayTopBar;//文件展示顶部栏类
    public static FileDisplayMainPanel fileDisplayMainPanel;//文件展示主面板类
    public static FileDisplayBottomBar fileDisplayBottomBar;//文件展示底部栏类
    public static FileDisplayPopupMenu fileDisplayPopupMenu;//文件展示右键弹出菜单类
    public static FileEditPanel fileEditPanel;//幻灯片图片面板类
    public static FileEditScrollPane fileEditScrollPane;//幻灯片滚动栏类
    public static FileEditToolBar fileEditToolBar;//幻灯片工具栏类

    public static JScrollPane directoryTreeScrollPane;//目录树滚动条
    public static JPanel fileDisplayTopBarPanel;//文件展示顶部栏面板
    public static JScrollPane fileDisplayMainPanelScrollPane;//文件展示主面板滚动条
    public static JPanel fileDisplayBottomBarPanel;//文件展示底部栏面板
    public static JPanel fileDisplayPanel;//文件展示面板
    public static JSplitPane panelSplitPane;//面板分割
    public static JFrame diskManagementSystemFrame;//磁盘管理系统窗口
    public static JFrame editFrame;//幻灯片窗口

    public static final JPanel bottomTipInformationPanel = new JPanel();//底部提示信息面板
    public static final JTextArea bottomTipInformation = new JTextArea();//底部提示信息文本域
    public static final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();//当前电脑分辨率
    public static final Path spikeVisionCloudPath = Path.of(System.getProperty("user.home"), "SpikeVisionCloud");//应用文件夹路径

    public record SettingState() {//设置状态记录类
        public static String userAccount;//用户账号（false为默认，true则相反，下同）
        public static String userPhone;//用户手机号
        public static String userPassword;//用户密码
        public static long userAvailableCloudCapacity;//用户云盘可用容量（以字节为单位，需要long存储）
        public static long recentSuggestionFeedbackTime;//最近用户反馈时间
        public static boolean windowState;//窗口状态
        public static boolean systemLanguage;//系统语言
        public static boolean themeColor;//主题颜色
        public static boolean dbclickBehavior;//双击行为
        public static boolean hoverTip;//悬浮提示
        public static boolean deleteTip;//删除提示
        public static boolean pictureSuffix;//图片后缀
        public static boolean renameStrategy;//命名策略
        public static boolean searchStrategy;//搜索策略
        public static int customRecycleCleanTime;//自定义回收清理时间
        public static boolean recycleStrategy;//回收策略
        public static int masterVolume;//总体音量
        public static boolean masterState;//总体状态
        public static int bgmVolume;//背景音乐音量
        public static boolean bgmState;//背景音乐状态
        public static int effectVolume;//音效音量
        public static boolean effectState;//音效状态
        public static int utilizeTimes;//使用次数
        public static HashMap<String, String> settingStateHashMap = new HashMap<>();//设置状态哈希表（用于映射JSON存档文件）
    }

    public static JTextArea getBottomTipInformation() {//获取提示信息（供PicturePreviewMainPanel调用）
        return bottomTipInformation;
    }

    public static void setBottomTipInformation(String information) {//设置提示信息（供PicturePreviewMainPanel调用）
        bottomTipInformation.setText(information);
    }

    public static void initSettingState() {//初始化设置状态
        try {
            if (!Files.exists(spikeVisionCloudPath)) {//如果不存在应用文件夹路径
                Files.createDirectory(spikeVisionCloudPath);//就创建路径
            }
            Path settingStateSave = Path.of(String.valueOf(spikeVisionCloudPath), "appSettingState.enc");//获取自定义设置状态存档路径（文件格式为json，但经过加密变成enc文件）
            if (!Files.exists(settingStateSave)) {//如果不存在自定义设置状态存档
                SettingState.settingStateHashMap.put("userAccount", "");//初始化存档，默认状态是选中第一个选项
                SettingState.settingStateHashMap.put("userPhone", "");
                SettingState.settingStateHashMap.put("userPassword", "");
                SettingState.settingStateHashMap.put("userAvailableCloudCapacity", "0");//初始支持的字节数：0
                SettingState.settingStateHashMap.put("recentSuggestionFeedbackTime", "0");
                SettingState.settingStateHashMap.put("windowState", "false");
                SettingState.settingStateHashMap.put("systemLanguage", "false");
                SettingState.settingStateHashMap.put("themeColor", "false");
                SettingState.settingStateHashMap.put("dbclickBehavior", "false");
                SettingState.settingStateHashMap.put("hoverTip", "false");
                SettingState.settingStateHashMap.put("deleteTip", "false");
                SettingState.settingStateHashMap.put("effectTip", "false");
                SettingState.settingStateHashMap.put("pictureSuffix", "false");
                SettingState.settingStateHashMap.put("renameStrategy", "false");
                SettingState.settingStateHashMap.put("searchStrategy", "false");
                SettingState.settingStateHashMap.put("masterVolume", "88");
                SettingState.settingStateHashMap.put("masterState", "false");
                SettingState.settingStateHashMap.put("bgmVolume", "0");
                SettingState.settingStateHashMap.put("bgmState", "false");
                SettingState.settingStateHashMap.put("effectVolume", "0");
                SettingState.settingStateHashMap.put("effectState", "false");
                SettingState.settingStateHashMap.put("utilizeTimes", "0");

                File jsonFile = Files.createFile(settingStateSave).toFile();//就创建JSON存档并创建JSON文件
                JSONObject json = new JSONObject(SettingState.settingStateHashMap);//根据设置状态哈希表初始化JSON文件
                String encryptData = EncryptionUtil.encryptData(json.toString(), EncryptionUtil.ENCRYPTION_PASSWORD);//把JSON文件转化成字符串并通过密钥加密
                Files.write(settingStateSave, encryptData.getBytes());//将加密后的数据写入存档
                jsonFile.setReadOnly();//设置只读
                SettingState.userAccount = "";//再初始化数据
                SettingState.userPhone = "";
                SettingState.userPassword = "";
                SettingState.userAvailableCloudCapacity = 0;
                SettingState.recentSuggestionFeedbackTime = 0;
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
                SettingState.utilizeTimes = 1;
            } else {//否则存在自定义设置状态存档
                String encryptedData = new String(Files.readAllBytes(settingStateSave));//读取加密数据
                String jsonString = EncryptionUtil.decryptData(encryptedData, EncryptionUtil.ENCRYPTION_PASSWORD);//将加密数据通过密钥解密
                JSONObject json = new JSONObject(jsonString);//根据解密文件创建JSON文件

                SettingState.userAccount = json.getString("userAccount");//获取json文件数据
                SettingState.userPhone = json.getString("userPhone");
                SettingState.userPassword = json.getString("userPassword");
                SettingState.userAvailableCloudCapacity = json.getLong("userAvailableCloudCapacity");
                SettingState.recentSuggestionFeedbackTime = json.getLong("recentSuggestionFeedbackTime");
                SettingState.windowState = json.getBoolean("windowState");
                SettingState.systemLanguage = json.getBoolean("systemLanguage");
                SettingState.themeColor = json.getBoolean("themeColor");
                SettingState.dbclickBehavior = json.getBoolean("dbclickBehavior");
                SettingState.hoverTip = json.getBoolean("hoverTip");
                SettingState.deleteTip = json.getBoolean("deleteTip");
                SettingState.pictureSuffix = json.getBoolean("pictureSuffix");
                SettingState.renameStrategy = json.getBoolean("renameStrategy");
                SettingState.searchStrategy = json.getBoolean("searchStrategy");
                SettingState.customRecycleCleanTime = json.getInt("customRecycleCleanTime");
                SettingState.recycleStrategy = json.getBoolean("recycleStrategy");
                SettingState.masterVolume = json.getInt("masterVolume");
                SettingState.masterState = json.getBoolean("masterState");
                SettingState.bgmVolume = json.getInt("bgmVolume");
                SettingState.bgmState = json.getBoolean("bgmState");
                SettingState.effectVolume = json.getInt("effectVolume");
                SettingState.effectState = json.getBoolean("effectState");
                SettingState.utilizeTimes = json.getInt("utilizeTimes") + 1;//使用次数+1
                System.out.println(SettingState.utilizeTimes);
            }
        } catch (Exception e) {
            handleErrorLog(e.getMessage());//处理错误日志
            throw new RuntimeException(e);//捕获异常
        }
    }

    public static void initMain() {//初始化主类
        directoryTree = new DirectoryTree();//创建目录树类，只在main中创建变量，其余时候用类名调用方法（需要全部设为static，防止创建多个类，数据发生丢失）
        fileDisplayMainPanel = new FileDisplayMainPanel();//创建图片预览主面板
        fileDisplayTopBar = new FileDisplayTopBar();//创建图片预览顶部栏
        fileDisplayBottomBar = new FileDisplayBottomBar();//创建图片预览底部栏
        fileDisplayPopupMenu = new FileDisplayPopupMenu();//创建图片预览右键弹出菜单
        fileEditPanel = new FileEditPanel();//创建幻灯片图片面板
        fileEditScrollPane = new FileEditScrollPane();//创建幻灯片滚动栏
        fileEditToolBar = new FileEditToolBar();//创建幻灯片工具栏

        directoryTreeScrollPane = new JScrollPane(DirectoryTree.directoryTree);//为目录树面板添加滚动条
        fileDisplayTopBarPanel = topBarPanel;//获取顶部栏面板（固定高度）
        fileDisplayMainPanelScrollPane = new JScrollPane(mainPanel) {//创建主面板滚动栏
            @Override
            protected void paintComponent(Graphics g) {//重写绘制方法
                super.paintComponent(g);//调用父类方法绘制清除背景
                setForeground(Color.blue);//设置前景色
            }
        };
        fileDisplayBottomBarPanel = bottomBarPanel;//获取底部栏面板（固定高度）
        fileDisplayPanel = new JPanel(new BorderLayout());//创建图片预览面板，使用BorderLayout布局管理器
        panelSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, directoryTreeScrollPane, fileDisplayPanel);//创建目录树滚动条和图片预览面板之间的分割
        diskManagementSystemFrame = new JFrame(SettingState.systemLanguage ? "Disk Management System" : "磁盘管理系统");//创建磁盘管理系统窗口
        editFrame = new JFrame();//创建幻灯片窗口

        directoryTreeScrollPane.getVerticalScrollBar().setUnitIncrement(25);//设置垂直滚动条滚动长度
        directoryTreeScrollPane.getHorizontalScrollBar().setUnitIncrement(5);//设置水平滚动条滚动长度

        fileDisplayMainPanelScrollPane.getVerticalScrollBar().setUnitIncrement(70);//设置主面板垂直滚动条单词滚动长度
        fileDisplayMainPanelScrollPane.getHorizontalScrollBar().setUnitIncrement(20);//设置主面板水平滚动条单词滚动长度

        fileDisplayPanel.add(fileDisplayTopBarPanel, BorderLayout.NORTH);//顶部栏位于北部
        fileDisplayPanel.add(fileDisplayMainPanelScrollPane, BorderLayout.CENTER);//主面板位于中部
        fileDisplayPanel.add(fileDisplayBottomBarPanel, BorderLayout.SOUTH);//底部栏位于南部

        panelSplitPane.setDividerSize(5);//设置分割条厚度
        panelSplitPane.setContinuousLayout(true);//设置分割条可连续分割
        panelSplitPane.setDividerLocation(300);//设置分割条位置

        bottomTipInformation.setFocusable(false);//设置提示信息字体不可选中
        bottomTipInformation.setFont(new Font("微软雅黑", Font.PLAIN, 17));//设置提示信息字体
        bottomTipInformation.setForeground(Color.RED);//设置前景色：即提示信息字体颜色
        bottomTipInformation.setBackground(SettingState.themeColor ? DARK_TIP_IMFORMATION_COLOR : LIGHT_TIP_IMFORMATION_COLOR);//设置提示信息字体背景颜色
        bottomTipInformation.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));//设置提示信息鼠标为默认光标（不会变成字体编辑光标）

        bottomTipInformationPanel.add(bottomTipInformation);//添加提示信息到提示信息面板中
        bottomTipInformationPanel.setBackground(SettingState.themeColor ? DARK_TIP_IMFORMATION_COLOR : LIGHT_TIP_IMFORMATION_COLOR);//设置提示信息面板背景颜色

        directoryTreeScrollPane.setBackground(SettingState.themeColor ? DARK_DIRECTORY_MAIN_COLOR : LIGHT_DIRECTORY_MAIN_COLOR);//设置背景
        directoryTreeScrollPane.setOpaque(true);//设置不透明
        directoryTreeScrollPane.getViewport().setBackground(SettingState.themeColor ? DARK_DIRECTORY_MAIN_COLOR : LIGHT_DIRECTORY_MAIN_COLOR);//设置背景
        directoryTreeScrollPane.getViewport().setOpaque(true);//设置不透明
        fileDisplayBottomBarPanel.setBackground(SettingState.themeColor ? DARK_PICTURE_BAR_COLOR : LIGHT_PICTURE_BAR_COLOR);//设置背景
        fileDisplayBottomBarPanel.setOpaque(true);//设置不透明
        fileDisplayMainPanelScrollPane.setBackground(SettingState.themeColor ? DARK_PICTURE_MAIN_COLOR : LIGHT_PICTURE_MAIN_COLOR);//设置背景
        fileDisplayMainPanelScrollPane.setOpaque(true);//设置不透明
        fileDisplayMainPanelScrollPane.getViewport().setBackground(SettingState.themeColor ? DARK_PICTURE_MAIN_COLOR : LIGHT_PICTURE_MAIN_COLOR);//设置背景
        fileDisplayMainPanelScrollPane.getViewport().setOpaque(true);//设置不透明
        fileDisplayTopBarPanel.setBackground(SettingState.themeColor ? DARK_PICTURE_BAR_COLOR : LIGHT_PICTURE_BAR_COLOR);//设置背景
        fileDisplayTopBarPanel.setOpaque(true);//设置不透明
        fileDisplayPanel.setBackground(SettingState.themeColor ? DARK_PICTURE_MAIN_COLOR : LIGHT_PICTURE_MAIN_COLOR);//设置背景
        fileDisplayPanel.setOpaque(true);//设置不透明
        panelSplitPane.setBackground(SettingState.themeColor ? DARK_PICTURE_MAIN_COLOR : LIGHT_PICTURE_MAIN_COLOR);//设置背景
        panelSplitPane.setOpaque(true);//设置不透明

        diskManagementSystemFrame.add(panelSplitPane);//将分割条放入面板中
        diskManagementSystemFrame.add(bottomTipInformationPanel, BorderLayout.SOUTH);//把提示信息放到面板南部
        diskManagementSystemFrame.setSize(screenSize.width, screenSize.height);//设置窗口大小为当前电脑分辨率
        diskManagementSystemFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);//设置窗口关闭行为
        diskManagementSystemFrame.setAutoRequestFocus(true);//自动请求焦点
        try {
            diskManagementSystemFrame.setIconImage(ImageIO.read(new File("src/material/image/pictureManagementSystem.png")));//设置窗口图标
        } catch (IOException e) {
            handleErrorLog(e.getMessage());//处理错误日志
            throw new RuntimeException(e);//捕获异常
        }
        if (SettingState.windowState) {//如果全屏
            GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(diskManagementSystemFrame);//设置窗口全屏
        } else {//否则
            diskManagementSystemFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);//设置窗口直接最大化
        }
        diskManagementSystemFrame.setVisible(true);//设置窗口可见
        JRootPane pictureManagementSystemFrameRoot = diskManagementSystemFrame.getRootPane();//获取窗口的根
        pictureManagementSystemFrameRoot.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "closeFullscreen");//为根设置关闭窗口全屏ESC按键绑定
        pictureManagementSystemFrameRoot.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0), "toggleFullscreen");//为根设置切换窗口全屏F11按键绑定
        pictureManagementSystemFrameRoot.getActionMap().put("closeFullscreen", new AbstractAction() {//当ESC执行时
            public void actionPerformed(ActionEvent event) {//行为执行
                if (isCutOperation) {//如果是剪切操作
                    isCutOperation = false;//不再剪切
                    clipboardFiles.clear();//清空剪贴板
                    pasteButton.setEnabled(false);//不可粘贴
                    fileManipulationButtonEnableJudgement(selectionThumbnailItemList.size());//按钮判断
                    refreshMainPanel();//刷新
                } else if (SettingState.windowState) {//如果全屏
                    GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(null);//取消窗口全屏
                    diskManagementSystemFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);//设置窗口直接最大化
                    SettingState.windowState = false;//更新
                    Setting.displayTitleRadioButton.setSelected(true);//选中
                    Setting.settingDialog.setAlwaysOnTop(false);//设置不永远在最上层
                    progressWindow.setAlwaysOnTop(false);//设置不永远在最上层
                    if (SettingState.userAccount.isEmpty()) {//如果没有登录
                        User.logInDialog.setAlwaysOnTop(false);//设置不永远在最上层
                        User.registerDialog.setAlwaysOnTop(false);//设置不永远在最上层
                    } else {//否则
                        User.userDialog.setAlwaysOnTop(false);//设置不永远在最上层
                    }
                    picturePanel.setPreferredSize(new Dimension(Main.screenSize.width, PANEL_DEFAULT_HEIGHT));//设置大小
                }
            }
        });
        pictureManagementSystemFrameRoot.getActionMap().put("toggleFullscreen", new AbstractAction() {//当F11按键执行时
            public void actionPerformed(ActionEvent event) {//行为执行
                if (SettingState.windowState) {//如果全屏
                    GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(null);//取消窗口全屏
                    diskManagementSystemFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);//设置窗口直接最大化
                    SettingState.windowState = false;//更新
                    Setting.displayTitleRadioButton.setSelected(true);//选中
                    Setting.settingDialog.setAlwaysOnTop(false);//设置不永远在最上层
                    progressWindow.setAlwaysOnTop(false);//设置不永远在最上层
                    if (SettingState.userAccount.isEmpty()) {//如果没有登录
                        User.logInDialog.setAlwaysOnTop(false);//设置不永远在最上层
                        User.registerDialog.setAlwaysOnTop(false);//设置不永远在最上层
                    } else {//否则
                        User.userDialog.setAlwaysOnTop(false);//设置不永远在最上层
                    }
                    picturePanel.setPreferredSize(new Dimension(Main.screenSize.width, PANEL_DEFAULT_HEIGHT));//设置大小
                } else {//否则
                    GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(diskManagementSystemFrame);//设置窗口全屏
                    SettingState.windowState = true;//更新
                    createBottomTipWindow(SettingState.systemLanguage ? "Press ESC Or F11 To Exit Fullscreen" : "按下 ESC 或 F11 即可退出全屏");//提示
                    Setting.hideTitleRadioButton.setSelected(true);//选中
                    Setting.settingDialog.setAlwaysOnTop(true);//设置永远在最上层
                    progressWindow.setAlwaysOnTop(true);//设置永远在最上层
                    if (SettingState.userAccount.isEmpty()) {//如果没有登录
                        User.logInDialog.setAlwaysOnTop(true);//设置永远在最上层
                        User.registerDialog.setAlwaysOnTop(true);//设置永远在最上层
                    } else {//否则
                        User.userDialog.setAlwaysOnTop(true);//设置永远在最上层
                    }
                    picturePanel.setPreferredSize(new Dimension(Main.screenSize.width, PANEL_FULLSCREEN_HEIGHT));//设置大小
                }
            }
        });
        diskManagementSystemFrame.addWindowFocusListener(new WindowFocusListener() {//如果发生窗口聚焦事件
            @Override
            public void windowGainedFocus(WindowEvent e) {//如果窗口获得聚焦
                if (Setting.tutorialStep != 0) {//如果有教程
                    Setting.tutorialWindow.setVisible(true);//让窗口可见
                }

                if (isNotEdit) {//如果不是播放幻灯片
                    try {
                        Robot robot = new Robot();//创建Robot对象切换输入法
                        robot.keyPress(KeyEvent.VK_CONTROL);//按下ctrl
                        robot.delay(20);//延迟20ms
                        robot.keyPress(KeyEvent.VK_SPACE);//按下空格
                        robot.delay(20);//延迟20ms
                        robot.keyRelease(KeyEvent.VK_CONTROL);//松开ctrl
                        robot.delay(20);//延迟20ms
                        robot.keyRelease(KeyEvent.VK_SPACE);//松开空格
                    } catch (Exception ex) {
                        handleErrorLog(ex.getMessage());//处理错误日志
                        ex.printStackTrace();//捕获异常
                    }

                    if (!loading) {//如果不在加载中
                        if (currentFolder != null) {//如果当前文件夹不为空
                            if (!Objects.equals(currentFolder, SettingState.systemLanguage ? "My Cloud" : "我的云盘")) {//如果不是云盘结点
                                File[] currentPictureFileList = fileListSortProcess(detectFile(new File(currentFolder).listFiles()));//获取当前图片文件列表
                                boolean flag = false;//是否刷新判断
                                if (currentPictureFileList.length != FileDisplayMainPanel.currentFileList.length) {//如果长度不一致
                                    flag = true;//需要刷新
                                } else {//否则
                                    for (int i = 0; i < currentPictureFileList.length; i++) {//遍历目录树图片文件列表
                                        if (!currentPictureFileList[i].getAbsolutePath().equals(FileDisplayMainPanel.currentFileList[i].getAbsolutePath())) {//如果文件发生变化
                                            flag = true;//需要刷新
                                            break;//直接退出
                                        }
                                    }
                                }
                                if (flag) {//如果需要刷新
                                    DirectoryTree.updateCurrentFileList(currentPictureFileList);//更新文件夹
                                    updateFileDisplayMainPanel(false);//通知更新
                                    fileManipulationButtonEnableJudgement(getSelectionThumbnailItemList().size());//文件操作按钮判断
                                    directoryManipulationButtonEnableJudgement();//目录操作按钮判断
                                    FileDisplayBottomBar.historyManipulationButtonEnableJudgement();//历史操作按钮判断
                                }
                            }
                        }
                    }
                } else {//否则是播放幻灯片
                    isNotEdit = true;//停止播放幻灯片
                }
            }

            @Override
            public void windowLostFocus(WindowEvent e) {//如果窗口失去聚焦
                if (Setting.tutorialWindow.isVisible()) {//如果窗口可见
                    Setting.tutorialWindow.setVisible(false);//让窗口不可见
                }
                if (itemHoverTipWindow != null && itemHoverTipWindow.isVisible()) {//如果提示信息不为空
                    itemHoverTipWindow.dispose();//释放提示信息
                    itemHoverTipWindow = null;//提示信息置空
                }
            }
        });
        diskManagementSystemFrame.addWindowListener(new WindowAdapter() {//如果发生窗口事件
            @Override
            public void windowClosing(WindowEvent e) {//如果窗口正在关闭
                try {
                    SettingState.settingStateHashMap.clear();//清空
                    SettingState.settingStateHashMap.put("userAccount", SettingState.userAccount);//更新数据
                    SettingState.settingStateHashMap.put("userPhone", SettingState.userPhone);
                    SettingState.settingStateHashMap.put("userPassword", SettingState.userPassword);
                    SettingState.settingStateHashMap.put("userAvailableCloudCapacity", String.valueOf(SettingState.userAvailableCloudCapacity));
                    SettingState.settingStateHashMap.put("recentSuggestionFeedbackTime", String.valueOf(SettingState.recentSuggestionFeedbackTime));
                    SettingState.settingStateHashMap.put("windowState", String.valueOf(SettingState.windowState));
                    SettingState.settingStateHashMap.put("systemLanguage", String.valueOf(SettingState.systemLanguage));
                    SettingState.settingStateHashMap.put("themeColor", String.valueOf(SettingState.themeColor));
                    SettingState.settingStateHashMap.put("dbclickBehavior", String.valueOf(SettingState.dbclickBehavior));
                    SettingState.settingStateHashMap.put("hoverTip", String.valueOf(SettingState.hoverTip));
                    SettingState.settingStateHashMap.put("deleteTip", String.valueOf(SettingState.deleteTip));
                    SettingState.settingStateHashMap.put("pictureSuffix", String.valueOf(SettingState.pictureSuffix));
                    SettingState.settingStateHashMap.put("renameStrategy", String.valueOf(SettingState.renameStrategy));
                    SettingState.settingStateHashMap.put("searchStrategy", String.valueOf(SettingState.searchStrategy));
                    SettingState.settingStateHashMap.put("customRecycleCleanTime", String.valueOf(SettingState.customRecycleCleanTime));
                    SettingState.settingStateHashMap.put("recycleStrategy", String.valueOf(SettingState.recycleStrategy));
                    SettingState.settingStateHashMap.put("masterVolume", String.valueOf(SettingState.masterVolume));
                    SettingState.settingStateHashMap.put("masterState", String.valueOf(SettingState.masterState));
                    SettingState.settingStateHashMap.put("bgmVolume", String.valueOf(SettingState.bgmVolume));
                    SettingState.settingStateHashMap.put("bgmState", String.valueOf(SettingState.bgmState));
                    SettingState.settingStateHashMap.put("effectVolume", String.valueOf(SettingState.effectVolume));
                    SettingState.settingStateHashMap.put("effectState", String.valueOf(SettingState.effectState));
                    SettingState.settingStateHashMap.put("utilizeTimes", String.valueOf(SettingState.utilizeTimes));
//                    try {
//                        User.handleUserUploadUserSettingSate();//上传用户设置状态
//                    } catch (Exception ex) {
//                        ex.printStackTrace();//捕获异常
//                    }
                    try {
                        Path settingStateSave = Path.of(String.valueOf(spikeVisionCloudPath), "appSettingState.enc");//获取自定义设置状态存档路径
                        File jsonFile = new File(String.valueOf(settingStateSave));
                        jsonFile.setWritable(true);//设置可写
                        JSONObject json = new JSONObject(SettingState.settingStateHashMap);//将设置状态哈希表转换为JSON文件
                        String encryptedData = EncryptionUtil.encryptData(json.toString(), EncryptionUtil.ENCRYPTION_PASSWORD);//把JSON文件加密
                        Files.write(settingStateSave, encryptedData.getBytes());//把加密数据写回
                        jsonFile.setReadOnly();//设置只读
                    } catch (Exception ex) {
                        handleErrorLog(ex.getMessage());//处理错误日志
                        ex.printStackTrace();//捕获异常
                        JOptionPane.showMessageDialog(diskManagementSystemFrame, SettingState.systemLanguage ? "Failure Save Configuration" : "保存配置失败", SettingState.systemLanguage ? "Error" : "错误", JOptionPane.ERROR_MESSAGE);//提示用户保存失败
                    }

                    File[] recycleFileList = new File(spikeVisionCloudPath + "/.appRecycleBin").listFiles();//获取所有回收文件
                    if (recycleFileList != null) {//如果回收文件数组不为空
                        long removeGap = 86400000L * SettingState.customRecycleCleanTime;//删除时间间隔
                        for (File file : recycleFileList) {//遍历回收文件数组
                            long removeTime = Long.parseLong(file.getName().substring(21, 34));//获取图片删除时间戳
                            if (System.currentTimeMillis() - removeTime > removeGap) {//如果超过删除时间间隔
                                Files.deleteIfExists(file.toPath());//将回收文件删除
                            }
                        }
                    }

                    File[] bufferFileList = new File(spikeVisionCloudPath + "/.buffer").listFiles();//获取所有缓存文件
                    if (bufferFileList != null) {//如果缓存文件数组不为空
                        for (File file : bufferFileList) {//遍历缓存文件数组
                            Files.deleteIfExists(file.toPath());//将缓存文件删除
                        }
                    }
                } catch (Exception ex) {
                    handleErrorLog(ex.getMessage());//处理错误日志
                    throw new RuntimeException(ex);//捕获异常
                }
            }
        });

        editFrame.setLayout(new BorderLayout());//设置布局
        editFrame.add(picturePanel, BorderLayout.NORTH);//添加到北部
        editFrame.add(scrollPane, BorderLayout.CENTER);//添加到中心
        editFrame.add(toolBarPanel, BorderLayout.SOUTH);//添加到南部
        editFrame.setSize(screenSize.width, screenSize.height);//设置窗口大小为当前电脑分辨率
        editFrame.setAutoRequestFocus(true);//自动请求焦点
        try {
            editFrame.setIconImage(ImageIO.read(new File("src/material/image/slide.png")));//设置窗口图标
        } catch (IOException e) {
            handleErrorLog(e.getMessage());//处理错误日志
            throw new RuntimeException(e);//捕获异常
        }
        editFrame.addWindowFocusListener(new WindowFocusListener() {//如果发生窗口聚焦事件
            @Override
            public void windowGainedFocus(WindowEvent e) {//如果窗口获得聚焦
                try {
                    Robot robot = new Robot();//创建Robot对象切换输入法
                    robot.keyPress(KeyEvent.VK_CONTROL);//按下ctrl
                    robot.delay(20);//延迟20ms
                    robot.keyPress(KeyEvent.VK_SPACE);//按下空格
                    robot.delay(20);//延迟20ms
                    robot.keyRelease(KeyEvent.VK_CONTROL);//松开ctrl
                    robot.delay(20);//延迟20ms
                    robot.keyRelease(KeyEvent.VK_SPACE);//松开空格
                } catch (Exception ex) {
                    handleErrorLog(ex.getMessage());//处理错误日志
                    ex.printStackTrace();//捕获异常
                }
            }

            @Override
            public void windowLostFocus(WindowEvent e) {//被迫重写
            }
        });
        editFrame.addWindowListener(new WindowAdapter() {//为幻灯片窗口添加窗口监听
            @Override
            public void windowClosing(WindowEvent e) {//如果正在关闭
                if (bottomTipWindow != null && bottomTipWindow.isVisible()) {//如果底部提示窗口不为空
                    bottomTipWindow.dispose();//释放底部提示窗口
                }
                if (buttonHoverTipWindow != null && buttonHoverTipWindow.isVisible()) {//如果按钮提示窗口不为空
                    buttonHoverTipWindow.dispose();//释放按钮提示窗口
                }
                if (FileEditScrollPane.itemHoverTipWindow != null && FileEditScrollPane.itemHoverTipWindow.isVisible()) {//如果悬浮提示信息不为空
                    FileEditScrollPane.itemHoverTipWindow.dispose();//释放悬浮提示信息
                }
                diskManagementSystemFrame.setVisible(true);//图片管理系统窗口可见
                if (SettingState.windowState) {//如果全屏
                    GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(diskManagementSystemFrame);//设置窗口全屏
                } else {//否则
                    diskManagementSystemFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);//设置窗口直接最大化
                }
                slideCache.clear();//清空缓存
            }
        });
        JRootPane slideFrameRoot = editFrame.getRootPane();//获取窗口的根
        slideFrameRoot.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "closeFullscreen");//为根设置关闭窗口全屏ESC按键绑定
        slideFrameRoot.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0), "toggleFullscreen");//为根设置切换窗口全屏F11按键绑定
        slideFrameRoot.getActionMap().put("closeFullscreen", new AbstractAction() {//当ESC执行时
            public void actionPerformed(ActionEvent event) {//行为执行
                if (SettingState.windowState) {//如果全屏
                    closeSlideFrameFullscreen();//关闭幻灯片窗口全屏
                }
            }
        });
        slideFrameRoot.getActionMap().put("toggleFullscreen", new AbstractAction() {//当F11按键执行时
            public void actionPerformed(ActionEvent event) {//行为执行
                if (SettingState.windowState) {//如果全屏
                    closeSlideFrameFullscreen();//关闭幻灯片窗口全屏
                } else {//否则
                    openSlideFrameFullscreen();//开启幻灯片窗口全屏
                }
            }
        });

        directoryTreeScrollPane.getVerticalScrollBar().setUI(new CustomScrollPane());//设置滚动条UI
        directoryTreeScrollPane.getHorizontalScrollBar().setUI(new CustomScrollPane());//设置滚动条UI
        fileDisplayMainPanelScrollPane.getVerticalScrollBar().setUI(new CustomScrollPane());//设置滚动条UI
        fileDisplayMainPanelScrollPane.getHorizontalScrollBar().setUI(new CustomScrollPane());//设置滚动条UI

        try {
            Robot robot = new Robot();//创建Robot对象切换输入法
            robot.keyPress(KeyEvent.VK_CONTROL);//按下ctrl
            robot.delay(20);//延迟20ms
            robot.keyPress(KeyEvent.VK_SPACE);//按下空格
            robot.delay(20);//延迟20ms
            robot.keyRelease(KeyEvent.VK_CONTROL);//松开ctrl
            robot.delay(20);//延迟20ms
            robot.keyRelease(KeyEvent.VK_SPACE);//松开空格
        } catch (Exception e) {
            handleErrorLog(e.getMessage());//处理错误日志
            e.printStackTrace();//捕获异常
        }

        if (SettingState.utilizeTimes == 1) {//如果用户第一次使用
            JOptionPane.showMessageDialog(diskManagementSystemFrame, "感谢您使用该软件，我们将对本软件进行基础的教程介绍\nThank You For Using This Software, We Will Give Basic Tutorial Introduction", "欢迎 Welcome", JOptionPane.INFORMATION_MESSAGE);//展示提示信息
            Setting.handleUtilizeTutorial();//开启使用教程
        }
    }

    public static void closeSlideFrameFullscreen() {//关闭幻灯片窗口全屏
        GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(null);//取消窗口全屏
        editFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);//设置窗口直接最大化
        SettingState.windowState = false;//更新
        Setting.displayTitleRadioButton.setSelected(true);//选中
        Setting.settingDialog.setAlwaysOnTop(false);//设置不永远在最上层
        progressWindow.setAlwaysOnTop(false);//设置不永远在最上层
        if (SettingState.userAccount.isEmpty()) {//如果没有登录
            User.logInDialog.setAlwaysOnTop(false);//设置不永远在最上层
            User.registerDialog.setAlwaysOnTop(false);//设置不永远在最上层
        } else {//否则
            User.userDialog.setAlwaysOnTop(false);//设置不永远在最上层
        }
        if (!FileEditToolBar.isScrollPaneHide) {//如果滚动栏没有隐藏
            Main.editFrame.add(scrollPane);//添加滚动栏
            imageHeight -= 118;//更新图片高度
        }
        editFrame.add(toolBarPanel, BorderLayout.SOUTH);//添加工具栏面板
        picturePanel.setPreferredSize(new Dimension(Main.screenSize.width, FileEditToolBar.isScrollPaneHide ? PANEL_DEFAULT_HEIGHT + 118 : PANEL_DEFAULT_HEIGHT));//设置大小
        imageHeight -= 57;//更新图片高度
        updatePicturePanel();//更新图片面板
        Main.editFrame.revalidate();//重新验证布局
        Main.editFrame.repaint();//重新绘制
    }

    public static void openSlideFrameFullscreen() {//开启幻灯片窗口全屏
        GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(editFrame);//设置窗口全屏
        SettingState.windowState = true;//更新
        createBottomTipWindow(SettingState.systemLanguage ? "Press ESC Or F11 To Exit Fullscreen" : "按下 ESC 或 F11 即可退出全屏");//提示
        Setting.hideTitleRadioButton.setSelected(true);//选中
        Setting.settingDialog.setAlwaysOnTop(true);//设置永远在最上层
        progressWindow.setAlwaysOnTop(true);//设置永远在最上层
        if (SettingState.userAccount.isEmpty()) {//如果没有登录
            User.logInDialog.setAlwaysOnTop(true);//设置永远在最上层
            User.registerDialog.setAlwaysOnTop(true);//设置永远在最上层
        } else {//否则
            User.userDialog.setAlwaysOnTop(true);//设置永远在最上层
        }
        if (!FileEditToolBar.isScrollPaneHide) {//如果滚动栏没有隐藏
            Main.editFrame.remove(scrollPane);//移除滚动栏
            imageHeight += 118;//更新图片高度
        }
        editFrame.remove(toolBarPanel);//移除工具栏面板
        picturePanel.setPreferredSize(new Dimension(Main.screenSize.width, PANEL_FULLSCREEN_HEIGHT));//设置大小
        imageHeight += 57;//更新图片高度
        updatePicturePanel();//更新图片面板
        Main.editFrame.revalidate();//重新验证布局
        Main.editFrame.repaint();//重新绘制
    }

    public static class BackgroundImagePanel extends JPanel {//自定义背景图片面板
        private static BufferedImage originalImage;//背景图片
        private static float alpha;//不透明度

        public BackgroundImagePanel(BufferedImage image, float alpha) {//构造方法
            BackgroundImagePanel.originalImage = image;
            BackgroundImagePanel.alpha = alpha;
            setOpaque(false);//设置透明背景
        }

        public void setAlpha(float alpha) {//设置不透明度
            BackgroundImagePanel.alpha = alpha;
        }

        @Override
        protected void paintComponent(Graphics g) {//重写绘制方法
            super.paintComponent(g);//调用父类绘制方法清除背景
            if (originalImage == null) return;//如果图片为空，返回
            Graphics2D g2d = (Graphics2D) g.create();//创建工具类
            int screenWidth = (int) screenSize.getWidth();//获取屏幕宽度
            int screenHeight = (int) screenSize.getHeight();//获取屏幕高度
            int imageWidth = originalImage.getWidth(null);//获取图片原始宽度
            int imageHeight = originalImage.getHeight(null);//获取图片原始高度

            int targetWidth, targetHeight, x = 0, y = 0;//目标图片宽度和高度，目标图片左上角绘制x和y坐标
            if ((double) imageWidth / imageHeight > (double) screenWidth / screenHeight) {//如果图片宽高比比屏幕大：图片高度填充屏幕高度，图片宽度多余舍弃
                targetHeight = screenHeight;//目标高度为屏幕高度
                targetWidth = (int) (imageWidth * (targetHeight / (double) imageHeight));//图片更宽，按高度缩放，宽度多余部分裁剪
                x = (targetWidth - screenWidth) / 2;//目标图片水平居中
            } else {//否则图片宽高比比屏幕小：图片宽度填充屏幕宽度，图片高度多余舍弃
                targetWidth = screenWidth;//目标宽度为屏幕宽度
                targetHeight = (int) (imageHeight * (targetWidth / (double) imageWidth));//图片更窄，按宽度缩放，高度多余部分裁剪
                y = (targetHeight - screenHeight) / 2;//目标图片垂直居中
            }
            try {
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));//应用透明度
                g2d.drawImage(originalImage, -x, -y, targetWidth, targetHeight, 0, 0, imageWidth, imageHeight, null);//绘制缩放后的图片（裁剪多余部分）：裁剪后的起始坐标为x和y（负数表示向左/上偏移）
            } finally {//最终
                g2d.dispose();//释放
            }
        }
    }

    public static class CustomScrollPane extends BasicScrollBarUI {//自定义滚动栏：继承基础滚动栏UI

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {//重写绘制轨道
            boolean isVertical = scrollbar.getOrientation() == SwingConstants.VERTICAL;//判断是否为垂直滚动条
            if (isThumbRollover() || isDragging) {//如果滑块悬停或拖动：显示半透明颜色
                Graphics2D g2d = (Graphics2D) g.create();//创建g2d工具
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f));//设置混合颜色
                g2d.setColor(LIGHT_SCROLL_PANE_TRACK_COLOR);//轨道颜色
                if (isVertical) {//如果是垂直滚动条
                    g2d.fillRect(trackBounds.x, trackBounds.y, 12, trackBounds.height);//填充矩形
                } else {//否则
                    g2d.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, 12);//填充矩形
                }
                g2d.dispose();//释放
            }
            c.setBackground(null);//设置无背景
            c.setOpaque(false);//设置透明
            if (isVertical) {//如果是垂直滚动条
                c.setPreferredSize(new Dimension(12, c.getHeight()));//设置大小
            } else {//否则
                c.setPreferredSize(new Dimension(c.getWidth(), 12));//设置大小
            }
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {//重写绘制滑块
            if (!scrollbar.isEnabled()) {//如果无效
                return;//直接返回
            }
            Graphics2D g2d = (Graphics2D) g.create();//创建g2d工具
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);//设置抗锯齿
            g2d.setColor(isThumbRollover() ? SCROLL_PANE_THUMB_HOVER_COLOR : SCROLL_PANE_THUMB_COLOR);//设置颜色
            if (scrollbar.getOrientation() == SwingConstants.VERTICAL) {//如果是垂直滚动条
                g2d.fillRoundRect(thumbBounds.x, thumbBounds.y, 12, thumbBounds.height, 4, 4);//绘制圆角半径滑块
            } else {//否则
                g2d.fillRoundRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, 12, 4, 4);//绘制圆角半径滑块
            }
            g2d.dispose();//释放
        }

        @Override
        protected void installListeners() {//安装监听
            super.installListeners();//调用父类安装监听
            scrollbar.addMouseListener(new MouseAdapter() {//为滚动栏添加鼠标监听
                @Override
                public void mouseEntered(MouseEvent e) {//如果鼠标进入
                    if (scrollbar != null) {//如果滚动栏不为空
                        scrollbar.repaint();//重绘
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {//如果鼠标离开
                    if (scrollbar != null) {//如果滚动栏不为空
                        scrollbar.repaint();//重绘
                    }
                }
            });
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {//重写创建减少按钮
            JButton button = new JButton();//创建按钮
            button.setPreferredSize(new Dimension(0, 0));//设置大小为空
            button.setVisible(false);//设置不可见
            button.setOpaque(false);//设置透明
            return button;//返回按钮
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {//重写创建增加按钮
            JButton button = new JButton();//创建按钮
            button.setPreferredSize(new Dimension(0, 0));//设置大小为空
            button.setVisible(false);//设置不可见
            button.setOpaque(false);//设置透明
            return button;//返回按钮
        }

        @Override
        protected void configureScrollBarColors() {//重写配置滚动条颜色：空实现以移除默认颜色配置
        }

        @Override
        public Dimension getMinimumThumbSize() {//重写最小滑块大小
            if (scrollbar.getOrientation() == SwingConstants.VERTICAL) {//如果是垂直滚动条
                return new Dimension(12, 8);//返回大小
            } else {//否则
                return new Dimension(8, 12);//返回大小
            }
        }

        @Override
        public Dimension getPreferredSize(JComponent c) {//重写获取首选尺寸
            if (scrollbar.getOrientation() == SwingConstants.VERTICAL) {//如果是垂直滚动条
                return new Dimension(12, scrollbar.getHeight());//返回大小
            } else {//否则
                return new Dimension(scrollbar.getWidth(), 12);//返回大小
            }
        }

        @Override
        public Dimension getMaximumSize(JComponent c) {//重写最大尺寸
            return getPreferredSize(c);//最大尺寸与首选尺寸一致
        }

        @Override
        public Dimension getMinimumSize(JComponent c) {//重写最小尺寸
            return getPreferredSize(c);//最小尺寸与首选尺寸一致
        }
    }

    public static void main(String[] args) {//客户端主方法
        initSettingState();//初始化设置状态
        initMain();//初始化主类
    }
}
