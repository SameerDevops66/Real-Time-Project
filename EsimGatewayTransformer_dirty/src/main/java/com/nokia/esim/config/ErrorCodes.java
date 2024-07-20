package com.nokia.esim.config;

public enum ErrorCodes
{
    SUCCESS("0", "Success"),
    PROFILE_ICCID_UNKNOWN("10000", "Profile ICCID - Unknown"),
    PROFILE_ICCID_NOT_ALLOWED("10001", "Profile ICCID - Not Allowed (Authorisation)"),
    PROFILE_ICCID_ALREADY_IN_USE("10002", "Profile ICCID – Already in use"),
    PROFILE_ICCID_MISSING("10003", "Profile ICCID – Mandatory Element Missing"),
    PROFILE_TYPE_UNKNOWN("10004", "Profile Type – Unknown"),
    PROFILE_TYPE_NOT_ALLOWED("10005", "Profile Type - Not Allowed (Authorisation)"),
    PROFILE_TYPE_REFUSED("10006", "Profile Type - Refused"),
    PROFILE_TYPE_UNAVAILABLE("10007", "Profile Type - Unavailable"),
    EID_NOT_ALLOWED("10008", "EID - Not Allowed (Authorisation)"),
    EID_INVALID("10009", "EID - Invalid"),
    EID_INVALID_ASSOCIATION("10010", "EID – Invalid Association"),
    EID_MISSING("10011", "EID - Mandatory Element Missing"),
    CONFIRMATION_CODE_REFUSED("10012", "ConfirmationCode - Refused"),
    MATCHING_ID_REFUSED("10013", "Matching ID – Refused"),
    EVENT_RECORD_ALREADY_IN_USE("10014", "Event Record – Already in use"),
    FUNCTION_EXECUTION_ERROR("10015", "Function - Execution error"),
    FUNCTION_MANDATORY_PARAMETER_MISSING("10016", "Function - Mandatory Parameter missing"),
    SMDP_ADDRESS_UNKNOWN("10017", "SM-DP+ Address - Unknown"),
    SMDS_INACCESSIBLE("10018", "SMDS - Inaccessible"),
    SMDS_EXECUTION_ERROR("10019", "SMDS – Execution Error"),
    NO_JSON("10051", "No JSON specified"),
    ERROR_SCHEMA_VALIDATION("10052", "Error with schema validation check"),
    JSON_SCHEMA_ERROR("10053", "JSON does not match schema"),
    UPSTREAM_TIMEOUT("10055", "Upstream request timed out"),
    UPSTREAM_ERROR("10057", "Other error with upstream server"),
    NO_STATUS_RETURNED("10060", "No status returned or unexpected status supplied"),
    ES_ERROR("10063", "Error from entitlement server"),
    ICCID_MISSING("10066", "Iccid value not specified"),
    GENERAL_ERROR("10100", "General Error");

    private final String code;
    private final String message;

    ErrorCodes(String code, String message)
    {
        this.code = code;
        this.message = message;
    }

    public String getCode()
    {
        return code;
    }

    public String getMessage()
    {
        return message;
    }
}
