package com.ebay.demo.checker.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuctionRequestBody {
    LocalDateTime fromTime;
    LocalDateTime toTime;
    String itemId;
}
