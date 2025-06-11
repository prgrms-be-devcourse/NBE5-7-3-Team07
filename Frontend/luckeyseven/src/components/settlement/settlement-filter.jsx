"use client"

import {useState} from "react"
import {useLocation, useNavigate} from "react-router-dom"

export function SettlementFilter({users, expenses, initialFilters}) {
  const navigate = useNavigate()
  const location = useLocation()

  const [filters, setFilters] = useState({
    payerId: initialFilters.payerId || "",
    settlerId: initialFilters.settlerId || "",
    expenseId: initialFilters.expenseId || "",
    isSettled: initialFilters.isSettled || "",
  })

  const handleFilterChange = (key, value) => {
    setFilters((prev) => ({...prev, [key]: value}))
  }

  const applyFilters = () => {
    const searchParams = new URLSearchParams()

    Object.entries(filters).forEach(([key, value]) => {
      if (value) {
        searchParams.append(key, value)
      }
    })

    navigate(`${location.pathname}?${searchParams.toString()}`)
  }

  const resetFilters = () => {
    setFilters({
      payerId: "",
      settlerId: "",
      expenseId: "",
      isSettled: "",
    })

    navigate(location.pathname)
  }

  const hasActiveFilters = Object.values(filters).some((value) => value !== "")

  return (
      <div className="card">
        <div className="card-content">
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            <div className="form-group">
              <label htmlFor="payer-filter" className="form-label">
                결제자
              </label>
              <select
                  id="payer-filter"
                  className="form-select"
                  value={filters.payerId}
                  onChange={(e) => handleFilterChange("payerId",
                      e.target.value)}
              >
                <option value="">전체</option>
                {users.map((user) => (
                    <option key={user.id} value={user.id}>
                      {user.memberNickName}
                    </option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="settler-filter" className="form-label">
                정산자
              </label>
              <select
                  id="settler-filter"
                  className="form-select"
                  value={filters.settlerId}
                  onChange={(e) => handleFilterChange("settlerId",
                      e.target.value)}
              >
                <option value="">전체</option>
                {users.map((user) => (
                    <option key={user.id} value={user.id}>
                      {user.memberNickName}
                    </option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="expense-filter" className="form-label">
                지출 항목
              </label>
              <select
                  id="expense-filter"
                  className="form-select"
                  value={filters.expenseId}
                  onChange={(e) => handleFilterChange("expenseId",
                      e.target.value)}
              >
                <option value="">전체</option>
                {expenses.map((expense) => (
                    <option key={expense.id} value={expense.id}>
                      {expense.description} ({expense.amount})
                    </option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="status-filter" className="form-label">
                정산 상태
              </label>
              <select
                  id="status-filter"
                  className="form-select"
                  value={filters.isSettled}
                  onChange={(e) => handleFilterChange("isSettled",
                      e.target.value)}
              >
                <option value="">전체</option>
                <option value="true">정산 완료</option>
                <option value="false">정산 대기중</option>
              </select>
            </div>
          </div>

          <div className="flex justify-end mt-4 space-x-2">
            {hasActiveFilters && (
                <button className="btn btn-outline btn-sm"
                        onClick={resetFilters}>
                  필터 초기화
                </button>
            )}
            <button className="btn btn-primary btn-sm" onClick={applyFilters}>
              필터 적용
            </button>
          </div>
        </div>
      </div>
  )
}
