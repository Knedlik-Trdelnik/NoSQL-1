package alfarius.yushinon.nosql1.database.controllers;

import alfarius.yushinon.nosql1.config.CassandraConsistencyProperties;
import alfarius.yushinon.nosql1.database.services.cassandra.CassandraApiLogService;
import alfarius.yushinon.nosql1.dto.cassandra.ApiLogEvent;
import alfarius.yushinon.nosql1.dto.cassandra.CassandraPageResponse;
import alfarius.yushinon.nosql1.entity.cassandra.ApiErrorByComponent;
import alfarius.yushinon.nosql1.entity.cassandra.ApiLogByEndpoint;
import alfarius.yushinon.nosql1.entity.cassandra.ApiLogByHour;
import com.datastax.oss.driver.api.core.DefaultConsistencyLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/cassandra/logs")
@RequiredArgsConstructor
public class CassandraApiLogController {

    private final CassandraApiLogService logService;
    private final CassandraConsistencyProperties consistencyProperties;

    /**
     * Обязательный запрос варианта: последние ошибки заданного компонента.
     * Пример: GET /api/cassandra/logs/errors/AuthController?pageSize=10
     */
    @GetMapping("/errors/{component}")
    public ResponseEntity<CassandraPageResponse<ApiErrorByComponent>> getComponentErrors(
            @PathVariable String component,
            @RequestParam(required = false) String hourBucket,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String pagingState,
            @RequestParam(required = false) DefaultConsistencyLevel consistency) {

        return ResponseEntity.ok(
                logService.getErrorsByComponent(component, hourBucket, pageSize, pagingState, consistency)
        );
    }

    /**
     * Запрос 2: все обращения к REST API за час.
     * Пример: GET /api/cassandra/logs?pageSize=20
     */
    @GetMapping
    public ResponseEntity<CassandraPageResponse<ApiLogByHour>> getLogsByHour(
            @RequestParam(required = false) String hourBucket,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String pagingState,
            @RequestParam(required = false) DefaultConsistencyLevel consistency) {

        return ResponseEntity.ok(
                logService.getLogsByHour(hourBucket, pageSize, pagingState, consistency)
        );
    }

    /**
     * Запрос 3: обращения к конкретному эндпоинту за час.
     * Пример: GET /api/cassandra/logs/by-endpoint?endpoint=/api/auth/login&pageSize=10
     */
    @GetMapping("/by-endpoint")
    public ResponseEntity<CassandraPageResponse<ApiLogByEndpoint>> getLogsByEndpoint(
            @RequestParam String endpoint,
            @RequestParam(required = false) String hourBucket,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String pagingState,
            @RequestParam(required = false) DefaultConsistencyLevel consistency) {

        return ResponseEntity.ok(
                logService.getLogsByEndpoint(endpoint, hourBucket, pageSize, pagingState, consistency)
        );
    }

    /**
     * Эндпоинт для ручной записи / проверки уровней согласованности и нагрузки.
     */
    @PostMapping("/manual")
    public ResponseEntity<Map<String, Object>> writeManualEvent(
            @RequestBody ApiLogEvent event,
            @RequestParam(required = false) DefaultConsistencyLevel consistency) {

        DefaultConsistencyLevel cl = consistency != null ? consistency : consistencyProperties.getWrite();
        if (event.getEventTime() == null) {
            event.setEventTime(Instant.now());
        }
        long start = System.nanoTime();
        UUID eventId = logService.saveApiEvent(event, cl);
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        return ResponseEntity.ok(Map.of(
                "eventId", eventId.toString(),
                "consistencyLevel", cl.name(),
                "elapsedMs", elapsedMs
        ));
    }
}