package com.example.javawebcurriculumdesign.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Properties;

/**
 * 加密配置工具类
 * 从专门的加密配置文件中读取加密相关的密钥和参数
 */
public class CryptoConfig {
    private static final String CONFIG_FILE = "crypto.properties";
    private static Properties properties;
    
    // 缓存解码后的密钥
    private static byte[] sm4Key;
    private static byte[] sm4Iv;
    private static byte[] logHmacKey;
    
    // 静态代码块，在类加载时执行，初始化配置
    static {
        try {
            loadConfig();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("初始化加密配置失败", e);
        }
    }
    
    /**
     * 加载配置文件
     */
    private static void loadConfig() throws IOException {
        properties = new Properties();
        InputStream is = CryptoConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE);
        if (is == null) {
            throw new IOException("找不到配置文件: " + CONFIG_FILE);
        }
        
        try {
            properties.load(is);
            
            // 解码Base64编码的密钥
            String sm4KeyBase64 = properties.getProperty("crypto.sm4.key");
            String sm4IvBase64 = properties.getProperty("crypto.sm4.iv");
            String logHmacKeyBase64 = properties.getProperty("crypto.log.hmac.key");
            
            if (sm4KeyBase64 == null || sm4IvBase64 == null || logHmacKeyBase64 == null) {
                throw new IOException("加密配置不完整，请检查配置文件");
            }
            
            sm4Key = Base64.getDecoder().decode(sm4KeyBase64);
            sm4Iv = Base64.getDecoder().decode(sm4IvBase64);
            logHmacKey = Base64.getDecoder().decode(logHmacKeyBase64);
            
            // 验证密钥长度
            if (sm4Key.length != 16) {
                throw new IOException("SM4密钥长度必须为16字节");
            }
            if (sm4Iv.length != 16) {
                throw new IOException("SM4初始化向量长度必须为16字节");
            }
            
        } finally {
            is.close();
        }
    }
    
    /**
     * 获取SM4加密密钥
     * @return SM4密钥
     */
    public static byte[] getSM4Key() {
        return sm4Key.clone(); // 返回副本以防止修改
    }
    
    /**
     * 获取SM4初始化向量
     * @return SM4初始化向量
     */
    public static byte[] getSM4IV() {
        return sm4Iv.clone(); // 返回副本以防止修改
    }
    
    /**
     * 获取系统日志HMAC密钥
     * @return 日志HMAC密钥
     */
    public static byte[] getLogHmacKey() {
        return logHmacKey.clone(); // 返回副本以防止修改
    }
    
    /**
     * 重新加载配置（用于配置更新后的热加载）
     * @throws IOException 配置加载异常
     */
    public static synchronized void reloadConfig() throws IOException {
        loadConfig();
    }
    
    /**
     * 获取配置属性值
     * @param key 配置键
     * @return 配置值
     */
    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    /**
     * 获取配置属性值（带默认值）
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * 获取身份证号脱敏前缀长度
     * @return 前缀长度
     */
    public static int getIdCardMaskPrefix() {
        return Integer.parseInt(getProperty("crypto.mask.idcard.prefix", "3"));
    }

    /**
     * 获取身份证号脱敏后缀长度
     * @return 后缀长度
     */
    public static int getIdCardMaskSuffix() {
        return Integer.parseInt(getProperty("crypto.mask.idcard.suffix", "4"));
    }

    /**
     * 获取身份证号脱敏字符
     * @return 脱敏字符
     */
    public static String getIdCardMaskChar() {
        return getProperty("crypto.mask.idcard.char", "*");
    }

    /**
     * 获取手机号脱敏前缀长度
     * @return 前缀长度
     */
    public static int getPhoneMaskPrefix() {
        return Integer.parseInt(getProperty("crypto.mask.phone.prefix", "3"));
    }

    /**
     * 获取手机号脱敏后缀长度
     * @return 后缀长度
     */
    public static int getPhoneMaskSuffix() {
        return Integer.parseInt(getProperty("crypto.mask.phone.suffix", "4"));
    }

    /**
     * 获取手机号脱敏字符
     * @return 脱敏字符
     */
    public static String getPhoneMaskChar() {
        return getProperty("crypto.mask.phone.char", "*");
    }

    /**
     * 获取姓名脱敏字符
     * @return 脱敏字符
     */
    public static String getNameMaskChar() {
        return getProperty("crypto.mask.name.char", "*");
    }

    /**
     * 获取二维码尺寸
     * @return 二维码尺寸
     */
    public static int getQRCodeSize() {
        return Integer.parseInt(getProperty("crypto.qrcode.size", "300"));
    }

    /**
     * 获取二维码格式
     * @return 二维码格式
     */
    public static String getQRCodeFormat() {
        return getProperty("crypto.qrcode.format", "png");
    }
}
