package com.github.lajospolya.PopularVote.repository

import com.github.lajospolya.PopularVote.entity.Poll
import com.github.lajospolya.PopularVote.entity.PollSelectionCount
import io.r2dbc.spi.ConnectionFactory
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.CriteriaDefinition
import org.springframework.data.relational.core.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.RowsFetchSpec
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux

@Component
class PollRepository(
    connectionFactory: ConnectionFactory,
) {

    private val databaseClient = DatabaseClient.create(connectionFactory)

    fun getPollForPolicy(policyId: Long): Flux<PollSelectionCount> {
        return databaseClient.sql(
            """select selection_id, count(*) as count
          from poll
          where policy_id = :id
          group by selection_id
        """.trimMargin())
            .bind("id", policyId)
            .mapProperties(PollSelectionCount::class.java)
        .all()
    }
}