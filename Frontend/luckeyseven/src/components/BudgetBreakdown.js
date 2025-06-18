import React from 'react';
import styles from '../styles/BudgetBreakdown.module.css';
import {
  Cell,
  Legend,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip
} from "recharts";

// 예시 색상
const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#AD8CFF'];

const CATEGORY_COLORS = {
  MEAL: '#4CAF50',
  SNACK: '#2196F3',
  TRANSPORT: '#9C27B0',
  MISCELLANEOUS: '#FFC107',
  ACCOMMODATION: '#E91E63',
};

const CATEGORY_LABELS = {
  MEAL: '식사',
  SNACK: '간식',
  TRANSPORT: '교통',
  ACCOMMODATION: '숙박',
  MISCELLANEOUS: '기타',
};

const BudgetBreakdown = ({
  categoryExpenseSumList = [],
  totalAmount,
  totalExpense,
  balance
}) => {

  const chartData = categoryExpenseSumList.map(item => ({
    name: CATEGORY_LABELS[item.category] || item.category,
    value: Number(item.totalAmount), // 혹시 BigDecimal로 오면 Number 변환
  }));

  console.log("chartData", chartData);

  return (
      <div className={styles.budgetBreakdown}>
        <h4 className={styles.sectionTitle}>Budget Breakdown</h4>
        <p className={styles.sectionSubtitle}>How your budget is being spent</p>
        <div style={{width: '100%', height: 250}}>
          <ResponsiveContainer>
            <PieChart>
              <Pie
                  data={chartData}
                  dataKey="value"
                  nameKey="name"
                  cx="50%"
                  cy="50%"
                  outerRadius={80}
                  innerRadius={45}
                  fill="#8884d8"
                  label={({percent}) =>
                      `${(percent * 100).toFixed(1)}%`
                  }
              >
                {chartData.map((entry, idx) => (
                    <Cell key={`cell-${idx}`}
                          fill={CATEGORY_COLORS[categoryExpenseSumList[idx]?.category]
                              || COLORS[idx % COLORS.length]}/>
                ))}
              </Pie>
              <Tooltip
                  formatter={(value, name) => {
                    // value = 카테고리별 금액
                    // totalExpense가 전체 합계
                    const percent = totalExpense > 0 ? ((value / totalExpense)
                        * 100).toFixed(1) : 0;
                    return [`₩ ${value.toLocaleString()} `, name];
                  }}
              />
              <Legend/>
            </PieChart>
          </ResponsiveContainer>
        </div>
        <div className={styles.budgetStats}>
          <div className={styles.budgetStatRow}>
            <span>Total Budget:</span>
            <span>₩{Number(totalAmount ?? 0).toLocaleString()}</span>
          </div>
          <div className={styles.budgetStatRow}>
            <span>Total Expenses:</span>
            <span>₩{Number(totalExpense ?? 0).toLocaleString()}</span>
          </div>
          <div className={styles.budgetStatRow}>
            <span>Remaining Budget:</span>
            <span>₩{Number(balance ?? 0).toLocaleString()}</span>
          </div>
          <div
              className={`${styles.budgetStatRow} ${styles.budgetStatRowBold}`}>
            <span>Budget Used:</span>
            {/*<span>{budgetData.budgetUsedPercentage}%</span>*/}
          </div>
        </div>
      </div>
  );
};

export default BudgetBreakdown;
