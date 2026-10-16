package alfarius.yushinon.nosql1.entity.mongo;


import alfarius.yushinon.nosql1.entity.Role;
import alfarius.yushinon.nosql1.entity.User;
import lombok.Data;

@Data
public class CertainAction {
    private String username;

    private Role userRole;

    private Actions action;

}
