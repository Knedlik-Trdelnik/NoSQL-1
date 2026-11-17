package alfarius.yushinon.nosql1.database.repositories.cassandra;

import alfarius.yushinon.nosql1.entity.cassandra.ApiLogByEndpoint;
import org.springframework.data.cassandra.repository.MapIdCassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiLogByEndpointRepository extends MapIdCassandraRepository<ApiLogByEndpoint> {
}