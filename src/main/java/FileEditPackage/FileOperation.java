package FileEditPackage;

import DirectoryPackage.DirectoryManager;
import DirectoryPackage.DiskManager;
import DirectoryPackage.DirectoryEntry;
import java.util.ArrayList;
import java.util.List;


public class FileOperation {
    // 磁盘管理器：负责磁盘块读写、FAT表操作、FCB持久化
    private final DiskManager diskManager;
    // 目录管理器：负责目录查找、创建、删除、条目维护
    private final DirectoryManager directoryManager;
    // 已打开文件表：存储当前处于打开状态的文件信息，避免重复打开和频繁查询目录
    private final List<OpenFileEntry> openFileTable;
    // 模拟磁盘I/O缓冲（文档要求定义两个缓冲）：缓冲1用于读操作，缓冲2用于写操作
    private final byte[] buffer1;
    private final byte[] buffer2;


    private static class OpenFileEntry {
        String filePath;       // 文件完整路径（如 "/doc/a.txt"），唯一标识文件
        String fileName;       // 文件名（不含路径，如 "a"），用于显示
        int openMode;          // 打开模式（DiskManager.READ_MODE/WRITE_MODE）
        int readPointer;       // 读指针（字节位置）：标记下一次读操作的起始位置
        int writePointer;      // 写指针（字节位置）：标记下一次写操作的起始位置
        FCB fcb;               // 关联的文件控制块：存储文件元数据（属性、起始块、长度等）
        boolean buffer1Dirty;  // 缓冲1脏标记：true表示缓冲数据未写回磁盘，关闭时需同步
        boolean buffer2Dirty;  // 缓冲2脏标记：同缓冲1，用于写操作

        // 构造方法：初始化已打开文件条目
        public OpenFileEntry(String filePath, String fileName, int openMode, FCB fcb) {
            this.filePath = filePath;
            this.fileName = fileName;
            this.openMode = openMode;
            this.readPointer = 0;  // 初始读指针从文件开头（0字节处）开始
            this.writePointer = 0; // 初始写指针从文件开头开始（后续追加时会调整到末尾）
            this.fcb = fcb;
            this.buffer1Dirty = false;
            this.buffer2Dirty = false;
        }
    }


    public FileOperation() {
        this.diskManager = new DiskManager();
        this.directoryManager = new DirectoryManager(this.diskManager);
        this.openFileTable = new ArrayList<>();
        // 缓冲大小与磁盘块大小一致（DiskManager.BLOCK_SIZE=64字节），符合文档要求
        this.buffer1 = new byte[DiskManager.BLOCK_SIZE];
        this.buffer2 = new byte[DiskManager.BLOCK_SIZE];
        // 系统初始化：创建根目录（占用块2，含8个空闲FCB条目）
        this.directoryManager.initializeRootDirectory();
    }


    public boolean createFile(String filePath, byte attributes) {
        //只读属性的文件不能建立
        if ((attributes & DiskManager.READ_ONLY) != 0) {
            System.out.println("错误：不能建立只读属性的文件");
            return false;
        }

        // 解析路径：拆分父目录路径和完整文件名（含类型）
        String parentPath = directoryManager.getParentPath(filePath);
        String fullFileName = directoryManager.getFileName(filePath);
        String[] nameParts = splitFileNameAndType(fullFileName); // 拆分[文件名, 类型]
        String fileName = nameParts[0]; // 文件名最多3字节（FCB限制）
        String fileType = nameParts[1]; // 文件类型最多2字节（FCB限制）

        // 父目录不存在，建立文件失败
        Integer parentBlockInt = directoryManager.findDirectoryBlock(parentPath);
        if (parentBlockInt == null) {
            System.out.println("错误：父目录 " + parentPath + " 不存在");
            return false;
        }

        // 父目录下有重名文件，建立失败
        if (directoryManager.findFile(filePath) != null) {
            System.out.println("错误：文件 " + filePath + " 已存在");
            return false;
        }

        // 寻找空闲存储块（至少一块），通过FAT表分配块
        byte startBlock = diskManager.getFatManager().allocateBlock();
        if (startBlock == -1) { // allocateBlock返回-1表示无空闲块
            System.out.println("错误：磁盘无空闲块");
            return false;
        }

        //建立文件目录项（FCB）并填写已打开文件表
        boolean addSuccess = directoryManager.addEntryToDirectory(
                parentPath, fileName, fileType, attributes, startBlock, (byte) 1
        );
        // 若目录已满（无空闲FCB条目），回滚已分配的磁盘块
        if (!addSuccess) {
            System.out.println("错误：目录 " + parentPath + " 已满，无法添加文件");
            diskManager.getFatManager().freeBlock(startBlock); // 释放已分配的块
            return false;
        }

        // 填写已打开文件表：创建后默认以写模式打开（便于后续写入内容）
        FCB newFCB = directoryManager.findFile(filePath);
        OpenFileEntry entry = new OpenFileEntry(filePath, fileName, DiskManager.WRITE_MODE, newFCB);
        openFileTable.add(entry);
        System.out.println("成功：文件 " + filePath + " 建立");
        return true;
    }
    public boolean openFile(String filePath, int openMode) {
        // 文件不存在，打开失败
        FCB targetFCB = directoryManager.findFile(filePath);
        if (targetFCB == null) {
            System.out.println("错误：文件 " + filePath + " 不存在");
            return false;
        }

        // 不能以写方式打开只读文件
        if (openMode == DiskManager.WRITE_MODE && targetFCB.isReadOnly()) {
            System.out.println("错误：文件 " + filePath + " 是只读文件，不能写");
            return false;
        }

        // 文档要求“文件已经打开则不需要填写已打开文件表”
        if (findOpenFileEntry(filePath) != null) {
            System.out.println("提示：文件 " + filePath + " 已打开");
            return true;
        }

        // 未打开则填写已打开文件表
        String fileName = directoryManager.getFileName(filePath);
        OpenFileEntry entry = new OpenFileEntry(filePath, fileName, openMode, targetFCB);
        openFileTable.add(entry);
        System.out.println("成功：文件 " + filePath + " 以" + (openMode == 0 ? "读" : "写") + "模式打开");
        return true;
    }

    public byte[] readFile(String filePath, int readLength) {
        // 已打开文件表中不存在该文件，则打开后再读
        OpenFileEntry entry = findOpenFileEntry(filePath);
        if (entry == null) {
            boolean openSuccess = openFile(filePath, DiskManager.READ_MODE); // 自动以读模式打开
            if (!openSuccess) return null;
            entry = findOpenFileEntry(filePath); // 重新获取已打开条目
        }

        // 以写方式打开文件，则不允许读
        if (entry.openMode != DiskManager.READ_MODE) {
            System.out.println("错误：文件 " + filePath + " 以写模式打开，不能读");
            return null;
        }

        FCB fcb = entry.fcb;
        // 计算文件总字节数（文件块数 × 块大小）
        int totalBytes = fcb.getFileLength() * DiskManager.BLOCK_SIZE;
        // 实际可读取字节数：取“期望长度”和“剩余未读字节数”的最小值
        int actualReadLen = Math.min(readLength, totalBytes - entry.readPointer);
        if (actualReadLen <= 0) { // 无数据可读（已到文件末尾）
            System.out.println("提示：已到达文件末尾或无数据可读");
            return new byte[0];
        }

        // 初始化结果数组（存储读取到的数据）
        byte[] result = new byte[actualReadLen];
        int resultIdx = 0; // 结果数组的当前写入索引
        int currentPtr = entry.readPointer; // 当前读指针（从上次结束位置继续读）

        // 从已打开文件表中读出读指针，从该位置读取
        while (resultIdx < actualReadLen) {
            // 计算当前读操作对应的磁盘块号和块内偏移（指针→块映射）
            int blockIndex = currentPtr / DiskManager.BLOCK_SIZE; // 第N个数据块（从0开始）
            int blockOffset = currentPtr % DiskManager.BLOCK_SIZE; // 块内偏移（0~63字节）
            byte dataBlockNum = (byte) (fcb.getStartBlock() + blockIndex); // 实际磁盘块号

            //读操作需磁盘块读入主存缓冲后处理
            byte[] blockData = diskManager.readBlock(dataBlockNum);
            if (blockData == null) { // 磁盘块读取失败（如坏块）
                System.out.println("错误：读取磁盘块 " + dataBlockNum + " 失败");
                break;
            }
            System.arraycopy(blockData, 0, buffer1, 0, DiskManager.BLOCK_SIZE); // 复制到缓冲1

            // 从缓冲中读取数据，直到填满结果或遇到文件结束符
            while (blockOffset < DiskManager.BLOCK_SIZE && resultIdx < actualReadLen) {
                byte b = buffer1[blockOffset];
                // 用#表示文件结束，遇到#终止操作
                if (b == '#') {
                    System.out.println("提示：遇到文件结束符#，终止读取");
                    actualReadLen = resultIdx; // 截断结果（只保留已读数据）
                    break;
                }
                result[resultIdx++] = b; // 写入结果数组
                currentPtr++; // 更新读指针（字节位置+1）
                blockOffset++; // 更新块内偏移
            }
        }

        // 更新已打开文件表中的读指针（下次读从当前位置继续）
        entry.readPointer = currentPtr;
        System.out.println("成功：从文件 " + filePath + " 读取 " + actualReadLen + " 字节");
        return result;
    }
    public boolean writeFile(String filePath, byte[] data, int writeLength) {
        // 已打开文件表中不存在该文件，则打开后再写
        OpenFileEntry entry = findOpenFileEntry(filePath);
        if (entry == null) {
            boolean openSuccess = openFile(filePath, DiskManager.WRITE_MODE); // 自动以写模式打开
            if (!openSuccess) return false;
            entry = findOpenFileEntry(filePath); // 重新获取已打开条目
        }

        //非写方式打开文件，不能写
        if (entry.openMode != DiskManager.WRITE_MODE) {
            System.out.println("错误：文件 " + filePath + " 非写模式，不能写");
            return false;
        }

        FCB fcb = entry.fcb;
        int currentPtr = entry.writePointer; // 当前写指针
        int dataIdx = 0; // 待写入数据的当前读取索引

        //打开后的写入仅支持从文件末尾追加
        currentPtr = fcb.getFileLength() * DiskManager.BLOCK_SIZE; // 写指针强制到文件末尾
        entry.writePointer = currentPtr;

        // 写操作要写满缓冲后才写入磁盘
        while (dataIdx < writeLength) {
            // 计算当前写操作对应的磁盘块号和块内偏移
            int blockIndex = currentPtr / DiskManager.BLOCK_SIZE;
            int blockOffset = currentPtr % DiskManager.BLOCK_SIZE;
            byte dataBlockNum = (byte) (fcb.getStartBlock() + blockIndex);

            // 当前块超出文件现有块数（需要分配新块）
            if (blockIndex >= fcb.getFileLength()) {
                byte newBlock = diskManager.getFatManager().allocateBlock();
                if (newBlock == -1) { // 无空闲块，写入失败
                    System.out.println("错误：磁盘无空闲块，写入失败");
                    return false;
                }
                // 更新FAT表：链接新块（前一块指向新块，新块标记为文件结束）
                if (blockIndex > 0) { // 非第一个块，需设置前一块的下一块指针
                    // 修正：将int类型的(dataBlockNum-1)强转为byte，匹配setNextBlock参数类型
                    diskManager.getFatManager().setNextBlock((byte) (dataBlockNum - 1), newBlock);
                }
                diskManager.getFatManager().setNextBlock(newBlock, DiskManager.END_OF_FILE);
                // 更新FCB：文件块数+1
                fcb.setFileLength((byte) (fcb.getFileLength() + 1));
                dataBlockNum = newBlock; // 新块作为当前写入块
            }

            // 当前块是文件已有块（需先读取现有数据到缓冲，避免覆盖）
            if (blockIndex < fcb.getFileLength() - 1) {
                byte[] blockData = diskManager.readBlock(dataBlockNum);
                if (blockData != null) {
                    System.arraycopy(blockData, 0, buffer2, 0, DiskManager.BLOCK_SIZE);
                }
            }

            // 计算本次写入缓冲的字节数（不超过缓冲剩余空间和待写数据长度）
            int writeToBufferLen = Math.min(
                    writeLength - dataIdx,
                    DiskManager.BLOCK_SIZE - blockOffset
            );
            // 将待写数据写入缓冲2
            System.arraycopy(data, dataIdx, buffer2, blockOffset, writeToBufferLen);
            entry.buffer2Dirty = true; // 标记缓冲脏（数据未写回磁盘）

            // 写满缓冲或数据写完，将缓冲数据刷到磁盘
            if (blockOffset + writeToBufferLen == DiskManager.BLOCK_SIZE || dataIdx + writeToBufferLen == writeLength) {
                boolean writeSuccess = diskManager.writeBlock(dataBlockNum, buffer2);
                if (!writeSuccess) { // 磁盘写入失败
                    System.out.println("错误：写入磁盘块 " + dataBlockNum + " 失败");
                    return false;
                }
                entry.buffer2Dirty = false; // 清除脏标记（数据已同步到磁盘）
            }

            // 更新索引和指针
            dataIdx += writeToBufferLen; // 待写数据索引后移
            currentPtr += writeToBufferLen; // 写指针后移
            entry.writePointer = currentPtr; // 更新已打开文件表中的写指针
        }

        System.out.println("成功：向文件 " + filePath + " 写入 " + dataIdx + " 字节");
        return true;
    }


    public boolean closeFile(String filePath) {
        // 文件未打开，不用关闭
        OpenFileEntry entry = findOpenFileEntry(filePath);
        if (entry == null) {
            System.out.println("提示：文件 " + filePath + " 未打开");
            return true;
        }

        // 写方式打开的文件，要追加文件结束符#，修改目录项
        if (entry.openMode == DiskManager.WRITE_MODE) {
            // 追加文件结束符#（标记文件末尾）
            byte[] eof = new byte[]{'#'};
            writeFile(filePath, eof, 1);
            // 同步缓冲中未写回的数据（避免数据丢失）
            if (entry.buffer1Dirty) {
                int blockNum = entry.fcb.getStartBlock() + (entry.writePointer / DiskManager.BLOCK_SIZE);
                diskManager.writeBlock(blockNum, buffer1);
            }
            if (entry.buffer2Dirty) {
                int blockNum = entry.fcb.getStartBlock() + (entry.writePointer / DiskManager.BLOCK_SIZE);
                diskManager.writeBlock(blockNum, buffer2);
            }
            // 更新目录项
            String parentPath = directoryManager.getParentPath(filePath);
            String fullFileName = directoryManager.getFileName(filePath);
            String[] nameParts = splitFileNameAndType(fullFileName);
            directoryManager.removeEntryFromDirectory(filePath); // 删除旧目录项
            directoryManager.addEntryToDirectory( // 添加更新后的目录项
                    parentPath, nameParts[0], nameParts[1],
                    entry.fcb.getAttributes(), entry.fcb.getStartBlock(), entry.fcb.getFileLength()
            );
        }

        // 从已打开文件表中删除对应项
        openFileTable.remove(entry);
        System.out.println("成功：文件 " + filePath + " 已关闭");
        return true;
    }

    public boolean deleteFile(String filePath) {
        // 文件不存在，操作失败
        FCB targetFCB = directoryManager.findFile(filePath);
        if (targetFCB == null) {
            System.out.println("错误：文件 " + filePath + " 不存在");
            return false;
        }

        // 文件打开不能删除
        if (findOpenFileEntry(filePath) != null) {
            System.out.println("错误：文件 " + filePath + " 已打开，不能删除");
            return false;
        }

        // 删除文件目录项并归还文件所占磁盘空间
        boolean removeSuccess = directoryManager.removeEntryFromDirectory(filePath);
        if (!removeSuccess) { // 目录项删除失败（如目录损坏）
            System.out.println("错误：删除目录项失败");
            return false;
        }
        // 释放文件所有磁盘块（通过FAT表递归释放）
        diskManager.getFatManager().freeFileBlocks(targetFCB.getStartBlock());
        System.out.println("成功：文件 " + filePath + " 已删除");
        return true;
    }

    public String typeFile(String filePath) {
        // 文件不存在，指令执行失败
        FCB targetFCB = directoryManager.findFile(filePath);
        if (targetFCB == null) {
            return "错误：文件 " + filePath + " 不存在";
        }

        // 文件打开则不能显示文件内容
        if (findOpenFileEntry(filePath) != null) {
            return "错误：文件 " + filePath + " 已打开，无法显示";
        }

        // 拼接文件内容字符串
        StringBuilder content = new StringBuilder();
        content.append("文件内容：").append(filePath).append("\n");

        // 从目录中取出文件的起始盘块号，一块一块显示
        for (int i = 0; i < targetFCB.getFileLength(); i++) {
            byte blockNum = (byte) (targetFCB.getStartBlock() + i); // 第i个数据块
            byte[] blockData = diskManager.readBlock(blockNum);
            if (blockData == null) break; // 块读取失败，终止显示

            // 转换字节为字符，遇到#终止
            for (byte b : blockData) {
                if (b == '#') { // 文档要求“#表示文件结束”
                    content.append("\n（文件结束）");
                    return content.toString();
                }
                if (b != 0) { // 跳过空字节（避免显示乱码）
                    content.append((char) b);
                }
            }
        }
        return content.toString();
    }


    public boolean change(String filePath, byte newAttributes) {
        // 文件不存在，结束操作
        FCB targetFCB = directoryManager.findFile(filePath);
        if (targetFCB == null) {
            System.out.println("错误：文件 " + filePath + " 不存在");
            return false;
        }

        // 文件打开不能改变属性
        if (findOpenFileEntry(filePath) != null) {
            System.out.println("错误：文件 " + filePath + " 已打开，不能修改属性");
            return false;
        }

        // 解析路径：获取父目录和文件名
        String parentPath = directoryManager.getParentPath(filePath);
        String fullFileName = directoryManager.getFileName(filePath);
        String[] nameParts = splitFileNameAndType(fullFileName);

        // 根据要求改变目录项中属性值（先删旧条目，再加新条目）
        directoryManager.removeEntryFromDirectory(filePath); // 删除旧目录项
        boolean addSuccess = directoryManager.addEntryToDirectory(
                parentPath, nameParts[0], nameParts[1],
                newAttributes, targetFCB.getStartBlock(), targetFCB.getFileLength()
        );

        // 处理添加结果：成功则提示，失败则回滚（重新添加旧属性条目）
        if (addSuccess) {
            System.out.println("成功：文件 " + filePath + " 属性修改为 " + newAttributes);
            return true;
        } else {
            System.out.println("错误：修改属性失败，目录已满");
            // 回滚：重新添加旧属性的目录项
            directoryManager.addEntryToDirectory(
                    parentPath, nameParts[0], nameParts[1],
                    targetFCB.getAttributes(), targetFCB.getStartBlock(), targetFCB.getFileLength()
            );
            return false;
        }
    }


    public boolean md(String dirPath) {
        // 解析路径：获取父目录路径和新目录名
        String parentPath = directoryManager.getParentPath(dirPath);
        String dirName = directoryManager.getDirectoryName(dirPath);

        // 文档要求“父目录不存在，不能建立
        Integer parentBlockInt = directoryManager.findDirectoryBlock(parentPath);
        if (parentBlockInt == null) {
            System.out.println("错误：父目录 " + parentPath + " 不存在");
            return false;
        }

        // 文档要求“存在同名目录，不能建立
        List<FCB> parentEntries = directoryManager.listDirectory(parentPath);
        for (FCB entry : parentEntries) {
            if (entry.isDirectory() && entry.getFileName().trim().equals(dirName)) {
                System.out.println("错误：目录 " + dirPath + " 已存在");
                return false;
            }
        }

        // 文档要求“为目录申请一个盘块，并填写目录内容
        byte dirBlock = diskManager.getFatManager().allocateBlock();
        if (dirBlock == -1) { // 无空闲块，创建失败
            System.out.println("错误：磁盘无空闲块");
            return false;
        }
        // 初始化目录块：填充8个空闲FCB条目（标记为"$$$"）
        for (int i = 0; i < DiskManager.DIRECTORY_ENTRIES_PER_BLOCK; i++) {
            diskManager.writeFCBToBlock(dirBlock, i, new FCB());
        }

        // 在父目录中添加新目录的条目（属性为DIRECTORY）
        boolean addSuccess = directoryManager.addEntryToDirectory(
                parentPath, dirName, "  ", DiskManager.DIRECTORY, dirBlock, (byte) 1
        );
        // 若父目录已满，回滚已分配的目录块
        if (!addSuccess) {
            System.out.println("错误：父目录 " + parentPath + " 已满");
            diskManager.getFatManager().freeBlock(dirBlock); // 释放目录块
            return false;
        }

        System.out.println("成功：目录 " + dirPath + " 建立");
        return true;
    }

    public String dir(String dirPath) {
        // 目录不存在，指令执行失败
        Integer dirBlockInt = directoryManager.findDirectoryBlock(dirPath);
        if (dirBlockInt == null) {
            return "错误：目录 " + dirPath + " 不存在";
        }

        // 一项一项显示目录内容
        List<FCB> entries = directoryManager.listDirectory(dirPath);
        StringBuilder sb = new StringBuilder();
        sb.append("目录内容：").append(dirPath).append("\n");
        sb.append("-------------------------\n");

        // 遍历目录条目，区分目录和文件并格式化显示
        for (FCB entry : entries) {
            if (entry.isDirectory()) {
                sb.append(entry.getFileName().trim()).append("/ <DIR>\n"); // 目录标记为<DIR>
            } else {
                // 文件格式：文件名.类型 [块数]
                sb.append(entry.getFileName().trim()).append(".").append(entry.getFileType().trim())
                        .append(" [").append(entry.getFileLength()).append("块]\n");
            }
        }
        sb.append("-------------------------");
        return sb.toString();
    }


    public boolean rd(String dirPath) {
        //根目录不能删除
        if (dirPath.equals("/")) {
            System.out.println("错误：不能删除根目录");
            return false;
        }

        // 目录不存在，指令执行失败
        Integer dirBlockInt = directoryManager.findDirectoryBlock(dirPath);
        if (dirBlockInt == null) {
            System.out.println("错误：目录 " + dirPath + " 不存在");
            return false;
        }
        // 处理Integer转byte：先判空（已处理），再强转（块号0~127，无溢出）
        byte dirBlock = (byte) dirBlockInt.intValue();

        // 非空目录不能删除
        if (!directoryManager.isDirectoryEmpty(dirBlock)) {
            System.out.println("错误：目录 " + dirPath + " 非空，不能删除");
            return false;
        }

        // 删除其目录项并回收对应空间（与删除文件过程相似）
        boolean removeSuccess = directoryManager.removeEntryFromDirectory(dirPath);
        if (!removeSuccess) { // 目录项删除失败
            System.out.println("错误：删除目录条目失败");
            return false;
        }
        // 回收目录块（释放磁盘空间）
        diskManager.getFatManager().freeBlock(dirBlock);
        System.out.println("成功：空目录 " + dirPath + " 已删除");
        return true;
    }

    private OpenFileEntry findOpenFileEntry(String filePath) {
        for (OpenFileEntry entry : openFileTable) {
            if (entry.filePath.equals(filePath)) { // 按完整路径唯一匹配
                return entry;
            }
        }
        return null;
    }

    private String[] splitFileNameAndType(String fullFileName) {
        String[] parts = new String[]{"", "  "}; // 默认类型为两个空格（适配FCB的2字节）
        if (fullFileName.contains(".")) { // 包含类型（如 "a.txt"）
            String[] split = fullFileName.split("\\.", 2); // 按第一个.拆分（避免文件名含.的情况）
            // 文件名截断为3字节
            parts[0] = split[0].length() > 3 ? split[0].substring(0, 3) : split[0];
            // 类型截断为2字节
            parts[1] = split[1].length() > 2 ? split[1].substring(0, 2) : split[1];
        } else {
            parts[0] = fullFileName.length() > 3 ? fullFileName.substring(0, 3) : fullFileName;
        }
        // 不足长度补空格（确保符合FCB的固定字节数：文件名3字节，类型2字节）
        parts[0] = String.format("%-3s", parts[0]).substring(0, 3);
        parts[1] = String.format("%-2s", parts[1]).substring(0, 2);
        return parts;
    }

    public List<OpenFileEntry> getOpenFileTable() {
        return new ArrayList<>(openFileTable);
    }
}