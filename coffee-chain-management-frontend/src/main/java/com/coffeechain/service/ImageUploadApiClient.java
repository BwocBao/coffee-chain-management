package com.coffeechain.service;

import com.coffeechain.config.ApiConfig;
import com.coffeechain.dto.BaseResponse;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class ImageUploadApiClient extends ApiClientSupport {
  public UploadImageResponse uploadImage(Path imagePath) throws IOException, InterruptedException {
    String boundary = "----CoffeeChainBoundary" + UUID.randomUUID();
    byte[] body = multipartBody(boundary, imagePath);

    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(ApiConfig.IMAGE_UPLOAD_URL))
            .header("Authorization", bearerToken())
            .header("Content-Type", "multipart/form-data; boundary=" + boundary)
            .POST(HttpRequest.BodyPublishers.ofByteArray(body))
            .build();

    HttpResponse<String> response = send(request);
    BaseResponse<UploadImageResponse> baseResponse =
        readBaseResponse(response, new TypeReference<BaseResponse<UploadImageResponse>>() {});
    return extractData(baseResponse);
  }

  private byte[] multipartBody(String boundary, Path imagePath) throws IOException {
    String fileName = imagePath.getFileName().toString();
    String contentType = Files.probeContentType(imagePath);
    if (contentType == null || !contentType.startsWith("image/")) {
      contentType = "application/octet-stream";
    }

    byte[] fileBytes = Files.readAllBytes(imagePath);
    String head =
        "--"
            + boundary
            + "\r\n"
            + "Content-Disposition: form-data; name=\"file\"; filename=\""
            + fileName
            + "\"\r\n"
            + "Content-Type: "
            + contentType
            + "\r\n\r\n";
    String tail = "\r\n--" + boundary + "--\r\n";

    byte[] headBytes = head.getBytes(StandardCharsets.UTF_8);
    byte[] tailBytes = tail.getBytes(StandardCharsets.UTF_8);
    byte[] body = new byte[headBytes.length + fileBytes.length + tailBytes.length];
    System.arraycopy(headBytes, 0, body, 0, headBytes.length);
    System.arraycopy(fileBytes, 0, body, headBytes.length, fileBytes.length);
    System.arraycopy(tailBytes, 0, body, headBytes.length + fileBytes.length, tailBytes.length);
    return body;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class UploadImageResponse {
    private String url;
    private String secureUrl;
    private String publicId;
    private String format;
    private Long bytes;

    public String getUrl() {
      return url;
    }

    public void setUrl(String url) {
      this.url = url;
    }

    public String getSecureUrl() {
      return secureUrl;
    }

    public void setSecureUrl(String secureUrl) {
      this.secureUrl = secureUrl;
    }

    public String getPublicId() {
      return publicId;
    }

    public void setPublicId(String publicId) {
      this.publicId = publicId;
    }

    public String getFormat() {
      return format;
    }

    public void setFormat(String format) {
      this.format = format;
    }

    public Long getBytes() {
      return bytes;
    }

    public void setBytes(Long bytes) {
      this.bytes = bytes;
    }
  }
}
