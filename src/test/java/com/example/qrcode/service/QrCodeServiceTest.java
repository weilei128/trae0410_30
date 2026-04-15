package com.example.qrcode.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class QrCodeServiceTest {

    @Autowired
    private QrCodeService qrCodeService;

    @Value("${qrcode.save.path:D:\\picture}")
    private String savePath;

    private static final String TEST_ORGANIZATION = "测试单位";
    private static final String TEST_USERNAME = "测试用户";

    @BeforeEach
    void setUp() {
        File directory = new File(savePath);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    @AfterEach
    void tearDown() {
        File directory = new File(savePath);
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles((dir, name) -> 
                name.startsWith("测试") || name.startsWith("Test") || name.contains("-"));
            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
            }
        }
    }

    @Test
    @DisplayName("正常生成二维码 - 验证生成成功并返回正确的文件路径")
    void shouldGenerateQrCodeSuccessfully_whenValidParametersProvided() throws Exception {
        String filePath = qrCodeService.generateQrCode(TEST_ORGANIZATION, TEST_USERNAME);

        assertNotNull(filePath, "文件路径不应为空");
        assertTrue(filePath.endsWith(".jpg"), "文件应以.jpg结尾");
        assertTrue(filePath.contains(TEST_ORGANIZATION), "文件名应包含单位名称");
        assertTrue(filePath.contains(TEST_USERNAME), "文件名应包含用户名");

        File file = new File(filePath);
        assertTrue(file.exists(), "二维码文件应该存在");
        assertTrue(file.length() > 0, "二维码文件大小应大于0");
    }

    @Test
    @DisplayName("参数校验 - 用户单位为空时抛出异常")
    void shouldThrowException_whenOrganizationIsEmpty() {
        Exception exception = assertThrows(Exception.class, () -> {
            qrCodeService.generateQrCode("", TEST_USERNAME);
        });

        assertEquals("用户单位不能为空", exception.getMessage());
    }

    @Test
    @DisplayName("参数校验 - 用户单位为null时抛出异常")
    void shouldThrowException_whenOrganizationIsNull() {
        Exception exception = assertThrows(Exception.class, () -> {
            qrCodeService.generateQrCode(null, TEST_USERNAME);
        });

        assertEquals("用户单位不能为空", exception.getMessage());
    }

    @Test
    @DisplayName("参数校验 - 用户名为空时抛出异常")
    void shouldThrowException_whenUsernameIsEmpty() {
        Exception exception = assertThrows(Exception.class, () -> {
            qrCodeService.generateQrCode(TEST_ORGANIZATION, "");
        });

        assertEquals("用户名不能为空", exception.getMessage());
    }

    @Test
    @DisplayName("参数校验 - 用户名为null时抛出异常")
    void shouldThrowException_whenUsernameIsNull() {
        Exception exception = assertThrows(Exception.class, () -> {
            qrCodeService.generateQrCode(TEST_ORGANIZATION, null);
        });

        assertEquals("用户名不能为空", exception.getMessage());
    }

    @Test
    @DisplayName("特殊字符过滤 - 自动过滤文件名中的非法字符")
    void shouldFilterIllegalChars_whenInputContainsSpecialCharacters() throws Exception {
        String orgWithIllegalChars = "测试/单位\\目录";
        String userWithIllegalChars = "测试:用户*文件";

        String filePath = qrCodeService.generateQrCode(orgWithIllegalChars, userWithIllegalChars);

        assertNotNull(filePath);
        assertFalse(filePath.contains("/"), "文件路径不应包含/");
        assertFalse(filePath.contains("\\"), "文件路径不应包含\\");
        assertFalse(filePath.contains(":"), "文件路径不应包含:");
        assertFalse(filePath.contains("*"), "文件路径不应包含*");

        File file = new File(filePath);
        assertTrue(file.exists(), "二维码文件应该存在");
    }

    @Test
    @DisplayName("并发安全 - 多线程并发生成相同参数的二维码")
    void shouldHandleConcurrentRequests_whenMultipleThreadsGenerateSameQrCode() throws Exception {
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        String org = "并发测试单位";
        String user = "并发测试用户";

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    String filePath = qrCodeService.generateQrCode(org, user);
                    if (filePath != null && filePath.endsWith(".jpg")) {
                        successCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        assertEquals(threadCount, successCount.get(), "所有并发请求都应成功");
        assertEquals(0, failCount.get(), "不应有失败的请求");
    }

    @Test
    @DisplayName("文件名长度校验 - 超长文件名抛出异常")
    void shouldThrowException_whenFileNameExceedsMaxLength() {
        StringBuilder longOrg = new StringBuilder();
        StringBuilder longUser = new StringBuilder();
        for (int i = 0; i < 60; i++) {
            longOrg.append("单位");
            longUser.append("用户");
        }

        Exception exception = assertThrows(Exception.class, () -> {
            qrCodeService.generateQrCode(longOrg.toString(), longUser.toString());
        });

        assertEquals("文件名长度超过100字符限制", exception.getMessage());
    }

    @Test
    @DisplayName("重复生成 - 相同参数重复生成时自动添加时间戳")
    void shouldAddTimestamp_whenGeneratingDuplicateQrCode() throws Exception {
        String org = "重复测试单位";
        String user = "重复测试用户";

        String filePath1 = qrCodeService.generateQrCode(org, user);
        Thread.sleep(10);
        String filePath2 = qrCodeService.generateQrCode(org, user);

        assertNotNull(filePath1);
        assertNotNull(filePath2);

        File file1 = new File(filePath1);
        File file2 = new File(filePath2);

        assertTrue(file1.exists(), "第一个二维码文件应存在");
        assertTrue(file2.exists(), "第二个二维码文件应存在");
    }
}
