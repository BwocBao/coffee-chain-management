package com.coffeechain.service;

import com.coffeechain.dto.response.ImageUploadResponse;
import com.coffeechain.exception.AppException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CloudinaryUploadService {
  private static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024;

  private final RestTemplate restTemplate = new RestTemplate();
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Value("${cloudinary.cloud-name:}")
  private String cloudName;

  @Value("${cloudinary.api-key:}")
  private String apiKey;

  @Value("${cloudinary.api-secret:}")
  private String apiSecret;

  @Value("${cloudinary.folder:coffee-chain/products}")
  private String folder;

  public ImageUploadResponse uploadImage(MultipartFile file) {
    validateConfig();
    validateFile(file);

    try {
      long timestamp = Instant.now().getEpochSecond();
      Map<String, String> signedParams = new TreeMap<>();
      signedParams.put("folder", folder);
      signedParams.put("timestamp", String.valueOf(timestamp));
      String signature = sign(signedParams);

      MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
      body.add("file", multipartResource(file));
      body.add("api_key", apiKey);
      body.add("timestamp", String.valueOf(timestamp));
      body.add("folder", folder);
      body.add("signature", signature);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.MULTIPART_FORM_DATA);

      String url = "https://api.cloudinary.com/v1_1/" + cloudName + "/image/upload";
      ResponseEntity<String> response =
          restTemplate.exchange(
              url, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);

      if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
        throw new AppException(
            HttpStatus.BAD_GATEWAY, "Cloudinary khong tra ve ket qua upload hop le");
      }

      JsonNode json = objectMapper.readTree(response.getBody());
      return new ImageUploadResponse(
          text(json, "url"),
          text(json, "secure_url"),
          text(json, "public_id"),
          text(json, "format"),
          json.hasNonNull("bytes") ? json.get("bytes").asLong() : null);
    } catch (IOException e) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Khong doc duoc file anh upload");
    } catch (RestClientException e) {
      throw new AppException(
          HttpStatus.BAD_GATEWAY, "Upload anh len Cloudinary that bai: " + rootMessage(e));
    }
  }

  private void validateConfig() {
    if (isBlank(cloudName) || isBlank(apiKey) || isBlank(apiSecret)) {
      throw new AppException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Chua cau hinh Cloudinary tren backend");
    }
  }

  private void validateFile(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Vui long chon file anh");
    }
    if (file.getSize() > MAX_IMAGE_BYTES) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Anh upload khong duoc vuot qua 5MB");
    }
    String contentType = file.getContentType();
    if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
      throw new AppException(HttpStatus.BAD_REQUEST, "File upload phai la hinh anh");
    }
  }

  private ByteArrayResource multipartResource(MultipartFile file) throws IOException {
    return new ByteArrayResource(file.getBytes()) {
      @Override
      public String getFilename() {
        return file.getOriginalFilename() == null ? "product-image" : file.getOriginalFilename();
      }
    };
  }

  private String sign(Map<String, String> params) {
    StringBuilder raw = new StringBuilder();
    for (Map.Entry<String, String> entry : params.entrySet()) {
      if (raw.length() > 0) {
        raw.append('&');
      }
      raw.append(entry.getKey()).append('=').append(entry.getValue());
    }
    raw.append(apiSecret);
    return sha1(raw.toString());
  }

  private String sha1(String value) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-1");
      byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
      StringBuilder hex = new StringBuilder(hash.length * 2);
      for (byte b : hash) {
        hex.append(String.format("%02x", b));
      }
      return hex.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Khong tao duoc chu ky Cloudinary");
    }
  }

  private String text(JsonNode json, String field) {
    return json.hasNonNull(field) ? json.get(field).asText() : null;
  }

  private boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }

  private String rootMessage(Throwable throwable) {
    Throwable current = throwable;
    while (current.getCause() != null) {
      current = current.getCause();
    }
    return current.getMessage() == null ? "khong ro nguyen nhan" : current.getMessage();
  }
}
