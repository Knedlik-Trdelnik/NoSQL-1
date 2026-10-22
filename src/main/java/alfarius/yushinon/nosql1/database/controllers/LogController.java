package alfarius.yushinon.nosql1.database.controllers;

import alfarius.yushinon.nosql1.database.repositories.LogRepository;
import alfarius.yushinon.nosql1.entity.mongo.Log;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class LogController {

    private final LogRepository logRepository;

    @GetMapping
    public ResponseEntity<List<Log>> getAllLogs() {
        List<Log> logs = logRepository.findAll();
        return ResponseEntity.ok(logs);
    }
}