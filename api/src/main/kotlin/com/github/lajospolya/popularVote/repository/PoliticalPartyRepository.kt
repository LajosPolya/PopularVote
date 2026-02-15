package com.github.lajospolya.popularVote.repository

import com.github.lajospolya.popularVote.entity.PoliticalParty
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface PoliticalPartyRepository : ReactiveCrudRepository<PoliticalParty, Int> {
    fun findByLevelOfPoliticsId(levelOfPoliticsId: Long): Flux<PoliticalParty>
}
