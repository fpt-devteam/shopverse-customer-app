package com.example.shopverse_customer_app.utils;

import android.util.Log;

import com.example.shopverse_customer_app.data.model.ApiError;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;

import retrofit2.Response;

/**
 * Utility class for parsing API error responses
 * Extracts user-friendly error messages from API error responses
 */
public class ErrorParser {

    private static final String TAG = "ErrorParser";
    private static final Gson gson = new Gson();

    /**
     * Parse error response and return user-friendly message
     * Extracts the "msg" field from error JSON
     *
     * @param response The failed HTTP response
     * @return User-friendly error message
     */
    public static String parseError(Response<?> response) {
        if (response == null) {
            return "Unknown error occurred";
        }

        try {
            if (response.errorBody() != null) {
                String errorBody = response.errorBody().string();
                Log.d(TAG, "Raw error body: " + errorBody);

                // Try to parse as ApiError
                try {
                    ApiError apiError = gson.fromJson(errorBody, ApiError.class);
                    if (apiError != null) {
                        String userMessage = apiError.getUserMessage();
                        Log.d(TAG, "Parsed error message: " + userMessage);
                        return userMessage;
                    }
                } catch (JsonSyntaxException e) {
                    Log.w(TAG, "Failed to parse error as ApiError, returning raw body", e);
                    // If parsing fails, return the raw error body
                    return errorBody;
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error reading error body", e);
        }

        // Fallback based on HTTP status code
        return getDefaultErrorMessage(response.code());
    }

    /**
     * Parse error from Throwable (network errors)
     *
     * @param throwable The exception that occurred
     * @return User-friendly error message
     */
    public static String parseError(Throwable throwable) {
        if (throwable == null) {
            return "Unknown error occurred";
        }

        Log.e(TAG, "Network error: " + throwable.getMessage(), throwable);

        if (throwable instanceof IOException) {
            return "Lỗi kết nối mạng. Vui lòng kiểm tra kết nối internet.";
        }

        String message = throwable.getMessage();
        if (message != null && !message.isEmpty()) {
            return message;
        }

        return "Đã xảy ra lỗi. Vui lòng thử lại.";
    }

    /**
     * Get default error message based on HTTP status code
     *
     * @param statusCode HTTP status code
     * @return Default error message
     */
    private static String getDefaultErrorMessage(int statusCode) {
        switch (statusCode) {
            case 400:
                return "Yêu cầu không hợp lệ";
            case 401:
                return "Chưa xác thực. Vui lòng đăng nhập lại";
            case 403:
                return "Bạn không có quyền thực hiện thao tác này";
            case 404:
                return "Không tìm thấy tài nguyên";
            case 409:
                return "Dữ liệu đã tồn tại";
            case 422:
                return "Dữ liệu không hợp lệ";
            case 429:
                return "Quá nhiều yêu cầu. Vui lòng thử lại sau";
            case 500:
                return "Lỗi máy chủ. Vui lòng thử lại sau";
            case 502:
                return "Lỗi kết nối máy chủ";
            case 503:
                return "Dịch vụ tạm thời không khả dụng";
            default:
                if (statusCode >= 500) {
                    return "Lỗi máy chủ. Vui lòng thử lại sau";
                } else if (statusCode >= 400) {
                    return "Yêu cầu không hợp lệ";
                }
                return "Đã xảy ra lỗi (Code: " + statusCode + ")";
        }
    }

    /**
     * Create an ApiError object from error message
     *
     * @param message Error message
     * @return ApiError object
     */
    public static ApiError createApiError(String message) {
        ApiError apiError = new ApiError();
        apiError.setMsg(message);
        return apiError;
    }

    /**
     * Create an ApiError object with code and message
     *
     * @param code Error code
     * @param message Error message
     * @return ApiError object
     */
    public static ApiError createApiError(int code, String message) {
        ApiError apiError = new ApiError();
        apiError.setCode(code);
        apiError.setMsg(message);
        return apiError;
    }
}
