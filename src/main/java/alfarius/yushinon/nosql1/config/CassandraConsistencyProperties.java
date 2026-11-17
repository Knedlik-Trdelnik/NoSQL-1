package alfarius.yushinon.nosql1.config;

import com.datastax.oss.driver.api.core.DefaultConsistencyLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.cassandra.consistency")
public class CassandraConsistencyProperties {

    /**
     * Уровень согласованности для записи (по варианту — QUORUM)
     */
    private DefaultConsistencyLevel write = DefaultConsistencyLevel.QUORUM;

    /**
     * Уровень согласованности для чтения (по варианту — ONE)
     */
    private DefaultConsistencyLevel read = DefaultConsistencyLevel.ONE;
}