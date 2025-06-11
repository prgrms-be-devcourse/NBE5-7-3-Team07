"use client"

import {useEffect, useState} from "react"
import {useParams} from "react-router-dom"
import {SettlementForm} from "../../components/settlement/settlement-form"
import {getSettlementById, getUsers} from "../../service/settlementService"
import {useToast} from "../../context/ToastContext"
import {getAllExpense} from "../../service/ExpenseService";
import {useRecoilValue} from "recoil";
import {currentTeamIdState} from "../../recoil/atoms/teamAtoms";

export function SettlementEditPage() {
  const recoilTeamId = useRecoilValue(currentTeamIdState)
  const paramTeamId = useParams().teamId
  const teamId = recoilTeamId || paramTeamId
  const {settlementId} = useParams()
  const {addToast} = useToast()
  const [settlement, setSettlement] = useState(null)
  const [users, setUsers] = useState([])
  const [expenses, setExpenses] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    const fetchData = async () => {
      try {
        setIsLoading(true)
        const [settlementData, usersData, expensesData] = await Promise.all([
          getSettlementById(settlementId),
          getUsers(teamId),
          getAllExpense(teamId),
        ])
        setSettlement(settlementData)
        setUsers(usersData)
        setExpenses(expensesData)
      } catch (error) {
        console.error("데이터 조회 오류:", error)
        setError(error.message)
        addToast({
          title: "오류 발생",
          description: "데이터를 불러오는데 실패했습니다.",
          variant: "destructive",
        })
      } finally {
        setIsLoading(false)
      }
    }

    fetchData()
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
        <h1 className="text-2xl font-bold mb-6">정산 내역 수정</h1>
        <SettlementForm settlement={settlement} users={users}
                        expenses={expenses} isEditing={true}/>
      </div>
  )
}
