package com.github.lajospolya.popularVote.service

import com.github.lajospolya.popularVote.controller.exception.VotingClosedException
import com.github.lajospolya.popularVote.repository.VoteRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Service
class VoteService(
    private val voteRepo: VoteRepository,
    private val citizenService: CitizenService,
    private val policyService: PolicyService,
    private val selectionService: SelectionService,
) {
    fun vote(
        authId: String,
        policyId: Long,
        selectionId: Long,
    ): Mono<Boolean> {
        // Call services to validate the entities exist
        return citizenService
            .getCitizenByAuthId(authId)
            .flatMap { citizen ->
                Mono.zip(
                    Mono.just(citizen),
                    policyService.getPolicy(policyId).flatMap {
                        if (LocalDateTime.now().isAfter(it.closeDate)) {
                            Mono.error(
                                VotingClosedException(),
                            )
                        } else {
                            Mono.just(it)
                        }
                    },
                    selectionService.getSelection(selectionId),
                )
            }.flatMap { tuple ->
                val citizen = tuple.t1
                voteRepo.vote(citizen.id!!, policyId, selectionId)
            }
    }

    fun hasVoted(
        authId: String,
        policyId: Long,
    ): Mono<Boolean> =
        citizenService
            .getCitizenByAuthId(authId)
            .flatMap { citizen ->
                voteRepo.hasVoted(citizen.id!!, policyId)
            }
}
