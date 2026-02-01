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

    fun getPollForPolicy(policyId: Long): Flux<PollSelectionCount> =
        databaseClient
            .sql(
                """select ps.selection, count(p.selection_id) as count
          from poll_selection ps
          left join poll p on ps.id = p.selection_id and p.policy_id = :id
          group by ps.id
                """.trimMargin(),
            ).bind("id", policyId)
            .mapProperties(PollSelectionCount::class.java)
            .all()
}
