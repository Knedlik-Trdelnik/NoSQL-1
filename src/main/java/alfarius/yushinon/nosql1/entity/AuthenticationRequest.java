package alfarius.yushinon.nosql1.entity;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(exclude = "password")
public class AuthenticationRequest {

    private String login;
    private String password;
}
