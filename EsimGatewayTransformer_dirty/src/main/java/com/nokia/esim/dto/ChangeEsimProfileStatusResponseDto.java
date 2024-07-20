package com.nokia.esim.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nokia.esim.config.ErrorCodes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ChangeEsimProfileStatusResponseDto
{

    @JsonProperty(value = "response_id")
    private String responseId;

    @JsonProperty(value = "iccid")
    private String iccid;

    @JsonProperty(value = "error_code")
    private String errorCode = ErrorCodes.SUCCESS.getCode();

    @JsonProperty(value = "error_message")
    private String errorMessage = ErrorCodes.SUCCESS.getMessage();

}
