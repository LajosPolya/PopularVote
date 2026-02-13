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

    @Test
    fun `get opinion like counts`() {
        val policyId = createPolicy("counts-policy")
        val opinionId1 = createOpinion(policyId, "counts-op1")
        val opinionId2 = createOpinion(policyId, "counts-op2")

        val user1 = "user-1"
        val user2 = "user-2"
        createCitizen(user1)
        createCitizen(user2)

        // user1 likes opinion1
        likeOpinion(user1, opinionId1)
        // user2 likes opinion1
        likeOpinion(user2, opinionId1)
        // user2 likes opinion2
        likeOpinion(user2, opinionId2)

        // Fetch counts
        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject("any-user") }
                    .authorities(
                        SimpleGrantedAuthority("SCOPE_read:opinions")
                    )
            )
            .get()
            .uri { builder ->
                builder.path("/opinions/likes/count")
                    .queryParam("opinionIds", listOf(opinionId1, opinionId2))
                    .build()
            }
            .exchange()
            .expectStatus().isOk
            .expectBody<List<OpinionLikeCountDto>>()
            .value { counts ->
                assert(counts != null)
                assert(counts!!.size == 2)
                val count1 = counts!!.find { it.opinionId == opinionId1 }
                val count2 = counts!!.find { it.opinionId == opinionId2 }
                assert(count1?.likeCount == 2L)
                assert(count2?.likeCount == 1L)
            }
    }

    private fun likeOpinion(authId: String, opinionId: Long) {
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
    }

    private fun createCitizen(authId: String): Long {
        val createCitizenDto = CreateCitizenDto(
            givenName = "Like",
            surname = "Tester-$authId",
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

    private fun createPolicy(authorSuffix: String = "policy-author"): Long {
        val authId = "auth-$authorSuffix"
        createCitizen(authId)
        val createPolicyDto = CreatePolicyDto(
            description = "Policy for like test $authorSuffix",
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

    private fun createOpinion(policyId: Long, authorSuffix: String = "opinion-author"): Long {
        val authId = "auth-$authorSuffix"
        createCitizen(authId)
        val createOpinionDto = CreateOpinionDto(description = "Opinion for like test $authorSuffix", policyId = policyId)
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
