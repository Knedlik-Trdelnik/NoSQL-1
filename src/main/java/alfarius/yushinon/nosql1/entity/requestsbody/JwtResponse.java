package alfarius.yushinon.nosql1.entity.requestsbody;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JwtResponse {

    private final String type = "Bearer";
    private String accesToken;
}
