package DirectoryPackage;

import FileEditPackage.FCB;
import java.util.ArrayList;
import java.util.List;

public class DirectoryManager {
    //成员变量
    private DiskManager diskManager;

    public DirectoryManager(DiskManager diskManager) {//构造函数
        this.diskManager = diskManager;
    }

    public void initializeRootDirectory() {//初始化根目录
        // 根目录占用块2，包含8个目录项
        for (int i = 0; i < DiskManager.DIRECTORY_ENTRIES_PER_BLOCK; i++) {
            FCB emptyFCB = new FCB();
            diskManager.writeFCBToBlock(DiskManager.ROOT_BLOCK, i, emptyFCB);
        }
    }

    public FCB findFile(String filePath) {//查找文件
        String parentPath = getParentPath(filePath);
        String fileName = getFileName(filePath);

        Integer parentBlock = findDirectoryBlock(parentPath);
        if (parentBlock == null) return null;

        for (int i = 0; i < DiskManager.DIRECTORY_ENTRIES_PER_BLOCK; i++) {
            FCB entry = diskManager.readFCBFromBlock(parentBlock, i);
            if (entry.getFileName().trim().equals(fileName) && !entry.isDirectory()) {
                return entry;
            }
        }
        return null;
    }

    public Integer findDirectoryBlock(String dirPath) {//查找目录
        if (dirPath.equals("/")) return DiskManager.ROOT_BLOCK;

        String[] pathComponents = dirPath.substring(1).split("/");
        int currentBlock = DiskManager.ROOT_BLOCK;

        for (String component : pathComponents) {
            boolean found = false;
            for (int i = 0; i < DiskManager.DIRECTORY_ENTRIES_PER_BLOCK; i++) {
                FCB entry = diskManager.readFCBFromBlock(currentBlock, i);
                if (entry.getFileName().trim().equals(component) && entry.isDirectory()) {
                    currentBlock = entry.getStartBlock();
                    found = true;
                    break;
                }
            }
            if (!found) return null;
        }
        return currentBlock;
    }

    public boolean addEntryToDirectory(String dirPath, String name, String type,
                                       byte attributes, byte startBlock, byte length) {
        //在目录中添加条目
        Integer dirBlock = findDirectoryBlock(dirPath);
        if (dirBlock == null) return false;

        // 查找空闲目录项
        for (int i = 0; i < DiskManager.DIRECTORY_ENTRIES_PER_BLOCK; i++) {
            FCB entry = diskManager.readFCBFromBlock(dirBlock, i);
            if (entry.getFileName().equals("$$$")) {
                // 找到空闲位置
                FCB newEntry = new FCB(name, type, attributes, startBlock, length);
                diskManager.writeFCBToBlock(dirBlock, i, newEntry);
                return true;
            }
        }
        return false; // 目录已满
    }

    public boolean removeEntryFromDirectory(String entryPath) {//从目录中删除条目
        String parentPath = getParentPath(entryPath);
        String entryName = getFileName(entryPath);

        Integer parentBlock = findDirectoryBlock(parentPath);
        if (parentBlock == null) return false;

        for (int i = 0; i < DiskManager.DIRECTORY_ENTRIES_PER_BLOCK; i++) {
            FCB entry = diskManager.readFCBFromBlock(parentBlock, i);
            if (entry.getFileName().trim().equals(entryName)) {
                // 标记为空闲
                FCB emptyEntry = new FCB();
                diskManager.writeFCBToBlock(parentBlock, i, emptyEntry);
                return true;
            }
        }
        return false;
    }

    public List<FCB> listDirectory(String dirPath) {//列出目录内容
        List<FCB> contents = new ArrayList<>();

        Integer dirBlock = findDirectoryBlock(dirPath);
        if (dirBlock == null) return contents;

        for (int i = 0; i < DiskManager.DIRECTORY_ENTRIES_PER_BLOCK; i++) {
            FCB entry = diskManager.readFCBFromBlock(dirBlock, i);
            if (!entry.getFileName().equals("$$$")) {
                contents.add(entry);
            }
        }
        return contents;
    }

    public boolean isDirectoryEmpty(int dirBlock) {//检查目录是否为空
        for (int i = 0; i < DiskManager.DIRECTORY_ENTRIES_PER_BLOCK; i++) {
            FCB entry = diskManager.readFCBFromBlock(dirBlock, i);
            if (!entry.getFileName().equals("$$$")) {
                return false;
            }
        }
        return true;
    }

    //路径处理工具方法
    public String getParentPath(String path) {
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash == 0) return "/";
        return path.substring(0, lastSlash);
    }

    public String getFileName(String filePath) {
        int lastSlash = filePath.lastIndexOf('/');
        return filePath.substring(lastSlash + 1);
    }

    public String getDirectoryName(String dirPath) {
        int lastSlash = dirPath.lastIndexOf('/');
        return dirPath.substring(lastSlash + 1);
    }
}