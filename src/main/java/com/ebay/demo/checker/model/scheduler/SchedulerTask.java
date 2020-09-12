package com.ebay.demo.checker.model.scheduler;

import com.ebay.demo.checker.model.auction.AuctionRequest;
import lombok.Data;

@Data
public class SchedulerTask {
    private AuctionRequest auctionRequest;
    private SchedulerTaskStatus schedulerTaskStatus;
}
