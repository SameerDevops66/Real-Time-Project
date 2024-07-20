package com.nokia.esim.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

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
public class UnlinkAndReserveProfileRequestDto
{

    @JsonProperty(value = "request_id")
    private String requestId;

    @JsonProperty(value = "source_system")
    private String sourceSystem;

    @JsonProperty(value = "iccid")
    private String iccid;

    @JsonProperty(value = "smdp_name")
    private String smdpName;

    @JsonProperty(value = "msisdn")
    private String msisdn;

    @JsonProperty(value = "eid")
    private String eid;

    @JsonProperty(value = "order_id")
    private String orderId;

    @JsonProperty(value = "release_flag")
    private boolean releaseFlag = true;

}
