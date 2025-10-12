package FileEditPackage;

import DirectoryPackage.DirectoryManager;
import DirectoryPackage.DiskManager;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import java.util.HashMap;
import java.util.Map;
import java.nio.*;

public class FileOperation {
    private DirectoryManager directoryManager;
    private DiskManager diskManager;
    private Map<String, String> openFiles; // 存储打开的文件名和内容
    private String currentPath = "/";

    public FileOperation(DirectoryManager directoryManager, DiskManager diskManager) {
        this.directoryManager = directoryManager;
        this.diskManager = diskManager;
        this.openFiles = new HashMap<>();
    }

    public void createFile() {//创建文件
        String fileName = JOptionPane.showInputDialog("请输入文件名:");
        if (fileName == null || fileName.trim().isEmpty()) return;

        String fileType = JOptionPane.showInputDialog("请输入文件类型:");
        if (fileType == null || fileType.trim().isEmpty()) return;

        byte startBlock = diskManager.getFatManager().allocateBlock();// 分配磁盘块
        if (startBlock == -1) {
            JOptionPane.showMessageDialog(null, "磁盘空间不足!");
            return;
        }

        byte attributes = DiskManager.NORMAL_FILE;// 创建FCB并添加到目录
        byte length = 0; // 初始长度为0
        String currentPath = getCurrentValidPath(); // /获取当前目录路径


        boolean success = directoryManager.addEntryToDirectory(currentPath, fileName, fileType,
                attributes, startBlock, length);
        if (success) {
            JOptionPane.showMessageDialog(null, "文件创建成功!");
        } else {
            diskManager.getFatManager().freeBlock(startBlock);
            JOptionPane.showMessageDialog(null, "文件创建失败! 目录可能已满。");
        }
    }

    public void openFile() {//打开文件
        String filePath = JOptionPane.showInputDialog("请输入要打开的文件路径:");
        if (filePath == null || filePath.trim().isEmpty()) return;

        FCB file = directoryManager.findFile(filePath);
        if (file == null) {
            JOptionPane.showMessageDialog(null, "文件不存在!");
            return;
        }

        if (file.isDirectory()) {
            JOptionPane.showMessageDialog(null, "不能打开目录!");
            return;
        }

        String content = readFileContent(file);// 读取文件内容
        openFiles.put(filePath, content);

        JOptionPane.showMessageDialog(null, "文件已打开: " + filePath);
    }

    public void readFile() {//读文件
        String filePath = JOptionPane.showInputDialog("请输入要读取的文件路径:");
        if (filePath == null || filePath.trim().isEmpty()) return;

        String content = openFiles.get(filePath);
        if (content == null) {
            FCB file = directoryManager.findFile(filePath);
            if (file == null) {
                JOptionPane.showMessageDialog(null, "文件不存在或未打开!");
                return;
            }
            content = readFileContent(file);
        }

        JOptionPane.showMessageDialog(null, "文件内容:\n" + content);
    }

    public void writeFile() {//写文件
        String filePath = JOptionPane.showInputDialog("请输入要写入的文件路径:");
        if (filePath == null || filePath.trim().isEmpty()) return;

        if (!openFiles.containsKey(filePath)) {
            JOptionPane.showMessageDialog(null, "文件未打开!");
            return;
        }

        FCB file = directoryManager.findFile(filePath);
        if (file == null) {
            JOptionPane.showMessageDialog(null, "文件不存在!");
            return;
        }

        if (file.isReadOnly()) {
            JOptionPane.showMessageDialog(null, "文件是只读的，不能写入!");
            return;
        }

        String content = JOptionPane.showInputDialog("请输入文件内容:");
        if (content != null) {
            openFiles.put(filePath, content);
            writeFileContent(filePath,file, content);// 这里需要实现将内容写入磁盘的逻辑
            JOptionPane.showMessageDialog(null, "文件写入成功!");
        }
    }

    public void closeFile() {//关闭文件
        String filePath = JOptionPane.showInputDialog("请输入要关闭的文件路径:");
        if (filePath == null || filePath.trim().isEmpty()) return;

        if (openFiles.remove(filePath) != null) {
            JOptionPane.showMessageDialog(null, "文件已关闭!");
        } else {
            JOptionPane.showMessageDialog(null, "文件未打开!");
        }
    }

    public void deleteFile() {//删除文件
        String filePath = JOptionPane.showInputDialog("请输入要删除的文件路径:");
        if (filePath == null || filePath.trim().isEmpty()) return;

        FCB file = directoryManager.findFile(filePath);
        if (file == null) {
            JOptionPane.showMessageDialog(null, "文件不存在!");
            return;
        }

        if (openFiles.containsKey(filePath)) {
            JOptionPane.showMessageDialog(null, "文件正在使用中，请先关闭!");
            return;
        }

        // 释放文件占用的磁盘块
        diskManager.getFatManager().freeFileBlocks(file.getStartBlock());

        // 从目录中删除条目
        boolean success = directoryManager.removeEntryFromDirectory(filePath);
        if (success) {
            JOptionPane.showMessageDialog(null, "文件删除成功!");
        } else {
            JOptionPane.showMessageDialog(null, "文件删除失败!");
        }
    }

    public void typeFile() {//显示文件内容
        String filePath = JOptionPane.showInputDialog("请输入要显示内容的文件路径:");
        if (filePath == null || filePath.trim().isEmpty()) return;

        FCB file = directoryManager.findFile(filePath);
        if (file == null) {
            JOptionPane.showMessageDialog(null, "文件不存在!");
            return;
        }

        if (file.isDirectory()) {
            JOptionPane.showMessageDialog(null, "不能显示目录内容!");
            return;
        }

        String content = readFileContent(file);
        JOptionPane.showMessageDialog(null,
                "文件名: " + file.getFileName() + "\n" +
                        "文件类型: " + file.getFileType() + "\n" +
                        "属性: " + (file.isReadOnly() ? "只读" : "可写") + "\n" +
                        "起始块: " + file.getStartBlock() + "\n" +
                        "长度: " + file.getFileLength() + "块\n" +
                        "内容: " + content);
    }

    public void change() {//改变文件属性
        String filePath = JOptionPane.showInputDialog("请输入要改变属性的文件路径:");
        if (filePath == null || filePath.trim().isEmpty()) return;

        FCB file = directoryManager.findFile(filePath);
        if (file == null) {
            JOptionPane.showMessageDialog(null, "文件不存在!");
            return;
        }

        if (file.isDirectory()) {
            JOptionPane.showMessageDialog(null, "不能改变目录属性!");
            return;
        }

        String[] options = {"只读", "普通"};
        int choice = JOptionPane.showOptionDialog(null,
                "选择文件属性:", "改变属性",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                null, options, options[0]);

        byte newAttributes;
        if (choice == 0) {
            newAttributes = (byte) (file.getAttributes() | DiskManager.READ_ONLY);
        } else {
            newAttributes = (byte) (file.getAttributes() & ~DiskManager.READ_ONLY);
        }

        file.setAttributes(newAttributes);
        // 需要更新磁盘上的FCB
        updateFCBOnDisk(filePath, file);
        JOptionPane.showMessageDialog(null, "属性修改成功!");
    }

    public void createCatalog() {//建立目录
        String dirPath = JOptionPane.showInputDialog("请输入目录路径:");
        if (dirPath == null || dirPath.trim().isEmpty()) return;

        String parentPath = directoryManager.getParentPath(dirPath);
        String dirName = directoryManager.getDirectoryName(dirPath);

        // 分配磁盘块给新目录
        byte startBlock = diskManager.getFatManager().allocateBlock();
        if (startBlock == -1) {
            JOptionPane.showMessageDialog(null, "磁盘空间不足!");
            return;
        }

        // 创建目录条目
        boolean success = directoryManager.addEntryToDirectory(parentPath, dirName, "  ",
                DiskManager.DIRECTORY, startBlock, (byte)0);
        if (success) {
            // 初始化新目录
            initializeNewDirectory(startBlock);
            JOptionPane.showMessageDialog(null, "目录创建成功!");
        } else {
            diskManager.getFatManager().freeBlock(startBlock);
            JOptionPane.showMessageDialog(null, "目录创建失败!");
        }
    }

    public void showCatalog() {//显示目录
        String dirPath = JOptionPane.showInputDialog("请输入目录路径:");
        if (dirPath == null) dirPath = "/";

        List<FCB> contents = directoryManager.listDirectory(dirPath);

        StringBuilder sb = new StringBuilder();
        sb.append("目录: ").append(dirPath).append("\n\n");

        if (contents.isEmpty()) {
            sb.append("目录为空\n");
        } else {
            for (FCB entry : contents) {// 遍历目录中的所有文件控制块(FCB)条目
                if (entry.isDirectory()) {// 根据条目类型显示不同的格式：目录或文件
                    sb.append("<DIR>   ").append(entry.getFileName()).append("/\n");
                } else {
                    sb.append("        ").append(entry.getFileName())
                            .append(".").append(entry.getFileType())
                            .append(" (").append(entry.getFileLength()).append(" blocks)\n");
                }
            }
        }

        sb.append("\n").append(diskManager.getDiskUsage());
        JOptionPane.showMessageDialog(null, sb.toString());
    }

    public void deleteCatalog() {//删除空目录
        String dirPath = JOptionPane.showInputDialog("请输入要删除的目录路径:");
        if (dirPath == null || dirPath.trim().isEmpty()) return;

        if (dirPath.equals("/")) {
            JOptionPane.showMessageDialog(null, "不能删除根目录!");
            return;
        }

        Integer dirBlock = directoryManager.findDirectoryBlock(dirPath);
        if (dirBlock == null) {
            JOptionPane.showMessageDialog(null, "目录不存在!");
            return;
        }

        if (!directoryManager.isDirectoryEmpty(dirBlock)) {
            JOptionPane.showMessageDialog(null, "目录非空，不能删除!");
            return;
        }
        diskManager.getFatManager().freeBlock((byte)dirBlock.intValue()); // 释放目录占用的磁盘块


        boolean success = directoryManager.removeEntryFromDirectory(dirPath);// 从父目录中删除条目
        if (success) {
            JOptionPane.showMessageDialog(null, "目录删除成功!");
        } else {
            JOptionPane.showMessageDialog(null, "目录删除失败!");
        }
    }

    // 辅助方法
    private String getCurrentValidPath() {
        // 验证当前路径是否存在
        Integer dirBlock = directoryManager.findDirectoryBlock(currentPath);
        if (dirBlock != null) {
            return currentPath;
        } else {
            // 如果当前路径无效，回退到根目录
            currentPath = "/";
            return currentPath;
        }
    }

    private String readFileContent(FCB file) {
        if (file == null) {// 排除非法文件（空文件、目录、无效起始块）
            System.err.println("读取文件失败：FCB为null（非法文件）");
            return "";
        }
        if (file.isDirectory()) {
            System.err.println("读取文件失败：目标是目录，不能读取内容");
            return "";
        }
        byte startBlock = file.getStartBlock();
        int fileTotalBlocks = file.getFileLength() & 0xFF; // 转无符号int（避免byte负数问题）
        if (startBlock < 3 || startBlock >= DiskManager.TOTAL_BLOCKS) { // 块3起为用户块，排除系统/非法块
            System.err.printf("读取文件失败：起始块[%d]非法（需在3-%d之间）%n",
                    startBlock, DiskManager.TOTAL_BLOCKS - 1);
            return "";
        }
        if (fileTotalBlocks <= 0) { // 空文件（无数据块）
            return "";
        }
        StringBuilder content = new StringBuilder();//初始化工具：内容缓存、FAT表（用于判断块状态）、当前块指针
        DiskManager.FATManager fatManager = diskManager.getFatManager();
        byte[] fatTable = fatManager.getFAT(); // 获取完整FAT表，判断块是否为坏块/空闲块
        byte currentBlock = startBlock;
        int readBlockCount = 0; // 已读取块数（用于控制不超过文件总块数）

        // 遍历FAT链读取数据（双重控制：块数+FAT链结束，避免无限循环）
        while (currentBlock != -1 && currentBlock != DiskManager.END_OF_FILE && readBlockCount < fileTotalBlocks) {
            if (currentBlock < 3 || currentBlock >= DiskManager.TOTAL_BLOCKS) { // 校验当前块合法性（二次防护：避免越界/系统块）
                System.err.printf("读取中断：当前块[%d]非法，已读取%d块%n",
                        currentBlock, readBlockCount);
                break;
            }
            byte fatBlockStatus = fatTable[currentBlock];// 判断当前块状态（排除坏块/空闲块，避免读垃圾数据）
            if (fatBlockStatus == DiskManager.BAD_BLOCK) {
                System.err.printf("读取警告：当前块[%d]是坏块，跳过该块%n", currentBlock);
                currentBlock = fatManager.getNextBlock(currentBlock); // 继续读下一块（容错）
                readBlockCount++;
                continue;
            }
            if (fatBlockStatus == DiskManager.FREE_BLOCK) {
                System.err.printf("读取警告：当前块[%d]是空闲块（FAT链异常），停止读取%n", currentBlock);
                break;
            }
            byte[] blockData = diskManager.readBlock(currentBlock);// 读取块数据（指定编码+不删空白字符，还原文件原始内容）
            if (blockData == null) {
                System.err.printf("读取失败：块[%d]数据为空，跳过该块%n", currentBlock);
                currentBlock = fatManager.getNextBlock(currentBlock);
                readBlockCount++;
                continue;
            }


            String blockContent = new String(blockData, StandardCharsets.UTF_8);// 避免乱码，不trim（保留原始空白）
            content.append(blockContent);
            currentBlock = fatManager.getNextBlock(currentBlock);
            readBlockCount++; // 更新指针+计数（准备读下一块）
        }
        return content.toString();
    }

    private void writeFileContent(String filePath, FCB file, String content) {
        if (file == null) {// 校验文件合法性：非空、非目录、路径非空
            System.err.println("写入失败：FCB为空（非法文件）");
            return;
        }
        if (file.isDirectory()) {
            System.err.println("写入失败：目标是目录，不支持写入内容");
            return;
        }
        if (filePath == null || filePath.trim().isEmpty()) {
            System.err.println("写入失败：文件路径为空，无法定位FCB");
            return;
        }
        content = (content == null) ? "" : content;// 处理空内容（允许清空文件）
        DiskManager.FATManager fatManager = diskManager.getFatManager();// 获取FAT管理器（核心工具类）
        List<Byte> allocatedBlocks = new ArrayList<>(); // 存储已分配的块（用于异常回滚，避免磁盘块泄漏）

        try {
            byte oldStartBlock = file.getStartBlock();// 释放文件原有磁盘块（避免泄漏）
            if (oldStartBlock != 0 && oldStartBlock != -1) { // 原有块非空（说明文件之前有数据）
                fatManager.freeFileBlocks(oldStartBlock);
                System.out.printf("已释放文件原有块：起始块[%d]%n", oldStartBlock);
            }
            byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8); // 统一UTF-8编码（避免乱码）
            int totalBytes = contentBytes.length;
            int blocksNeeded = (totalBytes + DiskManager.BLOCK_SIZE - 1) / DiskManager.BLOCK_SIZE;// 计算所需块数：向上取整

            if (blocksNeeded > 254) {// 校验块数：fileLength是byte类型（1字节），无符号最大254（255是END_OF_FILE）
                throw new RuntimeException(String.format("所需块数[%d]超过上限（最大254块），文件过大", blocksNeeded));
            }
            if (blocksNeeded == 0) { // 空内容：至少分配1块（避免startBlock为0，符合文件系统规则）
                blocksNeeded = 1;
            }
            byte newStartBlock = -1; // 新文件的起始块
            byte prevBlock = -1;     // 上一个块（用于构建链表）

            for (int i = 0; i < blocksNeeded; i++) {
                // 分配单个块
                byte newBlock = fatManager.allocateBlock();
                if (newBlock == -1) {
                    throw new RuntimeException(String.format("分配第%d块失败，磁盘空间不足", i + 1));
                }
                allocatedBlocks.add(newBlock); // 加入已分配列表（用于回滚）


                if (i == 0) {// 构建FAT链：第一个块作为startBlock，后续块关联到前一个块
                    newStartBlock = newBlock; // 第一个块是起始块
                } else {
                    // 前一个块的next指向当前块
                    fatManager.setNextBlock(prevBlock, newBlock);
                }

                // 最后一个块的next设为END_OF_FILE（标记文件结束）
                if (i == blocksNeeded - 1) {
                    fatManager.setNextBlock(newBlock, DiskManager.END_OF_FILE);
                }

                prevBlock = newBlock; // 更新前一个块指针
            }
            System.out.printf("成功分配%d块，FAT链构建完成：起始块[%d]%n", blocksNeeded, newStartBlock);

            //  拆分内容到块并写入磁盘
            for (int i = 0; i < blocksNeeded; i++) {
                byte targetBlock = allocatedBlocks.get(i); // 当前要写入的块
                // 计算当前块的字节范围：[起始索引, 结束索引)
                int startIdx = i * DiskManager.BLOCK_SIZE;
                int endIdx = Math.min(startIdx + DiskManager.BLOCK_SIZE, totalBytes);
                // 创建块数据（固定64个字节，不足补0，避免块大小不匹配）
                byte[] blockData = new byte[DiskManager.BLOCK_SIZE];
                if (startIdx < totalBytes) { // 有有效数据时拷贝
                    System.arraycopy(contentBytes, startIdx, blockData, 0, endIdx - startIdx);
                }

                // 写入块到磁盘（校验写入结果）
                boolean writeSuccess = diskManager.writeBlock(targetBlock, blockData);
                if (!writeSuccess) {
                    throw new RuntimeException(String.format("写入块[%d]失败（块号非法或数据长度错误）", targetBlock));
                }
                System.out.printf("已写入块[%d]：有效数据%d字节%n", targetBlock, endIdx - startIdx);
            }

            // 更新FCB并同步到磁盘
            file.setStartBlock(newStartBlock); // 更新起始块（关键：关联新分配的块）
            file.setFileLength((byte) blocksNeeded); // 更新文件长度（块数）
            updateFCBOnDisk(filePath, file); // 同步到磁盘（用完整路径避免同名冲突）
            System.out.printf("文件FCB更新完成：路径[%s]，块数[%d]，起始块[%d]%n",
                    filePath, blocksNeeded, newStartBlock);

        } catch (RuntimeException e) {
            // -------------------------- 7. 异常回滚（核心：避免磁盘块泄漏） --------------------------
            System.err.printf("写入文件失败：%s，开始回滚已分配块%n", e.getMessage());
            // 释放所有已分配的块
            for (byte block : allocatedBlocks) {
                fatManager.freeBlock(block);
                System.out.printf("回滚释放块[%d]%n", block);
            }
            // 重置FCB（避免残留无效数据）
            file.setStartBlock((byte) 0);
            file.setFileLength((byte) 0);
            updateFCBOnDisk(filePath, file);

        }
    }

    private void initializeNewDirectory(byte startBlock) {
        // 初始化新目录，创建空的目录项
        for (int i = 0; i < DiskManager.DIRECTORY_ENTRIES_PER_BLOCK; i++) {
            FCB emptyFCB = new FCB();
            diskManager.writeFCBToBlock(startBlock, i, emptyFCB);
        }
    }

    private void updateFCBOnDisk(String filePath, FCB fcb) {
        // 更新磁盘上的FCB
        String parentPath = directoryManager.getParentPath(filePath);
        String fileName = directoryManager.getFileName(filePath);

        Integer parentBlock = directoryManager.findDirectoryBlock(parentPath);
        if (parentBlock != null) {
            for (int i = 0; i < DiskManager.DIRECTORY_ENTRIES_PER_BLOCK; i++) {
                FCB entry = diskManager.readFCBFromBlock(parentBlock, i);
                if (entry.getFileName().trim().equals(fileName)) {
                    diskManager.writeFCBToBlock(parentBlock, i, fcb);
                    break;
                }
            }
        }
    }
}