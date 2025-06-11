import {Link} from "react-router-dom"
import Header from "../../components/Header";

export function HomePage() {
  return (
      <div>
        <Header/>
        <div className="container py-8">
          <div className="text-center mb-12">
            <h1 className="text-4xl font-bold mb-4">여행 정산 관리</h1>
            <p className="text-xl text-muted">여행 경비를 쉽고 간편하게 정산하세요.</p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            <div className="card">
              <div className="card-header">
                <h3 className="card-title">정산 내역 관리</h3>
                <p className="text-sm text-muted">여행 중 발생한 정산 내역을 관리합니다.</p>
              </div>
              <div className="card-content">
                <p>결제자, 정산자, 지출 항목 등을 기록하고 정산 상태를 추적할 수 있습니다.</p>
              </div>
              <div className="card-footer">
                <Link to="/teams/team1/settlements" className="w-full">
                  <button className="btn btn-primary w-full">정산 내역 보기</button>
                </Link>
              </div>
            </div>

            <div className="card">
              <div className="card-header">
                <h3 className="card-title">새 정산 생성</h3>
                <p className="text-sm text-muted">새로운 정산 내역을 생성합니다.</p>
              </div>
              <div className="card-content">
                <p>여행 중 발생한 지출에 대한 정산 내역을 생성하고 관리할 수 있습니다.</p>
              </div>
              <div className="card-footer">
                <Link to="/settlements/new" className="w-full">
                  <button className="btn btn-primary w-full">새 정산 생성</button>
                </Link>
              </div>
            </div>

            <div className="card">
              <div className="card-header">
                <h3 className="card-title">정산 통계</h3>
                <p className="text-sm text-muted">정산 내역에 대한 통계를 확인합니다.</p>
              </div>
              <div className="card-content">
                <p>팀원별 지출 및 정산 금액, 정산 완료 상태 등의 통계를 확인할 수 있습니다.</p>
              </div>
              <div className="card-footer">
                <button className="btn btn-primary w-full" disabled>
                  준비 중
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
  )
}
