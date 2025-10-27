package com.example.shopverse_customer_app.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Model class for API error responses
 * Standard error structure: {"code": xxx, "error": "xxxx", "msg": "..."}
 */
public class ApiError {

    @SerializedName("code")
    private Integer code;

    @SerializedName("error")
    private String error;

    @SerializedName("msg")
    private String msg;

    @SerializedName("message")
    private String message; // Some APIs use "message" instead of "msg"

    public ApiError() {
    }

    public ApiError(Integer code, String error, String msg) {
        this.code = code;
        this.error = error;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Get user-friendly error message
     * Returns msg field if available, otherwise message, otherwise error
     */
    public String getUserMessage() {
        if (msg != null && !msg.isEmpty()) {
            return msg;
        }
        if (message != null && !message.isEmpty()) {
            return message;
        }
        if (error != null && !error.isEmpty()) {
            return error;
        }
        return "Unknown error occurred";
    }

    @Override
    public String toString() {
        return "ApiError{" +
                "code=" + code +
                ", error='" + error + '\'' +
                ", msg='" + msg + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
