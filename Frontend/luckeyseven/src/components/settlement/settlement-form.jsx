"use client"

import {useEffect, useState} from "react"
import {useNavigate} from "react-router-dom"
import {useToast} from "../../context/ToastContext"
import {
  createSettlement,
  updateSettlement
} from "../../service/settlementService"

export function SettlementForm({
  settlement,
  users,
  expenses,
  isEditing = false,
  onFormSubmit,
  onCancel
}) {
  const navigate = useNavigate()
  const {addToast} = useToast()
  const [isLoading, setIsLoading] = useState(false)

  const [formData, setFormData] = useState({
    payerId: isEditing ? settlement.payerId : "",
    settlerId: isEditing ? settlement.settlerId : "",
    expenseId: isEditing ? settlement.expenseId : "",
    amount: isEditing ? settlement.amount.toString() : "",
    isSettled: isEditing ? settlement.isSettled : false
  })

  // 수정 모드일 경우 초기 데이터 설정
  useEffect(() => {
    if (isEditing && settlement) {
      setFormData({
        payerId: settlement.payerId || "",
        settlerId: settlement.settlerId || "",
        expenseId: settlement.expenseId || "",
        amount: settlement.amount ? settlement.amount.toString() : "",
        isSettled: settlement.isSettled || false,
      })
    }
  }, [isEditing, settlement])

  const handleChange = (field, value) => {
    setFormData((prev) => ({
      ...prev,
      [field]: value,
    }))
  }

  const handleExpenseChange = (expenseId) => {
    handleChange("expenseId", expenseId)

    // 지출 항목이 변경되면 금액도 자동으로 업데이트
    if (expenseId) {
      const selectedExpense = expenses.find(
          (expense) => expense.id === expenseId)
      if (selectedExpense) {
        // 기본적으로 지출 금액의 절반으로 설정
        const defaultAmount = Math.floor(selectedExpense.amount / 2)
        handleChange("amount", defaultAmount.toString())
      }
    }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!formData.payerId || !formData.settlerId || !formData.expenseId
        || !formData.amount) {
      addToast({
        title: "입력 오류",
        description: "모든 필수 항목을 입력해주세요.",
        variant: "destructive",
      })
      return
    }

    if (formData.payerId === formData.settlerId) {
      addToast({
        title: "입력 오류",
        description: "결제자와 정산자는 서로 다른 사람이어야 합니다.",
        variant: "destructive",
      })
      return
    }

    try {
      setIsLoading(true)

      const data = {
        ...formData,
        amount: Number.parseInt(formData.amount, 10),
      }

      let result
      if (isEditing) {
        result = await updateSettlement(settlement.id, data)
      } else {
        result = await createSettlement(data)
      }

      addToast({
        title: isEditing ? "정산 수정 완료" : "정산 생성 완료",
        description: isEditing ? "정산 내역이 성공적으로 수정되었습니다."
            : "새로운 정산 내역이 생성되었습니다.",
      })

      // onFormSubmit이 있으면 호출 (모달에서 사용할 때)
      if (onFormSubmit) {
        onFormSubmit(result)
        return
      }

      // 수정 완료 후 상세 페이지로 이동
      navigate(`/settlements/${isEditing ? settlement.id : result.id}`)
    } catch (error) {
      console.error(isEditing ? "정산 수정 오류:" : "정산 생성 오류:", error)
      addToast({
        title: "오류 발생",
        description: error.message,
        variant: "destructive",
      })
    } finally {
      setIsLoading(false)
    }
  }

  const handleCancel = () => {
    if (onCancel) {
      onCancel()
    } else {
      navigate(-1)
    }
  }

  // 선택된 지출 항목 정보
  const selectedExpense = formData.expenseId ? expenses.find(
      (expense) => expense.id === formData.expenseId) : null

  return (
      <form onSubmit={handleSubmit}>
        <div className="card">
          <div className="card-content space-y-6">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="space-y-4">
                <div className="form-group">
                  <label htmlFor="payer" className="form-label">
                    결제자
                  </label>
                  <select
                      id="payer"
                      className="form-select"
                      value={formData.payerId}
                      onChange={(e) => handleChange("payerId", e.target.value)}
                      disabled={isLoading}
                  >
                    <option value="">결제자 선택</option>
                    {users.map((user) => (
                        <option key={user.memberId} value={user.memberId}>
                          {user.memberNickName}
                        </option>
                    ))}
                  </select>
                </div>

                <div style={{height: '9px'}}></div>

                <div className="form-group">
                  <label htmlFor="settler" className="form-label">
                    정산자
                  </label>
                  <select
                      id="settler"
                      className="form-select"
                      value={formData.settlerId}
                      onChange={(e) => handleChange("settlerId",
                          e.target.value)}
                      disabled={isLoading}
                  >
                    <option value="">정산자 선택</option>
                    {users.map((user) => (
                        <option key={user.memberId} value={user.memberId}>
                          {user.memberNickName}
                        </option>
                    ))}
                  </select>
                </div>
              </div>

              <div className="space-y-4">
                <div className="form-group">
                  <label htmlFor="expense" className="form-label">
                    지출 항목
                  </label>
                  <select
                      id="expense"
                      className="form-select"
                      value={formData.expenseId}
                      onChange={(e) => handleExpenseChange(e.target.value)}
                      disabled={isLoading}
                  >
                    <option value="">지출 항목 선택</option>
                    {expenses.map((expense) => (
                        <option key={expense.id} value={expense.id}>
                          {expense.description}
                        </option>
                    ))}
                  </select>

                  <div className="h-3 mt-1">
                  </div>

                </div>

                <div className="form-group">
                  <label htmlFor="amount" className="form-label">
                    정산 금액
                  </label>
                  <input
                      id="amount"
                      type="number"
                      className="form-input"
                      value={formData.amount}
                      onChange={(e) => handleChange("amount", e.target.value)}
                      placeholder="정산 금액 입력"
                      disabled={isLoading}
                  />
                </div>

                <div className="flex items-center space-x-2 pt-2">
                  <input
                      type="checkbox"
                      id="isSettled"
                      checked={formData.isSettled}
                      onChange={(e) => handleChange("isSettled",
                          e.target.checked)}
                      disabled={isLoading}
                  />
                  <label htmlFor="isSettled" className="cursor-pointer">
                    정산 완료
                  </label>
                </div>
              </div>
            </div>
          </div>

          <div className="card-footer">
            <button type="button" className="btn btn-outline"
                    onClick={handleCancel} disabled={isLoading}>
              취소
            </button>
            <button type="submit" className="btn btn-primary"
                    disabled={isLoading}>
              {isLoading ? "처리 중..." : isEditing ? "수정 완료" : "생성하기"}
            </button>
          </div>
        </div>
      </form>
  )
}