package ru.mattakvshi.throttlingtesttask.helpers;

import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimiter {
    private final ConcurrentHashMap<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> requestTimes = new ConcurrentHashMap<>();

    private final int MAX_REQUESTS_PER_INTERVAL = 50;
    private final long TIME_INTERVAL = 60000L;

    public boolean isAllowed(HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        long now = System.currentTimeMillis();

        System.out.println(now);

        requestTimes.entrySet().removeIf(entry -> now - entry.getValue() > TIME_INTERVAL);

        System.out.println(requestTimes);

        requestTimes.keySet().forEach(ipAddress -> {
            if (!requestTimes.containsKey(ipAddress)) {
                requestCounts.remove(ipAddress);
            }
        });

        requestCounts.putIfAbsent(ip, new AtomicInteger(0));
        requestTimes.put(ip, now);

        System.out.println(requestTimes);

        int currentCount = requestCounts.get(ip).incrementAndGet();

        System.out.println(currentCount);

        return currentCount <= MAX_REQUESTS_PER_INTERVAL;
    }
}
