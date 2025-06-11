package com.luckyseven.backend.domain.budget.entity;

import static com.luckyseven.backend.sharedkernel.exception.ExceptionCode.INSUFFICIENT_BALANCE;

import com.luckyseven.backend.domain.team.entity.Team;
import com.luckyseven.backend.sharedkernel.entity.BaseEntity;
import com.luckyseven.backend.sharedkernel.exception.CustomLogicException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Budget extends BaseEntity {

  @Id
  @Column(name = "budget_id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(mappedBy = "budget")
  private Team team;

  @Column(nullable = false)
  private BigDecimal totalAmount;

  @Setter
  @Column(nullable = false)
  private Long setBy;

  @Column(nullable = false)
  private BigDecimal balance;

  private BigDecimal foreignBalance;

  @Setter
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 3)
  private CurrencyCode foreignCurrency;

  private BigDecimal avgExchangeRate;

  @Builder
  public Budget(Team team, BigDecimal totalAmount, Long setBy,
      BigDecimal balance, BigDecimal foreignBalance, CurrencyCode foreignCurrency,
      BigDecimal avgExchangeRate) {
    this.team = team;
    this.totalAmount = totalAmount;
    this.setBy = setBy;
    this.balance = balance;
    this.foreignBalance = foreignBalance;
    this.foreignCurrency = foreignCurrency;
    this.avgExchangeRate = avgExchangeRate;
  }

  public void setTotalAmount(BigDecimal totalAmount) {
    this.balance = this.balance.add(totalAmount.subtract(this.totalAmount));
    this.totalAmount = totalAmount;
  }

  public void setExchangeInfo(boolean isExchanged, BigDecimal amount, BigDecimal exchangeRate) {
    if (!isExchanged) {
      this.avgExchangeRate = null;
      this.foreignBalance = null;
      return;
    }

    updateForeignBalance(amount, exchangeRate);
  }

  public void updateExchangeInfo(boolean isExchanged, BigDecimal amount, BigDecimal exchangeRate) {
    if (!isExchanged) {
      return;
    }

    updateAvgExchangeRate(amount, exchangeRate);
    updateForeignBalance(amount, exchangeRate);

  }

  // 예산 추가 후 외화잔고 및 평균환율 수정
  private void updateAvgExchangeRate(BigDecimal amount, BigDecimal exchangeRate) {
    if (this.avgExchangeRate == null || this.avgExchangeRate.compareTo(BigDecimal.ZERO) == 0) {
      avgExchangeRate = exchangeRate;
      return;
    }
    BigDecimal foreignAmount = amount.divide(exchangeRate, 10,
        RoundingMode.HALF_UP); // 외화 환산, 충분한 정밀도 확보
    BigDecimal totalCost = this.foreignBalance.multiply(this.avgExchangeRate).add(amount);
    BigDecimal totalForeign = this.foreignBalance.add(foreignAmount);
    this.avgExchangeRate = totalCost.divide(totalForeign, 2, RoundingMode.HALF_UP);

  }

  private void updateForeignBalance(BigDecimal amount, BigDecimal exchangeRate) {
    BigDecimal additionalBudget = amount.divide(exchangeRate, 2, RoundingMode.HALF_UP);
    if (this.foreignBalance == null) {
      foreignBalance = BigDecimal.ZERO;
    }
    this.foreignBalance = this.foreignBalance.add(additionalBudget);
    this.avgExchangeRate = exchangeRate;
  }

  public void setForeignBalance() {
    if (avgExchangeRate != null) {
      this.foreignBalance = totalAmount.divide(avgExchangeRate, 2, RoundingMode.HALF_UP);
    }
  }

  public void setForeignBalance(BigDecimal amount) {
    this.foreignBalance = amount;
  }

  public Budget setTeam(Team team) {
    // 기존 연결 해제
    if (this.team != null) {
      this.team.setBudget(null);
    }

    this.team = team;

    // 새로운 연결 설정 (Team이 null이 아닌 경우)
    if (team != null && team.getBudget() != this) {
      team.setBudget(this);
    }

    return this;
  }

  public void debitKrw(BigDecimal krwAmount) {
    validateSufficientBalance(krwAmount, balance);
    balance = balance.subtract(krwAmount);
  }

  public void debitForeign(BigDecimal foreignAmount) {
    BigDecimal krwAmount = foreignAmount.multiply(avgExchangeRate);
    validateSufficientBalance(krwAmount, balance);
    validateSufficientBalance(foreignAmount, foreignBalance);
    balance = balance.subtract(krwAmount);
    foreignBalance = foreignBalance.subtract(foreignAmount);
  }

  public void creditKrw(BigDecimal krwAmount) {
    balance = balance.add(krwAmount);
  }

  public void creditForeign(BigDecimal foreignAmount) {
    BigDecimal krwAmount = foreignAmount.multiply(avgExchangeRate);
    this.balance = this.balance.add(krwAmount);

    if (this.foreignBalance == null) {
      this.foreignBalance = BigDecimal.ZERO;
    }

    this.foreignBalance = this.foreignBalance.add(foreignAmount);
  }


  private void validateSufficientBalance(BigDecimal amount, BigDecimal current) {
    if (current.compareTo(amount) < 0) {
      throw new CustomLogicException(INSUFFICIENT_BALANCE);
    }
  }

}
