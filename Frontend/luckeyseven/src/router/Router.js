import {Route, Routes} from "react-router-dom";
import Login from "../pages/Login/Login";
import Signup from "../pages/Login/Signup";
import TeamSetup from "../pages/Team/TeamSetup";
import Home from "../pages/Home";
import ExpensesPage from "../pages/ExpensesPage";
import {TeamSettlementsPage} from "../pages/Settlement/TeamSettlementsPage";
import {SettlementNewPage} from "../pages/Settlement/SettlementNewPage";
import {SettlementDetailPage} from "../pages/Settlement/SettlementDetailPage";
import {SettlementEditPage} from "../pages/Settlement/SettlementEditPage";

export default function Router() {
  return (
      <Routes>
        <Route path="/login" element={<Login/>}></Route>
        <Route path="/Signup" element={<Signup/>}></Route>
        <Route path="/" element={<Home/>}></Route>
        <Route path="/teams/:teamId/settlements"
               element={<TeamSettlementsPage/>}/>
        <Route path="/teams/:teamId/settlements/new"
               element={<SettlementNewPage/>}/>
        <Route path="/settlements/:settlementId"
               element={<SettlementDetailPage/>}/>
        <Route path="/teams/:teamId/settlements/:settlementId/edit"
               element={<SettlementEditPage/>}/>
        <Route path="/team-setup" element={<TeamSetup/>}></Route>
        <Route path="/TeamDashBoard" element={<TeamDashBoard/>}/>
        <Route path="/" element={<Home/>}></Route>
        <Route path="/teams/:teamId/expenses" element={<ExpensesPage/>}/>
        {/* <Route path="/teams/:teamId/settlements" element={<SettlementPage/>}/> */}
        <Route path="*" element={<Navigate to="/" replace/>}/>
      </Routes>
  )
}
