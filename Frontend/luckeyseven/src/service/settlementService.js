import {privateApi} from "./ApiService";

export const getListSettlements = async (teamId, page = 0, size = 10,
    sort = 'createdAt,DESC', filters = {}) => {
  try {
    const response = await privateApi.get(`/api/teams/${teamId}/settlements`,
        {
          params: {
            ...filters,
            page,
            size,
            sort
          }
        });
    return response.data;
  } catch (error) {
    console.error("정산 내역 조회 오류:", error);
    throw error;
  }
};

// 특정 정산 내역 조회
export const getSettlementById = async (id) => {
  try {
    const response = await privateApi.get(`/api/settlements/${id}`);
    return response.data;
  } catch (error) {
    console.error(`정산 내역(ID: ${id}) 조회 오류:`, error);
    throw error;
  }
};

// 정산 생성
export const createSettlement = async (settlementData) => {
  try {
    const response = await privateApi.post("/api/settlements", settlementData);
    return response.data;
  } catch (error) {
    console.error("정산 생성 오류:", error);
    throw error;
  }
};

// 정산 수정
export const updateSettlement = async (id, settlementData,
    settledOnly = false) => {
  try {
    // settledOnly가 true인 경우 정산 완료 처리만 수행
    if (settledOnly) {
      const response = await privateApi.patch(`/api/settlements/${id}`, {},
          {params: {settledOnly: true}});
      return response.data;
    } else {
      // 전체 정산 정보 업데이트
      const response = await privateApi.patch(`/api/settlements/${id}`,
          settlementData);
      return response.data;
    }
  } catch (error) {
    console.error(`정산 수정(ID: ${id}) 오류:`, error);
    throw error;
  }
};

// 정산 삭제
export const deleteSettlement = async (id) => {
  try {
    const response = await privateApi.delete(`/api/settlements/${id}`);
    return response.data;
  } catch (error) {
    console.error(`정산 삭제(ID: ${id}) 오류:`, error);
    throw error;
  }
};

// 사용자 목록 조회
export const getUsers = async (teamId) => {
  try {
    const response = await privateApi.get(`/api/teams/${teamId}/members`);
    return response.data;
  } catch (error) {
    console.error("사용자 목록 조회 오류:", error);
    throw error;
  }
};
/**
 * 모든 정산 내역을 페이지네이션을 통해 전체 조회하는 함수
 * @param {string|number} teamId 팀 ID
 * @param {Object} filters 필터링 조건 (payerId, settlerId, expenseId, isSettled 등)
 * @param {string} sort 정렬 조건, 기본값: 'createdAt,DESC'
 * @returns {Promise<{settlements: Array, users: Array, expenses: Array}>} 모든 정산 내역과 관련 데이터
 */
export const getAllSettlements = async (teamId, filters = {},
    sort = 'createdAt,DESC') => {
  try {
    const allSettlements = [];

    let page = 0;
    const size = 50;
    let firstResponseData = null;
    let hasMore = true;

    while (hasMore) {
      const response = await getListSettlements(teamId, page, size, sort,
          filters);

      if (page === 0) {
        firstResponseData = response;
      }

      const {content, last} = response;

      if (!content || content.length === 0) {
        hasMore = false;
      } else {
        allSettlements.push(...content);
        hasMore = !last;
        page++;
      }
    }

    return allSettlements

  } catch (error) {
    console.error("모든 정산 내역 조회 오류:", error);
    throw error;
  }
};
/**
 * 팀의 정산 집계 정보를 조회하는 함수
 * @param {string|number} teamId 팀 ID
 * @returns {Promise<Object>} 팀원 간 정산 집계 정보
 */
export const getSettlementAggregation = async (teamId) => {
  try {
    const response = await privateApi.get(
        `/api/teams/${teamId}/settlements/aggregation`);
    return response.data;
  } catch (error) {
    console.error("정산 집계 정보 조회 오류:", error);
    throw error;
  }
};

/**
 * 두 팀원 간의 모든 정산을 완료 처리하는 함수
 * @param {string|number} teamId 팀 ID
 * @param {string|number} fromMemberId 정산 지불자 ID
 * @param {string|number} toMemberId 정산 수령자 ID
 * @returns {Promise<void>}
 */
export const settleBetweenMembers = async (teamId, fromMemberId,
    toMemberId) => {
  try {
    await privateApi.post(`/api/teams/${teamId}/settlements`, {
      from: fromMemberId,
      to: toMemberId
    });
  } catch (error) {
    console.error(`두 사용자(${fromMemberId}, ${toMemberId}) 간 정산 완료 처리 오류:`,
        error);
    throw error;
  }
};
