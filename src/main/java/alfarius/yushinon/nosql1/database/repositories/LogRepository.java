package alfarius.yushinon.nosql1.database.repositories;

import alfarius.yushinon.nosql1.entity.mongo.Log;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;

public interface LogRepository extends MongoRepository<Log, LocalDate> {

}
