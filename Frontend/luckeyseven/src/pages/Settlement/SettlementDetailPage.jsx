"use client"

import {useEffect, useState} from "react"
import {useParams} from "react-router-dom"
import {SettlementDetail} from "../../components/settlement/settlement-detail"
import {getSettlementById} from "../../service/settlementService"
import {useToast} from "../../context/ToastContext"

export function SettlementDetailPage() {
  const {settlementId} = useParams()
  const {addToast} = useToast()
  const [settlement, setSettlement] = useState(null)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    const fetchSettlement = async () => {
      try {
        setIsLoading(true)
        const data = await getSettlementById(settlementId)
        setSettlement(data)
      } catch (error) {
        console.error("정산 내역 조회 오류:", error)
        setError(error.message)
        addToast({
          title: "오류 발생",
          description: "정산 내역을 불러오는데 실패했습니다.",
          variant: "destructive",
        })
      } finally {
        setIsLoading(false)
      }
    }

    fetchSettlement()
  }, [settlementId, addToast])

  if (isLoading) {
    return (
        <div className="container py-8">
          <div className="flex justify-center items-center h-64">
            <p>로딩 중...</p>
          </div>
        </div>
    )
  }

  if (error || !settlement) {
    return (
        <div className="container py-8">
          <div className="flex flex-col justify-center items-center h-64">
            <p className="text-lg font-medium text-error">오류가 발생했습니다.</p>
            <p className="text-muted">{error || "정산 내역을 찾을 수 없습니다."}</p>
          </div>
        </div>
    )
  }

  return (
      <div className="container py-8">
        <h1 className="text-2xl font-bold mb-6">정산 상세 내역</h1>
        <SettlementDetail settlement={settlement}/>
      </div>
  )
}