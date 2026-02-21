package com.github.lajospolya.popularVote.repository

import com.github.lajospolya.popularVote.entity.PoliticalParty
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface PoliticalPartyRepository : ReactiveCrudRepository<PoliticalParty, Int> {
    fun findByLevelOfPoliticsId(levelOfPoliticsId: Long): Flux<PoliticalParty>

    @org.springframework.data.r2dbc.repository.Query(
        "SELECT * FROM political_party WHERE province_and_territory_id = :provinceAndTerritoryId",
    )
    fun findByProvinceAndTerritoryId(provinceAndTerritoryId: Int): Flux<PoliticalParty>

    @org.springframework.data.r2dbc.repository.Query(
        "SELECT * FROM political_party WHERE level_of_politics_id = :levelOfPoliticsId AND province_and_territory_id = :provinceAndTerritoryId",
    )
    fun findByLevelOfPoliticsIdAndProvinceAndTerritoryId(
        levelOfPoliticsId: Long,
        provinceAndTerritoryId: Int,
    ): Flux<PoliticalParty>
}
