package alfarius.yushinon.nosql1.database.repositories;

import alfarius.yushinon.nosql1.entity.User;
import org.hibernate.annotations.processing.SQL;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Integer> {

    public User findByUsername(@Param("username") String username);
}