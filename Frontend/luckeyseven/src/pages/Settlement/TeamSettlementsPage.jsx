"use client"

import {useEffect, useState} from "react"
import {useLocation, useNavigate, useParams} from "react-router-dom"
import {SettlementList} from "../../components/settlement/settlement-list"
import {SettlementFilter} from "../../components/settlement/settlement-filter"
import {
  getListSettlements,
  getSettlementAggregation,
  getSettlementById,
  getUsers,
  settleBetweenMembers,
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

  // 화면 모드 상태 추가 (list: 정산 목록, aggregation: 정산 집계)
  const [viewMode, setViewMode] = useState("list")

  // 정산 집계 관련 상태 추가
  const [aggregationData, setAggregationData] = useState(null)
  const [isProcessing, setIsProcessing] = useState(false)

  // 정산 집계 필터 및 정렬 상태 추가
  const [filteredAggregations, setFilteredAggregations] = useState([])
  const [aggregationFilter, setAggregationFilter] = useState({
    payerName: "",
    receiverName: ""
  })
  const [aggregationSort, setAggregationSort] = useState({
    field: "amount",
    direction: "desc"
  })

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

  // 사용자 ID로 닉네임 찾기
  const getUserNickname = (userId) => {
    const user = users.find(user => user.memberId === userId)
    return user ? user.memberNickName : `사용자 ${userId}`
  }

  // 두 사용자 간 정산 처리하기
  const handleSettlementBetweenMembers = async (fromMemberId, toMemberId) => {
    try {
      setIsProcessing(true);

      // 정산 처리 API 호출
      await settleBetweenMembers(teamId, fromMemberId, toMemberId);

      // 정산 완료 후 데이터 다시 가져오기
      const newAggregation = await getSettlementAggregation(teamId);
      setAggregationData(newAggregation);

      addToast({
        title: "정산 완료",
        description: `${getUserNickname(fromMemberId)}님과 ${getUserNickname(
            toMemberId)}님의 정산이 완료되었습니다.`,
      });
    } catch (error) {
      console.error("정산 처리 오류:", error);
      console.error("오류 상세 정보:", error.response?.data || error.message);
      addToast({
        title: "오류 발생",
        description: "정산 처리 중 오류가 발생했습니다.",
        variant: "destructive",
      });
    } finally {
      setIsProcessing(false);
    }
  };

  // 정산 집계 데이터 가져오기
  const fetchAggregationData = async () => {
    try {
      setIsLoading(true);

      // 집계 정보와 사용자 정보 병렬로 가져오기 (원본 SettlementAggregationPage 방식으로 수정)
      const [aggregationResponse, usersResponse] = await Promise.all([
        getSettlementAggregation(teamId),
        getUsers(teamId)
      ]);

      setAggregationData(aggregationResponse);
      setUsers(usersResponse);
    } catch (error) {
      console.error("정산 집계 데이터 조회 오류:", error);
      setError(error.message);
      addToast({
        title: "오류 발생",
        description: "정산 집계 데이터를 불러오는데 실패했습니다.",
        variant: "destructive",
      });
    } finally {
      setIsLoading(false);
    }
  };

  // 정산 집계 필터링 및 정렬 함수
  const filterAndSortAggregations = () => {
    if (!aggregationData || !aggregationData.aggregations) {
      return [];
    }

    // 0원 초과인 항목만 필터링
    let result = aggregationData.aggregations.filter(agg => agg.amount > 0);

    // 필터 적용
    if (aggregationFilter.payerName) {
      result = result.filter(agg => {
        const nickname = getUserNickname(agg.from);
        return nickname === aggregationFilter.payerName;
      });
    }

    if (aggregationFilter.receiverName) {
      result = result.filter(agg => {
        const nickname = getUserNickname(agg.to);
        return nickname === aggregationFilter.receiverName;
      });
    }

    // 정렬 적용
    result.sort((a, b) => {
      const field = aggregationSort.field;
      const direction = aggregationSort.direction === "asc" ? 1 : -1;

      if (field === "amount") {
        return direction * (a.amount - b.amount);
      } else if (field === "payer") {
        return direction * getUserNickname(a.from).localeCompare(
            getUserNickname(b.from));
      } else if (field === "receiver") {
        return direction * getUserNickname(a.to).localeCompare(
            getUserNickname(b.to));
      }

      return 0;
    });

    return result;
  };

  // 필터 변경 핸들러
  const handleFilterChange = (e) => {
    const {name, value} = e.target;
    setAggregationFilter(prev => ({
      ...prev,
      [name]: value
    }));
  };

  // 정렬 변경 핸들러
  const handleSortChange = (field) => {
    setAggregationSort(prev => {
      if (prev.field === field) {
        // 같은 필드 클릭 시 방향 전환
        return {
          field,
          direction: prev.direction === "asc" ? "desc" : "asc"
        };
      } else {
        // 다른 필드 클릭 시 기본 내림차순
        return {
          field,
          direction: "desc"
        };
      }
    });
  };

  // 정산 집계 데이터가 업데이트될 때마다 필터링된 결과도 업데이트
  useEffect(() => {
    if (aggregationData) {
      setFilteredAggregations(filterAndSortAggregations());
    }
  }, [aggregationData, aggregationFilter, aggregationSort]);

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

  // 화면 모드가 변경될 때마다 처리
  useEffect(() => {
    if (viewMode === "aggregation") {
      fetchAggregationData();
    }
  }, [viewMode, teamId]); // teamId를 의존성 배열에 추가

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
          <h1 className="text-2xl font-bold">
            {viewMode === "list" ? "팀 정산 내역" : "정산 집계 현황"}
          </h1>
          <div className="flex gap-2">
            {viewMode === "list" ? (
                <button
                    className="btn btn-outline"
                    onClick={() => setViewMode("aggregation")}
                >
                  정산 집계 보기
                </button>
            ) : (
                <button
                    className="btn btn-outline"
                    onClick={() => setViewMode("list")}
                >
                  정산 목록 보기
                </button>
            )}
          </div>
        </div>

        {/* 필터는 정산 목록 화면일 때만 표시 */}
        {viewMode === "list" && (
            <div className="mb-6">
              <SettlementFilter users={users} expenses={expenses}
                                initialFilters={filters} teamId={teamId}/>
            </div>
        )}

        {viewMode === "list" ? (
            <SettlementList
                settlements={settlements.content}
                onSettlementClick={handleSettlementClick}
            />
        ) : (
            <div className="card">

              {/* 정산 집계 필터 UI 추가 */}
              <div className="card">
                <div className="card-content">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div className="form-group">
                      <label htmlFor="payer-filter" className="form-label">
                        결제자
                      </label>
                      <select
                          id="payer-filter"
                          className="form-select"
                          name="payerName"
                          value={aggregationFilter.payerName}
                          onChange={handleFilterChange}
                      >
                        <option value="">전체</option>
                        {users.map((user) => (
                            <option key={`payer-${user.memberId}`}
                                    value={user.memberNickName}>
                              {user.memberNickName}
                            </option>
                        ))}
                      </select>
                    </div>
                    <div className="form-group">
                      <label htmlFor="receiver-filter" className="form-label">
                        정산자
                      </label>
                      <select
                          id="receiver-filter"
                          className="form-select"
                          name="receiverName"
                          value={aggregationFilter.receiverName}
                          onChange={handleFilterChange}
                      >
                        <option value="">전체</option>
                        {users.map((user) => (
                            <option key={`receiver-${user.memberId}`}
                                    value={user.memberNickName}>
                              {user.memberNickName}
                            </option>
                        ))}
                      </select>
                    </div>
                  </div>

                  <div className="flex justify-end mt-4 space-x-2">
                    <button
                        className="btn btn-outline btn-sm"
                        onClick={() => setAggregationFilter({
                          payerName: "",
                          receiverName: ""
                        })}
                    >
                      필터 초기화
                    </button>
                  </div>
                </div>
              </div>

              <div className="card-content">
                {aggregationData && aggregationData.aggregations
                && aggregationData.aggregations.length > 0 ? (
                    <div>
                      <div
                          className="grid grid-cols-1 md:grid-cols-2 gap-4 p-4">
                      </div>

                      <div className="mt-4">
                        <div className="overflow-x-auto">
                          <table className="table w-full border-collapse">
                            <thead>
                            <tr className="bg-base-200">
                              <th className="border-b-2 border-base-300 p-3">
                                <div
                                    className="flex items-center cursor-pointer"
                                    onClick={() => handleSortChange("payer")}
                                >
                                  지불자
                                  {aggregationSort.field === "payer" && (
                                      <span className="ml-1">
                                      {aggregationSort.direction === "asc" ? "↑"
                                          : "↓"}
                                    </span>
                                  )}
                                </div>
                              </th>
                              <th className="border-b-2 border-base-300 p-3">
                                <div
                                    className="flex items-center cursor-pointer"
                                    onClick={() => handleSortChange("receiver")}
                                >
                                  수령자
                                  {aggregationSort.field === "receiver" && (
                                      <span className="ml-1">
                                      {aggregationSort.direction === "asc" ? "↑"
                                          : "↓"}
                                    </span>
                                  )}
                                </div>
                              </th>
                              <th className="border-b-2 border-base-300 p-3">
                                <div
                                    className="flex items-center cursor-pointer"
                                    onClick={() => handleSortChange("amount")}
                                >
                                  금액
                                  {aggregationSort.field === "amount" && (
                                      <span className="ml-1">
                                      {aggregationSort.direction === "asc" ? "↑"
                                          : "↓"}
                                    </span>
                                  )}
                                </div>
                              </th>
                              <th className="border-b-2 border-base-300 p-3">작업</th>
                            </tr>
                            </thead>
                            <tbody>
                            {filteredAggregations.length > 0 ? (
                                filteredAggregations.map((agg, index) => (
                                    <tr key={`${agg.from}-${agg.to}-${index}`}
                                        className="border-b border-base-200 hover:bg-base-100">
                                      <td className="p-3">
                                    <span className="font-medium text-red-500">
                                      {getUserNickname(agg.from)}
                                    </span>
                                      </td>
                                      <td className="p-3">
                                        <div className="flex items-center">
                                          <span
                                              className="text-gray-500 mx-2">→</span>
                                          <span
                                              className="font-medium text-green-500">
                                        {getUserNickname(agg.to)}
                                      </span>
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
                                            onClick={() => handleSettlementBetweenMembers(
                                                agg.from, agg.to)}
                                            disabled={isProcessing}
                                        >
                                          {isProcessing ? "처리 중..." : "정산 완료"}
                                        </button>
                                      </td>
                                    </tr>
                                ))
                            ) : (
                                <tr>
                                  <td colSpan="4" className="text-center py-6">
                                    <p className="text-muted">필터 조건에 맞는 정산 내역이
                                      없습니다.</p>
                                  </td>
                                </tr>
                            )}
                            </tbody>
                          </table>
                        </div>
                      </div>
                    </div>
                ) : (
                    <p className="text-center text-muted py-4">집계된 정산 정보가
                      없습니다.</p>
                )}
              </div>

              <div className="card-footer p-4 border-t">
                <div className="mt-4 p-4 bg-base-200 rounded-lg">
                  <h2 className="text-lg font-semibold mb-2">정산 집계 안내</h2>
                  <ul className="list-disc pl-5 space-y-1">
                    <li>위 표는 각 팀원 간 정산해야 할 총 금액을 보여줍니다.</li>
                    <li><strong>지불자</strong>는 <strong>수령자</strong>에게 해당 금액을 지불해야
                      합니다.
                    </li>
                    <li>테이블 헤더를 클릭하여 정렬 기준과 방향을 변경할 수 있습니다.</li>
                    <li>"정산 완료" 버튼을 클릭하면 해당 두 사람 간의 모든 정산이 완료 처리됩니다.</li>
                  </ul>
                </div>
              </div>
            </div>
        )}

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
                                      <p className="mt-1 font-medium">{selectedSettlement.payerNickName
                                          || "알 수 없음"}</p>
                                    </div>
                                    <div>
                                      <h3 className="text-sm text-muted">정산자
                                        정보</h3>
                                      <p className="mt-1 font-medium">{selectedSettlement.settlerNickName
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
