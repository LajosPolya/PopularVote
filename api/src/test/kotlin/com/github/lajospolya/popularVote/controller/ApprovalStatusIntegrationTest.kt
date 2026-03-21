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

    @Autowired
    private lateinit var template: org.springframework.data.r2dbc.core.R2dbcEntityTemplate

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
        val openCloseDate = now.plusDays(1)
        val closedCloseDate = now.minusDays(1)

        val approvedPolicyDto = CreatePolicyDto("Approved Policy", "Approved Policy", emptyList(), openCloseDate)
        val deniedPolicyDto = CreatePolicyDto("Denied Policy", "Denied Policy", emptyList(), openCloseDate)

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

        // Close policies to have approval status
        template.databaseClient
            .sql("UPDATE policy SET close_date = :closeDate WHERE id IN (:id1, :id2)")
            .bind("closeDate", closedCloseDate)
            .bind("id1", approvedPolicy.id)
            .bind("id2", deniedPolicy.id)
            .fetch()
            .rowsUpdated()
            .block()

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

    @Test
    fun `open policies with votes should not show up when filtering by approval status`() {
        val authId = "auth-open-policy-test"
        val citizenId = createCitizen(authId)
        testUtils.setupPoliticalDetailsForCitizen(citizenId)

        val now = LocalDateTime.now()
        val openCloseDate = now.plusDays(1)

        val openPolicyDto = CreatePolicyDto("Open Policy with Votes", "Open Policy with Votes", emptyList(), openCloseDate)

        // Create open policy
        val openPolicy =
            webTestClient
                .mutateWith(mockJwt().jwt { it.subject(authId) }.authorities(SimpleGrantedAuthority("SCOPE_write:policies")))
                .post()
                .uri("/policies")
                .bodyValue(openPolicyDto)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<PolicyDto>()
                .returnResult()
                .responseBody!!

        // Vote for Open Policy (all approve)
        vote(openPolicy.id, "voter-o-1", 1L) // approve
        vote(openPolicy.id, "voter-o-2", 1L) // approve

        // Test filtering by APPROVED
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

        // The open policy should NOT be returned even though it has more approve votes than disapprove
        assertTrue(approvedPolicies.content.none { it.id == openPolicy.id }, "Open policy should not be in approved list")

        // Test filtering by DENIED
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

        assertTrue(deniedPolicies.content.none { it.id == openPolicy.id }, "Open policy should not be in denied list")
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
