package com.luckyseven.backend.domain.budget.entity;

public enum CurrencyCode {
  AUD("호주 달러", "Australian Dollar"),
  BRL("브라질 헤알", "Brazilian Real"),
  CAD("캐나다 달러", "Canadian Dollar"),
  CHF("스위스 프랑", "Swiss Franc"),
  CNY("중국 위안", "Chinese Yuan"),
  EUR("유로", "Euro"),
  GBP("영국 파운드", "British Pound"),
  HKD("홍콩 달러", "Hong Kong Dollar"),
  INR("인도 루피", "Indian Rupee"),
  JPY("일본 엔", "Japanese Yen"),
  KRW("대한민국 원", "South Korean Won"),
  RUB("러시아 루블", "Russian Ruble"),
  SGD("싱가포르 달러", "Singapore Dollar"),
  THB("태국 바트", "Thai Baht"),
  USD("미국 달러", "United States Dollar"),
  VND("베트남 동", "Vietnamese Dong");

  private final String koreanName;
  private final String englishName;

  CurrencyCode(String koreanName, String englishName) {
    this.koreanName = koreanName;
    this.englishName = englishName;
  }

  @Override
  public String toString() {
    return "CurrencyCode{" +
        "koreanName='" + koreanName + '\'' +
        ", englishName='" + englishName + '\'' +
        '}';
  }
}
