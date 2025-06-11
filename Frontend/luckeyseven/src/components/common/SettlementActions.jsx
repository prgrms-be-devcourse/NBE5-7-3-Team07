"use client"

import {useState} from "react"
import {updateSettlement} from "../../service/settlementService"
import {useToast} from "../../context/ToastContext"

export function SettlementActions({settlement, onUpdate, inline = false}) {
  const {addToast} = useToast()
  const [isLoading, setIsLoading] = useState(false)

  const handleMarkAsSettled = async (e) => {
    if (e) {
      e.preventDefault()
      e.stopPropagation()
    }

    if (settlement.isSettled) {
      return
    }

    try {
      setIsLoading(true)
      const updatedSettlement = await updateSettlement(settlement.id, {}, true)

      addToast({
        title: "정산 완료",
        description: "정산이 완료 처리되었습니다.",
      })

      if (onUpdate) {
        onUpdate({
          ...settlement,
          isSettled: true,
          updatedAt: updatedSettlement.updatedAt || new Date().toISOString(),
        })
      }
    } catch (error) {
      console.error("정산 완료 처리 오류:", error)
      addToast({
        title: "오류 발생",
        description: "정산 완료 처리 중 오류가 발생했습니다.",
        variant: "destructive",
      })
    } finally {
      setIsLoading(false)
    }
  }

  const handleCancelSettlement = async (e) => {
    if (e) {
      e.preventDefault()
      e.stopPropagation()
    }

    if (!settlement.isSettled) {
      return
    }

    try {
      setIsLoading(true)
      const updatedSettlement = await updateSettlement(settlement.id,
          {}, true)

      addToast({
        title: "정산 완료 취소",
        description: "정산 완료가 취소되었습니다.",
      })

      if (onUpdate) {
        onUpdate({
          ...settlement,
          isSettled: false,
          updatedAt: updatedSettlement.updatedAt || new Date().toISOString(),
        })
      }
    } catch (error) {
      console.error("정산 완료 취소 오류:", error)
      addToast({
        title: "오류 발생",
        description: "정산 완료 취소 중 오류가 발생했습니다.",
        variant: "destructive",
      })
    } finally {
      setIsLoading(false)
    }
  }

  const btnClass = inline ? "btn-sm" : ""

  return (
      <div className="flex space-x-2">
        {settlement.isSettled ? (
            <button className={`btn btn-outline ${btnClass}`}
                    onClick={handleCancelSettlement} disabled={isLoading}>
              {isLoading ? "처리 중..." : "정산 완료 취소"}
            </button>
        ) : (
            <button className={`btn btn-primary ${btnClass}`}
                    onClick={handleMarkAsSettled} disabled={isLoading}>
              {isLoading ? "처리 중..." : "정산 완료 처리"}
            </button>
        )}
      </div>
  )
}
