package com.example.qrcode.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class CsvRecordServiceTest {

    @Autowired
    private CsvRecordService csvRecordService;


    public void testSaveUserRecord() throws Exception {
    public void testConcurrentSave() throws Exception {
        int threadCount = 30;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
    }

    @Test
    public void testGetHourlyStatistics() {
        Map<String, Object> statistics = csvRecordService.getHourlyStatistics();
        assertNotNull(statistics);
        assertTrue(statistics.containsKey("count"));
        assertTrue(statistics.containsKey("timeRange"));
        assertTrue(statistics.containsKey("timestamp"));
    }

    @Test
    public void testInvalidOrganization() {
        Exception exception = assertThrows(Exception.class, () -> {
            csvRecordService.saveUserRecord("", "测试用户");
        });
        assertEquals("用户单位不能为空", exception.getMessage());
    }

    @Test
    public void testInvalidUsername() {
        Exception exception = assertThrows(Exception.class, () -> {
            csvRecordService.saveUserRecord("测试单位", "");
        });
        assertEquals("用户名不能为空", exception.getMessage());
