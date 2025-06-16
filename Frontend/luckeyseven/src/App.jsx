import React, {useEffect} from "react"
import {
  BrowserRouter as Router,
  Navigate,
  Route,
  Routes,
  useLocation
} from "react-router-dom"
import Login from "./pages/Login/Login"
import Signup from "./pages/Login/Signup"
import EmailVerification from "./pages/Login/EmailVerification"
import EmailVerificationHandler from "./pages/Login/EmailVerificationHandler"
import Home from "./pages/Home"
import {TeamSettlementsPage} from "./pages/Settlement/TeamSettlementsPage"
import {SettlementNewPage} from "./pages/Settlement/SettlementNewPage"
import {SettlementEditPage} from "./pages/Settlement/SettlementEditPage"
import {SettlementDetailPage} from "./pages/Settlement/SettlementDetailPage"
import ExpenseList from "./pages/ExpenseDialog/ExpenseList"
import "./styles/auth.css"
import {getCurrentUser, verifyEmailToken} from "./service/AuthService"
import TeamDashBoard from "./pages/TeamDashBoard";
import TeamSetup from "./pages/TeamSetup"
import {ToastProvider} from "./context/ToastContext"
import {
  SettlementAggregationPage
} from "./pages/Settlement/SettlementAggregationPage";

// 보호된 라우트 컴포넌트
const ProtectedRoute = ({children}) => {
  const user = getCurrentUser();

  if (!user) {
    // 로그인되지 않은 경우 로그인 페이지로 리다이렉트
    return <Navigate to="/login" replace/>;
  }

  return children;
};

// 전역 중복 요청 방지
let globalVerificationInProgress = false;

// 이메일 인증 URL 처리를 위한 리다이렉트 컴포넌트
const EmailRedirect = () => {
  const location = useLocation();
  const [status, setStatus] = React.useState("processing"); // processing, success, error
  const verificationInProgress = React.useRef(false); // 중복 요청 방지용 ref
  const hasVerified = React.useRef(false); // 검증 완료 여부

  useEffect(() => {
    // 이미 검증했거나 진행 중인 경우 중복 요청 방지
    if (hasVerified.current || verificationInProgress.current
        || globalVerificationInProgress) {
      return;
    }

    const verifyToken = async () => {
      try {
        const queryParams = new URLSearchParams(location.search);
        const token = queryParams.get('token');

        if (!token) {
          setStatus("error");
          return;
        }

        // 같은 토큰으로 이미 처리된 경우 중복 요청 방지
        const processedTokens = JSON.parse(
            localStorage.getItem('processedTokens') || '[]');
        if (processedTokens.includes(token)) {
          setStatus("success");
          hasVerified.current = true;
          return;
        }

        // 전역 및 로컬 진행 중 플래그 설정 (중복 요청 방지)
        globalVerificationInProgress = true;
        verificationInProgress.current = true;

        // 백엔드 API 직접 호출하여 토큰 검증
        const response = await verifyEmailToken(token);

        if (response.status === 200) {
          // 이메일 주소 추출 (백엔드 응답에서 이메일을 반환하는 경우)
          const email = response.data?.email || '';

          // 인증 성공 시 로컬 스토리지에 인증 상태 저장
          localStorage.setItem('emailVerified', 'true');
          if (email) {
            localStorage.setItem('verifiedEmail', email);
          }

          // 기존 창에서 감지할 수 있도록 완료 플래그 설정
          localStorage.setItem('emailVerificationCompleted', 'true');

          // 처리된 토큰 목록에 추가 (중복 요청 방지)
          const processedTokens = JSON.parse(
              localStorage.getItem('processedTokens') || '[]');
          processedTokens.push(token);
          localStorage.setItem('processedTokens',
              JSON.stringify(processedTokens));

          setStatus("success");
          hasVerified.current = true;

        } else {
          setStatus("error");
          hasVerified.current = true;
        }
      } catch (err) {
        setStatus("error");
        hasVerified.current = true;
      } finally {
        // 진행 중 플래그 해제
        globalVerificationInProgress = false;
        verificationInProgress.current = false;
      }
    };

    verifyToken();
  }, [location.search]); // location.search만 의존성으로 설정

  return (
      <div
          className="min-h-screen flex items-center justify-center p-4 bg-gradient-to-br from-blue-50 to-indigo-100">
        <div
            className="w-full max-w-md bg-white rounded-xl shadow-lg border border-gray-100 p-8">
          <div className="text-center">
            {status === "processing" && (
                <>
                  <div className="mb-6 flex justify-center">
                    <div
                        className="w-20 h-20 bg-blue-100 rounded-full flex items-center justify-center">
                      <svg className="animate-spin h-10 w-10 text-blue-600"
                           xmlns="http://www.w3.org/2000/svg" fill="none"
                           viewBox="0 0 24 24">
                        <circle className="opacity-25" cx="12" cy="12" r="10"
                                stroke="currentColor" strokeWidth="4"></circle>
                        <path className="opacity-75" fill="currentColor"
                              d="M4 12a8 8 0 818-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 714 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                      </svg>
                    </div>
                  </div>
                  <h2 className="text-2xl font-semibold text-gray-800 mb-3">이메일
                    인증 처리 중</h2>
                  <p className="text-gray-600 mb-6">잠시만 기다려주세요...</p>
                  <div className="max-w-xs mx-auto">
                    <div
                        className="h-2 bg-gray-200 rounded-full overflow-hidden">
                      <div
                          className="h-full bg-blue-600 rounded-full animate-pulse"></div>
                    </div>
                  </div>
                </>
            )}

            {status === "success" && (
                <>
                  <div className="mb-6 flex justify-center relative">
                    <div
                        className="w-24 h-24 bg-green-100 rounded-full flex items-center justify-center">
                      <svg className="h-12 w-12 text-green-600"
                           xmlns="http://www.w3.org/2000/svg" fill="none"
                           viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round"
                              strokeWidth="2" d="M5 13l4 4L19 7"></path>
                      </svg>
                    </div>
                    {/* 성공 펄스 효과 */}
                    <div
                        className="absolute inset-0 rounded-full border-4 border-green-300 animate-ping opacity-30"></div>
                  </div>

                  <h2 className="text-3xl font-bold text-gray-800 mb-4">🎉 인증이
                    완료되었습니다!</h2>
                  <p className="text-lg text-gray-600 mb-8">이메일 인증이 성공적으로
                    완료되었습니다</p>

                  <div
                      className="max-w-sm mx-auto p-6 rounded-xl bg-gradient-to-r from-green-50 to-emerald-50 border border-green-100 mb-6">
                    <div className="flex items-center justify-center mb-3">
                      <svg xmlns="http://www.w3.org/2000/svg" width="20"
                           height="20" viewBox="0 0 24 24" fill="none"
                           stroke="#10b981" strokeWidth="2"
                           strokeLinecap="round" strokeLinejoin="round"
                           className="mr-2">
                        <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                        <polyline points="22 4 12 14.01 9 11.01"></polyline>
                      </svg>
                      <p className="text-green-700 font-semibold">이메일 인증 성공</p>
                    </div>
                    <p className="text-sm text-green-600 mb-4">
                      기존 회원가입 창에서 자동으로 다음 단계가 진행됩니다
                    </p>
                    <div
                        className="h-2 bg-green-200 rounded-full overflow-hidden">
                      <div className="h-full bg-green-500 animate-pulse"></div>
                    </div>
                  </div>

                  {/* 안내 메시지 */}
                  <div
                      className="p-4 rounded-lg bg-blue-50 border border-blue-100 mb-6">
                    <div className="flex items-start justify-center">
                      <svg xmlns="http://www.w3.org/2000/svg" width="18"
                           height="18" viewBox="0 0 24 24" fill="none"
                           stroke="#3b82f6" strokeWidth="2"
                           strokeLinecap="round" strokeLinejoin="round"
                           className="mr-2 mt-0.5 flex-shrink-0">
                        <circle cx="12" cy="12" r="10"></circle>
                        <path d="M12 16v-4"></path>
                        <path d="M12 8h.01"></path>
                      </svg>
                      <div className="text-center">
                        <p className="text-sm text-blue-800 font-medium mb-1">안내</p>
                        <p className="text-xs text-blue-600">
                          이 창을 닫고 원래 회원가입 창에서<br/>
                          계속 진행해주세요
                        </p>
                      </div>
                    </div>
                  </div>

                  {/* 창 닫기 버튼 */}
                  <div className="flex justify-center">
                    <button
                        onClick={() => {
                          try {
                            window.close();
                          } catch (e) {
                            alert("이 창을 닫고 회원가입 창으로 돌아가세요.");
                          }
                        }}
                        className="py-3 px-8 bg-gradient-to-r from-blue-600 to-blue-700 text-white rounded-lg hover:from-blue-700 hover:to-blue-800 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 font-medium transition-all duration-200 shadow-md"
                    >
                      창 닫기
                    </button>
                  </div>

                  <p className="text-xs text-gray-500 mt-4">
                    창이 자동으로 닫히지 않으면 수동으로 닫아주세요
                  </p>
                </>
            )}

            {status === "error" && (
                <>
                  <div className="mb-6 flex justify-center">
                    <div
                        className="w-20 h-20 bg-red-100 rounded-full flex items-center justify-center">
                      <svg className="h-10 w-10 text-red-600"
                           xmlns="http://www.w3.org/2000/svg" fill="none"
                           viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round"
                              strokeWidth="2" d="M6 18L18 6M6 6l12 12"></path>
                      </svg>
                    </div>
                  </div>
                  <h2 className="text-2xl font-semibold text-gray-800 mb-3">인증
                    오류</h2>
                  <p className="text-gray-600 mb-6">이메일 인증에 실패했습니다. 다시
                    시도해주세요.</p>
                  <div className="flex flex-col items-center space-y-4">
                    <button
                        onClick={() => window.location.reload()}
                        className="px-6 py-3 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 font-medium"
                    >
                      다시 시도하기
                    </button>
                    <button
                        onClick={() => {
                          try {
                            window.close();
                          } catch (e) {
                            alert("이 창을 닫고 회원가입을 다시 시도해주세요.");
                          }
                        }}
                        className="text-sm text-gray-600 hover:text-gray-800 font-medium"
                    >
                      창 닫기
                    </button>
                  </div>
                </>
            )}
          </div>
        </div>
      </div>
  );
};

function App() {
  return (
      <ToastProvider>
        <Router>
          <Routes>
            <Route path="/login" element={<Login/>}/>
            <Route path="/signup" element={<Signup/>}/>
            <Route path="/verify-email" element={<EmailVerification/>}/>
            <Route path="/email-verification-handler"
                   element={<EmailVerificationHandler/>}/>
            <Route path="/email-verify" element={<EmailRedirect/>}/>
            <Route path="/TeamDashBoard" element={<TeamDashBoard/>}/>
            <Route
                path="/"
                element={
                  <ProtectedRoute>
                    <Home/>
                  </ProtectedRoute>
                }
            />
            <Route path="/team-setup" element={<TeamSetup/>}/>

            {/* Settlement 관련 라우트 */}
            <Route
                path="/teams/:teamId/settlements"
                element={
                  <ProtectedRoute>
                    <TeamSettlementsPage/>
                  </ProtectedRoute>
                }
            />
            <Route
                path="/teams/:teamId/settlements/aggregation"
                element={
                  <ProtectedRoute>
                    <SettlementAggregationPage/>
                  </ProtectedRoute>}
            />
            <Route
                path="/teams/:teamId/settlements/new"
                element={
                  <ProtectedRoute>
                    <SettlementNewPage/>
                  </ProtectedRoute>
                }
            />
            <Route
                path="/settlements/:settlementId"
                element={
                  <ProtectedRoute>
                    <SettlementDetailPage/>
                  </ProtectedRoute>
                }
            />
            <Route
                path="/teams/:teamId/settlements/:settlementId/edit"
                element={
                  <ProtectedRoute>
                    <SettlementEditPage/>
                  </ProtectedRoute>
                }
            />

            {/* Expense 관련 라우트 */}
            <Route
                path="/expenses"
                element={
                  <ProtectedRoute>
                    <ExpenseList/>
                  </ProtectedRoute>
                }
            />
            <Route
                path="/teams/:teamId/expenses"
                element={
                  <ProtectedRoute>
                    <ExpenseList/>
                  </ProtectedRoute>
                }
            />
          </Routes>
        </Router>
      </ToastProvider>
  )
}

export default App