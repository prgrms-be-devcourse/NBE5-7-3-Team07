import React, { useState, useEffect } from "react"
import { Link, useNavigate } from "react-router-dom"
import "../../styles/auth.css"
import { login, getCurrentUser } from "../../service/AuthService"

export default function Login() {
  const navigate = useNavigate();
  const[loginEmail,setLoginEmail] = useState("");
  const[loginPassword,setLoginPassword] = useState("");
  const[error, setError] = useState("");
  const[isLoading, setIsLoading] = useState(false);
  
  // 비밀번호 표시/숨김 상태
  const[showPassword, setShowPassword] = useState(false);
  
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
    setIsLoading(true);
    
    try {
      const req = {
        email: loginEmail,
        password: loginPassword
      };
      console.log("로그인 요청:", req);
      
      const response = await login(req);
      console.log("로그인 응답:", response);
      
      // 로그인 후 홈으로 이동 (200ms 지연)
      setTimeout(() => {
        navigate('/team-setup', { replace: true });
      }, 200);
    } catch(err) {
      console.error("로그인 오류:", err);
      setError(err.response?.data?.message || '로그인에 실패했습니다.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center p-4 bg-gray-50">
      <div className="w-full max-w-md">
        <div className="bg-white rounded-lg shadow-md border border-gray-100">
          <div className="p-6 border-b border-gray-100">
            <h1 className="text-2xl font-bold text-center">로그인</h1>
            <p className="text-center text-gray-500 mt-2">Lucky Seven에 오신 것을 환영합니다</p>
          </div>
          
          <form className="p-6" method="POST" onSubmit={handleLogin}>
            {error && (
              <div className="mb-4 p-3 rounded-md bg-red-50 border-l-4 border-red-500 text-red-700">
                {error}
              </div>
            )}
            
            <div className="mb-4">
              <label htmlFor="email-address" className="block text-sm font-medium text-gray-700 mb-1">
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
                className="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                placeholder="example@example.com"
              />
            </div>
            
            <div className="mb-6">
              <label htmlFor="password" className="block text-sm font-medium text-gray-700 mb-1">
                비밀번호
              </label>
              <div className="relative">
                <input
                  id="password"
                  name="password"
                  type={showPassword ? "text" : "password"}
                  autoComplete="current-password"
                  required
                  value={loginPassword}
                  onChange={changeLoginPassword}
                  className="w-full px-4 py-2 pr-12 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  placeholder="••••••••"
                />
                <button
                  type="button"
                  className="absolute inset-y-0 right-0 flex items-center pr-3 text-gray-400 hover:text-gray-600 focus:outline-none"
                  onClick={() => setShowPassword(!showPassword)}
                >
                  {showPassword ? (
                    // 눈 감기 아이콘 (비밀번호 표시 중)
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                      <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
                      <line x1="1" y1="1" x2="23" y2="23"></line>
                    </svg>
                  ) : (
                    // 눈 뜨기 아이콘 (비밀번호 숨김 중)
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                      <circle cx="12" cy="12" r="3"></circle>
                    </svg>
                  )}
                </button>
              </div>
            </div>

            <div className="flex flex-col gap-4">
              <button 
                type="submit" 
                className="w-full py-2 px-4 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
                disabled={isLoading}
              >
                {isLoading ? (
                  <span className="flex items-center justify-center">
                    <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                    로그인 중...
                  </span>
                ) : "로그인"}
              </button>
              
              <div className="text-center">
                <span className="text-sm text-gray-600">계정이 없으신가요? </span>
                <Link to="/signup" className="text-sm text-blue-600 hover:text-blue-800 font-medium">
                  회원가입
                </Link>
              </div>
            </div>
          </form>
        </div>
      </div>
    </div>
  )
} 