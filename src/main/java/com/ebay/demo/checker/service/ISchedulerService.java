package com.ebay.demo.checker.service;

import com.ebay.demo.checker.model.scheduler.SchedulerTask;

public interface ISchedulerService {

   void scheduleAuction(String itemId);

   void retryToScheduleAuction(SchedulerTask schedulerTask);
}
