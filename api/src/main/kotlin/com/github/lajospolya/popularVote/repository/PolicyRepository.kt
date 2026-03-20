package com.github.lajospolya.popularVote.repository

import com.github.lajospolya.popularVote.dto.PolicySummaryDto
import com.github.lajospolya.popularVote.entity.Policy
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface PolicyRepository :
    ReactiveCrudRepository<Policy, Long>,
    PolicyRepositoryCustom {
    fun countByPublisherCitizenId(publisherCitizenId: Long): Mono<Long>

    @Query(
        "SELECT * FROM policy WHERE publisher_citizen_id IN (SELECT citizen_id FROM citizen_political_details WHERE political_party_id = :id) ORDER BY creation_date DESC, id DESC",
    )
    fun findAllByPublisherPoliticalPartyId(id: Int): Flux<Policy>

    fun findAllByPublisherCitizenIdOrderByCreationDateDescIdDesc(publisherCitizenId: Long): Flux<Policy>

    @Query(
        """
        SELECT 
            p.*, 
            c.full_name as publisher_name, 
            cpd.political_party_id as publisher_political_party_id,
            1 as is_bookmarked
        FROM policy p
        JOIN citizen c ON p.publisher_citizen_id = c.id
        LEFT JOIN citizen_political_details cpd ON p.publisher_citizen_id = cpd.citizen_id
        JOIN policy_bookmark pb ON p.id = pb.policy_id
        WHERE pb.citizen_id = :citizenId
        ORDER BY p.id DESC
        """,
    )
    fun findBookmarkedSummariesByCitizenId(citizenId: Long): Flux<PolicySummaryDto>
}
