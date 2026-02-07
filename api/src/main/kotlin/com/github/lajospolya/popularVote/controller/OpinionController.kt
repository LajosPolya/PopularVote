package com.github.lajospolya.popularVote.controller

import com.github.lajospolya.popularVote.dto.CreateOpinionDto
import com.github.lajospolya.popularVote.dto.OpinionDto
import com.github.lajospolya.popularVote.service.OpinionService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
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
    @PreAuthorize("hasAuthority('SCOPE_read:opinions')")
    @RequestMapping("opinions", method = [RequestMethod.GET])
    fun getOpinions(): Flux<OpinionDto> = opinionService.getOpinions()

    @PreAuthorize("hasAuthority('SCOPE_read:opinions')")
    @RequestMapping("policies/{policyId}/opinions", method = [RequestMethod.GET])
    fun getOpinionsByPolicyId(
        @PathVariable policyId: Long,
    ): Flux<OpinionDto> = opinionService.getOpinionsByPolicyId(policyId)

    @PreAuthorize("hasAuthority('SCOPE_read:opinions')")
    @RequestMapping("opinions/{id}", method = [RequestMethod.GET])
    fun getOpinion(
        @PathVariable id: Long,
    ): Mono<OpinionDto> = opinionService.getOpinion(id)

    @PreAuthorize("hasAuthority('SCOPE_write:opinions')")
    @RequestMapping("opinions", method = [RequestMethod.POST])
    fun postOpinion(
        @RequestBody opinion: CreateOpinionDto,
        @AuthenticationPrincipal jwt: Jwt,
    ): Mono<OpinionDto> = opinionService.createOpinion(opinion, jwt.subject)

    @PreAuthorize("hasAuthority('SCOPE_delete:opinions')")
    @RequestMapping("opinions/{id}", method = [RequestMethod.DELETE])
    fun deleteOpinion(
        @PathVariable id: Long,
    ): Mono<Void> = opinionService.deleteOpinion(id)
}
