"use client"

import {useState} from "react"
import {formatCurrency, formatDate} from "../../lib/utils"
import {StatusBadge} from "../common/StatusBadge"
import {SettlementActions} from "../common/SettlementActions"

export function SettlementList({
  settlements: initialSettlements,
  onSettlementClick
}) {
  const [settlements, setSettlements] = useState(initialSettlements)

  const handleUpdate = (updatedSettlement) => {
    setSettlements((prevSettlements) =>
        prevSettlements.map(
            (settlement) => (settlement.id === updatedSettlement.id
                ? updatedSettlement : settlement)),
    )
  }

  if (settlements.length === 0) {
    return (
        <div className="card">
          <div
              className="card-content flex flex-col items-center justify-center py-12">
            <p className="text-muted mb-4">조회된 정산 내역이 없습니다.</p>
            <p className="text-sm text-muted">필터를 변경하거나 새로운 정산을 추가해보세요.</p>
          </div>
        </div>
    )
  }

  return (
      <div className="space-y-4">
        {settlements.map((settlement) => (
            <div
                key={settlement.id}
                onClick={() => onSettlementClick(settlement.id)}
                className="block"
            >
              <div className="card hover:shadow-md transition cursor-pointer">
                <div className="card-content p-6">
                  <div className="flex justify-between items-start">
                    <div className="space-y-2">
                      <div className="flex items-center space-x-2">
                        <h3 className="font-medium">정산 #{String(
                            settlement.id).substring(0, 8)}</h3>
                        <StatusBadge isSettled={settlement.isSettled}/>
                      </div>

                      <div className="text-sm text-muted">
                        <span>생성일: {formatDate(settlement.createdAt)}</span>
                      </div>

                      <div
                          className="grid grid-cols-1 md:grid-cols-3 gap-4 mt-4">
                        <div>
                          <p className="text-sm text-muted">결제자</p>
                          <p className="font-medium">{settlement.payerNickname
                              || "알 수 없음"}</p>
                        </div>
                        <div>
                          <p className="text-sm text-muted">정산자</p>
                          <p className="font-medium">{settlement.settlerNickname
                              || "알 수 없음"}</p>
                        </div>
                        <div>
                          <p className="text-sm text-muted">지출 항목</p>
                          <p className="font-medium">{settlement.expenseDescription
                              || "알 수 없음"}</p>
                        </div>
                      </div>
                    </div>

                    <div className="flex flex-col items-end space-y-4">
                      <p className="text-xl font-bold">{formatCurrency(
                          settlement.amount)}</p>

                      <div className="flex space-x-2">
                        <SettlementActions settlement={settlement}
                                           onUpdate={handleUpdate}
                                           inline={true}
                                           onClick={(e) => e.stopPropagation()}/>
                        <button
                            className="btn btn-sm"
                            onClick={(e) => {
                              e.stopPropagation()
                              onSettlementClick(settlement.id)
                            }}
                        >
                          상세 보기
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
        ))}
      </div>
  )
}