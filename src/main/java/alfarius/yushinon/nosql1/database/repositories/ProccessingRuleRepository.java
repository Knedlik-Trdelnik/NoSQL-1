package alfarius.yushinon.nosql1.database.repositories;

import alfarius.yushinon.nosql1.entity.mongo.ProcessingRule;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProccessingRuleRepository extends MongoRepository<ProcessingRule, String> {
}
