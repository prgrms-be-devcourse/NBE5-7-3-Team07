import React, { useState, useEffect } from "react";
import {  Link, useNavigate } from "react-router-dom";
import "../styles/auth.css";
import { logout, getCurrentUser, refreshAccessToken } from "../service/AuthService";

export default function Home() {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [refreshStatus, setRefreshStatus] = useState("");

  useEffect(() => {
    // 로그인 상태 확인
    const checkLoginStatus = () => {
      try {
        const currentUser = getCurrentUser();
        if (currentUser) {
          setUser(currentUser);
        } else {
          setError("로그인이 필요합니다.");
          // navigate("/TeamDashBoard");
          navigate("/login")
        }
      } catch (err) {
        setError("로그인이 필요합니다.");
        navigate("/login");
      } finally {
        setLoading(false);
      }
    };

    checkLoginStatus();
  }, [navigate]);

  const handleLogout = async () => {
    try {
      const result = await logout();
      if (result.success) {
        // 히스토리를 조작해서 뒤로가기 방지
        window.history.replaceState(null, '', '/login');
        navigate("/login", { replace: true });
        
        // 추가 보안: 뒤로가기 이벤트 리스너 추가
        window.addEventListener('popstate', () => {
          navigate("/login", { replace: true });
        });
      } else {
        console.warn("서버 로그아웃 실패, 클라이언트 측 로그아웃은 완료됨");
        window.history.replaceState(null, '', '/login');
        navigate("/login", { replace: true });
      }
    } catch (err) {
      console.error("로그아웃 처리 중 예외 발생:", err);
      setError("로그아웃 중 오류가 발생했지만, 로그인 페이지로 이동합니다.");
      window.history.replaceState(null, '', '/login');
      navigate("/login", { replace: true });
    }
  };

  // 토큰 갱신 테스트 함수
  const handleRefreshToken = async () => {
    setRefreshStatus("토큰 갱신 중...");
    try {
      const result = await refreshAccessToken();
      if (result.success) {
        setRefreshStatus("토큰 갱신 성공!");
        console.log("토큰 갱신 결과:", result.data);
      } else {
        setRefreshStatus("토큰 갱신 실패: " + result.error);
      }
    } catch (err) {
      console.error("토큰 갱신 처리 중 예외 발생:", err);
      setRefreshStatus("토큰 갱신 중 오류 발생: " + err.message);
    }
    
    // 3초 후 상태 메시지 제거
    setTimeout(() => {
      setRefreshStatus("");
    }, 3000);
  };

  if (loading) {
    return <div className="loading">로딩 중...</div>;
  }

  return (
    <div className="auth-container">
      <div className="auth-card">
        <div className="auth-header">
          <h1 className="auth-title">홈</h1>
        </div>
        <div className="user-info">
          {error && <div className="error-message">{error}</div>}
          {refreshStatus && <div className="refresh-status">{refreshStatus}</div>}
          {user && (
            <>
              <div className="info-item">
                <strong>이메일:</strong> {user.email}
              </div>
              <div className="info-item">
                <strong>닉네임:</strong> {user.nickname}
              </div>
            </>
          )}
        </div>
        <div className="buttons">
          <button onClick={handleLogout} className="btn btn-primary">
            로그아웃
          </button>
          <button onClick={handleRefreshToken} className="btn btn-secondary">
            토큰 갱신 테스트
          </button>
        </div>
      </div>
    </div>
  );
} 