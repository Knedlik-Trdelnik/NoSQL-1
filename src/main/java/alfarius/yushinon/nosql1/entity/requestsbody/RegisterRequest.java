package alfarius.yushinon.nosql1.entity.requestsbody;

import alfarius.yushinon.nosql1.entity.Role;
import lombok.Data;
import lombok.ToString;
import org.springframework.security.core.parameters.P;

@Data
@ToString(exclude = "password")
public class RegisterRequest {

    private String login;
    private String password;
    private Role role;

}
