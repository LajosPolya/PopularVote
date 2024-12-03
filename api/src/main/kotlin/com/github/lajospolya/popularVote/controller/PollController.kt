package com.github.lajospolya.popularVote.controller

import com.github.lajospolya.popularVote.entity.PollSelectionCount
import com.github.lajospolya.popularVote.service.PollService
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
class PollController(
    private val pollService: PollService,
) {
    @RequestMapping("poll/{policyId}", method = [RequestMethod.GET])
    fun getPolicies(
        @PathVariable policyId: Long,
    ): Flux<PollSelectionCount> {
        return pollService.getPollSelectionForPolicy(policyId)
    }
}
