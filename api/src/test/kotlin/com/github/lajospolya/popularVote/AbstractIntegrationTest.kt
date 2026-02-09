package com.github.lajospolya.popularVote

import com.github.lajospolya.popularVote.config.TestSecurityConfig
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MySQLContainer

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestSecurityConfig::class)
abstract class AbstractIntegrationTest {
    companion object {
        @ServiceConnection
        @JvmStatic
        val mysqlContainer =
            MySQLContainer("mysql:8.1")
                .withDatabaseName("popular-vote")
                .withReuse(true)

        init {
            mysqlContainer.start()
        }

        @JvmStatic
        @DynamicPropertySource
        fun registerDynamicProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.flyway.url") { mysqlContainer.jdbcUrl }
            registry.add("spring.flyway.user") { mysqlContainer.username }
            registry.add("spring.flyway.password") { mysqlContainer.password }
            registry.add("spring.flyway.locations") { "filesystem:database/sql" }
            registry.add("spring.flyway.enabled") { "true" }
        }
    }
}
