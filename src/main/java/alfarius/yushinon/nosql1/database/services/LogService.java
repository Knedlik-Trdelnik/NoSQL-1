package alfarius.yushinon.nosql1.database.services;


import alfarius.yushinon.nosql1.database.repositories.LogRepository;
import alfarius.yushinon.nosql1.entity.mongo.CertainAction;
import alfarius.yushinon.nosql1.entity.mongo.Log;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LogService {

    private final LogRepository logRepository;
    private final MongoTemplate mongoTemplate;

    public Log getOrCreateLog(LocalDate date) {
        return logRepository.findById(date)
                .orElseGet(() -> {
                    Log log = new Log();
                    log.setDate(date);
                    log.setCertainLog(new ArrayList<>());
                    return logRepository.save(log);
                });
    }

    public void safeCertainLog(CertainAction action) {
        Log log = getOrCreateLog(LocalDate.now());
        log.addAction(action);
        logRepository.save(log);
    }

    public Optional<Log> getLogByDate(LocalDate date) {
        return logRepository.findById(date);
    }
}
