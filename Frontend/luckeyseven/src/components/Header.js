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
            // 히스토리를 조작해서 뒤로가기 방지
            window.history.replaceState(null, '', '/login');
            navigate("/login", { replace: true });
            
            // 추가 보안: 뒤로가기 이벤트 리스너 추가
            window.addEventListener('popstate', () => {
                navigate("/login", { replace: true });
            });
        } catch (err) {
            setError("로그아웃 중 오류가 발생했습니다.");
            window.history.replaceState(null, '', '/login');
            navigate("/login", { replace: true });
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
            {/* {!isTeamSetup && <Link to="/TeamDashboard" className={styles.navLink}>Dashboard</Link>} */}
            <button onClick={handleLogout} className={styles.navLink} style={{background:'none', border:'none', cursor:'pointer'}}>
                Logout
            </button>
        </nav>
      </header>
  );
};

export default Header;
