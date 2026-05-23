package com.coffeechain.controller;

import com.coffeechain.dto.BaseResponse;
import com.coffeechain.dto.response.ImageUploadResponse;
import com.coffeechain.security.AuthGuard;
import com.coffeechain.service.CloudinaryUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(
    name = "Upload hinh anh",
    description =
        "API upload hinh anh len Cloudinary. Frontend dung secureUrl tra ve de luu vao field hinhAnh cua san pham/cong thuc.")
@RestController
@RequestMapping("/api/uploads")
public class ImageUploadController {
  private final CloudinaryUploadService cloudinaryUploadService;
  private final AuthGuard authGuard;

  public ImageUploadController(
      CloudinaryUploadService cloudinaryUploadService, AuthGuard authGuard) {
    this.cloudinaryUploadService = cloudinaryUploadService;
    this.authGuard = authGuard;
  }

  @Operation(
      summary = "Upload anh san pham/cong thuc len Cloudinary",
      description =
          "Request multipart/form-data gom part file. Yeu cau quyen RECIPE:MANAGE. Response class ImageUploadResponse, frontend uu tien dung secureUrl de gan vao hinhAnh khi tao/cap nhat cong thuc.")
  @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<BaseResponse<ImageUploadResponse>> uploadImage(
      @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false)
          String authHeader,
      @Parameter(description = "File anh can upload, toi da 5MB", required = true)
          @RequestPart("file")
          MultipartFile file) {
    authGuard.requirePermission(authHeader, "RECIPE:MANAGE");

    return ResponseEntity.ok(
        BaseResponse.ok("Upload hinh anh thanh cong", cloudinaryUploadService.uploadImage(file)));
  }
}
