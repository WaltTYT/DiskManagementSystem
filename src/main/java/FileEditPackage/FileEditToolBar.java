package FileEditPackage;

import MainPackage.Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import static DirectoryPackage.DirectoryTree.createBottomTipWindow;
import static MainPackage.Setting.switchPictureClip;
import static MainPackage.ThemeColor.*;
import static FileDisplayPackage.FileDisplayTopBar.*;
import static FileEditPackage.FileEditPanel.*;
import static FileEditPackage.FileEditScrollPane.*;
import static java.awt.Font.PLAIN;

public class FileEditToolBar {//幻灯片工具栏类
    public static final JPanel toolBarPanel = new JPanel(new BorderLayout());//工具栏面板
    public static final JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));//左面板
    public static final JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));//中面板
    public static final JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));//右面板

    public static final JButton scrollPaneStrategyButton = new JButton();//滚动栏策略按钮
    public static final JLabel informationLabel = new JLabel();//信息标签

    public static final JButton firstButton = new JButton();//第一张图片按钮
    public static final JButton previousButton = new JButton();//上一张图片按钮
    public static final JButton autoPlayButton = new JButton();//自动播放图片按钮
    public static final JButton nextButton = new JButton();//下一张图片按钮
    public static final JButton lastButton = new JButton();//最后一张图片按钮

    public static final JButton zoomStrategyButton = new JButton();//缩放策略按钮
    public static final JButton shrinkButton = new JButton();//缩小图片按钮
    public static final JSlider zoomSlider = new JSlider(JSlider.HORIZONTAL, 1, 400, 33);//缩放拖动条
    public static final JButton magnifyButton = new JButton();//放大图片按钮
    public static final JTextField zoomTextField = new JTextField();//缩放文本域
    public static final JButton leftRotationButton = new JButton();//左旋转图片按钮
    public static final JButton rightRotationButton = new JButton();//右旋转图片按钮
    public static final JButton fullScreenButton = new JButton();//全屏播放图片按钮

    public static boolean isScrollPaneHide = false;//滚动栏是否隐藏
    public static boolean isAutoPlay = false;//是否自动播放
    public static boolean isZoomToActual = false;//是否缩放到实际大小
    public static final Timer autoPlayTimer = new Timer(2000, _ -> {//自动播放计时器：2s切换图片
        pictureIndex++;//更新索引
        if (pictureIndex >= scrollItemList.size()) {//如果大于
            pictureIndex -= scrollItemList.size();//减去
        }
        updatePicturePanel();//更新图片面板
    });

    public FileEditToolBar() {//构造方法
        scrollPaneStrategyButton.setSize(24, 24);//设置大小
        informationLabel.setSize(24, 24);//设置大小
        firstButton.setSize(24, 24);//设置大小
        previousButton.setSize(24, 24);//设置大小
        autoPlayButton.setSize(24, 24);//设置大小
        nextButton.setSize(24, 24);//设置大小
        lastButton.setSize(24, 24);//设置大小
        zoomStrategyButton.setSize(24, 24);//设置大小
        shrinkButton.setSize(24, 24);//设置大小
        magnifyButton.setSize(24, 24);//设置大小
        zoomTextField.setPreferredSize(new Dimension(46, 34));//设置大小
        leftRotationButton.setSize(24, 24);//设置大小
        rightRotationButton.setSize(24, 24);//设置大小
        fullScreenButton.setSize(24, 24);//设置大小

        scrollPaneStrategyButton.setFocusable(false);//设置不可聚焦
        firstButton.setFocusable(false);//设置不可聚焦
        previousButton.setFocusable(false);//设置不可聚焦
        autoPlayButton.setFocusable(false);//设置不可聚焦
        nextButton.setFocusable(false);//设置不可聚焦
        lastButton.setFocusable(false);//设置不可聚焦
        zoomStrategyButton.setFocusable(false);//设置不可聚焦
        shrinkButton.setFocusable(false);//设置不可聚焦
        magnifyButton.setFocusable(false);//设置不可聚焦
        leftRotationButton.setFocusable(false);//设置不可聚焦
        rightRotationButton.setFocusable(false);//设置不可聚焦
        fullScreenButton.setFocusable(false);//设置不可聚焦

        scrollPaneStrategyButton.setIcon(new ImageIcon(new ImageIcon("src/material/image/showScrollPane.png").getImage().getScaledInstance(scrollPaneStrategyButton.getWidth(), scrollPaneStrategyButton.getHeight(), Image.SCALE_DEFAULT)));//通过getScaledInstance使按钮适应图片大小
        firstButton.setIcon(new ImageIcon(new ImageIcon("src/material/image/first.png").getImage().getScaledInstance(firstButton.getWidth(), firstButton.getHeight(), Image.SCALE_DEFAULT)));
        previousButton.setIcon(new ImageIcon(new ImageIcon("src/material/image/retreat.png").getImage().getScaledInstance(previousButton.getWidth(), previousButton.getHeight(), Image.SCALE_DEFAULT)));
        autoPlayButton.setIcon(new ImageIcon(new ImageIcon("src/material/image/play.png").getImage().getScaledInstance(autoPlayButton.getWidth(), autoPlayButton.getHeight(), Image.SCALE_DEFAULT)));
        nextButton.setIcon(new ImageIcon(new ImageIcon("src/material/image/advance.png").getImage().getScaledInstance(nextButton.getWidth(), nextButton.getHeight(), Image.SCALE_DEFAULT)));
        lastButton.setIcon(new ImageIcon(new ImageIcon("src/material/image/last.png").getImage().getScaledInstance(lastButton.getWidth(), lastButton.getHeight(), Image.SCALE_DEFAULT)));
        zoomStrategyButton.setIcon(new ImageIcon(new ImageIcon("src/material/image/zoomToActual.png").getImage().getScaledInstance(zoomStrategyButton.getWidth(), zoomStrategyButton.getHeight(), Image.SCALE_DEFAULT)));
        shrinkButton.setIcon(new ImageIcon(new ImageIcon("src/material/image/shrink.png").getImage().getScaledInstance(shrinkButton.getWidth(), shrinkButton.getHeight(), Image.SCALE_DEFAULT)));
        magnifyButton.setIcon(new ImageIcon(new ImageIcon("src/material/image/magnify.png").getImage().getScaledInstance(magnifyButton.getWidth(), magnifyButton.getHeight(), Image.SCALE_DEFAULT)));
        leftRotationButton.setIcon(new ImageIcon(new ImageIcon("src/material/image/leftRotate.png").getImage().getScaledInstance(leftRotationButton.getWidth(), leftRotationButton.getHeight(), Image.SCALE_DEFAULT)));
        rightRotationButton.setIcon(new ImageIcon(new ImageIcon("src/material/image/rightRotate.png").getImage().getScaledInstance(rightRotationButton.getWidth(), rightRotationButton.getHeight(), Image.SCALE_DEFAULT)));
        fullScreenButton.setIcon(new ImageIcon(new ImageIcon("src/material/image/fullScreen.png").getImage().getScaledInstance(fullScreenButton.getWidth(), fullScreenButton.getHeight(), Image.SCALE_DEFAULT)));

        informationLabel.setForeground(Main.SettingState.themeColor ? DARK_PICTURE_FONT_COLOR : LIGHT_PICTURE_FONT_COLOR);//设置字体颜色
        scrollPaneStrategyButton.setBackground(SLIDE_BUTTON_COLOR);//设置背景颜色
        firstButton.setBackground(SLIDE_BUTTON_COLOR);
        previousButton.setBackground(SLIDE_BUTTON_COLOR);
        autoPlayButton.setBackground(SLIDE_BUTTON_COLOR);
        nextButton.setBackground(SLIDE_BUTTON_COLOR);
        lastButton.setBackground(SLIDE_BUTTON_COLOR);
        zoomStrategyButton.setBackground(SLIDE_BUTTON_COLOR);
        shrinkButton.setBackground(SLIDE_BUTTON_COLOR);
        magnifyButton.setBackground(SLIDE_BUTTON_COLOR);
        leftRotationButton.setBackground(SLIDE_BUTTON_COLOR);
        rightRotationButton.setBackground(SLIDE_BUTTON_COLOR);
        fullScreenButton.setBackground(SLIDE_BUTTON_COLOR);
        zoomSlider.setForeground(SLIDE_BUTTON_COLOR);
        zoomSlider.setBackground(Main.SettingState.themeColor ? DARK_PICTURE_BAR_COLOR : LIGHT_PICTURE_BAR_COLOR);

        informationLabel.setFont(new Font("楷体", PLAIN, 20));//设置字体
        zoomTextField.setFont(new Font("楷体", PLAIN, 20));//设置字体

        leftPanel.add(scrollPaneStrategyButton);
        leftPanel.add(informationLabel);
        leftPanel.setPreferredSize(new Dimension(Main.screenSize.width / 2 - 145, 34));//左面板固定宽度
        leftPanel.setBackground(Main.SettingState.themeColor ? DARK_PICTURE_BAR_COLOR : LIGHT_PICTURE_BAR_COLOR);//设置背景颜色

        centerPanel.add(firstButton);
        centerPanel.add(previousButton);
        centerPanel.add(autoPlayButton);
        centerPanel.add(nextButton);
        centerPanel.add(lastButton);
        centerPanel.setPreferredSize(new Dimension(290, 34));//中面板固定宽度
        centerPanel.setBackground(Main.SettingState.themeColor ? DARK_PICTURE_BAR_COLOR : LIGHT_PICTURE_BAR_COLOR);//设置背景颜色

        rightPanel.add(zoomStrategyButton);
        rightPanel.add(shrinkButton);
        rightPanel.add(zoomSlider);
        rightPanel.add(magnifyButton);
        rightPanel.add(zoomTextField);
        rightPanel.add(leftRotationButton);
        rightPanel.add(rightRotationButton);
        rightPanel.add(fullScreenButton);
        rightPanel.setPreferredSize(new Dimension(Main.screenSize.width / 2 - 145, 34));//右面板固定宽度
        rightPanel.setBackground(Main.SettingState.themeColor ? DARK_PICTURE_BAR_COLOR : LIGHT_PICTURE_BAR_COLOR);//设置背景颜色

        toolBarPanel.add(leftPanel, BorderLayout.WEST);//左面板添加到西部
        toolBarPanel.add(centerPanel, BorderLayout.CENTER);//中面板添加到中心
        toolBarPanel.add(rightPanel, BorderLayout.EAST);//右面板添加到东部
        toolBarPanel.setPreferredSize(new Dimension(Main.screenSize.width, 34));//设置大小
        toolBarPanel.setBackground(Main.SettingState.themeColor ? DARK_PICTURE_BAR_COLOR : LIGHT_PICTURE_BAR_COLOR);//设置背景颜色

        scrollPaneStrategyButton.addActionListener(_ -> handleScrollPaneStrategy());//处理滚动栏策略
        scrollPaneStrategyButton.addMouseListener(new MouseAdapter() {//为滚动栏策略按钮添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                if (isScrollPaneHide) {//如果隐藏滚动栏
                    hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Hide Picture Scroll Pane (F)" : "展示图片滚动栏（F）", scrollPaneStrategyButton));//展示提示窗口（鼠标悬浮一秒后展示）
                } else {//否则
                    hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Show Picture Scroll Pane (F)" : "隐藏图片滚动栏（F）", scrollPaneStrategyButton));//展示提示窗口（鼠标悬浮一秒后展示）
                }
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

        informationLabel.addMouseListener(new MouseAdapter() {//为信息标签添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Picture Resolution - Picture Size - Picture Modification Time - Current Picture / Entire Picture" : "图片分辨率 - 图片大小 - 图片修改时间 - 当前图片/全部图片", informationLabel));//展示提示窗口（鼠标悬浮一秒后展示）
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

        firstButton.addActionListener(_ -> handleFirstPicture());//处理第一张图片
        firstButton.addMouseListener(new MouseAdapter() {//为第一张图片按钮添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Advance To First Picture (Ctrl + ← / Ctrl + ↑)" : "前进到第一张图片（Ctrl + ← / Ctrl + ↑）", firstButton));//展示提示窗口（鼠标悬浮一秒后展示）
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

        previousButton.addActionListener(_ -> handlePreviousPicture());//处理上一张图片
        previousButton.addMouseListener(new MouseAdapter() {//为上一张图片按钮添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Previous Picture (← / ↑)" : "上一张图片（← / ↑）", previousButton));//展示提示窗口（鼠标悬浮一秒后展示）
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

        autoPlayButton.addActionListener(_ -> handleAutoPlay());//处理自动播放
        autoPlayButton.addMouseListener(new MouseAdapter() {//为自动播放图片按钮添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                if (isAutoPlay) {//如果正在自动播放
                    hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Pause Auto Play Slide (Space)" : "暂停自动播放幻灯片（空格）", autoPlayButton));//展示提示窗口（鼠标悬浮一秒后展示）
                } else {//否则
                    hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Start Auto Play Slide (Space)" : "开启自动播放幻灯片（空格）", autoPlayButton));//展示提示窗口（鼠标悬浮一秒后展示）
                }
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

        nextButton.addActionListener(_ -> handleNextPicture());//处理下一张图片
        nextButton.addMouseListener(new MouseAdapter() {//为下一张图片按钮添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Next Picture (→ / ↓)" : "下一张图片（→ / ↓）", nextButton));//展示提示窗口（鼠标悬浮一秒后展示）
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

        lastButton.addActionListener(_ -> handleLastPicture());//处理最后一张图片
        lastButton.addMouseListener(new MouseAdapter() {//为最后一张图片按钮添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Retreat To Last Picture (Ctrl + → / Ctrl + ↓)" : "后退到最后一张图片（Ctrl + → / Ctrl + ↓）", lastButton));//展示提示窗口（鼠标悬浮一秒后展示）
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

        zoomStrategyButton.addActionListener(_ -> handleZoomStrategy());//处理缩放策略
        zoomStrategyButton.addMouseListener(new MouseAdapter() {//为缩放策略按钮添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                if (isZoomToActual) {//如果缩放到实际
                    hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Scale Picture To Fit (Ctrl + 0)" : "缩放图片以适应（Ctrl + 0）", zoomStrategyButton));//展示提示窗口（鼠标悬浮一秒后展示）
                } else {//否则
                    hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Scale Picture To Actual Size (Ctrl + 1)" : "缩放图片到实际大小（Ctrl + 1）", zoomStrategyButton));//展示提示窗口（鼠标悬浮一秒后展示）
                }
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

        shrinkButton.addActionListener(_ -> handleShrinkPicture());//处理缩小图片
        shrinkButton.addMouseListener(new MouseAdapter() {//为缩小图片按钮添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Shrink Picture (Ctrl + -)" : "缩小图片（Ctrl + -）", shrinkButton));//展示提示窗口（鼠标悬浮一秒后展示）
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

        zoomSlider.addChangeListener(_ -> {//为缩放拖动条添加变化监听
            if (zoomSlider.hasFocus()) {//如果是通过缩放拖动条进行缩放
                currentScale = (float) zoomSlider.getValue() / 100;//直接设置缩放倍率
                zoomTextField.setText((int) (currentScale * 100) + "%");//设置文本
                currentSlideItem.zoom(0.0f, null);//缩放
                refreshPicturePanel();//刷新
            }
        });
        zoomSlider.addMouseListener(new MouseAdapter() {//为缩放拖动条添加鼠标监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Zoom Adjust (Mouse Wheel) " : "缩放调整（鼠标滚轮）", zoomSlider));//展示提示窗口（鼠标悬浮一秒后展示）
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

        magnifyButton.addActionListener(_ -> handleMagnifyPicture());//处理放大图片
        magnifyButton.addMouseListener(new MouseAdapter() {//为放大图片按钮添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Magnify Picture (Ctrl + +)" : "放大图片（Ctrl + +）", magnifyButton));//展示提示窗口（鼠标悬浮一秒后展示）
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

        zoomTextField.addKeyListener(new KeyAdapter() {//为缩放文本域添加键盘监听
            @Override
            public void keyPressed(KeyEvent e) {//如果键盘按下
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {//如果按下回车
                    String inputText = zoomTextField.getText();//获取输入文本
                    boolean legalFlag = true;//合法输入标志
                    if (inputText.isEmpty()) {//如果输入为空
                        zoomTextField.setText((int) (currentScale * 100) + "%");//返回原先数据
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "No Empty Input" : "输入不可为空");//提示
                        legalFlag = false;//非法
                    } else {//否则不为空
                        for (int i = 0; i < inputText.length(); i++) {//遍历输入文本
                            if (inputText.charAt(i) == '-' && i == 0) {//如果输入负数
                                zoomTextField.setText((int) (currentScale * 100) + "%");//返回原先数据
                                createBottomTipWindow(Main.SettingState.systemLanguage ? "No Negative" : "请勿输入负数");//提示
                                legalFlag = false;//非法
                                break;//直接结束
                            } else if (!(Character.isDigit(inputText.charAt(i)) || inputText.charAt(i) == '%' && i == inputText.length() - 1)) {//如果不是数字且不是最后一位的百分号
                                zoomTextField.setText((int) (currentScale * 100) + "%");//返回原先数据
                                createBottomTipWindow(Main.SettingState.systemLanguage ? "No Illegal Character" : "请勿输入非法字符");//提示
                                legalFlag = false;//非法
                                break;//直接结束
                            }
                        }
                    }
                    if (legalFlag) {//如果合法
                        float value;//缩放值
                        if (inputText.charAt(inputText.length() - 1) == '%') {//如果最后一位是百分号
                            value = Float.parseFloat(inputText.substring(0, inputText.length() - 1));//截断后设置缩放倍率
                            if (value < 1) {//如果值小于1
                                createBottomTipWindow(Main.SettingState.systemLanguage ? "Do Not Enter Number Less Than 1" : "请勿输入小于1的数");//提示
                                zoomTextField.setText((int) (currentScale * 100) + "%");//返回原先数据
                                return;//直接返回
                            } else if (value > 400) {//如果值大于400
                                createBottomTipWindow(Main.SettingState.systemLanguage ? "Do Not Enter Number More Than 400" : "请勿输入大于400的数");//提示
                                zoomTextField.setText((int) (currentScale * 100) + "%");//返回原先数据
                                return;//直接返回
                            }
                        } else {//否则
                            value = Float.parseFloat(inputText);//直接设置缩放倍率
                            if (value < 1) {//如果值小于1
                                createBottomTipWindow(Main.SettingState.systemLanguage ? "Do Not Enter Number Less Than 1" : "请勿输入小于1的数");//提示
                                zoomTextField.setText((int) (currentScale * 100) + "%");//返回原先数据
                                return;//直接返回
                            } else if (value > 400) {//如果值大于400
                                createBottomTipWindow(Main.SettingState.systemLanguage ? "Do Not Enter Number More Than 400" : "请勿输入大于400的数");//提示
                                zoomTextField.setText((int) (currentScale * 100) + "%");//返回原先数据
                                return;//直接返回
                            }
                            zoomTextField.setText(zoomTextField.getText() + "%");//手动添加%
                        }
                        currentScale = value / 100;//直接设置缩放倍率
                        currentSlideItem.zoom(0.0f, null);//缩放
                        refreshPicturePanel();//刷新
                        toolBarPanel.requestFocusInWindow();//返回聚焦
                    }
                    e.consume();//阻止默认行为
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {//如果按下ESC
                    toolBarPanel.requestFocusInWindow();//返回聚焦
                    e.consume();//阻止默认行为
                }
            }
        });
        zoomTextField.addFocusListener(new FocusAdapter() {//为缩放文本域添加聚焦监听
            @Override
            public void focusLost(FocusEvent e) {//如果失去聚焦
                String inputText = zoomTextField.getText();//获取输入文本
                boolean legalFlag = true;//合法输入标志
                if (inputText.isEmpty()) {//如果输入为空
                    zoomTextField.setText((int) (currentScale * 100) + "%");//返回原先数据
                    createBottomTipWindow(Main.SettingState.systemLanguage ? "No Empty Input" : "输入不可为空");//提示
                    legalFlag = false;//非法
                } else {//否则不为空
                    if (inputText.length() > 8) {//如果输入过多
                        zoomTextField.setText((int) (currentScale * 100) + "%");//返回原先数据
                        createBottomTipWindow(Main.SettingState.systemLanguage ? "Do Not Enter Too Much" : "请勿输入过多");//提示
                        return;//直接返回
                    }
                    for (int i = 0; i < inputText.length(); i++) {//遍历输入文本
                        if (inputText.charAt(i) == '-' && i == 0) {//如果输入负数
                            zoomTextField.setText((int) (currentScale * 100) + "%");//返回原先数据
                            createBottomTipWindow(Main.SettingState.systemLanguage ? "No Negative" : "请勿输入负数");//提示
                            legalFlag = false;//非法
                            break;//直接结束
                        } else if (!(Character.isDigit(inputText.charAt(i)) || inputText.charAt(i) == '%' && i == inputText.length() - 1)) {//如果不是数字且不是最后一位的百分号
                            zoomTextField.setText((int) (currentScale * 100) + "%");//返回原先数据
                            createBottomTipWindow(Main.SettingState.systemLanguage ? "No Illegal Character" : "请勿输入非法字符");//提示
                            legalFlag = false;//非法
                            break;//直接结束
                        }
                    }
                }
                if (legalFlag) {//如果合法
                    float value;//缩放值
                    if (inputText.charAt(inputText.length() - 1) == '%') {//如果最后一位是百分号
                        value = Float.parseFloat(inputText.substring(0, inputText.length() - 1));//截断后设置缩放倍率
                        if (value < 1) {//如果值小于1
                            createBottomTipWindow(Main.SettingState.systemLanguage ? "Do Not Enter Number Less Than 1" : "请勿输入小于1的数");//提示
                            zoomTextField.setText((int) (currentScale * 100) + "%");//返回原先数据
                            return;//直接返回
                        } else if (value > 400) {//如果值大于400
                            createBottomTipWindow(Main.SettingState.systemLanguage ? "Do Not Enter Number More Than 400" : "请勿输入大于400的数");//提示
                            zoomTextField.setText((int) (currentScale * 100) + "%");//返回原先数据
                            return;//直接返回
                        }
                    } else {//否则
                        value = Float.parseFloat(inputText);//直接设置缩放倍率
                        if (value < 1) {//如果值小于1
                            createBottomTipWindow(Main.SettingState.systemLanguage ? "Do Not Enter Number Less Than 1" : "请勿输入小于1的数");//提示
                            zoomTextField.setText((int) (currentScale * 100) + "%");//返回原先数据
                            return;//直接返回
                        } else if (value > 400) {//如果值大于400
                            createBottomTipWindow(Main.SettingState.systemLanguage ? "Do Not Enter Number More Than 400" : "请勿输入大于400的数");//提示
                            zoomTextField.setText((int) (currentScale * 100) + "%");//返回原先数据
                            return;//直接返回
                        }
                        zoomTextField.setText(zoomTextField.getText() + "%");//手动添加%
                    }
                    currentScale = value / 100;//直接设置缩放倍率
                    currentSlideItem.zoom(0.0f, null);//缩放
                    refreshPicturePanel();//刷新
                    toolBarPanel.requestFocusInWindow();//返回聚焦
                }
            }
        });
        zoomTextField.addMouseListener(new MouseAdapter() {//为缩放文本域添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow((Main.SettingState.systemLanguage ? "Enter Zoom Scale (Ctrl + I, Please Do Input Digit, Minimum Zoom Scale Is 1%, Maximum Is 400%)" : "输入缩放倍率（Ctrl + I 请勿输入非数字，缩放倍率最小为1%，最大为400%）"), zoomTextField));//展示提示窗口（鼠标悬浮一秒后展示）
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

        leftRotationButton.addActionListener(_ -> handleLeftRotation());//处理左旋转
        leftRotationButton.addMouseListener(new MouseAdapter() {//为左旋转图片按钮添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Rotate Picture Anti Clock Wise (Ctrl + R)" : "逆时针旋转图片（Ctrl + R）", leftRotationButton));//展示提示窗口（鼠标悬浮一秒后展示）
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

        rightRotationButton.addActionListener(_ -> handleRightRotation());//处理右旋转
        rightRotationButton.addMouseListener(new MouseAdapter() {//为右旋转图片按钮添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Rotate Picture Clock Wise (Ctrl + T)" : "顺时针旋转图片（Ctrl + T）", rightRotationButton));//展示提示窗口（鼠标悬浮一秒后展示）
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

        fullScreenButton.addActionListener(_ -> Main.openSlideFrameFullscreen());//开启幻灯片窗口全屏
        fullScreenButton.addMouseListener(new MouseAdapter() {//为全屏播放图片按钮添加鼠标事件监听
            @Override
            public void mouseEntered(MouseEvent e) {//如果鼠标进入
                hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Play Slide Fullscreen (F11)" : "全屏播放幻灯片（F11）", fullScreenButton));//展示提示窗口（鼠标悬浮一秒后展示）
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

    public static void handleScrollPaneStrategy() {//处理滚动栏策略
        if (hoverTimer != null) {//如果不为空
            hoverTimer.stop();//计时器结束
        }
        if (buttonHoverTipWindow != null) {//如果提示信息不为空
            buttonHoverTipWindow.dispose();//释放提示信息
            buttonHoverTipWindow = null;//提示信息置空
        }
        if (FileEditScrollPane.itemHoverTipWindow != null) {//如果提示信息不为空
            FileEditScrollPane.itemHoverTipWindow.dispose();//释放提示信息
            FileEditScrollPane.itemHoverTipWindow = null;//提示信息置空
        }
        if (isScrollPaneHide) {//如果隐藏滚动栏
            isScrollPaneHide = false;//展示滚动栏
            Main.editFrame.add(scrollPane);//添加滚动栏
            picturePanel.setPreferredSize(new Dimension(Main.screenSize.width, Main.SettingState.windowState ? PANEL_FULLSCREEN_HEIGHT : PANEL_DEFAULT_HEIGHT));//设置大小
            imageHeight -= 118;//更新图片高度
            updatePicturePanel();//更新图片面板
            Main.editFrame.revalidate();//重新验证布局
            Main.editFrame.repaint();//重新绘制
            scrollPaneStrategyButton.setIcon(new ImageIcon("src/material/image/showScrollPane.png"));//设置图标为展示滚动栏
            hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Hide Picture Scroll Pane (F)" : "隐藏图片滚动栏（F）", scrollPaneStrategyButton));//展示提示窗口（鼠标悬浮一秒后展示）
        } else {//否则
            isScrollPaneHide = true;//隐藏滚动栏
            Main.editFrame.remove(scrollPane);//移除滚动栏
            picturePanel.setPreferredSize(new Dimension(Main.screenSize.width, Main.SettingState.windowState ? PANEL_FULLSCREEN_HEIGHT + 118 : PANEL_DEFAULT_HEIGHT + 118));//设置大小
            imageHeight += 118;//更新图片高度
            updatePicturePanel();//更新图片面板
            Main.editFrame.revalidate();//重新验证布局
            Main.editFrame.repaint();//重新绘制
            scrollPaneStrategyButton.setIcon(new ImageIcon("src/material/image/hideScrollPane.png"));//设置图标为隐藏滚动栏
            hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Show Picture Scroll Pane (F)" : "展示图片滚动栏（F）", scrollPaneStrategyButton));//展示提示窗口（鼠标悬浮一秒后展示）
        }
        hoverTimer.setRepeats(false);//设置计时器不重复
        hoverTimer.start();//开始计时
    }

    public static void handleFirstPicture() {//处理第一张图片
        if (pictureIndex == 0) {//如果已经在第一张图片
            createBottomTipWindow(Main.SettingState.systemLanguage ? "Already The First Picture" : "已经是第一张图片");//提示
        } else {//否则
            if (!Main.SettingState.effectState && !Main.SettingState.masterState) {//如果没有关闭音效
                if (switchPictureClip.isRunning()) {//如果正在运行
                    switchPictureClip.stop();//停止
                }
                switchPictureClip.setFramePosition(0);//重置播放位置
                switchPictureClip.start();//开始播放音效
            }
            pictureIndex = 0;//更新索引
            updatePicturePanel();//更新图片面板
        }
    }

    public static void handlePreviousPicture() {//处理上一张图片
        if (!Main.SettingState.effectState && !Main.SettingState.masterState) {//如果没有关闭音效
            if (switchPictureClip.isRunning()) {//如果正在运行
                switchPictureClip.stop();//停止
            }
            switchPictureClip.setFramePosition(0);//重置播放位置
            switchPictureClip.start();//开始播放音效
        }
        pictureIndex--;//更新索引
        if (pictureIndex < 0) {//如果小于
            pictureIndex += scrollItemList.size();//加上
        }
        if (pictureIndex == 0) {//如果已经在第一张图片
            createBottomTipWindow(Main.SettingState.systemLanguage ? "Already The First Picture" : "已经是第一张图片");//提示
        }
        updatePicturePanel();//更新图片面板
    }

    public static void handleAutoPlay() {//处理自动播放
        if (hoverTimer != null) {//如果不为空
            hoverTimer.stop();//计时器结束
        }
        if (buttonHoverTipWindow != null) {//如果提示信息不为空
            buttonHoverTipWindow.dispose();//释放提示信息
            buttonHoverTipWindow = null;//提示信息置空
        }
        if (isAutoPlay) {//如果正在自动播放
            isAutoPlay = false;//暂停自动播放
            autoPlayTimer.stop();//停止计时器
            autoPlayButton.setIcon(new ImageIcon("src/material/image/play.png"));//设置图标为开启
            hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Start Auto Play Slide (Space)" : "开启自动播放幻灯片（空格）", autoPlayButton));//展示提示窗口（鼠标悬浮一秒后展示）
        } else {//否则
            isAutoPlay = true;//开启自动播放
            autoPlayTimer.start();//开始计时器
            autoPlayButton.setIcon(new ImageIcon("src/material/image/pause.png"));//设置图标为暂停
            hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Pause Auto Play Slide (Space)" : "暂停自动播放幻灯片（空格）", autoPlayButton));//展示提示窗口（鼠标悬浮一秒后展示）
        }
        hoverTimer.setRepeats(false);//设置计时器不重复
        hoverTimer.start();//开始计时
    }

    public static void handleNextPicture() {//处理下一张图片
        if (!Main.SettingState.effectState && !Main.SettingState.masterState) {//如果没有关闭音效
            if (switchPictureClip.isRunning()) {//如果正在运行
                switchPictureClip.stop();//停止
            }
            switchPictureClip.setFramePosition(0);//重置播放位置
            switchPictureClip.start();//开始播放音效
        }
        pictureIndex++;//更新索引
        if (pictureIndex >= scrollItemList.size()) {//如果大于
            pictureIndex -= scrollItemList.size();//减去
        }
        if (pictureIndex == scrollItemList.size() - 1) {//如果已经在最后一张图片
            createBottomTipWindow(Main.SettingState.systemLanguage ? "Already The Last Picture" : "已经是最后一张图片");//提示
        }
        updatePicturePanel();//更新图片面板
    }

    public static void handleLastPicture() {//处理最后一张图片
        if (pictureIndex == scrollItemList.size() - 1) {//如果已经在最后一张图片
            createBottomTipWindow(Main.SettingState.systemLanguage ? "Already The Last Picture" : "已经是最后一张图片");//提示
        } else {//否则
            if (!Main.SettingState.effectState && !Main.SettingState.masterState) {//如果没有关闭音效
                if (switchPictureClip.isRunning()) {//如果正在运行
                    switchPictureClip.stop();//停止
                }
                switchPictureClip.setFramePosition(0);//重置播放位置
                switchPictureClip.start();//开始播放音效
            }
            pictureIndex = scrollItemList.size() - 1;//更新索引
            updatePicturePanel();//更新图片面板
        }
    }

    public static void handleZoomStrategy() {//处理缩放策略
        if (hoverTimer != null) {//如果不为空
            hoverTimer.stop();//计时器结束
        }
        if (buttonHoverTipWindow != null) {//如果提示信息不为空
            buttonHoverTipWindow.dispose();//释放提示信息
            buttonHoverTipWindow = null;//提示信息置空
        }
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
            currentScale = 1.0f;//直接设置缩放倍率为100%
            zoomSlider.setValue((int) (currentScale * 100));//设置值
            zoomTextField.setText((int) (currentScale * 100) + "%");//设置文本
            currentSlideItem.zoom(0.0f, null);//缩放
            refreshPicturePanel();//刷新
            zoomStrategyButton.setIcon(new ImageIcon("src/material/image/zoomToAdapt.png"));//设置图标为缩放以适应
            hoverTimer = new Timer(1000, _ -> showButtonHoverTipWindow(Main.SettingState.systemLanguage ? "Scale Picture To Fit (Ctrl + 0)" : "缩放图片以适应（Ctrl + 0）", zoomStrategyButton));//展示提示窗口（鼠标悬浮一秒后展示）
        }
        hoverTimer.setRepeats(false);//设置计时器不重复
        hoverTimer.start();//开始计时
    }

    public static void handleShrinkPicture() {//处理缩小图片
        currentSlideItem.zoom(0.8f, null);//缩放
        zoomSlider.setValue((int) (currentScale * 100));//设置值
        zoomTextField.setText((int) (currentScale * 100) + "%");//设置文本
        refreshPicturePanel();//刷新
    }

    public static void handleMagnifyPicture() {//处理放大图片
        currentSlideItem.zoom(1.25f, null);//缩放
        zoomSlider.setValue((int) (currentScale * 100));//设置值
        zoomTextField.setText((int) (currentScale * 100) + "%");//设置文本
        refreshPicturePanel();//刷新
    }

    public static void handleLeftRotation() {//处理左旋转
        if (currentSlideItem.isNotGIF()) {//如果不是GIF
            currentSlideItem.rotate(-1);//逆时针旋转
        } else {//否则
            createBottomTipWindow(Main.SettingState.systemLanguage ? "Cannot Rotate GIF Picture" : "不支持旋转GIF图片");//提示
        }
    }

    public static void handleRightRotation() {//处理右旋转
        if (currentSlideItem.isNotGIF()) {//如果不是GIF
            currentSlideItem.rotate(1);//顺时针旋转
        } else {//否则
            createBottomTipWindow(Main.SettingState.systemLanguage ? "Cannot Rotate GIF Picture" : "不支持旋转GIF图片");//提示
        }
    }
}
