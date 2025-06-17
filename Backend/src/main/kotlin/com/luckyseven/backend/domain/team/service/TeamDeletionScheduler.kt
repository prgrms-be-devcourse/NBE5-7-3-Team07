package com.luckyseven.backend.domain.team.service

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class TeamDeletionScheduler(private val teamService: TeamService) {
    @Scheduled(cron = "0 0 0 * * ?")
    fun processTeamDeletion() {
        teamService.deleteMarkedTeams()
    }
}