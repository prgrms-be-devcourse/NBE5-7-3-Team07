package com.luckyseven.backend.sharedkernel.exception

data class ErrorResponse(
    val code: String,
    val message: String,
    val detail: String?
) {
    companion object {
        fun from(e: CustomLogicException): ErrorResponse {
            return ErrorResponse(
                code = e.exceptionCode.name,
                message = e.exceptionCode.message,
                detail = e.detail
            )
        }
    }
} 