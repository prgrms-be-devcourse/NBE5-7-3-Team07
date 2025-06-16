"use client"

import {useEffect, useState} from "react"
import {useNavigate, useParams} from "react-router-dom"
import {
  getSettlementAggregation,
  getUsers,
  settleBetweenMembers
} from "../../service/settlementService"
import {useToast} from "../../context/ToastContext"
import {useRecoilValue} from "recoil"
import {currentTeamIdState} from "../../recoil/atoms/teamAtoms"

export function SettlementAggregationPage() {
  const recoilTeamId = useRecoilValue(currentTeamIdState)
  const paramTeamId = useParams().teamId
  const teamId = recoilTeamId || paramTeamId
  const navigate = useNavigate()
  const {addToast} = useToast()

  const [aggregationData, setAggregationData] = useState(null)
  const [users, setUsers] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState(null)
  const [isProcessing, setIsProcessing] = useState(false)
  const [selectedSettlement, setSelectedSettlement] = useState(null)

  useEffect(() => {
    const fetchData = async () => {
      try {
        setIsLoading(true)

        // 집계 정보와 사용자 정보 병렬로 가져오기
        const [aggregationResponse, usersResponse] = await Promise.all([
          getSettlementAggregation(teamId),
          getUsers(teamId)
        ])

        setAggregationData(aggregationResponse)
        setUsers(usersResponse)
      } catch (error) {
        console.error("정산 집계 정보 조회 오류:", error)
        setError(error.message)
        addToast({
          title: "오류 발생",
          description: "정산 집계 정보를 불러오는데 실패했습니다.",
          variant: "destructive",
        })
      } finally {
        setIsLoading(false)
      }
    }

    fetchData()
  }, [teamId, addToast])

  // 사용자 ID로 닉네임 찾기
  const getUserNickname = (userId) => {
    const user = users.find(user => user.id === userId)
    return user ? user.memberNickName : `사용자 ${userId}`
  }

  // 정산 처리하기
  const handleSettlement = async (fromMemberId, toMemberId) => {
    try {
      setIsProcessing(true)
      setSelectedSettlement({from: fromMemberId, to: toMemberId})

      console.log('정산 처리 시작:', {teamId, fromMemberId, toMemberId})
      await settleBetweenMembers(teamId, fromMemberId, toMemberId)
      console.log('정산 처리 성공')

      // 정산 완료 후 데이터 다시 가져오기
      const newAggregation = await getSettlementAggregation(teamId)
      setAggregationData(newAggregation)

      addToast({
        title: "정산 완료",
        description: `${getUserNickname(fromMemberId)}님과 ${getUserNickname(
            toMemberId)}님의 정산이 완료되었습니다.`,
      })
    } catch (error) {
      console.error("정산 처리 오류:", error)
      console.error("오류 상세 정보:", error.response?.data || error.message)
      addToast({
        title: "오류 발생",
        description: "정산 처리 중 오류가 발생했습니다.",
        variant: "destructive",
      })
    } finally {
      setIsProcessing(false)
      setSelectedSettlement(null)
    }
  }

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
        <div className="flex justify-between items-center mb-6">
          <h1 className="text-2xl font-bold">정산 집계 현황</h1>
          <button
              className="btn btn-outline"
              onClick={() => navigate(`/teams/${teamId}/settlements`)}
          >
            정산 목록 보기
          </button>
        </div>

        {(!aggregationData || !aggregationData.aggregations
            || aggregationData.aggregations.length === 0) ? (
            <div className="bg-base-200 rounded-lg p-8 text-center">
              <p className="text-lg mb-2">집계된 정산 정보가 없습니다.</p>
              <p className="text-muted">아직 정산이 필요한 내역이 없거나 모든 정산이 완료되었습니다.</p>
            </div>
        ) : (
            <div className="overflow-x-auto">
              <table className="table w-full border-collapse">
                <thead>
                <tr className="bg-base-200">
                  <th className="border-b-2 border-base-300 p-3">지불자</th>
                  <th className="border-b-2 border-base-300 p-3">수령자</th>
                  <th className="border-b-2 border-base-300 p-3">금액</th>
                  <th className="border-b-2 border-base-300 p-3">작업</th>
                </tr>
                </thead>
                <tbody>
                {aggregationData.aggregations.map((agg, index) => {
                  // 0원인 경우는 표시하지 않음
                  if (agg.amount <= 0) {
                    return null;
                  }

                  return (
                      <tr key={`${agg.from}-${agg.to}-${index}`}
                          className="border-b border-base-200 hover:bg-base-100">
                        <td className="p-3">
                          <span
                              className="font-medium text-red-500">{getUserNickname(
                              agg.from)}</span>
                        </td>
                        <td className="p-3">
                          <div className="flex items-center">
                            <span className="text-gray-500 mx-2">→</span>
                            <span
                                className="font-medium text-green-500">{getUserNickname(
                                agg.to)}</span>
                          </div>
                        </td>
                        <td className="p-3 font-semibold">
                          {new Intl.NumberFormat('ko-KR', {
                            style: 'currency',
                            currency: 'KRW'
                          }).format(agg.amount)}
                        </td>
                        <td className="p-3">
                          <button
                              className="btn btn-sm btn-primary"
                              onClick={() => handleSettlement(agg.from, agg.to)}
                              disabled={isProcessing && selectedSettlement?.from
                                  === agg.from && selectedSettlement?.to
                                  === agg.to}
                          >
                            {isProcessing && selectedSettlement?.from
                            === agg.from && selectedSettlement?.to === agg.to
                                ? "처리 중..."
                                : "정산 완료"}
                          </button>
                        </td>
                      </tr>
                  );
                }).filter(Boolean)}
                </tbody>
              </table>
            </div>
        )}

        <div className="mt-8 p-4 bg-base-200 rounded-lg">
          <h2 className="text-lg font-semibold mb-2">정산 집계 안내</h2>
          <ul className="list-disc pl-5 space-y-1">
            <li>위 표는 각 팀원 간 정산해야 할 총 금액을 보여줍니다.</li>
            <li><strong>지불자</strong>는 <strong>수령자</strong>에게 해당 금액을 지불해야 합니다.
            </li>
            <li>"정산 완료" 버튼을 클릭하면 해당 두 사람 간의 모든 정산이 완료 처리됩니다.</li>
            <li>정산이 완료된 내역은 목록에서 제외됩니다.</li>
          </ul>
        </div>
      </div>
  )
}
