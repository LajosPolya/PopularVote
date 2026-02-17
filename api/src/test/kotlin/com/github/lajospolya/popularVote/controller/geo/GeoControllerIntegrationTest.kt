package com.github.lajospolya.popularVote.controller.geo

import com.github.lajospolya.popularVote.AbstractIntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.test.web.reactive.server.WebTestClient

@AutoConfigureWebTestClient
class GeoControllerIntegrationTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Test
    fun getGeoDataEndpointReturnsGeoData() {
        webTestClient
            .get()
            .uri("/geo-data")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.provincesAndTerritories").isArray
            .jsonPath("$.provincesAndTerritories[0].municipalities").isArray
            .jsonPath("$.provincesAndTerritories[0].municipalities[0].postalCodes").isArray
            .jsonPath("$.municipalities").isArray
            .jsonPath("$.municipalities[0].postalCodes").isArray
            .jsonPath("$.federalElectoralDistricts").isArray
            .jsonPath("$.postalCodes").isArray
    }
}
