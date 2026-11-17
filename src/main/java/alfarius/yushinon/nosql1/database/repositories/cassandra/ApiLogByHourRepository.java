package alfarius.yushinon.nosql1.database.repositories.cassandra;

import alfarius.yushinon.nosql1.entity.cassandra.ApiLogByHour;
import org.springframework.data.cassandra.repository.MapIdCassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiLogByHourRepository extends MapIdCassandraRepository<ApiLogByHour> {
}