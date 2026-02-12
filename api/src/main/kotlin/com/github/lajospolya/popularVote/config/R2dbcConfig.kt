package com.github.lajospolya.popularVote.config

import com.github.lajospolya.popularVote.entity.attributeConverter.RoleReadingConverter
import com.github.lajospolya.popularVote.entity.attributeConverter.RoleWritingConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions
import org.springframework.data.r2dbc.dialect.MySqlDialect

@Configuration
class R2dbcConfig {
    @Bean
    fun r2dbcCustomConversions(): R2dbcCustomConversions {
        val converters =
            listOf(
                RoleWritingConverter(),
                RoleReadingConverter(),
            )
        return R2dbcCustomConversions.of(MySqlDialect.INSTANCE, converters)
    }
}
