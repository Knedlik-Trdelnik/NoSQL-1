package alfarius.yushinon.nosql1.entity.requestsbody;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class RegisterResponce {
    private boolean wasSuccessful;
    private String message;
}
