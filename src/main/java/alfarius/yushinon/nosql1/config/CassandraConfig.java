package alfarius.yushinon.nosql1.config;

import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.config.DriverConfigLoaderBuilderConfigurer;
import org.springframework.data.cassandra.config.SchemaAction;
import org.springframework.data.cassandra.core.cql.keyspace.CreateKeyspaceSpecification;
import org.springframework.data.cassandra.core.cql.keyspace.DataCenterReplication;
import org.springframework.data.cassandra.core.cql.keyspace.KeyspaceOption;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

import java.time.Duration;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(CassandraConsistencyProperties.class)
@EnableCassandraRepositories(basePackages = "alfarius.yushinon.nosql1.database.repositories.cassandra")
public class CassandraConfig extends AbstractCassandraConfiguration {

    private final CassandraConsistencyProperties consistencyProperties;

    @Value("${spring.cassandra.contact-points:127.0.0.1:9042,127.0.0.1:9043,127.0.0.1:9044}")
    private String contactPoints;

    @Value("${spring.cassandra.port:9042}")
    private int port;

    @Value("${spring.cassandra.local-datacenter:datacenter1}")
    private String localDatacenter;

    @Value("${spring.cassandra.keyspace-name:nosql_lab3}")
    private String keyspaceName;

    @Value("${spring.cassandra.schema-action:CREATE_IF_NOT_EXISTS}")
    private SchemaAction schemaAction;

    @Value("${app.cassandra.replication-factor:3}")
    private int replicationFactor;

    @Override
    protected String getKeyspaceName() {
        return keyspaceName;
    }

    @Override
    protected String getContactPoints() {
        return contactPoints;
    }

    @Override
    protected int getPort() {
        return port;
    }

    @Override
    protected String getLocalDataCenter() {
        return localDatacenter;
    }

    @Override
    public SchemaAction getSchemaAction() {
        return schemaAction;
    }

    @Override
    public String[] getEntityBasePackages() {
        return new String[]{"alfarius.yushinon.nosql1.entity.cassandra"};
    }

    @Override
    protected List<CreateKeyspaceSpecification> getKeyspaceCreations() {
        CreateKeyspaceSpecification spec = CreateKeyspaceSpecification.createKeyspace(keyspaceName)
                .ifNotExists()
                .with(KeyspaceOption.DURABLE_WRITES, true)
                .withNetworkReplication(DataCenterReplication.of(localDatacenter, replicationFactor));
        return List.of(spec);
    }

    @Override
    protected DriverConfigLoaderBuilderConfigurer getDriverConfigLoaderBuilderConfigurer() {
        return builder -> {
            builder
                    .withDuration(DefaultDriverOption.REQUEST_TIMEOUT, Duration.ofSeconds(10))
                    .withDuration(DefaultDriverOption.CONNECTION_CONNECT_TIMEOUT, Duration.ofSeconds(10))
                    .withDuration(DefaultDriverOption.CONNECTION_INIT_QUERY_TIMEOUT, Duration.ofSeconds(10))
                    .withDuration(DefaultDriverOption.CONTROL_CONNECTION_TIMEOUT, Duration.ofSeconds(10))
                    .withDuration(DefaultDriverOption.METADATA_SCHEMA_REQUEST_TIMEOUT, Duration.ofSeconds(15))
                    .withString(DefaultDriverOption.REQUEST_CONSISTENCY, consistencyProperties.getWrite().name())
                    .startProfile("write_quorum")
                    .withString(DefaultDriverOption.REQUEST_CONSISTENCY, consistencyProperties.getWrite().name())
                    .endProfile()
                    .startProfile("read_one")
                    .withString(DefaultDriverOption.REQUEST_CONSISTENCY, consistencyProperties.getRead().name())
                    .endProfile();
        };
    }
}