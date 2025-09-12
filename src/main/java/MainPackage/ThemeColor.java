package MainPackage;

import DirectoryPackage.DirectoryTree;
import NetworkPackage.User;
import FileDisplayPackage.FileDisplayBottomBar;
import FileDisplayPackage.FileDisplayMainPanel;
import FileDisplayPackage.FileDisplayTopBar;
import FileEditPackage.FileEditPanel;
import FileEditPackage.FileEditScrollPane;
import FileEditPackage.FileEditToolBar;

import java.awt.*;

public class ThemeColor {//主题颜色类
    public static final Color LIGHT_TIP_IMFORMATION_COLOR = new Color(238, 238, 238);//浅色主题提示信息颜色常量
    public static final Color LIGHT_DIRECTORY_MAIN_COLOR = new Color(255, 255, 255);//浅色主题目录树主颜色常量
    public static final Color LIGHT_DIRECTORY_BACKGROUND_SELECTION_COLOR = new Color(235, 235, 235);//浅色主题目录树背景选择颜色常量
    public static final Color LIGHT_DIRECTORY_BACKGROUND_NON_SELECTION_COLOR = new Color(255, 255, 255);//浅色主题目录树背景未选择颜色常量
    public static final Color LIGHT_DIRECTORY_TEXT_SELECTION_COLOR = new Color(5, 5, 5);//浅色主题目录树文字选择颜色常量
    public static final Color LIGHT_DIRECTORY_TEXT_NON_SELECTION_COLOR = new Color(15, 15, 15);//浅色主题目录树文字未选择颜色常量
    public static final Color LIGHT_DIRECTORY_BORDER_SELECTION_COLOR = new Color(235, 235, 235);//浅色主题目录树选择边框颜色常量
    public static final Color LIGHT_PICTURE_MAIN_COLOR = new Color(245, 245, 245);//浅色主题图片主面板颜色常量
    public static final Color LIGHT_PICTURE_EMPTY_COLOR = new Color(130, 130, 130);//浅色主题图片空标签颜色常量
    public static final Color LIGHT_PICTURE_FONT_COLOR = new Color(5, 5, 5);//浅色主题图片字体颜色常量
    public static final Color LIGHT_PICTURE_BAR_COLOR = new Color(220, 220, 225);//深色主题图片顶部底部栏颜色常量
    public static final Color LIGHT_PICTURE_DRAGGED_WINDOW_COLOR = new Color(245, 245, 245, 200);//浅色主题图片拖拽窗口颜色常量（需要rgba格式）
    public static final Color LIGHT_DIALOG_MAIN_COLOR = new Color(245, 245, 245);//浅色主题设置主面板颜色常量
    public static final Color LIGHT_DIALOG_FONT_COLOR = new Color(5, 5, 5);//浅色主题设置字体颜色常量
    public static final Color LIGHT_LOG_IN_ACTIVATE_COLOR = new Color(245, 245, 245);//浅色主题登录激活颜色常量
    public static final Color LIGHT_LOG_IN_DEACTIVATE_COLOR = new Color(185, 185, 185);//浅色主题登录未激活颜色常量
    public static final Color LIGHT_USER_OPERATION_BUTTON_COLOR = new Color(238, 238, 238);//浅色用户操作按钮颜色常量
    public static final Color LIGHT_USER_OPERATION_BUTTON_FONT_COLOR = new Color(7, 7, 7);//浅色用户操作按钮字体颜色常量

    public static final Color DARK_TIP_IMFORMATION_COLOR = new Color(40, 40, 40);//深色主题提示信息颜色常量
    public static final Color DARK_DIRECTORY_MAIN_COLOR = new Color(23, 27, 27);//深色主题目录树颜色常量
    public static final Color DARK_DIRECTORY_BACKGROUND_SELECTION_COLOR = new Color(70, 70, 70);//深色主题目录树背景选择颜色常量
    public static final Color DARK_DIRECTORY_BACKGROUND_NON_SELECTION_COLOR = new Color(27, 27, 27);//深色主题目录树背景未选择颜色常量
    public static final Color DARK_DIRECTORY_TEXT_SELECTION_COLOR = new Color(250, 250, 250);//深色主题目录树文字选择颜色常量
    public static final Color DARK_DIRECTORY_TEXT_NON_SELECTION_COLOR = new Color(240, 240, 240);//深色主题目录树文字未选择颜色常量
    public static final Color DARK_DIRECTORY_BORDER_SELECTION_COLOR = new Color(70, 70, 70);//深色主题目录树选择边框颜色常量
    public static final Color DARK_PICTURE_MAIN_COLOR = new Color(34, 34, 35);//深色主题主面板颜色常量
    public static final Color DARK_PICTURE_EMPTY_COLOR = new Color(225, 225, 225);//深色主题空标签颜色常量
    public static final Color DARK_PICTURE_FONT_COLOR = new Color(250, 250, 250);//深色主题图片字体颜色常量
    public static final Color DARK_PICTURE_BAR_COLOR = new Color(26, 26, 26);//深色主题图片顶部底部栏颜色常量
    public static final Color DARK_PICTURE_DRAGGED_WINDOW_COLOR = new Color(27, 27, 27, 200);//深色主题图片拖拽窗口颜色常量（需要rgba格式）
    public static final Color DARK_DIALOG_MAIN_COLOR = new Color(25, 25, 25);//深色主题设置主面板颜色常量
    public static final Color DARK_DIALOG_FONT_COLOR = new Color(245, 245, 245);//深色主题设置字体颜色常量
    public static final Color DARK_LOG_IN_ACTIVATE_COLOR = new Color(25, 25, 25);//深色主题登录激活颜色常量
    public static final Color DARK_LOG_IN_DEACTIVATE_COLOR = new Color(145, 145, 145);//深色主题登录未激活颜色常量
    public static final Color DARK_USER_OPERATION_BUTTON_COLOR = new Color(24, 24, 24);//深色用户操作按钮颜色常量
    public static final Color DARK_USER_OPERATION_BUTTON_FONT_COLOR = new Color(244, 244, 244);//深色用户操作按钮字体颜色常量

    public static final Color SLIDE_BUTTON_COLOR = new Color(244, 244, 247);//幻灯片按钮颜色常量
    public static final Color SETTING_BUTTON_COLOR = new Color(217, 233, 244);//设置按钮颜色常量
    public static final Color USER_LOG_BUTTON_COLOR = new Color(245, 181, 179);//用户登录按钮颜色常量
    public static final Color MOUSE_SELECTION_COLOR = new Color(100, 180, 255, 80);//鼠标框选颜色常量
    public static final Color SCROLL_PANE_THUMB_COLOR = new Color(128, 128, 128, 128);//滚动栏滑块默认颜色
    public static final Color SCROLL_PANE_THUMB_HOVER_COLOR = new Color(128, 128, 128, 192);//滚动栏滑块悬浮颜色
    public static final Color LIGHT_SCROLL_PANE_TRACK_COLOR = new Color(220, 220, 220);//浅色主题滚动栏轨道颜色
    public static final Color DARK_SCROLL_PANE_TRACK_COLOR = new Color(100, 100, 100);//深色主题滚动栏轨道颜色
    public static final Color PICTURE_SELECTED_BORDER_COLOR = new Color(255, 0, 0, 200);//图片选中边框颜色常量

    public static void switchThemeColor() {//切换主题颜色
        boolean themeColor = Main.SettingState.themeColor;//获取主题颜色
        if (Main.SettingState.backgroundPictureDirectory.isEmpty()) {//如果背景图片路径为空
            Main.bottomTipInformation.setBackground(themeColor ? DARK_TIP_IMFORMATION_COLOR : LIGHT_TIP_IMFORMATION_COLOR);//设置提示信息字体背景颜色
            Main.bottomTipInformationPanel.setBackground(themeColor ? DARK_TIP_IMFORMATION_COLOR : LIGHT_TIP_IMFORMATION_COLOR);//设置提示信息面板背景颜色
            DirectoryTree.directoryTree.setBackground(themeColor ? DARK_DIRECTORY_MAIN_COLOR : LIGHT_DIRECTORY_MAIN_COLOR);//设置背景色
            DirectoryTree.directoryTree.setCellRenderer(new DirectoryTree.DriveTreeRenderer());//设置自定义节点渲染器
            FileDisplayBottomBar.zoomSlider.setBackground(themeColor ? DARK_PICTURE_BAR_COLOR : LIGHT_PICTURE_BAR_COLOR);//设置背景颜色
            FileDisplayBottomBar.bottomBarPanel.setBackground(themeColor ? DARK_PICTURE_BAR_COLOR : LIGHT_PICTURE_BAR_COLOR);//设置背景颜色
            FileDisplayMainPanel.mainPanel.setBackground(themeColor ? DARK_PICTURE_MAIN_COLOR : LIGHT_PICTURE_MAIN_COLOR);//设置背景颜色
            FileDisplayMainPanel.emptyLabel.setForeground(themeColor ? DARK_PICTURE_EMPTY_COLOR : LIGHT_PICTURE_EMPTY_COLOR);//设置前景色
            FileDisplayTopBar.topBarPanel.setBackground(themeColor ? DARK_PICTURE_BAR_COLOR : LIGHT_PICTURE_BAR_COLOR);//设置背景颜色
            Main.directoryTreeScrollPane.getVerticalScrollBar().repaint();//重新绘制
            Main.directoryTreeScrollPane.getHorizontalScrollBar().repaint();//重新绘制
            Main.picturePreviewMainPanelScrollPane.getVerticalScrollBar().repaint();//重新绘制
            Main.picturePreviewMainPanelScrollPane.getHorizontalScrollBar().repaint();//重新绘制
        }

        FileDisplayMainPanel.refreshMainPanel();//刷新
        Setting.initSettingDialog();//刷新设置菜单
        if (Main.SettingState.userAccount.isEmpty()) {//如果没有用户名
            User.initLogInDialog();//刷新登录菜单
            User.initRegisterDialog();//刷新注册菜单
        } else {//否则有用户名
            User.initUserDialog();//刷新用户菜单
        }
        FileEditPanel.picturePanel.setBackground(themeColor ? DARK_PICTURE_MAIN_COLOR : LIGHT_PICTURE_MAIN_COLOR);//设置背景颜色
        FileEditScrollPane.scrollPanePanel.setBackground(themeColor ? DARK_DIRECTORY_MAIN_COLOR : LIGHT_DIRECTORY_MAIN_COLOR);//设置背景颜色
        FileEditToolBar.toolBarPanel.setBackground(themeColor ? DARK_PICTURE_BAR_COLOR : LIGHT_PICTURE_BAR_COLOR);//设置背景颜色
        FileEditToolBar.leftPanel.setBackground(Main.SettingState.themeColor ? DARK_PICTURE_BAR_COLOR : LIGHT_PICTURE_BAR_COLOR);//设置背景颜色
        FileEditToolBar.centerPanel.setBackground(Main.SettingState.themeColor ? DARK_PICTURE_BAR_COLOR : LIGHT_PICTURE_BAR_COLOR);//设置背景颜色
        FileEditToolBar.rightPanel.setBackground(Main.SettingState.themeColor ? DARK_PICTURE_BAR_COLOR : LIGHT_PICTURE_BAR_COLOR);//设置背景颜色
        FileEditToolBar.informationLabel.setForeground(themeColor ? DARK_PICTURE_FONT_COLOR : LIGHT_PICTURE_FONT_COLOR);//设置字体颜色
        FileEditToolBar.zoomSlider.setBackground(themeColor ? DARK_PICTURE_BAR_COLOR : LIGHT_PICTURE_BAR_COLOR);//设置背景颜色
    }
}
