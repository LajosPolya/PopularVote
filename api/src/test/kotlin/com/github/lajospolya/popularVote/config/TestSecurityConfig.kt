package com.github.lajospolya.popularVote.config

import com.github.lajospolya.popularVote.service.Auth0ManagementService
import org.mockito.Mockito
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder

@TestConfiguration
class TestSecurityConfig {
    @Bean
    @Primary
    fun testJwtDecoder(): ReactiveJwtDecoder = Mockito.mock(ReactiveJwtDecoder::class.java)

    @Bean
    @Primary
    fun auth0ManagementService(): Auth0ManagementService {
        val mock = Mockito.mock(Auth0ManagementService::class.java)
        Mockito.`when`(mock.addRoleToUser(Mockito.anyString(), Mockito.anyString())).thenReturn(
            reactor.core.publisher.Mono
                .empty(),
        )
        Mockito.`when`(mock.removeRoleFromUser(Mockito.anyString(), Mockito.anyString())).thenReturn(
            reactor.core.publisher.Mono
                .empty(),
        )
        return mock
    }
}
