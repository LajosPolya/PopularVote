package com.github.lajospolya.popularVote.controller.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler

class VotingClosedException : RuntimeException("Voting period has closed")

@ControllerAdvice
class VotingClosedExceptionHandler : ResponseEntityExceptionHandler() {
    @ExceptionHandler(value = [VotingClosedException::class])
    fun handleException(): ResponseEntity<Void> = ResponseEntity(HttpStatus.UNPROCESSABLE_ENTITY)
}
