package com.github.lajospolya.popularVote.repository

import com.github.lajospolya.popularVote.entity.PolicyBookmark
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface PolicyBookmarkRepository : ReactiveCrudRepository<PolicyBookmark, String> {
    fun findByPolicyId(policyId: Long): Flux<PolicyBookmark>
    fun findByCitizenId(citizenId: Long): Flux<PolicyBookmark>
    fun findByPolicyIdAndCitizenId(
        policyId: Long,
        citizenId: Long,
    ): Mono<PolicyBookmark>
}
