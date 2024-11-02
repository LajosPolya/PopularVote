package com.github.lajospolya.PopularVote.service

import com.github.lajospolya.PopularVote.repository.VoteRepository
import org.springframework.r2dbc.core.RowsFetchSpec
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