package com.ebay.demo.checker.service.impl;

import com.ebay.demo.checker.model.auction.AuctionRequest;
import com.ebay.demo.checker.model.auction.AuctionRequestReponce;
import com.ebay.demo.checker.model.auction.AuctionResponse;
import com.ebay.demo.checker.service.IAuctionCreatorService;
import com.ebay.demo.checker.service.ISchedulerService;
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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class AuctionCreatorService implements IAuctionCreatorService {

    public static final String EBAY_SERVERS = "ebayServers";

    private WebClient client;

    private RestTemplate restTemplate;

    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private ISchedulerService schedulerService;

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

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int counter=0;
            while ((line = br.readLine()) != null) {
                log.info("counter {}", counter++);
                schedulerService.scheduleAuction(line);
            }
        }catch (Exception e){
            log.error("{}", e);
        }

//        try (Stream<String> stream = Files.lines(file.toPath())) {
//            stream.forEach(line -> {
//
//            });
//        }catch (Exception e){
//            log.error("", e);
//        }
    }

    private ConcurrentHashMap<String, String> ebayServers = new ConcurrentHashMap<>();

    @Override
    public AuctionRequestReponce createSingleAuction(AuctionRequestReponce auctionRequestReponce) {

        AuctionRequest auctionRequest = auctionRequestReponce.getAuctionRequest();
        AuctionResponse auctionResponse = new AuctionResponse();
        try {
            if (StringUtils.isEmpty(auctionRequestReponce.getAuctionRequest().getItemId())) {
                throw new RuntimeException("Can't call for empty itemUuid.");
            }

            if(auctionRequest.getFromTime()==null || auctionRequest.getToTime()==null){
                throw new RuntimeException("Can't call for empty time frame.");
            }

            String ebayServer = ebayServers.get(EBAY_SERVERS);
            if(ebayServer==null) {
                List<ServiceInstance> instances =  discoveryClient.getInstances("ebay-auction-service");
                ServiceInstance instance = instances.get(0);//TODO add round-robin?
                ebayServers.put(EBAY_SERVERS, "http://" + instance.getHost() + ":" +  instance.getPort());
            }

            log.info("Trying to schedule {}", auctionRequest);
            //TODO think about https
            String response = restTemplate
                    .postForObject(   ebayServer+
                                    "/auction/set-auction?fromTime="
                                    + auctionRequest.getFromTime() + "&toTime=" + auctionRequest.getToTime().toString() + "&itemId=" + auctionRequest.getItemId(),
                            null, String.class);

            auctionResponse.setMessage(response);
            log.info("response {}", response);
        }catch (Exception e){
            auctionResponse.setError(e.getMessage());
            log.error("", e);
        }

        auctionRequestReponce.setAuctionResponse(auctionResponse);

        return auctionRequestReponce;

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
