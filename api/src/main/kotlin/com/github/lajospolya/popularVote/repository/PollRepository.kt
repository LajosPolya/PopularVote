package com.github.lajospolya.popularVote.repository

import com.github.lajospolya.popularVote.entity.PollSelectionCount
import io.r2dbc.spi.ConnectionFactory
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux

@Component
class PollRepository(
    connectionFactory: ConnectionFactory,
) {

    private val databaseClient = DatabaseClient.create(connectionFactory)

    fun getPollForPolicy(policyId: Long): Flux<PollSelectionCount> {
        return databaseClient.sql(
            """select selection, count(*) as count
          from poll
          join poll_selection on id = selection_id
          where policy_id = :id
          group by selection_id
        """.trimMargin())
            .bind("id", policyId)
            .mapProperties(PollSelectionCount::class.java)
        .all()
    }
}