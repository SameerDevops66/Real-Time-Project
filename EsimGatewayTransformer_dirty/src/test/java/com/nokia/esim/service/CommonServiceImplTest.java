package com.nokia.esim.service;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.nokia.esim.config.ErrorCodes;
import com.nokia.esim.dto.ChangeEsimProfileStatusRequestDto;
import com.nokia.esim.dto.GetEsimProfileStatusRequestDto;
import com.nokia.esim.dto.PrepareEsimProfileRequestDto;
import com.nokia.esim.dto.UnlinkAndReserveProfileRequestDto;
import com.nokia.esim.exception.TransformerException;


@SpringBootTest
class CommonServiceImplTest
{

    @Mock
    private RestTemplate restTemplate;

    @Autowired
    private CommonServiceImpl commonService;

    private CommonServiceImpl commonServiceMockBean;

    @SuppressWarnings("deprecation")
    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.initMocks(this);
        commonServiceMockBean = new CommonServiceImpl(restTemplate);
    }

    @Test
    void testSendGetRequest()
    {
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<String> mockResponseEntity = new ResponseEntity<>("Mock Response", HttpStatus.OK);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(mockResponseEntity);

        ResponseEntity<String> responseEntity = commonServiceMockBean.sendGetRequest("http://example.com", headers,
                String.class);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("Mock Response", responseEntity.getBody());
    }

    @Test
    void testSendPostRequest()
    {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> mockResponseEntity = new ResponseEntity<>("Mock Response", HttpStatus.CREATED);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(mockResponseEntity);

        ResponseEntity<String> responseEntity = commonServiceMockBean.sendPostRequest("http://example.com", headers,
                "Request", String.class);

        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertEquals("Mock Response", responseEntity.getBody());
    }

    @Test
    void testSendPutRequest()
    {
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<String> mockResponseEntity = new ResponseEntity<>("Mock Response", HttpStatus.OK);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.PUT), any(HttpEntity.class), eq(String.class)))
                .thenReturn(mockResponseEntity);

        ResponseEntity<String> responseEntity = commonServiceMockBean.sendPutRequest("http://example.com", headers,
                "Request", String.class);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("Mock Response", responseEntity.getBody());

        when(restTemplate.exchange(anyString(), eq(HttpMethod.PUT), any(HttpEntity.class), eq(String.class)))
                .thenThrow(HttpClientErrorException.class);
        TransformerException exception = assertThrows(TransformerException.class, () -> {
            commonServiceMockBean.sendPutRequest("http://example.com", headers, "Request", String.class);
        });
        assertEquals(ErrorCodes.UPSTREAM_TIMEOUT.getCode(), exception.getErrorCode());
    }

    @Test
    void testValidateGetEsimProfileStatusRequest()
    {
        GetEsimProfileStatusRequestDto validRequest = new GetEsimProfileStatusRequestDto();
        validRequest.setIccid("123");
        validRequest.setRequestId("456");

        assertDoesNotThrow(() -> commonService.validateGetEsimProfileStatusRequest(validRequest));

        GetEsimProfileStatusRequestDto missingIccidRequest = new GetEsimProfileStatusRequestDto();
        missingIccidRequest.setRequestId("456");

        TransformerException exception = assertThrows(TransformerException.class, () -> {
            commonService.validateGetEsimProfileStatusRequest(missingIccidRequest);
        });
        assertEquals(ErrorCodes.ICCID_MISSING.getCode(), exception.getErrorCode());

        missingIccidRequest.setIccid(StringUtils.EMPTY);
        exception = assertThrows(TransformerException.class, () -> {
            commonService.validateGetEsimProfileStatusRequest(missingIccidRequest);
        });
        assertEquals(ErrorCodes.ICCID_MISSING.getCode(), exception.getErrorCode());

        GetEsimProfileStatusRequestDto missingRequestIdRequest = new GetEsimProfileStatusRequestDto();
        missingRequestIdRequest.setIccid("123");

        exception = assertThrows(TransformerException.class, () -> {
            commonService.validateGetEsimProfileStatusRequest(missingRequestIdRequest);
        });
        assertEquals(ErrorCodes.ERROR_SCHEMA_VALIDATION.getCode(), exception.getErrorCode());

        missingRequestIdRequest.setRequestId(StringUtils.EMPTY);
        exception = assertThrows(TransformerException.class, () -> {
            commonService.validateGetEsimProfileStatusRequest(missingRequestIdRequest);
        });
        assertEquals(ErrorCodes.ERROR_SCHEMA_VALIDATION.getCode(), exception.getErrorCode());
    }

    @Test
    void testValidatePrepareEsimProfileRequest()
    {
        PrepareEsimProfileRequestDto validRequest = new PrepareEsimProfileRequestDto();
        validRequest.setIccId("123");
        validRequest.setRequestId("456");

        assertDoesNotThrow(() -> commonService.validatePrepareEsimProfileRequest(validRequest));

        PrepareEsimProfileRequestDto missingIccIdRequest = new PrepareEsimProfileRequestDto();
        missingIccIdRequest.setRequestId("456");

        TransformerException exception = assertThrows(TransformerException.class, () -> {
            commonService.validatePrepareEsimProfileRequest(missingIccIdRequest);
        });
        assertEquals(ErrorCodes.PROFILE_ICCID_MISSING.getCode(), exception.getErrorCode());

        missingIccIdRequest.setIccId(StringUtils.EMPTY);
        exception = assertThrows(TransformerException.class, () -> {
            commonService.validatePrepareEsimProfileRequest(missingIccIdRequest);
        });
        assertEquals(ErrorCodes.PROFILE_ICCID_MISSING.getCode(), exception.getErrorCode());

        PrepareEsimProfileRequestDto missingRequestIdRequest = new PrepareEsimProfileRequestDto();
        missingRequestIdRequest.setIccId("123");

        exception = assertThrows(TransformerException.class, () -> {
            commonService.validatePrepareEsimProfileRequest(missingRequestIdRequest);
        });
        assertEquals(ErrorCodes.ERROR_SCHEMA_VALIDATION.getCode(), exception.getErrorCode());

        missingRequestIdRequest.setRequestId(StringUtils.EMPTY);
        exception = assertThrows(TransformerException.class, () -> {
            commonService.validatePrepareEsimProfileRequest(missingRequestIdRequest);
        });
        assertEquals(ErrorCodes.ERROR_SCHEMA_VALIDATION.getCode(), exception.getErrorCode());
    }

    @Test
    void testValidateChangeEsimProfileStatus()
    {
        ChangeEsimProfileStatusRequestDto validRequest = new ChangeEsimProfileStatusRequestDto();
        validRequest.setIccid("123");
        validRequest.setRequestId("456");
        validRequest.setProfileStatus("ACTIVE");

        assertDoesNotThrow(() -> commonService.validateChangeEsimProfileStatus(validRequest));

        ChangeEsimProfileStatusRequestDto missingIccidRequest = new ChangeEsimProfileStatusRequestDto();
        missingIccidRequest.setRequestId("456");
        missingIccidRequest.setProfileStatus("ACTIVE");

        TransformerException exception = assertThrows(TransformerException.class, () -> {
            commonService.validateChangeEsimProfileStatus(missingIccidRequest);
        });
        assertEquals(ErrorCodes.ICCID_MISSING.getCode(), exception.getErrorCode());

        missingIccidRequest.setIccid(StringUtils.EMPTY);
        exception = assertThrows(TransformerException.class, () -> {
            commonService.validateChangeEsimProfileStatus(missingIccidRequest);
        });
        assertEquals(ErrorCodes.ICCID_MISSING.getCode(), exception.getErrorCode());

        ChangeEsimProfileStatusRequestDto missingRequestIdRequest = new ChangeEsimProfileStatusRequestDto();
        missingRequestIdRequest.setIccid("123");
        missingRequestIdRequest.setProfileStatus("ACTIVE");

        exception = assertThrows(TransformerException.class, () -> {
            commonService.validateChangeEsimProfileStatus(missingRequestIdRequest);
        });
        assertEquals(ErrorCodes.ERROR_SCHEMA_VALIDATION.getCode(), exception.getErrorCode());

        missingRequestIdRequest.setRequestId(StringUtils.EMPTY);
        exception = assertThrows(TransformerException.class, () -> {
            commonService.validateChangeEsimProfileStatus(missingRequestIdRequest);
        });
        assertEquals(ErrorCodes.ERROR_SCHEMA_VALIDATION.getCode(), exception.getErrorCode());

        ChangeEsimProfileStatusRequestDto missingProfileStatusRequest = new ChangeEsimProfileStatusRequestDto();
        missingProfileStatusRequest.setIccid("123");
        missingProfileStatusRequest.setRequestId("456");

        exception = assertThrows(TransformerException.class, () -> {
            commonService.validateChangeEsimProfileStatus(missingProfileStatusRequest);
        });
        assertEquals(ErrorCodes.ERROR_SCHEMA_VALIDATION.getCode(), exception.getErrorCode());

        missingProfileStatusRequest.setProfileStatus(StringUtils.EMPTY);
        exception = assertThrows(TransformerException.class, () -> {
            commonService.validateChangeEsimProfileStatus(missingProfileStatusRequest);
        });
        assertEquals(ErrorCodes.ERROR_SCHEMA_VALIDATION.getCode(), exception.getErrorCode());
    }

    @Test
    void testValidateUnlinkAndReserveProfile()
    {
        UnlinkAndReserveProfileRequestDto validRequest = new UnlinkAndReserveProfileRequestDto();
        validRequest.setIccid("123");
        validRequest.setRequestId("456");
        validRequest.setEid("789");

        assertDoesNotThrow(() -> commonService.validateUnlinkAndReserveProfile(validRequest));

        UnlinkAndReserveProfileRequestDto missingIccidRequest = new UnlinkAndReserveProfileRequestDto();
        missingIccidRequest.setRequestId("456");
        missingIccidRequest.setEid("789");

        TransformerException exception = assertThrows(TransformerException.class, () -> {
            commonService.validateUnlinkAndReserveProfile(missingIccidRequest);
        });
        assertEquals(ErrorCodes.PROFILE_ICCID_MISSING.getCode(), exception.getErrorCode());

        missingIccidRequest.setIccid(StringUtils.EMPTY);
        exception = assertThrows(TransformerException.class, () -> {
            commonService.validateUnlinkAndReserveProfile(missingIccidRequest);
        });
        assertEquals(ErrorCodes.PROFILE_ICCID_MISSING.getCode(), exception.getErrorCode());

        UnlinkAndReserveProfileRequestDto missingRequestIdRequest = new UnlinkAndReserveProfileRequestDto();
        missingRequestIdRequest.setIccid("123");
        missingRequestIdRequest.setEid("789");

        exception = assertThrows(TransformerException.class, () -> {
            commonService.validateUnlinkAndReserveProfile(missingRequestIdRequest);
        });
        assertEquals(ErrorCodes.ERROR_SCHEMA_VALIDATION.getCode(), exception.getErrorCode());

        missingRequestIdRequest.setRequestId(StringUtils.EMPTY);
        exception = assertThrows(TransformerException.class, () -> {
            commonService.validateUnlinkAndReserveProfile(missingRequestIdRequest);
        });
        assertEquals(ErrorCodes.ERROR_SCHEMA_VALIDATION.getCode(), exception.getErrorCode());

        UnlinkAndReserveProfileRequestDto missingEidRequest = new UnlinkAndReserveProfileRequestDto();
        missingEidRequest.setIccid("123");
        missingEidRequest.setRequestId("456");

        exception = assertThrows(TransformerException.class, () -> {
            commonService.validateUnlinkAndReserveProfile(missingEidRequest);
        });
        assertEquals(ErrorCodes.EID_MISSING.getCode(), exception.getErrorCode());

        missingEidRequest.setEid(StringUtils.EMPTY);
        exception = assertThrows(TransformerException.class, () -> {
            commonService.validateUnlinkAndReserveProfile(missingEidRequest);
        });
        assertEquals(ErrorCodes.EID_MISSING.getCode(), exception.getErrorCode());
    }

    @Test
    void testGenerateBasicAuthHeader()
    {
        String authHeader = commonService.generateBasicAuthHeader("user", "password");
        assertEquals("Basic dXNlcjpwYXNzd29yZA==", authHeader);
    }

    @Test
    void testVerifyCredentials()
    {
        String authHeader = "Basic ZXNpbWd3dXNlcjplczFtZ3d1czNy";
        assertTrue(commonService.verifyCredentials(authHeader));
        assertFalse(commonService.verifyCredentials("Basic ZXNpbWd3dXNlcg=="));
        assertFalse(commonService.verifyCredentials("Basic ZXNpbWd3dXNlcjplczFtZ3d1czM="));
        assertFalse(commonService.verifyCredentials("Basic ZXNpbWd3dXNlOmVzMW1nd3VzMw=="));
        assertFalse(commonService.verifyCredentials(authHeader.substring(2, authHeader.length())));
        assertFalse(commonService.verifyCredentials(null));
    }
}
