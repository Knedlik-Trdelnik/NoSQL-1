package alfarius.yushinon.nosql1.database.repositories;

import alfarius.yushinon.nosql1.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByLogin(String username);

    boolean existsByLogin(String username);

}