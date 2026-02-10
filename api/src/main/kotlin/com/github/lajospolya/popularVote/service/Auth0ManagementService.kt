package com.github.lajospolya.popularVote.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Service
class Auth0ManagementService(
    private val auth0WebClient: WebClient,
) {
    @Value("\${auth0.management.audience}")
    private lateinit var managementAudience: String

    fun addRoleToUser(
        userId: String,
        roleId: String,
    ): Mono<Void> {
        val body = mapOf("roles" to listOf(roleId))

        return auth0WebClient
            .post()
            .uri("${managementAudience}users/$userId/roles")
            .attributes(clientRegistrationId("internal-client"))
            .attribute("auth0_management_audience", managementAudience)
            .bodyValue(body)
            .retrieve()
            .bodyToMono(Void::class.java)
    }

    fun removeRoleFromUser(
        userId: String,
        roleId: String,
    ): Mono<Void> {
        val body = mapOf("roles" to listOf(roleId))

        return auth0WebClient
            .method(org.springframework.http.HttpMethod.DELETE)
            .uri("${managementAudience}users/$userId/roles")
            .attributes(clientRegistrationId("internal-client"))
            .attribute("auth0_management_audience", managementAudience)
            .bodyValue(body)
            .retrieve()
            .bodyToMono(Void::class.java)
    }
}
