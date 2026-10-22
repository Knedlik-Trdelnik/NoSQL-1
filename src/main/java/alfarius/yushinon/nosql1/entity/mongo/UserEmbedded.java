package alfarius.yushinon.nosql1.entity.mongo;

import lombok.Data;

@Data
public class UserEmbedded {
    private Long id;
    private String username;
}