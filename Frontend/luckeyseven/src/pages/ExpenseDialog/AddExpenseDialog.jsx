import React, {useEffect, useState} from 'react';
import {useParams} from 'react-router-dom';
import {createExpense, getTeamMembers} from '../../service/ExpenseService';
import '../../components/styles/addExpenseDialog.css';
import {useRecoilValue} from "recoil";
import {
  currentTeamIdState,
  teamForeignCurrencyState
} from "../../recoil/atoms/teamAtoms";

const CATEGORY_LABELS = {
  MEAL: '식사',
  SNACK: '간식',
  TRANSPORT: '교통',
  ACCOMMODATION: '숙박',
  MISCELLANEOUS: '기타',
};
const PAYMENT_LABELS = {
  CARD: '카드',
  CASH: '현금',
  OTHER: '기타',
};
const categories = Object.keys(CATEGORY_LABELS);
const paymentMethods = Object.keys(PAYMENT_LABELS);

export default function AddExpenseDialog({onClose, onSuccess}) {
  const recoilTeamId = useRecoilValue(currentTeamIdState);
  const foreignCurrency = useRecoilValue(teamForeignCurrencyState) || 'USD'; // 외화 통화 단위 가져오기
  const paramTeamId = useParams().teamId;
  const teamId = recoilTeamId || paramTeamId;
  const [users, setUsers] = useState([]);
  const [form, setForm] = useState({
    description: '',
    amount: '',
    category: categories[0],
    payerId: '',
    paymentMethod: paymentMethods[0],
    settlerIds: []
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

  useEffect(() => {
    async function fetchMembers() {
      try {
        const members = await getTeamMembers(teamId);
        console.log('members:', members);
        setUsers(members);
        if (members.length > 0) {
          const firstId = String(members[0].id);
          setForm(f => ({
            ...f,
            payerId: firstId,
            settlerIds: [firstId]
          }));
        }
      } catch (err) {
        alert('팀 멤버 로딩에 실패했습니다. 다시 시도해주세요.');
      }
    }

    fetchMembers();
  }, [teamId]);

  const handleChange = (e) => {
    const {name, value, options} = e.target;

    if (name === 'settlerIds') {
      const selected = Array.from(options)
      .filter(o => o.selected)
      .map(o => o.value);
      setForm(f => ({...f, settlerIds: selected}));
    } else if (name === 'amount') {
      setForm(f => ({...f, amount: Number(value)}));
    } else if (name === 'payerId') {
      setForm(f => ({...f, payerId: value}));
    } else if (name === 'paymentMethod') {
      // 결제 수단이 변경되면 금액을 초기화
      setForm(f => ({...f, paymentMethod: value, amount: ''}));
    } else {
      setForm(f => ({...f, [name]: value}));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const payload = {
        description: form.description,
        amount: form.amount,
        category: form.category,
        payerId: Number(form.payerId),
        paymentMethod: form.paymentMethod,
        settlerId: form.settlerIds.map(id => Number(id))
      };
      const result = await createExpense(teamId, payload);
      if (onSuccess) {
        const newExpense = {
          id: result.id,
          description: form.description,
          amount: result.amount,
          category: form.category,
          payerId: Number(form.payerId),
          paymentMethod: form.paymentMethod,
          settlerIds: form.settlerIds.map(id => Number(id)),
          createdAt: result.createdAt
        };
        onSuccess(newExpense, {
          balance: result.balance,
          foreignBalance: result.foreignBalance
        });
      } else {
        alert(
            `등록 완료!\n잔고: ₩${result.balance}\n해외잔고: ${result.foreignBalance} ${foreignCurrency}`);
      }
      onClose();
    } catch (err) {
      const msg = err.response?.data?.message || '지출 등록에 실패했습니다.';
      alert(msg);
    }
  };

  // 현재 선택된 결제 수단에 따른 통화 단위
  const currencyUnit = CURRENCY_LABELS[form.paymentMethod] || '';

  return (
      <div className="modal-overlay">
        <div className="modal">
          <header>
            <h3>새 지출 추가</h3>
            <button className="close-btn" onClick={onClose}>×</button>
          </header>
          <form onSubmit={handleSubmit} className="modal-form">
            <label>
              설명
              <input
                  name="description"
                  value={form.description}
                  onChange={handleChange}
                  required
              />
            </label>
            <label>
              금액
              <div className="amount-input-container" style={{
                position: 'relative',
                display: 'flex',
                alignItems: 'center'
              }}>
                <input
                    name="amount"
                    type="number"
                    step="0.01"
                    value={form.amount}
                    onChange={handleChange}
                    min="0"
                    required
                    style={{
                      width: '100%',
                      paddingRight: currencyUnit ? '45px' : '10px'
                    }}
                    placeholder={`금액 입력 (${currencyUnit || '통화 단위'})`}
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
            </label>
            <label>
              카테고리
              <select name="category" value={form.category}
                      onChange={handleChange}>
                {categories.map(key => (
                    <option key={key} value={key}>
                      {CATEGORY_LABELS[key]}
                    </option>
                ))}
              </select>
            </label>
            <label>
              결제자
              <select name="payerId" value={form.payerId}
                      onChange={handleChange} required>
                {users.map(u => (
                    <option key={u.memberId} value={String(u.memberId)}>
                      {u.memberNickName}
                    </option>
                ))}
              </select>
            </label>
            <label>
              결제 수단
              <select name="paymentMethod" value={form.paymentMethod}
                      onChange={handleChange}>
                {paymentMethods.map(key => (
                    <option key={key} value={key}>
                      {PAYMENT_LABELS[key]} {CURRENCY_LABELS[key]
                        ? `(${CURRENCY_LABELS[key]})` : ''}
                    </option>
                ))}
              </select>
            </label>
            <label>
              정산 대상자
              <select
                  name="settlerIds"
                  multiple
                  value={form.settlerIds}
                  onChange={handleChange}
                  required
              >
                {users.map(u => (
                    <option key={u.memberId} value={String(u.memberId)}>
                      {u.memberNickName}
                    </option>
                ))}
              </select>
            </label>
            <div className="modal-actions">
              <button type="button" onClick={onClose}>취소</button>
              <button type="submit">저장</button>
            </div>
          </form>
        </div>
      </div>
  );
}