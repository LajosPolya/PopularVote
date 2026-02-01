package com.github.lajospolya.popularVote.controller

import com.github.lajospolya.popularVote.AbstractIntegrationTest
import com.github.lajospolya.popularVote.dto.CitizenDto
import com.github.lajospolya.popularVote.dto.CreateCitizenDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody

@AutoConfigureWebTestClient
class CitizenControllerIntegrationTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Test
    fun `create citizen and then fetch it`() {
        val createCitizenDto =
            CreateCitizenDto(
                givenName = "John",
                surname = "Doe",
                middleName = "Quincy",
            )

        val createdCitizen =
            webTestClient
                .post()
                .uri("/citizens")
                .bodyValue(createCitizenDto)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<CitizenDto>()
                .returnResult()
                .responseBody

        assertNotNull(createdCitizen)
        assertNotNull(createdCitizen?.id)
        assertEquals(createCitizenDto.givenName, createdCitizen?.givenName)
        assertEquals(createCitizenDto.surname, createdCitizen?.surname)
        assertEquals(createCitizenDto.middleName, createdCitizen?.middleName)

        val fetchedCitizen =
            webTestClient
                .get()
                .uri("/citizens/${createdCitizen?.id}")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(CitizenDto::class.java)
                .returnResult()
                .responseBody

        assertNotNull(fetchedCitizen)
        assertEquals(createdCitizen?.id, fetchedCitizen?.id)
        assertEquals(createCitizenDto.givenName, fetchedCitizen?.givenName)
        assertEquals(createCitizenDto.surname, fetchedCitizen?.surname)
        assertEquals(createCitizenDto.middleName, fetchedCitizen?.middleName)
    }

    @Test
    fun `create citizen, verify exists, delete it, and verify deleted`() {
        val createCitizenDto =
            CreateCitizenDto(
                givenName = "Jane",
                surname = "Smith",
                middleName = null,
            )

        val createdCitizen =
            webTestClient
                .post()
                .uri("/citizens")
                .bodyValue(createCitizenDto)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<CitizenDto>()
                .returnResult()
                .responseBody

        val id = createdCitizen?.id
        assertNotNull(id)

        webTestClient
            .get()
            .uri("/citizens/$id")
            .exchange()
            .expectStatus()
            .isOk

        webTestClient
            .delete()
            .uri("/citizens/$id")
            .exchange()
            .expectStatus()
            .isOk

        webTestClient
            .get()
            .uri("/citizens/$id")
            .exchange()
            .expectStatus()
            .isNotFound
    }

    @Test
    fun `create two citizens and verify count increases`() {
        val initialCount =
            webTestClient
                .get()
                .uri("/citizens")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<List<CitizenDto>>()
                .returnResult()
                .responseBody
                ?.size ?: 0

        val citizen1 =
            CreateCitizenDto(
                givenName = "First",
                surname = "Citizen",
                middleName = null,
            )
        webTestClient
            .post()
            .uri("/citizens")
            .bodyValue(citizen1)
            .exchange()
            .expectStatus()
            .isOk

        webTestClient
            .get()
            .uri("/citizens")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody<List<CitizenDto>>()
            .consumeWith { result ->
                assertEquals(initialCount + 1, result.responseBody?.size)
            }

        val citizen2 =
            CreateCitizenDto(
                givenName = "Second",
                surname = "Citizen",
                middleName = null,
            )
        webTestClient
            .post()
            .uri("/citizens")
            .bodyValue(citizen2)
            .exchange()
            .expectStatus()
            .isOk

        webTestClient
            .get()
            .uri("/citizens")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody<List<CitizenDto>>()
            .consumeWith { result ->
                assertEquals(initialCount + 2, result.responseBody?.size)
            }
    }
}
