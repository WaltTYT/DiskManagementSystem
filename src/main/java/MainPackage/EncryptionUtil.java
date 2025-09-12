package MainPackage;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.Base64;

public class EncryptionUtil {//加密工具类
    private static final String ALGORITHM = "AES";//加密算法常量：AES
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";//加密转化策略模式常量：ECB
    public static final String ENCRYPTION_PASSWORD = "your_secure_password";//加密密钥常量

    private static SecretKeySpec generateKey(String password) throws Exception {//生成密钥
        return new SecretKeySpec(MessageDigest.getInstance("SHA-256").digest(password.getBytes()), 0, 16, ALGORITHM);//获取算法实例与密钥字节数组并截取前16字节作为AES-128密钥
    }

    public static String encryptData(String jsonString, String password) throws Exception {//加密JSON字符串数据
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);//根据转化策略创建转化实例
        cipher.init(Cipher.ENCRYPT_MODE, generateKey(password));//根据加密模式让密钥初始化转化策略
        return Base64.getEncoder().encodeToString(cipher.doFinal(jsonString.getBytes()));//把JSON字符串加密并返回
    }

    public static String decryptData(String encryptedBase64, String password) throws Exception {//解密JSON字符串数据
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);//根据转化策略创建转化实例
        cipher.init(Cipher.DECRYPT_MODE, generateKey(password));//根据解密模式让密钥初始化转化策略
        return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedBase64)));//把JSON字符串解密并返回
    }
}
