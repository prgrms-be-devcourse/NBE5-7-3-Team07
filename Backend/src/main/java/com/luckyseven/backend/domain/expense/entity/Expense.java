package com.luckyseven.backend.domain.expense.entity;

import com.luckyseven.backend.domain.expense.enums.ExpenseCategory;
import com.luckyseven.backend.domain.expense.enums.PaymentMethod;
import com.luckyseven.backend.domain.member.entity.Member;
import com.luckyseven.backend.domain.team.entity.Team;
import com.luckyseven.backend.sharedkernel.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
    indexes = {
        @Index(name = "idx_expense_payer", columnList = "payer_id"),
        @Index(name = "idx_team", columnList = "team_id")
    }
)
public class Expense extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "expense_id", nullable = false)
  private Long id;

  @Column(nullable = false)
  private String description;

  @Column(nullable = false, precision = 15, scale = 2)
  private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ExpenseCategory category;

  @Enumerated(EnumType.STRING)
  @Column(name = "payment_method", nullable = false)
  private PaymentMethod paymentMethod;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT),
      nullable = false
  )
  private Member payer;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "team_id",
      foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT),
      nullable = false
  )
  private Team team;

  @Builder
  public Expense(String description,
      BigDecimal amount,
      ExpenseCategory category,
      PaymentMethod paymentMethod,
      Member payer,
      Team team) {
    this.description = description;
    this.amount = amount;
    this.category = category;
    this.paymentMethod = paymentMethod;
    this.payer = payer;
    this.team = team;
  }

  public void update(String description, BigDecimal amount, ExpenseCategory category) {
    if (description != null) {
      this.description = description;
    }
    if (amount != null) {
      this.amount = amount;
    }
    if (category != null) {
      this.category = category;
    }
  }
}
