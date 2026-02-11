package com.github.lajospolya.popularVote.controller

import com.github.lajospolya.popularVote.AbstractIntegrationTest
import com.github.lajospolya.popularVote.dto.CreatePolicyDto
import com.github.lajospolya.popularVote.dto.PolicyDetailsDto
import com.github.lajospolya.popularVote.dto.PolicyDto
import com.github.lajospolya.popularVote.entity.PoliticalAffiliation
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

@AutoConfigureWebTestClient
class PolicyControllerIntegrationTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var auth0ManagementService: Auth0ManagementService

    @Test
    fun `create policy and then fetch it`() {
        val authId = "auth-policy-1"
        val citizenId = createCitizen(authId)
        val createPolicyDto =
            CreatePolicyDto(
                description = "Test Policy Description",
                coAuthorCitizenIds = emptyList(),
            )

        val createdPolicy =
            webTestClient
                .mutateWith(mockJwt().jwt { it.subject(authId) }.authorities(SimpleGrantedAuthority("SCOPE_write:policies")))
                .post()
                .uri("/policies")
                .bodyValue(createPolicyDto)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<PolicyDto>()
                .returnResult()
                .responseBody

        assertNotNull(createdPolicy)
        assertNotNull(createdPolicy?.id)
        assertEquals(createPolicyDto.description, createdPolicy?.description)
        assertEquals(citizenId, createdPolicy?.publisherCitizenId)

        val fetchedPolicy =
            webTestClient
                .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:policies")))
                .get()
                .uri("/policies/${createdPolicy?.id}")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(PolicyDto::class.java)
                .returnResult()
                .responseBody

        assertNotNull(fetchedPolicy)
        assertEquals(createdPolicy?.id, fetchedPolicy?.id)
        assertEquals(createPolicyDto.description, fetchedPolicy?.description)
        assertEquals(citizenId, fetchedPolicy?.publisherCitizenId)
    }

    @Test
    fun `create policy, verify exists, delete it, and verify deleted`() {
        val authId = "auth-policy-2"
        createCitizen(authId)
        val createPolicyDto =
            CreatePolicyDto(
                description = "Policy to be deleted",
                coAuthorCitizenIds = emptyList(),
            )

        val createdPolicy =
            webTestClient
                .mutateWith(mockJwt().jwt { it.subject(authId) }.authorities(SimpleGrantedAuthority("SCOPE_write:policies")))
                .post()
                .uri("/policies")
                .bodyValue(createPolicyDto)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<PolicyDto>()
                .returnResult()
                .responseBody

        val id = createdPolicy?.id
        assertNotNull(id)

        webTestClient
            .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:policies")))
            .get()
            .uri("/policies/$id")
            .exchange()
            .expectStatus()
            .isOk

        webTestClient
            .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_delete:policies")))
            .delete()
            .uri("/policies/$id")
            .exchange()
            .expectStatus()
            .isOk

        webTestClient
            .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:policies")))
            .get()
            .uri("/policies/$id")
            .exchange()
            .expectStatus()
            .isNotFound
    }

    @Test
    fun `create policy and then fetch its details`() {
        val authId = "auth-policy-details"
        createCitizen(authId)
        val createPolicyDto =
            CreatePolicyDto(
                description = "Policy for Details Test",
                coAuthorCitizenIds = emptyList(),
            )

        val createdPolicy =
            webTestClient
                .mutateWith(mockJwt().jwt { it.subject(authId) }.authorities(SimpleGrantedAuthority("SCOPE_write:policies")))
                .post()
                .uri("/policies")
                .bodyValue(createPolicyDto)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<PolicyDto>()
                .returnResult()
                .responseBody!!

        val fetchedDetails =
            webTestClient
                .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:policies")))
                .get()
                .uri("/policies/${createdPolicy.id}/details")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<PolicyDetailsDto>()
                .returnResult()
                .responseBody

        assertNotNull(fetchedDetails)
        assertEquals(createdPolicy.id, fetchedDetails?.id)
        assertEquals(createPolicyDto.description, fetchedDetails?.description)
        assertEquals("Publisher Citizen", fetchedDetails?.publisherName)
        assertEquals(
            com.github.lajospolya.popularVote.entity.PoliticalAffiliation.INDEPENDENT,
            fetchedDetails?.publisherPoliticalAffiliation,
        )
        assertNotNull(fetchedDetails?.opinions)
    }

    @Test
    fun `get policies returns 403 when missing read policies scope`() {
        webTestClient
            .mutateWith(mockJwt())
            .get()
            .uri("/policies")
            .exchange()
            .expectStatus()
            .isForbidden
    }

    @Test
    fun `create two policies and verify count increases`() {
        val authId = "auth-policy-3"
        createCitizen(authId)
        val initialCount =
            webTestClient
                .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:policies")))
                .get()
                .uri("/policies")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<List<PolicyDto>>()
                .returnResult()
                .responseBody
                ?.size ?: 0

        val policy1 =
            CreatePolicyDto(
                description = "First Policy",
                coAuthorCitizenIds = emptyList(),
            )
        webTestClient
            .mutateWith(mockJwt().jwt { it.subject(authId) }.authorities(SimpleGrantedAuthority("SCOPE_write:policies")))
            .post()
            .uri("/policies")
            .bodyValue(policy1)
            .exchange()
            .expectStatus()
            .isOk

        webTestClient
            .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:policies")))
            .get()
            .uri("/policies")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody<List<PolicyDto>>()
            .consumeWith { result ->
                assertEquals(initialCount + 1, result.responseBody?.size)
            }

        val policy2 =
            CreatePolicyDto(
                description = "Second Policy",
                coAuthorCitizenIds = emptyList(),
            )
        webTestClient
            .mutateWith(mockJwt().jwt { it.subject(authId) }.authorities(SimpleGrantedAuthority("SCOPE_write:policies")))
            .post()
            .uri("/policies")
            .bodyValue(policy2)
            .exchange()
            .expectStatus()
            .isOk

        webTestClient
            .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:policies")))
            .get()
            .uri("/policies")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody<List<PolicyDto>>()
            .consumeWith { result ->
                assertEquals(initialCount + 2, result.responseBody?.size)
            }
    }

    @Test
    fun `create policy, add opinion, and fetch details`() {
        val policyAuthId = "auth-policy-opinion-details"
        createCitizen(policyAuthId)
        val createPolicyDto = CreatePolicyDto(description = "Policy with Opinion", coAuthorCitizenIds = emptyList())
        val policy =
            webTestClient
                .mutateWith(mockJwt().jwt { it.subject(policyAuthId) }.authorities(SimpleGrantedAuthority("SCOPE_write:policies")))
                .post()
                .uri("/policies")
                .bodyValue(createPolicyDto)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<PolicyDto>()
                .returnResult()
                .responseBody!!

        val opinionAuthId = "auth-opinion-author-details"
        createCitizen(
            opinionAuthId,
            "Opinion",
            "Author",
            com.github.lajospolya.popularVote.entity.PoliticalAffiliation.LIBERAL_PARTY_OF_CANADA,
        )
        val createOpinionDto =
            com.github.lajospolya.popularVote.dto.CreateOpinionDto(
                description = "Opinion Description",
                policyId = policy.id,
            )
        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt {
                        it.subject(opinionAuthId)
                    }.authorities(SimpleGrantedAuthority("SCOPE_read:policies"), SimpleGrantedAuthority("SCOPE_write:opinions")),
            ).post()
            .uri("/opinions")
            .bodyValue(createOpinionDto)
            .exchange()
            .expectStatus()
            .isOk

        val fetchedDetails =
            webTestClient
                .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:policies")))
                .get()
                .uri("/policies/${policy.id}/details")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<PolicyDetailsDto>()
                .returnResult()
                .responseBody!!

        assertEquals(1, fetchedDetails.opinions.size)
        val opinion = fetchedDetails.opinions[0]
        assertEquals("Opinion Description", opinion.description)
        assertEquals("Opinion Author", opinion.authorName)
        assertEquals(
            com.github.lajospolya.popularVote.entity.PoliticalAffiliation.LIBERAL_PARTY_OF_CANADA,
            opinion.authorPoliticalAffiliation,
        )
    }

    private fun createCitizen(
        authId: String,
        givenName: String = "Publisher",
        surname: String = "Citizen",
        politicalAffiliation: PoliticalAffiliation = PoliticalAffiliation.INDEPENDENT,
    ): Long {
        val createCitizenDto =
            com.github.lajospolya.popularVote.dto.CreateCitizenDto(
                givenName = givenName,
                surname = surname,
                middleName = null,
                politicalAffiliation = politicalAffiliation,
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
            .expectBody<com.github.lajospolya.popularVote.dto.CitizenDto>()
            .returnResult()
            .responseBody
            ?.id!!
    }
}
