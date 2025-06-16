import React, { useState, useEffect } from 'react';
import styles from '../../styles/TeamSetup.module.css';
import Header from '../../components/Header';
import { createTeam, joinTeam } from '../../service/TeamService';
import { useNavigate } from 'react-router-dom';
import { useSetRecoilState } from 'recoil';
import { currentTeamIdState } from '../../recoil/atoms/teamAtoms';
import { getMyTeams } from '../../service/TeamService'; // 팀 목록 불러오기




// 공통 폼 컴포넌트
const TeamForm = ({
  mode = "create",
  onSubmit,
  loading,
  error,
  buttonText,
}) => {
  const [teamName, setTeamName] = useState('');
  const [teamCode, setTeamCode] = useState('');
  const [teamPassword, setTeamPassword] = useState('');

  const isCreate = mode === "create";

  const handleSubmit = (e) => {
    e.preventDefault();
    if (isCreate && (!teamName || !teamPassword)) return;
    if (!isCreate && (!teamCode || !teamPassword)) return;
    onSubmit({ teamName, teamPassword, teamCode });
  };

  return (
      <form className={styles.formContainer} onSubmit={handleSubmit}>
        {isCreate ? (
            <label className={styles.formLabel}>
              팀 이름
              <input
                  type="text"
                  value={teamName}
                  onChange={e => setTeamName(e.target.value)}
                  className={styles.formInput}
                  required
              />
            </label>
        ) : (
            <label className={styles.formLabel}>
              팀 코드
              <input
                  type="text"
                  value={teamCode}
                  onChange={e => setTeamCode(e.target.value)}
                  className={styles.formInput}
                  required
              />
            </label>
        )}
        <label className={styles.formLabel}>
          팀 비밀번호
          <input
              type="password"
              value={teamPassword}
              onChange={e => setTeamPassword(e.target.value)}
              className={styles.formInput}
              required
          />
        </label>
        <button type="submit" className={styles.formButton} disabled={loading}>
          {loading ? "처리 중..." : buttonText}
        </button>
        {error && <div style={{ color: "red", marginTop: "0.5rem" }}>{error}</div>}
      </form>
  );
};

const TeamSetup = () => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const setCurrentTeamId = useSetRecoilState(currentTeamIdState);
  const navigate = useNavigate();

  // 팀 목록 상태
  const [teams, setTeams] = useState([]);
  const [teamsLoading, setTeamsLoading] = useState(false);
  
  // 목업 데이터
  const MOCK_TEAMS = [
    { id: 1, name: "오사카 여행" },
    { id: 2, name: "후쿠오카 여행" },
    { id: 3, name: "도쿄 여행" },
  ];

  // useEffect 부분 임시 수정
  // useEffect(() => {
  //   // 목업 데이터로 테스트
  //   setTeams(MOCK_TEAMS);
  //   // 실제 연동 시에는 위 코드 대신 fetchTeams 함수 사용
  // }, []);
  // 내 팀 목록 불러오기
  useEffect(() => {
    const fetchTeams = async () => {
      setTeamsLoading(true);
      try {
        const myTeams = await getMyTeams();
        setTeams(myTeams);
      } catch (e) {
        setTeams([]);
      } finally {
        setTeamsLoading(false);
      }
    };
    fetchTeams();
  }, [loading]); // 팀 생성/참가 후 자동 새로고침(loading 끝나면 재호출)

  // 팀 생성
  const handleCreate = async ({ teamName, teamPassword }) => {
    setLoading(true);
    setError("");
    try {
      const newTeam = await createTeam(teamName, teamPassword);
      const teamId = newTeam.id?.toString();
      setCurrentTeamId(teamId);
      navigate(`/TeamDashBoard`);
    } catch (e) {
      setError("팀 생성에 실패했습니다. 잠시 후 다시 시도해주세요.");
    } finally {
      setLoading(false);
    }
  };

  // 팀 참가
  const handleJoin = async ({ teamCode, teamPassword }) => {
    setLoading(true);
    setError("");
    try {
      const joinedTeam = await joinTeam(teamCode, teamPassword);
      const teamId = joinedTeam.id?.toString();
      setCurrentTeamId(teamId);
      navigate(`/TeamDashBoard`);
    } catch (e) {
      setError("팀 참가에 실패했습니다. 팀 코드와 비밀번호를 확인하세요.");
    } finally {
      setLoading(false);
    }
  };

  // 팀 리스트/목업은 생략

  return (
      <div>
        <Header />
        <div className={styles.teamSetupContainer}>
          <div className={styles.mainContent}>
            <div className={styles.leftColumn}>
              <h1>팀 생성 / 참가</h1>
              <TeamForm
                  mode="create"
                  onSubmit={handleCreate}
                  loading={loading}
                  error={error}
                  buttonText="팀 생성"
              />
              <TeamForm
                  mode="join"
                  onSubmit={handleJoin}
                  loading={loading}
                  error={error}
                  buttonText="팀 참가"
              />
            </div>
            <div className={styles.rightColumn}>
              {/* 팀 카드/목록은 이후 구현 */}
              <div className={styles.teamCardContainer}>
                <h1>내 팀</h1>
                {teamsLoading ? (
                <div className={styles.noTeamsCard}>
                  <p>팀 목록을 불러오는 중...</p>
                </div>
              ) : teams.length === 0 ? (
                <div className={styles.noTeamsCard}>
                  <p>팀을 생성하거나 참여하세요!</p>
                </div>
              ) : (
                teams.map((team) => (
                  <div key={team.id} className={styles.teamCard}>
                    <p className={styles.teamCardTitle}>{team.name}</p>
                    <button
                      className={styles.teamCardButton}
                      onClick={() => {
                        setCurrentTeamId(team.id);
                        navigate(`/TeamDashBoard`);
                      }}
                    >
                      팀 작업 공간으로 이동
                    </button>
                  </div>
                ))
              )}
              </div>
            </div>
          </div>
        </div>
      </div>
  );
};

export default TeamSetup;
