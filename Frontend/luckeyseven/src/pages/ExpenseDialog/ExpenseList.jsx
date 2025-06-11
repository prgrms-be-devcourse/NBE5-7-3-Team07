import React, {useCallback, useEffect, useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {useRecoilValue} from 'recoil';
import AddExpenseDialog from './AddExpenseDialog';
import ExpenseDetailDialog from './ExpenseDetailDialog';
import Header from '../../components/Header';
import {getListExpense} from '../../service/ExpenseService';
import {
  currentTeamIdState,
  teamForeignCurrencyState
} from '../../recoil/atoms/teamAtoms';
import {FaMoneyBillWave} from 'react-icons/fa';
import {FiPlus} from 'react-icons/fi';
import '../../components/styles/expenseList.css';

const CATEGORY_LABELS = {
  MEAL: '식사',
  SNACK: '간식',
  TRANSPORT: '교통',
  ACCOMMODATION: '숙박',
  MISCELLANEOUS: '기타',
};

// 결제 수단에 따른 통화 단위 매핑
const PAYMENT_METHOD_TO_CURRENCY = {
  CARD: 'KRW',
  CASH: '', // 실제 외화 통화 단위는 동적으로 결정됨
  OTHER: '',
};

export default function ExpenseList() {
  const teamId = useRecoilValue(currentTeamIdState);
  const foreignCurrency = useRecoilValue(teamForeignCurrencyState) || 'USD'; // 외화 통화 단위 가져오기
  const navigate = useNavigate();
  const [expenses, setExpenses] = useState([]);
  const [page, setPage] = useState(0);
  const [size] = useState(10);
  const [totalPages, setTotalPages] = useState(1);
  const [sortDirection, setSortDirection] = useState('DESC');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [balances, setBalances] = useState(null);
  const [notification, setNotification] = useState({message: '', type: ''});
  const [showAddDialog, setShowAddDialog] = useState(false);
  const [selectedExpenseId, setSelectedExpenseId] = useState(null);

  const fetchExpenses = useCallback(async () => {
    setLoading(true);
    try {
      const data = await getListExpense(teamId, page, size,
          `createdAt,${sortDirection}`);
      setExpenses(data.content);
      setTotalPages(data.totalPages);
      setError(null);
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
    }
  }, [teamId, page, size, sortDirection]);

  useEffect(() => {
    fetchExpenses();
  }, [fetchExpenses]);

  useEffect(() => {
    if (!balances && !notification.message) {
      return;
    }
    const timer = setTimeout(() => {
      setBalances(null);
      setNotification({message: '', type: ''});
    }, 10000);
    return () => clearTimeout(timer);
  }, [balances, notification]);

  const fmt = v => (v != null ? v.toLocaleString() : '-');

  const formatDate = d =>
      new Date(d).toLocaleDateString('ko-KR', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        weekday: 'short',
      });

  // 결제 수단에 따라 통화 단위 반환
  const getCurrencyUnit = (paymentMethod) => {
    if (paymentMethod === 'CARD') {
      return 'KRW';
    }
    if (paymentMethod === 'CASH') {
      return foreignCurrency;
    }
    return '';
  };

  const openDetail = id => setSelectedExpenseId(id);
  const closeDetail = () => setSelectedExpenseId(null);
  const goToPage = n => setPage(n - 1);

  const handleAddSuccess = async (_, bal) => {
    setBalances(bal);
    setNotification({message: '지출이 성공적으로 등록되었습니다.', type: 'register'});
    setShowAddDialog(false);
    await fetchExpenses();
  };

  const handleUpdateSuccess = (updatedExpense, bal) => {
    setExpenses(prev => prev.map(
        e => (e.id === updatedExpense.id ? updatedExpense : e)));
    setBalances(bal);
    setNotification({message: '지출이 성공적으로 수정되었습니다.', type: 'update'});
    closeDetail();
  };

  const handleDeleteSuccess = (deletedId, bal) => {
    setExpenses(prev => prev.filter(e => e.id !== deletedId));
    setBalances(bal);
    setNotification({message: '지출이 성공적으로 삭제되었습니다.', type: 'delete'});
    closeDetail();
  };

  if (loading) {
    return (
        <div className="expense-tracker">
          <Header/>
          <div className="content">
            <div className="loading">데이터를 불러오고 있습니다...</div>
          </div>
        </div>
    );
  }

  if (error) {
    return (
        <div className="expense-tracker">
          <div className="content">
            <div className="error">
              <p>데이터를 불러오는 중 오류가 발생했습니다</p>
              <p>{error.message}</p>
            </div>
          </div>
        </div>
    );
  }

  return (
      <div className="expense-tracker">
        <div className="content">
          <h2 className="section-title">
            <FaMoneyBillWave className="section-icon"/> 지출 내역
          </h2>
          {/* 잔고/알림 배너 */}
          {(balances || notification.message) && (
              <div className="balance-banner">
                {balances && (
                    <div>
                      <span className="label">원화 잔고:</span>
                      <strong>₩{fmt(balances.balance)}</strong>&nbsp;&nbsp;
                      <span className="label">외화 잔고:</span>
                      <strong>{fmt(
                          balances.foreignBalance)} {foreignCurrency}</strong>
                    </div>
                )}
                {notification.message && (
                    <div className={`notification ${notification.type}`}>
                      {notification.message}
                    </div>
                )}
              </div>
          )}
          {/* 액션 바 */}
          <div className="actions">
            <div className="header-actions">
              <button className="btn btn-filled"
                      onClick={() => setShowAddDialog(true)}>
                <FiPlus/> 지출 추가
              </button>
            </div>
            <div className="sort-control">
              <button
                  className="sort-btn"
                  onClick={() => setSortDirection(
                      prev => (prev === 'DESC' ? 'ASC' : 'DESC'))}
              >
                날짜순 <span className="icon">{sortDirection === 'DESC' ? '↓'
                  : '↑'}</span>
              </button>
            </div>
          </div>
          {/* 테이블 / 빈 상태 */}
          {expenses.length === 0 ? (
              <div className="empty-state">
                <h3>지출 내역이 없습니다</h3>
                <p>'지출 추가' 버튼을 클릭하여 첫 지출을 등록해보세요.</p>
              </div>
          ) : (
              <div className="expense-table">
                <table>
                  <thead>
                  <tr>
                    <th>제목</th>
                    <th>가격</th>
                    <th>카테고리</th>
                    <th>날짜</th>
                    <th>결제자</th>
                  </tr>
                  </thead>
                  <tbody>
                  {expenses.map(exp => {
                    const currencyUnit = getCurrencyUnit(exp.paymentMethod);
                    return (
                        <tr key={exp.id} onClick={() => openDetail(exp.id)}
                            style={{cursor: 'pointer'}}>
                          <td>{exp.description}</td>
                          <td className="amount">
                            {exp.amount.toLocaleString()} {currencyUnit
                              && `${currencyUnit}`}
                          </td>
                          <td>
                          <span className="category"
                                data-category={exp.category}>
                            {CATEGORY_LABELS[exp.category] || exp.category}
                          </span>
                          </td>
                          <td>{formatDate(exp.createdAt)}</td>
                          <td>{exp.payerNickname}</td>
                        </tr>
                    );
                  })}
                  </tbody>
                </table>
              </div>
          )}
          {/* 페이지네이션 */}
          {totalPages > 1 && (
              <div className="pagination">
                <button onClick={() => goToPage(page)} disabled={page === 0}>
                  ←
                </button>
                {Array.from({length: totalPages}, (_, i) => {
                  const p = i + 1,
                      cur = page + 1;
                  if (p === 1 || p === totalPages || (p >= cur - 1 && p <= cur
                      + 1)) {
                    return (
                        <button key={i} className={p === cur ? 'active' : ''}
                                onClick={() => goToPage(p)}>
                          {p}
                        </button>
                    );
                  }
                  if (p === cur - 2 && cur > 3) {
                    return <span key="e1"
                                 className="pagination-ellipsis">...</span>;
                  }
                  if (p === cur + 2 && cur < totalPages - 2) {
                    return <span
                        key="e2" className="pagination-ellipsis">...</span>;
                  }
                  return null;
                })}
                <button onClick={() => goToPage(page + 2)}
                        disabled={page + 1 === totalPages}>
                  →
                </button>
              </div>
          )}
          {/* 다이얼로그 */}
          {showAddDialog && <AddExpenseDialog
              onClose={() => setShowAddDialog(false)}
              onSuccess={handleAddSuccess}/>}
          {selectedExpenseId && (
              <ExpenseDetailDialog
                  expenseId={selectedExpenseId}
                  onClose={closeDetail}
                  onUpdate={handleUpdateSuccess}
                  onDelete={handleDeleteSuccess}
              />
          )}
        </div>
      </div>
  );
}