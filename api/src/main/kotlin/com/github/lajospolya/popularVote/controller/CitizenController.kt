package com.github.lajospolya.popularVote.controller

import com.github.lajospolya.popularVote.dto.CitizenDto
import com.github.lajospolya.popularVote.dto.CreateCitizenDto
import com.github.lajospolya.popularVote.service.CitizenService
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
class CitizenController(
    private val citizenService: CitizenService,
) {
    @RequestMapping("citizen", method = [RequestMethod.GET])
    fun getCitizens(): Flux<CitizenDto> {
        return citizenService.getCitizens()
    }

    @RequestMapping("citizen/{id}", method = [RequestMethod.GET])
    fun getCitizen(
        @PathVariable id: Long,
    ): Mono<CitizenDto> {
        return citizenService.getCitizen(id)
    }

    @RequestMapping("citizen", method = [RequestMethod.POST])
    fun postCitizen(
        @RequestBody citizen: CreateCitizenDto,
    ): Mono<CitizenDto> {
        return citizenService.saveCitizen(citizen)
    }

    @RequestMapping("citizen/{id}", method = [RequestMethod.DELETE])
    fun deleteCitizen(
        @PathVariable id: Long,
    ): Mono<Void> {
        return citizenService.deleteCitizen(id)
    }
}
