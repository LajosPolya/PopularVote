package com.github.lajospolya.popularVote.service

import com.github.lajospolya.popularVote.repository.VoteRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class VoteService(
    private val voteRepo: VoteRepository,
) {

    fun vote(citizenId: Long, policyId: Long, selectionId: Long): Mono<String> {
        return voteRepo.vote(citizenId, policyId, selectionId)
    }
}