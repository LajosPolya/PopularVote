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

    @Query(
        """
        SELECT * FROM policy 
        WHERE province_and_territory_id = :provinceAndTerritoryId 
        ORDER BY creation_date DESC, id DESC 
        LIMIT :pageSize OFFSET :offset
        """,
    )
    fun findAllByProvinceAndTerritoryId(
        provinceAndTerritoryId: Int,
        pageSize: Int,
        offset: Long,
    ): Flux<Policy>

    @Query(
        """
        SELECT * FROM policy 
        WHERE level_of_politics_id = :levelOfPoliticsId AND province_and_territory_id = :provinceAndTerritoryId 
        ORDER BY creation_date DESC, id DESC 
        LIMIT :pageSize OFFSET :offset
        """,
    )
    fun findAllByLevelOfPoliticsIdAndProvinceAndTerritoryId(
        levelOfPoliticsId: Int,
        provinceAndTerritoryId: Int,
        pageSize: Int,
        offset: Long,
    ): Flux<Policy>

    fun findAllByPublisherCitizenIdOrderByCreationDateDescIdDesc(publisherCitizenId: Long): Flux<Policy>

    @Query(
        """
        SELECT count(*) FROM policy 
        WHERE level_of_politics_id = :levelOfPoliticsId
        """,
    )
    fun countByLevelOfPoliticsId(levelOfPoliticsId: Int): Mono<Long>

    @Query(
        """
        SELECT count(*) FROM policy 
        WHERE province_and_territory_id = :provinceAndTerritoryId
        """,
    )
    fun countByProvinceAndTerritoryId(provinceAndTerritoryId: Int): Mono<Long>

    @Query(
        """
        SELECT count(*) FROM policy 
        WHERE level_of_politics_id = :levelOfPoliticsId AND province_and_territory_id = :provinceAndTerritoryId
        """,
    )
    fun countByLevelOfPoliticsIdAndProvinceAndTerritoryId(
        levelOfPoliticsId: Int,
        provinceAndTerritoryId: Int,
    ): Mono<Long>

    @Query(
        """
        SELECT * FROM policy 
        WHERE close_date >= :now
        ORDER BY creation_date DESC, id DESC 
        LIMIT :pageSize OFFSET :offset
        """,
    )
    fun findAllByOpen(
        now: java.time.LocalDateTime,
        pageSize: Int,
        offset: Long,
    ): Flux<Policy>

    @Query(
        """
        SELECT * FROM policy 
        WHERE close_date < :now
        ORDER BY creation_date DESC, id DESC 
        LIMIT :pageSize OFFSET :offset
        """,
    )
    fun findAllByClosed(
        now: java.time.LocalDateTime,
        pageSize: Int,
        offset: Long,
    ): Flux<Policy>

    @Query(
        """
        SELECT * FROM policy 
        WHERE level_of_politics_id = :levelOfPoliticsId AND close_date >= :now
        ORDER BY creation_date DESC, id DESC 
        LIMIT :pageSize OFFSET :offset
        """,
    )
    fun findAllByLevelOfPoliticsIdAndOpen(
        levelOfPoliticsId: Int,
        now: java.time.LocalDateTime,
        pageSize: Int,
        offset: Long,
    ): Flux<Policy>

    @Query(
        """
        SELECT * FROM policy 
        WHERE level_of_politics_id = :levelOfPoliticsId AND close_date < :now
        ORDER BY creation_date DESC, id DESC 
        LIMIT :pageSize OFFSET :offset
        """,
    )
    fun findAllByLevelOfPoliticsIdAndClosed(
        levelOfPoliticsId: Int,
        now: java.time.LocalDateTime,
        pageSize: Int,
        offset: Long,
    ): Flux<Policy>

    @Query(
        """
        SELECT * FROM policy 
        WHERE province_and_territory_id = :provinceAndTerritoryId AND close_date >= :now
        ORDER BY creation_date DESC, id DESC 
        LIMIT :pageSize OFFSET :offset
        """,
    )
    fun findAllByProvinceAndTerritoryIdAndOpen(
        provinceAndTerritoryId: Int,
        now: java.time.LocalDateTime,
        pageSize: Int,
        offset: Long,
    ): Flux<Policy>

    @Query(
        """
        SELECT * FROM policy 
        WHERE province_and_territory_id = :provinceAndTerritoryId AND close_date < :now
        ORDER BY creation_date DESC, id DESC 
        LIMIT :pageSize OFFSET :offset
        """,
    )
    fun findAllByProvinceAndTerritoryIdAndClosed(
        provinceAndTerritoryId: Int,
        now: java.time.LocalDateTime,
        pageSize: Int,
        offset: Long,
    ): Flux<Policy>

    @Query(
        """
        SELECT * FROM policy 
        WHERE level_of_politics_id = :levelOfPoliticsId AND province_and_territory_id = :provinceAndTerritoryId AND close_date >= :now
        ORDER BY creation_date DESC, id DESC 
        LIMIT :pageSize OFFSET :offset
        """,
    )
    fun findAllByLevelOfPoliticsIdAndProvinceAndTerritoryIdAndOpen(
        levelOfPoliticsId: Int,
        provinceAndTerritoryId: Int,
        now: java.time.LocalDateTime,
        pageSize: Int,
        offset: Long,
    ): Flux<Policy>

    @Query(
        """
        SELECT * FROM policy 
        WHERE level_of_politics_id = :levelOfPoliticsId AND province_and_territory_id = :provinceAndTerritoryId AND close_date < :now
        ORDER BY creation_date DESC, id DESC 
        LIMIT :pageSize OFFSET :offset
        """,
    )
    fun findAllByLevelOfPoliticsIdAndProvinceAndTerritoryIdAndClosed(
        levelOfPoliticsId: Int,
        provinceAndTerritoryId: Int,
        now: java.time.LocalDateTime,
        pageSize: Int,
        offset: Long,
    ): Flux<Policy>

    @Query(
        """
        SELECT count(*) FROM policy 
        WHERE close_date >= :now
        """,
    )
    fun countByOpen(now: java.time.LocalDateTime): Mono<Long>

    @Query(
        """
        SELECT count(*) FROM policy 
        WHERE close_date < :now
        """,
    )
    fun countByClosed(now: java.time.LocalDateTime): Mono<Long>

    @Query(
        """
        SELECT count(*) FROM policy 
        WHERE level_of_politics_id = :levelOfPoliticsId AND close_date >= :now
        """,
    )
    fun countByLevelOfPoliticsIdAndOpen(
        levelOfPoliticsId: Int,
        now: java.time.LocalDateTime,
    ): Mono<Long>

    @Query(
        """
        SELECT count(*) FROM policy 
        WHERE level_of_politics_id = :levelOfPoliticsId AND close_date < :now
        """,
    )
    fun countByLevelOfPoliticsIdAndClosed(
        levelOfPoliticsId: Int,
        now: java.time.LocalDateTime,
    ): Mono<Long>

    @Query(
        """
        SELECT count(*) FROM policy 
        WHERE province_and_territory_id = :provinceAndTerritoryId AND close_date >= :now
        """,
    )
    fun countByProvinceAndTerritoryIdAndOpen(
        provinceAndTerritoryId: Int,
        now: java.time.LocalDateTime,
    ): Mono<Long>

    @Query(
        """
        SELECT count(*) FROM policy 
        WHERE province_and_territory_id = :provinceAndTerritoryId AND close_date < :now
        """,
    )
    fun countByProvinceAndTerritoryIdAndClosed(
        provinceAndTerritoryId: Int,
        now: java.time.LocalDateTime,
    ): Mono<Long>

    @Query(
        """
        SELECT count(*) FROM policy 
        WHERE level_of_politics_id = :levelOfPoliticsId AND province_and_territory_id = :provinceAndTerritoryId AND close_date >= :now
        """,
    )
    fun countByLevelOfPoliticsIdAndProvinceAndTerritoryIdAndOpen(
        levelOfPoliticsId: Int,
        provinceAndTerritoryId: Int,
        now: java.time.LocalDateTime,
    ): Mono<Long>

    @Query(
        """
        SELECT count(*) FROM policy 
        WHERE level_of_politics_id = :levelOfPoliticsId AND province_and_territory_id = :provinceAndTerritoryId AND close_date < :now
        """,
    )
    fun countByLevelOfPoliticsIdAndProvinceAndTerritoryIdAndClosed(
        levelOfPoliticsId: Int,
        provinceAndTerritoryId: Int,
        now: java.time.LocalDateTime,
    ): Mono<Long>
}
