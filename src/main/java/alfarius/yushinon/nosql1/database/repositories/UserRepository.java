package alfarius.yushinon.nosql1.database.repositories;

import alfarius.yushinon.nosql1.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    User deleteUserByUsername(String username);
    User findUserById(Long id);
}