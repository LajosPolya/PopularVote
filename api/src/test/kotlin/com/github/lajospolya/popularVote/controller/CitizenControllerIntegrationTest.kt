package com.github.lajospolya.popularVote.controller

import com.github.lajospolya.popularVote.AbstractIntegrationTest
import com.github.lajospolya.popularVote.dto.CitizenDto
import com.github.lajospolya.popularVote.dto.CitizenSelfDto
import com.github.lajospolya.popularVote.dto.CreateCitizenDto
import com.github.lajospolya.popularVote.dto.CreatePolicyDto
import com.github.lajospolya.popularVote.dto.PolicyDto
import com.github.lajospolya.popularVote.dto.VoteDto
import com.github.lajospolya.popularVote.entity.PoliticalAffiliation
import com.github.lajospolya.popularVote.entity.Role
import com.github.lajospolya.popularVote.service.Auth0ManagementService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import reactor.core.publisher.Mono

@AutoConfigureWebTestClient
class CitizenControllerIntegrationTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var auth0ManagementService: Auth0ManagementService

    @BeforeEach
    fun setUp() {
        whenever(auth0ManagementService.addRoleToUser(any(), any())).thenReturn(Mono.empty())
    }

    @Test
    fun `create citizen and then fetch it by self`() {
        val authId = "auth-123"
        val createCitizenDto =
            CreateCitizenDto(
                givenName = "John",
                surname = "Doe",
                middleName = "Quincy",
                politicalAffiliation = PoliticalAffiliation.LIBERAL_PARTY_OF_CANADA,
            )

        whenever(auth0ManagementService.addRoleToUser(anyString(), anyString())).thenReturn(Mono.empty())

        val createdCitizen =
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
                .expectBody<CitizenDto>()
                .returnResult()
                .responseBody

        assertNotNull(createdCitizen)
        assertNotNull(createdCitizen?.id)
        assertEquals(createCitizenDto.givenName, createdCitizen?.givenName)
        assertEquals(createCitizenDto.surname, createdCitizen?.surname)
        assertEquals(createCitizenDto.middleName, createdCitizen?.middleName)
        assertEquals(createCitizenDto.politicalAffiliation, createdCitizen?.politicalAffiliation)
        assertEquals(Role.CITIZEN, createdCitizen?.role)

        val fetchedCitizen =
            webTestClient
                .mutateWith(
                    mockJwt()
                        .jwt { it.subject(authId) },
                ).get()
                .uri("/citizens/self")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(CitizenSelfDto::class.java)
                .returnResult()
                .responseBody

        assertNotNull(fetchedCitizen)
        assertEquals(createCitizenDto.givenName, fetchedCitizen?.givenName)
        assertEquals(createCitizenDto.surname, fetchedCitizen?.surname)
        assertEquals(createCitizenDto.middleName, fetchedCitizen?.middleName)
        assertEquals(createCitizenDto.politicalAffiliation, fetchedCitizen?.politicalAffiliation)
        assertEquals(Role.CITIZEN, fetchedCitizen?.role)
        assertEquals(0L, fetchedCitizen?.policyCount)
        assertEquals(0L, fetchedCitizen?.voteCount)
        assertEquals(false, fetchedCitizen?.isVerificationPending)
    }

    @Test
    fun `create citizen, verify exists, delete it, and verify deleted`() {
        val authId = "auth-456"
        val createCitizenDto =
            CreateCitizenDto(
                givenName = "Jane",
                surname = "Smith",
                middleName = null,
                politicalAffiliation = PoliticalAffiliation.CONSERVATIVE_PARTY_OF_CANADA,
            )

        val createdCitizen =
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
                .expectBody<CitizenDto>()
                .returnResult()
                .responseBody

        val id = createdCitizen?.id
        assertNotNull(id)

        webTestClient
            .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:citizens")))
            .get()
            .uri("/citizens/$id")
            .exchange()
            .expectStatus()
            .isOk

        webTestClient
            .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_delete:citizens")))
            .delete()
            .uri("/citizens/$id")
            .exchange()
            .expectStatus()
            .isOk

        webTestClient
            .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:citizens")))
            .get()
            .uri("/citizens/$id")
            .exchange()
            .expectStatus()
            .isNotFound
    }

    @Test
    fun `head citizen by authId returns 204 when exists and 404 when not`() {
        val authId = "auth-head-test"
        val createCitizenDto =
            CreateCitizenDto(
                givenName = "Head",
                surname = "Test",
                middleName = null,
                politicalAffiliation = PoliticalAffiliation.INDEPENDENT,
            )

        // 1. Verify 404 before creation
        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(authId) },
            ).head()
            .uri("/citizens/self")
            .exchange()
            .expectStatus()
            .isNotFound

        // 2. Create citizen
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

        // 3. Verify 204 after creation
        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(authId) },
            ).head()
            .uri("/citizens/self")
            .exchange()
            .expectStatus()
            .isNoContent

        // 4. Verify 404 for non-existent authId (by using a different JWT subject)
        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject("non-existent-auth") },
            ).head()
            .uri("/citizens/self")
            .exchange()
            .expectStatus()
            .isNotFound
    }

    @Test
    fun `create two citizens and verify count increases`() {
        val initialCount =
            webTestClient
                .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:citizens")))
                .get()
                .uri("/citizens")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<List<CitizenDto>>()
                .returnResult()
                .responseBody
                ?.size ?: 0

        val authId1 = "auth-789"
        val citizen1 =
            CreateCitizenDto(
                givenName = "First",
                surname = "Citizen",
                middleName = null,
                politicalAffiliation = PoliticalAffiliation.GREEN_PARTY_OF_CANADA,
            )
        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(authId1) },
            ).post()
            .uri("/citizens/self")
            .bodyValue(citizen1)
            .exchange()
            .expectStatus()
            .isOk

        webTestClient
            .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:citizens")))
            .get()
            .uri("/citizens")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody<List<CitizenDto>>()
            .consumeWith { result ->
                assertEquals(initialCount + 1, result.responseBody?.size)
            }

        val authId2 = "auth-012"
        val citizen2 =
            CreateCitizenDto(
                givenName = "Second",
                surname = "Citizen",
                middleName = null,
                politicalAffiliation = PoliticalAffiliation.BLOC_QUEBECOIS,
            )
        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(authId2) },
            ).post()
            .uri("/citizens/self")
            .bodyValue(citizen2)
            .exchange()
            .expectStatus()
            .isOk

        webTestClient
            .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:citizens")))
            .get()
            .uri("/citizens")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody<List<CitizenDto>>()
            .consumeWith { result ->
                assertEquals(initialCount + 2, result.responseBody?.size)
            }
    }

    @Test
    fun `create citizen and search by name`() {
        val authId = "auth-345"
        val createCitizenDto =
            CreateCitizenDto(
                givenName = "Alice",
                surname = "Wonderland",
                middleName = "In",
                politicalAffiliation = PoliticalAffiliation.NEW_DEMOCRATIC_PARTY,
            )

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

        val searchedCitizen =
            webTestClient
                .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:citizens")))
                .get()
                .uri("/citizens/search?givenName=Alice&surname=Wonderland")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<CitizenDto>()
                .returnResult()
                .responseBody

        assertNotNull(searchedCitizen)
        assertEquals(createCitizenDto.givenName, searchedCitizen?.givenName)
        assertEquals(createCitizenDto.surname, searchedCitizen?.surname)
        assertEquals(createCitizenDto.politicalAffiliation, searchedCitizen?.politicalAffiliation)

        webTestClient
            .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:citizens")))
            .get()
            .uri("/citizens/search?givenName=Non&surname=Existent")
            .exchange()
            .expectStatus()
            .isNotFound
    }

    @Test
    fun `get self returns policy and vote counts`() {
        val authId = "auth-counts-123"
        val createCitizenDto =
            CreateCitizenDto(
                givenName = "Count",
                surname = "Tester",
                middleName = null,
                politicalAffiliation = PoliticalAffiliation.INDEPENDENT,
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

        // 2. Create 2 Policies
        repeat(2) { i ->
            webTestClient
                .mutateWith(
                    mockJwt()
                        .jwt { it.subject(authId) }
                        .authorities(SimpleGrantedAuthority("SCOPE_write:policies")),
                ).post()
                .uri("/policies")
                .bodyValue(CreatePolicyDto(description = "Policy $i"))
                .exchange()
                .expectStatus()
                .isOk
        }

        // 3. Create 1 Policy from another citizen (should not be counted)
        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject("other-auth") }
                    .authorities(SimpleGrantedAuthority("SCOPE_write:self")),
            ).post()
            .uri("/citizens/self")
            .bodyValue(createCitizenDto.copy(givenName = "Other"))
            .exchange()
            .expectStatus()
            .isOk
        val otherPolicy =
            webTestClient
                .mutateWith(
                    mockJwt()
                        .jwt { it.subject("other-auth") }
                        .authorities(SimpleGrantedAuthority("SCOPE_write:policies")),
                ).post()
                .uri("/policies")
                .bodyValue(CreatePolicyDto(description = "Other Policy"))
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(PolicyDto::class.java)
                .returnResult()
                .responseBody!!

        // 4. Vote for 1 Policy
        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(authId) }
                    .authorities(SimpleGrantedAuthority("SCOPE_write:votes")),
            ).post()
            .uri("/votes")
            .bodyValue(VoteDto(policyId = otherPolicy.id, selectionId = 1L)) // 1L is likely 'approve'
            .exchange()
            .expectStatus()
            .isOk

        // 5. Verify counts
        val fetchedCitizen =
            webTestClient
                .mutateWith(
                    mockJwt()
                        .jwt { it.subject(authId) },
                ).get()
                .uri("/citizens/self")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(CitizenSelfDto::class.java)
                .returnResult()
                .responseBody

        assertNotNull(fetchedCitizen)
        assertEquals(2L, fetchedCitizen?.policyCount)
        assertEquals(1L, fetchedCitizen?.voteCount)
        assertEquals(false, fetchedCitizen?.isVerificationPending)
    }

    @Test
    fun `create citizen with roleId updates user role in Auth0`() {
        val authId = "auth-role-123"
        val createCitizenDto =
            CreateCitizenDto(
                givenName = "Role",
                surname = "User",
                middleName = null,
                politicalAffiliation = PoliticalAffiliation.INDEPENDENT,
            )

        whenever(auth0ManagementService.addRoleToUser(eq(authId), any())).thenReturn(Mono.empty())

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

        verify(auth0ManagementService).addRoleToUser(eq(authId), any())
    }

    @Test
    fun `declare politician writes to verification table`() {
        val authId = "auth-politician-123"
        val createCitizenDto =
            CreateCitizenDto(
                givenName = "Future",
                surname = "Politician",
                middleName = null,
                politicalAffiliation = PoliticalAffiliation.INDEPENDENT,
            )

        whenever(auth0ManagementService.addRoleToUser(anyString(), anyString())).thenReturn(Mono.empty())

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
        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(authId) }
                    .authorities(SimpleGrantedAuthority("SCOPE_write:declare-politician")),
            ).post()
            .uri("/citizens/self/declare-politician")
            .exchange()
            .expectStatus()
            .isAccepted

        // Verify that the citizen role remains until verified
        val fetchedCitizen =
            webTestClient
                .mutateWith(
                    mockJwt()
                        .jwt { it.subject(authId) },
                ).get()
                .uri("/citizens/self")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(CitizenSelfDto::class.java)
                .returnResult()
                .responseBody

        assertEquals(Role.CITIZEN, fetchedCitizen?.role)
        assertEquals(true, fetchedCitizen?.isVerificationPending)
    }

    @Test
    fun `verify politician updates role and Auth0`() {
        val authId = "auth-verify-123"
        val createCitizenDto =
            CreateCitizenDto(
                givenName = "Verify",
                surname = "Me",
                middleName = null,
                politicalAffiliation = PoliticalAffiliation.INDEPENDENT,
            )

        whenever(auth0ManagementService.addRoleToUser(anyString(), anyString())).thenReturn(Mono.empty())
        whenever(auth0ManagementService.removeRoleFromUser(anyString(), anyString())).thenReturn(Mono.empty())

        // 1. Create Citizen
        val createdCitizen =
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
                .expectBody(CitizenDto::class.java)
                .returnResult()
                .responseBody!!

        val citizenId = createdCitizen.id!!

        // 2. Declare Politician (to put them in the verification table)
        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(authId) }
                    .authorities(SimpleGrantedAuthority("SCOPE_write:declare-politician")),
            ).post()
            .uri("/citizens/self/declare-politician")
            .exchange()
            .expectStatus()
            .isAccepted

        // 3. Verify Politician
        val updatedCitizen =
            webTestClient
                .mutateWith(
                    mockJwt()
                        .authorities(SimpleGrantedAuthority("SCOPE_write:verify-politician")),
                ).put()
                .uri("/citizens/$citizenId/verify-politician")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(CitizenSelfDto::class.java)
                .returnResult()
                .responseBody

        assertNotNull(updatedCitizen)
        assertEquals(Role.POLITICIAN, updatedCitizen?.role)
        assertEquals(false, updatedCitizen?.isVerificationPending)

        // 4. Verify Auth0 calls
        // addRoleToUser: once for CITIZEN, once for POLITICIAN
        verify(auth0ManagementService, org.mockito.Mockito.times(2)).addRoleToUser(eq(authId), anyString())
        verify(auth0ManagementService).removeRoleFromUser(eq(authId), anyString())

        // 5. Verify they are no longer in the verification list
        webTestClient
            .mutateWith(
                mockJwt()
                    .authorities(SimpleGrantedAuthority("SCOPE_read:verify-politician")),
            ).get()
            .uri("/citizens/verify-politician")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody<List<CitizenDto>>()
            .consumeWith { result ->
                val list = result.responseBody
                assertNotNull(list)
                val found = list!!.any { it.id == citizenId }
                assertEquals(false, found)
            }
    }

    @Test
    fun `verify politician fails if already a politician`() {
        val authId = "auth-already-politician"
        val createCitizenDto =
            CreateCitizenDto(
                givenName = "Already",
                surname = "Politician",
                middleName = null,
                politicalAffiliation = PoliticalAffiliation.INDEPENDENT,
            )

        whenever(auth0ManagementService.addRoleToUser(anyString(), anyString())).thenReturn(Mono.empty())
        whenever(auth0ManagementService.removeRoleFromUser(anyString(), anyString())).thenReturn(Mono.empty())

        // 1. Create Citizen
        val createdCitizen =
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
                .expectBody(CitizenDto::class.java)
                .returnResult()
                .responseBody!!

        val citizenId = createdCitizen.id!!

        // 2. Verify Politician first time
        webTestClient
            .mutateWith(
                mockJwt()
                    .authorities(SimpleGrantedAuthority("SCOPE_write:verify-politician")),
            ).put()
            .uri("/citizens/$citizenId/verify-politician")
            .exchange()
            .expectStatus()
            .isOk

        // 3. Verify Politician second time (should fail)
        webTestClient
            .mutateWith(
                mockJwt()
                    .authorities(SimpleGrantedAuthority("SCOPE_write:verify-politician")),
            ).put()
            .uri("/citizens/$citizenId/verify-politician")
            .exchange()
            .expectStatus()
            .is5xxServerError
    }

    @Test
    fun `verify politician requires write verify-politician scope`() {
        webTestClient
            .mutateWith(
                mockJwt(),
                // No authorities
            ).put()
            .uri("/citizens/1/verify-politician")
            .exchange()
            .expectStatus()
            .isForbidden
    }

    @Test
    fun `get politician verifications returns pending citizens`() {
        val authId = "auth-verify-pending"
        val createCitizenDto =
            CreateCitizenDto(
                givenName = "Pending",
                surname = "Verification",
                middleName = null,
                politicalAffiliation = PoliticalAffiliation.INDEPENDENT,
            )

        whenever(auth0ManagementService.addRoleToUser(anyString(), anyString())).thenReturn(Mono.empty())

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
        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(authId) }
                    .authorities(SimpleGrantedAuthority("SCOPE_write:declare-politician")),
            ).post()
            .uri("/citizens/self/declare-politician")
            .exchange()
            .expectStatus()
            .isAccepted

        // 3. Get verifications
        webTestClient
            .mutateWith(
                mockJwt()
                    .authorities(SimpleGrantedAuthority("SCOPE_read:verify-politician")),
            ).get()
            .uri("/citizens/verify-politician")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody<List<CitizenDto>>()
            .consumeWith { result ->
                val list = result.responseBody
                assertNotNull(list)
                val found = list!!.any { it.givenName == "Pending" && it.surname == "Verification" }
                assertEquals(true, found)
            }
    }
}
