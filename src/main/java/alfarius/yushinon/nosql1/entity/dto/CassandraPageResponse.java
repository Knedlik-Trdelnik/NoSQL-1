package alfarius.yushinon.nosql1.dto.cassandra;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CassandraPageResponse<T> {
    private List<T> items;
    private String nextPagingState;
    private boolean hasNext;
    private int pageSize;
    private String consistencyLevel;
    private long elapsedMs;
}