package FileDisplayPackage;

import DirectoryPackage.DirectoryTree;
import MainPackage.Main;
import MainPackage.Setting;
import NetworkPackage.User;
import FileEditPackage.FileEditPanel;
import FileEditPackage.FileEditScrollPane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.Stack;
import java.util.stream.Collectors;

import static DirectoryPackage.DirectoryTree.createBottomTipWindow;
import static MainPackage.Setting.handleErrorLog;
import static MainPackage.ThemeColor.DARK_PICTURE_BAR_COLOR;
import static MainPackage.ThemeColor.LIGHT_PICTURE_BAR_COLOR;
import static NetworkPackage.User.*;
import static FileDisplayPackage.FileDisplayMainPanel.*;
import static FileDisplayPackage.FileDisplayTopBar.*;

public class FileDisplayBottomBar {//文件展示底部栏
    public static final JPanel bottomBarPanel = new JPanel();//底部面板：用于放置按钮等组件
    public static final JButton undoButton = new JButton();//撤销按钮
    public static final JButton redoButton = new JButton();//恢复按钮
    public static final JButton editButton = new JButton();//编辑按钮（当前选中文件为一个且文件非目录时可选中）
    public static final JButton userButton = new JButton();//用户按钮
    public static final JButton uploadToCloudButton = new JButton();//上传图片至云端按钮
    public static final JButton settingButton = new JButton();//设置按钮
    public static final JSlider zoomSlider = new JSlider(JSlider.HORIZONTAL, 100, 750, 300);//缩放调整拖动条（水平朝向，最小值为100，最大值为3000，初始值为300）

    public static final Stack<FileOperation> undoStack = new Stack<>();//撤销操作历史堆栈
    public static final Stack<FileOperation> redoStack = new Stack<>();//恢复操作历史堆栈

    public enum HistoryOperationType {//历史操作类型枚举常量
        MOVE, COPY, RENAME, DELETE, UPLOAD//0：移动；1：复制；2：重命名；3：删除；4：上传到云盘
    }

    public static void historyManipulationButtonEnableJudgement() {//历史操作按钮判断
        boolean flag = true;//是否没有到达栈底
        int stackIndex = undoStack.size() - 1;//索引
        if (stackIndex >= 0) {//如果>=0
            while (undoStack.get(stackIndex).isCloudOperation) {//如果是云盘操作
                if (Main.SettingState.userAccount.isEmpty()) {//如果退出登录
                    if (stackIndex == 0) {//如果为0
                        flag = false;//到达栈底
                        break;//退出
                    }
                    stackIndex--;//自减
                } else {//否则
                    break;//退出
                }
            }
        }
        undoButton.setEnabled(flag && !undoStack.isEmpty());//如果撤销堆栈为空则设为否，如果非空则设为是
        flag = true;//是否没有到达栈底
        stackIndex = redoStack.size() - 1;//索引
        if (stackIndex >= 0) {//如果>=0
            while (redoStack.get(stackIndex).isCloudOperation) {//如果是云盘操作
                if (Main.SettingState.userAccount.isEmpty()) {//如果退出登录
                    if (stackIndex == 0) {//如果为0
                        flag = false;//到达栈底
                        break;//退出
                    }
                    stackIndex--;//自减
                } else {//否则
                    break;//退出
                }
            }
        }
        redoButton.setEnabled(flag && !redoStack.isEmpty());//如果恢复堆栈为空则设为否，如果非空则设为是
    }

    public FileDisplayBottomBar() {//构造方法
        undoButton.setEnabled(false);//初始无效
        redoButton.setEnabled(false);
        editButton.setEnabled(false);
        uploadToCloudButton.setEnabled(false);

        undoButton.setSize(24, 24);//设置大小
        redoButton.setSize(24, 24);
        editButton.setSize(24, 24);
        userButton.setSize(24, 24);
        uploadToCloudButton.setSize(24, 24);
        settingButton.setSize(24, 24);

        undoButton.setIcon(new ImageIcon(new ImageIcon("src/material/image/quash.png").getImage().getScaledInstance(undoButton.getWidth(), undoButton.getHeight(), Image.SCALE_DEFAULT)));//通过getScaledInstance使按钮适应图片大小
        redoButton.setIcon(new ImageIcon(new ImageIcon("src/material/image/recover.png").getImage().getScaledInstance(redoButton.getWidth(), redoButton.getHeight(), Image.SCALE_DEFAULT)));
        editButton.setIcon(new ImageIcon(new ImageIcon("src/material/image/edit.png").getImage().getScaledInstance(editButton.getWidth(), editButton.getHeight(), Image.SCALE_DEFAULT)));
        userButton.setIcon(new ImageIcon(new ImageIcon("src/material/image/user.png").getImage().getScaledInstance(userButton.getWidth(), userButton.getHeight(), Image.SCALE_DEFAULT)));
        uploadToCloudButton.setIcon(new ImageIcon(new ImageIcon("src/material/image/uploadToCloud.png").getImage().getScaledInstance(uploadToCloudButton.getWidth(), uploadToCloudButton.getHeight(), Image.SCALE_DEFAULT)));
        settingButton.setIcon(new ImageIcon(new ImageIcon("src/material/image/setting.png").getImage().getScaledInstance(settingButton.getWidth(), settingButton.getHeight(), Image.SCALE_DEFAULT)));

        zoomSlider.setBackground(Main.SettingState.themeColor ? DARK_PICTURE_BAR_COLOR : LIGHT_PICTURE_BAR_COLOR);//设置背景颜色

        bottomBarPanel.add(undoButton);
        bottomBarPanel.add(redoButton);
        bottomBarPanel.add(editButton);
        bottomBarPanel.add(userButton);
        bottomBarPanel.add(uploadToCloudButton);
        bottomBarPanel.add(settingButton);
        bottomBarPanel.add(zoomSlider);
        bottomBarPanel.setLayout(new BarWrapLayout(FlowLayout.LEFT, 0, 0));
        bottomBarPanel.setBackground(Main.SettingState.themeColor ? DARK_PICTURE_BAR_COLOR : LIGHT_PICTURE_BAR_COLOR);//设置背景颜色
        bottomBarPanel.addComponentListener(new ComponentAdapter() {//为底部栏面板添加组件监听
            @Override
            public void componentResized(ComponentEvent e) {//如果组件大小变化
                bottomBarPanel.setSize(bottomBarPanel.getLayout().preferredLayoutSize(bottomBarPanel));//就重新设置布局
            }
        });

        undoButton.addActionListener(_ -> handleUndo());//为撤销按钮添加事件监听
        undoButton.addMouseListener(new MouseAdapter() {//为撤销按钮添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Undo (Ctrl + Z)" : "撤销（Ctrl + Z）", undoButton));//展示提示窗口（鼠标悬浮一秒后展示）
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

        redoButton.addActionListener(_ -> handleRedo());//为恢复按钮添加事件监听
        redoButton.addMouseListener(new MouseAdapter() {//为恢复按钮添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Redo (Ctrl + Y)" : "恢复（Ctrl + Y）", redoButton));//展示提示窗口（鼠标悬浮一秒后展示）
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

        editButton.addActionListener(_ -> handleEdit());//为编辑按钮添加事件监听
        editButton.addMouseListener(new MouseAdapter() {//为幻灯片按钮添加鼠标事件监听

            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Edit File (Ctrl + D)" : "编辑文件（Ctrl + D）", editButton));//展示提示窗口（鼠标悬浮一秒后展示）
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

        userButton.addActionListener(_ -> User.handleUser());//为用户按钮添加事件监听
        userButton.addMouseListener(new MouseAdapter() {//为用户按钮添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "User (Ctrl + U)" : "用户（Ctrl + U）", userButton));//展示提示窗口（鼠标悬浮一秒后展示）
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

        uploadToCloudButton.addActionListener(_ -> {//为上传图片至云端按钮添加事件监听
            handleUserSaveUserUploadPicture(getSelectionThumbnailItemFileList(), true);//处理上传图片至云盘
            handleRefresh();//刷新
        });
        uploadToCloudButton.addMouseListener(new MouseAdapter() {//为上传图片至云端按钮添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Upload Picture To Cloud (Ctrl + P  To Protect Your Privacy, Please Cautious When Upload Image That Contain Personal Information)" : "上传图片至云端（Ctrl + P  为了保护您的隐私，请谨慎上传含有个人信息的图片）", uploadToCloudButton));//展示提示窗口（鼠标悬浮一秒后展示）
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

        settingButton.addActionListener(_ -> Setting.handleSetting());//为设置按钮添加事件监听
        settingButton.addMouseListener(new MouseAdapter() {//为设置按钮添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Setting (Ctrl + S)" : "设置（Ctrl + S）", settingButton));//展示提示窗口（鼠标悬浮一秒后展示）
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

        zoomSlider.addChangeListener(_ -> {//为缩放调整拖动条添加变化监听
            if (zoomSlider.hasFocus()) {//如果有焦点
                thumbnailItemWidth = zoomSlider.getValue();//设置图片大小为拖动条的值
                SelectionMouseAdapter.applyZoom();//应用缩放
            }
        });
        zoomSlider.addMouseListener(new MouseAdapter() {//为缩放调整拖动条添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Zoom Adjust (Ctrl + Mouse Wheel) Please Not Zoom Too Quick" : "缩放调整（Ctrl + 鼠标滚轮）请勿缩放过快", zoomSlider));//展示提示窗口（鼠标悬浮一秒后展示）
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

        historyManipulationButtonEnableJudgement();//初始化撤销和恢复按钮状态
        Setting.initSettingDialog();//初始化设置菜单
        if (Main.SettingState.userAccount.isEmpty()) {//如果没有用户名
            User.initLogInDialog();//初始化登录菜单
            User.initRegisterDialog();//初始化注册菜单
        } else {//否则有用户名
            User.initUserDialog();//初始化用户菜单
        }
    }

    public static void recordFileOperation(FileOperation operation) {//记录文件操作
        undoStack.push(operation);//撤销操作压入撤销堆栈
        if (undoStack.size() > MAX_HISTORY) {//如果堆栈空间已满（恢复堆栈不需要添加，因为撤销堆栈已经保证不会超过最大空间，恢复堆栈最多和撤销堆栈等大，也不会超过最大空间）
            undoStack.removeFirst();//删除第一条历史记录（LRU策略）
        }
        redoStack.clear();//清空恢复堆栈
        historyManipulationButtonEnableJudgement();//判断按钮状态
    }

    public static void handleUndo() {//处理撤销
        if (!undoStack.isEmpty()) {//如果非空
            if (itemHoverTipWindow != null) {//如果提示信息不为空
                itemHoverTipWindow.dispose();//释放提示信息
                itemHoverTipWindow = null;//提示信息置空
            }
            FileOperation op = undoStack.pop();//获取最近的操作
            redoStack.push(op);//恢复堆栈压入
            try {
                switch (op.type) {//根据类型判断
                    case MOVE://如果是移动
                        if (Objects.equals(op.extraInformation, "cutFromCloud")) {//如果是从云盘剪切：云盘为原文件，本地是目标文件
                            if (Main.SettingState.userAccount.isEmpty()) {//如果用户退出登录
                                handleUndo();//递归
                            } else {//否则
                                if (handleUserSaveUserUploadPicture(op.targetFileList, false) != null) {//目标文件存回云盘，如果不为空证明操作成功
                                    for (File target : op.targetFileList) {//遍历被剪切目标文件：删除本地目标文件
                                        if (target.exists()) Files.delete(target.toPath());//目标文件全部删除
                                    }
                                }
                                handleUserLoadUserUploadPicture(null);//加载云盘文件保证云盘原文件加载到本地缓存，使本地缓存和云盘同步
                            }
                        } else if (Objects.equals(op.extraInformation, "cutToCloud")) {//如果是剪切到云盘：本地为原文件，云盘是目标文件
                            if (Main.SettingState.userAccount.isEmpty()) {//如果用户退出登录
                                handleUndo();//递归
                            } else {//否则
                                File[] pictureFileList = handleUserLoadUserUploadPicture(op.targetStringList);//获取云端指定名称的图片文件
                                for (int i = 0; i < op.sourceFileList.size(); i++) {//遍历被移动原文件：原文件存回本地
                                    if (pictureFileList != null) {//如果不为空
                                        Files.move(pictureFileList[i].toPath(), op.sourceFileList.get(i).toPath(), StandardCopyOption.REPLACE_EXISTING);//通过替换方案把原文件全部恢复，目标文件全部删除
                                    }
                                }
                                handleUserRemoveUserUploadPicture(op.targetStringList, false);//删除云盘目标文件
                            }
                        } else {//否则
                            for (int i = 0; i < op.sourceFileList.size(); i++) {//遍历被移动原文件
                                Files.move(op.targetFileList.get(i).toPath(), op.sourceFileList.get(i).toPath(), StandardCopyOption.REPLACE_EXISTING);//通过替换方案把原文件全部恢复，目标文件全部删除
                            }
                        }
                        break;
                    case COPY://如果是复制
                        if (Objects.equals(op.extraInformation, "copyToCloud")) {//如果是复制到云盘：本地为原文件，云盘是目标文件
                            if (Main.SettingState.userAccount.isEmpty()) {//如果用户退出登录
                                handleUndo();//递归
                            } else {//否则
                                handleUserRemoveUserUploadPicture(op.targetStringList, false);//删除云盘目标文件
                            }
                        } else {//否则（从云盘复制逻辑相同）
                            for (File target : op.targetFileList) {//遍历被复制目标文件
                                if (target.exists()) Files.delete(target.toPath());//目标文件全部删除
                            }
                        }
                        break;
                    case RENAME://如果是重命名
                        if (op.extraInformation == null) {//如果是多文件重命名
                            if (op.isCloudOperation) {//如果是云盘操作
                                if (Main.SettingState.userAccount.isEmpty()) {//如果用户退出登录
                                    handleUndo();//递归
                                } else {//否则
                                    for (int i = 0; i < op.sourceFileList.size(); i++) {//遍历被移动原文件
                                        Files.move(op.targetFileList.get(i).toPath(), op.sourceFileList.get(i).toPath(), StandardCopyOption.REPLACE_EXISTING);//通过替换方案把原文件全部恢复，目标文件全部删除
                                    }
                                    handleUserLoadUserUploadPicture(null);//加载云盘文件保证云盘原文件加载到本地缓存，让本地缓存能恢复到本地目标文件
                                    handleUserSaveUserUploadPicture(op.sourceFileList, false);//保存旧文件
                                    handleUserRemoveUserUploadPicture(op.targetStringList, false);//删除新文件
                                }
                            } else {//否则
                                for (int i = 0; i < op.sourceFileList.size(); i++) {//遍历被移动原文件
                                    Files.move(op.targetFileList.get(i).toPath(), op.sourceFileList.get(i).toPath(), StandardCopyOption.REPLACE_EXISTING);//通过替换方案把原文件全部恢复，目标文件全部删除
                                }
                            }
                        } else {//否则是单文件重命名
                            if (op.isCloudOperation) {//如果是云盘操作
                                if (Main.SettingState.userAccount.isEmpty()) {//如果用户退出登录
                                    handleUndo();//递归
                                } else {//否则
                                    File original = new File(op.extraInformation);//通过原文件名称重新创建文件
                                    Files.move(op.targetFileList.getFirst().toPath(), original.toPath(), StandardCopyOption.REPLACE_EXISTING);//通过替换方案把原文件恢复，把目标文件删除
                                    handleUserLoadUserUploadPicture(null);//加载云盘文件保证云盘原文件加载到本地缓存，让本地缓存能恢复到本地目标文件
                                    handleUserSaveUserUploadPicture(op.sourceFileList, false);//保存旧文件
                                    handleUserRemoveUserUploadPicture(Collections.singletonList(op.targetStringList.getFirst()), false);//删除新文件
                                }
                            } else {//否则
                                File original = new File(op.extraInformation);//通过原文件名称重新创建文件
                                Files.move(op.targetFileList.getFirst().toPath(), original.toPath(), StandardCopyOption.REPLACE_EXISTING);//通过替换方案把原文件恢复，把目标文件删除
                            }
                        }
                        break;
                    case DELETE://如果是删除
                        if (op.sourceFileList.size() != op.recycleFileList.size()) {//添加大小检查，如果大小不相等
                            throw new IOException("操作记录不完整");//捕获异常
                        }
                        if (op.isCloudOperation) {//如果是云盘操作
                            if (Main.SettingState.userAccount.isEmpty()) {//如果用户退出登录
                                handleUndo();//递归
                            } else {//否则
                                for (int i = op.sourceFileList.size() - 1; i >= 0; i--) {//遍历被删除原文件，逆序恢复以防止文件名冲突
                                    Path source = op.sourceFileList.get(i).toPath();//获取需要恢复的原文件
                                    Path recycle = op.recycleFileList.get(i).toPath();//获取需要删除的回收文件
                                    Files.createDirectories(source.getParent());//确保恢复原文件的目录存在
                                    if (Files.exists(recycle)) {//三步检查法确保安全恢复，先检查回收文件是否存在
                                        if (Files.exists(source)) {//再检查需要恢复的原文件是否存在
                                            Files.delete(source);//删除可能存在的冲突文件
                                        }
                                        Files.move(recycle, source);//检查结束后把回收站被删除原文件恢复
                                    }
                                }
                                handleUserLoadUserUploadPicture(null);//加载云盘文件保证云盘原文件加载到本地缓存，让本地缓存能恢复到本地目标文件
                                handleUserSaveUserUploadPicture(op.sourceFileList, false);//保存旧文件
                            }
                        } else {//否则
                            for (int i = op.sourceFileList.size() - 1; i >= 0; i--) {//遍历被删除原文件，逆序恢复以防止文件名冲突
                                Path source = op.sourceFileList.get(i).toPath();//获取需要恢复的原文件
                                Path recycle = op.recycleFileList.get(i).toPath();//获取需要删除的回收文件
                                Files.createDirectories(source.getParent());//确保恢复原文件的目录存在
                                if (Files.exists(recycle)) {//三步检查法确保安全恢复，先检查回收文件是否存在
                                    if (Files.exists(source)) {//再检查需要恢复的原文件是否存在
                                        Files.delete(source);//删除可能存在的冲突文件
                                    }
                                    Files.move(recycle, source);//检查结束后把回收站被删除原文件恢复
                                }
                            }
                        }
                        break;
                    case UPLOAD:
                        if (Main.SettingState.userAccount.isEmpty()) {//如果用户退出登录
                            handleUndo();//递归
                        } else {//否则
                            handleUserRemoveUserUploadPicture(op.targetStringList, false);//删除云盘目标文件
                        }
                        break;
                }
                refreshAfterFileHistoryOperation();//刷新
            } catch (IOException e) {
                handleErrorLog(e.getMessage());//处理错误日志
                createBottomTipWindow(Main.SettingState.systemLanguage ? "Failure Undo Operation: " : "撤销操作失败：" + e.getMessage());//捕获异常
            }
        }
    }

    public static void handleRedo() {//处理恢复
        if (!redoStack.isEmpty()) {//如果非空
            if (itemHoverTipWindow != null) {//如果提示信息不为空
                itemHoverTipWindow.dispose();//释放提示信息
                itemHoverTipWindow = null;//提示信息置空
            }
            FileOperation op = redoStack.pop();//获取最近的操作
            undoStack.push(op);//撤销堆栈压入
            try {
                switch (op.type) {//根据类型判断
                    case MOVE://如果是移动
                        if (Objects.equals(op.extraInformation, "cutFromCloud")) {//如果是从云盘剪切：云盘为原文件，本地是目标文件
                            if (Main.SettingState.userAccount.isEmpty()) {//如果用户退出登录
                                handleRedo();//递归
                            } else {//否则
                                java.util.List<String> fileNameList = new ArrayList<>();//文件名列表
                                for (File clipboardFile : op.sourceFileList) {//遍历原文件
                                    fileNameList.add(clipboardFile.getName());//添加文件名
                                }
                                handleUserLoadUserUploadPicture(null);//加载云盘文件保证云盘原文件加载到本地缓存，让本地缓存能恢复到本地目标文件
                                for (int i = 0; i < op.targetFileList.size(); i++) {//遍历被撤销移动目标文件：存回本地目标文件
                                    Files.move(op.sourceFileList.get(i).toPath(), op.targetFileList.get(i).toPath(), StandardCopyOption.REPLACE_EXISTING);//通过替换方案把原文件再次删除，目标文件再次恢复
                                }
                                handleUserRemoveUserUploadPicture(fileNameList, false);//最后删除云盘原文件
                            }
                        } else if (Objects.equals(op.extraInformation, "cutToCloud")) {//如果是剪切到云盘：本地为原文件，云盘是目标文件
                            if (Main.SettingState.userAccount.isEmpty()) {//如果用户退出登录
                                handleRedo();//递归
                            } else {//否则
                                if (handleUserSaveUserUploadPicture(op.sourceFileList, false) != null) {//原文件存回云盘，如果不为空证明操作成功
                                    for (File source : op.sourceFileList) {//遍历被剪切原文件：删除本地原文件
                                        if (source.exists()) Files.delete(source.toPath());//原文件全部删除
                                    }
                                }
                            }
                        } else {//否则
                            for (int i = 0; i < op.sourceFileList.size(); i++) {//遍历被撤销移动原文件
                                Files.move(op.sourceFileList.get(i).toPath(), op.targetFileList.get(i).toPath(), StandardCopyOption.REPLACE_EXISTING);//通过替换方案把原文件再次删除，目标文件再次恢复
                            }
                        }
                        break;
                    case COPY://如果是复制
                        if (Objects.equals(op.extraInformation, "copyToCloud")) {//如果是复制到云盘：本地为原文件，云盘是目标文件
                            if (Main.SettingState.userAccount.isEmpty()) {//如果用户退出登录
                                handleRedo();//递归
                            } else {//否则
                                handleUserSaveUserUploadPicture(op.sourceFileList, false);//存回云盘原文件
                            }
                        } else {//否则（从云盘复制逻辑相同）
                            for (int i = 0; i < op.sourceFileList.size(); i++) {//遍历被撤销复制原文件
                                Files.copy(op.sourceFileList.get(i).toPath(), op.targetFileList.get(i).toPath(), StandardCopyOption.REPLACE_EXISTING);//通过替换方案把目标文件再次恢复
                            }
                        }
                        break;
                    case RENAME://如果是重命名
                        if (op.extraInformation == null) {//如果是多文件重命名
                            if (op.isCloudOperation) {//如果是云盘操作
                                if (Main.SettingState.userAccount.isEmpty()) {//如果用户退出登录
                                    handleRedo();//递归
                                } else {//否则
                                    for (int i = 0; i < op.sourceFileList.size(); i++) {//遍历被撤销移动原文件
                                        Files.move(op.sourceFileList.get(i).toPath(), op.targetFileList.get(i).toPath(), StandardCopyOption.REPLACE_EXISTING);//通过替换方案把原文件再次删除，目标文件再次恢复
                                    }
                                    handleUserLoadUserUploadPicture(null);//加载云盘文件保证云盘原文件加载到本地缓存，让本地缓存能恢复到本地目标文件
                                    handleUserSaveUserUploadPicture(op.targetFileList, false);//保存旧文件
                                    handleUserRemoveUserUploadPicture(op.sourceFileList.stream().map(File::getName).collect(Collectors.toList()), false);//删除新文件
                                }
                            } else {//否则
                                for (int i = 0; i < op.sourceFileList.size(); i++) {//遍历被撤销移动原文件
                                    Files.move(op.sourceFileList.get(i).toPath(), op.targetFileList.get(i).toPath(), StandardCopyOption.REPLACE_EXISTING);//通过替换方案把原文件再次删除，目标文件再次恢复
                                }
                            }
                        } else {//否则是单文件重命名
                            if (op.isCloudOperation) {//如果是云盘操作
                                if (Main.SettingState.userAccount.isEmpty()) {//如果用户退出登录
                                    handleRedo();//递归
                                } else {//否则
                                    Files.move(new File(op.extraInformation).toPath(), op.targetFileList.getFirst().toPath(), StandardCopyOption.REPLACE_EXISTING);//通过替换方案把目标文件再次恢复，原文件再次删除
                                    handleUserLoadUserUploadPicture(null);//加载云盘文件保证云盘原文件加载到本地缓存，让本地缓存能恢复到本地目标文件
                                    handleUserSaveUserUploadPicture(op.targetFileList, false);//保存旧文件
                                    handleUserRemoveUserUploadPicture(Collections.singletonList(op.sourceFileList.getFirst().getName()), false);//删除新文件
                                }
                            } else {//否则
                                Files.move(new File(op.extraInformation).toPath(), op.targetFileList.getFirst().toPath(), StandardCopyOption.REPLACE_EXISTING);//通过替换方案把目标文件再次恢复，原文件再次删除
                            }
                        }
                        break;
                    case DELETE://如果是删除
                        if (op.sourceFileList.size() != op.recycleFileList.size()) {//添加大小检查，如果大小不相等
                            throw new IOException("操作记录不完整");//捕获异常
                        }
                        if (op.isCloudOperation) {//如果是云盘操作
                            if (Main.SettingState.userAccount.isEmpty()) {//如果用户退出登录
                                handleRedo();//递归
                            } else {//否则
                                for (int i = 0; i < op.sourceFileList.size(); i++) {//遍历已撤销删除原文件
                                    Files.move(op.sourceFileList.get(i).toPath(), op.recycleFileList.get(i).toPath(), StandardCopyOption.REPLACE_EXISTING);//通过替换方案把把原文件重新移动到回收站（时间戳并不改变）
                                }
                                handleUserLoadUserUploadPicture(null);//加载云盘文件保证云盘原文件加载到本地缓存，让本地缓存能恢复到本地目标文件
                                handleUserRemoveUserUploadPicture(op.targetStringList, false);//删除新文件
                            }
                        } else {//否则
                            for (int i = 0; i < op.sourceFileList.size(); i++) {//遍历已撤销删除原文件
                                Files.move(op.sourceFileList.get(i).toPath(), op.recycleFileList.get(i).toPath(), StandardCopyOption.REPLACE_EXISTING);//通过替换方案把把原文件重新移动到回收站（时间戳并不改变）
                            }
                        }
                        break;
                    case UPLOAD:
                        if (Main.SettingState.userAccount.isEmpty()) {//如果用户退出登录
                            handleRedo();//递归
                        } else {//否则
                            handleUserSaveUserUploadPicture(op.sourceFileList, false);//存回云盘原文件
                        }
                        break;
                }
                refreshAfterFileHistoryOperation();//刷新
            } catch (IOException e) {
                handleErrorLog(e.getMessage());//处理错误日志
                createBottomTipWindow(Main.SettingState.systemLanguage ? "Failure Redo Operation: " : "恢复操作失败：" + e.getMessage());//捕获异常
            }
        }
    }

    public static void refreshAfterFileHistoryOperation() {//在文件历史操作后进行刷新
        if (Objects.equals(currentFolder, Main.SettingState.systemLanguage ? "My Cloud" : "我的云盘")) {//如果是云盘结点
            try {
                DirectoryTree.setCurrentFileList(User.handleUserLoadUserUploadPicture(null));//更新图片文件列表
            } catch (IOException e) {
                handleErrorLog(e.getMessage());//处理错误日志
                throw new RuntimeException(e);//捕获异常
            }
        } else {//否则
            if (currentFolder != null) {//如果不为空
                DirectoryTree.updateCurrentFileList(new File(currentFolder).listFiles());//更新文件夹
            }
        }
        new Timer(100, e -> {//在100ms时延后进行
            FileDisplayMainPanel.updateFileDisplayMainPanel(false);//更新面板
            fileManipulationButtonEnableJudgement(0);//文件操作按钮状态判断
            historyManipulationButtonEnableJudgement();//历史操作按钮状态判断
            ((Timer) e.getSource()).stop();//停止计时器
        }).start();//开始计时器
    }

    public static void handleEdit() {//处理幻灯片
        FileEditPanel.isNotEdit = false;//播放幻灯片
        if (itemHoverTipWindow != null) {//如果提示信息不为空
            itemHoverTipWindow.dispose();//释放提示信息
            itemHoverTipWindow = null;//提示信息置空
        }
        Main.diskManagementSystemFrame.setVisible(false);//不可见
        Main.editFrame.setVisible(true);//可见
        if (Main.SettingState.windowState) {//如果全屏
            GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(Main.editFrame);//设置窗口全屏
        } else {//否则
            Main.editFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);//设置窗口直接最大化
        }

        FileEditScrollPane.pictureIndex = FileDisplayMainPanel.selectionAnchorIndex == -1 ? 0 : FileDisplayMainPanel.selectionAnchorIndex;//设置索引
        FileEditScrollPane.updateScrollPane();//更新滚动栏
        FileEditPanel.updatePicturePanel();//更新图片面板

        try {
            Thread.sleep(50);//睡眠50ms
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

    public static class FileOperation {//文件操作记录类
        final HistoryOperationType type;//操作类型
        final java.util.List<File> sourceFileList;//操作原文件列表
        final java.util.List<File> targetFileList;//操作目标文件列表
        final String extraInformation;//额外信息存储（如存储文件原名称、云盘操作类型）
        final java.util.List<File> recycleFileList;//记录文件回收站路径
        final java.util.List<String> targetStringList;//目标字符串列表（存储服务端处理后文件名）
        final boolean isCloudOperation;//是否是云盘操作

        public FileOperation(HistoryOperationType type, java.util.List<File> sourceFileList, java.util.List<File> targetFileList, String extraInformation, java.util.List<File> recycleFileList, java.util.List<String> targetStringList, boolean isCloudOperation) {//构造函数
            this.type = type;
            this.sourceFileList = sourceFileList;
            this.targetFileList = targetFileList;
            this.extraInformation = extraInformation;
            this.recycleFileList = recycleFileList;
            this.targetStringList = targetStringList;
            this.isCloudOperation = isCloudOperation;
        }
    }
}
