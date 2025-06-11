import React from 'react';
import styles from '../styles/RecentExpensesTable.module.css';

const RecentExpensesTable = ({ expenses, foreignCurrency }) => {
  const CATEGORY_LABELS ={
    MEAL : '식사',
    SNACK: '간식',
    TRANSPORT: '교통',
    MISCELLANEOUS: '기타',
    ACCOMMODATION: '숙박',
  }
  const categoryColors = {
    MEAL : '#4CAF50',
    SNACK: '#2196F3',
    TRANSPORT: '#9C27B0',
    MISCELLANEOUS: '#FFC107',
    ACCOMMODATION: '#E91E63',
  };

  function getCategoryLabel(code) {
    return CATEGORY_LABELS[code] ?? code;
  }

  return (
      <div className={styles.recentExpenses}>
        <h4 className={styles.sectionTitle}>Recent Expenses</h4>
        <p className={styles.sectionSubtitle}>Latest team expenses</p>
        <table className={styles.expensesTable}>
          <thead>
          <tr>
            <th>Title</th>
            <th>Amount</th>
            <th>Category</th>
            <th>Date</th>
            <th>Paid By</th>
          </tr>
          </thead>
          <tbody>
          {expenses.map((exp, index) => (
              <tr key={index}>
                <td data-label="Title">{exp.description}</td>
                <td data-label="Amount">
                  <div className={styles.expenseAmount}>{exp.paymentMethod == "CASH" ? foreignCurrency : '₩'}{exp.amount.toLocaleString()}</div>
                  <div className={styles.expenseAmountSecondary}>{foreignCurrency}{exp.amount}</div>
                </td>
                <td data-label="Category">
                <span
                    className={styles.expenseCategory}
                    style={{ backgroundColor: categoryColors[exp.category] || '#ccc' }}
                >
                  {getCategoryLabel(exp.category)}
                </span>
                </td>
                <td data-label="Date">{exp.date}</td>
                <td data-label="Paid By">{exp.paidBy}</td>
              </tr>
          ))}
          </tbody>
        </table>
      </div>
  );
};

export default RecentExpensesTable;
