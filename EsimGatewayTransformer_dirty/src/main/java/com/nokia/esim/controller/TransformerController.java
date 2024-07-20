package com.nokia.esim.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.nokia.esim.config.ErrorCodes;
import com.nokia.esim.dto.ChangeEsimProfileStatusRequestDto;
import com.nokia.esim.dto.ChangeEsimProfileStatusResponseDto;
import com.nokia.esim.dto.GetEsimProfileStatusRequestDto;
import com.nokia.esim.dto.GetEsimProfileStatusResponseDto;
import com.nokia.esim.dto.PrepareEsimProfileRequestDto;
import com.nokia.esim.dto.PrepareEsimProfileResponseDto;
import com.nokia.esim.dto.UnlinkAndReserveProfileRequestDto;
import com.nokia.esim.dto.UnlinkAndReserveProfileResponseDto;
import com.nokia.esim.exception.TransformerException;
import com.nokia.esim.service.CommonService;
import com.nokia.esim.service.CommonServiceImpl;
import com.nokia.esim.service.TransformerService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class TransformerController
{

    private final TransformerService transformerService;
    private final CommonService commonService;

    public TransformerController(TransformerService transformerService, CommonService commonService)
    {
        this.transformerService = transformerService;
        this.commonService = commonService;
    }

    @PostMapping(path = "/getEsimProfileStatus", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetEsimProfileStatusResponseDto> getEsimProfileStatus(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody GetEsimProfileStatusRequestDto esimProfileStatusRequestDto)
    {
        if (!commonService.verifyCredentials(authHeader))
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try
        {
            log.info("Request received for getEsimProfileStatus");
            GetEsimProfileStatusResponseDto response = transformerService
                    .getEsimProfileStatus(esimProfileStatusRequestDto);
            return ResponseEntity.status(HttpStatus.OK).headers(new HttpHeaders()).body(response);
        }
        catch (TransformerException e)
        {
            log.error(CommonServiceImpl.getStackTraceAsString(e));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new GetEsimProfileStatusResponseDto(null, null, null, e.getErrorCode(), e.getErrorMessage()));
        }
        catch (Exception e)
        {
            log.error(CommonServiceImpl.getStackTraceAsString(e));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GetEsimProfileStatusResponseDto(
                    null, null, null, ErrorCodes.GENERAL_ERROR.getCode(), ErrorCodes.GENERAL_ERROR.getMessage()));
        }
    }

    @PostMapping(path = "/prepareEsimProfile", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PrepareEsimProfileResponseDto> prepareEsimProfile(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody PrepareEsimProfileRequestDto prepareEsimProfileRequestDto)
    {
        if (!commonService.verifyCredentials(authHeader))
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try
        {
            log.info("Request received for prepareEsimProfile");
            PrepareEsimProfileResponseDto response = transformerService
                    .prepareEsimProfile(prepareEsimProfileRequestDto);
            return ResponseEntity.status(HttpStatus.OK).headers(new HttpHeaders()).body(response);
        }
        catch (TransformerException e)
        {
            log.error(CommonServiceImpl.getStackTraceAsString(e));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new PrepareEsimProfileResponseDto(null, null, null, e.getErrorCode(), e.getErrorMessage()));
        }
        catch (Exception e)
        {
            log.error(CommonServiceImpl.getStackTraceAsString(e));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new PrepareEsimProfileResponseDto(null,
                    null, null, ErrorCodes.GENERAL_ERROR.getCode(), ErrorCodes.GENERAL_ERROR.getMessage()));
        }
    }

    @PostMapping(path = "/changeEsimProfileStatus", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ChangeEsimProfileStatusResponseDto> changeEsimProfileStatus(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody ChangeEsimProfileStatusRequestDto changeEsimProfileStatusRequestDto)
    {
        if (!commonService.verifyCredentials(authHeader))
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try
        {
            log.info("Request received for changeEsimProfileStatus");
            ChangeEsimProfileStatusResponseDto response = transformerService
                    .changeEsimProfileStatus(changeEsimProfileStatusRequestDto);
            return ResponseEntity.status(HttpStatus.OK).headers(new HttpHeaders()).body(response);
        }
        catch (TransformerException e)
        {
            log.error(CommonServiceImpl.getStackTraceAsString(e));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ChangeEsimProfileStatusResponseDto(null, null, e.getErrorCode(), e.getErrorMessage()));
        }
        catch (Exception e)
        {
            log.error(CommonServiceImpl.getStackTraceAsString(e));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ChangeEsimProfileStatusResponseDto(
                    null, null, ErrorCodes.GENERAL_ERROR.getCode(), ErrorCodes.GENERAL_ERROR.getMessage()));
        }
    }

    @PostMapping(path = "/unlinkAndReserveProfile", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UnlinkAndReserveProfileResponseDto> unlinkAndReserveProfile(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody UnlinkAndReserveProfileRequestDto unlinkAndReserveProfileRequestDto)
    {
        if (!commonService.verifyCredentials(authHeader))
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try
        {
            log.info("Request received for unlinkAndReserveProfile");
            UnlinkAndReserveProfileResponseDto response = transformerService
                    .unlinkAndReserveProfile(unlinkAndReserveProfileRequestDto);
            return ResponseEntity.status(HttpStatus.OK).headers(new HttpHeaders()).body(response);
        }
        catch (TransformerException e)
        {
            log.error(CommonServiceImpl.getStackTraceAsString(e));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new UnlinkAndReserveProfileResponseDto(null, null, null, e.getErrorCode(), e.getErrorMessage()));
        }
        catch (Exception e)
        {
            log.error(CommonServiceImpl.getStackTraceAsString(e));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new UnlinkAndReserveProfileResponseDto(
                    null, null, null, ErrorCodes.GENERAL_ERROR.getCode(), ErrorCodes.GENERAL_ERROR.getMessage()));
        }
    }

}
