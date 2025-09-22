package com.luckyseven.backend.domain.expense.repository

import com.luckyseven.backend.domain.expense.dto.ExpenseResponse
import com.luckyseven.backend.domain.expense.entity.Expense
import java.math.BigDecimal
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ExpenseRepository : JpaRepository<Expense, Long> {

    @EntityGraph(attributePaths = ["payer"])
    fun findByTeamId(
        @Param("teamId") teamId: Long,
        pageable: Pageable
    ): Page<Expense>

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
        """,
        countQuery = "select count(e) from Expense e where e.team.id = :teamId"
    )
    fun findResponsesByTeamId(
        @Param("teamId") teamId: Long,
        pageable: Pageable
    ): Page<ExpenseResponse>

    @Query(
        """
        select e from Expense e
        join fetch e.payer p
        where e.id = :expenseId
        """
    )
    fun findByIdWithPayer(
        @Param("expenseId") expenseId: Long
    ): Expense?

    @Query(
        """
        select e from Expense e
        join fetch e.team t
        join fetch t.budget
        where e.id = :expenseId
        """
    )
    fun findWithTeamAndBudgetById(
        @Param("expenseId") expenseId: Long
    ): Expense?

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
        """,
        nativeQuery = true
    )
    fun findCategoryExpenseSumsByTeamId(
        @Param("teamId") teamId: Long
    ): List<CategoryExpenseSum>

    @Query(
        value = """
        SELECT 
          e.category AS category,
          SUM(
            CASE 
              WHEN e.payment_method = 'CASH'
                THEN e.amount * COALESCE(:avgExchangeRate, 1)
              ELSE e.amount
            END
          ) AS total_amount
        FROM expense e
        WHERE e.team_id = :teamId
        GROUP BY e.category
        """,
        nativeQuery = true
    )
    fun findCategoryExpenseSumsByTeamId(
        @Param("teamId") teamId: Long,
        @Param("avgExchangeRate") avgExchangeRate: BigDecimal?
    ): List<CategoryExpenseSum>

    // 지출이 있는 경우 예산 삭제를 제한하기 위한 지출 조회
    fun existsByTeamId(teamId: Long): Boolean
}
