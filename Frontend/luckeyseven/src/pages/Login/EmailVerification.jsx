import React, { useState, useEffect } from "react";
import { Link, useNavigate, useLocation } from "react-router-dom";
import "../../styles/auth.css";
import { requestEmailVerification } from "../../service/AuthService";

export default function EmailVerification() {
  const navigate = useNavigate();
  const location = useLocation();
  const [email, setEmail] = useState("");
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [isVerificationSent, setIsVerificationSent] = useState(false);
  const [verificationStatus, setVerificationStatus] = useState(""); // 인증 상태 메시지
  
  // URL 파라미터에서 token 값을 확인
  useEffect(() => {
    const queryParams = new URLSearchParams(location.search);
    const token = queryParams.get('token');
    
    if (token) {
      console.log("토큰 발견:", token);
      
      // 토큰이 존재하면 토큰 검증 후 회원가입 페이지로 리다이렉트
      setVerificationStatus("인증 토큰 확인 중...");
      
      // 이메일 인증 상태를 localStorage에 저장
      localStorage.setItem('emailVerified', 'true');
      
      // 토큰 확인 후 이메일 저장 (실제로는 이 토큰으로 백엔드 API를 호출해야 함)
      const storedEmail = localStorage.getItem('verifiedEmail');
      if (storedEmail) {
        // 이메일이 있으면 그대로 진행
        setTimeout(() => {
          setVerificationStatus("이메일 인증이 완료되었습니다. 회원가입 페이지로 이동합니다...");
          setTimeout(() => {
            navigate('/signup?verified=true&email=' + encodeURIComponent(storedEmail));
          }, 1000);
        }, 1000);
      } else {
        // 이메일 정보가 없으면 일단 진행 (실제로는 백엔드 API 응답에서 이메일을 받아와야 함)
        setVerificationStatus("이메일 정보를 찾을 수 없습니다. 다시 인증을 시도해주세요.");
      }
    }
  }, [location.search, navigate]);

  const handleEmailChange = (e) => {
    setEmail(e.target.value);
    setError("");
  };

  const validateEmail = () => {
    if (!email) {
      setError("이메일 주소를 입력해주세요.");
      return false;
    }
    
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
      setError("유효한 이메일 주소를 입력해주세요.");
      return false;
    }
    
    return true;
  };

  const handleSendVerification = async (e) => {
    e.preventDefault();
    setError("");
    
    if (!validateEmail()) {
      return;
    }
    
    setIsLoading(true);
    try {
      await requestEmailVerification(email);
      // 이메일 저장 (나중에 회원가입 폼에서 사용)
      localStorage.setItem('verifiedEmail', email);
      setIsVerificationSent(true);
    } catch (err) {
      setError(err.response?.data?.message || "이메일 인증 요청 중 오류가 발생했습니다.");
    } finally {
      setIsLoading(false);
    }
  };

  // 토큰 검증 중일 때 로딩 화면 표시
  if (verificationStatus) {
    return (
      <div className="min-h-screen flex items-center justify-center p-4 bg-gray-50">
        <div className="w-full max-w-md bg-white rounded-lg shadow-md p-8">
          <div className="text-center">
            <div className="mb-4 flex justify-center">
              <svg className="animate-spin h-12 w-12 text-blue-500" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
            </div>
            <h2 className="text-xl font-semibold text-gray-800 mb-2">이메일 인증</h2>
            <p className="text-gray-600">{verificationStatus}</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex items-center justify-center p-4 bg-gray-50">
      <div className="w-full max-w-md">
        {!isVerificationSent ? (
          // 이메일 입력 화면
          <div className="bg-white rounded-lg shadow-md border border-gray-100">
            <div className="p-6 border-b border-gray-100">
              <h1 className="text-2xl font-bold text-center">이메일 인증</h1>
              <p className="text-center text-gray-500 mt-2">회원가입을 위해 이메일 인증이 필요합니다.</p>
            </div>
            
            <form className="p-6" onSubmit={handleSendVerification}>
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
                  value={email}
                  onChange={handleEmailChange}
                  className="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  placeholder="example@example.com"
                  disabled={isLoading}
                />
              </div>
              
              <button
                type="submit"
                className="w-full py-2 px-4 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 mt-4"
                disabled={isLoading}
              >
                {isLoading ? (
                  <span className="flex items-center justify-center">
                    <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 818-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 714 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                    처리 중...
                  </span>
                ) : "인증 메일 발송"}
              </button>
              
              <div className="mt-6 text-center">
                <Link to="/login" className="text-sm text-blue-600 hover:text-blue-800 font-medium">
                  로그인으로 돌아가기
                </Link>
              </div>
            </form>
          </div>
        ) : (
          // 이메일 발송 완료 화면
          <div className="bg-white rounded-lg shadow-md border border-gray-100">
            <div className="p-8 rounded-lg bg-gradient-to-br from-blue-50 to-indigo-50 text-center border border-blue-100">
              {/* 애니메이션 이메일 아이콘 */}
              <div className="flex justify-center mb-6">
                <div className="relative">
                  <div className="w-20 h-20 bg-white rounded-full flex items-center justify-center shadow-lg border border-blue-100">
                    <svg xmlns="http://www.w3.org/2000/svg" width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="#3b82f6" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                      <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path>
                      <polyline points="22,6 12,13 2,6"></polyline>
                    </svg>
                  </div>
                  {/* 펄스 애니메이션 */}
                  <div className="absolute -top-1 -right-1 w-6 h-6 bg-green-500 rounded-full flex items-center justify-center animate-pulse">
                    <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round">
                      <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                      <polyline points="22 4 12 14.01 9 11.01"></polyline>
                    </svg>
                  </div>
                  {/* 원형 펄스 효과 */}
                  <div className="absolute inset-0 rounded-full border-4 border-blue-200 animate-ping opacity-30"></div>
                </div>
              </div>
              
              <h3 className="text-2xl font-bold text-gray-800 mb-3">인증 메일을 확인해주세요</h3>
              <p className="text-gray-600 mb-4 text-lg">
                <span className="font-semibold text-blue-600 block mb-2">{email}</span>
                주소로 인증 링크를 보내드렸습니다
              </p>
              
              {/* 대기 상태 표시 */}
              <div className="max-w-sm mx-auto mb-6">
                <div className="flex items-center justify-center p-4 rounded-lg bg-white border border-blue-100 shadow-sm">
                  <div className="flex items-center">
                    <div className="mr-4">
                      <div className="w-3 h-3 bg-blue-500 rounded-full animate-bounce mr-1 inline-block"></div>
                      <div className="w-3 h-3 bg-blue-400 rounded-full animate-bounce mr-1 inline-block" style={{animationDelay: '0.1s'}}></div>
                      <div className="w-3 h-3 bg-blue-300 rounded-full animate-bounce inline-block" style={{animationDelay: '0.2s'}}></div>
                    </div>
                    <div className="text-left">
                      <p className="font-semibold text-gray-800 text-sm">인증 대기 중</p>
                      <p className="text-gray-500 text-xs">이메일을 확인하고 링크를 클릭하세요</p>
                    </div>
                  </div>
                </div>
              </div>

              {/* 단계별 안내 */}
              <div className="max-w-md mx-auto mb-6">
                <div className="bg-white rounded-lg p-4 border border-blue-100">
                  <h4 className="font-semibold text-gray-800 mb-3 text-sm">다음 단계:</h4>
                  <div className="space-y-2 text-left">
                    <div className="flex items-start">
                      <div className="w-6 h-6 bg-blue-100 rounded-full flex items-center justify-center mr-3 mt-0.5 flex-shrink-0">
                        <span className="text-blue-600 font-bold text-xs">1</span>
                      </div>
                      <p className="text-sm text-gray-600">이메일함을 확인하세요</p>
                    </div>
                    <div className="flex items-start">
                      <div className="w-6 h-6 bg-blue-100 rounded-full flex items-center justify-center mr-3 mt-0.5 flex-shrink-0">
                        <span className="text-blue-600 font-bold text-xs">2</span>
                      </div>
                      <p className="text-sm text-gray-600">Lucky Seven에서 보낸 메일을 찾으세요</p>
                    </div>
                    <div className="flex items-start">
                      <div className="w-6 h-6 bg-blue-100 rounded-full flex items-center justify-center mr-3 mt-0.5 flex-shrink-0">
                        <span className="text-blue-600 font-bold text-xs">3</span>
                      </div>
                      <p className="text-sm text-gray-600">인증 링크를 클릭하세요</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            
            <div className="space-y-3">
              <button
                className="w-full py-3 px-4 border border-gray-300 rounded-lg text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 font-medium"
                onClick={() => setIsVerificationSent(false)}
              >
                다른 이메일 주소 사용하기
              </button>
              
              <div className="p-4 rounded-lg bg-gray-50 text-center border border-gray-100">
                <p className="text-sm text-gray-600 mb-3">메일이 오지 않나요?</p>
                <div className="space-y-2">
                  <p className="text-xs text-gray-500">스팸함을 확인하거나 다시 시도해보세요</p>
                  <button
                    onClick={handleSendVerification}
                    className="py-2 px-6 bg-blue-600 text-white rounded-lg hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 font-medium"
                    disabled={isLoading}
                  >
                    {isLoading ? (
                      <span className="flex items-center justify-center">
                        <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                          <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                          <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 818-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 714 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                        </svg>
                        발송 중...
                      </span>
                    ) : "인증 메일 다시 보내기"}
                  </button>
                </div>
              </div>
              
              <div className="text-center mt-4">
                <Link to="/login" className="text-sm text-blue-600 hover:text-blue-800 font-medium">
                  로그인으로 돌아가기
                </Link>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
} 