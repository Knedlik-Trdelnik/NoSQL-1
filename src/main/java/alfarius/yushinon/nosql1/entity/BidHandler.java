package alfarius.yushinon.nosql1.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "bid_handler")
@Getter
@Setter
public class BidHandler {

    @EmbeddedId
    private BidHandlerId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("bidId")
    @JoinColumn(name = "bid_id")
    private Bid bid;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("applicantId")
    @JoinColumn(name = "applicant_id")
    private User applicant;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("inspectorId")
    @JoinColumn(name = "inspector_id")
    private User inspector;
}