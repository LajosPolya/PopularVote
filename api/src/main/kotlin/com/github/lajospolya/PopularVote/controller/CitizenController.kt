package com.github.lajospolya.PopularVote.controller

import com.github.lajospolya.PopularVote.dto.CitizenDto
import com.github.lajospolya.PopularVote.service.CitizenService
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import kotlin.coroutines.RestrictsSuspension

@RestController
class CitizenController(
    val citizenService: CitizenService,
) {
    @RequestMapping("citizen")
    fun getCitizens(): Flux<CitizenDto> {
        return citizenService.getCitizens()
    }
}