package com.github.lajospolya.popularVote.controller.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class ExceptionHandler : ResponseEntityExceptionHandler() {
    @ExceptionHandler(value = [ResourceNotFoundException::class])
    fun handleException(): ResponseEntity<Void> {
        return ResponseEntity(HttpStatus.NOT_FOUND)
    }
}
