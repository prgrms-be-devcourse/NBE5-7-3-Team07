package com.luckyseven.backend.sharedkernel.dto

import org.springframework.data.domain.Page

data class PageResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalPages: Int,
    val totalElements: Long
) {
    companion object {
        fun <T> fromPage(pageData: Page<T>): PageResponse<T> = PageResponse(
            content = pageData.content,
            page = pageData.number,
            size = pageData.size,
            totalPages = pageData.totalPages,
            totalElements = pageData.totalElements
        )
    }
}
