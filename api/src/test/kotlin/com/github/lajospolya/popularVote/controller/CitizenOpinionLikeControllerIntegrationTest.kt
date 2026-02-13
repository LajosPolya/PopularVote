package com.github.lajospolya.popularVote.controller

import com.github.lajospolya.popularVote.AbstractIntegrationTest
import com.github.lajospolya.popularVote.dto.*
import com.github.lajospolya.popularVote.entity.PoliticalAffiliation
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody

@AutoConfigureWebTestClient
class CitizenOpinionLikeControllerIntegrationTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Test
    fun `like, fetch liked, and unlike opinion`() {
        val authId = "auth-like-test"
        val citizenId = createCitizen(authId)
        val policyId = createPolicy()
        val opinionId = createOpinion(policyId)

        // 1. Like Opinion
        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(authId) }
                    .authorities(
                        SimpleGrantedAuthority("SCOPE_read:self"),
                        SimpleGrantedAuthority("SCOPE_read:opinions")
                    )
            )
            .post()
            .uri("/opinions/$opinionId/like")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.citizenId").isEqualTo(citizenId)
            .jsonPath("$.opinionId").isEqualTo(opinionId)

        // 2. Fetch Liked Opinions
        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(authId) }
                    .authorities(
                        SimpleGrantedAuthority("SCOPE_read:self"),
                        SimpleGrantedAuthority("SCOPE_read:opinions")
                    )
            )
            .get()
            .uri("/citizens/self/liked-opinions")
            .exchange()
            .expectStatus().isOk
            .expectBody<List<Long>>()
            .consumeWith { result ->
                val likedIds = result.responseBody
                assert(likedIds != null && likedIds.contains(opinionId))
            }

        // 3. Unlike Opinion
        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(authId) }
                    .authorities(
                        SimpleGrantedAuthority("SCOPE_read:self"),
                        SimpleGrantedAuthority("SCOPE_read:opinions")
                    )
            )
            .delete()
            .uri("/opinions/$opinionId/like")
            .exchange()
            .expectStatus().isOk

        // 4. Verify Unliked
        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(authId) }
                    .authorities(
                        SimpleGrantedAuthority("SCOPE_read:self"),
                        SimpleGrantedAuthority("SCOPE_read:opinions")
                    )
            )
            .get()
            .uri("/citizens/self/liked-opinions")
            .exchange()
            .expectStatus().isOk
            .expectBody<List<Long>>()
            .consumeWith { result ->
                val likedIds = result.responseBody
                assert(likedIds != null && !likedIds.contains(opinionId))
            }
    }

    private fun createCitizen(authId: String): Long {
        val createCitizenDto = CreateCitizenDto(
            givenName = "Like",
            surname = "Tester",
            middleName = null,
            politicalAffiliation = PoliticalAffiliation.INDEPENDENT,
        )

        return webTestClient
            .mutateWith(mockJwt().jwt { it.subject(authId) })
            .post()
            .uri("/citizens/self")
            .bodyValue(createCitizenDto)
            .exchange()
            .expectStatus().isOk
            .expectBody<CitizenDto>()
            .returnResult()
            .responseBody!!.id
    }

    private fun createPolicy(): Long {
        val authId = "auth-policy-author"
        createCitizen(authId)
        val createPolicyDto = CreatePolicyDto(
            description = "Policy for like test",
            coAuthorCitizenIds = emptyList()
        )
        return webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(authId) }
                    .authorities(SimpleGrantedAuthority("SCOPE_write:policies"))
            )
            .post()
            .uri("/policies")
            .bodyValue(createPolicyDto)
            .exchange()
            .expectStatus().isOk
            .expectBody<PolicyDto>()
            .returnResult()
            .responseBody!!.id
    }

    private fun createOpinion(policyId: Long): Long {
        val authId = "auth-opinion-author"
        createCitizen(authId)
        val createOpinionDto = CreateOpinionDto(description = "Opinion for like test", policyId = policyId)
        return webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(authId) }
                    .authorities(SimpleGrantedAuthority("SCOPE_write:opinions"))
            )
            .post()
            .uri("/opinions")
            .bodyValue(createOpinionDto)
            .exchange()
            .expectStatus().isOk
            .expectBody<OpinionDto>()
            .returnResult()
            .responseBody!!.id
    }
}
