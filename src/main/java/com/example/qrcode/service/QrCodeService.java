package com.example.qrcode.service;

import com.example.qrcode.util.SnowflakeIdGenerator;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class QrCodeService {

    private static final Logger logger = LoggerFactory.getLogger(QrCodeService.class);

    @Value("${qrcode.save.path:D:\\\\picture}")
    private String savePath;

    @Value("${qrcode.width:300}")
    private int qrCodeWidth;

    @Value("${qrcode.height:300}")
    private int qrCodeHeight;

    @Autowired
    private SnowflakeIdGenerator snowflakeIdGenerator;

    private final ConcurrentHashMap<String, ReentrantLock> fileLocks = new ConcurrentHashMap<>();

    private static final int MAX_FILENAME_LENGTH = 100;

    @PostConstruct
    public void init() {
        File directory = new File(savePath);
        if (!directory.exists()) {
            directory.mkdirs();
            logger.info("创建保存目录: {}", savePath);
        }
    }

    public String generateQrCode(String organization, String username) throws Exception {
        validateParameters(organization, username);

        String filteredOrg = filterIllegalChars(organization);
        String filteredUser = filterIllegalChars(username);

        String fileName = filteredOrg + "-" + filteredUser + ".jpg";
        validateFileNameLength(fileName);

        String uniqueId = String.valueOf(snowflakeIdGenerator.nextId());
        String currentTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String qrContent = String.format("%s-%s-%s-%s", filteredOrg, filteredUser, currentTime, uniqueId);

        String filePath = savePath + File.separator + fileName;

        String lockKey = filteredOrg + "-" + filteredUser;
        ReentrantLock lock = fileLocks.computeIfAbsent(lockKey, k -> new ReentrantLock());

        lock.lock();
        try {
            File file = new File(filePath);
            if (file.exists()) {
                String timestamp = String.valueOf(System.currentTimeMillis());
                fileName = filteredOrg + "-" + filteredUser + "-" + timestamp + ".jpg";
                filePath = savePath + File.separator + fileName;
                logger.warn("文件已存在，生成新文件名: {}", fileName);
            }

            createQrCodeImage(qrContent, filePath);
            logger.info("二维码生成成功: {}", filePath);

            return filePath;
        } finally {
            lock.unlock();
        }
    }

    private void validateParameters(String organization, String username) throws Exception {
        if (organization == null || organization.trim().isEmpty()) {
            throw new Exception("用户单位不能为空");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new Exception("用户名不能为空");
        }
    }

    private void validateFileNameLength(String fileName) throws Exception {
        if (fileName.length() > MAX_FILENAME_LENGTH) {
            throw new Exception("文件名长度超过100字符限制");
        }
    }

    private String filterIllegalChars(String input) {
        if (input == null) {
            return "";
        }
        return input.replaceAll("[\\\\/:*?\"<>|]", "").trim();
    }

    private void createQrCodeImage(String content, String filePath) throws Exception {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();

        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.MARGIN, 1);

        BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, qrCodeWidth, qrCodeHeight, hints);

        Path path = Paths.get(filePath);
        MatrixToImageWriter.writeToPath(bitMatrix, "jpg", path);
    }
}
