package com.github.lajospolya.popularVote.controller

import com.github.lajospolya.popularVote.AbstractIntegrationTest
import com.github.lajospolya.popularVote.dto.CreateOpinionDto
import com.github.lajospolya.popularVote.dto.CreatePolicyDto
import com.github.lajospolya.popularVote.dto.PageDto
import com.github.lajospolya.popularVote.dto.PolicyDetailsDto
import com.github.lajospolya.popularVote.dto.PolicyDto
import com.github.lajospolya.popularVote.dto.PolicySummaryDto
import com.github.lajospolya.popularVote.dto.VoteDto
import com.github.lajospolya.popularVote.entity.ApprovalStatus
import com.github.lajospolya.popularVote.repository.CitizenPoliticalDetailsRepository
import com.github.lajospolya.popularVote.repository.CitizenRepository
import com.github.lajospolya.popularVote.service.Auth0ManagementService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
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

    @Autowired
    private lateinit var template: org.springframework.data.r2dbc.core.R2dbcEntityTemplate

    private val testUtils by lazy { TestUtils(webTestClient, auth0ManagementService, citizenPoliticalDetailsRepository) }

    @Test
    fun `create policy and then fetch it`() {
        val authId = "auth-policy-1"
        val citizenId = testUtils.createCitizen(authId)
        // Set political details for the citizen so they can create a policy
        testUtils.setupPoliticalDetailsForCitizen(citizenId)

        val createPolicyDto =
            CreatePolicyDto(
                title = "Test Policy Title",
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
        assertEquals(createPolicyDto.title, createdPolicy?.title)
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
        assertEquals(createPolicyDto.title, fetchedPolicy?.title)
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
                title = "Delete Policy",
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
                title = "Details Policy",
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
        assertEquals(createPolicyDto.title, fetchedDetails?.title)
        assertEquals(createPolicyDto.description, fetchedDetails?.description)
        assertEquals("Publisher Citizen", fetchedDetails?.publisherName)
        assertEquals(1, fetchedDetails?.publisherPoliticalAffiliationId)
        assertNotNull(fetchedDetails?.opinions)
        assertEquals(0L, fetchedDetails?.approvedVotes)
        assertEquals(0L, fetchedDetails?.deniedVotes)
        assertEquals(0L, fetchedDetails?.abstainedVotes)
    }

    @Test
    fun `create policy, vote, and verify details has correct vote counts`() {
        // 1. Setup publisher
        val publisherAuthId = "auth-policy-details-publisher"
        val publisherId = createCitizen(publisherAuthId)
        declareSelfPolitician(publisherAuthId, 1)
        verifyPolitician(publisherId)

        // 2. Create Policy
        val createPolicyDto =
            CreatePolicyDto(
                title = "Vote Counts Policy",
                description = "Policy for Vote Counts Test",
                coAuthorCitizenIds = emptyList(),
                LocalDateTime.now().plusDays(1),
            )

        val createdPolicy =
            webTestClient
                .mutateWith(mockJwt().jwt { it.subject(publisherAuthId) }.authorities(SimpleGrantedAuthority("SCOPE_write:policies")))
                .post()
                .uri("/policies")
                .bodyValue(createPolicyDto)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<PolicyDto>()
                .returnResult()
                .responseBody!!

        // 3. Setup voters and vote
        val voters =
            listOf(
                "voter-approve" to 1L, // approve
                "voter-disapprove" to 2L, // disapprove
                "voter-abstain" to 3L, // abstain
                "voter-approve-2" to 1L, // approve
            )

        voters.forEach { (authId, selectionId) ->
            createCitizen(authId, "Voter", authId)
            val voteDto = VoteDto(policyId = createdPolicy.id, selectionId = selectionId)
            webTestClient
                .mutateWith(mockJwt().jwt { it.subject(authId) }.authorities(SimpleGrantedAuthority("SCOPE_write:votes")))
                .post()
                .uri("/votes")
                .bodyValue(voteDto)
                .exchange()
                .expectStatus()
                .isOk
        }

        // Close policy to have approval status
        template.databaseClient
            .sql("UPDATE policy SET close_date = :closeDate WHERE id = :id")
            .bind("closeDate", LocalDateTime.now().minusDays(1))
            .bind("id", createdPolicy.id)
            .fetch()
            .rowsUpdated()
            .block()

        // 4. Fetch details and verify counts
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
                .responseBody!!

        assertEquals(2L, fetchedDetails.approvedVotes)
        assertEquals(1L, fetchedDetails.deniedVotes)
        assertEquals(1L, fetchedDetails.abstainedVotes)
        assertEquals(ApprovalStatus.APPROVED, fetchedDetails.approvalStatus)

        // 5. Add more disapprove votes to flip status
        val moreVoters =
            listOf(
                "voter-disapprove-2" to 2L,
                "voter-disapprove-3" to 2L,
            )

        // Re-open policy to allow voting
        template.databaseClient
            .sql("UPDATE policy SET close_date = :closeDate WHERE id = :id")
            .bind("closeDate", LocalDateTime.now().plusDays(1))
            .bind("id", createdPolicy.id)
            .fetch()
            .rowsUpdated()
            .block()

        moreVoters.forEach { (authId, selectionId) ->
            createCitizen(authId, "Voter", authId)
            val voteDto = VoteDto(policyId = createdPolicy.id, selectionId = selectionId)
            webTestClient
                .mutateWith(mockJwt().jwt { it.subject(authId) }.authorities(SimpleGrantedAuthority("SCOPE_write:votes")))
                .post()
                .uri("/votes")
                .bodyValue(voteDto)
                .exchange()
                .expectStatus()
                .isOk
        }

        // Close policy again to have approval status
        template.databaseClient
            .sql("UPDATE policy SET close_date = :closeDate WHERE id = :id")
            .bind("closeDate", LocalDateTime.now().minusDays(1))
            .bind("id", createdPolicy.id)
            .fetch()
            .rowsUpdated()
            .block()

        // 6. Fetch details again and verify status is DENIED
        val updatedDetails =
            webTestClient
                .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:policies")))
                .get()
                .uri("/policies/${createdPolicy.id}/details")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<PolicyDetailsDto>()
                .returnResult()
                .responseBody!!

        assertEquals(2L, updatedDetails.approvedVotes)
        assertEquals(3L, updatedDetails.deniedVotes)
        assertEquals(ApprovalStatus.DENIED, updatedDetails.approvalStatus)
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
    fun `verify pagination and sorting of policies`() {
        val authId = "auth-pagination-test"
        val citizenId = createCitizen(authId)
        setupPoliticalDetailsForCitizen(citizenId)

        // Create 3 policies with different creation dates
        val now = LocalDateTime.now().withNano(0)
        val policy1 = CreatePolicyDto("P1", "Policy 1", emptyList(), now.plusDays(3), now.minusDays(2))
        val policy2 = CreatePolicyDto("P2", "Policy 2", emptyList(), now.plusDays(3), now.minusDays(1))
        val policy3 = CreatePolicyDto("P3", "Policy 3", emptyList(), now.plusDays(3), now)

        listOf(policy1, policy2, policy3).forEach { dto ->
            webTestClient
                .mutateWith(mockJwt().jwt { it.subject(authId) }.authorities(SimpleGrantedAuthority("SCOPE_write:policies")))
                .post()
                .uri("/policies")
                .bodyValue(dto)
                .exchange()
                .expectStatus()
                .isOk
        }

        // Fetch page 0, size 2
        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(authId) }
                    .authorities(SimpleGrantedAuthority("SCOPE_read:policies")),
            ).get()
            .uri("/policies?page=0&size=2")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody<PageDto<PolicySummaryDto>>()
            .consumeWith { result ->
                val page = result.responseBody!!
                // Use >= since other tests create policies
                assert(page.content.size >= 2)
                assert(page.totalElements >= 3)
                assert(page.totalPages >= 2)
                assertEquals(0, page.pageNumber)
                // Sorting should be DESC, so policy 3 then policy 2
                // We find them in the content since there might be other policies from other tests
                val descriptions = page.content.map { it.description }
                // We check if our policies are present in the expected order relative to each other
                // but they might not be at index 0 and 1 if there are newer ones
                val policy3Index = descriptions.indexOf("Policy 3")
                val policy2Index = descriptions.indexOf("Policy 2")
                if (policy3Index != -1 && policy2Index != -1) {
                    assert(policy3Index < policy2Index)
                }
            }

        // Fetch page 1, size 2
        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(authId) }
                    .authorities(SimpleGrantedAuthority("SCOPE_read:policies")),
            ).get()
            .uri("/policies?page=1&size=2")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody<PageDto<PolicySummaryDto>>()
            .consumeWith { result ->
                val page = result.responseBody!!
                val descriptions = page.content.map { it.description }
                // Policy 1 should be here if no more than 1 policy from other tests is newer than Policy 2
                assert(descriptions.contains("Policy 1") || page.totalElements > 3)
            }
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
                .expectBody<PageDto<PolicySummaryDto>>()
                .returnResult()
                .responseBody
                ?.totalElements ?: 0

        val policy1 =
            CreatePolicyDto(
                title = "P1",
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
            .uri("/policies?size=50")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody<PageDto<PolicySummaryDto>>()
            .consumeWith { result ->
                val policies = result.responseBody!!.content
                val policy = policies.find { it.id == createdPolicy.id }
                assertNotNull(
                    policy,
                    "Could not find policy ${createdPolicy.id} in page results. Page content IDs: ${policies.map { it.id }}",
                )
                assertEquals(false, policy?.isBookmarked)
            }

        val policy2 =
            CreatePolicyDto(
                title = "P2",
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
            .expectBody<PageDto<PolicySummaryDto>>()
            .consumeWith { result ->
                assertEquals(initialCount + 2, result.responseBody?.totalElements)
            }
    }

    @Test
    fun `create policy, add opinion, and fetch details`() {
        val policyAuthId = "auth-policy-opinion-details"
        val publisherId = createCitizen(policyAuthId)
        setupPoliticalDetailsForCitizen(publisherId)
        val createPolicyDto =
            CreatePolicyDto(
                title = "Policy Title",
                description = "Policy with Opinion",
                coAuthorCitizenIds = emptyList(),
                LocalDateTime.now(),
            )
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

    private fun setupPoliticalDetailsForCitizen(citizenId: Long) {
        testUtils.setupPoliticalDetailsForCitizen(citizenId)
    }

    private fun verifyPolitician(citizenId: Long) {
        testUtils.verifyPolitician(citizenId)
    }

    private fun declareSelfPolitician(
        authId: String,
        levelOfPoliticsId: Int,
    ) {
        testUtils.declareSelfPolitician(authId, levelOfPoliticsId)
    }

    private fun createCitizen(
        authId: String,
        givenName: String = "Publisher",
        surname: String = "Citizen",
    ): Long = testUtils.createCitizen(authId, givenName, surname)

    @Test
    fun `create policy with co-authors and fetch policy and details`() {
        val publisherAuth = "auth-policy-coauthors-publisher"
        val publisherId = testUtils.createCitizen(publisherAuth)
        testUtils.setupPoliticalDetailsForCitizen(publisherId)

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
                title = "CoAuthors Policy",
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
                title = "P1",
                description = "Policy 1",
                coAuthorCitizenIds = emptyList(),
                LocalDateTime.now(),
            )
        val createPolicyDto2 =
            CreatePolicyDto(
                title = "P2",
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
                title = "Bookmarked Title",
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

        // Close policy to have approval status
        template.databaseClient
            .sql("UPDATE policy SET close_date = :closeDate WHERE id = :id")
            .bind("closeDate", LocalDateTime.now().minusDays(1))
            .bind("id", createdPolicy.id)
            .fetch()
            .rowsUpdated()
            .block()

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
    fun `verify stable sorting with page size 5`() {
        val authId = "auth-page-size-5-test"
        val citizenId = createCitizen(authId)
        setupPoliticalDetailsForCitizen(citizenId)

        // Create 12 policies with different creation dates
        val now = LocalDateTime.now().withNano(0)
        val createdIds =
            (1..12).map { i ->
                val dto = CreatePolicyDto("T $i", "Policy $i", emptyList(), now.plusDays(3), now.plusMinutes(i.toLong()))
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
                    .id
            }

        // Fetch page 0, size 5
        val page0 =
            webTestClient
                .mutateWith(
                    mockJwt()
                        .jwt { it.subject(authId) }
                        .authorities(SimpleGrantedAuthority("SCOPE_read:policies")),
                ).get()
                .uri("/policies?page=0&size=5")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<PageDto<PolicySummaryDto>>()
                .returnResult()
                .responseBody!!

        // Fetch page 1, size 5
        val page1 =
            webTestClient
                .mutateWith(
                    mockJwt()
                        .jwt { it.subject(authId) }
                        .authorities(SimpleGrantedAuthority("SCOPE_read:policies")),
                ).get()
                .uri("/policies?page=1&size=5")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<PageDto<PolicySummaryDto>>()
                .returnResult()
                .responseBody!!

        // Fetch page 2, size 5
        val page2 =
            webTestClient
                .mutateWith(
                    mockJwt()
                        .jwt { it.subject(authId) }
                        .authorities(SimpleGrantedAuthority("SCOPE_read:policies")),
                ).get()
                .uri("/policies?page=2&size=5")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<PageDto<PolicySummaryDto>>()
                .returnResult()
                .responseBody!!

        val allFetchedIds = (page0.content + page1.content + page2.content).map { it.id }
        val relevantFetchedIds = allFetchedIds.filter { it in createdIds }
        val expectedOrder = createdIds.reversed()

        assertEquals(expectedOrder, relevantFetchedIds)
    }

    @Test
    fun `bookmark endpoints require correct permissions`() {
        val authId = "auth-perms-citizen"
        val citizenId = createCitizen(authId)
        setupPoliticalDetailsForCitizen(citizenId)

        val policyDto =
            CreatePolicyDto(
                title = "Perm Test",
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
