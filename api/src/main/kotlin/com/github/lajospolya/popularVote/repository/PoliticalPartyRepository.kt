package com.github.lajospolya.popularVote.repository

import com.github.lajospolya.popularVote.entity.PoliticalParty
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface PoliticalPartyRepository : ReactiveCrudRepository<PoliticalParty, Int> {
    @org.springframework.data.r2dbc.repository.Query(
        "SELECT * FROM political_party LIMIT :limit OFFSET :offset",
    )
    fun findAllBy(
        limit: Int,
        offset: Long,
    ): Flux<PoliticalParty>

    @org.springframework.data.r2dbc.repository.Query(
        "SELECT * FROM political_party WHERE level_of_politics_id = :levelOfPoliticsId LIMIT :limit OFFSET :offset",
    )
    fun findAllByLevelOfPoliticsId(
        levelOfPoliticsId: Long,
        limit: Int,
        offset: Long,
    ): Flux<PoliticalParty>

    @org.springframework.data.r2dbc.repository.Query(
        "SELECT * FROM political_party WHERE province_and_territory_id = :provinceAndTerritoryId LIMIT :limit OFFSET :offset",
    )
    fun findAllByProvinceAndTerritoryId(
        provinceAndTerritoryId: Int,
        limit: Int,
        offset: Long,
    ): Flux<PoliticalParty>

    @org.springframework.data.r2dbc.repository.Query(
        "SELECT * FROM political_party WHERE level_of_politics_id = :levelOfPoliticsId AND province_and_territory_id = :provinceAndTerritoryId LIMIT :limit OFFSET :offset",
    )
    fun findAllByLevelOfPoliticsIdAndProvinceAndTerritoryId(
        levelOfPoliticsId: Long,
        provinceAndTerritoryId: Int,
        limit: Int,
        offset: Long,
    ): Flux<PoliticalParty>

    @org.springframework.data.r2dbc.repository.Query(
        "SELECT count(*) FROM political_party WHERE level_of_politics_id = :levelOfPoliticsId",
    )
    fun countByLevelOfPoliticsId(levelOfPoliticsId: Long): Mono<Long>

    @org.springframework.data.r2dbc.repository.Query(
        "SELECT count(*) FROM political_party WHERE province_and_territory_id = :provinceAndTerritoryId",
    )
    fun countByProvinceAndTerritoryId(provinceAndTerritoryId: Int): Mono<Long>

    @org.springframework.data.r2dbc.repository.Query(
        "SELECT count(*) FROM political_party WHERE level_of_politics_id = :levelOfPoliticsId AND province_and_territory_id = :provinceAndTerritoryId",
    )
    fun countByLevelOfPoliticsIdAndProvinceAndTerritoryId(
        levelOfPoliticsId: Long,
        provinceAndTerritoryId: Int,
    ): Mono<Long>
}
