package alfarius.yushinon.nosql1.dto;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "Classrom")
@Data
public class ClassroomDTO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "name",nullable = false)
    private String name;

    @OneToMany
    private List<BidDTO> bids;
}
