package com.github.lajospolya.popularVote.repository

import io.r2dbc.spi.ConnectionFactory
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class VoteRepository(
    connectionFactory: ConnectionFactory,
) {
    private val databaseClient = DatabaseClient.create(connectionFactory)

    fun vote(
        citizenId: Long,
        policyId: Long,
        selectionId: Long,
    ): Mono<Boolean> =
        databaseClient
            .sql("call cast_vote(:citizen_id, :policy_id, :selection_id, @error, @msg); select @msg;")
            .bind("citizen_id", citizenId)
            .bind("policy_id", policyId)
            .bind("selection_id", selectionId)
            .map {
                // can't return null here
                it.get(0, String::class.java) ?: ""
            }.one()
            .flatMap { it ->
                if (it.isEmpty()) {
                    Mono.just(true)
                } else {
                    Mono.error(RuntimeException(it))
                }
            }
}
