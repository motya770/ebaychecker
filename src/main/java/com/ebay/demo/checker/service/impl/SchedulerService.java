package com.ebay.demo.checker.service.impl;

import com.ebay.demo.checker.model.auction.AuctionRequest;
import com.ebay.demo.checker.model.auction.AuctionRequestReponce;
import com.ebay.demo.checker.model.scheduler.SchedulerTask;
import com.ebay.demo.checker.model.scheduler.SchedulerTaskStatus;
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

    private ConcurrentHashMap<String, SchedulerTask> tasks = new ConcurrentHashMap<>();

    private ConcurrentHashMap<LocalDate, LocalDate> fullDates = new ConcurrentHashMap<>();

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
        if(requestResponse.getAuctionResponse().getError() != null){

            if(requestResponse.getAuctionResponse().getError().contains("DayFullAuctionException")){
                LocalDate fromDate=  requestResponse.getAuctionRequest().getFromTime().toLocalDate();
                fullDates.put(fromDate, fromDate);
            }
            else if(requestResponse.getAuctionResponse().getError().contains("WeekFullAuctionException")){
                LocalDate fromDate=  requestResponse.getAuctionRequest().getFromTime().toLocalDate();
                //TODO add all day of the week
                fullDates.put(fromDate, fromDate);
            }

            SchedulerTask schedulerTask = new SchedulerTask();
            schedulerTask.setSchedulerTaskStatus(SchedulerTaskStatus.REJECTED_TEMPORALLY);
            schedulerTask.setAuctionRequest(requestResponse.getAuctionRequest());

            tasks.put(itemId, schedulerTask);
        }
    }
}
