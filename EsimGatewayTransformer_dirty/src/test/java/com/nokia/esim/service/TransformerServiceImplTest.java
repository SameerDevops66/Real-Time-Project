package com.nokia.esim.service;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import com.nokia.esim.config.ErrorCodes;
import com.nokia.esim.dto.ChangeEsimProfileStatusRequestDto;
import com.nokia.esim.dto.ChangeEsimProfileStatusResponseDto;
import com.nokia.esim.dto.GetEsimProfileStatusRequestDto;
import com.nokia.esim.dto.GetEsimProfileStatusResponseDto;
import com.nokia.esim.dto.GetPrimaryInfoByIccidResponseDto_Es;
import com.nokia.esim.dto.PrepareEsimProfileRequestDto;
import com.nokia.esim.dto.PrepareEsimProfileResponseDto;
import com.nokia.esim.dto.UnlinkAndReserveProfileRequestDto;
import com.nokia.esim.dto.UnlinkAndReserveProfileResponseDto;
import com.nokia.esim.exception.TransformerException;

@SpringBootTest
class TransformerServiceImplTest
{

    @Autowired
    private TransformerServiceImpl transformerService;

    @MockBean
    private CommonService commonService;

    @SuppressWarnings("deprecation")
    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testGetEsimProfileStatus_Success()
    {
        GetEsimProfileStatusRequestDto requestDto = new GetEsimProfileStatusRequestDto();
        GetPrimaryInfoByIccidResponseDto_Es responseDto = new GetPrimaryInfoByIccidResponseDto_Es();
        responseDto.setESimProfileStatus("Available");

        doNothing().when(commonService).validateGetEsimProfileStatusRequest(requestDto);
        when(commonService.sendGetRequest(anyString(), any(), any(Class.class)))
                .thenReturn(new ResponseEntity<GetPrimaryInfoByIccidResponseDto_Es>(responseDto, HttpStatus.OK));

        GetEsimProfileStatusResponseDto result = transformerService.getEsimProfileStatus(requestDto);

        assertEquals("Available", result.getProfileStatus());
    }

    @Test
    void testGetEsimProfileStatus_EmptyEsimProfileStatus()
    {
        GetEsimProfileStatusRequestDto requestDto = new GetEsimProfileStatusRequestDto();
        GetPrimaryInfoByIccidResponseDto_Es responseDto = new GetPrimaryInfoByIccidResponseDto_Es();
        responseDto.setESimProfileStatus("");

        doNothing().when(commonService).validateGetEsimProfileStatusRequest(requestDto);
        when(commonService.sendGetRequest(anyString(), any(), any(Class.class)))
                .thenReturn(new ResponseEntity<GetPrimaryInfoByIccidResponseDto_Es>(responseDto, HttpStatus.OK));

        TransformerException exception = assertThrows(TransformerException.class,
                () -> transformerService.getEsimProfileStatus(requestDto));

        assertEquals(ErrorCodes.NO_STATUS_RETURNED.getCode(), exception.getErrorCode());
    }

    @Test
    void testGetEsimProfileStatus_HttpClientErrorException()
    {
        GetEsimProfileStatusRequestDto requestDto = new GetEsimProfileStatusRequestDto();
        GetPrimaryInfoByIccidResponseDto_Es responseDto = new GetPrimaryInfoByIccidResponseDto_Es();
        responseDto.setESimProfileStatus("Available");

        doNothing().when(commonService).validateGetEsimProfileStatusRequest(requestDto);
        when(commonService.sendGetRequest(anyString(), any(), any(Class.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        assertThrows(TransformerException.class, () -> {
            transformerService.getEsimProfileStatus(requestDto);
        });
    }

    @Test
    void testGetEsimProfileStatus_ExceptionThrown()
    {
        GetEsimProfileStatusRequestDto requestDto = new GetEsimProfileStatusRequestDto();

        doNothing().when(commonService).validateGetEsimProfileStatusRequest(requestDto);

        assertThrows(TransformerException.class, () -> {
            transformerService.getEsimProfileStatus(requestDto);
        });
    }

    @Test
    void testPrepareEsimProfile_Success() throws JSONException
    {
        PrepareEsimProfileRequestDto requestDto = new PrepareEsimProfileRequestDto();
        PrepareEsimProfileRequestDto requestDto_EID = new PrepareEsimProfileRequestDto();
        requestDto_EID.setEID("1234567");
        requestDto_EID.setReleaseFlag(false);
        JSONObject downloadOrderResponse = new JSONObject("{\r\n" + "    \"iccid\": \"8931100000000000000F\",\r\n"
                + "    \"header\": {\r\n" + "        \"functionExecutionStatus\": {\r\n"
                + "            \"status\": \"Executed-Success\",\r\n" + "            \"statusCodeData\": null\r\n"
                + "        }\r\n" + "    }\r\n" + "}");

        JSONObject confirmOrderResponse = new JSONObject("{\r\n" + "    \"eid\": null,\r\n"
                + "    \"matchingId\": null,\r\n" + "    \"smdpAddress\": null,\r\n" + "    \"header\": {\r\n"
                + "        \"functionExecutionStatus\": {\r\n" + "            \"status\": \"Executed-Success\",\r\n"
                + "            \"statusCodeData\": null\r\n" + "        }\r\n" + "    }\r\n" + "}");

        doNothing().when(commonService).validatePrepareEsimProfileRequest(requestDto);
        when(commonService.sendGetRequest(anyString(), any(), any(Class.class))).thenReturn(new ResponseEntity(
                "Tenant=Default:1;CMR-Postpaid:2;CMR-Prepaid:3;OB:4;DSDS-PALM-MOA:5", HttpStatus.OK));
        when(commonService.sendPostRequest(anyString(), any(HttpHeaders.class), anyString(), any(Class.class)))
                .thenReturn(new ResponseEntity(downloadOrderResponse, HttpStatus.OK))
                .thenReturn(new ResponseEntity(confirmOrderResponse, HttpStatus.OK))
                .thenReturn(new ResponseEntity(downloadOrderResponse, HttpStatus.OK))
                .thenReturn(new ResponseEntity(confirmOrderResponse, HttpStatus.OK));

        PrepareEsimProfileResponseDto result1 = transformerService.prepareEsimProfile(requestDto);
        assertNotNull(result1);

        PrepareEsimProfileResponseDto result2 = transformerService.prepareEsimProfile(requestDto_EID);
        assertNotNull(result2);
    }

    @Test
    void testPrepareEsimProfile_DownloadOrderFailure() throws JSONException
    {
        PrepareEsimProfileRequestDto requestDto = new PrepareEsimProfileRequestDto();
        JSONObject downloadOrderResponse = new JSONObject(
                "{\"iccid\": \"8961026023401154361F\",\"header\":{\"functionExecutionStatus\":{\"status\":"
                        + "\"Failed\",\"statusCodeData\":{\"subjectCode\":\"8.2.1\",\"reasonCode\":\"3.9\",\"subjectIdentifier\""
                        + ":\"\",\"message\":\"header\"}}}}");

        doNothing().when(commonService).validatePrepareEsimProfileRequest(requestDto);
        when(commonService.sendGetRequest(anyString(), any(), any(Class.class))).thenReturn(new ResponseEntity(
                "Tenant=Default:1;CMR-Postpaid:2;CMR-Prepaid:3;OB:4;DSDS-PALM-MOA:5", HttpStatus.OK));
        when(commonService.sendPostRequest(anyString(), any(HttpHeaders.class), anyString(), any(Class.class)))
                .thenReturn(new ResponseEntity(downloadOrderResponse, HttpStatus.OK));

        PrepareEsimProfileResponseDto result = transformerService.prepareEsimProfile(requestDto);

        assertEquals(ErrorCodes.PROFILE_ICCID_UNKNOWN.getCode(), result.getErrorCode());
    }

    @Test
    void testPrepareEsimProfile_DownloadOrderFailure_UnknownSubjectCode() throws JSONException
    {
        PrepareEsimProfileRequestDto requestDto = new PrepareEsimProfileRequestDto();
        JSONObject downloadOrderResponse = new JSONObject(
                "{\"iccid\": \"8961026023401154361F\",\"header\":{\"functionExecutionStatus\":{\"status\":"
                        + "\"Failed\",\"statusCodeData\":{\"subjectCode\":\"8.2.1.1.1\",\"reasonCode\":\"3.9\",\"subjectIdentifier\""
                        + ":\"\",\"message\":\"header\"}}}}");

        doNothing().when(commonService).validatePrepareEsimProfileRequest(requestDto);
        when(commonService.sendGetRequest(anyString(), any(), any(Class.class))).thenReturn(new ResponseEntity(
                "Tenant=Default:1;CMR-Postpaid:2;CMR-Prepaid:3;OB:4;DSDS-PALM-MOA:5", HttpStatus.OK));
        when(commonService.sendPostRequest(anyString(), any(HttpHeaders.class), anyString(), any(Class.class)))
                .thenReturn(new ResponseEntity(downloadOrderResponse, HttpStatus.OK));

        PrepareEsimProfileResponseDto result = transformerService.prepareEsimProfile(requestDto);

        assertEquals(ErrorCodes.GENERAL_ERROR.getCode(), result.getErrorCode());
    }

    @Test
    void testPrepareEsimProfile_DownloadOrderFailure_UnknownReasonCode() throws JSONException
    {
        PrepareEsimProfileRequestDto requestDto = new PrepareEsimProfileRequestDto();
        requestDto.setReleaseFlag(false);
        JSONObject downloadOrderResponse = new JSONObject(
                "{\"iccid\": \"8961026023401154361F\",\"header\":{\"functionExecutionStatus\":{\"status\":"
                        + "\"Failed\",\"statusCodeData\":{\"subjectCode\":\"8.2.1\",\"reasonCode\":\"3.9.6.2\",\"subjectIdentifier\""
                        + ":\"\",\"message\":\"header\"}}}}");

        doNothing().when(commonService).validatePrepareEsimProfileRequest(requestDto);
        when(commonService.sendGetRequest(anyString(), any(), any(Class.class))).thenReturn(new ResponseEntity(
                "Tenant=Default:1;CMR-Postpaid:2;CMR-Prepaid:3;OB:4;DSDS-PALM-MOA:5", HttpStatus.OK));
        when(commonService.sendPostRequest(anyString(), any(HttpHeaders.class), anyString(), any(Class.class)))
                .thenReturn(new ResponseEntity(downloadOrderResponse, HttpStatus.OK));

        PrepareEsimProfileResponseDto result = transformerService.prepareEsimProfile(requestDto);

        assertEquals(ErrorCodes.GENERAL_ERROR.getCode(), result.getErrorCode());
    }

    @Test
    void testPrepareEsimProfile_DownloadOrderCallFailure()
    {
        PrepareEsimProfileRequestDto requestDto = new PrepareEsimProfileRequestDto();

        doNothing().when(commonService).validatePrepareEsimProfileRequest(requestDto);
        when(commonService.sendGetRequest(anyString(), any(), any(Class.class))).thenReturn(new ResponseEntity(
                "Tenant=Default:1;CMR-Postpaid:2;CMR-Prepaid:3;OB:4;DSDS-PALM-MOA:5", HttpStatus.OK));
        when(commonService.sendPostRequest(anyString(), any(HttpHeaders.class), anyString(), any(Class.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        TransformerException exception = assertThrows(TransformerException.class,
                () -> transformerService.prepareEsimProfile(requestDto));

        assertEquals(ErrorCodes.UPSTREAM_ERROR.getCode(), exception.getErrorCode());
    }

    @Test
    void testPrepareEsimProfile_DownloadOrderCallFailure_Ex()
    {
        PrepareEsimProfileRequestDto requestDto = new PrepareEsimProfileRequestDto();

        doNothing().when(commonService).validatePrepareEsimProfileRequest(requestDto);
        when(commonService.sendGetRequest(anyString(), any(), any(Class.class))).thenReturn(new ResponseEntity(
                "Tenant=Default:1;CMR-Postpaid:2;CMR-Prepaid:3;OB:4;DSDS-PALM-MOA:5", HttpStatus.OK));
        when(commonService.sendPostRequest(anyString(), any(HttpHeaders.class), anyString(), any(Class.class)))
                .thenThrow(new RuntimeException());

        TransformerException exception = assertThrows(TransformerException.class,
                () -> transformerService.prepareEsimProfile(requestDto));

        assertEquals(ErrorCodes.UPSTREAM_TIMEOUT.getCode(), exception.getErrorCode());
    }

    @Test
    void testPrepareEsimProfile_ConfirmOrderFailure() throws JSONException
    {
        PrepareEsimProfileRequestDto requestDto = new PrepareEsimProfileRequestDto();

        JSONObject downloadOrderResponse = new JSONObject("{\r\n" + "    \"iccid\": \"8931100000000000000F\",\r\n"
                + "    \"header\": {\r\n" + "        \"functionExecutionStatus\": {\r\n"
                + "            \"status\": \"Executed-Success\",\r\n" + "            \"statusCodeData\": null\r\n"
                + "        }\r\n" + "    }\r\n" + "}");

        JSONObject confirmOrderResponse = new JSONObject("{\r\n" + "    \"eid\": null,\r\n"
                + "    \"matchingId\": null,\r\n" + "    \"smdpAddress\": null,\r\n" + "    \"header\": {\r\n"
                + "        \"functionExecutionStatus\": {\r\n" + "            \"status\": \"Failed\",\r\n"
                + "            \"statusCodeData\": {\"subjectCode\":\"8.2.1\",\"reasonCode\":\"3.9\", "
                + "\"subjectIdentifier\":\"\", \"message\":\"\"}\r\n}\r\n" + "    }\r\n" + "}");

        doNothing().when(commonService).validatePrepareEsimProfileRequest(requestDto);
        when(commonService.sendGetRequest(anyString(), any(), any(Class.class))).thenReturn(new ResponseEntity(
                "Tenant=Default:1;CMR-Postpaid:2;CMR-Prepaid:3;OB:4;DSDS-PALM-MOA:5", HttpStatus.OK));
        when(commonService.sendPostRequest(anyString(), any(HttpHeaders.class), anyString(), any(Class.class)))
                .thenReturn(new ResponseEntity(downloadOrderResponse, HttpStatus.OK))
                .thenReturn(new ResponseEntity(confirmOrderResponse, HttpStatus.OK));

        PrepareEsimProfileResponseDto result = transformerService.prepareEsimProfile(requestDto);
        assertEquals(ErrorCodes.PROFILE_ICCID_UNKNOWN.getCode(), result.getErrorCode());
    }

    @Test
    void testPrepareEsimProfile_ConfirmOrderFailure_UnknownSubjectCode() throws JSONException
    {
        PrepareEsimProfileRequestDto requestDto = new PrepareEsimProfileRequestDto();

        JSONObject downloadOrderResponse = new JSONObject("{\r\n" + "    \"iccid\": \"8931100000000000000F\",\r\n"
                + "    \"header\": {\r\n" + "        \"functionExecutionStatus\": {\r\n"
                + "            \"status\": \"Executed-Success\",\r\n" + "            \"statusCodeData\": null\r\n"
                + "        }\r\n" + "    }\r\n" + "}");

        JSONObject confirmOrderResponse = new JSONObject("{\r\n" + "    \"eid\": null,\r\n"
                + "    \"matchingId\": null,\r\n" + "    \"smdpAddress\": null,\r\n" + "    \"header\": {\r\n"
                + "        \"functionExecutionStatus\": {\r\n" + "            \"status\": \"Failed\",\r\n"
                + "            \"statusCodeData\": {\"subjectCode\":\"8.2.1.1.1\",\"reasonCode\":\"3.9\", "
                + "\"subjectIdentifier\":\"\", \"message\":\"\"}\r\n}\r\n" + "    }\r\n" + "}");

        doNothing().when(commonService).validatePrepareEsimProfileRequest(requestDto);
        when(commonService.sendGetRequest(anyString(), any(), any(Class.class))).thenReturn(new ResponseEntity(
                "Tenant=Default:1;CMR-Postpaid:2;CMR-Prepaid:3;OB:4;DSDS-PALM-MOA:5", HttpStatus.OK));
        when(commonService.sendPostRequest(anyString(), any(HttpHeaders.class), anyString(), any(Class.class)))
                .thenReturn(new ResponseEntity(downloadOrderResponse, HttpStatus.OK))
                .thenReturn(new ResponseEntity(confirmOrderResponse, HttpStatus.OK));

        PrepareEsimProfileResponseDto result = transformerService.prepareEsimProfile(requestDto);
        assertEquals(ErrorCodes.GENERAL_ERROR.getCode(), result.getErrorCode());
    }

    @Test
    void testPrepareEsimProfile_ConfirmOrderFailure_UnknownReasonCode() throws JSONException
    {
        PrepareEsimProfileRequestDto requestDto = new PrepareEsimProfileRequestDto();

        JSONObject downloadOrderResponse = new JSONObject("{\r\n" + "    \"iccid\": \"8931100000000000000F\",\r\n"
                + "    \"header\": {\r\n" + "        \"functionExecutionStatus\": {\r\n"
                + "            \"status\": \"Executed-Success\",\r\n" + "            \"statusCodeData\": null\r\n"
                + "        }\r\n" + "    }\r\n" + "}");

        JSONObject confirmOrderResponse = new JSONObject("{\r\n" + "    \"eid\": null,\r\n"
                + "    \"matchingId\": null,\r\n" + "    \"smdpAddress\": null,\r\n" + "    \"header\": {\r\n"
                + "        \"functionExecutionStatus\": {\r\n" + "            \"status\": \"Failed\",\r\n"
                + "            \"statusCodeData\": {\"subjectCode\":\"8.2.1\",\"reasonCode\":\"3.9.1.1\", "
                + "\"subjectIdentifier\":\"\", \"message\":\"\"}\r\n}\r\n" + "    }\r\n" + "}");

        doNothing().when(commonService).validatePrepareEsimProfileRequest(requestDto);
        when(commonService.sendGetRequest(anyString(), any(), any(Class.class))).thenReturn(new ResponseEntity(
                "Tenant=Default:1;CMR-Postpaid:2;CMR-Prepaid:3;OB:4;DSDS-PALM-MOA:5", HttpStatus.OK));
        when(commonService.sendPostRequest(anyString(), any(HttpHeaders.class), anyString(), any(Class.class)))
                .thenReturn(new ResponseEntity(downloadOrderResponse, HttpStatus.OK))
                .thenReturn(new ResponseEntity(confirmOrderResponse, HttpStatus.OK));

        PrepareEsimProfileResponseDto result = transformerService.prepareEsimProfile(requestDto);
        assertEquals(ErrorCodes.GENERAL_ERROR.getCode(), result.getErrorCode());
    }

    @Test
    void testPrepareEsimProfile_ConfirmOrderCallFailure() throws JSONException
    {
        PrepareEsimProfileRequestDto requestDto = new PrepareEsimProfileRequestDto();

        JSONObject downloadOrderResponse = new JSONObject("{\r\n" + "    \"iccid\": \"8931100000000000000F\",\r\n"
                + "    \"header\": {\r\n" + "        \"functionExecutionStatus\": {\r\n"
                + "            \"status\": \"Executed-Success\",\r\n" + "            \"statusCodeData\": null\r\n"
                + "        }\r\n" + "    }\r\n" + "}");

        doNothing().when(commonService).validatePrepareEsimProfileRequest(requestDto);
        when(commonService.sendGetRequest(anyString(), any(), any(Class.class))).thenReturn(new ResponseEntity(
                "Tenant=Default:1;CMR-Postpaid:2;CMR-Prepaid:3;OB:4;DSDS-PALM-MOA:5", HttpStatus.OK));
        when(commonService.sendPostRequest(anyString(), any(HttpHeaders.class), anyString(), any(Class.class)))
                .thenReturn(new ResponseEntity(downloadOrderResponse, HttpStatus.OK))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        TransformerException exception = assertThrows(TransformerException.class,
                () -> transformerService.prepareEsimProfile(requestDto));

        assertEquals(ErrorCodes.UPSTREAM_ERROR.getCode(), exception.getErrorCode());
    }

    @Test
    void testPrepareEsimProfile_ConfirmOrderCallFailure_Ex() throws JSONException
    {
        PrepareEsimProfileRequestDto requestDto = new PrepareEsimProfileRequestDto();

        JSONObject downloadOrderResponse = new JSONObject("{\r\n" + "    \"iccid\": \"8931100000000000000F\",\r\n"
                + "    \"header\": {\r\n" + "        \"functionExecutionStatus\": {\r\n"
                + "            \"status\": \"Executed-Success\",\r\n" + "            \"statusCodeData\": null\r\n"
                + "        }\r\n" + "    }\r\n" + "}");

        doNothing().when(commonService).validatePrepareEsimProfileRequest(requestDto);
        when(commonService.sendGetRequest(anyString(), any(), any(Class.class))).thenReturn(new ResponseEntity(
                "Tenant=Default:1;CMR-Postpaid:2;CMR-Prepaid:3;OB:4;DSDS-PALM-MOA:5", HttpStatus.OK));
        when(commonService.sendPostRequest(anyString(), any(HttpHeaders.class), anyString(), any(Class.class)))
                .thenReturn(new ResponseEntity(downloadOrderResponse, HttpStatus.OK)).thenThrow(new RuntimeException());

        TransformerException exception = assertThrows(TransformerException.class,
                () -> transformerService.prepareEsimProfile(requestDto));

        assertEquals(ErrorCodes.UPSTREAM_TIMEOUT.getCode(), exception.getErrorCode());
    }

    @Test
    void testChangeEsimProfileStatus_Success() throws JSONException
    {
        ChangeEsimProfileStatusRequestDto requestDto = new ChangeEsimProfileStatusRequestDto();
        GetPrimaryInfoByIccidResponseDto_Es res = new GetPrimaryInfoByIccidResponseDto_Es();
        JSONObject cancelOrderResponse = new JSONObject(
                "{\"header\": {\"functionExecutionStatus\": {\"status\": \"Executed-Success\", "
                        + "\"statusCodeData\": {\"subjectCode\": \"8.2.1\", \"reasonCode\": \"3.10\", \"subjectIdentifier\": "
                        + "\"ProfileICCID\", \"message\": \"Invalid Association\"}}}, \"error_message\": \"Invalid Association\", "
                        + "\"error_code\": 10061}}");

        doNothing().when(commonService).validateChangeEsimProfileStatus(requestDto);
        when(commonService.sendGetRequest(anyString(), any(), any(Class.class)))
                .thenReturn(new ResponseEntity(res, HttpStatus.OK));
        when(commonService.sendPostRequest(anyString(), any(HttpHeaders.class), anyString(), any(Class.class)))
                .thenReturn(new ResponseEntity(cancelOrderResponse, HttpStatus.OK));

        assertNotNull(transformerService.changeEsimProfileStatus(requestDto));
    }

    @Test
    void testChangeEsimProfileStatus_CancelOrderFailure() throws JSONException
    {
        ChangeEsimProfileStatusRequestDto requestDto = new ChangeEsimProfileStatusRequestDto();
        GetPrimaryInfoByIccidResponseDto_Es res = new GetPrimaryInfoByIccidResponseDto_Es();
        JSONObject cancelOrderResponse = new JSONObject(
                "{\"header\": {\"functionExecutionStatus\": {\"status\": \"Failed\", "
                        + "\"statusCodeData\": {\"subjectCode\": \"8.2.1\", \"reasonCode\": \"3.9\", \"subjectIdentifier\": "
                        + "\"ProfileICCID\", \"message\": \"Invalid Association\"}}}, \"error_message\": \"Invalid Association\", "
                        + "\"error_code\": 10061}}");

        doNothing().when(commonService).validateChangeEsimProfileStatus(requestDto);
        when(commonService.sendGetRequest(anyString(), any(), any(Class.class)))
                .thenReturn(new ResponseEntity(res, HttpStatus.OK));
        when(commonService.sendPostRequest(anyString(), any(HttpHeaders.class), anyString(), any(Class.class)))
                .thenReturn(new ResponseEntity(cancelOrderResponse, HttpStatus.OK));

        ChangeEsimProfileStatusResponseDto response = transformerService.changeEsimProfileStatus(requestDto);
        assertEquals(ErrorCodes.PROFILE_ICCID_UNKNOWN.getCode(), response.getErrorCode());
    }

    @Test
    void testChangeEsimProfileStatus_CancelOrderFailure_UnknownSubjectCode() throws JSONException
    {
        ChangeEsimProfileStatusRequestDto requestDto = new ChangeEsimProfileStatusRequestDto();
        GetPrimaryInfoByIccidResponseDto_Es res = new GetPrimaryInfoByIccidResponseDto_Es();
        JSONObject cancelOrderResponse = new JSONObject(
                "{\"header\": {\"functionExecutionStatus\": {\"status\": \"Failed\", "
                        + "\"statusCodeData\": {\"subjectCode\": \"8.2.1.1\", \"reasonCode\": \"3.9\", \"subjectIdentifier\": "
                        + "\"ProfileICCID\", \"message\": \"Invalid Association\"}}}, \"error_message\": \"Invalid Association\", "
                        + "\"error_code\": 10061}}");

        doNothing().when(commonService).validateChangeEsimProfileStatus(requestDto);
        when(commonService.sendGetRequest(anyString(), any(), any(Class.class)))
                .thenReturn(new ResponseEntity(res, HttpStatus.OK));
        when(commonService.sendPostRequest(anyString(), any(HttpHeaders.class), anyString(), any(Class.class)))
                .thenReturn(new ResponseEntity(cancelOrderResponse, HttpStatus.OK));

        ChangeEsimProfileStatusResponseDto response = transformerService.changeEsimProfileStatus(requestDto);
        assertEquals(ErrorCodes.GENERAL_ERROR.getCode(), response.getErrorCode());
    }

    @Test
    void testChangeEsimProfileStatus_CancelOrderFailure_UnknownReasonCode() throws JSONException
    {
        ChangeEsimProfileStatusRequestDto requestDto = new ChangeEsimProfileStatusRequestDto();
        GetPrimaryInfoByIccidResponseDto_Es res = new GetPrimaryInfoByIccidResponseDto_Es();
        JSONObject cancelOrderResponse = new JSONObject(
                "{\"header\": {\"functionExecutionStatus\": {\"status\": \"Failed\", "
                        + "\"statusCodeData\": {\"subjectCode\": \"8.2.1\", \"reasonCode\": \"3.9.1\", \"subjectIdentifier\": "
                        + "\"ProfileICCID\", \"message\": \"Invalid Association\"}}}, \"error_message\": \"Invalid Association\", "
                        + "\"error_code\": 10061}}");

        doNothing().when(commonService).validateChangeEsimProfileStatus(requestDto);
        when(commonService.sendGetRequest(anyString(), any(), any(Class.class)))
                .thenReturn(new ResponseEntity(res, HttpStatus.OK));
        when(commonService.sendPostRequest(anyString(), any(HttpHeaders.class), anyString(), any(Class.class)))
                .thenReturn(new ResponseEntity(cancelOrderResponse, HttpStatus.OK));

        ChangeEsimProfileStatusResponseDto response = transformerService.changeEsimProfileStatus(requestDto);
        assertEquals(ErrorCodes.GENERAL_ERROR.getCode(), response.getErrorCode());
    }

    @Test
    void testChangeEsimProfileStatus_CancelOrderCallFailure()
    {
        ChangeEsimProfileStatusRequestDto requestDto = new ChangeEsimProfileStatusRequestDto();
        GetPrimaryInfoByIccidResponseDto_Es res = new GetPrimaryInfoByIccidResponseDto_Es();

        doNothing().when(commonService).validateChangeEsimProfileStatus(requestDto);
        when(commonService.sendGetRequest(anyString(), any(), any(Class.class)))
                .thenReturn(new ResponseEntity(res, HttpStatus.OK));
        when(commonService.sendPostRequest(anyString(), any(HttpHeaders.class), anyString(), any(Class.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        TransformerException exception = assertThrows(TransformerException.class,
                () -> transformerService.changeEsimProfileStatus(requestDto));
        assertEquals(ErrorCodes.UPSTREAM_ERROR.getCode(), exception.getErrorCode());
    }

    @Test
    void testChangeEsimProfileStatus_CancelOrderCallFailure_Ex()
    {
        ChangeEsimProfileStatusRequestDto requestDto = new ChangeEsimProfileStatusRequestDto();
        GetPrimaryInfoByIccidResponseDto_Es res = new GetPrimaryInfoByIccidResponseDto_Es();

        doNothing().when(commonService).validateChangeEsimProfileStatus(requestDto);
        when(commonService.sendGetRequest(anyString(), any(), any(Class.class)))
                .thenReturn(new ResponseEntity(res, HttpStatus.OK));
        when(commonService.sendPostRequest(anyString(), any(HttpHeaders.class), anyString(), any(Class.class)))
                .thenThrow(new RuntimeException());

        TransformerException exception = assertThrows(TransformerException.class,
                () -> transformerService.changeEsimProfileStatus(requestDto));
        assertEquals(ErrorCodes.UPSTREAM_TIMEOUT.getCode(), exception.getErrorCode());
    }

    @Test
    void testUnlinkAndReserveProfile_Success() throws JSONException
    {
        UnlinkAndReserveProfileRequestDto requestDto = new UnlinkAndReserveProfileRequestDto();
        requestDto.setEid("1234");
        JSONObject cancelOrderResponse = new JSONObject(
                "{\"header\": {\"functionExecutionStatus\": {\"status\": \"Executed-Success\", "
                        + "\"statusCodeData\": {\"subjectCode\": \"8.2.1\", \"reasonCode\": \"3.9\", \"subjectIdentifier\": "
                        + "\"ProfileICCID\", \"message\": \"Invalid Association\"}}}, \"error_message\": \"Invalid Association\", "
                        + "\"error_code\": 10061}}");
        JSONObject downloadOrderResponse = new JSONObject("{\r\n" + "    \"iccid\": \"8931100000000000000F\",\r\n"
                + "    \"header\": {\r\n" + "        \"functionExecutionStatus\": {\r\n"
                + "            \"status\": \"Executed-Success\",\r\n" + "            \"statusCodeData\": null\r\n"
                + "        }\r\n" + "    }\r\n" + "}");

        JSONObject confirmOrderResponse = new JSONObject("{\r\n" + "    \"eid\": null,\r\n"
                + "    \"matchingId\": null,\r\n" + "    \"smdpAddress\": null,\r\n" + "    \"header\": {\r\n"
                + "        \"functionExecutionStatus\": {\r\n" + "            \"status\": \"Executed-Success\",\r\n"
                + "            \"statusCodeData\": null\r\n" + "        }\r\n" + "    }\r\n" + "}");

        doNothing().when(commonService).validateUnlinkAndReserveProfile(requestDto);
        when(commonService.sendGetRequest(anyString(), any(), any(Class.class))).thenReturn(new ResponseEntity(
                "Tenant=Default:1;CMR-Postpaid:2;CMR-Prepaid:3;OB:4;DSDS-PALM-MOA:5", HttpStatus.OK));
        when(commonService.sendPostRequest(anyString(), any(HttpHeaders.class), anyString(), any(Class.class)))
                .thenReturn(new ResponseEntity(cancelOrderResponse, HttpStatus.OK))
                .thenReturn(new ResponseEntity(downloadOrderResponse, HttpStatus.OK))
                .thenReturn(new ResponseEntity(confirmOrderResponse, HttpStatus.OK));

        assertNotNull(transformerService.unlinkAndReserveProfile(requestDto));
    }

    @Test
    void testUnlinkAndReserveProfile_CancelOrderFailure() throws JSONException
    {
        UnlinkAndReserveProfileRequestDto requestDto = new UnlinkAndReserveProfileRequestDto();
        requestDto.setEid("1234");
        JSONObject cancelOrderResponse = new JSONObject(
                "{\"header\": {\"functionExecutionStatus\": {\"status\": \"Failed\", "
                        + "\"statusCodeData\": {\"subjectCode\": \"8.2.1\", \"reasonCode\": \"3.9\", \"subjectIdentifier\": "
                        + "\"ProfileICCID\", \"message\": \"Invalid Association\"}}}, \"error_message\": \"Invalid Association\", "
                        + "\"error_code\": 10061}}");

        doNothing().when(commonService).validateUnlinkAndReserveProfile(requestDto);
        when(commonService.sendPostRequest(anyString(), any(HttpHeaders.class), anyString(), any(Class.class)))
                .thenReturn(new ResponseEntity(cancelOrderResponse, HttpStatus.OK));

        UnlinkAndReserveProfileResponseDto response = transformerService.unlinkAndReserveProfile(requestDto);
        assertEquals(ErrorCodes.PROFILE_ICCID_UNKNOWN.getCode(), response.getErrorCode());
    }

    @Test
    void testUnlinkAndReserveProfile_CancelOrderFailure_UnknownSubjectCode() throws JSONException
    {
        UnlinkAndReserveProfileRequestDto requestDto = new UnlinkAndReserveProfileRequestDto();
        requestDto.setEid("1234");
        JSONObject cancelOrderResponse = new JSONObject(
                "{\"header\": {\"functionExecutionStatus\": {\"status\": \"Failed\", "
                        + "\"statusCodeData\": {\"subjectCode\": \"8.2.1.1\", \"reasonCode\": \"3.9\", \"subjectIdentifier\": "
                        + "\"ProfileICCID\", \"message\": \"Invalid Association\"}}}, \"error_message\": \"Invalid Association\", "
                        + "\"error_code\": 10061}}");

        doNothing().when(commonService).validateUnlinkAndReserveProfile(requestDto);
        when(commonService.sendPostRequest(anyString(), any(HttpHeaders.class), anyString(), any(Class.class)))
                .thenReturn(new ResponseEntity(cancelOrderResponse, HttpStatus.OK));

        UnlinkAndReserveProfileResponseDto response = transformerService.unlinkAndReserveProfile(requestDto);
        assertEquals(ErrorCodes.GENERAL_ERROR.getCode(), response.getErrorCode());
    }

    @Test
    void testUnlinkAndReserveProfile_CancelOrderFailure_UnknownReasonCode() throws JSONException
    {
        UnlinkAndReserveProfileRequestDto requestDto = new UnlinkAndReserveProfileRequestDto();
        requestDto.setEid("1234");
        JSONObject cancelOrderResponse = new JSONObject(
                "{\"header\": {\"functionExecutionStatus\": {\"status\": \"Failed\", "
                        + "\"statusCodeData\": {\"subjectCode\": \"8.2.1\", \"reasonCode\": \"3.9.1\", \"subjectIdentifier\": "
                        + "\"ProfileICCID\", \"message\": \"Invalid Association\"}}}, \"error_message\": \"Invalid Association\", "
                        + "\"error_code\": 10061}}");

        doNothing().when(commonService).validateUnlinkAndReserveProfile(requestDto);
        when(commonService.sendPostRequest(anyString(), any(HttpHeaders.class), anyString(), any(Class.class)))
                .thenReturn(new ResponseEntity(cancelOrderResponse, HttpStatus.OK));

        UnlinkAndReserveProfileResponseDto response = transformerService.unlinkAndReserveProfile(requestDto);
        assertEquals(ErrorCodes.GENERAL_ERROR.getCode(), response.getErrorCode());
    }

    @Test
    void testUnlinkAndReserveProfile_CancelOrderCallFailure() throws JSONException
    {
        UnlinkAndReserveProfileRequestDto requestDto = new UnlinkAndReserveProfileRequestDto();
        requestDto.setEid("1234");

        doNothing().when(commonService).validateUnlinkAndReserveProfile(requestDto);
        when(commonService.sendPostRequest(anyString(), any(HttpHeaders.class), anyString(), any(Class.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        TransformerException exception = assertThrows(TransformerException.class,
                () -> transformerService.unlinkAndReserveProfile(requestDto));
        assertEquals(ErrorCodes.UPSTREAM_ERROR.getCode(), exception.getErrorCode());
    }

    @Test
    void testUnlinkAndReserveProfile_CancelOrderCallFailure_Ex() throws JSONException
    {
        UnlinkAndReserveProfileRequestDto requestDto = new UnlinkAndReserveProfileRequestDto();
        requestDto.setEid("1234");

        doNothing().when(commonService).validateUnlinkAndReserveProfile(requestDto);
        when(commonService.sendPostRequest(anyString(), any(HttpHeaders.class), anyString(), any(Class.class)))
                .thenThrow(new RuntimeException());

        TransformerException exception = assertThrows(TransformerException.class,
                () -> transformerService.unlinkAndReserveProfile(requestDto));
        assertEquals(ErrorCodes.UPSTREAM_TIMEOUT.getCode(), exception.getErrorCode());
    }

    @Test
    void testSendGetPrimaryInfoByIccidRequest_Ex()
    {
        when(commonService.sendGetRequest(anyString(), any(), any(Class.class))).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
        
        TransformerException exception1 = assertThrows(TransformerException.class, () -> transformerService.sendGetPrimaryInfoByIccidRequest(new Object(), "123"));
        assertEquals(ErrorCodes.UPSTREAM_ERROR.getCode(), exception1.getErrorCode());
        
        when(commonService.sendGetRequest(anyString(), any(), any(Class.class))).thenThrow(new RuntimeException());
        
        TransformerException exception2 = assertThrows(TransformerException.class, () -> transformerService.sendGetPrimaryInfoByIccidRequest(new Object(), "123"));
        assertEquals(ErrorCodes.UPSTREAM_TIMEOUT.getCode(), exception2.getErrorCode());
    }

    @Test
    void testPrepareAndSendAddOrUpdatePrimaryInfo()
    {
        assertNull(transformerService.prepareAndSendAddOrUpdatePrimaryInfo(null, null, null, null, "12345"));
        assertNull(transformerService.prepareAndSendAddOrUpdatePrimaryInfo("", "", "", "", ""));
    }

    @Test
    void testCheckSmdpResponse_Success() throws JSONException
    {
        assertEquals(false, transformerService.checkSmdpResponse(null));

        JSONObject json1 = new JSONObject("{\"test\":\"value\"}");
        assertEquals(false, transformerService.checkSmdpResponse(json1));
    }

}
