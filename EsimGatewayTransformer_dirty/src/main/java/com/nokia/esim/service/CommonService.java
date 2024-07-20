package com.nokia.esim.service;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import com.nokia.esim.dto.ChangeEsimProfileStatusRequestDto;
import com.nokia.esim.dto.GetEsimProfileStatusRequestDto;
import com.nokia.esim.dto.PrepareEsimProfileRequestDto;
import com.nokia.esim.dto.UnlinkAndReserveProfileRequestDto;

public interface CommonService
{

    public <T> ResponseEntity<T> sendGetRequest(String url, HttpHeaders headers, Class<T> responseType);

    public <T, R> ResponseEntity<R> sendPostRequest(String url, HttpHeaders headers, T requestBody,
            Class<R> responseType);

    public <T, R> ResponseEntity<R> sendPutRequest(String url, HttpHeaders headers, T requestBody,
            Class<R> responseType);

    public void validateGetEsimProfileStatusRequest(GetEsimProfileStatusRequestDto esimProfileStatusRequestDto);

    public void validatePrepareEsimProfileRequest(PrepareEsimProfileRequestDto prepareEsimProfileRequestDto);

    public void validateChangeEsimProfileStatus(ChangeEsimProfileStatusRequestDto changeEsimProfileStatusRequestDto);

    public void validateUnlinkAndReserveProfile(UnlinkAndReserveProfileRequestDto unlinkAndReserveProfileRequestDto);

    public String generateBasicAuthHeader(String username, String password);

    public boolean verifyCredentials(String authorizationHeader);

}
