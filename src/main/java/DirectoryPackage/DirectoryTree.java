package DirectoryPackage;

import MainPackage.Main;
import NetworkPackage.User;
import FileDisplayPackage.FileDisplayMainPanel;
import FileDisplayPackage.FileDisplayTopBar;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import static MainPackage.Setting.*;
import static MainPackage.ThemeColor.*;
import static NetworkPackage.User.*;
import static FileDisplayPackage.FileDisplayMainPanel.*;
import static FileDisplayPackage.FileDisplayTopBar.*;

public class DirectoryTree {//目录树类：采用懒加载方式，即只有打开文件夹时才对目录进行加载，极大优化程序
    public static JTree directoryTree;//目录树
    public static DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(Main.SettingState.systemLanguage ? "Device" : "设备");//根结点
    public static DefaultMutableTreeNode computerNode = new DefaultMutableTreeNode(Main.SettingState.systemLanguage ? "My Computer" : "我的电脑");//电脑结点
    public static DefaultMutableTreeNode pictureNode;//图片结点
    public static DefaultMutableTreeNode cloudNode = new DefaultMutableTreeNode(Main.SettingState.systemLanguage ? "My Cloud" : "我的云盘");//云盘结点

    public static JWindow bottomTipWindow;//底部提示窗口
    public static JLabel bottomTipLabel = new JLabel();//底部提示标签
    public static JPanel roundRectangleBottomTipPanel = new JPanel() {//自定义圆角矩形底部提示面板
        @Override
        protected void paintComponent(Graphics g) {//进行绘制
            super.paintComponent(g);//调用父类清除背景
            Graphics2D g2d = (Graphics2D) g.create();//创建工具类
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(Color.WHITE);//设置背景为白色
            g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 17, 17));//绘制圆角矩形背景
            g2d.setColor(Color.GRAY);//设置边框为灰色
            g2d.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 17, 17));//绘制圆角矩形边框
            g2d.dispose();//释放
        }
    };

    private static File[] pictureFileList = null;//当前图片文件数组
    public static Object currentNodeObject = null;//当前悬浮结点

    public DirectoryTree() {//构造方法
        bottomTipLabel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.GRAY), BorderFactory.createEmptyBorder(5, 5, 5, 5)));//设置边框
        bottomTipLabel.setFont(new Font("楷体", Font.PLAIN, 25));//设置字体
        bottomTipLabel.setForeground(Color.BLACK);//设置前景色

        roundRectangleBottomTipPanel.setLayout(new BorderLayout());//设置布局管理器

        rootNode.add(computerNode);//将我的电脑添加到根结点中
        File[] roots = File.listRoots();//通过File类自带的listRoots方法获取该电脑所有磁盘根目录（盘符如C:\, D:\等）
        Arrays.stream(roots).forEach(root -> {//遍历所有盘符创建子结点
            DefaultMutableTreeNode driveNode = new DefaultMutableTreeNode(root);//创建盘符结点，存储对应的File对象
            driveNode.add(new DefaultMutableTreeNode(new Placeholder()));//为所有盘符添加占位符结点用于触发懒加载（因设计为程序运行后显示根结点我的电脑和子结点盘符，所以只需要从盘符开始添加占位符结点即可）
            computerNode.add(driveNode);//将盘符结点添加到根结点中
        });

        File picturesDir;//创建系统图片路径
        if (!Main.SettingState.pictureDirectory.isEmpty()) {//如果拥有对图片路径的存档
            picturesDir = new File(Main.SettingState.pictureDirectory);//就设置路径为存档路径
            pictureNode = new DefaultMutableTreeNode(picturesDir);//是则创建图片结点
            pictureNode.add(new DefaultMutableTreeNode(new Placeholder()));//也要为其添加占位符
            rootNode.add(pictureNode);//将其添加到根结点中
        } else {//否则
            String userHome = System.getProperty("user.home");//获取用户主目录
            String picturesSubdir = "Pictures";//获取系统图片子路径，优先检查Pictures（新版Windows）
            picturesDir = new File(userHome + File.separator + picturesSubdir);//拼接得到系统图片文件夹
            if (!picturesDir.exists()) {//如果该路径不存在
                picturesSubdir = "My Pictures";//尝试My Pictures，旧版Windows可能使用此名称
                picturesDir = new File(userHome + File.separator + picturesSubdir);//重新构造图片文件夹
            }
            if (picturesDir.exists() && picturesDir.isDirectory()) {//验证路径是否存在且路径是文件夹
                pictureNode = new DefaultMutableTreeNode(picturesDir);//是则创建图片结点
                pictureNode.add(new DefaultMutableTreeNode(new Placeholder()));//也要为其添加占位符
                rootNode.add(pictureNode);//将其添加到根结点中
                Main.SettingState.pictureDirectory = picturesDir.getAbsolutePath();//将图片地址存档
            } else {//否则
                if (Main.SettingState.utilizeTimes == 1) {//如果用户第一次使用，弹出菜单提示
                    JOptionPane.showMessageDialog(null, Main.SettingState.systemLanguage ? "To Users Who Using For First Time: Unable To Found System Picture Folder Directory, Please Setting Manually" : "致第一次使用该软件的用户：无法获取您的系统图片文件夹路径，请前往设置手动更改", Main.SettingState.systemLanguage ? "Error" : "错误", JOptionPane.ERROR_MESSAGE);//报错
                } else {//否则多次使用，只弹出底部信息提示
                    createBottomTipWindow(Main.SettingState.systemLanguage ? "Unable To Found System Picture Folder Directory, Please Setting Manually" : "无法获取您的系统图片文件夹路径，请前往设置手动更改");//创建提示窗口
                }
            }
        }
        if (!Main.SettingState.userAccount.isEmpty()) {//如果用户登录
            rootNode.add(cloudNode);//将云盘结点添加到根结点中
        }

        directoryTree = new JTree(new DefaultTreeModel(rootNode));//通过树模型创建目录树
        configureTreeComponents(rootNode);//配置树组件

        directoryTree.addTreeSelectionListener(e -> {//监听点击树结点事件
            if (draggedThumbnailItemWindow == null) {//如果被拖拽缩略图项目窗口为空
                TreePath path = e.getPath();//获取结点路径
                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();//获取路径的最后一个结点（因为结点路径包括前面的结点，而每一个结点都是文件绝对路径，所以只获取最后一个点击结点即可）
                Object nodeObject = selectedNode.getUserObject();//获取结点对象
                if (nodeObject instanceof File selectedFile) {//如果结点对象是文件
                    if (!Objects.equals(currentFolder, selectedFile.getPath())) {//如果结点变化
                        pictureFileList = detectPictureFile(selectedFile.listFiles());//更新图片文件列表
                        String currentFolder = selectedFile.getPath();//获取当前文件夹
                        FileDisplayTopBar.updateFolder(currentFolder);//更新当前文件夹和文件夹列表
                        FileDisplayTopBar.setDirectoryField(currentFolder);//设置当前文件路径文本
                        FileDisplayMainPanel.updateMainPanel(false);//通知更新图片预览面板（采用类名调用的方式，防止创建多个类）
                        directoryManipulationButtonEnableJudgement();//按钮判断
                    }
                } else if (nodeObject instanceof String && nodeObject.equals(Main.SettingState.systemLanguage ? "My Cloud" : "我的云盘")) {//如果是云盘结点
                    if (!Objects.equals(currentFolder, Main.SettingState.systemLanguage ? "My Cloud" : "我的云盘")) {//如果结点变化
                        try {
                            pictureFileList = User.handleUserLoadUserUploadPicture(null);//更新图片文件列表
                        } catch (IOException ex) {
                            handleErrorLog(ex.getMessage());//处理错误日志
                            throw new RuntimeException(ex);//捕获异常
                        }
                        String currentFolder = Main.SettingState.systemLanguage ? "My Cloud" : "我的云盘";//获取当前文件夹
                        FileDisplayTopBar.updateFolder(currentFolder);//更新当前文件夹和文件夹列表
                        FileDisplayTopBar.setDirectoryField(currentFolder);//设置当前文件路径
                        FileDisplayMainPanel.updateMainPanel(false);//通知更新图片预览面板（采用类名调用的方式，防止创建多个类）
                        directoryManipulationButtonEnableJudgement();//按钮判断
                    }
                }//如果是根结点则不处理
            }
        });

        directoryTree.addMouseListener(new MouseAdapter() {//为目录树添加鼠标事件监听
            static Timer pathHoverTimer = null;//路径悬浮计时器

            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                if (draggedThumbnailItemWindow != null) {//如果被拖动缩略图项目窗口不为空
                    TreePath currentPath = directoryTree.getPathForLocation(e.getX(), e.getY());//获取鼠标坐标对应的结点路径
                    if (currentPath != null) {//如果路径不为空
                        DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) currentPath.getLastPathComponent();//获取当前结点
                        currentNodeObject = currentNode.getUserObject();//获得结点对象
                        directoryTree.setSelectionPath(currentPath);//选中节点
                        if (pathHoverTimer != null && pathHoverTimer.isRunning()) {//如果当前仍有计时器
                            pathHoverTimer.stop();//就停止计时器
                        }
                        pathHoverTimer = new Timer(1000, _ -> {//如果悬浮在某个结点超过1秒
                            directoryTree.expandPath(currentPath);//就自动打开结点
                        });
                        pathHoverTimer.start();//开始计时器
                        pathHoverTimer.setRepeats(false);//设置不重复
                    } else {//否则
                        currentNodeObject = null;//结点置空
                    }
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {//当鼠标离开
                if (draggedThumbnailItemWindow != null) {//如果被拖动缩略图项目窗口不为空
                    if (currentNodeObject != null) {//如果路径不为空
                        currentNodeObject = null;//结点置空防止鼠标离开结点仍可剪切
                    }
                }
            }
        });
    }

    public static void createCloudNode() {//创建云盘结点
        rootNode.add(cloudNode);//将其添加到根结点中
        directoryTree.setModel(new DefaultTreeModel(rootNode));//重新设置目录树模型
        if (!folderList.isEmpty()) {//如果不为空
            folderList.removeLast();//重新添加树结点后会复制当前结点，需要手动移除
            if (folderListIndex == folderList.size()) {//如果在最后一个结点
                folderListIndex--;//自减
            }
        }
    }

    public static void removeCloudNode() {//删除云盘结点
        if (Objects.equals(currentFolder, Main.SettingState.systemLanguage ? "My Cloud" : "我的云盘")) {//如果退出时在云盘结点
            currentFolder = null;//当前文件夹置空
            directoryField.setTextField();//目录文本域清空
            if (itemHoverTipWindow != null) {//如果提示信息不为空
                itemHoverTipWindow.dispose();//释放提示信息
                itemHoverTipWindow = null;//提示信息置空
            }
            mainPanel.removeAll();//清空
            refreshMainPanel();//刷新
            directoryTree.setSelectionPath(directoryTree.getPathForRow(0));//选中节点
        }
        rootNode.remove(cloudNode);//从根结点中删除云盘结点
        directoryTree.setModel(new DefaultTreeModel(rootNode));//重新设置目录树模型
        if (!folderList.isEmpty()) {//如果不为空
            folderList.removeLast();//重新添加树结点后会复制当前结点，需要手动移除
            if (folderListIndex == folderList.size()) {//如果在最后一个结点
                folderListIndex--;//自减
            }
        }
    }

    public static File[] getPictureFileList() {//获取图片文件数组（供Main调用）
        return pictureFileList;
    }

    public static void setPictureFileList(File[] pictureFileList) {//设置图片文件列表（供PicturePreviewTopBar调用）
        DirectoryTree.pictureFileList = pictureFileList;
    }

    public static void updatePictureFileList(File[] fileList) {//更新图片文件列表
        pictureFileList = detectPictureFile(fileList);//获取图片文件后赋值给图片文件数组
    }

    private static void configureTreeComponents(DefaultMutableTreeNode rootNode) {//配置树组件属性
        directoryTree.setFont(new Font("楷体", Font.PLAIN, 17));//设置字体
        directoryTree.setFocusable(false);//不可聚焦
        directoryTree.setRowHeight(20);//设置行高
        directoryTree.setShowsRootHandles(true);//显示根结点的展开手柄
        directoryTree.expandPath(new TreePath(rootNode.getPath()));//自动展开根结点
        directoryTree.setCellRenderer(new DriveTreeRenderer());//设置自定义节点渲染器
        directoryTree.addTreeWillExpandListener(new DriveTreeExpansionListener());//添加展开事件监听器
        directoryTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);//设置目录树只可单选不可多选
        directoryTree.setBackground(Main.SettingState.themeColor ? DARK_DIRECTORY_MAIN_COLOR : LIGHT_DIRECTORY_MAIN_COLOR);//设置背景色
    }

    static class DriveTreeExpansionListener implements TreeWillExpandListener {//目录树展开事件监听器

        @Override
        public void treeWillExpand(TreeExpansionEvent event) {//当树展开时
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();//获取被展开的结点，根结点或云盘结点不处理
            if (node.getUserObject() instanceof File) {//如果是盘符根目录或图片目录或普通文件目录
                handleDirectoryExpansion(node);//就将其展开
            }
        }

        private void handleDirectoryExpansion(DefaultMutableTreeNode node) {//处理盘符根目录或图片目录或普通文件目录的展开
            if (node.getChildCount() == 1 && ((DefaultMutableTreeNode) node.getChildAt(0)).getUserObject() instanceof Placeholder) {//检查是否需要移除占位符，如果只有一个子结点且该结点是占位符类
                node.removeAllChildren();//移除占位符结点
                loadDirectoryContents(node, (File) node.getUserObject());//加载实际内容
            }
        }

        private void loadDirectoryContents(DefaultMutableTreeNode parentNode, File directory) {//加载目录内容到指定结点
            try {
                File[] subDirs = directory.listFiles(file -> file.isDirectory() && !file.isHidden() && !isSystemDirectory(file));//获取符合条件的子目录（过滤隐藏/系统/特殊目录）
                if (subDirs != null) {//只要过滤后目录不为空
                    Arrays.stream(subDirs).filter(File::canRead).forEach(dir -> {//过滤可读目录
                        DefaultMutableTreeNode child = new DefaultMutableTreeNode(dir);//创建子结点
                        child.add(new DefaultMutableTreeNode(new Placeholder()));//添加占位符
                        parentNode.add(child);//把子结点添加到父结点中
                    });
                }
            } catch (SecurityException e) {
                handleErrorLog(e.getMessage());//处理错误日志
                System.err.println("SecurityException:" + directory.getAbsolutePath());//捕获异常
            }
        }

        @Override
        public void treeWillCollapse(TreeExpansionEvent event) {//被迫重写，但空实现
        }
    }

    private static boolean isSystemDirectory(File file) {//判断是否是系统目录
        String name = file.getName();//获取名称
        return name.equals("System Volume Information") || name.equals("Documents and Settings") || name.equals("$Recycle.Bin") || name.equals("Recovery");//匹配常见系统目录名称
    }

    public static class DriveTreeRenderer extends DefaultTreeCellRenderer {//自定义树结点渲染器
        private final Icon deviceIcon = new ImageIcon("src/material/image/home.png");//设备图标
        private final Icon computerIcon = new ImageIcon("src/material/image/myComputer.png");//我的电脑图标
        private final Icon driveIcon = new ImageIcon("src/material/image/disk.png");//磁盘图标
        private final Icon folderOpenIcon = new ImageIcon("src/material/image/folderOpen.png");//文件夹图标
        private final Icon folderCloseIcon = new ImageIcon("src/material/image/folderClose.png");//文件夹图标
        private final Icon pictureIcon = new ImageIcon("src/material/image/picture.png");//图片图标
        private final Icon cloudIcon = new ImageIcon("src/material/image/cloud.png");//云盘图标

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {//重写树结点渲染
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);//调用父类方法初始化基础设置
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;//获取结点
            Object userObject = node.getUserObject();//获取结点对象

            if (userObject instanceof String) {//如果是根结点或云盘
                handleStringNodeRendering((String) userObject);//对根结点或云盘进行渲染
            } else if (userObject instanceof File) { //如果是盘符根目录或图片目录或普通文件目录
                handleFileNodeRendering((File) userObject);//对盘符根目录或图片目录或普通文件目录进行渲染
            }
            if (Main.SettingState.backgroundPictureDirectory.isEmpty()) {//如果背景图片路径为空
                setForeground(Main.SettingState.themeColor ? DARK_DIRECTORY_TEXT_NON_SELECTION_COLOR : LIGHT_DIRECTORY_TEXT_NON_SELECTION_COLOR);
                setBackgroundSelectionColor(Main.SettingState.themeColor ? DARK_DIRECTORY_BACKGROUND_SELECTION_COLOR : LIGHT_DIRECTORY_BACKGROUND_SELECTION_COLOR);//设置选择背景颜色
                setBackgroundNonSelectionColor(Main.SettingState.themeColor ? DARK_DIRECTORY_BACKGROUND_NON_SELECTION_COLOR : LIGHT_DIRECTORY_BACKGROUND_NON_SELECTION_COLOR);//设置未选择背景颜色
                setTextSelectionColor(Main.SettingState.themeColor ? DARK_DIRECTORY_TEXT_SELECTION_COLOR : LIGHT_DIRECTORY_TEXT_SELECTION_COLOR);//设置选择文字颜色
                setTextNonSelectionColor(Main.SettingState.themeColor ? DARK_DIRECTORY_TEXT_NON_SELECTION_COLOR : LIGHT_DIRECTORY_TEXT_NON_SELECTION_COLOR);//设置未选择文字颜色
                setBorderSelectionColor(Main.SettingState.themeColor ? DARK_DIRECTORY_BORDER_SELECTION_COLOR : LIGHT_DIRECTORY_BORDER_SELECTION_COLOR);//设置选择边框颜色
            } else {//否则
                setBackgroundSelectionColor(new Color(255, 255, 255, 50));//设置选择背景颜色
                setBackgroundNonSelectionColor(null);//设置未选择背景颜色
                setTextSelectionColor(Color.black);//设置选择文字颜色
                setTextNonSelectionColor(Color.black);//设置未选择文字颜色
                setBorderSelectionColor(null);//设置选择边框颜色
            }
            return this;//返回自身
        }

        private void handleStringNodeRendering(String label) {//渲染根结点或云盘
            if (Objects.equals(label, Main.SettingState.systemLanguage ? "Device" : "设备")) {//如果是根结点
                setText(label);//设置显示文本
                setIcon(deviceIcon);//设置设备图标
            } else if (Objects.equals(label, Main.SettingState.systemLanguage ? "My Computer" : "我的电脑")) {//如果是我的电脑结点
                setText(label);//设置显示文本
                setIcon(computerIcon);//设置我的电脑图标
            } else {//否则是云盘目录
                setText(label);//设置显示文本
                setIcon(cloudIcon);//设置云盘图标
            }
        }

        private void handleFileNodeRendering(File file) {//渲染盘符根目录或图片目录或普通文件目录
            if (isDriveRoot(file)) {//如果是盘符根目录
                setText(getDriveDisplayName(file));//显示盘符名称
                setIcon(driveIcon);//设置磁盘图标
            } else if (isPictureDirectory(file)) {//如果是图片目录
                setText(file.getName());//显示图片目录名
                setIcon(pictureIcon);//设置图片图标
            } else {//如果是普通目录
                setText(file.getName());//显示目录名
                setOpenIcon(folderOpenIcon);//设置打开状态图标
                setClosedIcon(folderCloseIcon);//设置关闭状态图片
                setLeafIcon(folderCloseIcon);//设置叶子图片
            }
        }

        private boolean isDriveRoot(File file) {//判断是否为盘符根目录
            return file.getPath().matches("^[A-Z]:\\\\$");//通过正则表达式判断是否是盘符+:+\
        }

        private boolean isPictureDirectory(File file) {//判断是否为图片目录
            String[] split = file.getPath().split("\\\\");//截取绝对路径
            return split[split.length - 1].matches(".*Pictures.*");//通过正则表达式判断文件名是否含Pictures
        }

        private String getDriveDisplayName(File drive) {//生成盘符显示名称
            String type = getDriveType(drive);//获取磁盘类型
            String letter = drive.getPath().substring(0, 1);//获取盘符字母
            return String.format("%s (%s)", type, letter);
        }

        private String getDriveType(File drive) {//判断磁盘类型
            if (drive.getTotalSpace() == 0) {
                return Main.SettingState.systemLanguage ? "CD Driver" : "CD 驱动器";//光盘驱动器
            } else if (drive.getPath().startsWith("\\\\")) {
                return Main.SettingState.systemLanguage ? "Network Driver" : "网络驱动器";//网络路径
            } else {
                return Main.SettingState.systemLanguage ? "Local Disk" : "本地磁盘";//本地硬盘
            }
        }
    }

    public static class Placeholder {//占位符标识类（用于触发懒加载）
    }//如果没有占位符，所有未打开的结点会变成叶子结点，会陷入只有双击打开结点才能将结点变成非叶子结点，但结点是叶子结点无法打开的逻辑闭环，所以一开始要往结点添加占位符使得结点是非叶子结点，可以打开，打开后再把占位符删除

    public static File[] detectPictureFile(File[] fileList) {//检查图片文件：获取传入文件列表中的图片文件，并返回图片文件列表
        if (fileList == null) return new File[0];//为空直接返回
        ArrayList<File> pictureList = new ArrayList<>();//创建一个集合列表用于返回
        for (File file : fileList) {//遍历列表
            if (file == null || !file.isFile()) continue;//如果文件为空或文件不是文件就continue
            if (detectPictureFileByMagicNumber(file)) {//如果列表中的文件是图片（采用一种检测方法）
                pictureList.add(file);//就往集合列表中进行添加
            }
        }
        return pictureList.toArray(new File[0]);//再返回集合
    }

//    private static boolean detectByExtension(File file) {//方法1：扩展名检测
//        if (file == null || !file.isFile()) return false;//如果文件为空或文件不是文件就返回否
//        String name = file.getName().toLowerCase();//获取文件名字，注意全部变成小写
//        return name.endsWith(".jpg") || name.endsWith(".jpeg")//对结尾进行匹配
//                || name.endsWith(".gif") || name.endsWith(".png")
//                || name.endsWith(".bmp");
//    }

    public static boolean detectPictureFileByMagicNumber(File file) {//方法2：魔数检测
        if (currentWorker != null && !currentWorker.isDone()) {//如果当前有任务
            currentWorker.cancel(true);//取消未完成的任务
        }
        if (file == null || !file.isFile()) return false;//如果文件为空或文件不是文件就返回否
        try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {//根据文件创建文件输入流再转化成缓存输入流再转化成数据输入流
            byte[] header = new byte[8];//创建文件前8位字节数组
            int bytesRead = dis.read(header);//通过数据输入流读取文件前8位字节，返回一个int类型的数字,表示真是读取的有效字节数
            return isJpegFile(header, bytesRead) || isPngFile(header, bytesRead) || isGifFile(header, bytesRead) || isBmpFile(header, bytesRead);//调用文件类型判断方法
        } catch (IOException e) {
            System.err.println("IOException:" + file.getAbsolutePath());//捕获异常
            return false;//返回错误
        }
    }

    private static boolean isJpegFile(byte[] header, int bytesRead) {//魔数检测辅助方法：判断是否为jpeg
        return bytesRead >= 2//如果读取有效字节数超过2
                && (header[0] & 0xFF) == 0xFF//第一个字节是十六进制的FF，以此类推
                && (header[1] & 0xFF) == 0xD8;
    }

    private static boolean isPngFile(byte[] header, int bytesRead) {//魔数检测辅助方法：判断是否为png
        return bytesRead >= 8
                && header[0] == (byte) 0x89
                && header[1] == 0x50
                && header[2] == 0x4E
                && header[3] == 0x47
                && header[4] == 0x0D
                && header[5] == 0x0A
                && header[6] == 0x1A
                && header[7] == 0x0A;
    }

    private static boolean isGifFile(byte[] header, int bytesRead) {//魔数检测辅助方法：判断是否为gif
        return bytesRead >= 4
                && header[0] == 'G'
                && header[1] == 'I'
                && header[2] == 'F'
                && header[3] == '8';
    }

    private static boolean isBmpFile(byte[] header, int bytesRead) {//魔数检测辅助方法：判断是否为bmp
        return bytesRead >= 2
                && header[0] == 'B'
                && header[1] == 'M';
    }

//    private static boolean detectByImageIO(File file) {//方法3：ImageIO检测
//        if (file == null || !file.isFile()) return false;//如果文件为空或文件不是文件就返回否
//        try (ImageInputStream iis = ImageIO.createImageInputStream(file)) {
//            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
//            if (readers.hasNext()) {
//                ImageReader reader = readers.next();
//                String format = reader.getFormatName().toUpperCase();
//                return format.equals("JPEG") || format.equals("JPG")
//                        || format.equals("PNG") || format.equals("GIF")
//                        || format.equals("BMP");
//            }
//        } catch (IOException e) {
//            return false;//f
//        }
//        return false;//没有检测到
//    }

    public static void createBottomTipWindow(String tipInformation) {//创建屏幕下方提示信息
        if (bottomTipWindow != null) {//如果底部提示窗口不为空
            bottomTipWindow.dispose();//释放底部提示窗口
            bottomTipWindow = null;//底部提示窗口置空
        }
        if (Main.slideFrame != null && Main.slideFrame.isVisible()) {//如果当前在幻灯片窗口
            bottomTipWindow = new JWindow(Main.slideFrame);//创建提示窗口（设置父组件防止覆盖）
        } else {//否则
            if (insertImageDialog.isVisible()) {//如果插入图片窗口可见
                bottomTipWindow = new JWindow(insertImageDialog);//创建提示窗口（设置父组件防止覆盖）
            } else if (suggestionFeedbackDialog.isVisible()) {//如果建议反馈窗口可见
                bottomTipWindow = new JWindow(suggestionFeedbackDialog);//创建提示窗口（设置父组件防止覆盖）
            } else if (settingDialog.isVisible()) {//如果设置窗口可见
                bottomTipWindow = new JWindow(settingDialog);//创建提示窗口（设置父组件防止覆盖）
            } else if (logInDialog.isVisible()) {//如果登录窗口可见
                bottomTipWindow = new JWindow(logInDialog);//创建提示窗口（设置父组件防止覆盖）
            } else if (registerDialog.isVisible()) {//如果注册窗口可见
                bottomTipWindow = new JWindow(registerDialog);//创建提示窗口（设置父组件防止覆盖）
            } else if (changeUserAccountDialog.isVisible()) {//如果更改用户名窗口可见
                bottomTipWindow = new JWindow(changeUserAccountDialog);//创建提示窗口（设置父组件防止覆盖）
            } else if (changeUserPhoneDialog.isVisible()) {//如果更改手机号窗口可见
                bottomTipWindow = new JWindow(changeUserPhoneDialog);//创建提示窗口（设置父组件防止覆盖）
            } else if (changeUserPasswordDialog.isVisible()) {//如果更改密码窗口可见
                bottomTipWindow = new JWindow(changeUserPasswordDialog);//创建提示窗口（设置父组件防止覆盖）
            } else if (logOutDialog.isVisible()) {//如果注销窗口可见
                bottomTipWindow = new JWindow(logOutDialog);//创建提示窗口（设置父组件防止覆盖）
            } else if (userDialog.isVisible()) {//如果用户窗口可见
                bottomTipWindow = new JWindow(userDialog);//创建提示窗口（设置父组件防止覆盖）
            } else {//否则
                bottomTipWindow = new JWindow(Main.pictureManagementSystemFrame);//创建提示窗口（设置父组件防止覆盖）
            }
        }

        bottomTipLabel.setText(tipInformation);//设置提示信息
        roundRectangleBottomTipPanel.removeAll();//清空
        roundRectangleBottomTipPanel.add(bottomTipLabel, BorderLayout.CENTER);//将标签添加到自定义面板中
        bottomTipWindow.setContentPane(roundRectangleBottomTipPanel);//放入内容
        bottomTipWindow.setAlwaysOnTop(Main.SettingState.windowState);//设置永远在最上层
        bottomTipWindow.pack();//合适
        bottomTipWindow.setLocation(Main.screenSize.width / 2 - bottomTipWindow.getWidth() / 2, 4 * Main.screenSize.height / 5);//设置位置
        bottomTipWindow.setShape(new RoundRectangle2D.Double(0, 0, bottomTipWindow.getWidth(), bottomTipWindow.getHeight(), 17, 17));//绘制圆角窗口
        bottomTipWindow.setVisible(true);//设置可见
        new Timer(3000, e -> {//3秒后
            bottomTipWindow.dispose();//关闭窗口
            ((Timer) e.getSource()).stop();//结束计时
        }).start();//开始计时
    }
}
