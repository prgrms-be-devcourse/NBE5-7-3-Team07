import React, { useState } from "react"
import { Link, useNavigate } from "react-router-dom"
import "../../styles/auth.css"
import { join } from "../../service/AuthService"


export default function Signup() {
  const navigate = useNavigate();
  const[error, setError] = useState("");
  const[email, setEmail] = useState("");
  const[password, setPassword] = useState("");
  const[checkPassword, setCheckPassword] = useState("");
  const[nickname, setNickname] = useState("");

  const changeNickname = (e) => {
    setNickname(e.target.value);
  };

  const changeEmail = (e) => {
    setEmail(e.target.value);
  };

  const changePassword = (e) => {
    setPassword(e.target.value);
  };

  const changeCheckPassword = (e) => {
    setCheckPassword(e.target.value);
  };

  const validateForm = () => {
    if (!email || !password || !checkPassword || !nickname) {
      setError("모든 필드를 입력해주세요.");
      return false;
    }
    
    if (password !== checkPassword) {
      setError("비밀번호가 일치하지 않습니다.");
      return false;
    }
    
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
      setError("유효한 이메일 주소를 입력해주세요.");
      return false;
    }
    
    if (password.length < 6) {
      setError("비밀번호는 최소 6자 이상이어야 합니다.");
      return false;
    }
    
    return true;
  };

  const handleJoin = async(e) => {
    e.preventDefault();
    setError("");
    
    if (!validateForm()) {
      return;
    }
    
    try {
      const req = {
        email: email,
        password: password,
        checkPassword: checkPassword,
        nickname: nickname
      };
      await join(req);
      alert("회원가입이 완료되었습니다.");
      navigate('/login');
    } catch (err) {
      setError(err.response?.data?.message || "회원가입 중 오류가 발생했습니다.");
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card">
        <div className="auth-header">
          <h1 className="auth-title">회원가입</h1>
        </div>
        <form className="auth-form" method="POST" onSubmit={handleJoin}>
          {error && <div className="error-message">{error}</div>}
          <div className="form-group">
            <label htmlFor="name" className="form-label">
              이름
            </label>
            <input
              id="name"
              name="name"
              type="text"
              autoComplete="name"
              required
              value={nickname}
              onChange={changeNickname}
              className="form-input"
              placeholder="홍길동"
            />
          </div>
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
              value={email}
              onChange={changeEmail}
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
              autoComplete="new-password"
              required
              value={password}
              onChange={changePassword}
              className="form-input"
            />
          </div>
          <div className="form-group">
            <label htmlFor="password-confirm" className="form-label">
              비밀번호 확인
            </label>
            <input
              id="password-confirm"
              name="password-confirm"
              type="password"
              autoComplete="new-password"
              required
              value={checkPassword}
              onChange={changeCheckPassword}
              className="form-input"
            />
          </div>

          <div className="remember-row">
            <Link to="/login" className="btn btn-outline">
              로그인으로 돌아가기
            </Link>
          </div>

          <button type="submit" className="btn btn-primary">
            회원가입
          </button>
        </form>
      </div>
    </div>
  )
} 