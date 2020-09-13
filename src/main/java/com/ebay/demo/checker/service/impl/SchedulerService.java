package com.ebay.demo.checker.service.impl;

import com.ebay.demo.checker.model.auction.AuctionRequest;
import com.ebay.demo.checker.model.auction.AuctionRequestReponce;
import com.ebay.demo.checker.model.scheduler.SchedulerTask;
import com.ebay.demo.checker.model.scheduler.SchedulerTaskStatus;
import com.ebay.demo.checker.service.ISchedulerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.*;

@Slf4j
@Service
public class SchedulerService implements ISchedulerService {

    private ConcurrentHashMap<SchedulerTask, String> tasks = new ConcurrentHashMap<>();

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
        executorService.scheduleWithFixedDelay(resend, 2_000, 30_000, TimeUnit.MICROSECONDS);
    }

    private void resendAuctions(){
        tasks.entrySet()
           .parallelStream()
                .forEach(entry -> {
                    log.info("Resending on...." + entry.getKey() + ":" + entry.getValue());

                });
    }

    // gives date that is not already full (looking to the next day)
    private LocalDateTime getSuitableNextTime(LocalDateTime dateTime){

        if(dateTime.getHour()>=23 || dateTime.getHour()<8){
            dateTime = dateTime.plusDays(1);
        }

        LocalDate localDate = dateTime.toLocalDate();
        while (localDate!=null){
            localDate = fullDates.get(dateTime.toLocalDate());
            if(localDate!=null){
                dateTime = dateTime.plusDays(1);
            }
        }
        return dateTime;
    }

    @Override
    public void scheduleAuction(String itemId, SchedulerTask schedulerTask) {

        Runnable runnable = ()->{

            AuctionRequestReponce auctionRequestReponce = null;
            //if task is fired for first time
            if(schedulerTask==null){
                auctionRequestReponce = new AuctionRequestReponce();

                //starting from 8pm
                LocalDateTime fromTime =LocalDateTime.of(LocalDate.now(), LocalTime.MIN)
                        .plus(Duration.of(8, ChronoUnit.HOURS)).plus(Duration.of(1, ChronoUnit.MINUTES));
                fromTime = getSuitableNextTime(fromTime);
                LocalDateTime toTime = fromTime.plus(2, ChronoUnit.HOURS);

                AuctionRequest auctionRequest = new AuctionRequest();
                auctionRequest.setFromTime(fromTime);
                auctionRequest.setToTime(toTime);
                auctionRequest.setItemId(itemId);
                auctionRequestReponce.setAuctionRequest(auctionRequest);

            }else {

                AuctionRequest auctionRequest =  schedulerTask.getAuctionRequest();

                LocalDateTime fromTime = schedulerTask.getAuctionRequest().getFromTime();
                fromTime = fromTime.plus(2, ChronoUnit.HOURS)
                        .plus(1, ChronoUnit.MINUTES);
                fromTime = getSuitableNextTime(fromTime);
                LocalDateTime toTime = fromTime.plus(2, ChronoUnit.HOURS);

                auctionRequest.setFromTime(fromTime);
                auctionRequest.setToTime(toTime);
            }

            auctionRequestReponce = auctionCreatorService.createSingleAuction(auctionRequestReponce);
            if(auctionRequestReponce.getAuctionResponse().getError() != null){

                if(auctionRequestReponce.getAuctionResponse().getError().contains("DayFullAuctionException")){
                    LocalDate fromDate=  auctionRequestReponce.getAuctionRequest().getFromTime().toLocalDate();
                    fullDates.put(fromDate, fromDate);
                }
                else if(auctionRequestReponce.getAuctionResponse().getError().contains("WeekFullAuctionException")){
                    LocalDate fromDate=  auctionRequestReponce.getAuctionRequest().getFromTime().toLocalDate();
                    //TODO add all day of the week
                    fullDates.put(fromDate, fromDate);
                }

                schedulerTask.setSchedulerTaskStatus(SchedulerTaskStatus.REJECTED_TEMPORALLY);
                schedulerTask.setAuctionRequest(auctionRequestReponce.getAuctionRequest());

                tasks.put(schedulerTask, itemId);
            }else {
                tasks.remove(schedulerTask);
            }
        };

        executorService.execute(runnable);
    }
}
