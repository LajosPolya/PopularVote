package com.github.lajospolya.popularVote.controller

import com.github.lajospolya.popularVote.dto.CreateOpinionDto
import com.github.lajospolya.popularVote.dto.CreatePolicyDto
import com.github.lajospolya.popularVote.dto.OpinionDto
import com.github.lajospolya.popularVote.dto.PolicyDto
import com.github.lajospolya.popularVote.entity.PoliticalSpectrum
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class OpinionControllerIntegrationTest {
    @Autowired
    private lateinit var webTestClient: WebTestClient

    private fun createPolicy(): PolicyDto {
        val createPolicyDto = CreatePolicyDto(description = "Policy for Opinion Test")
        return webTestClient
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
        val policy = createPolicy()
        val createOpinionDto =
            CreateOpinionDto(
                politicalSpectrum = PoliticalSpectrum.CENTER,
                description = "Neutral opinion",
                author = "Opinion Author",
                policyId = policy.id,
            )

        val createdOpinion =
            webTestClient
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
        assertEquals(createOpinionDto.politicalSpectrum, createdOpinion?.politicalSpectrum)
        assertEquals(createOpinionDto.description, createdOpinion?.description)
        assertEquals(createOpinionDto.author, createdOpinion?.author)
        assertEquals(createOpinionDto.policyId, createdOpinion?.policyId)

        val fetchedOpinion =
            webTestClient
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
        val policy = createPolicy()
        val createOpinionDto =
            CreateOpinionDto(
                politicalSpectrum = PoliticalSpectrum.LEFT,
                description = "Leftist opinion",
                author = "Author Name",
                policyId = policy.id,
            )

        val createdOpinion =
            webTestClient
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
            .get()
            .uri("/opinions/$id")
            .exchange()
            .expectStatus()
            .isOk

        webTestClient
            .delete()
            .uri("/opinions/$id")
            .exchange()
            .expectStatus()
            .isOk

        webTestClient
            .get()
            .uri("/opinions/$id")
            .exchange()
            .expectStatus()
            .isNotFound
    }

    @Test
    fun `create two opinions and verify count increases`() {
        val policy = createPolicy()
        val initialCount =
            webTestClient
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
                politicalSpectrum = PoliticalSpectrum.RIGHT,
                description = "Rightist opinion 1",
                author = "Author 1",
                policyId = policy.id,
            )
        webTestClient
            .post()
            .uri("/opinions")
            .bodyValue(opinion1)
            .exchange()
            .expectStatus()
            .isOk

        webTestClient
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
                politicalSpectrum = PoliticalSpectrum.CENTER,
                description = "Center opinion 2",
                author = "Author 2",
                policyId = policy.id,
            )
        webTestClient
            .post()
            .uri("/opinions")
            .bodyValue(opinion2)
            .exchange()
            .expectStatus()
            .isOk

        webTestClient
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
}
