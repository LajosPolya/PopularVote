package com.github.lajospolya.popularVote.repository

import com.github.lajospolya.popularVote.entity.Policy
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface PolicyRepository : ReactiveCrudRepository<Policy, Long> {
    fun countByPublisherCitizenId(publisherCitizenId: Long): Mono<Long>
}
