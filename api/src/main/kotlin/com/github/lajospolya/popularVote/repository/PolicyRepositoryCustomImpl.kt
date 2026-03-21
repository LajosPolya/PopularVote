package com.github.lajospolya.popularVote.repository

import com.github.lajospolya.popularVote.dto.PolicySummaryDto
import com.github.lajospolya.popularVote.entity.ApprovalStatus
import com.github.lajospolya.popularVote.entity.Policy
import com.github.lajospolya.popularVote.entity.VotingStatus
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

class PolicyRepositoryCustomImpl(
    private val template: R2dbcEntityTemplate,
) : PolicyRepositoryCustom {
    override fun findAllBy(
        levelOfPoliticsId: Int?,
        provinceAndTerritoryId: Int?,
        status: VotingStatus?,
        approvalStatus: ApprovalStatus?,
        publisherPoliticalPartyId: Int?,
        now: LocalDateTime,
        pageSize: Int,
        offset: Long,
    ): Flux<Policy> {
        val (sql, binds) =
            buildSqlAndBinds(
                levelOfPoliticsId,
                provinceAndTerritoryId,
                status,
                approvalStatus,
                publisherPoliticalPartyId,
                now,
                false,
            )
        var client = template.databaseClient.sql(sql)
        binds.forEach { (name, value) -> client = client.bind(name, value) }
        client = client.bind("limit", pageSize).bind("offset", offset)

        return client
            .map { row ->
                Policy(
                    id = row.get("id", java.lang.Long::class.java)?.toLong(),
                    description = row.get("description", String::class.java)!!,
                    publisherCitizenId = row.get("publisher_citizen_id", java.lang.Long::class.java)!!.toLong(),
                    levelOfPoliticsId = row.get("level_of_politics_id", java.lang.Integer::class.java)!!.toInt(),
                    provinceAndTerritoryId = row.get("province_and_territory_id", java.lang.Integer::class.java)?.toInt(),
                    closeDate = row.get("close_date", LocalDateTime::class.java)!!,
                    creationDate = row.get("creation_date", LocalDateTime::class.java)!!,
                )
            }.all()
    }

    override fun countBy(
        levelOfPoliticsId: Int?,
        provinceAndTerritoryId: Int?,
        status: VotingStatus?,
        approvalStatus: ApprovalStatus?,
        publisherPoliticalPartyId: Int?,
        now: LocalDateTime,
    ): Mono<Long> {
        val (sql, binds) =
            buildSqlAndBinds(
                levelOfPoliticsId,
                provinceAndTerritoryId,
                status,
                approvalStatus,
                publisherPoliticalPartyId,
                now,
                true,
            )
        var client = template.databaseClient.sql(sql)
        binds.forEach { (name, value) -> client = client.bind(name, value) }

        return client.map { row -> row.get(0, java.lang.Long::class.java)!!.toLong() }.one()
    }

    override fun findBookmarkedSummariesByCitizenId(citizenId: Long): Flux<PolicySummaryDto> {
        val sql =
            """
            SELECT 
                p.id,
                p.description,
                p.publisher_citizen_id,
                p.level_of_politics_id,
                c.full_name, 
                p.close_date,
                p.creation_date,
                cpd.political_party_id,
                p.province_and_territory_id,
                (SELECT COUNT(*) FROM poll p2 JOIN poll_selection ps ON p2.selection_id = ps.id WHERE p2.policy_id = p.id AND ps.selection = 'approve') as approved_votes,
                (SELECT COUNT(*) FROM poll p3 JOIN poll_selection ps2 ON p3.selection_id = ps2.id WHERE p3.policy_id = p.id AND ps2.selection = 'disapprove') as denied_votes
            FROM policy p
            JOIN citizen c ON p.publisher_citizen_id = c.id
            LEFT JOIN citizen_political_details cpd ON p.publisher_citizen_id = cpd.citizen_id
            JOIN policy_bookmark pb ON p.id = pb.policy_id
            WHERE pb.citizen_id = :citizenId
            ORDER BY p.id DESC
            """.trimIndent()

        return template.databaseClient
            .sql(sql)
            .bind("citizenId", citizenId)
            .map { row ->
                PolicySummaryDto(
                    id = row.get("id", java.lang.Long::class.java)!!.toLong(),
                    description = row.get("description", String::class.java)!!,
                    publisherCitizenId = row.get("publisher_citizen_id", java.lang.Long::class.java)!!.toLong(),
                    levelOfPoliticsId = row.get("level_of_politics_id", java.lang.Integer::class.java)!!.toInt(),
                    publisherName = row.get("full_name", String::class.java)!!,
                    isBookmarked = true,
                    closeDate = row.get("close_date", LocalDateTime::class.java)!!,
                    creationDate = row.get("creation_date", LocalDateTime::class.java)!!,
                    publisherPoliticalPartyId = row.get("political_party_id", java.lang.Integer::class.java)?.toInt(),
                    provinceAndTerritoryId = row.get("province_and_territory_id", java.lang.Integer::class.java)?.toInt(),
                    approvalStatus =
                        if ((row.get("approved_votes", java.lang.Integer::class.java)?.toLong() ?: 0L) >
                            (row.get("denied_votes", java.lang.Integer::class.java)?.toLong() ?: 0L)
                        ) {
                            ApprovalStatus.APPROVED
                        } else {
                            ApprovalStatus.DENIED
                        },
                )
            }.all()
    }

    private fun buildSqlAndBinds(
        levelOfPoliticsId: Int?,
        provinceAndTerritoryId: Int?,
        status: VotingStatus?,
        approvalStatus: ApprovalStatus?,
        publisherPoliticalPartyId: Int?,
        now: LocalDateTime,
        isCount: Boolean,
    ): Pair<String, Map<String, Any>> {
        val binds = mutableMapOf<String, Any>()

        var sql = if (isCount) "SELECT COUNT(*) FROM (" else ""
        sql += "SELECT p.* FROM policy p WHERE 1=1"

        if (levelOfPoliticsId != null) {
            sql += " AND p.level_of_politics_id = :levelOfPoliticsId"
            binds["levelOfPoliticsId"] = levelOfPoliticsId
        }
        if (provinceAndTerritoryId != null) {
            sql += " AND p.province_and_territory_id = :provinceAndTerritoryId"
            binds["provinceAndTerritoryId"] = provinceAndTerritoryId
        }
        if (publisherPoliticalPartyId != null) {
            sql +=
                " AND p.publisher_citizen_id IN (SELECT citizen_id FROM citizen_political_details WHERE political_party_id = :publisherPoliticalPartyId)"
            binds["publisherPoliticalPartyId"] = publisherPoliticalPartyId
        }
        if (status == VotingStatus.OPEN) {
            sql += " AND p.close_date >= :now"
            binds["now"] = now
        } else if (status == VotingStatus.CLOSED) {
            sql += " AND p.close_date < :now"
            binds["now"] = now
        }

        if (approvalStatus != null) {
            val filter = if (approvalStatus == ApprovalStatus.APPROVED) ">" else "<="
            sql +=
                " AND (SELECT COUNT(*) FROM poll p2 JOIN poll_selection ps ON p2.selection_id = ps.id WHERE p2.policy_id = p.id AND ps.selection = 'approve') $filter (SELECT COUNT(*) FROM poll p3 JOIN poll_selection ps2 ON p3.selection_id = ps2.id WHERE p3.policy_id = p.id AND ps2.selection = 'disapprove')"
        }

        if (isCount) {
            sql += " ) as t"
        } else {
            sql += " ORDER BY p.creation_date DESC, p.id DESC LIMIT :limit OFFSET :offset"
        }

        return sql to binds
    }
}
