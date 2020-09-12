package com.ebay.demo.checker;

import com.ebay.demo.checker.service.IAuctionCreatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CheckerApplication implements CommandLineRunner {

    @Autowired
    private IAuctionCreatorService auctionCreatorService;

    @Override
    public void run(String... args) throws Exception {
        auctionCreatorService.startCreateAuctionsFromFile("classpath:home_assignment_uuids.txt");
    }

    public static void main(String[] args) {
        SpringApplication.run(CheckerApplication.class, args);
    }

}
