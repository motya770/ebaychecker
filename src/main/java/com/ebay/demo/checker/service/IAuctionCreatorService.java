package com.ebay.demo.checker.service;

public interface IAuctionCreatorService {
    void startCreateAuctionsFromFile(String filePath);

    void createSingleAuction(String itemUuId);
}
