package com.github.lajospolya.popularVote

import com.github.lajospolya.popularVote.config.TestSecurityConfig
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestSecurityConfig::class)
@Testcontainers
abstract class AbstractIntegrationTest(
    withReuse: Boolean = true,
) {
    init {
        mysqlContainer.withReuse(withReuse)
    }

    companion object {
        @Container
        @ServiceConnection
        @JvmStatic
        val mysqlContainer =
            MySQLContainer<Nothing>("mysql:8.1").apply {
                withDatabaseName("popular-vote")
            }
    }
}
