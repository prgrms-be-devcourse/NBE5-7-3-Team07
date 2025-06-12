package com.luckyseven.backend.domain.expense.repository;

import com.luckyseven.backend.domain.expense.dto.ExpenseResponse;
import com.luckyseven.backend.domain.expense.entity.Expense;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

  @EntityGraph(attributePaths = {"payer"})
  Page<Expense> findByTeamId(Long teamId, Pageable pageable);

  // TODO: 쿼리 최적화
  @EntityGraph(attributePaths = "payer")
  @Query(
      value = """
            select new com.luckyseven.backend.domain.expense.dto.ExpenseResponse(
              e.id,
              e.description,
              e.amount,
              e.category,
              p.id,
              p.nickname,
              e.createdAt,
              e.updatedAt,
              e.paymentMethod
            )
            from Expense e
            join e.payer p
            where e.team.id = :teamId
          """,
      countQuery = "select count(e) from Expense e where e.team.id = :teamId"
  )
  Page<ExpenseResponse> findResponsesByTeamId(Long teamId, Pageable pageable);


  @Query("""
        select e from Expense e
         join fetch e.payer p
         where e.id = :expenseId
      """)
  Optional<Expense> findByIdWithPayer(Long expenseId);


  @Query("""
         select e from Expense e
          join fetch e.team t
          join fetch t.budget
          where e.id = :expenseId
      """)
  Optional<Expense> findWithTeamAndBudgetById(Long expenseId);

  @Query(value = """
    SELECT 
      e.category AS category,
      SUM(
        CASE 
          WHEN e.payment_method = 'CASH'
            THEN e.amount * b.avg_exchange_rate
          ELSE e.amount
        END
      ) AS total_amount
    FROM expense e
    JOIN team t ON e.team_id = t.team_id
    JOIN budget b ON t.budget_id = b.budget_id
    WHERE e.team_id = :teamId
    GROUP BY e.category
    """, nativeQuery = true)
  Optional<List<CategoryExpenseSum>> findCategoryExpenseSumsByTeamId(@Param("teamId") Long teamId);

}
