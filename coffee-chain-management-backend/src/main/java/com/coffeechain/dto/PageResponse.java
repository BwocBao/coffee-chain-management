package com.coffeechain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Ket qua phan trang")
public class PageResponse<T> {
  @Schema(description = "Danh sach du lieu")
  private List<T> content;

  @Schema(description = "Tong so dong du lieu", example = "100")
  private long totalElements;

  @Schema(description = "Trang hien tai", example = "0")
  private int page;

  @Schema(description = "So dong moi trang", example = "20")
  private int size;

  @Schema(description = "Tong so trang", example = "5")
  private int totalPages;

  public PageResponse() {}

  public PageResponse(List<T> content, long totalElements, int page, int size) {
    this.content = content;
    this.totalElements = totalElements;
    this.page = page;
    this.size = size;
    this.totalPages = size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
  }

  public static <T> PageResponse<T> of(List<T> content, long totalElements, int page, int size) {
    return new PageResponse<>(content, totalElements, page, size);
  }

  public List<T> getContent() {
    return content;
  }

  public void setContent(List<T> content) {
    this.content = content;
  }

  public long getTotalElements() {
    return totalElements;
  }

  public void setTotalElements(long totalElements) {
    this.totalElements = totalElements;
  }

  public int getPage() {
    return page;
  }

  public void setPage(int page) {
    this.page = page;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public int getTotalPages() {
    return totalPages;
  }

  public void setTotalPages(int totalPages) {
    this.totalPages = totalPages;
  }
}
