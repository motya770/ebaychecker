package com.ebay.demo.checker.service;

import com.ebay.demo.checker.model.AuctionRequest;
import com.ebay.demo.checker.model.AuctionRequestReponce;

public interface IAuctionCreatorService {
    void startCreateAuctionsFromFile(String filePath);

    AuctionRequestReponce createSingleAuction(AuctionRequestReponce auctionRequestReponce);
}
