package alfarius.yushinon.nosql1.database.repositories;

import alfarius.yushinon.nosql1.entity.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassroomRepository extends JpaRepository<Classroom, Long> {
    Classroom findById(long id);
    Classroom findByName(String name);
}
