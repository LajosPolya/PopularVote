package com.github.lajospolya.popularVote.controller

import com.github.lajospolya.popularVote.AbstractIntegrationTest
import com.github.lajospolya.popularVote.dto.CitizenDto
import com.github.lajospolya.popularVote.dto.CitizenProfileDto
import com.github.lajospolya.popularVote.dto.CitizenSelfDto
import com.github.lajospolya.popularVote.dto.CreateCitizenDto
import com.github.lajospolya.popularVote.dto.CreatePolicyDto
import com.github.lajospolya.popularVote.dto.DeclarePoliticianDto
import com.github.lajospolya.popularVote.dto.PolicyDto
import com.github.lajospolya.popularVote.dto.VerifyIdentityDto
import com.github.lajospolya.popularVote.dto.VoteDto
import com.github.lajospolya.popularVote.entity.Citizen
import com.github.lajospolya.popularVote.entity.CitizenPoliticalDetails
import com.github.lajospolya.popularVote.entity.Role
import com.github.lajospolya.popularVote.service.Auth0ManagementService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito

import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@AutoConfigureWebTestClient
class CitizenControllerIntegrationTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var auth0ManagementService: Auth0ManagementService

    @Autowired
    private lateinit var r2dbcEntityTemplate: R2dbcEntityTemplate

    @BeforeEach
    fun setUp() {
        Mockito.reset(auth0ManagementService)
        whenever(auth0ManagementService.addRoleToUser(any(), any())).thenReturn(Mono.empty())
        whenever(auth0ManagementService.removeRoleFromUser(any(), any())).thenReturn(Mono.empty())
    }

    @Test
    fun `create citizen and then fetch it by self`() {
        val authId = "auth-123"
        val createCitizenDto =
            CreateCitizenDto(
                givenName = "John",
                surname = "Doe",
                middleName = "Quincy",
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
            .expectBody<CitizenProfileDto>()
            .consumeWith { result ->
                val fetched = result.responseBody!!
                assertEquals(id, fetched.id)
                assertEquals(0L, fetched.policyCount)
                assertEquals(0L, fetched.voteCount)
            }

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
    fun `verify identity`() {
        val authId = "auth-update-pc"
        val createCitizenDto =
            CreateCitizenDto(
                givenName = "Update",
                surname = "PostalCode",
                middleName = null,
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

        val verifyIdentityDto = VerifyIdentityDto(postalCodeId = 1)

        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(authId) }
                    .authorities(SimpleGrantedAuthority("SCOPE_write:self")),
            ).put()
            .uri("/citizens/self/verify-identity")
            .bodyValue(verifyIdentityDto)
            .exchange()
            .expectStatus()
            .isOk

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
                .expectBody<CitizenSelfDto>()
                .returnResult()
                .responseBody

        assertNotNull(fetchedCitizen)
        assertEquals(1, fetchedCitizen?.postalCodeId)
        assertNotNull(fetchedCitizen?.postalCode)
        assertEquals("V8V", fetchedCitizen?.postalCode?.name)

        verify(auth0ManagementService).addRoleToUser(eq(authId), eq("test-role-id"))
        verify(auth0ManagementService).addRoleToUser(eq(authId), eq("test-read-only-role-id"))
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

        // 1.1 Declare Politician (to have political details)
        val declarePoliticianDto =
            DeclarePoliticianDto(
                levelOfPoliticsId = 1,
                geographicLocation = "Canada",
                politicalAffiliationId = 1,
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

        // 2. Create 2 Policies
        repeat(2) { i ->
            webTestClient
                .mutateWith(
                    mockJwt()
                        .jwt { it.subject(authId) }
                        .authorities(SimpleGrantedAuthority("SCOPE_write:policies")),
                ).post()
                .uri("/policies")
                .bodyValue(CreatePolicyDto(description = "Policy $i", coAuthorCitizenIds = emptyList(), LocalDateTime.now()))
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

        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject("other-auth") }
                    .authorities(SimpleGrantedAuthority("SCOPE_write:declare-politician")),
            ).post()
            .uri("/citizens/self/declare-politician")
            .bodyValue(declarePoliticianDto)
            .exchange()
            .expectStatus()
            .isAccepted

        val otherPolicy =
            webTestClient
                .mutateWith(
                    mockJwt()
                        .jwt { it.subject("other-auth") }
                        .authorities(SimpleGrantedAuthority("SCOPE_write:policies")),
                ).post()
                .uri("/policies")
                .bodyValue(CreatePolicyDto(description = "Other Policy", coAuthorCitizenIds = emptyList(), LocalDateTime.now().plusDays(1)))
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
        assertEquals(true, fetchedCitizen?.isVerificationPending)
    }

    @Test
    fun `create citizen with roleId updates user role in Auth0`() {
        val authId = "auth-role-123"
        val createCitizenDto =
            CreateCitizenDto(
                givenName = "Role",
                surname = "User",
                middleName = null,
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
        val declarePoliticianDto =
            DeclarePoliticianDto(
                levelOfPoliticsId = 1,
                geographicLocation = "Canada",
                politicalAffiliationId = 6,
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
    fun `get politicians returns only citizens with politician role`() {
        val authId = "auth-politician-search"
        val createCitizenDto =
            CreateCitizenDto(
                givenName = "Politician",
                surname = "Search",
                middleName = null,
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
                .expectBody<CitizenDto>()
                .returnResult()
                .responseBody!!

        val citizenId = createdCitizen.id!!

        // 2. Verify not in politicians list
        webTestClient
            .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:citizens")))
            .get()
            .uri("/citizens/politicians")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody<List<CitizenDto>>()
            .value { politicians ->
                assert(politicians.none { it.id == citizenId })
            }

        // 3. Declare Politician
        val declarePoliticianDto =
            DeclarePoliticianDto(
                levelOfPoliticsId = 1,
                geographicLocation = "Canada",
                politicalAffiliationId = 6,
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

        // 4. Verify Politician
        webTestClient
            .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_write:verify-politician")))
            .put()
            .uri("/citizens/$citizenId/verify-politician")
            .exchange()
            .expectStatus()
            .isOk

        // 5. Verify in politicians list
        webTestClient
            .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:citizens")))
            .get()
            .uri("/citizens/politicians")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody<List<CitizenDto>>()
            .value { politicians ->
                assert(politicians.any { it.id == citizenId })
                val politician = politicians.find { it.id == citizenId }!!
                assertEquals(createCitizenDto.givenName, politician.givenName)
                assertEquals(createCitizenDto.surname, politician.surname)
                assertEquals(Role.POLITICIAN, politician.role)
            }
    }

    @Test
    fun `verify politician updates role and Auth0`() {
        val authId = "auth-verify-123"
        val createCitizenDto =
            CreateCitizenDto(
                givenName = "Verify",
                surname = "Me",
                middleName = null,
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
                .expectBody<CitizenDto>()
                .returnResult()
                .responseBody!!

        val citizenId = createdCitizen.id!!

        // 2. Declare Politician (to put them in the verification table)
        val declarePoliticianDto =
            DeclarePoliticianDto(
                levelOfPoliticsId = 1,
                geographicLocation = "Canada",
                politicalAffiliationId = 6,
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
                .expectBody<CitizenDto>()
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
        val declarePoliticianDto =
            DeclarePoliticianDto(
                levelOfPoliticsId = 1,
                geographicLocation = "Canada",
                politicalAffiliationId = 6,
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

    @Test
    fun `get citizen profile returns levelOfPoliticsName when present`() {
        // 1. Create Citizen first
        val citizen =
            Citizen(
                givenName = "Politician",
                surname = "WithLevel",
                middleName = null,
                role = Role.POLITICIAN,
                authId = "auth-politician-with-level",
            )
        val savedCitizen = r2dbcEntityTemplate.insert(citizen).block()!!

        // 2. Create CitizenPoliticalDetails linked to citizen (Level 1 is Federal from seed_data.sql)
        val details =
            CitizenPoliticalDetails(
                citizenId = savedCitizen.id!!,
                levelOfPoliticsId = 1,
                geographicLocation = "Canada",
                politicalPartyId = 1,
            )
        r2dbcEntityTemplate.insert(details).block()!!

        // 3. Fetch profile and verify
        webTestClient
            .mutateWith(
                mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:citizens")),
            ).get()
            .uri("/citizens/${savedCitizen.id}")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(CitizenProfileDto::class.java)
            .consumeWith { result ->
                val profile = result.responseBody!!
                assertEquals(savedCitizen.id, profile.id)
                assertEquals("Politician", profile.givenName)
                assertEquals("Federal", profile.levelOfPoliticsName)
            }
    }

    @Test
    fun `filter politicians by level of politics`() {
        whenever(auth0ManagementService.addRoleToUser(anyString(), anyString())).thenReturn(Mono.empty())
        whenever(auth0ManagementService.removeRoleFromUser(anyString(), anyString())).thenReturn(Mono.empty())

        // Create two citizens with different levels of politics
        val federalAuthId = "auth-federal-politician"
        val provincialAuthId = "auth-provincial-politician"

        val federalCitizenDto =
            CreateCitizenDto(
                givenName = "Federal",
                surname = "Politician",
                middleName = null,
            )

        val provincialCitizenDto =
            CreateCitizenDto(
                givenName = "Provincial",
                surname = "Politician",
                middleName = null,
            )

        // Create federal citizen
        val federalCitizen =
            webTestClient
                .mutateWith(mockJwt().jwt { it.subject(federalAuthId) })
                .post()
                .uri("/citizens/self")
                .bodyValue(federalCitizenDto)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<CitizenDto>()
                .returnResult()
                .responseBody!!

        // Create provincial citizen
        val provincialCitizen =
            webTestClient
                .mutateWith(mockJwt().jwt { it.subject(provincialAuthId) })
                .post()
                .uri("/citizens/self")
                .bodyValue(provincialCitizenDto)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<CitizenDto>()
                .returnResult()
                .responseBody!!

        // Declare federal politician
        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(federalAuthId) }
                    .authorities(SimpleGrantedAuthority("SCOPE_write:declare-politician")),
            ).post()
            .uri("/citizens/self/declare-politician")
            .bodyValue(
                DeclarePoliticianDto(
                    levelOfPoliticsId = 1,
                    geographicLocation = "Canada",
                    politicalAffiliationId = 2,
                ),
            ).exchange()
            .expectStatus()
            .isAccepted

        // Declare provincial politician
        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(provincialAuthId) }
                    .authorities(SimpleGrantedAuthority("SCOPE_write:declare-politician")),
            ).post()
            .uri("/citizens/self/declare-politician")
            .bodyValue(
                DeclarePoliticianDto(
                    levelOfPoliticsId = 2,
                    geographicLocation = "Ontario",
                    politicalAffiliationId = 2,
                ),
            ).exchange()
            .expectStatus()
            .isAccepted

        // Verify federal politician
        webTestClient
            .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_write:verify-politician")))
            .put()
            .uri("/citizens/${federalCitizen.id}/verify-politician")
            .exchange()
            .expectStatus()
            .isOk

        // Verify provincial politician
        webTestClient
            .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_write:verify-politician")))
            .put()
            .uri("/citizens/${provincialCitizen.id}/verify-politician")
            .exchange()
            .expectStatus()
            .isOk

        // Get all politicians - should contain both
        val allPoliticians =
            webTestClient
                .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:citizens")))
                .get()
                .uri("/citizens/politicians")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<List<CitizenDto>>()
                .returnResult()
                .responseBody!!

        val allPoliticianIds = allPoliticians.map { it.id }
        assert(allPoliticianIds.contains(federalCitizen.id))
        assert(allPoliticianIds.contains(provincialCitizen.id))

        // Filter by federal level (levelOfPolitics=1)
        val federalPoliticians =
            webTestClient
                .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:citizens")))
                .get()
                .uri("/citizens/politicians?levelOfPolitics=1")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<List<CitizenDto>>()
                .returnResult()
                .responseBody!!

        val federalPoliticianIds = federalPoliticians.map { it.id }
        assert(federalPoliticianIds.contains(federalCitizen.id))
        assert(!federalPoliticianIds.contains(provincialCitizen.id))

        // Filter by provincial level (levelOfPolitics=2)
        val provincialPoliticians =
            webTestClient
                .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:citizens")))
                .get()
                .uri("/citizens/politicians?levelOfPolitics=2")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<List<CitizenDto>>()
                .returnResult()
                .responseBody!!

        val provincialPoliticianIds = provincialPoliticians.map { it.id }
        assert(!provincialPoliticianIds.contains(federalCitizen.id))
        assert(provincialPoliticianIds.contains(provincialCitizen.id))
    }

    @Test
    fun `get citizen profile returns null levelOfPoliticsName when not present`() {
        // Create a new citizen without political details
        val authId = "auth-no-politics"
        val createCitizenDto =
            CreateCitizenDto(
                givenName = "Regular",
                surname = "Citizen",
                middleName = null,
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
                .responseBody!!

        webTestClient
            .mutateWith(
                mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:citizens")),
            ).get()
            .uri("/citizens/${createdCitizen.id}")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(CitizenProfileDto::class.java)
            .consumeWith { result ->
                val profile = result.responseBody!!
                assertEquals(createdCitizen.id, profile.id)
                assertEquals("Regular", profile.givenName)
                assertEquals(null, profile.levelOfPoliticsName)
            }
    }
}
