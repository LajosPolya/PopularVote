package com.github.lajospolya.popularVote.controller

import com.github.lajospolya.popularVote.AbstractIntegrationTest
import com.github.lajospolya.popularVote.dto.CreatePolicyDto
import com.github.lajospolya.popularVote.dto.PageDto
import com.github.lajospolya.popularVote.dto.PolicyDto
import com.github.lajospolya.popularVote.dto.PolicySummaryDto
import com.github.lajospolya.popularVote.dto.VoteDto
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
class ApprovalStatusIntegrationTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var auth0ManagementService: Auth0ManagementService

    @Autowired
    private lateinit var citizenPoliticalDetailsRepository: CitizenPoliticalDetailsRepository

    private val testUtils by lazy { TestUtils(webTestClient, auth0ManagementService, citizenPoliticalDetailsRepository) }

    @BeforeEach
    fun setUp() {
        whenever(auth0ManagementService.addRoleToUser(any(), any())).thenReturn(Mono.empty())
    }

    private fun createCitizen(authId: String): Long = testUtils.createCitizen(authId, "Voter", authId)

    @Test
    fun `filter policies by approval status`() {
        val authId = "auth-approval-status-test"
        val citizenId = createCitizen(authId)
        testUtils.setupPoliticalDetailsForCitizen(citizenId)

        val now = LocalDateTime.now()
        val closeDate = now.plusDays(1)

        val approvedPolicyDto = CreatePolicyDto("Approved Policy", emptyList(), closeDate)
        val deniedPolicyDto = CreatePolicyDto("Denied Policy", emptyList(), closeDate)

        // Create approved policy
        val approvedPolicy =
            webTestClient
                .mutateWith(mockJwt().jwt { it.subject(authId) }.authorities(SimpleGrantedAuthority("SCOPE_write:policies")))
                .post()
                .uri("/policies")
                .bodyValue(approvedPolicyDto)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<PolicyDto>()
                .returnResult()
                .responseBody!!

        // Create denied policy
        val deniedPolicy =
            webTestClient
                .mutateWith(mockJwt().jwt { it.subject(authId) }.authorities(SimpleGrantedAuthority("SCOPE_write:policies")))
                .post()
                .uri("/policies")
                .bodyValue(deniedPolicyDto)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<PolicyDto>()
                .returnResult()
                .responseBody!!

        // Vote for Approved Policy (2 approve, 1 disapprove)
        // From database/dev/seed_data.sql: 1=approve, 2=disapprove
        vote(approvedPolicy.id, "voter-a-1", 1L) // approve
        vote(approvedPolicy.id, "voter-a-2", 1L) // approve
        vote(approvedPolicy.id, "voter-a-3", 2L) // disapprove

        // Vote for Denied Policy (1 approve, 2 disapprove)
        vote(deniedPolicy.id, "voter-d-1", 1L) // approve
        vote(deniedPolicy.id, "voter-d-2", 2L) // disapprove
        vote(deniedPolicy.id, "voter-d-3", 2L) // disapprove

        // Test approvalStatus=APPROVED
        val approvedPolicies =
            webTestClient
                .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:policies")))
                .get()
                .uri("/policies?approvalStatus=APPROVED&size=100")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<PageDto<PolicySummaryDto>>()
                .returnResult()
                .responseBody!!

        assertTrue(approvedPolicies.content.any { it.description == "Approved Policy" })
        assertTrue(approvedPolicies.content.none { it.description == "Denied Policy" })

        // Test approvalStatus=DENIED
        val deniedPolicies =
            webTestClient
                .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:policies")))
                .get()
                .uri("/policies?approvalStatus=DENIED&size=100")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<PageDto<PolicySummaryDto>>()
                .returnResult()
                .responseBody!!

        assertTrue(deniedPolicies.content.any { it.description == "Denied Policy" })
        assertTrue(deniedPolicies.content.none { it.description == "Approved Policy" })
    }

    private fun vote(
        policyId: Long,
        authId: String,
        selectionId: Long,
    ) {
        createCitizen(authId)
        val voteDto = VoteDto(policyId = policyId, selectionId = selectionId)
        webTestClient
            .mutateWith(mockJwt().jwt { it.subject(authId) }.authorities(SimpleGrantedAuthority("SCOPE_write:votes")))
            .post()
            .uri("/votes")
            .bodyValue(voteDto)
            .exchange()
            .expectStatus()
            .isOk
    }
}
