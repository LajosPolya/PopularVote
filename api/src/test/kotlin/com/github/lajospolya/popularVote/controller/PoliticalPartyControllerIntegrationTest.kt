package com.github.lajospolya.popularVote.controller

import com.github.lajospolya.popularVote.AbstractIntegrationTest
import com.github.lajospolya.popularVote.dto.CreatePoliticalPartyDto
import com.github.lajospolya.popularVote.dto.PoliticalPartyDto
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
class PoliticalPartyControllerIntegrationTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Test
    fun `create, fetch, update and delete political party`() {
        val createDto = CreatePoliticalPartyDto(
            displayName = "New Political Party"
        )

        // 1. Create
        val createdParty = webTestClient
            .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_write:political-parties")))
            .post()
            .uri("/political-parties")
            .bodyValue(createDto)
            .exchange()
            .expectStatus().isOk
            .expectBody<PoliticalPartyDto>()
            .returnResult()
            .responseBody!!

        assertNotNull(createdParty.id)
        assertEquals(createDto.displayName, createdParty.displayName)

        // 2. Fetch by ID
        val fetchedParty = webTestClient
            .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:political-parties")))
            .get()
            .uri("/political-parties/${createdParty.id}")
            .exchange()
            .expectStatus().isOk
            .expectBody<PoliticalPartyDto>()
            .returnResult()
            .responseBody!!

        assertEquals(createdParty.id, fetchedParty.id)

        // 3. Update
        val updateDto = CreatePoliticalPartyDto(
            displayName = "Updated Political Party"
        )

        val updatedParty = webTestClient
            .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_write:political-parties")))
            .put()
            .uri("/political-parties/${createdParty.id}")
            .bodyValue(updateDto)
            .exchange()
            .expectStatus().isOk
            .expectBody<PoliticalPartyDto>()
            .returnResult()
            .responseBody!!

        assertEquals(createdParty.id, updatedParty.id)
        assertEquals(updateDto.displayName, updatedParty.displayName)

        // 4. Delete
        webTestClient
            .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_delete:political-parties")))
            .delete()
            .uri("/political-parties/${createdParty.id}")
            .exchange()
            .expectStatus().isOk

        // 5. Verify deleted
        webTestClient
            .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:political-parties")))
            .get()
            .uri("/political-parties/${createdParty.id}")
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `get all political parties`() {
        val parties = webTestClient
            .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:political-parties")))
            .get()
            .uri("/political-parties")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(PoliticalPartyDto::class.java)
            .returnResult()
            .responseBody!!

        // There should be at least the 6 seeded parties
        assertNotNull(parties)
        assert(parties.size >= 6)
    }
}
