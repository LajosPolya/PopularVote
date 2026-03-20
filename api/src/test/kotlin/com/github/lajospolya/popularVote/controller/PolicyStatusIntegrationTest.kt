package com.github.lajospolya.popularVote.controller

import com.github.lajospolya.popularVote.AbstractIntegrationTest
import com.github.lajospolya.popularVote.dto.CitizenDto
import com.github.lajospolya.popularVote.dto.CreateCitizenDto
import com.github.lajospolya.popularVote.dto.CreatePolicyDto
import com.github.lajospolya.popularVote.dto.PageDto
import com.github.lajospolya.popularVote.dto.PolicySummaryDto
import com.github.lajospolya.popularVote.entity.CitizenPoliticalDetails
import com.github.lajospolya.popularVote.repository.CitizenPoliticalDetailsRepository
import com.github.lajospolya.popularVote.service.Auth0ManagementService
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
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
import java.time.LocalDateTime

@AutoConfigureWebTestClient
class PolicyStatusIntegrationTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var auth0ManagementService: Auth0ManagementService

    @Autowired
    private lateinit var citizenPoliticalDetailsRepository: CitizenPoliticalDetailsRepository

    @BeforeEach
    fun setUp() {
        whenever(auth0ManagementService.addRoleToUser(any(), any())).thenReturn(Mono.empty())
    }

    private fun createCitizen(authId: String): Long {
        val createCitizenDto = CreateCitizenDto("John", "Doe", "Quincy")
        return webTestClient
            .mutateWith(mockJwt().jwt { it.subject(authId) })
            .post()
            .uri("/citizens/self")
            .bodyValue(createCitizenDto)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody<CitizenDto>()
            .returnResult()
            .responseBody!!
            .id!!
    }

    private fun setupPoliticalDetailsForCitizen(citizenId: Long) {
        citizenPoliticalDetailsRepository
            .save(
                CitizenPoliticalDetails(
                    citizenId = citizenId,
                    levelOfPoliticsId = 1,
                    electoralDistrictId = 1,
                    politicalPartyId = 1,
                ),
            ).block()!!
    }

    @Test
    fun `filter policies by status`() {
        val authId = "auth-status-test"
        val citizenId = createCitizen(authId)
        setupPoliticalDetailsForCitizen(citizenId)

        val now = LocalDateTime.now()
        val openDate = now.plusDays(1)
        val closedDate = now.minusDays(1)

        val openPolicy = CreatePolicyDto("Open Policy", emptyList(), openDate)
        val closedPolicy = CreatePolicyDto("Closed Policy", emptyList(), closedDate)

        // Create open policy
        webTestClient
            .mutateWith(mockJwt().jwt { it.subject(authId) }.authorities(SimpleGrantedAuthority("SCOPE_write:policies")))
            .post()
            .uri("/policies")
            .bodyValue(openPolicy)
            .exchange()
            .expectStatus()
            .isOk

        // Create closed policy
        webTestClient
            .mutateWith(mockJwt().jwt { it.subject(authId) }.authorities(SimpleGrantedAuthority("SCOPE_write:policies")))
            .post()
            .uri("/policies")
            .bodyValue(closedPolicy)
            .exchange()
            .expectStatus()
            .isOk

        // Test status=open
        val openPolicies =
            webTestClient
                .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:policies")))
                .get()
                .uri("/policies?status=open&size=100")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<PageDto<PolicySummaryDto>>()
                .returnResult()
                .responseBody!!

        assertTrue(openPolicies.content.any { it.description == "Open Policy" })
        assertTrue(openPolicies.content.none { it.description == "Closed Policy" })

        // Test status=closed
        val closedPolicies =
            webTestClient
                .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:policies")))
                .get()
                .uri("/policies?status=closed&size=100")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<PageDto<PolicySummaryDto>>()
                .returnResult()
                .responseBody!!

        assertTrue(closedPolicies.content.any { it.description == "Closed Policy" })
        assertTrue(closedPolicies.content.none { it.description == "Open Policy" })

        // Test status=invalid
        webTestClient
            .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:policies")))
            .get()
            .uri("/policies?status=invalid")
            .exchange()
            .expectStatus()
            .isBadRequest

        // Test no status
        val allPolicies =
            webTestClient
                .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:policies")))
                .get()
                .uri("/policies")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<PageDto<PolicySummaryDto>>()
                .returnResult()
                .responseBody!!

        assertTrue(allPolicies.content.any { it.description == "Open Policy" })
        assertTrue(allPolicies.content.any { it.description == "Closed Policy" })
    }
}
