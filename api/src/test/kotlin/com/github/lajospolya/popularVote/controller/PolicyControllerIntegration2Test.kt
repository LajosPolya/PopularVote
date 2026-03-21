package com.github.lajospolya.popularVote.controller

import com.github.lajospolya.popularVote.AbstractIntegrationTest
import com.github.lajospolya.popularVote.dto.CitizenDto
import com.github.lajospolya.popularVote.dto.CitizenSelfDto
import com.github.lajospolya.popularVote.dto.CreateCitizenDto
import com.github.lajospolya.popularVote.dto.CreatePolicyDto
import com.github.lajospolya.popularVote.dto.DeclarePoliticianDto
import com.github.lajospolya.popularVote.dto.PageDto
import com.github.lajospolya.popularVote.dto.PolicySummaryDto
import com.github.lajospolya.popularVote.entity.CitizenPoliticalDetails
import com.github.lajospolya.popularVote.repository.CitizenPoliticalDetailsRepository
import com.github.lajospolya.popularVote.repository.CitizenRepository
import com.github.lajospolya.popularVote.service.Auth0ManagementService
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
class PolicyControllerIntegration2Test : AbstractIntegrationTest() {
    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var auth0ManagementService: Auth0ManagementService

    @Autowired
    private lateinit var citizenRepository: CitizenRepository

    @Autowired
    private lateinit var template: org.springframework.data.r2dbc.core.R2dbcEntityTemplate

    @Autowired
    private lateinit var citizenPoliticalDetailsRepository: CitizenPoliticalDetailsRepository

    private fun setupPoliticalDetailsForCitizen(citizenId: Long) {
        citizenPoliticalDetailsRepository
            .save(
                CitizenPoliticalDetails(
                    citizenId = citizenId,
                    levelOfPoliticsId = 1, // Federal
                    electoralDistrictId = 1,
                    politicalPartyId = 1,
                ),
            ).block()!!
    }

    @Test
    fun `get policies by province and territory ID`() {
        val authIdBC = "auth-bc-publisher"
        val authIdON = "auth-on-publisher"

        // BC is ID 1, Ontario is ID 5
        val citizenBC = createCitizen(authIdBC, "BC", "Publisher")
        val citizenON = createCitizen(authIdON, "ON", "Publisher")

        // Declare BC citizen as provincial politician (level 2) in BC (district 1 is BC, province 1)
        declareSelfPolitician(authIdBC, 2)
        verifyPolitician(citizenBC)

        // Declare ON citizen as provincial politician (level 2) in ON (district 101 is ON, province 5)
        val declareOnPoliticianDto =
            DeclarePoliticianDto(
                levelOfPoliticsId = 2,
                electoralDistrictId = 67,
                politicalAffiliationId = 7, // Progressive Conservative Party of Ontario
            )
        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(authIdON) }
                    .authorities(SimpleGrantedAuthority("SCOPE_write:declare-politician")),
            ).post()
            .uri("/citizens/self/declare-politician")
            .bodyValue(declareOnPoliticianDto)
            .exchange()
            .expectStatus()
            .isAccepted
        verifyPolitician(citizenON)

        // Create BC Policy
        val bcPolicyDto =
            CreatePolicyDto(
                title = "BC Policy",
                description = "BC Provincial Policy",
                coAuthorCitizenIds = emptyList(),
                closeDate = LocalDateTime.now().plusDays(30),
            )
        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(authIdBC) }
                    .authorities(SimpleGrantedAuthority("SCOPE_read:policies"), SimpleGrantedAuthority("SCOPE_write:policies")),
            ).post()
            .uri("/policies")
            .bodyValue(bcPolicyDto)
            .exchange()
            .expectStatus()
            .isOk

        // Create ON Policy
        val onPolicyDto =
            CreatePolicyDto(
                title = "ON Policy",
                description = "ON Provincial Policy",
                coAuthorCitizenIds = emptyList(),
                closeDate = LocalDateTime.now().plusDays(30),
            )
        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(authIdON) }
                    .authorities(SimpleGrantedAuthority("SCOPE_read:policies"), SimpleGrantedAuthority("SCOPE_write:policies")),
            ).post()
            .uri("/policies")
            .bodyValue(onPolicyDto)
            .exchange()
            .expectStatus()
            .isOk

        // Close policies to allow them to be found if any filter would check it
        template.databaseClient
            .sql("UPDATE policy SET close_date = :closeDate")
            .bind("closeDate", LocalDateTime.now().minusDays(1))
            .fetch()
            .rowsUpdated()
            .block()

        // 1. Filter by BC
        val bcPolicies =
            webTestClient
                .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:policies")))
                .get()
                .uri("/policies?provinceAndTerritoryId=1")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<PageDto<PolicySummaryDto>>()
                .returnResult()
                .responseBody!!

        assert(bcPolicies.content.any { it.description == "BC Provincial Policy" })
        assert(bcPolicies.content.none { it.description == "ON Provincial Policy" })

        // 2. Filter by ON
        val onPolicies =
            webTestClient
                .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:policies")))
                .get()
                .uri("/policies?provinceAndTerritoryId=5")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<PageDto<PolicySummaryDto>>()
                .returnResult()
                .responseBody!!

        assert(onPolicies.content.any { it.description == "ON Provincial Policy" })
        assert(onPolicies.content.none { it.description == "BC Provincial Policy" })

        // 3. Filter by Level and Province
        val bcProvincialPolicies =
            webTestClient
                .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:policies")))
                .get()
                .uri("/policies?levelOfPolitics=2&provinceAndTerritoryId=1")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<PageDto<PolicySummaryDto>>()
                .returnResult()
                .responseBody!!

        assert(bcProvincialPolicies.content.any { it.description == "BC Provincial Policy" })
    }

    private fun declareSelfPolitician(
        authId: String,
        levelOfPoliticsId: Int,
    ) {
        val declareSelfPoliticianDto =
            DeclarePoliticianDto(
                levelOfPoliticsId = levelOfPoliticsId,
                electoralDistrictId = 1,
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
}
