package com.luckyseven.backend.sharedkernel.exception

class CustomLogicException : RuntimeException {
    val exceptionCode: ExceptionCode
    val detail: String?
    
    constructor(exceptionCode: ExceptionCode) : super(exceptionCode.message) {
        this.exceptionCode = exceptionCode
        this.detail = null
    }
    
    constructor(exceptionCode: ExceptionCode, detail: Any?) : super(exceptionCode.message) {
        this.exceptionCode = exceptionCode
        this.detail = detail?.toString()
    }
    
    constructor(exceptionCode: ExceptionCode, vararg args: Any) : super(String.format(exceptionCode.message, *args)) {
        this.exceptionCode = exceptionCode
        this.detail = args.joinToString(", ")
    }
} 