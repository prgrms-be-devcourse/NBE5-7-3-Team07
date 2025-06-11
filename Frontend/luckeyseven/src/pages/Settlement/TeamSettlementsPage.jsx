"use client"

import {useEffect, useState} from "react"
import {useLocation, useNavigate, useParams} from "react-router-dom"
import {SettlementList} from "../../components/settlement/settlement-list"
import {SettlementFilter} from "../../components/settlement/settlement-filter"
import {
  getListSettlements,
  getSettlementById,
  getUsers,
  updateSettlement
} from "../../service/settlementService"
import {useToast} from "../../context/ToastContext"
import {getAllExpense} from "../../service/ExpenseService";
import {useRecoilValue} from "recoil";
import {currentTeamIdState} from "../../recoil/atoms/teamAtoms";
import {SettlementForm} from "../../components/settlement/settlement-form"
import {StatusBadge} from "../../components/common/StatusBadge";

export function TeamSettlementsPage() {
  const recoilTeamId = useRecoilValue(currentTeamIdState)
  const paramTeamId = useParams().teamId
  const teamId = recoilTeamId || paramTeamId
  const location = useLocation()
  const navigate = useNavigate()
  const {addToast} = useToast()

  const [settlements, setSettlements] = useState([])
  const [users, setUsers] = useState([])
  const [expenses, setExpenses] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState(null)

  // 모달 관련 상태 추가
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [selectedSettlement, setSelectedSettlement] = useState(null)
  const [isModalLoading, setIsModalLoading] = useState(false)
  const [modalMode, setModalMode] = useState("detail") // "detail" 또는 "edit"
  const [isActionLoading, setIsActionLoading] = useState(false)

  // 페이징 관련 상태
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)

  // URL 쿼리 파라미터 파싱
  const searchParams = new URLSearchParams(location.search)
  const filters = {
    payerId: searchParams.get("payerId") || "",
    settlerId: searchParams.get("settlerId") || "",
    expenseId: searchParams.get("expenseId") || "",
    isSettled: searchParams.get("isSettled") || "",
  }

  // 페이징 정보 파싱
  const page = parseInt(searchParams.get("page") || "0", 10)
  const size = parseInt(searchParams.get("size") || "10", 10)
  const sort = searchParams.get("sort") || "createdAt,DESC"

  // 페이지 변경 핸들러
  const handlePageChange = (newPage) => {
    const params = new URLSearchParams(location.search)
    params.set("page", newPage.toString())
    navigate(`${location.pathname}?${params.toString()}`)
  }

  // 페이지 크기 변경 핸들러
  const handlePageSizeChange = (newSize) => {
    const params = new URLSearchParams(location.search)
    params.set("size", newSize.toString())
    params.set("page", "0") // 페이지 크기 변경 시 첫 페이지로 돌아가기
    navigate(`${location.pathname}?${params.toString()}`)
  }

  // 정산 항목 클릭 핸들러
  const handleSettlementClick = async (settlementId) => {
    try {
      setIsModalLoading(true)
      setIsModalOpen(true)
      setModalMode("detail")
      const settlement = await getSettlementById(settlementId)
      setSelectedSettlement(settlement)
    } catch (error) {
      console.error("정산 내역 조회 오류:", error)
      addToast({
        title: "오류 발생",
        description: "정산 내역을 불러오는데 실패했습니다.",
        variant: "destructive",
      })
    } finally {
      setIsModalLoading(false)
    }
  }

  // 모달 닫기 핸들러
  const handleCloseModal = () => {
    setIsModalOpen(false)
    setSelectedSettlement(null)
    setModalMode("detail")
  }

  // 정산 편집 모드로 전환
  const handleEditMode = () => {
    setModalMode("edit")
  }

  // 정산 상태 변경 (완료/취소)
  const handleSettlementStatusChange = async () => {
    if (!selectedSettlement) {
      return
    }

    try {
      setIsActionLoading(true)
      const newStatus = !selectedSettlement.isSettled

      // updateSettlement 함수 호출 (3번째 매개변수가 toggleSettled를 의미)
      const updatedSettlement = await updateSettlement(
          selectedSettlement.id,
          {},
          true
      )

      // 토스트 메시지 표시
      addToast({
        title: newStatus ? "정산 완료" : "정산 완료 취소",
        description: newStatus ? "정산이 완료 처리되었습니다." : "정산 완료가 취소되었습니다.",
      })

      // 선택된 정산 업데이트
      const updated = {
        ...selectedSettlement,
        isSettled: newStatus,
        updatedAt: updatedSettlement.updatedAt || new Date().toISOString()
      }
      setSelectedSettlement(updated)

      // 목록에서도 업데이트
      setSettlements(prevSettlements => {
        return {
          ...prevSettlements,
          content: prevSettlements.content.map(item =>
              item.id === updated.id ? updated : item
          )
        }
      })
    } catch (error) {
      console.error("정산 상태 변경 오류:", error)
      addToast({
        title: "오류 발생",
        description: "정산 상태 변경 중 오류가 발생했습니다.",
        variant: "destructive",
      })
    } finally {
      setIsActionLoading(false)
    }
  }

  // 정산 편집 완료 핸들러
  const handleFormSubmit = (updatedSettlement) => {
    setSelectedSettlement(updatedSettlement)
    setModalMode("detail")

    // 목록 업데이트
    setSettlements(prevSettlements => {
      return {
        ...prevSettlements,
        content: prevSettlements.content.map(item =>
            item.id === updatedSettlement.id ? updatedSettlement : item
        )
      }
    })

    addToast({
      title: "정산 수정 완료",
      description: "정산 내역이 성공적으로 수정되었습니다.",
    })
  }

  useEffect(() => {
    const fetchData = async () => {
      try {
        setIsLoading(true)
        const settlementResponse = await getListSettlements(teamId, page, size,
            sort, filters)
        setSettlements(settlementResponse)
        const usersResponse = await getUsers(teamId)
        setUsers(usersResponse)
        const expensesResponse = await getAllExpense(teamId)
        setExpenses(expensesResponse)

        // 페이징 메타데이터 설정
        setTotalPages(settlementResponse.totalPages)
        setTotalElements(settlementResponse.totalElements)
      } catch (error) {
        console.error("팀 정산 내역 조회 오류:", error)
        setError(error.message)
        addToast({
          title: "오류 발생",
          description: "팀 정산 내역을 불러오는데 실패했습니다.",
          variant: "destructive",
        })
      } finally {
        setIsLoading(false)
      }
    }

    fetchData()
  }, [teamId, page, size, sort, location.search, addToast])

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
          <h1 className="text-2xl font-bold">팀 정산 내역</h1>
        </div>

        <div className="mb-6">
          <SettlementFilter users={users} expenses={expenses}
                            initialFilters={filters} teamId={teamId}/>
        </div>

        <SettlementList
            settlements={settlements.content}
            onSettlementClick={handleSettlementClick}
        />

        {/* 페이지네이션 컴포넌트 */}
        <div className="flex justify-between items-center mt-6">
          <div className="text-sm text-muted">
            총 {totalElements}개 항목 중 {page * size + 1}-{Math.min(
              (page + 1) * size, totalElements)}
          </div>

          <div className="flex gap-2">
            <select
                className="select select-sm select-bordered"
                value={size}
                onChange={(e) => handlePageSizeChange(
                    parseInt(e.target.value, 10))}
            >
              <option value="5">5개씩 보기</option>
              <option value="10">10개씩 보기</option>
              <option value="20">20개씩 보기</option>
              <option value="50">50개씩 보기</option>
            </select>

            <div className="join">
              <button
                  className="join-item btn btn-sm"
                  disabled={page === 0}
                  onClick={() => handlePageChange(0)}
              >
                «
              </button>
              <button
                  className="join-item btn btn-sm"
                  disabled={page === 0}
                  onClick={() => handlePageChange(page - 1)}
              >
                ‹
              </button>

              {/* 페이지 번호 버튼 */}
              {Array.from({length: Math.min(5, totalPages)}, (_, i) => {
                const pageNum = page - 2 + i

                // 음수 페이지 번호는 표시하지 않음
                if (pageNum < 0) {
                  return null
                }

                // 총 페이지 수를 초과하는 페이지 번호는 표시하지 않음
                if (pageNum >= totalPages) {
                  return null
                }

                return (
                    <button
                        key={pageNum}
                        className={`join-item btn btn-sm ${pageNum === page
                            ? 'btn-active' : ''}`}
                        onClick={() => handlePageChange(pageNum)}
                    >
                      {pageNum + 1}
                    </button>
                )
              })}

              <button
                  className="join-item btn btn-sm"
                  disabled={page >= totalPages - 1}
                  onClick={() => handlePageChange(page + 1)}
              >
                ›
              </button>
              <button
                  className="join-item btn btn-sm"
                  disabled={page >= totalPages - 1}
                  onClick={() => handlePageChange(totalPages - 1)}
              >
                »
              </button>
            </div>
          </div>
        </div>

        {/* 모달창 */}
        {isModalOpen && (
            <div
                className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
              <div
                  className="bg-white rounded-lg shadow-xl w-full max-w-4xl max-h-[90vh] overflow-y-auto">
                <div className="p-4 flex justify-end">
                  <button
                      className="btn btn-sm btn-circle"
                      onClick={handleCloseModal}
                  >
                    ✕
                  </button>
                </div>

                {isModalLoading ? (
                    <div className="flex justify-center items-center h-64">
                      <p>로딩 중...</p>
                    </div>
                ) : selectedSettlement ? (
                    <div className="p-4">
                      {modalMode === "detail" ? (
                          <>
                            <div className="card">
                              <div className="card-header">
                                <div
                                    className="flex justify-between items-center">
                                  <h3 className="card-title">정산 #{String(
                                      selectedSettlement.id).substring(0,
                                      8)}</h3>
                                  <div>
                                    <StatusBadge
                                        isSettled={selectedSettlement.isSettled}/>
                                  </div>
                                </div>
                              </div>
                              <div className="card-content space-y-6">
                                <div
                                    className="grid grid-cols-1 md:grid-cols-2 gap-6">
                                  <div className="space-y-4">
                                    <div>
                                      <h3 className="text-sm text-muted">결제자
                                        정보</h3>
                                      <p className="mt-1 font-medium">{selectedSettlement.payerNickname
                                          || "알 수 없음"}</p>
                                    </div>
                                    <div>
                                      <h3 className="text-sm text-muted">정산자
                                        정보</h3>
                                      <p className="mt-1 font-medium">{selectedSettlement.settlerNickname
                                          || "알 수 없음"}</p>
                                    </div>
                                  </div>

                                  <div className="space-y-4">
                                    <div>
                                      <h3 className="text-sm text-muted">연관
                                        지출</h3>
                                      <p className="mt-1 font-medium">{selectedSettlement.expenseDescription
                                          || "알 수 없음"}</p>
                                      {selectedSettlement.expense && (
                                          <p className="text-sm text-muted">
                                            {new Date(
                                                selectedSettlement.expense.date).toLocaleDateString()} ·
                                            {new Intl.NumberFormat('ko-KR', {
                                              style: 'currency',
                                              currency: 'KRW'
                                            }).format(
                                                selectedSettlement.expense.amount)}
                                          </p>
                                      )}
                                    </div>

                                    <div>
                                      <h3 className="text-sm text-muted">정산
                                        금액</h3>
                                      <p className="mt-1 text-xl font-bold">
                                        {new Intl.NumberFormat('ko-KR', {
                                          style: 'currency',
                                          currency: 'KRW'
                                        }).format(selectedSettlement.amount)}
                                      </p>
                                    </div>
                                  </div>
                                </div>

                                <div className="pt-4 border-t">
                                  <h3 className="text-sm text-muted mb-2">정산
                                    정보</h3>
                                  <div
                                      className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                    <div>
                                      <p className="text-sm text-muted">생성일</p>
                                      <p>{new Date(
                                          selectedSettlement.createdAt).toLocaleString()}</p>
                                    </div>
                                    <div>
                                      <p className="text-sm text-muted">수정일</p>
                                      <p>{new Date(
                                          selectedSettlement.updatedAt).toLocaleString()}</p>
                                    </div>
                                  </div>
                                </div>
                              </div>
                              <div className="card-footer">
                                <div className="flex space-x-2">
                                  <button
                                      className="btn btn-outline"
                                      onClick={handleEditMode}
                                      disabled={isActionLoading}
                                  >
                                    수정하기
                                  </button>
                                  <button
                                      className={`btn ${selectedSettlement.isSettled
                                          ? 'btn-outline' : 'btn-primary'}`}
                                      onClick={handleSettlementStatusChange}
                                      disabled={isActionLoading}
                                  >
                                    {isActionLoading ? "처리 중..." : (
                                        selectedSettlement.isSettled
                                            ? "정산 완료 취소" : "정산 완료 처리"
                                    )}
                                  </button>
                                </div>
                              </div>
                            </div>
                          </>
                      ) : (
                          <div className="card">
                            <div className="card-header">
                              <h3 className="card-title">정산 내역 수정</h3>
                            </div>
                            <SettlementForm
                                settlement={selectedSettlement}
                                users={users}
                                expenses={expenses}
                                isEditing={true}
                                onFormSubmit={handleFormSubmit}
                                onCancel={() => setModalMode("detail")}
                            />
                          </div>
                      )}
                    </div>
                ) : (
                    <div
                        className="flex flex-col justify-center items-center h-64">
                      <p className="text-lg font-medium text-error">정산 내역을 불러올 수
                        없습니다.</p>
                    </div>
                )}
              </div>
            </div>
        )}
      </div>
  )
}