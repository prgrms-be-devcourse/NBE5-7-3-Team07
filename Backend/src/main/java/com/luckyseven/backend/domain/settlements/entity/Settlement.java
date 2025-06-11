package com.luckyseven.backend.domain.settlements.entity;

import com.luckyseven.backend.domain.expense.entity.Expense;
import com.luckyseven.backend.domain.member.entity.Member;
import com.luckyseven.backend.sharedkernel.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    indexes = {
        @Index(name = "idx_settler", columnList = "settler_id"),  // settler에 대한 인덱스
        @Index(name = "idx_payer", columnList = "payer_id"),      // payer에 대한 인덱스
        @Index(name = "idx_expense", columnList = "expense_id")   // expense에 대한 인덱스
    }
)
public class Settlement extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, precision = 15, scale = 2)
  private BigDecimal amount;
  @Column(nullable = false)
  private Boolean isSettled = false;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT), name = "settler_id", nullable = false)
  private Member settler;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT), name = "payer_id", nullable = false)
  private Member payer;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT), name = "expense_id", nullable = false)
  private Expense expense;

  @Builder
  public Settlement(BigDecimal amount, Member settler, Member payer, Expense expense) {
    this.amount = amount;
    this.settler = settler;
    this.payer = payer;
    this.expense = expense;
  }

  public void update(BigDecimal amount, Member settler, Member payer, Expense expense,
      Boolean isSettled) {
    if (amount != null) {
      this.amount = amount;
    }
    if (settler != null) {
      this.settler = settler;
    }
    if (payer != null) {
      this.payer = payer;
    }
    if (expense != null) {
      this.expense = expense;
    }
    if (isSettled != null) {
      this.isSettled = isSettled;
    }
  }

  public void convertSettled() {
    this.isSettled = !this.isSettled;
  }
}
