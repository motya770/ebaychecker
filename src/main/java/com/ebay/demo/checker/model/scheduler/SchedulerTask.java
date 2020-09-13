package com.ebay.demo.checker.model.scheduler;

import com.ebay.demo.checker.model.auction.AuctionRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(exclude = {"schedulerTaskStatus", "resendFromTime"})
@Data
public class SchedulerTask {
    private AuctionRequest auctionRequest;
    private SchedulerTaskStatus schedulerTaskStatus;

    private LocalDateTime resendFromTime;
}
