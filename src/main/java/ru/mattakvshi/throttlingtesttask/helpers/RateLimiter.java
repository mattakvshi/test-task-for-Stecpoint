package ru.mattakvshi.throttlingtesttask.helpers;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class RateLimiter {

    private static final Logger logger = LoggerFactory.getLogger(RateLimiter.class);

    private final ConcurrentHashMap<String, Queue<Long>> requestTimes = new ConcurrentHashMap<>();

    private final int MAX_REQUESTS_PER_INTERVAL = 50;
    private final long TIME_INTERVAL = 60000L;

    public boolean isAllowed(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For"); //Чтобы локально с эмулировать обращение с разных IP (смотреть тесты)
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        long  now = System.currentTimeMillis();

        Queue<Long> times = requestTimes.computeIfAbsent(ip, q -> new ConcurrentLinkedQueue<>());

        logger.info(requestTimes.toString());

        synchronized (times) {
            while (!times.isEmpty() && now - times.peek() > TIME_INTERVAL) {
                times.poll();
            }

            times.add(now);

            return times.size() <= MAX_REQUESTS_PER_INTERVAL;
        }
    }
}
