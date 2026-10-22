package alfarius.yushinon.nosql1.database.services;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final MongoTemplate mongoTemplate;

    public List<TagCountResult> getMostFrequentTags() {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.unwind("tags"),
                Aggregation.group("tags").count().as("count"),
                Aggregation.sort(Sort.Direction.DESC, "count"),
                Aggregation.limit(10)
        );

        AggregationResults<TagCountResult> results = mongoTemplate.aggregate(
                aggregation, "bids", TagCountResult.class
        );

        return results.getMappedResults();
    }

    public String explainQuery(String ruleId, java.time.LocalDateTime date) {
        org.bson.Document query = new org.bson.Document("processingRuleId", ruleId)
                .append("updatedAt", new org.bson.Document("$gte", date));

        // это план выполнения запроса
        org.bson.Document explainCommand = new org.bson.Document("explain",
                new org.bson.Document("find", "bids")
                        .append("filter", query)
        );

        org.bson.Document result = mongoTemplate.executeCommand(explainCommand);
        return result.toJson(); //а это статистика выполнения плана запроса выше
    }
}

@Data
class TagCountResult {
    private String id;
    private Long count;
}