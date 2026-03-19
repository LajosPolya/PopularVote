package com.github.lajospolya.popularVote.repository

import com.github.lajospolya.popularVote.entity.Policy
import org.springframework.data.domain.Sort
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

class PolicyRepositoryCustomImpl(
    private val template: R2dbcEntityTemplate,
) : PolicyRepositoryCustom {
    override fun findAllBy(
        levelOfPoliticsId: Int?,
        provinceAndTerritoryId: Int?,
        status: String?,
        now: LocalDateTime,
        pageSize: Int,
        offset: Long,
    ): Flux<Policy> {
        val query =
            Query
                .query(buildCriteria(levelOfPoliticsId, provinceAndTerritoryId, status, now))
                .sort(Sort.by(Sort.Order.desc("creationDate"), Sort.Order.desc("id")))
                .limit(pageSize)
                .offset(offset)

        return template
            .select(Policy::class.java)
            .from("policy")
            .matching(query)
            .all()
    }

    override fun countBy(
        levelOfPoliticsId: Int?,
        provinceAndTerritoryId: Int?,
        status: String?,
        now: LocalDateTime,
    ): Mono<Long> {
        val query = Query.query(buildCriteria(levelOfPoliticsId, provinceAndTerritoryId, status, now))

        return template.count(query, Policy::class.java)
    }

    private fun buildCriteria(
        levelOfPoliticsId: Int?,
        provinceAndTerritoryId: Int?,
        status: String?,
        now: LocalDateTime,
    ): Criteria {
        var criteria = Criteria.empty()
        if (levelOfPoliticsId != null) {
            criteria = criteria.and("level_of_politics_id").`is`(levelOfPoliticsId)
        }
        if (provinceAndTerritoryId != null) {
            criteria = criteria.and("province_and_territory_id").`is`(provinceAndTerritoryId)
        }
        if (status == "open") {
            criteria = criteria.and("close_date").greaterThanOrEquals(now)
        } else if (status == "closed") {
            criteria = criteria.and("close_date").lessThan(now)
        }
        return criteria
    }
}
