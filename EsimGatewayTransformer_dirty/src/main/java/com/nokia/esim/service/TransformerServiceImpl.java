package com.nokia.esim.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

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

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TransformerServiceImpl implements TransformerService
{

    private static final String EID = "eid";
    private static final String ICCID = "iccid";
    private static final String MATCHING_ID = "matchingId";
    private static final String REASON_CODE = "reasonCode";
    private static final String SUBJECT_CODE = "subjectCode";
    private static final String HEADER = "header";
    private static final String MESSAGE = "message";
    private static final String MESSAGE_TENANT_ID = "TenantId is :: ";
    private static final String MESSAGE_POST_REQUEST_FAILED = "sendPostRequest failed for URL:{}";
    private static final String FUNCTION_CALL_IDENTIFIER = "functionCallIdentifier";
    private static final String FUNCTION_CALL_IDENTIFIER_VALUE = "Optus";
    private static final String FUNCTION_REQUEST_IDENTIFIER = "functionRequesterIdentifier";
    private static final String FUNCTION_REQUEST_IDENTIFIER_VALUE = "1.3.6.1.4.1.19914";
    private static final String AUTHORIZATION = "Authorization";

    private static HashMap<String, HashMap<String, ErrorCodes>> smdpErrorCodes;

    private final CommonService commonService;

    @Value("${es.getPrimaryInfoByIccid.url}")
    private String getPrimaryInfoByIccidUrl;

    @Value("${es.getProfileStatusInfoByPrimaryIccid.url}")
    private String getProfileStatusInfoByPrimaryIccidUrl;

    @Value("${es.getProfileStatus.secondaryIccid.url}")
    private String secondaryIccidUrl;

    @Value("${smdp.downloadOrder.url}")
    private String downloadOrderUrl;

    @Value("${smdp.confirmOrder.url}")
    private String confirmOrderUrl;

    @Value("${smdp.cancelOrder.url}")
    private String cancelOrderUrl;

    @Value("${es.addOrUpdatePrimaryInfo.url}")
    private String addOrUpdatePrimaryInfoUrl;

    @Value("${smds.address}")
    private String smdsAddress;

    @Value("${es.auth.username}")
    private String esUsername;

    @Value("${es.auth.password}")
    private String esPassword;

    @Value("${es.getSceProperty.url}")
    private String getScePropertyUrl;

    @Value("${es.sceProperty.tenantId}")
    private String tenantIdSceProperty;

    @Value("${es.profileType}")
    private String profileTypeValue;
    
    @Value("${es.profileTypeRequired}")
    private boolean profileTypeRequired;
    
    public TransformerServiceImpl(CommonService commonService)
    {
        this.commonService = commonService;
        TransformerServiceImpl.prepareSmdpErrorCodesMap();
    }

    @Override
    public GetEsimProfileStatusResponseDto getEsimProfileStatus(
            GetEsimProfileStatusRequestDto esimProfileStatusRequestDto)
    {
        log.info(esimProfileStatusRequestDto + "Validating GetEsimProfileStatus Request");
        commonService.validateGetEsimProfileStatusRequest(esimProfileStatusRequestDto);
        JSONObject response = null;

        String finalUrl = getProfileStatusInfoByPrimaryIccidUrl + esimProfileStatusRequestDto.getIccid();

        try
        {
            HttpHeaders headers = new HttpHeaders();
            headers.add(AUTHORIZATION, commonService.generateBasicAuthHeader(esUsername, esPassword));

            log.info(esimProfileStatusRequestDto
                    + "Sending GET request to ES for GetProfileStatusInfoByPrimaryIccid URL :: {}", finalUrl);
            response = new JSONObject(commonService.sendGetRequest(finalUrl, headers, String.class).getBody());
        }
        catch (HttpClientErrorException | HttpServerErrorException e)
        {
            log.info(esimProfileStatusRequestDto
                    + "sendGetRequest failed for GetProfileStatusInfoByPrimaryIccid with URL {} , status {} , headers {} , response {}",
                    finalUrl, e.getStatusCode(), e.getResponseHeaders(), e.getResponseBodyAsString());
            log.error(esimProfileStatusRequestDto + CommonServiceImpl.getStackTraceAsString(e));
            throw new TransformerException(ErrorCodes.UPSTREAM_ERROR.getCode(), ErrorCodes.UPSTREAM_ERROR.getMessage(),
                    e.getMessage());
        }
        catch (Exception e)
        {
            log.error(
                    esimProfileStatusRequestDto + "sendGetRequest failed for GetProfileStatusInfoByPrimaryIccid URL:{}",
                    finalUrl);
            log.error(esimProfileStatusRequestDto + CommonServiceImpl.getStackTraceAsString(e));
            throw new TransformerException(ErrorCodes.UPSTREAM_TIMEOUT.getCode(),
                    ErrorCodes.UPSTREAM_TIMEOUT.getMessage(), e.getMessage());
        }

        if (!response.has("eSimProfileStatus") || null == response.getString("eSimProfileStatus")
                || response.getString("eSimProfileStatus").isEmpty())
        {
        	GetEsimProfileStatusResponseDto notFoundResponse = new GetEsimProfileStatusResponseDto(
        			esimProfileStatusRequestDto.getRequestId(), 
        			esimProfileStatusRequestDto.getIccid(), 
        			"", 
        			ErrorCodes.NO_STATUS_RETURNED.getCode(),
                    ErrorCodes.NO_STATUS_RETURNED.getMessage());
			/*
			 * throw new TransformerException(ErrorCodes.NO_STATUS_RETURNED.getCode(),
			 * ErrorCodes.NO_STATUS_RETURNED.getMessage(),
			 * "EsimProfileStatus is NULL/EMPTY in response of GetProfileStatusInfoByPrimaryIccid call"
			 * );
			 */
        	log.info(esimProfileStatusRequestDto + "Final Response :: {}", notFoundResponse);
        	return notFoundResponse;
        }

        GetEsimProfileStatusResponseDto finalResponse = prepareGetEsimProfileStatusResponse(
                esimProfileStatusRequestDto.getRequestId(), esimProfileStatusRequestDto.getIccid(),
                response.getString("eSimProfileStatus"));

        log.info(esimProfileStatusRequestDto + "Final Response :: {}", finalResponse);
        return finalResponse;
    }

    @Override
    public PrepareEsimProfileResponseDto prepareEsimProfile(PrepareEsimProfileRequestDto prepareEsimProfileRequestDto)
    {
        log.info(prepareEsimProfileRequestDto + "Validating PrepareEsimProfile Request");
        commonService.validatePrepareEsimProfileRequest(prepareEsimProfileRequestDto);

        return prepareEsimProfile(prepareEsimProfileRequestDto, prepareEsimProfileRequestDto.getIccId(),
                prepareEsimProfileRequestDto.getEID(), prepareEsimProfileRequestDto.getRequestId(),
                prepareEsimProfileRequestDto.isReleaseFlag(), prepareEsimProfileRequestDto.getMsisdn());
    }

    public PrepareEsimProfileResponseDto prepareEsimProfile(Object req, String iccid, String eid, String requestId,
            boolean releaseFlag, String msisdn)
    {
        String iccId = null;
        String errorCode = ErrorCodes.SUCCESS.getCode();
        String errorMessage = ErrorCodes.SUCCESS.getMessage();
        String esimProfileStatus = "Available";
        String tenantId = StringUtils.EMPTY;

        // DOWNLOAD ORDER REQUEST
        log.info(req + "Sending Download Order request");
        ResponseEntity<String> downloadOrderResponse = null;

        try
        {
            downloadOrderResponse = sendDownloadOrderRequest(iccid, eid);
            JSONObject downloadJson = new JSONObject(downloadOrderResponse.getBody());
            if (!checkSmdpResponse(downloadJson))
            {
                log.info(req + "Download Order Request Failed with URL {} , status {} , response {}", downloadOrderUrl,
                        200, downloadOrderResponse.getBody());
                if (releaseFlag)
                {
                    tenantId = getTenantId(tenantIdSceProperty);
                    log.info(req + MESSAGE_TENANT_ID + tenantId);
                }

                log.info(req + "Sending AddOrUpdatePrimaryInfo Request to ES");
//                ResponseEntity<String> addOrUpdateResponse = prepareAndSendAddOrUpdatePrimaryInfo(iccid,
//                        esimProfileStatus, eid, tenantId, msisdn);
                ResponseEntity<String> addOrUpdateResponse = temp_retry_method(iccid, esimProfileStatus, eid, tenantId,
                        msisdn);

                log.info(req + "AddOrUpdatePrimaryInfo Response :: {}", addOrUpdateResponse);

                HashMap<String, String> statusCodeData = getStatusCodeData(downloadJson);
                HashMap<String, ErrorCodes> statusCodeMap = TransformerServiceImpl.smdpErrorCodes
                        .get(statusCodeData.get(SUBJECT_CODE));

                if (null == statusCodeMap || null == statusCodeMap.get(statusCodeData.get(REASON_CODE)))
                {
                    errorCode = ErrorCodes.GENERAL_ERROR.getCode();
                    errorMessage = ErrorCodes.GENERAL_ERROR.getMessage() + ", " + statusCodeData.get(MESSAGE);
                }
                else
                {
                    errorCode = statusCodeMap.get(statusCodeData.get(REASON_CODE)).getCode();
                    errorMessage = statusCodeMap.get(statusCodeData.get(REASON_CODE)).getMessage();
                }

                PrepareEsimProfileResponseDto finalResponse = preparePrepareEsimProfileResponse(requestId, iccId, "",
                        errorCode, errorMessage);

                log.info(req + "Final Response :: {}", finalResponse);
                return finalResponse;
            }
            else
            {
                log.info(req + "Download Order Request Success, Response :: {}", downloadJson.toString());

                iccId = downloadJson.getString(ICCID).substring(0, 19);

                if (null == eid)
                {
                    esimProfileStatus = "Allocated";
                }
                else
                {
                    esimProfileStatus = "Linked";
                }
            }
        }
        catch (HttpClientErrorException | HttpServerErrorException e)
        {
            log.info(req + "Download Order Request Failed with URL {} , status {} , headers {} , response {}",
                    downloadOrderUrl, e.getStatusCode(), e.getResponseHeaders(), e.getResponseBodyAsString());
            log.error(req + CommonServiceImpl.getStackTraceAsString(e));
            throw new TransformerException(ErrorCodes.UPSTREAM_ERROR.getCode(), ErrorCodes.UPSTREAM_ERROR.getMessage(),
                    e.getMessage());
        }
        catch (Exception e)
        {
            log.error(req + MESSAGE_POST_REQUEST_FAILED, downloadOrderUrl);
            log.error(req + CommonServiceImpl.getStackTraceAsString(e));
            throw new TransformerException(ErrorCodes.UPSTREAM_TIMEOUT.getCode(),
                    ErrorCodes.UPSTREAM_TIMEOUT.getMessage(), e.getMessage());
        }

        String activationCode = StringUtils.EMPTY;

        // CONFIRM ORDER REQUEST
        log.info(req + "Sending Confirm Order request");
        ResponseEntity<String> confirmOrderResponse = null;

        try
        {
            confirmOrderResponse = sendConfirmOrderRequest(eid, iccid, releaseFlag, StringUtils.EMPTY, smdsAddress);
            JSONObject confirmResponse = new JSONObject(confirmOrderResponse.getBody());
            if (!checkSmdpResponse(confirmResponse))
            {
                log.info(req + "Confirm Order Request Failed with URL {} , status {} , response {}", confirmOrderUrl,
                        200, confirmOrderResponse.getBody());

                HashMap<String, String> statusCodeData = getStatusCodeData(confirmResponse);
                HashMap<String, ErrorCodes> statusCodeMap = TransformerServiceImpl.smdpErrorCodes
                        .get(statusCodeData.get(SUBJECT_CODE));

                if (null == statusCodeMap || null == statusCodeMap.get(statusCodeData.get(REASON_CODE)))
                {
                    errorCode = ErrorCodes.GENERAL_ERROR.getCode();
                    errorMessage = ErrorCodes.GENERAL_ERROR.getMessage() + ", " + statusCodeData.get(MESSAGE);
                }
                else
                {
                    errorCode = statusCodeMap.get(statusCodeData.get(REASON_CODE)).getCode();
                    errorMessage = statusCodeMap.get(statusCodeData.get(REASON_CODE)).getMessage();
                }
            }
            else
            {
                log.info(req + "Confirm Order Request Success, Response :: {}", confirmResponse.toString());
                if (releaseFlag)
                {
                    esimProfileStatus = "Released";
                }
                else
                {
                    esimProfileStatus = "Confirmed";
                }

                if (null == eid)
                {
//                    JSONObject confirmOrderResponseJson = confirmOrderResponse.getBody();
                    activationCode = "1$" + confirmResponse.getString("smdpAddress") + "$"
                            + confirmResponse.getString(MATCHING_ID);
                }
                else
                {
                    activationCode = StringUtils.EMPTY;
                }
            }
        }
        catch (HttpClientErrorException | HttpServerErrorException e)
        {
            log.info(req + "Confirm Order Request Failed with URL {} , status {} , headers {} , response {}",
                    confirmOrderUrl, e.getStatusCode(), e.getResponseHeaders(), e.getResponseBodyAsString());
            log.error(req + CommonServiceImpl.getStackTraceAsString(e));

            throw new TransformerException(ErrorCodes.UPSTREAM_ERROR.getCode(), ErrorCodes.UPSTREAM_ERROR.getMessage(),
                    e.getMessage());
        }
        catch (Exception e)
        {
            log.error(req + MESSAGE_POST_REQUEST_FAILED, confirmOrderUrl);
            log.error(req + CommonServiceImpl.getStackTraceAsString(e));
            throw new TransformerException(ErrorCodes.UPSTREAM_TIMEOUT.getCode(),
                    ErrorCodes.UPSTREAM_TIMEOUT.getMessage(), e.getMessage());
        }

        if (releaseFlag)
        {
            tenantId = getTenantId("generic.system.property2");
            log.info(req + MESSAGE_TENANT_ID + tenantId);
        }

        log.info(req + "Sending AddOrUpdatePrimaryInfo Request to ES");
//        ResponseEntity<String> addOrUpdateResponse = prepareAndSendAddOrUpdatePrimaryInfo(iccId, esimProfileStatus, eid,
//                tenantId, msisdn);
        ResponseEntity<String> addOrUpdateResponse = temp_retry_method(iccId, esimProfileStatus, eid, tenantId, msisdn);

        log.info(req + "AddOrUpdatePrimaryInfo Response :: {}", addOrUpdateResponse);

        PrepareEsimProfileResponseDto finalResponse = preparePrepareEsimProfileResponse(requestId, iccId,
                activationCode, errorCode, errorMessage);

        log.info(req + "Final Response :: {}", finalResponse);
        return finalResponse;
    }

    @Override
    public ChangeEsimProfileStatusResponseDto changeEsimProfileStatus(
            ChangeEsimProfileStatusRequestDto changeEsimProfileStatusRequestDto)
    {
        log.info(changeEsimProfileStatusRequestDto + "Validating changeEsimProfileStatus Request");
        commonService.validateChangeEsimProfileStatus(changeEsimProfileStatusRequestDto);

//        JSONObject response = new JSONObject(sendGetPrimaryInfoByIccidRequest(changeEsimProfileStatusRequestDto,
//                changeEsimProfileStatusRequestDto.getIccid()));
        String rr = sendGetPrimaryInfoByIccidRequest(changeEsimProfileStatusRequestDto,
                changeEsimProfileStatusRequestDto.getIccid());
        JSONObject response = new JSONObject(rr.substring(1, rr.length() - 1));

        log.info(changeEsimProfileStatusRequestDto + "GetPrimaryInfoByIccid Response :: {}", response.toString());

        String errorCode = ErrorCodes.SUCCESS.getCode();
        String errorMessage = ErrorCodes.SUCCESS.getMessage();

        try
        {
            ResponseEntity<String> cancelOrderResponse = sendCancelOrderRequest(
                    response.has(EID) ? response.getString(EID) : StringUtils.EMPTY,
                    changeEsimProfileStatusRequestDto.getIccid(), changeEsimProfileStatusRequestDto.getProfileStatus());

            JSONObject cancelOrderResponseJson = new JSONObject(cancelOrderResponse.getBody());

            if (!checkSmdpResponse(cancelOrderResponseJson))
            {
                log.info(
                        changeEsimProfileStatusRequestDto
                                + "Cancel Order Request Failed with URL {} , status {} , response {}",
                        cancelOrderUrl, 200, cancelOrderResponse.getBody());

                HashMap<String, String> statusCodeData = getStatusCodeData(cancelOrderResponseJson);
                HashMap<String, ErrorCodes> statusCodeMap = TransformerServiceImpl.smdpErrorCodes
                        .get(statusCodeData.get(SUBJECT_CODE));

                if (null == statusCodeMap || null == statusCodeMap.get(statusCodeData.get(REASON_CODE)))
                {
                    errorCode = ErrorCodes.GENERAL_ERROR.getCode();
                    errorMessage = ErrorCodes.GENERAL_ERROR.getMessage() + ", " + statusCodeData.get(MESSAGE);
                }
                else
                {
                    errorCode = statusCodeMap.get(statusCodeData.get(REASON_CODE)).getCode();
                    errorMessage = statusCodeMap.get(statusCodeData.get(REASON_CODE)).getMessage();
                }
            }
            else
            {
                log.info(changeEsimProfileStatusRequestDto + "Cancel Order Request Success, Response :: {}",
                        cancelOrderResponseJson.toString());
                String tenantId = getTenantId("generic.system.property2");
                log.info(changeEsimProfileStatusRequestDto + MESSAGE_TENANT_ID + tenantId);

                log.info(changeEsimProfileStatusRequestDto + "Sending AddOrUpdatePrimaryInfo Request to ES");
//                ResponseEntity<String> addOrUpdateResponse = prepareAndSendAddOrUpdatePrimaryInfo(
//                        changeEsimProfileStatusRequestDto.getIccid(),
//                        changeEsimProfileStatusRequestDto.getProfileStatus(), null, tenantId, null);
                ResponseEntity<String> addOrUpdateResponse = temp_retry_method(
                        changeEsimProfileStatusRequestDto.getIccid(),
                        changeEsimProfileStatusRequestDto.getProfileStatus(), null, tenantId, null);

                log.info(changeEsimProfileStatusRequestDto + "AddOrUpdatePrimaryInfo Response :: {}",
                        addOrUpdateResponse);
            }
        }
        catch (HttpClientErrorException | HttpServerErrorException e)
        {
            log.info(
                    changeEsimProfileStatusRequestDto
                            + "Cancel Order Request Failed with URL {} , status {} , headers {} , response {}",
                    cancelOrderUrl, e.getStatusCode(), e.getResponseHeaders(), e.getResponseBodyAsString());
            log.error(changeEsimProfileStatusRequestDto + CommonServiceImpl.getStackTraceAsString(e));

            throw new TransformerException(ErrorCodes.UPSTREAM_ERROR.getCode(), ErrorCodes.UPSTREAM_ERROR.getMessage(),
                    e.getMessage());
        }
        catch (Exception e)
        {
            log.error(changeEsimProfileStatusRequestDto + MESSAGE_POST_REQUEST_FAILED, cancelOrderUrl);
            log.error(changeEsimProfileStatusRequestDto + CommonServiceImpl.getStackTraceAsString(e));
            throw new TransformerException(ErrorCodes.UPSTREAM_TIMEOUT.getCode(),
                    ErrorCodes.UPSTREAM_TIMEOUT.getMessage(), e.getMessage());
        }

        ChangeEsimProfileStatusResponseDto finalResponse = prepareChangeEsimProfileStatusResponse(
                changeEsimProfileStatusRequestDto.getRequestId(), changeEsimProfileStatusRequestDto.getIccid(),
                errorCode, errorMessage);

        log.info(changeEsimProfileStatusRequestDto + "Final Response :: {}", finalResponse);
        return finalResponse;
    }

    @Override
    public UnlinkAndReserveProfileResponseDto unlinkAndReserveProfile(
            UnlinkAndReserveProfileRequestDto unlinkAndReserveProfileRequestDto)
    {
        log.info(unlinkAndReserveProfileRequestDto + "Validating unlinkAndReserveProfile Request");
        commonService.validateUnlinkAndReserveProfile(unlinkAndReserveProfileRequestDto);

        String errorCode = ErrorCodes.SUCCESS.getCode();
        String errorMessage = ErrorCodes.SUCCESS.getMessage();

        try
        {
            ResponseEntity<String> cancelOrderResponse = sendCancelOrderRequest(
                    unlinkAndReserveProfileRequestDto.getEid(), unlinkAndReserveProfileRequestDto.getIccid(),
                    "Available");

            JSONObject cancelOrderResponseJson = new JSONObject(cancelOrderResponse.getBody());

            if (!checkSmdpResponse(cancelOrderResponseJson))
            {
                log.info(
                        unlinkAndReserveProfileRequestDto
                                + "Cancel Order Request Failed with URL {} , status {} , response {}",
                        cancelOrderUrl, 200, cancelOrderResponse.getBody());

                HashMap<String, String> statusCodeData = getStatusCodeData(cancelOrderResponseJson);
                HashMap<String, ErrorCodes> statusCodeMap = TransformerServiceImpl.smdpErrorCodes
                        .get(statusCodeData.get(SUBJECT_CODE));

                if (null == statusCodeMap || null == statusCodeMap.get(statusCodeData.get(REASON_CODE)))
                {
                    errorCode = ErrorCodes.GENERAL_ERROR.getCode();
                    errorMessage = ErrorCodes.GENERAL_ERROR.getMessage() + ", " + statusCodeData.get(MESSAGE);
                }
                else
                {
                    errorCode = statusCodeMap.get(statusCodeData.get(REASON_CODE)).getCode();
                    errorMessage = statusCodeMap.get(statusCodeData.get(REASON_CODE)).getMessage();
                }

                PrepareEsimProfileResponseDto finalResponse = preparePrepareEsimProfileResponse(
                        unlinkAndReserveProfileRequestDto.getRequestId(), unlinkAndReserveProfileRequestDto.getIccid(),
                        "", errorCode, errorMessage);

                log.info(unlinkAndReserveProfileRequestDto + "Final Response :: {}", finalResponse);
                return finalResponse;
            }
            else
            {
                log.info(unlinkAndReserveProfileRequestDto + "Cancel Order Request Success, Response :: {}",
                        cancelOrderResponseJson.toString());

                return prepareEsimProfile(unlinkAndReserveProfileRequestDto,
                        unlinkAndReserveProfileRequestDto.getIccid(), null,
                        unlinkAndReserveProfileRequestDto.getRequestId(),
                        unlinkAndReserveProfileRequestDto.isReleaseFlag(),
                        unlinkAndReserveProfileRequestDto.getMsisdn());
            }
        }
        catch (HttpClientErrorException | HttpServerErrorException e)
        {
            log.info(
                    unlinkAndReserveProfileRequestDto
                            + "Cancel Order Request Failed with URL {} , status {} , headers {} , response {}",
                    cancelOrderUrl, e.getStatusCode(), e.getResponseHeaders(), e.getResponseBodyAsString());
            log.error(unlinkAndReserveProfileRequestDto + CommonServiceImpl.getStackTraceAsString(e));

            throw new TransformerException(ErrorCodes.UPSTREAM_ERROR.getCode(), ErrorCodes.UPSTREAM_ERROR.getMessage(),
                    e.getMessage());
        }
        
        
        catch (Exception e)
        {
            log.error(unlinkAndReserveProfileRequestDto + MESSAGE_POST_REQUEST_FAILED, cancelOrderUrl);
            log.error(unlinkAndReserveProfileRequestDto + CommonServiceImpl.getStackTraceAsString(e));
            throw new TransformerException(ErrorCodes.UPSTREAM_TIMEOUT.getCode(),
                    ErrorCodes.UPSTREAM_TIMEOUT.getMessage(), e.getMessage());
        }
    }

    public String sendGetPrimaryInfoByIccidRequest(Object req, String iccid)
    {
        String finalUrl = getPrimaryInfoByIccidUrl + iccid;

        try
        {
            HttpHeaders headers = new HttpHeaders();
            headers.add(AUTHORIZATION, commonService.generateBasicAuthHeader(esUsername, esPassword));

            log.info(req + "Sending GET request to ES for GetPrimaryInfoByIccid");
            return commonService.sendGetRequest(finalUrl, headers, String.class).getBody();
        }
        catch (HttpClientErrorException | HttpServerErrorException e)
        {
            log.info(req + "sendGetRequest failed for GetPrimaryInfoByIccid with status {} , headers {} , response {}",
                    e.getStatusCode(), e.getResponseHeaders(), e.getResponseBodyAsString());
            log.error(req + CommonServiceImpl.getStackTraceAsString(e));
            throw new TransformerException(ErrorCodes.UPSTREAM_ERROR.getCode(), ErrorCodes.UPSTREAM_ERROR.getMessage(),
                    e.getMessage());
        }
        catch (Exception e)
        {
            log.error(req + "sendGetRequest failed for GetPrimaryInfoByIccid URL:{}", finalUrl);
            log.error(req + CommonServiceImpl.getStackTraceAsString(e));
            throw new TransformerException(ErrorCodes.UPSTREAM_TIMEOUT.getCode(),
                    ErrorCodes.UPSTREAM_TIMEOUT.getMessage(), e.getMessage());
        }
    }

    public PrepareEsimProfileResponseDto preparePrepareEsimProfileResponse(String responseId, String iccId,
            String activationCode, String errorCode, String errorMessage)
    {
        return new PrepareEsimProfileResponseDto(responseId, iccId, activationCode, errorCode, errorMessage);
    }

    public GetEsimProfileStatusResponseDto prepareGetEsimProfileStatusResponse(String responseId, String iccid,
            String profileStatus)
    {
        return new GetEsimProfileStatusResponseDto(responseId, iccid, profileStatus, ErrorCodes.SUCCESS.getCode(),
                ErrorCodes.SUCCESS.getMessage());
    }

    public ChangeEsimProfileStatusResponseDto prepareChangeEsimProfileStatusResponse(String responseId, String iccid,
            String errorCode, String errorMessage)
    {
        return new ChangeEsimProfileStatusResponseDto(responseId, iccid, errorCode, errorMessage);
    }

    public ResponseEntity<String> sendDownloadOrderRequest(String iccId, String eid)
    {
        JSONObject downloadOrderRequest = new JSONObject();
        downloadOrderRequest.put(ICCID, iccId);

        JSONObject header = new JSONObject();
        
        System.out.println("sendDownloadOrderRequest>> profileTypeValue : " + profileTypeValue);
        System.out.println("sendDownloadOrderRequest>> Req profileTypeValue : " + profileTypeRequired);

        //downloadOrderRequest.put("profileType", "ESIM10010"); // Removing the profileType
        //downloadOrderRequest.put("profileType", profileTypeValue);
        
        //downloadOrderRequest.put("profileType", NULL);
        
        if(profileTypeRequired) {
        	System.out.println("Properties : profile type required..");
        	downloadOrderRequest.put("profileType", profileTypeValue);
        } 
                       
        
        header.put(FUNCTION_CALL_IDENTIFIER, FUNCTION_CALL_IDENTIFIER_VALUE);
        header.put(FUNCTION_REQUEST_IDENTIFIER, FUNCTION_REQUEST_IDENTIFIER_VALUE);

        downloadOrderRequest.put(HEADER, header);

        if (null != eid)
        {
            downloadOrderRequest.put(EID, eid);
        }

        HttpHeaders headers = new HttpHeaders();
        log.info("Downloadorder request is :"+ downloadOrderRequest.toString());
        return commonService.sendPostRequest(downloadOrderUrl, headers, downloadOrderRequest.toString(), String.class);
    }

    public ResponseEntity<String> sendConfirmOrderRequest(String eid, String iccId, boolean releaseFlag,
            String matchingId, String smdsAddress)
    {
        JSONObject confirmOrderRequest = new JSONObject();
        confirmOrderRequest.put(ICCID, iccId);
        confirmOrderRequest.put("releaseFlag", releaseFlag);

        JSONObject header = new JSONObject();
        header.put(FUNCTION_CALL_IDENTIFIER, FUNCTION_CALL_IDENTIFIER_VALUE);
        header.put(FUNCTION_REQUEST_IDENTIFIER, FUNCTION_REQUEST_IDENTIFIER_VALUE);

        confirmOrderRequest.put(HEADER, header);

        if (null != eid)
        {
            confirmOrderRequest.put(EID, eid);
            confirmOrderRequest.put(MATCHING_ID, matchingId);
            confirmOrderRequest.put("smdsAddress", smdsAddress);
        }

        HttpHeaders headers = new HttpHeaders();

        return commonService.sendPostRequest(confirmOrderUrl, headers, confirmOrderRequest.toString(), String.class);
    }

    public ResponseEntity<String> sendCancelOrderRequest(String eid, String iccId, String finalProfileStatusIndicator)
    {
        JSONObject cancelOrderRequest = new JSONObject();
        cancelOrderRequest.put(ICCID, iccId);
        cancelOrderRequest.put("finalProfileStatusIndicator", finalProfileStatusIndicator);

        JSONObject header = new JSONObject();
        header.put(FUNCTION_CALL_IDENTIFIER, FUNCTION_CALL_IDENTIFIER_VALUE);
        header.put(FUNCTION_REQUEST_IDENTIFIER, FUNCTION_REQUEST_IDENTIFIER_VALUE);

        cancelOrderRequest.put(HEADER, header);

        if (null != eid)
        {
            cancelOrderRequest.put(EID, eid);
        }

        HttpHeaders headers = new HttpHeaders();

        return commonService.sendPostRequest(cancelOrderUrl, headers, cancelOrderRequest.toString(), String.class);
    }

    public ResponseEntity<String> prepareAndSendAddOrUpdatePrimaryInfo(String iccId, String esimProfileStatus,
            String eid, String tenantId, String msisdn)
    {
        JSONObject addOrUpdatePrimaryInfoRequest = new JSONObject();
        addOrUpdatePrimaryInfoRequest.put(ICCID, iccId);
        addOrUpdatePrimaryInfoRequest.put("eSimProfileStatus", esimProfileStatus);
        addOrUpdatePrimaryInfoRequest.put("inUse", "true");

        if (null != tenantId && !tenantId.isEmpty())
        {
            addOrUpdatePrimaryInfoRequest.put("tenantId", tenantId);
        }
        if (null != eid && !eid.isEmpty())
        {
            addOrUpdatePrimaryInfoRequest.put(EID, eid);
        }
        if (null != msisdn && !msisdn.isEmpty())
        {
            addOrUpdatePrimaryInfoRequest.put("msisdn", msisdn);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add(AUTHORIZATION, commonService.generateBasicAuthHeader(esUsername, esPassword));

        return commonService.sendPutRequest(addOrUpdatePrimaryInfoUrl, headers, addOrUpdatePrimaryInfoRequest,
                String.class);
    }

    public String getTenantId(String propertyName)
    {
        try
        {
            HttpHeaders headers = new HttpHeaders();
            headers.add(AUTHORIZATION, commonService.generateBasicAuthHeader(esUsername, esPassword));

            String response = commonService
                    .sendGetRequest(getScePropertyUrl.replace("{key}", propertyName), headers, String.class).getBody();

            return Arrays.stream(response.substring(7, response.length()).split(";")).map(kvPair -> kvPair.split(":"))
                    .collect(Collectors.toMap(kv -> kv[0], kv -> kv[1])).get("DSDS-PALM-MOA");
        }
        catch (Exception e)
        {
            log.info("Failed to fetch Tenant ID using ES API");
            log.info(CommonServiceImpl.getStackTraceAsString(e));

            return StringUtils.EMPTY;
        }
    }

    public boolean checkSmdpResponse(JSONObject response)
    {
        try
        {
            if (null != response)
            {
                JSONObject functionExecutionStatus = response.getJSONObject(HEADER)
                        .getJSONObject("functionExecutionStatus");

                if ("Executed-Success".equalsIgnoreCase(functionExecutionStatus.getString("status")))
                {
                    return true;
                }
            }
        }
        catch (Exception e)
        {
            CommonServiceImpl.getStackTraceAsString(e);
        }

        return false;
    }

    public HashMap<String, String> getStatusCodeData(JSONObject responseBody)
    {
        JSONObject statusCodeDataJson = responseBody.getJSONObject(HEADER).getJSONObject("functionExecutionStatus")
                .getJSONObject("statusCodeData");

        HashMap<String, String> statusCodeData = new HashMap<>();
        statusCodeData.put(SUBJECT_CODE, statusCodeDataJson.getString(SUBJECT_CODE));
        statusCodeData.put(REASON_CODE, statusCodeDataJson.getString(REASON_CODE));
        statusCodeData.put("subjectIdentifier", statusCodeDataJson.getString("subjectIdentifier"));
        statusCodeData.put(MESSAGE, statusCodeDataJson.getString(MESSAGE));

        return statusCodeData;
    }

    public static void prepareSmdpErrorCodesMap()
    {
        smdpErrorCodes = new HashMap<>();

        HashMap<String, ErrorCodes> map_8_2_1 = new HashMap<>();
        map_8_2_1.put("3.9", ErrorCodes.PROFILE_ICCID_UNKNOWN);
        map_8_2_1.put("1.2", ErrorCodes.PROFILE_ICCID_NOT_ALLOWED);
        map_8_2_1.put("2.2", ErrorCodes.PROFILE_ICCID_MISSING);
        map_8_2_1.put("3.3", ErrorCodes.PROFILE_ICCID_ALREADY_IN_USE);

        HashMap<String, ErrorCodes> map_8_2_5 = new HashMap<>();
        map_8_2_5.put("3.9", ErrorCodes.PROFILE_TYPE_UNKNOWN);
        map_8_2_5.put("1.2", ErrorCodes.PROFILE_TYPE_NOT_ALLOWED);
        map_8_2_5.put("3.7", ErrorCodes.PROFILE_TYPE_UNAVAILABLE);
        map_8_2_5.put("3.8", ErrorCodes.PROFILE_TYPE_REFUSED);

        HashMap<String, ErrorCodes> map_8_9 = new HashMap<>();
        map_8_9.put("5.1", ErrorCodes.SMDS_INACCESSIBLE);
        map_8_9.put("4.2", ErrorCodes.SMDS_EXECUTION_ERROR);

        HashMap<String, ErrorCodes> map_8_1_1 = new HashMap<>();
        map_8_1_1.put("1.2", ErrorCodes.EID_NOT_ALLOWED);
        map_8_1_1.put("2.1", ErrorCodes.EID_INVALID);
        map_8_1_1.put("2.2", ErrorCodes.EID_MISSING);
        map_8_1_1.put("3.10", ErrorCodes.EID_INVALID_ASSOCIATION);

        HashMap<String, ErrorCodes> map_1_6 = new HashMap<>();
        map_1_6.put("4.2", ErrorCodes.FUNCTION_EXECUTION_ERROR);
        map_1_6.put("2.2", ErrorCodes.FUNCTION_MANDATORY_PARAMETER_MISSING);

        HashMap<String, ErrorCodes> map_8_2_6 = new HashMap<>();
        map_8_2_6.put("3.8", ErrorCodes.MATCHING_ID_REFUSED);

        HashMap<String, ErrorCodes> map_8_2_7 = new HashMap<>();
        map_8_2_7.put("3.8", ErrorCodes.CONFIRMATION_CODE_REFUSED);

        HashMap<String, ErrorCodes> map_8_9_5 = new HashMap<>();
        map_8_9_5.put("3.3", ErrorCodes.EVENT_RECORD_ALREADY_IN_USE);

        HashMap<String, ErrorCodes> map_8_8_1 = new HashMap<>();
        map_8_8_1.put("1.1", ErrorCodes.SMDP_ADDRESS_UNKNOWN);

        smdpErrorCodes.put("8.1.1", map_8_1_1);
        smdpErrorCodes.put("8.2.1", map_8_2_1);
        smdpErrorCodes.put("8.2.5", map_8_2_5);
        smdpErrorCodes.put("8.9", map_8_9);
        smdpErrorCodes.put("1.6", map_1_6);
        smdpErrorCodes.put("8.2.6", map_8_2_6);
        smdpErrorCodes.put("8.2.7", map_8_2_7);
        smdpErrorCodes.put("8.9.5", map_8_9_5);
        smdpErrorCodes.put("8.8.1", map_8_8_1);
    }

    public ResponseEntity<String> temp_retry_method(String iccid, String esimProfileStatus, String eid, String tenantId,
            String msisdn)
    {
        ResponseEntity<String> response = null;

        for (int i = 0; i < 3; i++) {
            try {
                response = prepareAndSendAddOrUpdatePrimaryInfo(iccid, esimProfileStatus, eid, tenantId, msisdn);
            } catch (TransformerException e) {
                continue;
            }
            
            // Check if the response body contains the substring
            if (response.getBody().contains("Create or update of primary info details is successful")) {
                break;
                
            } 
            
            else 
            
            {
                log.info("AddOrUpdatePrimaryInfo failed, Retrying");
            }
        }

        return response;
    }

}
