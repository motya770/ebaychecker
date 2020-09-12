package com.ebay.demo.checker.service;

import com.ebay.demo.checker.model.SchedulingTask;

public interface ISchedulerService {

   SchedulingTask scheduleAuction(String itemId);
}
