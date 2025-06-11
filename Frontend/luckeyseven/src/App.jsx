import React from "react"
import {
  BrowserRouter as Router,
  Navigate,
  Route,
  Routes
} from "react-router-dom"
import Login from "./pages/Login/Login"
import Signup from "./pages/Login/Signup"
import Home from "./pages/Home"
import {TeamSettlementsPage} from "./pages/Settlement/TeamSettlementsPage"
import {SettlementNewPage} from "./pages/Settlement/SettlementNewPage"
import {SettlementEditPage} from "./pages/Settlement/SettlementEditPage"
import {SettlementDetailPage} from "./pages/Settlement/SettlementDetailPage"
import ExpenseList from "./pages/ExpenseDialog/ExpenseList"
import "./styles/auth.css"
import {getCurrentUser} from "./service/AuthService"
import TeamDashBoard from "./pages/TeamDashBoard";
import TeamSetup from "./pages/TeamSetup"
import {ToastProvider} from "./context/ToastContext"

// 보호된 라우트 컴포넌트
const ProtectedRoute = ({children}) => {
  const user = getCurrentUser();

  if (!user) {
    // 로그인되지 않은 경우 로그인 페이지로 리다이렉트
    return <Navigate to="/login" replace/>;
  } 

  return children;
};

function App() {
  return (
      <ToastProvider>
        <Router>
          <Routes>
            <Route path="/login" element={<Login/>}/>
            <Route path="/signup" element={<Signup/>}/>
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