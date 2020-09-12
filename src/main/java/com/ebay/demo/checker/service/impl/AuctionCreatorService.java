package com.ebay.demo.checker.service.impl;

import com.ebay.demo.checker.model.AuctionRequestBody;
import com.ebay.demo.checker.service.IAuctionCreatorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

@Slf4j
@Service
public class AuctionCreatorService implements IAuctionCreatorService {
    private WebClient client;

    @PostConstruct
    public void init(){
        client = WebClient
                .builder()
                .defaultCookie("cookieKey", "cookieValue")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public void startCreateAuctionsFromFile(String filePath) {
        //concurrent
        try (Stream<String> stream = Files.lines(Paths.get(filePath))) {
            stream.parallel().forEach(line -> {
                createSingleAuction(line);
            });
        }catch (Exception e){
            log.error("", e);
        }
    }

    @Override
    public void createSingleAuction(String itemUuId) {
        if(StringUtils.isEmpty(itemUuId)){
            throw new RuntimeException("Can't call for empty itemUuid.");
        }

        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plus(2, ChronoUnit.HOURS);

        AuctionRequestBody auctionRequestBody= new AuctionRequestBody();
        auctionRequestBody.setFromTime(startTime);
        auctionRequestBody.setToTime(endTime);
        auctionRequestBody.setItemId(itemUuId);

        Mono<String> response = client.post().uri("localhost:8080/auction/set-action?")
                .body(auctionRequestBody, AuctionRequestBody.class).retrieve().bodyToMono(String.class);
        response.subscribe(str->{
            log.info("response {}", str);
        });
    }
}
