package com.ebay.demo.checker.service.impl;

import com.ebay.demo.checker.model.AuctionRequestReponce;
import com.ebay.demo.checker.model.SchedulingTask;
import com.ebay.demo.checker.model.SchedulingTaskStatus;
import com.ebay.demo.checker.service.ISchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SchedulerService implements ISchedulerService {

    private ConcurrentHashMap<String, SchedulingTask> tasks = new ConcurrentHashMap<>();

    @Autowired
    private AuctionCreatorService auctionCreatorService;

    @Override
    public void scheduleAuction(String itemId) {

        AuctionRequestReponce auctionRequestReponce = auctionCreatorService.createSingleAuction(itemId);
        if(auctionRequestReponce!=null){
            SchedulingTask schedulingTask = new SchedulingTask();
            schedulingTask.setSchedulingTaskStatus(SchedulingTaskStatus.REJECTED_TEMPORALLY);
            schedulingTask.setAuctionRequest(auctionRequestReponce.getAuctionRequest());

            tasks.put(itemId, schedulingTask);
        }
    }
}
