package com.example.qrcode.service;

import com.example.qrcode.util.SnowflakeIdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * QrCodeService单元测试类
 * 测试二维码生成服务的核心业务逻辑
 */
class QrCodeServiceTest {

    private QrCodeService qrCodeService;
    private SnowflakeIdGenerator snowflakeIdGenerator;

    @TempDir
    Path tempDir;

    private static final String TEST_ORGANIZATION = "测试单位";
    private static final String TEST_USERNAME = "测试用户";
    private static final long TEST_SNOWFLAKE_ID = 123456789L;

    @BeforeEach
    void setUp() {
        qrCodeService = new QrCodeService();
        snowflakeIdGenerator = mock(SnowflakeIdGenerator.class);

        // 使用反射设置私有字段
        ReflectionTestUtils.setField(qrCodeService, "savePath", tempDir.toString());
        ReflectionTestUtils.setField(qrCodeService, "qrCodeWidth", 300);
        ReflectionTestUtils.setField(qrCodeService, "qrCodeHeight", 300);
        ReflectionTestUtils.setField(qrCodeService, "snowflakeIdGenerator", snowflakeIdGenerator);

        // 默认模拟雪花ID生成器
        when(snowflakeIdGenerator.nextId()).thenReturn(TEST_SNOWFLAKE_ID);
    }

    /**
     * 测试场景1: 正常生成二维码
     * 验证使用有效参数能够成功生成二维码文件
     */
    @Test
    @DisplayName("正常生成二维码文件")
    void generateQrCode_WithValidParams_Success() throws Exception {
        // 执行测试
        String filePath = qrCodeService.generateQrCode(TEST_ORGANIZATION, TEST_USERNAME);

        // 验证结果
        assertNotNull(filePath);
        assertTrue(filePath.contains(TEST_ORGANIZATION));
        assertTrue(filePath.contains(TEST_USERNAME));
        assertTrue(filePath.endsWith(".jpg"));

        // 验证文件是否实际创建
        File generatedFile = new File(filePath);
        assertTrue(generatedFile.exists());
        assertTrue(generatedFile.length() > 0);
    }

    /**
     * 测试场景2: 用户单位为空抛出异常
     * 验证当organization参数为空或空白时抛出异常
     */
    @Test
    @DisplayName("用户单位为空时抛出异常")
    void generateQrCode_EmptyOrganization_ThrowsException() {
        Exception exception = assertThrows(Exception.class, () -> {
            qrCodeService.generateQrCode("", TEST_USERNAME);
        });
        assertEquals("用户单位不能为空", exception.getMessage());
    }

    /**
     * 测试场景3: 用户名为空抛出异常
     * 验证当username参数为空或空白时抛出异常
     */
    @Test
    @DisplayName("用户名为空时抛出异常")
    void generateQrCode_EmptyUsername_ThrowsException() {
        Exception exception = assertThrows(Exception.class, () -> {
            qrCodeService.generateQrCode(TEST_ORGANIZATION, "");
        });
        assertEquals("用户名不能为空", exception.getMessage());
    }

    /**
     * 测试场景4: 非法字符过滤
     * 验证输入中的非法字符被正确过滤并生成二维码
     */
    @Test
    @DisplayName("过滤非法字符后生成二维码")
    void generateQrCode_WithIllegalChars_FiltersAndSuccess() throws Exception {
        // 包含非法字符的输入
        String orgWithIllegalChars = "TestOrg/:*?\"<>|";
        String userWithIllegalChars = "TestUser/:*?\"<>|";

        String filePath = qrCodeService.generateQrCode(orgWithIllegalChars, userWithIllegalChars);

        // 验证结果
        assertNotNull(filePath);
        // 验证非法字符被过滤 - 只检查文件名部分
        String fileName = new File(filePath).getName();
        assertFalse(fileName.contains("/"), "文件名不应包含/");
        assertFalse(fileName.contains(":"), "文件名不应包含:");
        assertFalse(fileName.contains("*"), "文件名不应包含*");
        assertFalse(fileName.contains("?"), "文件名不应包含?");
        assertFalse(fileName.contains("\""), "文件名不应包含\"");
        assertFalse(fileName.contains("<"), "文件名不应包含<");
        assertFalse(fileName.contains(">"), "文件名不应包含>");
        assertFalse(fileName.contains("|"), "文件名不应包含|");

        // 验证过滤后的内容存在
        assertTrue(fileName.contains("TestOrg"), "文件名应包含过滤后的组织名");
        assertTrue(fileName.contains("TestUser"), "文件名应包含过滤后的用户名");

        // 验证文件创建成功
        File generatedFile = new File(filePath);
        assertTrue(generatedFile.exists());
    }
}
