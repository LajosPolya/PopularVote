package com.github.lajospolya.popularVote.controller

import com.github.lajospolya.popularVote.dto.VoteDto
import com.github.lajospolya.popularVote.service.VoteService
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
class VoteController(
    private val voteService: VoteService,
) {

    @RequestMapping("vote", method = [RequestMethod.POST])
    fun postPolicy(@RequestBody voteDto: VoteDto): Mono<String> {
        return voteService.vote(voteDto.citizenId, voteDto.policyId, voteDto.selectionId)
    }
}