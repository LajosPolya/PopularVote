package com.github.lajospolya.popularVote.controller

import com.github.lajospolya.popularVote.AbstractIntegrationTest
import com.github.lajospolya.popularVote.dto.CitizenDto
import com.github.lajospolya.popularVote.dto.CreateCitizenDto
import com.github.lajospolya.popularVote.dto.CreatePolicyDto
import com.github.lajospolya.popularVote.dto.PolicyDto
import com.github.lajospolya.popularVote.dto.VoteDto
import com.github.lajospolya.popularVote.entity.PoliticalAffiliation
import com.github.lajospolya.popularVote.entity.PollSelectionCount
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody

@AutoConfigureWebTestClient
class VoteControllerIntegrationTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Test
    fun `create citizen, policy, and vote, then verify poll`() {
        // 1. Create Citizen
        val authId = "auth-voter-1"
        val createCitizenDto =
            CreateCitizenDto(
                givenName = "Voter",
                surname = "One",
                middleName = null,
                politicalAffiliation = PoliticalAffiliation.LIBERAL_PARTY_OF_CANADA,
            )
        val citizen =
            webTestClient
                .mutateWith(
                    mockJwt()
                        .jwt { it.subject(authId) }
                        .authorities(SimpleGrantedAuthority("SCOPE_write:citizens"))
                )
                .post()
                .uri("/citizens")
                .bodyValue(createCitizenDto)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<CitizenDto>()
                .returnResult()
                .responseBody!!

        // 2. Create Policy
        val createPolicyDto =
            CreatePolicyDto(
                description = "Test Policy for Voting",
            )
        val policy =
            webTestClient
                .mutateWith(
                    mockJwt()
                        .jwt { it.subject(authId) }
                        .authorities(SimpleGrantedAuthority("SCOPE_write:policies"))
                )
                .post()
                .uri("/policies")
                .bodyValue(createPolicyDto)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<PolicyDto>()
                .returnResult()
                .responseBody!!

        // 3. Get initial poll results to find a valid selectionId
        // Assuming there are some poll selections already in the database
        val initialPoll =
            webTestClient
                .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:polls")))
                .get()
                .uri("/polls/${policy.id}")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<List<PollSelectionCount>>()
                .returnResult()
                .responseBody!!

        // If the database is empty of selections, this test might fail.
        // But usually, there are default selections like "Yes", "No" etc.
        // We'll try to use selectionId 1 first, but better if we can find it.
        // However, PollSelectionCount doesn't have ID.
        // Wait, PollRepository.getPollForPolicy joins with poll_selection.
        // If I can't find selectionId from API, I might have to assume one or find another way.
        // Let's assume selectionId 1 exists for now, as it's a common default.
        val selectionId = 1L

        // 4. Vote
        val voteDto =
            VoteDto(
                policyId = policy.id,
                selectionId = selectionId,
            )

        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(authId) }
                    .authorities(SimpleGrantedAuthority("SCOPE_write:votes"))
            )
            .post()
            .uri("/votes")
            .bodyValue(voteDto)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody<Boolean>()
            .consumeWith { result ->
                assertTrue(result.responseBody == true)
            }

        // 5. Verify Poll
        val updatedPoll =
            webTestClient
                .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:polls")))
                .get()
                .uri("/polls/${policy.id}")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<List<PollSelectionCount>>()
                .returnResult()
                .responseBody!!

        // Verify that only the voted-for selection has a count of 1, and others have 0.
        // Since it's a new policy, all initial counts should have been 0.
        assertTrue(initialPoll.all { it.count == 0L }, "Initial poll counts should all be 0 for a new policy")

        val selectionWithVote = updatedPoll.find { it.selection == "approve" }
        assertNotNull(selectionWithVote, "The 'approve' selection should be present in the poll results")
        assertEquals(1L, selectionWithVote?.count, "The 'approve' selection should have a count of 1 after voting")

        val otherSelections = updatedPoll.filter { it.selection != "approve" }
        assertTrue(otherSelections.all { it.count == 0L }, "All other selections should still have count 0")
        assertEquals(1, updatedPoll.count { it.count == 1L }, "Exactly one selection should have a count of 1")
    }

    @Test
    fun `three citizens vote for unique selections`() {
        // 0. Create Publisher Citizen
        val publisherAuthId = "auth-publisher-unique"
        createCitizen(publisherAuthId)

        // 1. Create Policy
        val createPolicyDto =
            CreatePolicyDto(
                description = "Three Voter Policy",
            )
        val policy =
            webTestClient
                .mutateWith(
                    mockJwt()
                        .jwt { it.subject(publisherAuthId) }
                        .authorities(SimpleGrantedAuthority("SCOPE_write:policies"))
                )
                .post()
                .uri("/policies")
                .bodyValue(createPolicyDto)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<PolicyDto>()
                .returnResult()
                .responseBody!!

        // 2. Create Three Citizens
        val citizens =
            (1..3).map { i ->
                val authId = "auth-voter-unique-$i"
                val createCitizenDto =
                    CreateCitizenDto(
                        givenName = "Voter",
                        surname = "Number $i",
                        middleName = null,
                        politicalAffiliation = PoliticalAffiliation.LIBERAL_PARTY_OF_CANADA,
                    )
                webTestClient
                    .mutateWith(
                        mockJwt()
                            .jwt { it.subject(authId) }
                            .authorities(SimpleGrantedAuthority("SCOPE_write:citizens"))
                    )
                    .post()
                    .uri("/citizens")
                    .bodyValue(createCitizenDto)
                    .exchange()
                    .expectStatus()
                    .isOk
                    .expectBody<CitizenDto>()
                    .returnResult()
                    .responseBody!!
            }

        // 3. Vote with unique selections
        // 1L = "approve", 2L = "disapprove", 3L = "abstain"
        val votes =
            listOf(
                1L to "approve",
                2L to "disapprove",
                3L to "abstain",
            )

        votes.forEachIndexed { index, (selectionId, _) ->
            val authId = "auth-voter-unique-${index + 1}"
            val voteDto =
                VoteDto(
                    policyId = policy.id,
                    selectionId = selectionId,
                )
            webTestClient
                .mutateWith(
                    mockJwt()
                        .jwt { it.subject(authId) }
                        .authorities(SimpleGrantedAuthority("SCOPE_write:votes"))
                )
                .post()
                .uri("/votes")
                .bodyValue(voteDto)
                .exchange()
                .expectStatus()
                .isOk
        }

        // 4. Verify Poll Results
        val updatedPoll =
            webTestClient
                .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_read:polls")))
                .get()
                .uri("/polls/${policy.id}")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<List<PollSelectionCount>>()
                .returnResult()
                .responseBody!!

        // Verify that "approve", "disapprove", and "abstain" each have 1 vote
        votes.forEach { (_, selectionName) ->
            val selectionCount = updatedPoll.find { it.selection == selectionName }
            assertNotNull(selectionCount, "The '$selectionName' selection should be present in the poll results")
            assertEquals(1L, selectionCount?.count, "The '$selectionName' selection should have a count of 1")
        }

        // Verify that all other selections have 0 votes
        val selectionNamesWithVotes = votes.map { it.second }.toSet()
        val otherSelections = updatedPoll.filter { it.selection !in selectionNamesWithVotes }
        assertTrue(otherSelections.all { it.count == 0L }, "All other selections should still have count 0")

        // Exactly three selections should have a count of 1
        assertEquals(3, updatedPoll.count { it.count == 1L }, "Exactly three selections should have a count of 1")
    }

    @Test
    fun `check hasVoted before and after voting`() {
        val authId = "auth-has-voted"
        createCitizen(authId)

        val createPolicyDto = CreatePolicyDto(description = "Has Voted Policy")
        val policy =
            webTestClient
                .mutateWith(
                    mockJwt()
                        .jwt { it.subject(authId) }
                        .authorities(SimpleGrantedAuthority("SCOPE_write:policies"))
                )
                .post()
                .uri("/policies")
                .bodyValue(createPolicyDto)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<PolicyDto>()
                .returnResult()
                .responseBody!!

        // 1. Check before voting
        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(authId) }
                    .authorities(SimpleGrantedAuthority("SCOPE_read:votes"))
            )
            .get()
            .uri("/votes/policies/${policy.id}/has-voted")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody<Boolean>()
            .consumeWith { result ->
                assertFalse(result.responseBody == true)
            }

        // 2. Vote
        val voteDto = VoteDto(policyId = policy.id, selectionId = 1L)
        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(authId) }
                    .authorities(SimpleGrantedAuthority("SCOPE_write:votes"))
            )
            .post()
            .uri("/votes")
            .bodyValue(voteDto)
            .exchange()
            .expectStatus()
            .isOk

        // 3. Check after voting
        webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(authId) }
                    .authorities(SimpleGrantedAuthority("SCOPE_read:votes"))
            )
            .get()
            .uri("/votes/policies/${policy.id}/has-voted")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody<Boolean>()
            .consumeWith { result ->
                assertTrue(result.responseBody == true)
            }
    }

    private fun createCitizen(authId: String): Long {
        val createCitizenDto =
            CreateCitizenDto(
                givenName = "Publisher",
                surname = "Citizen",
                middleName = null,
                politicalAffiliation = PoliticalAffiliation.INDEPENDENT,
            )

        return webTestClient
            .mutateWith(
                mockJwt()
                    .jwt { it.subject(authId) }
                    .authorities(SimpleGrantedAuthority("SCOPE_write:citizens"))
            )
            .post()
            .uri("/citizens")
            .bodyValue(createCitizenDto)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody<CitizenDto>()
            .returnResult()
            .responseBody
            ?.id!!
    }
}
