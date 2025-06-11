package com.luckyseven.backend.sharedkernel.dto;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PageResponse<T> {

  private List<T> content;
  private int page;
  private int size;
  private int totalPages;
  private long totalElements;

  public static <T> PageResponse<T> fromPage(Page<T> pageData) {
    return PageResponse.<T>builder()
        .content(pageData.getContent())
        .page(pageData.getNumber())
        .size(pageData.getSize())
        .totalPages(pageData.getTotalPages())
        .totalElements(pageData.getTotalElements())
        .build();
  }
}
