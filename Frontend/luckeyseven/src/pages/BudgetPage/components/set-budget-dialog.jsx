import React, { useState } from 'react';
import axios from 'axios';
import '../styles/BudgetDialog.css';

const SetBudgetDialog = ({ teamId, closeDialog, onBudgetUpdate }) => {
  const [totalAmount, setTotalAmount] = useState(0);
  const [isExchanged, setIsExchanged] = useState(false);
  const [foreignCurrency, setForeignCurrency] = useState('KRW');
  const [exchangeRate, setExchangeRate] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState('');

  const resetForm = () => {
    setTotalAmount(0);
    setIsExchanged(false);
    setForeignCurrency('KRW');
    setExchangeRate('');
    setError('');
  };

  const handleClose = () => {
    resetForm();
    closeDialog();
  };

  // 예산 설정 함수 - 수정된 버전
  const handleSubmit = async () => {
    if (isSubmitting) return;
    setIsSubmitting(true);
    setError('');

    // 입력값 유효성 검사
    if (totalAmount <= 0) {
      setError('예산 금액은 0보다 커야 합니다.');
      setIsSubmitting(false);
      return;
    }

    if (isExchanged && (!exchangeRate || exchangeRate <= 0)) {
      setError('유효한 환율을 입력해주세요.');
      setIsSubmitting(false);
      return;
    }

    try {
      // 기존 예산 확인을 위한 GET 요청
      try {
        const checkResponse = await axios.get(`/api/teams/${teamId}/budgets`);
        
        // 이미 예산이 있는 경우, 409 에러를 설정하고 종료
        if (checkResponse.status === 200) {
          setError('이미 설정된 예산이 있습니다. 예산 수정을 이용해 주세요.');
          setIsSubmitting(false);
          return;
        }
      } catch (checkError) {
        // 404는 예산이 없는 경우이므로 계속 진행
        if (checkError.response && checkError.response.status !== 404) {
          console.warn('예산 확인 중 오류:', checkError);
          setError('예산 확인 중 오류가 발생했습니다.');
          setIsSubmitting(false);
          return;
        }
      }

      // 새 예산 설정
      const response = await axios.post(`/api/teams/${teamId}/budgets`, {
        totalAmount: Number(totalAmount),
        isExchanged,
        foreignCurrency,
        exchangeRate: isExchanged ? Number(exchangeRate) : null,
      });
      
      console.log('Budget setup response:', response.data);
      
      if (onBudgetUpdate) {
        onBudgetUpdate(response.data);
      }
      
      resetForm();
      closeDialog(); // 다이얼로그 닫기
    } catch (error) {
      console.error('Error setting budget:', error);
      
      if (error.response) {
        // 409는 이미 예산이 있는 경우
        if (error.response.status === 409) {
          setError('이미 설정된 예산이 있습니다. 예산 삭제 후 다시 시도해주세요.');
        } else {
          setError('예산 설정 중 오류가 발생했습니다: ' + (error.response.data?.message || error.message));
        }
      } else {
        setError('서버와 통신 중 오류가 발생했습니다.');
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={handleClose}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <h2>예산 설정</h2>

        {error && <div className="error-message">{error}</div>}
                
        <div className="notice-box">
          <span>팀의 예산을 설정해 보세요! 나중에 환율을 업데이트하거나 예산을 추가할 수 있습니다.</span>
        </div>
        
        <label>예산 금액</label>
        <input
          type="number"
          value={totalAmount}
          onChange={(e) => setTotalAmount(e.target.value)}
          placeholder="예산 금액"
          min="0"
          step="100"
        />
        
        <label>통화 코드</label>
        <select value={foreignCurrency} onChange={(e) => setForeignCurrency(e.target.value)}>
          <option value="USD">USD - 미국 달러</option>
          <option value="EUR">EUR - 유로</option>
          <option value="KRW">KRW - 대한민국 원</option>
          <option value="JPY">JPY - 일본 엔화</option>
          <option value="CNY">CNY - 중국 위안</option>
          <option value="GBP">GBP - 영국 파운드</option>
          <option value="AUD">AUD - 호주 달러</option>
          <option value="CAD">CAD - 캐나다 달러</option>
          <option value="CHF">CHF - 스위스 프랑</option>
          <option value="INR">INR - 인도 루피</option>
          <option value="SGD">SGD - 싱가포르 달러</option>
          <option value="THB">THB - 태국 바트</option>
          <option value="HKD">HKD - 홍콩 달러</option>
          <option value="RUB">RUB - 러시아 루블</option>
          <option value="BRL">BRL - 브라질 헤알</option>
        </select>
        
        <div className="toggle-buttons">
          <label>환율 적용 여부</label>
          <button 
            className={isExchanged ? 'active' : ''} 
            onClick={() => setIsExchanged(true)}
          >
            예
          </button>
          <button 
            className={!isExchanged ? 'active' : ''} 
            onClick={() => setIsExchanged(false)}
          >
            아니오
          </button>
        </div>
        
        {isExchanged && (
          <>
            <label>환율</label>
            <input
              type="number"
              value={exchangeRate}
              onChange={(e) => setExchangeRate(e.target.value)}
              placeholder="환율"
              min="0"
            />
          </>
        )}
        
        <div className="modal-buttons">
          <button onClick={handleClose}>취소</button>
          <button 
            className="primary" 
            onClick={handleSubmit}
            disabled={isSubmitting}
          >
            {isSubmitting ? '처리 중...' : '예산 설정'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default SetBudgetDialog;