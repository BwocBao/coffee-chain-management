package com.coffeechain.service;

import com.coffeechain.dto.BaseResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * Lớp nền cho các API client gọi backend.
 * Gom phần dùng chung như Jackson, HttpClient, parse BaseResponse và bearer token để các client nghiệp vụ gọn hơn.
 */
public abstract class ApiClientSupport {
    protected final ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    protected ApiClientSupport() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    protected String toJson(Object value) throws IOException {
        return objectMapper.writeValueAsString(value);
    }

    /**
     * Tạo giá trị header Authorization từ token đang lưu trong {@link SessionManager}.
     */
    protected String bearerToken() {
        return "Bearer " + SessionManager.getToken();
    }

    protected HttpResponse<String> send(HttpRequest request) throws IOException, InterruptedException {
        return httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
        );
    }

    protected <T> BaseResponse<T> readBaseResponse(
            HttpResponse<String> response,
            TypeReference<BaseResponse<T>> typeReference
    ) throws IOException {
        try {
            return objectMapper.readValue(response.body(), typeReference);
        } catch (Exception e) {
            throw new IOException("Không đọc được phản hồi từ server. HTTP " + response.statusCode());
        }
    }

    protected <T> T extractData(BaseResponse<T> baseResponse) throws IOException {
        if (baseResponse.isSuccess()) {
            return baseResponse.getData();
        }

        throw new IOException(baseResponse.getMessage());
    }
}
