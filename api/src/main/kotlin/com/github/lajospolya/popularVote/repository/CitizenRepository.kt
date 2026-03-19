package com.github.lajospolya.popularVote.repository

import com.github.lajospolya.popularVote.entity.Citizen
import com.github.lajospolya.popularVote.entity.Role
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface CitizenRepository : ReactiveCrudRepository<Citizen, Long> {
    fun findByGivenNameAndSurname(
        givenName: String,
        surname: String,
    ): Mono<Citizen>

    fun findByAuthId(authId: String): Mono<Citizen>

    @Query(
        """
        SELECT * FROM citizen
        WHERE role = :role
        ORDER BY id DESC
        LIMIT :limit OFFSET :offset
        """,
    )
    fun findAllByRole(
        role: Role,
        limit: Int,
        offset: Long,
    ): Flux<Citizen>

    @Query(
        """
        SELECT count(*) FROM citizen
        WHERE role = :role
        """,
    )
    fun countByRole(role: Role): Mono<Long>

    @Query(
        """
        SELECT c.* FROM citizen c
        JOIN citizen_political_details cpd ON c.id = cpd.citizen_id
        WHERE cpd.political_party_id = :politicalPartyId AND c.role = :role
        """,
    )
    fun findAllByPoliticalPartyIdAndRole(
        politicalPartyId: Int,
        role: Role,
    ): Flux<Citizen>

    @Query("SELECT c.* FROM citizen c JOIN politician_verification pv ON c.id = pv.citizen_id")
    fun findAllPendingVerification(): Flux<Citizen>

    @Query(
        """
        SELECT c.* FROM citizen c
        JOIN citizen_political_details cpd ON c.id = cpd.citizen_id
        WHERE c.role = :role AND cpd.level_of_politics_id = :levelOfPoliticsId
        ORDER BY c.id DESC
        LIMIT :limit OFFSET :offset
        """,
    )
    fun findAllByRoleAndLevelOfPoliticsId(
        role: Role,
        levelOfPoliticsId: Long,
        limit: Int,
        offset: Long,
    ): Flux<Citizen>

    @Query(
        """
        SELECT count(*) FROM citizen c
        JOIN citizen_political_details cpd ON c.id = cpd.citizen_id
        WHERE c.role = :role AND cpd.level_of_politics_id = :levelOfPoliticsId
        """,
    )
    fun countByRoleAndLevelOfPoliticsId(
        role: Role,
        levelOfPoliticsId: Long,
    ): Mono<Long>

    @Query(
        """
        SELECT c.* FROM citizen c
        JOIN citizen_political_details cpd ON c.id = cpd.citizen_id
        JOIN electoral_district ed ON cpd.electoral_district_id = ed.id
        WHERE c.role = :role AND ed.province_territory_id = :provinceAndTerritoryId
        ORDER BY c.id DESC
        LIMIT :limit OFFSET :offset
        """,
    )
    fun findAllByRoleAndProvinceAndTerritoryId(
        role: Role,
        provinceAndTerritoryId: Long,
        limit: Int,
        offset: Long,
    ): Flux<Citizen>

    @Query(
        """
        SELECT c.* FROM citizen c
        JOIN citizen_political_details cpd ON c.id = cpd.citizen_id
        JOIN electoral_district ed ON cpd.electoral_district_id = ed.id
        WHERE c.role = :role AND cpd.level_of_politics_id = :levelOfPoliticsId AND ed.province_territory_id = :provinceAndTerritoryId
        ORDER BY c.id DESC
        LIMIT :limit OFFSET :offset
        """,
    )
    fun findAllByRoleAndLevelOfPoliticsIdAndProvinceAndTerritoryId(
        role: Role,
        levelOfPoliticsId: Long,
        provinceAndTerritoryId: Long,
        limit: Int,
        offset: Long,
    ): Flux<Citizen>

    @Query(
        """
        SELECT count(*) FROM citizen c
        JOIN citizen_political_details cpd ON c.id = cpd.citizen_id
        JOIN electoral_district ed ON cpd.electoral_district_id = ed.id
        WHERE c.role = :role AND ed.province_territory_id = :provinceAndTerritoryId
        """,
    )
    fun countByRoleAndProvinceAndTerritoryId(
        role: Role,
        provinceAndTerritoryId: Long,
    ): Mono<Long>

    @Query(
        """
        SELECT count(*) FROM citizen c
        JOIN citizen_political_details cpd ON c.id = cpd.citizen_id
        JOIN electoral_district ed ON cpd.electoral_district_id = ed.id
        WHERE c.role = :role AND cpd.level_of_politics_id = :levelOfPoliticsId AND ed.province_territory_id = :provinceAndTerritoryId
        """,
    )
    fun countByRoleAndLevelOfPoliticsIdAndProvinceAndTerritoryId(
        role: Role,
        levelOfPoliticsId: Long,
        provinceAndTerritoryId: Long,
    ): Mono<Long>
}
