package com.luckyseven.backend.domain.expense.repository

import com.luckyseven.backend.domain.expense.dto.ExpenseResponse
import com.luckyseven.backend.domain.expense.entity.Expense
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface ExpenseRepository : JpaRepository<Expense?, Long?> {
    @EntityGraph(attributePaths = ["payer"])
    fun findByTeamId(teamId: Long?, pageable: Pageable?): Page<Expense?>?

    // TODO: 쿼리 최적화
    @EntityGraph(attributePaths = ["payer"])
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
          
          """.trimIndent(), countQuery = "select count(e) from Expense e where e.team.id = :teamId"
    )
    fun findResponsesByTeamId(teamId: Long?, pageable: Pageable?): Page<ExpenseResponse?>?


    @Query(
        """
        select e from Expense e
         join fetch e.payer p
         where e.id = :expenseId
      
      """.trimIndent()
    )
    fun findByIdWithPayer(expenseId: Long?): Optional<Expense?>?


    @Query(
        """
         select e from Expense e
          join fetch e.team t
          join fetch t.budget
          where e.id = :expenseId
      
      """.trimIndent()
    )
    fun findWithTeamAndBudgetById(expenseId: Long?): Optional<Expense?>?

    @Query(
        value = """
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
    
    """.trimIndent(), nativeQuery = true
    )
    fun findCategoryExpenseSumsByTeamId(@Param("teamId") teamId: Long?): Optional<MutableList<CategoryExpenseSum?>?>?
}
