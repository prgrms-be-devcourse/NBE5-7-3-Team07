import React, {useState} from 'react';
import {Link, useLocation, useNavigate} from 'react-router-dom';
import styles from '../styles/Header.module.css';
import {logout} from "../service/AuthService";

const Header = () => {
    const navigate = useNavigate();
    const location = useLocation(); // 현재 경로 확인
    const [error, setError] = useState("");
    const handleLogout = async () => {
        try {
            await logout();
            navigate("/login");
        } catch (err) {
            setError("로그아웃 중 오류가 발생했습니다.");
        }
    };
    const isTeamSetup = location.pathname === "/team-setup";
  return (
      <header className={styles.header}>
        <div className={styles.headerLogo}>
          <span className={styles.headerIcon}>🌍</span>
          <h1 className={styles.headerTitle}>Travel Expense Manager</h1>
        </div>
        <nav className={styles.headerNav}>
          <Link to="/team-setup" className={styles.navLink}>Team Setup</Link>
            {!isTeamSetup && <Link to="/TeamDashboard" className={styles.navLink}>Dashboard</Link>}
            <button onClick={handleLogout} className={styles.navLink} style={{background:'none', border:'none', cursor:'pointer'}}>
                Logout
            </button>
        </nav>
      </header>
  );
};

export default Header;
