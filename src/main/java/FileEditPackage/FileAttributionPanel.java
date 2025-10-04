package FileEditPackage;

import javax.swing.*;
import java.awt.*;
import java.text.*;
import java.util.List;
import java.util.*;

public class FileAttributionPanel {//文件属性面板类
    private int[] fat = new int[128]; // FAT表，模拟磁盘空间管理，128个块
    private int freeBlocks = 126; // 空闲块数，初始为126
    private FileModel root;    // 根目录
    private FileModel currentDir; // 当前目录

    private Map<String, FileModel> openFiles = new HashMap<>();    // 打开的文件列表，存储文件名和文件对象的映射

    class FileModel { // 文件模型类，表示文件或目录
        String name;        // 文件名
        char attribute;     // 文件属性：'R'只读，'N'普通，'D'目录
        int startBlock;     // 起始盘块号
        int length;         // 文件长度（字节）
        FileModel father;   // 父目录
        Map<String, FileModel> children; // 子文件/目录的映射
        String content;     // 文件内容（如果是目录，则content为空）

        public FileModel(String name, char attribute, int startBlock) { // 构造函数
            this.name = name; // 设置文件名
            this.attribute = attribute; // 设置文件属性
            this.startBlock = startBlock; // 设置起始盘块号
            this.length = 0; // 初始长度为0
            this.father = null; // 初始父目录为空
            this.children = new HashMap<>(); // 初始化子文件映射
            this.content = ""; // 初始内容为空
        }

        public boolean isDirectory() {        // 判断是否是目录
            return attribute == 'D'; // 属性为'D'表示目录
        }

        public boolean isReadOnly() {        // 判断是否是只读
            return attribute == 'R'; // 属性为'R'表示只读
        }
        public void addChild(FileModel child) {        // 添加子文件或目录
            child.father = this; // 设置子文件的父目录为当前目录
            children.put(child.name, child); // 将子文件添加到子文件映射中
        }
        public boolean removeChild(String name) {// 删除子文件或目录
            return children.remove(name) != null; // 从子文件映射中移除指定文件
        }
        public FileModel getChild(String name) {// 获取子文件或目录
            return children.get(name); // 从子文件映射中获取指定文件
        }
        public List<FileModel> listChildren() {// 列出所有子项
            return new ArrayList<>(children.values()); // 返回子文件映射中的所有值
        }
    }

    public FileAttributionPanel() { // 构造函数

        initializeFAT();// 初始化FAT表

        root = new FileModel("root", 'D', 1); // 创建根目录
        root.father = root; // 根目录的父目录指向自己
        currentDir = root; // 当前目录初始化为根目录


        fat[1] = -1; // 标记根目录占用的磁盘块,-1表示文件结束
        freeBlocks--; // 空闲块数减1
    }

    private void initializeFAT() { // 初始化FAT表
        for (int i = 0; i < fat.length; i++) { // 遍历FAT表所有项
            fat[i] = 0; // 0表示空闲
        }
        fat[0] = 126; // 记录空闲块数
    }
    private int allocateBlock() {// 分配磁盘块
        for (int i = 2; i < fat.length; i++) { // 从第2块开始查找
            if (fat[i] == 0) { // 如果块空闲
                fat[i] = -1; // 标记为已占用
                freeBlocks--; // 空闲块数减1
                fat[0] = freeBlocks; // 更新FAT表中记录的空闲块数
                return i; // 返回分配的块号
            }
        }
        return -1; // 没有空闲块
    }
    private void freeBlock(int block) {// 释放磁盘块
        if (block > 0 && block < fat.length) { // 检查块号是否有效
            fat[block] = 0; // 标记块为空闲
            freeBlocks++; // 空闲块数加1
            fat[0] = freeBlocks; // 更新FAT表中记录的空闲块数
        }
    }
    private List<Integer> allocateBlocks(int numBlocks) {  // 分配多个连续磁盘块
        List<Integer> blocks = new ArrayList<>(); // 创建块列表
        for (int i = 0; i < numBlocks; i++) { // 循环分配指定数量的块
            int block = allocateBlock(); // 分配一个块
            if (block == -1) { // 如果分配失败
                // 分配失败，释放已分配的块
                for (int b : blocks) { // 遍历已分配的块
                    freeBlock(b); // 释放块
                }
                return new ArrayList<>(); // 返回空列表
            }
            blocks.add(block); // 将分配的块添加到列表
        }
        return blocks; // 返回分配的块列表
    }

    public void createFile(){//创建文件
        String fileName = JOptionPane.showInputDialog("请输入文件名:"); // 获取文件名输入
        if (fileName == null || fileName.trim().isEmpty()) return; // 如果文件名为空则返回

        String type = JOptionPane.showInputDialog("请输入文件类型:"); // 获取文件类型输入
        if (type == null) return; // 如果文件类型为空则返回

        String sizeStr = JOptionPane.showInputDialog("请输入文件大小:"); // 获取文件大小输入
        if (sizeStr == null) return; // 如果文件大小为空则返回

        try {
            int size = Integer.parseInt(sizeStr); // 将文件大小转换为整数
            if (freeBlocks < size) { // 检查磁盘空间是否足够
                JOptionPane.showMessageDialog(null, "磁盘空间不足!"); // 显示错误消息
                return; // 返回
            }

            if (currentDir.getChild(fileName) != null) { // 检查文件是否已存在
                JOptionPane.showMessageDialog(null, "文件已存在!"); // 显示错误消息
                return; // 返回
            }

            List<Integer> blocks = allocateBlocks(size); // 分配磁盘块
            if (blocks.isEmpty()) { // 检查分配是否成功
                JOptionPane.showMessageDialog(null, "分配磁盘块失败!"); // 显示错误消息
                return; // 返回
            }

            FileModel newFile = new FileModel(fileName, 'N', blocks.get(0)); // 创建新文件
            newFile.length = size; // 设置文件长度
            currentDir.addChild(newFile); // 将文件添加到当前目录

            JOptionPane.showMessageDialog(null, "文件创建成功!"); // 显示成功消息
        } catch (NumberFormatException e) { // 捕获数字格式异常
            JOptionPane.showMessageDialog(null, "文件大小必须是数字!"); // 显示错误消息
        }
    }

    public void openFile(){//打开文件
        String fileName = JOptionPane.showInputDialog("请输入要打开的文件名:"); // 获取文件名输入
        if (fileName == null || fileName.trim().isEmpty()) return; // 如果文件名为空则返回

        FileModel file = currentDir.getChild(fileName); // 从当前目录获取文件
        if (file == null) { // 如果文件不存在
            JOptionPane.showMessageDialog(null, "文件不存在!"); // 显示错误消息
            return; // 返回
        }

        if (file.isDirectory()) { // 如果是目录
            currentDir = file; // 将当前目录设置为该目录
            JOptionPane.showMessageDialog(null, "已进入目录: " + fileName); // 显示成功消息
        } else { // 如果是文件
            openFiles.put(fileName, file); // 将文件添加到打开文件列表
            JOptionPane.showMessageDialog(null, "文件已打开: " + fileName); // 显示成功消息
        }
    }

    public void readFile(){//读文件
        String fileName = JOptionPane.showInputDialog("请输入要读取的文件名:"); // 获取文件名输入
        if (fileName == null || fileName.trim().isEmpty()) return; // 如果文件名为空则返回

        FileModel file = openFiles.get(fileName); // 从打开文件列表获取文件
        if (file == null) { // 如果文件未打开
            file = currentDir.getChild(fileName); // 从当前目录获取文件
            if (file == null) { // 如果文件不存在
                JOptionPane.showMessageDialog(null, "文件不存在或未打开!"); // 显示错误消息
                return; // 返回
            }
        }

        if (file.isDirectory()) { // 如果是目录
            JOptionPane.showMessageDialog(null, "不能读取目录!"); // 显示错误消息
            return; // 返回
        }

        JOptionPane.showMessageDialog(null, "文件内容:\n" + file.content); // 显示文件内容
    }

    public void writeFile(){//写文件
        String fileName = JOptionPane.showInputDialog("请输入要写入的文件名:"); // 获取文件名输入
        if (fileName == null || fileName.trim().isEmpty()) return; // 如果文件名为空则返回

        FileModel file = openFiles.get(fileName); // 从打开文件列表获取文件
        if (file == null) { // 如果文件未打开
            JOptionPane.showMessageDialog(null, "文件未打开!"); // 显示错误消息
            return; // 返回
        }

        if (file.isReadOnly()) { // 如果文件是只读的
            JOptionPane.showMessageDialog(null, "文件是只读的，不能写入!"); // 显示错误消息
            return; // 返回
        }

        String content = JOptionPane.showInputDialog("请输入文件内容:"); // 获取文件内容输入
        if (content != null) { // 如果内容不为空
            file.content = content; // 设置文件内容
            file.length = content.length(); // 设置文件长度
            JOptionPane.showMessageDialog(null, "文件写入成功!"); // 显示成功消息
        }
    }

    public void closeFile(){//关闭文件
        String fileName = JOptionPane.showInputDialog("请输入要关闭的文件名:"); // 获取文件名输入
        if (fileName == null || fileName.trim().isEmpty()) return; // 如果文件名为空则返回

        if (openFiles.remove(fileName) != null) { // 从打开文件列表移除文件
            JOptionPane.showMessageDialog(null, "文件已关闭!"); // 显示成功消息
        } else { // 如果文件未打开
            JOptionPane.showMessageDialog(null, "文件未打开!"); // 显示错误消息
        }
    }

    public void deleteFile(){//删除文件
        String fileName = JOptionPane.showInputDialog("请输入要删除的文件名:"); // 获取文件名输入
        if (fileName == null || fileName.trim().isEmpty()) return; // 如果文件名为空则返回

        FileModel file = currentDir.getChild(fileName); // 从当前目录获取文件
        if (file == null) { // 如果文件不存在
            JOptionPane.showMessageDialog(null, "文件不存在!"); // 显示错误消息
            return; // 返回
        }

        if (file.isDirectory() && !file.children.isEmpty()) { // 如果是目录且不为空
            JOptionPane.showMessageDialog(null, "目录非空，不能删除!"); // 显示错误消息
            return; // 返回
        }

        if (openFiles.containsKey(fileName)) { // 如果文件已打开
            JOptionPane.showMessageDialog(null, "文件正在使用中，请先关闭!"); // 显示错误消息
            return; // 返回
        }
        freeBlock(file.startBlock);// 释放磁盘块
        currentDir.removeChild(fileName);// 从父目录中删除
        JOptionPane.showMessageDialog(null, "删除成功!"); // 显示成功消息
    }

    public void typeFile(){//显示文件内容
        String fileName = JOptionPane.showInputDialog("请输入要显示内容的文件名:"); // 获取文件名输入
        if (fileName == null || fileName.trim().isEmpty()) return; // 如果文件名为空则返回
        FileModel file = currentDir.getChild(fileName); // 从当前目录获取文件
        if (file == null) { // 如果文件不存在
            JOptionPane.showMessageDialog(null, "文件不存在!"); // 显示错误消息
            return; // 返回
        }

        if (file.isDirectory()) { // 如果是目录
            JOptionPane.showMessageDialog(null, "不能显示目录内容!"); // 显示错误消息
            return; // 返回
        }
        JOptionPane.showMessageDialog(null, // 显示文件信息
                "文件名: " + file.name + "\n" + // 文件名
                        "属性: " + (file.isReadOnly() ? "只读" : "普通") + "\n" + // 文件属性
                        "起始块: " + file.startBlock + "\n" + // 起始块号
                        "长度: " + file.length + "字节\n" + // 文件长度
                        "内容: " + file.content); // 文件内容
    }
    public void change(){//改变文件属性
        String fileName = JOptionPane.showInputDialog("请输入要改变属性的文件名:"); // 获取文件名输入
        if (fileName == null || fileName.trim().isEmpty()) return; // 如果文件名为空则返回

        FileModel file = currentDir.getChild(fileName); // 从当前目录获取文件
        if (file == null) { // 如果文件不存在
            JOptionPane.showMessageDialog(null, "文件不存在!"); // 显示错误消息
            return; // 返回
        }

        if (file.isDirectory()) { // 如果是目录
            JOptionPane.showMessageDialog(null, "不能改变目录属性!"); // 显示错误消息
            return; // 返回
        }

        String[] options = {"只读", "普通"}; // 属性选项
        int choice = JOptionPane.showOptionDialog(null, // 显示选项对话框
                "选择文件属性:", "改变属性", // 对话框标题和消息
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, // 对话框选项
                null, options, options[0]); // 选项数组和默认选项

        if (choice == 0) { // 如果选择只读
            file.attribute = 'R'; // 设置属性为只读
        } else if (choice == 1) { // 如果选择普通
            file.attribute = 'N'; // 设置属性为普通
        }

        JOptionPane.showMessageDialog(null, "属性修改成功!"); // 显示成功消息
    }

    public void md(){//建立目录
        String dirName = JOptionPane.showInputDialog("请输入目录名:"); // 获取目录名输入
        if (dirName == null || dirName.trim().isEmpty()) return; // 如果目录名为空则返回

        if (currentDir.getChild(dirName) != null) { // 检查目录是否已存在
            JOptionPane.showMessageDialog(null, "目录已存在!"); // 显示错误消息
            return; // 返回
        }

        int block = allocateBlock(); // 分配磁盘块
        if (block == -1) { // 如果分配失败
            JOptionPane.showMessageDialog(null, "磁盘空间不足!"); // 显示错误消息
            return; // 返回
        }

        FileModel newDir = new FileModel(dirName, 'D', block); // 创建新目录
        currentDir.addChild(newDir); // 将目录添加到当前目录

        JOptionPane.showMessageDialog(null, "目录创建成功!"); // 显示成功消息
    }

    public void dir(){//显示目录
        StringBuilder sb = new StringBuilder(); // 创建字符串构建器
        sb.append("目录: ").append(currentDir.name).append("\n\n"); // 添加目录标题

        List<FileModel> children = currentDir.listChildren(); // 获取子文件列表
        if (children.isEmpty()) { // 如果目录为空
            sb.append("目录为空\n"); // 添加空目录消息
        } else { // 如果目录不为空
            for (FileModel child : children) { // 遍历所有子文件
                sb.append(child.isDirectory() ? "<DIR>" : "     ") // 添加目录标记或空格
                        .append("  ") // 添加空格
                        .append(child.name) // 添加文件名
                        .append(child.isDirectory() ? "" : " (" + child.length + " bytes)") // 如果是文件则添加长度
                        .append("\n"); // 添加换行
            }
        }

        sb.append("\n空闲磁盘块: ").append(freeBlocks); // 添加空闲磁盘块信息
        JOptionPane.showMessageDialog(null, sb.toString()); // 显示目录内容
    }

    public void rd(){//删除空目录
        String dirName = JOptionPane.showInputDialog("请输入要删除的目录名:"); // 获取目录名输入
        if (dirName == null || dirName.trim().isEmpty()) return; // 如果目录名为空则返回

        FileModel dir = currentDir.getChild(dirName); // 从当前目录获取目录
        if (dir == null) { // 如果目录不存在
            JOptionPane.showMessageDialog(null, "目录不存在!"); // 显示错误消息
            return; // 返回
        }

        if (!dir.isDirectory()) { // 如果不是目录
            JOptionPane.showMessageDialog(null, "不是目录!"); // 显示错误消息
            return; // 返回
        }

        if (!dir.children.isEmpty()) { // 如果目录不为空
            JOptionPane.showMessageDialog(null, "目录非空，不能删除!"); // 显示错误消息
            return; // 返回
        }
        freeBlock(dir.startBlock);// 释放磁盘块
        currentDir.removeChild(dirName); // 从父目录中删除
        JOptionPane.showMessageDialog(null, "目录删除成功!"); // 显示成功消息
    }
    public void backToParent() {  // 返回到上级目录
        if (currentDir != root) { // 如果当前目录不是根目录
            currentDir = currentDir.father; // 将当前目录设置为父目录
            JOptionPane.showMessageDialog(null, "已返回上级目录"); // 显示成功消息
        } else { // 如果当前目录是根目录
            JOptionPane.showMessageDialog(null, "已经是根目录"); // 显示提示消息
        }
    }
    /*public void showFAT() { // 显示FAT表（已注释）
        StringBuilder sb = new StringBuilder(); // 创建字符串构建器
        sb.append("FAT表:\n"); // 添加标题

        for (int i = 0; i < fat.length; i += 8) { // 每8个块一行
            sb.append(String.format("%3d-%3d: ", i, Math.min(i+7, fat.length-1))); // 添加块范围
            for (int j = i; j < Math.min(i+8, fat.length); j++) { // 遍历当前行的块
                sb.append(String.format("%4d", fat[j])); // 添加块状态
            }
            sb.append("\n"); // 添加换行
        }

        sb.append("\n空闲块数: ").append(freeBlocks); // 添加空闲块数
        JOptionPane.showMessageDialog(null, sb.toString()); // 显示FAT表
    }*/
}