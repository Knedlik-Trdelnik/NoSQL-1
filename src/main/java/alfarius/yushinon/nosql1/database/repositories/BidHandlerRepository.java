package alfarius.yushinon.nosql1.database.repositories;

import alfarius.yushinon.nosql1.entity.BidHandler;
import alfarius.yushinon.nosql1.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BidHandlerRepository extends JpaRepository<BidHandler, Long> {
    List<BidHandler> findByApplicant(User applicant);
}