package com.github.lajospolya.popularVote.controller

import com.github.lajospolya.popularVote.AbstractIntegrationTest
import com.github.lajospolya.popularVote.dto.CitizenDto
import com.github.lajospolya.popularVote.dto.CitizenSelfDto
import com.github.lajospolya.popularVote.dto.CreateCitizenDto
import com.github.lajospolya.popularVote.dto.CreateOpinionDto
import com.github.lajospolya.popularVote.dto.CreatePolicyDto
import com.github.lajospolya.popularVote.dto.DeclarePoliticianDto
import com.github.lajospolya.popularVote.dto.OpinionDto
import com.github.lajospolya.popularVote.dto.PolicyDto
import com.github.lajospolya.popularVote.service.Auth0ManagementService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@AutoConfigureWebTestClient
class OpinionControllerIntegrationTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var auth0ManagementService: Auth0ManagementService

    private fun verifyPolitician(citizenId: Long) {
        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { }
                    .authorities(
                        SimpleGrantedAuthority("SCOPE_write:verify-politician"),
                    ),
            ).put()
            .uri("/citizens/$citizenId/verify-politician")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody<CitizenSelfDto>()
            .returnResult()
    }

    private fun declareSelfPolitician(authId: String) {
        val declareSelfPoliticianDto =
            DeclarePoliticianDto(
                levelOfPoliticsId = 1,
                federalElectoralDistrictId = 1,
                politicalAffiliationId = 2,
            )

        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(authId) }
                    .authorities(
                        SimpleGrantedAuthority("SCOPE_write:declare-politician"),
                    ),
            ).post()
            .uri("/citizens/self/declare-politician")
            .bodyValue(declareSelfPoliticianDto)
            .exchange()
            .expectStatus()
            .isAccepted
            .expectBody<CitizenDto>()
            .returnResult()
            .status
    }

    private fun createPolicy(authId: String = "auth-policy-opinion"): PolicyDto {
        val citizenId = createCitizen(authId)
        declareSelfPolitician(authId)
        verifyPolitician(citizenId)
        val createPolicyDto =
            CreatePolicyDto(
                description = "Policy for Opinion Test",
                coAuthorCitizenIds = emptyList(),
                LocalDateTime.now(),
            )
        return webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(authId) }
                    .authorities(
                        SimpleGrantedAuthority("SCOPE_write:self"),
                        SimpleGrantedAuthority("SCOPE_write:policies"),
                    ),
            ).post()
            .uri("/policies")
            .bodyValue(createPolicyDto)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody<PolicyDto>()
            .returnResult()
            .responseBody!!
    }

    @Test
    fun `create opinion and then fetch it`() {
        val policyAuthId = "auth-policy-opinion-fetch"
        val policy = createPolicy(policyAuthId)
        val authorAuthId = "auth-opinion-author-fetch"
        val citizenId = createCitizen(authorAuthId)
        val createOpinionDto =
            CreateOpinionDto(
                description = "Neutral opinion",
                policyId = policy.id,
            )

        val createdOpinion =
            webTestClient
                .mutateWith(
                    mockJwt()
                        .jwt { it.subject(authorAuthId) }
                        .authorities(
                            SimpleGrantedAuthority("SCOPE_read:policies"),
                            SimpleGrantedAuthority("SCOPE_write:opinions"),
                        ),
                ).post()
                .uri("/opinions")
                .bodyValue(createOpinionDto)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<OpinionDto>()
                .returnResult()
                .responseBody

        assertNotNull(createdOpinion)
        assertNotNull(createdOpinion?.id)
        assertEquals(createOpinionDto.description, createdOpinion?.description)
        assertEquals(citizenId, createdOpinion?.authorId)
        assertEquals(createOpinionDto.policyId, createdOpinion?.policyId)

        val fetchedOpinion =
            webTestClient
                .mutateWith(
                    mockJwt()
                        .authorities(SimpleGrantedAuthority("SCOPE_read:opinions")),
                ).get()
                .uri("/opinions/${createdOpinion?.id}")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<OpinionDto>()
                .returnResult()
                .responseBody

        assertNotNull(fetchedOpinion)
        assertEquals(createdOpinion?.id, fetchedOpinion?.id)
        assertEquals(createOpinionDto.description, fetchedOpinion?.description)
    }

    @Test
    fun `create opinion, verify exists, delete it, and verify deleted`() {
        val policyAuthId = "auth-policy-opinion-delete"
        val policy = createPolicy(policyAuthId)
        val authorAuthId = "auth-opinion-author-delete"
        createCitizen(authorAuthId)
        val createOpinionDto =
            CreateOpinionDto(
                description = "Leftist opinion",
                policyId = policy.id,
            )

        val createdOpinion =
            webTestClient
                .mutateWith(
                    mockJwt()
                        .jwt { it.subject(authorAuthId) }
                        .authorities(
                            SimpleGrantedAuthority("SCOPE_read:policies"),
                            SimpleGrantedAuthority("SCOPE_write:opinions"),
                        ),
                ).post()
                .uri("/opinions")
                .bodyValue(createOpinionDto)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<OpinionDto>()
                .returnResult()
                .responseBody

        val id = createdOpinion?.id
        assertNotNull(id)

        webTestClient
            .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:opinions")))
            .get()
            .uri("/opinions/$id")
            .exchange()
            .expectStatus()
            .isOk

        webTestClient
            .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_delete:opinions")))
            .delete()
            .uri("/opinions/$id")
            .exchange()
            .expectStatus()
            .isOk

        webTestClient
            .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:opinions")))
            .get()
            .uri("/opinions/$id")
            .exchange()
            .expectStatus()
            .isNotFound
    }

    @Test
    fun `create two opinions and verify count increases`() {
        val policyAuthId = "auth-policy-opinion-count"
        val policy = createPolicy(policyAuthId)
        val authorAuthId = "auth-opinion-author-count"
        createCitizen(authorAuthId)
        val initialCount =
            webTestClient
                .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:opinions")))
                .get()
                .uri("/opinions")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<List<OpinionDto>>()
                .returnResult()
                .responseBody
                ?.size ?: 0

        val opinion1 =
            CreateOpinionDto(
                description = "Rightist opinion 1",
                policyId = policy.id,
            )
        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(authorAuthId) }
                    .authorities(
                        SimpleGrantedAuthority("SCOPE_read:policies"),
                        SimpleGrantedAuthority("SCOPE_write:opinions"),
                    ),
            ).post()
            .uri("/opinions")
            .bodyValue(opinion1)
            .exchange()
            .expectStatus()
            .isOk

        webTestClient
            .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:opinions")))
            .get()
            .uri("/opinions")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody<List<OpinionDto>>()
            .consumeWith { result ->
                assertEquals(initialCount + 1, result.responseBody?.size)
            }

        val opinion2 =
            CreateOpinionDto(
                description = "Center opinion 2",
                policyId = policy.id,
            )
        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(authorAuthId) }
                    .authorities(
                        SimpleGrantedAuthority("SCOPE_read:policies"),
                        SimpleGrantedAuthority("SCOPE_write:opinions"),
                    ),
            ).post()
            .uri("/opinions")
            .bodyValue(opinion2)
            .exchange()
            .expectStatus()
            .isOk

        webTestClient
            .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:opinions")))
            .get()
            .uri("/opinions")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody<List<OpinionDto>>()
            .consumeWith { result ->
                assertEquals(initialCount + 2, result.responseBody?.size)
            }
    }

    @Test
    fun `create opinions for different policies and verify filtering`() {
        val policyAuthId1 = "auth-policy-opinion-1"
        val policyAuthId2 = "auth-policy-opinion-2"
        val policy1 = createPolicy(policyAuthId1)
        val policy2 = createPolicy(policyAuthId2)
        val authorAuthId = "auth-opinion-author-filter"
        createCitizen(authorAuthId)

        val opinion1 =
            CreateOpinionDto(
                description = "Opinion for Policy 1",
                policyId = policy1.id,
            )
        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(authorAuthId) }
                    .authorities(
                        SimpleGrantedAuthority("SCOPE_read:policies"),
                        SimpleGrantedAuthority("SCOPE_write:opinions"),
                    ),
            ).post()
            .uri("/opinions")
            .bodyValue(opinion1)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody<OpinionDto>()

        val opinion2 =
            CreateOpinionDto(
                description = "Opinion for Policy 2",
                policyId = policy2.id,
            )
        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(authorAuthId) }
                    .authorities(
                        SimpleGrantedAuthority("SCOPE_read:policies"),
                        SimpleGrantedAuthority("SCOPE_write:opinions"),
                    ),
            ).post()
            .uri("/opinions")
            .bodyValue(opinion2)
            .exchange()
            .expectStatus()
            .isOk

        // Verify policy 1 opinions
        webTestClient
            .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:opinions")))
            .get()
            .uri("/policies/${policy1.id}/opinions")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody<List<OpinionDto>>()
            .consumeWith { result ->
                val opinions = result.responseBody
                assertNotNull(opinions)
                assertEquals(1, opinions?.size)
                assertEquals("Opinion for Policy 1", opinions?.first()?.description)
            }

        // Verify policy 2 opinions
        webTestClient
            .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:opinions")))
            .get()
            .uri("/policies/${policy2.id}/opinions")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody<List<OpinionDto>>()
            .consumeWith { result ->
                val opinions = result.responseBody
                assertNotNull(opinions)
                assertEquals(1, opinions?.size)
                assertEquals("Opinion for Policy 2", opinions?.first()?.description)
            }
    }

    private fun createCitizen(authId: String): Long {
        val createCitizenDto =
            CreateCitizenDto(
                givenName = "Publisher",
                surname = "Citizen",
                middleName = null,
            )

        whenever(auth0ManagementService.addRoleToUser(any(), any())).thenReturn(Mono.empty())

        return webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(authId) }
                    .authorities(SimpleGrantedAuthority("SCOPE_write:self")),
            ).post()
            .uri("/citizens/self")
            .bodyValue(createCitizenDto)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody<CitizenDto>()
            .returnResult()
            .responseBody
            ?.id!!
    }
}
