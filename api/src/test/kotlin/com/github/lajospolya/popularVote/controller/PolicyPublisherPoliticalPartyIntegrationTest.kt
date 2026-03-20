package com.github.lajospolya.popularVote.controller

import com.github.lajospolya.popularVote.AbstractIntegrationTest
import com.github.lajospolya.popularVote.dto.CitizenDto
import com.github.lajospolya.popularVote.dto.CreateCitizenDto
import com.github.lajospolya.popularVote.dto.CreatePolicyDto
import com.github.lajospolya.popularVote.dto.DeclarePoliticianDto
import com.github.lajospolya.popularVote.dto.PageDto
import com.github.lajospolya.popularVote.dto.PolicySummaryDto
import com.github.lajospolya.popularVote.repository.CitizenRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import java.time.LocalDateTime

@AutoConfigureWebTestClient
class PolicyPublisherPoliticalPartyIntegrationTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var citizenRepo: CitizenRepository

    @Test
    fun `get policies filtered by publisher political party`() {
        val authIdParty1 = "auth-party-1-publisher"
        val authIdParty2 = "auth-party-2-publisher"

        val citizenParty1 = createCitizen(authIdParty1, "Party1", "Publisher")
        val citizenParty2 = createCitizen(authIdParty2, "Party2", "Publisher")

        // Party IDs: 1 and 2 (assuming they exist in the test DB)
        declareSelfPolitician(authIdParty1, 1, 1)
        declareSelfPolitician(authIdParty2, 1, 2)

        // Create Policy for Party 1
        val policy1Dto =
            CreatePolicyDto(
                description = "Policy for Party 1",
                coAuthorCitizenIds = emptyList(),
                closeDate = LocalDateTime.now().plusDays(30),
            )
        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(authIdParty1) }
                    .authorities(SimpleGrantedAuthority("SCOPE_read:policies"), SimpleGrantedAuthority("SCOPE_write:policies")),
            ).post()
            .uri("/policies")
            .bodyValue(policy1Dto)
            .exchange()
            .expectStatus()
            .isOk

        // Create Policy for Party 2
        val policy2Dto =
            CreatePolicyDto(
                description = "Policy for Party 2",
                coAuthorCitizenIds = emptyList(),
                closeDate = LocalDateTime.now().plusDays(30),
            )
        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(authIdParty2) }
                    .authorities(SimpleGrantedAuthority("SCOPE_read:policies"), SimpleGrantedAuthority("SCOPE_write:policies")),
            ).post()
            .uri("/policies")
            .bodyValue(policy2Dto)
            .exchange()
            .expectStatus()
            .isOk

        // 1. Filter by Party 1
        val party1Policies =
            webTestClient
                .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:policies")))
                .get()
                .uri("/policies?publisher-political-party=1&size=100")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<PageDto<PolicySummaryDto>>()
                .returnResult()
                .responseBody!!

        assert(party1Policies.content.any { it.description == "Policy for Party 1" })
        assert(party1Policies.content.none { it.description == "Policy for Party 2" })

        // 2. Filter by Party 2
        val party2Policies =
            webTestClient
                .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:policies")))
                .get()
                .uri("/policies?publisher-political-party=2&size=100")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<PageDto<PolicySummaryDto>>()
                .returnResult()
                .responseBody!!

        assert(party2Policies.content.any { it.description == "Policy for Party 2" })
        assert(party2Policies.content.none { it.description == "Policy for Party 1" })
    }

    private fun declareSelfPolitician(
        authId: String,
        levelOfPoliticsId: Int,
        politicalAffiliationId: Int,
    ) {
        val declarePoliticianDto =
            DeclarePoliticianDto(
                levelOfPoliticsId = levelOfPoliticsId,
                electoralDistrictId = 1, // Default district
                politicalAffiliationId = politicalAffiliationId,
            )
        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(authId) }
                    .authorities(SimpleGrantedAuthority("SCOPE_write:declare-politician")),
            ).post()
            .uri("/citizens/self/declare-politician")
            .bodyValue(declarePoliticianDto)
            .exchange()
            .expectStatus()
            .isAccepted
    }

    private fun createCitizen(
        authId: String,
        givenName: String,
        surname: String,
    ): Long {
        val createCitizenDto =
            CreateCitizenDto(
                givenName = givenName,
                surname = surname,
                middleName = null,
            )
        return webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(authId) }
                    .authorities(SimpleGrantedAuthority("SCOPE_write:citizens")),
            ).post()
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
}
