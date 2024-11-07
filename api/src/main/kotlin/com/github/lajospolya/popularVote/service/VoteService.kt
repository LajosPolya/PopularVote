package com.github.lajospolya.popularVote.service

import com.github.lajospolya.popularVote.repository.VoteRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class VoteService(
    private val voteRepo: VoteRepository,
    private val citizenService: CitizenService,
    private val policyService: PolicyService,
) {
    fun vote(
        citizenId: Long,
        policyId: Long,
        selectionId: Long,
    ): Mono<String> {
        // Call services to validate the entities exist
        return Mono.zip(citizenService.getCitizen(citizenId), policyService.getPolicy(policyId))
            .then(voteRepo.vote(citizenId, policyId, selectionId))
    }
}
