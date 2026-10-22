package alfarius.yushinon.nosql1.entity;

import alfarius.yushinon.nosql1.entity.mongo.ClassroomEmbedded;
import alfarius.yushinon.nosql1.entity.mongo.UserEmbedded;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "bids")
@CompoundIndexes({
        @CompoundIndex(name = "rule_date_idx", def = "{'processingRuleId': 1, 'updatedAt': -1}")
})
public class Bid {
    @Id
    private String id;
    private String name;
    private String description;
    private LocalDateTime timeToLife;

    private ClassroomEmbedded classroom;
    private List<TimeWindow> timeWindows;

    private UserEmbedded applicant;
    private UserEmbedded inspector;
    private String status;

    private List<String> tags;
    private LocalDateTime updatedAt;
    private String processingRuleId;
}