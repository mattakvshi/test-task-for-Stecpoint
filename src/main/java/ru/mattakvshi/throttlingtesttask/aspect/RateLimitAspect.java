package ru.mattakvshi.throttlingtesttask.aspect;

import javax.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mattakvshi.throttlingtesttask.helpers.RateLimited;
import ru.mattakvshi.throttlingtesttask.helpers.RateLimiter;
import ru.mattakvshi.throttlingtesttask.helpers.TooManyRequestsException;

@Aspect
@Component
public class RateLimitAspect {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private RateLimiter rateLimiter;

    @Before("@within(rateLimited) || @annotation(rateLimited)")
    public void rateLimited(JoinPoint joinPoint, RateLimited rateLimited) throws TooManyRequestsException {
        if (!rateLimiter.isAllowed(request)) {
            throw new TooManyRequestsException("Too many requests!");
        }
    }

}
