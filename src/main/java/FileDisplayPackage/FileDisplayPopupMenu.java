package FileDisplayPackage;

import MainPackage.Main;
import MainPackage.Setting;

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
import java.util.Objects;

import static DirectoryPackage.DirectoryTree.createBottomTipWindow;
import static MainPackage.Main.spikeVisionCloudPath;
import static MainPackage.Setting.handleErrorLog;
import static NetworkPackage.User.handleUserSaveUserUploadPicture;
import static FileDisplayPackage.FileDisplayBottomBar.HistoryOperationType.DELETE;
import static FileDisplayPackage.FileDisplayBottomBar.historyManipulationButtonEnableJudgement;
import static FileDisplayPackage.FileDisplayBottomBar.undoStack;
import static FileDisplayPackage.FileDisplayMainPanel.*;
import static FileDisplayPackage.FileDisplayTopBar.*;

public class FileDisplayPopupMenu {//图片预览右键弹出菜单
    public static JPopupMenu rightMousePopupMenu = new JPopupMenu();//鼠标右键弹出菜单
    public static JMenuItem cutButton = new JMenuItem(Main.SettingState.systemLanguage ? "Cut (Ctrl + X)" : "剪切（Ctrl + X）");//剪切按钮（有选中图片时有效）
    public static JMenuItem copyButton = new JMenuItem(Main.SettingState.systemLanguage ? "Copy (Ctrl + C)" : "复制（Ctrl + C）");//复制按钮（有选中图片时有效）
    public static JMenuItem pasteButton = new JMenuItem(Main.SettingState.systemLanguage ? "Paste (Ctrl + V)" : "粘贴（Ctrl + V）");//粘贴按钮（剪贴板有图片时有效）
    public static JMenuItem renameButton = new JMenuItem(Main.SettingState.systemLanguage ? "Rename (F2)" : "重命名（F2）");//重命名按钮（选中图片为一时有效）
    public static JMenuItem removeButton = new JMenuItem(Main.SettingState.systemLanguage ? "Delete (Delete)" : "删除（Delete）");//删除按钮（有选中图片时有效）
    public static JMenuItem refreshButton = new JMenuItem(Main.SettingState.systemLanguage ? "Refresh (F5)" : "刷新（F5）");//刷新按钮（任何时候有效）
    public static JMenuItem openInOtherButton = new JMenuItem(Main.SettingState.systemLanguage ? "Open Image In Other (Enter)" : "在其他软件中打开（Enter）");//在其他软件中打开按钮（有选中图片时有效）
    public static JMenuItem openInExplorerButton = new JMenuItem(Main.SettingState.systemLanguage ? "Open Image In Explorer (Ctrl + Enter)" : "在资源管理器中打开（Ctrl + Enter）");//在资源管理器中打开按钮（有选中图片时有效）
    public static JMenu recycleBinMenu = new JMenu(Main.SettingState.systemLanguage ? "Picture Recycle Bin" : "图片回收站");//图片回收站父级菜单项（任何时候有效）
    public static JMenuItem openRecycleBinButton = new JMenuItem(Main.SettingState.systemLanguage ? "Open Image Recycle Bin (Ctrl + O)" : "打开图片回收站（Ctrl + O）");//打开图片回收站按钮（任何时候有效）
    public static JMenuItem emptyRecycleBinButton = new JMenuItem(Main.SettingState.systemLanguage ? "Empty Image Recycle Bin (Ctrl + E)" : "清空图片回收站（Ctrl + E）");//清空图片回收站按钮（任何时候有效）
    public static JMenuItem getPathButton = new JMenuItem(Main.SettingState.systemLanguage ? "Get Image Path (Ctrl + Shift + C)" : "获取图片路径（Ctrl + Shift + C）");//获取图片路径按钮（有选中图片时有效）
    public static JMenuItem uploadToCloudButton = new JMenuItem(Main.SettingState.systemLanguage ? "Upload Picture To Cloud (Ctrl + P)" : "上传图片至云端（Ctrl + P）");//上传图片至云端按钮（用户登录且云端未满且有选中图片）

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
        rightMousePopupMenu.addSeparator();
        rightMousePopupMenu.add(openInOtherButton);
        rightMousePopupMenu.addSeparator();
        rightMousePopupMenu.add(openInExplorerButton);
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
        openInOtherButton.setEnabled(false);
        openInExplorerButton.setEnabled(false);
        getPathButton.setEnabled(false);
        uploadToCloudButton.setEnabled(false);

        cutButton.addActionListener(_ -> handleCut());//处理剪切
        copyButton.addActionListener(_ -> handleCopy());//处理复制
        pasteButton.addActionListener(_ -> handlePaste());//处理粘贴
        renameButton.addActionListener(_ -> handleRename());//处理重命名
        removeButton.addActionListener(_ -> handleRemove());//处理删除
        refreshButton.addActionListener(_ -> handleRefresh());//处理刷新
        openInOtherButton.addActionListener(_ -> handleOpenInOther());//处理在其他软件打开
        openInExplorerButton.addActionListener(_ -> handleOpenInExplorer());//处理在资源管理器打开
        openRecycleBinButton.addActionListener(_ -> handleOpenRecycleBin());//处理打开图片回收站
        emptyRecycleBinButton.addActionListener(_ -> handleEmptyRecycleBin());//处理清空图片回收站
        getPathButton.addActionListener(_ -> handleGetPath());//处理获取路径
        uploadToCloudButton.addActionListener(_ -> {
            handleUserSaveUserUploadPicture(getSelectionThumbnailItemFileList(), true);//处理上传图片至云端
            handleRefresh();//刷新
        });
    }

    public static void handleOpenInOther() {//处理在其他软件打开
        List<ThumbnailItem> fileList = getSelectionThumbnailItemList();//获取文件列表
        if (!fileList.isEmpty()) {//如果非空
            if (fileList.size() > 10) {//如果打开图片过多
                createBottomTipWindow(Main.SettingState.systemLanguage ? "Please Not Open More Than 10 Picture At A Time" : "请勿一次性打开超过10张图片");//提示
            } else {//否则
                try {
                    Desktop desktop = Desktop.getDesktop();//创建桌面类
                    for (ThumbnailItem thumbnailItem : fileList) {//遍历文件列表
                        desktop.open(thumbnailItem.getFile());//打开文件
                    }
                } catch (Exception e) {
                    handleErrorLog(e.getMessage());//处理错误日志
                    System.err.println("Error Desktop Execution:" + e);//捕获异常
                }
            }
        }
    }

    public static void handleOpenInExplorer() {//处理在资源管理器打开
        List<ThumbnailItem> fileList = getSelectionThumbnailItemList();//获取文件列表
        if (!fileList.isEmpty()) {//如果非空
            try {
                Runtime.getRuntime().exec("explorer.exe /select," + fileList.getFirst().getFile().getAbsolutePath());//在资源管理器中打开图片
            } catch (Exception e) {
                handleErrorLog(e.getMessage());//处理错误日志
                System.err.println("Error Explorer Execution:" + e);//捕获异常
            }
        }
    }

    public static void handleOpenRecycleBin() {//处理打开图片回收站
        try {
            Path recycleBinPath = Path.of(String.valueOf(spikeVisionCloudPath), ".appRecycleBin");//获取自定义回收站路径
            if (!Files.exists(recycleBinPath)) {//如果不存在自定义回收站路径
                Files.createDirectories(recycleBinPath);//就创建路径
            }
            Desktop.getDesktop().open(recycleBinPath.toFile());//打开自定义回收站
        } catch (IOException e) {
            handleErrorLog(e.getMessage());//处理错误日志
            throw new RuntimeException(e);//捕获异常
        }
    }

    public static void handleEmptyRecycleBin() {//处理清空图片回收站
        Path recycleBinPath = Path.of(String.valueOf(spikeVisionCloudPath), ".appRecycleBin");//获取自定义回收站路径
        if (Files.exists(recycleBinPath)) {//如果存在自定义回收站路径
            int confirm = JOptionPane.showConfirmDialog(Main.pictureManagementSystemFrame, Main.SettingState.systemLanguage ? "Are You Sure To Empty Picture Recycle Bin? You Cannot Undo This Operation" : "确定要清空图片回收站吗？这个操作无法撤销", Main.SettingState.systemLanguage ? "Empty Confirm" : "确认清空", JOptionPane.YES_NO_OPTION);//创建确认信息
            if (confirm == JOptionPane.YES_OPTION) {//如果确认
                File[] recycleBinFileList = new File(String.valueOf(recycleBinPath)).listFiles();//获取回收站内图片
                if (recycleBinFileList != null) {//如果非空
                    for (File file : recycleBinFileList) {//遍历
                        try {
                            undoStack.removeIf(fileOperation -> fileOperation.type == DELETE);//删除撤销栈中的全部删除操作
                            Files.deleteIfExists(file.toPath());//删除图片
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

    public static void handleAutoEmptyRecycleBin() {//处理自动清空图片回收站
        Path recycleBinPath = Path.of(String.valueOf(spikeVisionCloudPath), ".appRecycleBin");//获取自定义回收站路径
        if (Files.exists(recycleBinPath)) {//如果存在自定义回收站路径
            File[] recycleBinFileList = new File(String.valueOf(recycleBinPath)).listFiles();//获取回收站内图片
            if (recycleBinFileList != null) {//如果非空
                for (File file : recycleBinFileList) {//遍历
                    try {
                        undoStack.removeIf(fileOperation -> fileOperation.type == DELETE);//删除撤销栈中的全部删除操作
                        Files.deleteIfExists(file.toPath());//删除图片
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
            StringSelection stringSelection = new StringSelection(newContent.toString());//将新内容放入剪贴板
            clipboard.setContents(stringSelection, null);//设置剪贴板内容
            createBottomTipWindow(Main.SettingState.systemLanguage ? "Image Path Copied To Clipboard" : "图片路径已复制到剪贴板");
        }
    }

    public static void handleSetAsLockscreen() {//处理设置图片为锁屏
        List<ThumbnailItem> list = getSelectionThumbnailItemList();//获取被选中的图片列表
        if (list.size() == 1) {//如果大小为1
            File file = list.getFirst().getFile();//获取文件
            if (file != null && file.exists()) {//如果文件不为空且存在
                if (administratorJudgement()) {//管理员判断
                    createBottomTipWindow(Main.SettingState.systemLanguage ? "Insufficient Permission, Run The Program With Administrator Privilege" : "权限不足，请使用管理员权限运行程序");//提示
                    return;//直接返回
                }
                try {
                    setLockScreenByCSP(file);//通过CSP设置锁屏
                } catch (Exception e) {
                    handleErrorLog(e.getMessage());//处理错误日志
                    throw new RuntimeException(e);//捕获异常
                }
            } else {
                createBottomTipWindow(Main.SettingState.systemLanguage ? "Fail To Set Picture As Lockscreen, The Picture Is Invalid" : "设置锁屏失败，图片已失效");//提示
            }
        }
    }

    private static void setLockScreenByCSP(File file) throws Exception {//通过CSP设置锁屏
        String psCommand = String.format("Set-ItemProperty -Path 'HKLM:\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\PersonalizationCSP' " +
                "-Name LockScreenImagePath -Value '%s' -Force; " +
                "Set-ItemProperty -Path 'HKLM:\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\PersonalizationCSP' " +
                "-Name LockScreenImageUrl -Value '%s' -Force; " +
                "Set-ItemProperty -Path 'HKLM:\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\PersonalizationCSP' " +
                "-Name LockScreenImageStatus -Value 1 -Force", file.getAbsolutePath(), file.getAbsolutePath());//修改锁屏命令
        executeCommand("powershell", "-Command", psCommand);//执行命令
    }

    public static void handleSetAsWallpaper() {//处理设置图片为壁纸
        List<ThumbnailItem> list = getSelectionThumbnailItemList();//获取被选中的图片列表
        if (list.size() == 1) {//如果大小为1
            File file = list.getFirst().getFile();//获取文件
            if (file != null && file.exists()) {//如果文件不为空且存在
                File normalizedFile;//经过处理后的文件
                try {
                    normalizedFile = new File(normalizePath(file));//对文件路径进行简化处理
                    setWallpaperByRegistry(normalizedFile);//通过注册表设置壁纸
                    refreshWallpaper();//刷新壁纸
                    Thread.sleep(1000);//延迟验证
                    if (!isWallpaperChanged(normalizedFile)) {//验证结果：如果壁纸未更新
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "Wallpaper Not Update" : "壁纸未更新");//提示
                        return;//直接返回
                    }
                    createBottomTipWindow(Main.SettingState.systemLanguage ? "Success to Set Wallpaper" : "壁纸设置成功");//提示
                } catch (Exception e) {
                    handleErrorLog(e.getMessage());//处理错误日志
                    createBottomTipWindow(Main.SettingState.systemLanguage ? "Failed to Set Wallpaper" : "壁纸设置失败");//提示
                    throw new RuntimeException(e);//捕获异常
                }
            } else {
                createBottomTipWindow(Main.SettingState.systemLanguage ? "Fail To Set Picture As Wallpaper, The Picture Is Invalid" : "设置壁纸失败，图片已失效");//提示
            }
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

    private static String normalizePath(File file) throws Exception {//处理文件路径
//        String psCmd = "(Get-Item -LiteralPath '" + file.getAbsolutePath().replace("'", "''") + "').FullName";//获取命令
//        Process p = Runtime.getRuntime().exec(new String[]{"powershell.exe", "-Command", psCmd});//使用PowerShell获取更可靠的短路径
//        try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), "GBK"))) {//通过GBK编码创建缓冲读入者
//            String result = br.readLine();//获取结果
//            return (result != null && new File(result).exists()) ? result : file.getAbsolutePath();//返回路径结果
//        }

        String escapedPath = file.getAbsolutePath().replace("'", "''");//通过文件绝对路径创建命令并替换引号
        String psCmd = "Get-Item -LiteralPath '" + escapedPath + "' | Select-Object -ExpandProperty FullName";//创建powershell命令
        Process p = Runtime.getRuntime().exec(new String[]{"powershell.exe", "-NoProfile", "-ExecutionPolicy", "Bypass", "-Command", psCmd});//使用PowerShell获取更可靠的短路径
        try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {//通过UTF_8编码创建缓冲读入者
            String result = br.readLine();//获取结果
            return result != null && new File(result).exists() ? result : file.getAbsolutePath();//返回路径结果
        }
    }

    private static void setWallpaperByRegistry(File file) throws Exception {//通过注册表设置壁纸
        executeCommand("reg", "add", "HKCU\\Control Panel\\Desktop", "/v", "Wallpaper", "/t", "REG_SZ", "/d", file.getAbsolutePath(), "/f");//设置壁纸路径
        executeCommand("reg", "add", "HKCU\\Control Panel\\Desktop", "/v", "WallpaperStyle", "/t", "REG_SZ", "/d", "10", "/f");//设置显示样式
        executeCommand("reg", "add", "HKCU\\Control Panel\\Desktop", "/v", "TileWallpaper", "/t", "REG_SZ", "/d", "0", "/f");//铺设壁纸
        executeCommand("reg", "add", "HKCU\\Control Panel\\Desktop", "/v", "OriginalWallpaper", "/t", "REG_SZ", "/d", file.getAbsolutePath(), "/f");//设置原始壁纸
        executeCommand("reg", "add", "HKCU\\Control Panel\\Desktop", "/v", "MultiMonitorBackground", "/t", "REG_SZ", "/d", "1", "/f");//多显示器支持
    }

    private static void refreshWallpaper() throws Exception {//刷新壁纸
//        executeCommand("rundll32.exe", "user32.dll,UpdatePerUserSystemParameters");//刷新注册表

        executeCommand("rundll32.exe", "user32.dll,UpdatePerUserSystemParameters", "1");//强制刷新注册表
    }

    private static void executeCommand(String... command) throws Exception {//处理命令
//        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();//命令
//        StringBuilder output = new StringBuilder();//捕获输出（使用GBK编码）
//        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "GBK"))) {//通过命令创建读入者
//            String line;//每一行命令
//            while ((line = reader.readLine()) != null) {//如果读入不为空
//                output.append(line).append("\n");//就往输出增加行
//            }
//        }
//        int exitCode = process.waitFor();//处理并获取退出代码
//        if (exitCode != 0) {//如果不是正常退出
//            String errorDetail = String.format("命令执行失败: %s\n退出码: %d\n输出日志:\n%s", String.join(" ", command), exitCode, output);//输出错误信息
//            throw new RuntimeException(errorDetail);//抛出错误
//        }

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

    private static boolean isWallpaperChanged(File expectedFile) {//验证壁纸更换是否生效
        try {
            Process p = Runtime.getRuntime().exec("reg query \"HKCU\\Control Panel\\Desktop\" /v Wallpaper");//创建刷新注册表命令
            p.waitFor();//处理
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), "GBK"))) {//通过GBK编码创建缓冲读入者
                String line;//创建每一行输出
                while ((line = reader.readLine()) != null) {//如果读入不为空
                    if (line.contains("REG_SZ")) {//如果每一行包含受执行命令的注册表
                        String currentPath = line.split("\\s{4}")[3];//切割这一行创建当前路径
                        return currentPath.equalsIgnoreCase(expectedFile.getAbsolutePath());//返回路径（忽略大小写）
                    }
                }
            }
            return false;//返回错误
        } catch (Exception e) {
            handleErrorLog(e.getMessage());//处理错误日志
            return false;//返回错误
        }

//        try {
//            Process p = Runtime.getRuntime().exec("reg query \"HKCU\\Control Panel\\Desktop\" /v Wallpaper");//创建刷新注册表命令
//            p.waitFor();//处理
//            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {//通过UTF_8编码创建缓冲读入者
//                String line;//每一行输出
//                while ((line = reader.readLine()) != null) {//如果读入不为空
//                    if (line.contains("REG_SZ")) {//如果每一行包含受执行命令的注册表
//                        String currentPath = line.split("\\s+")[4].trim();//切割这一行创建当前路径
//                        currentPath = currentPath.replace("\"", "");//移除引号
//                        return currentPath.equalsIgnoreCase(expectedFile.getAbsolutePath());//返回路径（忽略大小写）
//                    }
//                }
//            }
//            return false;//返回错误
//        } catch (Exception e) {
//            handleErrorLog(e.getMessage());//处理错误日志
//            return false;//返回错误
//        }
    }
}
