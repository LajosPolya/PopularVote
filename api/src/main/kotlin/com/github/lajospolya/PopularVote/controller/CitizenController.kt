package com.github.lajospolya.PopularVote.controller

import com.github.lajospolya.PopularVote.dto.CitizenDto
import com.github.lajospolya.PopularVote.service.CitizenService
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import kotlin.coroutines.RestrictsSuspension

@RestController
class CitizenController(
    val citizenService: CitizenService,
) {
    @RequestMapping("citizen", method = [RequestMethod.GET])
    fun getCitizens(): Flux<CitizenDto> {
        return citizenService.getCitizens()
    }

    @RequestMapping("citizen/{id}", method = [RequestMethod.GET])
    fun getCitizen(@PathVariable id: Long): Mono<CitizenDto> {
        return citizenService.getCitizen(id)
    }
}