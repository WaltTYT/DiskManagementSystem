package FileDisplayPackage;

import DirectoryPackage.DirectoryTree;
import MainPackage.Main;
import org.apache.commons.io.FilenameUtils;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static DirectoryPackage.DirectoryTree.*;
import static FileDisplayPackage.FileDisplayBottomBar.editButton;
import static FileDisplayPackage.FileDisplayMainPanel.*;
import static FileDisplayPackage.FileDisplayPopupMenu.administratorJudgement;
import static MainPackage.Main.spikeVisionCloudPath;
import static MainPackage.Setting.*;
import static MainPackage.ThemeColor.*;
import static NetworkPackage.User.*;
import static java.awt.Font.PLAIN;

public class FileDisplayTopBar {//文件展示顶部栏
    public static final JPanel topBarPanel = new JPanel();//顶部面板：用于放置按钮等组件
    public static final JButton retreatButton = new JButton();//后退按钮
    public static final JButton advanceButton = new JButton();//前进按钮
    public static final JButton upperLayerButton = new JButton();//上一级文件夹按钮
    public static final JButton refreshButton = new JButton();//刷新按钮
    public static final JButton cutButton = new JButton();//剪切按钮（有选中文件时有效）
    public static final JButton copyButton = new JButton();//复制按钮（有选中文件时有效）
    public static final JButton pasteButton = new JButton();//粘贴按钮（剪贴板有文件时有效）
    public static final JButton renameButton = new JButton();//重命名按钮（选中文件为一时有效）
    public static final JButton removeButton = new JButton();//删除按钮（有选中文件时有效）
    public static JComboBox<String> sortComboBox = new JComboBox<>(new String[]{Main.SettingState.systemLanguage ? "Sort By Name (Ascending Order)" : "按名称排序（升序）", Main.SettingState.systemLanguage ? "Sort By Date (Ascending Order)" : "按日期排序（升序）", Main.SettingState.systemLanguage ? "Sort By Type (Ascending Order)" : "按类型排序（升序）", Main.SettingState.systemLanguage ? "Sort By Size (Ascending Order)" : "按大小排序（升序）"});//排序列表

    public static JWindow buttonHoverTipWindow = null;//按钮悬浮提示窗口
    public static JDialog singleFileRenameDialog = new JDialog(Main.diskManagementSystemFrame, Main.SettingState.systemLanguage ? "Rename" : "重命名", true);//单文件重命名对话窗口
    public static JDialog multipleFileRenameDialog = new JDialog(Main.diskManagementSystemFrame, Main.SettingState.systemLanguage ? "Rename" : "重命名", true);//多文件重命名对话窗口

    public static String currentFolder = null;//当前所在文件夹
    public static int folderListIndex = -1;//进入过的文件夹路径索引：防止循环文件夹出错
    public static List<String> folderList = new ArrayList<>();//进入过的文件夹路径字符串列表
    public static boolean isCutOperation = false;//是否是剪切操作
    public static boolean isCloudOperation = false;//是否是云盘操作
    public static final List<File> clipboardFiles = new ArrayList<>();//剪贴板列表
    public static List<Path> tmpPastePathList = new ArrayList<>();//临时存储粘贴文件的路径列表
    public static Timer hoverTimer = null;//按钮悬浮时间计时器
    public static int sortTypeIndex = 0;//排序方式索引
    public static SortType[] sortTypeList = {SortType.ANAME, SortType.ADATE, SortType.ATYPE, SortType.ASIZE};//排序方式列表

    public static class CustomTextField extends JPanel {//自定义文本框组件：继承面板JPanel
        private String emptyText;//空白时绘制文本
        private final JTextField inputTextField = new JTextField() {//输入文本域
            @Override
            protected void paintComponent(Graphics g) {//重写绘制方法
                super.paintComponent(g);//调用父类重写清除背景
                Graphics2D g2d = (Graphics2D) g;//创建二维绘制工具类
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);//消除画图锯齿
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);//消除文字锯齿
                if (this.getText().isEmpty()) {//如果文本为空
                    g2d.setFont(new Font("楷体", PLAIN, 23));//设置字体
                    g2d.setColor(Color.GRAY);//设置字体颜色
                    g2d.drawString(emptyText, 2, 23);//绘制文字
                }
                g2d.setStroke(new BasicStroke(1.0f));//设置画笔粗细
                g2d.setColor(Color.gray);//设置画笔颜色
                g2d.drawRect(0, 0, getWidth() - 1, getHeight() - 1);//手动绘制边框
                g2d.dispose();//释放
            }
        };
        private final JButton actionButton;//行为按钮

        public CustomTextField(int width, ImageIcon icon, String text) {//构造方法
            emptyText = text;//空白文本
            inputTextField.setSize(width, 34);//设置大小
            inputTextField.setFont(new Font("楷体", PLAIN, 19));//设置字体
            actionButton = new JButton(icon) {//内部按钮配置
                @Override
                public Dimension getPreferredSize() {//重写设置大小
                    return new Dimension(34, 34);//固定按钮尺寸
                }
            };
            actionButton.setFocusable(false);//设置不可聚焦
            setLayout(new BorderLayout(0, 0));//设置布局
            setPreferredSize(new Dimension(width, 34));//设置大小，固定高度与按钮一致
            add(inputTextField, BorderLayout.CENTER);//把文本域放在中心
            add(actionButton, BorderLayout.EAST);//行为按钮放在东部
        }

        public void setEmptyText(String emptyText) {//设置空白文本
            this.emptyText = emptyText;
        }

        public JTextField getInputTextField() {//获取文本域
            return inputTextField;
        }

        public void setTextField() {//设置文本域
            inputTextField.setText(currentFolder);//设置为当前文件夹
        }

        public JButton getActionButton() {//获取行为按钮
            return actionButton;
        }
    }

    public static final CustomTextField directoryField = new CustomTextField(450, new ImageIcon(new ImageIcon("src/material/image/clearText.png").getImage().getScaledInstance(24, 24, Image.SCALE_DEFAULT)), Main.SettingState.systemLanguage ? "Current File Folder Directory" : "当前文件夹路径");//根据自定义组件创建目录文本域
    public static final CustomTextField searchField = new CustomTextField(230, new ImageIcon(new ImageIcon("src/material/image/search.png").getImage().getScaledInstance(24, 24, Image.SCALE_DEFAULT)), Main.SettingState.systemLanguage ? "Search For File" : "搜索文件");//根据自定义组件创建搜索文本域

    public static void setDirectoryField(String currentFolder) {//设置当前文件路径（供DirectoryTree调用）
        directoryField.getInputTextField().setText(currentFolder.substring(3));
    }

    public static void updateFolder(String folder) {//更新当前文件夹和文件夹列表（供DirectoryTree调用）
        currentFolder = folder;//设置当前文件夹为指定文件夹
        folderList.add(folder);//文件夹列表加上新结点
        folderListIndex = folderList.size() - 1;//自增
        directoryManipulationButtonEnableJudgement();//状态更新
    }

    public static void directoryManipulationButtonEnableJudgement() {//前进、后退、上一级文件夹按钮判断
        boolean flag = true;//是否没有到达临界文件夹
        if (folderListIndex >= 1) {//如果大于
            int tmpFolderListIndex = folderListIndex;//临时索引：用于记录当前文件夹索引
            String tmpFolder = folderList.get(tmpFolderListIndex - 1);//临时文件夹：用于记录到达哪个文件夹
            while (Objects.equals(tmpFolder, (Main.SettingState.systemLanguage ? "My Cloud" : "我的云盘"))) {//如果当前是我的云盘结点
                if (Objects.equals(Main.SettingState.userAccount, "")) {//如果退出登录
                    if (tmpFolderListIndex - 1 == 0) {//如果到达第一个文件夹
                        retreatButton.setEnabled(false);//无效
                        flag = false;//到达临界文件夹
                        break;//退出
                    }
                    tmpFolder = folderList.get(--tmpFolderListIndex);//到上一个文件夹
                } else {//否则
                    break;//退出
                }
            }
        }
        retreatButton.setEnabled(flag && !(folderListIndex == 0));//是否到达临界文件夹

        flag = true;//重置
        if (folderListIndex + 1 < folderList.size()) {//如果小于
            int tmpFolderListIndex = folderListIndex;//临时索引：用于记录当前文件夹索引
            String tmpFolder = folderList.get(tmpFolderListIndex + 1);//临时文件夹：用于记录到达哪个文件夹
            while (Objects.equals(tmpFolder, (Main.SettingState.systemLanguage ? "My Cloud" : "我的云盘"))) {//如果当前是我的云盘结点
                if (Objects.equals(Main.SettingState.userAccount, "")) {//如果退出登录
                    if (tmpFolderListIndex + 1 == folderList.size()) {//如果到达最后一个文件夹
                        advanceButton.setEnabled(false);//无效
                        flag = false;//到达临界文件夹
                        break;//退出
                    }
                    tmpFolder = folderList.get(++tmpFolderListIndex);//到下一个文件夹
                } else {//否则
                    break;//退出
                }
            }
        }
        advanceButton.setEnabled(flag && !(folderListIndex == folderList.size() - 1));//是否到达临界文件夹

        if (currentFolder != null) {//如果不为空
            upperLayerButton.setEnabled(new File(currentFolder).getParent() != null);//上一级按钮
        }
    }

    public static void fileManipulationButtonEnableJudgement(int size) {//剪切、复制、粘贴、重命名、删除、获取文件路径、在其他软件中打开、编辑按钮判断
        if (size != 0) {//如果有选中
            cutButton.setEnabled(true);//设置可选
            copyButton.setEnabled(true);
            renameButton.setEnabled(true);
            removeButton.setEnabled(true);
            FileDisplayPopupMenu.cutButton.setEnabled(true);
            FileDisplayPopupMenu.cutButton.setFocusable(true);
            FileDisplayPopupMenu.copyButton.setEnabled(true);
            FileDisplayPopupMenu.renameButton.setEnabled(true);
            FileDisplayPopupMenu.removeButton.setEnabled(true);
            FileDisplayPopupMenu.getPathButton.setEnabled(true);
            FileDisplayPopupMenu.uploadToCloudButton.setEnabled(!Main.SettingState.userAccount.isEmpty());//还要根据用户是否登录设置是否可按下
            if (size == 1 && !selectionThumbnailItemList.getFirst().getFile().isDirectory()) {//如果选中为一个且非目录 TODO
                editButton.setEnabled(true);
            }
        } else {//否则
            cutButton.setEnabled(false);//设置不可选
            copyButton.setEnabled(false);
            renameButton.setEnabled(false);
            removeButton.setEnabled(false);
            FileDisplayBottomBar.uploadToCloudButton.setEnabled(false);
            FileDisplayPopupMenu.cutButton.setEnabled(false);
            FileDisplayPopupMenu.copyButton.setEnabled(false);
            FileDisplayPopupMenu.renameButton.setEnabled(false);
            FileDisplayPopupMenu.removeButton.setEnabled(false);
            FileDisplayPopupMenu.getPathButton.setEnabled(false);
            FileDisplayPopupMenu.uploadToCloudButton.setEnabled(false);
            editButton.setEnabled(false);
        }
    }

    public FileDisplayTopBar() {//构造方法
        retreatButton.setEnabled(false);//初始无效
        advanceButton.setEnabled(false);
        upperLayerButton.setEnabled(false);
        cutButton.setEnabled(false);
        copyButton.setEnabled(false);
        pasteButton.setEnabled(false);
        renameButton.setEnabled(false);
        removeButton.setEnabled(false);

        retreatButton.setSize(24, 24);//设置大小
        advanceButton.setSize(24, 24);
        upperLayerButton.setSize(24, 24);
        refreshButton.setSize(24, 24);
        cutButton.setSize(24, 24);
        copyButton.setSize(24, 24);
        pasteButton.setSize(24, 24);
        renameButton.setSize(24, 24);
        removeButton.setSize(24, 24);
        sortComboBox.setPreferredSize(new Dimension(Main.SettingState.systemLanguage ? 400 : 235, 34));//设置大小

        sortComboBox.setFont(new Font("楷体", PLAIN, 23));

        retreatButton.setIcon(new ImageIcon(new ImageIcon("src/material/image/retreat.png").getImage().getScaledInstance(retreatButton.getWidth(), retreatButton.getHeight(), Image.SCALE_DEFAULT)));//通过getScaledInstance使按钮适应图片大小
        advanceButton.setIcon(new ImageIcon(new ImageIcon("src/material/image/advance.png").getImage().getScaledInstance(advanceButton.getWidth(), advanceButton.getHeight(), Image.SCALE_DEFAULT)));
        upperLayerButton.setIcon(new ImageIcon(new ImageIcon("src/material/image/upper.png").getImage().getScaledInstance(upperLayerButton.getWidth(), upperLayerButton.getHeight(), Image.SCALE_DEFAULT)));
        refreshButton.setIcon(new ImageIcon(new ImageIcon("src/material/image/refresh.png").getImage().getScaledInstance(refreshButton.getWidth(), refreshButton.getHeight(), Image.SCALE_DEFAULT)));
        cutButton.setIcon(new ImageIcon(new ImageIcon("src/material/image/cut.png").getImage().getScaledInstance(cutButton.getWidth(), cutButton.getHeight(), Image.SCALE_DEFAULT)));
        copyButton.setIcon(new ImageIcon(new ImageIcon("src/material/image/copy.png").getImage().getScaledInstance(copyButton.getWidth(), copyButton.getHeight(), Image.SCALE_DEFAULT)));
        pasteButton.setIcon(new ImageIcon(new ImageIcon("src/material/image/paste.png").getImage().getScaledInstance(pasteButton.getWidth(), pasteButton.getHeight(), Image.SCALE_DEFAULT)));
        renameButton.setIcon(new ImageIcon(new ImageIcon("src/material/image/rename.png").getImage().getScaledInstance(renameButton.getWidth(), renameButton.getHeight(), Image.SCALE_DEFAULT)));
        removeButton.setIcon(new ImageIcon(new ImageIcon("src/material/image/remove.png").getImage().getScaledInstance(removeButton.getWidth(), removeButton.getHeight(), Image.SCALE_DEFAULT)));

        configureDirectoryField();//配置路径文本框
        configureSearchField();//配置搜索框

        topBarPanel.add(retreatButton);
        topBarPanel.add(advanceButton);
        topBarPanel.add(upperLayerButton);
        topBarPanel.add(refreshButton);
        topBarPanel.add(directoryField);
        topBarPanel.add(cutButton);
        topBarPanel.add(copyButton);
        topBarPanel.add(pasteButton);
        topBarPanel.add(renameButton);
        topBarPanel.add(removeButton);
        topBarPanel.add(sortComboBox);
        topBarPanel.add(searchField);
        topBarPanel.setLayout(new BarWrapLayout(FlowLayout.LEFT, 0, 0));
        topBarPanel.setBackground(Main.SettingState.themeColor ? DARK_PICTURE_BAR_COLOR : LIGHT_PICTURE_BAR_COLOR);//设置背景颜色
        topBarPanel.setSize(topBarPanel.getLayout().preferredLayoutSize(topBarPanel));//设置大小
        topBarPanel.addComponentListener(new ComponentAdapter() {//为顶部栏面板添加组件监听
            @Override
            public void componentResized(ComponentEvent e) {//如果组件大小变化
                topBarPanel.revalidate();//重新验证布局
                topBarPanel.repaint();//重新绘制
            }
        });

        retreatButton.addMouseListener(new MouseAdapter() {//为后退按钮添加鼠标事件监听
            @Override
            public void mouseClicked(MouseEvent e) {//如果鼠标点击
                if (SwingUtilities.isLeftMouseButton(e)) {//如果是鼠标左键
                    handleRetreat();//处理后退
                    if (hoverTimer != null) {//如果不为空
                        hoverTimer.stop();//计时器结束
                    }
                    if (buttonHoverTipWindow != null) {//如果提示信息不为空
                        buttonHoverTipWindow.dispose();//释放提示信息
                        buttonHoverTipWindow = null;//提示信息置空
                    }
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                if (!folderList.isEmpty() && currentFolder != null && folderListIndex - 1 >= 0) {//如果不为空
                    int tmpFolderListIndex = folderListIndex;//临时索引：用于记录当前文件夹索引
                    if (folderListIndex >= 1) {//如果大于
                        String tmpFolder = folderList.get(tmpFolderListIndex - 1);//临时文件夹：用于记录到达哪个文件夹
                        while (Objects.equals(tmpFolder, (Main.SettingState.systemLanguage ? "My Cloud" : "我的云盘"))) {//如果当前是我的云盘结点
                            if (Objects.equals(Main.SettingState.userAccount, "")) {//如果退出登录
                                if (tmpFolderListIndex - 1 == 0) {//如果到达第一个文件夹
                                    break;//退出
                                }
                                tmpFolder = folderList.get(--tmpFolderListIndex);//到上一个文件夹
                            } else {//否则
                                break;//退出
                            }
                        }
                    }
                    int finalTmpFolderListIndex = tmpFolderListIndex;
                    hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow((Main.SettingState.systemLanguage ? ("Retreat To " + folderList.get(finalTmpFolderListIndex - 1).substring(3) + " (Alt + ←)") : ("后退到 " + folderList.get(finalTmpFolderListIndex - 1).substring(3) + "（Alt + ←）")), retreatButton));//展示提示窗口（鼠标悬浮一秒后展示）
                    hoverTimer.setRepeats(false);//设置计时器不重复
                    hoverTimer.start();//开始计时
                }
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

        advanceButton.addMouseListener(new MouseAdapter() {//为前进按钮添加鼠标事件监听
            @Override
            public void mouseClicked(MouseEvent e) {//如果鼠标点击
                if (SwingUtilities.isLeftMouseButton(e)) {//如果是鼠标左键
                    handleAdvance();//处理前进
                    if (hoverTimer != null) {//如果不为空
                        hoverTimer.stop();//计时器结束
                    }
                    if (buttonHoverTipWindow != null) {//如果提示信息不为空
                        buttonHoverTipWindow.dispose();//释放提示信息
                        buttonHoverTipWindow = null;//提示信息置空
                    }
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                if (!folderList.isEmpty() && currentFolder != null && folderListIndex + 1 < folderList.size()) {//如果不为空
                    int tmpFolderListIndex = folderListIndex;//临时索引：用于记录当前文件夹索引
                    if (folderListIndex + 1 < folderList.size()) {//如果大于
                        String tmpFolder = folderList.get(tmpFolderListIndex + 1);//临时文件夹：用于记录到达哪个文件夹
                        while (Objects.equals(tmpFolder, (Main.SettingState.systemLanguage ? "My Cloud" : "我的云盘"))) {//如果当前是我的云盘结点
                            if (Objects.equals(Main.SettingState.userAccount, "")) {//如果退出登录
                                if (tmpFolderListIndex + 1 == 0) {//如果到达第一个文件夹
                                    break;//退出
                                }
                                tmpFolder = folderList.get(++tmpFolderListIndex);//到上一个文件夹
                            } else {//否则
                                break;//退出
                            }
                        }
                    }
                    int finalTmpFolderListIndex = tmpFolderListIndex;
                    hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? ("Advance To " + folderList.get(finalTmpFolderListIndex + 1).substring(3) + " (Alt + →)") : ("前进到 " + folderList.get(finalTmpFolderListIndex + 1).substring(3) + "（Alt + →）"), advanceButton));//展示提示窗口（鼠标悬浮一秒后展示）
                    hoverTimer.setRepeats(false);//设置计时器不重复
                    hoverTimer.start();//开始计时
                }
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

        upperLayerButton.addMouseListener(new MouseAdapter() {//为上一级按钮添加鼠标事件监听
            @Override
            public void mouseClicked(MouseEvent e) {//如果鼠标点击
                if (SwingUtilities.isLeftMouseButton(e)) {//如果是鼠标左键
                    handleUpperLayer();//处理上一级
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                if (folderList != null && currentFolder != null) {//如果不为空
                    hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Up To " + new File(currentFolder).getParent().substring(3) + " (Alt + ↑)" : "上移到 " + new File(currentFolder).getParent().substring(3) + "（Alt + ↑）", upperLayerButton));//展示提示窗口（鼠标悬浮一秒后展示）
                    hoverTimer.setRepeats(false);//设置计时器不重复
                    hoverTimer.start();//开始计时
                }
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

        refreshButton.addMouseListener(new MouseAdapter() {//为刷新按钮添加鼠标事件监听
            @Override
            public void mouseClicked(MouseEvent e) {//如果鼠标点击
                if (SwingUtilities.isLeftMouseButton(e)) {//如果是鼠标左键
                    handleRefresh();//处理刷新
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Refresh (F5 / Ctrl + R)" : "刷新（F5 / Ctrl + R）", refreshButton));//展示提示窗口（鼠标悬浮一秒后展示）
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

        cutButton.addMouseListener(new MouseAdapter() {//为剪切按钮添加鼠标事件监听
            @Override
            public void mouseClicked(MouseEvent e) {//如果鼠标点击
                if (SwingUtilities.isLeftMouseButton(e)) {//如果是鼠标左键
                    handleCut();//处理剪切
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Cut (Ctrl + X)" : "剪切（Ctrl + X）", cutButton));//展示提示窗口（鼠标悬浮一秒后展示）
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

        copyButton.addMouseListener(new MouseAdapter() {//为复制按钮添加鼠标事件监听
            @Override
            public void mouseClicked(MouseEvent e) {//如果鼠标点击
                if (SwingUtilities.isLeftMouseButton(e)) {//如果是鼠标左键
                    handleCopy();//处理复制
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Copy (Ctrl + C)" : "复制（Ctrl + C）", copyButton));//展示提示窗口（鼠标悬浮一秒后展示）
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

        pasteButton.addMouseListener(new MouseAdapter() {//为粘贴按钮添加鼠标事件监听
            @Override
            public void mouseClicked(MouseEvent e) {//如果鼠标点击
                if (SwingUtilities.isLeftMouseButton(e)) {//如果是鼠标左键
                    handlePaste();//处理粘贴
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Paste (Ctrl + V)" : "粘贴（Ctrl + V）", pasteButton));//展示提示窗口（鼠标悬浮一秒后展示）
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

        renameButton.addMouseListener(new MouseAdapter() {//为重命名按钮添加鼠标事件监听
            @Override
            public void mouseClicked(MouseEvent e) {//如果鼠标点击
                if (SwingUtilities.isLeftMouseButton(e)) {//如果是鼠标左键
                    handleRename();//处理重命名
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Rename (F2)" : "重命名（F2）", renameButton));//展示提示窗口（鼠标悬浮一秒后展示）
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

        removeButton.addMouseListener(new MouseAdapter() {//为删除按钮添加鼠标事件监听
            @Override
            public void mouseClicked(MouseEvent e) {//如果鼠标点击
                if (SwingUtilities.isLeftMouseButton(e)) {//如果是鼠标左键
                    handleRemove();//处理删除
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Remove (Delete)" : "删除（Delete）", removeButton));//展示提示窗口（鼠标悬浮一秒后展示）
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

        sortComboBox.addActionListener(_ -> {//为排序选择添加事件监听
            int index = sortComboBox.getSelectedIndex();//获取选择序号
            if (index != -1 && sortComboBox.getItemCount() == 4) {//如果不是切换语言导致的事件
                if (itemHoverTipWindow != null) {//如果提示信息不为空
                    itemHoverTipWindow.dispose();//释放提示信息
                    itemHoverTipWindow = null;//提示信息置空
                }
                SortType currentType;//当前排序方式
                if (index == sortTypeIndex) {//如果是选中自身排序方式：就逆转排序方式
                    switch (index) {//分支选择
                        case 1 -> {//按日期
                            if (sortTypeList[index] == SortType.ADATE) {//如果原先是升序
                                sortTypeList[index] = SortType.DDATE;//排序方式改为降序
                                currentType = SortType.DDATE;//当前排序方式改为降序
                            } else {//否则是降序
                                sortTypeList[index] = SortType.ADATE;//排序方式改为升序
                                currentType = SortType.ADATE;//当前排序方式改为升序
                            }
                        }
                        case 2 -> {//按类型
                            if (sortTypeList[index] == SortType.ATYPE) {//如果原先是升序
                                sortTypeList[index] = SortType.DTYPE;//排序方式改为降序
                                currentType = SortType.DTYPE;//当前排序方式改为降序
                            } else {//否则是降序
                                sortTypeList[index] = SortType.ATYPE;//排序方式改为升序
                                currentType = SortType.ATYPE;//当前排序方式改为升序
                            }
                        }
                        case 3 -> {//按大小
                            if (sortTypeList[index] == SortType.ASIZE) {//如果原先是升序
                                sortTypeList[index] = SortType.DSIZE;//排序方式改为降序
                                currentType = SortType.DSIZE;//当前排序方式改为降序
                            } else {//否则是降序
                                sortTypeList[index] = SortType.ASIZE;//排序方式改为升序
                                currentType = SortType.ASIZE;//当前排序方式改为升序
                            }
                        }
                        default -> {//按名称（默认）
                            if (sortTypeList[index] == SortType.ANAME) {//如果原先是升序
                                sortTypeList[index] = SortType.DNAME;//排序方式改为降序
                                currentType = SortType.DNAME;//当前排序方式改为降序
                            } else {//否则是降序
                                sortTypeList[index] = SortType.ANAME;//排序方式改为升序
                                currentType = SortType.ANAME;//当前排序方式改为升序
                            }
                        }
                    }
                } else {//否则：直接修改排序方式，不进行逆转
                    switch (index) {//分支选择
                        case 1 -> {//按日期
                            currentType = sortTypeList[index];//直接修改
                            sortTypeIndex = 1;//修改索引
                        }
                        case 2 -> {//按类型
                            currentType = sortTypeList[index];//直接修改
                            sortTypeIndex = 2;//修改索引
                        }
                        case 3 -> {//按大小
                            currentType = sortTypeList[index];//直接修改
                            sortTypeIndex = 3;//修改索引
                        }
                        default -> {//按名称（默认）
                            currentType = sortTypeList[index];//直接修改
                            sortTypeIndex = 0;//修改索引
                        }
                    }
                }
                sortComboBox.removeAllItems();//清空项目
                sortComboBox.addItem(sortTypeList[0] == SortType.ANAME ? (Main.SettingState.systemLanguage ? "Sort By Name (Ascending Order)" : "按名称排序（升序）") : (Main.SettingState.systemLanguage ? "Sort By Name (Descending Order)" : "按名称排序（降序）"));//重新添加
                sortComboBox.addItem(sortTypeList[1] == SortType.ADATE ? (Main.SettingState.systemLanguage ? "Sort By Date (Ascending Order)" : "按日期排序（升序）") : (Main.SettingState.systemLanguage ? "Sort By Date (Descending Order)" : "按日期排序（降序）"));//重新添加
                sortComboBox.addItem(sortTypeList[2] == SortType.ATYPE ? (Main.SettingState.systemLanguage ? "Sort By Type (Ascending Order)" : "按类型排序（升序）") : (Main.SettingState.systemLanguage ? "Sort By Type (Descending Order)" : "按类型排序（降序）"));//重新添加
                sortComboBox.addItem(sortTypeList[3] == SortType.ASIZE ? (Main.SettingState.systemLanguage ? "Sort By Size (Ascending Order)" : "按大小排序（升序）") : (Main.SettingState.systemLanguage ? "Sort By Size (Descending Order)" : "按大小排序（降序）"));//重新添加
                sortComboBox.setSelectedIndex(index);//设置选中项
                sortComboBox.setPreferredSize(new Dimension(Main.SettingState.systemLanguage ? 400 : 235, 34));//设置大小
                changeSortMethod(currentType);//更新选择方式
            }
        });
        sortComboBox.addMouseListener(new MouseAdapter() {//为排序选择添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Modify Sort Pattern (Support Switch Ascending Order And Descending Order)" : "修改排序方式（可以切换升序和降序）", sortComboBox));//展示提示窗口（鼠标悬浮一秒后展示）
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
    }

    public static void handleRetreat() {//处理后退
        if (currentFolder != null && !folderList.isEmpty() && folderListIndex - 1 >= 0) {//如果不为空
            if (itemHoverTipWindow != null) {//如果提示信息不为空
                itemHoverTipWindow.dispose();//释放提示信息
                itemHoverTipWindow = null;//提示信息置空
            }
            currentFolder = folderList.get(--folderListIndex);//设置当前文件夹为当前文件夹的上一级
            if (Objects.equals(currentFolder, Main.SettingState.systemLanguage ? "My Cloud" : "我的云盘")) {//如果是云盘结点
                if (Objects.equals(Main.SettingState.userAccount, "")) {//如果用户退出登录
                    handleRetreat();//递归
                } else {//否则
                    try {
                        DirectoryTree.setCurrentFileList(handleUserLoadUserUploadPicture(null));//更新图片文件列表
                    } catch (IOException e) {
                        handleErrorLog(e.getMessage());//处理错误日志
                        throw new RuntimeException(e);//捕获异常
                    }
                }
            } else {//否则
                DirectoryTree.setCurrentFileList(detectFile(new File(currentFolder).listFiles()));//设置当前文件列表为当前文件夹
            }
            directoryField.setTextField();//设置文件路径为当前文件夹
            updateFileDisplayMainPanel(false);//通知更新文件展示面板
            directoryManipulationButtonEnableJudgement();//按钮判断
        }
    }

    public static void handleAdvance() {//处理前进
        if (currentFolder != null && !folderList.isEmpty() && folderListIndex + 1 < folderList.size()) {//如果不为空
            if (itemHoverTipWindow != null) {//如果提示信息不为空
                itemHoverTipWindow.dispose();//释放提示信息
                itemHoverTipWindow = null;//提示信息置空
            }
            currentFolder = folderList.get(++folderListIndex);//设置当前文件夹为当前文件夹的下一级
            if (Objects.equals(currentFolder, Main.SettingState.systemLanguage ? "My Cloud" : "我的云盘")) {//如果是云盘结点
                if (Objects.equals(Main.SettingState.userAccount, "")) {//如果用户退出登录
                    handleAdvance();//递归
                } else {//否则
                    try {
                        DirectoryTree.setCurrentFileList(handleUserLoadUserUploadPicture(null));//更新当前文件列表
                    } catch (IOException e) {
                        handleErrorLog(e.getMessage());//处理错误日志
                        throw new RuntimeException(e);//捕获异常
                    }
                }
            } else {//否则
                DirectoryTree.setCurrentFileList(detectFile(new File(currentFolder).listFiles()));//设置当前文件列表为当前文件夹
            }
            directoryField.setTextField();//设置文件路径为当前文件夹
            updateFileDisplayMainPanel(false);//通知更新文件展示面板
            directoryManipulationButtonEnableJudgement();//按钮判断
        }
    }

    public static void handleUpperLayer() {//处理上一级
        if (currentFolder != null) {//如果不为空
            if (itemHoverTipWindow != null) {//如果提示信息不为空
                itemHoverTipWindow.dispose();//释放提示信息
                itemHoverTipWindow = null;//提示信息置空
            }
            File currentFileFolder = new File(currentFolder);//创建当前文件夹
            if (currentFileFolder.getParent() != null) {//如果该文件夹有父母
                updateFolder(currentFileFolder.getParent());//设置当前文件夹为父母并更新当前文件夹和文件夹列表
                directoryField.setTextField();//设置文件路径为当前文件夹
                DirectoryTree.setCurrentFileList(detectFile(new File(currentFolder).listFiles()));//设置当前文件列表为当前文件夹
                updateFileDisplayMainPanel(false);//通知更新文件展示面板
                directoryManipulationButtonEnableJudgement();//目录操作按钮判断
            }
        }
    }

    public static void handleRefresh() {//处理刷新
        if (currentFolder != null) {//如果当前文件夹不为空
            if (Objects.equals(currentFolder, Main.SettingState.systemLanguage ? "My Cloud" : "我的云盘")) {//如果是云盘结点
                try {
                    DirectoryTree.setCurrentFileList(handleUserLoadUserUploadPicture(null));//更新当前文件列表
                } catch (IOException e) {
                    handleErrorLog(e.getMessage());//处理错误日志
                    throw new RuntimeException(e);//捕获异常
                }
            } else {//否则
                DirectoryTree.updateCurrentFileList(new File(currentFolder).listFiles());//更新文件夹
            }
            updateFileDisplayMainPanel(false);//通知更新
            fileManipulationButtonEnableJudgement(getSelectionThumbnailItemList().size());//文件操作按钮判断
            directoryManipulationButtonEnableJudgement();//目录操作按钮判断
            FileDisplayBottomBar.historyManipulationButtonEnableJudgement();//历史操作按钮判断
        }
    }

    public static void handleCut() {//处理剪切
        List<ThumbnailItem> list = getSelectionThumbnailItemList();//获取列表
        if (!list.isEmpty()) {//如果非空
            clipboardFiles.clear();//清空剪贴板
            for (ThumbnailItem item : list) {//遍历选中文件列表
                clipboardFiles.add(item.getFile());//更新剪贴板
            }
            isCutOperation = true;//设置为剪切
            if (Objects.equals(currentFolder, Main.SettingState.systemLanguage ? "My Cloud" : "我的云盘")) {//如果是云盘结点
                isCloudOperation = true;//设置为云盘操作
            }
            pasteButton.setEnabled(true);//设置可选中
            FileDisplayPopupMenu.pasteButton.setEnabled(true);//右键菜单也要设置可选中
            refreshMainPanel();//刷新
        }
    }

    public static void handleCopy() {//处理复制
        List<ThumbnailItem> list = getSelectionThumbnailItemList();//获取列表
        if (!list.isEmpty()) {//如果非空
            clipboardFiles.clear();//清空剪贴板
            for (ThumbnailItem item : list) {//遍历选中文件列表
                clipboardFiles.add(item.getFile());//更新剪贴板
            }
            isCutOperation = false;//设置为复制
            pasteButton.setEnabled(true);//设置可选中
            FileDisplayPopupMenu.pasteButton.setEnabled(true);//右键菜单也要设置可选中
            refreshMainPanel();//刷新
        }
    }

    public static void handlePaste() {//处理粘贴
        if (!clipboardFiles.isEmpty()) {//如果非空
            if (itemHoverTipWindow != null) {//如果提示信息不为空
                itemHoverTipWindow.dispose();//释放提示信息
                itemHoverTipWindow = null;//提示信息置空
            }
            if (Objects.equals(currentFolder, Main.SettingState.systemLanguage ? "My Cloud" : "我的云盘")) {//如果是云盘结点
                tmpPastePathList.clear();//清空
                List<File> sources = new ArrayList<>();//原文件记录列表
                List<String> targetStringList = handleUserSaveUserUploadPicture(clipboardFiles, false);//把剪贴板传到云端
                new SwingWorker<Void, Void>() {//线程运行
                    @Override
                    protected Void doInBackground() {//后台加载
                        try {
                            for (File file : clipboardFiles) {//遍历剪贴板
                                sources.add(file);//往列表添加原文件
                                if (isCutOperation) {//如果是剪切
                                    Files.deleteIfExists(file.toPath());//删除文件
                                }
                            }
                        } catch (IOException e) {
                            if (administratorJudgement()) {//管理员判断
                                createBottomTipWindow(Main.SettingState.systemLanguage ? "Insufficient Permission, Run The Program With Administrator Privilege" : "权限不足，请使用管理员权限运行程序");//提示
                            } else {//否则
                                handleErrorLog(e.getMessage());//处理错误日志
                                SwingUtilities.invokeLater(() -> createBottomTipWindow(Main.SettingState.systemLanguage ? "Failure Copy: " + e.getMessage() : "粘贴失败：" + e.getMessage()));//捕获异常
                            }
                        }
                        return null;//返回空
                    }

                    @Override
                    protected void done() {//完成时
                        try {
                            DirectoryTree.setCurrentFileList(handleUserLoadUserUploadPicture(null));//更新当前文件列表
                        } catch (IOException e) {
                            handleErrorLog(e.getMessage());//处理错误日志
                            throw new RuntimeException(e);//捕获异常
                        }
                        updateFileDisplayMainPanel(true);//通知更新
                        if (isCutOperation) {//如果是剪切操作
                            FileDisplayBottomBar.recordFileOperation(new FileDisplayBottomBar.FileOperation(FileDisplayBottomBar.HistoryOperationType.MOVE, sources, null, "cutToCloud", null, targetStringList, true));//记录文件移动操作，原文件列表，目标文件列表，当前文件夹
                            clipboardFiles.clear();//清空剪贴板
                            pasteButton.setEnabled(false);//设置不可粘贴
                            isCutOperation = false;//清空剪贴判断
                        } else {//否则是复制
                            FileDisplayBottomBar.recordFileOperation(new FileDisplayBottomBar.FileOperation(FileDisplayBottomBar.HistoryOperationType.COPY, sources, null, "copyToCloud", null, targetStringList, true));//记录文件复制操作，原文件列表，目标文件列表，当前文件夹
                        }
                        if (itemHoverTipWindow != null) {//如果提示信息不为空
                            itemHoverTipWindow.dispose();//释放提示信息
                            itemHoverTipWindow = null;//提示信息置空
                        }
                    }
                }.execute();//开始执行
            } else {//否则
                File targetDir = new File(currentFolder);//获取目标文件夹
                tmpPastePathList.clear();//清空
                List<File> sources = new ArrayList<>();//原文件记录列表
                List<File> targets = new ArrayList<>();//目标文件记录列表
                new SwingWorker<Void, Void>() {//线程运行
                    @Override
                    protected Void doInBackground() {//后台加载
                        try {
                            for (File file : clipboardFiles) {//遍历剪贴板
                                Path source = file.toPath();//原文件
                                sources.add(file);//往列表添加原文件
                                Path target = targetDir.toPath().resolve(source.getFileName());//目标文件
                                if (Files.exists(target)) {//如果文件重复
                                    String baseName = FilenameUtils.getBaseName(file.getName());//获取文件名称
                                    String extension = FilenameUtils.getExtension(file.getName());//获取文件扩展名
                                    int counter = 1;//计数器
                                    while (Files.exists(target)) {//如果该文件一直重复
                                        String newName;//创建文件新名称，不断进行累加
                                        if (Main.SettingState.renameStrategy) {//如果是数字后缀策略
                                            newName = baseName + "(" + counter++ + ")." + extension;//为文件添加数字后缀
                                        } else {//否则是英文前缀策略
                                            newName = "NewName" + String.format("%04d", counter++) + baseName + "." + extension;//为文件添加英文前缀
                                        }
                                        target = targetDir.toPath().resolve(newName);//设置目标文件
                                    }
                                }
                                targets.add(target.toFile());//往列表添加目标文件
                                if (isCutOperation) {//如果是剪切
                                    Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);//进行文件移动
                                } else {//否则是复制
                                    Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);//进行文件粘贴
                                }
                                tmpPastePathList.add(target);//存储目标文件
                            }
                        } catch (IOException e) {
                            if (administratorJudgement()) {//管理员判断
                                createBottomTipWindow(Main.SettingState.systemLanguage ? "Insufficient Permission, Run The Program With Administrator Privilege" : "权限不足，请使用管理员权限运行程序");//提示
                            } else {//否则
                                handleErrorLog(e.getMessage());//处理错误日志
                                SwingUtilities.invokeLater(() -> createBottomTipWindow(Main.SettingState.systemLanguage ? "Failure Copy: " + e.getMessage() : "粘贴失败：" + e.getMessage()));//捕获异常
                            }
                        }
                        return null;//返回空
                    }

                    @Override
                    protected void done() {//完成时
                        DirectoryTree.updateCurrentFileList(new File(currentFolder).listFiles());//更新文件夹
                        updateFileDisplayMainPanel(true);//通知更新
                        if (isCutOperation) {//如果是剪切操作
                            pasteButton.setEnabled(false);//设置不可粘贴
                            if (isCloudOperation) {//如果是云盘操作
                                try {
                                    List<String> fileNameList = new ArrayList<>();//文件名列表
                                    for (File clipboardFile : clipboardFiles) {//遍历剪贴板
                                        fileNameList.add(clipboardFile.getName());//添加文件名
                                    }
                                    handleUserRemoveUserUploadPicture(fileNameList, false);//删除云盘中剪贴板的文件
                                    FileDisplayBottomBar.recordFileOperation(new FileDisplayBottomBar.FileOperation(FileDisplayBottomBar.HistoryOperationType.MOVE, sources, targets, "cutFromCloud", null, null, true));//记录文件移动操作，原文件列表，目标文件列表，当前文件夹
                                } catch (IOException e) {
                                    handleErrorLog(e.getMessage());//处理错误日志
                                    throw new RuntimeException(e);//捕获异常
                                }
                            } else {//否则
                                FileDisplayBottomBar.recordFileOperation(new FileDisplayBottomBar.FileOperation(FileDisplayBottomBar.HistoryOperationType.MOVE, sources, targets, "copyFromCloud", null, null, false));//记录文件移动操作，原文件列表，目标文件列表，当前文件夹
                            }
                            clipboardFiles.clear();//清空剪贴板
                            isCutOperation = false;//清空剪贴判断
                            isCloudOperation = false;//清空云盘判断
                        } else {//否则是复制
                            if (isCloudOperation) {//如果是云盘操作
                                FileDisplayBottomBar.recordFileOperation(new FileDisplayBottomBar.FileOperation(FileDisplayBottomBar.HistoryOperationType.COPY, sources, targets, "copyFromCloud", null, null, true));//记录文件复制操作，原文件列表，目标文件列表，当前文件夹
                            } else {//否则
                                FileDisplayBottomBar.recordFileOperation(new FileDisplayBottomBar.FileOperation(FileDisplayBottomBar.HistoryOperationType.COPY, sources, targets, currentFolder, null, null, false));//记录文件复制操作，原文件列表，目标文件列表，当前文件夹
                            }
                        }
                        if (itemHoverTipWindow != null) {//如果提示信息不为空
                            itemHoverTipWindow.dispose();//释放提示信息
                            itemHoverTipWindow = null;//提示信息置空
                        }
                    }
                }.execute();//开始执行
            }
        }
    }

    public static void handleRename() {//处理重命名
        if (itemHoverTipWindow != null) {//如果提示信息不为空
            itemHoverTipWindow.dispose();//释放提示信息
            itemHoverTipWindow = null;//提示信息置空
        }
        List<ThumbnailItem> list = getSelectionThumbnailItemList();//获取被选中的文件列表
        if (list.size() == 1) {//如果是单文件重命名
            CustomUserTextField singleFileRenameTextField = new CustomUserTextField(Main.SettingState.systemLanguage ? "Please Enter New File Name" : "请输入新文件名", false, false, true);//单文件重命名文本域
            String fileName = list.getFirst().getFile().getName();//获取文件名称
            singleFileRenameTextField.inputTextField.setText(fileName);//设置名称
            String pictureSuffix = FilenameUtils.getExtension(fileName);//获取图片扩展名
            if (!pictureSuffix.isEmpty()) {//如果不是空串，即找得到扩展名
                int index = fileName.lastIndexOf(pictureSuffix);//获取扩展名索引
                if (index != -1) {//如果能找到
                    singleFileRenameTextField.inputTextField.select(0, index - 1);//选中文件名
                } else {//否则
                    singleFileRenameTextField.inputTextField.select(0, fileName.length());//全选文件名
                }
            } else {//否则
                singleFileRenameTextField.inputTextField.select(0, fileName.length());//全选文件名
            }
            JButton confirmSingleFileRenameButton = new JButton(Main.SettingState.systemLanguage ? "Confirm Rename" : "确认重命名");//确认单文件重命名按钮
            confirmSingleFileRenameButton.setPreferredSize(new Dimension(760, 35));//设置大小
            confirmSingleFileRenameButton.setFont(new Font("微软雅黑", PLAIN, 21));//设置字体
            confirmSingleFileRenameButton.setBackground(USER_LOG_BUTTON_COLOR);//背景颜色
            confirmSingleFileRenameButton.setFocusable(false);//不可聚焦
            confirmSingleFileRenameButton.setBorder(null);//无边框
            singleFileRenameTextField.inputTextField.addKeyListener(new KeyAdapter() {//为单文件重命名文本域添加键盘监听
                @Override
                public void keyPressed(KeyEvent e) {//如果键盘按下
                    if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_DOWN) {//如果按下回车或向下
                        String newFileNameInput = singleFileRenameTextField.inputTextField.getText();//获取输入文本
                        if (newFileNameInput.isEmpty()) {//如果输入为空
                            createBottomTipWindow(Main.SettingState.systemLanguage ? "New File Name Cannot Be Empty" : "新文件名不可为空");//提示
                            singleFileRenameTextField.isWrong = true;//错误
                        } else {//否则
                            for (int i = 0; i < newFileNameInput.length(); i++) {//遍历输入文本
                                char c = newFileNameInput.charAt(i);//获取当前字符
                                if (c == '/' || c == '\\') {//如果是特殊字符
                                    createBottomTipWindow(Main.SettingState.systemLanguage ? "No Special Character" : "请勿输入特殊字符");//提示
                                    singleFileRenameTextField.isWrong = true;//错误
                                    singleFileRenameTextField.revalidate();//重新验证
                                    singleFileRenameTextField.repaint();//重新绘制
                                    return;//直接返回
                                }
                            }
                            singleFileRenameTextField.isWrong = false;//正确
                            confirmSingleFileRenameButton.doClick();//点击
                        }
                        singleFileRenameTextField.revalidate();//重新验证
                        singleFileRenameTextField.repaint();//重新绘制
                        e.consume();//阻止默认行为
                    } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {//如果按下ESC
                        singleFileRenameTextField.isWrong = false;//正确
                        singleFileRenameTextField.revalidate();//重新验证
                        singleFileRenameTextField.repaint();//重新绘制
                        singleFileRenameDialog.requestFocusInWindow();//焦点返回菜单
                        e.consume();//阻止默认行为
                    }
                }
            });
            singleFileRenameTextField.inputTextField.addFocusListener(new FocusAdapter() {//为单文件重命名文本域添加聚焦监听
                @Override
                public void focusLost(FocusEvent e) {//如果失去聚焦
                    if (singleFileRenameDialog.isVisible()) {//如果可见
                        String newFileNameInput = singleFileRenameTextField.inputTextField.getText();//获取输入文本
                        if (newFileNameInput.isEmpty()) {//如果输入为空
                            createBottomTipWindow(Main.SettingState.systemLanguage ? "New File Name Cannot Be Empty" : "新文件名不可为空");//提示
                            singleFileRenameTextField.isWrong = true;//错误
                        } else {//否则
                            for (int i = 0; i < newFileNameInput.length(); i++) {//遍历输入文本
                                char c = newFileNameInput.charAt(i);//获取当前字符
                                if (c == '/' || c == '\\') {//如果是特殊字符
                                    createBottomTipWindow(Main.SettingState.systemLanguage ? "No Special Character" : "请勿输入特殊字符");//提示
                                    singleFileRenameTextField.isWrong = true;//错误
                                    singleFileRenameTextField.revalidate();//重新验证
                                    singleFileRenameTextField.repaint();//重新绘制
                                    return;//直接返回
                                }
                            }
                            singleFileRenameTextField.isWrong = false;//正确
                        }
                        singleFileRenameTextField.revalidate();//重新验证
                        singleFileRenameTextField.repaint();//重新绘制
                    }
                }
            });
            singleFileRenameTextField.inputTextField.addMouseListener(new MouseAdapter() {//为单文件重命名文本域添加鼠标事件监听
                @Override
                public void mouseEntered(MouseEvent e) {//如果鼠标进入
                    hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Please Enter New File Name (Cannot Contain Special Character '/' '\\')" : "请输入新文件名（不可包含特殊字符 '/' '\\'）", singleFileRenameTextField.inputTextField));//展示提示窗口（鼠标悬浮一秒后展示）
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
            confirmSingleFileRenameButton.addActionListener(_ -> {//为确认单文件重命名按钮添加事件监听
                singleFileRenameTextField.isWrong = false;//正确
                singleFileRenameTextField.revalidate();//重新验证
                singleFileRenameTextField.repaint();//重新绘制
                String newFileNameInput = singleFileRenameTextField.inputTextField.getText().trim();//获取新文件名输入
                if (newFileNameInput.isEmpty()) {//如果为空
                    createBottomTipWindow(Main.SettingState.systemLanguage ? "New File Name Cannot Empty" : "新文件名不可为空");//提示
                    singleFileRenameTextField.isWrong = true;//错误
                    singleFileRenameTextField.revalidate();//重新验证
                    singleFileRenameTextField.repaint();//重新绘制
                    return;//直接返回
                }
                for (int i = 0; i < newFileNameInput.length(); i++) {//遍历输入文本
                    char c = newFileNameInput.charAt(i);//获取当前字符
                    if (c == '/' || c == '\\') {//如果是特殊字符
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "No Special Character" : "请勿输入特殊字符");//提示
                        singleFileRenameTextField.isWrong = true;//错误
                        singleFileRenameTextField.revalidate();//重新验证
                        singleFileRenameTextField.repaint();//重新绘制
                        return;//直接返回
                    }
                }
                File oldFile = list.getFirst().getFile();//存储旧文件
                File newFile = new File(oldFile.getParent(), singleFileRenameTextField.inputTextField.getText().trim());//创建新文件
                if (newFile.exists()) {//如果文件存在
                    createBottomTipWindow(Main.SettingState.systemLanguage ? "File Name Repetition" : "文件名重复");//提示
                } else {//否则
                    try {
                        Files.move(oldFile.toPath(), newFile.toPath());//覆盖原文件
                    } catch (IOException e) {
                        if (administratorJudgement()) {//管理员判断
                            createBottomTipWindow(Main.SettingState.systemLanguage ? "Insufficient Permission, Run The Program With Administrator Privilege" : "权限不足，请使用管理员权限运行程序");//提示
                        } else {//否则
                            String errorMessage = e.getMessage().substring(e.getMessage().indexOf(':') + 1);//捕获异常：截断错误信息，只展示':'后的内容
                            handleErrorLog(e.getMessage());//处理错误日志
                            createBottomTipWindow(Main.SettingState.systemLanguage ? "Failure Rename: " + errorMessage : "重命名失败：" + errorMessage);//提示
                        }
                    }
                    if (Objects.equals(currentFolder, Main.SettingState.systemLanguage ? "My Cloud" : "我的云盘")) {//如果是云盘结点
                        try {
                            List<String> newFileNameStringList = handleUserSaveUserUploadPicture(Collections.singletonList(newFile), false);//上传新文件
                            if (handleUserRemoveUserUploadPicture(Collections.singletonList(oldFile.getName()), false)) {//清除旧文件
                                FileDisplayBottomBar.recordFileOperation(new FileDisplayBottomBar.FileOperation(FileDisplayBottomBar.HistoryOperationType.RENAME, Collections.singletonList(oldFile), Collections.singletonList(newFile), oldFile.getAbsolutePath(), null, newFileNameStringList, true));//记录文件复制操作，原文件列表，目标文件列表，旧文件完整原始路径
                                DirectoryTree.setCurrentFileList(handleUserLoadUserUploadPicture(null));//更新图片文件列表
                                createBottomTipWindow(Main.SettingState.systemLanguage ? "Successful Cloud Picture Rename" : "云盘图片重命名成功");//提示
                            } else {//否则
                                createBottomTipWindow(Main.SettingState.systemLanguage ? "Failed Cloud Picture Rename" : "云盘图片重命名失败");//提示
                            }
                        } catch (IOException e) {
                            handleErrorLog(e.getMessage());//处理错误日志
                            throw new RuntimeException(e);//捕获异常
                        }
                    } else {//否则
                        FileDisplayBottomBar.recordFileOperation(new FileDisplayBottomBar.FileOperation(FileDisplayBottomBar.HistoryOperationType.RENAME, Collections.singletonList(oldFile), Collections.singletonList(newFile), oldFile.getAbsolutePath(), null, null, false));//记录文件复制操作，原文件列表，目标文件列表，旧文件完整原始路径
                        DirectoryTree.updateCurrentFileList(new File(currentFolder).listFiles());//更新文件夹
                    }
                    updateFileDisplayMainPanel(false);//通知更新
                    singleFileRenameDialog.dispose();//释放
                    if (itemHoverTipWindow != null) {//如果提示信息不为空
                        itemHoverTipWindow.dispose();//释放提示信息
                        itemHoverTipWindow = null;//提示信息置空
                    }
                }
            });
            confirmSingleFileRenameButton.addMouseListener(new MouseAdapter() {//为确认单文件重命名按钮添加事件监听
                @Override
                public void mouseEntered(MouseEvent e) {//如果鼠标进入
                    hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Confirm Rename" : "确认重命名", confirmSingleFileRenameButton));//展示提示窗口（鼠标悬浮一秒后展示）
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
            contentPanel.add(singleFileRenameTextField);
            contentPanel.add(confirmSingleFileRenameButton);

            singleFileRenameDialog = new JDialog(Main.diskManagementSystemFrame, Main.SettingState.systemLanguage ? "Rename" : "重命名", true);//创建单文件重命名对话窗口
            singleFileRenameDialog.setIconImage(new ImageIcon("src/material/image/rename.png").getImage());//设置图标
            singleFileRenameDialog.setLayout(new BorderLayout());//设置布局
            singleFileRenameDialog.add(contentPanel, BorderLayout.CENTER);//内容面板添加到中心
            singleFileRenameDialog.setAlwaysOnTop(Main.SettingState.windowState);//设置永远在最上层
            singleFileRenameDialog.pack();//设置合适
            singleFileRenameDialog.setLocation(Main.screenSize.width / 2 - singleFileRenameDialog.getWidth() / 2, Main.screenSize.height / 2 - singleFileRenameDialog.getHeight() / 2);//设置位置
            JRootPane singleFileRenameDialogRoot = singleFileRenameDialog.getRootPane();//获取重命名窗口的根
            singleFileRenameDialogRoot.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "closeSingleFileRenameDialog");//为根设置窗口关闭ESC按键绑定
            singleFileRenameDialogRoot.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "closeSingleFileRenameDialog");//为根设置窗口关闭F2按键绑定
            singleFileRenameDialogRoot.getActionMap().put("closeSingleFileRenameDialog", new AbstractAction() {//当ESC按键执行时
                public void actionPerformed(ActionEvent event) {//行为执行
                    singleFileRenameDialog.dispatchEvent(new WindowEvent(singleFileRenameDialog, WindowEvent.WINDOW_CLOSING));//关闭窗口
                }
            });
            singleFileRenameDialog.addWindowListener(new WindowAdapter() {//为单文件重命名窗口添加窗口监听
                @Override
                public void windowClosing(WindowEvent e) {//如果窗口正在关闭
                    if (bottomTipWindow != null && bottomTipWindow.isVisible()) {//如果底部提示窗口可见
                        bottomTipWindow.dispose();//底部提示窗口置空
                    }
                }
            });
            singleFileRenameDialog.setVisible(true);//设置可见
        } else {//否则是多文件重命名
            CustomUserTextField multipleFileNumberRenameUnifiedPrefixTextField = new CustomUserTextField(Main.SettingState.systemLanguage ? "Please Enter New File Name Unified Prefix" : "请输入新文件名统一前缀", false, false, false);//多文件编号重命名统一前缀文本域
            CustomUserTextField multipleFileNumberRenameStartNumberTextField = new CustomUserTextField(Main.SettingState.systemLanguage ? "Please Enter New File Name Start Number" : "请输入新文件名起始编号", false, false, false);//多文件编号重命名起始编号文本域
            CustomUserTextField multipleFileNumberRenameNumberDigitTextField = new CustomUserTextField(Main.SettingState.systemLanguage ? "Please Enter New File Name Number Digit" : "请输入新文件名编号位数", false, false, false);//多文件编号重命名编号位数文本域
            JButton confirmMultipleFileNumberRenameButton = new JButton(Main.SettingState.systemLanguage ? "Confirm Rename" : "确认重命名");//确认多文件编号重命名按钮
            confirmMultipleFileNumberRenameButton.setPreferredSize(new Dimension(550, 35));//设置大小
            confirmMultipleFileNumberRenameButton.setFont(new Font("微软雅黑", PLAIN, 21));//设置字体
            confirmMultipleFileNumberRenameButton.setBackground(USER_LOG_BUTTON_COLOR);//背景颜色
            confirmMultipleFileNumberRenameButton.setFocusable(false);//不可聚焦
            confirmMultipleFileNumberRenameButton.setBorder(null);//无边框
            multipleFileNumberRenameUnifiedPrefixTextField.inputTextField.addKeyListener(new KeyAdapter() {//为多文件编号重命名统一前缀文本域添加键盘监听
                @Override
                public void keyPressed(KeyEvent e) {//如果键盘按下
                    if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_DOWN) {//如果按下回车或向下
                        String unifiedPrefixInput = multipleFileNumberRenameUnifiedPrefixTextField.inputTextField.getText();//获取输入文本
                        for (int i = 0; i < unifiedPrefixInput.length(); i++) {//遍历输入文本
                            char c = unifiedPrefixInput.charAt(i);//获取当前字符
                            if (c == '/' || c == '\\') {//如果是特殊字符
                                createBottomTipWindow(Main.SettingState.systemLanguage ? "No Special Character" : "请勿输入特殊字符");//提示
                                multipleFileNumberRenameUnifiedPrefixTextField.isWrong = true;//错误
                                multipleFileNumberRenameUnifiedPrefixTextField.revalidate();//重新验证
                                multipleFileNumberRenameUnifiedPrefixTextField.repaint();//重新绘制
                                return;//直接返回
                            }
                        }
                        multipleFileNumberRenameUnifiedPrefixTextField.isWrong = false;//正确
                        multipleFileNumberRenameUnifiedPrefixTextField.revalidate();//重新验证
                        multipleFileNumberRenameUnifiedPrefixTextField.repaint();//重新绘制
                        multipleFileNumberRenameStartNumberTextField.inputTextField.requestFocusInWindow();//焦点到下一行
                        e.consume();//阻止默认行为
                    } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {//如果按下ESC
                        multipleFileNumberRenameUnifiedPrefixTextField.isWrong = false;//正确
                        multipleFileNumberRenameUnifiedPrefixTextField.revalidate();//重新验证
                        multipleFileNumberRenameUnifiedPrefixTextField.repaint();//重新绘制
                        multipleFileRenameDialog.requestFocusInWindow();//焦点返回菜单
                        e.consume();//阻止默认行为
                    }
                }
            });
            multipleFileNumberRenameUnifiedPrefixTextField.inputTextField.addFocusListener(new FocusAdapter() {//为多文件编号重命名统一前缀文本域添加聚焦监听
                @Override
                public void focusLost(FocusEvent e) {//如果失去聚焦
                    if (multipleFileNumberRenameUnifiedPrefixTextField.inputTextField.isVisible() && multipleFileNumberRenameUnifiedPrefixTextField.inputTextField.isShowing()) {//如果可见
                        String unifiedPrefixInput = multipleFileNumberRenameUnifiedPrefixTextField.inputTextField.getText();//获取输入文本
                        for (int i = 0; i < unifiedPrefixInput.length(); i++) {//遍历输入文本
                            char c = unifiedPrefixInput.charAt(i);//获取当前字符
                            if (c == '/' || c == '\\') {//如果是特殊字符
                                createBottomTipWindow(Main.SettingState.systemLanguage ? "No Special Character" : "请勿输入特殊字符");//提示
                                multipleFileNumberRenameUnifiedPrefixTextField.isWrong = true;//错误
                                multipleFileNumberRenameUnifiedPrefixTextField.revalidate();//重新验证
                                multipleFileNumberRenameUnifiedPrefixTextField.repaint();//重新绘制
                                return;//直接返回
                            }
                        }
                        multipleFileNumberRenameUnifiedPrefixTextField.isWrong = false;//正确
                        multipleFileNumberRenameUnifiedPrefixTextField.revalidate();//重新验证
                        multipleFileNumberRenameUnifiedPrefixTextField.repaint();//重新绘制
                    }
                }
            });
            multipleFileNumberRenameUnifiedPrefixTextField.inputTextField.addMouseListener(new MouseAdapter() {//为多文件编号重命名统一前缀文本域添加鼠标事件监听
                @Override
                public void mouseEntered(MouseEvent e) {//如果鼠标进入
                    hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Please Enter New File Name Unified Prefix (Cannot Contain Special Character '/' '\\')" : "请输入新文件名统一前缀（不可包含特殊字符 '/' '\\'）", multipleFileNumberRenameUnifiedPrefixTextField.inputTextField));//展示提示窗口（鼠标悬浮一秒后展示）
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
            multipleFileNumberRenameStartNumberTextField.inputTextField.addKeyListener(new KeyAdapter() {//为多文件编号重命名起始编号文本域添加键盘监听
                @Override
                public void keyPressed(KeyEvent e) {//如果键盘按下
                    if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN) {//如果按下回车或向上或向下
                        String startNumberInput = multipleFileNumberRenameStartNumberTextField.inputTextField.getText();//获取输入文本
                        if (startNumberInput.isEmpty()) {//如果输入为空
                            createBottomTipWindow(Main.SettingState.systemLanguage ? "Start Number Cannot Be Empty" : "起始编号不可为空");//提示
                            multipleFileNumberRenameStartNumberTextField.isWrong = true;//错误
                        } else {//否则
                            for (int i = 0; i < startNumberInput.length(); i++) {//遍历输入文本
                                if (!Character.isDigit(startNumberInput.charAt(i))) {//如果非数字字符
                                    createBottomTipWindow(Main.SettingState.systemLanguage ? "No Non Digit Character" : "请勿输入非数字字符");//提示
                                    multipleFileNumberRenameStartNumberTextField.isWrong = true;//错误
                                    multipleFileNumberRenameStartNumberTextField.revalidate();//重新验证
                                    multipleFileNumberRenameStartNumberTextField.repaint();//重新绘制
                                    return;//直接返回
                                }
                            }
                            if (Integer.parseInt(startNumberInput) > 10000) {//如果输入过大
                                createBottomTipWindow(Main.SettingState.systemLanguage ? "Do Not Enter Too Large" : "请勿输入过大的数字");//提示
                                multipleFileNumberRenameStartNumberTextField.isWrong = true;//错误
                                multipleFileNumberRenameStartNumberTextField.revalidate();//重新验证
                                multipleFileNumberRenameStartNumberTextField.repaint();//重新绘制
                                return;//直接返回
                            }
                            multipleFileNumberRenameStartNumberTextField.isWrong = false;//正确
                            if (e.getKeyCode() == KeyEvent.VK_UP) {//如果是向上
                                multipleFileNumberRenameUnifiedPrefixTextField.inputTextField.requestFocusInWindow();//焦点到上一行
                            } else {//否则
                                multipleFileNumberRenameNumberDigitTextField.inputTextField.requestFocusInWindow();//焦点到下一行
                            }
                        }
                        multipleFileNumberRenameStartNumberTextField.revalidate();//重新验证
                        multipleFileNumberRenameStartNumberTextField.repaint();//重新绘制
                        e.consume();//阻止默认行为
                    } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {//如果按下ESC
                        multipleFileNumberRenameStartNumberTextField.isWrong = false;//正确
                        multipleFileNumberRenameStartNumberTextField.revalidate();//重新验证
                        multipleFileNumberRenameStartNumberTextField.repaint();//重新绘制
                        multipleFileRenameDialog.requestFocusInWindow();//焦点返回菜单
                        e.consume();//阻止默认行为
                    }
                }
            });
            multipleFileNumberRenameStartNumberTextField.inputTextField.addFocusListener(new FocusAdapter() {//为多文件编号重命名起始编号文本域添加聚焦监听
                @Override
                public void focusLost(FocusEvent e) {//如果失去聚焦
                    if (multipleFileNumberRenameStartNumberTextField.inputTextField.isVisible() && multipleFileNumberRenameStartNumberTextField.inputTextField.isShowing()) {//如果可见
                        String startNumberInput = multipleFileNumberRenameStartNumberTextField.inputTextField.getText();//获取输入文本
                        if (startNumberInput.isEmpty()) {//如果输入为空
                            createBottomTipWindow(Main.SettingState.systemLanguage ? "Start Number Cannot Be Empty" : "起始编号不可为空");//提示
                            multipleFileNumberRenameStartNumberTextField.isWrong = true;//错误
                        } else {//否则
                            for (int i = 0; i < startNumberInput.length(); i++) {//遍历输入文本
                                if (!Character.isDigit(startNumberInput.charAt(i))) {//如果非数字字符
                                    createBottomTipWindow(Main.SettingState.systemLanguage ? "No Non Digit Character" : "请勿输入非数字字符");//提示
                                    multipleFileNumberRenameStartNumberTextField.isWrong = true;//错误
                                    multipleFileNumberRenameStartNumberTextField.revalidate();//重新验证
                                    multipleFileNumberRenameStartNumberTextField.repaint();//重新绘制
                                    return;//直接返回
                                }
                            }
                            if (Integer.parseInt(startNumberInput) > 10000) {//如果输入过大
                                createBottomTipWindow(Main.SettingState.systemLanguage ? "Do Not Enter Too Large" : "请勿输入过大的数字");//提示
                                multipleFileNumberRenameStartNumberTextField.isWrong = true;//错误
                                multipleFileNumberRenameStartNumberTextField.revalidate();//重新验证
                                multipleFileNumberRenameStartNumberTextField.repaint();//重新绘制
                                return;//直接返回
                            }
                            multipleFileNumberRenameStartNumberTextField.isWrong = false;//正确
                        }
                        multipleFileNumberRenameStartNumberTextField.revalidate();//重新验证
                        multipleFileNumberRenameStartNumberTextField.repaint();//重新绘制
                    }
                }
            });
            multipleFileNumberRenameStartNumberTextField.inputTextField.addMouseListener(new MouseAdapter() {//为多文件编号重命名起始编号文本域添加鼠标事件监听
                @Override
                public void mouseEntered(MouseEvent e) {//如果鼠标进入
                    hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Please Enter New File Name Start Number" : "请输入新文件名起始编号", multipleFileNumberRenameStartNumberTextField.inputTextField));//展示提示窗口（鼠标悬浮一秒后展示）
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
            multipleFileNumberRenameNumberDigitTextField.inputTextField.addKeyListener(new KeyAdapter() {//为多文件编号重命名编号位数文本域添加键盘监听
                @Override
                public void keyPressed(KeyEvent e) {//如果键盘按下
                    if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN) {//如果按下回车或向上或向下
                        String numberDigitInput = multipleFileNumberRenameNumberDigitTextField.inputTextField.getText();//获取输入文本
                        if (numberDigitInput.isEmpty()) {//如果输入为空
                            createBottomTipWindow(Main.SettingState.systemLanguage ? "Number Digit Cannot Be Empty" : "编号位数不可为空");//提示
                            multipleFileNumberRenameNumberDigitTextField.isWrong = true;//错误
                        } else {//否则
                            for (int i = 0; i < numberDigitInput.length(); i++) {//遍历输入文本
                                if (!Character.isDigit(numberDigitInput.charAt(i))) {//如果非数字字符
                                    createBottomTipWindow(Main.SettingState.systemLanguage ? "No Non Digit Character" : "请勿输入非数字字符");//提示
                                    multipleFileNumberRenameNumberDigitTextField.isWrong = true;//错误
                                    multipleFileNumberRenameNumberDigitTextField.revalidate();//重新验证
                                    multipleFileNumberRenameNumberDigitTextField.repaint();//重新绘制
                                    return;//直接返回
                                }
                            }
                            if (Integer.parseInt(numberDigitInput) > 10) {//如果输入过大
                                createBottomTipWindow(Main.SettingState.systemLanguage ? "Do Not Enter Too Large" : "请勿输入过大的数字");//提示
                                multipleFileNumberRenameNumberDigitTextField.isWrong = true;//错误
                                multipleFileNumberRenameNumberDigitTextField.revalidate();//重新验证
                                multipleFileNumberRenameNumberDigitTextField.repaint();//重新绘制
                                return;//直接返回
                            }
                            multipleFileNumberRenameNumberDigitTextField.isWrong = false;//正确
                            if (e.getKeyCode() == KeyEvent.VK_UP) {//如果是向上
                                multipleFileNumberRenameStartNumberTextField.inputTextField.requestFocusInWindow();//焦点到上一行
                            } else {//否则
                                confirmMultipleFileNumberRenameButton.doClick();//点击
                            }
                        }
                        multipleFileNumberRenameNumberDigitTextField.revalidate();//重新验证
                        multipleFileNumberRenameNumberDigitTextField.repaint();//重新绘制
                        e.consume();//阻止默认行为
                    } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {//如果按下ESC
                        multipleFileNumberRenameNumberDigitTextField.isWrong = false;//正确
                        multipleFileNumberRenameNumberDigitTextField.revalidate();//重新验证
                        multipleFileNumberRenameNumberDigitTextField.repaint();//重新绘制
                        multipleFileRenameDialog.requestFocusInWindow();//焦点返回菜单
                        e.consume();//阻止默认行为
                    }
                }
            });
            multipleFileNumberRenameNumberDigitTextField.inputTextField.addFocusListener(new FocusAdapter() {//为多文件编号重命名编号位数文本域添加聚焦监听
                @Override
                public void focusLost(FocusEvent e) {//如果失去聚焦
                    if (multipleFileNumberRenameNumberDigitTextField.inputTextField.isVisible() && multipleFileNumberRenameNumberDigitTextField.inputTextField.isShowing()) {//如果可见
                        String numberDigitInput = multipleFileNumberRenameNumberDigitTextField.inputTextField.getText();//获取输入文本
                        if (numberDigitInput.isEmpty()) {//如果输入为空
                            createBottomTipWindow(Main.SettingState.systemLanguage ? "Number Digit Cannot Be Empty" : "编号位数不可为空");//提示
                            multipleFileNumberRenameNumberDigitTextField.isWrong = true;//错误
                        } else {//否则
                            for (int i = 0; i < numberDigitInput.length(); i++) {//遍历输入文本
                                if (!Character.isDigit(numberDigitInput.charAt(i))) {//如果非数字字符
                                    createBottomTipWindow(Main.SettingState.systemLanguage ? "No Non Digit Character" : "请勿输入非数字字符");//提示
                                    multipleFileNumberRenameNumberDigitTextField.isWrong = true;//错误
                                    multipleFileNumberRenameNumberDigitTextField.revalidate();//重新验证
                                    multipleFileNumberRenameNumberDigitTextField.repaint();//重新绘制
                                    return;//直接返回
                                }
                            }
                            if (numberDigitInput.charAt(0) == '0' && numberDigitInput.length() == 1) {//如果输入0
                                createBottomTipWindow(Main.SettingState.systemLanguage ? "No Zero" : "请勿输入0");//提示
                                multipleFileNumberRenameNumberDigitTextField.isWrong = true;//错误
                                multipleFileNumberRenameNumberDigitTextField.revalidate();//重新验证
                                multipleFileNumberRenameNumberDigitTextField.repaint();//重新绘制
                                return;//直接返回
                            }
                            if (Integer.parseInt(numberDigitInput) > 10) {//如果输入过大
                                createBottomTipWindow(Main.SettingState.systemLanguage ? "Do Not Enter Too Large" : "请勿输入过大的数字");//提示
                                multipleFileNumberRenameNumberDigitTextField.isWrong = true;//错误
                                multipleFileNumberRenameNumberDigitTextField.revalidate();//重新验证
                                multipleFileNumberRenameNumberDigitTextField.repaint();//重新绘制
                                return;//直接返回
                            }
                            multipleFileNumberRenameNumberDigitTextField.isWrong = false;//正确
                        }
                        multipleFileNumberRenameNumberDigitTextField.revalidate();//重新验证
                        multipleFileNumberRenameNumberDigitTextField.repaint();//重新绘制
                    }
                }
            });
            multipleFileNumberRenameNumberDigitTextField.inputTextField.addMouseListener(new MouseAdapter() {//为多文件编号重命名编号位数文本域添加鼠标事件监听
                @Override
                public void mouseEntered(MouseEvent e) {//如果鼠标进入
                    hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Please Enter New File Name Number Digit" : "请输入新文件名编号位数", multipleFileNumberRenameNumberDigitTextField.inputTextField));//展示提示窗口（鼠标悬浮一秒后展示）
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
            confirmMultipleFileNumberRenameButton.addActionListener(_ -> {//为确认多文件编号重命名按钮添加事件监听
                multipleFileNumberRenameUnifiedPrefixTextField.isWrong = false;//正确
                multipleFileNumberRenameUnifiedPrefixTextField.revalidate();//重新验证
                multipleFileNumberRenameUnifiedPrefixTextField.repaint();//重新绘制
                multipleFileNumberRenameStartNumberTextField.isWrong = false;//正确
                multipleFileNumberRenameStartNumberTextField.revalidate();//重新验证
                multipleFileNumberRenameStartNumberTextField.repaint();//重新绘制
                multipleFileNumberRenameNumberDigitTextField.isWrong = false;//正确
                multipleFileNumberRenameNumberDigitTextField.revalidate();//重新验证
                multipleFileNumberRenameNumberDigitTextField.repaint();//重新绘制
                String unifiedPrefixInput = multipleFileNumberRenameUnifiedPrefixTextField.inputTextField.getText().trim();//获取统一前缀输入
                String startNumberInput = multipleFileNumberRenameStartNumberTextField.inputTextField.getText().trim();//获取起始编号输入
                String numberDigitInput = multipleFileNumberRenameNumberDigitTextField.inputTextField.getText().trim();//获取编号位数输入
                if (startNumberInput.isEmpty()) {//如果起始编号为空
                    multipleFileNumberRenameStartNumberTextField.isWrong = true;//错误
                    multipleFileNumberRenameStartNumberTextField.revalidate();//重新验证
                    multipleFileNumberRenameStartNumberTextField.repaint();//重新绘制
                }
                if (numberDigitInput.isEmpty()) {//如果编号位数为空
                    multipleFileNumberRenameNumberDigitTextField.isWrong = false;//正确
                    multipleFileNumberRenameNumberDigitTextField.revalidate();//重新验证
                    multipleFileNumberRenameNumberDigitTextField.repaint();//重新绘制
                }
                if (startNumberInput.isEmpty() || numberDigitInput.isEmpty()) {//如果都为空
                    createBottomTipWindow(Main.SettingState.systemLanguage ? "None Of Fields Can Be Empty" : "起始编号和编号位数不可为空");//提示
                    return;//直接返回
                }
                for (int i = 0; i < unifiedPrefixInput.length(); i++) {//遍历输入文本
                    char c = unifiedPrefixInput.charAt(i);//获取当前字符
                    if (c == '/' || c == '\\') {//如果是特殊字符
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "No Special Character" : "请勿输入特殊字符");//提示
                        multipleFileNumberRenameUnifiedPrefixTextField.isWrong = true;//错误
                        multipleFileNumberRenameUnifiedPrefixTextField.revalidate();//重新验证
                        multipleFileNumberRenameUnifiedPrefixTextField.repaint();//重新绘制
                        return;//直接返回
                    }
                }
                for (int i = 0; i < startNumberInput.length(); i++) {//遍历输入文本
                    if (!Character.isDigit(startNumberInput.charAt(i))) {//如果非数字字符
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "No Non Digit Character" : "请勿输入非数字字符");//提示
                        multipleFileNumberRenameStartNumberTextField.isWrong = true;//错误
                        multipleFileNumberRenameStartNumberTextField.revalidate();//重新验证
                        multipleFileNumberRenameStartNumberTextField.repaint();//重新绘制
                        return;//直接返回
                    }
                }
                if (Integer.parseInt(startNumberInput) > 10000) {//如果输入过大
                    createBottomTipWindow(Main.SettingState.systemLanguage ? "Do Not Enter Too Large" : "请勿输入过大的数字");//提示
                    multipleFileNumberRenameStartNumberTextField.isWrong = true;//错误
                    multipleFileNumberRenameStartNumberTextField.revalidate();//重新验证
                    multipleFileNumberRenameStartNumberTextField.repaint();//重新绘制
                    return;//直接返回
                }
                for (int i = 0; i < numberDigitInput.length(); i++) {//遍历输入文本
                    if (!Character.isDigit(numberDigitInput.charAt(i))) {//如果非数字字符
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "No Non Digit Character" : "请勿输入非数字字符");//提示
                        multipleFileNumberRenameNumberDigitTextField.isWrong = true;//错误
                        multipleFileNumberRenameNumberDigitTextField.revalidate();//重新验证
                        multipleFileNumberRenameNumberDigitTextField.repaint();//重新绘制
                        return;//直接返回
                    }
                }
                if (numberDigitInput.charAt(0) == '0' && numberDigitInput.length() == 1) {//如果输入0
                    createBottomTipWindow(Main.SettingState.systemLanguage ? "No Zero" : "请勿输入0");//提示
                    multipleFileNumberRenameNumberDigitTextField.isWrong = true;//错误
                    multipleFileNumberRenameNumberDigitTextField.revalidate();//重新验证
                    multipleFileNumberRenameNumberDigitTextField.repaint();//重新绘制
                    return;//直接返回
                }
                if (Integer.parseInt(numberDigitInput) > 10) {//如果输入过大
                    createBottomTipWindow(Main.SettingState.systemLanguage ? "Do Not Enter Too Large" : "请勿输入过大的数字");//提示
                    multipleFileNumberRenameNumberDigitTextField.isWrong = true;//错误
                    multipleFileNumberRenameNumberDigitTextField.revalidate();//重新验证
                    multipleFileNumberRenameNumberDigitTextField.repaint();//重新绘制
                    return;//直接返回
                }
                File targetDir = new File(currentFolder);//获取目标文件夹
                List<File> sources = new ArrayList<>();//原文件记录列表
                List<File> targets = new ArrayList<>();//目标文件记录列表
                try {
                    int i = 0;//计时器
                    for (ThumbnailItem item : list) {//遍历列表
                        File oldFile = item.getFile();//存储旧文件
                        String newFileNameFormat = "%0" + numberDigitInput + "d";//新文件名格式
                        File newFile = new File(oldFile.getParent(), unifiedPrefixInput + String.format(newFileNameFormat, Integer.parseInt(startNumberInput) + i) + "." + FilenameUtils.getExtension(oldFile.getName()));//创建新文件
                        i++;//自增
                        if (newFile.exists()) {//如果文件存在
                            String baseName = FilenameUtils.getBaseName(newFile.getName());//获取文件名称
                            String extension = FilenameUtils.getExtension(newFile.getName());//获取文件扩展名
                            int counter = 1;//计数器
                            while (newFile.exists()) {//如果该文件一直重复
                                String newName;//创建文件新名称，不断进行累加
                                if (Main.SettingState.renameStrategy) {//如果是数字后缀策略
                                    newName = baseName + "(" + counter++ + ")." + extension;//为文件添加数字后缀
                                } else {//否则是英文前缀策略
                                    newName = "NewName" + String.format("%04d", counter++) + baseName + "." + extension;//为文件添加英文前缀
                                }
                                if (Objects.equals(currentFolder, Main.SettingState.systemLanguage ? "My Cloud" : "我的云盘")) {//如果是云盘结点
                                    newFile = new File(spikeVisionCloudPath + "/.buffer").toPath().resolve(newName).toFile();//设置新文件
                                } else {//否则
                                    newFile = targetDir.toPath().resolve(newName).toFile();//设置新文件
                                }
                            }
                        }
                        Files.move(oldFile.toPath(), newFile.toPath());//覆盖原文件
                        sources.add(oldFile);//往列表添加旧文件
                        targets.add(newFile);//往列表添加新文件
                    }
                    if (itemHoverTipWindow != null) {//如果提示信息不为空
                        itemHoverTipWindow.dispose();//释放提示信息
                        itemHoverTipWindow = null;//提示信息置空
                    }
                } catch (IOException e) {
                    if (administratorJudgement()) {//管理员判断
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "Insufficient Permission, Run The Program With Administrator Privilege" : "权限不足，请使用管理员权限运行程序");//提示
                    } else {//否则
                        String errorMessage = e.getMessage().substring(e.getMessage().indexOf(':') + 1);//截断错误信息，只展示':'后的内容
                        handleErrorLog(e.getMessage());//处理错误日志
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "Failure Rename: " + errorMessage : "重命名失败：" + errorMessage);//提示
                    }
                }

                if (Objects.equals(currentFolder, Main.SettingState.systemLanguage ? "My Cloud" : "我的云盘")) {//如果是云盘结点
                    try {
                        List<String> newFileNameStringList = handleUserSaveUserUploadPicture(targets, false);//上传新文件
                        if (handleUserRemoveUserUploadPicture(sources.stream().map(File::getName).collect(Collectors.toList()), false)) {//清除旧文件
                            FileDisplayBottomBar.recordFileOperation(new FileDisplayBottomBar.FileOperation(FileDisplayBottomBar.HistoryOperationType.RENAME, sources, targets, null, null, newFileNameStringList, true));//记录文件复制操作，原文件列表，目标文件列表，旧文件完整原始路径
                            DirectoryTree.setCurrentFileList(handleUserLoadUserUploadPicture(null));//更新图片文件列表
                            createBottomTipWindow(Main.SettingState.systemLanguage ? "Successful Cloud Picture Rename" : "云盘图片重命名成功");//提示
                        } else {//否则
                            createBottomTipWindow(Main.SettingState.systemLanguage ? "Failed Cloud Picture Rename" : "云盘图片重命名失败");//提示
                        }
                    } catch (IOException e) {
                        handleErrorLog(e.getMessage());//处理错误日志
                        throw new RuntimeException(e);//捕获异常
                    }
                } else {//否则
                    FileDisplayBottomBar.recordFileOperation(new FileDisplayBottomBar.FileOperation(FileDisplayBottomBar.HistoryOperationType.RENAME, sources, targets, null, null, null, false));//记录文件复制操作，原文件列表，目标文件列表，旧文件完整原始路径
                    DirectoryTree.updateCurrentFileList(new File(currentFolder).listFiles());//更新文件夹
                }
                updateFileDisplayMainPanel(false);//通知更新
                multipleFileRenameDialog.dispose();//释放
            });
            confirmMultipleFileNumberRenameButton.addMouseListener(new MouseAdapter() {//为确认多文件编号重命名按钮添加鼠标监听
                @Override
                public void mouseEntered(MouseEvent e) {//如果鼠标进入
                    hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Confirm Rename" : "确认重命名", confirmMultipleFileNumberRenameButton));//展示提示窗口（鼠标悬浮一秒后展示）
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
            JPanel multipleFileNumberRenamePanel = new JPanel(new GridLayout(0, 1, 5, 5));//创建多文件编号重命名面板
            multipleFileNumberRenamePanel.setBorder(BorderFactory.createEmptyBorder(7, 10, 7, 10));//创建边框
            multipleFileNumberRenamePanel.setBackground(Main.SettingState.themeColor ? DARK_DIALOG_MAIN_COLOR : LIGHT_DIALOG_MAIN_COLOR);//设置背景颜色
            multipleFileNumberRenamePanel.add(multipleFileNumberRenameUnifiedPrefixTextField);
            multipleFileNumberRenamePanel.add(multipleFileNumberRenameStartNumberTextField);
            multipleFileNumberRenamePanel.add(multipleFileNumberRenameNumberDigitTextField);
            multipleFileNumberRenamePanel.add(confirmMultipleFileNumberRenameButton);

            CustomUserTextField multipleFileInformationRenameUnifiedPrefixTextField = new CustomUserTextField(Main.SettingState.systemLanguage ? "Please Enter New File Name Unified Prefix" : "请输入新文件名统一前缀", false, false, false);//多文件信息重命名统一前缀文本域
            JLabel multipleFileInformationRenameInformationLabel = new JLabel(Main.SettingState.systemLanguage ? "Add Information: " : "添加信息：");//多文件信息重命名信息标签
            multipleFileInformationRenameInformationLabel.setForeground(Main.SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//设置字体颜色
            multipleFileInformationRenameInformationLabel.setBackground(Main.SettingState.themeColor ? DARK_DIALOG_MAIN_COLOR : LIGHT_DIALOG_MAIN_COLOR);//设置背景颜色
            multipleFileInformationRenameInformationLabel.setFont(new Font("微软雅黑", PLAIN, 21));//设置字体
            JCheckBox multipleFileInformationRenameCreateDateCheckBox = new JCheckBox(Main.SettingState.systemLanguage ? "Create Date" : "创建时间");//多文件信息重命名创建时间复选框
            multipleFileInformationRenameCreateDateCheckBox.setForeground(Main.SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//设置字体颜色
            multipleFileInformationRenameCreateDateCheckBox.setBackground(Main.SettingState.themeColor ? DARK_DIALOG_MAIN_COLOR : LIGHT_DIALOG_MAIN_COLOR);//设置背景颜色
            multipleFileInformationRenameCreateDateCheckBox.setFont(new Font("微软雅黑", PLAIN, 21));//设置字体
            JCheckBox multipleFileInformationRenameModifyDateCheckBox = new JCheckBox(Main.SettingState.systemLanguage ? "Modify Date" : "修改时间");//多文件信息重命名修改时间复选框
            multipleFileInformationRenameModifyDateCheckBox.setForeground(Main.SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//设置字体颜色
            multipleFileInformationRenameModifyDateCheckBox.setBackground(Main.SettingState.themeColor ? DARK_DIALOG_MAIN_COLOR : LIGHT_DIALOG_MAIN_COLOR);//设置背景颜色
            multipleFileInformationRenameModifyDateCheckBox.setFont(new Font("微软雅黑", PLAIN, 21));//设置字体
            JCheckBox multipleFileInformationRenameAccessDateCheckBox = new JCheckBox(Main.SettingState.systemLanguage ? "Access Date" : "访问时间");//多文件信息重命名访问时间复选框
            multipleFileInformationRenameAccessDateCheckBox.setForeground(Main.SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//设置字体颜色
            multipleFileInformationRenameAccessDateCheckBox.setBackground(Main.SettingState.themeColor ? DARK_DIALOG_MAIN_COLOR : LIGHT_DIALOG_MAIN_COLOR);//设置背景颜色
            multipleFileInformationRenameAccessDateCheckBox.setFont(new Font("微软雅黑", PLAIN, 21));//设置字体
            JPanel multipleFileInformationRenameInformationPanel = new JPanel();//多文件信息重命名信息面板
            multipleFileInformationRenameInformationPanel.setBackground(Main.SettingState.themeColor ? DARK_DIALOG_MAIN_COLOR : LIGHT_DIALOG_MAIN_COLOR);//设置背景颜色
            multipleFileInformationRenameInformationPanel.setLayout(new FlowLayout(FlowLayout.LEFT));//设置布局
            multipleFileInformationRenameInformationPanel.add(multipleFileInformationRenameInformationLabel);
            multipleFileInformationRenameInformationPanel.add(multipleFileInformationRenameCreateDateCheckBox);
            multipleFileInformationRenameInformationPanel.add(multipleFileInformationRenameModifyDateCheckBox);
            multipleFileInformationRenameInformationPanel.add(multipleFileInformationRenameAccessDateCheckBox);
            JButton confirmMultipleFileInformationRenameButton = new JButton(Main.SettingState.systemLanguage ? "Confirm Rename" : "确认重命名");//确认多文件信息重命名按钮
            confirmMultipleFileInformationRenameButton.setPreferredSize(new Dimension(550, 35));//设置大小
            confirmMultipleFileInformationRenameButton.setFont(new Font("微软雅黑", PLAIN, 21));//设置字体
            confirmMultipleFileInformationRenameButton.setBackground(USER_LOG_BUTTON_COLOR);//背景颜色
            confirmMultipleFileInformationRenameButton.setFocusable(false);//不可聚焦
            confirmMultipleFileInformationRenameButton.setBorder(null);//无边框
            multipleFileInformationRenameUnifiedPrefixTextField.inputTextField.addKeyListener(new KeyAdapter() {//为多文件信息重命名统一前缀文本域添加键盘监听
                @Override
                public void keyPressed(KeyEvent e) {//如果键盘按下
                    if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_DOWN) {//如果按下回车或向下
                        String unifiedPrefixInput = multipleFileInformationRenameUnifiedPrefixTextField.inputTextField.getText();//获取输入文本
                        for (int i = 0; i < unifiedPrefixInput.length(); i++) {//遍历输入文本
                            char c = unifiedPrefixInput.charAt(i);//获取当前字符
                            if (c == '/' || c == '\\') {//如果是特殊字符
                                createBottomTipWindow(Main.SettingState.systemLanguage ? "No Special Character" : "请勿输入特殊字符");//提示
                                multipleFileInformationRenameUnifiedPrefixTextField.isWrong = true;//错误
                                multipleFileInformationRenameUnifiedPrefixTextField.revalidate();//重新验证
                                multipleFileInformationRenameUnifiedPrefixTextField.repaint();//重新绘制
                                return;//直接返回
                            }
                        }
                        multipleFileInformationRenameUnifiedPrefixTextField.isWrong = false;//正确
                        multipleFileInformationRenameUnifiedPrefixTextField.revalidate();//重新验证
                        multipleFileInformationRenameUnifiedPrefixTextField.repaint();//重新绘制
                        e.consume();//阻止默认行为
                    } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {//如果按下ESC
                        multipleFileInformationRenameUnifiedPrefixTextField.isWrong = false;//正确
                        multipleFileInformationRenameUnifiedPrefixTextField.revalidate();//重新验证
                        multipleFileInformationRenameUnifiedPrefixTextField.repaint();//重新绘制
                        multipleFileRenameDialog.requestFocusInWindow();//焦点返回菜单
                        e.consume();//阻止默认行为
                    }
                }
            });
            multipleFileInformationRenameUnifiedPrefixTextField.inputTextField.addFocusListener(new FocusAdapter() {//为多文件信息重命名统一前缀文本域添加聚焦监听
                @Override
                public void focusLost(FocusEvent e) {//如果失去聚焦
                    if (multipleFileInformationRenameUnifiedPrefixTextField.inputTextField.isVisible()) {//如果可见
                        String unifiedPrefixInput = multipleFileInformationRenameUnifiedPrefixTextField.inputTextField.getText();//获取输入文本
                        for (int i = 0; i < unifiedPrefixInput.length(); i++) {//遍历输入文本
                            char c = unifiedPrefixInput.charAt(i);//获取当前字符
                            if (c == '/' || c == '\\') {//如果是特殊字符
                                createBottomTipWindow(Main.SettingState.systemLanguage ? "No Special Character" : "请勿输入特殊字符");//提示
                                multipleFileInformationRenameUnifiedPrefixTextField.isWrong = true;//错误
                                multipleFileInformationRenameUnifiedPrefixTextField.revalidate();//重新验证
                                multipleFileInformationRenameUnifiedPrefixTextField.repaint();//重新绘制
                                return;//直接返回
                            }
                        }
                        multipleFileInformationRenameUnifiedPrefixTextField.isWrong = false;//正确
                        multipleFileInformationRenameUnifiedPrefixTextField.revalidate();//重新验证
                        multipleFileInformationRenameUnifiedPrefixTextField.repaint();//重新绘制
                    }
                }
            });
            multipleFileInformationRenameUnifiedPrefixTextField.inputTextField.addMouseListener(new MouseAdapter() {//为多文件信息重命名统一前缀文本域添加鼠标事件监听
                @Override
                public void mouseEntered(MouseEvent e) {//如果鼠标进入
                    hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Please Enter New File Name Unified Prefix (Cannot Contain Special Character '/' '\\')" : "请输入新文件名统一前缀（不可包含特殊字符 '/' '\\'）", multipleFileInformationRenameUnifiedPrefixTextField.inputTextField));//展示提示窗口（鼠标悬浮一秒后展示）
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
            multipleFileInformationRenameInformationLabel.addMouseListener(new MouseAdapter() {//为多文件信息重命名信息标签添加鼠标事件监听
                @Override
                public void mouseEntered(MouseEvent e) {//如果鼠标进入
                    hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Add Additional Information After New Unified Prefix File Name" : "在新文件名统一前缀后添加图片额外信息", multipleFileInformationRenameInformationLabel));//展示提示窗口（鼠标悬浮一秒后展示）
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
            multipleFileInformationRenameCreateDateCheckBox.addMouseListener(new MouseAdapter() {//为多文件信息重命名创建时间复选框添加鼠标事件监听
                @Override
                public void mouseEntered(MouseEvent e) {//如果鼠标进入
                    hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Add Create Date After New Unified Prefix File Name" : "在新文件名统一前缀后添加图片创建时间", multipleFileInformationRenameCreateDateCheckBox));//展示提示窗口（鼠标悬浮一秒后展示）
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
            multipleFileInformationRenameModifyDateCheckBox.addMouseListener(new MouseAdapter() {//为多文件信息重命名修改时间复选框添加鼠标事件监听
                @Override
                public void mouseEntered(MouseEvent e) {//如果鼠标进入
                    hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Add Modify Date After New Unified Prefix File Name" : "在新文件名统一前缀后添加图片修改时间", multipleFileInformationRenameModifyDateCheckBox));//展示提示窗口（鼠标悬浮一秒后展示）
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
            multipleFileInformationRenameAccessDateCheckBox.addMouseListener(new MouseAdapter() {//为多文件信息重命名访问时间复选框添加鼠标事件监听
                @Override
                public void mouseEntered(MouseEvent e) {//如果鼠标进入
                    hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Add Access Date After New Unified Prefix File Name" : "在新文件名统一前缀后添加图片访问时间", multipleFileInformationRenameAccessDateCheckBox));//展示提示窗口（鼠标悬浮一秒后展示）
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
            confirmMultipleFileInformationRenameButton.addActionListener(_ -> {//为确认多文件信息重命名按钮添加事件监听
                multipleFileInformationRenameUnifiedPrefixTextField.isWrong = false;//正确
                multipleFileInformationRenameUnifiedPrefixTextField.revalidate();//重新验证
                multipleFileInformationRenameUnifiedPrefixTextField.repaint();//重新绘制
                String unifiedPrefixInput = multipleFileInformationRenameUnifiedPrefixTextField.inputTextField.getText().trim();//获取统一前缀输入
                for (int i = 0; i < unifiedPrefixInput.length(); i++) {//遍历输入文本
                    char c = unifiedPrefixInput.charAt(i);//获取当前字符
                    if (c == '/' || c == '\\') {//如果是特殊字符
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "No Special Character" : "请勿输入特殊字符");//提示
                        multipleFileInformationRenameUnifiedPrefixTextField.isWrong = true;//错误
                        multipleFileInformationRenameUnifiedPrefixTextField.revalidate();//重新验证
                        multipleFileInformationRenameUnifiedPrefixTextField.repaint();//重新绘制
                        return;//直接返回
                    }
                }
                if (!multipleFileInformationRenameCreateDateCheckBox.isSelected() && !multipleFileInformationRenameModifyDateCheckBox.isSelected() && !multipleFileInformationRenameAccessDateCheckBox.isSelected()) {//如果都没有被选中
                    createBottomTipWindow(Main.SettingState.systemLanguage ? "Please Select At Least One Information" : "请至少选择一项信息");//提示
                    return;//直接返回
                }
                File targetDir = new File(currentFolder);//获取目标文件夹
                List<File> sources = new ArrayList<>();//原文件记录列表
                List<File> targets = new ArrayList<>();//目标文件记录列表
                try {
                    for (ThumbnailItem item : list) {//遍历列表
                        File oldFile = item.getFile();//存储旧文件
                        StringBuilder newFileNameStringBuilder = new StringBuilder(unifiedPrefixInput.trim());//创建新文件名字符串构造者
                        if (multipleFileInformationRenameCreateDateCheckBox.isSelected()) {//如果创建时间选中
                            if (!newFileNameStringBuilder.isEmpty()) {//如果不为空
                                newFileNameStringBuilder.append(" ");//添加空格
                            }
                            newFileNameStringBuilder.append(new SimpleDateFormat("yyyy-MM-dd HHmmss").format(Files.readAttributes(oldFile.toPath(), BasicFileAttributes.class).creationTime().toMillis()));//添加创建时间
                        }
                        if (multipleFileInformationRenameModifyDateCheckBox.isSelected()) {//如果修改时间选中
                            if (!newFileNameStringBuilder.isEmpty()) {//如果不为空
                                newFileNameStringBuilder.append(" ");//添加空格
                            }
                            newFileNameStringBuilder.append(new SimpleDateFormat("yyyy-MM-dd HHmmss").format(new Date(oldFile.lastModified())));//添加修改时间
                        }
                        if (multipleFileInformationRenameAccessDateCheckBox.isSelected()) {//如果访问时间选中
                            if (!newFileNameStringBuilder.isEmpty()) {//如果不为空
                                newFileNameStringBuilder.append(" ");//添加空格
                            }
                            newFileNameStringBuilder.append(new SimpleDateFormat("yyyy-MM-dd HHmmss").format(Files.readAttributes(oldFile.toPath(), BasicFileAttributes.class).lastAccessTime().toMillis()));//添加访问时间
                        }
                        String pictureSuffix = FilenameUtils.getExtension(oldFile.getName());//获取图片扩展名
                        File newFile;//创建新文件
                        if (!pictureSuffix.isEmpty()) {//如果不是空串，即找得到扩展名
                            newFile = new File(oldFile.getParent(), newFileNameStringBuilder.append(".").append(pictureSuffix).toString());//添加后缀
                        } else {//否则
                            newFile = new File(oldFile.getParent(), newFileNameStringBuilder.toString());//不添加
                        }
                        if (newFile.exists()) {//如果文件存在
                            String baseName = FilenameUtils.getBaseName(newFile.getName());//获取文件名称
                            String extension = FilenameUtils.getExtension(newFile.getName());//获取文件扩展名
                            int counter = 1;//计数器
                            while (newFile.exists()) {//如果该文件一直重复
                                String newName;//创建文件新名称，不断进行累加
                                if (Main.SettingState.renameStrategy) {//如果是数字后缀策略
                                    newName = baseName + "(" + counter++ + ")." + extension;//为文件添加数字后缀
                                } else {//否则是英文前缀策略
                                    newName = "NewName" + String.format("%04d", counter++) + baseName + "." + extension;//为文件添加英文前缀
                                }
                                if (Objects.equals(currentFolder, Main.SettingState.systemLanguage ? "My Cloud" : "我的云盘")) {//如果是云盘结点
                                    newFile = new File(spikeVisionCloudPath + "/.buffer").toPath().resolve(newName).toFile();//设置新文件
                                } else {//否则
                                    newFile = targetDir.toPath().resolve(newName).toFile();//设置新文件
                                }
                            }
                        }
                        Files.move(oldFile.toPath(), newFile.toPath());//覆盖原文件
                        sources.add(oldFile);//往列表添加旧文件
                        targets.add(newFile);//往列表添加新文件
                    }
                    if (itemHoverTipWindow != null) {//如果提示信息不为空
                        itemHoverTipWindow.dispose();//释放提示信息
                        itemHoverTipWindow = null;//提示信息置空
                    }
                } catch (IOException e) {
                    if (administratorJudgement()) {//管理员判断
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "Insufficient Permission, Run The Program With Administrator Privilege" : "权限不足，请使用管理员权限运行程序");//提示
                    } else {//否则
                        String errorMessage = e.getMessage().substring(e.getMessage().indexOf(':') + 1);//截断错误信息，只展示':'后的内容
                        handleErrorLog(e.getMessage());//处理错误日志
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "Failure Rename: " + errorMessage : "重命名失败：" + errorMessage);//提示
                    }
                }

                if (Objects.equals(currentFolder, Main.SettingState.systemLanguage ? "My Cloud" : "我的云盘")) {//如果是云盘结点
                    try {
                        List<String> newFileNameStringList = handleUserSaveUserUploadPicture(targets, false);//上传新文件
                        if (handleUserRemoveUserUploadPicture(sources.stream().map(File::getName).collect(Collectors.toList()), false)) {//清除旧文件
                            FileDisplayBottomBar.recordFileOperation(new FileDisplayBottomBar.FileOperation(FileDisplayBottomBar.HistoryOperationType.RENAME, sources, targets, null, null, newFileNameStringList, true));//记录文件复制操作，原文件列表，目标文件列表，旧文件完整原始路径
                            DirectoryTree.setCurrentFileList(handleUserLoadUserUploadPicture(null));//更新图片文件列表
                            createBottomTipWindow(Main.SettingState.systemLanguage ? "Successful Cloud Picture Rename" : "云盘图片重命名成功");//提示
                        } else {//否则
                            createBottomTipWindow(Main.SettingState.systemLanguage ? "Failed Cloud Picture Rename" : "云盘图片重命名失败");//提示
                        }
                    } catch (IOException e) {
                        handleErrorLog(e.getMessage());//处理错误日志
                        throw new RuntimeException(e);//捕获异常
                    }
                } else {//否则
                    FileDisplayBottomBar.recordFileOperation(new FileDisplayBottomBar.FileOperation(FileDisplayBottomBar.HistoryOperationType.RENAME, sources, targets, null, null, null, false));//记录文件复制操作，原文件列表，目标文件列表，旧文件完整原始路径
                    DirectoryTree.updateCurrentFileList(new File(currentFolder).listFiles());//更新文件夹
                }
                updateFileDisplayMainPanel(false);//通知更新
                multipleFileRenameDialog.dispose();//释放
            });
            confirmMultipleFileInformationRenameButton.addMouseListener(new MouseAdapter() {//为确认多文件信息重命名按钮添加鼠标监听
                @Override
                public void mouseEntered(MouseEvent e) {//如果鼠标进入
                    hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Confirm Rename" : "确认重命名", confirmMultipleFileInformationRenameButton));//展示提示窗口（鼠标悬浮一秒后展示）
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
            JPanel multipleFileInformationRenamePanel = new JPanel(new GridLayout(0, 1, 5, 5));//创建多文件信息重命名面板
            multipleFileInformationRenamePanel.setBorder(BorderFactory.createEmptyBorder(7, 10, 7, 10));//创建边框
            multipleFileInformationRenamePanel.setBackground(Main.SettingState.themeColor ? DARK_DIALOG_MAIN_COLOR : LIGHT_DIALOG_MAIN_COLOR);//设置背景颜色
            multipleFileInformationRenamePanel.add(multipleFileInformationRenameUnifiedPrefixTextField);
            multipleFileInformationRenamePanel.add(multipleFileInformationRenameInformationPanel);
            multipleFileInformationRenamePanel.add(confirmMultipleFileInformationRenameButton);

            JButton multipleFileNumberRenameSwitchButton = new JButton(Main.SettingState.systemLanguage ? "Rename By Number" : "编号重命名");//多文件编号重命名切换按钮
            JButton multipleFileInformationRenameSwitchButton = new JButton(Main.SettingState.systemLanguage ? "Rename By Information" : "信息重命名");//多文件信息重命名切换按钮
            multipleFileNumberRenameSwitchButton.setPreferredSize(new Dimension(275, 35));//设置大小
            multipleFileInformationRenameSwitchButton.setPreferredSize(new Dimension(275, 35));//设置大小
            multipleFileNumberRenameSwitchButton.setFont(new Font("微软雅黑", PLAIN, 20));//设置字体
            multipleFileInformationRenameSwitchButton.setFont(new Font("微软雅黑", PLAIN, 20));//设置字体
            multipleFileNumberRenameSwitchButton.setForeground(Main.SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//设置字体颜色
            multipleFileInformationRenameSwitchButton.setForeground(Main.SettingState.themeColor ? DARK_DIALOG_FONT_COLOR : LIGHT_DIALOG_FONT_COLOR);//设置字体颜色
            multipleFileNumberRenameSwitchButton.setBackground(Main.SettingState.themeColor ? DARK_LOG_IN_ACTIVATE_COLOR : LIGHT_LOG_IN_ACTIVATE_COLOR);//背景颜色
            multipleFileInformationRenameSwitchButton.setBackground(Main.SettingState.themeColor ? DARK_LOG_IN_DEACTIVATE_COLOR : LIGHT_LOG_IN_DEACTIVATE_COLOR);//背景颜色
            multipleFileNumberRenameSwitchButton.setFocusable(false);//不可聚焦
            multipleFileInformationRenameSwitchButton.setFocusable(false);//不可聚焦
            multipleFileNumberRenameSwitchButton.setBorder(null);//无边框
            multipleFileInformationRenameSwitchButton.setBorder(null);//无边框
            multipleFileNumberRenameSwitchButton.addMouseListener(new MouseAdapter() {//为多文件编号重命名切换按钮添加鼠标监听
                @Override
                public void mouseClicked(MouseEvent e) {//如果鼠标点击
                    if (!multipleFileNumberRenameUnifiedPrefixTextField.isShowing()) {//如果不展示
                        multipleFileNumberRenameUnifiedPrefixTextField.isWrong = false;//正确
                        multipleFileNumberRenameUnifiedPrefixTextField.revalidate();//重新验证
                        multipleFileNumberRenameUnifiedPrefixTextField.repaint();//重新绘制
                        multipleFileNumberRenameStartNumberTextField.isWrong = false;//正确
                        multipleFileNumberRenameStartNumberTextField.revalidate();//重新验证
                        multipleFileNumberRenameStartNumberTextField.repaint();//重新绘制
                        multipleFileNumberRenameNumberDigitTextField.isWrong = false;//正确
                        multipleFileNumberRenameNumberDigitTextField.revalidate();//重新验证
                        multipleFileNumberRenameNumberDigitTextField.repaint();//重新绘制
                        multipleFileNumberRenameSwitchButton.setBackground(Main.SettingState.themeColor ? DARK_LOG_IN_ACTIVATE_COLOR : LIGHT_LOG_IN_ACTIVATE_COLOR);//背景颜色
                        multipleFileInformationRenameSwitchButton.setBackground(Main.SettingState.themeColor ? DARK_LOG_IN_DEACTIVATE_COLOR : LIGHT_LOG_IN_DEACTIVATE_COLOR);//背景颜色
                        multipleFileRenameDialog.remove(multipleFileInformationRenamePanel);//移除
                        multipleFileRenameDialog.add(multipleFileNumberRenamePanel, BorderLayout.CENTER);//把密码登录面板添加到中心
                        multipleFileRenameDialog.setSize(new Dimension(multipleFileRenameDialog.getWidth(), multipleFileRenameDialog.getHeight() + 36));//设置大小
                        multipleFileRenameDialog.revalidate();//重新验证
                        multipleFileRenameDialog.repaint();//重新绘制
                        multipleFileNumberRenameUnifiedPrefixTextField.inputTextField.requestFocusInWindow();//聚焦
                    }
                }
            });
            multipleFileInformationRenameSwitchButton.addMouseListener(new MouseAdapter() {//为多文件信息重命名切换按钮添加鼠标监听
                @Override
                public void mouseClicked(MouseEvent e) {//如果鼠标点击
                    if (!multipleFileInformationRenameUnifiedPrefixTextField.isShowing()) {//如果不展示
                        multipleFileInformationRenameUnifiedPrefixTextField.isWrong = false;//正确
                        multipleFileInformationRenameUnifiedPrefixTextField.revalidate();//重新验证
                        multipleFileInformationRenameUnifiedPrefixTextField.repaint();//重新绘制
                        multipleFileNumberRenameSwitchButton.setBackground(Main.SettingState.themeColor ? DARK_LOG_IN_DEACTIVATE_COLOR : LIGHT_LOG_IN_DEACTIVATE_COLOR);//背景颜色
                        multipleFileInformationRenameSwitchButton.setBackground(Main.SettingState.themeColor ? DARK_LOG_IN_ACTIVATE_COLOR : LIGHT_LOG_IN_ACTIVATE_COLOR);//背景颜色
                        multipleFileRenameDialog.remove(multipleFileNumberRenamePanel);//移除
                        multipleFileRenameDialog.add(multipleFileInformationRenamePanel, BorderLayout.CENTER);//把验证码登录面板添加到中心
                        multipleFileRenameDialog.setSize(new Dimension(multipleFileRenameDialog.getWidth(), multipleFileRenameDialog.getHeight() - 36));//设置大小
                        multipleFileRenameDialog.revalidate();//重新验证
                        multipleFileRenameDialog.repaint();//重新绘制
                        multipleFileInformationRenameUnifiedPrefixTextField.inputTextField.requestFocusInWindow();//聚焦
                    }
                }
            });
            JPanel multipleFileRenameSwitchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));//多文件重命名切换面板：设置布局管理器为中心流式布局
            multipleFileRenameSwitchPanel.setBackground(Main.SettingState.themeColor ? DARK_DIALOG_MAIN_COLOR : LIGHT_DIALOG_MAIN_COLOR);//设置背景颜色
            multipleFileRenameSwitchPanel.add(multipleFileNumberRenameSwitchButton);
            multipleFileRenameSwitchPanel.add(multipleFileInformationRenameSwitchButton);
            multipleFileRenameSwitchPanel.addComponentListener(new ComponentAdapter() {//为登录切换面板添加组件监听
                @Override
                public void componentResized(ComponentEvent e) {//如果大小变化
                    multipleFileNumberRenameSwitchButton.setPreferredSize(new Dimension(multipleFileRenameSwitchPanel.getWidth() / 2, 35));//重新设置按钮大小
                    multipleFileInformationRenameSwitchButton.setPreferredSize(new Dimension(multipleFileRenameSwitchPanel.getWidth() / 2, 35));//重新设置按钮大小
                }
            });

            multipleFileRenameDialog = new JDialog(Main.diskManagementSystemFrame, Main.SettingState.systemLanguage ? "Rename" : "重命名", true);//创建多文件重命名对话窗口
            multipleFileRenameDialog.setIconImage(new ImageIcon("src/material/image/rename.png").getImage());//设置图标
            multipleFileRenameDialog.setLayout(new BorderLayout());//设置布局
            multipleFileRenameDialog.add(multipleFileRenameSwitchPanel, BorderLayout.NORTH);//多文件重命名切换面板添加到北部
            multipleFileRenameDialog.add(multipleFileNumberRenamePanel, BorderLayout.CENTER);//多文件编号重命名面板添加到中心
            multipleFileRenameDialog.setAlwaysOnTop(Main.SettingState.windowState);//设置永远在最上层
            multipleFileRenameDialog.pack();//设置合适
            multipleFileRenameDialog.setLocation(Main.screenSize.width / 2 - multipleFileRenameDialog.getWidth() / 2, Main.screenSize.height / 2 - multipleFileRenameDialog.getHeight() / 2);//设置位置
            JRootPane multipleFileRenameDialogRoot = multipleFileRenameDialog.getRootPane();//获取重命名窗口的根
            multipleFileRenameDialogRoot.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "closeMultipleFileRenameDialog");//为根设置窗口关闭ESC按键绑定
            multipleFileRenameDialogRoot.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "closeMultipleFileRenameDialog");//为根设置窗口关闭F2按键绑定
            multipleFileRenameDialogRoot.getActionMap().put("closeMultipleFileRenameDialog", new AbstractAction() {//当ESC按键执行时
                public void actionPerformed(ActionEvent event) {//行为执行
                    multipleFileRenameDialog.dispatchEvent(new WindowEvent(multipleFileRenameDialog, WindowEvent.WINDOW_CLOSING));//关闭窗口
                }
            });
            multipleFileRenameDialog.addWindowListener(new WindowAdapter() {//为多文件重命名窗口添加窗口监听
                @Override
                public void windowClosing(WindowEvent e) {//如果窗口正在关闭
                    if (bottomTipWindow != null && bottomTipWindow.isVisible()) {//如果底部提示窗口可见
                        bottomTipWindow.dispose();//底部提示窗口置空
                    }
                }
            });
            multipleFileRenameDialog.setVisible(true);//设置可见
        }
    }

    public static void handleRemove() {//处理删除
        List<ThumbnailItem> list = getSelectionThumbnailItemList();//获取选中文件列表
        if (!list.isEmpty()) {//如果非空
            if (itemHoverTipWindow != null) {//如果提示信息不为空
                itemHoverTipWindow.dispose();//释放提示信息
                itemHoverTipWindow = null;//提示信息置空
            }
            if (Main.SettingState.deleteTip) {//如果关闭删除提示
                List<File> operationRecycleFiles = new ArrayList<>();//临时存储回收文件列表
                SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS");//设置时间戳格式（不采用含冒号和空格的格式防止系统不允许，精确到毫秒防止文件冲突）
                Path recycleBin = Path.of("D:/Recycle Bin");//获取自定义回收站路径
                if (!Files.exists(recycleBin)) {//如果不存在自定义回收站路径
                    try {
                        Files.createDirectories(recycleBin);//就创建路径
                    } catch (IOException e) {
                        handleErrorLog(e.getMessage());//处理错误日志
                        throw new RuntimeException(e);//捕获异常
                    }
                }
                new SwingWorker<Void, Void>() {//创建线程
                    @Override
                    public Void doInBackground() {//在背景运行
                        try {
                            List<String> removeFileNameStringList = new ArrayList<>();//删除文件名字符串列表
                            for (ThumbnailItem item : list) {//遍历列表
                                File file = item.getFile();//获取原文件
                                removeFileNameStringList.add(file.getName());//记录
                                String timestamp = timestampFormat.format(System.currentTimeMillis());//获取当前时间戳
                                String safeBaseName = FilenameUtils.getBaseName(file.getName()).replaceAll("[^a-zA-Z0-9_-]", "");//生成安全文件名：时间戳+原始文件名哈希值
                                String uniqueName = String.format("%s%s_%s_%d_%s.%s", "$", timestamp, safeBaseName, file.hashCode(), file.getName(), FilenameUtils.getExtension(file.getName()));//生成独特回收文件名：包含删除时间（精确到毫秒）与文件哈希值，防止快速连续操作冲突
                                Path target = recycleBin.resolve(uniqueName);//把回收站地址和文件名进行拼接
                                Files.move(file.toPath(), target, StandardCopyOption.REPLACE_EXISTING);//通过替换方案把原文件全部移动到回收站
                                operationRecycleFiles.add(target.toFile());//记录被删除文件
                                Thread.sleep(1);//暂停1ms保证每个文件有不同的时间戳
                            }
                            if (Objects.equals(currentFolder, Main.SettingState.systemLanguage ? "My Cloud" : "我的云盘")) {//如果是云盘结点
                                FileDisplayBottomBar.recordFileOperation(new FileDisplayBottomBar.FileOperation(FileDisplayBottomBar.HistoryOperationType.DELETE, list.stream().map(ThumbnailItem::getFile).collect(Collectors.toList()), null, currentFolder, new ArrayList<>(operationRecycleFiles), removeFileNameStringList, true));//创建副本
                                handleUserRemoveUserUploadPicture(removeFileNameStringList, false);//删除文件
                            } else {//否则
                                FileDisplayBottomBar.recordFileOperation(new FileDisplayBottomBar.FileOperation(FileDisplayBottomBar.HistoryOperationType.DELETE, list.stream().map(ThumbnailItem::getFile).collect(Collectors.toList()), null, currentFolder, new ArrayList<>(operationRecycleFiles), removeFileNameStringList, false));//创建副本
                            }
                        } catch (IOException | InterruptedException e) {
                            if (administratorJudgement()) {//管理员判断
                                createBottomTipWindow(Main.SettingState.systemLanguage ? "Insufficient Permission, Run The Program With Administrator Privilege" : "权限不足，请使用管理员权限运行程序");//提示
                            } else {//否则
                                handleErrorLog(e.getMessage());//处理错误日志
                                SwingUtilities.invokeLater(() -> createBottomTipWindow(Main.SettingState.systemLanguage ? "Failure Remove: " + e.getMessage() : "删除失败：" + e.getMessage()));//捕获异常
                            }
                        }
                        return null;//返回空
                    }

                    @Override
                    protected void done() {//完成时
                        if (Objects.equals(currentFolder, Main.SettingState.systemLanguage ? "My Cloud" : "我的云盘")) {//如果是云盘结点
                            try {
                                DirectoryTree.setCurrentFileList(handleUserLoadUserUploadPicture(null));//更新图片文件列表
                            } catch (IOException e) {
                                handleErrorLog(e.getMessage());//处理错误日志
                                throw new RuntimeException(e);//捕获异常
                            }
                        } else {//否则
                            DirectoryTree.updateCurrentFileList(new File(currentFolder).listFiles());//更新文件夹
                        }
                        new Timer(100, evt -> {//添加100ms延时确保组件完全卸载再通知更新
                            updateFileDisplayMainPanel(false);//更新面板
                            fileManipulationButtonEnableJudgement(list.size());//更新文件操作按钮状态
                            ((Timer) evt.getSource()).stop();//停止计时器
                        }).start();//开始计时器
                        if (Main.SettingState.customRecycleCleanTime == 0 && !Main.SettingState.recycleStrategy) {//如果立即清理且没有关闭清理
                            FileDisplayPopupMenu.handleAutoEmptyRecycleBin();//调用自动清空图片回收站
                        }
                        if (itemHoverTipWindow != null) {//如果提示信息不为空
                            itemHoverTipWindow.dispose();//释放提示信息
                            itemHoverTipWindow = null;//提示信息置空
                        }
                    }
                }.execute();//开始执行
            } else {//否则开启删除提示
                if (!Main.SettingState.effectState && !Main.SettingState.masterState) {//如果没有关闭音效
                    if (removeTipClip.isRunning()) {//如果正在运行
                        removeTipClip.stop();//停止
                    }
                    removeTipClip.setFramePosition(0);//重置播放位置
                    removeTipClip.start();//开始播放音效
                }
                int confirm = JOptionPane.showConfirmDialog(Main.diskManagementSystemFrame, (Main.SettingState.systemLanguage ? "Are You Sure To Remove The " : "确定要删除选中的 ") + list.size() + (Main.SettingState.systemLanguage ? " Selected Picture?\nRemoved Picture Are Placed In Recycle Bin\nYou Can Undo This Operation To Recover\nPicture Would Only Completely Removed By Emptying Recycle Bin" : " 张图片吗？\n删除后的图片将放入回收站\n可以撤销删除操作恢复图片\n只有清空回收站才能彻底删除图片"), Main.SettingState.systemLanguage ? "Remove Confirm" : "确认删除", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);//创建确认信息
                if (confirm == JOptionPane.YES_OPTION) {//如果确认
                    List<File> operationRecycleFiles = new ArrayList<>();//临时存储回收文件列表
                    SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS");//设置时间戳格式（不采用含冒号和空格的格式防止系统不允许，精确到毫秒防止文件冲突）
                    Path recycleBin = Path.of("D:/Recycle Bin");//获取自定义回收站路径
                    if (!Files.exists(recycleBin)) {//如果不存在自定义回收站路径
                        try {
                            Files.createDirectories(recycleBin);//就创建路径
                        } catch (IOException e) {
                            handleErrorLog(e.getMessage());//处理错误日志
                            throw new RuntimeException(e);//捕获异常
                        }
                    }
                    new SwingWorker<Void, Void>() {//创建线程
                        @Override
                        public Void doInBackground() {//在背景运行
                            try {
                                List<String> removeFileNameStringList = new ArrayList<>();//删除文件名字符串列表
                                for (ThumbnailItem item : list) {//遍历列表
                                    File file = item.getFile();//获取原文件
                                    removeFileNameStringList.add(file.getName());//记录
                                    long time = System.currentTimeMillis();//当前时间
                                    String timestamp = timestampFormat.format(time);//获取当前时间戳
                                    String uniqueName = String.format("%s%s_%s_%d_%s", "$", timestamp, time, file.hashCode(), file.getName());//生成独特回收文件名：包含删除时间（精确到毫秒）、删除时间戳与文件哈希值，防止快速连续操作冲突
                                    Path target = recycleBin.resolve(uniqueName);//把回收站地址和文件名进行拼接
                                    Files.move(file.toPath(), target, StandardCopyOption.REPLACE_EXISTING);//通过替换方案把原文件全部移动到回收站
                                    operationRecycleFiles.add(target.toFile());//记录被删除文件
                                    Thread.sleep(1);//暂停1ms保证每个文件有不同的时间戳
                                }
                                if (Objects.equals(currentFolder, Main.SettingState.systemLanguage ? "My Cloud" : "我的云盘")) {//如果是云盘结点
                                    FileDisplayBottomBar.recordFileOperation(new FileDisplayBottomBar.FileOperation(FileDisplayBottomBar.HistoryOperationType.DELETE, list.stream().map(ThumbnailItem::getFile).collect(Collectors.toList()), null, currentFolder, new ArrayList<>(operationRecycleFiles), removeFileNameStringList, true));//创建副本
                                    handleUserRemoveUserUploadPicture(removeFileNameStringList, false);//删除文件
                                } else {//否则
                                    FileDisplayBottomBar.recordFileOperation(new FileDisplayBottomBar.FileOperation(FileDisplayBottomBar.HistoryOperationType.DELETE, list.stream().map(ThumbnailItem::getFile).collect(Collectors.toList()), null, currentFolder, new ArrayList<>(operationRecycleFiles), removeFileNameStringList, false));//创建副本
                                }
                            } catch (IOException | InterruptedException e) {
                                if (administratorJudgement()) {//管理员判断
                                    createBottomTipWindow(Main.SettingState.systemLanguage ? "Insufficient Permission, Run The Program With Administrator Privilege" : "权限不足，请使用管理员权限运行程序");//提示
                                } else {//否则
                                    handleErrorLog(e.getMessage());//处理错误日志
                                    createBottomTipWindow(Main.SettingState.systemLanguage ? "Failure Remove: " + e.getMessage() : "删除失败：" + e.getMessage());//捕获异常
                                }
                            }
                            return null;//返回空
                        }

                        @Override
                        protected void done() {//完成时
                            if (Objects.equals(currentFolder, Main.SettingState.systemLanguage ? "My Cloud" : "我的云盘")) {//如果是云盘结点
                                try {
                                    DirectoryTree.setCurrentFileList(handleUserLoadUserUploadPicture(null));//更新图片文件列表
                                } catch (IOException e) {
                                    handleErrorLog(e.getMessage());//处理错误日志
                                    throw new RuntimeException(e);//捕获异常
                                }
                            } else {//否则
                                DirectoryTree.updateCurrentFileList(new File(currentFolder).listFiles());//更新文件夹
                            }
                            new Timer(100, evt -> {//添加100ms延时确保组件完全卸载再通知更新
                                updateFileDisplayMainPanel(false);//更新面板
                                fileManipulationButtonEnableJudgement(list.size());//更新文件操作按钮状态
                                ((Timer) evt.getSource()).stop();//停止计时器
                            }).start();//开始计时器
                            if (Main.SettingState.customRecycleCleanTime == 0 && !Main.SettingState.recycleStrategy) {//如果立即清理且没有关闭清理
                                FileDisplayPopupMenu.handleAutoEmptyRecycleBin();//调用自动清空图片回收站
                            }
                            if (itemHoverTipWindow != null) {//如果提示信息不为空
                                itemHoverTipWindow.dispose();//释放提示信息
                                itemHoverTipWindow = null;//提示信息置空
                            }
                        }
                    }.execute();//开始执行
                }
            }
        }
    }

    public static void showButtonHoverTipWindow(String tipInformation, JComponent component) {//显示提示信息
        if (component.isEnabled() && component.isShowing()) {//如果组件有效且正在展示
            Point mousePos = MouseInfo.getPointerInfo().getLocation();//获取鼠标位置
            Point componentPos = component.getLocationOnScreen();//获取组件位置
            Rectangle bounds = new Rectangle(componentPos, component.getSize());//创建矩形，左上角为组件坐标，大小为组件大小，即与组件同大
            if (bounds.contains(mousePos)) {//如果矩形内包含鼠标
                createButtonHoverTipWindow(tipInformation, mousePos);//创建按钮悬浮提示窗口
            }
        }
    }

    public static void createButtonHoverTipWindow(String tipInformation, Point mousePos) {//创建提示窗口
        if (buttonHoverTipWindow != null) {//如果提示窗口存在
            buttonHoverTipWindow.dispose();//关闭之前的提示窗口
        }
        if (Main.editFrame.isVisible()) {//如果当前在幻灯片窗口
            buttonHoverTipWindow = new JWindow(Main.editFrame);//创建提示窗口（设置父组件防止覆盖）
        } else {//否则
            if (insertImageDialog.isVisible()) {//如果插入图片窗口可见
                buttonHoverTipWindow = new JWindow(insertImageDialog);//创建提示窗口（设置父组件防止覆盖）
            } else if (suggestionFeedbackDialog.isVisible()) {//如果建议反馈窗口可见
                buttonHoverTipWindow = new JWindow(suggestionFeedbackDialog);//创建提示窗口（设置父组件防止覆盖）
            } else if (settingDialog.isVisible()) {//如果设置窗口可见
                buttonHoverTipWindow = new JWindow(settingDialog);//创建提示窗口（设置父组件防止覆盖）
            } else if (logInDialog.isVisible()) {//如果登录窗口可见
                buttonHoverTipWindow = new JWindow(logInDialog);//创建提示窗口（设置父组件防止覆盖）
            } else if (registerDialog.isVisible()) {//如果注册窗口可见
                buttonHoverTipWindow = new JWindow(registerDialog);//创建提示窗口（设置父组件防止覆盖）
            } else if (changeUserAccountDialog.isVisible()) {//如果更改用户名窗口可见
                buttonHoverTipWindow = new JWindow(changeUserAccountDialog);//创建提示窗口（设置父组件防止覆盖）
            } else if (changeUserPhoneDialog.isVisible()) {//如果更改手机号窗口可见
                buttonHoverTipWindow = new JWindow(changeUserPhoneDialog);//创建提示窗口（设置父组件防止覆盖）
            } else if (changeUserPasswordDialog.isVisible()) {//如果更改密码窗口可见
                buttonHoverTipWindow = new JWindow(changeUserPasswordDialog);//创建提示窗口（设置父组件防止覆盖）
            } else if (logOutDialog.isVisible()) {//如果注销窗口可见
                buttonHoverTipWindow = new JWindow(logOutDialog);//创建提示窗口（设置父组件防止覆盖）
            } else if (userDialog.isVisible()) {//如果用户窗口可见
                buttonHoverTipWindow = new JWindow(userDialog);//创建提示窗口（设置父组件防止覆盖）
            } else if (singleFileRenameDialog.isVisible()) {//如果用户窗口可见
                buttonHoverTipWindow = new JWindow(singleFileRenameDialog);//创建提示窗口（设置父组件防止覆盖）
            } else if (multipleFileRenameDialog.isVisible()) {//如果用户窗口可见
                buttonHoverTipWindow = new JWindow(multipleFileRenameDialog);//创建提示窗口（设置父组件防止覆盖）
            } else {//否则
                buttonHoverTipWindow = new JWindow(Main.diskManagementSystemFrame);//创建提示窗口（设置父组件防止覆盖）
            }
        }
        JLabel content = new JLabel(tipInformation);//创建内容标签
        content.setBackground(new Color(250, 250, 250));//设置背景颜色
        content.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.GRAY), BorderFactory.createEmptyBorder(5, 5, 5, 5)));//用边框可以不用把文字设置到组件中心也能让文字左右隔出距离
        content.setFont(new Font("楷体", PLAIN, 14));//设置字体
        buttonHoverTipWindow.setContentPane(content);//放入内容
        buttonHoverTipWindow.pack();//合适化
        int x = mousePos.x;//x坐标
        int y = mousePos.y + 18;//y坐标向下偏移18px避免遮挡鼠标
        if (x + buttonHoverTipWindow.getWidth() > Main.screenSize.width) {//防止溢出屏幕
            x -= x + buttonHoverTipWindow.getWidth() - Main.screenSize.width;//x减去溢出部分
        }
        if (y + buttonHoverTipWindow.getHeight() > Main.screenSize.height) {//防止溢出屏幕
            y = mousePos.y - 28;//y坐标向上偏移28px：不能只是单纯减去溢出部分，因为鼠标指针自身也会遮挡窗口
        }
        buttonHoverTipWindow.setLocation(x, y);//设置位置
        buttonHoverTipWindow.setAlwaysOnTop(Main.SettingState.windowState);//设置永远在最上层
        buttonHoverTipWindow.setFocusableWindowState(false);//设置不可聚焦
        buttonHoverTipWindow.setVisible(true);//设置可见
    }

    private static void configureDirectoryField() {//配置目录文本域
        JTextField directoryTextField = directoryField.getInputTextField();//获取文本域
        JButton clearButton = directoryField.getActionButton();//获取按钮

        directoryTextField.addActionListener(_ -> {//为目录文本域添加事件监听，处理回车事件
            String path = directoryTextField.getText().trim();//获取路径文本
            if (!path.isEmpty() && !path.equals(currentFolder)) {//如果路径非空且发生变化
                path = "D:/" + path;//增加前缀
                if (path.equals(Main.SettingState.systemLanguage ? "My Cloud" : "我的云盘")) {//如果是云盘
                    if (!Objects.equals(Main.SettingState.userAccount, "")) {//如果用户没有退出登录
                        try {
                            DirectoryTree.setCurrentFileList(handleUserLoadUserUploadPicture(null));//更新当前文件列表
                            updateFileDisplayMainPanel(false);//通知更新文件展示面板
                            directoryManipulationButtonEnableJudgement();//按钮判断
                        } catch (IOException e) {
                            handleErrorLog(e.getMessage());//处理错误日志
                            throw new RuntimeException(e);//捕获异常
                        }
                    } else {//否则
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "Nonexistent Path" : "路径不存在");//提示
                    }
                } else {//否则
                    File target = new File(path);//根据路径创建文件
                    if (target.exists() && target.isDirectory()) {//如果文件存在且是文件夹
                        DirectoryTree.setCurrentFileList(detectFile(target.listFiles()));//设置图片文件列表为当前文件夹
                        updateFileDisplayMainPanel(false);//通知更新图片预览面板
                        directoryManipulationButtonEnableJudgement();//按钮判断
                    } else {//否则
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "Nonexistent Path" : "路径不存在");//提示
                    }
                }
            }
        });
        directoryTextField.addFocusListener(new FocusAdapter() {//为目录文本域添加聚焦监听
            @Override
            public void focusLost(FocusEvent e) {//当聚焦丢失时
                String path = directoryTextField.getText().trim();//获取路径文本
                if (!path.isEmpty() && !path.equals(currentFolder)) {//如果路径非空且发生变化
                    path = "D:/" + path;//增加前缀
                    if (path.equals(Main.SettingState.systemLanguage ? "My Cloud" : "我的云盘")) {//如果是云盘
                        if (Main.SettingState.userAccount.isEmpty()) {//如果用户退出登录
                            createBottomTipWindow(Main.SettingState.systemLanguage ? "Nonexistent Path" : "路径不存在");//提示
                            directoryTextField.setText(currentFolder.substring(3));//恢复当前路径
                        } else {//否则
                            try {
                                DirectoryTree.setCurrentFileList(handleUserLoadUserUploadPicture(null));//更新当前文件列表
                                updateFileDisplayMainPanel(false);//通知更新文件展示面板
                                directoryManipulationButtonEnableJudgement();//按钮判断
                            } catch (IOException ex) {
                                handleErrorLog(ex.getMessage());//处理错误日志
                                throw new RuntimeException(ex);//捕获异常
                            }
                        }
                    } else {//否则
                        File target = new File(path);//根据路径创建文件
                        if (target.exists() && target.isDirectory()) {//如果文件存在且是文件夹
                            DirectoryTree.setCurrentFileList(detectFile(target.listFiles()));//设置当前文件列表为当前文件夹
                        } else {//否则
                            createBottomTipWindow(Main.SettingState.systemLanguage ? "Nonexistent Path" : "路径不存在");//提示
                            directoryTextField.setText(currentFolder.substring(3));//恢复当前路径
                            DirectoryTree.setCurrentFileList(detectFile(new File(currentFolder).listFiles()));//设置图片文件列表为当前文件夹
                        }
                        updateFileDisplayMainPanel(false);//通知更新图片预览面板
                        directoryManipulationButtonEnableJudgement();//按钮判断
                    }
                }
            }
        });
        directoryTextField.addKeyListener(new KeyAdapter() {//为目录文本域添加键盘监听
            @Override
            public void keyPressed(KeyEvent e) {//如果键盘按下
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {//如果是ESC
                    if (currentFolder != null) {//如果不为空
                        if (currentFolder.equals(Main.SettingState.systemLanguage ? "My Cloud" : "我的云盘")) {//如果是云盘
                            directoryTextField.setText(currentFolder.substring(3));//恢复当前路径
                            try {
                                DirectoryTree.setCurrentFileList(handleUserLoadUserUploadPicture(null));//更新当前文件列表
                                updateFileDisplayMainPanel(false);//通知更新文件展示面板
                                directoryManipulationButtonEnableJudgement();//按钮判断
                            } catch (IOException ex) {
                                handleErrorLog(ex.getMessage());//处理错误日志
                                throw new RuntimeException(ex);//捕获异常
                            }
                        } else {//否则
                            directoryTextField.setText(currentFolder.substring(3));//恢复当前路径
                            DirectoryTree.setCurrentFileList(detectFile(new File(currentFolder).listFiles()));//设置当前文件列表为当前文件夹
                            updateFileDisplayMainPanel(false);//通知更新文件展示面板
                            directoryManipulationButtonEnableJudgement();//按钮判断
                        }
                    }
                    mainPanel.requestFocusInWindow();//焦点返回主面板
                    e.consume();//阻止默认行为
                }
            }
        });
        directoryTextField.addMouseListener(new MouseAdapter() {//为目录文本域添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? (directoryTextField.getText().isEmpty() ? "Current Directory (F4 / Ctrl + L)" : "Current Directory (F4 / Ctrl + L): " + directoryTextField.getText()) : (directoryTextField.getText().isEmpty() ? "当前路径（F4 / Ctrl + L）" : "当前路径（F4 / Ctrl + L）：" + directoryTextField.getText()), directoryTextField));//展示提示窗口（鼠标悬浮一秒后展示）
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

        clearButton.addActionListener(_ -> {//为清空按钮添加事件监听
            if (currentFolder != null && !Objects.equals(directoryTextField.getText(), currentFolder)) {//如果不为空且路径变化
                directoryTextField.setText(currentFolder.substring(3));//设置为当前文件夹
                if (Objects.equals(currentFolder, Main.SettingState.systemLanguage ? "My Cloud" : "我的云盘")) {//如果是云盘结点
                    try {
                        DirectoryTree.setCurrentFileList(handleUserLoadUserUploadPicture(null));//更新图片文件列表
                    } catch (IOException e) {
                        handleErrorLog(e.getMessage());//处理错误日志
                        throw new RuntimeException(e);//捕获异常
                    }
                } else {//否则
                    DirectoryTree.setCurrentFileList(detectFile(new File(currentFolder).listFiles()));//设置当前文件列表为当前文件夹
                }
                updateFileDisplayMainPanel(false);//通知更新文件展示面板
                directoryManipulationButtonEnableJudgement();//按钮判断
            }
        });
        clearButton.addMouseListener(new MouseAdapter() {//为清空按钮添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Clear Navigation Path (ESC)" : "清空导航路径（ESC）", clearButton));//展示提示窗口（鼠标悬浮一秒后展示）
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
    }

    private static void configureSearchField() {//配置搜索文本域
        JTextField searchTextField = searchField.getInputTextField();//获取文本域
        JButton searchButton = searchField.getActionButton();//获取按钮
        Timer searchTimer = new Timer(500, e -> {//搜索逻辑（500ms后触发防抖）
            performSearch();//进行搜索
            ((Timer) e.getSource()).stop();//确保计时器停止
        });
        searchTimer.setRepeats(false);//设置计时器不重复

        searchTextField.getDocument().addDocumentListener(new DocumentListener() {//为搜索文本域添加文本监听
            public void changedUpdate(DocumentEvent e) {//如果改变更新
                searchTimer.restart();//重新开始计时器
            }

            public void insertUpdate(DocumentEvent e) {//如果输入更新
                searchTimer.restart();//重新开始计时器
            }

            public void removeUpdate(DocumentEvent e) {//如果移除更新
                searchTimer.restart();//重新开始计时器
            }
        });
        searchTextField.addKeyListener(new KeyAdapter() {//为搜索文本域添加键盘监听
            @Override
            public void keyPressed(KeyEvent e) {//如果键盘按下
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {//如果按下回车
                    performSearch();//进行搜索
                    e.consume();//阻止默认行为
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {//如果按下ESC
                    mainPanel.requestFocusInWindow();//焦点返回主面板
                    e.consume();//阻止默认行为
                }
            }
        });
        searchTextField.addFocusListener(new FocusAdapter() {//为搜索文本域添加聚焦监听
            @Override
            public void focusLost(FocusEvent e) {//如果失去聚焦
                performSearch();//进行搜索
            }
        });
        searchTextField.addMouseListener(new MouseAdapter() {//为搜索文本域添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Search For Picture In This Directory (F3 / Ctrl + F) Space Segmentation Is Supported For Multiple Keyword" : "搜索该路径下的图片（F3 / Ctrl + F）支持空格分割多关键词", searchTextField));//展示提示窗口（鼠标悬浮一秒后展示）
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

        searchButton.addActionListener(_ -> performSearch());//如果点击按钮，就进行搜索
        searchButton.addMouseListener(new MouseAdapter() {//为搜索按钮添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Search (Enter)" : "开始搜索（Enter）", searchButton));//展示提示窗口（鼠标悬浮一秒后展示）
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
    }

    private static void performSearch() {//进行搜索
        String keyword = searchField.getInputTextField().getText().trim();//获取搜索文本
        if (currentFolder != null) {//如果当前路径不为空
            if (keyword.isEmpty()) {//如果搜索文本为空
                if (Objects.equals(currentFolder, Main.SettingState.systemLanguage ? "My Cloud" : "我的云盘")) {//如果是云盘结点
                    try {
                        DirectoryTree.setCurrentFileList(handleUserLoadUserUploadPicture(null));//更新图片文件列表
                    } catch (IOException ex) {
                        handleErrorLog(ex.getMessage());//处理错误日志
                        throw new RuntimeException(ex);//捕获异常
                    }
                } else {//否则
                    DirectoryTree.updateCurrentFileList(new File(currentFolder).listFiles());//更新文件夹
                }
                updateFileDisplayMainPanel(false);//通知更新
            } else {//否则
                if (Objects.equals(currentFolder, Main.SettingState.systemLanguage ? "My Cloud" : "我的云盘")) {//如果是云盘结点
                    File currentDir = new File(spikeVisionCloudPath + "/.buffer");//实时获取当前目录文件
                    File[] allFiles = currentDir.listFiles();//获取当前文件夹下所有文件
                    if (allFiles != null) {//如果文件列表非空
                        List<File> result;//搜索结果文件列表
                        String[] keywords = keyword.split("\\s+");//把所有空格分割的关键字分割，获取关键字字符串数组
                        if (Main.SettingState.searchStrategy) {//如果关闭忽略
                            result = Arrays.stream(allFiles).filter(f -> Arrays.stream(keywords).allMatch(word -> f.getName().contains(word))).toList();//通过流过滤文件，获取结果列表
                        } else {//否则开启忽略
                            String[] lowerCaseKeywords = Arrays.stream(keywords).map(String::toLowerCase).toArray(String[]::new);//先将关键字转化为小写以忽略大小写
                            result = Arrays.stream(allFiles).filter(f -> {//通过流过滤文件
                                return Arrays.stream(lowerCaseKeywords).allMatch(f.getName().toLowerCase()::contains);//如果小写关键字匹配小写文件名
                            }).toList();//最终过滤获取结果列表
                        }
                        try {
                            DirectoryTree.setCurrentFileList(handleUserLoadUserUploadPicture(result.stream().map(File::getName).collect(Collectors.toList())));//更新图片文件列表
                        } catch (IOException e) {
                            handleErrorLog(e.getMessage());//处理错误日志
                            throw new RuntimeException(e);//捕获异常
                        }
                    }
                } else {//否则
                    DirectoryTree.updateCurrentFileList(new File(currentFolder).listFiles());//更新文件夹
                    updateFileDisplayMainPanel(false);//通知更新
                    File[] allFiles = FileDisplayMainPanel.currentFileList;//获取当前文件夹下所有图片文件
                    if (allFiles != null) {//如果文件列表非空
                        List<File> result;//搜索结果文件列表
                        String[] keywords = keyword.split("\\s+");//把所有空格分割的关键字分割，获取关键字字符串数组
                        if (Main.SettingState.searchStrategy) {//如果关闭忽略
                            result = Arrays.stream(allFiles).filter(f -> Arrays.stream(keywords).allMatch(word -> f.getName().contains(word))).toList();//通过流过滤文件，获取结果列表
                        } else {//否则开启忽略
                            String[] lowerCaseKeywords = Arrays.stream(keywords).map(String::toLowerCase).toArray(String[]::new);//先将关键字转化为小写以忽略大小写
                            result = Arrays.stream(allFiles).filter(f -> {//通过流过滤文件
                                return Arrays.stream(lowerCaseKeywords).allMatch(f.getName().toLowerCase()::contains);//如果小写关键字匹配小写文件名
                            }).toList();//最终过滤获取结果列表
                        }
                        DirectoryTree.setCurrentFileList(result.toArray(new File[0]));//通过结果列表更新图片文件列表
                    }
                }
                updateFileDisplayMainPanel(false);//通知更新
            }
        }
    }

    public static class BarWrapLayout extends FlowLayout {//顶部底部栏自动换行布局管理器

        public BarWrapLayout(int align, int hgap, int vgap) {//构造对象时传入布局方式，水平间隔，垂直间隔
            super(align, hgap, vgap);//调用父类
        }

        @Override
        public Dimension preferredLayoutSize(Container target) {//重写布局大小方法

            int width = target.getWidth(), x = 0, y = getVgap(), rowHeight = 0;//初始宽度为自身宽度，x和y为当前组件的存放位置坐标，x初始为0（即最左侧），y为垂直间隔，初始行高为0
            for (Component comp : target.getComponents()) {//遍历当前顶部栏的所有组件
                Dimension dim = comp.getPreferredSize();//获取组件的大小（必须是首选大小不能只是普通大小）
                if (x + dim.width + getHgap() > width) {//如果x加上组件大小加上水平间隔比父组件视口宽度还大，x和y就要进入下一行
                    x = 0;//就重置x为0
                    y += rowHeight + getVgap();//y累计加上行高和垂直间隔
                    rowHeight = 0;//行高重置为0
                }
                if (dim.height > rowHeight) rowHeight = dim.height;//如果组件高度比行高还高，垂直高度为行高，以获取这一行的最大行高
                x += dim.width + getHgap();//摆放这个组件后，x加上组件自身宽度宽和水平间隔
            }
            return new Dimension(width, y + rowHeight + getVgap());//返回布局大小，宽为宽度，高为y加上行高和垂直间隔
        }
    }
}
