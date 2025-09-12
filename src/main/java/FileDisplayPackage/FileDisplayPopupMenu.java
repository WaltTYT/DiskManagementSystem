package FileDisplayPackage;

import MainPackage.Main;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static DirectoryPackage.DirectoryTree.createBottomTipWindow;
import static MainPackage.Setting.handleErrorLog;
import static NetworkPackage.User.handleUserSaveUserUploadPicture;
import static FileDisplayPackage.FileDisplayBottomBar.HistoryOperationType.DELETE;
import static FileDisplayPackage.FileDisplayBottomBar.historyManipulationButtonEnableJudgement;
import static FileDisplayPackage.FileDisplayBottomBar.undoStack;
import static FileDisplayPackage.FileDisplayMainPanel.*;
import static FileDisplayPackage.FileDisplayTopBar.*;

public class FileDisplayPopupMenu {//文件展示右键弹出菜单
    public static JPopupMenu rightMousePopupMenu = new JPopupMenu();//鼠标右键弹出菜单
    public static JMenuItem cutButton = new JMenuItem(Main.SettingState.systemLanguage ? "Cut (Ctrl + X)" : "剪切（Ctrl + X）");//剪切按钮（有选中图片时有效）
    public static JMenuItem copyButton = new JMenuItem(Main.SettingState.systemLanguage ? "Copy (Ctrl + C)" : "复制（Ctrl + C）");//复制按钮（有选中图片时有效）
    public static JMenuItem pasteButton = new JMenuItem(Main.SettingState.systemLanguage ? "Paste (Ctrl + V)" : "粘贴（Ctrl + V）");//粘贴按钮（剪贴板有图片时有效）
    public static JMenuItem renameButton = new JMenuItem(Main.SettingState.systemLanguage ? "Rename (F2)" : "重命名（F2）");//重命名按钮（选中图片为一时有效）
    public static JMenuItem removeButton = new JMenuItem(Main.SettingState.systemLanguage ? "Delete (Delete)" : "删除（Delete）");//删除按钮（有选中图片时有效）
    public static JMenuItem refreshButton = new JMenuItem(Main.SettingState.systemLanguage ? "Refresh (F5)" : "刷新（F5）");//刷新按钮（任何时候有效）
    public static JMenu recycleBinMenu = new JMenu(Main.SettingState.systemLanguage ? "Recycle Bin" : "回收站");//回收站父级菜单项（任何时候有效）
    public static JMenuItem openRecycleBinButton = new JMenuItem(Main.SettingState.systemLanguage ? "Open Recycle Bin (Ctrl + O)" : "打开回收站（Ctrl + O）");//打开回收站按钮（任何时候有效）
    public static JMenuItem emptyRecycleBinButton = new JMenuItem(Main.SettingState.systemLanguage ? "Empty Recycle Bin (Ctrl + E)" : "清空回收站（Ctrl + E）");//清空回收站按钮（任何时候有效）
    public static JMenuItem getPathButton = new JMenuItem(Main.SettingState.systemLanguage ? "Get File Path (Ctrl + Shift + C)" : "获取文件路径（Ctrl + Shift + C）");//获取文件路径按钮（有选中文件时有效）
    public static JMenuItem uploadToCloudButton = new JMenuItem(Main.SettingState.systemLanguage ? "Upload File To Cloud (Ctrl + P)" : "上传至云端（Ctrl + P）");//上传文件至云端按钮（用户登录且云端未满且有选中文件）

    public FileDisplayPopupMenu() {//构造方法
        rightMousePopupMenu.add(cutButton);
        rightMousePopupMenu.addSeparator();
        rightMousePopupMenu.add(copyButton);
        rightMousePopupMenu.addSeparator();
        rightMousePopupMenu.add(pasteButton);
        rightMousePopupMenu.addSeparator();
        rightMousePopupMenu.add(renameButton);
        rightMousePopupMenu.addSeparator();
        rightMousePopupMenu.add(removeButton);
        rightMousePopupMenu.addSeparator();
        rightMousePopupMenu.add(refreshButton);
        recycleBinMenu.add(openRecycleBinButton);
        recycleBinMenu.addSeparator();
        recycleBinMenu.add(emptyRecycleBinButton);
        rightMousePopupMenu.addSeparator();
        rightMousePopupMenu.add(recycleBinMenu);
        rightMousePopupMenu.addSeparator();
        rightMousePopupMenu.add(getPathButton);
        rightMousePopupMenu.addSeparator();
        rightMousePopupMenu.add(uploadToCloudButton);

        cutButton.setEnabled(false);
        copyButton.setEnabled(false);
        pasteButton.setEnabled(false);
        renameButton.setEnabled(false);
        removeButton.setEnabled(false);
        getPathButton.setEnabled(false);
        uploadToCloudButton.setEnabled(false);

        cutButton.addActionListener(_ -> handleCut());//处理剪切
        copyButton.addActionListener(_ -> handleCopy());//处理复制
        pasteButton.addActionListener(_ -> handlePaste());//处理粘贴
        renameButton.addActionListener(_ -> handleRename());//处理重命名
        removeButton.addActionListener(_ -> handleRemove());//处理删除
        refreshButton.addActionListener(_ -> handleRefresh());//处理刷新
        openRecycleBinButton.addActionListener(_ -> handleOpenRecycleBin());//处理打开图片回收站
        emptyRecycleBinButton.addActionListener(_ -> handleEmptyRecycleBin());//处理清空图片回收站
        getPathButton.addActionListener(_ -> handleGetPath());//处理获取路径
        uploadToCloudButton.addActionListener(_ -> {
            handleUserSaveUserUploadPicture(getSelectionThumbnailItemFileList(), true);//处理上传图片至云端
            handleRefresh();//刷新
        });
    }

    public static void handleOpenRecycleBin() {//处理打开回收站
        try {
            Path recycleBinPath = Path.of("D:/Recycle Bin");//获取自定义回收站路径
            if (!Files.exists(recycleBinPath)) {//如果不存在自定义回收站路径
                Files.createDirectories(recycleBinPath);//就创建路径
            }
            Desktop.getDesktop().open(recycleBinPath.toFile());//打开自定义回收站
        } catch (IOException e) {
            handleErrorLog(e.getMessage());//处理错误日志
            throw new RuntimeException(e);//捕获异常
        }
    }

    public static void handleEmptyRecycleBin() {//处理清空回收站
        Path recycleBinPath = Path.of("D:/Recycle Bin");//获取自定义回收站路径
        if (Files.exists(recycleBinPath)) {//如果存在自定义回收站路径
            int confirm = JOptionPane.showConfirmDialog(Main.diskManagementSystemFrame, Main.SettingState.systemLanguage ? "Are You Sure To Empty Recycle Bin? You Cannot Undo This Operation" : "确定要清空回收站吗？这个操作无法撤销", Main.SettingState.systemLanguage ? "Empty Confirm" : "确认清空", JOptionPane.YES_NO_OPTION);//创建确认信息
            if (confirm == JOptionPane.YES_OPTION) {//如果确认
                File[] recycleBinFileList = new File(String.valueOf(recycleBinPath)).listFiles();//获取回收站内文件
                if (recycleBinFileList != null) {//如果非空
                    for (File file : recycleBinFileList) {//遍历
                        try {
                            undoStack.removeIf(fileOperation -> fileOperation.type == DELETE);//删除撤销栈中的全部删除操作
                            Files.deleteIfExists(file.toPath());//删除文件
                        } catch (IOException e) {
                            handleErrorLog(e.getMessage());//处理错误日志
                            throw new RuntimeException(e);//捕获异常
                        }
                    }
                    historyManipulationButtonEnableJudgement();//判断
                }
            }
        }
    }

    public static void handleAutoEmptyRecycleBin() {//处理自动清空回收站
        Path recycleBinPath = Path.of("D:/Recycle Bin");//获取自定义回收站路径
        if (Files.exists(recycleBinPath)) {//如果存在自定义回收站路径
            File[] recycleBinFileList = new File(String.valueOf(recycleBinPath)).listFiles();//获取回收站内文件
            if (recycleBinFileList != null) {//如果非空
                for (File file : recycleBinFileList) {//遍历
                    try {
                        undoStack.removeIf(fileOperation -> fileOperation.type == DELETE);//删除撤销栈中的全部删除操作
                        Files.deleteIfExists(file.toPath());//删除文件
                    } catch (IOException e) {
                        handleErrorLog(e.getMessage());//处理错误日志
                        throw new RuntimeException(e);//捕获异常
                    }
                }
                historyManipulationButtonEnableJudgement();//判断
            }
        }
    }

    public static void handleGetPath() {//处理获取路径
        List<ThumbnailItem> fileList = getSelectionThumbnailItemList();//获取文件列表
        if (!fileList.isEmpty()) {//如果非空
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();//获取系统剪贴板
            StringBuilder newContent = new StringBuilder();//创建字符串创建者添加文件路径
            for (ThumbnailItem thumbnailItem : fileList) {//遍历文件列表
                if (!newContent.isEmpty()) {//如果不为空
                    newContent.append(System.lineSeparator());//添加换行符分隔路径
                }
                newContent.append(thumbnailItem.getFile().getAbsoluteFile());//添加文件路径
            }
            StringSelection stringSelection = new StringSelection(newContent.substring(3));//将新内容放入剪贴板
            clipboard.setContents(stringSelection, null);//设置剪贴板内容
            createBottomTipWindow(Main.SettingState.systemLanguage ? "File Path Copied To Clipboard" : "文件路径已复制到剪贴板");
        }
    }

    public static boolean administratorJudgement() {//管理员判断
        try {
            executeCommand("fsutil", "dirty", "query", System.getenv("SYSTEMDRIVE"));//执行命令
            return false;//返回否
        } catch (Exception e) {//如果捕获到异常
            return true;//返回是
        }
    }

    private static void executeCommand(String... command) throws Exception {//处理命令
        ProcessBuilder pb = new ProcessBuilder(command);//通过命令创建进程创建者
        pb.redirectErrorStream(true);//重导错误流
        Process process = pb.start();//开始进程
        StringBuilder output = new StringBuilder();//创建输出
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {//通过UTF_8编码创建读入者
            String line;//每一行输出
            while ((line = reader.readLine()) != null) {//如果读入不为空
                output.append(line).append("\n");//就往每一行输出增加换行
            }
        }
        int exitCode = process.waitFor();//处理并获取退出代码
        if (exitCode != 0) {//如果不是正常退出
            String errorDetail = String.format("Command failed: %s\nExit code: %d\nOutput:\n%s", String.join(" ", command), exitCode, output);//输出错误信息
            throw new RuntimeException(errorDetail);//抛出错误
        }
    }
}
