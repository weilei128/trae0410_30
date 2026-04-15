package com.example.qrcode.controller;

import com.example.qrcode.service.CsvRecordService;
import com.example.qrcode.service.QrCodeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * QrCodeController单元测试类
 * 测试二维码生成接口的各种场景
 */
@WebMvcTest(QrCodeController.class)
class QrCodeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private QrCodeService qrCodeService;

    @MockBean
    private CsvRecordService csvRecordService;

    private static final String TEST_ORGANIZATION = "测试单位";
    private static final String TEST_USERNAME = "测试用户";
    private static final String TEST_FILE_PATH = "D:\\picture\\测试单位-测试用户.jpg";

    @BeforeEach
    void setUp() throws Exception {
        // 默认模拟成功生成二维码
        when(qrCodeService.generateQrCode(anyString(), anyString())).thenReturn(TEST_FILE_PATH);
    }

    /**
     * 测试场景1: POST请求生成二维码成功
     * 验证正常参数下二维码生成成功，返回200状态码和正确的响应数据
     */
    @Test
    @DisplayName("POST请求生成二维码成功")
    void generateQrCode_PostRequest_Success() throws Exception {
        // 构建请求体
        QrCodeController.QrCodeRequest request = new QrCodeController.QrCodeRequest();
        request.setOrganization(TEST_ORGANIZATION);
        request.setUsername(TEST_USERNAME);

        // 执行请求并验证结果
        mockMvc.perform(post("/api/qrcode/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("二维码生成成功"))
                .andExpect(jsonPath("$.data").value(TEST_FILE_PATH));
    }

    /**
     * 测试场景2: GET请求生成二维码成功
     * 验证使用查询参数方式生成二维码成功
     */
    @Test
    @DisplayName("GET请求生成二维码成功")
    void generateQrCode_GetRequest_Success() throws Exception {
        mockMvc.perform(get("/api/qrcode/generate")
                .param("organization", TEST_ORGANIZATION)
                .param("username", TEST_USERNAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("二维码生成成功"))
                .andExpect(jsonPath("$.data").value(TEST_FILE_PATH));
    }

    /**
     * 测试场景3: POST请求参数验证失败-用户单位为空
     * 验证当organization参数为空时返回400错误
     */
    @Test
    @DisplayName("POST请求用户单位为空返回400错误")
    void generateQrCode_PostRequest_EmptyOrganization_ReturnsBadRequest() throws Exception {
        QrCodeController.QrCodeRequest request = new QrCodeController.QrCodeRequest();
        request.setOrganization("");
        request.setUsername(TEST_USERNAME);

        mockMvc.perform(post("/api/qrcode/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    /**
     * 测试场景4: 二维码生成服务异常处理
     * 验证当服务层抛出异常时返回400错误和错误信息
     */
    @Test
    @DisplayName("服务异常时返回400错误")
    void generateQrCode_ServiceException_ReturnsBadRequest() throws Exception {
        // 模拟服务抛出异常
        String errorMessage = "文件名长度超过限制";
        when(qrCodeService.generateQrCode(anyString(), anyString()))
                .thenThrow(new Exception(errorMessage));

        mockMvc.perform(get("/api/qrcode/generate")
                .param("organization", TEST_ORGANIZATION)
                .param("username", TEST_USERNAME))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(errorMessage));
    }
}
