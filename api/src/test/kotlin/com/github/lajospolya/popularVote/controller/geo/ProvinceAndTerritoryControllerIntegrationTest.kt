package com.github.lajospolya.popularVote.controller.geo

import com.github.lajospolya.popularVote.AbstractIntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt
import org.springframework.test.web.reactive.server.WebTestClient

@AutoConfigureWebTestClient
class ProvinceAndTerritoryControllerIntegrationTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Test
    fun `get provinces and territories`() {
        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject("authId") },
            ).get()
            .uri("/provinces-and-territories")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$")
            .isArray
    }
}
