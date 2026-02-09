package com.github.lajospolya.popularVote.service

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.web.reactive.function.client.WebClient
import reactor.test.StepVerifier

class Auth0ManagementServiceTest {
    private lateinit var mockWebServer: MockWebServer
    private lateinit var auth0ManagementService: Auth0ManagementService

    @BeforeEach
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val webClient = WebClient.builder()
            .baseUrl(mockWebServer.url("/").toString())
            .build()

        auth0ManagementService = Auth0ManagementService(webClient)
        ReflectionTestUtils.setField(auth0ManagementService, "managementAudience", mockWebServer.url("/").toString())
    }

    @AfterEach
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `addRoleToUser should call Auth0 Management API correctly`() {
        val userId = "user_123"
        val roleId = "role_456"

        mockWebServer.enqueue(MockResponse().setResponseCode(204))

        val result = auth0ManagementService.addRoleToUser(userId, roleId)

        StepVerifier.create(result)
            .verifyComplete()

        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("POST", recordedRequest.method)
        assertEquals("/users/$userId/roles", recordedRequest.path)
        assertEquals("{\"roles\":[\"$roleId\"]}", recordedRequest.body.readUtf8())
    }
}
