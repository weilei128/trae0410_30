package com.example.qrcode.controller;

import com.example.qrcode.service.CsvRecordService;
import com.example.qrcode.service.QrCodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/qrcode")
public class QrCodeController {

    private static final Logger logger = LoggerFactory.getLogger(QrCodeController.class);

    @Autowired
    private QrCodeService qrCodeService;

    @Autowired
    private CsvRecordService csvRecordService;

    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateQrCode(@Valid @RequestBody QrCodeRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String filePath = qrCodeService.generateQrCode(request.getOrganization(), request.getUsername());

            response.put("code", 200);
            response.put("message", "二维码生成成功");
            response.put("data", filePath);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("二维码生成失败: {}", e.getMessage());

            response.put("code", 400);
            response.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateQrCodeGet(
            @RequestParam @NotBlank(message = "用户单位不能为空") String organization,
            @RequestParam @NotBlank(message = "用户名不能为空") String username) {
        Map<String, Object> response = new HashMap<>();

        try {
            String filePath = qrCodeService.generateQrCode(organization, username);

            response.put("code", 200);
            response.put("message", "二维码生成成功");
            response.put("data", filePath);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("二维码生成失败: {}", e.getMessage());

            response.put("code", 400);
            response.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    public static class QrCodeRequest {
        @NotBlank(message = "用户单位不能为空")
        @Size(max = 50, message = "用户单位长度不能超过50字符")
        private String organization;

        @NotBlank(message = "用户名不能为空")
        @Size(max = 50, message = "用户名长度不能超过50字符")
        private String username;

        public String getOrganization() {
            return organization;
        }

        public void setOrganization(String organization) {
            this.organization = organization;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }
    }

    @PostMapping("/record")
    public ResponseEntity<Map<String, Object>> saveUserRecord(@Valid @RequestBody QrCodeRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            csvRecordService.saveUserRecord(request.getOrganization(), request.getUsername());

            response.put("code", 200);
            response.put("message", "用户记录保存成功");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("用户记录保存失败: {}", e.getMessage());

            response.put("code", 400);
            response.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/record")
    public ResponseEntity<Map<String, Object>> saveUserRecordGet(
            @RequestParam @NotBlank(message = "用户单位不能为空") String organization,
            @RequestParam @NotBlank(message = "用户名不能为空") String username) {
        Map<String, Object> response = new HashMap<>();

        try {
            csvRecordService.saveUserRecord(organization, username);

            response.put("code", 200);
            response.put("message", "用户记录保存成功");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("用户记录保存失败: {}", e.getMessage());

            response.put("code", 400);
            response.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getHourlyStatistics() {
        Map<String, Object> response = new HashMap<>();

        try {
            Map<String, Object> statistics = csvRecordService.getHourlyStatistics();

            response.put("code", 200);
            response.put("message", "获取统计信息成功");
            response.put("data", statistics);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取统计信息失败: {}", e.getMessage());

            response.put("code", 400);
            response.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }
}
