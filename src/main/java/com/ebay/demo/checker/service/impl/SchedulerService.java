package com.ebay.demo.checker.service.impl;

import com.ebay.demo.checker.model.Consts;
import com.ebay.demo.checker.model.auction.AuctionRequest;
import com.ebay.demo.checker.model.auction.AuctionRequestReponce;
import com.ebay.demo.checker.model.scheduler.SchedulerTask;
import com.ebay.demo.checker.model.scheduler.SchedulerTaskStatus;
import com.ebay.demo.checker.service.ISchedulerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.*;

@Slf4j
@Service
public class SchedulerService implements ISchedulerService {



    private ConcurrentLinkedQueue<SchedulerTask> tasks = new ConcurrentLinkedQueue<>();

    private ConcurrentHashMap<LocalDate, LocalDate> fullDates = new ConcurrentHashMap<>();

    private ConcurrentHashMap<LocalDateTime, LocalDateTime> overlappingTime = new ConcurrentHashMap<>();

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

    //this code resend failed tasks from the queue
    private void resendAuctions(){
        //Can use parallelStream
        int counter=0;
        while (counter<=10) {
            SchedulerTask schedulerTask =  tasks.poll();
            if(schedulerTask==null){
                break;
            }
            scheduleAuction(schedulerTask.getAuctionRequest().getItemId(), schedulerTask);
            counter++;
        }
    }

    // gives date that is not already full (looking to the next day)
    //and not in the night
    private LocalDateTime getSuitableDateTime(LocalDateTime dateTime){

        LocalDateTime localDateTime = overlappingTime.get(dateTime);
        if(localDateTime!=null){
            dateTime = dateTime.plusHours(Consts.TWO_HOURS).plusMinutes(1);
        }

        if(dateTime.getHour()>= Consts.END_HOUR || dateTime.getHour()< Consts.START_HOUR){
            dateTime = dateTime.plusDays(1);
            dateTime = getStartingHourOfScheduler(dateTime);
        }

        LocalDate localDate = dateTime.toLocalDate();
        while (localDate!=null){
            localDate = fullDates.get(dateTime.toLocalDate());
            if(localDate!=null){
                localDate = localDate.plusDays(1);
                dateTime = getStartingHourOfScheduler(dateTime.plusDays(1));
            }else {
                break;
            }
        }
        return dateTime;
    }

    private LocalDateTime getStartingHourOfScheduler(LocalDateTime dateTime) {
        dateTime = dateTime.withHour(8).withMinute(1);
        return dateTime;
    }

    @Override
    public void scheduleAuction(String itemId, SchedulerTask schedulerTask) {

        Runnable runnable = ()->{

            AuctionRequestReponce auctionRequestReponce = new AuctionRequestReponce();
            //if task is fired for first time

            AuctionRequest auctionRequest = null;

            LocalDateTime fromTime=null;
            LocalDateTime  toTime=null;
            if(schedulerTask==null){

                //starting from 8pm
                fromTime = getStartingHourOfScheduler(LocalDateTime.now());
                fromTime = getSuitableDateTime(fromTime);
                toTime = fromTime.plus(Consts.TWO_HOURS, ChronoUnit.HOURS);

                auctionRequest = new AuctionRequest();
                auctionRequest.setItemId(itemId);

            } else {

                auctionRequest =  schedulerTask.getAuctionRequest();

                fromTime = schedulerTask.getAuctionRequest().getFromTime();
                fromTime = getSuitableDateTime(fromTime);
                fromTime = fromTime.plus(Consts.TWO_HOURS, ChronoUnit.HOURS)
                        .plus(1, ChronoUnit.MINUTES);

                toTime = fromTime.plus(Consts.TWO_HOURS, ChronoUnit.HOURS);
            }

            auctionRequest.setFromTime(fromTime);
            auctionRequest.setToTime(toTime);
            auctionRequestReponce.setAuctionRequest(auctionRequest);

            auctionRequestReponce = auctionCreatorService.createSingleAuction(auctionRequestReponce);
            String error = auctionRequestReponce.getAuctionResponse().getError();

            //via error types we are mapping dates and time to be full (not usable for us).
            if(!StringUtils.isEmpty(error)){
                if(error.contains("DayFullAuctionException")){

                    LocalDate fromDate=  auctionRequestReponce.getAuctionRequest().getFromTime().toLocalDate();
                    fullDates.put(fromDate, fromDate);

                }
                else if(error.contains("WeekFullAuctionException")){

                    LocalDate fromDate=  auctionRequestReponce.getAuctionRequest().getFromTime().toLocalDate();
                    addFullWeek(fromDate);

                }else if(error.contains("OverlappingException")){

                    LocalDateTime overlappingFromTime=  auctionRequestReponce.getAuctionRequest().getFromTime();
                    overlappingTime.put(overlappingFromTime, overlappingFromTime);

                }

                SchedulerTask st = schedulerTask;
                if(st==null){
                    st=new SchedulerTask();
                }

                st.setSchedulerTaskStatus(SchedulerTaskStatus.REJECTED_TEMPORALLY);
                st.setAuctionRequest(auctionRequestReponce.getAuctionRequest());

                tasks.add(st);
            }
        };

        executorService.execute(runnable);
    }

    private void addFullWeek(LocalDate fromDate) {
        int day = Consts.DAYS_IN_WEEK - fromDate.getDayOfWeek().getValue();
        for (int i = 0; i <= day; i++) {
            LocalDate ld = fromDate.plusDays(i);
            fullDates.put(ld, ld);
        }
    }

    public static void main(String[] args) {
        LocalDate localDate = LocalDate.now();
        int day = 7 - localDate.getDayOfWeek().getValue();

        for(int i = 0; i <= day; i++){
            LocalDate ld = localDate.plusDays(i);
            log.info("{}", ld);
        }
    }
}
