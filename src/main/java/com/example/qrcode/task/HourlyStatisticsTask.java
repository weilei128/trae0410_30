package com.example.qrcode.task;

import com.example.qrcode.service.CsvRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class HourlyStatisticsTask {

    private static final Logger logger = LoggerFactory.getLogger(HourlyStatisticsTask.class);

    @Autowired
    private CsvRecordService csvRecordService;

    @Scheduled(fixedRate = 3600000)
    public void executeHourlyStatistics() {
        logger.info("开始执行每小时用户统计任务...");

        try {
            Map<String, Object> statistics = csvRecordService.getHourlyStatistics();
            int count = (int) statistics.get("count");
            String timestamp = (String) statistics.get("timestamp");

            logger.info("每小时统计结果 - 时间: {}, 过去1小时新增用户数量: {}", timestamp, count);

        } catch (Exception e) {
            logger.error("每小时统计任务执行失败: {}", e.getMessage(), e);
        }
    }
}
