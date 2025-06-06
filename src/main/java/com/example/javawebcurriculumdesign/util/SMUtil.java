package com.example.javawebcurriculumdesign.util;

import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.crypto.engines.SM4Engine;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.modes.CBCModeCipher;
import org.bouncycastle.crypto.paddings.BlockCipherPadding;
import org.bouncycastle.crypto.paddings.PKCS7Padding;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Base64;

/**
 * 国密算法工具类
 * 实现SM3哈希、SM4加解密等功能
 */
public class SMUtil {
    
    // 静态代码块，在类加载时注册BouncyCastle提供者
    static {
        Security.addProvider(new BouncyCastleProvider());
    }
    
    /**
     * SM3哈希算法，用于密码加密存储
     * @param input 输入字符串
     * @return SM3哈希值的十六进制字符串
     */
    public static String sm3(String input) {
        if (input == null) {
            return null;
        }
        
        byte[] inputBytes = input.getBytes(StandardCharsets.UTF_8);
        SM3Digest digest = new SM3Digest();
        digest.update(inputBytes, 0, inputBytes.length);
        
        byte[] hash = new byte[digest.getDigestSize()];
        digest.doFinal(hash, 0);
        
        return Hex.toHexString(hash);
    }
    
    /**
     * 使用HMAC-SM3计算消息认证码，用于日志完整性保护
     * @param input 输入数据
     * @param key 密钥
     * @return HMAC-SM3值的十六进制字符串
     */
    public static String hmacSM3(String input, byte[] key) {
        if (input == null) {
            return null;
        }
        
        byte[] inputBytes = input.getBytes(StandardCharsets.UTF_8);
        HMac hmac = new HMac(new SM3Digest());
        KeyParameter keyParam = new KeyParameter(key);
        hmac.init(keyParam);
        hmac.update(inputBytes, 0, inputBytes.length);
        
        byte[] result = new byte[hmac.getMacSize()];
        hmac.doFinal(result, 0);
        
        return Hex.toHexString(result);
    }
    
    /**
     * SM4加密（使用配置文件中的默认密钥）
     * @param plaintext 明文
     * @return Base64编码的密文
     */
    public static String sm4Encrypt(String plaintext) throws Exception {
        return sm4Encrypt(plaintext, CryptoConfig.getSM4Key(), CryptoConfig.getSM4IV());
    }
    
    /**
     * SM4加密（使用指定密钥和IV）
     * @param plaintext 明文
     * @param key 密钥
     * @param iv 初始化向量
     * @return Base64编码的密文
     */
    public static String sm4Encrypt(String plaintext, byte[] key, byte[] iv) throws Exception {
        if (plaintext == null) {
            return null;
        }
        
        byte[] plaintextBytes = plaintext.getBytes(StandardCharsets.UTF_8);
        byte[] ciphertextBytes = sm4Crypt(true, plaintextBytes, key, iv);
        
        return Base64.getEncoder().encodeToString(ciphertextBytes);
    }
    
    /**
     * SM4解密（使用配置文件中的默认密钥）
     * @param ciphertext Base64编码的密文
     * @return 明文
     */
    public static String sm4Decrypt(String ciphertext) throws Exception {
        return sm4Decrypt(ciphertext, CryptoConfig.getSM4Key(), CryptoConfig.getSM4IV());
    }
    
    /**
     * SM4解密（使用指定密钥和IV）
     * @param ciphertext Base64编码的密文
     * @param key 密钥
     * @param iv 初始化向量
     * @return 明文
     */
    public static String sm4Decrypt(String ciphertext, byte[] key, byte[] iv) throws Exception {
        if (ciphertext == null) {
            return null;
        }
        
        byte[] ciphertextBytes = Base64.getDecoder().decode(ciphertext);
        byte[] plaintextBytes = sm4Crypt(false, ciphertextBytes, key, iv);
        
        return new String(plaintextBytes, StandardCharsets.UTF_8);
    }
    
    /**
     * SM4加密/解密通用方法
     * @param forEncryption 是否为加密操作
     * @param input 输入数据
     * @param key 密钥
     * @param iv 初始化向量
     * @return 加密/解密结果
     * @throws Exception 加密/解密异常
     */
    private static byte[] sm4Crypt(boolean forEncryption, byte[] input, byte[] key, byte[] iv) throws Exception {
        BlockCipherPadding padding = new PKCS7Padding();
        // 使用 CBCModeCipher 类型
        CBCModeCipher blockCipher = CBCBlockCipher.newInstance(new SM4Engine());
        PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(blockCipher, padding);
        
        KeyParameter keyParam = new KeyParameter(key);
        ParametersWithIV params = new ParametersWithIV(keyParam, iv);
        
        cipher.init(forEncryption, params);
        
        byte[] output = new byte[cipher.getOutputSize(input.length)];
        int length = cipher.processBytes(input, 0, input.length, output, 0);
        length += cipher.doFinal(output, length);
        
        byte[] result = new byte[length];
        System.arraycopy(output, 0, result, 0, length);
        
        return result;
    }
    
    /**
     * 生成随机密钥
     * @param length 密钥长度（字节）
     * @return 随机密钥
     */
    public static byte[] generateRandomKey(int length) {
        byte[] key = new byte[length];
        SecureRandom random = new SecureRandom();
        random.nextBytes(key);
        return key;
    }
    
    /**
     * 对身份证号进行部分脱敏处理（使用配置文件中的规则）
     * @param idCard 身份证号
     * @return 脱敏后的身份证号
     */
    public static String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 8) {
            return idCard;
        }

        int prefixLen = CryptoConfig.getIdCardMaskPrefix();
        int suffixLen = CryptoConfig.getIdCardMaskSuffix();
        String maskChar = CryptoConfig.getIdCardMaskChar();

        // 确保前缀和后缀长度不超过身份证号长度
        if (prefixLen + suffixLen >= idCard.length()) {
            return idCard; // 如果配置不合理，返回原值
        }

        int maskLen = idCard.length() - prefixLen - suffixLen;
        return idCard.substring(0, prefixLen) +
               maskChar.repeat(maskLen) +
               idCard.substring(idCard.length() - suffixLen);
    }
    
    /**
     * 对姓名进行脱敏处理（使用配置文件中的脱敏字符）
     * 规则：2个字的，中间加*；超过2个字的，中间全部用*代替
     * @param name 姓名
     * @return 脱敏后的姓名
     */
    public static String maskName(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }

        String maskChar = CryptoConfig.getNameMaskChar();
        int length = name.length();

        if (length == 1) {
            return name;
        } else if (length == 2) {
            return name.charAt(0) + maskChar;
        } else {
            return name.charAt(0) + maskChar.repeat(length - 2) + name.charAt(length - 1);
        }
    }
    
    /**
     * 对手机号进行脱敏处理（使用配置文件中的规则）
     * @param phone 手机号
     * @return 脱敏后的手机号
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 8) {
            return phone;
        }

        int prefixLen = CryptoConfig.getPhoneMaskPrefix();
        int suffixLen = CryptoConfig.getPhoneMaskSuffix();
        String maskChar = CryptoConfig.getPhoneMaskChar();

        // 确保前缀和后缀长度不超过手机号长度
        if (prefixLen + suffixLen >= phone.length()) {
            return phone; // 如果配置不合理，返回原值
        }

        int maskLen = phone.length() - prefixLen - suffixLen;
        return phone.substring(0, prefixLen) +
               maskChar.repeat(maskLen) +
               phone.substring(phone.length() - suffixLen);
    }
} 