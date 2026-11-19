package alfarius.yushinon.nosql1.aspect;

import alfarius.yushinon.nosql1.database.services.cassandra.CassandraApiLogService;
import alfarius.yushinon.nosql1.dto.cassandra.ApiLogEvent;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@Slf4j
@Aspect
@Order(1)
@Component
@RequiredArgsConstructor
public class RestApiLoggingAspect {

    private final CassandraApiLogService cassandraApiLogService;

    @Pointcut("@within(org.springframework.web.bind.annotation.RestController) " +
            "&& within(alfarius.yushinon.nosql1..*) " +
            "&& !within(alfarius.yushinon.nosql1.database.controllers.CassandraApiLogController)")
    public void restApiMethods() {
    }

    @Around("restApiMethods()")
    public Object logRestApiCall(ProceedingJoinPoint joinPoint) throws Throwable {
        long startNs = System.nanoTime();
        String component = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        HttpServletRequest request = extractRequest();
        String httpMethod = request != null ? request.getMethod() : "INTERNAL";
        String endpoint = request != null ? request.getRequestURI() : "/" + component + "/" + methodName;
        String clientIp = request != null ? request.getRemoteAddr() : "unknown";
        String username = extractUsername();

        int statusCode = 200;
        boolean isError = false;
        String errorMessage = null;

        try {
            Object result = joinPoint.proceed();
            if (result instanceof ResponseEntity<?> responseEntity) {
                statusCode = responseEntity.getStatusCode().value();
                if (statusCode >= 400) {
                    isError = true;
                    errorMessage = "HTTP " + statusCode + (responseEntity.getBody() != null
                            ? ": " + responseEntity.getBody()
                            : "");
                }
            }
            return result;
        } catch (Throwable ex) {
            isError = true;
            statusCode = (ex instanceof ResponseStatusException rse)
                    ? rse.getStatusCode().value()
                    : 500;
            errorMessage = ex.getClass().getSimpleName() + ": " + ex.getMessage();
            throw ex;
        } finally {
            long durationMs = (System.nanoTime() - startNs) / 1_000_000;
            ApiLogEvent event = ApiLogEvent.builder()
                    .component(component)
                    .methodName(methodName)
                    .httpMethod(httpMethod)
                    .endpoint(endpoint)
                    .statusCode(statusCode)
                    .error(isError)
                    .errorMessage(errorMessage)
                    .username(username)
                    .clientIp(clientIp)
                    .durationMs(durationMs)
                    .eventTime(Instant.now())
                    .build();

            try {
                cassandraApiLogService.saveApiEvent(event);
            } catch (Exception cassandraEx) {
                log.error("Не удалось записать REST API событие в Cassandra ({}): {}",
                        component, cassandraEx.getMessage());
            }
        }
    }

    private HttpServletRequest extractRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
            return attrs.getRequest();
        }
        return null;
    }

    private String extractUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "anonymous";
        }
        return auth.getName();
    }
}