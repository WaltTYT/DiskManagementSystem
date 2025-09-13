package FileDisplayPackage;

import DirectoryPackage.DirectoryTree;
import FileEditPackage.FileEditToolBar;
import MainPackage.Main;
import MainPackage.Setting;
import NetworkPackage.User;
import com.ibm.icu.text.Collator;
import com.ibm.icu.util.ULocale;
import org.apache.commons.io.FilenameUtils;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static DirectoryPackage.DirectoryTree.bottomTipLabel;
import static DirectoryPackage.DirectoryTree.createBottomTipWindow;
import static FileDisplayPackage.FileDisplayBottomBar.*;
import static FileDisplayPackage.FileDisplayTopBar.*;
import static FileEditPackage.FileEditToolBar.handleFirstPicture;
import static FileEditPackage.FileEditToolBar.zoomTextField;
import static MainPackage.Setting.*;
import static MainPackage.ThemeColor.*;
import static NetworkPackage.User.handleUserSaveUserUploadPicture;
import static java.awt.Font.PLAIN;
import static java.awt.event.InputEvent.*;

public class FileDisplayMainPanel {//文件展示主面板类
    public static final int IMAGE_MAX_HEIGHT = 5000;//图片最大高度常量
    public static final int BORDER_THICKNESS = 2;//选中边框粗细常量
    public static final int MAX_HISTORY = 50;//堆栈最大历史长度常量

    public enum SortType {//排序方式枚举常量
        ANAME, ADATE, ATYPE, ASIZE, DNAME, DDATE, DTYPE, DSIZE//0：名称排序（升序），1：日期排序（升序），2：类型排序（升序），3：大小排序（升序），4：名称排序（降序），5：日期排序（降序），6：类型排序（降序），7：大小排序（降序）
    }

    public static JPanel mainPanel = null;//文件展示主面板：用于放置文件
    public static JLabel emptyLabel = new JLabel(Main.SettingState.systemLanguage ? "No File" : "暂无文件", SwingConstants.CENTER);//空状态提示标签：居中展示
    public static JWindow itemHoverTipWindow;//项目悬浮提示窗口
    public static JWindow draggedThumbnailItemWindow = null;//被拖拽缩略图项目窗口
    public static final JWindow progressWindow = new JWindow();//进度条窗口
    public static JProgressBar circularProgressBar;//后台工作圆形进度条

    public static File[] currentFileList = null;//图片文件数组
    public static final List<ThumbnailItem> thumbnailItemList = new ArrayList<>();//缩略图项目列表
    public static final List<ThumbnailItem> selectionThumbnailItemList = new ArrayList<>();//选中缩略图项目列表
    private static final Set<String> sortRecallSelectedFilePaths = new HashSet<>();//保存选中文件路径，以便在更改排序方式后进行恢复

    public static int thumbnailItemWidth = 200;//缩略图宽度
    private static int totalFiles;//总共应处理文件数目
    private static int processedFiles;//已处理文件数目
    public static boolean loading = false;//文件是否在加载
    private static boolean dragging = false;//是否发生拖动（用于在鼠标松开时判断是否发生了拖动）
    public static int selectionAnchorIndex = -1;//锚点索引（用于保存选中项目，方便shift+点击进行调用）
    private static double fileTotalKiloByte = 0.0;//该文件夹中文件总大小（设置基本单位为1KB）
    private static Point selectionStart;//鼠标框选起点
    private static final Rectangle selectionRect = new Rectangle();//鼠标框选矩形
    public static SortType currentSortType = SortType.ANAME;//当前排序方式，默认为名称排序
    private static final Collator CHINESE_COLLATOR = Collator.getInstance(ULocale.SIMPLIFIED_CHINESE);//使用Collator进行中文拼音排序比较
    public static ThumbnailItem draggedThumbnailItem = null;//被拖拽缩略图项目
    private static final Font thumbnailItemFont = new Font("楷体", PLAIN, 20);//缩略图项目字体常量
    private static BufferedImage directoryIcon;//目录图标
    private static BufferedImage textIcon;//文本图标

    public static SwingWorker<Void, ThumbnailItem> currentWorker;//后台工作任务引用
    public static final ExecutorService currentWorkerLoaderPool = Executors.newFixedThreadPool(Math.max(4, Runtime.getRuntime().availableProcessors() - 1));//创建有上限的全局线程池（限制最大并发数为系统可用线程-1（留下至少一个线程保证运行不卡顿），最少也需要四个线程）
    private static final Timer memoryMonitorTimer = new Timer(5000, _ -> {//内存监控计时器：每5s执行一次
        Runtime runtime = Runtime.getRuntime();//获取运行
        if (runtime.totalMemory() - runtime.freeMemory() > 2147483647) {//当内存超过2G时触发清理
            System.gc();//清除系统垃圾
        }

        if (!Main.SettingState.masterState && !Main.SettingState.bgmState) {//如果没有关闭音频或音乐：顺便检查音乐状态
            switch (currentBGM) {//根据当前音乐选择
                case 1:
                    if (!bgmClip1.isRunning()) {//如果停止
                        currentBGM = 2;//切换BGM
                        bgmClip2.setFramePosition(0);//重置播放位置
                        bgmClip2.start();//开始播放音乐
                    }
                    break;
                case 2:
                    if (!bgmClip2.isRunning()) {//如果停止
                        currentBGM = 3;//切换BGM
                        bgmClip3.setFramePosition(0);//重置播放位置
                        bgmClip3.start();//开始播放音乐
                    }
                    break;
                case 3:
                    if (!bgmClip3.isRunning()) {//如果停止
                        currentBGM = 4;//切换BGM
                        bgmClip4.setFramePosition(0);//重置播放位置
                        bgmClip4.start();//开始播放音乐
                    }
                    break;
                case 4:
                    if (!bgmClip4.isRunning()) {//如果停止
                        currentBGM = 5;//切换BGM
                        bgmClip5.setFramePosition(0);//重置播放位置
                        bgmClip5.start();//开始播放音乐
                    }
                    break;
                case 5:
                    if (!bgmClip5.isRunning()) {//如果停止
                        currentBGM = 1;//切换BGM
                        bgmClip1.setFramePosition(0);//重置播放位置
                        bgmClip1.start();//开始播放音乐
                    }
                    break;
            }
        }
    });
    private static final Timer scrollTimer = new Timer(16, new ActionListener() {//滚动计时器，每10ms执行一次
        @Override
        public void actionPerformed(ActionEvent evt) {//如果执行
            if (dragging) {//仅在拖动时滚动
                try {
                    JScrollPane scrollPane = (JScrollPane) mainPanel.getParent().getParent();//获取滚动条（注意mainPanel的父组件是视口，还要获得一次父组件才能获取滚动条）
                    JViewport viewport = scrollPane.getViewport();//获取视口
                    JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();//获取垂直滚动框
                    Point mouseScreenPoint = MouseInfo.getPointerInfo().getLocation();//获取鼠标位置
                    Point viewportScreenPoint = viewport.getLocationOnScreen();//获取视口在屏幕的位置
                    int relativeY = mouseScreenPoint.y - viewportScreenPoint.y;//获取鼠标相对视口移动了多少距离
                    int scrollSpeed = 0;//滚动速度
                    if (relativeY > 70 && relativeY <= 150) {//如果在阈值以内
                        scrollSpeed = -5;//向上滚动
                    } else if (relativeY > 20 && relativeY <= 70) {//如果在阈值以内
                        scrollSpeed = -15;//向上滚动
                    } else if (relativeY > 5 && relativeY <= 20) {//如果在阈值以内
                        scrollSpeed = -50;//向上滚动
                    } else if (relativeY <= 5) {//如果在阈值以内
                        scrollSpeed = -150;//向上滚动
                    } else if (relativeY < viewport.getHeight() - 70 && relativeY >= viewport.getHeight() - 150) {//如果在视口高度的倒数阈值以内
                        scrollSpeed = 5;//向下滚动
                    } else if (relativeY < viewport.getHeight() - 20 && relativeY >= viewport.getHeight() - 70) {//如果在视口高度的倒数阈值以内
                        scrollSpeed = 15;//向下滚动
                    } else if (relativeY < viewport.getHeight() - 5 && relativeY >= viewport.getHeight() - 20) {//如果在视口高度的倒数阈值以内
                        scrollSpeed = 50;//向下滚动
                    } else if (relativeY >= viewport.getHeight() - 5) {//如果在视口高度的倒数阈值以内
                        scrollSpeed = 150;//向下滚动
                    }

                    if (scrollSpeed != 0) {//如果滚动速度不为0
                        verticalScrollBar.setValue(Math.max(0, Math.min(verticalScrollBar.getValue() + scrollSpeed, verticalScrollBar.getMaximum() - verticalScrollBar.getVisibleAmount())));//先让滚动后位置和可滚动距离取最小值防止滚出滚动条，再与0取最小值防止滚动值是负值
                    } else {//否则
                        scrollTimer.stop();//停下计时器
                    }
                } catch (Exception e) {
                    handleErrorLog(e.getMessage());//处理错误日志
                    e.printStackTrace();//捕获异常
                }
            }
        }
    });

    static {//静态代码块：类创建时自动调用
        ImageIO.setCacheDirectory(null);//禁用磁盘缓存
        ImageIO.setUseCache(true);//使用ImageIO缓存加速读取
        ImageIO.setCacheDirectory(new File(System.getProperty("java.io.tmpdir")));//设置缓存路径

        CHINESE_COLLATOR.setDecomposition(Collator.NO_DECOMPOSITION);//预加载排序数据

        try (ImageInputStream directoryIconInput = ImageIO.createImageInputStream(new File("src/material/image/directoryIcon.png"))) {//生成目录图标
            ImageReader reader = ImageIO.getImageReaders(directoryIconInput).next();//读取图像
            reader.setInput(directoryIconInput);//设置读取出来的图像
            directoryIcon = reader.read(0, reader.getDefaultReadParam());//获取原始图像
        } catch (IOException e) {
            handleErrorLog(e.getMessage());//处理错误日志
            System.err.println("IOException: src/material/image/directoryIcon.png");//捕获异常
        }
        try (ImageInputStream textIconInput = ImageIO.createImageInputStream(new File("src/material/image/textIcon.png"))) {//生成文本图标
            ImageReader reader = ImageIO.getImageReaders(textIconInput).next();//读取图像
            reader.setInput(textIconInput);//设置读取出来的图像
            textIcon = reader.read(0, reader.getDefaultReadParam());//获取原始图像
        } catch (IOException e) {
            handleErrorLog(e.getMessage());//处理错误日志
            System.err.println("IOException: src/material/image/textIcon.png");//捕获异常
        }

        memoryMonitorTimer.start();//开启内存监控计时器
    }

    public FileDisplayMainPanel() {//构造方法：初始化UI布局
        mainPanel = new JPanel(new WrapLayout(FlowLayout.LEFT, 15, 15)) {//布局管理器为自动换行的流式布局（向左对齐），水平和垂直间隔为15
            @Override
            protected void paintChildren(Graphics g) {//重写方法
                super.paintChildren(g);//先绘制子组件（缩略图）
                if (!selectionRect.isEmpty()) {//如果矩形框非空
                    Graphics2D g2d = (Graphics2D) g.create();
                    try {
                        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2d.setColor(MOUSE_SELECTION_COLOR);//设置选中颜色
                        g2d.fill(selectionRect);//绘制背景
                        g2d.setColor(Color.BLUE);//设置矩形框颜色
                        g2d.draw(selectionRect);//绘制矩形框
                    } finally {//最终
                        g2d.dispose();//释放资源
                    }
                }
            }
        };
        mainPanel.setFocusable(true);//确保面板可获取焦点
        mainPanel.requestFocusInWindow();//初始化后直接获取焦点
        mainPanel.setDoubleBuffered(true);//使用双缓冲加速
        mainPanel.setBackground(Main.SettingState.themeColor ? DARK_PICTURE_MAIN_COLOR : LIGHT_PICTURE_MAIN_COLOR);//设置背景颜色
        mainPanel.addMouseListener(new SelectionMouseAdapter());//主面板添加鼠标监听
        mainPanel.addMouseMotionListener(new SelectionMouseAdapter());//主面板添加鼠标动作监听
        mainPanel.addMouseWheelListener(new SelectionMouseAdapter());//主面板添加鼠标滚轮监听

        emptyLabel.setFont(new Font("微软雅黑", PLAIN, 25));//设置字体
        emptyLabel.setForeground(LIGHT_PICTURE_EMPTY_COLOR);//设置前景色
        showEmptyState();//初始显示空状态

        circularProgressBar = new JProgressBar() {//初始化圆形进度条
            @Override
            protected void paintComponent(Graphics g) {//绘制
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);//抗锯齿
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
                g2.setComposite(AlphaComposite.Clear);//透明背景
                g2.fillRect(0, 0, getWidth(), getHeight());//绘制边框
                g2.setComposite(AlphaComposite.SrcOver);//恢复混合模式

                int stroke = 6;//设置进度圆环画笔粗细
                int size = Math.min(getWidth(), getHeight()) - stroke;//设置大小为宽度和高度取最小值减去画笔粗细
                int x = (getWidth() - size) / 2;//x坐标
                int y = (getHeight() - size) / 2;//y坐标
                g2.setColor(new Color(220, 220, 220, 80));//设置背景圆颜色
                g2.fillOval(x, y, size, size);//绘制背景

                int arcAngle = (int) (360 * getPercentComplete());//进度圆弧
                g2.setColor(new Color(90, 165, 255));//设置进度圆弧颜色
                g2.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));//设置画笔粗细
                g2.drawArc(x + stroke / 2, y + stroke / 2, size - stroke, size - stroke, 90, -arcAngle);//绘制圆弧

                String text = String.format("%d%%", (int) (getPercentComplete() * 100));//进度文字
                FontMetrics fm = g2.getFontMetrics();//文字字体
                int textWidth = fm.stringWidth(text);//字体宽度
                int textHeight = fm.getHeight();//字体高度
                g2.setColor(Color.BLACK);//字体颜色
                g2.drawString(text, (getWidth() - textWidth) / 2, (getHeight() + textHeight / 2) / 2);//绘制文字
                g2.dispose();//释放
            }
        };
        circularProgressBar.setPreferredSize(new Dimension(80, 80));//设置大小
        circularProgressBar.setBackground(new Color(0, 0, 0, 0));//透明背景

        progressWindow.setForeground(new Color(0, 0, 0, 0));//透明前景
        progressWindow.setBackground(new Color(0, 0, 0, 0));//透明背景
        progressWindow.setLayout(new BorderLayout());//设置窗口布局
        progressWindow.add(circularProgressBar);//进度条添加到窗口
        progressWindow.setAlwaysOnTop(Main.SettingState.windowState);//设置窗口永远在最上层
        progressWindow.setShape(new Ellipse2D.Double(0, 0, 80, 80));//设置圆形形状
        progressWindow.setLocation(Main.screenSize.width - Main.screenSize.width / 10, Main.screenSize.height - Main.screenSize.width / 10);//设置窗口位置
        progressWindow.setFocusableWindowState(false);//禁止获取焦点
        progressWindow.setVisible(false);//初始时进度条不可见
        progressWindow.pack();//合适

        initMainPanelShortcuts();//添加键盘绑定初始化
    }

    public static List<ThumbnailItem> getSelectionThumbnailItemList() {//获取选中图片列表（供PicturePreviewTopBar调用）
        return selectionThumbnailItemList;
    }

    public static List<File> getSelectionThumbnailItemFileList() {//获取选中图片文件列表（供PicturePreviewBottomBar和PicturePreviewPopupMenu调用）
        List<File> list = new ArrayList<>();//临时文件列表
        for (ThumbnailItem thumbnailItem : selectionThumbnailItemList) {//遍历选择项目
            list.add(thumbnailItem.getFile());//添加文件
        }
        return list;//返回文件列表
    }

    private static void showEmptyState() {//显示空状态提示
        mainPanel.add(emptyLabel, BorderLayout.CENTER);//添加空状态标签到主面板的中心区域
        refreshMainPanel();//刷新
    }

    private static void updateProgress(int progress) {//更新进度方法
        SwingUtilities.invokeLater(() -> {//推迟调用
            circularProgressBar.setValue(progress);//设置进度条的值
            if (progress == 0 || progress >= 100) {//如果没有开始任务或任务已完成
                progressWindow.setVisible(false);//设置进度条不可见
            } else {//否则
                if (!progressWindow.isVisible()) {//如果没有更新进度条可见
                    progressWindow.setVisible(true);//设置进度条可见
                }
            }
            circularProgressBar.repaint();//重新绘制进度条
        });
    }

    public static void initMainPanelShortcuts() {//启用主面板快捷键并设置键盘按键绑定
        InputMap inputMap = mainPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);//获取主面板输入映射，如果聚焦主面板则监听主面板的键盘输入
        ActionMap actionMap = mainPanel.getActionMap();//获取主面板行动映射

        bindKey(inputMap, actionMap, KeyEvent.VK_LEFT, ALT_DOWN_MASK, "retreat");//后退
        bindKey(inputMap, actionMap, KeyEvent.VK_RIGHT, ALT_DOWN_MASK, "advance");//前进
        bindKey(inputMap, actionMap, KeyEvent.VK_UP, ALT_DOWN_MASK, "upperLayer");//上一级
        bindKey(inputMap, actionMap, KeyEvent.VK_F5, 0, "refresh");//刷新
        bindKey(inputMap, actionMap, KeyEvent.VK_R, CTRL_DOWN_MASK, "refresh");//刷新
        bindKey(inputMap, actionMap, KeyEvent.VK_F4, 0, "directory");//路径
        bindKey(inputMap, actionMap, KeyEvent.VK_L, CTRL_DOWN_MASK, "directory");//路径
        bindKey(inputMap, actionMap, KeyEvent.VK_A, CTRL_DOWN_MASK, "selectAll");//全选
        bindKey(inputMap, actionMap, KeyEvent.VK_X, CTRL_DOWN_MASK, "cut");//剪切
        bindKey(inputMap, actionMap, KeyEvent.VK_C, CTRL_DOWN_MASK, "copy");//复制
        bindKey(inputMap, actionMap, KeyEvent.VK_V, CTRL_DOWN_MASK, "paste");//粘贴
        bindKey(inputMap, actionMap, KeyEvent.VK_F2, 0, "rename");//重命名
        bindKey(inputMap, actionMap, KeyEvent.VK_DELETE, 0, "delete");//删除
        bindKey(inputMap, actionMap, KeyEvent.VK_F3, 0, "search");//搜索
        bindKey(inputMap, actionMap, KeyEvent.VK_F, CTRL_DOWN_MASK, "search");//搜索
        bindKey(inputMap, actionMap, KeyEvent.VK_Z, CTRL_DOWN_MASK, "undo");//撤销
        bindKey(inputMap, actionMap, KeyEvent.VK_Y, CTRL_DOWN_MASK, "redo");//恢复
        bindKey(inputMap, actionMap, KeyEvent.VK_D, CTRL_DOWN_MASK, "edit");//编辑
        bindKey(inputMap, actionMap, KeyEvent.VK_U, CTRL_DOWN_MASK, "user");//用户
        bindKey(inputMap, actionMap, KeyEvent.VK_P, CTRL_DOWN_MASK, "uploadToCloud");//上传到云盘
        bindKey(inputMap, actionMap, KeyEvent.VK_S, CTRL_DOWN_MASK, "setting");//设置
        bindKey(inputMap, actionMap, KeyEvent.VK_C, CTRL_DOWN_MASK + SHIFT_DOWN_MASK, "getPath");//获取路径
        bindKey(inputMap, actionMap, KeyEvent.VK_O, CTRL_DOWN_MASK, "openRecycleBin");//打开回收站
        bindKey(inputMap, actionMap, KeyEvent.VK_E, CTRL_DOWN_MASK, "emptyRecycleBin");//清空回收站
        bindKey(inputMap, actionMap, KeyEvent.VK_M, CTRL_DOWN_MASK + SHIFT_DOWN_MASK, "muteMaster");//静音总音量
        bindKey(inputMap, actionMap, KeyEvent.VK_P, CTRL_DOWN_MASK + SHIFT_DOWN_MASK, "muteMusic");//静音音乐
        bindKey(inputMap, actionMap, KeyEvent.VK_E, CTRL_DOWN_MASK + SHIFT_DOWN_MASK, "muteSoundEffect");//静音音乐
    }

    public static void bindKey(InputMap inputMap, ActionMap actionMap, int keyCode, int modifiers, String actionKey) {//绑定按键
        KeyStroke keyStroke = KeyStroke.getKeyStroke(keyCode, modifiers);//获取当前按键输入
        inputMap.put(keyStroke, actionKey);//放入输入映射
        actionMap.put(actionKey, new AbstractAction() {//添加行为绑定
            @Override
            public void actionPerformed(ActionEvent e) {//如果行为执行
                handleKeyAction(actionKey);//进行按键处理
            }
        });
    }

    private static void handleKeyAction(String actionKey) {//处理按键
        switch (actionKey) {//根据输入按键
            case "retreat":
                handleRetreat();//调用后退
                break;
            case "advance":
                handleAdvance();//调用前进
                break;
            case "upperLayer":
                handleUpperLayer();//调用上一级
                break;
            case "refresh":
                handleRefresh();//调用刷新
                break;
            case "directory":
                directoryField.getInputTextField().requestFocusInWindow();//请求聚焦
                break;
            case "selectAll":
                handleSelectAll();//调用全选
                break;
            case "cut":
                handleCut();//调用剪切
                break;
            case "copy":
                handleCopy();//调用复制
                break;
            case "paste":
                handlePaste();//调用粘贴
                break;
            case "rename":
                handleRename();//调用重命名
                break;
            case "delete":
                handleRemove();//调用删除
                break;
            case "search":
                searchField.getInputTextField().requestFocusInWindow();//请求聚焦
                break;
            case "undo":
                if (undoButton.isEnabled()) {//如果有效
                    handleUndo();//调用撤销
                }
                break;
            case "redo":
                if (redoButton.isEnabled()) {//如果有效
                    handleRedo();//调用恢复
                }
                break;
            case "edit":
                if (editButton.isEnabled()) {//如果编辑按钮有效
                    handleEdit();//调用编辑
                }
                break;
            case "user":
                User.handleUser();//调用用户
                break;
            case "uploadToCloud":
                handleUserSaveUserUploadPicture(getSelectionThumbnailItemFileList(), true);//调用上传至云盘
                handleRefresh();//刷新
                break;
            case "setting":
                Setting.handleSetting();//调用设置
                break;
            case "getPath":
                FileDisplayPopupMenu.handleGetPath();//调用获取路径
                break;
            case "openRecycleBin":
                FileDisplayPopupMenu.handleOpenRecycleBin();//调用打开回收站
                break;
            case "emptyRecycleBin":
                FileDisplayPopupMenu.handleEmptyRecycleBin();//调用清空回收站
                break;

            case "scrollPaneStrategy":
                FileEditToolBar.handleScrollPaneStrategy();//处理滚动栏策略
                break;
            case "firstPicture":
                handleFirstPicture();//处理第一张图片
                break;
            case "previousPicture":
                FileEditToolBar.handlePreviousPicture();//处理上一张图片
                break;
            case "nextPicture":
                FileEditToolBar.handleNextPicture();//处理下一张图片
                break;
            case "lastPicture":
                FileEditToolBar.handleLastPicture();//处理最后一张图片
                break;
            case "leftRotation":
                FileEditToolBar.handleLeftRotation();//处理左旋转
                break;
            case "rightRotation":
                FileEditToolBar.handleRightRotation();//处理右旋转
                break;
            case "autoPlay":
                FileEditToolBar.handleAutoPlay();//处理自动播放
                break;
            case "zoomToActual":
                if (!FileEditToolBar.isZoomToActual) {//如果没有缩放到实际
                    FileEditToolBar.handleZoomStrategy();//处理缩放策略
                }
                break;
            case "zoomToAdapt":
                if (FileEditToolBar.isZoomToActual) {//如果缩放到实际
                    FileEditToolBar.handleZoomStrategy();//处理缩放策略
                }
                break;
            case "shrinkPicture":
                FileEditToolBar.handleShrinkPicture();//处理缩小图片
                break;
            case "magnifyPicture":
                FileEditToolBar.handleMagnifyPicture();//处理放大图片
                break;
            case "inputZoomRatio":
                zoomTextField.requestFocusInWindow();//缩放文本域获取焦点
                break;

            case "muteMaster":
                if (Main.SettingState.masterState) {//如果关闭
                    Main.SettingState.masterState = false;//更新
                    closeAudioRadioButton.setSelected(false);//设置选中
                } else {//否则
                    Main.SettingState.masterState = true;//更新
                    closeAudioRadioButton.setSelected(true);//设置选中
                }
                switchBGM();//切换BGM
                break;
            case "muteMusic":
                if (Main.SettingState.bgmState) {//如果关闭
                    Main.SettingState.bgmState = false;//更新
                    closeMusicRadioButton.setSelected(false);//设置选中
                } else {//否则
                    Main.SettingState.bgmState = true;//更新
                    closeMusicRadioButton.setSelected(true);//设置选中
                }
                switchBGM();//切换BGM
                break;
            case "muteSoundEffect":
                if (Main.SettingState.effectState) {//如果关闭
                    Main.SettingState.effectState = false;//更新
                    closeEffectRadioButton.setSelected(false);//设置选中
                } else {//否则
                    Main.SettingState.effectState = true;//更新
                    closeEffectRadioButton.setSelected(true);//设置选中
                }
                break;
        }
    }

    private static void handleSelectAll() {//处理全选
        selectionThumbnailItemList.clear();//清空选中列表
        selectionThumbnailItemList.addAll(thumbnailItemList);//全部选中
        selectionAnchorIndex = thumbnailItemList.isEmpty() ? -1 : 0;//重设锚点，有选中则设置0，否则为-1
        updateBottomTipInformation();//更新选中
        FileDisplayTopBar.fileManipulationButtonEnableJudgement(selectionThumbnailItemList.size());//更新
        refreshMainPanel();//刷新
    }

    public static class SelectionMouseAdapter extends MouseAdapter {//内部类处理鼠标事件

        @Override
        public void mouseClicked(MouseEvent e) {//如果鼠标点击
            mainPanel.requestFocusInWindow();//确保主面板被点击时刷新键盘绑定
            if (SwingUtilities.isRightMouseButton(e)) {//如果是鼠标右键
                FileDisplayPopupMenu.rightMousePopupMenu.show(Main.diskManagementSystemFrame, e.getLocationOnScreen().x, e.getLocationOnScreen().y);//展示右键菜单
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {//如果鼠标按下
            if (SwingUtilities.isLeftMouseButton(e)) {//如果按下左键
                dragging = true;//标记拖动状态
                selectionStart = e.getPoint();//记录起点（即鼠标相对mainPanel左上角的位置）
                selectionRect.setBounds(0, 0, 0, 0);//绘制框架
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {//如果鼠标拖动
            if (dragging) {//确保只在拖动状态处理
                if (selectionStart != null) {//如果有起点
                    int x = Math.min(selectionStart.x, e.getX());//获取左上角x坐标
                    int y = Math.min(selectionStart.y, e.getY());//获取左上角y坐标
                    int width = Math.abs(e.getX() - selectionStart.x);//获取宽度
                    int height = Math.abs(e.getY() - selectionStart.y);//获取高度
                    selectionRect.setBounds(x, y, width, height);//绘制框架：从左上角开始绘制宽为width高为height的矩形
                    mainPanel.repaint();//触发重绘

                    try {
                        JScrollPane scrollPane = (JScrollPane) mainPanel.getParent().getParent();//获取滚动条
                        JViewport viewport = scrollPane.getViewport();//获取视口
                        Point mouseScreenPoint = e.getLocationOnScreen();//获取鼠标位置
                        Point viewportScreenPoint = viewport.getLocationOnScreen();//获取视口在屏幕的位置
                        int relativeY = mouseScreenPoint.y - viewportScreenPoint.y;//获取鼠标相对视口移动了多少距离
                        if (relativeY <= 150 || relativeY >= viewport.getHeight() - 150) {//如果在阈值以内或在视口高度的倒数阈值以内
                            if (!scrollTimer.isRunning()) {//如果计时器没有运行
                                scrollTimer.start();//就开始运行
                            }
                        } else {//否则
                            if (scrollTimer.isRunning()) {//如果计时器正在运行
                                scrollTimer.stop();//就停止运行
                            }
                        }
                    } catch (Exception ex) {
                        handleErrorLog(ex.getMessage());//处理错误日志
                        ex.printStackTrace();//捕获异常
                    }
                }
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {//如果鼠标松开
            dragging = false;//清除拖动状态
            if (scrollTimer.isRunning()) {//如果计时器仍在运行
                scrollTimer.stop();//就停下
            }
            if (selectionStart != null) {//如果有起点
                handleSelection(e);//就选中图片
                selectionStart = null;//起点清空
                selectionRect.setBounds(0, 0, 0, 0);//矩形清空
                mainPanel.repaint();//触发重绘
            }
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {//如果鼠标滚动
            if (e.isControlDown()) {//如果ctrl键被按下
                if (loading) {//如果图像仍在加载
                    createBottomTipWindow(Main.SettingState.systemLanguage ? "File Loading, Please Waiting To Scaling" : "文件正在加载，请等待加载完成进行缩放");//创建提示窗口
                    return;//返回
                }
                handleZoom(e);//进行缩放处理
            } else {//否则进行滚动
                mainPanel.getParent().dispatchEvent(e);//传递滚动事件给父容器
            }
        }

        public static void handleZoom(MouseWheelEvent e) {//处理缩放
            e.consume();//消耗事件阻止事件继续传播
            int steps = e.getWheelRotation();//动态计算缩放步长
            int baseStep = Math.max(50, thumbnailItemWidth / 10);//基础步长
            int delta = (int) (steps * baseStep * (e.getScrollAmount() / 2.0));//缩放倍率
            int newWidth = Math.max(100, Math.min(750, thumbnailItemWidth - delta));//限制缩放范围
            if (newWidth != thumbnailItemWidth) {//如果缩放发生更新
                thumbnailItemWidth = newWidth;//设置图片宽度为新宽度
                applyZoom();//应用缩放
            }
        }

        public static void applyZoom() {//应用缩放
            if (loading) {//如果正在加载
                if (!bottomTipLabel.getText().equals(Main.SettingState.systemLanguage ? "Zoom Too Quick" : "请勿过快缩放")) {//如果没有提示过
                    createBottomTipWindow(Main.SettingState.systemLanguage ? "Zoom Too Quick" : "请勿过快缩放");//创建提示窗口
                }
                return;//返回
            }
            if (itemHoverTipWindow != null) {//如果提示信息不为空
                itemHoverTipWindow.dispose();//释放提示信息
                itemHoverTipWindow = null;//提示信息置空
            }
            loading = true;//开始加载
            Main.diskManagementSystemFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));//设置光标为等待
            zoomSlider.setValue(thumbnailItemWidth);//设置拖动条的值
            JScrollPane scrollPane = (JScrollPane) mainPanel.getParent().getParent();//获取滚动条
            JViewport viewport = scrollPane.getViewport();//获取视口
            Point originalPos = viewport.getViewPosition();//获取视口位置
            if (currentWorker != null && !currentWorker.isDone()) {//如果当前有工作
                currentWorker.cancel(true);//进行取消
            }
            currentWorker = new SwingWorker<>() {//使用swingWorker在线程中加载
                @Override
                protected Void doInBackground() {//后台运行
                    ExecutorService executor = Executors.newFixedThreadPool(4);//创建有上限的线程池，限制线程并发数
                    List<Future<ThumbnailItem>> futures = new ArrayList<>();//未来工作列表
                    updateProgress(0);//初始化进度
                    totalFiles = currentFileList.length;//获取全部文件数量
                    processedFiles = 0;//初始化已处理文件数量
                    for (ThumbnailItem item : thumbnailItemList) {//并行处理每个缩略图项
                        if (isCancelled()) {//如果工作取消
                            break;//就退出
                        }
                        futures.add(executor.submit(() -> {//增加工作
                            processedFiles++;//已处理文件增加
                            item.updateThumbnailItemSize(thumbnailItemWidth, item.file);//更新缩略图尺寸
                            return item;//返回项目
                        }));//增加工作
                    }
                    for (Future<ThumbnailItem> future : futures) {//等待所有任务完成
                        if (isCancelled()) {//如果工作取消
                            break;//就退出
                        }
                        try {
                            publish(future.get());//发布
                            updateProgress(100 * processedFiles / totalFiles);//更新进度条
                        } catch (Exception e) {
                            if (!isCancelled()) {//如果取消任务
                                handleErrorLog(e.getMessage());//处理错误日志
                                e.printStackTrace();//捕获异常
                            }
                        }
                    }
                    executor.shutdown();//关闭线程池
                    return null;//返回空
                }

                @Override
                protected void done() {//完成时
                    refreshMainPanel();//刷新
                    viewport.setViewPosition(originalPos);//恢复滚动条位置
                    Main.diskManagementSystemFrame.setCursor(Cursor.getDefaultCursor());//设置光标为默认
                    loading = false;//加载结束
                }
            };
            currentWorker.execute();//开始执行
        }
    }

    private static void handleSelection(MouseEvent e) {//处理选中图片
        if ((CTRL_DOWN_MASK & e.getModifiersEx()) != 0) {//如果ctrl键被按下：矩形框内选中的取消选中，矩形框外不会被取消选中
            for (ThumbnailItem item : thumbnailItemList) {//遍历列表
                Rectangle bounds = item.getBounds();
                bounds.setLocation(item.getLocation());
                if (FileDisplayMainPanel.selectionRect.intersects(bounds)) {
                    if (selectionThumbnailItemList.contains(item)) {//如果已经被选中
                        selectionThumbnailItemList.remove(item);//取消选中
                    } else {//否则
                        selectionThumbnailItemList.add(item);//设置被选中
                    }
                }
            }
        } else {//如果ctrl键没被按下：矩形框外全部取消选中，只有矩形框内才会被选中
            selectionThumbnailItemList.clear();//清除所有选中状态
            for (ThumbnailItem item : thumbnailItemList) {//遍历列表
                Rectangle bounds = item.getBounds();//获取组件矩形
                bounds.setLocation(item.getLocation());//设置矩形位置
                if (FileDisplayMainPanel.selectionRect.intersects(bounds)) {//如果选择矩形包含组件矩形
                    selectionThumbnailItemList.add(item);//设置被选中
                }
            }
        }
        updateBottomTipInformation();//更新提示信息
        if (selectionThumbnailItemList.isEmpty()) {//如果选中文件列表为空
            selectionAnchorIndex = -1;//重置锚点
        } else if (selectionThumbnailItemList.size() == 1) {//如果选中一个项目
            selectionAnchorIndex = thumbnailItemList.indexOf(selectionThumbnailItemList.getFirst());//设置锚点
        }
        FileDisplayTopBar.fileManipulationButtonEnableJudgement(selectionThumbnailItemList.size());//进行按钮判断
    }

    public static void changeSortMethod(SortType type) {//更改排序方法
        sortRecallSelectedFilePaths.clear();//保存当前选中项路径以便进行恢复
        for (ThumbnailItem item : selectionThumbnailItemList) {//遍历选中项目列表
            sortRecallSelectedFilePaths.add(item.getFile().getAbsolutePath());//添加到排序选择文件路径回忆列表
        }
        currentSortType = type;//设置当前排序方式
        updateFileDisplayMainPanel(false);//进行更新
    }

    public static File[] fileListSortProcess(File[] files) {//文件列表排序处理
        Arrays.sort(files, (f1, f2) -> {//进行排序
            try {
                return switch (currentSortType) {//根据当前排序方式
                    case ADATE -> Long.compare(f1.lastModified(), f2.lastModified());//对比日期
                    case ATYPE ->
                            FilenameUtils.getExtension(f1.getName()).compareToIgnoreCase(FilenameUtils.getExtension(f2.getName()));//对比文件扩展名
                    case ASIZE -> Long.compare(f1.length(), f2.length());//对比大小
                    case DNAME -> -naturalCompareAlgorithm(f1.getName(), f2.getName());//对比名称再取反
                    case DDATE -> -Long.compare(f1.lastModified(), f2.lastModified());//对比日期再取反
                    case DTYPE ->
                            -FilenameUtils.getExtension(f1.getName()).compareToIgnoreCase(FilenameUtils.getExtension(f2.getName()));//对比文件扩展名再取反
                    case DSIZE -> -Long.compare(f1.length(), f2.length());//对比大小再取反
                    default -> naturalCompareAlgorithm(f1.getName(), f2.getName());//默认按名称排序
                };
            } catch (Exception e) {
                handleErrorLog(e.getMessage());//处理错误日志
                return 0;//捕获异常
            }
        });
        return files;//返回文件列表
    }

    private static int naturalCompareAlgorithm(String s1, String s2) {//自然排序算法
        char[] charArray1 = s1.toCharArray();//切割字符串获取字符数组
        char[] charArray2 = s2.toCharArray();
        int minLength = Math.min(charArray1.length, charArray2.length);//获取长度
        for (int i = 0; i < minLength; i++) {//根据最短长度遍历
            int cmp = compareCharacter(charArray1[i], charArray2[i], i, s1, s2);//根据每个字符进行比较
            if (cmp != 0) {//如果有比较结果
                return cmp;//返回结果
            }
        }
        return Integer.compare(charArray1.length, charArray2.length);//否则根据长度比较
    }

    private static int compareCharacter(char c1, char c2, int index, String s1, String s2) {//根据每个字符比较
        int type1 = getCharacterType(c1);//类型判断
        int type2 = getCharacterType(c2);
        if (type1 != type2) {//如果类型不同
            return Integer.compare(type1, type2);//根据类型优先级比较
        }
        return switch (type1) {//同类型详细比较
            case 0 -> compareSpecialCharacter(c1, c2);//特殊字符
            case 1 -> compareNumericCharacter(c1, c2, index, s1, s2);//数字（数字需要额外处理连续数字问题）
            case 2 -> compareAlphabeticCharacter(c1, c2);//英文
            case 3 -> CHINESE_COLLATOR.compare(String.valueOf(c1), String.valueOf(c2));//中文
            default -> 0;//如果都不是则返回0
        };
    }

    private static int getCharacterType(char c) {//获取这个字符的类型
        if (Character.isDigit(c)) {//判断是否为数字
            return 1;//是则返回1
        }
        if (Character.isLetter(c) && !(Character.UnicodeScript.of(c) == Character.UnicodeScript.HAN)) {//判断是否为字母
            return 2;//是则返回2
        }
        if (Character.UnicodeScript.of(c) == Character.UnicodeScript.HAN) {//判断是否为中文
            return 3;//是则返回3
        }
        return 0;//否则为特殊字符类型
    }

    private static int compareSpecialCharacter(char c1, char c2) {//比较特殊字符
        return Character.compare(c1, c2);//按照ASCII值大小排序
    }

    private static int compareNumericCharacter(char c1, char c2, int index, String s1, String s2) {//比较数字
        StringBuilder sb1 = new StringBuilder(String.valueOf(c1)), sb2 = new StringBuilder(String.valueOf(c2));//创建字符串拼接者
        for (int i = index + 1; Character.isDigit(s1.charAt(i)); i++) {//循环判断该数字后的字符是否还是数字
            sb1.append(s1.charAt(i));//如果是则进行拼接
        }
        for (int i = index + 1; Character.isDigit(s2.charAt(i)); i++) {//循环判断该数字后的字符是否还是数字
            sb2.append(s2.charAt(i));//如果是则进行拼接
        }
        BigInteger n1 = new BigInteger(String.valueOf(sb1));//将拼接者转化成字符串（会自动删除前导零，只按数字大小排序，和windows排序对齐）
        BigInteger n2 = new BigInteger(String.valueOf(sb2));//将拼接者转化成字符串
        return n1.compareTo(n2);//按照数字大小排序
    }

    private static int compareAlphabeticCharacter(char c1, char c2) {//比较字母
        int cmp = Character.compare(Character.toLowerCase(c1), Character.toLowerCase(c2));//不区分大小写比较
        return cmp != 0 ? cmp : Character.compare(c1, c2);//若忽略大小写后相等，则大写字母优先（和windows排序对齐）
    }

    public static void updateFileDisplayMainPanel(boolean isPaste) {//更新文件展示主面板
        if (currentWorker != null && !currentWorker.isDone()) {//如果当前有任务
            currentWorker.cancel(true);//则设置取消正在进行的加载任务：不取消，后面未加载的图片会进入新加载的文件夹中
        }
        loading = true;//图像正在加载
        editButton.setEnabled(false);//编辑按钮无效
        currentFileList = DirectoryTree.getCurrentFileList();//获取当前文件列表
        mainPanel.removeAll();//清空主面板
        thumbnailItemList.clear();//清空缩略图项目列表
        selectionThumbnailItemList.clear();//清空选中缩略图项目列表
        fileTotalKiloByte = 0;//清空图片总大小
        updateProgress(0);//初始化进度

        if (currentFileList != null && currentFileList.length > 0) {//如果当前文件列表不为空
            Main.diskManagementSystemFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));//设置光标为等待
            currentFileList = fileListSortProcess(currentFileList);//排序处理
            currentWorker = new SwingWorker<>() {//使用SwingWorker实现分块加载
                private final ExecutorService threadPool = currentWorkerLoaderPool;//设置线程数控制并发数

                @Override
                protected Void doInBackground() {//在后台运行加载
                    processedFiles = 0;//初始化已处理文件数量
                    totalFiles = currentFileList.length;//获取全部文件数量
                    List<Future<ThumbnailItem>> futures = new ArrayList<>();//未来任务列表
                    for (File file : currentFileList) {//遍历图像文件列表
                        if (isCancelled()) {//检测到取消任务
                            break;//直接退出
                        }
                        futures.add(threadPool.submit(() -> createThumbnailItem(file)));//增加任务
                        fileTotalKiloByte += (double) file.length() / 1024;//文件总大小加上该图片大小（因返回的是字节，转化成KB需要除1024，且需要强制转换成double类型，否则计算不精确）
                    }
                    for (Future<ThumbnailItem> future : futures) {//超绝加速算法
                        if (isCancelled()) {//检测到取消任务
                            break;//直接退出
                        }
                        try {
                            ThumbnailItem item = future.get();//创建缩略图项目
                            publish(item);//发布项目
                        } catch (Exception e) {
                            if (isCancelled()) {//检测到取消任务
                                break;//直接退出
                            }
                            e.printStackTrace();//捕获异常
                        }
                    }
                    return null;//返回空
                }

                @Override
                protected void process(List<ThumbnailItem> chunks) {//每当项目发布就会调用该处理方法
                    for (ThumbnailItem item : chunks) {//对块进行遍历
                        if (isCancelled()) {//这里也要添加检测，防止虽然任务以取消但是仍有文件在块中还是加载出来
                            break;//直接退出
                        }
                        thumbnailItemList.add(item);//向列表添加项目
                        processedFiles++;//已处理文件增加
                        mainPanel.add(item);//向主面板中添加项目
                    }
                    mainPanel.revalidate();//重新验证布局
                    mainPanel.repaint();//重新绘制
                    updateProgress(100 * processedFiles / totalFiles);//更新进度条
                }

                @Override
                protected void done() {//完成时
                    initBottomTipInformation();//初始化提示信息
                    if (!sortRecallSelectedFilePaths.isEmpty()) {//恢复选中状态，如果有记录
                        selectionThumbnailItemList.clear();//清空选中列表
                        for (ThumbnailItem item : thumbnailItemList) {//遍历项目列表
                            if (sortRecallSelectedFilePaths.contains(item.getFile().getAbsolutePath())) {//判断是否包含
                                selectionThumbnailItemList.add(item);//进行选中恢复
                            }
                        }
                        sortRecallSelectedFilePaths.clear();//再把记录清空
                        refreshMainPanel();//刷新
                    }
                    if (isPaste) {//如果是粘贴操作
                        for (int i = 0; i < FileDisplayTopBar.tmpPastePathList.size(); i++) {//遍历路径列表
                            for (ThumbnailItem thumbnailItem : thumbnailItemList) {//遍历图片文件列表
                                if (Objects.equals(thumbnailItem.getFile(), new File(String.valueOf(FileDisplayTopBar.tmpPastePathList.get(i))))) {//如果发现该文件
                                    selectionThumbnailItemList.add(thumbnailItem);//添加图片文件
                                }
                            }
                        }
                        fileManipulationButtonEnableJudgement(selectionThumbnailItemList.size());//文件操作按钮状态更新
                        updateBottomTipInformation();//更新提示信息
                    }
                    Main.diskManagementSystemFrame.setCursor(Cursor.getDefaultCursor());//设置光标为默认
                    loading = false;//图像结束加载
                }
            };
            currentWorker.execute();//任务执行
        } else {//否则
            showEmptyState();//显示空状态
            initBottomTipInformation();//初始化提示信息
        }
    }

    public static void initBottomTipInformation() {//初始化底部提示信息
        if (currentFileList != null && currentFileList.length > 0) {//如果不为空
            String information = currentFileList.length + (Main.SettingState.systemLanguage ? " Items (" : "个项目（共");//显示共多少项目
            if (fileTotalKiloByte / 1048576 > 1.0) {//如果文件总大小在1GB以上
                information += String.format("%.2f", fileTotalKiloByte / 1048576) + (Main.SettingState.systemLanguage ? "GB Totally) " : "GB）");//显示多少GB
            } else if (fileTotalKiloByte / 1024 > 1.0) {//如果文件总大小在1MB以上
                information += String.format("%.2f", fileTotalKiloByte / 1024) + (Main.SettingState.systemLanguage ? "MB Totally) " : "MB）");//显示多少MB
            } else {//否则
                information += String.format("%.2f", fileTotalKiloByte) + (Main.SettingState.systemLanguage ? "KB Totally) " : "KB）");//显示多少KB
            }
            Main.setBottomTipInformation(information);//将信息添加到主面板的信息区域中
        } else {//否则
            Main.setBottomTipInformation("");//信息置空
        }
    }

    public static void updateBottomTipInformation() {//更新底部提示信息
        String information = Main.getBottomTipInformation().getText();//获取原先信息
        if (selectionThumbnailItemList.isEmpty()) {//如果没有文件被选中
            int endIndex = information.indexOf('-');//获取先前图片的选中信息位置
            if (endIndex == -1) {//如果是没有文件选中时还是没有文件选中
                return;//不需要处理直接返回
            }//否则会越界
            Main.setBottomTipInformation(information.substring(0, endIndex));//重新获取提示信息并将信息更新到主面板的信息区域中
            return;//直接返回
        }
        if (information.contains("-")) {//如果先前选中过文件
            int endIndex = information.indexOf('-');//获取先前文件的选中信息位置
            information = information.substring(0, endIndex);//重新获取提示信息
        }
        information += (Main.SettingState.systemLanguage ? "- Select " : "- 选中") + selectionThumbnailItemList.size() + (Main.SettingState.systemLanguage ? " Items (" : "个项目（共");//添加选中信息
        double fileTotalSize = getSelectionThumbnailItemTotalSize();//获取选中图片总大小
        if (fileTotalSize / 1048576 > 1.0) {//如果文件总大小在1GB以上
            information += String.format("%.2f", fileTotalSize / 1048576) + (Main.SettingState.systemLanguage ? "GB Totally) " : "GB）");//显示多少GB
        } else if (fileTotalSize / 1024 > 1.0) {//如果文件总大小在1MB以上
            information += String.format("%.2f", fileTotalSize / 1024) + (Main.SettingState.systemLanguage ? "MB Totally) " : "MB）");//显示多少MB
        } else {//否则
            information += String.format("%.2f", fileTotalSize) + (Main.SettingState.systemLanguage ? "KB Totally) " : "KB）");//显示多少KB
        }
        Main.setBottomTipInformation(information);//将信息更新到主面板的信息区域中
    }

    public static double getSelectionThumbnailItemTotalSize() {//获取选中项目总大小
        List<Integer> count = new ArrayList<>();//临时存储选中文件数目
        for (int i = 0, j = 0; i < thumbnailItemList.size(); i++) {//遍历所有文件项目
            if (j >= selectionThumbnailItemList.size()) {//如果越界
                break;//直接退出
            }
            if (thumbnailItemList.get(i) == selectionThumbnailItemList.get(j)) {//如果文件项目被选中
                count.add(i);//添加被选中项目
                j++;//自增
            }
        }
        double fileTotalSize = 0.0;//选中项目总大小
        for (Integer integer : count) {//遍历被选中项目
            fileTotalSize += (double) currentFileList[integer].length() / 1024;//项目总大小增加
        }
        return fileTotalSize;//返回选中项目总大小
    }

    private static ThumbnailItem createThumbnailItem(File file) {//创建单个缩略图项目（创建完成返回缩略图项目类）
        SwingUtilities.invokeLater(() -> updateProgress((int) (++processedFiles * 100.0 / totalFiles)));//在读取文件后发送进度更新
        if (file.isDirectory()) {//如果是目录
            double ratio = Math.min((double) thumbnailItemWidth / directoryIcon.getWidth(), (double) IMAGE_MAX_HEIGHT / directoryIcon.getHeight());//计算缩略图缩放
            BufferedImage thumbnail = new BufferedImage((int) (directoryIcon.getWidth() * ratio), (int) (directoryIcon.getHeight() * ratio), BufferedImage.TYPE_INT_ARGB);//生成高质量缩略图
            Graphics2D g2d = thumbnail.createGraphics();//创建高质量缩放图像
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);//优先速度
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);//抗锯齿设置
            g2d.drawImage(directoryIcon, 0, 0, thumbnail.getWidth(), thumbnail.getHeight(), null);//绘制图像
            g2d.dispose();//释放
            ThumbnailItem item = new ThumbnailItem(file, thumbnail);//创建项目
            handleThumbnailMouseEvent(item);//处理项目鼠标事件
            handleThumbnailKeyEvent(item);//处理项目键盘事件
            return item;//返回项目
        } else {//如果是流式文件
            double ratio = Math.min((double) thumbnailItemWidth / textIcon.getWidth(), (double) IMAGE_MAX_HEIGHT / textIcon.getHeight());//计算缩略图缩放
            BufferedImage thumbnail = new BufferedImage((int) (textIcon.getWidth() * ratio), (int) (textIcon.getHeight() * ratio), BufferedImage.TYPE_INT_ARGB);//生成高质量缩略图
            Graphics2D g2d = thumbnail.createGraphics();//创建高质量缩放图像
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);//优先速度
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);//抗锯齿设置
            g2d.drawImage(textIcon, 0, 0, thumbnail.getWidth(), thumbnail.getHeight(), null);//绘制图像
            g2d.dispose();//释放
            ThumbnailItem item = new ThumbnailItem(file, thumbnail);//创建项目
            handleThumbnailMouseEvent(item);//处理项目鼠标事件
            handleThumbnailKeyEvent(item);//处理项目键盘事件
            return item;//返回项目
        }
    }

    private static void handleThumbnailMouseEvent(ThumbnailItem item) {//处理项目鼠标事件
        item.addMouseListener(new MouseAdapter() {//创建鼠标监听
            @Override
            public void mouseClicked(MouseEvent e) {//如果鼠标点击
                if (!mainPanel.hasFocus()) {//如果只对项目进行点击时
                    mainPanel.requestFocusInWindow();//主面板要获取焦点防止快捷键失效
                }
                if (SwingUtilities.isLeftMouseButton(e)) {//如果是鼠标左键
                    if ((e.getModifiers() & ActionEvent.CTRL_MASK) == ActionEvent.CTRL_MASK) {//如果ctrl键被按下
                        handleThumbnailCtrlClick(item);//ctrl+鼠标点击处理
                    } else if ((e.getModifiers() & ActionEvent.SHIFT_MASK) == ActionEvent.SHIFT_MASK) {//如果shift键被按下
                        handleThumbnailShiftClick(item);//shift+鼠标点击处理
                    } else {//如果都没有
                        handleThumbnailClick(item);//鼠标点击处理
                    }
                    updateBottomTipInformation();//更新提示信息
                    FileDisplayTopBar.fileManipulationButtonEnableJudgement(selectionThumbnailItemList.size());//进行按钮判断
                } else {//否则是鼠标右键
                    FileDisplayPopupMenu.rightMousePopupMenu.show(Main.diskManagementSystemFrame, e.getLocationOnScreen().x, e.getLocationOnScreen().y);//展示右键菜单
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {//如果鼠标按下
                if (SwingUtilities.isLeftMouseButton(e)) {//如果按下左键
                    selectionStart = new Point(item.getX() + e.getX(), item.getY() + e.getY());//记录起点（注意e获取的是鼠标相对这个组件左上角的位置，需要加上组件item的位置（item的位置是item相对mainPanel左上角的位置）才能获取鼠标在父组件mainPanel的位置）
                    selectionRect.setBounds(0, 0, 0, 0);//绘制框架
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {//如果鼠标松开
                if (draggedThumbnailItemWindow != null) {//如果项目拖拽窗口不为空
                    if (DirectoryTree.currentNodeObject != null) {//如果当前结点对象非空
                        if (DirectoryTree.currentNodeObject instanceof File selectedFile) {//如果当前结点对象是文件
                            String currentFolder = selectedFile.getPath();//获取当前文件夹
                            DirectoryTree.setCurrentFileList(DirectoryTree.detectFile(new File(currentFolder).listFiles()));//设置图片文件列表为当前文件夹
                            FileDisplayTopBar.updateFolder(currentFolder);//更新当前文件夹和文件夹列表
                            FileDisplayTopBar.setDirectoryField(currentFolder);//设置当前文件路径
                            FileDisplayMainPanel.updateFileDisplayMainPanel(false);//通知更新图片预览面板（采用类名调用的方式，防止创建多个类）
                            directoryManipulationButtonEnableJudgement();//按钮判断
                            handlePaste();//处理粘贴
                        } else if (Objects.equals(DirectoryTree.currentNodeObject, (Main.SettingState.systemLanguage ? "My Cloud" : "我的云盘"))) {//如果是云盘结点
                            String currentFolder = Main.SettingState.systemLanguage ? "My Cloud" : "我的云盘";//获取当前文件夹
                            try {
                                DirectoryTree.setCurrentFileList(User.handleUserLoadUserUploadPicture(null));//更新图片文件列表
                            } catch (IOException ex) {
                                handleErrorLog(ex.getMessage());//处理错误日志
                                throw new RuntimeException(ex);//捕获异常
                            }
                            FileDisplayTopBar.updateFolder(currentFolder);//更新当前文件夹和文件夹列表
                            FileDisplayTopBar.setDirectoryField(currentFolder);//设置当前文件路径
                            FileDisplayMainPanel.updateFileDisplayMainPanel(false);//通知更新图片预览面板（采用类名调用的方式，防止创建多个类）
                            directoryManipulationButtonEnableJudgement();//按钮判断
                            handlePaste();//处理粘贴
                        }
                        DirectoryTree.currentNodeObject = null;//选中结点置空
                    } else {//否则
                        isCutOperation = false;//不再剪切
                        clipboardFiles.clear();//清空
                    }
                    if (draggedThumbnailItem != null) {//如果拖拽项目不为空
                        draggedThumbnailItem.dispose();//就释放拖拽项目
                        draggedThumbnailItem = null;//拖拽项目置空
                    }
                    draggedThumbnailItemWindow.removeAll();//清空
                    draggedThumbnailItemWindow.dispose();//释放项目拖拽窗口
                    draggedThumbnailItemWindow = null;//项目拖拽窗口置空
                    fileManipulationButtonEnableJudgement(selectionThumbnailItemList.size());//按钮状态判断
                    refreshMainPanel();//刷新
                }
                if (selectionStart != null && dragging) {//如果有起点且鼠标正在拖拽
                    handleSelection(e);//就选中图片
                    selectionStart = null;//起点清空
                    selectionRect.setBounds(0, 0, 0, 0);//矩形清空
                    mainPanel.repaint();//触发重绘
                    dragging = false;//清除拖动状态
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                item.hoverTipTimer.start();//计时器开始
            }

            @Override
            public void mouseExited(MouseEvent e) {//如果鼠标离开
                item.hoverTipTimer.stop();//计时器结束
                if (itemHoverTipWindow != null) {//如果提示信息不为空
                    itemHoverTipWindow.dispose();//释放提示信息
                    itemHoverTipWindow = null;//提示信息置空
                }
            }
        });
        item.addMouseMotionListener(new MouseMotionAdapter() {//创建鼠标滚轮监听
            @Override
            public void mouseDragged(MouseEvent e) {//如果鼠标拖动
                dragging = true;//正在拖动
                if (selectionThumbnailItemList.contains(item)) {//如果这个项目被选中，就拖动图片（同windows，只有被选中的项目可以拖动）
                    if (draggedThumbnailItemWindow != null) {//如果已经创建拖拽窗口：进行拖拽处理
                        draggedThumbnailItemWindow.setVisible(true);//设置可见
                        handleDraggedThumbnailItem(e.getLocationOnScreen());//就处理拖拽项目
                    } else {//如果尚未创建拖拽窗口，就进行初始化
                        isCutOperation = true;//剪切
                        clipboardFiles.clear();//清空剪贴板
                        for (ThumbnailItem item : selectionThumbnailItemList) {//遍历选中文件列表
                            clipboardFiles.add(item.getFile());//更新剪贴板
                        }
                        BufferedImage original = selectionThumbnailItemList.getFirst().getFileImage();//获取原始图像
                        double ratio = (double) 120 / original.getWidth();//计算图像缩放比例
                        int newHeight = (int) (original.getHeight() * ratio);//计算图像新高度
                        BufferedImage thumbnail = new BufferedImage(120, newHeight, BufferedImage.TYPE_INT_RGB);//创建缩放图像
                        Graphics2D g2d = thumbnail.createGraphics();//创建
                        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);//抗锯齿
                        g2d.drawImage(original, 0, 0, 120, newHeight, null);//绘制图像
                        g2d.dispose();//释放

                        draggedThumbnailItem = new ThumbnailItem(thumbnail, selectionThumbnailItemList.size());//创建拖拽项目
                        draggedThumbnailItemWindow = new JWindow();//创建项目拖拽窗口
                        draggedThumbnailItemWindow.add(draggedThumbnailItem);//添加拖拽项目
                        draggedThumbnailItemWindow.setBackground(Main.SettingState.themeColor ? DARK_PICTURE_DRAGGED_WINDOW_COLOR : LIGHT_PICTURE_DRAGGED_WINDOW_COLOR);//设置背景颜色
                        draggedThumbnailItemWindow.setSize((int) draggedThumbnailItem.getPreferredSize().getWidth(), (int) draggedThumbnailItem.getPreferredSize().getHeight());//设置窗口大小与被拖拽缩略图项目大小等大
                        draggedThumbnailItemWindow.setLocation(e.getLocationOnScreen());//就设置窗口位置
                        draggedThumbnailItemWindow.setAlwaysOnTop(true);//设置在顶部
                        fileManipulationButtonEnableJudgement(selectionThumbnailItemList.size());//按钮状态判断
                        refreshMainPanel();//刷新
                    }
                } else if (selectionStart != null) {//否则没有被选中，进行鼠标框选判断，如果有鼠标起点，就开始框选（同windows，只有没被选中的项目可以开始框选）
                    int x = Math.min(selectionStart.x, item.getX() + e.getX());//获取左上角x坐标
                    int y = Math.min(selectionStart.y, item.getY() + e.getY());//获取左上角y坐标
                    int width = Math.abs(item.getX() + e.getX() - selectionStart.x);//获取宽度
                    int height = Math.abs(item.getY() + e.getY() - selectionStart.y);//获取高度
                    selectionRect.setBounds(x, y, width, height);//绘制框架：从左上角开始绘制宽为width高为height的矩形
                    mainPanel.repaint();//触发重绘

                    try {
                        JScrollPane scrollPane = (JScrollPane) mainPanel.getParent().getParent();//获取滚动条
                        JViewport viewport = scrollPane.getViewport();//获取视口
                        Point mouseScreenPoint = e.getLocationOnScreen();//获取鼠标位置
                        Point viewportScreenPoint = viewport.getLocationOnScreen();//获取视口在屏幕的位置
                        int relativeY = mouseScreenPoint.y - viewportScreenPoint.y;//获取鼠标相对视口移动了多少距离
                        if (relativeY < 150 || relativeY > viewport.getHeight() - 150) {//如果在阈值以内或在视口高度的倒数阈值以内
                            if (!scrollTimer.isRunning()) {//如果计时器没有运行
                                scrollTimer.start();//就开始运行
                            }
                        } else {//否则
                            if (scrollTimer.isRunning()) {//如果计时器正在运行
                                scrollTimer.stop();//就停止运行
                            }
                        }
                    } catch (Exception ex) {
                        handleErrorLog(ex.getMessage());//处理错误日志
                        ex.printStackTrace();//捕获异常
                    }
                }
            }
        });
    }

    private static void handleThumbnailClick(ThumbnailItem clickedItem) {//处理缩略图点击事件
        if (selectionThumbnailItemList.isEmpty()) {//如果没有项目被选中
            selectionThumbnailItemList.add(clickedItem);//设置当前项选中
            selectionAnchorIndex = thumbnailItemList.indexOf(clickedItem);//设置锚点为当前项目
        } else if (selectionThumbnailItemList.size() == 1) {//如果只有一个项目被选中
            if (clickedItem == selectionThumbnailItemList.getFirst()) {//如果选中项目是自身
                if (Main.SettingState.dbclickBehavior) {//如果是双击打开
                    if (editButton.isEnabled()) {//如果幻灯片按钮有效
                        FileDisplayBottomBar.handleEdit();//处理打开
                    }
                } else {//否则是双击反选
                    selectionThumbnailItemList.clear();//清空所有选中状态
                    selectionAnchorIndex = -1;//重置锚点
                }
            } else {//如果选中项目不是自身
                selectionThumbnailItemList.clear();//清空所有选中状态
                selectionThumbnailItemList.add(clickedItem);//设置当前项选中
                selectionAnchorIndex = thumbnailItemList.indexOf(clickedItem);//设置锚点为当前项目
            }
        } else {//如果有多个项目被选中
            selectionThumbnailItemList.clear();//清空所有选中状态
            selectionThumbnailItemList.add(clickedItem);//设置当前项选中
            selectionAnchorIndex = thumbnailItemList.indexOf(clickedItem);//设置锚点为当前项目
        }
        refreshMainPanel();//刷新更新边框显示
    }

    private static void handleThumbnailCtrlClick(ThumbnailItem clickedItem) {//处理缩略图ctrl+点击事件
        if (selectionThumbnailItemList.contains(clickedItem)) {//如果点击的项目是已经被选中了（getOrDefault返回clickedItem的值，如果找不到项目就返回defaultValue里的值）
            selectionThumbnailItemList.remove(clickedItem);//设置当前项取消选中
            if (selectionThumbnailItemList.isEmpty()) {//如果选中文件列表为空
                selectionAnchorIndex = -1;//重置锚点
            }
        } else {//否则
            selectionThumbnailItemList.add(clickedItem);//设置当前项选中
            selectionAnchorIndex = thumbnailItemList.indexOf(clickedItem);//设置锚点为当前项目
        }
        refreshMainPanel();//刷新更新边框显示
    }

    private static void handleThumbnailShiftClick(ThumbnailItem clickedItem) {//处理缩略图shift+点击事件
        int index = thumbnailItemList.indexOf(clickedItem);//获取当前选中的索引值
        if (selectionAnchorIndex == -1) {//如果没有选中项目
            selectionAnchorIndex = 0;//设置锚点为0以从0开始选择
        }
        int start = Math.min(selectionAnchorIndex, index);//获取小的作为起点
        int end = Math.max(selectionAnchorIndex, index);//获取大的作为起点
        selectionThumbnailItemList.clear();//清空所有选中状态
        for (int i = start; i <= end; i++) {//遍历两者之间（如果没有选中，就从0开始选择到当前；如果有一个选中：选择这两个项目之间的全部项目，其他项目置为未选中；如果有多个选中：选择这最近被选择项目和被选择项目之间的全部项目，其他项目置为未选中）
            selectionThumbnailItemList.add(thumbnailItemList.get(i));//设置选中
        }
        refreshMainPanel();//刷新更新边框显示
    }

    private static void handleThumbnailKeyEvent(ThumbnailItem item) {//处理项目键盘事件
        InputMap inputMap = item.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);//设置当前正在输入的面板，如果聚焦主面板则监听项目的键盘输入
        ActionMap actionMap = item.getActionMap();//获取项目的行动面板
        bindKey(inputMap, actionMap, KeyEvent.VK_ENTER, 0, "openInOther");//在其他软件打开
    }

    private static void handleDraggedThumbnailItem(Point point) {//处理被拖拽缩略图项目
        if (draggedThumbnailItemWindow != null) {//如果项目拖拽窗口不为空
            draggedThumbnailItemWindow.setLocation(point);//就设置窗口位置
        }
    }

    public static String calculateItemHoverTipFileSize(long bytes) {//计算文件大小信息
        if (bytes < 1024) {//如果在1KB内
            return bytes + " B";//展示多少字节
        }
        int exp = (int) (Math.log(bytes) / Math.log(1024));//否则根据大小展示
        return String.format("%.3f %sB", bytes / Math.pow(1024, exp), "KMGTPE".charAt(exp - 1));//返回文件大小信息
    }

    public static void refreshMainPanel() {//刷新主面板
        mainPanel.revalidate();//重新验证
        mainPanel.repaint();//重新绘制
    }

    public static class ThumbnailItem extends JComponent {//缩略图项目类（继承JComponent）
        private File file;//文件
        private String fileName;//文件名称
        private BufferedImage fileImage;//文件图像
        private List<String> textLines;//文件名称多行文本行（一个字符串列表，一个单位字符串代表一整行文本，实现对文件名称的分离）
        private transient int textHeight;//瞬态缓存字体高度
        private transient Timer hoverTipTimer;//瞬态鼠标悬浮时间计数器
        private final transient FontMetrics fontMetrics = getFontMetrics(thumbnailItemFont);//瞬态字体格式

        public File getFile() {//获取文件
            return file;
        }

        public BufferedImage getFileImage() {//获取图片
            return fileImage;
        }

        public ThumbnailItem(File file, BufferedImage fileImage) {//构造方法：正常创建缩略图项目
            this.file = file;
            this.fileName = file.getName();
            if (Main.SettingState.pictureSuffix) {//如果关闭图片后缀
                String pictureSuffix = FilenameUtils.getExtension(fileName);//获取图片扩展名
                if (!pictureSuffix.isEmpty()) {//如果不是空串，即找得到扩展名
                    int index = fileName.lastIndexOf(pictureSuffix);//获取最后定位到的扩展名的索引值
                    if (index != -1) {//如果找到扩展名
                        fileName = fileName.substring(0, index - 1);//截断重命名
                    }
                }
            }
            this.fileImage = fileImage;
            this.textLines = calculateTextLines(thumbnailItemWidth - 10);//计算文本行
            this.textHeight = textLines.size() * getFontMetrics(thumbnailItemFont).getHeight();//计算文本高度为文本行行数乘字体高度
            hoverTipTimer = new Timer(1000, _ -> showItemHoverTipWindow());//初始化悬停计时器，设置1秒后就传递鼠标事件，展示提示信息
            hoverTipTimer.setRepeats(false);//设置计时器不重复
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));//设置鼠标为手指，提升交互体验
            setPreferredSize(new Dimension(fileImage.getWidth(), fileImage.getHeight() + textHeight));//设置组件大小（宽度和图片一致，高度为图片高度加上文本高度）
        }

        public ThumbnailItem(BufferedImage image, int size) {//构造方法：创建被拖拽缩略图项目
            this.fileImage = image;
            this.textLines = List.of((Main.SettingState.systemLanguage ? "" : "共") + size + (Main.SettingState.systemLanguage ? "Item Totally" : "个项目"));//创建文本行为共多少张被拖拽缩略图项目
            this.textHeight = textLines.size() * getFontMetrics(thumbnailItemFont).getHeight();//设置文本高度
            setPreferredSize(new Dimension(image.getWidth(), image.getHeight() + textHeight));//设置组件大小
        }

        private List<String> calculateTextLines(int containerWidth) {//计算文本行：将文件名称转化成多行文本行
            List<String> lines = new ArrayList<>();//创建临时文本行
            StringBuilder currentLine = new StringBuilder();//当前行
            for (char c : fileName.toCharArray()) {//对文件名进行字符遍历
                if (fontMetrics.stringWidth(currentLine.toString() + c) > containerWidth) {//如果当前文本行宽度加上当前字符已经超出最大宽度
                    lines.add(currentLine.toString());//把当前行添加到文本行中
                    currentLine = new StringBuilder();//然后重新创建当前行，即对其清空
                }
                currentLine.append(c);//把最后一个字符添加到当前行
            }
            if (!currentLine.isEmpty()) lines.add(currentLine.toString());//如果最后一行还有内容，再添加到文本行中
            textHeight = lines.size() * fontMetrics.getHeight();//通过文本行大小乘上文本高度获取文本组件高度
            return lines;//返回临时文本行
        }

        private void showItemHoverTipWindow() {//显示提示信息
            if (Main.SettingState.hoverTip || hoverTipTimer == null || !isDisplayable() || !isShowing() || !isVisible()) {//如果取消悬浮提示或悬浮提示不可展示或为空或不可见
                return;//直接返回
            }
            if (!SwingUtilities.isEventDispatchThread()) {//如果不是这个事件的调度线程
                SwingUtilities.invokeLater(this::showItemHoverTipWindow);//就推迟调用
                return;//返回
            }
            try {
                Point mousePosition = MouseInfo.getPointerInfo().getLocation();//获取鼠标位置
                Rectangle bounds = new Rectangle(this.getLocationOnScreen(), this.getSize());//创建矩形，左上角为组件坐标，大小为组件大小，即与组件同大
                if (bounds.contains(mousePosition)) {//如果矩形内包含鼠标
                    createItemHoverTipWindow(mousePosition);//进行展示
                }
            } catch (IllegalComponentStateException e) {
                handleErrorLog(e.getMessage());//处理错误日志
                dispose();//捕获异常并清理资源
            }
        }

        private void createItemHoverTipWindow(Point mousePos) {//创建并显示提示窗口
            if (itemHoverTipWindow != null) {//如果提示信息窗口存在
                itemHoverTipWindow.dispose();//关闭之前的提示信息窗口
            }
            if (file != null) {//如果不为空
                JPanel content = new JPanel(new GridLayout(4, 1));//设置网格布局为6行1列
                content.setBackground(new Color(250, 250, 250));//设置背景颜色
                content.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.GRAY), BorderFactory.createEmptyBorder(5, 5, 5, 5)));//用边框可以不用把文字设置到组件中心也能让文字左右隔出距离
                try {
                    content.add(createItemHoverTipLabel((Main.SettingState.systemLanguage ? "Create Date: " : "创建时间：") + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Files.readAttributes(file.toPath(), BasicFileAttributes.class).creationTime().toMillis())));//图片创建时间
                    content.add(createItemHoverTipLabel((Main.SettingState.systemLanguage ? "Modify Date: " : "修改时间：") + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(file.lastModified()))));//图片修改时间
                    content.add(createItemHoverTipLabel((Main.SettingState.systemLanguage ? "Access Date: " : "访问时间：") + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Files.readAttributes(file.toPath(), BasicFileAttributes.class).lastAccessTime().toMillis())));//图片访问时间
                } catch (IOException e) {
                    handleErrorLog(e.getMessage());//处理错误日志
                    throw new RuntimeException(e);//捕获异常
                }
                content.add(createItemHoverTipLabel((Main.SettingState.systemLanguage ? "Size: " : "大小: ") + calculateItemHoverTipFileSize(file.length())));//图片大小
                itemHoverTipWindow = new JWindow(Main.diskManagementSystemFrame);//创建提示信息窗口
                itemHoverTipWindow.setContentPane(content);//放入内容
                itemHoverTipWindow.pack();//合适
                int x = mousePos.x;//x坐标
                int y = mousePos.y + 23;//y坐标向下偏移23px避免遮挡鼠标
                if (x + itemHoverTipWindow.getWidth() > Main.screenSize.width) {//防止溢出屏幕
                    x -= x + itemHoverTipWindow.getWidth() - Main.screenSize.width;//x减去溢出部分
                }
                if (y + itemHoverTipWindow.getHeight() > Main.screenSize.height) {//防止溢出屏幕
                    y = mousePos.y - itemHoverTipWindow.getHeight();//y坐标向上偏移窗口高度：不能只是单纯减去溢出部分，因为鼠标指针自身也会遮挡窗口
                }
                itemHoverTipWindow.setLocation(x, y);//设置位置
                itemHoverTipWindow.setAlwaysOnTop(Main.SettingState.windowState);//设置永远在最上层
                itemHoverTipWindow.setFocusableWindowState(false);//设置不可聚焦
                itemHoverTipWindow.setVisible(true);//设置可见
            }
        }

        private JLabel createItemHoverTipLabel(String text) {//根据文本创建面板
            JLabel label = new JLabel(text);//创建面板
            label.setFont(new Font("楷体", PLAIN, 14));//设置字体
            return label;//返回面板
        }

        public void updateThumbnailItemSize(int newWidth, File file) {//更新缩略图尺寸
            if (file.isDirectory()) {//如果是目录
                double ratio = (double) newWidth / directoryIcon.getWidth();//计算图像缩放比例
                int newHeight = (int) (directoryIcon.getHeight() * ratio);//计算图像新高度
                BufferedImage thumbnail = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);//创建缩放图像
                Graphics2D g2d = thumbnail.createGraphics();//创建
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);//抗锯齿
                g2d.drawImage(directoryIcon, 0, 0, newWidth, newHeight, null);//绘制图像
                g2d.dispose();//释放
                SwingUtilities.invokeLater(() -> {//推迟进行
                    this.fileImage = thumbnail;//设置图像为新缩放图像
                    updateImageSize(newWidth, newHeight);//更新图像大小
                });
            } else {//否则是流式文件
                double ratio = (double) newWidth / textIcon.getWidth();//计算图像缩放比例
                int newHeight = (int) (textIcon.getHeight() * ratio);//计算图像新高度
                BufferedImage thumbnail = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);//创建缩放图像
                Graphics2D g2d = thumbnail.createGraphics();//创建
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);//抗锯齿
                g2d.drawImage(textIcon, 0, 0, newWidth, newHeight, null);//绘制图像
                g2d.dispose();//释放
                SwingUtilities.invokeLater(() -> {//推迟进行
                    this.fileImage = thumbnail;//设置图像为新缩放图像
                    updateImageSize(newWidth, newHeight);//更新图像大小
                });
            }
        }

        private void updateImageSize(int width, int height) {//更新图像大小
            this.textLines = calculateTextLines(width - 10);//重新计算文本行
            this.textHeight = textLines.size() * fontMetrics.getHeight();//重新计算文本高度
            setPreferredSize(new Dimension(width, height + textHeight + 4));//设置组件大小
            revalidate();//重新分配
            repaint();//重新绘制
        }

        @Override
        public void setVisible(boolean visible) {//设置是否可见
            super.setVisible(visible);//调用父类设置是否可见
        }

        @Override
        public void removeNotify() {//释放资源
            if (getParent() == null) {//只有当组件未被任何容器使用时才释放
                dispose();//先执行清理
                if (fileImage != null) {//如果图像非空
                    fileImage.flush();//释放图像
                    fileImage = null;//图像置空
                }
                if (hoverTipTimer != null && hoverTipTimer.isRunning()) {//如果有计时器
                    hoverTipTimer.stop();//停止计时器
                }
                if (itemHoverTipWindow != null) {//如果提示信息窗口不为空
                    itemHoverTipWindow.dispose();//释放提示信息窗口
                    itemHoverTipWindow = null;//提示信息窗口置空
                }
                super.removeNotify();//调用父类清除
            }
        }

        public void dispose() {//进行释放时
            if (fileImage != null) {//如果文件图片不为空
                fileImage.flush();//清空文件图片
                fileImage = null;//文件图片置空
            }
            if (hoverTipTimer != null && hoverTipTimer.isRunning()) {//如果有计时器
                hoverTipTimer.stop();//停止计时器
            }
            if (itemHoverTipWindow != null) {//如果提示信息窗口不为空
                itemHoverTipWindow.dispose();//释放提示信息窗口
                itemHoverTipWindow = null;//提示信息窗口置空
            }
        }

        @Override
        protected void paintComponent(Graphics g) {//绘制组件（该方法在类初始化时会自动调用）
            super.paintComponent(g);//调用父类方法确保正确清除背景
            Graphics2D g2d = (Graphics2D) g;//创建二维绘制工具类
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);//抗锯齿设置
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
            try {
                if (fileImage != null) {//如果文件图片非空
                    g2d.setColor(Main.SettingState.themeColor ? DARK_PICTURE_MAIN_COLOR : LIGHT_PICTURE_MAIN_COLOR);//设置背景色
                    g2d.fillRect(0, 0, getWidth(), fileImage.getHeight());//绘制背景色
                    g2d.drawImage(fileImage, 0, 0, this);//绘制图像
                }

                if (textLines != null) {//如果不为空
                    g.setColor(Main.SettingState.themeColor ? DARK_PICTURE_FONT_COLOR : LIGHT_PICTURE_FONT_COLOR);//设置字体颜色
                    g.setFont(thumbnailItemFont);//设置字体
                    int y = fileImage.getHeight() + 18;//文本垂直位置为图像下方18px
                    for (String line : textLines) {//遍历文本行每一行
                        int x = (getWidth() - fontMetrics.stringWidth(line)) / 2;//文本水平位置为图像中心
                        g.drawString(line, x, y);//绘制当前文本行
                        y += fontMetrics.getHeight();//为垂直位置增加当前文本行偏移量
                    }
                }

                if (selectionThumbnailItemList.contains(this) && Main.diskManagementSystemFrame.isVisible()) {//如果图片被选中且不在幻灯片
                    g2d.setStroke(new BasicStroke(BORDER_THICKNESS));//设置边框厚度
                    g2d.setColor(PICTURE_SELECTED_BORDER_COLOR);//设置边框颜色
                    g2d.drawRect(BORDER_THICKNESS / 2, BORDER_THICKNESS / 2, getWidth() - BORDER_THICKNESS, getHeight() - BORDER_THICKNESS);//绘制矩形
                }

                if (isCutOperation && clipboardFiles.contains(this.getFile()) && Main.diskManagementSystemFrame.isVisible()) {//如果正在进行剪切且剪切板有这张图片且不在幻灯片
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));//设置混和边框
                    g2d.setColor(new Color(255, 255, 255, 150));//设置颜色为白色半透明
                    g2d.fillRect(0, 0, getWidth(), fileImage.getHeight());//绘制边框
                    g2d.setComposite(AlphaComposite.SrcOver);//恢复默认混合模式
                }
            } finally {//不管有没有创建成功都执行
                g2d.dispose();//释放图像资源
            }
        }
    }

    public static class WrapLayout extends FlowLayout {//自动换行布局管理器

        public WrapLayout(int align, int hgap, int vgap) {//构造对象时传入布局方式，水平间隔，垂直间隔
            super(align, hgap, vgap);//调用父类
        }

        @Override
        public Dimension preferredLayoutSize(Container target) {//重写布局大小方法
            synchronized (target.getTreeLock()) {//同步代码块：把临界资源操作目标锁起来，解决线程安全问题（注意锁对象必须唯一才能真正锁起来）
                int width = target.getParent().getWidth() - 1, x = 0, y = getVgap(), rowHeight = 0;//初始宽度为父容器视口宽度（增加1px边距缓冲），x和y为当前图片的存放位置坐标，x初始为0（即最左侧），y为垂直间隔，初始行高为0
                for (Component comp : target.getComponents()) {//遍历当前主面板的所有组件
                    Dimension dim = comp.getPreferredSize();//获取组件item大小
                    if (x + dim.width + getHgap() > width) {//如果x加上组件大小加上水平间隔比父组件mainPanel宽度还大，x和y就要进入下一行
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
}
