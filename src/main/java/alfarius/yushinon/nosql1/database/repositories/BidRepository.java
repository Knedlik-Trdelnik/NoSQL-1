package alfarius.yushinon.nosql1.database.repositories;

import alfarius.yushinon.nosql1.entity.Bid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BidRepository extends MongoRepository<Bid, String> {

    Page<Bid> findByClassroomNameContainingIgnoreCase(String classroomName, Pageable pageable);

    @Query("{ 'status': ?0, 'applicant.username': { $regex: ?1, $options: 'i' } }")
    Page<Bid> findByStatusAndApplicantUsername(String status, String username, Pageable pageable);
}