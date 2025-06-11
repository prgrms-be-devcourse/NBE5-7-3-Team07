import React, { useState, useEffect } from "react"
import { Link, useNavigate } from "react-router-dom"
import "../../styles/auth.css"
import { login, getCurrentUser } from "../../service/AuthService"

export default function Login() {
  const navigate = useNavigate();
  const[loginEmail,setLoginEmail] = useState("");
  const[loginPassword,setLoginPassword] = useState("");
  const[error, setError] = useState("");
  
  // 이미 로그인된 사용자는 홈 페이지로 리다이렉트
  useEffect(() => {
    const checkLoggedIn = () => {
      const user = getCurrentUser();
      if (user) {
        console.log("이미 로그인됨, 홈으로 이동", user);
        navigate('/team-setup', { replace: true });
      }
    };
    
    checkLoggedIn();
  }, [navigate]);

  const changeLoginEmail = (e) => {
    setLoginEmail(e.target.value);
  };

  const changeLoginPassword = (e) => {
    setLoginPassword(e.target.value)
  };

  const handleLogin = async(e) => {
    e.preventDefault();
    setError("");
    
    try {
      const req = {
        email: loginEmail,
        password: loginPassword
      };
      console.log("로그인 요청:", req);
      
      const response = await login(req);
      console.log("로그인 응답:", response);
      
      // 로그인 성공 처리
      alert("로그인 성공!");
      
      // 로그인 후 홈으로 이동 (200ms 지연)
      setTimeout(() => {
        navigate('/team-setup', { replace: true });
      }, 200);
    } catch(err) {
      console.error("로그인 오류:", err);
      setError(err.response?.data?.message || '로그인에 실패했습니다.');
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card">
        <div className="auth-header">
          <h1 className="auth-title">로그인</h1>
        </div>
        <form className="auth-form" method="POST" onSubmit={handleLogin}>
          {error && <div className="error-message">{error}</div>}
          <div className="form-group">
            <label htmlFor="email-address" className="form-label">
              이메일 주소
            </label>
            <input
              id="email-address"
              name="email"
              type="email"
              autoComplete="email"
              required
              value={loginEmail}
              onChange={changeLoginEmail}
              className="form-input"
              placeholder="example@example.com"
            />
          </div>
          <div className="form-group">
            <label htmlFor="password" className="form-label">
              비밀번호
            </label>
            <input
              id="password"
              name="password"
              type="password"
              autoComplete="current-password"
              required
              value={loginPassword}
              onChange={changeLoginPassword}
              className="form-input"
            />
          </div>

          <div className="remember-row">
            <Link to="/signup" className="btn btn-outline">
              회원가입
            </Link>
          </div>

          <button type="submit" className="btn btn-primary">
            로그인
          </button>
        </form>
      </div>
    </div>
  )
} 