package com.acme.salary.scheduler;

import com.acme.salary.entity.AvailableLeaves;
import com.acme.salary.entity.LeavesMonthlyRefill;
import com.acme.salary.enums.LeaveType;
import com.acme.salary.repository.AvailableLeavesRepository;
import com.acme.salary.repository.LeavesMonthlyRefillRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class LeaveRefillScheduler {

    private final LeavesMonthlyRefillRepository leavesMonthlyRefillRepository;
    private final AvailableLeavesRepository availableLeavesRepository;

    public LeaveRefillScheduler(LeavesMonthlyRefillRepository leavesMonthlyRefillRepository,
                                AvailableLeavesRepository availableLeavesRepository) {
        this.leavesMonthlyRefillRepository = leavesMonthlyRefillRepository;
        this.availableLeavesRepository = availableLeavesRepository;
    }

    // This runs at 00:00:00 on the 1st of every month.
    @Scheduled(cron = "0 0 0 1 * ?")
    @Transactional
    public void refillLeavesEveryMonthStart() {
        log.info("Executing monthly leave refill job...");

        for (LeaveType leaveType : LeaveType.values()) {
            Optional<LeavesMonthlyRefill> refillConfigOpt = leavesMonthlyRefillRepository.findById(leaveType);

            if (refillConfigOpt.isPresent()) {
                int refillValue = refillConfigOpt.get().getRefillValue();
                List<AvailableLeaves> availableLeavesList = availableLeavesRepository.findAllByLeaveType(leaveType);

                for (AvailableLeaves availableLeaves : availableLeavesList) {
                    availableLeaves.setAvailableLeaves(availableLeaves.getAvailableLeaves() + refillValue);
                }

                availableLeavesRepository.saveAll(availableLeavesList);
                log.info("Refilled {} leaves by {} units for {} employees.", leaveType, refillValue, availableLeavesList.size());
            }
        }

        log.info("Monthly leave refill completed successfully.");
    }
}