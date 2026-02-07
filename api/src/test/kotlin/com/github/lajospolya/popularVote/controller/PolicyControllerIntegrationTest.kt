package com.github.lajospolya.popularVote.controller

import com.github.lajospolya.popularVote.AbstractIntegrationTest
import com.github.lajospolya.popularVote.dto.CreatePolicyDto
import com.github.lajospolya.popularVote.dto.PolicyDetailsDto
import com.github.lajospolya.popularVote.dto.PolicyDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody

@AutoConfigureWebTestClient
class PolicyControllerIntegrationTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Test
    fun `create policy and then fetch it`() {
        val authId = "auth-policy-1"
        val citizenId = createCitizen(authId)
        val createPolicyDto =
            CreatePolicyDto(
                description = "Test Policy Description",
            )

        val createdPolicy =
            webTestClient
                .mutateWith(mockJwt().jwt { it.subject(authId) })
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
                .mutateWith(mockJwt())
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
            )

        val createdPolicy =
            webTestClient
                .mutateWith(mockJwt().jwt { it.subject(authId) })
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
            .mutateWith(mockJwt())
            .get()
            .uri("/policies/$id")
            .exchange()
            .expectStatus()
            .isOk

        webTestClient
            .mutateWith(mockJwt())
            .delete()
            .uri("/policies/$id")
            .exchange()
            .expectStatus()
            .isOk

        webTestClient
            .mutateWith(mockJwt())
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
            )

        val createdPolicy =
            webTestClient
                .mutateWith(mockJwt().jwt { it.subject(authId) })
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
                .mutateWith(mockJwt())
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
            )
        webTestClient
            .mutateWith(mockJwt().jwt { it.subject(authId) })
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
            )
        webTestClient
            .mutateWith(mockJwt().jwt { it.subject(authId) })
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

    private fun createCitizen(authId: String): Long {
        val createCitizenDto =
            com.github.lajospolya.popularVote.dto.CreateCitizenDto(
                givenName = "Publisher",
                surname = "Citizen",
                middleName = null,
                politicalAffiliation = com.github.lajospolya.popularVote.entity.PoliticalAffiliation.INDEPENDENT,
            )

        return webTestClient
            .mutateWith(mockJwt().jwt { it.subject(authId) })
            .post()
            .uri("/citizens")
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
