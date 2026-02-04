package com.github.lajospolya.popularVote.controller

import com.github.lajospolya.popularVote.dto.CitizenDto
import com.github.lajospolya.popularVote.dto.CreateCitizenDto
import com.github.lajospolya.popularVote.service.CitizenService
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
class CitizenController(
    private val citizenService: CitizenService,
) {
    @RequestMapping("citizens", method = [RequestMethod.GET])
    fun getCitizens(): Flux<CitizenDto> = citizenService.getCitizens()

    @RequestMapping("citizens/{id}", method = [RequestMethod.GET])
    fun getCitizen(
        @PathVariable id: Long,
    ): Mono<CitizenDto> = citizenService.getCitizen(id)

    @RequestMapping("citizens/search", method = [RequestMethod.GET])
    fun getCitizenByName(
        @RequestParam givenName: String,
        @RequestParam surname: String,
    ): Mono<CitizenDto> = citizenService.getCitizenByName(givenName, surname)

    @RequestMapping("citizens/auth/{authId}", method = [RequestMethod.GET])
    fun getCitizenByAuthId(
        @PathVariable authId: String,
    ): Mono<CitizenDto> = citizenService.getCitizenByAuthId(authId)

    @RequestMapping("citizens", method = [RequestMethod.POST])
    fun postCitizen(
        @RequestBody citizen: CreateCitizenDto,
    ): Mono<CitizenDto> = citizenService.saveCitizen(citizen)

    @RequestMapping("citizens/{id}", method = [RequestMethod.DELETE])
    fun deleteCitizen(
        @PathVariable id: Long,
    ): Mono<Void> = citizenService.deleteCitizen(id)
}
