package com.github.lajospolya.popularVote.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
class HealthController {
    @RequestMapping("health", method = [RequestMethod.GET])
    fun health(): ResponseEntity<Void> = ResponseEntity(HttpStatus.NO_CONTENT)
}
