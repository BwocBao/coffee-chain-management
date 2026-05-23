
package com.coffeechain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Thong tin anh da upload len Cloudinary")
public record ImageUploadResponse(
        @Schema(description = "URL anh dang http neu Cloudinary tra ve", example = "http://res.cloudinary.com/demo/image/upload/v1/coffee-chain/products/abc.jpg")
        String url,
        @Schema(description = "URL anh dang https, frontend uu tien dung gia tri nay", example = "https://res.cloudinary.com/demo/image/upload/v1/coffee-chain/products/abc.jpg")
        String secureUrl,
        @Schema(description = "Public id cua anh tren Cloudinary", example = "coffee-chain/products/abc")
        String publicId,
        @Schema(description = "Dinh dang anh", example = "jpg")
        String format,
        @Schema(description = "Kich thuoc file tinh bang byte", example = "153421")
        Long bytes
) {
}
