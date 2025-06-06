package com.example.javawebcurriculumdesign.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 二维码生成工具类
 */
public class QRCodeUtil {
    
    /**
     * 生成二维码图像
     * @param content 二维码内容
     * @param width 宽度
     * @param height 高度
     * @param foregroundColor 前景色
     * @param backgroundColor 背景色
     * @return 二维码图像
     */
    public static BufferedImage generateQRCode(String content, int width, int height, 
                                              Color foregroundColor, Color backgroundColor) throws WriterException {
        // 设置二维码参数
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H); // 容错级别最高
        hints.put(EncodeHintType.MARGIN, 1); // 边距
        
        // 生成二维码矩阵
        BitMatrix bitMatrix = new MultiFormatWriter().encode(
                content,
                BarcodeFormat.QR_CODE,
                width,
                height,
                hints
        );
        
        // 转换为图像
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, bitMatrix.get(x, y) ? foregroundColor.getRGB() : backgroundColor.getRGB());
            }
        }
        
        return image;
    }
    
    /**
     * 生成紫色二维码图像（有效通行码）
     * @param content 二维码内容
     * @return 二维码图像
     */
    public static BufferedImage generateValidQRCode(String content) throws WriterException {
        int size = CryptoConfig.getQRCodeSize();
        return generateQRCode(content, size, size, new Color(138, 43, 226), Color.WHITE);
    }

    /**
     * 生成灰色二维码图像（无效通行码）
     * @param content 二维码内容
     * @return 二维码图像
     */
    public static BufferedImage generateInvalidQRCode(String content) throws WriterException {
        int size = CryptoConfig.getQRCodeSize();
        return generateQRCode(content, size, size, Color.GRAY, Color.WHITE);
    }
    
    /**
     * 将二维码图像转换为Base64编码
     * @param image 二维码图像
     * @return Base64编码的图像
     */
    public static String toBase64(BufferedImage image) throws IOException {
        String format = CryptoConfig.getQRCodeFormat();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, format, outputStream);
        return "data:image/" + format + ";base64," +
                Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }
    
    /**
     * 生成有效通行码的Base64编码
     * @param content 二维码内容
     * @return Base64编码的有效通行码
     */
    public static String generateValidQRCodeBase64(String content) throws WriterException, IOException {
        BufferedImage image = generateValidQRCode(content);
        return toBase64(image);
    }
    
    /**
     * 生成无效通行码的Base64编码
     * @param content 二维码内容
     * @return Base64编码的无效通行码
     */
    public static String generateInvalidQRCodeBase64(String content) throws WriterException, IOException {
        BufferedImage image = generateInvalidQRCode(content);
        return toBase64(image);
    }
} 