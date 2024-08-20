package ru.mattakvshi.throttlingtesttask;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ThrottlingTestTaskApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    private static final Logger logger = LoggerFactory.getLogger(ThrottlingTestTaskApplicationTests.class);

    //ТЕСТЫ АДЕКВАТНО РАБОТАЮТ ТОЛЬКО ПО ОТДЕЛЬНОСТИ

    @Test
    public void mainTestMethod() throws Exception {

        //Тест последжовательных запросов
        //testThrottlingLimit();

        //Тест параллельных запросов
        //parallelTestThrottlingLimit();

        //Тест параллельных запросов (две партии с перерывом в 1 мин)
        //parallelTestThrottlingLimitTwice();

        //Тест с паралельными запросами с 5 разных IP по 51 одному запросу
        parallelTestThrottlingLimitWithMultipleIPs();

    }


    public void testThrottlingLimit() throws Exception {
        for (int i = 1; i <= 50; i++) {
            mockMvc.perform(get("/api/throttling/test"))
                    .andExpect(status().isOk());

        }

        mockMvc.perform(get("/api/throttling/test"))
                .andExpect(status().is(502));
    }



    public void parallelTestThrottlingLimit() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<ResultActions> results = new ArrayList<>();

        for (int i = 0; i < 51; i++) {
            executor.submit(() -> {
                try {
                    ResultActions result = mockMvc.perform(get("/api/throttling/test"));
                    synchronized (results) {
                        results.add(result);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        for (int i = 0; i < results.size(); i++) {
            if (i < 50) {
                results.get(i).andExpect(status().isOk());
            } else {
                results.get(i).andExpect(status().is(502));
            }
        }
    }


    public void parallelTestThrottlingLimitTwice() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<ResultActions> resultsFirstBatch = new ArrayList<>();
        List<ResultActions> resultsSecondBatch = new ArrayList<>();

        // Первая партия запросов
        for (int i = 0; i < 51; i++) {
            executor.submit(() -> {
                try {
                    ResultActions result = mockMvc.perform(get("/api/throttling/test"));
                    synchronized (resultsFirstBatch) {
                        resultsFirstBatch.add(result);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        // Проверяем первую партию запросов
        for (int i = 0; i < resultsFirstBatch.size(); i++) {
            if (i < 50) {
                resultsFirstBatch.get(i).andExpect(status().isOk());
            } else {
                resultsFirstBatch.get(i).andExpect(status().is(502));
            }
        }

        // Ожидание
        TimeUnit.SECONDS.sleep(62);

        // Восстановление пула потоков для второй партии запросов
        executor = Executors.newFixedThreadPool(10);

        // Вторая партия запросов
        for (int i = 0; i < 51; i++) {
            executor.submit(() -> {
                try {
                    ResultActions result = mockMvc.perform(get("/api/throttling/test"));
                    synchronized (resultsSecondBatch) {
                        resultsSecondBatch.add(result);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        // Проверяем вторую партию запросов
        for (int i = 0; i < resultsSecondBatch.size(); i++) {
            if (i < 50) {
                resultsSecondBatch.get(i).andExpect(status().isOk());
            } else {
                resultsSecondBatch.get(i).andExpect(status().is(502));
            }
        }
    }

    public void parallelTestThrottlingLimitWithMultipleIPs() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<ResultActions> results = new ArrayList<>();
        Map<String, Integer> ipRequestCount = new HashMap<>();
        int numberOfIPs = 5;  // Количество различных IP-адресов
        int requestsPerIP = 51;  // Количество запросов с каждого IP
        int totalRequests = numberOfIPs * requestsPerIP;

        for (int i = 0; i < totalRequests; i++) {
            final String ip = "192.168.0." + (i % numberOfIPs);  // Генерация IP-адресов
            synchronized (ipRequestCount) {
                ipRequestCount.put(ip, ipRequestCount.getOrDefault(ip, 0) + 1);
            }
            int requestNumber = ipRequestCount.get(ip);

            executor.submit(() -> {
                try {
                    ResultActions result = mockMvc.perform(
                            get("/api/throttling/test")
                                    .header("X-Forwarded-For", ip)
                    );
                    synchronized (results) {
                        results.add(result);
                    }
                    logger.info("IP: " + ip + " | Request number: " + requestNumber);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        // Группируем результаты по IP и проверяем их последовательно
        Map<String, List<ResultActions>> ipResults = new HashMap<>();

        for (int i = 0; i < results.size(); i++) {
            final String ip = "192.168.0." + (i % numberOfIPs);
            ipResults.computeIfAbsent(ip, k -> new ArrayList<>()).add(results.get(i));
        }

        for (Map.Entry<String, List<ResultActions>> entry : ipResults.entrySet()) {
            String ip = entry.getKey();
            List<ResultActions> ipResultList = entry.getValue();

            for (int i = 0; i < ipResultList.size(); i++) {
                if (i < 50) {
                    ipResultList.get(i).andExpect(status().isOk());
                } else {
                    ipResultList.get(i).andExpect(status().is(502));
                }
            }
        }
    }
}
