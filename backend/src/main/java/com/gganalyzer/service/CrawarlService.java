package com.gganalyzer.service;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class CrawarlService {
    public static String executeGetRequest(String url) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            return httpClient.execute(request, response -> {
                int status = response.getCode();
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response.getEntity();
                    return entity != null ? EntityUtils.toString(entity) : null;
                } else {
                    return null;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Async
    @Scheduled(fixedRate = 3600000)
    public void autoExecuteGetRequest() {
        String url = "https://www.bilibili.com/v/game/match/singledata/32216";
        // String html = executeGetRequest(url);
        // if (html != null) {
        // saveHtmlToFile(html);
        // }
    }

    private void saveHtmlToFile(String html) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = "match_data_" + timestamp + ".html";
            Path directory = Paths.get("crawled_data");
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }
            Path filePath = directory.resolve(filename);
            Files.write(filePath, html.getBytes(StandardCharsets.UTF_8));
            System.out.println("Saved HTML to " + filePath.toAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
