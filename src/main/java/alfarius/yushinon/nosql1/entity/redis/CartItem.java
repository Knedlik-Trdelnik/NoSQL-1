package alfarius.yushinon.nosql1.entity.redis;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {

    private Long serviceId;

    private Long classroomId;

    private Long timeWindowId;

    private LocalTime timeStart;

    private LocalTime timeEnd;
}