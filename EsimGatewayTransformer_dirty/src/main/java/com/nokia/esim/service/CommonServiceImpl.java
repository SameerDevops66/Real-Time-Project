package com.nokia.esim.service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Base64;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.nokia.esim.config.ErrorCodes;
import com.nokia.esim.dto.ChangeEsimProfileStatusRequestDto;
import com.nokia.esim.dto.GetEsimProfileStatusRequestDto;
import com.nokia.esim.dto.PrepareEsimProfileRequestDto;
import com.nokia.esim.dto.UnlinkAndReserveProfileRequestDto;
import com.nokia.esim.exception.TransformerException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CommonServiceImpl implements CommonService
{

    private RestTemplate restTemplate;

    @Value("${transformer.user.name}")
    private String expectedUsername;

    @Value("${transformer.user.password}")
    private String expectedPassword;

    private static final String AUTH_HEADER_SUB_STRING = "Basic ";
    private static final String MESSAGE_ICCID = "IccId is either null or empty";
    private static final String MESSAGE_REQUEST_ID = "RequestId is either null or empty";

    public CommonServiceImpl(RestTemplate restTemplate)
    {
        this.restTemplate = restTemplate;
    }

    @Override
    public <T> ResponseEntity<T> sendGetRequest(String url, HttpHeaders headers, Class<T> responseType)
    {
        HttpEntity<T> requestEntity = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.GET, requestEntity, responseType);
    }

    @Override
    public <T, R> ResponseEntity<R> sendPostRequest(String url, HttpHeaders headers, T requestBody,
            Class<R> responseType)
    {
        headers.add("Content-Type", "application/json");
        log.info("Headers :: " + headers + ", Request Body :: " + requestBody);
        HttpEntity<T> requestEntity = new HttpEntity<>(requestBody, headers);
        return restTemplate.postForEntity(url, requestEntity, responseType);
    }

    @Override
    public <T, R> ResponseEntity<R> sendPutRequest(String url, HttpHeaders headers, T requestBody,
            Class<R> responseType)
    {
        try
        {
            headers.add("Content-Type", "application/json");
            log.info("Headers :: " + headers + ", Request Body :: " + requestBody);
            HttpEntity<String> requestEntity = new HttpEntity<>(requestBody.toString(), headers);
            return restTemplate.exchange(url, HttpMethod.PUT, requestEntity, responseType);
        }
        catch (Exception e)
        {
            log.error("sendPutRequest failed for URL:{}", url);
            log.error(CommonServiceImpl.getStackTraceAsString(e));
            throw new TransformerException(ErrorCodes.UPSTREAM_TIMEOUT.getCode(),
                    ErrorCodes.UPSTREAM_TIMEOUT.getMessage(), e.getMessage());
        }
    }

    @Override
    public void validateGetEsimProfileStatusRequest(GetEsimProfileStatusRequestDto esimProfileStatusRequestDto)
    {
        if (null == esimProfileStatusRequestDto.getIccid()
                || StringUtils.EMPTY.equals(esimProfileStatusRequestDto.getIccid()))
        {
            log.info(esimProfileStatusRequestDto + MESSAGE_ICCID);
            throw new TransformerException(ErrorCodes.ICCID_MISSING.getCode(), ErrorCodes.ICCID_MISSING.getMessage(),
                    MESSAGE_ICCID);
        }
        if (null == esimProfileStatusRequestDto.getRequestId()
                || StringUtils.EMPTY.equals(esimProfileStatusRequestDto.getRequestId()))
        {
            log.info(esimProfileStatusRequestDto + MESSAGE_REQUEST_ID);
            throw new TransformerException(ErrorCodes.ERROR_SCHEMA_VALIDATION.getCode(),
                    ErrorCodes.ERROR_SCHEMA_VALIDATION.getMessage(), MESSAGE_REQUEST_ID);
        }

        log.info(esimProfileStatusRequestDto + "Validation Success - GetEsimProfileStatusRequest");
    }

    @Override
    public void validatePrepareEsimProfileRequest(PrepareEsimProfileRequestDto prepareEsimProfileRequestDto)
    {
        if (null == prepareEsimProfileRequestDto.getIccId()
                || StringUtils.EMPTY.equals(prepareEsimProfileRequestDto.getIccId()))
        {
            log.info(prepareEsimProfileRequestDto + MESSAGE_ICCID);
            throw new TransformerException(ErrorCodes.PROFILE_ICCID_MISSING.getCode(),
                    ErrorCodes.PROFILE_ICCID_MISSING.getMessage(), MESSAGE_ICCID);
        }
        if (null == prepareEsimProfileRequestDto.getRequestId()
                || StringUtils.EMPTY.equals(prepareEsimProfileRequestDto.getRequestId()))
        {
            log.info(prepareEsimProfileRequestDto + MESSAGE_REQUEST_ID);
            throw new TransformerException(ErrorCodes.ERROR_SCHEMA_VALIDATION.getCode(),
                    ErrorCodes.ERROR_SCHEMA_VALIDATION.getMessage(), MESSAGE_REQUEST_ID);
        }

        log.info(prepareEsimProfileRequestDto + "Validation Success - PrepareEsimProfileRequest");
    }

    @Override
    public void validateChangeEsimProfileStatus(ChangeEsimProfileStatusRequestDto changeEsimProfileStatusRequestDto)
    {
        if (null == changeEsimProfileStatusRequestDto.getIccid()
                || StringUtils.EMPTY.equals(changeEsimProfileStatusRequestDto.getIccid()))
        {
            log.info(changeEsimProfileStatusRequestDto + MESSAGE_ICCID);
            throw new TransformerException(ErrorCodes.ICCID_MISSING.getCode(), ErrorCodes.ICCID_MISSING.getMessage(),
                    MESSAGE_ICCID);
        }
        if (null == changeEsimProfileStatusRequestDto.getRequestId()
                || StringUtils.EMPTY.equals(changeEsimProfileStatusRequestDto.getRequestId()))
        {
            log.info(changeEsimProfileStatusRequestDto + MESSAGE_REQUEST_ID);
            throw new TransformerException(ErrorCodes.ERROR_SCHEMA_VALIDATION.getCode(),
                    ErrorCodes.ERROR_SCHEMA_VALIDATION.getMessage(), MESSAGE_REQUEST_ID);
        }
        if (null == changeEsimProfileStatusRequestDto.getProfileStatus()
                || StringUtils.EMPTY.equals(changeEsimProfileStatusRequestDto.getProfileStatus()))
        {
            log.info(changeEsimProfileStatusRequestDto + "ProfileStatus is either null or empty");
            throw new TransformerException(ErrorCodes.ERROR_SCHEMA_VALIDATION.getCode(),
                    ErrorCodes.ERROR_SCHEMA_VALIDATION.getMessage(), "ProfileStatus is either null or empty");
        }

        log.info(changeEsimProfileStatusRequestDto + "Validation Success - ChangeEsimProfileStatusRequest");
    }

    @Override
    public void validateUnlinkAndReserveProfile(UnlinkAndReserveProfileRequestDto unlinkAndReserveProfileRequestDto)
    {
        if (null == unlinkAndReserveProfileRequestDto.getIccid()
                || StringUtils.EMPTY.equals(unlinkAndReserveProfileRequestDto.getIccid()))
        {
            log.info(unlinkAndReserveProfileRequestDto + MESSAGE_ICCID);
            throw new TransformerException(ErrorCodes.PROFILE_ICCID_MISSING.getCode(),
                    ErrorCodes.PROFILE_ICCID_MISSING.getMessage(), MESSAGE_ICCID);
        }
        if (null == unlinkAndReserveProfileRequestDto.getRequestId()
                || StringUtils.EMPTY.equals(unlinkAndReserveProfileRequestDto.getRequestId()))
        {
            log.info(unlinkAndReserveProfileRequestDto + MESSAGE_REQUEST_ID);
            throw new TransformerException(ErrorCodes.ERROR_SCHEMA_VALIDATION.getCode(),
                    ErrorCodes.ERROR_SCHEMA_VALIDATION.getMessage(), MESSAGE_REQUEST_ID);
        }
        if (null == unlinkAndReserveProfileRequestDto.getEid()
                || StringUtils.EMPTY.equals(unlinkAndReserveProfileRequestDto.getEid()))
        {
            log.info(unlinkAndReserveProfileRequestDto + "eid is either null or empty");
            throw new TransformerException(ErrorCodes.EID_MISSING.getCode(), ErrorCodes.EID_MISSING.getMessage(),
                    "eid is either null or empty");
        }

        log.info(unlinkAndReserveProfileRequestDto + "Validation Success - UnlinkAndReserveProfileRequest");
    }

    @Override
    public String generateBasicAuthHeader(String username, String password)
    {
        String credentials = username + ":" + password;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());
        return AUTH_HEADER_SUB_STRING + encodedCredentials;
    }

    @Override
    public boolean verifyCredentials(String authorizationHeader)
    {
        if (authorizationHeader != null && authorizationHeader.startsWith(AUTH_HEADER_SUB_STRING))
        {
            try
            {
                String base64Credentials = authorizationHeader.substring(AUTH_HEADER_SUB_STRING.length()).trim();
                String credentials = new String(Base64.getDecoder().decode(base64Credentials));
                String[] parts = credentials.split(":", 2);

                if (parts.length == 2)
                {
                    String username = parts[0];
                    String password = parts[1];

                    return expectedUsername.equals(username) && expectedPassword.equals(password);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static String getStackTraceAsString(Exception exception)
    {
        StringWriter stringWriter = new StringWriter();
        exception.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }

}
