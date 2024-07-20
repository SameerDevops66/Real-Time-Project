package com.nokia.esim.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nokia.esim.service.CommonServiceImpl;
import com.nokia.esim.service.HealthCheckService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class HealthCheckController
{

//    @Value("${downloadOrder.response}")
//    private String downloadOrderResponse;
//    @Value("${confirmOrder.response}")
//    private String confirmOrderResponse;
//    @Value("${cancelOrder.response}")
//    private String cancelOrderResponse;
//
//    @Value("${downloadOrder.responseCode}")
//    private int downloadOrderResponseCode;
//    @Value("${confirmOrder.responseCode}")
//    private int confirmOrderResponseCode;
//    @Value("${cancelOrder.responseCode}")
//    private int cancelOrderResponseCode;

    private final HealthCheckService healthCheckService;

    public HealthCheckController(HealthCheckService healthCheckService)
    {
        this.healthCheckService = healthCheckService;
    }

    @GetMapping(path = "/healthCheck", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getHealthStatus()
    {
        try
        {
            log.info("Request received for HealthCheck");
            return ResponseEntity.status(HttpStatus.OK).body(healthCheckService.getHealthStatus().toString());
        }
        catch (Exception e)
        {
            log.error(CommonServiceImpl.getStackTraceAsString(e));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

//    @GetMapping(path = "/ui/optus/orderStatus", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<String> orderStatus()
//    {
//        try
//        {
//            log.info("Request received for orderStatus");
//            String obj = "{\r\n" + "\r\n" + "    \"order_id\": \"1234\",\r\n" + "\r\n"
//                    + "    \"order_status\": \"Failed\",\r\n" + "\r\n"
//                    + "    \"primary_msisdn\": \"+919535052428\",\r\n" + "\r\n" + "    \"request_id\": \"987997\",\r\n"
//                    + "\r\n" + "    \"response_code\": \"200\",\r\n" + "\r\n"
//                    + "    \"response_message\": \"Success\",\r\n" + "\r\n"
//                    + "    \"secondary_iccid\": \"1122330000000000004F\",\r\n" + "\r\n"
//                    + "    \"secondary_msisdn\": \"1122330000000000004F\"\r\n" + "\r\n" + "}";
//
//            return ResponseEntity.status(HttpStatus.OK).body(obj);
//        }
//        catch (Exception e)
//        {
//            log.error(CommonServiceImpl.getStackTraceAsString(e));
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
//
//    @PostMapping(path = "/downloadOrder", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<String> downloadOrder()
//    {
//        try
//        {
//            log.info("Request received for downloadOrder");
//
//            return ResponseEntity.status(downloadOrderResponseCode).body(downloadOrderResponse);
//        }
//        catch (Exception e)
//        {
//            log.error(CommonServiceImpl.getStackTraceAsString(e));
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
//
//    @PostMapping(path = "/confirmOrder", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<String> confirmOrder()
//    {
//        try
//        {
//            log.info("Request received for confirmOrder");
//
//            return ResponseEntity.status(confirmOrderResponseCode).body(confirmOrderResponse);
//        }
//        catch (Exception e)
//        {
//            log.error(CommonServiceImpl.getStackTraceAsString(e));
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
//
//    @PostMapping(path = "/cancelOrder", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<String> cancelOrder()
//    {
//        try
//        {
//            log.info("Request received for cancelOrder");
//
//            return ResponseEntity.status(cancelOrderResponseCode).body(cancelOrderResponse);
//        }
//        catch (Exception e)
//        {
//            log.error(CommonServiceImpl.getStackTraceAsString(e));
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }

}
