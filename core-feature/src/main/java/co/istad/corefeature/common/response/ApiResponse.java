package co.istad.corefeature.common.response;

import lombok.Builder;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Builder
public record ApiResponse<T>(
        Integer status,
        String message,
        T payload,
        LocalDateTime timestamp
) {
    public static <T> ApiResponse<T> of(HttpStatus status, String message, T payload) {
        return ApiResponse.<T>builder()
                .status(status.value())
                .message(message)
                .payload(payload)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> ok(String message, T payload) {
        return of(HttpStatus.OK, message, payload);
    }

    public static <T> ApiResponse<T> ok(String message) {
        return of(HttpStatus.OK, message, null);
    }

    public static <T> ApiResponse<T> created(String message, T payload) {
        return of(HttpStatus.CREATED, message, payload);
    }
}
