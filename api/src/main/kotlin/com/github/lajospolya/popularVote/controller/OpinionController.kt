package com.github.lajospolya.popularVote.controller

import com.github.lajospolya.popularVote.dto.CreateOpinionDto
import com.github.lajospolya.popularVote.dto.OpinionDto
import com.github.lajospolya.popularVote.service.OpinionService
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
    @RequestMapping("opinions", method = [RequestMethod.GET])
    fun getOpinions(): Flux<OpinionDto> = opinionService.getOpinions()

    @RequestMapping("opinions/{id}", method = [RequestMethod.GET])
    fun getOpinion(
        @PathVariable id: Long,
    ): Mono<OpinionDto> = opinionService.getOpinion(id)

    @RequestMapping("opinions", method = [RequestMethod.POST])
    fun postOpinion(
        @RequestBody citizen: CreateOpinionDto,
    ): Mono<OpinionDto> = opinionService.createOpinion(citizen)

    @RequestMapping("opinions/{id}", method = [RequestMethod.DELETE])
    fun deleteOpinion(
        @PathVariable id: Long,
    ): Mono<Void> = opinionService.deleteOpinion(id)
}
