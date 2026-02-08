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

    fun hasVoted(
        citizenId: Long,
        policyId: Long,
    ): Mono<Boolean> =
        databaseClient
            .sql("SELECT COUNT(*) FROM vote WHERE citizen_id = :citizen_id AND policy_id = :policy_id")
            .bind("citizen_id", citizenId)
            .bind("policy_id", policyId)
            .map { row -> row.get(0, Long::class.java)!! > 0 }
            .one()
            .switchIfEmpty(Mono.just(false))

    fun countByCitizenId(citizenId: Long): Mono<Long> =
        databaseClient
            .sql("SELECT COUNT(*) FROM vote WHERE citizen_id = :citizen_id")
            .bind("citizen_id", citizenId)
            .map { row -> row.get(0, Long::class.java)!! }
            .one()
            .switchIfEmpty(Mono.just(0L))
}
