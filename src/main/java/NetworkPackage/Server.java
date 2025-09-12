package NetworkPackage;

import MainPackage.EncryptionUtil;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONObject;

import javax.swing.*;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Server {//服务器类
    public static final int SavaUserInformation = 0;//0：存储用户信息
    public static final int LoadUserInformation = 1;//1：加载用户信息
    public static final int ModifyUserInformation = 2;//2：修改用户信息
    public static final int ValidateUserUnique = 3;//3：验证用户名唯一性
    public static final int ValidateUserInformation = 4;//4：验证用户信息
    public static final int RemoveUserInformation = 5;//5：删除用户信息
    public static final int SaveUserUploadPicture = 6;//6：存储用户上传图片
    public static final int LoadUserUploadPicture = 7;//7：加载用户上传图片
    public static final int RemoveUserUploadPicture = 8;//8：删除用户上传图片
    public static final int ReceiveUserErrorLog = 9;//9：接收用户错误日志
    public static final int ReceiveUserSuggestionFeedback = 10;//10：接收用户建议反馈
    public static final int ReceiveUserSettingState = 11;//11：接收用户设置状态
    public static final int GenerateVerificationCode = 12;//12：生成验证码
    public static final int VerifyVerificationCodeState = 13;//13：校验验证码

    private static JSONObject userJSONObject;//用户JSON对象
    private static final Path cloudAccountStoragePath = Path.of("D:\\CloudDataBase\\CloudAccountStorage.enc");//云端用户存储路径
    private static final Path cloudAccountErrorLogPath = Path.of("D:\\CloudDataBase\\CloudAccountErrorLog.txt");//云端用户错误日志路径
    private static final Path cloudAccountSuggestionFeedbackPath = Path.of("D:\\CloudDataBase\\CloudAccountSuggestionFeedback.txt");//云端用户建议反馈路径
    private static final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(3, 16, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(2), Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());//核心线程数量为3，线程池总大小为16，空闲时间60秒，空闲时间单位为秒，阻塞队列最多2个线程，使用默认线程工厂，阻塞队列超出直接抛弃政策

    private static final Map<String, String> verificationCodeMap = new HashMap<>();//存储手机号与验证码的映射
    private static final Map<String, Long> verificationCodeExpirationMap = new HashMap<>();//存储验证码与过期时间（5分钟）的映射

//    public static final String ACCESS_KEY_ID = "";//AccessKey ID
//    public static final String ACCESS_KEY_SECRET = "";//AccessKey Secret
//    public static final String SIGN_NAME = "阿里云短信测试";//签名
//    public static final String TEMPLATE_CODE = "SMS_154950909";//模板CODE
//    private static final String OSS_ENDPOINT = "oss-cn-shenzhen.aliyuncs.com";//OSS Bucket所在区域
//    private static final String OSS_BUCKET_NAME = "picture-management-system-cloud-storage";//OSS Bucket名称

    private static final Timer verificationCodeMonitorTimer = new Timer(60000, _ -> {//验证码监控计时器：每60s执行一次
        Set<String> userPhoneSet = verificationCodeExpirationMap.keySet();//获取手机号
        for (String userPhone : userPhoneSet) {//遍历过期时间集合
            if (System.currentTimeMillis() > verificationCodeExpirationMap.get(userPhone)) {//如果验证码过期
                System.out.println("已删除手机号" + userPhone + "的验证码" + verificationCodeMap.get(userPhone));
                verificationCodeExpirationMap.remove(userPhone);//清除验证码映射
                verificationCodeMap.remove(userPhone);//清除验证码映射
            }
        }
    });

    public static void createServerSocket() {//创建服务器
        try (ServerSocket serverSocket = new ServerSocket(27568, 50, InetAddress.getByName("::"))) {//创建端口为27568的服务器接口（可用端口为0~65535（2^16-1），其中0~1024是主要程序的端口，一般不占用）
            System.out.println("服务端已启动，监听端口：" + 27568);
            while (true) {//不断等待
                Socket userSocket = serverSocket.accept();//等待客户端连接
                System.out.println("新客户端连接：" + userSocket.getInetAddress());
                threadPoolExecutor.submit(() -> new ServerRunnable(userSocket).run());//连接后向线程池提交并开启一个线程：一个用户对应服务器的一个线程
            }
        } catch (IOException e) {
            handleError(e.getMessage());//处理错误日志
            e.printStackTrace();//捕获异常
        }
        verificationCodeMonitorTimer.start();//开启验证码监控计时器
    }

    private static void loadCloudAccountStorage() throws Exception {//加载云端用户存储数据
        try {
            if (!Files.exists(cloudAccountStoragePath)) {//如果不存在云盘账户存储文件
                if (!Files.exists(new File("D:\\CloudDataBase").toPath())) {//如果不存在云盘数据库
                    Files.createDirectory(new File("D:\\CloudDataBase").toPath());//就创建云盘数据库
                    Path dataBaseFilePath = Path.of("D:\\CloudDataBase\\CloudPictureStorage\\");//创建用户数据库文件路径
                    if (!Files.exists(dataBaseFilePath)) {//如果用户数据库文件路径不存在
                        Files.createDirectory(dataBaseFilePath);//就创建用户数据库文件路径
                    }
                }
                Files.createFile(cloudAccountStoragePath);//就创建文件
                JSONObject administrator = new JSONObject();//创建管理员对象
                administrator.put("userAccount", "Root");//放入用户名键值对
                administrator.put("userPhone", "15914208576");//放入手机号键值对
                administrator.put("userPassword", "666666");//放入密码键值对
                administrator.put("userAvailableCloudCapacity", "10000000000000");//放入云盘可用容量键值对
                administrator.put("recentSuggestionFeedbackTime", "0");//放入
                administrator.put("windowState", "false");//放入
                administrator.put("systemLanguage", "false");//放入
                administrator.put("themeColor", "false");//放入
                administrator.put("dbclickBehavior", "false");//放入
                administrator.put("hoverTip", "false");//放入
                administrator.put("deleteTip", "false");//放入
                administrator.put("pictureSuffix", "false");//放入
                administrator.put("renameStrategy", "false");//放入
                administrator.put("searchStrategy", "false");//放入
                administrator.put("GPUAcceleration", "false");//放入
                administrator.put("customGIFFrameAmount", "5");//放入
                administrator.put("GIFStrategy", "false");//放入
                administrator.put("customCacheRemainAmount", "500");//放入
                administrator.put("cacheStrategy", "false");//放入
                administrator.put("pictureDirectory", "");//放入
                administrator.put("backgroundPictureDirectory", "");//放入
                administrator.put("imageOpacity", "50");//放入
                administrator.put("masterVolume", "0");//放入
                administrator.put("masterState", "false");//放入
                administrator.put("bgmVolume", "0");//放入
                administrator.put("bgmState", "false");//放入
                administrator.put("effectVolume", "0");//放入
                administrator.put("effectState", "false");//放入
                administrator.put("utilizeTimes", "3000");//放入
                userJSONObject = new JSONObject();//创建外部json对象
                userJSONObject.put("Root", administrator);//再嵌套
                String encryptedData = EncryptionUtil.encryptData(userJSONObject.toString(31), EncryptionUtil.ENCRYPTION_PASSWORD);//把JSON文件加密（内嵌套JSON的内置数据键值对为4）
                Files.write(cloudAccountStoragePath, encryptedData.getBytes());//把加密数据写回
            }
        } catch (Exception e) {
            throw new RuntimeException(e);//捕获异常
        }

        String encryptData = new String(Files.readAllBytes(cloudAccountStoragePath));//读取加密数据
        String jsonString = EncryptionUtil.decryptData(encryptData, EncryptionUtil.ENCRYPTION_PASSWORD);//将加密数据通过密钥解密
        userJSONObject = new JSONObject(jsonString);//根据解密文件创建JSON文件
        System.out.println("加载用户存档");
    }

    private static void saveCloudAccountStorage() throws Exception {//保存云端用户存储数据
        String encryptedData = EncryptionUtil.encryptData(userJSONObject.toString(31), EncryptionUtil.ENCRYPTION_PASSWORD);//把JSON文件加密（内嵌套JSON的内置数据键值对为4）
        Files.write(cloudAccountStoragePath, encryptedData.getBytes());//把加密数据写回
        System.out.println("保存用户存档");
    }

    public static void addUser(String userAccount, String userPhone, String userPassword, long userAvailableCloudCapacity, long recentSuggestionFeedbackTime, boolean windowState, boolean systemLanguage, boolean themeColor, boolean dbclickBehavior, boolean hoverTip, boolean deleteTip, boolean pictureSuffix, boolean renameStrategy, boolean searchStrategy, boolean GPUAcceleration,
                               int customGIFFrameAmount, boolean GIFStrategy, int customCacheRemainAmount, boolean cacheStrategy, int customRecycleCleanTime, boolean recycleStrategy, String pictureDirectory, String backgroundPictureDirectory, int imageOpacity, int masterVolume, boolean masterState, int bgmVolume, boolean bgmState, int effectVolume, boolean effectState, int utilizeTimes) throws Exception {//添加用户：用户注册
        loadCloudAccountStorage();//加载
        JSONObject user = new JSONObject();//创建新用户对象
        user.put("userAccount", userAccount);//放入用户名键值对
        user.put("userPhone", userPhone);//放入手机号键值对
        user.put("userPassword", userPassword);//放入密码键值对
        user.put("userAvailableCloudCapacity", userAvailableCloudCapacity);//放入云盘可用容量键值对
        user.put("recentSuggestionFeedbackTime", recentSuggestionFeedbackTime);//放入
        user.put("windowState", windowState);//放入
        user.put("systemLanguage", systemLanguage);//放入
        user.put("themeColor", themeColor);//放入
        user.put("dbclickBehavior", dbclickBehavior);//放入
        user.put("hoverTip", hoverTip);//放入
        user.put("deleteTip", deleteTip);//放入
        user.put("pictureSuffix", pictureSuffix);//放入
        user.put("renameStrategy", renameStrategy);//放入
        user.put("searchStrategy", searchStrategy);//放入
        user.put("GPUAcceleration", GPUAcceleration);//放入
        user.put("customGIFFrameAmount", customGIFFrameAmount);//放入
        user.put("GIFStrategy", GIFStrategy);//放入
        user.put("customCacheRemainAmount", customCacheRemainAmount);//放入
        user.put("cacheStrategy", cacheStrategy);//放入
        user.put("customRecycleCleanTime", customRecycleCleanTime);//放入
        user.put("recycleStrategy", recycleStrategy);//放入
        user.put("pictureDirectory", pictureDirectory);//放入
        user.put("backgroundPictureDirectory", backgroundPictureDirectory);//放入
        user.put("imageOpacity", imageOpacity);//放入
        user.put("masterVolume", masterVolume);//放入
        user.put("masterState", masterState);//放入
        user.put("bgmVolume", bgmVolume);//放入
        user.put("bgmState", bgmState);//放入
        user.put("effectVolume", effectVolume);//放入
        user.put("effectState", effectState);//放入
        user.put("utilizeTimes", utilizeTimes);//放入
        userJSONObject.put(userAccount, user);//再嵌套用户名和新用户对象的键值对以快速查找用户名信息
        saveCloudAccountStorage();//保存
        verificationCodeMap.remove(userPhone);//清除验证码映射
        verificationCodeExpirationMap.remove(userPhone);//清除验证码有效期
        Path dataBaseFilePath = Path.of("D:\\CloudDataBase\\CloudPictureStorage\\" + userAccount);//创建用户数据库文件路径
        if (!Files.exists(dataBaseFilePath)) {//如果用户数据库文件路径不存在
            Files.createDirectory(dataBaseFilePath);//就创建用户数据库文件路径
        }
        System.out.println("用户注册：" + user);
    }

    public static JSONObject findUser(String input, boolean type) throws Exception {//查找用户：根据用户名或手机号
        loadCloudAccountStorage();//加载
        if (type) {//如果根据手机号查找
            for (Object key : userJSONObject.keySet()) {//遍历所有用户
                JSONObject candidate = userJSONObject.optJSONObject((String) key);//获取这个用户的内嵌套信息
                if (candidate.getString("userPhone").equals(input)) {//如果内嵌他信息的手机号值与查找的手机号一致
                    System.out.println("用户登录：" + candidate);
                    return candidate;//就返回候选者
                }
            }
            return null;//没有找到
        } else {//否则根据用户名查找
            JSONObject user = userJSONObject.optJSONObject(input);//通过用户名在外嵌套寻找
            System.out.println("用户登录：" + user);
            return user;//返回用户
        }
    }

    public static boolean modifyUser(String input1, String input2, int type) throws Exception {//修改用户信息（输入1永远是用户名；输入2可以是新用户名、新手机号、新密码或用户可用云盘容量）
        loadCloudAccountStorage();//加载
        JSONObject userInformation = userJSONObject.optJSONObject(input1);//获取用户信息
        if (userInformation == null) {//如果为空
            return false;//失败
        }
        switch (type) {//根据类型选择
            case 1://修改用户名
                if (!removeUser(input1)) {//删除旧外嵌套
                    return false;//失败
                }
                userInformation.put("userAccount", input2);//再修改用户名
                userJSONObject.put(input2, userInformation);//再放入新外嵌套
                Files.move(Path.of("D:\\CloudDataBase\\CloudPictureStorage\\" + input1), Path.of("D:\\CloudDataBase\\CloudPictureStorage\\" + input2), StandardCopyOption.REPLACE_EXISTING);//数据库路径重命名
                System.out.println("用户" + input1 + "修改用户名为：" + input2);
                break;
            case 2://修改手机号
                userInformation.put("userPhone", input2);//修改手机号
                verificationCodeMap.remove(input2);//清除验证码映射
                verificationCodeExpirationMap.remove(input2);//清除验证码有效期
                System.out.println("用户" + input1 + "修改手机号为：" + input2);
                break;
            case 3://修改密码
                userInformation.put("userPassword", input2);//修改密码
                System.out.println("用户" + input1 + "修改密码为：" + input2);
                break;
            case 4://修改用户可用云盘容量
                userInformation.put("userAvailableCloudCapacity", input2);//修改用户可用云盘容量
                System.out.println("用户" + input1 + "可用云盘容量变为：" + input2);
                break;
        }
        saveCloudAccountStorage();//存储
        return true;//成功
    }

    public static boolean validateUserUnique(boolean type, String input) throws Exception {//验证用户名或手机号唯一性
        loadCloudAccountStorage();//加载
        if (type) {//如果是验证手机号
            for (Object key : userJSONObject.keySet()) {//遍历所有用户
                JSONObject candidate = userJSONObject.optJSONObject((String) key);//获取这个用户的内嵌套信息
                if (candidate.getString("userPhone").equals(input)) {//如果内嵌他信息的手机号值与查找的手机号一致
                    return true;//就返回真
                }
            }
            return false;//否则返回假
        } else {//否则是验证用户名
            return userJSONObject.has(input);//返回有无用户名
        }
    }

    public static int validateUserInformation(String userAccount, String userPhone, String userPassword, boolean type) throws Exception {//验证用户信息：0和1为真，0表示正常登录，1表示用户通过手机号和密码登录；2和3为假，2表示无用户异常，3表示用户名密码映射异常
        loadCloudAccountStorage();//加载
        if (type) {//如果是验证手机号
            for (Object key : userJSONObject.keySet()) {//遍历所有用户
                JSONObject userInformation = userJSONObject.getJSONObject((String) key);//获取这个用户的内嵌套信息
                if (userInformation.getString("userPhone").equals(userPhone)) {//如果内嵌信息的手机号值与查找的手机号一致
                    verificationCodeMap.remove(userPhone);//清除验证码映射
                    verificationCodeExpirationMap.remove(userPhone);//清除验证码有效期
                    return 0;//就返回真
                }
            }
        } else {//否则是验证用户名
            if (userJSONObject.has(userAccount)) {//如果用户名存在
                JSONObject userInformation = (JSONObject) userJSONObject.get(userAccount);//获取用户信息
                return userInformation.getString("userPassword").equals(userPassword) ? 0 : 3;//如果内嵌信息的密码值与验证的密码一致
            } else {//否则判断是否是输入手机号登录
                for (Object key : userJSONObject.keySet()) {//遍历所有用户
                    JSONObject userInformation = userJSONObject.getJSONObject((String) key);//获取这个用户的内嵌套信息
                    if (userInformation.getString("userPhone").equals(userAccount)) {//如果内嵌信息的手机号值与查找的用户名一致
                        return userInformation.getString("userPassword").equals(userPassword) ? 1 : 3;//如果内嵌信息的密码值与验证的密码一致
                    }
                }
            }
        }
        return 2;//否则返回假
    }

    public static boolean removeUser(String userAccount) throws Exception {//删除用户
        loadCloudAccountStorage();//加载
        if (!userJSONObject.has(userAccount)) {//如果没有该用户
            return false;//返回失败
        }
        System.out.println("用户注销：" + userJSONObject.optJSONObject(userAccount));
        userJSONObject.remove(userAccount);//否则移除用户
        saveCloudAccountStorage();//保存
        return true;//返回成功
    }

    public static void handleServerSaveUserInformation(DataInputStream dis, DataOutputStream dos) throws IOException {//存储用户信息：操作码0
        try {
            addUser(dis.readUTF(), dis.readUTF(), dis.readUTF(), dis.readLong(), dis.readLong(), dis.readBoolean(), dis.readBoolean(), dis.readBoolean(), dis.readBoolean(), dis.readBoolean(), dis.readBoolean(), dis.readBoolean(), dis.readBoolean(), dis.readBoolean(), dis.readBoolean(),
                    dis.readInt(), dis.readBoolean(), dis.readInt(), dis.readBoolean(), dis.readInt(), dis.readBoolean(), dis.readUTF(), dis.readUTF(), dis.readInt(), dis.readInt(), dis.readBoolean(), dis.readInt(), dis.readBoolean(), dis.readInt(), dis.readBoolean(), dis.readInt());//添加用户
            dos.writeBoolean(true);//添加成功
        } catch (Exception e) {
            dos.writeBoolean(false);//添加失败
            handleError(e.getMessage());//处理错误日志
            throw new RuntimeException(e);//捕获异常
        }
    }

    public static void handleServerLoadUserInformation(DataInputStream dis, DataOutputStream dos) throws Exception {//加载用户信息：操作码1
        if (dis.readBoolean()) {//输入类型为手机号加载
            String input = dis.readUTF();//输入手机号
            JSONObject userInformation = findUser(input, true);//查找用户
            if (userInformation != null) {//如果不为空
                dos.writeUTF(userInformation.getString("userAccount"));//输出用户名
                dos.writeUTF(userInformation.getString("userPassword"));//输出密码
                dos.writeLong(userInformation.getLong("userAvailableCloudCapacity"));//输出云盘可用容量
                dos.writeLong(userInformation.getLong("recentSuggestionFeedbackTime"));//输出
                dos.writeBoolean(userInformation.getBoolean("windowState"));//输出
                dos.writeBoolean(userInformation.getBoolean("systemLanguage"));//输出
                dos.writeBoolean(userInformation.getBoolean("themeColor"));//输出
                dos.writeBoolean(userInformation.getBoolean("dbclickBehavior"));//输出
                dos.writeBoolean(userInformation.getBoolean("hoverTip"));//输出
                dos.writeBoolean(userInformation.getBoolean("deleteTip"));//输出
                dos.writeBoolean(userInformation.getBoolean("pictureSuffix"));//输出
                dos.writeBoolean(userInformation.getBoolean("renameStrategy"));//输出
                dos.writeBoolean(userInformation.getBoolean("searchStrategy"));//输出
                dos.writeBoolean(userInformation.getBoolean("GPUAcceleration"));//输出
                dos.writeInt(userInformation.getInt("customGIFFrameAmount"));//输出
                dos.writeBoolean(userInformation.getBoolean("GIFStrategy"));//输出
                dos.writeInt(userInformation.getInt("customCacheRemainAmount"));//输出
                dos.writeBoolean(userInformation.getBoolean("cacheStrategy"));//输出
                dos.writeInt(userInformation.getInt("customRecycleCleanTime"));//输出
                dos.writeBoolean(userInformation.getBoolean("recycleStrategy"));//输出
                dos.writeUTF(userInformation.getString("pictureDirectory"));//输出
                dos.writeUTF(userInformation.getString("backgroundPictureDirectory"));//输出
                dos.writeInt(userInformation.getInt("imageOpacity"));//输出
                dos.writeInt(userInformation.getInt("masterVolume"));//输出
                dos.writeBoolean(userInformation.getBoolean("masterState"));//输出
                dos.writeInt(userInformation.getInt("bgmVolume"));//输出
                dos.writeBoolean(userInformation.getBoolean("bgmState"));//输出
                dos.writeInt(userInformation.getInt("effectVolume"));//输出
                dos.writeBoolean(userInformation.getBoolean("effectState"));//输出
                dos.writeInt(userInformation.getInt("utilizeTimes"));//输出
            } else {
                dos.writeUTF("");//输出用户名
                dos.writeUTF("");//输出密码
                dos.writeLong(0);//输出云盘可用容量
            }
        } else {//否则为用户名加载
            String input = dis.readUTF();//输入用户名
            JSONObject userInformation = findUser(input, false);//查找用户
            if (userInformation != null) {//如果不为空
                dos.writeUTF(userInformation.getString("userPhone"));//输出手机号
                dos.writeLong(userInformation.getLong("userAvailableCloudCapacity"));//输出云盘可用容量
                dos.writeLong(userInformation.getLong("recentSuggestionFeedbackTime"));//输出
                dos.writeBoolean(userInformation.getBoolean("windowState"));//输出
                dos.writeBoolean(userInformation.getBoolean("systemLanguage"));//输出
                dos.writeBoolean(userInformation.getBoolean("themeColor"));//输出
                dos.writeBoolean(userInformation.getBoolean("dbclickBehavior"));//输出
                dos.writeBoolean(userInformation.getBoolean("hoverTip"));//输出
                dos.writeBoolean(userInformation.getBoolean("deleteTip"));//输出
                dos.writeBoolean(userInformation.getBoolean("pictureSuffix"));//输出
                dos.writeBoolean(userInformation.getBoolean("renameStrategy"));//输出
                dos.writeBoolean(userInformation.getBoolean("searchStrategy"));//输出
                dos.writeBoolean(userInformation.getBoolean("GPUAcceleration"));//输出
                dos.writeInt(userInformation.getInt("customGIFFrameAmount"));//输出
                dos.writeBoolean(userInformation.getBoolean("GIFStrategy"));//输出
                dos.writeInt(userInformation.getInt("customCacheRemainAmount"));//输出
                dos.writeBoolean(userInformation.getBoolean("cacheStrategy"));//输出
                dos.writeInt(userInformation.getInt("customRecycleCleanTime"));//输出
                dos.writeBoolean(userInformation.getBoolean("recycleStrategy"));//输出
                dos.writeUTF(userInformation.getString("pictureDirectory"));//输出
                dos.writeUTF(userInformation.getString("backgroundPictureDirectory"));//输出
                dos.writeInt(userInformation.getInt("imageOpacity"));//输出
                dos.writeInt(userInformation.getInt("masterVolume"));//输出
                dos.writeBoolean(userInformation.getBoolean("masterState"));//输出
                dos.writeInt(userInformation.getInt("bgmVolume"));//输出
                dos.writeBoolean(userInformation.getBoolean("bgmState"));//输出
                dos.writeInt(userInformation.getInt("effectVolume"));//输出
                dos.writeBoolean(userInformation.getBoolean("effectState"));//输出
                dos.writeInt(userInformation.getInt("utilizeTimes"));//输出
            } else {
                dos.writeUTF("");//输出手机号
                dos.writeLong(0);//输出云盘可用容量
            }
        }
    }

    public static void handleServerModifyUserInformation(DataInputStream dis, DataOutputStream dos) {//修改用户信息：操作码2
        try {
            int type = dis.readInt();//输入类型
            dos.writeBoolean(modifyUser(dis.readUTF(), dis.readUTF(), type));//修改用户
        } catch (Exception e) {
            try {
                dos.writeBoolean(false);//失败
            } catch (IOException ex) {
                handleError(e.getMessage());//处理错误日志
                throw new RuntimeException(ex);//捕获异常
            }
            handleError(e.getMessage());//处理错误日志
            throw new RuntimeException(e);//捕获异常
        }
    }

    public static void handleServerValidateUserUnique(DataInputStream dis, DataOutputStream dos) throws Exception {//验证用户名唯一性：操作码3
        dos.writeBoolean(validateUserUnique(dis.readBoolean(), dis.readUTF()));//返回是否唯一
    }

    public static void handleServerValidateUserInformation(DataInputStream dis, DataOutputStream dos) throws Exception {//验证用户信息：操作码4
        if (dis.readBoolean()) {//如果验证手机号
            dos.writeInt(validateUserInformation(null, dis.readUTF(), null, true));//验证用户信息
        } else {//否则验证用户名
            dos.writeInt(validateUserInformation(dis.readUTF(), null, dis.readUTF(), false));//验证用户信息
        }
    }

    public static void handleServerRemoveUserInformation(DataInputStream dis, DataOutputStream dos) {//删除用户信息：操作码5
        try {
            dos.writeBoolean(removeUser(dis.readUTF()));//删除用户
        } catch (Exception e) {
            handleError(e.getMessage());//处理错误日志
            throw new RuntimeException(e);//捕获异常
        }
    }

    public static void handleServerSaveUserUploadPicture(DataInputStream dis, DataOutputStream dos) throws Exception {//存储用户上传图片：操作码6
        String userAccount = dis.readUTF();//输入用户名
        boolean renameStrategy = dis.readBoolean();//输入命名策略
        long userAvailableCloudCapacity = 0;//用户可用云盘容量
        try {
            JSONObject userInformation = findUser(userAccount, false);//根据用户名查找用户
            if (userInformation != null) {//如果不为空
                userAvailableCloudCapacity = userInformation.getLong("userAvailableCloudCapacity");//获取用户可用云盘容量
            }
            int listSize = dis.readInt();//输入列表长度
            for (int i = 0; i < listSize; i++) {//根据列表长度遍历
                String fileName = dis.readUTF();//输入文件名
                userAvailableCloudCapacity -= dis.readLong();//用户可用云盘容量减去输入文件大小
                int bytesLength = dis.readInt();//输入文件字节数组长度
                byte[] bytes = new byte[bytesLength];//根据文件字节数组长度创建文件字节数组
                dis.readFully(bytes, 0, bytesLength);//读取完整字节数组（确保读取到所有数据）

                Path dataBaseFilePath = Path.of("D:\\CloudDataBase\\CloudPictureStorage\\" + userAccount);//创建用户数据库文件路径
                if (!Files.exists(dataBaseFilePath)) {//如果用户数据库文件路径不存在
                    Files.createDirectory(dataBaseFilePath);//就创建用户数据库文件路径
                }
                File dataBaseFile = new File(dataBaseFilePath + "\\" + fileName);//创建数据库文件
                if (Files.exists(dataBaseFile.toPath())) {//如果文件重复
                    String baseName = FilenameUtils.getBaseName(dataBaseFile.getName());//获取文件名称
                    String extension = FilenameUtils.getExtension(dataBaseFile.getName());//获取文件扩展名
                    int counter = 1;//计数器
                    while (Files.exists(dataBaseFile.toPath())) {//如果该文件一直重复
                        String newName;//创建文件新名称，不断进行累加
                        if (renameStrategy) {//如果是数字后缀策略
                            newName = baseName + "(" + counter++ + ")." + extension;//为文件添加数字后缀
                        } else {//否则是英文前缀策略
                            newName = "NewName" + String.format("%04d", counter++) + baseName + "." + extension;//为文件添加英文前缀
                        }
                        dataBaseFile = dataBaseFilePath.resolve(newName).toFile();//设置目标文件
                    }
                }
                try (FileOutputStream fos = new FileOutputStream(String.valueOf(dataBaseFile))) {//根据数据库文件创建文件输出流
                    fos.write(bytes);//向文件输出流写入文件字节数组数据
                }
                dos.writeUTF(dataBaseFile.getName());//向客户端回写处理后的文件名
            }
            dos.writeBoolean(modifyUser(userAccount, String.valueOf(userAvailableCloudCapacity), 4));//修改用户可用云盘容量
            System.out.println("用户" + userAccount + "上传了" + listSize + "张云盘图片");

        } catch (Exception e) {
            modifyUser(userAccount, String.valueOf(userAvailableCloudCapacity), 4);//失败，直接保存修改用户可用云盘容量
            dos.writeBoolean(false);//失败
            handleError(e.getMessage());//处理错误日志
            throw new RuntimeException(e);//捕获异常
        }
    }

    public static void handleServerLoadUserUploadPicture(DataInputStream dis, DataOutputStream dos) throws IOException {//加载用户上传图片：操作码7
        String userAccount = dis.readUTF();//输入用户名
        Path dataBaseFilePath = Path.of("D:\\CloudDataBase\\CloudPictureStorage\\" + userAccount);//创建用户数据库文件路径
        if (!Files.exists(dataBaseFilePath)) {//如果用户数据库文件路径不存在
            Files.createDirectory(dataBaseFilePath);//就创建用户数据库文件路径
            dos.writeInt(0);//输出长度为0
            dos.writeBoolean(true);//返回成功
        } else {//否则存在
            try {
                File[] files = new File(String.valueOf(dataBaseFilePath)).listFiles();//创建数据库图片文件数组
                if (files != null) {//如果文件非空
                    if (dis.readBoolean()) {//如果加载全部文件
                        dos.writeInt(files.length);//输出长度
                        for (File file : files) {//遍历图片文件数组
                            dos.writeUTF(file.getName());//输出文件名

                            FileInputStream fis = new FileInputStream(file);//通过文件创建文件输入流
                            ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);//创建有1024个缓存字节的字节数组输出流
                            byte[] buffer = new byte[1024];//创建大小为1024字节的字节缓存数组
                            int len;//单次读取长度
                            while ((len = fis.read(buffer)) != -1) {//文件输入流读取到字节缓存数组，读取长度为len，如果读取没有结束
                                bos.write(buffer, 0, len);//往字节数组输出流写入长度为len的字节缓存数组
                            }
                            byte[] bytes = bos.toByteArray();//把字节数组输出流的全部数据转化成完整的字节数组
                            dos.writeInt(bytes.length);//输出文件字节数组长度
                            dos.write(bytes);//输出文件字节数组
                            fis.close();//释放文件输入流
                            bos.close();//释放字节数组输出流
                        }
                        System.out.println("用户" + userAccount + "加载了" + files.length + "张云盘图片");
                    } else {//否则
                        int listSize = dis.readInt();//输入文件名列表大小
                        for (int i = 0; i < listSize; i++) {//遍历文件名列表
                            String fileName = dis.readUTF();//获取文件名
                            for (File file : files) {//遍历图片文件数组
                                if (file.getName().equals(fileName)) {//如果找到文件
                                    FileInputStream fis = new FileInputStream(file);//通过文件创建文件输入流
                                    ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);//创建有1024个缓存字节的字节数组输出流
                                    byte[] buffer = new byte[1024];//创建大小为1024字节的字节缓存数组
                                    int len;//单次读取长度
                                    while ((len = fis.read(buffer)) != -1) {//文件输入流读取到字节缓存数组，读取长度为len，如果读取没有结束
                                        bos.write(buffer, 0, len);//往字节数组输出流写入长度为len的字节缓存数组
                                    }
                                    byte[] bytes = bos.toByteArray();//把字节数组输出流的全部数据转化成完整的字节数组
                                    dos.writeInt(bytes.length);//输出文件字节数组长度
                                    dos.write(bytes);//输出文件字节数组
                                    fis.close();//释放文件输入流
                                    bos.close();//释放字节数组输出流
                                    break;//直接跳过
                                }
                            }
                        }
                    }
                } else {//否则文件为空
                    dos.writeInt(0);//输出长度为0
                }
                dos.writeBoolean(true);//返回成功
            } catch (Exception e) {
                dos.writeBoolean(false);//返回失败
                handleError(e.getMessage());//处理错误日志
                e.printStackTrace();//捕获异常
            }
        }
    }

    public static void handleServerRemoveUserUploadPicture(DataInputStream dis, DataOutputStream dos) throws Exception {//删除用户上传图片：操作码8
        String userAccount = dis.readUTF();//输入用户名
        if (dis.readBoolean()) {//如果是注销
            try {
                File file = new File("D:\\CloudDataBase\\CloudPictureStorage\\" + userAccount);//创建数据库文件夹
                File[] files = file.listFiles();//获取数据库文件夹下所有文件
                if (files != null) {//如果不为空
                    for (File listFile : files) {//遍历
                        Files.deleteIfExists(listFile.toPath());//将数据库文件夹删除
                    }
                }
                Files.deleteIfExists(file.toPath());//将数据库文件夹删除
                dos.writeBoolean(true);//成功
            } catch (Exception e) {
                dos.writeBoolean(false);//失败
                handleError(e.getMessage());//处理错误日志
                throw new RuntimeException(e);//捕获异常
            }
        } else {//否则
            long userAvailableCloudCapacity = 0;//用户可用云盘容量
            try {
                JSONObject userInformation = findUser(userAccount, false);//根据用户名查找用户
                if (userInformation != null) {//如果不为空
                    userAvailableCloudCapacity = userInformation.getLong("userAvailableCloudCapacity");//获取用户可用云盘容量
                }
                int listSize = dis.readInt();//输入列表长度
                for (int i = 0; i < listSize; i++) {//根据列表长度遍历
                    String fileName = dis.readUTF();//输入文件名

                    File file = new File("D:\\CloudDataBase\\CloudPictureStorage\\" + userAccount + "\\" + fileName);//创建文件
                    userAvailableCloudCapacity += file.length();//用户可用云盘容量加上文件大小
                    Files.deleteIfExists(file.toPath());//将数据库文件删除
                }
                dos.writeLong(userAvailableCloudCapacity);//写回
                dos.writeBoolean(modifyUser(userAccount, String.valueOf(userAvailableCloudCapacity), 4));//修改用户可用云盘容量
                System.out.println("用户" + userAccount + "删除了" + listSize + "张云盘图片");
            } catch (Exception e) {
                modifyUser(userAccount, String.valueOf(userAvailableCloudCapacity), 4);//失败，直接保存修改用户可用云盘容量
                dos.writeBoolean(false);//失败
                handleError(e.getMessage());//处理错误日志
                throw new RuntimeException(e);//捕获异常
            }
        }
    }

    public static void handleServerReceiveUserErrorLog(DataInputStream dis) throws Exception {//接收用户错误日志：操作码9
        String userAccount = dis.readUTF();//获取用户名
        modifyUser(userAccount, String.valueOf(dis.readLong()), 4);//修改用户云盘可用空间
        handleError(dis.readUTF());//处理错误日志
        System.out.println("用户" + userAccount + "提交了错误日志");
    }

    public static void handleServerReceiveUserSuggestionFeedback(DataInputStream dis) throws Exception {//接收用户建议反馈：操作码10
        String userAccount = dis.readUTF();//获取用户名
        if (!userAccount.isEmpty()) {//如果用户登录
            modifyUser(userAccount, String.valueOf(dis.readLong()), 4);//修改用户云盘可用空间
        }
        String suggestionFeedback = new StringBuilder().append(userAccount).append(new SimpleDateFormat("（yyyy年MM月dd日 HH时mm分ss秒）：").format(System.currentTimeMillis())).append(dis.readUTF()).toString();//输入建议反馈
        try {
            if (!Files.exists(cloudAccountSuggestionFeedbackPath)) {//如果不存在建议反馈路径
                Files.createFile(cloudAccountSuggestionFeedbackPath);//就创建路径
            }
        } catch (IOException e) {
            handleError(e.getMessage());//处理错误日志
            throw new RuntimeException(e);//捕获异常
        }
        try {
            try (FileOutputStream fos = new FileOutputStream(cloudAccountSuggestionFeedbackPath.toFile(), true)) {//创建文件输出流，且为追加信息
                fos.write(suggestionFeedback.getBytes());//写入信息
                fos.write("\n".getBytes());//写入换行
                System.out.println("用户" + userAccount + "提交了建议反馈");
            }
        } catch (IOException e) {
            handleError(e.getMessage());//处理错误日志
            throw new RuntimeException(e);//捕获异常
        }
    }

    public static void handleServerReceiveUserSettingState(DataInputStream dis) throws Exception {//接收用户设置状态：操作码11
        loadCloudAccountStorage();//加载
        String userAccount = dis.readUTF();
        JSONObject userInformation = userJSONObject.optJSONObject(userAccount);//获取用户信息
        if (userInformation != null) {//如果不为空
            userInformation.put("recentSuggestionFeedbackTime", dis.readLong());//放入
            userInformation.put("windowState", dis.readBoolean());//放入
            userInformation.put("systemLanguage", dis.readBoolean());//放入
            userInformation.put("themeColor", dis.readBoolean());//放入
            userInformation.put("dbclickBehavior", dis.readBoolean());//放入
            userInformation.put("hoverTip", dis.readBoolean());//放入
            userInformation.put("deleteTip", dis.readBoolean());//放入
            userInformation.put("pictureSuffix", dis.readBoolean());//放入
            userInformation.put("renameStrategy", dis.readBoolean());//放入
            userInformation.put("searchStrategy", dis.readBoolean());//放入
            userInformation.put("GPUAcceleration", dis.readBoolean());//放入
            userInformation.put("customGIFFrameAmount", dis.readInt());//放入
            userInformation.put("GIFStrategy", dis.readBoolean());//放入
            userInformation.put("customCacheRemainAmount", dis.readInt());//放入
            userInformation.put("cacheStrategy", dis.readBoolean());//放入
            userInformation.put("customRecycleCleanTime", dis.readInt());//放入
            userInformation.put("recycleStrategy", dis.readBoolean());//放入
            userInformation.put("pictureDirectory", dis.readUTF());//放入
            userInformation.put("backgroundPictureDirectory", dis.readUTF());//放入
            userInformation.put("imageOpacity", dis.readInt());//放入
            userInformation.put("masterVolume", dis.readInt());//放入
            userInformation.put("masterState", dis.readBoolean());//放入
            userInformation.put("bgmVolume", dis.readInt());//放入
            userInformation.put("bgmState", dis.readBoolean());//放入
            userInformation.put("effectVolume", dis.readInt());//放入
            userInformation.put("effectState", dis.readBoolean());//放入
            userInformation.put("utilizeTimes", dis.readInt());//放入
        }
        saveCloudAccountStorage();//存储
        System.out.println("上传了用户" + userAccount + "的设置状态");
    }

    public static void handleServerGenerateVerificationCode(DataInputStream dis, DataOutputStream dos) throws IOException {//生成用户验证码：操作码12
        String userPhone = dis.readUTF();//输入手机号
        String verificationCode = String.format("%06d", (int) (Math.random() * 1_000_000));//随机生成6位数字验证码
        while (Objects.equals(verificationCodeMap.get(userPhone), verificationCode)) {//如果与已有验证码相同
            verificationCode = String.format("%06d", (int) (Math.random() * 1_000_000));//重新随机生成6位数字验证码
        }
        verificationCodeMap.put(userPhone, verificationCode);//放入映射
        verificationCodeExpirationMap.put(userPhone, System.currentTimeMillis() + 450000);//5分钟过期（5*60*1000=450000）
        dos.writeUTF(verificationCode);//发送验证码
        System.out.println("验证码已发送到：" + userPhone + "，验证码：" + verificationCode);
//            HashMap<String, String> currentVerificationMap = new HashMap<>();//临时验证码映射：用于发送信息
//            currentVerificationMap.put(userPhone, verificationCode);//放入映射

//            Client client = createClient();//创建客户端
//            com.aliyun.dysmsapi20170525.models.SendSmsRequest sendSmsRequest = new com.aliyun.dysmsapi20170525.models.SendSmsRequest();//创建请求信息
//            sendSmsRequest.setSignName(SIGN_NAME);//签名名称
//            sendSmsRequest.setPhoneNumbers(phoneNumber);//电话号码
//            sendSmsRequest.setTemplateCode(TEMPLATE_CODE);//模版Code
//            sendSmsRequest.setTemplateParam(JSONObject.toJSONString(currentVerificationMap));//模板参数：把哈希表转化成json文件
//            SendSmsResponse response;
//            try {
//                response = client.sendSms(sendSmsRequest);
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//            System.out.println("发送成功：" + new Gson().toJson(response));
//            try {
//                client.sendSmsWithOptions(sendSmsRequest, new com.aliyun.teautil.models.RuntimeOptions());//发送
//            } catch (TeaException error) {
//                System.out.println(error.getMessage());//错误信息
//                System.out.println(error.getData().get("Recommend"));//诊断地址
//                com.aliyun.teautil.Common.assertAsString(error.message);
//            } catch (Exception _error) {
//                TeaException error = new TeaException(_error.getMessage(), _error);
//                System.out.println(error.getMessage());//错误信息
//                System.out.println(error.getData().get("Recommend"));//诊断地址
//                com.aliyun.teautil.Common.assertAsString(error.message);
//            }
    }

//        public static Client createClient() {//创建客户端
//            Config config = new Config()//配置
//                    .setAccessKeyId(ACCESS_KEY_ID)//确保代码运行环境设置了环境变量ALIBABA_CLOUD_ACCESS_KEY_ID
//                    .setAccessKeySecret(ACCESS_KEY_SECRET);//确保代码运行环境设置了环境变量ALIBABA_CLOUD_ACCESS_KEY_SECRET
//            config.endpoint = "dysmsapi.aliyuncs.com";//客户端访问域名
//            try {
//                return new Client(config);//返回客户端
//            } catch (Exception e) {
//                handleErrorLog(e.getMessage());//处理错误日志
//                throw new RuntimeException(e);//捕获异常
//            }
//        }

    public static void handleServerVerifyVerificationCode(DataInputStream dis, DataOutputStream dos) throws IOException {//验证用户验证码：操作码14
        String userPhone = dis.readUTF();//输入手机号
        if (verificationCodeExpirationMap.get(userPhone) == null || verificationCodeMap.get(userPhone) == null || !dis.readUTF().equals(verificationCodeMap.get(userPhone))) {//验证验证码是否正确
            dos.writeInt(1);//返回验证码错误
            System.out.println("手机号为" + userPhone + "的用户验证码错误");
        } else if (System.currentTimeMillis() > verificationCodeExpirationMap.get(userPhone)) {//验证验证码是否在有效时间内
            dos.writeInt(2);//返回验证码过期
            System.out.println("手机号为" + userPhone + "的用户验证码过期");
        } else {//否则
            dos.writeInt(0);//返回成功
            System.out.println("手机号为" + userPhone + "的用户验证码验证成功");
        }
    }

    public static void handleError(String errorMessage) {//处理用户错误日志
        try {
            if (!Files.exists(cloudAccountErrorLogPath)) {//如果不存在错误日志路径
                Files.createFile(cloudAccountErrorLogPath);//就创建路径
            }
        } catch (IOException e) {
            throw new RuntimeException(e);//捕获异常
        }
        if (errorMessage != null) {//如果不为空：添加错误日志
            errorMessage = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒：").format(System.currentTimeMillis()) + errorMessage;//为错误信息添加时间前缀
            try {
                try (FileOutputStream fos = new FileOutputStream(cloudAccountErrorLogPath.toFile(), true)) {//创建文件输出流，且为追加信息
                    fos.write(errorMessage.getBytes());//写入信息
                    fos.write("\n".getBytes());//写入换行
                }
            } catch (IOException e) {
                throw new RuntimeException(e);//捕获异常
            }
        }
    }

    public static void main(String[] args) {//服务端主方法
        createServerSocket();//创建服务器接口
    }
}
