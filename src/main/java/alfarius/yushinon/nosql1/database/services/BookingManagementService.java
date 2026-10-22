package alfarius.yushinon.nosql1.database.services;

import alfarius.yushinon.nosql1.database.repositories.BidRepository;
import alfarius.yushinon.nosql1.database.repositories.ProccessingRuleRepository;
import alfarius.yushinon.nosql1.database.repositories.UserRepository;
import alfarius.yushinon.nosql1.entity.Bid;
import alfarius.yushinon.nosql1.entity.User;
import alfarius.yushinon.nosql1.entity.mongo.Actions;
import alfarius.yushinon.nosql1.entity.mongo.CertainAction;
import alfarius.yushinon.nosql1.entity.mongo.ProcessingRule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BookingManagementService {

    private final BidRepository bidRepository;
    private final ProccessingRuleRepository ruleRepository;
    private final BookingService bookingService;
    private final UserRepository userRepository;
    private final LogService logService;

    @Transactional(transactionManager = "transactionManager")
    public void updateBidAndRule(String bidId, String newStatus, String newRuleName) {
        // надо тут обновить связанные сущности иначе будет ошибка в бД
        ProcessingRule rule = ruleRepository.findAll().stream().findFirst().orElse(new ProcessingRule());
        rule.setRuleName(newRuleName);
        ruleRepository.save(rule);

        // потом уже саму заявку обновляем
        Bid bid = bidRepository.findById(bidId).orElseThrow(() -> new RuntimeException("Заявка не найдена"));
        bid.setStatus(newStatus);
        bid.setProcessingRuleId(rule.getId());
        bid.setUpdatedAt(LocalDateTime.now());

        bidRepository.save(bid);


        Actions logAction = "APPROVED".equalsIgnoreCase(newStatus)
                ? Actions.BOOKING_REQUEST_APPROVED
                : Actions.BOOKING_REQUEST_UPDATED;

        User currentUser = userRepository.findByUsername(bookingService.getCurrentUsername());

        CertainAction action = new CertainAction();
        action.setUsername(bookingService.getCurrentUsername());
        action.setUserRole(currentUser != null ? currentUser.getRole() : null);
        action.setAction(logAction);
        logService.safeCertainLog(action);
    }
}