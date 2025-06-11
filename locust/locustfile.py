from locust import HttpUser, task, between

class TeamDashboardUser(HttpUser):
    wait_time = between(0.01, 0.2)   # 호출 사이 시간

    team_id = 1   # 사용할 팀 ID (임의로 1번, 실제 값에 맞게 변경)

    @task
    def get_team_dashboard(self):
        for _ in range(2000):    # 2,000회 반복 호출
            self.client.get(f"/api/team/{self.team_id}/dashboard")