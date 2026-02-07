package com.github.lajospolya.popularVote.controller

import com.github.lajospolya.popularVote.dto.VoteDto
import com.github.lajospolya.popularVote.service.VoteService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
class VoteController(
    private val voteService: VoteService,
) {
    @RequestMapping("votes", method = [RequestMethod.POST])
    fun postVotes(
        @RequestBody voteDto: VoteDto,
        @AuthenticationPrincipal jwt: Jwt,
    ): Mono<Boolean> = voteService.vote(jwt.subject, voteDto.policyId, voteDto.selectionId)
}
