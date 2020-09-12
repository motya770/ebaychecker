package com.ebay.demo.checker.service;

import com.ebay.demo.checker.model.auction.AuctionRequestReponce;

public interface IAuctionCreatorService {
    void startCreateAuctionsFromFile(String filePath);

    AuctionRequestReponce createSingleAuction(AuctionRequestReponce auctionRequestReponce);
}
