package alfarius.yushinon.nosql1.database.repositories;

import alfarius.yushinon.nosql1.entity.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {

}