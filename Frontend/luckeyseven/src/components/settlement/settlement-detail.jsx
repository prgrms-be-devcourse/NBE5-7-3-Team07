"use client"

import {useState} from "react"
import {useNavigate} from "react-router-dom"
import {formatCurrency, formatDate} from "../../lib/utils"
import {StatusBadge} from "../common/StatusBadge"
import {UserProfile} from "../common/UserProfile"
import {SettlementActions} from "../common/SettlementActions"

export function SettlementDetail({settlement: initialSettlement}) {
  const navigate = useNavigate()
  const [settlement, setSettlement] = useState(initialSettlement)

  const handleUpdate = (updatedSettlement) => {
    setSettlement(updatedSettlement)
  }

  const handleEdit = () => {
    navigate(`/teams/${settlement.teamId}/settlements/${settlement.id}/edit`)
  }
  console.info("팀아이디", settlement.teamId)

  return (
      <div className="card">
        <div className="card-header">
          <div className="flex justify-between items-center">
            <h3 className="card-title">정산 #{String(settlement.id).substring(
                0,
                8)}</h3>
            <StatusBadge isSettled={settlement.isSettled}/>
          </div>
        </div>
        <div className="card-content space-y-6">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="space-y-4">
              <UserProfile nickname={settlement.payerNickname} label="결제자 정보"/>
              <UserProfile nickname={settlement.settlerNickname}
                           label="정산자 정보"/>
            </div>

            <div className="space-y-4">
              <div>
                <h3 className="text-sm text-muted">연관 지출</h3>
                <p className="mt-1 font-medium">{settlement.expenseDescription
                    || "알 수 없음"}</p>
                {settlement.expense && (
                    <p className="text-sm text-muted">
                      {formatDate(settlement.expense.date)} · {formatCurrency(
                        settlement.expense.amount)}
                    </p>
                )}
              </div>

              <div>
                <h3 className="text-sm text-muted">정산 금액</h3>
                <p className="mt-1 text-xl font-bold">{formatCurrency(
                    settlement.amount)}</p>
              </div>
            </div>
          </div>

          <div className="pt-4 border-t">
            <h3 className="text-sm text-muted mb-2">정산 정보</h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <p className="text-sm text-muted">생성일</p>
                <p>{formatDate(settlement.createdAt)}</p>
              </div>
              <div>
                <p className="text-sm text-muted">수정일</p>
                <p>{formatDate(settlement.updatedAt)}</p>
              </div>
            </div>
          </div>
        </div>
        <div className="card-footer">
          <button className="btn btn-outline" onClick={() => navigate(-1)}>
            뒤로 가기
          </button>
          <div className="flex space-x-2">
            <button className="btn btn-outline" onClick={handleEdit}>
              수정하기
            </button>
            <SettlementActions settlement={settlement} onUpdate={handleUpdate}/>
          </div>
        </div>
      </div>
  )
}
