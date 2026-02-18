package com.github.lajospolya.popularVote.repository

import com.github.lajospolya.popularVote.entity.Policy
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface PolicyRepository : ReactiveCrudRepository<Policy, Long> {
    fun countByPublisherCitizenId(publisherCitizenId: Long): Mono<Long>

    @Query(
        "SELECT * FROM policy WHERE publisher_citizen_id IN (SELECT citizen_id FROM citizen_political_details WHERE political_party_id = :id) ORDER BY creation_date DESC, id DESC",
    )
    fun findAllByPublisherPoliticalPartyId(id: Int): Flux<Policy>

    @Query(
        """
        SELECT * FROM policy 
        ORDER BY creation_date DESC, id DESC 
        LIMIT :pageSize OFFSET :offset
        """,
    )
    fun findAllBy(
        pageSize: Int,
        offset: Long,
    ): Flux<Policy>

    @Query(
        """
        SELECT * FROM policy 
        WHERE level_of_politics_id = :levelOfPoliticsId 
        ORDER BY creation_date DESC, id DESC 
        LIMIT :pageSize OFFSET :offset
        """,
    )
    fun findAllByLevelOfPoliticsId(
        levelOfPoliticsId: Int,
        pageSize: Int,
        offset: Long,
    ): Flux<Policy>

    fun findAllByPublisherCitizenIdOrderByCreationDateDescIdDesc(publisherCitizenId: Long): Flux<Policy>

    fun countByLevelOfPoliticsId(levelOfPoliticsId: Int): Mono<Long>
}
