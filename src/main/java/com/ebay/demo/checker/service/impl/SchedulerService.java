package com.ebay.demo.checker.service.impl;

import com.ebay.demo.checker.model.SchedulingTask;
import com.ebay.demo.checker.service.ISchedulerService;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class SchedulerService implements ISchedulerService {

    private ConcurrentHashMap<String, SchedulingTask> tasks = new ConcurrentHashMap<>();

    @Override
    public SchedulingTask scheduleAuction(String itemId) {
        return null;
    }
}
