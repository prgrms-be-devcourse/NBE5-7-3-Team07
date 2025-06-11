import {expenses, settlements, users} from "./mockData"

// 지연 시간 시뮬레이션 (ms)
const DELAY = 500

// 모든 정산 내역 조회
export const getAllSettlements = async (filters = {}) => {
  return new Promise((resolve) => {
    setTimeout(() => {
      let filteredSettlements = [...settlements]

      // 필터 적용
      if (filters.payerId && filters.payerId !== "all") {
        filteredSettlements = filteredSettlements.filter(
            (s) => s.payerId === filters.payerId)
      }

      if (filters.settlerId && filters.settlerId !== "all") {
        filteredSettlements = filteredSettlements.filter(
            (s) => s.settlerId === filters.settlerId)
      }

      if (filters.expenseId && filters.expenseId !== "all") {
        filteredSettlements = filteredSettlements.filter(
            (s) => s.expenseId === filters.expenseId)
      }

      if (filters.isSettled !== undefined && filters.isSettled !== null
          && filters.isSettled !== "all") {
        const isSettledBool = filters.isSettled === "true" || filters.isSettled
            === true
        filteredSettlements = filteredSettlements.filter(
            (s) => s.isSettled === isSettledBool)
      }

      resolve({
        settlements: filteredSettlements,
        users,
        expenses,
      })
    }, DELAY)
  })
}

// 특정 정산 내역 조회
export const getSettlementById = async (id) => {
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      const settlement = settlements.find((s) => s.id === id)
      if (settlement) {
        resolve(settlement)
      } else {
        reject(new Error("정산 내역을 찾을 수 없습니다."))
      }
    }, DELAY)
  })
}

// 정산 생성
export const createSettlement = async (settlementData) => {
  return new Promise((resolve) => {
    setTimeout(() => {
      const newSettlement = {
        id: `settlement${Date.now()}`,
        ...settlementData,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
        payer: users.find((u) => u.id === settlementData.payerId),
        settler: users.find((u) => u.id === settlementData.settlerId),
        expense: expenses.find((e) => e.id === settlementData.expenseId),
      }

      settlements.push(newSettlement)
      resolve(newSettlement)
    }, DELAY)
  })
}

// 정산 수정
export const updateSettlement = async (id, settlementData,
    settledOnly = false) => {
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      const index = settlements.findIndex((s) => s.id === id)
      if (index === -1) {
        reject(new Error("정산 내역을 찾을 수 없습니다."))
        return
      }

      if (settledOnly) {
        // 정산 완료 처리만 수행
        settlements[index] = {
          ...settlements[index],
          isSettled: true,
          updatedAt: new Date().toISOString(),
        }
      } else {
        // 전체 정산 정보 업데이트
        settlements[index] = {
          ...settlements[index],
          ...settlementData,
          updatedAt: new Date().toISOString(),
          payer: settlementData.payerId ? users.find(
                  (u) => u.id === settlementData.payerId)
              : settlements[index].payer,
          settler: settlementData.settlerId
              ? users.find((u) => u.id === settlementData.settlerId)
              : settlements[index].settler,
          expense: settlementData.expenseId
              ? expenses.find((e) => e.id === settlementData.expenseId)
              : settlements[index].expense,
        }
      }

      resolve(settlements[index])
    }, DELAY)
  })
}

// 정산 삭제
export const deleteSettlement = async (id) => {
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      const index = settlements.findIndex((s) => s.id === id)
      if (index === -1) {
        reject(new Error("정산 내역을 찾을 수 없습니다."))
        return
      }

      const deletedSettlement = settlements[index]
      settlements.splice(index, 1)
      resolve(deletedSettlement)
    }, DELAY)
  })
}

// 사용자 목록 조회
export const getUsers = async () => {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve(users)
    }, DELAY)
  })
}

// 지출 목록 조회
export const getExpenses = async () => {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve(expenses)
    }, DELAY)
  })
}
