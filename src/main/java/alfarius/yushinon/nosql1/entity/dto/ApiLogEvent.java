package alfarius.yushinon.nosql1.dto.cassandra;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ApiLogEvent {
    private String component;
    private String methodName;
    private String httpMethod;
    private String endpoint;
    private int statusCode;
    private boolean isError;
    private String errorMessage;
    private String username;
    private String clientIp;
    private long durationMs;
    private Instant eventTime;
}