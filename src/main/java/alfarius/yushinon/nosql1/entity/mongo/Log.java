package alfarius.yushinon.nosql1.entity.mongo;


import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "logs")
public class Log {
    @Indexed
    @Id
    private LocalDate date;

    private List<CertainAction> certainLog;

    public void addAction(CertainAction action) {
        if (certainLog == null) {
            certainLog = new ArrayList<>();
        }
        certainLog.add(action);
    }
}
