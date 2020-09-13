package com.ebay.demo.checker.model.auction;

import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

//@EqualsAndHashCode
@ToString
@Data
public class AuctionRequest {
   private LocalDateTime fromTime;
   private LocalDateTime toTime;
   private String itemId;
}
