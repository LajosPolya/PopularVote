package com.github.lajospolya.popularVote.controller

import com.github.lajospolya.popularVote.AbstractIntegrationTest
import com.github.lajospolya.popularVote.dto.PageDto
import com.github.lajospolya.popularVote.dto.PoliticalPartyDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody

@AutoConfigureWebTestClient
class PoliticalPartyControllerIntegrationPaginationTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Test
    fun `get political parties with pagination`() {
        // Fetch first page with size 2
        val page0 =
            webTestClient
                .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:political-parties")))
                .get()
                .uri("/political-parties?page=0&size=2")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<PageDto<PoliticalPartyDto>>()
                .returnResult()
                .responseBody!!

        assertEquals(0, page0.pageNumber)
        assertEquals(2, page0.pageSize)
        assertEquals(2, page0.content.size)
        assertTrue(page0.totalElements >= 6)

        // Fetch second page with size 2
        val page1 =
            webTestClient
                .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:political-parties")))
                .get()
                .uri("/political-parties?page=1&size=2")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<PageDto<PoliticalPartyDto>>()
                .returnResult()
                .responseBody!!

        assertEquals(1, page1.pageNumber)
        assertEquals(2, page1.pageSize)
        assertEquals(2, page1.content.size)

        // Ensure content is different
        val ids0 = page0.content.map { it.id }
        val ids1 = page1.content.map { it.id }
        assertTrue(ids0.none { it in ids1 }, "Page 0 and Page 1 should have different parties")
    }

    @Test
    fun `get political parties with pagination and levelOfPolitics filter`() {
        val levelId = 1L // Federal
        val size = 3
        val page =
            webTestClient
                .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:political-parties")))
                .get()
                .uri("/political-parties?levelOfPolitics=$levelId&page=0&size=$size")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<PageDto<PoliticalPartyDto>>()
                .returnResult()
                .responseBody!!

        assertEquals(0, page.pageNumber)
        assertEquals(size, page.pageSize)
        assertTrue(page.content.size <= size)
        assertTrue(page.content.all { it.levelOfPoliticsId == levelId })
    }
}
