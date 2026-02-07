package com.github.lajospolya.popularVote.controller

import com.github.lajospolya.popularVote.AbstractIntegrationTest
import com.github.lajospolya.popularVote.dto.CitizenDto
import com.github.lajospolya.popularVote.dto.CitizenSelfDto
import com.github.lajospolya.popularVote.dto.CreateCitizenDto
import com.github.lajospolya.popularVote.entity.PoliticalAffiliation
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
class CitizenControllerIntegrationTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Test
    fun `create citizen and then fetch it by self`() {
        val authId = "auth-123"
        val createCitizenDto =
            CreateCitizenDto(
                givenName = "John",
                surname = "Doe",
                middleName = "Quincy",
                politicalAffiliation = PoliticalAffiliation.LIBERAL_PARTY_OF_CANADA,
            )

        val createdCitizen =
            webTestClient
                .mutateWith(
                    mockJwt()
                        .jwt { it.subject(authId) }
                        .authorities(SimpleGrantedAuthority("SCOPE_write:citizens"))
                )
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
        assertEquals(createCitizenDto.politicalAffiliation, createdCitizen?.politicalAffiliation)
        assertEquals(authId, createdCitizen?.authId)

        val fetchedCitizen =
            webTestClient
                .mutateWith(
                    mockJwt()
                        .jwt { it.subject(authId) }
                        .authorities(SimpleGrantedAuthority("SCOPE_read:citizens"))
                )
                .get()
                .uri("/citizens/self")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(CitizenSelfDto::class.java)
                .returnResult()
                .responseBody

        assertNotNull(fetchedCitizen)
        assertEquals(createCitizenDto.givenName, fetchedCitizen?.givenName)
        assertEquals(createCitizenDto.surname, fetchedCitizen?.surname)
        assertEquals(createCitizenDto.middleName, fetchedCitizen?.middleName)
        assertEquals(createCitizenDto.politicalAffiliation, fetchedCitizen?.politicalAffiliation)
    }

    @Test
    fun `create citizen, verify exists, delete it, and verify deleted`() {
        val authId = "auth-456"
        val createCitizenDto =
            CreateCitizenDto(
                givenName = "Jane",
                surname = "Smith",
                middleName = null,
                politicalAffiliation = PoliticalAffiliation.CONSERVATIVE_PARTY_OF_CANADA,
            )

        val createdCitizen =
            webTestClient
                .mutateWith(
                    mockJwt()
                        .jwt { it.subject(authId) }
                        .authorities(SimpleGrantedAuthority("SCOPE_write:citizens"))
                )
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
            .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:citizens")))
            .get()
            .uri("/citizens/$id")
            .exchange()
            .expectStatus()
            .isOk

        webTestClient
            .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_delete:citizens")))
            .delete()
            .uri("/citizens/$id")
            .exchange()
            .expectStatus()
            .isOk

        webTestClient
            .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:citizens")))
            .get()
            .uri("/citizens/$id")
            .exchange()
            .expectStatus()
            .isNotFound
    }

    @Test
    fun `head citizen by authId returns 204 when exists and 404 when not`() {
        val authId = "auth-head-test"
        val createCitizenDto =
            CreateCitizenDto(
                givenName = "Head",
                surname = "Test",
                middleName = null,
                politicalAffiliation = PoliticalAffiliation.INDEPENDENT,
            )

        // 1. Verify 404 before creation
        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(authId) }
                    .authorities(SimpleGrantedAuthority("SCOPE_read:citizens"))
            )
            .head()
            .uri("/citizens/self")
            .exchange()
            .expectStatus()
            .isNotFound

        // 2. Create citizen
        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(authId) }
                    .authorities(SimpleGrantedAuthority("SCOPE_write:citizens"))
            )
            .post()
            .uri("/citizens")
            .bodyValue(createCitizenDto)
            .exchange()
            .expectStatus()
            .isOk

        // 3. Verify 204 after creation
        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(authId) }
                    .authorities(SimpleGrantedAuthority("SCOPE_read:citizens"))
            )
            .head()
            .uri("/citizens/self")
            .exchange()
            .expectStatus()
            .isNoContent

        // 4. Verify 404 for non-existent authId (by using a different JWT subject)
        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject("non-existent-auth-id") }
                    .authorities(SimpleGrantedAuthority("SCOPE_read:citizens"))
            )
            .head()
            .uri("/citizens/self")
            .exchange()
            .expectStatus()
            .isNotFound
    }

    @Test
    fun `create two citizens and verify count increases`() {
        val initialCount =
            webTestClient
                .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:citizens")))
                .get()
                .uri("/citizens")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<List<CitizenDto>>()
                .returnResult()
                .responseBody
                ?.size ?: 0

        val authId1 = "auth-789"
        val citizen1 =
            CreateCitizenDto(
                givenName = "First",
                surname = "Citizen",
                middleName = null,
                politicalAffiliation = PoliticalAffiliation.GREEN_PARTY_OF_CANADA,
            )
        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(authId1) }
                    .authorities(SimpleGrantedAuthority("SCOPE_write:citizens"))
            )
            .post()
            .uri("/citizens")
            .bodyValue(citizen1)
            .exchange()
            .expectStatus()
            .isOk

        webTestClient
            .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:citizens")))
            .get()
            .uri("/citizens")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody<List<CitizenDto>>()
            .consumeWith { result ->
                assertEquals(initialCount + 1, result.responseBody?.size)
            }

        val authId2 = "auth-012"
        val citizen2 =
            CreateCitizenDto(
                givenName = "Second",
                surname = "Citizen",
                middleName = null,
                politicalAffiliation = PoliticalAffiliation.BLOC_QUEBECOIS,
            )
        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(authId2) }
                    .authorities(SimpleGrantedAuthority("SCOPE_write:citizens"))
            )
            .post()
            .uri("/citizens")
            .bodyValue(citizen2)
            .exchange()
            .expectStatus()
            .isOk

        webTestClient
            .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:citizens")))
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

    @Test
    fun `create citizen and search by name`() {
        val authId = "auth-345"
        val createCitizenDto =
            CreateCitizenDto(
                givenName = "Alice",
                surname = "Wonderland",
                middleName = "In",
                politicalAffiliation = PoliticalAffiliation.NEW_DEMOCRATIC_PARTY,
            )

        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(authId) }
                    .authorities(SimpleGrantedAuthority("SCOPE_write:citizens"))
            )
            .post()
            .uri("/citizens")
            .bodyValue(createCitizenDto)
            .exchange()
            .expectStatus()
            .isOk

        val searchedCitizen =
            webTestClient
                .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:citizens")))
                .get()
                .uri("/citizens/search?givenName=Alice&surname=Wonderland")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<CitizenDto>()
                .returnResult()
                .responseBody

        assertNotNull(searchedCitizen)
        assertEquals(createCitizenDto.givenName, searchedCitizen?.givenName)
        assertEquals(createCitizenDto.surname, searchedCitizen?.surname)
        assertEquals(createCitizenDto.politicalAffiliation, searchedCitizen?.politicalAffiliation)

        webTestClient
            .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:citizens")))
            .get()
            .uri("/citizens/search?givenName=Non&surname=Existent")
            .exchange()
            .expectStatus()
            .isNotFound
    }
}
