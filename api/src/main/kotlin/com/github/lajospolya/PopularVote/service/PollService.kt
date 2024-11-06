package com.github.lajospolya.PopularVote.service

import com.github.lajospolya.PopularVote.entity.PollSelectionCount
import com.github.lajospolya.PopularVote.repository.PollRepository
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