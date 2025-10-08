package DirectoryPackage;

public class DirectoryEntry {
    private String dirName;         // 目录名 (3字节)
    private byte attributes;        // 目录属性 (1字节)
    private byte startBlock;        // 起始盘块号 (1字节)

    public DirectoryEntry() {
        this.dirName = "$$$";  // 空白目录项标记
        this.attributes = DiskManager.DIRECTORY;
        this.startBlock = 0;
    }

    public DirectoryEntry(String dirName, byte startBlock) {
        this.dirName = dirName;
        this.attributes = DiskManager.DIRECTORY;
        this.startBlock = startBlock;
    }

    // Getters and Setters
    public String getDirName() { return dirName; }
    public void setDirName(String dirName) { this.dirName = dirName; }

    public byte getAttributes() { return attributes; }
    public void setAttributes(byte attributes) { this.attributes = attributes; }

    public byte getStartBlock() { return startBlock; }
    public void setStartBlock(byte startBlock) { this.startBlock = startBlock; }

    @Override
    public String toString() {
        return String.format("%s/ <DIR> Block:%d", dirName.trim(), startBlock);
    }
}