package alfarius.yushinon.nosql1.entity;

import jakarta.persistence.Embeddable;
import lombok.*;
import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class BidHandlerId implements Serializable {
    private Long bidId;
    private Long applicantId;
    private Long inspectorId;
}