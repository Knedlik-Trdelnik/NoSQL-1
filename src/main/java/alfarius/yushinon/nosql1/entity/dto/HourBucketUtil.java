package alfarius.yushinon.nosql1.entity.dto;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public final class HourBucketUtil {

    private static final DateTimeFormatter HOUR_BUCKET_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH").withZone(ZoneOffset.UTC);

    private HourBucketUtil() {
    }

    public static String fromInstant(Instant instant) {
        return HOUR_BUCKET_FORMATTER.format(instant);
    }

    public static String currentHourBucket() {
        return fromInstant(Instant.now());
    }
}