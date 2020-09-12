package com.ebay.demo.checker.model.auction;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AuctionRequest {
   private LocalDateTime fromTime;
   private LocalDateTime toTime;
   private String itemId;
}
