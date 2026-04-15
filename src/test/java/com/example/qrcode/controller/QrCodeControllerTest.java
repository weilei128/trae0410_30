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

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(QrCodeController.class)
public class QrCodeControllerTest {

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

    private QrCodeController.QrCodeRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new QrCodeController.QrCodeRequest();
        validRequest.setOrganization(TEST_ORGANIZATION);
        validRequest.setUsername(TEST_USERNAME);
    }

    @Test
    @DisplayName("POST生成二维码 - 正常请求返回成功响应")
    void shouldReturnSuccessResponse_whenPostGenerateQrCodeWithValidRequest() throws Exception {
        when(qrCodeService.generateQrCode(TEST_ORGANIZATION, TEST_USERNAME))
                .thenReturn(TEST_FILE_PATH);

        mockMvc.perform(post("/api/qrcode/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("二维码生成成功"))
                .andExpect(jsonPath("$.data").value(TEST_FILE_PATH));
    }

    @Test
    @DisplayName("POST生成二维码 - 参数校验失败时返回400错误")
    void shouldReturnBadRequest_whenPostGenerateQrCodeWithInvalidParameters() throws Exception {
        QrCodeController.QrCodeRequest invalidRequest = new QrCodeController.QrCodeRequest();
        invalidRequest.setOrganization("");
        invalidRequest.setUsername("");

        mockMvc.perform(post("/api/qrcode/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET生成二维码 - 正常请求返回成功响应")
    void shouldReturnSuccessResponse_whenGetGenerateQrCodeWithValidParameters() throws Exception {
        when(qrCodeService.generateQrCode(TEST_ORGANIZATION, TEST_USERNAME))
                .thenReturn(TEST_FILE_PATH);

        mockMvc.perform(get("/api/qrcode/generate")
                        .param("organization", TEST_ORGANIZATION)
                        .param("username", TEST_USERNAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("二维码生成成功"))
                .andExpect(jsonPath("$.data").value(TEST_FILE_PATH));
    }

    @Test
    @DisplayName("GET生成二维码 - 缺少必要参数时返回400错误")
    void shouldReturnBadRequest_whenGetGenerateQrCodeWithMissingParameters() throws Exception {
        mockMvc.perform(get("/api/qrcode/generate"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST保存用户记录 - 正常请求返回成功响应")
    void shouldReturnSuccessResponse_whenPostSaveUserRecordWithValidRequest() throws Exception {
        mockMvc.perform(post("/api/qrcode/record")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("用户记录保存成功"));
    }

    @Test
    @DisplayName("GET保存用户记录 - 正常请求返回成功响应")
    void shouldReturnSuccessResponse_whenGetSaveUserRecordWithValidParameters() throws Exception {
        mockMvc.perform(get("/api/qrcode/record")
                        .param("organization", TEST_ORGANIZATION)
                        .param("username", TEST_USERNAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("用户记录保存成功"));
    }

    @Test
    @DisplayName("GET获取统计信息 - 正常返回统计数据")
    void shouldReturnStatistics_whenGetHourlyStatistics() throws Exception {
        Map<String, Object> mockStatistics = new HashMap<>();
        mockStatistics.put("count", 10);
        mockStatistics.put("timeRange", "过去1小时");

        when(csvRecordService.getHourlyStatistics()).thenReturn(mockStatistics);

        mockMvc.perform(get("/api/qrcode/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("获取统计信息成功"))
                .andExpect(jsonPath("$.data.count").value(10))
                .andExpect(jsonPath("$.data.timeRange").value("过去1小时"));
    }

    @Test
    @DisplayName("POST生成二维码 - 服务异常时返回错误响应")
    void shouldReturnErrorResponse_whenServiceThrowsException() throws Exception {
        when(qrCodeService.generateQrCode(anyString(), anyString()))
                .thenThrow(new Exception("用户单位不能为空"));

        mockMvc.perform(post("/api/qrcode/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("用户单位不能为空"));
    }
}
