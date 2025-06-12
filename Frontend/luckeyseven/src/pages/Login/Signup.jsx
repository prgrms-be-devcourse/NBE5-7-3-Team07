import React, { useState, useEffect } from "react"
import { Link, useNavigate, useLocation } from "react-router-dom"
import "../../styles/auth.css"
import { join, requestEmailVerification } from "../../service/AuthService"


export default function Signup() {
  const navigate = useNavigate();
  const location = useLocation();
  const [error, setError] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [checkPassword, setCheckPassword] = useState("");
  const [nickname, setNickname] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [isEmailVerified, setIsEmailVerified] = useState(false);
  const [verificationSent, setVerificationSent] = useState(false);
  const [currentStep, setCurrentStep] = useState(1); // 1: 이메일 입력, 2: 인증 대기, 3: 인증 완료, 4: 회원정보 입력
  
  // 비밀번호 표시/숨김 상태
  const [showPassword, setShowPassword] = useState(false);
  const [showCheckPassword, setShowCheckPassword] = useState(false);

  // URL 파라미터에서 verified와 email 값을 확인
  useEffect(() => {
    // URL 파라미터 확인 (EmailVerificationHandler에서 오는 것은 무시)
    const queryParams = new URLSearchParams(location.search);
    const verified = queryParams.get('verified') === 'true';
    const emailFromParams = queryParams.get('email');
    const stepFromParams = parseInt(queryParams.get('step')) || 4;
    
    console.log("URL 파라미터 확인:", { verified, email: emailFromParams, step: stepFromParams });
    
    // EmailVerificationHandler에서 오는 URL 파라미터는 무시
    // (path가 /email-verify인 경우는 처리하지 않음)
    if (verified && emailFromParams && !window.location.pathname.includes('email-verify')) {
      setIsEmailVerified(true);
      setCurrentStep(stepFromParams); // URL에서 받은 step 사용 (3 또는 4)
      setEmail(emailFromParams);
      
      // step=3일 경우 잠시 보여준 후 step=4로 이동
      if (stepFromParams === 3) {
        setTimeout(() => {
          setCurrentStep(4);
          // URL에서 step 파라미터 제거하고 유지
          window.history.replaceState({}, document.title, 
            `${window.location.pathname}?verified=true&email=${encodeURIComponent(emailFromParams)}`);
        }, 2000);
      } else {
        // step=4일 경우 URL에서 파라미터 제거
        window.history.replaceState({}, document.title, window.location.pathname);
      }
      
      // localStorage에 인증 상태 저장
      localStorage.setItem('emailVerified', 'true');
      localStorage.setItem('verifiedEmail', emailFromParams);
      
      return;
    }
    
    // location.state에서 이메일 인증 상태 확인
    if (location.state?.emailVerified) {
      setIsEmailVerified(true);
      const stepFromState = location.state?.currentStep || 3; // 3단계에서 멈춤
      setCurrentStep(stepFromState);
      
      // 자동으로 4단계로 넘어가지 않음 (사용자가 버튼을 클릭해야 함)
    }
    
    // localStorage에서 이메일 인증 상태 확인
    const emailVerifiedFromStorage = localStorage.getItem('emailVerified') === 'true';
    if (emailVerifiedFromStorage && window.location.pathname === '/signup') {
      setIsEmailVerified(true);
      setCurrentStep(3); // 3단계에서 멈춤 (자동으로 4단계로 가지 않음)
      
      // 인증된 이메일 주소 저장을 위한 처리
      const storedEmail = localStorage.getItem('verifiedEmail');
      if (storedEmail) {
        setEmail(storedEmail);
      }
    }
  }, [location.search, location.state]);

  // localStorage 변화 감지 (실시간 인증 상태 체크)
  useEffect(() => {
    // /signup 페이지가 아닌 경우 리스너 등록하지 않음
    if (window.location.pathname !== '/signup') {
      return;
    }

    const handleStorageChange = (e) => {
      // emailVerificationCompleted가 새로 추가되면 인증 완료로 간주
      if (e.key === 'emailVerificationCompleted' && e.newValue === 'true') {
        const verifiedEmail = localStorage.getItem('verifiedEmail');
        if (verifiedEmail && currentStep === 2) {
          setEmail(verifiedEmail);
          setIsEmailVerified(true);
          setCurrentStep(3); // 3단계에서 멈춤 (자동으로 4단계로 가지 않음)
          
          // 완료 플래그 제거
          localStorage.removeItem('emailVerificationCompleted');
        }
      }
    };

    // localStorage 이벤트 리스너 등록
    window.addEventListener('storage', handleStorageChange);
    
    // 컴포넌트 언마운트 시 리스너 제거
    return () => {
      window.removeEventListener('storage', handleStorageChange);
    };
  }, [currentStep]);

  // 페이지가 활성화될 때 localStorage 상태를 다시 확인 (같은 탭에서의 변화 감지)
  useEffect(() => {
    const handleVisibilityChange = () => {
      if (!document.hidden && currentStep === 2) {
        const emailVerificationCompleted = localStorage.getItem('emailVerificationCompleted') === 'true';
        const verifiedEmail = localStorage.getItem('verifiedEmail');
        
        if (emailVerificationCompleted && verifiedEmail) {
          setEmail(verifiedEmail);
          setIsEmailVerified(true);
          setCurrentStep(3); // 3단계에서 멈춤 (자동으로 4단계로 가지 않음)
          
          localStorage.removeItem('emailVerificationCompleted');
        }
      }
    };

    document.addEventListener('visibilitychange', handleVisibilityChange);
    
    return () => {
      document.removeEventListener('visibilitychange', handleVisibilityChange);
    };
  }, [currentStep]);

  // 2단계 대기 중일 때 주기적으로 인증 상태 확인 (폴링)
  useEffect(() => {
    let intervalId;
    
    // /signup 페이지가 아닌 경우 폴링하지 않음
    if (window.location.pathname !== '/signup') {
      return;
    }
    
    if (currentStep === 2) {
      intervalId = setInterval(() => {
        const emailVerificationCompleted = localStorage.getItem('emailVerificationCompleted') === 'true';
        const verifiedEmail = localStorage.getItem('verifiedEmail');
        
        if (emailVerificationCompleted && verifiedEmail) {
          setEmail(verifiedEmail);
          setIsEmailVerified(true);
          setCurrentStep(3); // 3단계에서 멈춤 (자동으로 4단계로 가지 않음)
          
          localStorage.removeItem('emailVerificationCompleted');
          clearInterval(intervalId);
        }
      }, 1000); // 1초마다 확인
    }
    
    return () => {
      if (intervalId) {
        clearInterval(intervalId);
      }
    };
  }, [currentStep]);



  const changeEmail = (e) => {
    setEmail(e.target.value);
    // 이메일이 변경되면 인증 상태 초기화
    setIsEmailVerified(false);
    setCurrentStep(1);
  };

  const changeNickname = (e) => {
    setNickname(e.target.value);
  };

  const changePassword = (e) => {
    setPassword(e.target.value);
  };

  const changeCheckPassword = (e) => {
    setCheckPassword(e.target.value);
  };

  const validateEmail = () => {
    if (!email) {
      setError("이메일을 입력해주세요.");
      return false;
    }
    
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
      setError("올바른 이메일 형식을 입력해주세요.");
      return false;
    }
    
    return true;
  };

  const validateForm = () => {
    if (!password || !checkPassword || !nickname) {
      setError("모든 필드를 입력해주세요.");
      return false;
    }
    
    if (password !== checkPassword) {
      setError("비밀번호가 일치하지 않습니다.");
      return false;
    }
    
    if (password.length < 6) {
      setError("비밀번호는 최소 6자 이상이어야 합니다.");
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
      setVerificationSent(true);
      setCurrentStep(2); // 인증 대기 단계로 이동
      // 인증 이메일 주소를 로컬 스토리지에 저장
      localStorage.setItem('verifiedEmail', email);
    } catch (err) {
      setError(err.response?.data?.message || "인증 메일 발송 중 오류가 발생했습니다.");
    } finally {
      setIsLoading(false);
    }
  };

  const handleJoin = async(e) => {
    e.preventDefault();
    setError("");
    
    if (!validateForm()) {
      return;
    }
    
    setIsLoading(true);
    try {
      const req = {
        email: email,
        password: password,
        checkPassword: checkPassword,
        nickname: nickname
      };
      await join(req);
      alert("회원가입이 완료되었습니다.");
      // 인증 이메일 정보 삭제
      localStorage.removeItem('verifiedEmail');
      localStorage.removeItem('emailVerified');
      navigate('/login');
    } catch (err) {
      setError(err.response?.data?.message || "회원가입 중 오류가 발생했습니다.");
    } finally {
      setIsLoading(false);
    }
  };

  // 4단계 프로그레스 표시기 컴포넌트
  const ProgressIndicator = ({ currentStep }) => {
    const steps = [
      { id: 1, title: "이메일 입력" },
      { id: 2, title: "인증 대기" },
      { id: 3, title: "인증 완료" },
      { id: 4, title: "정보 입력" }
    ];

    return (
      <div className="mb-8">
        <div className="relative">
          {/* 연결선 배경 */}
          <div className="absolute top-5 left-0 right-0 h-0.5 bg-gray-200"></div>
          
          {/* 진행된 연결선 */}
          <div 
            className="absolute top-5 left-0 h-0.5 bg-green-600 transition-all duration-500"
            style={{ width: `${((currentStep - 1) / (steps.length - 1)) * 100}%` }}
          ></div>
          
          {/* 단계들 */}
          <div className="flex justify-between relative">
            {steps.map((step, index) => (
              <div key={step.id} className="flex flex-col items-center z-10">
                {/* 숫자/체크마크 원 */}
                <div className={`w-10 h-10 rounded-full flex items-center justify-center text-sm font-medium transition-all duration-300 ${
                  currentStep > step.id 
                    ? 'bg-green-600 text-white' 
                    : currentStep === step.id 
                      ? 'bg-blue-600 text-white' 
                      : 'bg-gray-200 text-gray-600'
                }`}>
                  {currentStep > step.id ? (
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                      <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                      <polyline points="22 4 12 14.01 9 11.01"></polyline>
                    </svg>
                  ) : step.id}
                </div>
                
                {/* 단계 제목 */}
                <span className={`mt-3 text-xs text-center transition-all duration-300 max-w-20 ${
                  currentStep === step.id ? 'text-blue-600 font-medium' : 'text-gray-600'
                }`}>
                  {step.title}
                </span>
              </div>
            ))}
          </div>
        </div>
      </div>
    );
  };

  // 이메일 인증 단계 렌더링
  const renderEmailVerificationStep = () => {
    return (
      <div className="min-h-screen flex items-center justify-center p-4">
        <div className="w-full max-w-md">
          {/* 5단계 프로그레스 표시기 */}
          <ProgressIndicator currentStep={currentStep} />

          {currentStep === 1 ? (
            // 1단계: 이메일 입력 화면
            <div className="auth-card rounded-lg shadow-md border border-gray-100">
              <div className="auth-header p-6 border-b border-gray-100">
                <h1 className="auth-title text-2xl font-bold text-center">회원가입</h1>
                <p className="auth-subtitle text-center text-gray-500 mt-2">이메일 주소를 입력하여 인증을 시작하세요</p>
              </div>
              <form className="auth-form p-6" onSubmit={handleSendVerification}>
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
                    onChange={changeEmail}
                    className="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                    placeholder="example@example.com"
                    disabled={isLoading}
                  />
                </div>
                
                <button
                  type="submit"
                  className={`w-full py-3 px-4 text-white rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 mt-4 font-medium transition-all duration-200 ${
                    isLoading 
                      ? 'bg-blue-400 cursor-not-allowed' 
                      : 'bg-blue-600 hover:bg-blue-700 hover:shadow-lg'
                  }`}
                  disabled={isLoading}
                >
                  {isLoading ? (
                    <div className="flex flex-col items-center justify-center relative overflow-hidden py-4">
                      <div className="relative w-full h-12 flex items-center justify-center">
                        <div className="flying-email-1">
                          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="2">
                            <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path>
                            <polyline points="22,6 12,13 2,6"></polyline>
                          </svg>
                        </div>
                        <div className="flying-email-2">
                          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="rgba(255,255,255,0.7)" strokeWidth="2">
                            <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path>
                            <polyline points="22,6 12,13 2,6"></polyline>
                          </svg>
                        </div>
                        <div className="flying-email-3">
                          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="rgba(255,255,255,0.5)" strokeWidth="2">
                            <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path>
                            <polyline points="22,6 12,13 2,6"></polyline>
                          </svg>
                        </div>
                        <div className="dotted-trail"></div>
                      </div>
                    </div>
                  ) : (
                    <span className="flex items-center justify-center">
                      <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="mr-2">
                        <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path>
                        <polyline points="22,6 12,13 2,6"></polyline>
                      </svg>
                      인증 메일 발송
                    </span>
                  )}
                </button>
                
                <div className="text-center mt-6">
                  <Link to="/login" className="text-sm text-blue-600 hover:text-blue-800 font-medium">
                    로그인으로 돌아가기
                  </Link>
                </div>
              </form>
            </div>
          ) : currentStep === 2 ? (
            // 2단계: 인증 대기 화면
            <div className="auth-card rounded-lg shadow-md border border-gray-100">
              <div className="auth-header p-6 border-b border-gray-100">
                <h1 className="auth-title text-2xl font-bold text-center text-blue-600">인증 메일을 확인해주세요</h1>
                <p className="auth-subtitle text-center text-gray-500 mt-2">이메일로 전송된 링크를 클릭하여 인증을 완료하세요</p>
              </div>
              <div className="p-6 text-center">
                <div className="mb-6">
                  <div className="w-20 h-20 mx-auto bg-blue-100 rounded-full flex items-center justify-center mb-4">
                    <svg className="w-10 h-10 text-blue-600 animate-pulse" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M3 8l7.89 4.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"/>
                    </svg>
                  </div>
                </div>
                
                <p className="text-gray-600 mb-6">
                  <span className="font-semibold text-blue-600">{email}</span><br/>
                  인증 메일이 전송되었습니다.
                </p>
                
                <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4 mb-6">
                  <p className="text-sm text-yellow-800">
                    💡 메일이 보이지 않나요?<br/>
                    스팸 폴더도 확인해보세요!
                  </p>
                </div>
                
                <button
                  onClick={() => setCurrentStep(1)}
                  className="w-full py-2 px-4 border border-gray-300 text-gray-700 rounded-md hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 mb-4"
                >
                  이메일 다시 입력하기
                </button>
                
                <div className="text-center">
                  <Link to="/login" className="text-sm text-blue-600 hover:text-blue-800 font-medium">
                    로그인으로 돌아가기
                  </Link>
                </div>
              </div>
            </div>
          ) : (
            // 3단계: 인증 완료 화면
            <div className="auth-card rounded-lg shadow-md border border-gray-100">
              <div className="auth-header p-6 border-b border-gray-100">
                <h1 className="auth-title text-2xl font-bold text-center text-green-600">이메일 인증 완료!</h1>
                <p className="auth-subtitle text-center text-gray-500 mt-2">이제 회원 정보를 입력해주세요</p>
              </div>
              <div className="p-6 text-center">
                <div className="mb-6">
                  <div className="w-20 h-20 mx-auto bg-green-100 rounded-full flex items-center justify-center mb-4">
                    <div className="checkmark-container">
                      <svg className="checkmark w-12 h-12 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path className="checkmark-path" strokeLinecap="round" strokeLinejoin="round" strokeWidth="3" d="M5 13l4 4L19 7"/>
                      </svg>
                    </div>
                  </div>
                </div>
                
                <p className="text-gray-600 mb-6">
                  <span className="font-semibold text-green-600">{email}</span><br/>
                  이메일 인증이 완료되었습니다!
                </p>
                
                <button
                  onClick={() => setCurrentStep(4)}
                  className="w-full py-3 px-4 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 font-medium"
                >
                  회원 정보 입력하기 →
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    );
  };

  // 회원정보 입력 단계 렌더링 (4단계)
  const renderUserInfoStep = () => {
    return (
      <div className="min-h-screen flex items-center justify-center p-4">
        <div className="w-full max-w-md">
          {/* 5단계 프로그레스 표시기 */}
          <ProgressIndicator currentStep={currentStep} />

          {currentStep === 4 ? (
            <div className="auth-card rounded-lg shadow-md border border-gray-100">
              <div className="auth-header p-6 border-b border-gray-100">
                <h1 className="auth-title text-2xl font-bold text-center">회원 정보 입력</h1>
                <p className="auth-subtitle text-center text-gray-500 mt-2">마지막 단계입니다. 회원 정보를 입력해주세요</p>
              </div>
              <form className="auth-form p-6" onSubmit={handleJoin}>
                {error && (
                  <div className="mb-4 p-3 rounded-md bg-red-50 border-l-4 border-red-500 text-red-700">
                    {error}
                  </div>
                )}
                
                <div className="mb-4">
                  <label htmlFor="verified-email" className="block text-sm font-medium text-gray-700 mb-1">
                    이메일 주소
                  </label>
                  <input
                    id="verified-email"
                    type="email"
                    value={email}
                    readOnly
                    className="w-full px-4 py-2 border border-gray-300 rounded-md bg-gray-50 text-gray-500"
                  />
                  <div className="flex items-center mt-1 text-green-600 text-xs">
                    <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="mr-1">
                      <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                      <polyline points="22 4 12 14.01 9 11.01"></polyline>
                    </svg>
                    인증된 이메일 주소입니다.
                  </div>
                </div>
                
                <div className="mb-4">
                  <label htmlFor="name" className="block text-sm font-medium text-gray-700 mb-1">
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
                    className="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                    placeholder="홍길동"
                  />
                </div>
                
                <div className="mb-4">
                  <label htmlFor="password" className="block text-sm font-medium text-gray-700 mb-1">
                    비밀번호
                  </label>
                  <div className="relative">
                    <input
                      id="password"
                      name="password"
                      type={showPassword ? "text" : "password"}
                      autoComplete="new-password"
                      required
                      value={password}
                      onChange={changePassword}
                      className="w-full px-4 py-2 pr-12 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                      placeholder="최소 6자 이상"
                    />
                    <button
                      type="button"
                      className="absolute inset-y-0 right-0 flex items-center pr-3 text-gray-400 hover:text-gray-600 focus:outline-none"
                      onClick={() => setShowPassword(!showPassword)}
                    >
                      {showPassword ? (
                        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                          <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
                          <line x1="1" y1="1" x2="23" y2="23"></line>
                        </svg>
                      ) : (
                        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                          <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                          <circle cx="12" cy="12" r="3"></circle>
                        </svg>
                      )}
                    </button>
                  </div>
                  <p className="mt-1 text-xs text-gray-500">비밀번호는 최소 6자 이상이어야 합니다.</p>
                </div>
                
                <div className="mb-6">
                  <label htmlFor="password-confirm" className="block text-sm font-medium text-gray-700 mb-1">
                    비밀번호 확인
                  </label>
                  <div className="relative">
                    <input
                      id="password-confirm"
                      name="password-confirm"
                      type={showCheckPassword ? "text" : "password"}
                      autoComplete="new-password"
                      required
                      value={checkPassword}
                      onChange={changeCheckPassword}
                      className="w-full px-4 py-2 pr-12 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                      placeholder="비밀번호를 다시 입력하세요"
                    />
                    <button
                      type="button"
                      className="absolute inset-y-0 right-0 flex items-center pr-3 text-gray-400 hover:text-gray-600 focus:outline-none"
                      onClick={() => setShowCheckPassword(!showCheckPassword)}
                    >
                      {showCheckPassword ? (
                        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                          <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
                          <line x1="1" y1="1" x2="23" y2="23"></line>
                        </svg>
                      ) : (
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
                          <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 818-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 714 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                        </svg>
                        처리 중...
                      </span>
                    ) : "회원가입"}
                  </button>
                  
                  <div className="text-center">
                    <Link to="/login" className="text-sm text-blue-600 hover:text-blue-800 font-medium">
                      로그인으로 돌아가기
                    </Link>
                  </div>
                </div>
              </form>
            </div>
          )}
        </div>
      </div>
    );
  };

  // 현재 단계에 따라 렌더링
  return isEmailVerified ? renderUserInfoStep() : renderEmailVerificationStep();
} 