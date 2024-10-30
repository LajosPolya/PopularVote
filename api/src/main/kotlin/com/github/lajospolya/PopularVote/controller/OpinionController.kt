package com.github.lajospolya.PopularVote.controller

import com.github.lajospolya.PopularVote.dto.CitizenDto
import com.github.lajospolya.PopularVote.dto.CreateCitizenDto
import com.github.lajospolya.PopularVote.dto.CreateOpinionDto
import com.github.lajospolya.PopularVote.dto.OpinionDto
import com.github.lajospolya.PopularVote.mapper.OpinionMapper
import com.github.lajospolya.PopularVote.service.OpinionService
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
class OpinionController(
    private val opinionService: OpinionService,
) {

    @RequestMapping("opinion", method = [RequestMethod.GET])
    fun getCitizens(): Flux<OpinionDto> {
        return opinionService.getOpinions()
    }

    @RequestMapping("opinion/{id}", method = [RequestMethod.GET])
    fun getCitizen(@PathVariable id: Long): Mono<OpinionDto> {
        return opinionService.getOpinion(id)
    }

    @RequestMapping("opinion", method = [RequestMethod.POST])
    fun postCitizen(@RequestBody citizen: CreateOpinionDto): Mono<OpinionDto> {
        return opinionService.createOpinion(citizen)
    }

    @RequestMapping("opinion/{id}", method = [RequestMethod.DELETE])
    fun deleteCitizen(@PathVariable id: Long): Mono<Void> {
        return opinionService.deleteOpinion(id)
    }


}