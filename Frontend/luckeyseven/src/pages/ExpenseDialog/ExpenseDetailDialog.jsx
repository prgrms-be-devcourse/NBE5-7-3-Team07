import React, {useEffect, useState} from 'react';
import {
  deleteExpense,
  getExpense,
  updateExpense
} from '../../service/ExpenseService';
import '../../components/styles/expenseDetailDialog.css';
import {useRecoilValue} from "recoil";
import {teamForeignCurrencyState} from "../../recoil/atoms/teamAtoms";

const CATEGORY_LABELS = {
  MEAL: '식사',
  SNACK: '간식',
  TRANSPORT: '교통',
  ACCOMMODATION: '숙박',
  MISCELLANEOUS: '기타'
};

const PAYMENT_LABELS = {
  CARD: '카드',
  CASH: '현금',
  OTHER: '기타'
};

export default function ExpenseDetailDialog({
  expenseId,
  onClose,
  onUpdate,
  onDelete
}) {
  const foreignCurrency = useRecoilValue(teamForeignCurrencyState) || 'USD'; // 외화 통화 단위 가져오기
  const [detail, setDetail] = useState(null);
  const [isEditing, setIsEditing] = useState(false);
  const [formData, setFormData] = useState({
    description: '',
    amount: 0,
    category: ''
  });

  // 결제 수단에 따른 통화 단위
  const CURRENCY_LABELS = {
    CARD: 'KRW',
    CASH: foreignCurrency,
    OTHER: '',
  };

  // ESC 키로 닫기
  useEffect(() => {
    const handleKeyDown = (e) => {
      if (e.key === 'Escape') {
        onClose();
      }
    };
    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, [onClose]);

  // 1) 상세 데이터 로드
  useEffect(() => {
    if (!expenseId) {
      return;
    }
    (async () => {
      try {
        const data = await getExpense(expenseId);
        setDetail(data);
        setFormData({
          description: data.description,
          amount: data.amount,
          category: data.category
        });
      } catch (err) {
        alert('상세 조회에 실패했습니다.');
        onClose();
      }
    })();
  }, [expenseId, onClose]);

  if (!detail) {
    return null;
  }

  const fmtDate = dt => new Date(dt).toLocaleString();

  const handleChange = (e) => {
    const {name, value} = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: name === 'amount' ? Number(value) : value
    }));
  };

  // 저장 (수정) 처리
  const handleSave = async () => {
    try {
      const req = {
        description: formData.description,
        amount: formData.amount,
        category: formData.category
      };
      const updated = await updateExpense(detail.id, req);
      onUpdate(
          {
            ...detail,
            ...req,
            amount: updated.amount,
            createdAt: updated.createdAt,
            updatedAt: updated.updatedAt
          },
          {balance: updated.balance, foreignBalance: updated.foreignBalance}
      );
      setDetail(prev => ({...prev, ...req}));
      setIsEditing(false);
      onClose();
    } catch (err) {
      const msg = err.response?.data?.message || '수정에 실패했습니다.';
      alert(msg);
    }
  };

  // 삭제 처리
  const handleDelete = async () => {
    if (!window.confirm('정말 이 지출을 삭제하시겠어요?')) {
      return;
    }
    try {
      const resp = await deleteExpense(detail.id);
      onDelete(detail.id,
          {balance: resp.balance, foreignBalance: resp.foreignBalance});
      onClose();
    } catch (err) {
      const msg = err.response?.data?.message || '삭제에 실패했습니다.';
      alert(msg);
    }
  };

  const handleCancel = () => {
    setFormData({
      description: detail.description,
      amount: detail.amount,
      category: detail.category
    });
    setIsEditing(false);
  };

  // 현재 선택된 결제 수단에 따른 통화 단위
  const currencyUnit = CURRENCY_LABELS[detail.paymentMethod] || '';

  return (
      <div className="modal-overlay">
        <div className="modal">
          <header>
            <h3>{isEditing ? '지출 수정' : '지출 상세'}</h3>
            <button className="close-btn" onClick={onClose}>×</button>
          </header>

          <div className="detail-content">
            {isEditing ? (
                <>
                  <div className="field">
                    <label>설명</label>
                    <input
                        type="text"
                        name="description"
                        value={formData.description}
                        onChange={handleChange}
                    />
                  </div>
                  <div className="field">
                    <label>금액</label>
                    <div style={{
                      position: 'relative',
                      display: 'flex',
                      alignItems: 'center'
                    }}>
                      <input
                          type="number"
                          name="amount"
                          step="100"
                          min="0"
                          value={formData.amount}
                          onChange={handleChange}
                          style={{
                            width: '100%',
                            paddingRight: currencyUnit ? '45px' : '10px'
                          }}
                      />
                      {currencyUnit && (
                          <span
                              style={{
                                position: 'absolute',
                                right: '10px',
                                pointerEvents: 'none',
                                color: '#666'
                              }}
                          >
                          {currencyUnit}
                        </span>
                      )}
                    </div>
                  </div>
                  <div className="field">
                    <label>카테고리</label>
                    <select
                        name="category"
                        value={formData.category}
                        onChange={handleChange}
                    >
                      {Object.entries(CATEGORY_LABELS).map(([k, l]) => (
                          <option key={k} value={k}>{l}</option>
                      ))}
                    </select>
                  </div>
                </>
            ) : (
                <>
                  <p><strong>설명</strong> {detail.description}</p>
                  <p>
                    <strong>지출 금액</strong>
                    <span className="amount">
                      {detail.amount.toLocaleString()}
                      {currencyUnit && ` ${currencyUnit}`}
                    </span>
                  </p>
                  <p><strong>카테고리</strong>{' '}<span className="category"
                                                     data-category={detail.category}>{CATEGORY_LABELS[detail.category]}</span>
                  </p>
                  <p><strong>결제 수단</strong>{' '}<span className="payment"
                                                      data-payment={detail.paymentMethod}>{PAYMENT_LABELS[detail.paymentMethod]} {currencyUnit
                      && `(${currencyUnit})`}</span>
                  </p>
                  <p><strong>결제자</strong> {detail.payerNickname}</p>
                  <p><strong>결제일</strong> {fmtDate(detail.createdAt)}</p>
                  <p><strong>수정일</strong> {fmtDate(detail.updatedAt)}</p>
                </>
            )}
          </div>

          <div className="modal-actions">
            {isEditing ? (
                <>
                  <button onClick={handleCancel}>취소</button>
                  <button className="save-btn" onClick={handleSave}>수정</button>
                </>
            ) : (
                <>
                  <button onClick={() => setIsEditing(true)}>수정</button>
                  <button className="delete-btn" onClick={handleDelete}>삭제
                  </button>
                </>
            )}
          </div>
        </div>
      </div>
  );
}