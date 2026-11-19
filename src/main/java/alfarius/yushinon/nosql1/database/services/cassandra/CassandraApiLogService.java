package alfarius.yushinon.nosql1.database.services.cassandra;

import alfarius.yushinon.nosql1.config.CassandraConsistencyProperties;
import alfarius.yushinon.nosql1.dto.cassandra.ApiLogEvent;
import alfarius.yushinon.nosql1.dto.cassandra.CassandraPageResponse;
import alfarius.yushinon.nosql1.entity.cassandra.ApiErrorByComponent;
import alfarius.yushinon.nosql1.entity.cassandra.ApiLogByEndpoint;
import alfarius.yushinon.nosql1.entity.cassandra.ApiLogByHour;
import alfarius.yushinon.nosql1.entity.dto.HourBucketUtil;
import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.DefaultConsistencyLevel;
import com.datastax.oss.driver.api.core.uuid.Uuids;
import lombok.RequiredArgsConstructor;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.InsertOptions;
import org.springframework.data.cassandra.core.cql.QueryOptions;
import org.springframework.data.cassandra.core.query.CassandraPageRequest;
import org.springframework.data.cassandra.core.query.Criteria;
import org.springframework.data.cassandra.core.query.Query;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ExecutionInfo;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.metadata.Node;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CassandraApiLogService {

    private final CassandraTemplate cassandraTemplate;
    private final CassandraConsistencyProperties consistencyProperties;

    public UUID saveApiEvent(ApiLogEvent event) {
        return saveApiEvent(event, consistencyProperties.getWrite());
    }

    public UUID saveApiEvent(ApiLogEvent event, ConsistencyLevel writeConsistency) {
        Instant eventTime = event.getEventTime() != null ? event.getEventTime() : Instant.now();
        String hourBucket = HourBucketUtil.fromInstant(eventTime);
        UUID eventId = Uuids.timeBased();

        InsertOptions insertOptions = InsertOptions.builder()
                .consistencyLevel(writeConsistency)
                .build();

        ApiLogByHour logByHour = ApiLogByHour.builder()
                .hourBucket(hourBucket)
                .eventId(eventId)
                .eventTime(eventTime)
                .component(event.getComponent())
                .methodName(event.getMethodName())
                .httpMethod(event.getHttpMethod())
                .endpoint(event.getEndpoint())
                .statusCode(event.getStatusCode())
                .isError(event.isError())
                .errorMessage(event.getErrorMessage())
                .username(event.getUsername())
                .clientIp(event.getClientIp())
                .durationMs(event.getDurationMs())
                .build();
        cassandraTemplate.insert(logByHour, insertOptions);

        ApiLogByEndpoint logByEndpoint = ApiLogByEndpoint.builder()
                .endpoint(event.getEndpoint())
                .hourBucket(hourBucket)
                .eventId(eventId)
                .eventTime(eventTime)
                .component(event.getComponent())
                .methodName(event.getMethodName())
                .httpMethod(event.getHttpMethod())
                .statusCode(event.getStatusCode())
                .isError(event.isError())
                .errorMessage(event.getErrorMessage())
                .username(event.getUsername())
                .clientIp(event.getClientIp())
                .durationMs(event.getDurationMs())
                .build();
        cassandraTemplate.insert(logByEndpoint, insertOptions);

        if (event.isError()) {
            ApiErrorByComponent errorByComponent = ApiErrorByComponent.builder()
                    .component(event.getComponent())
                    .hourBucket(hourBucket)
                    .eventId(eventId)
                    .eventTime(eventTime)
                    .methodName(event.getMethodName())
                    .httpMethod(event.getHttpMethod())
                    .endpoint(event.getEndpoint())
                    .statusCode(event.getStatusCode())
                    .errorMessage(event.getErrorMessage())
                    .username(event.getUsername())
                    .clientIp(event.getClientIp())
                    .durationMs(event.getDurationMs())
                    .build();
            cassandraTemplate.insert(errorByComponent, insertOptions);
        }

        return eventId;
    }

    /**
     * Обязательный запрос варианта №22222: последние ошибки заданного компонента.
     */
    public CassandraPageResponse<ApiErrorByComponent> getErrorsByComponent(
            String component,
            String hourBucket,
            int pageSize,
            String pagingState,
            DefaultConsistencyLevel readConsistency) {

        String bucket = resolveBucket(hourBucket);
        ConsistencyLevel cl = resolveReadConsistency(readConsistency);
        Pageable pageable = buildPageRequest(pageSize, pagingState);

        Query query = Query.query(Criteria.where("component").is(component))
                .and(Criteria.where("hour_bucket").is(bucket))
                .pageRequest(pageable)
                .queryOptions(QueryOptions.builder().consistencyLevel(cl).build());

        long start = System.nanoTime();
        Slice<ApiErrorByComponent> slice = cassandraTemplate.slice(query, ApiErrorByComponent.class);
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        return toPageResponse(slice, pageSize, cl, elapsedMs);
    }

    /**
     * Запрос 2: все обращения к REST API за указанный часовой бакет.
     */
    public CassandraPageResponse<ApiLogByHour> getLogsByHour(
            String hourBucket,
            int pageSize,
            String pagingState,
            DefaultConsistencyLevel readConsistency) {

        String bucket = resolveBucket(hourBucket);
        ConsistencyLevel cl = resolveReadConsistency(readConsistency);
        Pageable pageable = buildPageRequest(pageSize, pagingState);

        Query query = Query.query(Criteria.where("hour_bucket").is(bucket))
                .pageRequest(pageable)
                .queryOptions(QueryOptions.builder().consistencyLevel(cl).build());

        long start = System.nanoTime();
        Slice<ApiLogByHour> slice = cassandraTemplate.slice(query, ApiLogByHour.class);
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        return toPageResponse(slice, pageSize, cl, elapsedMs);
    }

    /**
     * Запрос 3: обращения к конкретному REST-эндпоинту за указанный часовой бакет.
     */
    public CassandraPageResponse<ApiLogByEndpoint> getLogsByEndpoint(
            String endpoint,
            String hourBucket,
            int pageSize,
            String pagingState,
            DefaultConsistencyLevel readConsistency) {

        String bucket = resolveBucket(hourBucket);
        ConsistencyLevel cl = resolveReadConsistency(readConsistency);
        Pageable pageable = buildPageRequest(pageSize, pagingState);

        Query query = Query.query(Criteria.where("endpoint").is(endpoint))
                .and(Criteria.where("hour_bucket").is(bucket))
                .pageRequest(pageable)
                .queryOptions(QueryOptions.builder().consistencyLevel(cl).build());

        long start = System.nanoTime();
        Slice<ApiLogByEndpoint> slice = cassandraTemplate.slice(query, ApiLogByEndpoint.class);
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        return toPageResponse(slice, pageSize, cl, elapsedMs);
    }

    private String resolveBucket(String hourBucket) {
        return (hourBucket == null || hourBucket.isBlank())
                ? HourBucketUtil.currentHourBucket()
                : hourBucket.trim();
    }

    private ConsistencyLevel resolveReadConsistency(DefaultConsistencyLevel override) {
        return override != null ? override : consistencyProperties.getRead();
    }

    private Pageable buildPageRequest(int pageSize, String pagingState) {
        int safeSize = Math.max(1, Math.min(pageSize, 200));
        if (pagingState == null || pagingState.isBlank()) {
            return CassandraPageRequest.first(safeSize);
        }
        byte[] decoded = Base64.getUrlDecoder().decode(pagingState);
        return CassandraPageRequest.of(PageRequest.of(0, safeSize), ByteBuffer.wrap(decoded));
    }

    private <T> CassandraPageResponse<T> toPageResponse(
            Slice<T> slice, int pageSize, ConsistencyLevel cl, long elapsedMs) {

        String nextToken = null;
        if (slice.hasNext() && slice.nextPageable() instanceof CassandraPageRequest nextReq && nextReq.getPagingState() != null) {
            ByteBuffer bb = nextReq.getPagingState().duplicate();
            byte[] bytes = new byte[bb.remaining()];
            bb.get(bytes);
            nextToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        }

        return CassandraPageResponse.<T>builder()
                .items(slice.getContent())
                .nextPagingState(nextToken)
                .hasNext(slice.hasNext())
                .pageSize(pageSize)
                .consistencyLevel(cl.name())
                .elapsedMs(elapsedMs)
                .build();
    }

    private final CqlSession cqlSession;

    public List<Map<String, Object>> getClusterNodesInfo() {
        return cqlSession.getMetadata().getNodes().values().stream()
                .sorted(Comparator.comparing(n -> n.getEndPoint().toString()))
                .map(node -> {
                    Map<String, Object> info = new LinkedHashMap<>();
                    info.put("hostId", node.getHostId() != null ? node.getHostId().toString() : "unknown");
                    info.put("endPoint", node.getEndPoint().toString());
                    info.put("broadcastRpcAddress", node.getBroadcastRpcAddress().map(Object::toString).orElse("n/a"));
                    info.put("datacenter", node.getDatacenter());
                    info.put("rack", node.getRack());
                    info.put("state", node.getState().name());
                    info.put("cassandraVersion", node.getCassandraVersion() != null ? node.getCassandraVersion().toString() : "unknown");
                    info.put("openConnections", node.getOpenConnections());
                    info.put("upSinceMillis", node.getUpSinceMillis());
                    return info;
                })
                .toList();
    }

    public Map<String, Object> probeReadConsistency(DefaultConsistencyLevel consistency, String hourBucket) {
        ConsistencyLevel cl = consistency != null ? consistency : consistencyProperties.getRead();
        String bucket = resolveBucket(hourBucket);
        String keyspace = cqlSession.getKeyspace()
                .map(id -> id.asCql(true))
                .orElse("nosql_lab3");

        SimpleStatement stmt = SimpleStatement.builder(
                        "SELECT hour_bucket, event_id FROM " + keyspace + ".api_logs_by_hour WHERE hour_bucket = ? LIMIT 1")
                .addPositionalValue(bucket)
                .setConsistencyLevel(cl)
                .build();

        long start = System.nanoTime();
        ResultSet rs = cqlSession.execute(stmt);
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        ExecutionInfo ei = rs.getExecutionInfo();
        Node coordinator = ei.getCoordinator();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("operation", "READ_PROBE");
        result.put("consistencyLevel", cl.name());
        result.put("coordinator", coordinator != null ? coordinator.getEndPoint().toString() : "unknown");
        result.put("coordinatorHostId", coordinator != null && coordinator.getHostId() != null ? coordinator.getHostId().toString() : "unknown");
        result.put("responseSizeBytes", ei.getResponseSizeInBytes());
        result.put("speculativeExecutions", ei.getSpeculativeExecutionCount());
        result.put("schemaInAgreement", ei.isSchemaInAgreement());
        result.put("rowsFound", rs.getAvailableWithoutFetching());
        result.put("elapsedMs", elapsedMs);
        return result;
    }
}