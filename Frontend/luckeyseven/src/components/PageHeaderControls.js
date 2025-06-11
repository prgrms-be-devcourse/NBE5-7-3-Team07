import React from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import styles from '../styles/PageHeaderControls.module.css';
import { useRecoilValue } from "recoil";
import { currentTeamIdState } from "../recoil/atoms/teamAtoms";

const PageHeaderControls = ({ pageHeaderData, onBudgetDelete }) => {
  const navigate = useNavigate();
  const teamId = useRecoilValue(currentTeamIdState);
  const { teamName, openDialog } = pageHeaderData || {};

  const handleSetBudget = () => {
    if (typeof openDialog === 'function') {
      openDialog('set');
    } else {
      console.error('openDialog is not a function');
    }
  };

  const handleEditBudget = () => {
    if (typeof openDialog === 'function') {
      openDialog('edit');
    } else {
      console.error('openDialog is not a function');
    }
  };

  const handleAddBudget = () => {
    if (typeof openDialog === 'function') {
      openDialog('add');
    } else {
      console.error('openDialog is not a function');
    }
  };

  const handleDeleteBudget = async () => {
    if (!teamId) {
      console.error('teamId is missing');
      return;
    }

    // 사용자에게 삭제 확인 요청
    if (!window.confirm('정말로 예산을 삭제하시겠습니까?')) {
      return;
    }

    try {
      const response = await axios.delete(`/api/teams/${teamId}/budget`);
      
      if (response.status === 204) {
        console.log('예산 삭제 완료');
        alert('예산이 성공적으로 삭제되었습니다.');
        
        // 예산 삭제 후 부모 컴포넌트에 알림
        if (typeof onBudgetDelete === 'function') {
          onBudgetDelete();
        }
      } else {
        alert('예산 삭제에 실패했습니다.');
      }
    } catch (error) {
      console.error('예산 삭제 실패:', error);
      
      // 404 에러는 이미 삭제된 경우이므로 성공으로 처리
      if (error.response && error.response.status === 404) {
        console.log('이미 삭제된 예산');
        if (typeof onBudgetDelete === 'function') {
          onBudgetDelete();
        }
      } else {
        // 다른 에러는 사용자에게 알림
        alert('예산 삭제 실패: ' + (error.response?.data?.message || error.message));
      }
    }
  };

  return (
    <div className={styles.pageHeaderControls}>
      <div>
        <h2 className={styles.pageTitle}>{teamName || 'Team Dashboard'}</h2>
        <p className={styles.pageSubtitle}>Manage your team's expenses and budget</p>
      </div>
      <div className={styles.pageActions}>
        <button className={styles.buttonPrimary} onClick={handleSetBudget}>예산 설정</button>
        <button className={styles.buttonSecondary} onClick={handleEditBudget}>예산 수정</button>
        <button className={styles.buttonSecondary} onClick={handleAddBudget}>예산 추가</button>
        <button className={styles.buttonDanger} onClick={handleDeleteBudget}>예산 삭제</button>
      </div>
    </div>
  );
};

export default PageHeaderControls;