package com.github.lajospolya.popularVote.controller

import com.github.lajospolya.popularVote.AbstractIntegrationTest
import com.github.lajospolya.popularVote.dto.CreateOpinionDto
import com.github.lajospolya.popularVote.dto.CreatePolicyDto
import com.github.lajospolya.popularVote.dto.OpinionDto
import com.github.lajospolya.popularVote.dto.PolicyDto
import com.github.lajospolya.popularVote.dto.CreateCitizenDto
import com.github.lajospolya.popularVote.dto.CitizenDto
import com.github.lajospolya.popularVote.entity.PoliticalAffiliation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt

@AutoConfigureWebTestClient
class OpinionControllerIntegrationTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var webTestClient: WebTestClient

    private fun createPolicy(authId: String = "auth-policy-opinion"): PolicyDto {
        createCitizen(authId)
        val createPolicyDto = CreatePolicyDto(
            description = "Policy for Opinion Test",
        )
        return webTestClient
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
    }

    @Test
    fun `create opinion and then fetch it`() {
        val policy = createPolicy("auth-policy-opinion-fetch")
        val createOpinionDto =
            CreateOpinionDto(
                description = "Neutral opinion",
                author = "Opinion Author",
                policyId = policy.id,
            )

        val createdOpinion =
            webTestClient
                .mutateWith(mockJwt())
                .post()
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
        assertEquals(createOpinionDto.author, createdOpinion?.author)
        assertEquals(createOpinionDto.policyId, createdOpinion?.policyId)

        val fetchedOpinion =
            webTestClient
                .mutateWith(mockJwt())
                .get()
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
        val policy = createPolicy("auth-policy-opinion-delete")
        val createOpinionDto =
            CreateOpinionDto(
                description = "Leftist opinion",
                author = "Author Name",
                policyId = policy.id,
            )

        val createdOpinion =
            webTestClient
                .mutateWith(mockJwt())
                .post()
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
            .mutateWith(mockJwt())
            .get()
            .uri("/opinions/$id")
            .exchange()
            .expectStatus()
            .isOk

        webTestClient
            .mutateWith(mockJwt())
            .delete()
            .uri("/opinions/$id")
            .exchange()
            .expectStatus()
            .isOk

        webTestClient
            .mutateWith(mockJwt())
            .get()
            .uri("/opinions/$id")
            .exchange()
            .expectStatus()
            .isNotFound
    }

    @Test
    fun `create two opinions and verify count increases`() {
        val policy = createPolicy("auth-policy-opinion-count")
        val initialCount =
            webTestClient
                .mutateWith(mockJwt())
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
                author = "Author 1",
                policyId = policy.id,
            )
        webTestClient
            .mutateWith(mockJwt())
            .post()
            .uri("/opinions")
            .bodyValue(opinion1)
            .exchange()
            .expectStatus()
            .isOk

        webTestClient
            .mutateWith(mockJwt())
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
                author = "Author 2",
                policyId = policy.id,
            )
        webTestClient
            .mutateWith(mockJwt())
            .post()
            .uri("/opinions")
            .bodyValue(opinion2)
            .exchange()
            .expectStatus()
            .isOk

        webTestClient
            .mutateWith(mockJwt())
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
        val policy1 = createPolicy("auth-policy-opinion-1")
        val policy2 = createPolicy("auth-policy-opinion-2")

        val opinion1 =
            CreateOpinionDto(
                description = "Opinion for Policy 1",
                author = "Author 1",
                policyId = policy1.id,
            )
        webTestClient
            .mutateWith(mockJwt())
            .post()
            .uri("/opinions")
            .bodyValue(opinion1)
            .exchange()
            .expectStatus()
            .isOk

        val opinion2 =
            CreateOpinionDto(
                description = "Opinion for Policy 2",
                author = "Author 2",
                policyId = policy2.id,
            )
        webTestClient
            .mutateWith(mockJwt())
            .post()
            .uri("/opinions")
            .bodyValue(opinion2)
            .exchange()
            .expectStatus()
            .isOk

        // Verify policy 1 opinions
        webTestClient
            .mutateWith(mockJwt())
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
            .mutateWith(mockJwt())
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
                politicalAffiliation = PoliticalAffiliation.INDEPENDENT,
                authId = authId,
            )

        return webTestClient
            .mutateWith(mockJwt())
            .post()
            .uri("/citizens")
            .bodyValue(createCitizenDto)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody<CitizenDto>()
            .returnResult()
            .responseBody?.id!!
    }
}
