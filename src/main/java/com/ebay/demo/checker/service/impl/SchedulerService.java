package com.ebay.demo.checker.service.impl;

import com.ebay.demo.checker.model.AuctionRequest;
import com.ebay.demo.checker.model.AuctionRequestReponce;
import com.ebay.demo.checker.model.SchedulingTask;
import com.ebay.demo.checker.model.SchedulingTaskStatus;
import com.ebay.demo.checker.service.ISchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.*;

@Service
public class SchedulerService implements ISchedulerService {

    private ConcurrentHashMap<String, SchedulingTask> tasks = new ConcurrentHashMap<>();

    private ScheduledExecutorService  executorService = Executors.newScheduledThreadPool(5);

    @Autowired
    private AuctionCreatorService auctionCreatorService;

    @PostConstruct
    public void init(){
        startResend();
    }

    private void startResend(){
        Runnable resend  = ()->{
            resendAuctions();
        };
        executorService.scheduleWithFixedDelay(resend, 2_000, 10_000, TimeUnit.MICROSECONDS);
    }

    private void resendAuctions(){

    }

    @Override
    public void scheduleAuction(String itemId) {

        AuctionRequestReponce requestResponse = new AuctionRequestReponce();

        LocalDateTime fromTime = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime toTime = fromTime.plus(2, ChronoUnit.HOURS);

        AuctionRequest auctionRequest = new AuctionRequest();
        auctionRequest.setFromTime(fromTime);
        auctionRequest.setToTime(toTime);
        auctionRequest.setItemId(itemId);
        requestResponse.setAuctionRequest(auctionRequest);

        requestResponse = auctionCreatorService.createSingleAuction(requestResponse);
        if(requestResponse.getAuctionResponse()!=null){
            SchedulingTask schedulingTask = new SchedulingTask();
            schedulingTask.setSchedulingTaskStatus(SchedulingTaskStatus.REJECTED_TEMPORALLY);
            schedulingTask.setAuctionRequest(requestResponse.getAuctionRequest());

            tasks.put(itemId, schedulingTask);
        }
    }
}
