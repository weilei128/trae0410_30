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

    @Test
    public void testSaveUserRecord() throws Exception {
        csvRecordService.saveUserRecord("测试单位", "测试用户");
    }

    @Test
    public void testConcurrentSave() throws Exception {
        int threadCount = 30;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executorService.submit(() -> {
                try {
                    String organization = "单位" + (index % 5);
                    String username = "用户" + (index % 10);
                    csvRecordService.saveUserRecord(organization, username);
                    System.out.println("线程" + index + "保存成功");
                } catch (Exception e) {
                    System.err.println("线程" + index + "保存失败: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();
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
    }
}
