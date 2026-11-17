package alfarius.yushinon.nosql1.entity.cassandra;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("api_errors_by_component")
public class ApiErrorByComponent {

    @PrimaryKeyColumn(name = "component", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String component;

    @PrimaryKeyColumn(name = "hour_bucket", ordinal = 1, type = PrimaryKeyType.PARTITIONED)
    private String hourBucket;

    @PrimaryKeyColumn(name = "event_id", ordinal = 2, type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
    @CassandraType(type = CassandraType.Name.TIMEUUID)
    private UUID eventId;

    @Column("event_time")
    private Instant eventTime;

    @Column("method_name")
    private String methodName;

    @Column("http_method")
    private String httpMethod;

    @Column("endpoint")
    private String endpoint;

    @Column("status_code")
    private int statusCode;

    @Column("error_message")
    private String errorMessage;

    @Column("username")
    private String username;

    @Column("client_ip")
    private String clientIp;

    @Column("duration_ms")
    private long durationMs;
}