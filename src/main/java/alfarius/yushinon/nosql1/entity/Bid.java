package alfarius.yushinon.nosql1.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "bids")
@Getter
@Setter
public class Bid {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "time_to_life")
    private LocalDateTime timeToLife;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id", nullable = false)
    private Classroom classroom;

    @ManyToMany
    @JoinTable(
            name = "bid_time_window",
            joinColumns = @JoinColumn(name = "bid_id"),
            inverseJoinColumns = @JoinColumn(name = "time_window_id")
    )
    private List<TimeWindow> timeWindows;

    // хуета с фронта
    @Transient
    private Long serviceId;

    @Transient
    private Long timeWindowId;

    @Transient
    private LocalTime timeStart;

    @Transient
    private LocalTime timeEnd;
}