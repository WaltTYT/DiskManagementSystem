package FileEditPackage;

import MainPackage.Main;
import FileDisplayPackage.FileDisplayMainPanel;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static MainPackage.Setting.handleErrorLog;
import static MainPackage.ThemeColor.*;
import static FileDisplayPackage.FileDisplayMainPanel.BORDER_THICKNESS;
import static FileDisplayPackage.FileDisplayMainPanel.calculateItemHoverTipFileSize;
import static FileEditPackage.FileEditPanel.updatePicturePanel;
import static java.awt.Font.PLAIN;

public class FileEditScrollPane {//幻灯片滚动栏类
    public static final int SCROLL_PANE_IMAGE_HEIGHT = 100;//滚动栏图片高度常量

    public static final JPanel scrollPanePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));//滚动栏面板
    public static final JScrollPane scrollPane = new JScrollPane(scrollPanePanel);//滚动栏滚动条
    public static JWindow itemHoverTipWindow;//项目悬浮提示窗口

    public static int pictureIndex = 0;//当前图片索引
    public static final Point dragStartPoint = new Point();//拖动起点坐标：用于拖动缩略图
    public static final List<ScrollItem> scrollItemList = new ArrayList<>();//滚动栏项目列表：不直接引用主面板的列表，而是创建副本

    public FileEditScrollPane() {//构造方法
        scrollPane.getHorizontalScrollBar().setUnitIncrement(55);//设置水平滚动条单次滚动长度
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);//强制显示水平滚动条
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);//禁用垂直滚动条
        scrollPane.getHorizontalScrollBar().setUI(new Main.CustomScrollPane());//设置滚动条UI

        scrollPanePanel.setBackground(Main.SettingState.themeColor ? DARK_DIRECTORY_MAIN_COLOR : LIGHT_DIRECTORY_MAIN_COLOR);//设置背景颜色
        scrollPanePanel.addMouseListener(new MouseAdapter() {//添加鼠标监听
            @Override
            public void mousePressed(MouseEvent e) {//如果鼠标按下
                dragStartPoint.setLocation(e.getX(), e.getY());//更新起点为当前鼠标位置
            }

            @Override
            public void mouseReleased(MouseEvent e) {//如果鼠标松开
                dragStartPoint.setLocation(0, 0);//置空
            }
        });
        scrollPanePanel.addMouseMotionListener(new MouseAdapter() {//添加鼠标动作监听
            @Override
            public void mouseDragged(MouseEvent e) {//如果鼠标拖动
                JScrollBar scrollbar = scrollPane.getHorizontalScrollBar();//获取水平拖动条
                scrollbar.setValue(scrollbar.getValue() + dragStartPoint.x - e.getX());//设置值
            }
        });
    }

    public static void updateScrollPane() {//更新滚动栏
        scrollItemList.clear();//清空缩略图项目列表
        scrollPanePanel.removeAll();//清空图片面板
        for (FileDisplayMainPanel.ThumbnailItem originalItem : FileDisplayMainPanel.thumbnailItemList) {//遍历原始缩略图项目列表
            ScrollItem copyItem = createScrollItem(originalItem.getFile(), originalItem.getFileImage());//创建复制项目（因为javaswing的组件同时只能有一份实例，直接移动会清空原组件，必须复制）
            copyItem.addMouseListener(new MouseAdapter() {//为复制项目添加鼠标监听
                @Override
                public void mouseClicked(MouseEvent e) {//如果鼠标点击
                    pictureIndex = scrollItemList.indexOf(copyItem);//索引更新为当前项目
                    updatePicturePanel();//更新图片面板
                }
            });
            scrollItemList.add(copyItem);//复制后的项目添加到缩略图项目列表中
            scrollPanePanel.add(copyItem);//复制后的项目添加到图片面板中
        }
    }

    private static ScrollItem createScrollItem(File file, BufferedImage bufferedImage) {//创建滚动栏项目
        double ratio = (double) SCROLL_PANE_IMAGE_HEIGHT / bufferedImage.getHeight();//计算缩略图缩放
        BufferedImage scaled = new BufferedImage((int) (bufferedImage.getWidth() * ratio), (int) (bufferedImage.getHeight() * ratio), BufferedImage.TYPE_INT_RGB);//生成高质量缩略图
        Graphics2D g2d = scaled.createGraphics();//创建高质量缩放图像
        g2d.drawImage(bufferedImage, 0, 0, scaled.getWidth(), scaled.getHeight(), null);//绘制图像
        g2d.dispose();//释放
        return new ScrollItem(file, scaled);//返回项目
    }

    public static void scrollToVisible() {//滚动到可视
        if (pictureIndex < 0 || pictureIndex >= scrollItemList.size()) {//如果越界
            return;//直接返回
        }
        Rectangle rect = scrollItemList.get(pictureIndex).getBounds();//获取目标组件的边界（相对于滚动面板）
        int x = rect.x + rect.width;//获取组件的右边界
        int width = Main.screenSize.width;//获取屏幕宽度
        int value = scrollPane.getHorizontalScrollBar().getValue();//获取滚动条值
        if (x - value > width) {//如果需要向右滚动
            scrollPane.getHorizontalScrollBar().setValue(x - width + 3);//向右滚到右边界（手动+3保证右边框可见）
        } else if (value - rect.x > 0) {//如果需要向左滚动
            scrollPane.getHorizontalScrollBar().setValue(rect.x);//向左滚到左边界
        }
    }

    public static void refreshScrollPanePanel() {//刷新滚动栏面板
        scrollPanePanel.revalidate();//重新验证布局
        scrollPanePanel.repaint();//重新绘制
    }

    public static class ScrollItem extends JComponent {//自定义滚动栏项目类（继承JComponent）
        private final File file;//文件
        private BufferedImage fileImage;//文件图像
        private final transient Timer hoverTipTimer;//瞬态鼠标悬浮时间计数器

        public ScrollItem(File file, BufferedImage fileImage) {//构造方法：幻灯片滚动栏创建缩略图项目
            this.file = file;
            this.fileImage = fileImage;
            hoverTipTimer = new Timer(1000, _ -> showItemHoverTipWindow());//初始化悬停计时器，设置1秒后就传递鼠标事件，展示提示信息
            hoverTipTimer.setRepeats(false);//设置计时器不重复
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));//设置鼠标为手指，提升交互体验
            int width = fileImage.getWidth(), height = fileImage.getHeight();//宽度和高度
            setPreferredSize(new Dimension(width, height + 2));//设置组件大小（宽度和图片一致，高度为图片高度加上文本高度加上2px）

            addMouseListener(new MouseAdapter() {//添加鼠标监听
                @Override
                public void mousePressed(MouseEvent e) {//如果鼠标按下
                    dragStartPoint.setLocation(e.getX(), e.getY());//更新起点为当前鼠标位置
                }

                @Override
                public void mouseReleased(MouseEvent e) {//如果鼠标松开
                    dragStartPoint.setLocation(0, 0);//置空
                }

                @Override
                public void mouseEntered(MouseEvent e) {//如果鼠标进入
                    hoverTipTimer.start();//计时器开始
                }

                @Override
                public void mouseExited(MouseEvent e) {//如果鼠标离开
                    hoverTipTimer.stop();//计时器结束
                    if (itemHoverTipWindow != null) {//如果提示信息不为空
                        itemHoverTipWindow.dispose();//释放提示信息
                        itemHoverTipWindow = null;//提示信息置空
                    }
                }
            });
            addMouseMotionListener(new MouseAdapter() {//添加鼠标动作监听
                @Override
                public void mouseDragged(MouseEvent e) {//如果鼠标拖动
                    JScrollBar scrollbar = scrollPane.getHorizontalScrollBar();//获取水平拖动条
                    scrollbar.setValue(scrollbar.getValue() + dragStartPoint.x - e.getX());//设置值
                    hoverTipTimer.stop();//计时器结束
                    if (itemHoverTipWindow != null) {//如果提示信息不为空
                        itemHoverTipWindow.dispose();//释放提示信息
                        itemHoverTipWindow = null;//提示信息置空
                    }
                }
            });
        }

        private void showItemHoverTipWindow() {//显示提示信息
            if (Main.SettingState.hoverTip || hoverTipTimer == null || !isShowing() || !isVisible()) {//如果取消悬浮提示或悬浮提示为空或不可见
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
                JPanel content = new JPanel(new GridLayout(6, 1));//设置网格布局为6行1列
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
                try (ImageInputStream input = ImageIO.createImageInputStream(file)) {//根据文件创建图片输入流
                    ImageReader reader = ImageIO.getImageReaders(input).next();//创建图片读入者读取图像
                    reader.setInput(input);//设置读取出来的图像
                    BufferedImage original = reader.read(0, reader.getDefaultReadParam());//获取原始图像
                    content.add(createItemHoverTipLabel((Main.SettingState.systemLanguage ? "Resolution: " : "分辨率: ") + original.getWidth() + "x" + original.getHeight()));//图片分辨率
                } catch (IOException e) {
                    handleErrorLog(e.getMessage());//处理错误日志
                    throw new RuntimeException(e);//捕获异常
                }
                content.add(createItemHoverTipLabel((Main.SettingState.systemLanguage ? "Size: " : "大小: ") + calculateItemHoverTipFileSize(file.length())));//图片大小
                itemHoverTipWindow = new JWindow(Main.editFrame);//创建提示信息窗口
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

        @Override
        public void removeNotify() {//释放资源
            if (getParent() == null) {//只有当组件未被任何容器使用时才释放
                dispose();//先执行清理
                if (fileImage != null) {//如果图像非空
                    fileImage.flush();//释放图像
                    fileImage = null;//图像置空
                }
                super.removeNotify();//调用父类清除
            }
        }

        public void dispose() {//进行释放时
            if (fileImage != null) {//如果文件图片不为空
                fileImage.flush();//清空文件图片
                fileImage = null;//文件图片置空
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
                    g2d.drawImage(fileImage, 0, 2, null);//绘制图像
                }

                if (scrollItemList.indexOf(this) == FileEditScrollPane.pictureIndex && Main.editFrame.isVisible()) {//如果图片被选中且在幻灯片
                    g2d.setStroke(new BasicStroke(BORDER_THICKNESS));//设置边框厚度
                    g2d.setColor(PICTURE_SELECTED_BORDER_COLOR);//设置边框颜色
                    g2d.drawRect(BORDER_THICKNESS / 2, BORDER_THICKNESS / 2, getWidth() - BORDER_THICKNESS, getHeight() - BORDER_THICKNESS);//绘制矩形
                }
            } finally {//不管有没有创建成功都执行
                g2d.dispose();//释放图像资源
            }
        }
    }
}
