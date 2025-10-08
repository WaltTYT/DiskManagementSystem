package FileEditPackage;

import  DirectoryPackage.DirectoryManager;
import DirectoryPackage.DiskManager;

public class FCB {
    private String fileName;        // 文件名 (3字节)
    private String fileType;        // 文件类型 (2字节)
    private byte attributes;        // 文件属性 (1字节)
    private byte startBlock;        // 起始盘块号 (1字节)
    private byte fileLength;        // 文件长度 (块数) (1字节)

    public FCB() {
        this.fileName = "$$$";  // 空白目录项标记
        this.fileType = "  ";
        this.attributes = 0;
        this.startBlock = 0;
        this.fileLength = 0;
    }

    public FCB(String fileName, String fileType, byte attributes, byte startBlock, byte fileLength) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.attributes = attributes;
        this.startBlock = startBlock;
        this.fileLength = fileLength;
    }

    // Getters and Setters
    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public byte getAttributes() {
        return attributes;
    }
    public void setAttributes(byte attributes) {
        this.attributes = attributes;
    }

    public byte getStartBlock() {
        return startBlock;
    }
    public void setStartBlock(byte startBlock) {
        this.startBlock = startBlock;
    }

    public byte getFileLength() {
        return fileLength; }
    public void setFileLength(byte fileLength) {
        this.fileLength = fileLength;
    }

    public boolean isDirectory() {
        return (attributes & DiskManager.DIRECTORY) != 0;
    }

    public boolean isReadOnly() {

        return (attributes & DiskManager.READ_ONLY) != 0;
    }

    @Override
    public String toString() {
        if (fileName.equals("$$$")) return "Empty Entry";

        if (isDirectory()) {
            return String.format("%s/ <DIR>", fileName.trim());
        } else {
            return String.format("%s.%s [%d blocks] Attr:%d",
                    fileName.trim(), fileType.trim(), fileLength, attributes);
        }
    }
}