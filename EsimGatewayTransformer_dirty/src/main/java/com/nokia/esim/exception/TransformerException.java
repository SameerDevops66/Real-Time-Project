package com.nokia.esim.exception;

public class TransformerException extends RuntimeException
{

    private static final long serialVersionUID = 1L;

    private final String errorCode;

    private final String errorMessage;

    public TransformerException(String errorCode, String errorMessage, String message)
    {
        super(message);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public String getErrorCode()
    {
        return errorCode;
    }

    public String getErrorMessage()
    {
        return errorMessage;
    }

}
