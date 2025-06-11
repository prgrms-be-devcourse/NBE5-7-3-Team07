"use client"

import {useEffect, useState} from "react"
import {SettlementForm} from "../../components/settlement/settlement-form"
import {getUsers} from "../../service/settlementService"
import {useToast} from "../../context/ToastContext"
import {getAllExpense} from "../../service/ExpenseService";
import {useParams} from "react-router-dom";
import {useRecoilValue} from "recoil";
import {currentTeamIdState} from "../../recoil/atoms/teamAtoms";

export function SettlementNewPage() {
  const recoilTeamId = useRecoilValue(currentTeamIdState)
  const paramTeamId = useParams().teamId
  const teamId = recoilTeamId || paramTeamId
  const {addToast} = useToast()
  const [users, setUsers] = useState([])
  const [expenses, setExpenses] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    const fetchData = async () => {
      try {
        setIsLoading(true)
        const [usersData, expensesData] = await Promise.all([
          getUsers(teamId),
          getAllExpense(teamId)
        ])
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
  }, [addToast])

  if (isLoading) {
    return (
        <div className="container py-8">
          <div className="flex justify-center items-center h-64">
            <p>로딩 중...</p>
          </div>
        </div>
    )
  }

  if (error) {
    return (
        <div className="container py-8">
          <div className="flex flex-col justify-center items-center h-64">
            <p className="text-lg font-medium text-error">오류가 발생했습니다.</p>
            <p className="text-muted">{error}</p>
          </div>
        </div>
    )
  }

  return (
      <div className="container py-8">
        <h1 className="text-2xl font-bold mb-6">새 정산 생성</h1>
        <SettlementForm users={users} expenses={expenses}/>
      </div>
  )
}
