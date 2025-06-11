import React, {useRef} from 'react';
import styles from '../styles/App.module.css';
import SummaryCard from './SummaryCard';
import BudgetBreakdown from './BudgetBreakdown';
import RecentExpensesTable from './RecentExpenseTable';

const OverviewTabContent = ({dashboardData}) => {
  // 이제 매번 balance로부터 계산할 것이므로 필요 없음
  const initializedRef = useRef(false);

  // 조기 반환은 모든 Hooks가 호출된 후에 수행
  if (!dashboardData) {
    return <div>Loading overview...</div>;
  }

  const {
    // team_id,
    // teamName,
    totalAmount = 0, // 기본값 설정
    balance = 0, // 기본값 설정, 이제 이 값을 그대로 사용
    foreignBalance = 0, // 기본값 설정
    foreignCurrency = 'KRW', // 기본값 설정
    expenseList = [], // 기본값 설정
    categoryExpenseSumList = [],
    // avgExchangeRate = 0, // 기본값 설정
  } = dashboardData;

  // 총 지출을 항상 totalAmount - balance로 계산
  const totalExpense = totalAmount - balance;

  // 지출 퍼센티지 계산
  const totalExpensePercentage = totalAmount > 0 ? (totalExpense / totalAmount)
      * 100 : 0;

  // 남은 예산 퍼센티지 계산
  const remainingBudgetPercentage = totalAmount > 0 ? (balance / totalAmount)
      * 100 : 0;

  // 지출 목록이 없는 경우 빈 배열로 처리
  const transformedExpenses = Array.isArray(expenseList) ? expenseList.map(
      expense => ({
        id: expense.id,
        title: `Expense by ${expense.payerNickname}`,
        amount: parseFloat(expense.amount),
        category: expense.category,
        description: expense.description,
        date: expense.date,
        paidBy: expense.payerNickname,
        paymentMethod: expense.paymentMethod,
        currency: expense.paymentMethod === "CASH" ? foreignCurrency : '₩',
      })) : [];

  return (
      <div>
        <div className={styles.summaryCardContainer}>
          <SummaryCard title="총 예산" amount={totalAmount} currency="₩"/>
          <SummaryCard title="총 지출" amount={totalExpense} currency="₩"
                       percentage={totalExpensePercentage.toFixed(1)}
                       of="of budget"/>
          <SummaryCard title="남은 예산" amount={balance} currency="₩"
                       percentage={remainingBudgetPercentage.toFixed(1)}
                       of="of budget"/>
          {foreignCurrency && foreignBalance !== undefined && foreignCurrency
              !== 'KRW' && (
                  <SummaryCard
                      title={`남은 외화 (${foreignCurrency})`}
                      amount={foreignBalance}
                      currency={foreignCurrency}
                  />
              )}
        </div>
        <div className={styles.detailsContainer}>
          <BudgetBreakdown
              categoryExpenseSumList={categoryExpenseSumList}
              totalAmount={totalAmount}
              totalExpense={totalExpense}
              balance={balance}
          />
          <RecentExpensesTable expenses={transformedExpenses}
                               foreginCurrency={foreignCurrency}/>
        </div>
      </div>
  );
};

export default OverviewTabContent;