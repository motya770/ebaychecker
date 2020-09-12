package com.ebay.demo.checker.model;

import lombok.Data;

@Data
public class SchedulingTask {
    private AuctionRequestBody auctionRequestBody;
    private SchedulingTaskStatus schedulingTaskStatus;
}
