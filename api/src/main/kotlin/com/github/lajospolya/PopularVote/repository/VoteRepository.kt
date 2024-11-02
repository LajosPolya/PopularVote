package com.github.lajospolya.PopularVote.repository

import com.fasterxml.jackson.annotation.Nulls
import io.r2dbc.spi.ConnectionFactory
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.RowsFetchSpec
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class VoteRepository(
    connectionFactory: ConnectionFactory,
) {

    private val databaseClient = DatabaseClient.create(connectionFactory)

    fun vote(citizenId: Long, policyId: Long, selectionId: Long): Mono<String> {

        val error: Int = 0
        val errorMessage: String = ""
        return databaseClient.sql("call cast_vote(:citizen_id, :policy_id, :selection_id, @error, @msg); select @msg;")
            .bind("citizen_id", citizenId)
            .bind("policy_id", policyId)
            .bind("selection_id", selectionId)
            .map {
                try {
                    it.get(0, String::class.java) ?: "return me"
                } catch (e: NullPointerException) {
                    // no-error
                    "success"
                }
            }.first()
            .switchIfEmpty(Mono.just("empty"))
    }
}