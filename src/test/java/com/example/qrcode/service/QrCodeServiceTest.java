package com.example.qrcode.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class QrCodeServiceTest {

    @Autowired
    private QrCodeService qrCodeService;

    @Test
    public void testGenerateQrCode() throws Exception {
        String filePath = qrCodeService.generateQrCode("测试单位", "测试用户");
        assertNotNull(filePath);
        assertTrue(filePath.endsWith(".jpg"));
    }

    @Test
    public void testConcurrentGenerate() throws Exception {
        int threadCount = 30;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executorService.submit(() -> {
                try {
                    String organization = "单位" + (index % 5);
                    String username = "用户" + (index % 10);
                    String filePath = qrCodeService.generateQrCode(organization, username);
                    System.out.println("线程" + index + "生成成功: " + filePath);
                } catch (Exception e) {
                    System.err.println("线程" + index + "生成失败: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();
    }

    @Test
    public void testInvalidOrganization() {
        Exception exception = assertThrows(Exception.class, () -> {
            qrCodeService.generateQrCode("", "测试用户");
        });
        assertEquals("用户单位不能为空", exception.getMessage());
    }

    @Test
    public void testInvalidUsername() {
        Exception exception = assertThrows(Exception.class, () -> {
            qrCodeService.generateQrCode("测试单位", "");
        });
        assertEquals("用户名不能为空", exception.getMessage());
    }

    @Test
    public void testIllegalChars() throws Exception {
        String filePath = qrCodeService.generateQrCode("测试/单位", "测试:用户");
        assertNotNull(filePath);
        assertFalse(filePath.contains("/"));
        assertFalse(filePath.contains(":"));
    }
}
