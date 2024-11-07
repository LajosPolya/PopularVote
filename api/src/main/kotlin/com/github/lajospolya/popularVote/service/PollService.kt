package com.github.lajospolya.popularVote.service

import com.github.lajospolya.popularVote.entity.PollSelectionCount
import com.github.lajospolya.popularVote.repository.PollRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class PollService(
    private val pollRepository: PollRepository
) {

    fun getPollSelectionForPolicy(policyId: Long): Flux<PollSelectionCount> {
        return pollRepository.getPollForPolicy(policyId)
    }
}