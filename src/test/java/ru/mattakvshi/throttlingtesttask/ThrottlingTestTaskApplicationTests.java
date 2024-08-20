package ru.mattakvshi.throttlingtesttask;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ThrottlingTestTaskApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testThrottlingLimit() throws Exception {
        // 51 запросов, чтобы превысить лимит
        for (int i = 1; i <= 50; i++) {
            mockMvc.perform(get("/api/throttling/test"))
                    .andExpect(status().isOk());

        }

        mockMvc.perform(get("/api/throttling/test"))
                .andExpect(status().is(502));
    }


//    @Test
//    public void parallelTestThrottlingLimit() throws Exception {
//        ExecutorService executor = Executors.newFixedThreadPool(10);
//        List<ResultActions> results = new ArrayList<>();
//
//        for (int i = 0; i < 51; i++) {
//            executor.submit(() -> {
//                try {
//                    ResultActions result = mockMvc.perform(get("/api/throttling/test"));
//                    synchronized (results) {
//                        results.add(result);
//                    }
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }
//            });
//        }
//
//        executor.shutdown();
//        executor.awaitTermination(10, TimeUnit.SECONDS);
//
//        for (int i = 0; i < results.size(); i++) {
//            if (i < 50) {
//                results.get(i).andExpect(status().isOk());
//            } else {
//                results.get(i).andExpect(status().is(502));
//            }
//        }
//    }

}
