package com.github.lajospolya.popularVote.controller

import com.github.lajospolya.popularVote.AbstractIntegrationTest
import com.github.lajospolya.popularVote.dto.CitizenDto
import com.github.lajospolya.popularVote.dto.CitizenSelfDto
import com.github.lajospolya.popularVote.dto.CreateCitizenDto
import com.github.lajospolya.popularVote.dto.CreatePolicyDto
import com.github.lajospolya.popularVote.dto.CreatePoliticalPartyDto
import com.github.lajospolya.popularVote.dto.DeclarePoliticianDto
import com.github.lajospolya.popularVote.dto.PolicySummaryDto
import com.github.lajospolya.popularVote.dto.PoliticalPartyDto
import com.github.lajospolya.popularVote.entity.Role
import com.github.lajospolya.popularVote.service.Auth0ManagementService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
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
import org.springframework.test.web.reactive.server.expectBodyList
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@AutoConfigureWebTestClient
class PoliticalPartyControllerIntegrationTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var auth0ManagementService: Auth0ManagementService

    @BeforeEach
    fun setUp() {
        whenever(auth0ManagementService.addRoleToUser(any(), any())).thenReturn(Mono.empty())
        whenever(auth0ManagementService.removeRoleFromUser(any(), any())).thenReturn(Mono.empty())
    }

    @Test
    fun `create, fetch, update and delete political party`() {
        val createDto =
            CreatePoliticalPartyDto(
                displayName = "New Political Party",
                hexColor = "#123456",
                description = "A brand new party",
                levelOfPoliticsId = 1,
                provinceAndTerritoryId = 5,
            )

        // 1. Create
        val createdParty =
            webTestClient
                .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_write:political-parties")))
                .post()
                .uri("/political-parties")
                .bodyValue(createDto)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<PoliticalPartyDto>()
                .returnResult()
                .responseBody!!

        assertNotNull(createdParty.id)
        assertEquals(createDto.displayName, createdParty.displayName)
        assertEquals(createDto.hexColor, createdParty.hexColor)
        assertEquals(createDto.description, createdParty.description)
        assertEquals(createDto.provinceAndTerritoryId, createdParty.provinceAndTerritoryId)

        // 2. Fetch by ID
        val fetchedParty =
            webTestClient
                .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:political-parties")))
                .get()
                .uri("/political-parties/${createdParty.id}")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<PoliticalPartyDto>()
                .returnResult()
                .responseBody!!

        assertEquals(createdParty.id, fetchedParty.id)

        // 3. Update
        val updateDto =
            CreatePoliticalPartyDto(
                displayName = "Updated Political Party",
                hexColor = "#654321",
                description = "An updated description",
                levelOfPoliticsId = 2,
                provinceAndTerritoryId = 1,
            )

        val updatedParty =
            webTestClient
                .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_write:political-parties")))
                .put()
                .uri("/political-parties/${createdParty.id}")
                .bodyValue(updateDto)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<PoliticalPartyDto>()
                .returnResult()
                .responseBody!!

        assertEquals(createdParty.id, updatedParty.id)
        assertEquals(updateDto.displayName, updatedParty.displayName)
        assertEquals(updateDto.hexColor, updatedParty.hexColor)
        assertEquals(updateDto.description, updatedParty.description)
        assertEquals(updateDto.provinceAndTerritoryId, updatedParty.provinceAndTerritoryId)

        // 4. Delete
        webTestClient
            .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_delete:political-parties")))
            .delete()
            .uri("/political-parties/${createdParty.id}")
            .exchange()
            .expectStatus()
            .isOk

        // 5. Verify deleted
        webTestClient
            .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:political-parties")))
            .get()
            .uri("/political-parties/${createdParty.id}")
            .exchange()
            .expectStatus()
            .isNotFound
    }

    @Test
    fun `get all political parties`() {
        val parties =
            webTestClient
                .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:political-parties")))
                .get()
                .uri("/political-parties")
                .exchange()
                .expectStatus()
                .isOk
                .expectBodyList(PoliticalPartyDto::class.java)
                .returnResult()
                .responseBody!!

        // There should be at least the 6 seeded parties
        assertNotNull(parties)
        assert(parties.size >= 6)
    }

    @Test
    fun `get political parties by province and territory ID`() {
        // Liberal Party (ID 1), Conservative Party (ID 2), NDP (ID 3), Green (ID 4), Bloc (ID 5), PPC (ID 6) are federal (level 1)
        // seeded parties from V24:
        // BC New Democratic Party, Conservative Party of British Columbia, BC Green Party have province_and_territory_id = 1
        // Progressive Conservative Party of Ontario, Ontario New Democratic Party, Ontario Liberal Party, Green Party of Ontario have province_and_territory_id = 5

        val provinceId = 5
        val parties =
            webTestClient
                .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:political-parties")))
                .get()
                .uri("/political-parties?provinceAndTerritoryId=$provinceId")
                .exchange()
                .expectStatus()
                .isOk
                .expectBodyList(PoliticalPartyDto::class.java)
                .returnResult()
                .responseBody!!

        assertNotNull(parties)
        assert(parties.isNotEmpty())
        assert(parties.all { it.provinceAndTerritoryId == provinceId })
        // There should be at least the 4 seeded Ontario parties
        assert(parties.size >= 4)
    }

    @Test
    fun `get political parties by level and province and territory ID`() {
        val levelId = 2L
        val provinceId = 1
        val parties =
            webTestClient
                .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:political-parties")))
                .get()
                .uri("/political-parties?levelOfPolitics=$levelId&provinceAndTerritoryId=$provinceId")
                .exchange()
                .expectStatus()
                .isOk
                .expectBodyList(PoliticalPartyDto::class.java)
                .returnResult()
                .responseBody!!

        assertNotNull(parties)
        assert(parties.all { it.levelOfPoliticsId == levelId && it.provinceAndTerritoryId == provinceId })
    }

    @Test
    fun `get political party members`() {
        val authId = "auth-party-members"
        val createCitizenDto =
            CreateCitizenDto(
                givenName = "Justin",
                surname = "Trudeau",
                middleName = "Pierre",
            )

        // 1. Create Citizen
        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(authId) },
            ).post()
            .uri("/citizens/self")
            .bodyValue(createCitizenDto)
            .exchange()
            .expectStatus()
            .isOk

        // 2. Declare Politician
        val declareSelfPoliticianDto =
            DeclarePoliticianDto(
                levelOfPoliticsId = 1,
                electoralDistrictId = 1,
                politicalAffiliationId = 1,
            )

        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(authId) }
                    .authorities(SimpleGrantedAuthority("SCOPE_write:declare-politician")),
            ).post()
            .uri("/citizens/self/declare-politician")
            .bodyValue(declareSelfPoliticianDto)
            .exchange()
            .expectStatus()
            .isAccepted

        // 3. Verify Politician
        // We need to find the citizen ID first
        val citizen =
            webTestClient
                .mutateWith(
                    mockJwt()
                        .jwt { it.subject(authId) },
                ).get()
                .uri("/citizens/self")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<CitizenSelfDto>()
                .returnResult()
                .responseBody!!

        webTestClient
            .mutateWith(
                mockJwt()
                    .authorities(SimpleGrantedAuthority("SCOPE_write:verify-politician")),
            ).put()
            .uri("/citizens/${citizen.id}/verify-politician")
            .exchange()
            .expectStatus()
            .isOk

        // 4. Fetch Liberal Party members (ID 1)
        val members =
            webTestClient
                .mutateWith(
                    mockJwt().authorities(
                        SimpleGrantedAuthority("SCOPE_read:political-parties"),
                        SimpleGrantedAuthority("SCOPE_read:citizens"),
                    ),
                ).get()
                .uri("/political-parties/1/members")
                .exchange()
                .expectStatus()
                .isOk
                .expectBodyList<CitizenDto>()
                .returnResult()
                .responseBody!!

        assertNotNull(members)
        // Justin Trudeau should be in the list
        assert(members.any { it.givenName == "Justin" && it.surname == "Trudeau" })
        // All members should be politicians
        assert(members.all { it.role == Role.POLITICIAN })
    }

    @Test
    fun `get political party policies`() {
        val authId = "auth-party-policies"
        val createCitizenDto =
            CreateCitizenDto(
                givenName = "Politician",
                surname = "Publisher",
                middleName = null,
            )

        // 1. Create Citizen
        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(authId) },
            ).post()
            .uri("/citizens/self")
            .bodyValue(createCitizenDto)
            .exchange()
            .expectStatus()
            .isOk

        // 2. Declare and Verify Politician (to be sure they are a valid publisher and role is set)
        val self =
            webTestClient
                .mutateWith(mockJwt().jwt { it.subject(authId) })
                .get()
                .uri("/citizens/self")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<CitizenSelfDto>()
                .returnResult()
                .responseBody!!

        val declareSelfPoliticianDto =
            DeclarePoliticianDto(
                levelOfPoliticsId = 1,
                electoralDistrictId = 1,
                politicalAffiliationId = 1,
            )

        webTestClient
            .mutateWith(mockJwt().jwt { it.subject(authId) }.authorities(SimpleGrantedAuthority("SCOPE_write:declare-politician")))
            .post()
            .uri("/citizens/self/declare-politician")
            .bodyValue(declareSelfPoliticianDto)
            .exchange()
            .expectStatus()
            .isAccepted

        webTestClient
            .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_write:verify-politician")))
            .put()
            .uri("/citizens/${self.id}/verify-politician")
            .exchange()
            .expectStatus()
            .isOk

        // 3. Create Policy
        val createPolicyDto =
            CreatePolicyDto(
                description = "Test policy for political party",
                coAuthorCitizenIds = emptyList(),
                LocalDateTime.now(),
            )

        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(authId) }
                    .authorities(SimpleGrantedAuthority("SCOPE_write:policies")),
            ).post()
            .uri("/policies")
            .bodyValue(createPolicyDto)
            .exchange()
            .expectStatus()
            .isOk

        // 4. Fetch Liberal Party policies (ID 1)
        webTestClient
            .mutateWith(
                mockJwt().authorities(
                    SimpleGrantedAuthority("SCOPE_read:political-parties"),
                    SimpleGrantedAuthority("SCOPE_read:policies"),
                ),
            ).get()
            .uri("/political-parties/1/policies")
            .exchange()
            .expectStatus()
            .isOk
            .expectBodyList<PolicySummaryDto>()
            .consumeWith<WebTestClient.ListBodySpec<PolicySummaryDto>> { result ->
                val policies = result.responseBody
                assertNotNull(policies)
            }
    }
}
