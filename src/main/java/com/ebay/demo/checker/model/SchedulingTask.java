package com.ebay.demo.checker.model;

import lombok.Data;

@Data
public class SchedulingTask {
    private AuctionRequest auctionRequest;
    private SchedulingTaskStatus schedulingTaskStatus;
}