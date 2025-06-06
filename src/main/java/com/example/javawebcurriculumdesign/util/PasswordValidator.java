package com.example.javawebcurriculumdesign.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 密码复杂度验证工具类
 * 按照等保三级要求验证密码复杂度
 */
public class PasswordValidator {
    
    // 密码复杂度要求
    private static final int MIN_LENGTH = 8;
    private static final Pattern DIGIT_PATTERN = Pattern.compile(".*\\d.*");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile(".*[a-z].*");
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile(".*[A-Z].*");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");
    
    /**
     * 验证密码复杂度
     * @param password 密码
     * @return 验证结果
     */
    public static PasswordValidationResult validate(String password) {
        List<String> errors = new ArrayList<>();
        
        if (password == null || password.isEmpty()) {
            errors.add("密码不能为空");
            return new PasswordValidationResult(false, errors);
        }
        
        // 检查长度
        if (password.length() < MIN_LENGTH) {
            errors.add("密码长度至少需要" + MIN_LENGTH + "位");
        }
        
        // 检查是否包含数字
        if (!DIGIT_PATTERN.matcher(password).matches()) {
            errors.add("密码必须包含至少一个数字");
        }
        
        // 检查是否包含小写字母
        if (!LOWERCASE_PATTERN.matcher(password).matches()) {
            errors.add("密码必须包含至少一个小写字母");
        }
        
        // 检查是否包含大写字母
        if (!UPPERCASE_PATTERN.matcher(password).matches()) {
            errors.add("密码必须包含至少一个大写字母");
        }
        
        // 检查是否包含特殊字符
        if (!SPECIAL_CHAR_PATTERN.matcher(password).matches()) {
            errors.add("密码必须包含至少一个特殊字符(!@#$%^&*等)");
        }
        
        // 检查是否包含连续字符
        if (hasConsecutiveChars(password)) {
            errors.add("密码不能包含连续的相同字符");
        }
        
        // 检查是否为常见弱密码
        if (isCommonWeakPassword(password)) {
            errors.add("密码过于简单，请使用更复杂的密码");
        }
        
        boolean isValid = errors.isEmpty();
        return new PasswordValidationResult(isValid, errors);
    }
    
    /**
     * 检查是否包含连续的相同字符
     * @param password 密码
     * @return 是否包含连续字符
     */
    private static boolean hasConsecutiveChars(String password) {
        for (int i = 0; i < password.length() - 2; i++) {
            if (password.charAt(i) == password.charAt(i + 1) && 
                password.charAt(i + 1) == password.charAt(i + 2)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 检查是否为常见弱密码
     * @param password 密码
     * @return 是否为弱密码
     */
    private static boolean isCommonWeakPassword(String password) {
        String lowerPassword = password.toLowerCase();
        
        // 常见弱密码列表
        String[] weakPasswords = {
            "password", "123456", "12345678", "qwerty", "abc123",
            "password123", "admin", "root", "user", "test",
            "123456789", "1234567890", "qwerty123", "admin123"
        };
        
        for (String weak : weakPasswords) {
            if (lowerPassword.contains(weak)) {
                return true;
            }
        }
        
        // 检查是否为键盘序列
        String[] keyboardSequences = {
            "qwertyuiop", "asdfghjkl", "zxcvbnm",
            "1234567890", "0987654321"
        };
        
        for (String sequence : keyboardSequences) {
            if (lowerPassword.contains(sequence.substring(0, Math.min(4, sequence.length())))) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 获取密码强度等级
     * @param password 密码
     * @return 强度等级 (1-5)
     */
    public static int getPasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return 0;
        }
        
        int score = 0;
        
        // 长度评分
        if (password.length() >= 8) score++;
        if (password.length() >= 12) score++;
        
        // 字符类型评分
        if (DIGIT_PATTERN.matcher(password).matches()) score++;
        if (LOWERCASE_PATTERN.matcher(password).matches()) score++;
        if (UPPERCASE_PATTERN.matcher(password).matches()) score++;
        if (SPECIAL_CHAR_PATTERN.matcher(password).matches()) score++;
        
        // 复杂度评分
        if (!hasConsecutiveChars(password)) score++;
        if (!isCommonWeakPassword(password)) score++;
        
        return Math.min(5, score);
    }
    
    /**
     * 获取密码强度描述
     * @param password 密码
     * @return 强度描述
     */
    public static String getPasswordStrengthDescription(String password) {
        int strength = getPasswordStrength(password);
        
        switch (strength) {
            case 0:
            case 1:
                return "非常弱";
            case 2:
                return "弱";
            case 3:
                return "一般";
            case 4:
                return "强";
            case 5:
                return "非常强";
            default:
                return "未知";
        }
    }
    
    /**
     * 密码验证结果类
     */
    public static class PasswordValidationResult {
        private final boolean valid;
        private final List<String> errors;
        
        public PasswordValidationResult(boolean valid, List<String> errors) {
            this.valid = valid;
            this.errors = errors;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public List<String> getErrors() {
            return errors;
        }
        
        public String getErrorMessage() {
            if (errors.isEmpty()) {
                return "";
            }
            return String.join("; ", errors);
        }
    }
}
