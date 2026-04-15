package com.example.qrcode.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Service
public class CsvRecordService {

    private static final Logger logger = LoggerFactory.getLogger(CsvRecordService.class);

    @Value("${qrcode.save.path:D:\\\\picture}")
    private String savePath;

    private static final String CSV_FILE_NAME = "user_records.csv";
    private static final String[] CSV_HEADERS = {"id", "organization", "username", "create_time"};

    private final AtomicLong idGenerator = new AtomicLong(0);
    private final ReentrantLock csvLock = new ReentrantLock();

    @PostConstruct
    public void init() {
        try {
            Path csvPath = Paths.get(savePath, CSV_FILE_NAME);
            File csvFile = csvPath.toFile();

            if (!csvFile.exists()) {
                createNewCsvFile(csvPath);
                logger.info("创建CSV文件: {}", csvPath);
            } else {
                loadMaxIdFromCsv(csvPath);
                logger.info("加载CSV文件，当前最大ID: {}", idGenerator.get());
            }
        } catch (Exception e) {
            logger.error("初始化CSV服务失败: {}", e.getMessage(), e);
        }
    }

    public void saveUserRecord(String organization, String username) throws Exception {
        if (organization == null || organization.trim().isEmpty()) {
            throw new Exception("用户单位不能为空");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new Exception("用户名不能为空");
        }

        long id = idGenerator.incrementAndGet();
        String createTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        String filteredOrg = filterIllegalChars(organization);
        String filteredUser = filterIllegalChars(username);

        String csvLine = String.format("%d,%s,%s,%s", id, filteredOrg, filteredUser, createTime);

        csvLock.lock();
        try {
            Path csvPath = Paths.get(savePath, CSV_FILE_NAME);
            Files.write(csvPath, (csvLine + System.lineSeparator()).getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            logger.info("保存用户记录到CSV: id={}, organization={}, username={}", id, filteredOrg, filteredUser);
        } finally {
            csvLock.unlock();
        }
    }

    public int countUsersInLastHour() {
        try {
            Path csvPath = Paths.get(savePath, CSV_FILE_NAME);
            if (!csvPath.toFile().exists()) {
                return 0;
            }

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.HOUR_OF_DAY, -1);
            Date oneHourAgo = calendar.getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            List<String> lines = Files.readAllLines(csvPath, StandardCharsets.UTF_8);
            int count = 0;

            for (String line : lines) {
                if (line.startsWith("id,")) {
                    continue;
                }

                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    try {
                        Date recordTime = sdf.parse(parts[3]);
                        if (recordTime.after(oneHourAgo)) {
                            count++;
                        }
                    } catch (Exception e) {
                        logger.warn("解析时间失败: {}", line);
                    }
                }
            }

            return count;
        } catch (Exception e) {
            logger.error("统计用户数量失败: {}", e.getMessage(), e);
            return 0;
        }
    }

    public Map<String, Object> getHourlyStatistics() {
        Map<String, Object> result = new HashMap<>();
        int count = countUsersInLastHour();
        result.put("count", count);
        result.put("timeRange", "过去1小时");
        result.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        return result;
    }

    private void createNewCsvFile(Path csvPath) throws IOException {
        File directory = csvPath.getParent().toFile();
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String header = String.join(",", CSV_HEADERS);
        Files.write(csvPath, (header + System.lineSeparator()).getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE);
    }

    private void loadMaxIdFromCsv(Path csvPath) {
        try {
            List<String> lines = Files.readAllLines(csvPath, StandardCharsets.UTF_8);
            long maxId = 0;

            for (String line : lines) {
                if (line.startsWith("id,")) {
                    continue;
                }

                String[] parts = line.split(",");
                if (parts.length > 0) {
                    try {
                        long id = Long.parseLong(parts[0]);
                        if (id > maxId) {
                            maxId = id;
                        }
                    } catch (NumberFormatException e) {
                        logger.warn("解析ID失败: {}", line);
                    }
                }
            }

            idGenerator.set(maxId);
        } catch (Exception e) {
            logger.error("加载CSV文件失败: {}", e.getMessage(), e);
            idGenerator.set(0);
        }
    }

    private String filterIllegalChars(String input) {
        if (input == null) {
            return "";
        }
        return input.replaceAll("[\\\\/:*?\"<>|,]", "").trim();
    }
}
