package com.nokia.esim.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetPrimaryInfoByIccidResponseDto_Es
{

    @JsonProperty(value = "id")
    private String id;

    @JsonProperty(value = "imei")
    private String imei;

    @JsonProperty(value = "imsi")
    private String imsi;

    @JsonProperty(value = "msisdn")
    private String msisdn;

    @JsonProperty(value = "iccid")
    private String iccid;

    @JsonProperty(value = "eid")
    private String eid;

    @JsonProperty(value = "meid")
    private String meid;

    @JsonProperty(value = "deviceType")
    private String deviceType;

    @JsonProperty(value = "esimProfileStatus")
    private String eSimProfileStatus;

    @JsonProperty(value = "networkProfileStatus")
    private String networkProfileStatus;

    @JsonProperty(value = "profileType")
    private String profileType;

    @JsonProperty(value = "tenantId")
    private String tenantId;

    @JsonProperty(value = "inUse")
    private String inUse;

    @JsonProperty(value = "inserted")
    private String inserted;

    @JsonProperty(value = "updated")
    private String updated;

}
