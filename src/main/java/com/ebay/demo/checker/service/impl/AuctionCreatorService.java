package com.ebay.demo.checker.service.impl;

import com.ebay.demo.checker.model.AuctionRequest;
import com.ebay.demo.checker.model.AuctionRequestReponce;
import com.ebay.demo.checker.model.AuctionResponse;
import com.ebay.demo.checker.service.IAuctionCreatorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import javax.annotation.PostConstruct;
import java.io.File;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
public class AuctionCreatorService implements IAuctionCreatorService {
    private WebClient client;

    RestTemplate restTemplate;

    @Autowired
    private DiscoveryClient discoveryClient;

    @PostConstruct
    public void init(){
        client = WebClient
                .builder()
                .defaultCookie("cookieKey", "cookieValue")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        restTemplate = new RestTemplate();
    }

    @Override
    public void startCreateAuctionsFromFile(String filePath) {
        File file = null;
        try {
            file = ResourceUtils.getFile(
                    filePath);
        }catch (Exception e){
            log.error("", e);
        }
        //concurrent
        try (Stream<String> stream = Files.lines(file.toPath())) {
            stream.forEach(line -> {
                log.info("Working on {}", line);
                createSingleAuction(line);
            });
        }catch (Exception e){
            log.error("", e);
        }
    }

    @Override
    public AuctionRequestReponce createSingleAuction(String itemUuId) {

        AuctionRequestReponce requestReponce = new AuctionRequestReponce();



        LocalDateTime fromTime = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime toTime = fromTime.plus(2, ChronoUnit.HOURS);

        AuctionRequest auctionRequest = new AuctionRequest();
        auctionRequest.setFromTime(fromTime);
        auctionRequest.setToTime(toTime);
        auctionRequest.setItemId(itemUuId);
        requestReponce.setAuctionRequest(auctionRequest);

        try {
            if (StringUtils.isEmpty(itemUuId)) {
                throw new RuntimeException("Can't call for empty itemUuid.");
            }

            List<ServiceInstance> instances =  discoveryClient.getInstances("ebay-auction-service");
            ServiceInstance instance = instances.get(0);//TODO add roundrobin?

            //TODO think about https
            String response = restTemplate
                    .postForObject(  "http://" + instance.getHost() + ":" +  instance.getPort() +
                                    "/auction/set-auction?fromTime="
                                    + fromTime.toString() + "&toTime=" + toTime.toString() + "&itemId=" + itemUuId,
                            null, String.class);

            AuctionResponse auctionResponse = new AuctionResponse();
            auctionResponse.setMessage(response);

            requestReponce.setAuctionResponse(auctionResponse);
            log.info("response {}", response);
        }catch (Exception e){
            log.error("", e);
        }

        return requestReponce;

//        Mono<String> response = client.post()
//                .uri("localhost:8080/auction/set-action")
//                .accept(MediaType.APPLICATION_JSON)
//                .body(Mono.just(auctionRequestBody), AuctionRequestBody.class)
//                .retrieve()
//                .onStatus(HttpStatus::is4xxClientError, r1 -> {
//                    r1.bodyToMono(String.class).subscribe(s->{
//                        log.error(s);
//                    });
//                    return Mono.error(new RuntimeException("Error"));
//                })
//                .onStatus(HttpStatus::isError, r2 -> {
//                    r2.bodyToMono(String.class).subscribe(s->{
//                        log.error(s);
//                    });
//                    return Mono.error(new RuntimeException("Error"));
//                })
//                .bodyToMono(String.class);
//        response.subscribe(str->{
//            log.info("response {}", str);
//        });
    }
}
