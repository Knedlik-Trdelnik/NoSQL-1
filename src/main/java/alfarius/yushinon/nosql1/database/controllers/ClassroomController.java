package alfarius.yushinon.nosql1.database.controllers;


import alfarius.yushinon.nosql1.database.services.ClassroomService;
import alfarius.yushinon.nosql1.entity.Classroom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/services")
public class ClassroomController {

    @Autowired
    ClassroomService classroomService;

    @GetMapping("/classrooms")
    @Cacheable(value = "classrooms_cache")
    public ResponseEntity<List<Classroom>> getClassrooms() {
        // TODO: добавить кеширование обосанный аудиторий ФТМИ
        return ResponseEntity.ok(classroomService.getAllClassrooms());
    }
}
