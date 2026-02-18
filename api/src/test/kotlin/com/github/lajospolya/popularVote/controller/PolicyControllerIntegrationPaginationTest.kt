package com.github.lajospolya.popularVote.controller

import com.github.lajospolya.popularVote.AbstractIntegrationTest
import com.github.lajospolya.popularVote.dto.CitizenDto
import com.github.lajospolya.popularVote.dto.CitizenSelfDto
import com.github.lajospolya.popularVote.dto.CreateCitizenDto
import com.github.lajospolya.popularVote.dto.CreatePolicyDto
import com.github.lajospolya.popularVote.dto.DeclarePoliticianDto
import com.github.lajospolya.popularVote.dto.PageDto
import com.github.lajospolya.popularVote.dto.PolicyDto
import com.github.lajospolya.popularVote.dto.PolicySummaryDto
import com.github.lajospolya.popularVote.repository.CitizenPoliticalDetailsRepository
import com.github.lajospolya.popularVote.repository.CitizenRepository
import com.github.lajospolya.popularVote.service.Auth0ManagementService
import org.junit.jupiter.api.Assertions.assertEquals
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
class PolicyControllerIntegrationPaginationTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var auth0ManagementService: Auth0ManagementService

    @Autowired
    private lateinit var citizenRepository: CitizenRepository

    @Autowired
    private lateinit var citizenPoliticalDetailsRepository: CitizenPoliticalDetailsRepository

    @Test
    fun `verify stable sorting when filtering by level of politics`() {
        val authId = "auth-stable-sort-test"
        val citizenId = createCitizen(authId)
        declareSelfPolitician(authId, 1) // Federal
        verifyPolitician(citizenId)

        // Create 3 policies with the SAME creation date to test tie-breaker (id)
        val now = LocalDateTime.now().withNano(0)
        val policy1 = CreatePolicyDto("Same Date 1", emptyList(), now.plusDays(3), now)
        val policy2 = CreatePolicyDto("Same Date 2", emptyList(), now.plusDays(3), now)
        val policy3 = CreatePolicyDto("Same Date 3", emptyList(), now.plusDays(3), now)

        val createdIds = mutableListOf<Long>()
        listOf(policy1, policy2, policy3).forEach { dto ->
            val result =
                webTestClient
                    .mutateWith(mockJwt().jwt { it.subject(authId) }.authorities(SimpleGrantedAuthority("SCOPE_write:policies")))
                    .post()
                    .uri("/policies")
                    .bodyValue(dto)
                    .exchange()
                    .expectStatus()
                    .isOk
                    .expectBody<PolicyDto>()
                    .returnResult()
                    .responseBody!!
            createdIds.add(result.id)
            // Small delay to ensure they might get different IDs if they were really fast,
            // but the database handles that.
            Thread.sleep(10)
        }

        // Fetch with levelOfPolitics filter
        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(authId) }
                    .authorities(SimpleGrantedAuthority("SCOPE_read:policies")),
            ).get()
            .uri("/policies?levelOfPolitics=1")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody<PageDto<PolicySummaryDto>>()
            .consumeWith { result ->
                val page = result.responseBody!!
                // We want to see if they are in DESC order of ID since creation date is the same
                val contentIds = page.content.map { it.id }

                // Find our 3 policies in the result (there might be others from other tests)
                val relevantIds = contentIds.filter { it in createdIds }

                // Sorting should be DESC, so the latest created ID should come first.
                // Since they are created in order 1, 2, 3, they should appear as 3, 2, 1.
                val sortedCreatedIds = createdIds.sortedDescending()

                assertEquals(sortedCreatedIds, relevantIds, "Expected stable DESC sorting by ID")
            }
    }

    private fun verifyPolitician(citizenId: Long) {
        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { }
                    .authorities(
                        SimpleGrantedAuthority("SCOPE_write:verify-politician"),
                    ),
            ).put()
            .uri("/citizens/$citizenId/verify-politician")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody<CitizenSelfDto>()
            .returnResult()
    }

    private fun declareSelfPolitician(
        authId: String,
        levelOfPoliticsId: Int,
    ) {
        val declareSelfPoliticianDto =
            DeclarePoliticianDto(
                levelOfPoliticsId = levelOfPoliticsId,
                federalElectoralDistrictId = 1,
                politicalAffiliationId = 1,
            )

        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(authId) }
                    .authorities(
                        SimpleGrantedAuthority("SCOPE_write:declare-politician"),
                    ),
            ).post()
            .uri("/citizens/self/declare-politician")
            .bodyValue(declareSelfPoliticianDto)
            .exchange()
            .expectStatus()
            .isAccepted
            .expectBody<CitizenDto>()
            .returnResult()
            .status
    }

    private fun createCitizen(
        authId: String,
        givenName: String = "Publisher",
        surname: String = "Citizen",
    ): Long {
        val createCitizenDto =
            CreateCitizenDto(
                givenName = givenName,
                surname = surname,
                middleName = null,
            )

        whenever(auth0ManagementService.addRoleToUser(any(), any())).thenReturn(Mono.empty())

        return webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(authId) }
                    .authorities(SimpleGrantedAuthority("SCOPE_write:self")),
            ).post()
            .uri("/citizens/self")
            .bodyValue(createCitizenDto)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody<CitizenDto>()
            .returnResult()
            .responseBody
            ?.id!!
    }
}
