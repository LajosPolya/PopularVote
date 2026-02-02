package com.github.lajospolya.popularVote.repository

import com.github.lajospolya.popularVote.entity.Opinion
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux

interface OpinionRepository : ReactiveCrudRepository<Opinion, Long> {
    fun findByPolicyId(policyId: Long): Flux<Opinion>
}
