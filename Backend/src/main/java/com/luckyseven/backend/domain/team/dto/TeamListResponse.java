package com.luckyseven.backend.domain.team.dto;

public record TeamListResponse(
    Long id,
    String name,
    String teamCode
) {}