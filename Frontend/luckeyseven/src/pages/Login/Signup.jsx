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
  const [currentStep, setCurrentStep] = useState(1); // 1: 이메일 입력, 2: 인증 대기, 3: 인증 완료, 4: 회원정보 입력, 5: 가입 완료
  const [countdown, setCountdown] = useState(3); // 회원가입 완료 후 카운트다운
  
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

  // 5단계 완료 시 카운트다운 타이머
  useEffect(() => {
    if (currentStep === 5) {
      const timer = setInterval(() => {
        setCountdown((prev) => {
          if (prev <= 1) {
            clearInterval(timer);
            localStorage.removeItem('verifiedEmail');
            localStorage.removeItem('emailVerified');
            navigate('/login');
            return 0;
          }
          return prev - 1;
        });
      }, 1000);

      return () => clearInterval(timer);
    } else {
      // 5단계가 아닐 때는 카운트다운 리셋
      setCountdown(3);
    }
  }, [currentStep, navigate]);

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
      setCurrentStep(2); // 인증 대기 단계로 이동
      // 인증 이메일 주소를 로컬 스토리지에 저장
      localStorage.setItem('verifiedEmail', email);
    } catch (err) {
      setError(err.response?.data?.message || "이메일 인증 요청 중 오류가 발생했습니다.");
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
      
      // alert 대신 5단계로 이동
      setCurrentStep(5);
      
    } catch (err) {
      setError(err.response?.data?.message || "회원가입 중 오류가 발생했습니다.");
    } finally {
      setIsLoading(false);
    }
  };

  // 5단계 프로그레스 표시기 컴포넌트
  const ProgressIndicator = ({ currentStep }) => {
    const steps = [
      { id: 1, title: "이메일 입력" },
      { id: 2, title: "인증 대기" },
      { id: 3, title: "인증 완료" },
      { id: 4, title: "정보 입력" },
      { id: 5, title: "가입 완료" }
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
                      {/* 이메일 날아가는 애니메이션 영역 */}
                      <div className="relative w-full h-12 flex items-center justify-center">
                        {/* 날아가는 이메일들 */}
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
                        
                        {/* 점선 경로 */}
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
                
                {/* CSS 애니메이션 추가 */}
                <style>
                  {`
                    @keyframes progress {
                      0% { width: 0%; }
                      50% { width: 70%; }
                      100% { width: 100%; }
                    }
                    
                    @keyframes flyEmail1 {
                      0% { 
                        transform: translateX(-100px) translateY(0px) rotate(0deg);
                        opacity: 0;
                      }
                      20% { 
                        opacity: 1;
                      }
                      100% { 
                        transform: translateX(100px) translateY(-20px) rotate(15deg);
                        opacity: 0;
                      }
                    }
                    
                    @keyframes flyEmail2 {
                      0% { 
                        transform: translateX(-80px) translateY(5px) rotate(-5deg);
                        opacity: 0;
                      }
                      30% { 
                        opacity: 0.7;
                      }
                      100% { 
                        transform: translateX(120px) translateY(-15px) rotate(20deg);
                        opacity: 0;
                      }
                    }
                    
                    @keyframes flyEmail3 {
                      0% { 
                        transform: translateX(-60px) translateY(-5px) rotate(5deg);
                        opacity: 0;
                      }
                      40% { 
                        opacity: 0.5;
                      }
                      100% { 
                        transform: translateX(140px) translateY(-25px) rotate(25deg);
                        opacity: 0;
                      }
                    }
                    
                    @keyframes dottedTrail {
                      0% { 
                        width: 0%;
                        opacity: 0;
                      }
                      50% { 
                        width: 80%;
                        opacity: 1;
                      }
                      100% { 
                        width: 100%;
                        opacity: 0;
                      }
                    }
                    
                    .flying-email-1 {
                      position: absolute;
                      animation: flyEmail1 2.5s ease-out infinite;
                      animation-delay: 0s;
                    }
                    
                    .flying-email-2 {
                      position: absolute;
                      animation: flyEmail2 2.8s ease-out infinite;
                      animation-delay: 0.3s;
                    }
                    
                    .flying-email-3 {
                      position: absolute;
                      animation: flyEmail3 3s ease-out infinite;
                      animation-delay: 0.6s;
                    }
                    
                    .dotted-trail {
                      position: absolute;
                      top: 50%;
                      left: 0;
                      height: 2px;
                      background: linear-gradient(to right, 
                        transparent, 
                        rgba(255,255,255,0.3) 20%, 
                        rgba(255,255,255,0.6) 50%, 
                        rgba(255,255,255,0.3) 80%, 
                        transparent
                      );
                      background-size: 10px 2px;
                      border-radius: 1px;
                      animation: dottedTrail 2s ease-in-out infinite;
                    }
                    
                    .flying-email-small-1 {
                      position: absolute;
                      animation: flyEmailSmall1 1.8s ease-out infinite;
                    }
                    
                    .flying-email-small-2 {
                      position: absolute;
                      animation: flyEmailSmall2 2.2s ease-out infinite;
                      animation-delay: 0.4s;
                    }
                    
                    .dotted-trail-small {
                      position: absolute;
                      top: 50%;
                      left: 0;
                      height: 1px;
                      background: linear-gradient(to right, 
                        transparent, 
                        rgba(255,255,255,0.4) 30%, 
                        rgba(255,255,255,0.7) 50%, 
                        rgba(255,255,255,0.4) 70%, 
                        transparent
                      );
                      border-radius: 1px;
                      animation: dottedTrailSmall 1.5s ease-in-out infinite;
                    }
                    
                    @keyframes flyEmailSmall1 {
                      0% { 
                        transform: translateX(-40px) translateY(0px) rotate(0deg);
                        opacity: 0;
                      }
                      25% { 
                        opacity: 1;
                      }
                      100% { 
                        transform: translateX(50px) translateY(-10px) rotate(12deg);
                        opacity: 0;
                      }
                    }
                    
                    @keyframes flyEmailSmall2 {
                      0% { 
                        transform: translateX(-30px) translateY(3px) rotate(-3deg);
                        opacity: 0;
                      }
                      35% { 
                        opacity: 0.6;
                      }
                      100% { 
                        transform: translateX(60px) translateY(-8px) rotate(15deg);
                        opacity: 0;
                      }
                    }
                    
                    @keyframes dottedTrailSmall {
                      0% { 
                        width: 0%;
                        opacity: 0;
                      }
                      50% { 
                        width: 70%;
                        opacity: 1;
                      }
                      100% { 
                        width: 100%;
                        opacity: 0;
                      }
                    }
                    
                    .border-3 {
                      border-width: 3px;
                    }
                  `}
                </style>
                
                <div className="mt-6 text-center">
                  <Link to="/login" className="text-sm text-blue-600 hover:text-blue-800 font-medium">
                    로그인으로 돌아가기
                  </Link>
                </div>
              </form>
            </div>
          ) : (
            // 2단계: 이메일 인증 대기 화면
            <div className="auth-card rounded-lg shadow-md border border-gray-100">
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


                
                <div className="space-y-3">
                  <button
                    type="button"
                    className="w-full py-3 px-4 border border-gray-300 rounded-lg text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 font-medium"
                    onClick={() => {
                      setCurrentStep(1);
                    }}
                  >
                    다른 이메일 주소 사용하기
                  </button>
                  
                  <div className="p-4 rounded-lg bg-gray-50 text-center border border-gray-100">
                    <p className="text-sm text-gray-600 mb-3">메일이 오지 않나요?</p>
                    <div className="space-y-2">
                      <p className="text-xs text-gray-500">스팸함을 확인하거나 다시 시도해보세요</p>
                      <button
                        onClick={handleSendVerification}
                        className={`py-3 px-6 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 font-medium transition-all duration-200 ${
                          isLoading 
                            ? 'bg-blue-400 cursor-not-allowed text-white' 
                            : 'bg-blue-600 hover:bg-blue-700 hover:shadow-lg text-white'
                        }`}
                        disabled={isLoading}
                      >
                        {isLoading ? (
                          <div className="flex items-center justify-center relative overflow-hidden py-3">
                            {/* 작은 이메일 날아가는 애니메이션 */}
                            <div className="relative w-24 h-8 flex items-center justify-center">
                              <div className="flying-email-small-1">
                                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="2">
                                  <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path>
                                  <polyline points="22,6 12,13 2,6"></polyline>
                                </svg>
                              </div>
                              <div className="flying-email-small-2">
                                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="rgba(255,255,255,0.6)" strokeWidth="2">
                                  <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path>
                                  <polyline points="22,6 12,13 2,6"></polyline>
                                </svg>
                              </div>
                              <div className="dotted-trail-small"></div>
                            </div>
                          </div>
                        ) : (
                          <span className="flex items-center justify-center">
                            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="mr-2">
                              <path d="M21.2 8.4c.5.38.8.97.8 1.6v10a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V10a2 2 0 0 1 .8-1.6l8-6a2 2 0 0 1 2.4 0l8 6Z"></path>
                              <path d="m22 10-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 10"></path>
                            </svg>
                            인증 메일 다시 보내기
                          </span>
                        )}
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
          {/* 4단계 프로그레스 표시기 */}
          <ProgressIndicator currentStep={currentStep} />

          {currentStep === 3 ? (
            // 3단계: 인증 완료 화면
            <div className="auth-card rounded-lg shadow-md border border-gray-100">
              <div className="p-8 rounded-lg bg-gradient-to-br from-green-50 to-emerald-50 text-center border border-green-100">
                {/* 성공 아이콘 */}
                <div className="flex justify-center mb-6">
                  <div className="relative">
                    <div className="w-20 h-20 bg-white rounded-full flex items-center justify-center shadow-lg border border-green-100">
                      <svg xmlns="http://www.w3.org/2000/svg" width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="#10b981" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                        <polyline points="22 4 12 14.01 9 11.01"></polyline>
                      </svg>
                    </div>
                    {/* 성공 펄스 효과 */}
                    <div className="absolute inset-0 rounded-full border-4 border-green-200 animate-ping opacity-30"></div>
                  </div>
                </div>
                
                <h3 className="text-2xl font-bold text-gray-800 mb-3">🎉 이메일 인증 완료!</h3>
                <p className="text-gray-600 mb-4 text-lg">
                  <span className="font-semibold text-green-600 block mb-2">{email}</span>
                  이메일 인증이 성공적으로 완료되었습니다
                </p>
                
                {/* 진행 상태 표시 */}
                <div className="max-w-sm mx-auto mb-6">
                  <div className="flex items-center justify-center p-4 rounded-lg bg-white border border-green-100 shadow-sm">
                    <div className="flex items-center">
                      <div className="mr-4">
                        <div className="w-3 h-3 bg-green-500 rounded-full animate-bounce mr-1 inline-block"></div>
                        <div className="w-3 h-3 bg-green-400 rounded-full animate-bounce mr-1 inline-block" style={{animationDelay: '0.1s'}}></div>
                        <div className="w-3 h-3 bg-green-300 rounded-full animate-bounce inline-block" style={{animationDelay: '0.2s'}}></div>
                      </div>
                      <div className="text-left">
                        <p className="font-semibold text-gray-800 text-sm">인증 완료</p>
                        <p className="text-gray-500 text-xs">추가 정보 입력 화면으로 이동합니다</p>
                      </div>
                    </div>
                  </div>
                </div>



                {/* 다음 단계로 가기 버튼 */}
                <button
                  onClick={() => setCurrentStep(4)}
                  className="w-full py-3 px-6 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 transition-all duration-200 font-medium text-lg"
                >
                  회원정보 입력 →
                </button>
              </div>
            </div>
          ) : (
            // 4단계: 회원정보 입력 화면
            <div className="auth-card rounded-lg shadow-md border border-gray-100">
              <div className="auth-header p-6 border-b border-gray-100">
                <h1 className="auth-title text-2xl font-bold text-center">추가 정보 입력</h1>
                <p className="auth-subtitle text-center text-gray-500 mt-2">이메일 인증이 완료되었습니다. 추가 정보를 입력해주세요.</p>
              </div>
              <form className="auth-form p-6" method="POST" onSubmit={handleJoin}>
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
                    value={email}
                    className="w-full px-4 py-2 bg-gray-100 border border-gray-300 rounded-md text-gray-500"
                    disabled={true}
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

  // 5단계: 회원가입 완료 화면
  const renderCompletionStep = () => {
    return (
      <div className="min-h-screen flex items-center justify-center p-4">
        <div className="w-full max-w-md">
          {/* 5단계 프로그레스 표시기 */}
          <ProgressIndicator currentStep={currentStep} />

          <div className="auth-card completion-card rounded-lg shadow-md border border-gray-100 text-center success-bounce">
            <div className="p-8">
              {/* 성공 애니메이션 */}
              <div className="mb-6">
                <div className="w-20 h-20 mx-auto bg-green-100 rounded-full flex items-center justify-center mb-4">
                  <div className="checkmark-container">
                    <svg className="checkmark w-12 h-12 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path className="checkmark-path" strokeLinecap="round" strokeLinejoin="round" strokeWidth="3" d="M5 13l4 4L19 7"/>
                    </svg>
                  </div>
                </div>
              </div>

              {/* 완료 메시지 */}
              <h1 className="text-3xl font-bold text-gray-800 mb-3">
                🎉 회원가입이 완료되셨습니다!
              </h1>
              
              <p className="text-gray-600 mb-6 text-lg">
                <span className="font-semibold text-green-600 block mb-2">{nickname}님</span>
                환영합니다! 곧 로그인 페이지로 이동합니다.
              </p>

              {/* 자동 이동 카운트다운 */}
              <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-6">
                <div className="flex items-center justify-center">
                  <svg className="animate-spin -ml-1 mr-3 h-5 w-5 text-blue-600" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                                     <span className="text-blue-600 font-medium">{countdown}초 후 로그인 페이지로 이동합니다</span>
                </div>
              </div>

              {/* 즉시 이동 버튼 */}
              <button
                onClick={() => {
                  localStorage.removeItem('verifiedEmail');
                  localStorage.removeItem('emailVerified');
                  navigate('/login');
                }}
                className="w-full py-3 px-6 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 transition-all duration-200 font-medium"
              >
                지금 로그인하기 →
              </button>
            </div>
          </div>
        </div>
      </div>
    );
  };

  // 현재 단계에 따라 렌더링
  if (currentStep === 5) {
    return renderCompletionStep();
  }
  return isEmailVerified ? renderUserInfoStep() : renderEmailVerificationStep();
} 