package alfarius.yushinon.nosql1.database.services;

import alfarius.yushinon.nosql1.database.repositories.ClassroomRepository;
import alfarius.yushinon.nosql1.entity.Classroom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClassroomService {

    @Autowired
    private ClassroomRepository classroomRepository;

    public Classroom getClassroomByName(String name) {
        return classroomRepository.findByName(name);
    }

    public List<Classroom> getAllClassrooms() {
        return classroomRepository.findAll();
    }
}
