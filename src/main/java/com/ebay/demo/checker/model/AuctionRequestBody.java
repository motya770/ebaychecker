package com.ebay.demo.checker.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AuctionRequestBody {
   private LocalDateTime fromTime;
   private LocalDateTime toTime;
   private String itemId;
}
