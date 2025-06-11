package com.luckyseven.backend.domain.budget.entity;

public enum CurrencyCode {
  USD("미국 달러", "United States Dollar"),
  EUR("유로", "Euro"),
  KRW("대한민국 원", "South Korean Won"),
  JPY("일본 엔", "Japanese Yen"),
  CNY("중국 위안", "Chinese Yuan"),
  GBP("영국 파운드", "British Pound"),
  AUD("호주 달러", "Australian Dollar"),
  CAD("캐나다 달러", "Canadian Dollar"),
  CHF("스위스 프랑", "Swiss Franc"),
  INR("인도 루피", "Indian Rupee"),
  SGD("싱가포르 달러", "Singapore Dollar"),
  THB("태국 바트", "Thai Baht"),
  HKD("홍콩 달러", "Hong Kong Dollar"),
  RUB("러시아 루블", "Russian Ruble"),
  BRL("브라질 헤알", "Brazilian Real");

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
