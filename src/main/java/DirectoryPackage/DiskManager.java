package DirectoryPackage;
import  FileEditPackage.FCB;

public class DiskManager {
    // 系统常量
    public static final int TOTAL_BLOCKS = 128; //硬盘总块数
    public static final int BLOCK_SIZE = 64;    //块大小
    public static final int FAT_BLOCKS = 2;     //FAT表所占块数
    public static final int ROOT_BLOCK = 2;     //根块数
    public static final int DIRECTORY_ENTRIES_PER_BLOCK = 8;    //
    public static final int ENTRY_SIZE = 8;     //

    // FAT表常量
    public static final byte FREE_BLOCK = 0;            // 空闲块标志
    public static final byte END_OF_FILE = (byte) 255;  // 结束块标志
    public static final byte BAD_BLOCK = (byte) 254;    // 损坏块标志

    // 文件属性常量
    public static final byte READ_ONLY = 1;      // 只读文件
    public static final byte SYSTEM_FILE = 2;    // 系统文件
    public static final byte NORMAL_FILE = 4;    // 普通文件
    public static final byte DIRECTORY = 8;      // 目录登记项

    // 打开文件模式
    public static final int READ_MODE = 0;  //只读模式
    public static final int WRITE_MODE = 1; //只写模式

    public class FATManager{//用于管理FAT表的内部类

        private byte[] fat;

        private void initializeFAT() {//初始化FAT表
            for (int i = 0; i < TOTAL_BLOCKS; i++) {
                fat[i] = FREE_BLOCK;
            }
            // 标记系统区域
            fat[0] = END_OF_FILE;  // FAT表自身
            fat[1] = END_OF_FILE;  // FAT表自身
            fat[2] = END_OF_FILE;  // 根目录
            // 标记坏块
            fat[23] = BAD_BLOCK;
            fat[49] = BAD_BLOCK;
            // 将FAT表写入磁盘
            updateFATOnDisk();
        }

        public byte allocateBlock() {//分配空闲磁盘块
            for (byte i = 3; i < TOTAL_BLOCKS; i++) {
                if (fat[i] == FREE_BLOCK) {
                    fat[i] = END_OF_FILE;
                    updateFATOnDisk();
                    return i;
                }
            }
            return -1;  // 没有空闲块
        }

        public void freeBlock(byte blockNum) {//释放磁盘块
            if (blockNum >= 3 && blockNum < TOTAL_BLOCKS) {
                fat[blockNum] = FREE_BLOCK;
                updateFATOnDisk();
            }
        }

        public void freeFileBlocks(byte startBlock) {//释放文件的所有磁盘块
            byte currentBlock = startBlock;
            while (currentBlock != -1 && currentBlock != END_OF_FILE) {
                byte nextBlock = fat[currentBlock];
                freeBlock(currentBlock);
                currentBlock = nextBlock;
            }
        }

        public byte getNextBlock(byte currentBlock) {//获取下一个磁盘块
            if (currentBlock < 0 || currentBlock >= TOTAL_BLOCKS) {
                return -1;
            }
            byte next = fat[currentBlock];
            return (next == END_OF_FILE || next == FREE_BLOCK) ? -1 : next;
        }

        public void setNextBlock(byte currentBlock, byte nextBlock) {//设置下一个磁盘块
            if (currentBlock >= 0 && currentBlock < TOTAL_BLOCKS) {
                fat[currentBlock] = nextBlock;
                updateFATOnDisk();
            }
        }

        private void updateFATOnDisk() {//更新磁盘上的FAT表
            System.arraycopy(fat, 0, disk[0], 0, BLOCK_SIZE);
            System.arraycopy(fat, BLOCK_SIZE, disk[1], 0, BLOCK_SIZE);
        }

        public String getFATStatus() {//获取FAT表状态
            StringBuilder status = new StringBuilder();
            status.append("FAT Table Status:\n");
            for (int i = 0; i < TOTAL_BLOCKS; i++) {
                String blockStatus;
                if (i < FAT_BLOCKS) {
                    blockStatus = "FAT System";
                } else if (fat[i] == FREE_BLOCK) {
                    blockStatus = "Free";
                } else if (fat[i] == END_OF_FILE) {
                    blockStatus = "End of File";
                } else if (fat[i] == BAD_BLOCK) {
                    blockStatus = "Bad Block";
                } else {
                    blockStatus = "Used -> Block " + fat[i];
                }
                status.append(String.format("Block %3d: %3d (%s)\n", i, fat[i] & 0xFF, blockStatus));
            }
            return status.toString();
        }

        public byte[] getFAT() {
            return fat;
        }
    }

    //成员变量
    private byte[][] disk;
    private FATManager FM;

    public DiskManager() {
        this.disk = new byte[TOTAL_BLOCKS][BLOCK_SIZE];
        initializeDisk();
        this.FM = new FATManager();
    }

    private void initializeDisk() { //初始化磁盘
        FM.initializeFAT();
        for (int i = 0; i < TOTAL_BLOCKS; i++) {
            for (int j = 0; j < BLOCK_SIZE; j++) {
                disk[i][j] = 0;
            }
        }
    }

    public byte[] readBlock(int blockNum) {//读取磁盘块
        if (blockNum < 0 || blockNum >= TOTAL_BLOCKS) {
            return null;
        }
        return disk[blockNum].clone();
    }

    public boolean writeBlock(int blockNum, byte[] data) {//写入磁盘块
        if (blockNum < 0 || blockNum >= TOTAL_BLOCKS || data.length != BLOCK_SIZE) {
            return false;
        }
        System.arraycopy(data, 0, disk[blockNum], 0, BLOCK_SIZE);
        return true;
    }

    public void writeFCBToBlock(int blockNum, int index, FCB fcb) {//将FCB写入磁盘块
        int baseOffset = index * ENTRY_SIZE;

        // 写入文件名 (3字节)
        for (int i = 0; i < 3 && i < fcb.getFileName().length(); i++) {
            disk[blockNum][baseOffset + i] = (byte) fcb.getFileName().charAt(i);
        }

        // 写入文件类型 (2字节)
        for (int i = 0; i < 2 && i < fcb.getFileType().length(); i++) {
            disk[blockNum][baseOffset + 3 + i] = (byte) fcb.getFileType().charAt(i);
        }

        // 写入属性、起始块、文件长度
        disk[blockNum][baseOffset + 5] = fcb.getAttributes();
        disk[blockNum][baseOffset + 6] = fcb.getStartBlock();
        disk[blockNum][baseOffset + 7] = fcb.getFileLength();
    }

    public FCB readFCBFromBlock(int blockNum, int index) {//从磁盘块读取FCB
        int baseOffset = index * ENTRY_SIZE;
        FCB fcb = new FCB();

        // 读取文件名
        StringBuilder fileName = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            byte b = disk[blockNum][baseOffset + i];
            if (b != 0) fileName.append((char) b);
        }
        fcb.setFileName(fileName.toString());

        // 读取文件类型
        StringBuilder fileType = new StringBuilder();
        for (int i = 0; i < 2; i++) {
            byte b = disk[blockNum][baseOffset + 3 + i];
            if (b != 0) fileType.append((char) b);
        }
        fcb.setFileType(fileType.toString());

        // 读取属性、起始块、文件长度
        fcb.setAttributes(disk[blockNum][baseOffset + 5]);
        fcb.setStartBlock(disk[blockNum][baseOffset + 6]);
        fcb.setFileLength(disk[blockNum][baseOffset + 7]);

        return fcb;
    }

    public void writeDirectoryEntryToBlock(int blockNum, int index, DirectoryEntry entry) {//写入目录项到磁盘块
        int baseOffset = index * ENTRY_SIZE;

        // 写入目录名
        for (int i = 0; i < 3 && i < entry.getDirName().length(); i++) {
            disk[blockNum][baseOffset + i] = (byte) entry.getDirName().charAt(i);
        }

        // 保留2字节为空格
        disk[blockNum][baseOffset + 3] = ' ';
        disk[blockNum][baseOffset + 4] = ' ';

        // 写入属性、起始块
        disk[blockNum][baseOffset + 5] = entry.getAttributes();
        disk[blockNum][baseOffset + 6] = entry.getStartBlock();

        // 保留1字节为0
        disk[blockNum][baseOffset + 7] = 0;
    }

    public DirectoryEntry readDirectoryEntryFromBlock(int blockNum, int index) {//从磁盘块读取目录项
        int baseOffset = index * ENTRY_SIZE;
        DirectoryEntry entry = new DirectoryEntry();

        // 读取目录名
        StringBuilder dirName = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            byte b = disk[blockNum][baseOffset + i];
            if (b != 0) dirName.append((char) b);
        }
        entry.setDirName(dirName.toString());

        // 读取属性、起始块
        entry.setAttributes(disk[blockNum][baseOffset + 5]);
        entry.setStartBlock(disk[blockNum][baseOffset + 6]);

        return entry;
    }

    public String getDiskUsage() {//获取磁盘使用情况
        int usedBlocks = 0;
        int freeBlocks = 0;
        int badBlocks = 0;
        int systemBlocks = 3; // 块0,1,2

        byte[] fat = FM.getFAT();
        for (int i = 0; i < TOTAL_BLOCKS; i++) {
            if (i < systemBlocks) {
                continue;
            }
            if (fat[i] == FREE_BLOCK) {
                freeBlocks++;
            } else if (fat[i] == BAD_BLOCK) {
                badBlocks++;
            } else {
                usedBlocks++;
            }
        }

        return String.format("Disk Usage: Total=%d, Used=%d, Free=%d, Bad=%d, System=%d",
                TOTAL_BLOCKS, usedBlocks, freeBlocks, badBlocks, systemBlocks);
    }

    public FATManager getFatManager() {
        return FM;
    }

    public byte[][] getDisk() {
        return disk;
    }

}
