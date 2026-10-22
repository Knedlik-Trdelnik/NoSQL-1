package alfarius.yushinon.nosql1.entity.mongo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "processing_rules")
public class ProcessingRule {
    @Id
    private String id;
    private String ruleName;
    private CertainAction description;
}