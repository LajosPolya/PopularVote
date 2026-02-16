package com.github.lajospolya.popularVote.controller

import com.github.lajospolya.popularVote.AbstractIntegrationTest
import com.github.lajospolya.popularVote.dto.CitizenDto
import com.github.lajospolya.popularVote.dto.CitizenSelfDto
import com.github.lajospolya.popularVote.dto.CreateCitizenDto
import com.github.lajospolya.popularVote.dto.CreateOpinionDto
import com.github.lajospolya.popularVote.dto.CreatePolicyDto
import com.github.lajospolya.popularVote.dto.DeclarePoliticianDto
import com.github.lajospolya.popularVote.dto.PolicyDetailsDto
import com.github.lajospolya.popularVote.dto.PolicyDto
import com.github.lajospolya.popularVote.dto.PolicySummaryDto
import com.github.lajospolya.popularVote.entity.CitizenPoliticalDetails
import com.github.lajospolya.popularVote.repository.CitizenPoliticalDetailsRepository
import com.github.lajospolya.popularVote.repository.CitizenRepository
import com.github.lajospolya.popularVote.service.Auth0ManagementService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
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
class PolicyControllerIntegrationTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var auth0ManagementService: Auth0ManagementService

    @Autowired
    private lateinit var citizenRepository: CitizenRepository

    @Autowired
    private lateinit var citizenPoliticalDetailsRepository: CitizenPoliticalDetailsRepository

    private fun setupPoliticalDetailsForCitizen(citizenId: Long) {
        citizenPoliticalDetailsRepository
            .save(
                CitizenPoliticalDetails(
                    citizenId = citizenId,
                    levelOfPoliticsId = 1, // Federal
                    geographicLocation = "Canada",
                    politicalPartyId = 1,
                ),
            ).block()!!
    }

    @Test
    fun `create policy and then fetch it`() {
        val authId = "auth-policy-1"
        val citizenId = createCitizen(authId)
        // Set political details for the citizen so they can create a policy
        setupPoliticalDetailsForCitizen(citizenId)

        val createPolicyDto =
            CreatePolicyDto(
                description = "Test Policy Description",
                coAuthorCitizenIds = emptyList(),
                LocalDateTime.now(),
            )

        val createdPolicy =
            webTestClient
                .mutateWith(mockJwt().jwt { it.subject(authId) }.authorities(SimpleGrantedAuthority("SCOPE_write:policies")))
                .post()
                .uri("/policies")
                .bodyValue(createPolicyDto)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<PolicyDto>()
                .returnResult()
                .responseBody

        assertNotNull(createdPolicy)
        assertNotNull(createdPolicy?.id)
        assertEquals(createPolicyDto.description, createdPolicy?.description)
        assertEquals(citizenId, createdPolicy?.publisherCitizenId)

        val fetchedPolicy =
            webTestClient
                .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:policies")))
                .get()
                .uri("/policies/${createdPolicy?.id}")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(PolicyDto::class.java)
                .returnResult()
                .responseBody

        assertNotNull(fetchedPolicy)
        assertEquals(createdPolicy?.id, fetchedPolicy?.id)
        assertEquals(createPolicyDto.description, fetchedPolicy?.description)
        assertEquals(citizenId, fetchedPolicy?.publisherCitizenId)
    }

    @Test
    fun `create policy, verify exists, delete it, and verify deleted`() {
        val authId = "auth-policy-2"
        val citizenId = createCitizen(authId)
        setupPoliticalDetailsForCitizen(citizenId)
        val createPolicyDto =
            CreatePolicyDto(
                description = "Policy to be deleted",
                coAuthorCitizenIds = emptyList(),
                LocalDateTime.now(),
            )

        val createdPolicy =
            webTestClient
                .mutateWith(mockJwt().jwt { it.subject(authId) }.authorities(SimpleGrantedAuthority("SCOPE_write:policies")))
                .post()
                .uri("/policies")
                .bodyValue(createPolicyDto)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<PolicyDto>()
                .returnResult()
                .responseBody

        val id = createdPolicy?.id
        assertNotNull(id)

        webTestClient
            .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:policies")))
            .get()
            .uri("/policies/$id")
            .exchange()
            .expectStatus()
            .isOk

        webTestClient
            .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_delete:policies")))
            .delete()
            .uri("/policies/$id")
            .exchange()
            .expectStatus()
            .isOk

        webTestClient
            .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:policies")))
            .get()
            .uri("/policies/$id")
            .exchange()
            .expectStatus()
            .isNotFound
    }

    @Test
    fun `create policy and then fetch its details`() {
        val authId = "auth-policy-details"
        val citizenId = createCitizen(authId)
        declareSelfPolitician(authId, 1) // Federal
        verifyPolitician(citizenId)
        val createPolicyDto =
            CreatePolicyDto(
                description = "Policy for Details Test",
                coAuthorCitizenIds = emptyList(),
                LocalDateTime.now(),
            )

        val createdPolicy =
            webTestClient
                .mutateWith(mockJwt().jwt { it.subject(authId) }.authorities(SimpleGrantedAuthority("SCOPE_write:policies")))
                .post()
                .uri("/policies")
                .bodyValue(createPolicyDto)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<PolicyDto>()
                .returnResult()
                .responseBody!!

        val fetchedDetails =
            webTestClient
                .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:policies")))
                .get()
                .uri("/policies/${createdPolicy.id}/details")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<PolicyDetailsDto>()
                .returnResult()
                .responseBody

        assertNotNull(fetchedDetails)
        assertEquals(createdPolicy.id, fetchedDetails?.id)
        assertEquals(createPolicyDto.description, fetchedDetails?.description)
        assertEquals("Publisher Citizen", fetchedDetails?.publisherName)
        assertEquals(1, fetchedDetails?.publisherPoliticalAffiliationId)
        assertNotNull(fetchedDetails?.opinions)
    }

    @Test
    fun `get policies returns 403 when missing read policies scope`() {
        webTestClient
            .mutateWith(mockJwt())
            .get()
            .uri("/policies")
            .exchange()
            .expectStatus()
            .isForbidden
    }

    @Test
    fun `create two policies and verify count increases`() {
        val authId = "auth-policy-3"
        val citizenId = createCitizen(authId)
        setupPoliticalDetailsForCitizen(citizenId)
        val initialCount =
            webTestClient
                .mutateWith(
                    mockJwt()
                        .jwt { it.subject(authId) }
                        .authorities(SimpleGrantedAuthority("SCOPE_read:policies")),
                ).get()
                .uri("/policies")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<List<PolicySummaryDto>>()
                .returnResult()
                .responseBody
                ?.size ?: 0

        val policy1 =
            CreatePolicyDto(
                description = "First Policy",
                coAuthorCitizenIds = emptyList(),
                LocalDateTime.now(),
            )
        val createdPolicy =
            webTestClient
                .mutateWith(mockJwt().jwt { it.subject(authId) }.authorities(SimpleGrantedAuthority("SCOPE_write:policies")))
                .post()
                .uri("/policies")
                .bodyValue(policy1)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<PolicyDto>()
                .returnResult()
                .responseBody!!

        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(authId) }
                    .authorities(SimpleGrantedAuthority("SCOPE_read:policies")),
            ).get()
            .uri("/policies")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody<List<PolicySummaryDto>>()
            .consumeWith { result ->
                val policies = result.responseBody!!
                val policy = policies.find { it.id == createdPolicy.id }
                assertNotNull(policy)
                assertEquals(false, policy?.isBookmarked)
            }

        val policy2 =
            CreatePolicyDto(
                description = "Second Policy",
                coAuthorCitizenIds = emptyList(),
                LocalDateTime.now(),
            )
        webTestClient
            .mutateWith(mockJwt().jwt { it.subject(authId) }.authorities(SimpleGrantedAuthority("SCOPE_write:policies")))
            .post()
            .uri("/policies")
            .bodyValue(policy2)
            .exchange()
            .expectStatus()
            .isOk

        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(authId) }
                    .authorities(SimpleGrantedAuthority("SCOPE_read:policies")),
            ).get()
            .uri("/policies")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody<List<PolicySummaryDto>>()
            .consumeWith { result ->
                assertEquals(initialCount + 2, result.responseBody?.size)
            }
    }

    @Test
    fun `create policy, add opinion, and fetch details`() {
        val policyAuthId = "auth-policy-opinion-details"
        val publisherId = createCitizen(policyAuthId)
        setupPoliticalDetailsForCitizen(publisherId)
        val createPolicyDto = CreatePolicyDto(description = "Policy with Opinion", coAuthorCitizenIds = emptyList(), LocalDateTime.now())
        val policy =
            webTestClient
                .mutateWith(mockJwt().jwt { it.subject(policyAuthId) }.authorities(SimpleGrantedAuthority("SCOPE_write:policies")))
                .post()
                .uri("/policies")
                .bodyValue(createPolicyDto)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<PolicyDto>()
                .returnResult()
                .responseBody!!

        val opinionAuthId = "auth-opinion-author-details"
        val opinionCitizenId =
            createCitizen(
                opinionAuthId,
                "Opinion",
                "Author",
            )
        declareSelfPolitician(opinionAuthId, 1) // Federal
        verifyPolitician(opinionCitizenId)
        val createOpinionDto =
            CreateOpinionDto(
                description = "Opinion Description",
                policyId = policy.id,
            )
        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt {
                        it.subject(opinionAuthId)
                    }.authorities(SimpleGrantedAuthority("SCOPE_read:policies"), SimpleGrantedAuthority("SCOPE_write:opinions")),
            ).post()
            .uri("/opinions")
            .bodyValue(createOpinionDto)
            .exchange()
            .expectStatus()
            .isOk

        val fetchedDetails =
            webTestClient
                .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:policies")))
                .get()
                .uri("/policies/${policy.id}/details")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<PolicyDetailsDto>()
                .returnResult()
                .responseBody!!

        assertEquals(1, fetchedDetails.opinions.size)
        val opinion = fetchedDetails.opinions[0]
        assertEquals("Opinion Description", opinion.description)
        assertEquals("Opinion Author", opinion.authorName)
        assertEquals(
            1,
            opinion.authorPoliticalAffiliationId,
        )
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
                geographicLocation = "Waterloo, Ontario, Canada",
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

    @Test
    fun `create policy with co-authors and fetch policy and details`() {
        val publisherAuth = "auth-policy-coauthors-publisher"
        val publisherId = createCitizen(publisherAuth)
        setupPoliticalDetailsForCitizen(publisherId)

        val co1Id =
            createCitizen(
                authId = "auth-coauthor-1",
                givenName = "Co",
                surname = "AuthorOne",
            )
        val co2Id =
            createCitizen(
                authId = "auth-coauthor-2",
                givenName = "Co",
                surname = "AuthorTwo",
            )

        val createPolicyDto =
            CreatePolicyDto(
                description = "Policy with CoAuthors",
                coAuthorCitizenIds = listOf(co1Id, co2Id),
                LocalDateTime.now(),
            )

        val createdPolicy =
            webTestClient
                .mutateWith(
                    mockJwt()
                        .jwt { it.subject(publisherAuth) }
                        .authorities(SimpleGrantedAuthority("SCOPE_write:policies")),
                ).post()
                .uri("/policies")
                .bodyValue(createPolicyDto)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<PolicyDto>()
                .returnResult()
                .responseBody!!

        // Verify co-authors in creation response
        assertEquals(2, createdPolicy.coAuthorCitizens.size)
        val returnedIds = createdPolicy.coAuthorCitizens.map { it.id }.toSet()
        assertEquals(setOf(co1Id, co2Id), returnedIds)

        // Verify single fetch includes co-authors
        val fetchedPolicy =
            webTestClient
                .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:policies")))
                .get()
                .uri("/policies/${createdPolicy.id}")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(PolicyDto::class.java)
                .returnResult()
                .responseBody!!

        assertEquals(2, fetchedPolicy.coAuthorCitizens.size)
        val fetchedIds = fetchedPolicy.coAuthorCitizens.map { it.id }.toSet()
        assertEquals(setOf(co1Id, co2Id), fetchedIds)

        // Verify details endpoint includes co-authors
        val details =
            webTestClient
                .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:policies")))
                .get()
                .uri("/policies/${createdPolicy.id}/details")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(PolicyDetailsDto::class.java)
                .returnResult()
                .responseBody!!

        assertEquals(2, details.coAuthorCitizens.size)
        val detailsIds = details.coAuthorCitizens.map { it.id }.toSet()
        assertEquals(setOf(co1Id, co2Id), detailsIds)
    }

    @Test
    fun `fetch policies by citizen id`() {
        val authId = "auth-citizen-policies"
        val citizenId = createCitizen(authId)
        setupPoliticalDetailsForCitizen(citizenId)

        val createPolicyDto1 =
            CreatePolicyDto(
                description = "Policy 1",
                coAuthorCitizenIds = emptyList(),
                LocalDateTime.now(),
            )
        val createPolicyDto2 =
            CreatePolicyDto(
                description = "Policy 2",
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
            .bodyValue(createPolicyDto1)
            .exchange()
            .expectStatus()
            .isOk

        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(authId) }
                    .authorities(SimpleGrantedAuthority("SCOPE_write:policies")),
            ).post()
            .uri("/policies")
            .bodyValue(createPolicyDto2)
            .exchange()
            .expectStatus()
            .isOk

        val citizenPolicies =
            webTestClient
                .mutateWith(
                    mockJwt()
                        .authorities(SimpleGrantedAuthority("SCOPE_read:policies")),
                ).get()
                .uri("/citizens/$citizenId/policies")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<List<PolicySummaryDto>>()
                .returnResult()
                .responseBody!!

        assertEquals(2, citizenPolicies.size)
        val descriptions = citizenPolicies.map { it.description }.toSet()
        assertEquals(setOf("Policy 1", "Policy 2"), descriptions)
    }

    @Test
    fun `bookmark and get bookmarked policies`() {
        val citizenAuth = "auth-bookmark-citizen"
        val citizenId = createCitizen(citizenAuth)
        setupPoliticalDetailsForCitizen(citizenId)

        val policyDto =
            CreatePolicyDto(
                description = "Bookmarked Policy",
                coAuthorCitizenIds = emptyList(),
                LocalDateTime.now(),
            )

        val createdPolicy =
            webTestClient
                .mutateWith(
                    mockJwt()
                        .jwt { it.subject(citizenAuth) }
                        .authorities(SimpleGrantedAuthority("SCOPE_write:policies")),
                ).post()
                .uri("/policies")
                .bodyValue(policyDto)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<PolicyDto>()
                .returnResult()
                .responseBody!!

        // Bookmark the policy
        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(citizenAuth) }
                    .authorities(SimpleGrantedAuthority("SCOPE_write:self")),
            ).post()
            .uri("/policies/${createdPolicy.id}/bookmark")
            .exchange()
            .expectStatus()
            .isNoContent

        // Get bookmarked policies
        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(citizenAuth) }
                    .authorities(SimpleGrantedAuthority("SCOPE_read:self"), SimpleGrantedAuthority("SCOPE_read:policies")),
            ).get()
            .uri("/policies/bookmarks")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody<List<PolicySummaryDto>>()
            .consumeWith { result ->
                val bookmarks = result.responseBody!!
                assertEquals(1, bookmarks.size)
                assertEquals(createdPolicy.id, bookmarks[0].id)
                assertEquals("Bookmarked Policy", bookmarks[0].description)
                assertEquals("Publisher Citizen", bookmarks[0].publisherName)
                assertEquals(true, bookmarks[0].isBookmarked)
            }

        // Check if bookmarked via new endpoint
        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(citizenAuth) }
                    .authorities(SimpleGrantedAuthority("SCOPE_read:self"), SimpleGrantedAuthority("SCOPE_read:policies")),
            ).get()
            .uri("/policies/${createdPolicy.id}/is-bookmarked")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody<Boolean>()
            .isEqualTo(true)

        // Check a non-bookmarked policy
        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(citizenAuth) }
                    .authorities(SimpleGrantedAuthority("SCOPE_read:self"), SimpleGrantedAuthority("SCOPE_read:policies")),
            ).get()
            .uri("/policies/999/is-bookmarked")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody<Boolean>()
            .isEqualTo(false)

        // Delete the bookmark
        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(citizenAuth) }
                    .authorities(SimpleGrantedAuthority("SCOPE_write:self"), SimpleGrantedAuthority("SCOPE_read:policies")),
            ).delete()
            .uri("/policies/${createdPolicy.id}/bookmark")
            .exchange()
            .expectStatus()
            .isNoContent

        // Verify it's no longer bookmarked
        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(citizenAuth) }
                    .authorities(SimpleGrantedAuthority("SCOPE_read:self"), SimpleGrantedAuthority("SCOPE_read:policies")),
            ).get()
            .uri("/policies/${createdPolicy.id}/is-bookmarked")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody<Boolean>()
            .isEqualTo(false)
    }

    @Test
    fun `filter policies by level of politics`() {
        // Create citizens with different levels of politics
        val federalAuthId = "auth-federal-policy"
        val provincialAuthId = "auth-provincial-policy"

        val federalCitizenId = createCitizen(federalAuthId, "Federal", "Politician")
        declareSelfPolitician(federalAuthId, 1) // Federal
        verifyPolitician(federalCitizenId)
        val provincialCitizenId = createCitizen(provincialAuthId, "Provincial", "Politician")
        declareSelfPolitician(provincialAuthId, 2) // Provincial
        verifyPolitician(provincialCitizenId)

        // Create federal policy
        val federalPolicyDto =
            CreatePolicyDto(
                description = "Federal Policy",
                coAuthorCitizenIds = emptyList(),
                LocalDateTime.now(),
            )

        val federalPolicy =
            webTestClient
                .mutateWith(
                    mockJwt()
                        .jwt { it.subject(federalAuthId) }
                        .authorities(SimpleGrantedAuthority("SCOPE_write:policies")),
                ).post()
                .uri("/policies")
                .bodyValue(federalPolicyDto)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<PolicyDto>()
                .returnResult()
                .responseBody!!

        // Create provincial policy
        val provincialPolicyDto =
            CreatePolicyDto(
                description = "Provincial Policy",
                coAuthorCitizenIds = emptyList(),
                LocalDateTime.now(),
            )

        val provincialPolicy =
            webTestClient
                .mutateWith(
                    mockJwt()
                        .jwt { it.subject(provincialAuthId) }
                        .authorities(SimpleGrantedAuthority("SCOPE_write:policies")),
                ).post()
                .uri("/policies")
                .bodyValue(provincialPolicyDto)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<PolicyDto>()
                .returnResult()
                .responseBody!!

        // Get all policies - should contain both
        val allPolicies =
            webTestClient
                .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:policies")))
                .get()
                .uri("/policies")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<List<PolicySummaryDto>>()
                .returnResult()
                .responseBody!!

        val allPolicyIds = allPolicies.map { it.id }
        assert(allPolicyIds.contains(federalPolicy.id))
        assert(allPolicyIds.contains(provincialPolicy.id))

        // Filter by federal level (levelOfPolitics=1)
        val federalPolicies =
            webTestClient
                .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:policies")))
                .get()
                .uri("/policies?levelOfPolitics=1")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<List<PolicySummaryDto>>()
                .returnResult()
                .responseBody!!

        val federalPolicyIds = federalPolicies.map { it.id }
        assert(federalPolicyIds.contains(federalPolicy.id))
        assert(!federalPolicyIds.contains(provincialPolicy.id))

        // Filter by provincial level (levelOfPolitics=2)
        val provincialPolicies =
            webTestClient
                .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:policies")))
                .get()
                .uri("/policies?levelOfPolitics=2")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<List<PolicySummaryDto>>()
                .returnResult()
                .responseBody!!

        val provincialPolicyIds = provincialPolicies.map { it.id }
        assert(!provincialPolicyIds.contains(federalPolicy.id))
        assert(provincialPolicyIds.contains(provincialPolicy.id))
    }

    @Test
    fun `bookmark endpoints require correct permissions`() {
        val authId = "auth-perms-citizen"
        val citizenId = createCitizen(authId)
        setupPoliticalDetailsForCitizen(citizenId)

        val policyDto =
            CreatePolicyDto(
                description = "Permission Test Policy",
                coAuthorCitizenIds = emptyList(),
                LocalDateTime.now(),
            )

        val createdPolicy =
            webTestClient
                .mutateWith(
                    mockJwt()
                        .jwt { it.subject(authId) }
                        .authorities(SimpleGrantedAuthority("SCOPE_write:policies")),
                ).post()
                .uri("/policies")
                .bodyValue(policyDto)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<PolicyDto>()
                .returnResult()
                .responseBody!!

        // postBookmark requires SCOPE_write:self
        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(authId) }
                    .authorities(SimpleGrantedAuthority("SCOPE_read:self")), // Missing write:self
            ).post()
            .uri("/policies/${createdPolicy.id}/bookmark")
            .exchange()
            .expectStatus()
            .isForbidden

        // getBookmarks requires SCOPE_read:self AND SCOPE_read:policies
        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(authId) }
                    .authorities(SimpleGrantedAuthority("SCOPE_read:self")), // Missing read:policies
            ).get()
            .uri("/policies/bookmarks")
            .exchange()
            .expectStatus()
            .isForbidden

        // isBookmarked requires SCOPE_read:self AND SCOPE_read:policies
        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(authId) }
                    .authorities(SimpleGrantedAuthority("SCOPE_read:self")), // Missing read:policies
            ).get()
            .uri("/policies/${createdPolicy.id}/is-bookmarked")
            .exchange()
            .expectStatus()
            .isForbidden

        // deleteBookmark requires SCOPE_write:self AND SCOPE_read:policies
        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(authId) }
                    .authorities(SimpleGrantedAuthority("SCOPE_write:self")), // Missing read:policies
            ).delete()
            .uri("/policies/${createdPolicy.id}/bookmark")
            .exchange()
            .expectStatus()
            .isForbidden
    }
}
