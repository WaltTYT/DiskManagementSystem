package FileEditPackage;

import DirectoryPackage.DirectoryManager;
import DirectoryPackage.DiskManager;
import DirectoryPackage.DirectoryEntry;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FileOperation {
    private DiskManager diskManager;
    private DirectoryManager directoryManager;
    private Map<String, OpenFileTableEntry> openFileTable; // 已打开文件表
    private byte[] buffer1;
    private byte[] buffer2;

    public FileOperation() {
        this.diskManager = new DiskManager();
        this.directoryManager = new DirectoryManager(diskManager);
        this.openFileTable = new HashMap<>();
        this.buffer1 = new byte[DiskManager.BLOCK_SIZE];
        this.buffer2 = new byte[DiskManager.BLOCK_SIZE];
        directoryManager.initializeRootDirectory();// 初始化根目录
    }
    private class OpenFileTableEntry {
        String filePath;
        int mode;           // 打开模式
        long readPointer;   // 读指针
        long writePointer;  // 写指针
        FCB fcb;           // 对应的FCB

        public OpenFileTableEntry(String filePath, int mode, FCB fcb) {
            this.filePath = filePath;
            this.mode = mode;
            this.fcb = fcb;
            this.readPointer = 0;
            this.writePointer = getFileSize(fcb); // 写指针初始在文件末尾
        }

        private long getFileSize(FCB fcb) {
            return fcb.getFileLength() * DiskManager.BLOCK_SIZE;
        }
    }

    public void createFile(String filePath, byte attributes) {//创建文件
        if ((attributes & DiskManager.READ_ONLY) != 0) {// 检查文件属性
            System.out.println("Error: Cannot create read-only file");
            return;
        }

        String parentPath = directoryManager.getParentPath(filePath);
        String fileName = directoryManager.getFileName(filePath);
        if (directoryManager.findDirectoryBlock(parentPath) == null) {// 检查父目录是否存在
            System.out.println("Error: Parent directory does not exist");
            return;
        }
        if (directoryManager.findFile(filePath) != null) {// 检查重名文件
            System.out.println("Error: File already exists");
            return;
        }

        byte startBlock = diskManager.getFatManager().allocateBlock();
        if (startBlock == -1) {
            System.out.println("Error: No free disk blocks available");
            return;
        }
        boolean success = directoryManager.addEntryToDirectory(parentPath, fileName, "TXT", attributes, startBlock, (byte)1);// 在目录中添加文件条目

        if (success) {
            FCB newFCB = directoryManager.findFile(filePath);
            OpenFileTableEntry entry = new OpenFileTableEntry(filePath, DiskManager.WRITE_MODE, newFCB);// 填写已打开文件表
            openFileTable.put(filePath, entry);

            System.out.println("File created successfully: " + filePath);
        } else {
            diskManager.getFatManager().freeBlock(startBlock);
            System.out.println("Error: Failed to create file " + filePath);
        }
    }

    public void openFile(String filePath, int mode) {//打开文件
        FCB fcb = directoryManager.findFile(filePath);
        if (fcb == null) {// 检查文件是否存在
            System.out.println("Error: File not found - " + filePath);
            return;
        }
        if (mode == DiskManager.WRITE_MODE && fcb.isReadOnly()) {// 检查打开方式
            System.out.println("Error: Cannot open read-only file in write mode");
            return;
        }
        if (openFileTable.containsKey(filePath)) { // 检查文件是否已经打开
            System.out.println("File is already opened: " + filePath);
            return;
        }
        OpenFileTableEntry entry = new OpenFileTableEntry(filePath, mode, fcb);// 填写已打开文件表
        openFileTable.put(filePath, entry);

        System.out.println("File opened successfully: " + filePath + " in " +
                (mode == DiskManager.READ_MODE ? "read" : "write") + " mode");
    }

    public void readFile(String filePath, int length) {//读文件
        OpenFileTableEntry entry = openFileTable.get(filePath); // 检查文件是否在已打开文件表中
        if (entry == null) {
            openFile(filePath, DiskManager.READ_MODE);// 如果不存在，先打开文件（默认读模式）
            entry = openFileTable.get(filePath);
            if (entry == null) return;
        }
        if (entry.mode == DiskManager.WRITE_MODE) {// 检查打开模式
            System.out.println("Error: Cannot read file opened in write mode");
            return;
        }

        System.out.println("Reading " + length + " bytes from file: " + filePath);

        byte currentBlock = entry.fcb.getStartBlock();
        int totalBytesRead = 0;


        long targetPosition = entry.readPointer;// 定位到读指针位置
        int startBlockOffset = (int)(targetPosition / DiskManager.BLOCK_SIZE);
        int byteOffset = (int)(targetPosition % DiskManager.BLOCK_SIZE);

        for (int i = 0; i < startBlockOffset && currentBlock != -1; i++) {// 跳过前面的块
            currentBlock = diskManager.getFatManager().getNextBlock(currentBlock);
        }
        while (currentBlock != -1 && currentBlock != DiskManager.END_OF_FILE && totalBytesRead < length) {// 读取数据
            byte[] blockData = diskManager.readBlock(currentBlock); // 读取块到缓冲区
            if (blockData == null) break;
            int bytesToRead = Math.min(length - totalBytesRead, DiskManager.BLOCK_SIZE - byteOffset);// 从缓冲区读取数据
            for (int i = 0; i < bytesToRead; i++) {
                byte b = blockData[byteOffset + i];
                if (b == (byte)'#') { // 遇到文件结束符
                    System.out.println("Reached end of file");
                    entry.readPointer += totalBytesRead + i;
                    return;
                }
                System.out.print((char)b);
                totalBytesRead++;
            }

            System.out.println(); // 换行

            currentBlock = diskManager.getFatManager().getNextBlock(currentBlock);
            byteOffset = 0; // 后续块从开始读取
        }
        entry.readPointer += totalBytesRead;// 更新读指针
        System.out.println("Total bytes read: " + totalBytesRead);
    }

    public void writeFile(String filePath, byte[] buffer, int length) {//写文件
        OpenFileTableEntry entry = openFileTable.get(filePath);// 检查文件是否在已打开文件表中
        if (entry == null) {
            openFile(filePath, DiskManager.WRITE_MODE);// 如果不存在，先打开文件（默认写模式）
            entry = openFileTable.get(filePath);
            if (entry == null) return;
        }
        if (entry.mode != DiskManager.WRITE_MODE) {// 检查打开模式
            System.out.println("Error: Cannot write to file opened in read mode");
            return;
        }

        System.out.println("Writing " + length + " bytes to file: " + filePath);


        byte currentBlock = entry.fcb.getStartBlock(); // 从写指针位置开始写入（追加方式）
        int blocksUsed = 0;
        byte lastBlock = currentBlock;// 找到最后一个块
        while (currentBlock != -1 && currentBlock != DiskManager.END_OF_FILE) {
            lastBlock = currentBlock;
            currentBlock = diskManager.getFatManager().getNextBlock(currentBlock);
            if (currentBlock != -1 && currentBlock != DiskManager.END_OF_FILE) {
                blocksUsed++;
            }
        }

        currentBlock = lastBlock;
        int byteOffset = (int)(entry.writePointer % DiskManager.BLOCK_SIZE);


        int dataIndex = 0;
        while (dataIndex < length) {
            byte[] blockData;
            if (byteOffset == 0) {
                byte newBlock = diskManager.getFatManager().allocateBlock();
                if (newBlock == -1) {
                    System.out.println("Error: No free blocks available");
                    break;
                }

                if (currentBlock != -1) {
                    diskManager.getFatManager().setNextBlock(currentBlock, newBlock);
                }

                blockData = new byte[DiskManager.BLOCK_SIZE];
                currentBlock = newBlock;
                blocksUsed++;
            } else {
                blockData = diskManager.readBlock(currentBlock);
            }
            int bytesToWrite = Math.min(length - dataIndex, DiskManager.BLOCK_SIZE - byteOffset); // 计算当前需要写入的字节数，确保不会超出数据总长度
            System.arraycopy(buffer, dataIndex, blockData, byteOffset, bytesToWrite);// 写入数据到缓冲区
            diskManager.writeBlock(currentBlock, blockData);// 写满缓冲区后写入磁盘

            dataIndex += bytesToWrite;
            byteOffset = (byteOffset + bytesToWrite) % DiskManager.BLOCK_SIZE;
        }

        entry.fcb.setFileLength((byte)(blocksUsed + 1));// 更新文件长度和写指针
        entry.writePointer += length;

        System.out.println("File written successfully. Total blocks: " + (blocksUsed + 1));
    }

    public void closeFile(String filePath) {//关闭文件
        OpenFileTableEntry entry = openFileTable.get(filePath);
        if (entry == null) {
            System.out.println("File is not opened: " + filePath);
            return;
        }
        if (entry.mode == DiskManager.WRITE_MODE) {// 如果是以写方式打开，追加文件结束符
            byte[] endMarker = {'#'};// 在文件末尾写入结束符'#'
            writeFile(filePath, endMarker, 1);
        }
        openFileTable.remove(filePath);// 从已打开文件表中删除
        System.out.println("File closed: " + filePath);
    }

    public void deleteFile(String filePath) {//删除文件
        FCB fcb = directoryManager.findFile(filePath);
        if (fcb == null) {// 检查文件是否存在
            System.out.println("Error: File not found - " + filePath);
            return;
        }
        if (openFileTable.containsKey(filePath)) {// 检查文件是否打开
            System.out.println("Error: Cannot delete opened file");
            return;
        }
        diskManager.getFatManager().freeFileBlocks(fcb.getStartBlock());// 释放文件占用的所有磁盘块
        boolean success = directoryManager.removeEntryFromDirectory(filePath);// 从目录中删除条目
        if (success) {
            System.out.println("File deleted successfully: " + filePath);
        } else {
            System.out.println("Error: Failed to delete file " + filePath);
        }
    }

    public void typeFile(String filePath) {//显示文件内容
        FCB fcb = directoryManager.findFile(filePath);
        if (fcb == null) {// 检查文件是否存在
            System.out.println("Error: File not found - " + filePath);
            return;
        }
        if (openFileTable.containsKey(filePath)) { // 检查文件是否打开
            System.out.println("Error: Cannot display content of opened file");
            return;
        }

        System.out.println("Content of file: " + filePath);
        byte currentBlock = fcb.getStartBlock();

        while (currentBlock != -1 && currentBlock != DiskManager.END_OF_FILE) { // 当前块不是文件结束标记时继续读取
            byte[] blockData = diskManager.readBlock(currentBlock);
            if (blockData != null) {
                for (int i = 0; i < DiskManager.BLOCK_SIZE; i++) {
                    if (blockData[i] == (byte)'#') {
                        System.out.println("\n[End of file]");
                        return;
                    }
                    if (blockData[i] != 0) {
                        System.out.print((char)blockData[i]);
                    }
                }
            }
            currentBlock = diskManager.getFatManager().getNextBlock(currentBlock);
        }
        System.out.println();
    }

    public void change(String filePath, byte newAttributes) {//改变文件属性
        FCB fcb = directoryManager.findFile(filePath);
        if (fcb == null) {// 检查文件是否存在
            System.out.println("Error: File not found - " + filePath);
            return;
        }
        if (openFileTable.containsKey(filePath)) {// 检查文件是否打开
            System.out.println("Error: Cannot change attributes of opened file");
            return;
        }
        fcb.setAttributes(newAttributes);
        System.out.println("File attributes changed for: " + filePath);
        System.out.println("New attributes: " + newAttributes);
    }

    public void createCatalog(String dirPath) {//建立目录
        String parentPath = directoryManager.getParentPath(dirPath);
        String dirName = directoryManager.getDirectoryName(dirPath);
        if (directoryManager.findDirectoryBlock(parentPath) == null) {// 检查父目录是否存在
            System.out.println("Error: Parent directory does not exist");
            return;
        }
        if (directoryManager.findDirectoryBlock(dirPath) != null) { // 检查是否存在同名目录
            System.out.println("Error: Directory already exists");
            return;
        }
        byte startBlock = diskManager.getFatManager().allocateBlock();// 分配磁盘块给新目录
        if (startBlock == -1) {
            System.out.println("Error: No free disk blocks available");
            return;
        }
        boolean success = directoryManager.addEntryToDirectory(// 在父目录中添加目录条目
                parentPath, dirName, "  ", DiskManager.DIRECTORY, startBlock, (byte)1
        );

        if (success) {
            for (int i = 0; i < DiskManager.DIRECTORY_ENTRIES_PER_BLOCK; i++) {// 初始化新目录（填充8个空目录项）
                FCB emptyFCB = new FCB();
                diskManager.writeFCBToBlock(startBlock, i, emptyFCB);
            }
            System.out.println("Directory created successfully: " + dirPath);
        } else {
            diskManager.getFatManager().freeBlock(startBlock);
            System.out.println("Error: Failed to create directory " + dirPath);
        }
    }

    public void showCategory(String dirPath) {//显示目录内容
        if (directoryManager.findDirectoryBlock(dirPath) == null) {// 检查目录是否存在
            System.out.println("Error: Directory not found - " + dirPath);
            return;
        }

        List<FCB> contents = directoryManager.listDirectory(dirPath);

        if (contents.isEmpty()) {
            System.out.println("Directory is empty: " + dirPath);
            return;
        }

        System.out.println("Contents of directory: " + dirPath);
        for (FCB entry : contents) {
            System.out.println(entry.toString());
        }
    }
    public void deleteCategory(String dirPath) {//删除空目录
        if (dirPath.equals("/")) {// 检查是否是根目录
            System.out.println("Error: Cannot delete root directory");
            return;
        }
        Integer dirBlock = directoryManager.findDirectoryBlock(dirPath);//通过目录管理器根据给定的目录路径查找相应的目录块信息
        if (dirBlock == null) {
            System.out.println("Error: Directory not found - " + dirPath);
            return;
        }
        if (!directoryManager.isDirectoryEmpty(dirBlock)) { // 检查目录是否为空
            System.out.println("Error: Directory is not empty - " + dirPath);
            return;
        }
        boolean success = directoryManager.removeEntryFromDirectory(dirPath);// 从父目录中删除条目
        if (success) {
            diskManager.getFatManager().freeBlock((byte)dirBlock.intValue());// 释放目录占用的磁盘块
            System.out.println("Directory removed successfully: " + dirPath);
        } else {
            System.out.println("Error: Failed to remove directory " + dirPath);
        }
    }
}