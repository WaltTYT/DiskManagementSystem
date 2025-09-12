package FileEditPackage;

import MainPackage.Main;
import FileDisplayPackage.FileDisplayMainPanel;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import static MainPackage.Setting.handleErrorLog;
import static MainPackage.ThemeColor.DARK_PICTURE_MAIN_COLOR;
import static MainPackage.ThemeColor.LIGHT_PICTURE_MAIN_COLOR;
import static FileDisplayPackage.FileDisplayMainPanel.bindKey;
import static FileDisplayPackage.FileDisplayMainPanel.calculateItemHoverTipFileSize;
import static FileDisplayPackage.FileDisplayTopBar.hoverTimer;
import static FileDisplayPackage.FileDisplayTopBar.showButtonHoverTipWindow;
import static FileEditPackage.FileEditScrollPane.*;
import static FileEditPackage.FileEditToolBar.*;
import static java.awt.event.InputEvent.CTRL_DOWN_MASK;

public class FileEditPanel {//幻灯片图片面板类
    public static final int SHOW_THRESHOLD = 60;//靠近边缘显示按钮阈值常量
    public static final int MAX_SLIDE_CACHE_REMAIN_AMOUNT = 20;//设置最大幻灯片缓存保留数量用于清理
    public static final int PANEL_DEFAULT_HEIGHT = Main.screenSize.height - 176;//面板默认高度常量
    public static final int PANEL_FULLSCREEN_HEIGHT = Main.screenSize.height;//面板全屏高度常量

    public static final JPanel picturePanel = new JPanel();//幻灯片主面板：用于放置图片
    public static final JButton previousButton = new TransparentButton();//上一张按钮
    public static final JButton nextButton = new TransparentButton();//下一张按钮

    public static SlideItem currentSlideItem = null;//当前幻灯片项目
    public static boolean isNotEdit = true;//是否播放幻灯片
    public static int imageHeight = Main.screenSize.height - 176;//图片高度
    public static int originalWidth;//图片原始宽度
    public static int originalHeight;//图片原始高度
    public static float initialScale;//初始缩放比例（用于判断是否放大图片）
    public static float currentScale;//当前缩放比例
    public static final Map<String, SoftReference<BufferedImage>> slideCache =//幻灯片缓存：使用LRU缓存加软引用策略
            new LinkedHashMap<>(MAX_SLIDE_CACHE_REMAIN_AMOUNT, 0.75f, true) {//设置哈希映射表最大容量为最大缓存容量
                @Override
                protected boolean removeEldestEntry(Map.Entry eldest) {//重新清除旧数据方法策略
                    return size() > MAX_SLIDE_CACHE_REMAIN_AMOUNT;//当容量比最大缓存缩略图数量大时
                }
            };

    public static class TransparentButton extends JButton {//自定义透明按钮

        public TransparentButton() {//构造方法
            setBorder(null);//无边框
            setVisible(false);//初始不可见
            setSize(24, 24);//设置大小
            setBackground(new Color(0, 0, 0, 0));//设置背景颜色
        }

        @Override
        protected void paintComponent(Graphics g) {//绘制组件清空背景
            super.paintComponent(g);//调用父类方法确保正确清除背景
            g.clearRect(0, 0, getWidth(), getHeight());//先进行清除
            g.setColor(Main.SettingState.themeColor ? DARK_PICTURE_MAIN_COLOR : LIGHT_PICTURE_MAIN_COLOR);//设置背景颜色
            g.fillRect(0, 0, getWidth(), getHeight());//重新填充需要展示的区域
            previousButton.setIcon(new ImageIcon(new ImageIcon(Main.SettingState.themeColor ? "src/material/image/darkThemePrevious.png" : "src/material/image/lightThemePrevious.png").getImage().getScaledInstance(previousButton.getWidth(), previousButton.getHeight(), Image.SCALE_DEFAULT)));//通过getScaledInstance使按钮适应图片大小
            nextButton.setIcon(new ImageIcon(new ImageIcon(Main.SettingState.themeColor ? "src/material/image/darkThemeNext.png" : "src/material/image/lightThemeNext.png").getImage().getScaledInstance(nextButton.getWidth(), nextButton.getHeight(), Image.SCALE_DEFAULT)));//通过getScaledInstance使按钮适应图片大小
            Icon icon = getIcon();//获取图标
            g.drawImage(((ImageIcon) icon).getImage(), (getWidth() - icon.getIconWidth()) / 2, (getHeight() - icon.getIconHeight()) / 2, this);//绘制图标
        }
    }

    public FileEditPanel() {//构造方法
        picturePanel.setFocusable(true);//确保面板可获取焦点
        picturePanel.requestFocusInWindow();//初始化后直接获取焦点
        picturePanel.setDoubleBuffered(true);//使用双缓冲加速
        picturePanel.setLayout(new BorderLayout());//设置布局为BorderLayout以填满上方剩余空间
        if (Main.SettingState.windowState) {//如果全屏
            picturePanel.setPreferredSize(new Dimension(Main.screenSize.width, PANEL_FULLSCREEN_HEIGHT));//设置大小
        } else {//否则
            picturePanel.setPreferredSize(new Dimension(Main.screenSize.width, PANEL_DEFAULT_HEIGHT));//设置大小
        }
        picturePanel.setBackground(Main.SettingState.themeColor ? DARK_PICTURE_MAIN_COLOR : LIGHT_PICTURE_MAIN_COLOR);//设置背景颜色
        picturePanel.addMouseWheelListener(new MouseAdapter() {//为图片面板添加鼠标滚轮监听
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {//如果滚轮移动
                if (currentSlideItem.isNotGIF()) {//如果不是GIF
                    if (e.getWheelRotation() < 0) {//如果向上滚动
                        currentSlideItem.zoom(1.1f, e.getPoint());//放大
                    } else {//否则
                        currentSlideItem.zoom(0.9f, e.getPoint());//缩小
                    }
                    zoomSlider.setValue((int) (currentScale * 100));//设置值
                    zoomTextField.setText((int) (currentScale * 100) + "%");//设置文本
                    refreshPicturePanel();//刷新
                }
            }
        });
        initPicturePanelShortcuts();//启用图片面板快捷键并设置键盘按键绑定

        previousButton.addActionListener(_ -> handlePreviousPicture());//处理上一张图片
        previousButton.setIcon(new ImageIcon(new ImageIcon(Main.SettingState.themeColor ? "src/material/image/darkThemePrevious.png" : "src/material/image/lightThemePrevious.png").getImage().getScaledInstance(previousButton.getWidth(), previousButton.getHeight(), Image.SCALE_DEFAULT)));//通过getScaledInstance使按钮适应图片大小

        nextButton.addActionListener(_ -> handleNextPicture());//处理下一张图片
        nextButton.setIcon(new ImageIcon(new ImageIcon(Main.SettingState.themeColor ? "src/material/image/darkThemeNext.png" : "src/material/image/lightThemeNext.png").getImage().getScaledInstance(nextButton.getWidth(), nextButton.getHeight(), Image.SCALE_DEFAULT)));//通过getScaledInstance使按钮适应图片大小
    }

    public static void updatePicturePanel() {//更新图片面板
        picturePanel.removeAll();//清空图片面板
        FileDisplayMainPanel.ThumbnailItem thumbnailItem = FileDisplayMainPanel.thumbnailItemList.get(pictureIndex);//获取当前缩略图
        currentSlideItem = createSlideItem(thumbnailItem.getFile());//设置当前幻灯片项目为复制后的项目
        picturePanel.add(currentSlideItem);//当前幻灯片项目添加到图片面板中
        currentSlideItem.add(previousButton);//添加上一张图片按钮
        currentSlideItem.add(nextButton);//添加下一张图片按钮

        File file = currentSlideItem.getFile();//获取文件
        Main.editFrame.setTitle(file.getName());//设置标题为文件名
        informationLabel.setText(new StringBuilder().append(originalWidth).append("×").append(originalHeight).append(" - ").append(calculateItemHoverTipFileSize(file.length())).append(" - ").append(new SimpleDateFormat("yyyy-MM-dd").format(new Date(file.lastModified()))).append(" - ").append(pictureIndex + 1).append("/").append(scrollItemList.size()).toString());//设置信息标签文本

        if (currentSlideItem.isNotGIF()) {//如果不是GIF
            leftRotationButton.setEnabled(true);//有效
            rightRotationButton.setEnabled(true);
            zoomStrategyButton.setEnabled(true);
            shrinkButton.setEnabled(true);
            zoomSlider.setEnabled(true);
            magnifyButton.setEnabled(true);
            zoomTextField.setEnabled(true);
        } else {//否则
            leftRotationButton.setEnabled(false);//无效
            rightRotationButton.setEnabled(false);
            zoomStrategyButton.setEnabled(false);
            shrinkButton.setEnabled(false);
            zoomSlider.setEnabled(false);
            magnifyButton.setEnabled(false);
            zoomTextField.setEnabled(false);
        }

        refreshPicturePanel();//刷新图片面板
        refreshScrollPanePanel();//刷新滚动栏面板
        SwingUtilities.invokeLater(FileEditScrollPane::scrollToVisible);//滚动到可见区域
    }

    private static SlideItem createSlideItem(File file) {//创建幻灯片项目
        String cacheKey = file.getAbsolutePath();//缓存地址
        SoftReference<BufferedImage> cachedRef = slideCache.get(cacheKey);//优先从缓存获取（使用软引用）
        if (cachedRef != null) {//如果命中缓存
            BufferedImage original = cachedRef.get();//获取原始图像
            if (original != null) {//如果不为空
                originalWidth = original.getWidth();//获取原始宽度
                originalHeight = original.getHeight();//获取原始高度
                float ratio = (float) imageHeight / original.getHeight();//计算缩略图缩放
                BufferedImage scaled = new BufferedImage((int) (original.getWidth() * ratio), (int) (original.getHeight() * ratio), BufferedImage.TYPE_INT_RGB);//生成高质量缩略图
                Graphics2D g2d = scaled.createGraphics();//创建高质量缩放图像
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);//抗锯齿设置
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2d.drawImage(original, 0, 0, scaled.getWidth(), scaled.getHeight(), null);//绘制图像
                g2d.dispose();//释放
                return new SlideItem(scaled, file, ratio);//返回项目
            }
            return null;//返回空
        } else {//否则
            try (ImageInputStream input = ImageIO.createImageInputStream(file)) {//缓存未命中则生成缩略图
                ImageReader reader = ImageIO.getImageReaders(input).next();//读取图像
                reader.setInput(input);//设置读取出来的图像
                BufferedImage original = reader.read(0, reader.getDefaultReadParam());//获取原始图像
                slideCache.put(cacheKey, new SoftReference<>(original));//原始图片存入缓存
                originalWidth = original.getWidth();//获取原始宽度
                originalHeight = original.getHeight();//获取原始高度
                float ratio = (float) imageHeight / original.getHeight();//计算缩略图缩放
                BufferedImage scaledImage = new BufferedImage((int) (original.getWidth() * ratio), (int) (original.getHeight() * ratio), BufferedImage.TYPE_INT_RGB);//生成高质量缩略图
                Graphics2D g2d = scaledImage.createGraphics();//创建高质量缩放图像
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);//抗锯齿设置
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2d.drawImage(original, 0, 0, scaledImage.getWidth(), scaledImage.getHeight(), null);//绘制图像
                g2d.dispose();//释放
                return new SlideItem(scaledImage, file, ratio);//返回项目
            } catch (IOException e) {
                handleErrorLog(e.getMessage());//处理错误日志
                System.err.println("IOException:" + file.getName());//捕获异常
                return null;//返回空
            }
        }
    }

    private static void updateButtonVisibility(int x) {//更新按钮可见性
        if (scrollItemList.size() <= 1) {//如果图片数量<=1
            previousButton.setVisible(false);//一直不可见
            nextButton.setVisible(false);//一直不可见
            return;//直接返回
        }
        previousButton.setVisible(x <= SHOW_THRESHOLD);//如果鼠标x位置小于阈值则可见
        previousButton.setLocation(15, picturePanel.getHeight() / 2);//调整按钮位置
        nextButton.setVisible(x >= (picturePanel.getWidth() - SHOW_THRESHOLD));//如果鼠标x位置大于阈值则可见
        nextButton.setLocation(Main.screenSize.width - 39, picturePanel.getHeight() / 2);//调整按钮位置
    }

    public static void initPicturePanelShortcuts() {//启用图片面板快捷键并设置键盘按键绑定
        InputMap inputMap = picturePanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);//获取图片面板的输入映射
        ActionMap actionMap = picturePanel.getActionMap();//获取图片面板的行动映射

        bindKey(inputMap, actionMap, KeyEvent.VK_F, 0, "scrollPaneStrategy");//滚动栏策略
        bindKey(inputMap, actionMap, KeyEvent.VK_LEFT, CTRL_DOWN_MASK, "firstPicture");//第一张图片
        bindKey(inputMap, actionMap, KeyEvent.VK_UP, CTRL_DOWN_MASK, "firstPicture");//第一张图片
        bindKey(inputMap, actionMap, KeyEvent.VK_LEFT, 0, "previousPicture");//上一张图片
        bindKey(inputMap, actionMap, KeyEvent.VK_UP, 0, "previousPicture");//上一张图片
        bindKey(inputMap, actionMap, KeyEvent.VK_RIGHT, 0, "nextPicture");//下一张图片
        bindKey(inputMap, actionMap, KeyEvent.VK_DOWN, 0, "nextPicture");//下一张图片
        bindKey(inputMap, actionMap, KeyEvent.VK_RIGHT, CTRL_DOWN_MASK, "lastPicture");//最后一张图片
        bindKey(inputMap, actionMap, KeyEvent.VK_DOWN, CTRL_DOWN_MASK, "lastPicture");//最后一张图片
        bindKey(inputMap, actionMap, KeyEvent.VK_R, CTRL_DOWN_MASK, "leftRotation");//左旋转
        bindKey(inputMap, actionMap, KeyEvent.VK_T, CTRL_DOWN_MASK, "rightRotation");//右旋转
        bindKey(inputMap, actionMap, KeyEvent.VK_SPACE, 0, "autoPlay");//自动播放
        bindKey(inputMap, actionMap, KeyEvent.VK_1, CTRL_DOWN_MASK, "zoomToActual");//缩放到实际
        bindKey(inputMap, actionMap, KeyEvent.VK_0, CTRL_DOWN_MASK, "zoomToAdapt");//缩放以适应
        bindKey(inputMap, actionMap, KeyEvent.VK_MINUS, CTRL_DOWN_MASK, "shrinkPicture");//缩小图片
        bindKey(inputMap, actionMap, KeyEvent.VK_EQUALS, CTRL_DOWN_MASK, "magnifyPicture");//放大图片
        bindKey(inputMap, actionMap, KeyEvent.VK_I, CTRL_DOWN_MASK, "inputZoomRatio");//输入缩放比例
    }

    public static void refreshPicturePanel() {//刷新图片面板
        picturePanel.revalidate();//重新验证布局
        picturePanel.repaint();//重新绘制
    }

    public static class SlideItem extends JComponent {//幻灯片项目类（继承JComponent）
        private final File file;//文件
        private static BufferedImage fileImage;//文件图像（静态保证数据一致性）

        private boolean isGIF = false;//文件是否是GIF
        private int currentGIFFrame = 0;//当前帧数
        private int[] frameDelays;//帧延迟数组（每个元素存储这一帧需要多少延迟）
        private Timer GIFAnimationTimer;//GIF动画计时器
        private List<BufferedImage> GIFFrames = new ArrayList<>();//GIF帧缓存图片列表

        private boolean dragging = false;//是否正在拖动
        private final Point dragStartPoint = new Point();//拖动起点坐标：用于拖动图片
        private final Point dragOffset = new Point(0, 0);//图片偏移量
        private int rotationAngle = 0;//当前旋转角度（初始为0度）
        private final Timer dbclickTimer = new Timer(500, e -> ((Timer) e.getSource()).stop());//双击计时器

        public File getFile() {//获取文件
            return file;
        }

        public boolean isNotGIF() {//获取是否是GIF
            return !isGIF;
        }

        public SlideItem(BufferedImage fileImage, File file, float scale) {//构造方法：幻灯片主面板创建缩略图项目
            this.file = file;
            SlideItem.fileImage = fileImage;
            currentScale = scale;
            initialScale = scale;
            zoomSlider.setValue((int) (currentScale * 100));//设置值
            zoomTextField.setText((int) (currentScale * 100) + "%");//设置文本
            dragOffset.setLocation((Main.screenSize.width - fileImage.getWidth()) / 2, 0);//初始化偏移为居中位置

            setPreferredSize(new Dimension(Main.screenSize.width, fileImage.getHeight()));//设置组件大小（宽度和图片一致，高度为图片高度加上文本高度）

            addMouseListener(new MouseAdapter() {//添加鼠标监听
                @Override
                public void mouseClicked(MouseEvent e) {//如果鼠标按下
                    if (dbclickTimer.isRunning()) {//如果计时器正在进行
                        dbclickTimer.stop();//关闭计时器
                        if (isZoomToActual) {//如果缩放到实际
                            isZoomToActual = false;//缩放以适应
                            currentScale = initialScale;//直接设置缩放倍率为原始倍率
                            zoomSlider.setValue((int) (currentScale * 100));//设置值
                            zoomTextField.setText((int) (currentScale * 100) + "%");//设置文本
                            currentSlideItem.zoom(0.0f, null);//缩放
                            refreshPicturePanel();//刷新
                            zoomStrategyButton.setIcon(new ImageIcon("src/material/image/zoomToActual.png"));//设置图标为缩放到实际
                            hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Scale Picture To Actual Size (Ctrl + 1)" : "缩放图片到实际大小（Ctrl + 1）", zoomStrategyButton));//展示提示窗口（鼠标悬浮一秒后展示）
                        } else {//否则
                            isZoomToActual = true;//缩放到实际
                            zoom(1.0f / initialScale, e.getPoint());//先进行缩放
                            currentScale = 1.0f;//直接设置缩放倍率为100%
                            zoomSlider.setValue((int) (currentScale * 100));//设置值
                            zoomTextField.setText((int) (currentScale * 100) + "%");//设置文本
                            refreshPicturePanel();//刷新
                            zoomStrategyButton.setIcon(new ImageIcon("src/material/image/zoomToAdapt.png"));//设置图标为缩放以适应
                            hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Scale Picture To Fit (Ctrl + 0)" : "缩放图片以适应（Ctrl + 0）", zoomStrategyButton));//展示提示窗口（鼠标悬浮一秒后展示）
                        }
                    } else {//否则
                        dbclickTimer.restart();//重新开始计时器
                    }
                }

                @Override
                public void mousePressed(MouseEvent e) {//如果鼠标按下
                    if (SlideItem.fileImage.getWidth() > getPreferredSize().width || SlideItem.fileImage.getHeight() > getPreferredSize().height) {//只有放大后才能拖动
                        dragStartPoint.setLocation(e.getX(), e.getY());//设置起点
                        dragging = true;//开始拖动
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {//如果鼠标松开
                    setCursor(Cursor.getDefaultCursor());//设置光标为默认
                    dragging = false;//停止拖动
                }

                @Override
                public void mouseExited(MouseEvent e) {//如果鼠标离开
                    if (previousButton.isVisible()) {//如果是上一张图片按钮可见
                        Rectangle previousBounds = new Rectangle(previousButton.getLocationOnScreen(), previousButton.getSize());//计算上一张图片按钮矩形
                        if (!previousBounds.contains(e.getLocationOnScreen())) {//如果不在范围
                            previousButton.setVisible(false);//不可见
                        }
                    } else if (nextButton.isVisible()) {//如果是下一张图片按钮可见
                        Rectangle nextBounds = new Rectangle(nextButton.getLocationOnScreen(), nextButton.getSize());//计算下一张图片按钮矩形
                        if (!nextBounds.contains(e.getLocationOnScreen())) {//如果不在范围
                            nextButton.setVisible(false);//不可见
                        }
                    }
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {//添加鼠标动作监听
                @Override
                public void mouseDragged(MouseEvent e) {//如果鼠标拖动
                    if (dragging) {//如果正在拖动且缩放比例大于原始比例
                        setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));//设置光标为抓取
                        dragOffset.translate(e.getX() - dragStartPoint.x, e.getY() - dragStartPoint.y);//设置偏移平移到鼠标位置-起点位置
                        constrainOffset();//限制偏移
                        repaint();//重新绘制
                        dragStartPoint.setLocation(e.getX(), e.getY());//更新起点为当前鼠标位置
                    }
                }

                @Override
                public void mouseMoved(MouseEvent e) {//如果鼠标移动
                    updateButtonVisibility(e.getX());//更新按钮可见性
                }
            });
        }

        private void loadGIFFrame(File GIFFile) {//加载GIF帧
            try (ImageInputStream input = ImageIO.createImageInputStream(GIFFile)) {//读取GIF文件
                Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("GIF");//创建GIF读入者
                if (readers.hasNext()) {//如果读入者有当前元素
                    ImageReader reader = readers.next();//获取元素
                    reader.setInput(input);//设置输入为GIF文件
                    int numFrames = reader.getNumImages(true);//获取帧数量：完全加载
                    GIFFrames = new ArrayList<>(numFrames);//通过帧数量创建总容量为帧数量的GIF图片帧列表
                    frameDelays = new int[numFrames];//通过帧数量创建总容量为帧数量的帧延迟数组
                    Map<String, Integer> disposalMethodMap = new HashMap<>();//创建处置方法映射表
                    disposalMethodMap.put("none", 0);//当none时返回0
                    disposalMethodMap.put("doNotDispose", 1);//当doNotDispose返回1
                    disposalMethodMap.put("restoreToBackgroundColor", 2);//当restoreToBackgroundColor返回2
                    disposalMethodMap.put("restoreToPrevious", 3);//当restoreToPrevious返回3
                    int[] disposalMethods = new int[numFrames];//通过帧数创建处置方法数组
                    BufferedImage canvas = null;//创建缓存图片画布

                    for (int i = 0; i < numFrames; i++) {//遍历帧数
                        IIOMetadata metadata = reader.getImageMetadata(i);//读取帧延迟时间元数据
                        Node root = metadata.getAsTree("javax_imageio_gif_image_1.0");//将元数据以树形式创建根结点
                        NodeList children = root.getChildNodes();//获取根结点的子结点列表
                        int delayTime = 0;//初始延迟时间为0
                        int disposalMethod = 0;//初始处置方法默认为0
                        for (int j = 0; j < children.getLength(); j++) {//遍历子结点列表
                            Node node = children.item(j);//获取当前子结点
                            if (node.getNodeName().equals("GraphicControlExtension")) {//如果子结点的名称等于GraphicControlExtension
                                NamedNodeMap attrs = node.getAttributes();//获取结点属性
                                String disposalMethodStr = attrs.getNamedItem("disposalMethod").getNodeValue();//获取处置方法字符串
                                delayTime = Integer.parseInt(attrs.getNamedItem("delayTime").getNodeValue());//获取处置时间
                                disposalMethod = disposalMethodMap.getOrDefault(disposalMethodStr, 0);//获取处置方法
                            }
                        }
                        BufferedImage frame = reader.read(i);//读取帧图像
                        if (canvas == null) {//如果为空
                            canvas = new BufferedImage(frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_INT_ARGB);//就创建画布图像
                        }
                        Graphics2D g2d = canvas.createGraphics();//创建工具类
                        applyGIFDisposalMethod(g2d, disposalMethod, i, canvas);//应用处置方法
                        g2d.drawImage(frame, 0, 0, null);//绘制图像
                        g2d.dispose();//释放
                        BufferedImage copy = new BufferedImage(fileImage.getWidth(), imageHeight, BufferedImage.TYPE_INT_ARGB);//保存合成后的帧
                        Graphics2D copyG2d = copy.createGraphics();//创建保存工具类
                        copyG2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);//抗锯齿
                        copyG2d.drawImage(canvas, 0, 0, fileImage.getWidth(), imageHeight, null);//绘制画布图像并绘制为基础宽度和计算后高度
                        copyG2d.dispose();//释放
                        GIFFrames.add(copy);//把帧图像添加到列表中
                        frameDelays[i] = delayTime * 10;//将延迟转换为毫秒单位放入帧延迟数组
                        disposalMethods[i] = disposalMethod;//处置方法数组i的位置设置处置方法
                    }
                    reader.dispose();//释放读入者
                }
            } catch (IOException | NumberFormatException e) {
                handleErrorLog(e.getMessage());//处理错误日志
                e.printStackTrace();//捕获异常
                GIFFrames.clear();//清除帧列表
            }
        }

        private void startGIFAnimation() {//启动GIF动画
            if (GIFAnimationTimer == null && !GIFFrames.isEmpty()) {//如果计时器为空且帧列表不为空
                GIFAnimationTimer = new Timer(frameDelays[0], _ -> {//开启计时器，第一次延迟为帧延迟列表的第一个元素
                    Rectangle compBounds = getBounds();//获取组件在滚动面板中的可见性
                    compBounds.setLocation(getLocation());//设置位置
                    currentGIFFrame = (currentGIFFrame + 1) % GIFFrames.size();//当前帧循环（在帧大小范围内）
                    if (currentGIFFrame < frameDelays.length) {//如果当前帧小于帧延迟的长度
                        GIFAnimationTimer.setDelay(frameDelays[currentGIFFrame]);//设置计时器延迟为下一帧延迟
                    }
                    repaint();//重绘
                });
                GIFAnimationTimer.setInitialDelay(0);//设置计时器初始延迟为0
                GIFAnimationTimer.start();//开启计时器
            }
        }

        public void stopGIFAnimation() {//停止GIF动画并释放资源
            if (GIFAnimationTimer != null) {//如果GIF动画计时器不为空
                GIFAnimationTimer.stop();//停止GIF动画计时器
                GIFAnimationTimer = null;//GIF动画计时器置空
            }
            if (!GIFFrames.isEmpty()) {//如果GIF帧列表不为空
                GIFFrames.forEach(BufferedImage::flush);//就遍历每一帧进行清除
                GIFFrames.clear();//再清空列表
            }
        }

        private void applyGIFDisposalMethod(Graphics2D g2d, int method, int frameIndex, BufferedImage canvas) {//应用GIF处置方法
            switch (method) {//根据处置方法选择
                case 1://保持当前帧（不做处理）
                    break;
                case 2://恢复背景色
                    g2d.setBackground(new Color(0, 0, 0, 0));//设置背景颜色透明
                    g2d.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());//清除绘制
                    break;
                case 3://恢复前一帧
                    if (frameIndex > 0) {//如果帧索引不为0
                        g2d.drawImage(GIFFrames.get(frameIndex - 1), 0, 0, null);//就绘制上一帧
                    }
                    break;
            }
        }

        public void zoom(float factor, Point mousePosition) {//缩放
            if (factor != 0.0f) {//如果不是直接设置缩放倍率
                float newScale = currentScale * factor;//计算新倍率
                if (newScale > 4.0f) {//如果超过最大倍率
                    currentScale = 4.0f;//直接设为最大
                } else {//否则
                    currentScale = Math.max(newScale, 0.01f);//判断是否超过最小倍率
                }
            }

            SwingWorker<Void, Void> worker = new SwingWorker<>() {//异步执行缩放
                @Override
                protected Void doInBackground() {//在后台线程生成缩放后的图像
                    fileImage = scaleImage(Objects.requireNonNull(slideCache.get(file.getAbsolutePath()).get()), currentScale);//从缓存中获取原始图像
                    if (mousePosition == null) {//如果是按钮缩放
                        dragOffset.setLocation((getWidth() - fileImage.getWidth()) / 2, (getHeight() - fileImage.getHeight()) / 2);//计算偏移为居中位置
                    } else {//否则是滚轮缩放
                        dragOffset.setLocation((int) (dragOffset.x + (mousePosition.x - dragOffset.x) * (1 - factor)), (int) (dragOffset.y + (mousePosition.y - dragOffset.y) * (1 - factor)));//计算偏移为鼠标在图片上位置
                        constrainOffset();//限制偏移
                    }
                    return null;//返回空
                }

                @Override
                protected void done() {//完成时
                    revalidate();//重新验证布局
                    repaint();//重新绘制
                }
            };
            worker.execute();//开始执行
        }

        private BufferedImage scaleImage(BufferedImage original, float scale) {//缩放图片
            int width = (int) (original.getWidth() * scale);//获取宽度
            int height = (int) (original.getHeight() * scale);//获取高度
            BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);//创建缩放图片
            Graphics2D g2d = scaled.createGraphics();//创建绘制工具类
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);//设置双三次插值算法
            g2d.drawImage(original, 0, 0, width, height, null);//绘制图片
            g2d.dispose();//释放
            return scaled;//返回缩放后图片
        }

        private void constrainOffset() {//限制偏移
            int imageWidth = fileImage.getWidth();//图片宽度
            int imageHeight = fileImage.getHeight();//图片高度
            int componentWidth = getWidth();//组件宽度
            int componentHeight = getHeight();//组件高度

            if (imageWidth > componentWidth) {//X轴约束：如果图片宽度大于组件宽度
                dragOffset.x = Math.max(dragOffset.x, componentWidth - imageWidth);//限制最大x值
                dragOffset.x = Math.min(dragOffset.x, 0);//限制最小x值
            } else {//否则
                dragOffset.x = (componentWidth - imageWidth) / 2;//图片宽度不足时强制居中
            }

            if (imageHeight > componentHeight) {//Y轴约束：如果图片高度大于组件高度
                dragOffset.y = Math.max(dragOffset.y, componentHeight - imageHeight);//限制最大y值
                dragOffset.y = Math.min(dragOffset.y, 0);//限制最小y值
            } else {//否则
                dragOffset.y = (componentHeight - imageHeight) / 2;//图片高度不足时强制居中
            }
        }

        public void rotate(int direction) {//旋转：direction：1为顺时针，-1为逆时针
            rotationAngle += direction * 90;//更新旋转角度
            rotationAngle %= 360;//取模
            if (rotationAngle < 0) {//如果小于0
                rotationAngle += 360;//重新加上360
            }

            if (currentScale != initialScale) {//如果不是初始缩放比例
                currentScale = initialScale;//重置缩放比例到初始值
                zoomSlider.setValue((int) (currentScale * 100));//设置值
                zoomTextField.setText((int) (currentScale * 100) + "%");//设置文本
                fileImage = scaleImage(Objects.requireNonNull(slideCache.get(file.getAbsolutePath()).get()), currentScale);//从缓存中获取原始图像
            }

            BufferedImage rotated = rotateImage(Objects.requireNonNull(slideCache.get(file.getAbsolutePath()).get()), direction);//更新原始图片为旋转后的结果
            slideCache.put(file.getAbsolutePath(), new SoftReference<>(rotated));//旋转后原始图片存入缓存
            float ratio = (float) imageHeight / rotated.getHeight();//计算缩略图新缩放
            fileImage = scaleImage(rotated, ratio);//将旋转后原始图像缩放赋值给文件图像
            initialScale = ratio;//修改原始缩放为新缩放

            dragOffset.setLocation((Main.screenSize.width - fileImage.getWidth()) / 2, 0);//初始化偏移为居中位置
            revalidate();//重新验证布局
            repaint();//重新绘制
        }

        private BufferedImage rotateImage(BufferedImage original, int direction) {//旋转图片
            double radians = Math.toRadians(direction == 1 ? 90 : -90);//计算旋转弧度
            double sin = Math.abs(Math.sin(radians));//计算sin取绝对值
            double cos = Math.abs(Math.cos(radians));//计算cos取绝对值

            int newWidth = (int) Math.floor(original.getWidth() * cos + original.getHeight() * sin);//计算旋转后的新宽度
            int newHeight = (int) Math.floor(original.getWidth() * sin + original.getHeight() * cos);//计算旋转后的新高度

            BufferedImage rotated = new BufferedImage(newWidth, newHeight, original.getType());//通过新宽度和新高度创建旋转图片
            Graphics2D g2d = rotated.createGraphics();//创建g2d工具
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);//设置抗锯齿和插值
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

            g2d.translate(newWidth / 2, newHeight / 2);//平移至中心
            g2d.rotate(radians, 0, 0);//旋转
            g2d.translate(-original.getWidth() / 2, -original.getHeight() / 2);//再平移回去
            g2d.drawImage(original, 0, 0, original.getWidth(), original.getHeight(), null);//绘制图像
            g2d.dispose();//释放
            return rotated;//返回旋转后图像
        }

        @Override
        public void setVisible(boolean visible) {//设置是否可见
            super.setVisible(visible);//调用父类设置是否可见
            if (visible && isGIF) {//如果可见且是GIF
                startGIFAnimation();//开启GIF动画
            } else {//否则
                stopGIFAnimation();//停止GIF动画
                if (GIFFrames.size() > 1) {//如果GIF帧数>1
                    GIFFrames.subList(1, GIFFrames.size()).clear();//仅保留第一帧数据
                }
            }
        }

        @Override
        public void removeNotify() {//释放资源
            if (getParent() == null) {//只有当组件未被任何容器使用时才释放
                dispose();//先执行清理
                if (fileImage != null) {//如果图像非空
                    fileImage.flush();//释放图像
                    fileImage = null;//图像置空
                }
                if (GIFFrames != null) {//如果gif帧非空
                    GIFFrames.forEach(BufferedImage::flush);//遍历清空
                    GIFFrames.clear();//清空gif帧
                }
                super.removeNotify();//调用父类清除
            }
        }

        public void dispose() {//进行释放时
            stopGIFAnimation();//停止动画
            if (fileImage != null) {//如果文件图片不为空
                fileImage.flush();//清空文件图片
                fileImage = null;//文件图片置空
            }
            if (GIFFrames != null) {//如果gif帧非空
                GIFFrames.forEach(BufferedImage::flush);//遍历清空
                GIFFrames.clear();//清空gif帧
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
                if (isGIF && !GIFFrames.isEmpty()) {//如果是GIF且GIF列表非空（GIF优先）
                    g2d.drawImage(GIFFrames.get(currentGIFFrame), dragOffset.x, dragOffset.y, null);//绘制当前帧缓存图片
                } else if (fileImage != null) {//否则非GIF，如果文件图片非空
                    g2d.drawImage(fileImage, dragOffset.x, dragOffset.y, null);//绘制图像
                }
            } finally {//不管有没有创建成功都执行
                g2d.dispose();//释放图像资源
            }
        }
    }
}
