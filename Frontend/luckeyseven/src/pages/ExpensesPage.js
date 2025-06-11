import React from 'react';
import { useParams } from 'react-router-dom';
import Header from '../components/Header'; // Assuming Header is a common component
import styles from '../styles/App.module.css'; // Or a more specific style file

function ExpensesPage() {
  const { teamId } = useParams();

  return (
    <div className={styles.app}>
      <Header />
      <main className={styles.main}>
        <h1>Expenses Page</h1>
        <p>Content for team ID: {teamId} will be displayed here.</p>
        {/* Placeholder for expenses content */}
      </main>
    </div>
  );
}

export default ExpensesPage;
