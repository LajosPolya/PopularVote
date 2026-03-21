package com.github.lajospolya.popularVote.controller

import com.github.lajospolya.popularVote.dto.CitizenDto
import com.github.lajospolya.popularVote.dto.CitizenSelfDto
import com.github.lajospolya.popularVote.dto.CreateCitizenDto
import com.github.lajospolya.popularVote.dto.DeclarePoliticianDto
import com.github.lajospolya.popularVote.entity.CitizenPoliticalDetails
import com.github.lajospolya.popularVote.repository.CitizenPoliticalDetailsRepository
import com.github.lajospolya.popularVote.service.Auth0ManagementService
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import reactor.core.publisher.Mono

class TestUtils(
    private val webTestClient: WebTestClient,
    private val auth0ManagementService: Auth0ManagementService?,
    private val citizenPoliticalDetailsRepository: CitizenPoliticalDetailsRepository?,
) {
    fun createCitizen(
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

        auth0ManagementService?.let {
            whenever(it.addRoleToUser(any(), any())).thenReturn(Mono.empty())
        }

        return webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(authId) }
                    .authorities(
                        SimpleGrantedAuthority("SCOPE_write:self"),
                        SimpleGrantedAuthority("SCOPE_write:citizens"),
                    ),
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

    fun declareSelfPolitician(
        authId: String,
        levelOfPoliticsId: Int,
        politicalAffiliationId: Int = 1,
        electoralDistrictId: Int = 1,
    ) {
        val declareSelfPoliticianDto =
            DeclarePoliticianDto(
                levelOfPoliticsId = levelOfPoliticsId,
                electoralDistrictId = electoralDistrictId,
                politicalAffiliationId = politicalAffiliationId,
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
    }

    fun verifyPolitician(citizenId: Long) {
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

    fun setupPoliticalDetailsForCitizen(
        citizenId: Long,
        levelOfPoliticsId: Int = 1,
        electoralDistrictId: Int = 1,
        politicalPartyId: Int = 1,
    ) {
        citizenPoliticalDetailsRepository
            ?.save(
                CitizenPoliticalDetails(
                    citizenId = citizenId,
                    levelOfPoliticsId = levelOfPoliticsId,
                    electoralDistrictId = electoralDistrictId,
                    politicalPartyId = politicalPartyId,
                ),
            )?.block()!!
    }
}
