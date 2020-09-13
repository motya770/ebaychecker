package com.ebay.demo.checker.model.scheduler;

import com.ebay.demo.checker.model.auction.AuctionRequest;
import lombok.Data;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.time.LocalDateTime;

//@EqualsAndHashCode(exclude = {"schedulerTaskStatus", "resendFromTime"})
@Data
public class SchedulerTask {
    private AuctionRequest auctionRequest;
    private SchedulerTaskStatus schedulerTaskStatus;
    private LocalDateTime resendFromTime;

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31). // two randomly chosen prime numbers
                // if deriving: appendSuper(super.hashCode()).
                        append(auctionRequest.getFromTime()).
                        append(auctionRequest.getToTime()).
                        append(auctionRequest.getItemId()).
                        toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SchedulerTask))
            return false;
        if (obj == this)
            return true;

        SchedulerTask rhs = (SchedulerTask) obj;
        return new EqualsBuilder().
                // if deriving: appendSuper(super.equals(obj)).
                        append(auctionRequest.getFromTime(), rhs.getAuctionRequest().getFromTime()).
                        append(auctionRequest.getToTime(), rhs.getAuctionRequest().getToTime()).
                        append(auctionRequest.getItemId(), rhs.getAuctionRequest().getItemId()).
                        isEquals();
    }
}
