import React, { useEffect, useState } from "react";
import axios from 'axios';
import { useParams } from "react-router-dom";
import SetBudgetDialog from "./components/set-budget-dialog";
import AddBudgetDialog from "./components/add-budget-dialog";
import EditBudgetDialog from "./components/edit-budget-dialog";
import PageHeaderControls from "../../components/PageHeaderControls";
import { setBudgetInitialized } from "../../service/ApiService";
import { currentTeamIdState } from "../../recoil/atoms/teamAtoms";
import { useRecoilValue } from "recoil";
import { SafeFormatterUtil } from './utils/SafeFormatterUtil';

export function BudgetPage() {
  const teamId = useRecoilValue(currentTeamIdState);
  const [dialogType, setDialogType] = useState(null); // 'set' | 'edit' | 'add' | null
  const [budget, setBudget] = useState(null);
  const [loading, setLoading] = useState(true);

  const token = localStorage.getItem("accessToken");

  const handleClose = () => setDialogType(null);

  const handleUpdate = async (updatePayload) => {
    try {
      const res = await fetch(`/api/teams/${teamId}/budget`, {
        method: "PATCH",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${token}`,
        },
        body: JSON.stringify(updatePayload),
      });

      if (!res.ok) {
        if (res.status === 404) {
          alert("예산 정보를 찾을 수 없습니다.");
        } else {
          alert("예산 업데이트에 실패했습니다.");
        }
        return;
      }

      const updatedBudget = await res.json();
      setBudget(updatedBudget);
      setBudgetInitialized(true); // ApiService의 예산 초기화 상태 업데이트
      setDialogType(null);
    } catch (err) {
      alert("서버와 통신 중 오류가 발생했습니다.");
      console.error(err);
    }
  };

  // 예산 삭제 처리 함수
  const handleBudgetDelete = async () => {
    try {
      // 예산 상태 즉시 초기화 (UI 업데이트)
      setBudget(null);
      setBudgetInitialized(false);

      // 다이얼로그 닫기
      setDialogType(null);

      // 새로운 예산 정보 불러오기 (404가 예상됨)
      await fetchBudget();
    } catch (err) {
      console.error("예산 삭제 후 상태 업데이트 실패:", err);
    }
  };

  const fetchBudget = async () => {
    try {
      setLoading(true);

      const res = await axios.get(`/api/teams/${teamId}/budget`, {
        headers: {
          "Authorization": `Bearer ${token}`,
        }
      });

      if (res.status === 200) {
        setBudget(res.data);
        setBudgetInitialized(true);
      }
    } catch (err) {
      console.error("예산 정보를 불러오는 데 실패했습니다:", err);

      // 404 에러인 경우 예산이 없는 것으로 처리
      if (err.response && err.response.status === 404) {
        setBudget(null);
        setBudgetInitialized(false);
      }
    } finally {
      setLoading(false);
    }
  };

  // 예산 업데이트 처리 함수
  const handleBudgetUpdate = (updatedBudget) => {
    setBudget(updatedBudget);
    setBudgetInitialized(true);
    setDialogType(null);
  };

  useEffect(() => {
    fetchBudget();
  }, [teamId, token]);

  if (loading) return <p className="text-center mt-10 text-gray-600">불러오는 중...</p>;

  // 페이지 헤더 데이터 설정
  const pageHeaderData = {
    teamName: budget?.team?.name || `팀 ${teamId}`,
    teamId,
    openDialog: setDialogType
  };

  return (
    <div className="max-w-2xl mx-auto mt-10 p-6 bg-white shadow-md rounded-lg space-y-6">
      <PageHeaderControls
        pageHeaderData={pageHeaderData}
        onBudgetDelete={handleBudgetDelete}
      />

      <h1 className="text-2xl font-bold text-gray-800">
        [{budget?.team?.name || `팀 ${teamId}`}] 예산
      </h1>

      {budget ? (
        <div className="space-y-2 text-gray-700">
          <p>총 예산: <span className="font-medium">{SafeFormatterUtil.formatCurrency(budget?.totalAmount)} KRW</span></p>
          <p>원화 잔고: <span className="font-medium">{SafeFormatterUtil.formatCurrency(budget?.balance)} KRW</span></p>
          <p>외화 잔고: <span className="font-medium">{SafeFormatterUtil.formatCurrency(budget?.foreignBalance)} {budget?.foreignCurrency || 'KRW'}</span></p>
          <p>평균 환율: <span className="font-medium">{SafeFormatterUtil.formatCurrency(budget?.avgExchangeRate)}</span></p>
        </div>
      ) : (
        <div className="text-center p-4 bg-gray-100 rounded-lg">
          <p className="text-gray-600">아직 설정된 예산이 없습니다. 예산을 설정해주세요.</p>
          <button
            className="mt-4 px-4 py-2 bg-blue-500 text-white rounded-md"
            onClick={() => setDialogType('set')}
          >
            예산 설정하기
          </button>
        </div>
      )}

      {/* Dialogs */}
      {dialogType === "set" && (
        <SetBudgetDialog
          teamId={teamId}
          closeDialog={handleClose}
          onBudgetUpdate={handleBudgetUpdate}
        />
      )}

      {dialogType === "edit" && (
        <EditBudgetDialog
          teamId={teamId}
          closeDialog={handleClose}
          onBudgetUpdate={handleBudgetUpdate}
        />
      )}

      {dialogType === "add" && (
        <AddBudgetDialog
          teamId={teamId}
          closeDialog={handleClose}
          onBudgetUpdate={handleBudgetUpdate}
        />
      )}
    </div>
  );
}